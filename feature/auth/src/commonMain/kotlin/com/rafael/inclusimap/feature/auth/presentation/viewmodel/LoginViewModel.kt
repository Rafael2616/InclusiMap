package com.rafael.inclusimap.feature.auth.presentation.viewmodel

import androidx.compose.ui.graphics.decodeToImageBitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rafael.inclusimap.core.services.AwsFileApiService
import com.rafael.inclusimap.core.util.compressByteArray
import com.rafael.inclusimap.core.util.map.Constants.INCLUSIMAP_IMAGE_FOLDER_PATH
import com.rafael.inclusimap.core.util.map.Constants.INCLUSIMAP_PARAGOMINAS_PLACE_DATA_FOLDER_PATH
import com.rafael.inclusimap.core.util.map.Constants.INCLUSIMAP_USERS_FOLDER_PATH
import com.rafael.inclusimap.core.util.map.extractPlaceUserEmail
import com.rafael.inclusimap.core.util.map.extractUserEmail
import com.rafael.inclusimap.core.util.map.model.AccessibleLocalMarker
import com.rafael.inclusimap.feature.auth.data.repository.MailerSenderClient
import com.rafael.inclusimap.feature.auth.domain.model.DeleteProcess
import com.rafael.inclusimap.feature.auth.domain.model.LoginEntity
import com.rafael.inclusimap.feature.auth.domain.model.LoginEvent
import com.rafael.inclusimap.feature.auth.domain.model.LoginState
import com.rafael.inclusimap.feature.auth.domain.model.RegisteredUser
import com.rafael.inclusimap.feature.auth.domain.model.ServerState
import com.rafael.inclusimap.feature.auth.domain.model.User
import com.rafael.inclusimap.feature.auth.domain.repository.LoginRepository
import com.rafael.inclusimap.feature.auth.domain.utils.generateToken
import com.rafael.inclusimap.feature.auth.domain.utils.hashToken
import com.rafael.inclusimap.feature.auth.domain.utils.verifyToken
import kotlin.coroutines.resume
import kotlin.time.Clock.System
import kotlin.time.ExperimentalTime
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.IO
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.serialization.json.Json

class LoginViewModel(
    private val repository: LoginRepository,
    private val emailClient: MailerSenderClient,
    private val awsService: AwsFileApiService,
) : ViewModel() {
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
                            isBanned = loginData.isBanned,
                            isAdmin = loginData.isAdmin,
                            showFirstTimeAnimation = loginData.showFirstTimeAnimation,
                        ),
                        userProfilePicture = loginData.profilePicture,
                        userPath = loginData.userPathID,
                        tokenHash = loginData.tokenHash,
                        recoveryToken = loginData.recoveryToken,
                        tokenExpirationTime = loginData.tokenExpirationDate,
                    )
                }
            }
        }
        checkServerIsAvailableContinuously(60 * 1000L)
        checkUserIsBannedContinuously(60 * 1000L)
        checkLocalUserExists()
    }

    fun onEvent(event: LoginEvent) {
        when (event) {
            is LoginEvent.OnLogin -> login(event.registeredUser)
            is LoginEvent.OnRegisterNewUser -> registerNewUser(event.user)
            LoginEvent.OnLogout -> logout()
            is LoginEvent.UpdatePassword -> updatePassword(event.password)
            is LoginEvent.SetIsPasswordChanged -> _state.update {
                it.copy(isPasswordChanged = event.isChanged)
            }

            is LoginEvent.DeleteAccount -> deleteAccount(event.keepContributions)
            is LoginEvent.OnAddEditUserProfilePicture -> addEditProfilePicture(event.image)
            LoginEvent.OnRemoveUserProfilePicture -> onDeleteProfilePicture()
            is LoginEvent.UpdateUserName -> updateUserName(event.name)
            is LoginEvent.OnAllowPictureOptedIn -> allowUserToSeeProfilePicture(event.value)
            is LoginEvent.SendPasswordResetEmail -> recoveryPasswordProcess(event.email)
            is LoginEvent.ValidateToken -> validateToken(event.token)
            LoginEvent.InvalidateUpdatePasswordProcess -> invalidateUpdatePasswordProcess()
            is LoginEvent.SetIsBanned -> {
                _state.update {
                    it.copy(isUserBanned = event.isBanned)
                }
            }
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
            isBanned = false,
            isAdmin = false,
            showFirstTimeAnimation = true,
        )
        val userPath = "$INCLUSIMAP_USERS_FOLDER_PATH/${newUser.email}"

        viewModelScope.launch(Dispatchers.IO) {
            val userPathContents = awsService
                .listFiles(userPath)
                .getOrNull()

            _state.update {
                it.copy(userAlreadyRegistered = userPathContents != null)
            }
            val userExists =
                userPathContents?.find { userLoginFile -> userLoginFile == "${newUser.email}.json" }
            _state.update { it.copy(userAlreadyRegistered = userExists != null) }

            if (_state.value.userAlreadyRegistered) {
                _state.update { it.copy(isRegistering = false) }
                return@launch
            }
            awsService.createFolder(userPath)

            val userString = json.encodeToString<User>(user)
            async {
                awsService.uploadFile(
                    "$userPath/${newUser.email}.json",
                    userString,
                )
                awsService.uploadFile(
                    "$userPath/contributions.json",
                    "[]",
                )
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
                    loginData.userPathID = userPath

                    repository.updateLoginInfo(loginData)

                    _state.update {
                        it.copy(
                            user = user,
                            isLoggedIn = true,
                            userPath = userPath,
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
                val userPath = "$INCLUSIMAP_USERS_FOLDER_PATH/${registeredUser.email}"
                val userPathContents = awsService
                    .listFiles(userPath)
                    .getOrNull()

                _state.update {
                    it.copy(
                        isRegistering = false,
                        userAlreadyRegistered = userPathContents != null,
                    )
                }

                val userExists =
                    userPathContents?.find { userLoginFile -> userLoginFile == "${registeredUser.email}.json" }
                _state.update {
                    it.copy(
                        userAlreadyRegistered = userExists != null,
                        networkError = userExists == null,
                    )
                }

                awsService.downloadFile("$userPath/${registeredUser.email}.json").getOrNull()
                    .also { userLoginFile ->
                        println("User login file found!")
                        val userLoginFileContent = userLoginFile?.decodeToString()
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
                        println("Working on user path: ${_state.value.userPath}")
                        val userImageByteArray =
                            downloadUserProfilePicture(userObj.email)
                        val loginData =
                            repository.getLoginInfo(1)
                                ?: LoginEntity.getDefault()
                        loginData.userId = userObj.id
                        loginData.userName = userObj.name
                        loginData.userEmail = userObj.email
                        loginData.userPassword = userObj.password
                        loginData.userPathID = userPath
                        loginData.isLoggedIn = true
                        loginData.showProfilePictureOptedIn =
                            userObj.showProfilePictureOptedIn
                        loginData.profilePicture =
                            userImageByteArray
                        loginData.tokenHash = null
                        loginData.recoveryToken = null
                        loginData.tokenExpirationDate = null
                        loginData.isBanned = userObj.isBanned
                        loginData.isAdmin = userObj.isAdmin
                        loginData.showFirstTimeAnimation =
                            userObj.showFirstTimeAnimation == true
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
                                    isBanned = userObj.isBanned,
                                    isAdmin = userObj.isAdmin,
                                    showFirstTimeAnimation = userObj.showFirstTimeAnimation == true,
                                ),
                                isUserBanned = userObj.isBanned,
                                userProfilePicture = userImageByteArray,
                                userPath = userPath,
                            )
                        }
                    }
            }.await()
        }.invokeOnCompletion {
            println("Login completed")
            if (_state.value.userAlreadyRegistered && _state.value.isPasswordCorrect && !_state.value.networkError && _state.value.user != null) {
                _state.update {
                    it.copy(
                        isLoggedIn = !state.value.isUserBanned,
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
                        userPath = null,
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
        // Update the password in AWS
        viewModelScope.launch(Dispatchers.IO) {
            async {
                val userPath = "$INCLUSIMAP_USERS_FOLDER_PATH/${state.value.user?.email}"
                val userLoginFilePath = "$userPath/${state.value.user?.email + ".json"}"
                val userLoginFileContent = awsService
                    .downloadFile(userLoginFilePath)
                    .getOrNull()
                    ?.decodeToString()

                if (userLoginFileContent == null) {
                    println("User file content is null")
                    return@async
                }
                val userObj = json.decodeFromString<User>(userLoginFileContent)
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
                awsService.uploadFile(
                    userLoginFilePath,
                    json.encodeToString(userObj),
                )
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
                val userPath =
                    "$INCLUSIMAP_USERS_FOLDER_PATH/${_state.value.user?.email}/${_state.value.user?.email + ".json"}"
                val userContents = awsService
                    .listFiles(userPath)
                    .getOrNull()
                if (userContents != null) {
                    copyUserInfoToPosthumousVerification(state.value.user!!).invokeOnCompletion {
                        viewModelScope.launch(Dispatchers.IO) {
                            userContents.forEach { file ->
                                if (file != "contributions.json") {
                                    awsService.deleteFile(state.value.userPath + "/" + file)
                                }
                            }
                        }
                    }
                } else {
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
            awsService.listFiles(INCLUSIMAP_PARAGOMINAS_PLACE_DATA_FOLDER_PATH)
                .onSuccess { result ->
                    result.map { place ->
                        async {
                            if (place.extractPlaceUserEmail() != _state.value.user?.email) {
                                return@async
                            }
                            println("Deleting place: $place - posted by user ${_state.value.user?.email}")
                            awsService.deleteFile("$INCLUSIMAP_PARAGOMINAS_PLACE_DATA_FOLDER_PATH/$place")
                        }
                    }.awaitAll()
                }.onFailure {
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
                    awsService.listFiles(INCLUSIMAP_IMAGE_FOLDER_PATH).onSuccess { places ->
                        places.forEach { place ->
                            if (place.extractUserEmail() == _state.value.user?.email) {
                                awsService.deleteFile("$INCLUSIMAP_IMAGE_FOLDER_PATH/$place")
                                println("Deleting image folder place posted by user ${_state.value.user?.email}")
                                return@forEach
                            }
                            awsService.listFiles("$INCLUSIMAP_IMAGE_FOLDER_PATH/$place")
                                .onSuccess { images ->
                                    images.filter { image ->
                                        image.extractUserEmail() == _state.value.user?.email
                                    }.forEach { image ->
                                        println("Deleting file: $it posted by user ${_state.value.user?.email}")
                                        awsService.deleteFile("$INCLUSIMAP_IMAGE_FOLDER_PATH/$place/$image")
                                    }
                                }
                        }
                    }.onFailure {
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
                        awsService.listFiles(INCLUSIMAP_PARAGOMINAS_PLACE_DATA_FOLDER_PATH)
                            .onSuccess { result ->
                                result.map { place ->
                                    async {
                                        val placeContentString = awsService
                                            .downloadFile("$INCLUSIMAP_PARAGOMINAS_PLACE_DATA_FOLDER_PATH/$place")
                                            .getOrNull()
                                            ?.decodeToString()
                                        val placeContent =
                                            json.decodeFromString<AccessibleLocalMarker>(
                                                placeContentString ?: "",
                                            )

                                        val placeWithoutUserComments = placeContent.copy(
                                            comments = placeContent.comments.filterNot { it.email == _state.value.user?.email },
                                        )

                                        if (placeWithoutUserComments.comments != placeContent.comments) {
                                            println("Deleting comments in place $place")
                                            val updatedPlace =
                                                json.encodeToString<AccessibleLocalMarker>(
                                                    placeWithoutUserComments,
                                                )
                                            awsService.uploadFile(
                                                "$INCLUSIMAP_PARAGOMINAS_PLACE_DATA_FOLDER_PATH/${place + "_" + placeContent.authorEmail + ".json"}",
                                                updatedPlace,
                                            )
                                        }
                                    }
                                }.awaitAll()
                            }.onFailure {
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
                            awsService.deleteFile(state.value.userPath ?: return@launch)

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

    private fun checkLocalUserExists() {
        if (state.value.user == null) {
            _state.update { it.copy(isLoggedIn = false) }
            return
        }
        viewModelScope.launch(Dispatchers.IO) {
            val userPath = "$INCLUSIMAP_USERS_FOLDER_PATH/${_state.value.user?.email}"
            awsService.listFiles(userPath).getOrNull().also { user ->
                _state.update { it.copy(isLoggedIn = user != null) }
                val loginData = repository.getLoginInfo(1) ?: LoginEntity.getDefault()
                loginData.isLoggedIn = user != null
                if (user == null) {
                    loginData.userId = null
                    loginData.userName = null
                    loginData.userEmail = null
                    loginData.userPassword = null
                    loginData.showProfilePictureOptedIn = true
                    loginData.profilePicture = null
                    loginData.userPathID = null
                } else {
                    println("Working on user path: ${_state.value.userPath}")
                }
                repository.updateLoginInfo(loginData)
            }
        }.invokeOnCompletion {
            setupProfilePicture()
        }
    }

    private fun setupProfilePicture() {
        // Download user profile picture
        viewModelScope.launch(Dispatchers.IO) {
            val picture = downloadUserProfilePicture(state.value.user?.email)
            if (picture.contentEquals(state.value.userProfilePicture)) return@launch

            _state.update { it.copy(userProfilePicture = picture) }
            val loginData = repository.getLoginInfo(1) ?: LoginEntity.getDefault()
            loginData.profilePicture = picture
            repository.updateLoginInfo(loginData)
        }
    }

    private suspend fun checkUserExists(email: String) =
        suspendCancellableCoroutine { continuation ->
            viewModelScope.launch(Dispatchers.IO) {
                val userPath = "$INCLUSIMAP_USERS_FOLDER_PATH/$email"
                awsService.listFiles(userPath).getOrNull().also {
                    continuation.resume(it != null)
                }
            }
        }

    // This is explained in Terms and conditions
    private fun copyUserInfoToPosthumousVerification(user: User): Job =
        viewModelScope.launch(Dispatchers.IO) {
            val userEmail = repository.getLoginInfo(1)?.userEmail
            awsService.uploadFile(
                "UnregisteredUsers/$userEmail.json", // Posthumous Verification Folder
                json.encodeToString(user),
            ).also {
                println("User data copied to verification directory!")
            }
        }

    fun checkServerIsAvailable() {
        viewModelScope.launch(Dispatchers.IO) {
            _state.update { it.copy(isCheckingServerAvailability = true) }
            awsService.downloadFile("serverState.json").getOrNull()
                .also { state ->
                    println("Verifying InclusiMap server availability")
                    println(state?.decodeToString())
                    val serverState =
                        json.decodeFromString<ServerState>(state?.decodeToString() ?: return@launch)
                    _state.update {
                        it.copy(isServerAvailable = serverState.isOn)
                    }
                    println("Server state: isAvailable: ${serverState.isOn}")
                }
        }.invokeOnCompletion {
            _state.update {
                it.copy(isCheckingServerAvailability = false)
            }
        }
    }

    private fun addEditProfilePicture(image: ByteArray) {
        _state.update {
            it.copy(
                isUpdatingProfilePicture = true,
                isErrorUpdatingProfilePicture = false,
                isProfilePictureUpdated = false,
            )
        }
        viewModelScope.launch(Dispatchers.IO) {
            val compressedImage = compressByteArray(image)
            val isSuccessful = awsService.uploadImage(
                "${state.value.userPath}/picture.jpg",
                compressedImage,
            ).getOrNull()
            _state.update {
                it.copy(
                    userProfilePicture = image,
                    isProfilePictureUpdated = isSuccessful == 200,
                )
            }

            // Update in local database
            val user = repository.getLoginInfo(1) ?: LoginEntity.getDefault()
            repository.updateLoginInfo(
                user.copy(profilePicture = compressedImage),
            )
            if (!state.value.isErrorUpdatingProfilePicture) {
                _state.update {
                    it.copy(
                        isUpdatingProfilePicture = false,
                        isProfilePictureUpdated = true,
                    )
                }
            }
            println("New picture uploaded successfully")
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
            val isDeleted = awsService.deleteFile("${state.value.userPath}/picture.jpg").getOrNull()
            if (isDeleted == true) {
                _state.update {
                    it.copy(userProfilePicture = null)
                }
                val user = repository.getLoginInfo(1) ?: LoginEntity.getDefault()
                repository.updateLoginInfo(
                    user.copy(profilePicture = null),
                )
                println("Picture deleted successfully")
            } else {
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

    suspend fun allowedShowUserProfilePicture(email: String): Boolean =
        suspendCancellableCoroutine { continuation ->
            val job = viewModelScope.launch(Dispatchers.IO) {
                awsService.downloadFile(state.value.userPath + "/${"$email.json"}").getOrNull()
                    .also { user ->
                        val userObj = user?.decodeToString()?.let { userContent ->
                            runCatching { json.decodeFromString<User>(userContent) }.getOrNull()
                        }
                        println("User ${userObj?.email} opted in for show profile picture: ${userObj?.showProfilePictureOptedIn}")
                        continuation.resume(userObj?.showProfilePictureOptedIn == true)
                    }
            }
            continuation.invokeOnCancellation { job.cancel() }
        }

    suspend fun downloadUserProfilePicture(email: String?): ByteArray? {
        if (email == null) return null
        return suspendCancellableCoroutine { continuation ->
            val job = viewModelScope.launch(Dispatchers.IO) {
                awsService.downloadImage("${state.value.userPath}/picture.jpg").getOrNull()
                    .also { image ->
                        println("User image downloaded successfully: ${image != null}")
                        val img = runCatching {
                            image?.decodeToImageBitmap()?.width
                            image
                        }.getOrNull()
                        continuation.resume(img)
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
        // Update the value in AWS
        viewModelScope.launch(Dispatchers.IO) {
            awsService.uploadFile(
                "${state.value.userPath}/${state.value.user?.name}.json",
                json.encodeToString(
                    _state.value.user!!.copy(name = name),
                ),
            )
            println("User name updated successfully")
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
            awsService.uploadFile(
                "${state.value.userPath}/${state.value.user?.email + ".json"}",
                json.encodeToString(
                    state.value.user!!.copy(showProfilePictureOptedIn = isAllowed),
                ),
            ).also {
                println("User profile picture opted in successfully to: $isAllowed")
            }
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

    private fun recoveryPasswordProcess(email: String) {
        val token = generateRecoveryToken()
        sendEmailRecoveryWithToken(email, token)
    }

    @OptIn(ExperimentalTime::class)
    private fun generateRecoveryToken(): String {
        val expiration = System.now().toEpochMilliseconds() + (3 * 60 * 1000)
        val token = generateToken()
        _state.update {
            it.copy(
                recoveryToken = token,
                tokenHash = hashToken(token),
                tokenExpirationTime = expiration,
            )
        }
        viewModelScope.launch(Dispatchers.IO) {
            val loginData = repository.getLoginInfo(1) ?: LoginEntity.getDefault()
            loginData.recoveryToken = token
            loginData.tokenHash = hashToken(token)
            loginData.tokenExpirationDate = expiration
            repository.updateLoginInfo(loginData)
        }
        return token
    }

    private fun sendEmailRecoveryWithToken(email: String, token: String) {
        _state.update {
            it.copy(
                isSendingEmail = true,
                isEmailSent = false,
            )
        }
        viewModelScope.launch(Dispatchers.IO) {
            val userExists = checkUserExists(email)
            if (!userExists) {
                _state.update {
                    it.copy(
                        userExists = false,
                        isSendingEmail = false,
                        isEmailSent = false,
                    )
                }
                return@launch
            } else {
                _state.update {
                    it.copy(userExists = true)
                }
            }

            val userName = findUserNameByEmail(email)
            emailClient.sendEmail(
                receiver = email,
                subject = "Recuperação de senha",
                html = """
                    <div style="font-family: Arial, sans-serif; line-height: 1.6; color: #333;">
                        <h2 style="color: #000;">Recuperação de senha</h2>
                        <p>Olá, $userName!</p>
                        <p>Você solicitou a recuperação de senha. Para continuar, copie o código abaixo e cole no aplicativo:</p>
                        <p style="font-size: 24px; font-weight: bold; color: #000; text-align: center; margin: 20px 0;">
                           $token
                        </p>
                        <p style="font-size: 14px; color: #888;">Este código é válido por 3 minutos.</p>
                        <hr style="border: none; border-top: 1px solid #ddd; margin: 20px 0;">
                        <p style="font-size: 12px; color: #888;">Atenciosamente, <br>Equipe InclusiMap</p>
                    </div>
                """.trimIndent(),
            )
        }.invokeOnCompletion {
            if (it == null && state.value.userExists) {
                _state.update {
                    it.copy(
                        isSendingEmail = false,
                        isEmailSent = true,
                    )
                }
                tokenExpirationTimer()
            }
        }
    }

    private fun validateToken(receivedToken: String) {
        viewModelScope.launch(Dispatchers.IO) {
            _state.update {
                it.copy(
                    isValidatingToken = true,
                    isTokenValidated = false,
                )
            }
            var realToken = ""
            if (receivedToken == state.value.recoveryToken) {
                realToken = state.value.recoveryToken.orEmpty()
            }
            val isTokenValid = verifyToken(
                realToken,
                state.value.tokenHash ?: return@launch,
                state.value.tokenExpirationTime ?: return@launch,
            )
            delay(1500L)
            _state.update {
                it.copy(
                    isTokenValid = isTokenValid,
                    isValidatingToken = false,
                    isTokenValidated = true,
                )
            }
            if (isTokenValid) {
                println("Token hash validated! Allowing password redefinition.")
            } else {
                println("Token is invalid or expired.")
            }
        }
    }

    private suspend fun findUserNameByEmail(email: String) =
        suspendCancellableCoroutine { continuation ->
            viewModelScope.launch(Dispatchers.IO) {
                awsService.downloadFile(state.value.userPath + "/${"$email.json"}").getOrNull()
                    ?.decodeToString()
                    ?.let { userContentString ->
                        val user = json.decodeFromString<User>(userContentString)
                        continuation.resume(user.name)
                        if (state.value.user == null) {
                            _state.update { it.copy(user = user) }
                        }
                    }
            }
        }

    private fun invalidateUpdatePasswordProcess() {
        _state.update {
            it.copy(
                isTokenValidated = false,
                isTokenValid = false,
                isEmailSent = false,
                isSendingEmail = false,
                recoveryToken = null,
                tokenHash = null,
                tokenExpirationTime = null,
                tokenExpirationTimer = null,
            )
        }
        viewModelScope.launch(Dispatchers.IO) {
            val loginData = repository.getLoginInfo(1) ?: LoginEntity.getDefault()
            loginData.recoveryToken = null
            loginData.tokenHash = null
            loginData.tokenExpirationDate = null
            repository.updateLoginInfo(loginData)
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class, ExperimentalTime::class)
    private fun tokenExpirationTimer() {
        viewModelScope.launch {
            state.map { it.tokenExpirationTime }
                .distinctUntilChanged()
                .filterNotNull()
                .flatMapLatest { expirationTime ->
                    flow {
                        while (true) {
                            val remainingTime =
                                (expirationTime - System.now().toEpochMilliseconds())
                                    .coerceAtLeast(0L)
                            emit(remainingTime)
                            delay(1000L)
                        }
                    }
                }
                .flowOn(Dispatchers.IO)
                .collect { remainingTime ->
                    _state.update { it.copy(tokenExpirationTimer = remainingTime) }
                }
        }
    }

    private fun checkUserIsBannedContinuously(intervalInMinutes: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            while (true) {
                checkUserIsBanned()
                delay(intervalInMinutes)
            }
        }
    }

    private fun checkUserIsBanned() {
        if (state.value.userPath == null) {
            return
        }
        viewModelScope.launch(Dispatchers.IO) {
            println("Checking if user is banned. Path: ${state.value.userPath}")
            awsService.downloadFile(state.value.userPath + "/${state.value.user?.email}.json")
                .getOrNull()
                ?.decodeToString()
                ?.let { userContentString ->
                    json.decodeFromString<User>(userContentString).also { userLoginFile ->
                        _state.update {
                            it.copy(isUserBanned = userLoginFile.isBanned)
                        }
                        println("User is banned: ${userLoginFile.isBanned}")
                        if (userLoginFile.isBanned) {
                            logout()
                        }
                    }
                } ?: {
                println("Failed to check if user is banned")
            }
        }
    }

    private fun checkServerIsAvailableContinuously(frequency: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            while (true) {
                checkServerIsAvailable()
                delay(frequency)
            }
        }
    }

    fun onSetPresentationMode(value: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            awsService.downloadFile(state.value.userPath + "/${state.value.user?.email + ".json"}")
                .getOrNull()?.decodeToString()
                ?.let { json.decodeFromString<User>(it) }?.also { user ->
                    _state.update {
                        it.copy(user = it.user?.copy(showFirstTimeAnimation = value))
                    }
                    repository.updateLoginInfo(
                        repository.getLoginInfo(1)?.copy(
                            showFirstTimeAnimation = value,
                        ) ?: LoginEntity.getDefault(),
                    )
                    awsService.uploadFile(
                        state.value.userPath + "/${state.value.user?.email + ".json"}",
                        json.encodeToString(
                            user.copy(
                                showFirstTimeAnimation = value,
                            ),
                        ),
                    )
                } ?: {
                println("Failed to update first time animation value")
            }
        }
    }
}

internal fun Long.formatInMinutes(): String {
    this / 60000
    (this % 60000) / 1000
    return ""
//    return String.format(Locale.getDefault(), "%02d:%02d", minutes, remainingSeconds)
}
