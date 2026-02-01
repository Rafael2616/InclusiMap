package com.rafael.inclusimap.core.util.map.model

interface BaseLocalMarker {
    var position: Pair<Double, Double>
    var title: String
    var category: PlaceCategory?
    var authorEmail: String
    var comments: List<Comment>
    var time: String
    var id: String?
    var address: String
    var locatedIn: String
    var resources: List<AccessibilityResource>
}
