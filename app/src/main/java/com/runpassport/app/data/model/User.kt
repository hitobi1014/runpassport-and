package com.runpassport.app.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class User(
    val id: String,
    val email: String,
    val nickname: String?,
    @SerialName("display_name") val displayName: String?
)
