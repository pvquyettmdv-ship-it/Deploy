package com.example.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

class Repository(private val db: AppDatabase) {

    val allProducts: Flow<List<Product>> = db.productDao().getAllProducts()
    val allPosts: Flow<List<SocialPost>> = db.postDao().getAllPosts()
    val cartItems: Flow<List<CartItem>> = db.cartDao().getCartItems()
    val allOrders: Flow<List<Order>> = db.orderDao().getAllOrders()
    val chatMessages: Flow<List<Message>> = db.messageDao().getAllMessages()

    fun getProductsByCategory(category: String): Flow<List<Product>> {
        return db.productDao().getProductsByCategory(category)
    }

    suspend fun getProductById(id: Int): Product? {
        return db.productDao().getProductById(id)
    }

    suspend fun addProduct(product: Product) {
        db.productDao().insertProduct(product)
    }

    suspend fun updateProduct(product: Product) {
        db.productDao().updateProduct(product)
    }

    suspend fun addToCart(productId: Int, quantity: Int, selectedSku: String) {
        val currentCart = db.cartDao().getCartItems().first()
        val existing = currentCart.find { it.productId == productId && it.selectedSku == selectedSku }
        if (existing != null) {
            db.cartDao().updateQuantity(existing.id, existing.quantity + quantity)
        } else {
            db.cartDao().insertCartItem(CartItem(productId = productId, quantity = quantity, selectedSku = selectedSku))
        }
    }

    suspend fun deleteCartItem(id: Int) {
        db.cartDao().deleteCartItem(id)
    }

    suspend fun updateCartQuantity(id: Int, quantity: Int) {
        if (quantity <= 0) {
            db.cartDao().deleteCartItem(id)
        } else {
            db.cartDao().updateQuantity(id, quantity)
        }
    }

    suspend fun checkout(shippingAddress: String, paymentMethod: String, productsList: List<Pair<Product, CartItem>>, totalAmount: Double) {
        val itemsDesc = productsList.joinToString(", ") { "${it.second.quantity}x ${it.first.name} (${it.second.selectedSku})" }
        val orderId = "ORD-" + System.currentTimeMillis().toString().takeLast(6)
        val order = Order(
            id = orderId,
            totalAmount = totalAmount,
            status = "Ordered",
            timestamp = System.currentTimeMillis(),
            shippingAddress = shippingAddress,
            paymentMethod = paymentMethod,
            itemsDescription = itemsDesc
        )
        db.orderDao().insertOrder(order)
        db.cartDao().clearCart()
    }

    suspend fun insertMessage(message: Message) {
        db.messageDao().insertMessage(message)
    }

    suspend fun updatePostLikes(postId: Int, currentLikes: Int) {
        db.postDao().updateLikes(postId, currentLikes + 1)
    }

    suspend fun seedDatabaseIfEmpty() {
        val productCount = db.productDao().getAllProducts().first().size
        if (productCount == 0) {
            val mockProducts = listOf(
                Product(
                    id = 101,
                    name = "Aura Premium Oversized Hoodie",
                    description = "Áo hoodie phom rộng chất liệu nỉ bông Hàn Quốc siêu mềm mịn, đứng phom. Phong cách thời trang đường phố tối giản dạo phố ấm áp cực trendy.",
                    price = 490000.0,
                    originalPrice = 790000.0,
                    stock = 120,
                    rating = 4.8f,
                    reviewsCount = 320,
                    category = "Fashion",
                    imageUrl = "img_product_hoodie",
                    isAffiliate = true,
                    commissionRate = 15.0,
                    ratingSummary = "AI tóm tắt: Đánh giá cao về chất liệu nỉ bông dày dặn, phom đứng chuẩn Hàn Quốc. Co giãn tốt, không ra màu khi giặt."
                ),
                Product(
                    id = 102,
                    name = "LunarTech Wireless ANC Earbuds",
                    description = "Tai nghe không dây Bluetooth 5.3 chống ồn chủ động ANC vượt trội. Âm bass sâu trầm ấm mang lại không gian âm nhạc riêng tư hoàn hảo.",
                    price = 890000.0,
                    originalPrice = 1490000.0,
                    stock = 85,
                    rating = 4.7f,
                    reviewsCount = 180,
                    category = "Tech",
                    imageUrl = "img_product_earbuds",
                    isAffiliate = true,
                    commissionRate = 12.0,
                    ratingSummary = "AI tóm tắt: Âm bass trầm ấm đầy uy lực, chống ồn chủ động ANC lọc âm văn phòng rất tốt. Thời pin đạt 8 tiếng."
                ),
                Product(
                    id = 103,
                    name = "GlowGlow Velvet Lip Tint",
                    description = "Son tint lì dạng nhung siêu mịn mướt môi với dải màu trendy cuốn hút cực lâu trôi. Công thức dưỡng ẩm không làm khô ráp hay nứt nẻ môi.",
                    price = 150000.0,
                    originalPrice = 250000.0,
                    stock = 240,
                    rating = 4.9f,
                    reviewsCount = 650,
                    category = "Beauty",
                    imageUrl = "img_product_liptint",
                    isAffiliate = false,
                    commissionRate = 0.0,
                    ratingSummary = "AI tóm tắt: Màu son cực kì trendy tôn da châu Á, chất son mịn mượt tựa nhung không bị lộ vân môi. Bám màu lâu 4-6 tiếng."
                ),
                Product(
                    id = 104,
                    name = "ZenStone Minimalist Ceramic Vase",
                    description = "Bình hoa gốm nhám phong cách Bắc Âu tối giản sang trọng. Điểm nhấn tuyệt vời cho không gian phòng khách hoặc bàn làm việc thanh nhã.",
                    price = 340000.0,
                    originalPrice = 490000.0,
                    stock = 40,
                    rating = 4.6f,
                    reviewsCount = 75,
                    category = "Home",
                    imageUrl = "img_product_vase",
                    isAffiliate = false,
                    commissionRate = 0.0,
                    ratingSummary = "AI tóm tắt: Thiết kế tối giản tinh tế, chất liệu gốm ceramic cao cấp dày dặn. Thích hợp bài trí nhiều không gian khác nhau."
                ),
                Product(
                    id = 105,
                    name = "FitTrack Pro Smart Band",
                    description = "Vòng đeo tay thông minh theo dõi sức khỏe chuyên sâu, đo nhịp tim, SpO2 và đếm bước chân. Hỗ trợ hơn 100 chế độ tập luyện thể thao.",
                    price = 590000.0,
                    originalPrice = 990000.0,
                    stock = 150,
                    rating = 4.5f,
                    reviewsCount = 220,
                    category = "Tech",
                    imageUrl = "img_product_smartband",
                    isAffiliate = true,
                    commissionRate = 10.0,
                    ratingSummary = "AI tóm tắt: Màn hình AMOLED rực rỡ, đo đạc chỉ số sức khoẻ tương đối chính xác. Pin cực khoẻ kéo dài đến 14 ngày sử dụng."
                )
            )
            db.productDao().insertProducts(mockProducts)
        }

        val postCount = db.postDao().getAllPosts().first().size
        if (postCount == 0) {
            val mockPosts = listOf(
                SocialPost(
                    creatorName = "Chanh Beauty 🌸",
                    creatorAvatar = "img_avatar_chanh",
                    contentUrl = "img_post_reels_liptint",
                    caption = "Unbox dòng son Velvet Lip Tint siêu lì mướt môi đang hot rần rần bên Trung nè cả nhà ơi! Ưng cái bụng ghê luôn á 💖 #GlowGlow #LipTint #BeautyReview #ShopeeXuTrend",
                    likes = 1240,
                    commentsCount = 89,
                    postType = "video",
                    taggedProductId = 103
                ),
                SocialPost(
                    creatorName = "Minh Techie 💻",
                    creatorAvatar = "img_avatar_minh",
                    contentUrl = "img_post_red_earbuds",
                    caption = "Set tai nghe chống ồn chủ động LunarTech ANC Earbuds này đúng là cứu tinh cho những ngày làm việc ồn ào ở quán cafe. Nhỏ gọn, pin trâu mà âm thanh ấm cực kỳ 🎧✨ #LunarTech #DeskSetup #Earbuds #KOLReview",
                    likes = 850,
                    commentsCount = 45,
                    postType = "image",
                    taggedProductId = 102
                ),
                SocialPost(
                    creatorName = "Thanh Tùng Style 👕",
                    creatorAvatar = "img_avatar_tung",
                    contentUrl = "img_post_reels_hoodie",
                    caption = "Phối đồ phong cách tối giản đón thu cùng em Aura Premium Oversized Hoodie dày dặn cực đỉnh nha! Đi chơi đi học đều bao đẹp 😎👌 #Menswear #OversizedHoodie #AestheticStyle #TikTokFashion",
                    likes = 2310,
                    commentsCount = 156,
                    postType = "video",
                    taggedProductId = 101
                ),
                SocialPost(
                    creatorName = "Nhà Của Nắng 🌿",
                    creatorAvatar = "img_avatar_nang",
                    contentUrl = "img_post_red_vase",
                    caption = "Góc phòng nhỏ bình yên hơn hẳn từ khi tậu em bình gốm nhám ZenStone này. Chỉ cần cắm một nhành cây nhỏ là sáng bừng cả phòng khách rồi 🏡✨ #HomeAesthetic #Minimalism #ZenStone #GocDecor",
                    likes = 530,
                    commentsCount = 12,
                    postType = "image",
                    taggedProductId = 104
                )
            )
            db.postDao().insertPosts(mockPosts)
        }

        val msgCount = db.messageDao().getAllMessages().first().size
        if (msgCount == 0) {
            db.messageDao().insertMessage(
                Message(
                    sender = "AI",
                    content = "Xin chào! Tôi là Trợ lý Mua sắm AI của VibeCart. Bạn cần tôi tư vấn sản phẩm, tóm tắt đánh giá hay gợi ý phối đồ thời trang hôm nay không? 🤖✨",
                    timestamp = System.currentTimeMillis()
                )
            )
        }
    }
}
