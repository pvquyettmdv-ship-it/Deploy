package com.example.data.repository

import android.content.Context
import androidx.room.Room
import com.example.data.local.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SocialRepository private constructor(context: Context) {

    private val database: AppDatabase = Room.databaseBuilder(
        context.applicationContext,
        AppDatabase::class.java,
        "netvibe_social.db"
    )
    .fallbackToDestructiveMigration()
    .build()

    val userDao = database.userDao()
    val postDao = database.postDao()
    val commentDao = database.commentDao()
    val notificationDao = database.notificationDao()
    val aiChatDao = database.aiChatDao()

    // Observe flows
    val currentUser: Flow<User?> = userDao.observeCurrentUser()
    val otherUsers: Flow<List<User>> = userDao.getAllOtherUsers()
    val allPosts: Flow<List<Post>> = postDao.getAllPosts()
    val allNotifications: Flow<List<Notification>> = notificationDao.getAllNotifications()
    val aiMessages: Flow<List<AIChatMessage>> = aiChatDao.getAllMessages()

    companion object {
        @Volatile
        private var INSTANCE: SocialRepository? = null

        fun getInstance(context: Context): SocialRepository {
            return INSTANCE ?: synchronized(this) {
                val instance = SocialRepository(context)
                INSTANCE = instance
                instance
            }
        }
    }

    suspend fun initDefaultData() = withContext(Dispatchers.IO) {
        // Check if database already has users to avoid re-seeding
        val defaultUserExists = userDao.getUserByUsername("@q_pv") != null
        if (defaultUserExists) {
            return@withContext
        }

        // Seed Current User (default available user in DB, but not logged in initially)
        val me = User(
            username = "@q_pv",
            displayName = "Quyết Phạm",
            avatarUrl = "https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?auto=format&fit=crop&w=150&q=80",
            bio = "Đang lập trình ứng dụng mạng xã hội siêu mượt NetVibe với Jetpack Compose & Gemini 3.1 Pro 🚀",
            followersCount = 1250,
            followingCount = 342,
            isCurrentUser = false,
            email = "pvquyet.tmdv@gmail.com",
            password = "123456"
        )
        userDao.insertUser(me)

        // Seed Other Users
        val users = listOf(
            User(
                username = "@den_vau",
                displayName = "Đen Vâu",
                avatarUrl = "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?auto=format&fit=crop&w=150&q=80",
                bio = "Đi trốn tìm cùng âm nhạc và NetVibe... 🎤 Mang tiền về cho mẹ!",
                followersCount = 952000,
                followingCount = 12,
                isFollowing = true
            ),
            User(
                username = "@son_tung",
                displayName = "Sơn Tùng M-TP",
                avatarUrl = "https://images.unsplash.com/photo-1500648767791-00dcc994a43e?auto=format&fit=crop&w=150&q=80",
                bio = "Sky ơi! 🌟 Chúng ta của hiện tại đều có mặt trên NetVibe!",
                followersCount = 1240000,
                followingCount = 4,
                isFollowing = true
            ),
            User(
                username = "@tech_guru",
                displayName = "Tech Guru",
                avatarUrl = "https://images.unsplash.com/photo-1472099645785-5658abf4ff4e?auto=format&fit=crop&w=150&q=80",
                bio = "Review công nghệ, AI và lập trình di động hàng ngày 📱💻",
                followersCount = 45000,
                followingCount = 120,
                isFollowing = false
            ),
            User(
                username = "@gemini_bot",
                displayName = "AI Trợ Lý (Thinking Mode)",
                avatarUrl = "https://images.unsplash.com/photo-1618005182384-a83a8bd57fbe?auto=format&fit=crop&w=150&q=80",
                bio = "Trợ lý AI Suy nghĩ Sâu tích hợp mô hình Gemini 3.1 Pro 🧠 Giải quyết các vấn đề phức tạp step-by-step.",
                followersCount = 999999,
                followingCount = 1,
                isFollowing = true
            )
        )
        userDao.insertUsers(users)

        // Seed Default Posts
        val defaultPosts = listOf(
            Post(
                username = "@den_vau",
                displayName = "Đen Vâu",
                avatarUrl = "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?auto=format&fit=crop&w=150&q=80",
                content = "Mang tiền về cho mẹ, mang cả niềm vui lên NetVibe. Hôm nay trời mát mẻ thanh tịnh quá cả nhà ơi! Cảm ơn mọi người luôn ủng hộ Đen nhé! 🌅🌾",
                timestamp = System.currentTimeMillis() - 3600000 * 2, // 2h ago
                likesCount = 14200,
                repliesCount = 3,
                repostsCount = 450,
                isLiked = true,
                mediaUrl = "https://images.unsplash.com/photo-1501854140801-50d01698950b?auto=format&fit=crop&w=800&q=80"
            ),
            Post(
                username = "@tech_guru",
                displayName = "Tech Guru",
                avatarUrl = "https://images.unsplash.com/photo-1472099645785-5658abf4ff4e?auto=format&fit=crop&w=150&q=80",
                content = "Gemini 3.1 Pro vừa ra mắt chế độ Deep Thinking (Suy nghĩ sâu) giải mã các bài toán logic cực đỉnh. Các bạn đã thử bấm vào nút Trợ lý AI (Hình bộ não 🧠) trên menu của NetVibe chưa? Viết code, làm thơ, lập kế hoạch siêu đỉnh luôn! 🔥💻",
                timestamp = System.currentTimeMillis() - 3600000 * 4, // 4h ago
                likesCount = 285,
                repliesCount = 2,
                repostsCount = 18,
                mediaUrl = "https://images.unsplash.com/photo-1677442136019-21780efad99a?auto=format&fit=crop&w=800&q=80"
            ),
            Post(
                username = "@son_tung",
                displayName = "Sơn Tùng M-TP",
                avatarUrl = "https://images.unsplash.com/photo-1500648767791-00dcc994a43e?auto=format&fit=crop&w=150&q=80",
                content = "Chúng ta của hiện tại... đều đang lướt NetVibe rất vui vẻ! ❤️ Chúc các Sky yêu quý một ngày mới tràn ngập nụ cười và năng lượng tích cực nhé! Mãi yêu!",
                timestamp = System.currentTimeMillis() - 3600000 * 6, // 6h ago
                likesCount = 38900,
                repliesCount = 1,
                repostsCount = 1200,
                isLiked = false
            ),
            Post(
                username = "@gemini_bot",
                displayName = "AI Trợ Lý (Thinking Mode)",
                avatarUrl = "https://images.unsplash.com/photo-1618005182384-a83a8bd57fbe?auto=format&fit=crop&w=150&q=80",
                content = "Xin chào Quyết Phạm và các cư dân NetVibe! Tôi là Trợ lý AI thông minh tích hợp sẵn trong ứng dụng. Bạn có thể trò chuyện riêng với tôi bằng cách mở rộng menu bên, chọn 'Trợ lý AI' hoặc nhờ tôi tối ưu hóa bài viết trước khi đăng tải. Rất vui được đồng hành cùng bạn! 🧠✨",
                timestamp = System.currentTimeMillis() - 3600000 * 24, // 1 day ago
                likesCount = 988,
                repliesCount = 1,
                repostsCount = 142
            )
        )
        postDao.insertPosts(defaultPosts)

        // Seed some comments
        commentDao.insertComment(Comment(postId = 1, username = "@q_pv", displayName = "Quyết Phạm", avatarUrl = "https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?auto=format&fit=crop&w=150&q=80", content = "Nhạc anh Đen nghe lúc nào cũng mộc mạc và thấm thía cực kỳ! ❤️", timestamp = System.currentTimeMillis() - 3600000 * 1))
        commentDao.insertComment(Comment(postId = 1, username = "@son_tung", displayName = "Sơn Tùng M-TP", avatarUrl = "https://images.unsplash.com/photo-1500648767791-00dcc994a43e?auto=format&fit=crop&w=150&q=80", content = "Tuyệt vời quá anh ơi!", timestamp = System.currentTimeMillis() - 3600000 * 1))
        commentDao.insertComment(Comment(postId = 1, username = "@tech_guru", displayName = "Tech Guru", avatarUrl = "https://images.unsplash.com/photo-1472099645785-5658abf4ff4e?auto=format&fit=crop&w=150&q=80", content = "Vừa lướt NetVibe vừa nghe Đen Vâu chill hết nấc.", timestamp = System.currentTimeMillis() - 3600000 * 1))

        commentDao.insertComment(Comment(postId = 2, username = "@q_pv", displayName = "Quyết Phạm", avatarUrl = "https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?auto=format&fit=crop&w=150&q=80", content = "Tính năng Trợ lý AI Suy nghĩ Sâu siêu chất lượng, giải toán logic và lập kế hoạch rất chi tiết!", timestamp = System.currentTimeMillis() - 3600000 * 3))
        commentDao.insertComment(Comment(postId = 2, username = "@gemini_bot", displayName = "AI Trợ Lý (Thinking Mode)", avatarUrl = "https://images.unsplash.com/photo-1618005182384-a83a8bd57fbe?auto=format&fit=crop&w=150&q=80", content = "Cảm ơn Quyết Phạm đã tin dùng! Tôi luôn cố gắng phân tích thấu đáo nhất.", timestamp = System.currentTimeMillis() - 3600000 * 2))

        commentDao.insertComment(Comment(postId = 3, username = "@q_pv", displayName = "Quyết Phạm", avatarUrl = "https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?auto=format&fit=crop&w=150&q=80", content = "Mãi đỉnh Tùng ơi! 🔥", timestamp = System.currentTimeMillis() - 3600000 * 5))

        commentDao.insertComment(Comment(postId = 4, username = "@q_pv", displayName = "Quyết Phạm", avatarUrl = "https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?auto=format&fit=crop&w=150&q=80", content = "Chào bot nhé, tính năng AI xịn thật!", timestamp = System.currentTimeMillis() - 3600000 * 12))

        // Seed Default Notifications
        val defaultNotifications = listOf(
            Notification(
                type = "like",
                fromUsername = "@den_vau",
                fromDisplayName = "Đen Vâu",
                fromAvatarUrl = "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?auto=format&fit=crop&w=150&q=80",
                timestamp = System.currentTimeMillis() - 1200000, // 20m ago
                postContentSnippet = "Đang lập trình ứng dụng mạng xã hội siêu mượt NetVibe...",
                isRead = false
            ),
            Notification(
                type = "follow",
                fromUsername = "@son_tung",
                fromDisplayName = "Sơn Tùng M-TP",
                fromAvatarUrl = "https://images.unsplash.com/photo-1500648767791-00dcc994a43e?auto=format&fit=crop&w=150&q=80",
                timestamp = System.currentTimeMillis() - 3600000, // 1h ago
                isRead = false
            ),
            Notification(
                type = "comment",
                fromUsername = "@tech_guru",
                fromDisplayName = "Tech Guru",
                fromAvatarUrl = "https://images.unsplash.com/photo-1472099645785-5658abf4ff4e?auto=format&fit=crop&w=150&q=80",
                timestamp = System.currentTimeMillis() - 7200000, // 2h ago
                postContentSnippet = "Đang lập trình ứng dụng mạng xã hội siêu mượt NetVibe...",
                isRead = true
            )
        )
        for (notif in defaultNotifications) {
            notificationDao.insertNotification(notif)
        }

        // Seed initial AI welcome message
        aiChatDao.insertMessage(AIChatMessage(
            sender = "ai",
            text = "Chào Quyết Phạm! Tôi là Trợ lý AI Suy nghĩ Sâu (Thinking Mode) sử dụng mô hình Gemini 3.1 Pro. Bạn có thể hỏi tôi bất kỳ câu hỏi phức tạp nào, tôi sẽ tư duy và lập luận từng bước rõ ràng trước khi đưa ra câu trả lời cuối cùng. Tôi có thể giúp gì cho bạn hôm nay?",
            thinking = "Người dùng mới mở giao diện Trợ lý AI của NetVibe. Tôi cần chào đón họ bằng tiếng Việt thân thiện, giới thiệu vai trò của mình và khuyến khích họ đặt câu hỏi về công nghệ, viết lách hoặc các chủ đề phức tạp.",
            timestamp = System.currentTimeMillis()
        ))
    }

    // Interactive operations
    suspend fun toggleLikePost(postId: Int) = withContext(Dispatchers.IO) {
        val post = postDao.getPostByIdSync(postId) ?: return@withContext
        val isNowLiked = !post.isLiked
        val newLikesCount = if (isNowLiked) post.likesCount + 1 else post.likesCount - 1
        postDao.updatePost(post.copy(isLiked = isNowLiked, likesCount = newLikesCount))

        // If liked, create notification
        if (isNowLiked) {
            val me = userDao.getCurrentUser()
            if (me != null && post.username != me.username) {
                // simulated notification to the author (if author was me, don't notify me, but for mock, let's keep it)
            }
        }
    }

    suspend fun repostPost(postId: Int) = withContext(Dispatchers.IO) {
        val post = postDao.getPostByIdSync(postId) ?: return@withContext
        val isNowReposted = !post.isReposted
        val newRepostsCount = if (isNowReposted) post.repostsCount + 1 else post.repostsCount - 1
        postDao.updatePost(post.copy(isReposted = isNowReposted, repostsCount = newRepostsCount))
    }

    suspend fun addComment(postId: Int, content: String) = withContext(Dispatchers.IO) {
        val me = userDao.getCurrentUser() ?: return@withContext
        val comment = Comment(
            postId = postId,
            username = me.username,
            displayName = me.displayName,
            avatarUrl = me.avatarUrl,
            content = content,
            timestamp = System.currentTimeMillis()
        )
        commentDao.insertComment(comment)

        // Increment reply count in Post
        val post = postDao.getPostByIdSync(postId)
        if (post != null) {
            postDao.updatePost(post.copy(repliesCount = post.repliesCount + 1))
        }
    }

    suspend fun toggleFollowUser(username: String) = withContext(Dispatchers.IO) {
        val user = userDao.getUserByUsername(username) ?: return@withContext
        val isNowFollowing = !user.isFollowing
        val newFollowersCount = if (isNowFollowing) user.followersCount + 1 else user.followersCount - 1
        userDao.updateUser(user.copy(isFollowing = isNowFollowing, followersCount = newFollowersCount))

        // Create a simulated notification
        if (isNowFollowing) {
            notificationDao.insertNotification(Notification(
                type = "follow",
                fromUsername = user.username,
                fromDisplayName = user.displayName,
                fromAvatarUrl = user.avatarUrl,
                timestamp = System.currentTimeMillis()
            ))
        }
    }

    suspend fun createNewPost(content: String, mediaUrl: String? = null, mediaType: String? = "image", privacy: String = "public") = withContext(Dispatchers.IO) {
        val me = userDao.getCurrentUser() ?: return@withContext
        val post = Post(
            username = me.username,
            displayName = me.displayName,
            avatarUrl = me.avatarUrl,
            content = content,
            timestamp = System.currentTimeMillis(),
            likesCount = 0,
            repliesCount = 0,
            repostsCount = 0,
            mediaUrl = mediaUrl,
            mediaType = mediaType,
            privacy = privacy
        )
        postDao.insertPost(post)
    }

    suspend fun updateMyProfile(displayName: String, bio: String) = withContext(Dispatchers.IO) {
        val me = userDao.getCurrentUser() ?: return@withContext
        userDao.updateUser(me.copy(displayName = displayName, bio = bio))
    }

    suspend fun addAIChatMessage(text: String, thinking: String?, sender: String) = withContext(Dispatchers.IO) {
        aiChatDao.insertMessage(AIChatMessage(
            sender = sender,
            text = text,
            thinking = thinking,
            timestamp = System.currentTimeMillis()
        ))
    }

    suspend fun clearAIChatHistory() = withContext(Dispatchers.IO) {
        aiChatDao.clearHistory()
        // Re-seed welcome message
        aiChatDao.insertMessage(AIChatMessage(
            sender = "ai",
            text = "Mịch sử trò chuyện đã được xóa. Tôi là Trợ lý AI Suy nghĩ Sâu (Thinking Mode) của bạn. Hãy gửi cho tôi câu hỏi tiếp theo!",
            thinking = "Người dùng đã xóa lịch sử trò chuyện. Tôi cần gửi một tin nhắn chào mừng ngắn gọn để xác nhận và sẵn sàng cho câu hỏi tiếp theo.",
            timestamp = System.currentTimeMillis()
        ))
    }

    // Authentication methods
    suspend fun loginWithEmail(email: String, password: String): Boolean = withContext(Dispatchers.IO) {
        val user = userDao.getUserByEmail(email)
        if (user != null && user.password == password) {
            userDao.logoutAllUsers()
            userDao.updateUser(user.copy(isCurrentUser = true))
            return@withContext true
        }
        return@withContext false
    }

    suspend fun registerWithEmail(email: String, username: String, displayName: String, password: String): Boolean = withContext(Dispatchers.IO) {
        val emailExists = userDao.getUserByEmail(email) != null
        val usernameWithAt = if (username.startsWith("@")) username else "@$username"
        val usernameExists = userDao.getUserByUsername(usernameWithAt) != null
        
        if (emailExists || usernameExists) {
            return@withContext false
        }
        
        val newUser = User(
            username = usernameWithAt,
            displayName = displayName,
            avatarUrl = "https://images.unsplash.com/photo-1494790108377-be9c29b29330?auto=format&fit=crop&w=150&q=80",
            bio = "Thành viên mới của NetVibe ✨",
            followersCount = 0,
            followingCount = 0,
            isFollowing = false,
            isCurrentUser = true,
            email = email,
            password = password
        )
        userDao.logoutAllUsers()
        userDao.insertUser(newUser)
        return@withContext true
    }

    suspend fun logout() = withContext(Dispatchers.IO) {
        userDao.logoutAllUsers()
    }

    suspend fun googleLogin(email: String, displayName: String, avatarUrl: String): Boolean = withContext(Dispatchers.IO) {
        val existingUser = userDao.getUserByEmail(email)
        if (existingUser != null) {
            userDao.logoutAllUsers()
            userDao.updateUser(existingUser.copy(isCurrentUser = true))
            return@withContext true
        } else {
            // Register new Google user
            val usernamePart = email.substringBefore("@").lowercase().replace(Regex("[^a-z0-9_]"), "")
            var username = "@$usernamePart"
            var count = 1
            while (userDao.getUserByUsername(username) != null) {
                username = "@$usernamePart$count"
                count++
            }
            
            val newUser = User(
                username = username,
                displayName = displayName,
                avatarUrl = avatarUrl,
                bio = "Đăng nhập nhanh qua Google Gmail 🌐",
                followersCount = 0,
                followingCount = 0,
                isFollowing = false,
                isCurrentUser = true,
                email = email,
                password = null
            )
            userDao.logoutAllUsers()
            userDao.insertUser(newUser)
            return@withContext true
        }
    }
}
