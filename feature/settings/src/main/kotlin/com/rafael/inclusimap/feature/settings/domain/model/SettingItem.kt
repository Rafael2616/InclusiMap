package com.rafael.inclusimap.feature.settings.domain.model

import android.os.Build
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.navigation.NavController
import com.rafael.inclusimap.core.settings.domain.model.SettingsEvent
import com.rafael.inclusimap.core.settings.domain.model.SettingsState
import com.rafael.inclusimap.feature.settings.presentation.components.preferences.AboutAppPreference
import com.rafael.inclusimap.feature.settings.presentation.components.preferences.DarkThemePreference
import com.rafael.inclusimap.feature.settings.presentation.components.preferences.DynamicColorsPreference
import com.rafael.inclusimap.feature.settings.presentation.components.preferences.FollowSystemPreference
import com.rafael.inclusimap.feature.settings.presentation.components.preferences.LogoutPreference
import com.rafael.inclusimap.feature.settings.presentation.components.preferences.MapTypePreference
import com.rafael.inclusimap.feature.settings.presentation.components.preferences.OpenSourceLicensesPreference
import com.rafael.inclusimap.feature.settings.presentation.components.preferences.UpdatePasswordPreference
import com.rafael.inclusimap.settings.domain.model.SearchItemSpecs
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
        fun getSettingsItems(
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
                content = { onEvent, state, _ -> MapTypePreference(onEvent, state) },
                searchSpecs = SearchItemSpecs(
                    namespace = NAMESPACE,
                    id = Uuid.random().toString(),
                    text = "Estilo do mapa",
                ),
            ),
            // Atualizar senha
            SettingItem(
                content = { _, _, navController -> UpdatePasswordPreference(navController) },
                searchSpecs = SearchItemSpecs(
                    namespace = NAMESPACE,
                    id = Uuid.random().toString(),
                    text = "Atualizar Senha",
                ),
            ),
            // Desconectar
            SettingItem(
                content = { onEvent, _, navController -> LogoutPreference(navController, onEvent) },
                searchSpecs = SearchItemSpecs(
                    namespace = NAMESPACE,
                    id = Uuid.random().toString(),
                    text = "Configurações da conta",
                ),
            ),
            // Reexibir dicas
            SettingItem(
                content = { onEvent, state, _ -> TODO() },
                searchSpecs = SearchItemSpecs(
                    namespace = NAMESPACE,
                    id = Uuid.random().toString(),
                    text = "Reexibir dicas",
                ),
            ),
            // Sobre o app
            SettingItem(
                content = { _, _, navController -> AboutAppPreference(navController) },
                searchSpecs = SearchItemSpecs(
                    namespace = NAMESPACE,
                    id = Uuid.random().toString(),
                    text = "Sobre o app",
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
