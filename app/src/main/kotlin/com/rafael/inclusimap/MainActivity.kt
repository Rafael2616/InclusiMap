package com.rafael.inclusimap

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.gms.location.Priority
import com.rafael.inclusimap.data.GoogleDriveService
import com.rafael.inclusimap.ui.InclusiMapGoogleMapScreen
import com.rafael.inclusimap.ui.theme.InclusiMapTheme
import com.rafael.inclusimap.ui.viewmodel.InclusiMapGoogleMapScreenViewModel

class MainActivity : ComponentActivity() {
    private val fusedLocationClient by lazy { LocationServices.getFusedLocationProviderClient(this) }
    private val inclusiMapViewModel: InclusiMapGoogleMapScreenViewModel by viewModels()
    private val driveService = GoogleDriveService()

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        enableLocation()
        setContent {
            val state by inclusiMapViewModel.state.collectAsStateWithLifecycle()
            InclusiMapTheme {
                Scaffold(
                    modifier = Modifier
                        .fillMaxSize()
                ) { innerPadding ->
                    InclusiMapGoogleMapScreen(
                        state = state,
                        onEvent = inclusiMapViewModel::onEvent,
                        driveService = driveService,
                        fusedLocationClient = fusedLocationClient,
                        modifier = Modifier
                            .consumeWindowInsets(innerPadding)
                    )
                }
            }
        }
    }

    private fun enableLocation() {
        val locationRequest = LocationRequest.Builder(
            Priority.PRIORITY_HIGH_ACCURACY,
            10000
        ).build()

        LocationSettingsRequest.Builder()
            .addLocationRequest(locationRequest)
            .setAlwaysShow(true)
            .build()
    }
}
