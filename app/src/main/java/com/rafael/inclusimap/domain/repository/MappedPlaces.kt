package com.rafael.inclusimap.domain.repository

import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.MarkerState
import com.rafael.inclusimap.domain.AccessibleLocalMarker
import com.rafael.inclusimap.domain.Comment

val mappedPlaces = listOf(
    AccessibleLocalMarker(
        markerState = MarkerState(
            position = LatLng(-2.9961928457018034, -47.34839804470539)
        ),
        title = "Lago Verde",
        description = "Ponto Turístico",
        comments = listOf(
            Comment(
                postDate = "21/08/2024",
                id = 1,
                name = "Rafael de Moura",
                body = "Falta um pouco de acessibilidade, mas tudo bem. \nRecomendo \nMuito bom",
                email = "",
                accessibilityRate = 1,
            ),
            Comment(
                postDate = "23/08/2024",
                id = 2,
                name = "Alcilia Maria",
                body = "Lugar bom pra sorrir de cadeirante \nRecomendo \nMuito bom",
                email = "",
                accessibilityRate = 1,
            )
        ),
    ),
    AccessibleLocalMarker(
        markerState = MarkerState(
            position = LatLng(-2.9751372910178024, -47.360620871186256)
        ),
        title = "Top Alimentos",
        description = "Supermercado",
        comments = listOf(
            Comment(
                postDate = "17/08/2024",
                id = 1,
                name = "Rafael Moura",
                body = "O local possui uma certa acessibilidade, como um estacionamento acessível e algumas rampas, embora não seja completa",
                email = "",
                accessibilityRate = 2,
            ),
        )
    ),
    AccessibleLocalMarker(
        markerState = MarkerState(
            position = LatLng(-2.987957301132429, -47.357158809900284)
        ),
        title = "Mix Mateus",
        description = "Supermercado",
        comments = listOf(
            Comment(
                postDate = "17/08/2024",
                id = 1,
                name = "Cléber Felix",
                body = "O local possui acessibilidade, perfeito! \nRecomendo",
                email = "",
                accessibilityRate = 3,
            ),
        )
    ),
    AccessibleLocalMarker(
        markerState = MarkerState(
            position = LatLng(-2.985339672724703, -47.35865514725447)
        ),
        title = "UEPA",
        description = "Universidade",
        comments = listOf(
            Comment(
                postDate = "15/08/2024",
                id = 1,
                name = "Alcilia Maria",
                body = "Não exite acessibilidade significativa nesse local, como um estacionamento acessivel ou rampas, nem mesmo banheiros adaptados",
                email = "",
                accessibilityRate = 1,
            ),
        )
    )
)
