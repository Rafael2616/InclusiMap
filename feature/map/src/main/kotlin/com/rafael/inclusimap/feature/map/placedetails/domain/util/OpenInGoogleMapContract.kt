package com.rafael.inclusimap.feature.map.placedetails.domain.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.activity.result.contract.ActivityResultContract
import com.google.android.gms.maps.model.LatLng

class OpenInGoogleMapContract(private val placeUri: Uri?) : ActivityResultContract<LatLng, Unit>() {
    override fun createIntent(context: Context, input: LatLng): Intent {
        val gmmIntentUri = if (placeUri != null) {
            placeUri
        } else {
            Uri.parse("geo:${input.latitude},${input.longitude}?z=17")
        }

        return Intent(Intent.ACTION_VIEW, gmmIntentUri)
            .setPackage("com.google.android.apps.maps")
            .putExtra(Intent.EXTRA_REFERRER_NAME, context.packageName)
    }

    override fun parseResult(resultCode: Int, intent: Intent?) {
        // Do nothing
    }
}

