package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.viewmodel.SocialViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SystemArchitectureScreen(
    viewModel: SocialViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val currentUser by viewModel.currentUser.collectAsStateWithLifecycle()
    val allPosts by viewModel.allPosts.collectAsStateWithLifecycle()

    // Cache simulation state
    var isCacheEnabled by remember { mutableStateOf(true) }
    var cachedData by remember { mutableStateOf<String?>(null) }
    var cacheHitCount by remember { mutableStateOf(0) }
    var cacheMissCount by remember { mutableStateOf(0) }

    // Simulation states
    var currentStep by remember { mutableStateOf(0) } // 0: Idle, 1..7: Flow steps
    var currentLog by remember { mutableStateOf(listOf<String>()) }
    var simulatedHttpResponseCode by remember { mutableStateOf<Int?>(null) }
    var jsonResponseContent by remember { mutableStateOf("") }
    var simulatedUIPreviewText by remember { mutableStateOf("") }
    var isProcessing by remember { mutableStateOf(false) }

    // Helper to add logs
    fun addLog(msg: String) {
        currentLog = currentLog + "⚡ $msg"
    }

    // Step-by-step simulator
    fun runWorkflowSimulation(actionType: String, postContent: String = "") {
        if (isProcessing) return
        isProcessing = true
        currentLog = emptyList()
        currentStep = 1
        simulatedHttpResponseCode = null
        jsonResponseContent = ""
        simulatedUIPreviewText = ""

        coroutineScope.launch {
            // STEP 1: Người dùng kích hoạt hành động
            addLog("[Người dùng] Bấm nút kích hoạt hành động: $actionType")
            delay(800)
            currentStep = 2

            // STEP 2: Frontend Client đóng gói HTTP Request
            val endpoint = when (actionType) {
                "GET_FEED" -> "GET /api/posts?limit=10"
                "POST_POST" -> "POST /api/posts"
                "GET_PROFILE" -> "GET /api/profile?username=${currentUser?.username}"
                "AI_CHAT" -> "POST /api/ai/chat"
                else -> "GET /api/system/status"
            }
            addLog("[Frontend Web/App] Đóng gói và gửi yêu cầu HTTP: $endpoint")
            addLog("[Frontend] Headers: { Content-Type: application/json, Authorization: Bearer dev_session_token }")
            delay(800)
            currentStep = 3

            // STEP 3: Đường dẫn HTTP Request truyền đi tới API
            addLog("[Yêu cầu mạng (HTTP)] Gửi gói tin qua mạng TCP/IP...")
            addLog("[Yêu cầu mạng (HTTP)] Độ trễ giả lập: 45ms")
            delay(800)
            currentStep = 4

            // STEP 4: API Gateway tiếp nhận
            addLog("[API Gateway] Nhận request: $endpoint")
            addLog("[API Gateway] Đã định tuyến (Route) request tới lớp Backend Controller.")
            delay(800)
            currentStep = 5

            // STEP 5: Backend Controller xử lý nghiệp vụ, kiểm tra Cache và CSDL
            addLog("[Backend Server] Bắt đầu xử lý logic luồng dữ liệu...")
            
            val useCache = isCacheEnabled && (actionType == "GET_FEED" || actionType == "GET_PROFILE")
            var hit = false

            if (useCache) {
                addLog("[Backend] Kiểm tra bộ nhớ đệm (Cache) cho endpoint: $endpoint")
                delay(600)
                if (cachedData != null && actionType == "GET_FEED") {
                    hit = true
                    cacheHitCount++
                    addLog("[Cache] 🟢 CACHE HIT! Tìm thấy dữ liệu bảng tin trong bộ nhớ Cache.")
                } else {
                    cacheMissCount++
                    addLog("[Cache] 🔴 CACHE MISS! Không tìm thấy dữ liệu hợp lệ trong Cache.")
                }
            }

            if (!hit) {
                addLog("[CSDL Database] Truy vấn Cơ sở dữ liệu SQLite (Room DB) thực tế...")
                delay(800)
                when (actionType) {
                    "GET_FEED" -> {
                        val postsSize = allPosts.size
                        addLog("[CSDL Database] SQL Query: SELECT * FROM posts ORDER BY timestamp DESC")
                        addLog("[CSDL Database] Kết quả: Lấy ra thành công $postsSize bài viết từ bảng 'posts'.")
                        
                        // Populate Cache
                        if (isCacheEnabled) {
                            cachedData = "{\"status\":\"success\",\"count\":$postsSize,\"posts\":[...]}"
                            addLog("[Cache] 💾 Đã lưu dữ liệu bảng tin vào Cache để tối ưu hóa cho lần sau.")
                        }
                    }
                    "POST_POST" -> {
                        addLog("[CSDL Database] SQL Insert: INSERT INTO posts (username, content, timestamp) VALUES ('${currentUser?.username}', '$postContent', ...)")
                        viewModel.createPost(postContent, "public", null, "image")
                        addLog("[CSDL Database] Ghi cơ sở dữ liệu thành công! Hủy cache bảng tin cũ để đảm bảo tính nhất quán dữ liệu.")
                        cachedData = null // Invalidate cache on write
                    }
                    "GET_PROFILE" -> {
                        addLog("[CSDL Database] SQL Query: SELECT * FROM users WHERE username = '${currentUser?.username}' LIMIT 1")
                        addLog("[CSDL Database] Kết quả: Tìm thấy người dùng: ${currentUser?.displayName}")
                    }
                    "AI_CHAT" -> {
                        addLog("[CSDL Database] Đọc lịch sử hội thoại từ SQLite và gọi mô hình AI Gemini.")
                        addLog("[Backend Service] Đang kết nối mạng API Gemini thế hệ mới...")
                    }
                }
            }

            delay(800)
            currentStep = 6

            // STEP 6: Đóng gói Response JSON trả về
            simulatedHttpResponseCode = 200
            jsonResponseContent = when (actionType) {
                "GET_FEED" -> """
                {
                  "status": "success",
                  "code": 200,
                  "timestamp": ${System.currentTimeMillis()},
                  "data": {
                    "posts_count": ${allPosts.size},
                    "source": "${if (hit) "Cache (In-Memory)" else "Database (SQLite)"}"
                  }
                }
                """.trimIndent()
                "POST_POST" -> """
                {
                  "status": "success",
                  "code": 201,
                  "message": "Đăng bài viết mới thành công!",
                  "data": {
                    "content": "$postContent",
                    "author": "${currentUser?.username}"
                  }
                }
                """.trimIndent()
                "GET_PROFILE" -> """
                {
                  "status": "success",
                  "code": 200,
                  "data": {
                    "username": "${currentUser?.username}",
                    "display_name": "${currentUser?.displayName}",
                    "bio": "${currentUser?.bio}",
                    "followers": ${currentUser?.followersCount}
                  }
                }
                """.trimIndent()
                "AI_CHAT" -> """
                {
                  "status": "success",
                  "code": 200,
                  "ai_response": "Xin chào! Tôi là Trợ lý AI đang chạy tích hợp với mạng xã hội NetVibe của bạn."
                }
                """.trimIndent()
                else -> "{\"status\":\"ok\"}"
            }
            addLog("[Backend] Đóng gói thành công dữ liệu JSON Response (Mã HTTP: $simulatedHttpResponseCode)")
            delay(800)
            currentStep = 7

            // STEP 7: Frontend hiển thị dữ liệu lên giao diện
            simulatedUIPreviewText = when (actionType) {
                "GET_FEED" -> "📋 Bảng tin: Đã hiển thị ${allPosts.size} bài viết mới nhất (Lấy từ ${if (hit) "Cache" else "Database"})"
                "POST_POST" -> "✨ Bài viết mới của bạn đã xuất hiện trên bảng tin: \"$postContent\""
                "GET_PROFILE" -> "👤 Hồ sơ của bạn: ${currentUser?.displayName} (${currentUser?.username})"
                "AI_CHAT" -> "🤖 AI Trợ lý: \"Xin chào! Tôi luôn đồng hành cùng bạn.\""
                else -> "Giao diện đã cập nhật thành công!"
            }
            addLog("[Frontend hiển thị] Giao diện Jetpack Compose tiến hành dựng lại UI (Recompose).")
            addLog("[Frontend] 🎨 Hiển thị thành công dữ liệu lên màn hình người dùng!")
            
            isProcessing = false
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Hero Header Block
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f)),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Dns,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Kiến trúc hệ thống NetVibe",
                            fontWeight = FontWeight.Black,
                            fontSize = 18.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Mô phỏng trực quan dòng dữ liệu của ứng dụng theo kiến trúc Client-Server hiện đại. Thích hợp để hiểu cách vận hành của Cache, Database, API và giao diện UI.",
                        fontSize = 12.sp,
                        lineHeight = 16.sp,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f)
                    )
                }
            }
        }

        // Cache Panel Configuration
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Bộ nhớ đệm (Cache Simulation)",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Hit: $cacheHitCount | Miss: $cacheMissCount | Bộ đệm: ${if (cachedData == null) "Trống" else "Đã lưu bảng tin"}",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Switch(
                            checked = isCacheEnabled,
                            onCheckedChange = { isCacheEnabled = it },
                            modifier = Modifier.scale(0.8f)
                        )
                    }
                }
            }
        }

        // Interactive Diagram Block
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                        shape = RoundedCornerShape(16.dp)
                    ),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "Sơ đồ hoạt động thời gian thực",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                        modifier = Modifier.align(Alignment.Start)
                    )

                    // Node 1: Người dùng (User)
                    DiagramNode(
                        title = "1. Người dùng (User)",
                        subtitle = currentUser?.displayName ?: "Guest",
                        icon = Icons.Default.Person,
                        isActive = currentStep == 1,
                        activeColor = MaterialTheme.colorScheme.primary
                    )

                    ArrowIndicator(isActive = currentStep == 1 || currentStep == 2)

                    // Node 2: Frontend Client
                    DiagramNode(
                        title = "2. Giao diện (Frontend Web/App)",
                        subtitle = "NetVibe Client (Compose App)",
                        icon = Icons.Default.PhoneAndroid,
                        isActive = currentStep == 2,
                        activeColor = MaterialTheme.colorScheme.secondary
                    )

                    ArrowIndicator(isActive = currentStep == 2 || currentStep == 3)

                    // Node 3: Request Network
                    DiagramNode(
                        title = "3. Yêu cầu (HTTP Request)",
                        subtitle = "Headers, JSON payload, Token",
                        icon = Icons.Default.SwapHoriz,
                        isActive = currentStep == 3,
                        activeColor = MaterialTheme.colorScheme.tertiary
                    )

                    ArrowIndicator(isActive = currentStep == 3 || currentStep == 4)

                    // Node 4: API Endpoint Gateway
                    DiagramNode(
                        title = "4. Cổng tiếp nhận (API Gateway)",
                        subtitle = "Định tuyến tuyến đường (Routing)",
                        icon = Icons.Default.Hub,
                        isActive = currentStep == 4,
                        activeColor = MaterialTheme.colorScheme.error
                    )

                    ArrowIndicator(isActive = currentStep == 4 || currentStep == 5)

                    // Node 5: Backend & Cache & CSDL (Split View)
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(
                                width = if (currentStep == 5) 2.dp else 1.dp,
                                color = if (currentStep == 5) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                                shape = RoundedCornerShape(12.dp)
                            ),
                        colors = CardDefaults.cardColors(
                            containerColor = if (currentStep == 5) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.1f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.1f)
                        )
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(Icons.Default.Cloud, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("5. Dịch vụ xử lý (Backend Server)", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                // Cache Node
                                Card(
                                    modifier = Modifier
                                        .weight(1f)
                                        .border(
                                            width = if (currentStep == 5 && isCacheEnabled) 1.5.dp else 1.dp,
                                            color = if (currentStep == 5 && isCacheEnabled) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                                            shape = RoundedCornerShape(8.dp)
                                        ),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                                ) {
                                    Column(
                                        modifier = Modifier.padding(8.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Icon(Icons.Default.FlashOn, contentDescription = null, tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.size(14.dp))
                                        Text("Bộ nhớ đệm (Cache)", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                        Text(if (cachedData != null) "Đã đầy" else "Đang trống", fontSize = 8.sp, color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f))
                                    }
                                }

                                // Database Node
                                Card(
                                    modifier = Modifier
                                        .weight(1f)
                                        .border(
                                            width = if (currentStep == 5 && cachedData == null) 1.5.dp else 1.dp,
                                            color = if (currentStep == 5 && cachedData == null) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                                            shape = RoundedCornerShape(8.dp)
                                        ),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                                ) {
                                    Column(
                                        modifier = Modifier.padding(8.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Icon(Icons.Default.Storage, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(14.dp))
                                        Text("Cơ sở dữ liệu (Room)", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                        Text("Tổng bài: ${allPosts.size}", fontSize = 8.sp, color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f))
                                    }
                                }
                            }
                        }
                    }

                    ArrowIndicator(isActive = currentStep == 5 || currentStep == 6)

                    // Node 6: Response JSON
                    DiagramNode(
                        title = "6. Phản hồi mạng (JSON Response)",
                        subtitle = if (simulatedHttpResponseCode != null) "HTTP $simulatedHttpResponseCode | Trả về JSON" else "Đang chờ...",
                        icon = Icons.Default.Code,
                        isActive = currentStep == 6,
                        activeColor = MaterialTheme.colorScheme.secondary
                    )

                    ArrowIndicator(isActive = currentStep == 6 || currentStep == 7)

                    // Node 7: Render UI
                    DiagramNode(
                        title = "7. Render giao diện (Display)",
                        subtitle = if (simulatedUIPreviewText.isNotBlank()) "Màn hình cập nhật dữ liệu mới" else "Đang chờ...",
                        icon = Icons.Default.VideoLabel,
                        isActive = currentStep == 7,
                        activeColor = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }

        // Action Panel Launcher Buttons
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp),
                border = BoxBorderDefaults()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Chọn lệnh giả lập hệ thống",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(
                                onClick = { runWorkflowSimulation("GET_FEED") },
                                enabled = !isProcessing,
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                            ) {
                                Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("GET /api/posts", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }

                            Button(
                                onClick = { runWorkflowSimulation("GET_PROFILE") },
                                enabled = !isProcessing,
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                            ) {
                                Icon(Icons.Default.Person, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("GET /api/profile", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(
                                onClick = { runWorkflowSimulation("POST_POST", "Học kiến trúc hệ thống mạng cực chill cùng NetVibe!") },
                                enabled = !isProcessing,
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary)
                            ) {
                                Icon(Icons.Default.Publish, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("POST /api/posts", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }

                            Button(
                                onClick = { runWorkflowSimulation("AI_CHAT") },
                                enabled = !isProcessing,
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.outline)
                            ) {
                                Icon(Icons.Default.Psychology, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("POST /api/ai/chat", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        OutlinedButton(
                            onClick = {
                                cachedData = null
                                cacheHitCount = 0
                                cacheMissCount = 0
                                currentLog = emptyList()
                                currentStep = 0
                                simulatedHttpResponseCode = null
                                jsonResponseContent = ""
                                simulatedUIPreviewText = ""
                                Toast.makeText(context, "Đã làm trống dữ liệu Cache & Bộ đo hiệu năng!", Toast.LENGTH_SHORT).show()
                            },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
                        ) {
                            Icon(Icons.Default.DeleteSweep, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Dọn dẹp Cache & Khởi động lại", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // Live Log Console Panel
        if (currentLog.isNotEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color.Black),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(bottom = 8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(if (isProcessing) Color.Green else Color.Gray)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Nhật ký Hệ thống (Console Log Live)",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                        
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            currentLog.forEach { log ->
                                Text(
                                    text = log,
                                    color = Color.Green,
                                    fontSize = 11.sp,
                                    fontFamily = FontFamily.Monospace,
                                    lineHeight = 15.sp
                                )
                            }
                        }
                    }
                }
            }
        }

        // Raw Output JSON response rendering block
        if (jsonResponseContent.isNotBlank()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                    shape = RoundedCornerShape(12.dp),
                    border = BoxBorderDefaults()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Code, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Phản hồi gốc từ API (Raw JSON Response)", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = jsonResponseContent,
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            lineHeight = 15.sp,
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(8.dp))
                                .padding(10.dp)
                        )
                    }
                }
            }
        }

        // Live Render Preview Screen
        if (simulatedUIPreviewText.isNotBlank()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)),
                    shape = RoundedCornerShape(12.dp),
                    border = BoxBorderDefaults()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.FeaturedVideo, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Xem trước giao diện (Rendered App Screen)", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Smartphone, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(24.dp))
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = simulatedUIPreviewText,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DiagramNode(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isActive: Boolean,
    activeColor: Color
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = if (isActive) 2.dp else 1.dp,
                color = if (isActive) activeColor else MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                shape = RoundedCornerShape(12.dp)
            ),
        colors = CardDefaults.cardColors(
            containerColor = if (isActive) activeColor.copy(alpha = 0.1f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.1f)
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(if (isActive) activeColor else MaterialTheme.colorScheme.surfaceVariant, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = if (isActive) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(16.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = title,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isActive) activeColor else MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = subtitle,
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                )
            }
        }
    }
}

@Composable
fun ArrowIndicator(isActive: Boolean) {
    Row(
        horizontalArrangement = Arrangement.Center,
        modifier = Modifier.fillMaxWidth()
    ) {
        Icon(
            imageVector = Icons.Default.ArrowDownward,
            contentDescription = null,
            tint = if (isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
            modifier = Modifier.size(16.dp)
        )
    }
}

@Composable
fun BoxBorderDefaults() = androidx.compose.foundation.BorderStroke(
    1.dp,
    MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
)
