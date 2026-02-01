package com.rafael.inclusimap.feature.contributions.presentation.components

import androidx.compose.runtime.Composable

@Composable
expect fun KMPLottieAnimation(
    res: Int,
)

@Composable
expect fun NoInternetLottieAnimation()

@Composable
expect fun NoContributionsLottieAnimation()
