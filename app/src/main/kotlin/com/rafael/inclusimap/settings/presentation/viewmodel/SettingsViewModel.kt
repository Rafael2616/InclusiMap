package com.rafael.inclusimap.settings.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.maps.android.compose.MapType
import com.rafael.inclusimap.settings.domain.model.SettingsEntity
import com.rafael.inclusimap.settings.domain.model.SettingsEvent
import com.rafael.inclusimap.settings.domain.model.SettingsState
import com.rafael.inclusimap.settings.domain.repository.SettingsRepository
import kotlinx.coroutines.Dispatchers
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
        }
    }

    private fun MapType.toInt() = when (this) {
        MapType.NORMAL -> 1
        MapType.SATELLITE -> 2
        MapType.TERRAIN -> 3
        MapType.HYBRID -> 4
        else -> 1
    }
    private fun Int.toMapType() = when (this) {
        1 -> MapType.NORMAL
        2 -> MapType.SATELLITE
        3 -> MapType.TERRAIN
        4 -> MapType.HYBRID
        else -> MapType.NORMAL
    }
}
