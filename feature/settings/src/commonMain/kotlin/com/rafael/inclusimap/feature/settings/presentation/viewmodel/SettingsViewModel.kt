package com.rafael.inclusimap.feature.settings.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rafael.inclusimap.feature.settings.domain.model.SettingsEntity
import com.rafael.inclusimap.feature.settings.domain.model.SettingsEvent
import com.rafael.inclusimap.feature.settings.domain.model.SettingsState
import com.rafael.inclusimap.feature.settings.domain.repository.SettingsRepository
import com.rafael.libs.maps.interop.model.MapType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val repository: SettingsRepository,
) : ViewModel() {

    // Get default values for settings from SettingsEntity for first app usage
    private val defaultSettings = SettingsEntity.getDefaultSettings()

    private val _state = MutableStateFlow(SettingsState())
    val state = _state.asStateFlow()

    // Initialize ViewModel StateFlows variables with corresponding values from SettingsRepository
    init {
        viewModelScope.launch(Dispatchers.IO) {
            val settings = repository.getAllSettingsValues(1) ?: defaultSettings
            _state.update {
                it.copy(
                    isDarkThemeOn = settings.isDarkThemeOn,
                    isDynamicColorsOn = settings.isDynamicColorsOn,
                    isFollowingSystemOn = settings.isFollowingSystemOn,
                    appVersion = settings.appVersion,
                    mapType = settings.mapType.toMapType(),
                    searchHistoryEnabled = settings.searchHistoryEnabled,
                    isProfileSettingsTipShown = settings.isProfileSettingsTipShown,
                )
            }
        }
    }

    fun onEvent(event: SettingsEvent) {
        when (event) {
            SettingsEvent.ToggleIsDarkThemeOn -> {
                _state.update { it.copy(isDarkThemeOn = !it.isDarkThemeOn) }

                viewModelScope.launch(Dispatchers.IO) {
                    val settings = repository.getAllSettingsValues(1) ?: defaultSettings
                    settings.isDarkThemeOn = state.value.isDarkThemeOn
                    repository.setAllSettingsValues(settings)
                }
            }

            SettingsEvent.ToggleIsDynamicColorsOn -> {
                _state.update { it.copy(isDynamicColorsOn = !it.isDynamicColorsOn) }

                viewModelScope.launch(Dispatchers.IO) {
                    val settings = repository.getAllSettingsValues(1) ?: defaultSettings
                    settings.isDynamicColorsOn = state.value.isDynamicColorsOn
                    repository.setAllSettingsValues(settings)
                }
            }

            is SettingsEvent.ToggleIsFollowingSystemOn -> {
                _state.update {
                    it.copy(
                        isFollowingSystemOn = !it.isFollowingSystemOn,
                        isDarkThemeOn = event.isSystemInDarkTheme,
                    )
                }

                viewModelScope.launch(Dispatchers.IO) {
                    val settings = repository.getAllSettingsValues(1) ?: defaultSettings
                    settings.isFollowingSystemOn = state.value.isFollowingSystemOn
                    settings.isDarkThemeOn = event.isSystemInDarkTheme

                    repository.setAllSettingsValues(settings)
                }
            }

            is SettingsEvent.SetIsDarkThemeOn -> {
                _state.update { it.copy(isDarkThemeOn = event.value) }

                viewModelScope.launch(Dispatchers.IO) {
                    val settings = repository.getAllSettingsValues(1) ?: defaultSettings
                    settings.isDarkThemeOn = state.value.isDarkThemeOn
                    repository.setAllSettingsValues(settings)
                }
            }

            SettingsEvent.ToggleSearchHistoryEnabled -> {
                _state.update { it.copy(searchHistoryEnabled = !it.searchHistoryEnabled) }

                viewModelScope.launch(Dispatchers.IO) {
                    val settings = repository.getAllSettingsValues(1) ?: defaultSettings
                    settings.searchHistoryEnabled = state.value.searchHistoryEnabled
                    repository.setAllSettingsValues(settings)
                }
            }

            is SettingsEvent.ShowAboutAppCard -> {
                _state.update { it.copy(isAboutShown = event.value) }
            }

            is SettingsEvent.SetMapType -> {
                _state.update { it.copy(mapType = event.type) }
                viewModelScope.launch(Dispatchers.IO) {
                    val settings = repository.getAllSettingsValues(1) ?: defaultSettings
                    settings.mapType = state.value.mapType.toInt()
                    repository.setAllSettingsValues(settings)
                }
            }
            is SettingsEvent.SetIsProfileSettingsTipShown -> {
                _state.update { it.copy(isProfileSettingsTipShown = event.value) }

                viewModelScope.launch(Dispatchers.IO) {
                    val settings = repository.getAllSettingsValues(1) ?: defaultSettings
                    settings.isProfileSettingsTipShown = state.value.isProfileSettingsTipShown
                    repository.setAllSettingsValues(settings)
                }
            }

            is SettingsEvent.ShowLogoutDialog -> _state.update {
                it.copy(showLogoutDialog = event.value)
            }

            is SettingsEvent.ShowDeleteAccountDialog -> _state.update {
                it.copy(showDeleteAccountDialog = event.value)
            }

            is SettingsEvent.OpenTermsAndConditions -> _state.update {
                it.copy(showTermsAndConditions = event.value)
            }

            is SettingsEvent.ShowProfilePictureSettings -> _state.update {
                it.copy(showProfilePictureSettings = event.value)
            }
        }
    }

    private fun MapType.toInt() = when (this) {
        MapType.NORMAL -> 1
        MapType.SATELLITE -> 2
        MapType.TERRAIN -> 3
        MapType.HYBRID -> 4
    }

    private fun Int.toMapType() = when (this) {
        1 -> MapType.NORMAL
        2 -> MapType.SATELLITE
        3 -> MapType.TERRAIN
        4 -> MapType.HYBRID
        else -> MapType.NORMAL
    }
}
