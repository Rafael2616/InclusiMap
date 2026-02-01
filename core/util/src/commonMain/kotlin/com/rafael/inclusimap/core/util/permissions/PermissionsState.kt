package com.rafael.inclusimap.core.util.permissions

import dev.icerock.moko.permissions.PermissionState

data class PermissionsState(
    val storagePermissionState: PermissionState = PermissionState.NotDetermined,
    val writeStoragePermissionState: PermissionState = PermissionState.NotDetermined,
    val galleryPermissionState: PermissionState = PermissionState.NotDetermined,
    val recordAudioPermissionState: PermissionState = PermissionState.NotDetermined,
    val locationPermissionState: PermissionState = PermissionState.NotDetermined,
)
