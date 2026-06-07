package com.example

import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName

@Serializable
data class ApiChannel(
    val id: String = "",
    val name: String = "",
    val alt_names: List<String> = emptyList(),
    val network: String? = null,
    val owners: List<String> = emptyList(),
    val country: String? = null,
    val subdivision: String? = null,
    val city: String? = null,
    val broadcast_area: List<String> = emptyList(),
    val languages: List<String> = emptyList(),
    val categories: List<String> = emptyList(),
    val is_nsfw: Boolean = false,
    val launched: String? = null,
    val closed: String? = null,
    val replaced_by: String? = null,
    val website: String? = null,
    val logo: String? = null
)

@Serializable
data class ApiStream(
    val channel: String = "",
    val url: String = "",
    val timeshift: String? = null,
    val http_referrer: String? = null,
    val user_agent: String? = null
)

@Serializable
data class ChannelStream(
    val id: String,
    val name: String,
    val country: String,
    val categories: List<String>,
    val url: String,
    val logoUrl: String?
)
