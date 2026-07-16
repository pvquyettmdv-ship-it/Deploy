package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.viewmodel.SocialViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SocialViewModel,
    modifier: Modifier = Modifier
) {
    val isDarkByPref by viewModel.isDarkMode.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    var showFooterDialog by remember { mutableStateOf<String?>(null) } // "terms", "privacy", "help"

    // Settings Toggle states
    var privateAccountState by remember { mutableStateOf(false) }
    var likesNotifState by remember { mutableStateOf(true) }
    var repliesNotifState by remember { mutableStateOf(true) }
    var followNotifState by remember { mutableStateOf(true) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Cài đặt hệ thống", fontWeight = FontWeight.Bold, fontSize = 18.sp) }
            )
        },
        modifier = modifier
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(scrollState)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            // Theme Mode Section
            Text(
                text = "Giao diện hiển thị",
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.primary
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
                shape = RoundedCornerShape(16.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = if (isDarkByPref) Icons.Default.DarkMode else Icons.Default.LightMode,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text("Chế độ tối (Dark Mode)", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text("Tiết kiệm pin và dịu mắt hơn", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f))
                        }
                    }
                    Switch(
                        checked = isDarkByPref,
                        onCheckedChange = { viewModel.toggleDarkMode() },
                        modifier = Modifier.testTag("dark_mode_switch")
                    )
                }
            }

            // Privacy Section
            Text(
                text = "Quyền riêng tư & Bảo mật",
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.primary
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column {
                    SettingsToggleRow(
                        icon = Icons.Default.Lock,
                        title = "Tài khoản riêng tư",
                        subtitle = "Chỉ người theo dõi đã phê duyệt mới xem được bài đăng",
                        checked = privateAccountState,
                        onCheckedChange = {
                            privateAccountState = it
                            val msg = if (it) "Đã chuyển sang tài khoản riêng tư" else "Đã chuyển sang tài khoản công khai"
                            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                        }
                    )
                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f), modifier = Modifier.padding(horizontal = 16.dp))
                    
                    SettingsClickRow(
                        icon = Icons.Default.Block,
                        title = "Tài khoản đã chặn",
                        subtitle = "Quản lý danh sách người dùng bị hạn chế",
                        onClick = { Toast.makeText(context, "Danh sách chặn hiện tại trống.", Toast.LENGTH_SHORT).show() }
                    )
                }
            }

            // Notifications Section
            Text(
                text = "Cài đặt thông báo",
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.primary
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column {
                    SettingsToggleRow(
                        icon = Icons.Default.Favorite,
                        title = "Lượt thích bài viết",
                        subtitle = "Thông báo khi có người thả tim bài đăng của bạn",
                        checked = likesNotifState,
                        onCheckedChange = { likesNotifState = it }
                    )
                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f), modifier = Modifier.padding(horizontal = 16.dp))
                    SettingsToggleRow(
                        icon = Icons.Default.ChatBubble,
                        title = "Bình luận & Phản hồi",
                        subtitle = "Thông báo khi có người trả lời bài bưu thiếp",
                        checked = repliesNotifState,
                        onCheckedChange = { repliesNotifState = it }
                    )
                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f), modifier = Modifier.padding(horizontal = 16.dp))
                    SettingsToggleRow(
                        icon = Icons.Default.PersonAdd,
                        title = "Người theo dõi mới",
                        subtitle = "Thông báo khi có tài khoản bắt đầu theo dõi bạn",
                        checked = followNotifState,
                        onCheckedChange = { followNotifState = it }
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // Footer Links Section (Chân trang)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 24.dp, bottom = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Điều khoản sử dụng",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .clickable { showFooterDialog = "terms" }
                            .testTag("footer_terms")
                    )
                    Text(
                        text = "•",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f)
                    )
                    Text(
                        text = "Quyền riêng tư",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .clickable { showFooterDialog = "privacy" }
                            .testTag("footer_privacy")
                    )
                    Text(
                        text = "•",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f)
                    )
                    Text(
                        text = "Trợ giúp",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .clickable { showFooterDialog = "help" }
                            .testTag("footer_help")
                    )
                }

                Text(
                    text = "NetVibe v1.0.0 — Bản quyền thuộc về Quyết Phạm © 2026",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f),
                    textAlign = TextAlign.Center
                )
            }
        }

        // Footer Alert dialogs
        showFooterDialog?.let { target ->
            FooterDialog(
                type = target,
                onDismiss = { showFooterDialog = null }
            )
        }
    }
}

@Composable
fun SettingsToggleRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(title, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                Text(subtitle, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f))
            }
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                checkedTrackColor = MaterialTheme.colorScheme.primary
            )
        )
    }
}

@Composable
fun SettingsClickRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(title, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                Text(subtitle, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f))
            }
        }
        Icon(
            imageVector = Icons.Default.ChevronRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
            modifier = Modifier.size(20.dp)
        )
    }
}

@Composable
fun FooterDialog(
    type: String,
    onDismiss: () -> Unit
) {
    val dialogData = remember(type) {
        when (type) {
            "terms" -> Pair(
                "Điều khoản dịch vụ NetVibe",
                "Chào mừng bạn đến với mạng xã hội NetVibe. Bằng việc đăng ký tài khoản hoặc sử dụng dịch vụ của chúng tôi, bạn cam kết tuân thủ các quy tắc ứng xử cộng đồng lành mạnh:\n\n1. Không đăng tải nội dung đồi trụy, thù hận hoặc xúc phạm người khác.\n2. Tôn trọng quyền sở hữu trí tuệ của người khác.\n3. Tránh lan truyền tin giả hoặc hành vi quấy rối trực tuyến.\n\nChúng tôi có quyền khóa tài khoản của người dùng vi phạm nghiêm trọng."
            )
            "privacy" -> Pair(
                "Chính sách bảo mật quyền riêng tư",
                "Tại NetVibe, an toàn thông tin của bạn là ưu tiên hàng đầu của chúng tôi:\n\n1. Chúng tôi lưu trữ dữ liệu cục bộ trong cơ sở dữ liệu Room an toàn trên điện thoại của bạn để đảm bảo hiệu suất ngoại tuyến.\n2. Các yêu cầu phân tích tối ưu hóa bài đăng và trò chuyện với trợ lý AI sẽ được chuyển tiếp một cách ẩn danh và an toàn qua Google Gemini API để bảo mật danh tính.\n3. Chúng tôi tuyệt đối không mua bán hay chia sẻ thông tin cá nhân của bạn cho bất kỳ bên thứ ba nào."
            )
            else -> Pair(
                "Trung tâm trợ giúp NetVibe",
                "Cần hỗ trợ? Chúng tôi luôn sẵn sàng đồng hành cùng bạn:\n\n• Cách kích hoạt Trợ lý AI: Truy cập thanh mở rộng bên trái (☰) và chọn 'Trợ lý AI Suy nghĩ Sâu'.\n• Cách Tối ưu bài viết: Khi tạo bài viết mới, hãy bấm 'Tối ưu bằng Gemini AI 🧠' để AI viết lại mượt mà hơn.\n• Phản hồi lỗi: Gửi email liên hệ trực tiếp cho nhà phát triển pvquyet.tmdv@gmail.com để được phản hồi trong vòng 24 giờ."
            )
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(dialogData.first, fontWeight = FontWeight.Bold, fontSize = 16.sp) },
        text = {
            Text(
                text = dialogData.second,
                fontSize = 13.sp,
                lineHeight = 18.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
            )
        },
        confirmButton = {
            Button(onClick = onDismiss) {
                Text("Tôi đã hiểu")
            }
        },
        shape = RoundedCornerShape(16.dp)
    )
}
