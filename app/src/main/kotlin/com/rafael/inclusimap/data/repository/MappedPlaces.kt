package com.rafael.inclusimap.data.repository

import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.MarkerState
import com.rafael.inclusimap.domain.AccessibleLocalMarker
import com.rafael.inclusimap.domain.Comment

val mappedPlaces = listOf(
    AccessibleLocalMarker(
        position = -2.9961928457018034 to -47.34839804470539,
        title = "Lago Verde",
        author = "@InclusiMap",
        category = "Ponto Turístico",
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
        id = "d4dda9c4-4e88-4bc5-a7ee-71a23d046c0c",
        time = "2024-09-22T13:50:18.272Z"
    ),
    AccessibleLocalMarker(
        position = -2.9751372910178024 to -47.360620871186256,
        title = "Top Alimentos",
        author = "@InclusiMap",
        category = "Supermercado",
        comments = listOf(
            Comment(
                postDate = "17/08/2024",
                id = 1,
                name = "Rafael Moura",
                body = "O local possui uma certa acessibilidade, como um estacionamento acessível e algumas rampas, embora não seja completa",
                email = "",
                accessibilityRate = 2,
            ),
        ),
        id = "1df7905d-0757-4592-943c-0b14c304f849",
        time = "2024-09-22T13:50:18.272Z"
    ),
    AccessibleLocalMarker(
            position = -2.987957301132429 to -47.357158809900284,
        title = "Mix Mateus",
        author = "@InclusiMap",
        category = "Supermercado",
        comments = listOf(
            Comment(
                postDate = "17/08/2024",
                id = 1,
                name = "Cléber Felix",
                body = "O local possui acessibilidade, perfeito! \nRecomendo",
                email = "",
                accessibilityRate = 3,
            ),
        ),
        id = "ab1a96da-7067-4a6d-bacb-8f8c3bb1bf9a",
        time = "2024-09-22T13:50:18.272Z"
    ),
    AccessibleLocalMarker(
        position = -2.985339672724703 to -47.35865514725447,
        title = "UEPA",
        author = "@InclusiMap",
        category = "Universidade",
        comments = listOf(
            Comment(
                postDate = "15/08/2024",
                id = 1,
                name = "Alcilia Maria",
                body = "Não exite acessibilidade significativa nesse local, como um estacionamento acessivel ou rampas, nem mesmo banheiros adaptados",
                email = "",
                accessibilityRate = 1,
            ),
        ),
        id = "48c3f14d-78be-4c1e-82b0-11dc67099b56",
        time = "2024-09-22T13:50:18.272Z"
    ),
    AccessibleLocalMarker(
        position = -2.9951669634580633 to -47.35554747283459,
        title = "Shopping Diamond",
        author = "@InclusiMap",
        category = "Shopping",
        comments = listOf(
            Comment(
                postDate = "22/07/2024",
                id = 1,
                name = "Rogério Baena",
                body = "O local é até bem acessível, exixtem algumas cadeiras adaptadas e também banheiros, com um tamanho bem grande para pessoas com mobilidade reduzida, além de rampas",
                email = "",
                accessibilityRate = 3,
            ),
            Comment(
                postDate = "01/08/2024",
                id = 1,
                name = "Abner Farias",
                body = "Quanto a acessibilidade para cadeirantes o local realmente cumpre com as expectativas, possui uma estrutura super acessível",
                email = "",
                accessibilityRate = 2,
            ),
        ),
        id = "875fc8c1-3e41-4f56-b913-ef3b02fad399",
        time = "2024-09-22T13:50:18.272Z"
    ),
    AccessibleLocalMarker(
        position = -2.9948525691968286 to -47.35381476581097,
        title = "Banco do Brasil",
        author = "@InclusiMap",
        category = "Banco",
        comments = listOf(
            Comment(
                postDate = "12/10/2024",
                id = 1,
                name = "Rafael de Moura",
                body = "O local é bastante acessível, não há nenhum incoveniente notável para cadeirantes",
                email = "",
                accessibilityRate = 3,
            ),
        ),
        id = "fd9aa418-bc04-46fe-8974-f0bb8c400969",
        time = "2024-09-22T13:50:18.272Z"
    ),
)
