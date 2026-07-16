package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.api.GeminiClient
import com.example.data.api.GeminiResult
import com.example.data.local.*
import com.example.data.repository.SocialRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class SocialViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = SocialRepository.getInstance(application)

    // App Preferences / UI State
    val isDarkMode = MutableStateFlow(true) // Committed default aesthetic: premium dark!

    // Safety & Privacy State (Report, Block, Hide)
    val hiddenPostIds = MutableStateFlow<Set<Int>>(emptySet())
    val reportedPostIds = MutableStateFlow<Set<Int>>(emptySet())
    val blockedUsernames = MutableStateFlow<Set<String>>(emptySet())

    // Social Data Flows
    val currentUser: StateFlow<User?> = repository.currentUser.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = null
    )

    val otherUsers: StateFlow<List<User>> = combine(
        repository.otherUsers,
        blockedUsernames
    ) { users, blocked ->
        users.filter { it.username !in blocked }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val allPosts: StateFlow<List<Post>> = combine(
        repository.allPosts,
        hiddenPostIds,
        reportedPostIds,
        blockedUsernames
    ) { posts, hidden, reported, blocked ->
        posts.filter { post ->
            post.id !in hidden && post.id !in reported && post.username !in blocked
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val notifications: StateFlow<List<Notification>> = combine(
        repository.allNotifications,
        blockedUsernames
    ) { notifs, blocked ->
        notifs.filter { it.fromUsername !in blocked }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val aiMessages: StateFlow<List<AIChatMessage>> = repository.aiMessages.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // Interactive State
    val searchQuery = MutableStateFlow("")
    val selectedPostId = MutableStateFlow<Int?>(null)
    val activeNotificationFilter = MutableStateFlow("all") // "all", "like", "comment", "follow"

    // AI Generation States
    val isAiThinking = MutableStateFlow(false)
    val aiThinkingProcess = MutableStateFlow<String?>(null)
    val aiError = MutableStateFlow<String?>(null)

    // Draft post AI optimization state
    val isOptimizingPost = MutableStateFlow(false)
    val optimizedPostResult = MutableSharedFlow<String>()

    init {
        // Seed default database values if empty
        viewModelScope.launch {
            repository.initDefaultData()
        }
    }

    // Toggle theme
    fun toggleDarkMode() {
        isDarkMode.value = !isDarkMode.value
    }

    // Social actions
    fun toggleLike(postId: Int) {
        viewModelScope.launch {
            repository.toggleLikePost(postId)
        }
    }

    fun repostPost(postId: Int) {
        viewModelScope.launch {
            repository.repostPost(postId)
        }
    }

    fun addComment(postId: Int, content: String) {
        if (content.isBlank()) return
        viewModelScope.launch {
            repository.addComment(postId, content)
        }
    }

    fun toggleFollowUser(username: String) {
        viewModelScope.launch {
            repository.toggleFollowUser(username)
        }
    }

    fun createPost(content: String, mediaUrl: String? = null, mediaType: String? = "image", privacy: String = "public") {
        if (content.isBlank()) return
        viewModelScope.launch {
            repository.createNewPost(content, mediaUrl, mediaType, privacy)
        }
    }

    fun hidePost(postId: Int) {
        hiddenPostIds.value = hiddenPostIds.value + postId
    }

    fun reportPost(postId: Int) {
        reportedPostIds.value = reportedPostIds.value + postId
    }

    fun blockUser(username: String) {
        blockedUsernames.value = blockedUsernames.value + username
    }

    fun updateProfile(displayName: String, bio: String) {
        viewModelScope.launch {
            repository.updateMyProfile(displayName, bio)
        }
    }

    fun clearNotifications() {
        viewModelScope.launch {
            repository.notificationDao.markAllAsRead()
        }
    }

    // AI Thinking Assistant Chat flow using gemini-3.1-pro-preview with HIGH thinkingLevel
    fun sendChatMessage(userText: String) {
        if (userText.isBlank()) return
        viewModelScope.launch {
            // Append user message
            repository.addAIChatMessage(userText, null, "user")
            isAiThinking.value = true
            aiThinkingProcess.value = "Đang tư duy, lập luận từng bước bằng Gemini 3.1 Pro..."
            aiError.value = null

            val systemInstruction = "Bạn là Trợ lý AI Suy nghĩ Sâu (Thinking Mode) thông minh, hóm hỉnh và hữu ích tích hợp sẵn trong Mạng xã hội NetVibe. Hãy sử dụng lập luận chi tiết từng bước bằng tiếng Việt để giải quyết các thắc mắc phức tạp của người dùng."

            when (val result = GeminiClient.generateWithThinking(userText, systemInstruction)) {
                is GeminiResult.Success -> {
                    aiThinkingProcess.value = result.thinking
                    repository.addAIChatMessage(
                        text = result.response,
                        thinking = result.thinking,
                        sender = "ai"
                    )
                }
                is GeminiResult.Error -> {
                    aiError.value = result.message
                    repository.addAIChatMessage(
                        text = "Rất tiếc, đã xảy ra lỗi khi kết nối với máy chủ AI: ${result.message}",
                        thinking = "Lỗi kết nối hoặc API Key chưa được cài đặt chính xác.",
                        sender = "ai"
                    )
                }
            }
            isAiThinking.value = false
        }
    }

    fun clearAIChat() {
        viewModelScope.launch {
            repository.clearAIChatHistory()
        }
    }

    // AI Post Optimization flow using gemini-3.1-pro-preview
    fun optimizeDraftPost(draftContent: String) {
        if (draftContent.isBlank()) return
        viewModelScope.launch {
            isOptimizingPost.value = true
            val prompt = """
                Bạn là chuyên gia sáng tạo nội dung mạng xã hội. Hãy viết lại bài đăng này bằng tiếng Việt sao cho cực kỳ thu hút, dễ đọc, bố cục thông minh và thêm một vài emoji biểu cảm phù hợp. Giữ nguyên ý nghĩa ban đầu và không thêm bớt thông tin sai lệch.
                
                Bài đăng gốc:
                "$draftContent"
            """.trimIndent()

            val systemInstruction = "Bạn là trợ lý viết bài tối ưu hóa mạng xã hội thông thái."

            when (val result = GeminiClient.generateWithThinking(prompt, systemInstruction)) {
                is GeminiResult.Success -> {
                    optimizedPostResult.emit(result.response)
                }
                is GeminiResult.Error -> {
                    // Fail silently or notify
                    optimizedPostResult.emit("$draftContent (Lỗi AI: ${result.message})")
                }
            }
            isOptimizingPost.value = false
        }
    }
}
