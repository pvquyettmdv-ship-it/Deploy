package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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
import com.example.ui.viewmodel.SocialViewModel
import kotlinx.coroutines.flow.collectLatest

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreatePostScreen(
    viewModel: SocialViewModel,
    onPostCreated: () -> Unit,
    modifier: Modifier = Modifier
) {
    val currentUser by viewModel.currentUser.collectAsStateWithLifecycle()
    val isOptimizing by viewModel.isOptimizingPost.collectAsStateWithLifecycle()
    val context = LocalContext.current

    var postContent by remember { mutableStateOf("") }
    var selectedMediaUrl by remember { mutableStateOf<String?>(null) }
    var selectedMediaType by remember { mutableStateOf("image") } // "image", "video", "link", "text"
    var selectedPrivacy by remember { mutableStateOf("public") } // "public", "followers"
    
    var showUrlDialog by remember { mutableStateOf(false) }
    var rawUrlInput by remember { mutableStateOf("") }

    // Media preset templates
    val imageTemplates = listOf(
        Pair("Phong cảnh", "https://images.unsplash.com/photo-1501854140801-50d01698950b?auto=format&fit=crop&w=800&q=80"),
        Pair("Công nghệ", "https://images.unsplash.com/photo-1518770660439-4636190af475?auto=format&fit=crop&w=800&q=80"),
        Pair("Vũ trụ", "https://images.unsplash.com/photo-1451187580459-43490279c0fa?auto=format&fit=crop&w=800&q=80"),
        Pair("Mèo cưng", "https://images.unsplash.com/photo-1514888286974-6c03e2ca1dba?auto=format&fit=crop&w=800&q=80")
    )

    val videoTemplates = listOf(
        Pair("Thiên nhiên 4K", "https://assets.mixkit.co/videos/preview/mixkit-forest-stream-in-the-sunlight-529-large.mp4"),
        Pair("Bàn phím cơ", "https://assets.mixkit.co/videos/preview/mixkit-hands-of-a-programmer-typing-on-a-keyboard-40546-large.mp4"),
        Pair("Dữ liệu mạng", "https://assets.mixkit.co/videos/preview/mixkit-digital-animation-of-screens-and-data-analysis-31911-large.mp4")
    )

    val linkTemplates = listOf(
        Pair("Cộng đồng Viblo", "https://viblo.asia/tags/kotlin"),
        Pair("Jetpack Compose Docs", "https://developer.android.com/compose"),
        Pair("Google Gemini AI", "https://deepmind.google/technologies/gemini")
    )

    // Listen to optimized result from Gemini
    LaunchedEffect(viewModel) {
        viewModel.optimizedPostResult.collectLatest { result ->
            postContent = result
            Toast.makeText(context, "Mô hình Gemini 3.1 Pro đã tối ưu hóa bài đăng thành công!", Toast.LENGTH_SHORT).show()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Tạo bài đăng mới", fontSize = 18.sp, fontWeight = FontWeight.Bold) },
                actions = {
                    Button(
                        onClick = {
                            viewModel.createPost(
                                content = postContent,
                                mediaUrl = if (selectedMediaType == "text") null else selectedMediaUrl,
                                mediaType = if (selectedMediaType == "text") null else selectedMediaType,
                                privacy = selectedPrivacy
                            )
                            postContent = ""
                            selectedMediaUrl = null
                            Toast.makeText(context, "Đăng bài viết thành công!", Toast.LENGTH_SHORT).show()
                            onPostCreated()
                        },
                        enabled = postContent.isNotBlank() && !isOptimizing,
                        shape = RoundedCornerShape(20.dp),
                        modifier = Modifier
                            .padding(end = 8.dp)
                            .testTag("submit_post_button")
                    ) {
                        Text("Đăng tin", fontWeight = FontWeight.Bold, fontSize = 14.sp)
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
                .padding(16.dp)
        ) {
            // User identity & Privacy Control
            currentUser?.let { user ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        AsyncImage(
                            model = user.avatarUrl,
                            contentDescription = null,
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .border(1.dp, MaterialTheme.colorScheme.outline, CircleShape),
                            contentScale = ContentScale.Crop
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = user.displayName,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            Text(
                                text = user.username,
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                            )
                        }
                    }

                    // Privacy selection dropdown-like button
                    var showPrivacyMenu by remember { mutableStateOf(false) }
                    Box {
                        AssistChip(
                            onClick = { showPrivacyMenu = true },
                            label = { 
                                Text(
                                    if (selectedPrivacy == "public") "🌍 Công khai" else "👥 Người theo dõi",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                ) 
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector = if (selectedPrivacy == "public") Icons.Default.Public else Icons.Default.People,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        )
                        DropdownMenu(
                            expanded = showPrivacyMenu,
                            onDismissRequest = { showPrivacyMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("🌍 Công khai (Mọi người)") },
                                onClick = {
                                    selectedPrivacy = "public"
                                    showPrivacyMenu = false
                                    Toast.makeText(context, "Quyền riêng tư: Mọi người đều có thể tương tác", Toast.LENGTH_SHORT).show()
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("👥 Chỉ người theo dõi") },
                                onClick = {
                                    selectedPrivacy = "followers"
                                    showPrivacyMenu = false
                                    Toast.makeText(context, "Quyền riêng tư: Giới hạn tương tác chỉ dành cho Người theo dõi", Toast.LENGTH_SHORT).show()
                                }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Multi-media tab selection
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                    .padding(4.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                val mediaTabs = listOf(
                    Triple("text", "Văn bản", Icons.Default.Notes),
                    Triple("image", "Hình ảnh", Icons.Default.Image),
                    Triple("video", "Video", Icons.Default.Videocam),
                    Triple("link", "Liên kết", Icons.Default.Link)
                )
                mediaTabs.forEach { (type, label, icon) ->
                    val isSelected = selectedMediaType == type
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent)
                            .clickable {
                                selectedMediaType = type
                                if (type == "text") selectedMediaUrl = null
                            }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = icon,
                                contentDescription = label,
                                tint = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = label,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Text Draft Input
            OutlinedTextField(
                value = postContent,
                onValueChange = { if (it.length <= 500) postContent = it },
                placeholder = { 
                    val hint = when(selectedMediaType) {
                        "image" -> "Bạn đang nghĩ gì thế? Thêm nội dung và đính kèm hình ảnh..."
                        "video" -> "Nhập nội dung cho bài đăng video của bạn..."
                        "link" -> "Chia sẻ ý kiến về liên kết hữu ích này..."
                        else -> "Hãy chia sẻ suy nghĩ, ý kiến hoặc cập nhật nhanh của bạn với mọi người..."
                    }
                    Text(hint, fontSize = 14.sp) 
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .testTag("post_content_draft"),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color.Transparent,
                    unfocusedBorderColor = Color.Transparent,
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent
                ),
                maxLines = 15
            )

            // Remaining Chars & AI Optimizing Loader
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Character counter
                Text(
                    text = "${postContent.length}/500 ký tự",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
                )

                // Deep Thinking Optimizer Loader
                if (isOptimizing) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Gemini AI đang tối ưu nội dung...",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Preview Attachment
            AnimatedVisibility(visible = selectedMediaType != "text" && selectedMediaUrl != null) {
                selectedMediaUrl?.let { url ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(140.dp)
                            .padding(bottom = 12.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
                    ) {
                        Box(modifier = Modifier.fillMaxSize()) {
                            if (selectedMediaType == "video") {
                                // Simulated Video Player View
                                Row(
                                    modifier = Modifier.fillMaxSize()
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxHeight()
                                            .width(140.dp)
                                            .background(Color.Black),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.PlayCircleFilled,
                                            contentDescription = "Phát Video",
                                            tint = Color.White,
                                            modifier = Modifier.size(40.dp)
                                        )
                                    }
                                    Column(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .padding(12.dp),
                                        verticalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Column {
                                            Text("🎥 Video Đã Chọn", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(url, fontSize = 11.sp, color = MaterialTheme.colorScheme.primary, maxLines = 2, overflow = TextOverflow.Ellipsis)
                                        }
                                        Text("Phát thử mượt mà trên NetVibe", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f))
                                    }
                                }
                            } else if (selectedMediaType == "link") {
                                // Simulated Link preview card
                                Row(
                                    modifier = Modifier.fillMaxSize()
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxHeight()
                                            .width(120.dp)
                                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Link,
                                            contentDescription = "Liên kết",
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(36.dp)
                                        )
                                    }
                                    Column(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .padding(12.dp),
                                        verticalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Column {
                                            Text("🔗 Liên kết đính kèm", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(url, fontSize = 11.sp, color = MaterialTheme.colorScheme.primary, maxLines = 2, overflow = TextOverflow.Ellipsis)
                                        }
                                        Text("Sẽ tạo thẻ xem trước liên kết (Rich Link Preview)", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f))
                                    }
                                }
                            } else {
                                // Image
                                AsyncImage(
                                    model = url,
                                    contentDescription = "Attachment preview",
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            }

                            // Close button
                            IconButton(
                                onClick = { selectedMediaUrl = null },
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .padding(8.dp)
                                    .background(Color.Black.copy(alpha = 0.6f), CircleShape)
                                    .size(28.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Xóa tệp đính kèm",
                                    tint = Color.White,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Toolbar: AI Assist & Image Templates
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Công cụ bổ trợ bài đăng",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        // AI Optimizer button
                        Button(
                            onClick = { viewModel.optimizeDraftPost(postContent) },
                            enabled = postContent.isNotBlank() && !isOptimizing,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer,
                                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                            ),
                            shape = RoundedCornerShape(12.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                            modifier = Modifier.testTag("ai_optimize_btn")
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Psychology,
                                    contentDescription = "Tối ưu AI",
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Tối ưu bằng Gemini AI 🧠", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    if (selectedMediaType != "text") {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = when (selectedMediaType) {
                                    "image" -> "Gợi ý ảnh mẫu chất lượng cao:"
                                    "video" -> "Mẫu video clip tương tác:"
                                    else -> "Gợi ý liên kết tin tức nổi bật:"
                                },
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                            )

                            // Attach custom URL button
                            TextButton(
                                onClick = { showUrlDialog = true },
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(2.dp))
                                    Text(
                                        text = when (selectedMediaType) {
                                            "image" -> "Tự nhập URL Ảnh"
                                            "video" -> "Tự nhập URL Video"
                                            else -> "Tự nhập liên kết"
                                        },
                                        fontSize = 11.sp
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        // Render respective templates
                        val currentTemplates = when (selectedMediaType) {
                            "image" -> imageTemplates
                            "video" -> videoTemplates
                            else -> linkTemplates
                        }

                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            items(currentTemplates) { template ->
                                SuggestionChip(
                                    onClick = { selectedMediaUrl = template.second },
                                    label = { Text(template.first, fontSize = 11.sp) },
                                    shape = RoundedCornerShape(12.dp)
                                )
                            }
                        }
                    } else {
                        // Text only layout info
                        Text(
                            text = "💡 Mẹo: Viết cập nhật ngắn, sau đó dùng 'Tối ưu bằng Gemini AI' để bài viết của bạn sinh động, hấp dẫn hơn nhé!",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                            lineHeight = 16.sp
                        )
                    }
                }
            }
        }

        // URL input Dialog
        if (showUrlDialog) {
            AlertDialog(
                onDismissRequest = { showUrlDialog = false },
                title = { 
                    Text(
                        text = when (selectedMediaType) {
                            "image" -> "Nhập liên kết hình ảnh"
                            "video" -> "Nhập liên kết video (.mp4)"
                            else -> "Nhập liên kết trang web"
                        }, 
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    ) 
                },
                text = {
                    OutlinedTextField(
                        value = rawUrlInput,
                        onValueChange = { rawUrlInput = it },
                        placeholder = { Text("https://...") },
                        modifier = Modifier.fillMaxWidth()
                    )
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (rawUrlInput.isNotBlank()) {
                                selectedMediaUrl = rawUrlInput
                                rawUrlInput = ""
                            }
                            showUrlDialog = false
                        }
                    ) {
                        Text("Xác nhận")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showUrlDialog = false }) {
                        Text("Hủy")
                    }
                }
            )
        }
    }
}
