package com.rafael.inclusimap.core.domain.model

import androidx.compose.ui.graphics.Color

data class AccessibilityChipItem(
    val name: String,
    val color: Color,
    val type: AccessibilityType,
    val description: String,
) {
    companion object {
        fun get(): List<AccessibilityChipItem> = listOf(
            AccessibilityChipItem(
                name = "Acessível",
                type = AccessibilityType.ACCESSIBLE,
                color = Color.Green,
                description = "O local possui um suporte estrutural amplo e portanto é considerado como acessível para cadeirantes",
            ),
            AccessibilityChipItem(
                name = "Acessibilidade Média",
                type = AccessibilityType.MEDIUM,
                color = Color.Yellow,
                description = "O local possui algumas adaptações para acomodar cadeirantes, pórem faltam alguns recursos para atender aos requisitos de acessilidade completa",
            ),
            AccessibilityChipItem(
                name = "Sem acessibilidade",
                type = AccessibilityType.INACCESSIBLE,
                color = Color.Red,
                description = "Não existem suporte estrutural suficiente ou muito pouco, tornando o local inacessível para cadeirantes",
            ),
            AccessibilityChipItem(
                name = "Sem dados de acessibilidade",
                type = AccessibilityType.UNKNOWN,
                color = Color.Cyan,
                description = "Poucas pessoas avaliaram o local ou ele foi recentemente cadastrado. Considere contribuir com uma avaliação para ajudar a comunidade",
            ),
        )
    }
}
