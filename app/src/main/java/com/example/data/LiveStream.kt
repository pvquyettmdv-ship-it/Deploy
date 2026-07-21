package com.example.data

data class LiveStream(
    val id: String,
    val title: String,
    val hostName: String,
    val category: String,
    val viewersCount: Int,
    val likesCount: Int,
    val pinnedProductId: Int?,
    val isUserBroadcasting: Boolean,
    val coverColorStart: Long,
    val coverColorEnd: Long
)

data class LiveComment(
    val sender: String,
    val content: String,
    val isUser: Boolean = false
)
