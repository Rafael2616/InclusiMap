package com.rafael.inclusimap.core.domain.model

import com.google.api.services.drive.model.File

fun AccessibleLocalMarker.toFullAccessibleLocalMarker(
    images: List<PlaceImage?>,
    imageFolder: List<File>?,
    imageFolderId: String?,
): FullAccessibleLocalMarker =
    FullAccessibleLocalMarker(
        position = position,
        title = title,
        category = category,
        authorEmail = authorEmail,
        comments = comments,
        images = images,
        time = time,
        id = id,
        address = address,
        locatedIn = locatedIn,
        imageFolder = imageFolder,
        imageFolderId = imageFolderId,
        resources = resources,
    )

fun FullAccessibleLocalMarker.toAccessibleLocalMarker(): AccessibleLocalMarker =
    AccessibleLocalMarker(
        position = position,
        title = title,
        category = category,
        authorEmail = authorEmail,
        comments = comments,
        time = time,
        id = id,
        address = address,
        locatedIn = locatedIn,
        resources = resources,
    )
