package com.rafael.inclusimap.feature.contributions.presentation.components

import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieClipSpec
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition
import com.rafael.inclusimap.core.resources.R

@Composable
actual fun KMPLottieAnimation(res: Int) {
    val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(res))
    val progress by animateLottieCompositionAsState(
        composition,
        iterations = 1,
        clipSpec = LottieClipSpec.Progress(0f, 1f),
    )
    LottieAnimation(
        composition = composition,
        progress = { progress },
        modifier = Modifier.size(250.dp),
    )
}

@Composable
actual fun NoContributionsLottieAnimation() {
    KMPLottieAnimation(R.raw.no_contributions_anim)
}

@Composable
actual fun NoInternetLottieAnimation() {
    KMPLottieAnimation(R.raw.no_internet_anim)
}
