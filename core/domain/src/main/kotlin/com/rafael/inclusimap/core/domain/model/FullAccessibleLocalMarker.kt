package com.rafael.inclusimap.core.domain.model

import com.google.api.services.drive.model.File

data class FullAccessibleLocalMarker(
    override var position: Pair<Double, Double> = 0.0 to 0.0,
    override var title: String = "",
    override var category: PlaceCategory? = null,
    override var authorEmail: String = "",
    override var comments: List<Comment> = emptyList(),
    override var time: String = "",
    val images: List<PlaceImage?> = emptyList(),
    val imageFolder: List<File>? = null,
    val imageFolderId: String? = null,
    override var id: String? = null,
    override var address: String = "",
    override var locatedIn: String = "",
    override var resources: List<AccessibilityResource> = emptyList(),
) : BaseLocalMarker
