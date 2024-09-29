package com.rafael.inclusimap.settings.domain.model

import android.os.Build
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.navigation.NavController
import com.rafael.inclusimap.settings.presentation.components.preferences.AboutAppPreference
import com.rafael.inclusimap.settings.presentation.components.preferences.DarkThemePreference
import com.rafael.inclusimap.settings.presentation.components.preferences.DynamicColorsPreference
import com.rafael.inclusimap.settings.presentation.components.preferences.FollowSystemPreference
import com.rafael.inclusimap.settings.presentation.components.preferences.OpenSourceLicensesPreference
import com.rafael.inclusimap.settings.presentation.components.preferences.UpdateHistoryPreference
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@Stable
data class SettingItem(
    val content: (@Composable ((SettingsEvent) -> Unit, SettingsState, NavController) -> Unit)? = null,
    val searchSpecs: SearchItemSpecs,
    val isAvailable: Boolean = true,
) {
    companion object {
        private const val NAMESPACE = "settings"

        @OptIn(ExperimentalUuidApi::class)
        @Stable
        suspend fun getSettingsItems(
            settingsState: SettingsState,
        ): List<SettingItem> = listOf(
            // Follow System
            SettingItem(
                content = { onEvent, state, _ -> FollowSystemPreference(onEvent, state) },
                searchSpecs = SearchItemSpecs(
                    namespace = NAMESPACE,
                    id = Uuid.random().toString(),
                    text = "Siga o sistema",
                ),
            ),
            // Dark Theme
            SettingItem(
                content = { onEvent, state, _ -> DarkThemePreference(onEvent, state) },
                searchSpecs = SearchItemSpecs(
                    namespace = NAMESPACE,
                    id = Uuid.random().toString(),
                    text = "Tema escuro",
                ),
                isAvailable = !settingsState.isFollowingSystemOn,

            ),
            // Dynamic Colors
            SettingItem(
                content = { onEvent, state, _ -> DynamicColorsPreference(onEvent, state) },
                searchSpecs = SearchItemSpecs(
                    namespace = NAMESPACE,
                    id = Uuid.random().toString(),
                    text = "Cores dinâmicas",
                ),
                isAvailable = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S,
            ),
            // Estilo do mapa
            SettingItem(
                content = { onEvent, state, _ -> TODO() },
                searchSpecs = SearchItemSpecs(
                    namespace = NAMESPACE,
                    id = Uuid.random().toString(),
                    text = "Estilo do mapa",
                ),
            ),
            // Background Music
            SettingItem(
                content = { onEvent, state, _ -> TODO() },
                searchSpecs = SearchItemSpecs(
                    namespace = NAMESPACE,
                    id = Uuid.random().toString(),
                    text = "Tema do Mapa",
                ),
            ),
            // Winner Line Color
            SettingItem(
                content = { onEvent, state, _ -> TODO() },
                searchSpecs = SearchItemSpecs(
                    namespace = NAMESPACE,
                    id = Uuid.random().toString(),
                    text = "Configurações da conta",
                ),
            ),
            // Circle Color
            SettingItem(
                content = { onEvent, state, _ -> TODO() },
                searchSpecs = SearchItemSpecs(
                    namespace = NAMESPACE,
                    id = Uuid.random().toString(),
                    text = "Reexibir dicas",
                ),
            ),
            // About App
            SettingItem(
                content = { onEvent, state, _ -> AboutAppPreference(onEvent, state) },
                searchSpecs = SearchItemSpecs(
                    namespace = NAMESPACE,
                    id = Uuid.random().toString(),
                    text = "Sobre o app",
                ),
            ),
            // Update History
            SettingItem(
                content = { _, _, navController -> UpdateHistoryPreference(navController) },
                searchSpecs = SearchItemSpecs(
                    namespace = NAMESPACE,
                    id = Uuid.random().toString(),
                    text = "Histórico de atualizações",
                ),
            ),
            // Licences Screen
            SettingItem(
                content = { _, _, navController -> OpenSourceLicensesPreference(navController) },
                searchSpecs = SearchItemSpecs(
                    namespace = NAMESPACE,
                    id = Uuid.random().toString(),
                    text = "Licenças",
                ),
            ),
        )
    }
}
