package com.rafael.inclusimap.feature.map.domain.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.activity.result.contract.ActivityResultContract
import com.google.android.gms.maps.model.LatLng

class OpenInGoogleMapContract : ActivityResultContract<LatLng, Unit>() {
    override fun createIntent(context: Context, input: LatLng): Intent {
        val gmmIntentUri = Uri.parse("geo:${input.latitude},${input.longitude}?z=17")
        return Intent(Intent.ACTION_VIEW, gmmIntentUri)
            .setPackage("com.google.android.apps.maps")
    }

    override fun parseResult(resultCode: Int, intent: Intent?) {
        // Do nothing
    }
}
