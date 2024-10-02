package com.rafael.inclusimap.feature.auth.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rafael.inclusimap.core.domain.model.AccessibleLocalMarker
import com.rafael.inclusimap.core.domain.model.DeleteProcess
import com.rafael.inclusimap.core.domain.model.util.extractUserEmail
import com.rafael.inclusimap.core.domain.util.Constants.INCLUSIMAP_IMAGE_FOLDER_ID
import com.rafael.inclusimap.core.domain.util.Constants.INCLUSIMAP_PLACE_DATA_FOLDER_ID
import com.rafael.inclusimap.core.domain.util.Constants.INCLUSIMAP_USERS_FOLDER_ID
import com.rafael.inclusimap.core.services.GoogleDriveService
import com.rafael.inclusimap.feature.auth.domain.model.LoginEntity
import com.rafael.inclusimap.feature.auth.domain.model.LoginEvent
import com.rafael.inclusimap.feature.auth.domain.model.LoginState
import com.rafael.inclusimap.feature.auth.domain.model.RegisteredUser
import com.rafael.inclusimap.feature.auth.domain.model.User
import com.rafael.inclusimap.feature.auth.domain.repository.LoginRepository
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class LoginViewModel(
    private val repository: LoginRepository,
) : ViewModel() {
    private val driveService: GoogleDriveService = GoogleDriveService()
    private val _state = MutableStateFlow(LoginState())
    val state = _state.asStateFlow()

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
                        ),
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
        }
    }

    @OptIn(ExperimentalUuidApi::class)
    private fun registerNewUser(newUser: User) {
        _state.update {
            it.copy(
                isRegistering = true,
            )
        }
        val userID = Uuid.random().toString()
        val user = User(
            id = userID,
            name = newUser.name,
            email = newUser.email,
            password = newUser.password,
        )
        viewModelScope.launch(Dispatchers.IO) {
            async {
                _state.update {
                    it.copy(
                        userAlreadyRegistered = driveService.listFiles(
                            INCLUSIMAP_USERS_FOLDER_ID,
                        ).any { userFile ->
                            userFile.name.split(".json")[0] == newUser.email
                        }.also { isRegistered ->
                            println("User already registered? $isRegistered")
                        },
                        isRegistering = false,
                    )
                }
            }.await()

            if (_state.value.userAlreadyRegistered) {
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

            val json = Json { ignoreUnknownKeys = true }
            async {
                driveService.uploadFile(
                    json.encodeToString(user).byteInputStream(),
                    "${newUser.email}.json",
                    _state.value.userPathID
                        ?: throw IllegalStateException("Folder not found: Maybe an issue has occurred while creating the folder"),
                )
            }.await()
        }.invokeOnCompletion {
            if (!_state.value.userAlreadyRegistered) {
                viewModelScope.launch(Dispatchers.IO) {
                    val loginData = repository.getLoginInfo(1) ?: LoginEntity.getDefault()
                    loginData.userId = user.id
                    loginData.userName = user.name
                    loginData.userEmail = user.email
                    loginData.userPassword = user.password
                    loginData.isLoggedIn = true

                    repository.updateLoginInfo(loginData)

                    _state.update {
                        it.copy(
                            user = user,
                            isLoggedIn = true,
                            isRegistering = false,
                        )
                    }
                }
            }
        }
    }

    private fun login(registeredUser: RegisteredUser) {
        _state.update {
            it.copy(
                isRegistering = true,
                isPasswordCorrect = true,
                userAlreadyRegistered = true,
            )
        }
        viewModelScope.launch(Dispatchers.IO) {
            async {
                _state.update {
                    it.copy(
                        userAlreadyRegistered = driveService.listFiles(
                            INCLUSIMAP_USERS_FOLDER_ID,
                        ).any { userFile ->
                            userFile.name.split(".json")[0] == registeredUser.email
                        }.also { isRegistered ->
                            println("User already registered? $isRegistered")
                        },
                    )
                }
            }.await()

            if (!_state.value.userAlreadyRegistered) {
                println("User not found")
                return@launch
            }

            val json = Json { ignoreUnknownKeys = true }
            async {
                val user = driveService.listFiles(INCLUSIMAP_USERS_FOLDER_ID).find { userFile ->
                    userFile.name.split(".json")[0] == registeredUser.email
                }

                val userLoginFile = driveService.listFiles(user?.id ?: "").find { userFile ->
                    userFile.name.endsWith(".json")
                }

                val userLoginFileContent = driveService.getFileContent(
                    userLoginFile?.id ?: throw IllegalStateException("User not found"),
                )?.decodeToString()
                println("User file content: $userLoginFileContent")

                if (userLoginFileContent == null) {
                    println("User file content is null")
                    return@async
                }
                val userObj = json.decodeFromString<User>(userLoginFileContent)
                if (userObj.password != registeredUser.password) {
                    _state.update {
                        it.copy(
                            isPasswordCorrect = false,
                        )
                    }
                    println("Password is incorrect")
                    return@async
                }
                val loginData = repository.getLoginInfo(1) ?: LoginEntity.getDefault()
                loginData.userId = userObj.id
                loginData.userName = userObj.name
                loginData.userEmail = userObj.email
                loginData.userPassword = userObj.password
                loginData.isLoggedIn = true

                repository.updateLoginInfo(loginData)

                _state.update {
                    it.copy(
                        isPasswordCorrect = true,
                        user = User(
                            id = userObj.id,
                            name = userObj.name,
                            email = userObj.email,
                            password = userObj.password,
                        ),
                    )
                }
            }.await()
        }.invokeOnCompletion {
            if (_state.value.userAlreadyRegistered && _state.value.isPasswordCorrect) {
                _state.update {
                    it.copy(
                        isLoggedIn = true,
                        isPasswordCorrect = true,
                    )
                }
            }
            _state.update {
                it.copy(
                    isRegistering = false,
                )
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
                        isLoggedIn = false,
                        isLoginOut = false,
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
            )
        }
        // Update the password in local database
        viewModelScope.launch(Dispatchers.IO) {
            val loginData = repository.getLoginInfo(1) ?: LoginEntity.getDefault()
            loginData.userEmail = password
        }
        // Update the password in Google Drive
        viewModelScope.launch(Dispatchers.IO) {
            async {
                val user = driveService.listFiles(INCLUSIMAP_USERS_FOLDER_ID).find { userFile ->
                    userFile.name == _state.value.user?.email
                }

                val userLoginFile = driveService.listFiles(user?.id ?: "").find { userFile ->
                    userFile.name.endsWith(".json")
                }
                val json = Json { ignoreUnknownKeys = true }
                val userLoginFileContent =
                    driveService.getFileContent(
                        userLoginFile?.id ?: throw IllegalStateException("User not found"),
                    )?.decodeToString()

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
                driveService.updateFile(
                    userLoginFile.id,
                    userLoginFile.name,
                    json.encodeToString(userObj).byteInputStream(),
                )
            }.await()
        }.invokeOnCompletion {
            // Update the password in the state
            if (_state.value.isSamePassword) return@invokeOnCompletion
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
            )
        }
        viewModelScope.launch(Dispatchers.IO) {
            // Delete user info from Google Drive
            async {
                driveService.listFiles(INCLUSIMAP_USERS_FOLDER_ID).find { userFile ->
                    userFile.name == _state.value.user?.email
                }?.let {
                    driveService.deleteFile(it.id)
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
                    if (keepContributions) {
                        _state.update {
                            it.copy(deleteStep = DeleteProcess.SUCCESS)
                        }
                    } else {
                        _state.update {
                            it.copy(deleteStep = DeleteProcess.DELETING_USER_INFO)
                        }
                    }
                }
            }

            if (keepContributions) return@launch

            async {
                // Delete user posted places
                val json = Json { ignoreUnknownKeys = true }
                val places = driveService.listFiles(INCLUSIMAP_PLACE_DATA_FOLDER_ID).find {
                    it.name == "places.json"
                }
                val placesContent = places?.run {
                    val content = driveService.getFileContent(this.id)?.decodeToString()
                    json.decodeFromString<List<AccessibleLocalMarker>>(
                        content ?: return@async,
                    )
                }
                val placesWithoutUserPlaces = placesContent?.filterNot {
                    it.authorEmail == _state.value.user?.email
                }
                val updatedPlaces = json.encodeToString<List<AccessibleLocalMarker>>(
                    placesWithoutUserPlaces ?: return@async,
                )
                driveService.updateFile(
                    places.id,
                    "places.json",
                    updatedPlaces.toByteArray().inputStream(),
                )
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
                    _state.update { it.copy(deleteStep = DeleteProcess.DELETING_USER_LOCAL_MARKERS) }
                }
                async {
                    // Delete user posted images
                    val places = driveService.listFiles(INCLUSIMAP_IMAGE_FOLDER_ID)
                    places.forEach { place ->
                        val userImages = driveService.listFiles(place.id).filter { images ->
                            images.name.extractUserEmail() == _state.value.user?.email
                        }
                        userImages.forEach {
                            println("Deleting file: ${it.name} - ${it.id} posted by user ${_state.value.user?.email}")
                            async { driveService.deleteFile(it.id) }.await()
                        }
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
                        _state.update { it.copy(deleteStep = DeleteProcess.DELETING_USER_IMAGES) }
                    }

                    async {
                        // Delete user comments
                        val json = Json { ignoreUnknownKeys = true }
                        val places = driveService.listFiles(INCLUSIMAP_PLACE_DATA_FOLDER_ID).find {
                            it.name == "places.json"
                        }
                        val placesContent = places?.run {
                            val content = driveService.getFileContent(this.id)?.decodeToString()
                            json.decodeFromString<List<AccessibleLocalMarker>>(
                                content ?: return@async,
                            )
                        }
                        val placesWithoutUserComments = placesContent?.map { place ->
                            place.copy(comments = place.comments.filterNot { it.email == _state.value.user?.email })
                        }
                        val updatedPlaces = json.encodeToString<List<AccessibleLocalMarker>>(
                            placesWithoutUserComments ?: return@async,
                        )
                        driveService.updateFile(
                            places.id,
                            "places.json",
                            updatedPlaces.toByteArray().inputStream(),
                        )
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
                            _state.update { it.copy(deleteStep = DeleteProcess.DELETING_USER_COMMENTS) }
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
                        )
                    }
                } else {
                    _state.update { it.copy(deleteStep = DeleteProcess.SUCCESS) }
                }
                _state.update {
                    it.copy(
                        isDeletingAccount = false,
                        deleteStep = DeleteProcess.NO_OP,
                        isLoginOut = true,
                        isAccountDeleted = true,
                    )
                }
                delay(1000L)
            }.invokeOnCompletion {
                logout()
            }
        }
    }

    private fun checkUserExists() {
        viewModelScope.launch(Dispatchers.IO) {
            val userExists = driveService.listFiles(INCLUSIMAP_USERS_FOLDER_ID).find {
                it.name == _state.value.user?.email
            }
            _state.update {
                it.copy(isLoggedIn = userExists != null)
            }
            val loginData = repository.getLoginInfo(1) ?: LoginEntity.getDefault()
            loginData.isLoggedIn = userExists != null
            if (userExists == null) {
                loginData.userId = null
                loginData.userName = null
                loginData.userEmail = null
                loginData.userPassword = null
            }
            repository.updateLoginInfo(loginData)
        }
    }
}
