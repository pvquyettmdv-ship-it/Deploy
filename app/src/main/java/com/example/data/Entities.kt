package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "products")
data class Product(
    @PrimaryKey val id: Int,
    val name: String,
    val description: String,
    val price: Double,
    val originalPrice: Double,
    val stock: Int,
    val rating: Float,
    val reviewsCount: Int,
    val category: String,
    val imageUrl: String,
    val isAffiliate: Boolean,
    val commissionRate: Double,
    val ratingSummary: String
)

@Entity(tableName = "cart_items")
data class CartItem(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val productId: Int,
    val quantity: Int,
    val selectedSku: String
)

@Entity(tableName = "orders")
data class Order(
    @PrimaryKey val id: String,
    val totalAmount: Double,
    val status: String, // "Ordered", "Processing", "Shipped", "Delivered"
    val timestamp: Long,
    val shippingAddress: String,
    val paymentMethod: String,
    val itemsDescription: String
)

@Entity(tableName = "messages")
data class Message(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val sender: String, // "User", "AI", "Merchant"
    val content: String,
    val timestamp: Long,
    val isProductShared: Boolean = false,
    val sharedProductId: Int = 0,
    val sharedProductName: String = "",
    val sharedProductPrice: Double = 0.0
)

@Entity(tableName = "social_posts")
data class SocialPost(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val creatorName: String,
    val creatorAvatar: String,
    val contentUrl: String, // placeholder image or style descriptor
    val caption: String,
    val likes: Int,
    val commentsCount: Int,
    val postType: String, // "video" or "image"
    val taggedProductId: Int
)
