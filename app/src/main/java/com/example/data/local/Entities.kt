package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class User(
    @PrimaryKey val username: String,
    val displayName: String,
    val avatarUrl: String,
    val bio: String,
    val followersCount: Int,
    val followingCount: Int,
    val isFollowing: Boolean = false,
    val isCurrentUser: Boolean = false
)

@Entity(tableName = "posts")
data class Post(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val username: String,
    val displayName: String,
    val avatarUrl: String,
    val content: String,
    val timestamp: Long,
    val likesCount: Int,
    val repliesCount: Int,
    val repostsCount: Int,
    val isLiked: Boolean = false,
    val isReposted: Boolean = false,
    val mediaUrl: String? = null,
    val mediaType: String? = "image",
    val privacy: String = "public"
)

@Entity(tableName = "comments")
data class Comment(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val postId: Int,
    val username: String,
    val displayName: String,
    val avatarUrl: String,
    val content: String,
    val timestamp: Long
)

@Entity(tableName = "notifications")
data class Notification(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val type: String, // "like", "comment", "follow", "repost"
    val fromUsername: String,
    val fromDisplayName: String,
    val fromAvatarUrl: String,
    val timestamp: Long,
    val postContentSnippet: String? = null,
    val isRead: Boolean = false
)

@Entity(tableName = "ai_chat_messages")
data class AIChatMessage(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val sender: String, // "user", "ai"
    val text: String,
    val thinking: String? = null, // Store thoughts of the model
    val timestamp: Long = System.currentTimeMillis()
)
