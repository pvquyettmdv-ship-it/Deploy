package com.example.data

data class MeetingRoom(
    val id: String,
    val name: String,
    val description: String,
    val hostName: String,
    val category: String,
    val participantsCount: Int,
    val avatarColor: Long
)

data class RoomParticipant(
    val id: String,
    val name: String,
    val role: String, // "Host", "KOL", "Thành viên", "Bạn"
    var isCameraOn: Boolean,
    var isMicOn: Boolean,
    var isSpeaking: Boolean = false,
    var hasRaisedHand: Boolean = false,
    val avatarColor: Long
)
