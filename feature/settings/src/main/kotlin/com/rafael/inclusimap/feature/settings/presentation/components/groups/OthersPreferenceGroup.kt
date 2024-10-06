package com.rafael.inclusimap.feature.settings.presentation.components.groups

import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.rafael.inclusimap.core.settings.domain.model.SettingsEvent
import com.rafael.inclusimap.feature.settings.presentation.components.preferences.AboutAppPreference
import com.rafael.inclusimap.feature.settings.presentation.components.preferences.HowAppWorksPreference
import com.rafael.inclusimap.feature.settings.presentation.components.preferences.OpenSourceLicensesPreference
import com.rafael.inclusimap.feature.settings.presentation.components.preferences.TermsAndConditionsPreference
import com.rafael.inclusimap.feature.settings.presentation.components.templates.PreferenceGroup

@Composable
fun OthersPreferenceGroup(
    onEvent: (SettingsEvent) -> Unit,
    onAppIntroEvent: (Boolean) -> Unit,
    navController: NavController,
    modifier: Modifier = Modifier,
) {
    PreferenceGroup(
        heading = "Outros",
        modifier = modifier.padding(bottom = 8.dp),
    ) {
        HowAppWorksPreference(onAppIntroEvent)
        AboutAppPreference(navController)
        TermsAndConditionsPreference(onEvent)
        OpenSourceLicensesPreference(navController)
    }
}
