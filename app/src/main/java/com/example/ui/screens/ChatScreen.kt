package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.MeetingRoom
import com.example.data.Message
import com.example.data.RoomParticipant
import com.example.ui.MainViewModel
import kotlinx.coroutines.launch

@Composable
fun ChatScreen(
    viewModel: MainViewModel,
    onProductClick: (Int) -> Unit
) {
    val currentRoom by viewModel.currentJoinedRoom.collectAsState()

    if (currentRoom == null) {
        RoomListView(viewModel)
    } else {
        ZoomMeetingView(viewModel, currentRoom!!, onProductClick)
    }
}

@Composable
fun RoomListView(viewModel: MainViewModel) {
    val rooms by viewModel.meetingRooms.collectAsState()
    var showCreateDialog by remember { mutableStateOf(false) }

    var newRoomName by remember { mutableStateOf("") }
    var newRoomDesc by remember { mutableStateOf("") }
    var newRoomHost by remember { mutableStateOf("Tôi (Host)") }
    var newRoomCat by remember { mutableStateOf("Làm đẹp") }

    Scaffold(
        topBar = {
            @OptIn(ExperimentalMaterial3Api::class)
            TopAppBar(
                title = {
                    Text(
                        "Phòng Nhóm Trực Tuyến 👥",
                        fontWeight = FontWeight.Bold,
                        fontSize = 19.sp
                    )
                },
                actions = {
                    IconButton(
                        onClick = { showCreateDialog = true },
                        modifier = Modifier.testTag("btn_open_create_room")
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Add,
                            contentDescription = "Tạo phòng mới",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f))
        ) {
            // Header Banner
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.primary,
                                MaterialTheme.colorScheme.secondary
                            )
                        )
                    )
                    .padding(16.dp)
            ) {
                Column {
                    Text(
                        "Không Gian Trò Chuyện Nhóm 💬",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        "Họp mặt online dạng Zoom tiện lợi. Tương tác trực tiếp cùng KOL, Seller, chia sẻ deal hot và nhận tư vấn mua sắm theo nhóm thời gian thực!",
                        color = Color.White.copy(alpha = 0.9f),
                        fontSize = 11.sp,
                        lineHeight = 16.sp
                    )
                }
            }

            Text(
                "Các phòng đang trực tuyến hoạt động",
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                color = MaterialTheme.colorScheme.onSurface
            )

            if (rooms.isEmpty()) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Filled.Groups, contentDescription = null, modifier = Modifier.size(48.dp), tint = Color.LightGray)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Chưa có phòng chat nhóm nào. Hãy là người đầu tiên tạo phòng!", color = Color.Gray, fontSize = 13.sp)
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(rooms) { room ->
                        RoomCard(room = room, onJoin = { viewModel.joinMeetingRoom(room) })
                    }
                }
            }
        }
    }

    // Create custom room dialog
    if (showCreateDialog) {
        AlertDialog(
            onDismissRequest = { showCreateDialog = false },
            title = { Text("Tạo phòng thảo luận mới ➕", fontWeight = FontWeight.Bold, fontSize = 16.sp) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = newRoomName,
                        onValueChange = { newRoomName = it },
                        label = { Text("Tên phòng họp") },
                        placeholder = { Text("Ví dụ: Hội yêu tai nghe chống ồn") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = newRoomDesc,
                        onValueChange = { newRoomDesc = it },
                        label = { Text("Mô tả chủ đề thảo luận") },
                        placeholder = { Text("Mách nhỏ cách săn mã freeship...") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = newRoomHost,
                        onValueChange = { newRoomHost = it },
                        label = { Text("Tên Host đại diện") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    
                    // Category Selection Row
                    Text("Danh mục thảo luận:", fontWeight = FontWeight.Medium, fontSize = 12.sp)
                    val categories = listOf("Làm đẹp", "Công nghệ", "Khuyến mãi", "Đời sống")
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        categories.forEach { cat ->
                            val isSelected = newRoomCat == cat
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(
                                        if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
                                    )
                                    .clickable { newRoomCat = cat }
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = cat,
                                    fontSize = 11.sp,
                                    color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newRoomName.isNotBlank() && newRoomDesc.isNotBlank()) {
                            viewModel.createCustomRoom(
                                newRoomName,
                                newRoomDesc,
                                newRoomHost,
                                newRoomCat
                            )
                            showCreateDialog = false
                            newRoomName = ""
                            newRoomDesc = ""
                        }
                    },
                    enabled = newRoomName.isNotBlank() && newRoomDesc.isNotBlank()
                ) {
                    Text("Tạo & Trực tuyến")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCreateDialog = false }) {
                    Text("Huỷ")
                }
            }
        )
    }
}

@Composable
fun RoomCard(room: MeetingRoom, onJoin: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("room_card_${room.id}"),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Category Tag
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(room.avatarColor).copy(alpha = 0.1f))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = room.category,
                        color = Color(room.avatarColor),
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.sp
                    )
                }

                // Active online members tag
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF00C853))
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${room.participantsCount} người",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.Gray
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = room.name,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = room.description,
                fontSize = 12.sp,
                color = Color.Gray,
                lineHeight = 17.sp,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(12.dp))

            Divider(color = MaterialTheme.colorScheme.outlineVariant)

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(Color(room.avatarColor)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = room.hostName.firstOrNull()?.toString() ?: "H",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = room.hostName,
                            fontWeight = FontWeight.Medium,
                            fontSize = 11.sp
                        )
                        Text(
                            text = "Host đại diện",
                            fontSize = 9.sp,
                            color = Color.Gray
                        )
                    }
                }

                Button(
                    onClick = onJoin,
                    modifier = Modifier.height(36.dp),
                    shape = RoundedCornerShape(18.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    ),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 0.dp)
                ) {
                    Icon(Icons.Filled.VideoCall, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Tham gia", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun ZoomMeetingView(
    viewModel: MainViewModel,
    room: MeetingRoom,
    onProductClick: (Int) -> Unit
) {
    val participants by viewModel.roomParticipants.collectAsState()
    val messages by viewModel.roomMessages.collectAsState()
    val typingMember by viewModel.roomAiTyping.collectAsState()

    val userCam by viewModel.userCameraOn.collectAsState()
    val userMic by viewModel.userMicOn.collectAsState()
    val userHand by viewModel.userHandRaised.collectAsState()

    var chatExpanded by remember { mutableStateOf(true) }
    var inputMessage by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()
    val listState = rememberLazyListState()

    // Scroll to bottom when new messages arrive
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .background(Color(0xFF121212)) // Dark movie-theatre background for Zoom
    ) {
        // Meeting top header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFFF0055)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.RecordVoiceOver,
                        contentDescription = "Live",
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = room.name,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.widthIn(max = 180.dp)
                    )
                    Text(
                        text = "🟢 Zoom Hội Nghị • ${participants.size + 1} người",
                        color = Color(0xFF00E676),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            // Leave Meeting Button (Red)
            IconButton(
                onClick = { viewModel.leaveMeetingRoom() },
                modifier = Modifier
                    .clip(CircleShape)
                    .background(Color(0xFFE53935))
                    .size(34.dp)
                    .testTag("btn_leave_meeting")
            ) {
                Icon(
                    imageVector = Icons.Filled.CallEnd,
                    contentDescription = "Rời phòng",
                    tint = Color.White,
                    modifier = Modifier.size(18.dp)
                )
            }
        }

        Divider(color = Color(0xFF2C2C2C))

        // Large Video Grid area (Zoom Meeting Panels)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(if (chatExpanded) 1.0f else 2.2f)
                .background(Color.Black)
                .padding(10.dp)
        ) {
            // Grid layout: we have a maximum of 6 elements (5 participants + 1 User)
            val allList = remember(participants, userCam, userMic, userHand) {
                val userPart = RoomParticipant(
                    id = "p_user",
                    name = "Bạn",
                    role = "Thành viên",
                    isCameraOn = userCam,
                    isMicOn = userMic,
                    isSpeaking = false,
                    hasRaisedHand = userHand,
                    avatarColor = 0xFF2196F3
                )
                listOf(userPart) + participants
            }

            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Row 1 (first 3 participants)
                Row(
                    modifier = Modifier.weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    allList.take(3).forEach { p ->
                        Box(modifier = Modifier.weight(1f)) {
                            ZoomVideoTile(participant = p)
                        }
                    }
                    repeat(3 - allList.take(3).size) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }

                // Row 2 (next 3 participants)
                Row(
                    modifier = Modifier.weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    allList.drop(3).take(3).forEach { p ->
                        Box(modifier = Modifier.weight(1f)) {
                            ZoomVideoTile(participant = p)
                        }
                    }
                    repeat(3 - allList.drop(3).take(3).size) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }

        // Sleek Zoom Controls bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF1E1E1E))
                .padding(vertical = 8.dp, horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Camera toggle
            IconButton(
                onClick = { viewModel.userCameraOn.value = !userCam },
                modifier = Modifier
                    .clip(CircleShape)
                    .background(if (userCam) Color(0xFF333333) else Color(0xFFE53935))
                    .size(40.dp)
                    .testTag("btn_toggle_cam")
            ) {
                Icon(
                    imageVector = if (userCam) Icons.Filled.Videocam else Icons.Filled.VideocamOff,
                    contentDescription = "Camera",
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
            }

            // Mic toggle
            IconButton(
                onClick = { viewModel.userMicOn.value = !userMic },
                modifier = Modifier
                    .clip(CircleShape)
                    .background(if (userMic) Color(0xFF00E676) else Color(0xFFE53935))
                    .size(40.dp)
                    .testTag("btn_toggle_mic")
            ) {
                Icon(
                    imageVector = if (userMic) Icons.Filled.Mic else Icons.Filled.MicOff,
                    contentDescription = "Microphone",
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
            }

            // Hand raise toggle
            IconButton(
                onClick = { viewModel.userHandRaised.value = !userHand },
                modifier = Modifier
                    .clip(CircleShape)
                    .background(if (userHand) Color(0xFFFFD600) else Color(0xFF333333))
                    .size(40.dp)
                    .testTag("btn_raise_hand")
            ) {
                Icon(
                    imageVector = Icons.Filled.PanTool,
                    contentDescription = "Giơ tay",
                    tint = if (userHand) Color.Black else Color.White,
                    modifier = Modifier.size(18.dp)
                )
            }

            // Share Product inside chat quick tip
            IconButton(
                onClick = {
                    // Share a random product
                    viewModel.sendRoomMessage("", isProductShared = true, sharedProdId = (1..4).random())
                },
                modifier = Modifier
                    .clip(CircleShape)
                    .background(Color(0xFF2979FF))
                    .size(40.dp)
                    .testTag("btn_share_product_to_room")
            ) {
                Icon(
                    imageVector = Icons.Filled.Share,
                    contentDescription = "Chia sẻ sản phẩm",
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
            }

            // Chat expand toggle
            IconButton(
                onClick = { chatExpanded = !chatExpanded },
                modifier = Modifier
                    .clip(CircleShape)
                    .background(if (chatExpanded) MaterialTheme.colorScheme.primary else Color(0xFF333333))
                    .size(40.dp)
                    .testTag("btn_toggle_chat")
            ) {
                Icon(
                    imageVector = if (chatExpanded) Icons.Filled.ChatBubble else Icons.Outlined.ChatBubble,
                    contentDescription = "Chat",
                    tint = Color.White,
                    modifier = Modifier.size(18.dp)
                )
            }
        }

        // Chat Panel Area
        if (chatExpanded) {
            Column(
                modifier = Modifier
                    .weight(1.3f)
                    .fillMaxWidth()
                    .background(Color(0xFF1E1E1E))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF161616))
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Cuộc Trò Chuyện Trong Phòng 💬",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp
                    )
                    Text(
                        text = "Mọi người có thể thấy tin nhắn của bạn",
                        color = Color.Gray,
                        fontSize = 9.sp
                    )
                }

                // Messages Column
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                ) {
                    LazyColumn(
                        state = listState,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 14.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(messages) { msg ->
                            RoomChatBubble(message = msg, onProductClick = onProductClick)
                        }

                        // typing indicator
                        if (typingMember != null) {
                            item {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(start = 8.dp, top = 4.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(6.dp)
                                            .clip(CircleShape)
                                            .background(Color(0xFF00E676))
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "$typingMember đang gõ trả lời...",
                                        fontSize = 10.sp,
                                        color = Color.Gray
                                    )
                                }
                            }
                        }
                    }
                }

                // Chat Input Field
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF161616))
                        .navigationBarsPadding()
                        .padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = inputMessage,
                        onValueChange = { inputMessage = it },
                        placeholder = { Text("Gửi tin nhắn hoặc hỏi ý kiến nhóm...", fontSize = 13.sp, color = Color.Gray) },
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp)
                            .testTag("input_room_chat"),
                        shape = RoundedCornerShape(22.dp),
                        textStyle = androidx.compose.ui.text.TextStyle(color = Color.White, fontSize = 13.sp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedContainerColor = Color(0xFF2C2C2C),
                            unfocusedContainerColor = Color(0xFF222222),
                            focusedBorderColor = Color(0xFF3F51B5),
                            unfocusedBorderColor = Color.Transparent
                        ),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.width(6.dp))

                    IconButton(
                        onClick = {
                            if (inputMessage.isNotBlank()) {
                                viewModel.sendRoomMessage(inputMessage)
                                inputMessage = ""
                            }
                        },
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Send,
                            contentDescription = "Gửi",
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ZoomVideoTile(participant: RoomParticipant) {
    val isSpeaking = participant.isSpeaking
    val isCameraOn = participant.isCameraOn

    // Pulsing speaking border color
    val infiniteTransition = rememberInfiniteTransition()
    val borderColor by infiniteTransition.animateColor(
        initialValue = Color(0xFF00FF66),
        targetValue = Color(0xFF00B0FF),
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    Card(
        modifier = Modifier
            .fillMaxSize()
            .then(
                if (isSpeaking) {
                    Modifier.border(2.dp, borderColor, RoundedCornerShape(12.dp))
                } else {
                    Modifier.border(1.dp, Color(0xFF2C2C2C), RoundedCornerShape(12.dp))
                }
            ),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF1E1E1E)
        )
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            if (isCameraOn) {
                // Camera is ON - beautiful mock live webcam display
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                listOf(
                                    Color(participant.avatarColor).copy(alpha = 0.3f),
                                    Color(0xFF0E0E0E)
                                )
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    if (isSpeaking) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(3.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            repeat(4) { idx ->
                                val heightMultiplier = when (idx) {
                                    0 -> 0.4f
                                    1 -> 0.8f
                                    2 -> 0.5f
                                    else -> 0.9f
                                }
                                val waveAnim = rememberInfiniteTransition().animateFloat(
                                    initialValue = 10.dp.value,
                                    targetValue = 40.dp.value,
                                    animationSpec = infiniteRepeatable(
                                        animation = tween(400 + idx * 100, easing = FastOutSlowInEasing),
                                        repeatMode = RepeatMode.Reverse
                                    )
                                )
                                Box(
                                    modifier = Modifier
                                        .width(3.dp)
                                        .height((waveAnim.value * heightMultiplier).dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFF00FF66))
                                )
                            }
                        }
                    } else {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(Color(participant.avatarColor)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = participant.name.firstOrNull()?.toString() ?: "",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp
                            )
                        }
                    }
                }
            } else {
                // Camera is OFF
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(Color(participant.avatarColor).copy(alpha = 0.7f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = participant.name.firstOrNull()?.toString() ?: "",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "🔴 Tắt Cam",
                        color = Color.Gray,
                        fontSize = 9.sp
                    )
                }
            }

            // Top-Right indicators
            Row(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(6.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (participant.hasRaisedHand) {
                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(Color(0xFFFFD600))
                            .padding(4.dp)
                    ) {
                        Text("✋", fontSize = 10.sp)
                    }
                }

                Box(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(if (participant.isMicOn) Color(0xFF00E676) else Color(0xFFE53935))
                        .padding(4.dp)
                ) {
                    Icon(
                        imageVector = if (participant.isMicOn) Icons.Filled.Mic else Icons.Filled.MicOff,
                        contentDescription = "Mic State",
                        tint = Color.White,
                        modifier = Modifier.size(10.dp)
                    )
                }
            }

            // Bottom-Left label
            Box(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(6.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(Color.Black.copy(alpha = 0.6f))
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
                Text(
                    text = if (participant.role == "Host") "${participant.name} (Host)" else participant.name,
                    color = Color.White,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
fun RoomChatBubble(
    message: Message,
    onProductClick: (Int) -> Unit
) {
    val isUser = message.sender == "Bạn"

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
    ) {
        if (!isUser) {
            Box(
                modifier = Modifier
                    .size(26.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = message.sender.firstOrNull()?.toString() ?: "",
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp
                )
            }
            Spacer(modifier = Modifier.width(6.dp))
        }

        Column(
            horizontalAlignment = if (isUser) Alignment.End else Alignment.Start,
            modifier = Modifier.weight(1.0f, fill = false)
        ) {
            if (!isUser) {
                Text(
                    text = message.sender,
                    color = Color.Gray,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(bottom = 2.dp)
                )
            }

            // Shared Product Tag inside Chat
            if (message.isProductShared) {
                Row(
                    modifier = Modifier
                        .padding(bottom = 6.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFF2C2C2C))
                        .clickable { onProductClick(message.sharedProductId) }
                        .padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Filled.ShoppingBag,
                        contentDescription = "Product shared",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Column {
                        Text(
                            text = message.sharedProductName,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            color = Color.White,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.widthIn(max = 140.dp)
                        )
                        Text(
                            text = "${message.sharedProductPrice.toInt()}đ",
                            color = Color(0xFFFF0055),
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp
                        )
                    }
                }
            }

            // Message Bubble Text
            Box(
                modifier = Modifier
                    .clip(
                        RoundedCornerShape(
                            topStart = 12.dp,
                            topEnd = 12.dp,
                            bottomStart = if (isUser) 12.dp else 2.dp,
                            bottomEnd = if (isUser) 2.dp else 12.dp
                        )
                    )
                    .background(
                        if (isUser) MaterialTheme.colorScheme.primary else Color(0xFF2C2C2C)
                    )
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                Text(
                    text = message.content,
                    color = Color.White,
                    fontSize = 13.sp,
                    lineHeight = 18.sp
                )
            }
        }
    }
}
