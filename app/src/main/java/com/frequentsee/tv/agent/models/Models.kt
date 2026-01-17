package com.frequentsee.tv.agent.models

data class CastRequest(
    val streamUrl: String,
    val title: String,
    val subtitle: String
)

data class PlaybackState(
    val status: String, // "playing", "stopped", "idle"
    val streamUrl: String? = null,
    val title: String? = null,
    val subtitle: String? = null
)

data class ApiResponse(
    val success: Boolean,
    val message: String? = null
)
