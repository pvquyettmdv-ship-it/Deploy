package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.data.local.User
import com.example.ui.viewmodel.SocialViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

data class MockMessage(
    val id: Int,
    val senderIsMe: Boolean,
    val text: String,
    val timestamp: Long = System.currentTimeMillis()
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MessagesScreen(
    viewModel: SocialViewModel,
    initialChatUsername: String? = null,
    onChatOpened: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val otherUsers by viewModel.otherUsers.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var activeChatUser by remember { mutableStateOf<User?>(null) }
    var chatMessageInput by remember { mutableStateOf("") }

    // Resolve initial chat user if passed
    LaunchedEffect(initialChatUsername, otherUsers) {
        if (initialChatUsername != null) {
            val target = otherUsers.find { it.username == initialChatUsername }
            if (target != null) {
                activeChatUser = target
                onChatOpened()
            }
        }
    }
    
    // Simulating chat history per user
    val chatHistories = remember {
        mutableStateMapOf<String, List<MockMessage>>().apply {
            put("@den_vau", listOf(
                MockMessage(1, false, "Chào Quyết Phạm! Nghe bảo chú đang code app NetVibe chất lượng lắm đúng không?"),
                MockMessage(2, true, "Dạ vâng anh Đen ơi! Em đang tối ưu hóa mượt mà từng màn hình."),
                MockMessage(3, false, "Tuyệt vời quá! Hôm nào rảnh anh em mình làm chén trà chill đàm đạo nhé.")
            ))
            put("@son_tung", listOf(
                MockMessage(1, false, "Hello em trai! Đã cài nhạc của anh làm nhạc nền app chưa? Haha."),
                MockMessage(2, true, "Dạ em chuẩn bị thêm vào tính năng phát nhạc chill rồi anh Tùng ạ!"),
                MockMessage(3, false, "Quá đỉnh luôn Sky ơi! Keep going nhé!")
            ))
            put("@tech_guru", listOf(
                MockMessage(1, false, "Sản phẩm NetVibe này thiết kế M3 đẹp đấy. Có xài Room DB với Flow không?"),
                MockMessage(2, true, "Dạ có ạ! Mọi luồng dữ liệu đều được lưu Room offline-first và bắn Flow lên Compose."),
                MockMessage(3, false, "Sách giáo khoa luôn! Vote 5 sao.")
            ))
        }
    }

    // Auto-create empty history if chatting with someone new
    LaunchedEffect(activeChatUser) {
        activeChatUser?.let { user ->
            if (!chatHistories.containsKey(user.username)) {
                chatHistories[user.username] = emptyList()
            }
        }
    }

    if (activeChatUser != null) {
        val user = activeChatUser!!
        val messages = chatHistories[user.username] ?: emptyList()
        val listState = rememberLazyListState()
        var isTypingSimulator by remember { mutableStateOf(false) }

        // Auto scroll to bottom
        LaunchedEffect(messages.size, isTypingSimulator) {
            if (messages.isNotEmpty()) {
                listState.animateScrollToItem(messages.size - 1)
            }
        }

        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            AsyncImage(
                                model = user.avatarUrl,
                                contentDescription = null,
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(user.displayName, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                                Text(user.username, fontSize = 11.sp, color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f))
                            }
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = { activeChatUser = null }) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Quay lại")
                        }
                    }
                )
            },
            modifier = modifier
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                // Chats
                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    contentPadding = PaddingValues(vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(messages) { msg ->
                        ChatMsgBubble(msg = msg)
                    }

                    if (isTypingSimulator) {
                        item {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.Start
                            ) {
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Text(
                                        text = "${user.displayName} đang nhập tin nhắn...",
                                        fontSize = 11.sp,
                                        modifier = Modifier.padding(8.dp),
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }
                        }
                    }
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outline)

                // Input Bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextField(
                        value = chatMessageInput,
                        onValueChange = { chatMessageInput = it },
                        placeholder = { Text("Gửi tin nhắn trực tiếp...", fontSize = 14.sp) },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("direct_chat_input"),
                        shape = RoundedCornerShape(24.dp),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent
                        )
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    IconButton(
                        onClick = {
                            val userText = chatMessageInput
                            chatMessageInput = ""
                            val currentList = chatHistories[user.username] ?: emptyList()
                            
                            // 1. Add user message
                            val newUserMsg = MockMessage(currentList.size + 1, true, userText)
                            chatHistories[user.username] = currentList + newUserMsg

                            // 2. Trigger automated typing delayed reply
                            isTypingSimulator = true
                            coroutineScope.launch {
                                delay(1500) // Delay to feel realistic
                                val celebrityQuotes = when (user.username) {
                                    "@den_vau" -> listOf(
                                        "Lời anh nói mộc mạc như cỏ cây hoa lá. Chúc chú Quyết vạn sự hanh thông trên chặng đường sáng tạo này nhé! 🌾🎤",
                                        "Cảm ơn chú đã luôn lắng nghe tâm tư của anh qua các bài hát. NetVibe xịn sò lắm!",
                                        "Lao động hăng say, tình thương đầy túi. Cố lên nhé đồng chí!"
                                    )
                                    "@son_tung" -> listOf(
                                        "Uốn mình dưới ánh mặt trời tỏa sáng rực rỡ nhé Sky! Luôn bên chú em! 🌟🔥",
                                        "Thật tuyệt khi chú là một lập trình viên đầy đam mê. NetVibe đúng chất kết nối tâm hồn!",
                                        "Keep going! Chúng ta là thế hệ của sự bức phá!"
                                    )
                                    else -> listOf(
                                        "Bên kỹ thuật check thấy app phản hồi dưới 16ms là chuẩn mượt 60fps rồi đấy! Lập trình cứng tay đấy. 👍💻",
                                        "Tuyệt vời! Bản cập nhật tới nhớ thêm widget thông minh ngoài màn hình chủ nhé.",
                                        "Rất chuyên nghiệp! AI phản hồi cực kỳ chuẩn chỉ."
                                    )
                                }
                                val randomQuote = celebrityQuotes.random()
                                val updatedList = chatHistories[user.username] ?: emptyList()
                                val newReplyMsg = MockMessage(updatedList.size + 1, false, randomQuote)
                                chatHistories[user.username] = updatedList + newReplyMsg
                                isTypingSimulator = false
                            }
                        },
                        enabled = chatMessageInput.isNotBlank() && !isTypingSimulator,
                        colors = IconButtonDefaults.iconButtonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        ),
                        modifier = Modifier.testTag("direct_chat_send_btn")
                    ) {
                        Icon(Icons.Default.Send, contentDescription = "Gửi")
                    }
                }
            }
        }
    } else {
        // List of Active/Simulated Conversations
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Hộp thư tin nhắn", fontWeight = FontWeight.Bold, fontSize = 18.sp) }
                )
            },
            modifier = modifier
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                // Info Banner
                Card(
                    modifier = Modifier.padding(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f))
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Forum, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "Trò chuyện trực tiếp với những người nổi tiếng hoặc bạn bè bạn đang theo dõi trên hệ thống NetVibe!",
                            fontSize = 12.sp,
                            lineHeight = 16.sp,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }

                Text(
                    text = "Cuộc trò chuyện gần đây",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                )

                // List of users we have a chat history with
                val chatUsers = remember(otherUsers, chatHistories.keys.toList()) {
                    otherUsers.filter {
                        it.username == "@den_vau" || 
                        it.username == "@son_tung" || 
                        it.username == "@tech_guru" ||
                        chatHistories.containsKey(it.username)
                    }
                }

                LazyColumn(modifier = Modifier.fillMaxWidth().weight(1f)) {
                    items(chatUsers) { user ->
                        val history = chatHistories[user.username] ?: emptyList()
                        val lastMsg = history.lastOrNull()?.text ?: "Chưa có tin nhắn nào"

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { activeChatUser = user }
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            AsyncImage(
                                model = user.avatarUrl,
                                contentDescription = null,
                                modifier = Modifier
                                    .size(52.dp)
                                    .clip(CircleShape)
                                    .border(1.5.dp, MaterialTheme.colorScheme.outline, CircleShape),
                                contentScale = ContentScale.Crop
                            )
                            Spacer(modifier = Modifier.width(14.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = user.displayName,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = lastMsg,
                                    fontSize = 13.sp,
                                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            
                            Icon(
                                imageVector = Icons.Default.ChevronRight,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.3f)
                            )
                        }
                        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                    }
                }
            }
        }
    }
}

@Composable
fun ChatMsgBubble(msg: MockMessage) {
    val isMe = msg.senderIsMe

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isMe) Arrangement.End else Arrangement.Start
    ) {
        Card(
            colors = CardDefaults.cardColors(
                containerColor = if (isMe) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
            ),
            shape = if (isMe) {
                RoundedCornerShape(16.dp, 16.dp, 2.dp, 16.dp)
            } else {
                RoundedCornerShape(16.dp, 16.dp, 16.dp, 2.dp)
            },
            modifier = Modifier.widthIn(max = 280.dp)
        ) {
            Text(
                text = msg.text,
                fontSize = 14.sp,
                lineHeight = 18.sp,
                color = if (isMe) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
            )
        }
    }
}
