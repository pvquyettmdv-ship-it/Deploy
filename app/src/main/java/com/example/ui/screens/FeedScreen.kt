package com.example.ui.screens

import android.content.Context
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import com.example.data.repository.SocialRepository
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
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.data.local.Comment
import com.example.data.local.Post
import com.example.data.local.User
import com.example.ui.theme.HeartColor
import com.example.ui.theme.RepostColor
import com.example.ui.viewmodel.SocialViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FeedScreen(
    viewModel: SocialViewModel,
    onNavigateToProfile: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val posts by viewModel.allPosts.collectAsStateWithLifecycle()
    val otherUsers by viewModel.otherUsers.collectAsStateWithLifecycle()
    val currentUser by viewModel.currentUser.collectAsStateWithLifecycle()
    val context = LocalContext.current

    var selectedTab by remember { mutableStateOf(0) } // 0: Dành cho bạn, 1: Đang theo dõi
    var selectedTagFilter by remember { mutableStateOf<String?>(null) }
    var showCommentDialogForPost by remember { mutableStateOf<Post?>(null) }
    var activePostMenu by remember { mutableStateOf<Post?>(null) }

    // Filter posts for "Đang theo dõi" and by hashtag
    val filteredPosts = remember(posts, otherUsers, selectedTab, selectedTagFilter) {
        val baseList = if (selectedTab == 1) {
            val followedUsers = otherUsers.filter { it.isFollowing }.map { it.username }.toSet()
            posts.filter { followedUsers.contains(it.username) }
        } else {
            posts
        }

        if (selectedTagFilter != null) {
            baseList.filter { it.content.contains(selectedTagFilter!!, ignoreCase = true) }
        } else {
            baseList
        }
    }

    Scaffold(
        topBar = {
            Column(modifier = Modifier.fillMaxWidth()) {
                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.primary,
                    indicator = { tabPositions ->
                        TabRowDefaults.SecondaryIndicator(
                            Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                ) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        text = { Text("Dành cho bạn", fontWeight = FontWeight.Bold, fontSize = 15.sp) },
                        modifier = Modifier.testTag("tab_for_you")
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        text = { Text("Đang theo dõi", fontWeight = FontWeight.Bold, fontSize = 15.sp) },
                        modifier = Modifier.testTag("tab_following")
                    )
                }
            }
        },
        modifier = modifier
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(top = 8.dp, bottom = 16.dp)
            ) {
                // AI Recommendation panel at the top of "For You"
                if (selectedTab == 0) {
                    item {
                        AiRecommendationsSection(
                            viewModel = viewModel,
                            currentUser = currentUser,
                            onTagClick = { tag ->
                                selectedTagFilter = tag
                            }
                        )
                    }
                }

                // Active tag filter indicator
                if (selectedTagFilter != null) {
                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 6.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            AssistChip(
                                onClick = { selectedTagFilter = null },
                                label = { Text("Đang lọc: $selectedTagFilter", fontSize = 12.sp, fontWeight = FontWeight.Bold) },
                                trailingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Xóa lọc",
                                        modifier = Modifier
                                            .size(16.dp)
                                            .clickable { selectedTagFilter = null }
                                    )
                                },
                                colors = AssistChipDefaults.assistChipColors(
                                    labelColor = MaterialTheme.colorScheme.primary,
                                    leadingIconContentColor = MaterialTheme.colorScheme.primary
                                )
                            )
                            TextButton(onClick = { selectedTagFilter = null }) {
                                Text("Hiển thị tất cả", fontSize = 12.sp)
                            }
                        }
                    }
                }

                if (filteredPosts.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 64.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.padding(24.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.DynamicFeed,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f),
                                    modifier = Modifier.size(72.dp)
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    text = if (selectedTab == 1) "Chưa có bài đăng từ người đang theo dõi" else "Không tìm thấy bài viết nào",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 17.sp,
                                    color = MaterialTheme.colorScheme.onBackground
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = if (selectedTab == 1) "Hãy theo dõi thêm người dùng ở mục Tìm kiếm!" else "Thử xóa bộ lọc hoặc đăng tải bài viết mới nhé!",
                                    fontSize = 13.sp,
                                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                                )
                            }
                        }
                    }
                } else {
                    items(filteredPosts, key = { it.id }) { post ->
                        val isInteractionAllowed = post.privacy == "public" || 
                                                   post.username == currentUser?.username || 
                                                   otherUsers.any { it.username == post.username && it.isFollowing }

                        PostCard(
                            post = post,
                            isInteractionAllowed = isInteractionAllowed,
                            onLikeClick = { viewModel.toggleLike(post.id) },
                            onCommentClick = { showCommentDialogForPost = post },
                            onRepostClick = { viewModel.repostPost(post.id) },
                            onShareClick = {
                                copyToClipboard(context, "https://netvibe.social/post/${post.id}")
                                Toast.makeText(context, "Đã sao chép liên kết chia sẻ!", Toast.LENGTH_SHORT).show()
                            },
                            onMoreClick = { activePostMenu = post },
                            onUserClick = { onNavigateToProfile(post.username) }
                        )
                    }
                }
            }
        }

        // Reply / Comment Popup Dialog
        showCommentDialogForPost?.let { post ->
            CommentDialog(
                post = post,
                viewModel = viewModel,
                onDismiss = { showCommentDialogForPost = null }
            )
        }

        // More Post Options Bottom Sheet / Dialog (Report, Block, Hide)
        activePostMenu?.let { post ->
            PostOptionsMenu(
                post = post,
                onDismiss = { activePostMenu = null },
                onCopyText = {
                    copyToClipboard(context, post.content)
                    Toast.makeText(context, "Đã sao chép nội dung bài viết!", Toast.LENGTH_SHORT).show()
                    activePostMenu = null
                },
                onHide = {
                    viewModel.hidePost(post.id)
                    Toast.makeText(context, "Đã ẩn bài viết khỏi bảng tin của bạn.", Toast.LENGTH_SHORT).show()
                    activePostMenu = null
                },
                onBlockUser = {
                    viewModel.blockUser(post.username)
                    Toast.makeText(context, "Đã chặn người dùng ${post.username}.", Toast.LENGTH_LONG).show()
                    activePostMenu = null
                },
                onReport = {
                    viewModel.reportPost(post.id)
                    Toast.makeText(context, "Đã gửi báo cáo vi phạm nội dung bài viết.", Toast.LENGTH_LONG).show()
                    activePostMenu = null
                }
            )
        }
    }
}

@Composable
fun AiRecommendationsSection(
    viewModel: SocialViewModel,
    currentUser: User?,
    onTagClick: (String) -> Unit
) {
    var aiRecommendations by remember { mutableStateOf<List<String>?>(null) }
    var isGeneratingRecs by remember { mutableStateOf(false) }
    val context = LocalContext.current

    LaunchedEffect(currentUser) {
        if (currentUser != null && aiRecommendations == null) {
            isGeneratingRecs = true
            val prompt = """
                Hãy phân tích hồ sơ người dùng ${currentUser.displayName} (Bio: ${currentUser.bio}) và đề xuất 4 chủ đề thảo luận đang là xu hướng hot nhất phù hợp với sở thích của họ. 
                Hãy viết ngắn gọn dưới dạng thẻ ngắn (ví dụ: #JetpackCompose, #DeepThinking, #AmNhacChill, #Web3Dev). 
                Chỉ trả về 4 thẻ, ngăn cách bằng dấu phẩy, không thêm bất kỳ văn bản giải thích nào khác.
            """.trimIndent()
            
            val systemInstruction = "Bạn là trợ lý gợi ý chủ đề xu hướng cá nhân hóa."
            
            kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                when (val result = com.example.data.api.GeminiClient.generateWithThinking(prompt, systemInstruction)) {
                    is com.example.data.api.GeminiResult.Success -> {
                        val tags = result.response.split(",")
                            .map { it.trim() }
                            .filter { it.isNotBlank() }
                        aiRecommendations = tags
                    }
                    else -> {
                        aiRecommendations = listOf("#JetpackCompose", "#SleekAesthetic", "#GeminiPro3.1", "#NetVibeNetwork")
                    }
                }
                isGeneratingRecs = false
            }
        }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 6.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.22f)),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.25f))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Psychology,
                        contentDescription = "AI Recommendation",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Đề xuất thông minh bằng Gemini AI ✨",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                
                if (isGeneratingRecs) {
                    CircularProgressIndicator(modifier = Modifier.size(14.dp), strokeWidth = 2.dp)
                } else {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = "AI active",
                        tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f),
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Cá nhân hóa theo sở thích của bạn. Hãy bấm một thẻ để khám phá bài viết:",
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                lineHeight = 15.sp
            )
            Spacer(modifier = Modifier.height(10.dp))
            
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                val tagsList = aiRecommendations ?: listOf("#JetpackCompose", "#SleekAesthetic", "#GeminiPro3.1", "#NetVibeNetwork")
                items(tagsList) { tag ->
                    SuggestionChip(
                        onClick = { onTagClick(tag) },
                        label = { Text(tag, fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                        colors = SuggestionChipDefaults.suggestionChipColors(
                            containerColor = MaterialTheme.colorScheme.surface,
                            labelColor = MaterialTheme.colorScheme.primary
                        ),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.15f))
                    )
                }
            }
        }
    }
}

@Composable
fun PostCard(
    post: Post,
    isInteractionAllowed: Boolean,
    onLikeClick: () -> Unit,
    onCommentClick: () -> Unit,
    onRepostClick: () -> Unit,
    onShareClick: () -> Unit,
    onMoreClick: () -> Unit,
    onUserClick: () -> Unit
) {
    val context = LocalContext.current
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 6.dp)
            .clickable { 
                if (isInteractionAllowed) {
                    onCommentClick()
                } else {
                    Toast.makeText(context, "Tác giả giới hạn lượt phản hồi chỉ dành cho người theo dõi 🔒", Toast.LENGTH_LONG).show()
                }
            }
            .testTag("post_card_${post.id}"),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(all = 16.dp)
                .fillMaxWidth()
        ) {
            // Avatar
            AsyncImage(
                model = post.avatarUrl,
                contentDescription = "Avatar của ${post.displayName}",
                modifier = Modifier
                    .size(46.dp)
                    .clip(CircleShape)
                    .border(1.dp, MaterialTheme.colorScheme.outline, CircleShape)
                    .clickable { onUserClick() }
                    .testTag("avatar_${post.username}"),
                contentScale = ContentScale.Crop
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.fillMaxWidth()) {
                // Header Row (Name, Username, options)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .clickable { onUserClick() }
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = post.displayName,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = MaterialTheme.colorScheme.onBackground,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            if (post.username == "@gemini_bot") {
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(
                                    imageVector = Icons.Filled.Verified,
                                    contentDescription = "Verified AI",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                        Text(
                            text = post.username,
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    Text(
                        text = formatRelativeTime(post.timestamp),
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
                    )

                    IconButton(
                        onClick = onMoreClick,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.MoreHoriz,
                            contentDescription = "Thêm tùy chọn",
                            tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Post Content
                Text(
                    text = post.content,
                    fontSize = 15.sp,
                    lineHeight = 21.sp,
                    color = MaterialTheme.colorScheme.onBackground
                )

                // Optional Media Block: Image, Video, Link
                if (post.mediaUrl != null) {
                    Spacer(modifier = Modifier.height(10.dp))
                    if (post.mediaType == "video") {
                        // Simulated video view
                        var isPlaying by remember { mutableStateOf(false) }
                        var playProgress by remember { mutableFloatStateOf(0.12f) }
                        
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(180.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(16.dp)),
                            colors = CardDefaults.cardColors(containerColor = Color.Black)
                        ) {
                            Box(modifier = Modifier.fillMaxSize()) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(Color.Black.copy(alpha = 0.85f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Videocam,
                                        contentDescription = null,
                                        tint = Color.White.copy(alpha = 0.12f),
                                        modifier = Modifier.size(80.dp)
                                    )
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        modifier = Modifier.padding(16.dp)
                                    ) {
                                        Text(
                                            text = if (isPlaying) "🎥 Video đang phát..." else "🎞️ Bấm phát video tương tác",
                                            color = Color.White.copy(alpha = 0.85f),
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = post.mediaUrl.takeLast(35),
                                            color = Color.White.copy(alpha = 0.45f),
                                            fontSize = 10.sp,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }
                                
                                IconButton(
                                    onClick = {
                                        isPlaying = !isPlaying
                                        if (isPlaying) {
                                            playProgress = 0.45f
                                            Toast.makeText(context, "Đang phát video tương tác trên NetVibe...", Toast.LENGTH_SHORT).show()
                                        } else {
                                            Toast.makeText(context, "Tạm dừng video", Toast.LENGTH_SHORT).show()
                                        }
                                    },
                                    modifier = Modifier
                                        .align(Alignment.Center)
                                        .background(Color.Black.copy(alpha = 0.6f), CircleShape)
                                        .size(46.dp)
                                ) {
                                    Icon(
                                        imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                        contentDescription = "Play/Pause",
                                        tint = Color.White,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }

                                Row(
                                    modifier = Modifier
                                        .align(Alignment.BottomCenter)
                                        .fillMaxWidth()
                                        .background(Color.Black.copy(alpha = 0.5f))
                                        .padding(horizontal = 12.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(
                                        imageVector = if (isPlaying) Icons.Default.VolumeUp else Icons.Default.VolumeMute,
                                        contentDescription = "Mute/Unmute",
                                        tint = Color.White,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Slider(
                                        value = playProgress,
                                        onValueChange = { playProgress = it },
                                        modifier = Modifier.weight(1f),
                                        colors = SliderDefaults.colors(
                                            thumbColor = MaterialTheme.colorScheme.primary,
                                            activeTrackColor = MaterialTheme.colorScheme.primary,
                                            inactiveTrackColor = Color.White.copy(alpha = 0.3f)
                                        )
                                    )
                                    Text(
                                        text = if (isPlaying) "0:45 / 3:12" else "0:00 / 3:12",
                                        color = Color.White,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    } else if (post.mediaType == "link") {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(16.dp))
                                .clickable {
                                    Toast.makeText(context, "Đang mở liên kết: ${post.mediaUrl}", Toast.LENGTH_SHORT).show()
                                },
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                        ) {
                            Row(
                                modifier = Modifier
                                    .padding(12.dp)
                                    .fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(46.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Link,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "Xem liên kết đính kèm",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = post.mediaUrl,
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.primary,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                                Icon(
                                    imageVector = Icons.Default.ArrowOutward,
                                    contentDescription = "Mở link",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    } else {
                        // Image
                        AsyncImage(
                            model = post.mediaUrl,
                            contentDescription = "Phương tiện bài viết",
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(max = 280.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(16.dp)),
                            contentScale = ContentScale.Crop
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Action Bar (Like, Comment, Repost, Share)
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth(0.95f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Like button
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable { onLikeClick() }
                    ) {
                        Icon(
                            imageVector = if (post.isLiked) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                            contentDescription = "Thích",
                            tint = if (post.isLiked) HeartColor else MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = formatNumber(post.likesCount),
                            fontSize = 13.sp,
                            color = if (post.isLiked) HeartColor else MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                        )
                    }

                    // Comment / Reply button
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable { 
                            if (isInteractionAllowed) {
                                onCommentClick() 
                            } else {
                                Toast.makeText(context, "Tác giả giới hạn lượt phản hồi chỉ dành cho người theo dõi 🔒", Toast.LENGTH_LONG).show()
                            }
                        }
                    ) {
                        Icon(
                            imageVector = if (isInteractionAllowed) Icons.Outlined.ChatBubbleOutline else Icons.Default.Lock,
                            contentDescription = "Trả lời",
                            tint = MaterialTheme.colorScheme.onBackground.copy(alpha = if (isInteractionAllowed) 0.6f else 0.4f),
                            modifier = Modifier.size(19.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isInteractionAllowed) formatNumber(post.repliesCount) else "Giới hạn",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = if (isInteractionAllowed) 0.6f else 0.4f)
                        )
                    }

                    // Repost button
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable { onRepostClick() }
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Repeat,
                            contentDescription = "Đăng lại",
                            tint = if (post.isReposted) RepostColor else MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = formatNumber(post.repostsCount),
                            fontSize = 13.sp,
                            color = if (post.isReposted) RepostColor else MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                        )
                    }

                    // Share button
                    IconButton(
                        onClick = onShareClick,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Share,
                            contentDescription = "Chia sẻ",
                            tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                            modifier = Modifier.size(19.dp)
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommentDialog(
    post: Post,
    viewModel: SocialViewModel,
    onDismiss: () -> Unit
) {
    val repository = SocialRepository.getInstance(LocalContext.current)
    val commentsFlow = remember(post.id) { repository.commentDao.getCommentsForPost(post.id) }
    val comments by commentsFlow.collectAsState(initial = emptyList())
    var commentText by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.85f),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Phản hồi (${comments.size})",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Đóng")
                    }
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))

                // Comments List
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentPadding = PaddingValues(16.dp)
                ) {
                    item {
                        // Original post snippet
                        Row(modifier = Modifier.padding(bottom = 16.dp)) {
                            AsyncImage(
                                model = post.avatarUrl,
                                contentDescription = null,
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape),
                                contentScale = ContentScale.Crop
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(post.displayName, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Text(post.content, fontSize = 14.sp, color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f))
                            }
                        }
                        HorizontalDivider(
                            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                            modifier = Modifier.padding(bottom = 16.dp)
                        )
                    }

                    if (comments.isEmpty()) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 48.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "Chưa có phản hồi nào. Hãy là người đầu tiên thảo luận!",
                                    fontSize = 13.sp,
                                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
                                )
                            }
                        }
                    } else {
                        items(comments) { comment ->
                            Row(modifier = Modifier.padding(vertical = 8.dp)) {
                                AsyncImage(
                                    model = comment.avatarUrl,
                                    contentDescription = null,
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape),
                                    contentScale = ContentScale.Crop
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(comment.displayName, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = formatRelativeTime(comment.timestamp),
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = comment.content,
                                        fontSize = 13.sp,
                                        color = MaterialTheme.colorScheme.onBackground
                                    )
                                }
                            }
                        }
                    }
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))

                // Comment input
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextField(
                        value = commentText,
                        onValueChange = { commentText = it },
                        placeholder = { Text("Viết phản hồi của bạn...", fontSize = 14.sp) },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("comment_input_field"),
                        maxLines = 3,
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            disabledContainerColor = Color.Transparent,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent
                        ),
                        shape = RoundedCornerShape(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            viewModel.addComment(post.id, commentText)
                            commentText = ""
                        },
                        enabled = commentText.isNotBlank(),
                        modifier = Modifier.testTag("comment_submit_button"),
                        shape = RoundedCornerShape(24.dp)
                    ) {
                        Text("Gửi", fontSize = 14.sp)
                    }
                }
            }
        }
    }
}

@Composable
fun PostOptionsMenu(
    post: Post,
    onDismiss: () -> Unit,
    onCopyText: () -> Unit,
    onHide: () -> Unit,
    onBlockUser: () -> Unit,
    onReport: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .wrapContentHeight(),
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp
        ) {
            Column(modifier = Modifier.padding(vertical = 12.dp)) {
                Text(
                    text = "Tùy chọn bài viết",
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))

                DropdownMenuItem(
                    text = { Text("Sao chép nội dung") },
                    onClick = onCopyText,
                    leadingIcon = { Icon(Icons.Default.CopyAll, contentDescription = null) }
                )
                DropdownMenuItem(
                    text = { Text("Ẩn bài viết này") },
                    onClick = onHide,
                    leadingIcon = { Icon(Icons.Default.VisibilityOff, contentDescription = null) }
                )
                DropdownMenuItem(
                    text = { Text("Chặn người dùng ${post.username}") },
                    onClick = onBlockUser,
                    leadingIcon = { Icon(Icons.Default.Block, contentDescription = null) }
                )
                DropdownMenuItem(
                    text = { Text("Báo cáo vi phạm") },
                    onClick = onReport,
                    leadingIcon = { Icon(Icons.Default.Report, contentDescription = null) },
                    colors = MenuDefaults.itemColors(
                        textColor = MaterialTheme.colorScheme.error,
                        leadingIconColor = MaterialTheme.colorScheme.error
                    )
                )

                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = onDismiss,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant,
                        contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                ) {
                    Text("Đóng")
                }
            }
        }
    }
}

// Helpers
fun formatRelativeTime(timestamp: Long): String {
    val diff = System.currentTimeMillis() - timestamp
    val minute = 60000L
    val hour = 3600000L
    val day = 86400000L

    return when {
        diff < minute -> "Vừa xong"
        diff < hour -> "${diff / minute} phút trước"
        diff < day -> "${diff / hour} giờ trước"
        diff < day * 7 -> "${diff / day} ngày trước"
        else -> {
            val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            sdf.format(Date(timestamp))
        }
    }
}

fun formatNumber(number: Int): String {
    return when {
        number >= 1000000 -> String.format(Locale.US, "%.1fM", number / 1000000.0)
        number >= 1000 -> String.format(Locale.US, "%.1fK", number / 1000.0)
        else -> number.toString()
    }
}

fun copyToClipboard(context: Context, text: String) {
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
    val clip = android.content.ClipData.newPlainText("NetVibe Copy", text)
    clipboard.setPrimaryClip(clip)
}
