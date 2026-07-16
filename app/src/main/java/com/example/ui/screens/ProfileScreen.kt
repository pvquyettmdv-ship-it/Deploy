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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.data.local.Post
import com.example.data.local.User
import com.example.ui.viewmodel.SocialViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    viewModel: SocialViewModel,
    targetUsername: String?, // Null indicates current user
    onNavigateToProfile: (String) -> Unit,
    onNavigateToChat: (String) -> Unit,
    onBack: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val currentUser by viewModel.currentUser.collectAsStateWithLifecycle()
    val otherUsers by viewModel.otherUsers.collectAsStateWithLifecycle()
    val allPosts by viewModel.allPosts.collectAsStateWithLifecycle()

    var showEditDialog by remember { mutableStateOf(false) }
    var showCommentDialogForPost by remember { mutableStateOf<Post?>(null) }
    var showFollowListDialog by remember { mutableStateOf<String?>(null) } // "followers" or "following"

    // Resolve which user profile to show
    val userProfile = remember(targetUsername, currentUser, otherUsers) {
        if (targetUsername == null || targetUsername == currentUser?.username) {
            currentUser
        } else {
            otherUsers.find { it.username == targetUsername }
        }
    }

    // Filter posts for this user
    val userPosts = remember(allPosts, userProfile) {
        val username = userProfile?.username ?: ""
        allPosts.filter { it.username == username }
    }

    if (userProfile == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    val isMe = userProfile.username == currentUser?.username

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(userProfile.displayName, fontWeight = FontWeight.Bold, fontSize = 18.sp) },
                navigationIcon = {
                    if (onBack != null) {
                        IconButton(onClick = onBack) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Quay lại")
                        }
                    }
                }
            )
        },
        modifier = modifier
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Profile Header Card (Tiêu đề hồ sơ)
            item {
                ProfileHeaderSection(
                    user = userProfile,
                    isMe = isMe,
                    postsCount = userPosts.size,
                    onEditProfileClick = { showEditDialog = true },
                    onFollowToggle = { viewModel.toggleFollowUser(userProfile.username) },
                    onShowFollowers = { showFollowListDialog = "followers" },
                    onShowFollowing = { showFollowListDialog = "following" },
                    onNavigateToChat = onNavigateToChat
                )
            }

            // Divider and Title
            item {
                Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)) {
                    Text(
                        text = "Bài đăng của ${userProfile.displayName}",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    HorizontalDivider(color = MaterialTheme.colorScheme.primary, thickness = 2.dp, modifier = Modifier.width(60.dp))
                }
            }

            // Feed of User's Posts
            if (userPosts.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 48.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Chưa có bài đăng nào từ người dùng này.",
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
                        )
                    }
                }
            } else {
                items(userPosts, key = { it.id }) { post ->
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
                        onMoreClick = {},
                        onUserClick = {} // Already on their profile
                    )
                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))
                }
            }
        }

        // Edit Profile Dialog
        if (showEditDialog) {
            EditProfileDialog(
                user = userProfile,
                onDismiss = { showEditDialog = false },
                onSave = { name, bio ->
                    viewModel.updateProfile(name, bio)
                    showEditDialog = false
                    Toast.makeText(context, "Cập nhật hồ sơ cá nhân thành công!", Toast.LENGTH_SHORT).show()
                }
            )
        }

        // Comment Sheet Popup
        showCommentDialogForPost?.let { post ->
            CommentDialog(
                post = post,
                viewModel = viewModel,
                onDismiss = { showCommentDialogForPost = null }
            )
        }

        // Followers / Following dialog
        showFollowListDialog?.let { type ->
            FollowersListDialog(
                type = type,
                currentUser = currentUser,
                otherUsers = otherUsers,
                onDismiss = { showFollowListDialog = null },
                onNavigateToProfile = { username ->
                    showFollowListDialog = null
                    onNavigateToProfile(username)
                },
                viewModel = viewModel
            )
        }
    }
}

@Composable
fun ProfileHeaderSection(
    user: User,
    isMe: Boolean,
    postsCount: Int,
    onEditProfileClick: () -> Unit,
    onFollowToggle: () -> Unit,
    onShowFollowers: () -> Unit,
    onShowFollowing: () -> Unit,
    onNavigateToChat: (String) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column {
            // Visual decorative banner gradient background
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(90.dp)
                    .background(
                        Brush.linearGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.primary,
                                MaterialTheme.colorScheme.tertiary
                            )
                        )
                    )
            )

            // Profile info area (Avatar overlaps banner)
            Column(
                modifier = Modifier
                    .padding(horizontal = 16.dp, vertical = 12.dp)
                    .fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    // Overlapping Avatar
                    AsyncImage(
                        model = user.avatarUrl,
                        contentDescription = null,
                        modifier = Modifier
                            .size(76.dp)
                            .offset(y = (-38).dp)
                            .clip(CircleShape)
                            .border(4.dp, MaterialTheme.colorScheme.surface, CircleShape),
                        contentScale = ContentScale.Crop
                    )

                    // Profile CTA button
                    if (isMe) {
                        Button(
                            onClick = onEditProfileClick,
                            shape = RoundedCornerShape(16.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                            modifier = Modifier
                                .height(32.dp)
                                .testTag("edit_profile_dialog_trigger"),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = MaterialTheme.colorScheme.onPrimary
                            )
                        ) {
                            Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(13.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Chỉnh sửa hồ sơ", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    } else {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            val followBtnText = if (user.isFollowing) "Đang theo dõi" else "Theo dõi"
                            val followBtnColor = if (user.isFollowing) ButtonDefaults.outlinedButtonColors() else ButtonDefaults.buttonColors()
                            Button(
                                onClick = onFollowToggle,
                                colors = followBtnColor,
                                shape = RoundedCornerShape(16.dp),
                                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                                modifier = Modifier
                                    .height(32.dp)
                                    .testTag("follow_user_header_btn")
                            ) {
                                Icon(
                                    imageVector = if (user.isFollowing) Icons.Default.Check else Icons.Default.PersonAdd,
                                    contentDescription = null,
                                    modifier = Modifier.size(13.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(followBtnText, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }

                            // Message Button
                            Button(
                                onClick = { onNavigateToChat(user.username) },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                                ),
                                shape = RoundedCornerShape(16.dp),
                                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                                modifier = Modifier
                                    .height(32.dp)
                                    .testTag("message_user_header_btn")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Forum,
                                    contentDescription = null,
                                    modifier = Modifier.size(13.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Nhắn tin", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                // Profile Name, Username & Bio
                Column(modifier = Modifier.offset(y = (-16).dp)) {
                    Text(
                        text = user.displayName,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = user.username,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = user.bio,
                        fontSize = 14.sp,
                        lineHeight = 18.sp,
                        color = MaterialTheme.colorScheme.onBackground
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Social counters (Followers, Following, Posts)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(20.dp)
                    ) {
                        Column {
                            Text(
                                text = postsCount.toString(),
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            Text(
                                text = "Bài viết",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
                            )
                        }

                        Column(modifier = Modifier.clickable { onShowFollowing() }) {
                            Text(
                                text = formatNumber(user.followingCount),
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            Text(
                                text = "Đang theo dõi",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
                            )
                        }

                        Column(modifier = Modifier.clickable { onShowFollowers() }) {
                            Text(
                                text = formatNumber(user.followersCount),
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            Text(
                                text = "Người theo dõi",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun EditProfileDialog(
    user: User,
    onDismiss: () -> Unit,
    onSave: (displayName: String, bio: String) -> Unit
) {
    var displayNameInput by remember { mutableStateOf(user.displayName) }
    var bioInput by remember { mutableStateOf(user.bio) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .wrapContentHeight(),
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .fillMaxWidth()
            ) {
                Text(
                    text = "Chỉnh sửa hồ sơ",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                OutlinedTextField(
                    value = displayNameInput,
                    onValueChange = { displayNameInput = it },
                    label = { Text("Họ và Tên") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp)
                        .testTag("edit_display_name_input"),
                    singleLine = true
                )

                OutlinedTextField(
                    value = bioInput,
                    onValueChange = { bioInput = it },
                    label = { Text("Tiểu sử") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 20.dp)
                        .testTag("edit_bio_input"),
                    maxLines = 4
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Hủy")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = { onSave(displayNameInput, bioInput) },
                        enabled = displayNameInput.isNotBlank(),
                        modifier = Modifier.testTag("save_profile_button")
                    ) {
                        Text("Lưu hồ sơ")
                    }
                }
            }
        }
    }
}

@Composable
fun FollowersListDialog(
    type: String, // "followers" or "following"
    currentUser: User?,
    otherUsers: List<User>,
    onDismiss: () -> Unit,
    onNavigateToProfile: (String) -> Unit,
    viewModel: SocialViewModel
) {
    val usersList = remember(type, otherUsers) {
        if (type == "following") {
            otherUsers.filter { it.isFollowing }
        } else {
            // Simulated followers: anyone who isn't current user can be followers for prototyping,
            // let's show all other users as mock followers
            otherUsers.filter { it.username != "@tech_guru" }
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .fillMaxHeight(0.7f),
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp
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
                        text = if (type == "followers") "Người theo dõi" else "Đang theo dõi",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Đóng")
                    }
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))

                if (usersList.isEmpty()) {
                    Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                        Text(
                            text = "Danh sách đang trống.",
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
                        )
                    }
                } else {
                    LazyColumn(modifier = Modifier.weight(1f).fillMaxWidth()) {
                        items(usersList) { user ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onNavigateToProfile(user.username) }
                                    .padding(horizontal = 16.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                AsyncImage(
                                    model = user.avatarUrl,
                                    contentDescription = null,
                                    modifier = Modifier
                                        .size(38.dp)
                                        .clip(CircleShape)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = user.displayName,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp
                                    )
                                    Text(
                                        text = user.username,
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
                                    )
                                }
                                
                                // Quick Follow Action button on row
                                TextButton(
                                    onClick = { viewModel.toggleFollowUser(user.username) },
                                    modifier = Modifier.height(30.dp)
                                ) {
                                    Text(
                                        text = if (user.isFollowing) "Bỏ theo dõi" else "Theo dõi",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                        }
                    }
                }
            }
        }
    }
}
