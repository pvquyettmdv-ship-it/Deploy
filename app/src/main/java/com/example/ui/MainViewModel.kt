package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val database = AppDatabase.getDatabase(application)
    private val repository = Repository(database)

    // State flows from Database
    val allProducts: StateFlow<List<Product>> = repository.allProducts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allPosts: StateFlow<List<SocialPost>> = repository.allPosts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val cartItems: StateFlow<List<CartItem>> = repository.cartItems
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allOrders: StateFlow<List<Order>> = repository.allOrders
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val chatMessages: StateFlow<List<Message>> = repository.chatMessages
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // UI Interactive States
    private val _currentTab = MutableStateFlow("Feed") // "Feed", "Shop", "Live", "Chat", "Profile"
    val currentTab: StateFlow<String> = _currentTab.asStateFlow()

    private val _activeScreen = MutableStateFlow("Home") // "Home", "ProductDetail", "Cart", "Checkout", "OrderTracking", "SellerCenter"
    val activeScreen: StateFlow<String> = _activeScreen.asStateFlow()

    private val _selectedProductId = MutableStateFlow<Int?>(null)
    val selectedProductId: StateFlow<Int?> = _selectedProductId.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _aiLoading = MutableStateFlow(false)
    val aiLoading: StateFlow<Boolean> = _aiLoading.asStateFlow()

    private val _selectedCategory = MutableStateFlow("All")
    val selectedCategory: StateFlow<String> = _selectedCategory.asStateFlow()

    private val _selectedSku = MutableStateFlow("Tiêu chuẩn")
    val selectedSku: StateFlow<String> = _selectedSku.asStateFlow()

    // Combined/Derived States
    val cartItemWithProducts: StateFlow<List<Pair<Product, CartItem>>> = combine(
        cartItems,
        allProducts
    ) { items, products ->
        items.mapNotNull { item ->
            val prod = products.find { it.id == item.productId }
            if (prod != null) prod to item else null
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val cartTotalPrice: StateFlow<Double> = cartItemWithProducts.map { items ->
        items.sumOf { it.first.price * it.second.quantity }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val filteredProducts: StateFlow<List<Product>> = combine(
        allProducts,
        _searchQuery,
        _selectedCategory
    ) { products, query, cat ->
        var list = products
        if (cat != "All") {
            list = list.filter { it.category.equals(cat, ignoreCase = true) }
        }
        if (query.isNotEmpty()) {
            list = list.filter { 
                it.name.contains(query, ignoreCase = true) || 
                it.description.contains(query, ignoreCase = true) ||
                it.category.contains(query, ignoreCase = true)
            }
        }
        list
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Merchant KPI States
    val merchantSales = MutableStateFlow(12450000.0) // 12.45M VND
    val merchantOrdersCount = MutableStateFlow(32)
    val merchantFollowers = MutableStateFlow(2400)
    val merchantConversionRate = MutableStateFlow(3.4) // 3.4%

    // Zoom Room / Meeting Group States
    private val _meetingRooms = MutableStateFlow<List<MeetingRoom>>(emptyList())
    val meetingRooms: StateFlow<List<MeetingRoom>> = _meetingRooms.asStateFlow()

    private val _currentJoinedRoom = MutableStateFlow<MeetingRoom?>(null)
    val currentJoinedRoom: StateFlow<MeetingRoom?> = _currentJoinedRoom.asStateFlow()

    private val _roomParticipants = MutableStateFlow<List<RoomParticipant>>(emptyList())
    val roomParticipants: StateFlow<List<RoomParticipant>> = _roomParticipants.asStateFlow()

    private val _roomMessages = MutableStateFlow<List<Message>>(emptyList())
    val roomMessages: StateFlow<List<Message>> = _roomMessages.asStateFlow()

    val userCameraOn = MutableStateFlow(true)
    val userMicOn = MutableStateFlow(false)
    val userHandRaised = MutableStateFlow(false)

    private val _roomAiTyping = MutableStateFlow<String?>(null) // name of the member currently typing
    val roomAiTyping: StateFlow<String?> = _roomAiTyping.asStateFlow()

    private var simulationJob: kotlinx.coroutines.Job? = null

    // --- Live Stream States ---
    private val _liveStreams = MutableStateFlow<List<LiveStream>>(emptyList())
    val liveStreams: StateFlow<List<LiveStream>> = _liveStreams.asStateFlow()

    private val _currentLiveStream = MutableStateFlow<LiveStream?>(null)
    val currentLiveStream: StateFlow<LiveStream?> = _currentLiveStream.asStateFlow()

    private val _liveComments = MutableStateFlow<List<LiveComment>>(emptyList())
    val liveComments: StateFlow<List<LiveComment>> = _liveComments.asStateFlow()

    private val _liveLikesCount = MutableStateFlow(0)
    val liveLikesCount: StateFlow<Int> = _liveLikesCount.asStateFlow()

    private val _liveViewersCount = MutableStateFlow(0)
    val liveViewersCount: StateFlow<Int> = _liveViewersCount.asStateFlow()

    val liveUserCameraOn = MutableStateFlow(true)
    val liveUserMicOn = MutableStateFlow(true)

    private var liveSimulationJob: kotlinx.coroutines.Job? = null

    init {
        viewModelScope.launch {
            repository.seedDatabaseIfEmpty()
        }
        _liveStreams.value = listOf(
            LiveStream(
                id = "live_chanh",
                title = "Săn Deal Son GlowGlow 🌸 Độc Quyền Giảm 50%",
                hostName = "Chanh Beauty 🌸",
                category = "Làm đẹp",
                viewersCount = 2410,
                likesCount = 1420,
                pinnedProductId = 103,
                isUserBroadcasting = false,
                coverColorStart = 0xFFFC466B,
                coverColorEnd = 0xFF3F5EFB
            ),
            LiveStream(
                id = "live_huyen",
                title = "Thử Đồ Thu Đông Cùng Huyền My 👗 Freeship Toàn Quốc",
                hostName = "Huyền My Store 👗",
                category = "Thời trang",
                viewersCount = 890,
                likesCount = 3200,
                pinnedProductId = 101,
                isUserBroadcasting = false,
                coverColorStart = 0xFFE91E63,
                coverColorEnd = 0xFF9C27B0
            ),
            LiveStream(
                id = "live_tech",
                title = "Trên Tay Tai Nghe LunarTech & Vòng Đeo Sức Khoẻ 🎧",
                hostName = "Hoàng Sơn Review 📱",
                category = "Công nghệ",
                viewersCount = 1530,
                likesCount = 2800,
                pinnedProductId = 104,
                isUserBroadcasting = false,
                coverColorStart = 0xFF00BCD4,
                coverColorEnd = 0xFF3F51B5
            ),
            LiveStream(
                id = "live_bep",
                title = "Góc Bếp Ấm Cúng: Review Nồi Chiên Không Dầu Đa Năng 🍳",
                hostName = "Vào Bếp Cùng Vy Vy 🍳",
                category = "Đời sống",
                viewersCount = 420,
                likesCount = 1100,
                pinnedProductId = 102,
                isUserBroadcasting = false,
                coverColorStart = 0xFFFF9800,
                coverColorEnd = 0xFFF44336
            )
        )

        _meetingRooms.value = listOf(
            MeetingRoom(
                id = "room_deal",
                name = "Phòng Săn Deal Độc Quyền 🏷️",
                description = "Giao lưu trực tuyến cùng KOL Thúy Vi săn voucher giảm giá 50% các sản phẩm hot nhất hôm nay!",
                hostName = "Thúy Vi (KOL)",
                category = "Khuyến mãi",
                participantsCount = 15,
                avatarColor = 0xFFE91E63
            ),
            MeetingRoom(
                id = "room_fashion",
                name = "Hội Tám Chuyện Thời Trang 👗",
                description = "Cùng Stylist Minh Quân chia sẻ các tips phối đồ thu đông, chọn size phù hợp với mọi vóc dáng.",
                hostName = "Stylist Minh Quân",
                category = "Làm đẹp",
                participantsCount = 8,
                avatarColor = 0xFF9C27B0
            ),
            MeetingRoom(
                id = "room_tech",
                name = "Trải Nghiệm Đồ Công Nghệ 📱",
                description = "Hỏi đáp trực tiếp và review chi tiết các tính năng tai nghe chống ồn, vòng đeo tay sức khỏe.",
                hostName = "Reviewer Hoàng Sơn",
                category = "Công nghệ",
                participantsCount = 12,
                avatarColor = 0xFF00BCD4
            ),
            MeetingRoom(
                id = "room_home",
                name = "Góc Decor Nhà Cửa Tối Giản 🏠",
                description = "Chia sẻ ý tưởng trang trí phòng khách phong cách Bắc Âu tối giản, cắm hoa nghệ thuật.",
                hostName = "Khánh Linh",
                category = "Đời sống",
                participantsCount = 6,
                avatarColor = 0xFF4CAF50
            )
        )
    }

    fun createCustomRoom(name: String, description: String, hostName: String, category: String) {
        val newRoom = MeetingRoom(
            id = "room_${System.currentTimeMillis()}",
            name = name,
            description = description,
            hostName = hostName,
            category = category,
            participantsCount = 1,
            avatarColor = listOf(0xFFE91E63, 0xFF9C27B0, 0xFF00BCD4, 0xFF4CAF50, 0xFF3F51B5, 0xFFFF9800).random()
        )
        _meetingRooms.value = listOf(newRoom) + _meetingRooms.value
    }

    fun joinMeetingRoom(room: MeetingRoom) {
        _currentJoinedRoom.value = room
        userCameraOn.value = true
        userMicOn.value = false
        userHandRaised.value = false
        _roomAiTyping.value = null

        // Initialize active participants
        val host = RoomParticipant("p_host", room.hostName, "Host", isCameraOn = true, isMicOn = true, isSpeaking = true, avatarColor = room.avatarColor)
        val p1 = RoomParticipant("p1", "Minh Hằng", "KOL", isCameraOn = true, isMicOn = false, isSpeaking = false, avatarColor = 0xFF3F51B5)
        val p2 = RoomParticipant("p2", "Quốc Bảo", "Thành viên", isCameraOn = false, isMicOn = true, isSpeaking = false, avatarColor = 0xFFFF9800)
        val p3 = RoomParticipant("p3", "Khánh An", "Thành viên", isCameraOn = true, isMicOn = true, isSpeaking = false, avatarColor = 0xFF4CAF50)
        val p4 = RoomParticipant("p4", "Phương Linh", "Thành viên", isCameraOn = false, isMicOn = false, isSpeaking = false, avatarColor = 0xFFE91E63)
        _roomParticipants.value = listOf(host, p1, p2, p3, p4)

        // Initialize historical room messages
        _roomMessages.value = listOf(
            Message(sender = room.hostName, content = "Chào mừng mọi người đã tham gia ${room.name}! Hôm nay chúng ta sẽ cùng thảo luận và kết nối vui vẻ nhé. 🎉", timestamp = System.currentTimeMillis() - 60000),
            Message(sender = "Minh Hằng", content = "Chào cả nhà ạ! Em hóng phòng này từ nãy giờ luôn.", timestamp = System.currentTimeMillis() - 45000),
            Message(sender = "Quốc Bảo", content = "Chào anh Host và mọi người! Chúc mọi người một ngày đầy năng lượng.", timestamp = System.currentTimeMillis() - 30000)
        )

        // Start active room simulation
        startRoomSimulation(room)
    }

    fun leaveMeetingRoom() {
        simulationJob?.cancel()
        _currentJoinedRoom.value = null
        _roomParticipants.value = emptyList()
        _roomMessages.value = emptyList()
    }

    private fun startRoomSimulation(room: MeetingRoom) {
        simulationJob?.cancel()
        simulationJob = viewModelScope.launch {
            val templates = when (room.id) {
                "room_deal" -> listOf(
                    "Minh Hằng" to "Mọi người đã săn được mã giảm giá 50% chưa? Em vừa áp thử giảm hẳn một nửa sướng ghê!",
                    "Quốc Bảo" to "Có ai hỏi Host xem mẫu Hoodie Aura còn mã freeship không kìa.",
                    "Khánh An" to "Vừa chốt được 2 thỏi son Velvet Lip Tint bên shop, giá hời thật sự.",
                    "Phương Linh" to "Em muốn hỏi KOL tư vấn kĩ hơn về tai nghe LunarTech xem đeo lâu có bị đau tai không ạ?"
                )
                "room_fashion" -> listOf(
                    "Minh Hằng" to "Hôm nay stylist mặc set đồ đẹp thế! Cho em xin info cái áo khoác ngoài đi ạ.",
                    "Quốc Bảo" to "Các bạn nam cao m75 nặng 70kg thì nên mặc Hoodie size XL hay L nhỉ mọi người?",
                    "Khánh An" to "Em nghĩ phom oversized thì chọn L là vừa vặn rồi anh Bảo ơi, mặc XL sợ bị thụng quá.",
                    "Phương Linh" to "Phối đồ tone nâu ấm đi cafe chụp ảnh cực kỳ tôn da nha các bạn nữ."
                )
                "room_tech" -> listOf(
                    "Minh Hằng" to "Tai nghe LunarTech có chống ồn chủ động (ANC) tốt không anh Sơn ơi?",
                    "Quốc Bảo" to "Vừa test thử đo SpO2 trên vòng đeo FitTrack Pro, khớp phết so với máy đo y tế luôn.",
                    "Khánh An" to "Pin tai nghe dùng liên tục được bao nhiêu tiếng vậy cả nhà?",
                    "Phương Linh" to "Vòng tay FitTrack đeo đi bơi thoải mái nha, chống nước đỉnh lắm."
                )
                else -> listOf(
                    "Minh Hằng" to "Chào cả nhà nha, phòng này ấm cúng quá!",
                    "Quốc Bảo" to "Có ai thích phong cách tối giản Bắc Âu giống em không? Setup phòng chill cực kì.",
                    "Khánh An" to "Bình hoa gốm ZenStone đặt ở bậu cửa sổ đón nắng sớm đẹp xuất sắc luôn mọi người.",
                    "Phương Linh" to "Host hướng dẫn cắm hoa hồng kết hợp hoa baby đi ạ."
                )
            }

            while (true) {
                kotlinx.coroutines.delay((6000..10000).random().toLong())
                if (_currentJoinedRoom.value == null) break

                // Randomly perform actions
                val actionType = (1..3).random()
                when (actionType) {
                    1 -> {
                        // A participant speaks & chats
                        val randomChat = templates.random()
                        val senderName = randomChat.first
                        val content = randomChat.second

                        // Make them speak
                        _roomParticipants.value = _roomParticipants.value.map {
                            if (it.name == senderName) it.copy(isSpeaking = true, isMicOn = true) else it.copy(isSpeaking = false)
                        }

                        // Add message
                        _roomMessages.value = _roomMessages.value + Message(
                            sender = senderName,
                            content = content,
                            timestamp = System.currentTimeMillis()
                        )

                        // Stop speaking after 2.5s
                        kotlinx.coroutines.delay(2500)
                        _roomParticipants.value = _roomParticipants.value.map { it.copy(isSpeaking = false) }
                    }
                    2 -> {
                        // Toggle camera/mic for a random member
                        if (_roomParticipants.value.isNotEmpty()) {
                            val randomIdx = (0 until _roomParticipants.value.size).random()
                            _roomParticipants.value = _roomParticipants.value.mapIndexed { idx, p ->
                                if (idx == randomIdx && p.id != "p_host") {
                                    if ((1..2).random() == 1) {
                                        p.copy(isCameraOn = !p.isCameraOn)
                                    } else {
                                        p.copy(isMicOn = !p.isMicOn)
                                    }
                                } else p
                            }
                        }
                    }
                    3 -> {
                        // Raise hand simulation
                        if (_roomParticipants.value.isNotEmpty()) {
                            val randomIdx = (0 until _roomParticipants.value.size).random()
                            _roomParticipants.value = _roomParticipants.value.mapIndexed { idx, p ->
                                if (idx == randomIdx && p.id != "p_host") {
                                    p.copy(hasRaisedHand = !p.hasRaisedHand)
                                } else p
                            }
                        }
                    }
                }
            }
        }
    }

    fun sendRoomMessage(content: String, isProductShared: Boolean = false, sharedProdId: Int = 0) {
        val currentRoom = _currentJoinedRoom.value ?: return
        if (content.isBlank() && !isProductShared) return

        viewModelScope.launch {
            val userMsg = if (isProductShared) {
                val prod = repository.getProductById(sharedProdId)
                Message(
                    sender = "Bạn",
                    content = "Tôi vừa chia sẻ sản phẩm: ${prod?.name}. Hãy xem thử đi!",
                    timestamp = System.currentTimeMillis(),
                    isProductShared = true,
                    sharedProductId = sharedProdId,
                    sharedProductName = prod?.name ?: "",
                    sharedProductPrice = prod?.price ?: 0.0
                )
            } else {
                Message(
                    sender = "Bạn",
                    content = content,
                    timestamp = System.currentTimeMillis()
                )
            }

            // Append user message
            _roomMessages.value = _roomMessages.value + userMsg

            // Decide which participant will respond (either host or someone else)
            val responder = _roomParticipants.value.filter { it.name != "Bạn" }.randomOrNull() ?: return@launch
            _roomAiTyping.value = responder.name

            // Update speaking status for the responder
            _roomParticipants.value = _roomParticipants.value.map {
                if (it.name == responder.name) it.copy(isSpeaking = true, isMicOn = true) else it.copy(isSpeaking = false)
            }

            // Call Gemini or fallback to get interactive group discussion response
            val promptContext = """
                Bạn đang đóng vai là thành viên '${responder.name}' (vai trò: ${responder.role}) trong một phòng họp trực tuyến Zoom nhóm mang tên '${currentRoom.name}'.
                Chủ đề phòng là: "${currentRoom.description}".
                Người dùng ('Bạn') vừa gửi một tin nhắn vào group chat: "${userMsg.content}".
                Hãy viết câu phản hồi của bạn ngắn gọn, tự nhiên, đậm chất giao tiếp nói chuyện trong phòng họp Zoom trực tuyến (khoảng 1-2 câu ngắn, thân thiện bằng Tiếng Việt).
            """.trimIndent()

            val responseText = GeminiService.generateResponse(promptContext)

            // Append responder's response
            _roomMessages.value = _roomMessages.value + Message(
                sender = responder.name,
                content = responseText,
                timestamp = System.currentTimeMillis()
            )

            // Reset states
            _roomAiTyping.value = null
            _roomParticipants.value = _roomParticipants.value.map { it.copy(isSpeaking = false) }
        }
    }

    // Actions
    fun setTab(tab: String) {
        _currentTab.value = tab
        // Reset subscreen if tab switches
        _activeScreen.value = "Home"
    }

    fun navigateTo(screen: String) {
        _activeScreen.value = screen
    }

    fun selectProduct(productId: Int) {
        _selectedProductId.value = productId
        _selectedSku.value = "Tiêu chuẩn"
        navigateTo("ProductDetail")
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setCategory(category: String) {
        _selectedCategory.value = category
    }

    fun setSku(sku: String) {
        _selectedSku.value = sku
    }

    fun addToCart(productId: Int, quantity: Int) {
        viewModelScope.launch {
            repository.addToCart(productId, quantity, _selectedSku.value)
        }
    }

    fun updateCartQuantity(cartItemId: Int, quantity: Int) {
        viewModelScope.launch {
            repository.updateCartQuantity(cartItemId, quantity)
        }
    }

    fun deleteCartItem(cartItemId: Int) {
        viewModelScope.launch {
            repository.deleteCartItem(cartItemId)
        }
    }

    fun checkout(shippingAddress: String, paymentMethod: String) {
        viewModelScope.launch {
            val items = cartItemWithProducts.value
            val total = cartTotalPrice.value + 30000.0 // Add 30k delivery fee
            repository.checkout(shippingAddress, paymentMethod, items, total)
            navigateTo("OrderTracking")
        }
    }

    fun togglePostLike(postId: Int, currentLikes: Int) {
        viewModelScope.launch {
            repository.updatePostLikes(postId, currentLikes)
        }
    }

    // AI Messaging Chatbot Actions
    fun sendChatMessage(content: String, isProductShared: Boolean = false, sharedProdId: Int = 0) {
        if (content.isBlank() && !isProductShared) return
        
        viewModelScope.launch {
            val userMsg = if (isProductShared) {
                val prod = repository.getProductById(sharedProdId)
                Message(
                    sender = "User",
                    content = "Tôi đang quan tâm sản phẩm này: ${prod?.name}. Hãy tóm tắt đánh giá và tư vấn thêm nhé!",
                    timestamp = System.currentTimeMillis(),
                    isProductShared = true,
                    sharedProductId = sharedProdId,
                    sharedProductName = prod?.name ?: "",
                    sharedProductPrice = prod?.price ?: 0.0
                )
            } else {
                Message(
                    sender = "User",
                    content = content,
                    timestamp = System.currentTimeMillis()
                )
            }
            
            repository.insertMessage(userMsg)
            _aiLoading.value = true

            // Generate Prompt with context
            val productsListStr = allProducts.value.joinToString("\n") { 
                "- [ID: ${it.id}] ${it.name} (${it.price} VND). Phân loại: ${it.category}. Đánh giá: ${it.ratingSummary}"
            }
            val promptContext = """
                Bạn là Trợ lý Mua sắm AI siêu cấp của ứng dụng VibeCart.
                Đây là danh sách sản phẩm thực tế trong cửa hàng của chúng tôi:
                $productsListStr
                
                Hãy trả lời câu hỏi sau bằng Tiếng Việt một cách thân thiện, chu đáo, đưa ra đề xuất trực tiếp từ danh sách sản phẩm trên nếu phù hợp:
                "${userMsg.content}"
            """.trimIndent()

            val response = GeminiService.generateResponse(promptContext)
            
            val aiMsg = Message(
                sender = "AI",
                content = response,
                timestamp = System.currentTimeMillis()
            )
            repository.insertMessage(aiMsg)
            _aiLoading.value = false
        }
    }

    // AI Product Description Generator for Sellers
    fun generateProductDescription(productName: String, onComplete: (String) -> Unit) {
        if (productName.isBlank()) return
        viewModelScope.launch {
            _aiLoading.value = true
            val prompt = """
                Viết một bài mô tả sản phẩm siêu bán hàng ngắn gọn, đầy sức hút, kích thích mua sắm cho sản phẩm mang tên: "$productName".
                Bao gồm: điểm nổi bật, chất liệu, tính năng, và lời kêu gọi mua hàng đầy năng lượng.
                Hãy trả về bằng Tiếng Việt.
            """.trimIndent()
            val response = GeminiService.generateResponse(prompt)
            onComplete(response)
            _aiLoading.value = false
        }
    }

    // AI Review Summary helper
    fun summarizeProductReviews(productName: String, rawReviews: String, onComplete: (String) -> Unit) {
        viewModelScope.launch {
            _aiLoading.value = true
            val prompt = """
                Hãy viết một đoạn tóm tắt đánh giá AI ngắn gọn, súc tích (khoảng 2-3 câu) dựa trên các đánh giá sau của khách hàng về sản phẩm "$productName":
                "$rawReviews"
                Tập trung vào ưu điểm chính và nhược điểm nhỏ nếu có để người mua nắm được nhanh chóng. Trả về Tiếng Việt.
            """.trimIndent()
            val response = GeminiService.generateResponse(prompt)
            onComplete(response)
            _aiLoading.value = false
        }
    }

    // Seller CRUD
    fun addProductBySeller(name: String, category: String, price: Double, originalPrice: Double, description: String) {
        viewModelScope.launch {
            val newId = 200 + (1..100).random()
            val newProd = Product(
                id = newId,
                name = name,
                description = description,
                price = price,
                originalPrice = originalPrice,
                stock = 50,
                rating = 5.0f,
                reviewsCount = 0,
                category = category,
                imageUrl = "img_product_placeholder",
                isAffiliate = false,
                commissionRate = 0.0,
                ratingSummary = "AI tóm tắt: Chưa có đủ đánh giá từ người dùng."
            )
            repository.addProduct(newProd)
            
            // Adjust KPI metrics as simulated feedback
            merchantOrdersCount.value += 1
            merchantSales.value += price
        }
    }

    // --- Live Stream Actions ---
    fun joinLiveStream(stream: LiveStream) {
        liveSimulationJob?.cancel()
        _currentLiveStream.value = stream
        _liveLikesCount.value = stream.likesCount
        _liveViewersCount.value = stream.viewersCount
        liveUserCameraOn.value = true
        liveUserMicOn.value = true

        _liveComments.value = listOf(
            LiveComment("Linh Đan", "Màu son xinh quá shop ơi! 😍"),
            LiveComment("Hoàng Long", "Đã săn được mã giảm giá 50k nha cả nhà!"),
            LiveComment("Minh Thư", "Chất son lên môi mướt ghê á chị iu."),
            LiveComment("Quốc Anh", "Có được freeship không shop?")
        )

        startLiveSimulation(stream)
    }

    fun createLiveStream(title: String, category: String, pinnedProductId: Int?) {
        liveSimulationJob?.cancel()
        val colors = listOf(
            0xFFFC466B to 0xFF3F5EFB,
            0xFFE91E63 to 0xFF9C27B0,
            0xFF00BCD4 to 0xFF3F51B5,
            0xFFFF9800 to 0xFFF44336,
            0xFF4CAF50 to 0xFF009688,
            0xFF673AB7 to 0xFF3F51B5
        )
        val selectedColors = colors.random()
        val newStream = LiveStream(
            id = "live_${System.currentTimeMillis()}",
            title = title,
            hostName = "Tôi (Bạn) 🌟",
            category = category,
            viewersCount = 1,
            likesCount = 0,
            pinnedProductId = pinnedProductId,
            isUserBroadcasting = true,
            coverColorStart = selectedColors.first,
            coverColorEnd = selectedColors.second
        )

        _liveStreams.value = listOf(newStream) + _liveStreams.value
        _currentLiveStream.value = newStream
        _liveLikesCount.value = 0
        _liveViewersCount.value = 1
        liveUserCameraOn.value = true
        liveUserMicOn.value = true

        _liveComments.value = listOf(
            LiveComment("Hệ thống ⚡", "Livestream của bạn đã được kết nối! Thành viên khác đang tham gia vào xem..."),
        )

        startLiveSimulation(newStream)
    }

    fun leaveLiveStream() {
        liveSimulationJob?.cancel()
        _currentLiveStream.value = null
        _liveComments.value = emptyList()
    }

    fun likeCurrentStream() {
        _liveLikesCount.value += 1
    }

    fun sendLiveComment(content: String) {
        val currentStream = _currentLiveStream.value ?: return
        if (content.isBlank()) return

        val userComment = LiveComment("Bạn", content, isUser = true)
        _liveComments.value = _liveComments.value + userComment

        viewModelScope.launch {
            kotlinx.coroutines.delay((1500..3000).random().toLong())
            val responderName = listOf("Trần Nam", "Hương Giang", "Phú Vinh", "Tuyết Trinh", "Thành Đạt", "Bảo Ngọc").random()

            val promptContext = if (currentStream.isUserBroadcasting) {
                """
                Bạn đang đóng vai là một khán giả xem Livestream tên là '$responderName' trên ứng dụng mua sắm thương mại điện tử.
                Người dùng (đang livestream phát trực tiếp) vừa nói hoặc nhắn tin bình luận: "$content".
                Tiêu đề buổi live của họ là: "${currentStream.title}" thuộc danh mục hàng "${currentStream.category}".
                Hãy viết một câu bình luận tương tác cực kỳ ngắn gọn, tự nhiên, hào hứng, nói chuyện kiểu chat online trên Shopee Live (khoảng 1 câu ngắn bằng Tiếng Việt thân thiện, ví dụ: hỏi giá, khen sản phẩm, hoặc thả tim).
                """.trimIndent()
            } else {
                """
                Bạn đang đóng vai là Host/KOL tên là '${currentStream.hostName}' đang phát livestream trên ứng dụng mua sắm.
                Khán giả ('Bạn') vừa bình luận hỏi: "$content".
                Hãy viết câu phản hồi của KOL thật duyên dáng, thân thiện, vui vẻ để trả lời khán giả (khoảng 1-2 câu ngắn, cuốn hút bằng Tiếng Việt).
                """.trimIndent()
            }

            val response = GeminiService.generateResponse(promptContext)
            _liveComments.value = _liveComments.value + LiveComment(responderName, response)
        }
    }

    private fun startLiveSimulation(stream: LiveStream) {
        liveSimulationJob?.cancel()
        liveSimulationJob = viewModelScope.launch {
            val randomNames = listOf("Trần Nam", "Hương Giang", "Phú Vinh", "Tuyết Trinh", "Thành Đạt", "Bảo Ngọc", "Linh Chi", "Minh Quân", "Anh Tuấn", "Thu Trang")
            val randomMsgs = if (stream.isUserBroadcasting) {
                listOf(
                    "Chào idol nha! Live xịn quá.",
                    "Sản phẩm ghim nhìn mê thế.",
                    "Ủng hộ bạn nha!",
                    "Chất lượng hình ảnh rõ nét ghê.",
                    "Đã thả tim mỏi tay luôn nè.",
                    "Có quà tặng gì không chủ phòng ơi?",
                    "Cho mình xin thông tin chi tiết sản phẩm nha.",
                    "Vừa follow shop xong, chúc shop đắt hàng!"
                )
            } else {
                listOf(
                    "Giao hàng nhanh không ạ?",
                    "Màu này tôn da ngăm không shop?",
                    "Đã đặt 2 thỏi nha",
                    "Đẹp xuất sắc luôn chị ơi",
                    "Săn deal hời ghê",
                    "Cho em xem kĩ phom dáng ạ",
                    "Mã giảm giá áp ở đâu vậy shop?",
                    "Còn màu đen size M không shop ơi?"
                )
            }

            while (true) {
                kotlinx.coroutines.delay((3000..6000).random().toLong())
                if (_currentLiveStream.value == null) break

                // Increase viewers count slowly or fluctuate
                _liveViewersCount.value = (_liveViewersCount.value + (-2..6).random()).coerceAtLeast(1)
                
                // Increase likes count automatically by some tap simulation
                _liveLikesCount.value += (2..12).random()

                // Random comment
                if ((1..10).random() <= 7) {
                    val commenter = randomNames.random()
                    val message = randomMsgs.random()
                    _liveComments.value = (_liveComments.value + LiveComment(commenter, message)).takeLast(25)
                }
            }
        }
    }
}
