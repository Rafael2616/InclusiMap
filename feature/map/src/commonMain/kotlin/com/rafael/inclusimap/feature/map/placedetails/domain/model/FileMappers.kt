package com.rafael.inclusimap.feature.map.placedetails.domain.model

import com.rafael.inclusimap.core.services.domain.DriveFile
import com.rafael.inclusimap.core.util.map.model.File

fun DriveFile.toFile() = File(
    name = name,
    id = id,
)
