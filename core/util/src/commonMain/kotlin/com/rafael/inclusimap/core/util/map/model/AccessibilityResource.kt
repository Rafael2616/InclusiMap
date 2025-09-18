package com.rafael.inclusimap.core.util.map.model

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Airlines
import androidx.compose.material.icons.outlined.Attachment
import androidx.compose.material.icons.outlined.Bathroom
import androidx.compose.material.icons.outlined.Elevator
import androidx.compose.material.icons.outlined.EmojiPeople
import androidx.compose.material.icons.outlined.LocalOffer
import androidx.compose.material.icons.outlined.LocalParking
import androidx.compose.material.icons.outlined.SensorDoor
import androidx.compose.material.icons.outlined.ShoppingCart
import androidx.compose.material.icons.outlined.SpaceBar
import androidx.compose.material.icons.outlined.TextRotationAngleup
import androidx.compose.material.icons.outlined.WheelchairPickup
import androidx.compose.ui.graphics.vector.ImageVector
import kotlinx.serialization.Serializable

@Serializable
data class AccessibilityResource(
    val resource: Resource = Resource.NOT_APPLICABLE,
    val lastModified: String = "",
    val lastModifiedBy: String = "",
)

enum class Resource(
    val displayName: String,
) {
    WHEELCHAIR("Cadeira de rodas"),
    RAMP("Rampa acessível"),
    ELEVATOR("Elevador adaptado"),
    WIDE_CORRIDORS("Corredores espaçosos"),
    FEW_ELEVATIONS("Poucas elevações"),
    ADAPTED_BATHROOM("Banheiro adaptado"),
    PRIORITY_CHECKOUT("Caixa preferencial"),
    SPECIAL_PARKING("Estacionamento especial"),
    ADAPTED_CART("Carrinho adaptado"),
    HUMAN_ASSISTANCE("Assistência humana"),
    AUTOMATIC_DOOR("Porta automática ou de fácil manuseio"),
    NOT_APPLICABLE("Não aplicável"),
}

fun String.toResourceIcon(): ImageVector = when (this) {
    "Cadeira de rodas" -> Icons.Outlined.WheelchairPickup
    "Rampa acessível" -> Icons.Outlined.Airlines
    "Elevador adaptado" -> Icons.Outlined.Elevator
    "Corredores espaçosos" -> Icons.Outlined.SpaceBar
    "Poucas elevações" -> Icons.Outlined.TextRotationAngleup
    "Banheiro adaptado" -> Icons.Outlined.Bathroom
    "Caixa preferencial" -> Icons.Outlined.LocalOffer
    "Estacionamento especial" -> Icons.Outlined.LocalParking
    "Carrinho adaptado" -> Icons.Outlined.ShoppingCart
    "Assistência humana" -> Icons.Outlined.EmojiPeople
    "Porta automática ou de fácil manuseio" -> Icons.Outlined.SensorDoor
    else -> Icons.Outlined.Attachment
}
