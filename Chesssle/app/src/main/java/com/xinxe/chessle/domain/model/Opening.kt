package com.xinxe.chessle.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class Opening(
    val id: Int,
    val code: String,
    val openingName: String,
    val solution: List<String>,
)