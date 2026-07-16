package com.example.ui.screens

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
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.data.local.Notification
import com.example.ui.theme.HeartColor
import com.example.ui.theme.RepostColor
import com.example.ui.viewmodel.SocialViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActivityScreen(
    viewModel: SocialViewModel,
    onNavigateToProfile: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val notifications by viewModel.notifications.collectAsStateWithLifecycle()
    val activeFilter by viewModel.activeNotificationFilter.collectAsStateWithLifecycle()

    val filteredNotifications = remember(notifications, activeFilter) {
        when (activeFilter) {
            "like" -> notifications.filter { it.type == "like" }
            "comment" -> notifications.filter { it.type == "comment" }
            "follow" -> notifications.filter { it.type == "follow" }
            else -> notifications
        }
    }

    val unreadCount = remember(notifications) {
        notifications.count { !it.isRead }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Hoạt động & Thông báo", fontWeight = FontWeight.Bold, fontSize = 18.sp) },
                actions = {
                    if (unreadCount > 0) {
                        TextButton(
                            onClick = { viewModel.clearNotifications() },
                            modifier = Modifier.testTag("clear_notif_btn")
                        ) {
                            Text("Đọc tất cả", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        }
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
            // Filter Bar
            FilterRow(
                activeFilter = activeFilter,
                onFilterSelected = { viewModel.activeNotificationFilter.value = it }
            )

            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))

            if (filteredNotifications.isEmpty()) {
                // Empty state
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.NotificationsNone,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Không có hoạt động nào",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Các thông báo thích, bình luận, và theo dõi sẽ hiển thị tại đây.",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
                            modifier = Modifier.padding(horizontal = 16.dp),
                            lineHeight = 16.sp
                        )
                    }
                }
            } else {
                LazyColumn(modifier = Modifier.fillMaxSize().weight(1f)) {
                    items(filteredNotifications, key = { it.id }) { notification ->
                        NotificationRow(
                            notification = notification,
                            onUserClick = { onNavigateToProfile(notification.fromUsername) }
                        )
                        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                    }
                }
            }
        }
    }
}

@Composable
fun FilterRow(
    activeFilter: String,
    onFilterSelected: (String) -> Unit
) {
    val filters = listOf(
        Triple("all", "Tất cả", Icons.Default.Menu),
        Triple("like", "Lượt thích", Icons.Default.Favorite),
        Triple("comment", "Trả lời", Icons.Default.ChatBubble),
        Triple("follow", "Theo dõi", Icons.Default.PersonAdd)
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        filters.forEach { filter ->
            val isSelected = activeFilter == filter.first
            val containerColor = if (isSelected) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
            }
            val contentColor = if (isSelected) {
                MaterialTheme.colorScheme.onPrimary
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant
            }

            Surface(
                onClick = { onFilterSelected(filter.first) },
                color = containerColor,
                contentColor = contentColor,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .weight(1f)
                    .height(34.dp)
                    .testTag("filter_chip_${filter.first}")
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxSize()
                ) {
                    Icon(
                        imageVector = filter.third,
                        contentDescription = null,
                        modifier = Modifier.size(13.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = filter.second,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
fun NotificationRow(
    notification: Notification,
    onUserClick: () -> Unit
) {
    val isUnread = !notification.isRead

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(if (isUnread) MaterialTheme.colorScheme.primary.copy(alpha = 0.04f) else Color.Transparent)
            .clickable { onUserClick() }
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Icon indicator based on notification type
        val primaryColor = MaterialTheme.colorScheme.primary
        val secondaryColor = MaterialTheme.colorScheme.secondary
        val iconInfo = remember(notification.type, primaryColor, secondaryColor) {
            when (notification.type) {
                "like" -> Pair(Icons.Default.Favorite, HeartColor)
                "comment" -> Pair(Icons.Default.ChatBubble, secondaryColor)
                "follow" -> Pair(Icons.Default.PersonAdd, primaryColor)
                else -> Pair(Icons.Default.Repeat, RepostColor)
            }
        }

        Box(
            modifier = Modifier
                .size(42.dp)
        ) {
            // User Avatar
            AsyncImage(
                model = notification.fromAvatarUrl,
                contentDescription = null,
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .border(1.dp, MaterialTheme.colorScheme.outline, CircleShape),
                contentScale = ContentScale.Crop
            )

            // Small overlapping indicator icon
            Box(
                modifier = Modifier
                    .size(18.dp)
                    .background(iconInfo.second, CircleShape)
                    .align(Alignment.BottomEnd)
                    .border(1.5.dp, MaterialTheme.colorScheme.surface, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = iconInfo.first,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(9.dp)
                )
            }
        }

        Spacer(modifier = Modifier.width(14.dp))

        Column(modifier = Modifier.weight(1f)) {
            // Text detail
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = notification.fromDisplayName,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = notification.fromUsername,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.height(2.dp))

            val notificationMessage = remember(notification.type) {
                when (notification.type) {
                    "like" -> "đã thích bài viết của bạn"
                    "comment" -> "đã trả lời bài viết của bạn"
                    "follow" -> "đã bắt đầu theo dõi bạn"
                    else -> "đã chia sẻ lại bài viết của bạn"
                }
            }

            Text(
                text = if (notification.postContentSnippet != null) {
                    "$notificationMessage: \"${notification.postContentSnippet}\""
                } else {
                    notificationMessage
                },
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = formatRelativeTime(notification.timestamp),
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f)
            )
        }

        // Unread dot indicator
        if (isUnread) {
            Spacer(modifier = Modifier.width(8.dp))
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .background(MaterialTheme.colorScheme.primary, CircleShape)
            )
        }
    }
}
