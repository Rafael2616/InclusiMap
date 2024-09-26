package com.rafael.inclusimap.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rafael.inclusimap.data.GoogleDriveService
import com.rafael.inclusimap.domain.LoginEvent
import com.rafael.inclusimap.domain.LoginState
import com.rafael.inclusimap.domain.RegisteredUser
import com.rafael.inclusimap.domain.User
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

class LoginViewModel : ViewModel() {
    private val driveService: GoogleDriveService = GoogleDriveService()
    private val _state = MutableStateFlow(LoginState())
    val state = _state.asStateFlow()

    fun onEvent(event: LoginEvent) {
        when (event) {
            is LoginEvent.SetIsNewUser -> {
                _state.update {
                    it.copy(
                        isNewUser = event.isNewUser
                    )
                }
            }

            is LoginEvent.OnLogin -> login(event.registeredUser)
            is LoginEvent.OnRegisterNewUser -> registerNewUser(event.user)
        }
    }

    @OptIn(ExperimentalUuidApi::class)
    private fun registerNewUser(newUser: User) {
        _state.update {
            it.copy(
                isRegistering = true
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
                            INCLUSIMAP_USERS_FOLDER_ID
                        ).any { userFile ->
                            userFile.name.split(".json")[0] == newUser.email
                        }.also { isRegistered ->
                            println("User already registered? $isRegistered")
                        },
                        isRegistering = false
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
                            INCLUSIMAP_USERS_FOLDER_ID
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
                        ?: throw IllegalStateException("Folder not found: Maybe an issue has occurred while creating the folder")
                )
            }.await()
        }.invokeOnCompletion {
            if (!_state.value.userAlreadyRegistered) {
                _state.update {
                    it.copy(
                        user = user,
                        isLoggedIn = true,
                        isRegistering = false
                    )
                }
            }
        }
    }

    private fun login(registeredUser: RegisteredUser) {
        _state.update {
            it.copy(
                isRegistering = true
            )
        }
        viewModelScope.launch(Dispatchers.IO) {
            async {
                _state.update {
                    it.copy(
                        userAlreadyRegistered = driveService.listFiles(
                            INCLUSIMAP_USERS_FOLDER_ID
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
                _state.update {
                    it.copy(
                        isRegistering = false
                    )
                }
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

                val userLoginFileContent = driveService.getFileContent(userLoginFile?.id ?: throw IllegalStateException("User not found")).decodeToString()
                println("User file content: $userLoginFileContent")

                val userObj = json.decodeFromString<User>(userLoginFileContent)
                if (userObj.password != registeredUser.password) {
                    _state.update {
                        it.copy(
                            isPasswordCorrect = false,
                            isRegistering = false
                        )
                    }
                    println("Password is incorrect")
                    return@async
                }
                _state.update {
                    it.copy(
                        isPasswordCorrect = true,
                        user = User(
                            id = userObj.id,
                            name = userObj.name,
                            email = userObj.email,
                            password = userObj.password,
                        )
                    )
                }
            }.await()
        }.invokeOnCompletion {
            if (_state.value.userAlreadyRegistered && _state.value.isPasswordCorrect) {
                _state.update {
                    it.copy(
                        isLoggedIn = true,
                        isRegistering = false
                    )
                }
            }
        }
    }
}

const val INCLUSIMAP_USERS_FOLDER_ID = "1Vz3Ac1P9SkkNwObYB51eMo2IcVIQwmjD"