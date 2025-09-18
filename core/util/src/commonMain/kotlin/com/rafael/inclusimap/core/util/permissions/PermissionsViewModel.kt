package com.rafael.inclusimap.core.util.permissions

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.icerock.moko.permissions.DeniedAlwaysException
import dev.icerock.moko.permissions.DeniedException
import dev.icerock.moko.permissions.PermissionState
import dev.icerock.moko.permissions.PermissionsController
import dev.icerock.moko.permissions.RequestCanceledException
import dev.icerock.moko.permissions.location.LocationPermission
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class PermissionsViewModel(
    private val permissionsController: PermissionsController,
) : ViewModel() {
    var permissionsState = MutableStateFlow(PermissionsState())
        private set

    init {
        viewModelScope.launch {
            permissionsState.update {
                it.copy(
                    locationPermissionState = permissionsController.getPermissionState(
                        LocationPermission,
                    ),
                )
            }
        }
    }

    suspend fun provideOrRequestLocationPermission() {
        try {
            permissionsController.providePermission(LocationPermission)
            permissionsState.update { it.copy(locationPermissionState = PermissionState.Granted) }
        } catch (_: DeniedAlwaysException) {
            permissionsState.update { it.copy(locationPermissionState = PermissionState.DeniedAlways) }
        } catch (_: DeniedException) {
            permissionsState.update { it.copy(locationPermissionState = PermissionState.Denied) }
        } catch (e: RequestCanceledException) {
            e.printStackTrace()
        }
    }
}
