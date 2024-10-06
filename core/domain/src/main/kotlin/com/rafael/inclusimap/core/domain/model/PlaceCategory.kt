package com.rafael.inclusimap.core.domain.model

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
    OTHER,
}

fun PlaceCategory.toCategoryName() = when (this) {
    PlaceCategory.SHOPPING -> "Shopping"
    PlaceCategory.MARKET -> "Supemercado"
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
    PlaceCategory.OTHER -> "Outro"
}
