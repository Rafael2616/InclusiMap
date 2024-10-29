package com.rafael.inclusimap.feature.auth.presentation.viewmodel

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.api.services.drive.model.File
import com.rafael.inclusimap.core.domain.model.AccessibleLocalMarker
import com.rafael.inclusimap.core.domain.model.DeleteProcess
import com.rafael.inclusimap.core.domain.model.util.extractPlaceUserEmail
import com.rafael.inclusimap.core.domain.model.util.extractUserEmail
import com.rafael.inclusimap.core.domain.network.onError
import com.rafael.inclusimap.core.domain.network.onSuccess
import com.rafael.inclusimap.core.domain.util.Constants.INCLUSIMAP_IMAGE_FOLDER_ID
import com.rafael.inclusimap.core.domain.util.Constants.INCLUSIMAP_PARAGOMINAS_PLACE_DATA_FOLDER_ID
import com.rafael.inclusimap.core.domain.util.Constants.INCLUSIMAP_USERS_FOLDER_ID
import com.rafael.inclusimap.core.services.GoogleDriveService
import com.rafael.inclusimap.feature.auth.domain.model.LoginEntity
import com.rafael.inclusimap.feature.auth.domain.model.LoginEvent
import com.rafael.inclusimap.feature.auth.domain.model.LoginState
import com.rafael.inclusimap.feature.auth.domain.model.RegisteredUser
import com.rafael.inclusimap.feature.auth.domain.model.User
import com.rafael.inclusimap.feature.auth.domain.repository.LoginRepository
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import kotlin.coroutines.resume
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class LoginViewModel(
    private val repository: LoginRepository,
) : ViewModel() {
    private val driveService: GoogleDriveService = GoogleDriveService()
    private val _state = MutableStateFlow(LoginState())
    val state = _state.asStateFlow()
    private val json = Json {
        ignoreUnknownKeys = true
        prettyPrint = true
    }

    init {
        viewModelScope.launch(Dispatchers.IO) {
            val loginData = repository.getLoginInfo(1) ?: LoginEntity.getDefault()
            _state.update { it.copy(isLoggedIn = loginData.isLoggedIn) }
            if (!loginData.userId.isNullOrEmpty()) {
                _state.update {
                    it.copy(
                        user = User(
                            id = loginData.userId!!,
                            name = loginData.userName!!,
                            email = loginData.userEmail!!,
                            password = loginData.userPassword!!,
                            showProfilePictureOptedIn = loginData.showProfilePictureOptedIn,
                        ),
                        userProfilePicture = loginData.profilePicture?.let { picture ->
                            BitmapFactory.decodeByteArray(
                                picture,
                                0,
                                picture.size,
                            )?.asImageBitmap()
                        },
                        userPathID = loginData.userPathID,
                    )
                }
            }
        }
        checkUserExists()
    }

    fun onEvent(event: LoginEvent) {
        when (event) {
            is LoginEvent.OnLogin -> login(event.registeredUser)
            is LoginEvent.OnRegisterNewUser -> registerNewUser(event.user)
            LoginEvent.OnLogout -> logout()
            is LoginEvent.UpdatePassword -> updatePassword(event.password)
            is LoginEvent.SetIsNewUser -> _state.update {
                it.copy(isNewUser = event.isNewUser)
            }

            is LoginEvent.SetIsPasswordChanged -> _state.update {
                it.copy(isPasswordChanged = event.isChanged)
            }

            is LoginEvent.DeleteAccount -> deleteAccount(event.keepContributions)
            is LoginEvent.OnAddEditUserProfilePicture -> addEditProfilePicture(event.image)
            LoginEvent.OnRemoveUserProfilePicture -> onDeleteProfilePicture()
            is LoginEvent.UpdateUserName -> updateUserName(event.name)
            is LoginEvent.OnAllowPictureOptedIn -> allowUserToSeeProfilePicture(event.value)
        }
    }

    @OptIn(ExperimentalUuidApi::class)
    private fun registerNewUser(newUser: User) {
        _state.update {
            it.copy(
                isRegistering = true,
                networkError = false,
                userAlreadyRegistered = false,
            )
        }
        val userID = Uuid.random().toString()
        val user = User(
            id = userID,
            name = newUser.name,
            email = newUser.email,
            password = newUser.password,
            showProfilePictureOptedIn = newUser.showProfilePictureOptedIn,
        )
        viewModelScope.launch(Dispatchers.IO) {
            driveService.listFiles(INCLUSIMAP_USERS_FOLDER_ID)
                .onSuccess { result ->
                    val isUserRegistered =
                        result.any { userFile -> userFile.name == newUser.email }
                    _state.update {
                        it.copy(userAlreadyRegistered = isUserRegistered)
                    }
                }
                .onError {
                    _state.update {
                        it.copy(
                            isRegistering = false,
                            networkError = true,
                        )
                    }
                    return@launch
                }

            if (_state.value.userAlreadyRegistered) {
                _state.update { it.copy(isRegistering = false) }
                return@launch
            }

            async {
                _state.update {
                    it.copy(
                        userPathID = driveService.createFolder(
                            newUser.email,
                            INCLUSIMAP_USERS_FOLDER_ID,
                        ),
                    )
                }
            }.await()
            val userString = json.encodeToString<User>(user)
            async {
                driveService.createFile(
                    "${newUser.email}.json",
                    userString,
                    state.value.userPathID,
                )
                driveService.listFiles(state.value.userPathID ?: return@async).onSuccess {
                    it.find { file -> file.name == "contributions.json" }
                        ?: driveService.createFile(
                            "contributions.json",
                            "[]",
                            state.value.userPathID,
                        )
                }
            }.await()
            // Artificial Delay
            delay(500L)
        }.invokeOnCompletion {
            if (!_state.value.userAlreadyRegistered && !_state.value.networkError) {
                viewModelScope.launch(Dispatchers.IO) {
                    val loginData = repository.getLoginInfo(1) ?: LoginEntity.getDefault()
                    loginData.userId = user.id
                    loginData.userName = user.name
                    loginData.userEmail = user.email
                    loginData.userPassword = user.password
                    loginData.isLoggedIn = true
                    loginData.showProfilePictureOptedIn = true
                    loginData.userPathID = _state.value.userPathID

                    repository.updateLoginInfo(loginData)

                    _state.update {
                        it.copy(
                            user = user,
                            isLoggedIn = true,
                        )
                    }
                }
            }
            _state.update {
                it.copy(isRegistering = false)
            }
        }
    }

    private fun login(registeredUser: RegisteredUser) {
        _state.update {
            it.copy(
                isRegistering = true,
                isPasswordCorrect = true,
                networkError = false,
            )
        }
        viewModelScope.launch(Dispatchers.IO) {
            async {
                driveService.listFiles(INCLUSIMAP_USERS_FOLDER_ID).onSuccess { usersPaths ->
                    usersPaths.find { userPath -> userPath.name == registeredUser.email }
                        .also { userPath ->
                            if (userPath == null) {
                                _state.update {
                                    it.copy(
                                        isRegistering = false,
                                        userAlreadyRegistered = false,
                                    )
                                }
                            }
                            driveService.listFiles(userPath?.id ?: return@async).onSuccess {
                                val userExists =
                                    it.find { userLoginFile -> userLoginFile.name == "${registeredUser.email}.json" }
                                _state.update { it.copy(userAlreadyRegistered = userExists != null) }
                            }.onError {
                                _state.update {
                                    it.copy(
                                        isRegistering = false,
                                        userAlreadyRegistered = false,
                                    )
                                }
                            }
                        }
                }.onError {
                    _state.update {
                        it.copy(
                            isRegistering = false,
                            networkError = true,
                        )
                    }
                    return@async
                }
            }.await()

            if (!_state.value.userAlreadyRegistered) {
                println("User not found")
                return@launch
            }

            async {
                driveService.listFiles(INCLUSIMAP_USERS_FOLDER_ID).onSuccess { users ->
                    users.find { userPath -> userPath.name == registeredUser.email }
                        .also { user ->
                            println("User path found for: ${user?.name}")
                            driveService.listFiles(user?.id ?: return@async)
                                .onSuccess { userFiles ->
                                    userFiles.find { userFile ->
                                        userFile.name == "${registeredUser.email}.json"
                                    }.also { userLoginFile ->
                                        println("User login file found for: ${user.name}")
                                        val userLoginFileContent = userLoginFile?.let { file ->
                                            driveService.getFileContent(file.id)
                                                ?.decodeToString()
                                        }
                                        if (userLoginFileContent == null) {
                                            _state.update {
                                                it.copy(
                                                    isRegistering = false,
                                                    networkError = true,
                                                )
                                            }
                                            println("User file content is null")
                                            return@async
                                        }
                                        val userObj =
                                            json.decodeFromString<User>(userLoginFileContent)
                                        println("User object: $userObj")
                                        if (userObj.password != registeredUser.password) {
                                            _state.update {
                                                it.copy(isPasswordCorrect = false)
                                            }
                                            println("Password is incorrect")
                                            return@async
                                        }
                                        _state.update { it.copy(userPathID = user.id) }
                                        println("Working on user path: ${_state.value.userPathID}")
                                        val userImageByteArray = ByteArrayOutputStream()
                                        async {
                                            downloadUserProfilePicture(userObj.email)?.asAndroidBitmap()
                                                ?.compress(
                                                    Bitmap.CompressFormat.JPEG,
                                                    100,
                                                    userImageByteArray,
                                                )
                                        }.await()
                                        val loginData =
                                            repository.getLoginInfo(1)
                                                ?: LoginEntity.getDefault()
                                        loginData.userId = userObj.id
                                        loginData.userName = userObj.name
                                        loginData.userEmail = userObj.email
                                        loginData.userPassword = userObj.password
                                        loginData.userPathID = user.id
                                        loginData.isLoggedIn = true
                                        loginData.showProfilePictureOptedIn =
                                            userObj.showProfilePictureOptedIn
                                        loginData.profilePicture =
                                            userImageByteArray.toByteArray()
                                        repository.updateLoginInfo(loginData)

                                        _state.update {
                                            it.copy(
                                                isPasswordCorrect = true,
                                                user = User(
                                                    id = userObj.id,
                                                    name = userObj.name,
                                                    email = userObj.email,
                                                    password = userObj.password,
                                                    showProfilePictureOptedIn = userObj.showProfilePictureOptedIn,
                                                ),
                                                userProfilePicture = userImageByteArray.run {
                                                    BitmapFactory.decodeByteArray(
                                                        userImageByteArray.toByteArray(),
                                                        0,
                                                        userImageByteArray.toByteArray().size,
                                                    )?.asImageBitmap()
                                                },
                                            )
                                        }
                                    }
                                }.onError {
                                    _state.update {
                                        it.copy(
                                            isRegistering = false,
                                            networkError = true,
                                        )
                                    }
                                    return@async
                                }
                        }
                }
            }.await()
        }.invokeOnCompletion {
            println("Login completed")
            if (_state.value.userAlreadyRegistered && _state.value.isPasswordCorrect && !_state.value.networkError && _state.value.user != null) {
                _state.update {
                    it.copy(
                        isLoggedIn = true,
                        isPasswordCorrect = true,
                    )
                }
            }
            _state.update {
                it.copy(isRegistering = false)
            }
        }
    }

    private fun logout() {
        _state.update {
            it.copy(isLoginOut = true)
        }
        viewModelScope.launch(Dispatchers.IO) {
            val loginData = LoginEntity.getDefault()
            repository.updateLoginInfo(loginData)
        }.invokeOnCompletion {
            viewModelScope.launch(Dispatchers.IO) {
                delay(2000L)
                _state.update {
                    it.copy(
                        user = null,
                        userProfilePicture = null,
                        isLoggedIn = false,
                        isLoginOut = false,
                        userPathID = null,
                    )
                }
            }
        }
    }

    private fun updatePassword(password: String) {
        _state.update {
            it.copy(
                isUpdatingPassword = true,
                isPasswordChanged = false,
                isSamePassword = false,
                networkError = false,
            )
        }
        // Update the password in local database
        viewModelScope.launch(Dispatchers.IO) {
            val loginData = repository.getLoginInfo(1) ?: LoginEntity.getDefault()
            loginData.userPassword = password
            repository.updateLoginInfo(loginData)
        }
        // Update the password in Google Drive
        viewModelScope.launch(Dispatchers.IO) {
            async {
                driveService.listFiles(state.value.userPathID ?: return@async)
                    .onSuccess { result ->
                        result.find { userFile -> userFile.name == state.value.user?.email + ".json" }
                            .also { userLoginFile ->
                                val userLoginFileContent = userLoginFile?.id?.let { fileId ->
                                    driveService.getFileContent(fileId)?.decodeToString()
                                }

                                if (userLoginFileContent == null) {
                                    println("User file content is null")
                                    return@async
                                }
                                val userObj =
                                    json.decodeFromString<User>(userLoginFileContent)
                                if (userObj.password == password) {
                                    _state.update {
                                        it.copy(
                                            isSamePassword = true,
                                            isUpdatingPassword = false,
                                        )
                                    }
                                    return@async
                                }
                                userObj.password = password
                                driveService.updateFile(
                                    userLoginFile.id,
                                    userLoginFile.name,
                                    json.encodeToString(userObj).byteInputStream(),
                                )
                            }
                    }.onError {
                        _state.update {
                            it.copy(
                                isUpdatingPassword = false,
                                networkError = true,
                            )
                        }
                    }
            }.await()
        }.invokeOnCompletion {
            if (_state.value.isSamePassword) return@invokeOnCompletion
            if (_state.value.networkError) return@invokeOnCompletion

            // Update the password in the state
            _state.update {
                it.copy(
                    user = it.user?.copy(password = password),
                    isUpdatingPassword = false,
                    isPasswordChanged = true,
                )
            }
        }
    }

    private fun deleteAccount(keepContributions: Boolean) {
        _state.update {
            it.copy(
                isDeletingAccount = true,
                isAccountDeleted = false,
                deleteStep = DeleteProcess.NO_OP,
                networkError = false,
            )
        }
        viewModelScope.launch(Dispatchers.IO) {
            delay(300L)
            // Delete user info from Google Drive
            _state.update { it.copy(deleteStep = DeleteProcess.DELETING_USER_INFO) }
            async {
                driveService.listFiles(INCLUSIMAP_USERS_FOLDER_ID).onSuccess { result ->
                    result.find { userFile ->
                        userFile.name == _state.value.user?.email
                    }?.let { user ->
                        copyUserInfoToPosthumousVerification(user).invokeOnCompletion {
                            viewModelScope.launch(Dispatchers.IO) {
                                driveService.listFiles(user.id).onSuccess {
                                    it.forEach { file ->
                                        if (file.name == "contributions.json") return@forEach
                                        driveService.deleteFile(file.id)
                                    }
                                }
                            }
                        }
                    }
                }.onError {
                    _state.update {
                        it.copy(
                            deleteStep = DeleteProcess.ERROR,
                            isDeletingAccount = false,
                            isAccountDeleted = false,
                            networkError = true,
                        )
                    }
                    return@async
                }
            }.invokeOnCompletion {
                if (it != null) {
                    _state.update {
                        it.copy(
                            deleteStep = DeleteProcess.ERROR,
                            isDeletingAccount = false,
                            isAccountDeleted = false,
                        )
                    }
                } else {
                    if (keepContributions && !_state.value.networkError) {
                        _state.update {
                            it.copy(
                                deleteStep = DeleteProcess.SUCCESS,
                                isDeletingAccount = false,
                                isAccountDeleted = true,
                            )
                        }
                    }
                }
            }

            if (keepContributions) return@launch

            // Delete user posted places
            _state.update { it.copy(deleteStep = DeleteProcess.DELETING_USER_LOCAL_MARKERS) }
            driveService.listFiles(INCLUSIMAP_PARAGOMINAS_PLACE_DATA_FOLDER_ID)
                .onSuccess { result ->
                    result.map { place ->
                        async {
                            if (place.name.extractPlaceUserEmail() != _state.value.user?.email) {
                                return@async
                            } else {
                                println("Deleting place: ${place.name} - ${place.id} posted by user ${_state.value.user?.email}")
                            }
                            driveService.deleteFile(place.id)
                        }
                    }.awaitAll()
                }.onError {
                    _state.update {
                        it.copy(
                            deleteStep = DeleteProcess.ERROR,
                            isDeletingAccount = false,
                            isAccountDeleted = false,
                            networkError = true,
                        )
                    }
                    return@launch
                }
        }.invokeOnCompletion {
            if (keepContributions && !_state.value.networkError) {
                logout()
                return@invokeOnCompletion
            }
            viewModelScope.launch(Dispatchers.IO) {
                async {
                    // Delete user posted images
                    _state.update { it.copy(deleteStep = DeleteProcess.DELETING_USER_IMAGES) }
                    driveService.listFiles(INCLUSIMAP_IMAGE_FOLDER_ID).onSuccess { places ->
                        places.forEach { place ->
                            if (place.name.extractUserEmail() == _state.value.user?.email) {
                                driveService.deleteFile(place.id)
                                println("Deleting image folder ${place.name} - ${place.id} posted by user ${_state.value.user?.email}")
                                return@forEach
                            }
                            driveService.listFiles(place.id).onSuccess { images ->
                                images.filter { image ->
                                    image.name.extractUserEmail() == _state.value.user?.email
                                }.also { userImages ->
                                    userImages.forEach {
                                        println("Deleting file: ${it.name} - ${it.id} posted by user ${_state.value.user?.email}")
                                        async { driveService.deleteFile(it.id) }.await()
                                    }
                                }
                            }
                        }
                    }.onError {
                        _state.update {
                            it.copy(
                                deleteStep = DeleteProcess.ERROR,
                                isDeletingAccount = false,
                                isAccountDeleted = false,
                                networkError = true,
                            )
                        }
                        return@async
                    }
                }.invokeOnCompletion {
                    if (it != null) {
                        _state.update {
                            it.copy(
                                deleteStep = DeleteProcess.ERROR,
                                isDeletingAccount = false,
                                isAccountDeleted = false,
                            )
                        }
                    }

                    async {
                        // Delete user comments
                        _state.update { it.copy(deleteStep = DeleteProcess.DELETING_USER_COMMENTS) }
                        driveService.listFiles(INCLUSIMAP_PARAGOMINAS_PLACE_DATA_FOLDER_ID)
                            .onSuccess { result ->
                                result.map { place ->
                                    async {
                                        val placeContentString =
                                            driveService.getFileContent(place.id)
                                                ?.decodeToString()
                                        val placeContent =
                                            json.decodeFromString<AccessibleLocalMarker>(
                                                placeContentString ?: "",
                                            )

                                        val placeWithoutUserComments = placeContent.copy(
                                            comments = placeContent.comments.filterNot { it.email == _state.value.user?.email },
                                        )

                                        if (placeWithoutUserComments.comments != placeContent.comments) {
                                            println("Deleting comments in place ${place.name}")
                                            val updatedPlace =
                                                json.encodeToString<AccessibleLocalMarker>(
                                                    placeWithoutUserComments,
                                                )
                                            driveService.updateFile(
                                                place.id,
                                                place.id + "_" + placeContent.authorEmail + ".json",
                                                updatedPlace.toByteArray().inputStream(),
                                            )
                                        }
                                    }
                                }.awaitAll()
                            }.onError {
                                _state.update {
                                    it.copy(
                                        deleteStep = DeleteProcess.ERROR,
                                        isDeletingAccount = false,
                                        isAccountDeleted = false,
                                        networkError = true,
                                    )
                                }
                                return@async
                            }
                    }.invokeOnCompletion {
                        // Delete user folder
                        viewModelScope.launch(Dispatchers.IO) {
                            if (keepContributions) return@launch
                            driveService.deleteFile(state.value.userPathID ?: return@launch)

                            if (!_state.value.networkError) {
                                _state.update {
                                    it.copy(
                                        deleteStep = DeleteProcess.ERROR,
                                        isDeletingAccount = false,
                                        isAccountDeleted = false,
                                    )
                                }
                            }
                        }
                    }
                }
            }.invokeOnCompletion {
                viewModelScope.launch(Dispatchers.IO) {
                    if (it != null) {
                        _state.update {
                            it.copy(
                                deleteStep = DeleteProcess.ERROR,
                                isDeletingAccount = false,
                                isAccountDeleted = false,
                                isLoginOut = false,
                            )
                        }
                    } else if (!_state.value.networkError) {
                        _state.update {
                            it.copy(
                                isDeletingAccount = false,
                                deleteStep = DeleteProcess.SUCCESS,
                                isAccountDeleted = true,
                                isLoginOut = true,
                            )
                        }
                    }
                }.invokeOnCompletion {
                    if (!_state.value.networkError) {
                        logout()
                    }
                }
            }
        }
    }

    private fun checkUserExists() {
        viewModelScope.launch(Dispatchers.IO) {
            driveService.listFiles(INCLUSIMAP_USERS_FOLDER_ID).onSuccess { result ->
                result.find { it.name == _state.value.user?.email }.also { userExists ->
                    _state.update { it.copy(isLoggedIn = userExists != null) }
                    val loginData = repository.getLoginInfo(1) ?: LoginEntity.getDefault()
                    loginData.isLoggedIn = userExists != null
                    loginData.userPathID = userExists?.id
                    if (userExists == null) {
                        loginData.userId = null
                        loginData.userName = null
                        loginData.userEmail = null
                        loginData.userPassword = null
                        loginData.showProfilePictureOptedIn = true
                        loginData.profilePicture = null
                        loginData.userPathID = null
                    } else {
                        _state.update { it.copy(userPathID = userExists.id) }
                        println("Working on user path: ${_state.value.userPathID}")
                    }
                    repository.updateLoginInfo(loginData)
                }
            }
        }.invokeOnCompletion {
            // Download user profile picture
            viewModelScope.launch(Dispatchers.IO) {
                val picture = downloadUserProfilePicture(state.value.user?.email)
                if (picture == state.value.userProfilePicture) return@launch

                _state.update { it.copy(userProfilePicture = picture) }

                val imageByteArrayOutputStream = ByteArrayOutputStream()
                picture?.asAndroidBitmap()
                    ?.compress(Bitmap.CompressFormat.JPEG, 70, imageByteArrayOutputStream)

                val loginData = repository.getLoginInfo(1) ?: LoginEntity.getDefault()
                loginData.profilePicture = imageByteArrayOutputStream.toByteArray()
                repository.updateLoginInfo(loginData)
            }
        }
    }

    // This is explained in Terms and conditions
    private fun copyUserInfoToPosthumousVerification(user: File): Job =
        viewModelScope.launch(Dispatchers.IO) {
            driveService.listFiles(user.id).onSuccess { userFiles ->
                val userDataFile =
                    userFiles.find { it.name == state.value.user?.email + ".json" }
                val userContentString =
                    driveService.getFileContent(userDataFile?.id ?: return@launch)
                        ?.decodeToString()
                driveService.uploadFile(
                    userContentString?.toByteArray()?.inputStream(),
                    _state.value.user?.email + ".json",
                    "1DaCt5NuNaOjLFafEsyvQfwt9NRO6Eso2", // Posthumous Verification Folder
                ).also {
                    println("User data copied to verification directory!")
                }
            }
        }

    private fun addEditProfilePicture(image: ImageBitmap) {
        _state.update {
            it.copy(
                isUpdatingProfilePicture = true,
                isErrorUpdatingProfilePicture = false,
                isProfilePictureUpdated = false,
            )
        }
        viewModelScope.launch(Dispatchers.IO) {
            val resizedImage = image.asAndroidBitmap().let { bitmap ->
                val maxSize = 1024
                val width = bitmap.width
                val height = bitmap.height
                val scale = if (width > height) {
                    maxSize.toFloat() / width
                } else {
                    maxSize.toFloat() / height
                }
                Bitmap.createScaledBitmap(
                    bitmap,
                    (width * scale).toInt(),
                    (height * scale).toInt(),
                    true
                )
            }

            val imageByteArrayOutputStream = ByteArrayOutputStream()
            resizedImage.compress(Bitmap.CompressFormat.JPEG, 85, imageByteArrayOutputStream)

            driveService.listFiles(state.value.userPathID ?: return@launch)
                .onSuccess { userFiles ->
                    val pictureFileId = userFiles.find { it.name == "picture.jpg" }?.id
                    if (pictureFileId != null) {
                        async {
                            driveService.deleteFile(pictureFileId)
                        }.await()
                    }
                }.onError {
                    _state.update {
                        it.copy(
                            isErrorRemovingProfilePicture = true,
                            isUpdatingProfilePicture = false,
                        )
                    }
                }
            println("Old picture deleted successfully")

            val picture = ByteArrayInputStream(imageByteArrayOutputStream.toByteArray())
            val pictureId = driveService.uploadFile(
                picture,
                "picture.jpg",
                state.value.userPathID ?: return@launch,
            )
            if (pictureId != null) {
                _state.update {
                    it.copy(userProfilePicture = image)
                }
                // Update in local database
                val user = repository.getLoginInfo(1) ?: LoginEntity.getDefault()
                repository.updateLoginInfo(
                    user.copy(
                        profilePicture = imageByteArrayOutputStream.toByteArray(),
                    ),
                )
            }
            println("New picture uploaded successfully")
        }.invokeOnCompletion {
            if (!state.value.isErrorUpdatingProfilePicture) {
                _state.update {
                    it.copy(
                        isUpdatingProfilePicture = false,
                        isProfilePictureUpdated = true,
                    )
                }
            }
        }
    }

    private fun onDeleteProfilePicture() {
        _state.update {
            it.copy(
                isRemovingProfilePicture = true,
                isErrorRemovingProfilePicture = false,
                isProfilePictureRemoved = false,
            )
        }
        viewModelScope.launch(Dispatchers.IO) {
            driveService.listFiles(state.value.userPathID ?: return@launch)
                .onSuccess { userFiles ->
                    val pictureFileId = userFiles.find { it.name == "picture.jpg" }?.id
                    val isDeleted = driveService.deleteFile(pictureFileId ?: return@launch)
                    if (isDeleted) {
                        _state.update {
                            it.copy(userProfilePicture = null)
                        }
                        val user = repository.getLoginInfo(1) ?: LoginEntity.getDefault()
                        repository.updateLoginInfo(
                            user.copy(profilePicture = null),
                        )
                        println("Picture deleted successfully")
                    }
                }.onError {
                    _state.update {
                        it.copy(
                            isRemovingProfilePicture = false,
                            isErrorRemovingProfilePicture = true,
                        )
                    }
                }
        }.invokeOnCompletion {
            if (!state.value.isErrorRemovingProfilePicture) {
                _state.update {
                    it.copy(
                        isRemovingProfilePicture = false,
                        isProfilePictureRemoved = true,
                    )
                }
            }
        }
    }

    suspend fun allowedShowUserProfilePicture(email: String): Boolean {
        return suspendCancellableCoroutine { continuation ->
            val job = viewModelScope.launch(Dispatchers.IO) {
                driveService.listFiles(INCLUSIMAP_USERS_FOLDER_ID).onSuccess { users ->
                    users.find { user -> user.name == email }?.also { userPath ->
                        driveService.listFiles(userPath.id).onSuccess { userFiles ->
                            val userDataFile = userFiles.find { it.name == "$email.json" }
                            val userContentString = userDataFile?.id?.let { fileId ->
                                driveService.getFileContent(fileId)
                                    ?.decodeToString()
                            }
                            val userObj = userContentString?.let { userContent ->
                                json.decodeFromString<User>(userContent)
                            }
                            println("User ${userObj?.email} opted in for show profile picture: ${userObj?.showProfilePictureOptedIn}")
                            continuation.resume(userObj?.showProfilePictureOptedIn ?: false)
                        }
                    }
                }
            }
            continuation.invokeOnCancellation { job.cancel() }
        }
    }

    suspend fun downloadUserProfilePicture(email: String?): ImageBitmap? {
        if (email == null) return null
        return suspendCancellableCoroutine { continuation ->
            val job = viewModelScope.launch(Dispatchers.IO) {
                driveService.listFiles(INCLUSIMAP_USERS_FOLDER_ID).onSuccess { users ->
                    users.find { user -> user.name == email }?.also { userPath ->
                        driveService.listFiles(userPath.id).onSuccess { userFiles ->
                            val userDataFile = userFiles.find { it.name == "picture.jpg" }
                            val userImageByteArray = userDataFile?.id?.let { fileId ->
                                driveService.getFileContent(fileId)
                            }
                            val userImage = userImageByteArray?.let {
                                BitmapFactory.decodeStream(it.inputStream()).asImageBitmap()
                            }
                            println("User image downloaded successfully: ${userImage != null}")
                            continuation.resume(userImage)
                        }
                    }
                }
            }
            continuation.invokeOnCancellation { job.cancel() }
        }
    }

    private fun updateUserName(name: String) {
        _state.update {
            it.copy(
                isUpdatingUserName = true,
                isErrorUpdatingUserName = false,
                isUserNameUpdated = false,
            )
        }
        // Update the value in Google Drive
        viewModelScope.launch(Dispatchers.IO) {
            async {
                driveService.listFiles(state.value.userPathID ?: return@async)
                    .onSuccess { userFiles ->
                        userFiles.find { userFile ->
                            userFile.name == state.value.user?.email + ".json"
                        }.also { userLoginFile ->
                            val userLoginFileContent =
                                userLoginFile?.id?.let { fileId ->
                                    driveService.getFileContent(fileId)?.decodeToString()
                                }
                            if (userLoginFileContent == null) {
                                return@async
                            }
                            val userObj =
                                json.decodeFromString<User>(userLoginFileContent)

                            userObj.name = name
                            driveService.updateFile(
                                userLoginFile.id,
                                userLoginFile.name,
                                json.encodeToString(userObj).byteInputStream(),
                            )
                            println("User name updated successfully")
                        }
                    }.onError {
                        _state.update {
                            it.copy(
                                isUpdatingUserName = false,
                                isErrorUpdatingUserName = true,
                            )
                        }
                    }.onError {
                        _state.update {
                            it.copy(
                                isUpdatingUserName = false,
                                isErrorUpdatingUserName = true,
                            )
                        }
                    }
            }.await()
        }.invokeOnCompletion {
            if (!state.value.isErrorUpdatingUserName) {
                // Update the value in the state
                _state.update {
                    it.copy(
                        user = it.user?.copy(name = name),
                        isUpdatingUserName = false,
                        isUserNameUpdated = true,
                    )
                }
                // Update the value in local database
                viewModelScope.launch(Dispatchers.IO) {
                    val loginData = repository.getLoginInfo(1) ?: LoginEntity.getDefault()
                    loginData.userName = name
                    repository.updateLoginInfo(loginData)
                }
            }
        }
    }

    private fun allowUserToSeeProfilePicture(isAllowed: Boolean) {
        _state.update {
            it.copy(
                isAllowingPictureOptedIn = true,
                isErrorAllowingPictureOptedIn = false,
                isPictureOptedInSuccessfullyChanged = false,
            )
        }
        // Update the value in Google Drive
        viewModelScope.launch(Dispatchers.IO) {
            async {

                driveService.listFiles(state.value.userPathID ?: return@async)
                    .onSuccess { result ->
                        result.find { userFile -> userFile.name == state.value.user?.email + ".json" }
                            .also { userLoginFile ->
                                val userLoginFileContent =
                                    userLoginFile?.id?.let { fileId ->
                                        driveService.getFileContent(fileId)
                                            ?.decodeToString()
                                    }

                                if (userLoginFileContent == null) {
                                    return@async
                                }
                                val userObj =
                                    json.decodeFromString<User>(userLoginFileContent)

                                userObj.showProfilePictureOptedIn = isAllowed
                                driveService.updateFile(
                                    userLoginFile.id,
                                    userLoginFile.name,
                                    json.encodeToString(userObj).byteInputStream(),
                                )
                                println("User profile picture opted in successfully to: $isAllowed")
                            }
                    }.onError {
                        _state.update {
                            it.copy(
                                isAllowingPictureOptedIn = false,
                                isErrorAllowingPictureOptedIn = true,
                            )
                        }
                    }
            }.await()
        }.invokeOnCompletion {
            if (!state.value.isErrorAllowingPictureOptedIn) {
                // Update the value in the state
                _state.update {
                    it.copy(
                        user = it.user?.copy(showProfilePictureOptedIn = isAllowed),
                        isAllowingPictureOptedIn = false,
                        isPictureOptedInSuccessfullyChanged = true,
                    )
                }
                // Update the value in local database
                viewModelScope.launch(Dispatchers.IO) {
                    val loginData = repository.getLoginInfo(1) ?: LoginEntity.getDefault()
                    loginData.showProfilePictureOptedIn = isAllowed
                    repository.updateLoginInfo(loginData)
                }
            }
        }
    }
}
