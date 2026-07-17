package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
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
import com.example.data.local.Post
import com.example.data.local.User
import com.example.ui.viewmodel.SocialViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    viewModel: SocialViewModel,
    onNavigateToProfile: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val posts by viewModel.allPosts.collectAsStateWithLifecycle()
    val users by viewModel.otherUsers.collectAsStateWithLifecycle()
    val currentUser by viewModel.currentUser.collectAsStateWithLifecycle()
    val context = LocalContext.current

    var activeTab by remember { mutableStateOf(0) } // 0: Bài viết, 1: Người dùng
    var showCommentDialogForPost by remember { mutableStateOf<Post?>(null) }
    var activePostMenu by remember { mutableStateOf<Post?>(null) }

    // Search Filtering logic
    val filteredPosts = remember(posts, searchQuery) {
        if (searchQuery.isBlank()) {
            emptyList()
        } else {
            posts.filter {
                it.content.contains(searchQuery, ignoreCase = true) ||
                it.displayName.contains(searchQuery, ignoreCase = true) ||
                it.username.contains(searchQuery, ignoreCase = true)
            }
        }
    }

    val filteredUsers = remember(users, searchQuery) {
        if (searchQuery.isBlank()) {
            // If blank, show suggested users to follow! This is an amazing default state!
            users.filter { !it.isFollowing }
        } else {
            users.filter {
                it.displayName.contains(searchQuery, ignoreCase = true) ||
                it.username.contains(searchQuery, ignoreCase = true) ||
                it.bio.contains(searchQuery, ignoreCase = true)
            }
        }
    }

    Scaffold(
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                // Search Bar TextField
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { viewModel.searchQuery.value = it },
                    placeholder = { Text("Tìm kiếm bài viết, tài khoản...", fontSize = 14.sp) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("search_text_input"),
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Tìm kiếm",
                            tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
                        )
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { viewModel.searchQuery.value = "" }) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Xóa tìm kiếm",
                                    tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
                                )
                            }
                        }
                    },
                    shape = RoundedCornerShape(24.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline
                    ),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Tabs for Results
                TabRow(
                    selectedTabIndex = activeTab,
                    containerColor = Color.Transparent,
                    contentColor = MaterialTheme.colorScheme.primary,
                    indicator = { tabPositions ->
                        TabRowDefaults.SecondaryIndicator(
                            Modifier.tabIndicatorOffset(tabPositions[activeTab]),
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                ) {
                    Tab(
                        selected = activeTab == 0,
                        onClick = { activeTab = 0 },
                        text = {
                            Text(
                                text = "Bài viết (${if (searchQuery.isBlank()) 0 else filteredPosts.size})",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }
                    )
                    Tab(
                        selected = activeTab == 1,
                        onClick = { activeTab = 1 },
                        text = {
                            Text(
                                text = if (searchQuery.isBlank()) "Gợi ý theo dõi" else "Người dùng (${filteredUsers.size})",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }
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
            if (activeTab == 0) {
                // Posts Tab
                if (searchQuery.isBlank()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
                                modifier = Modifier.size(64.dp)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "Nhập từ khóa để tìm kiếm bài viết",
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                            )
                        }
                    }
                } else if (filteredPosts.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Không tìm thấy bài viết nào phù hợp",
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(top = 8.dp, bottom = 16.dp)
                    ) {
                        items(filteredPosts, key = { it.id }) { post ->
                            val isInteractionAllowed = post.privacy == "public" || 
                                                       post.username == currentUser?.username || 
                                                       users.any { it.username == post.username && it.isFollowing }

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
            } else {
                // Users Tab
                if (filteredUsers.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Không tìm thấy người dùng nào phù hợp",
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(bottom = 16.dp)
                    ) {
                        if (searchQuery.isBlank()) {
                            item {
                                Text(
                                    text = "Đề xuất cho bạn",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
                                )
                            }
                        }

                        items(filteredUsers, key = { it.username }) { user ->
                            UserSearchRow(
                                user = user,
                                onUserClick = { onNavigateToProfile(user.username) },
                                onFollowToggle = { viewModel.toggleFollowUser(user.username) }
                            )
                            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                        }
                    }
                }
            }
        }

        // Comment Sheet Popup inside Search Screen
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
fun UserSearchRow(
    user: User,
    onUserClick: () -> Unit,
    onFollowToggle: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onUserClick() }
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AsyncImage(
            model = user.avatarUrl,
            contentDescription = "Avatar",
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .border(1.dp, MaterialTheme.colorScheme.outline, CircleShape),
            contentScale = ContentScale.Crop
        )

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = user.displayName,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (user.username == "@gemini_bot") {
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Verified Bot",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(15.dp)
                    )
                }
            }
            Text(
                text = user.username,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = user.bio,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        // Follow/Unfollow Button
        val buttonColor = if (user.isFollowing) {
            ButtonDefaults.outlinedButtonColors()
        } else {
            ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
        }

        val buttonBorder = if (user.isFollowing) {
            ButtonDefaults.outlinedButtonBorder
        } else {
            null
        }

        Button(
            onClick = onFollowToggle,
            colors = buttonColor,
            border = buttonBorder,
            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
            modifier = Modifier
                .height(34.dp)
                .testTag("follow_btn_${user.username}"),
            shape = RoundedCornerShape(18.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = if (user.isFollowing) Icons.Default.PersonRemove else Icons.Default.PersonAdd,
                    contentDescription = null,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = if (user.isFollowing) "Đang theo dõi" else "Theo dõi",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
