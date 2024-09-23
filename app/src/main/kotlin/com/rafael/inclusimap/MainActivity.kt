package com.rafael.inclusimap

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.gms.location.Priority
import com.rafael.inclusimap.data.GoogleDriveService
import com.rafael.inclusimap.ui.InclusiMapGoogleMapScreen
import com.rafael.inclusimap.ui.theme.InclusiMapTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        enableLocation()
        val driveService = GoogleDriveService()
        setContent {
            InclusiMapTheme {
                Scaffold(
                    modifier = Modifier
                        .fillMaxSize()
                ) { innerPadding ->
                    InclusiMapGoogleMapScreen(
                        driveService = driveService,
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
