package com.rafael.inclusimap.core.util.map.model


data class PlaceImage(
    val userEmail: String?,
    val image: ByteArray,
    val placeID: String,
    val name: String,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as PlaceImage

        if (userEmail != other.userEmail) return false
        if (!image.contentEquals(other.image)) return false
        if (placeID != other.placeID) return false
        if (name != other.name) return false

        return true
    }

    override fun hashCode(): Int {
        var result = userEmail?.hashCode() ?: 0
        result = 31 * result + image.contentHashCode()
        result = 31 * result + placeID.hashCode()
        result = 31 * result + name.hashCode()
        return result
    }
}
