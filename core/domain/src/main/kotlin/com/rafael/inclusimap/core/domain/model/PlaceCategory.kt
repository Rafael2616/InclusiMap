package com.rafael.inclusimap.core.domain.model

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.DirectionsWalk
import androidx.compose.material.icons.outlined.BakeryDining
import androidx.compose.material.icons.outlined.BeachAccess
import androidx.compose.material.icons.outlined.Church
import androidx.compose.material.icons.outlined.Computer
import androidx.compose.material.icons.outlined.Fastfood
import androidx.compose.material.icons.outlined.Forest
import androidx.compose.material.icons.outlined.Hotel
import androidx.compose.material.icons.outlined.Icecream
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material.icons.outlined.LocalAirport
import androidx.compose.material.icons.outlined.LocalCafe
import androidx.compose.material.icons.outlined.LocalGasStation
import androidx.compose.material.icons.outlined.LocalHospital
import androidx.compose.material.icons.outlined.LocalParking
import androidx.compose.material.icons.outlined.LocalPharmacy
import androidx.compose.material.icons.outlined.Park
import androidx.compose.material.icons.outlined.Restaurant
import androidx.compose.material.icons.outlined.School
import androidx.compose.material.icons.outlined.ShoppingBag
import androidx.compose.material.icons.outlined.ShoppingCart
import androidx.compose.material.icons.outlined.WineBar
import com.rafael.inclusimap.core.resources.icons.BankNotes
import com.rafael.inclusimap.core.resources.icons.Cleaning
import com.rafael.inclusimap.core.resources.icons.Library
import com.rafael.inclusimap.core.resources.icons.Shirt

enum class PlaceCategory {
    SHOPPING,
    MARKET,
    SQUARE,
    CLOTHING_STORE,
    ELECTRONICS_STORE,
    HOSPITAL,
    RESTAURANT,
    BURGER,
    SCHOOL,
    UNIVERSITY,
    BANK,
    HOTEL,
    CAFE,
    PARK,
    BAKERY,
    PARKING,
    ICE_CREAM_PARLOR,
    GYM,
    CHURCH,
    BAR,
    LEISURE,
    LIBRARY,
    GAS_STATION,
    AIRPORT,
    BEAUTY_SALON,
    PHARMACY,
    OTHER,
}

fun PlaceCategory.toCategoryName() = when (this) {
    PlaceCategory.SHOPPING -> "Shopping"
    PlaceCategory.MARKET -> "Supermercado"
    PlaceCategory.SQUARE -> "Praça"
    PlaceCategory.CLOTHING_STORE -> "Loja de Roupas"
    PlaceCategory.ELECTRONICS_STORE -> "Loja de Eletrônicos"
    PlaceCategory.HOSPITAL -> "Hospital"
    PlaceCategory.RESTAURANT -> "Restaurante"
    PlaceCategory.BURGER -> "Hamburgueria"
    PlaceCategory.SCHOOL -> "Escola"
    PlaceCategory.UNIVERSITY -> "Universidade"
    PlaceCategory.BANK -> "Banco"
    PlaceCategory.HOTEL -> "Hotel"
    PlaceCategory.CAFE -> "Café"
    PlaceCategory.PARK -> "Parque"
    PlaceCategory.LEISURE -> "Lazer"
    PlaceCategory.BAKERY -> "Padaria"
    PlaceCategory.PARKING -> "Estacionamento"
    PlaceCategory.ICE_CREAM_PARLOR -> "Sorveteria"
    PlaceCategory.GYM -> "Academia"
    PlaceCategory.CHURCH -> "Igreja"
    PlaceCategory.BAR -> "Bar"
    PlaceCategory.LIBRARY -> "Biblioteca"
    PlaceCategory.GAS_STATION -> "Posto de Combustível"
    PlaceCategory.AIRPORT -> "Aeroporto"
    PlaceCategory.BEAUTY_SALON -> "Salão de Beleza"
    PlaceCategory.PHARMACY -> "Farmácia"
    PlaceCategory.OTHER -> "Outro"
}

fun String.toPlaceCategory() = when (this) {
    "Shopping" -> PlaceCategory.SHOPPING
    "Supermercado" -> PlaceCategory.MARKET
    "Praça" -> PlaceCategory.SQUARE
    "Loja de Roupas" -> PlaceCategory.CLOTHING_STORE
    "Loja de Eletrônicos" -> PlaceCategory.ELECTRONICS_STORE
    "Hospital" -> PlaceCategory.HOSPITAL
    "Restaurante" -> PlaceCategory.RESTAURANT
    "Hamburgueria" -> PlaceCategory.BURGER
    "Escola" -> PlaceCategory.SCHOOL
    "Universidade" -> PlaceCategory.UNIVERSITY
    "Banco" -> PlaceCategory.BANK
    "Hotel" -> PlaceCategory.HOTEL
    "Café" -> PlaceCategory.CAFE
    "Parque" -> PlaceCategory.PARK
    "Lazer" -> PlaceCategory.LEISURE
    "Padaria" -> PlaceCategory.BAKERY
    "Estacionamento" -> PlaceCategory.PARKING
    "Sorveteria" -> PlaceCategory.ICE_CREAM_PARLOR
    "Academia" -> PlaceCategory.GYM
    "Igreja" -> PlaceCategory.CHURCH
    "Bar" -> PlaceCategory.BAR
    "Biblioteca" -> PlaceCategory.LIBRARY
    "Posto de Combustível" -> PlaceCategory.GAS_STATION
    "Aeroporto" -> PlaceCategory.AIRPORT
    "Salão de Beleza" -> PlaceCategory.BEAUTY_SALON
    "Farmácia" -> PlaceCategory.PHARMACY
    "Outro" -> PlaceCategory.OTHER
    else -> PlaceCategory.OTHER
}

fun PlaceCategory.icon() = when (this) {
    PlaceCategory.SHOPPING -> Icons.Outlined.ShoppingBag
    PlaceCategory.MARKET -> Icons.Outlined.ShoppingCart
    PlaceCategory.SQUARE -> Icons.Outlined.Park
    PlaceCategory.CLOTHING_STORE -> Icons.Outlined.Shirt
    PlaceCategory.ELECTRONICS_STORE -> Icons.Outlined.Computer
    PlaceCategory.HOSPITAL -> Icons.Outlined.LocalHospital
    PlaceCategory.RESTAURANT -> Icons.Outlined.Restaurant
    PlaceCategory.BURGER -> Icons.Outlined.Fastfood
    PlaceCategory.SCHOOL -> Icons.Outlined.School
    PlaceCategory.UNIVERSITY -> Icons.Outlined.School
    PlaceCategory.BANK -> Icons.Outlined.BankNotes
    PlaceCategory.HOTEL -> Icons.Outlined.Hotel
    PlaceCategory.CAFE -> Icons.Outlined.LocalCafe
    PlaceCategory.PARK -> Icons.Outlined.Forest
    PlaceCategory.BAKERY -> Icons.Outlined.BakeryDining
    PlaceCategory.PARKING -> Icons.Outlined.LocalParking
    PlaceCategory.ICE_CREAM_PARLOR -> Icons.Outlined.Icecream
    PlaceCategory.GYM -> Icons.AutoMirrored.Outlined.DirectionsWalk
    PlaceCategory.CHURCH -> Icons.Outlined.Church
    PlaceCategory.BAR -> Icons.Outlined.WineBar
    PlaceCategory.LEISURE -> Icons.Outlined.BeachAccess
    PlaceCategory.LIBRARY -> Icons.Outlined.Library
    PlaceCategory.GAS_STATION -> Icons.Outlined.LocalGasStation
    PlaceCategory.AIRPORT -> Icons.Outlined.LocalAirport
    PlaceCategory.BEAUTY_SALON -> Icons.Outlined.Cleaning
    PlaceCategory.PHARMACY -> Icons.Outlined.LocalPharmacy
    PlaceCategory.OTHER -> Icons.Outlined.Image
}
