package com.rafael.inclusimap.domain

data class Comment(
    val postDate: String,
    val id: Int,
    val name: String,
    val email: String,
    val body: String,
    val accessibilityRate: Int,
)