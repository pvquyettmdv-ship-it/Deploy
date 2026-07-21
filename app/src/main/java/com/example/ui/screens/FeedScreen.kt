package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
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
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.GeminiService
import com.example.ui.MainViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

// --- Threads Clone Specific Data Models ---

data class ThreadPoll(
    val id: String,
    val question: String,
    val options: List<String>,
    var votes: MutableList<Int>,
    var userVotedIndex: Int? = null
)

data class ThreadComment(
    val id: String,
    val authorName: String,
    val authorUsername: String,
    val authorAvatar: String,
    var content: String,
    val timestamp: String,
    var likesCount: Int = 0,
    var userLiked: Boolean = false,
    val replies: MutableList<ThreadComment> = mutableListOf()
)

data class ThreadPost(
    val id: String,
    val authorName: String,
    val authorUsername: String,
    val authorAvatar: String,
    val isVerified: Boolean = false,
    var content: String,
    val mediaUrls: List<String> = emptyList(), // Simulated image colors / links
    val videoUrl: String? = null, // Simulated video
    val gifUrl: String? = null,
    val timestamp: String,
    var likesCount: Int,
    var commentsCount: Int,
    var repostsCount: Int = 0,
    var userLiked: Boolean = false,
    var userReposted: Boolean = false,
    var userSaved: Boolean = false,
    val location: String? = null,
    val poll: ThreadPoll? = null,
    val hashtags: List<String> = emptyList(),
    val mentions: List<String> = emptyList(),
    val linkPreview: String? = null,
    val comments: MutableList<ThreadComment> = mutableListOf()
)

data class DirectChat(
    val id: String,
    val chatName: String,
    val avatarColor: Long,
    val isGroup: Boolean = false,
    val messages: MutableList<DirectMessage> = mutableListOf(),
    var isTyping: Boolean = false
)

data class DirectMessage(
    val id: String,
    val sender: String, // "You" or specific name
    var content: String,
    val timestamp: String,
    val type: String = "text", // "text", "image", "gif", "voice"
    val isRevoked: Boolean = false,
    var reactions: MutableList<String> = mutableListOf(),
    var isSeen: Boolean = true
)

data class ThreadNotification(
    val id: String,
    val type: String, // "like", "comment", "mention", "follow", "system"
    val senderName: String,
    val senderAvatar: String,
    val content: String,
    val timestamp: String,
    var actionCompleted: Boolean = false
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FeedScreen(
    viewModel: MainViewModel,
    onProductClick: (Int) -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current

    // --- State persistence within Threads Session ---
    var isLoggedIn by remember { mutableStateOf(true) } // Bypass by default for convenience, can log out
    var loggedInUser by remember {
        mutableStateOf(
            mapOf(
                "name" to "Nguyễn Minh",
                "username" to "nguyenminh_threads",
                "avatar" to "NM",
                "bio" to "Đam mê lập trình & mua sắm online | VibeCart Seller 💻🛒",
                "website" to "vibecart.vn/nguyenminh",
                "followers" to "1,240",
                "following" to "482"
            )
        )
    }

    var isDarkMode by remember { mutableStateOf(true) }
    var currentLanguage by remember { mutableStateOf("vi") } // "vi" or "en"

    // Local colors mimicking Threads Dark and Light Mode
    val primaryColor = Color(0xFFFF5722) // VibeCart brand accent
    val threadsBgColor = if (isDarkMode) Color(0xFF101010) else Color(0xFFF9F9F9)
    val threadsSurfaceColor = if (isDarkMode) Color(0xFF1A1A1A) else Color(0xFFFFFFFF)
    val threadsTextColor = if (isDarkMode) Color.White else Color.Black
    val threadsSubTextColor = if (isDarkMode) Color.Gray else Color(0xFF757575)
    val threadsBorderColor = if (isDarkMode) Color(0xFF262626) else Color(0xFFE5E5E5)

    // Current screen context within the clone: "Home", "Search", "Create", "Notification", "Profile", "Messages", "Settings", "Admin"
    var currentSubTab by remember { mutableStateOf("Home") }

    // Search histories
    val searchHistory = remember { mutableStateListOf("shopeelive", "son tint glow", "phongnhom", "tainghe") }

    // Suggested accounts
    val suggestedFollows = remember {
        mutableStateListOf(
            mapOf("name" to "Chanh Beauty 🌸", "username" to "chanh_beauty", "avatar" to "CB", "isFollowed" to "false"),
            mapOf("name" to "Hoàng Sơn Review 📱", "username" to "son_techie", "avatar" to "HS", "isFollowed" to "false"),
            mapOf("name" to "Vào Bếp Cùng Vy Vy 🍳", "username" to "vy_kitchen", "avatar" to "VV", "isFollowed" to "false"),
            mapOf("name" to "Huyền My Store 👗", "username" to "huyen_my_fashion", "avatar" to "HM", "isFollowed" to "false")
        )
    }

    // Seed Initial Threads Posts
    val threadPosts = remember {
        mutableStateListOf(
            ThreadPost(
                id = "thread_1",
                authorName = "Chanh Beauty 🌸",
                authorUsername = "chanh_beauty",
                authorAvatar = "CB",
                isVerified = true,
                content = "Săn deal son GlowGlow ngay tối nay trên livestream nha cả nhà ơi! Chất son tint nhung mướt lắm luôn, giữ màu cực tốt, bôi vào căng bóng tràn sức sống ✨💄",
                location = "Hồ Chí Minh, Việt Nam",
                timestamp = "10 phút trước",
                likesCount = 421,
                commentsCount = 3,
                repostsCount = 12,
                hashtags = listOf("GlowGlowSon", "SănSaleLive", "LàmĐẹp"),
                linkPreview = "https://vibecart.vn/product/103",
                comments = mutableStateListOf(
                    ThreadComment(
                        id = "c1_1",
                        authorName = "Linh Đan",
                        authorUsername = "linhdan_99",
                        authorAvatar = "LD",
                        content = "Màu son xinh xỉu luôn chị ơi, tối nay em canh chốt liền!",
                        timestamp = "20 phút trước",
                        likesCount = 14,
                        replies = mutableStateListOf(
                            ThreadComment(
                                id = "c1_2",
                                authorName = "Chanh Beauty 🌸",
                                authorUsername = "chanh_beauty",
                                authorAvatar = "CB",
                                content = "Cảm ơn em iu nha, tối canh voucher 50k nữa nhé!",
                                timestamp = "18 phút trước"
                            )
                        )
                    ),
                    ThreadComment(
                        id = "c2_1",
                        authorName = "Trần Long",
                        authorUsername = "long_kt",
                        authorAvatar = "TL",
                        content = "Có màu hồng đất không shop ơi?",
                        timestamp = "35 phút trước"
                    )
                )
            ),
            ThreadPost(
                id = "thread_2",
                authorName = "Hoàng Sơn Review 📱",
                authorUsername = "son_techie",
                authorAvatar = "HS",
                isVerified = true,
                content = "Anh em vote thử xem tính năng nào của tai nghe LunarTech ANC đỉnh nhất nào? Đang phân vân để làm clip chi tiết lên sóng tuần tới 🤔🎧",
                timestamp = "2 giờ trước",
                likesCount = 189,
                commentsCount = 2,
                repostsCount = 5,
                poll = ThreadPoll(
                    id = "poll_tech",
                    question = "Tính năng đỉnh nhất của LunarTech?",
                    options = listOf("Chống ồn ANC cực sâu 🔇", "Thời lượng pin 40H 🔋", "Dải âm Bass mạnh mẽ 🎧", "Giá bán siêu rẻ 💰"),
                    votes = mutableStateListOf(42, 18, 25, 31)
                ),
                comments = mutableStateListOf(
                    ThreadComment(
                        id = "c3_1",
                        authorName = "Bảo Ngọc",
                        authorUsername = "ngoc_bao",
                        authorAvatar = "BN",
                        content = "Vote chống ồn nha anh Sơn, bật lên cái là cách biệt thế giới luôn!",
                        timestamp = "1 giờ trước"
                    )
                )
            ),
            ThreadPost(
                id = "thread_3",
                authorName = "VibeCart Official 🛒",
                authorUsername = "vibecart_co",
                authorAvatar = "VC",
                isVerified = true,
                content = "Chào mừng bạn gia nhập sân chơi Threads Clone hoành tráng nhất hệ mặt trời! Đầy đủ mọi tính năng: AI soạn bài, dịch bài, chát trực tiếp, thống kê admin. Trải nghiệm ngay nhé cả nhà! 🤖✨",
                timestamp = "1 ngày trước",
                likesCount = 1240,
                commentsCount = 0,
                repostsCount = 45,
                gifUrl = "Pulsing Welcome"
            )
        )
    }

    // Direct Chats list
    val directChats = remember {
        mutableStateListOf(
            DirectChat(
                id = "chat_huyen",
                chatName = "Huyền My Fashion 👗",
                avatarColor = 0xFFE91E63,
                messages = mutableStateListOf(
                    DirectMessage("m1", "Huyền My Fashion 👗", "Chào bạn, đơn hàng áo hoodie của bạn đã được đóng gói chuẩn bị giao nhé!", "09:30"),
                    DirectMessage("m2", "You", "Dạ vâng shop ơi, giao nhanh giúp mình nha, cuối tuần mình cần đi chụp ảnh ạ.", "09:32", isSeen = true),
                    DirectMessage("m3", "Huyền My Fashion 👗", "Oki bạn iu nè, ship hỏa tốc luôn nhé! 😘", "09:33", isSeen = true)
                )
            ),
            DirectChat(
                id = "chat_group_koc",
                chatName = "Hội Nhóm KOC Chốt Deal 👥",
                avatarColor = 0xFF009688,
                isGroup = true,
                messages = mutableStateListOf(
                    DirectMessage("mg1", "Chanh Beauty", "Tối nay các idol lên live bán hàng rần rần nha!", "Hôm qua"),
                    DirectMessage("mg2", "Hoàng Sơn Review", "Mới nhận hàng test okela lắm cả nhà ạ, tối lên sóng.", "Hôm qua"),
                    DirectMessage("mg3", "You", "Tuyệt vời, chúc nhóm mình tối nay bão đơn nhé!", "Hôm qua", isSeen = true)
                )
            )
        )
    }

    // Notifications List
    val notifications = remember {
        mutableStateListOf(
            ThreadNotification("n1", "follow", "Bảo Ngọc", "BN", "đã bắt đầu theo dõi bạn.", "5 phút trước"),
            ThreadNotification("n2", "like", "Trần Nam", "TN", "đã thích bài viết: Chào mừng bạn gia nhập sân chơi...", "12 phút trước"),
            ThreadNotification("n3", "comment", "Hương Giang", "HG", "đã trả lời một chủ đề của bạn: \"Sản phẩm chất lượng quá...\"", "1 giờ trước"),
            ThreadNotification("n4", "system", "Hệ thống bảo mật 🔒", "SYS", "Tài khoản của bạn đã kích hoạt tính năng Xác thực 2 lớp (2FA).", "Hôm qua")
        )
    }

    // Admin Logs & reports
    val adminReports = remember {
        mutableStateListOf(
            mapOf("id" to "R1", "reporter" to "Trần Nam", "reportedUser" to "SpamBot99", "reason" to "Spam link quảng cáo cờ bạc", "status" to "Đang xử lý"),
            mapOf("id" to "R2", "reporter" to "Linh Chi", "reportedUser" to "ToxicUser1", "reason" to "Bình luận thô tục trong live", "status" to "Đã xử lý")
        )
    }

    // Settings States
    var is2FAEnabled by remember { mutableStateOf(false) }
    var blockedUsers = remember { mutableStateListOf("SpamBot99", "Hacker_Lỏd") }

    // Draft/Schedule states
    val draftPostContent = remember { mutableStateOf("") }
    var activeChatId by remember { mutableStateOf<String?>(null) }

    // Dialog & Overlays
    var activePostForComments by remember { mutableStateOf<ThreadPost?>(null) }
    var activePostForTranslate by remember { mutableStateOf<ThreadPost?>(null) }
    var aiWritingPrompt by remember { mutableStateOf("") }
    var isGeneratingAI by remember { mutableStateOf(false) }
    var showAICodeSuggestion by remember { mutableStateOf(false) }
    var imageFullscreenUrl by remember { mutableStateOf<String?>(null) }
    var showCaptchaChallenge by remember { mutableStateOf(false) }
    var showOtpChallenge by remember { mutableStateOf(false) }

    // Registration Temp states
    var regEmail by remember { mutableStateOf("") }
    var regUsername by remember { mutableStateOf("") }
    var regPass by remember { mutableStateOf("") }
    var agreeToTerms by remember { mutableStateOf(false) }

    // --- Onboarding / Login View if logged out ---
    if (!isLoggedIn) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(threadsBgColor)
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Spacer(modifier = Modifier.height(40.dp))
            // Threads Loop Logo Representation
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .drawBehind {
                        drawCircle(
                            brush = Brush.radialGradient(
                                colors = listOf(Color(0xFFFC466B), Color(0xFF3F5EFB))
                            )
                        )
                    },
                contentAlignment = Alignment.Center
            ) {
                Text("@", color = Color.White, fontSize = 48.sp, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Threads Clone",
                color = threadsTextColor,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 28.sp
            )
            Text(
                text = "Không gian chia sẻ văn bản, hình ảnh, ý tưởng tức thời",
                color = threadsSubTextColor,
                fontSize = 12.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 24.dp)
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Choose either Login or Register tabs
            var loginTabActive by remember { mutableStateOf(true) }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(threadsSurfaceColor)
                    .border(1.dp, threadsBorderColor, RoundedCornerShape(12.dp))
                    .padding(4.dp)
            ) {
                Button(
                    onClick = { loginTabActive = true },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (loginTabActive) primaryColor else Color.Transparent,
                        contentColor = if (loginTabActive) Color.White else threadsTextColor
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Đăng Nhập", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
                Button(
                    onClick = { loginTabActive = false },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (!loginTabActive) primaryColor else Color.Transparent,
                        contentColor = if (!loginTabActive) Color.White else threadsTextColor
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Đăng Ký", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            if (loginTabActive) {
                // LOGIN SCREEN FORM
                var emailInput by remember { mutableStateOf("") }
                var passwordInput by remember { mutableStateOf("") }
                var rememberMe by remember { mutableStateOf(true) }

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = threadsSurfaceColor),
                    border = BorderStroke(1.dp, threadsBorderColor)
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        OutlinedTextField(
                            value = emailInput,
                            onValueChange = { emailInput = it },
                            label = { Text("Email hoặc Username") },
                            placeholder = { Text("nguyenminh_threads") },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = threadsTextColor,
                                unfocusedTextColor = threadsTextColor,
                                focusedBorderColor = primaryColor,
                                unfocusedBorderColor = threadsBorderColor
                            ),
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )

                        OutlinedTextField(
                            value = passwordInput,
                            onValueChange = { passwordInput = it },
                            label = { Text("Mật khẩu") },
                            placeholder = { Text("••••••••") },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = threadsTextColor,
                                unfocusedTextColor = threadsTextColor,
                                focusedBorderColor = primaryColor,
                                unfocusedBorderColor = threadsBorderColor
                            ),
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Checkbox(
                                    checked = rememberMe,
                                    onCheckedChange = { rememberMe = it },
                                    colors = CheckboxDefaults.colors(checkedColor = primaryColor)
                                )
                                Text("Ghi nhớ", color = threadsTextColor, fontSize = 12.sp)
                            }
                            TextButton(onClick = {
                                android.widget.Toast.makeText(context, "Tính năng khôi phục mật khẩu OTP đã được gửi đến email mẫu!", android.widget.Toast.LENGTH_LONG).show()
                            }) {
                                Text("Quên mật khẩu?", color = primaryColor, fontSize = 12.sp)
                            }
                        }

                        Button(
                            onClick = {
                                if (emailInput.isNotBlank() && passwordInput.isNotBlank()) {
                                    isLoggedIn = true
                                    android.widget.Toast.makeText(context, "Đăng nhập thành công!", android.widget.Toast.LENGTH_SHORT).show()
                                } else {
                                    android.widget.Toast.makeText(context, "Vui lòng điền đầy đủ thông tin đăng nhập!", android.widget.Toast.LENGTH_SHORT).show()
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = primaryColor),
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Đăng Nhập Ngay 🚀", fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text("Hoặc tiếp tục với", color = threadsSubTextColor, fontSize = 11.sp)
                Spacer(modifier = Modifier.height(8.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Button(
                        onClick = {
                            isLoggedIn = true
                            android.widget.Toast.makeText(context, "Đăng nhập thành công qua Google!", android.widget.Toast.LENGTH_SHORT).show()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = threadsSurfaceColor),
                        border = BorderStroke(1.dp, threadsBorderColor)
                    ) {
                        Text("Google 🌐", color = threadsTextColor, fontSize = 12.sp)
                    }
                    Button(
                        onClick = {
                            isLoggedIn = true
                            android.widget.Toast.makeText(context, "Đăng nhập thành công qua Facebook!", android.widget.Toast.LENGTH_SHORT).show()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = threadsSurfaceColor),
                        border = BorderStroke(1.dp, threadsBorderColor)
                    ) {
                        Text("Facebook 📘", color = threadsTextColor, fontSize = 12.sp)
                    }
                }
            } else {
                // REGISTER FORM WITH CAPTCHA & OTP SIMULATION
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = threadsSurfaceColor),
                    border = BorderStroke(1.dp, threadsBorderColor)
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        OutlinedTextField(
                            value = regEmail,
                            onValueChange = { regEmail = it },
                            label = { Text("Email đăng ký") },
                            placeholder = { Text("yourmail@gmail.com") },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = threadsTextColor,
                                unfocusedTextColor = threadsTextColor,
                                focusedBorderColor = primaryColor,
                                unfocusedBorderColor = threadsBorderColor
                            ),
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )

                        OutlinedTextField(
                            value = regUsername,
                            onValueChange = { regUsername = it },
                            label = { Text("Tên người dùng (Username)") },
                            placeholder = { Text("username_moi") },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = threadsTextColor,
                                unfocusedTextColor = threadsTextColor,
                                focusedBorderColor = primaryColor,
                                unfocusedBorderColor = threadsBorderColor
                            ),
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )

                        OutlinedTextField(
                            value = regPass,
                            onValueChange = { regPass = it },
                            label = { Text("Mật khẩu") },
                            placeholder = { Text("••••••••") },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = threadsTextColor,
                                unfocusedTextColor = threadsTextColor,
                                focusedBorderColor = primaryColor,
                                unfocusedBorderColor = threadsBorderColor
                            ),
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(
                                checked = agreeToTerms,
                                onCheckedChange = { agreeToTerms = it },
                                colors = CheckboxDefaults.colors(checkedColor = primaryColor)
                            )
                            Text("Tôi đồng ý với mọi điều khoản sử dụng và chính sách bảo mật.", color = threadsTextColor, fontSize = 10.sp)
                        }

                        Button(
                            onClick = {
                                if (regEmail.isBlank() || regUsername.isBlank() || regPass.isBlank()) {
                                    android.widget.Toast.makeText(context, "Vui lòng nhập đầy đủ thông tin đăng ký!", android.widget.Toast.LENGTH_SHORT).show()
                                } else if (!agreeToTerms) {
                                    android.widget.Toast.makeText(context, "Vui lòng chấp nhận điều khoản!", android.widget.Toast.LENGTH_SHORT).show()
                                } else {
                                    // Trigger CAPTCHA puzzle before letting them continue!
                                    showCaptchaChallenge = true
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = primaryColor),
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Đăng Ký & Nhận OTP 🚀", fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }
                }
            }
        }

        // CAPTCHA PUZZLE CHALLENGE DIALOG
        if (showCaptchaChallenge) {
            var captchaSlideValue by remember { mutableStateOf(0f) }
            val captchaTarget = 68f // Slide target for captcha completion
            AlertDialog(
                onDismissRequest = { showCaptchaChallenge = false },
                title = { Text("Thử thách CAPTCHA 🤖", color = Color.White, fontWeight = FontWeight.Bold) },
                text = {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("Kéo thanh trượt để khớp mảnh ghép thông minh (chống Spam):", color = Color.LightGray, fontSize = 12.sp)
                        Spacer(modifier = Modifier.height(16.dp))

                        // Box puzzle graphics mock
                        Box(
                            modifier = Modifier
                                .size(240.dp, 100.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFF2C2C2C)),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            // Target location
                            Box(
                                modifier = Modifier
                                    .offset(x = captchaTarget.dp)
                                    .size(36.dp)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(Color.White.copy(alpha = 0.2f))
                                    .border(2.dp, Color.Green, RoundedCornerShape(6.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Filled.Lock, contentDescription = null, tint = Color.Green, modifier = Modifier.size(16.dp))
                            }

                            // Slidable puzzle piece
                            Box(
                                modifier = Modifier
                                    .offset(x = captchaSlideValue.dp)
                                    .size(36.dp)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(primaryColor),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Filled.Key, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Slider(
                            value = captchaSlideValue,
                            onValueChange = { captchaSlideValue = it },
                            valueRange = 0f..200f,
                            colors = SliderDefaults.colors(
                                thumbColor = primaryColor,
                                activeTrackColor = primaryColor
                            )
                        )

                        Text("Vị trí hiện tại: ${captchaSlideValue.toInt()}", color = Color.Gray, fontSize = 11.sp)
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            val diff = Math.abs(captchaSlideValue - captchaTarget)
                            if (diff < 12f) {
                                showCaptchaChallenge = false
                                showOtpChallenge = true // CAPTCHA successful, trigger OTP verification email!
                                android.widget.Toast.makeText(context, "CAPTCHA chính xác! Đang gửi OTP...", android.widget.Toast.LENGTH_SHORT).show()
                            } else {
                                android.widget.Toast.makeText(context, "Chưa trùng khớp mảnh ghép! Thử lại.", android.widget.Toast.LENGTH_SHORT).show()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = primaryColor)
                    ) {
                        Text("Xác nhận ghép", color = Color.White)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showCaptchaChallenge = false }) { Text("Hủy", color = Color.Gray) }
                },
                containerColor = Color(0xFF1E1E1E)
            )
        }

        // OTP CHALLENGE DIALOG
        if (showOtpChallenge) {
            var otpValue by remember { mutableStateOf("") }
            AlertDialog(
                onDismissRequest = { showOtpChallenge = false },
                title = { Text("Xác thực mã OTP 📧", color = Color.White, fontWeight = FontWeight.Bold) },
                text = {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text("Một mã xác thực OTP 6 số đã được gửi mẫu đến email của bạn. Nhập mã bên dưới để hoàn tất đăng ký:", color = Color.LightGray, fontSize = 12.sp)
                        OutlinedTextField(
                            value = otpValue,
                            onValueChange = { if (it.length <= 6) otpValue = it },
                            label = { Text("Nhập OTP (ví dụ: 123456)") },
                            placeholder = { Text("Mã gồm 6 chữ số") },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = primaryColor,
                                unfocusedBorderColor = Color.Gray
                            ),
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (otpValue.length == 6) {
                                showOtpChallenge = false
                                loggedInUser = mapOf(
                                    "name" to regUsername.replaceFirstChar { it.uppercase() },
                                    "username" to regUsername.lowercase(),
                                    "avatar" to regUsername.take(2).uppercase(),
                                    "bio" to "Gia nhập Threads Clone thành công! 🌟",
                                    "website" to "vibecart.vn/${regUsername}",
                                    "followers" to "0",
                                    "following" to "0"
                                )
                                isLoggedIn = true
                                android.widget.Toast.makeText(context, "Chúc mừng! Đăng ký tài khoản thành công!", android.widget.Toast.LENGTH_LONG).show()
                            } else {
                                android.widget.Toast.makeText(context, "Mã OTP phải gồm 6 chữ số!", android.widget.Toast.LENGTH_SHORT).show()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = primaryColor)
                    ) {
                        Text("Xác thực", color = Color.White)
                    }
                },
                containerColor = Color(0xFF1E1E1E)
            )
        }
    } else {
        // --- MAIN WORKSPACE WORKFLOW (THREADS CLONE HUB) ---
        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(
                    title = {
                        // Official logo layout
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(CircleShape)
                                    .background(threadsTextColor),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("@", color = threadsBgColor, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                "Threads",
                                fontWeight = FontWeight.Black,
                                fontSize = 18.sp,
                                color = threadsTextColor
                            )
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = { isDarkMode = !isDarkMode }) {
                            Icon(
                                imageVector = if (isDarkMode) Icons.Outlined.LightMode else Icons.Outlined.DarkMode,
                                contentDescription = "Toggle Dark Mode",
                                tint = threadsTextColor
                            )
                        }
                    },
                    actions = {
                        // Direct Chat notification / entry icon button
                        Box(contentAlignment = Alignment.Center) {
                            IconButton(onClick = { currentSubTab = "Messages" }) {
                                Icon(
                                    imageVector = Icons.Outlined.Forum,
                                    contentDescription = "Messages",
                                    tint = if (currentSubTab == "Messages") primaryColor else threadsTextColor
                                )
                            }
                            // Message unread badge
                            Box(
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .padding(top = 4.dp, end = 4.dp)
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(primaryColor)
                            )
                        }

                        // Advanced Panel Toggle
                        IconButton(onClick = { currentSubTab = "Admin" }) {
                            Icon(
                                imageVector = Icons.Outlined.SupervisorAccount,
                                contentDescription = "Admin Area",
                                tint = if (currentSubTab == "Admin") primaryColor else threadsTextColor
                            )
                        }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = threadsBgColor,
                        titleContentColor = threadsTextColor
                    )
                )
            },
            bottomBar = {
                // Multi-tab design for optimal Threads clone experience
                NavigationBar(
                    containerColor = threadsBgColor,
                    tonalElevation = 8.dp,
                    modifier = Modifier.border(0.5.dp, threadsBorderColor, RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
                ) {
                    val tabs = listOf(
                        Triple("Home", Icons.Filled.Home, Icons.Outlined.Home),
                        Triple("Search", Icons.Filled.Search, Icons.Outlined.Search),
                        Triple("Create", Icons.Filled.AddBox, Icons.Outlined.AddBox),
                        Triple("Notification", Icons.Filled.Favorite, Icons.Outlined.FavoriteBorder),
                        Triple("Profile", Icons.Filled.AccountCircle, Icons.Outlined.AccountCircle)
                    )

                    tabs.forEach { (tabKey, activeIcon, inactiveIcon) ->
                        val isSelected = currentSubTab == tabKey
                        NavigationBarItem(
                            selected = isSelected,
                            onClick = { currentSubTab = tabKey },
                            icon = {
                                Icon(
                                    imageVector = if (isSelected) activeIcon else inactiveIcon,
                                    contentDescription = tabKey,
                                    tint = if (isSelected) primaryColor else threadsTextColor
                                )
                            },
                            colors = NavigationBarItemDefaults.colors(
                                indicatorColor = primaryColor.copy(alpha = 0.15f)
                            )
                        )
                    }
                }
            },
            containerColor = threadsBgColor
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .background(threadsBgColor)
            ) {
                // Crossfade container to navigate screens smoothly
                Crossfade(targetState = currentSubTab, animationSpec = tween(250)) { tab ->
                    when (tab) {
                        "Home" -> {
                            // --- THREADS HOME FEED SCREEN ---
                            var activeFeedType by remember { mutableStateOf("ForYou") } // "ForYou" or "Following"
                            
                            Column(modifier = Modifier.fillMaxSize()) {
                                // Sub-filters
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 16.dp, vertical = 6.dp),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(16.dp))
                                            .background(if (activeFeedType == "ForYou") primaryColor else threadsSurfaceColor)
                                            .clickable { activeFeedType = "ForYou" }
                                            .padding(horizontal = 14.dp, vertical = 6.dp)
                                    ) {
                                        Text("Dành cho bạn ✨", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(16.dp))
                                            .background(if (activeFeedType == "Following") primaryColor else threadsSurfaceColor)
                                            .border(1.dp, threadsBorderColor, RoundedCornerShape(16.dp))
                                            .clickable { activeFeedType = "Following" }
                                            .padding(horizontal = 14.dp, vertical = 6.dp)
                                    ) {
                                        Text("Đang theo dõi 👥", color = if (activeFeedType == "Following") Color.White else threadsTextColor, fontSize = 11.sp)
                                    }
                                }

                                Divider(color = threadsBorderColor, thickness = 0.5.dp, modifier = Modifier.padding(top = 4.dp))

                                // Threads scrolling list
                                LazyColumn(
                                    modifier = Modifier.fillMaxSize(),
                                    contentPadding = PaddingValues(bottom = 60.dp)
                                ) {
                                    items(threadPosts) { post ->
                                        ThreadPostCard(
                                            post = post,
                                            threadsSurfaceColor = threadsSurfaceColor,
                                            threadsTextColor = threadsTextColor,
                                            threadsSubTextColor = threadsSubTextColor,
                                            threadsBorderColor = threadsBorderColor,
                                            primaryColor = primaryColor,
                                            onLike = {
                                                post.userLiked = !post.userLiked
                                                post.likesCount += if (post.userLiked) 1 else -1
                                            },
                                            onRepost = {
                                                post.userReposted = !post.userReposted
                                                post.repostsCount += if (post.userReposted) 1 else -1
                                                android.widget.Toast.makeText(context, if (post.userReposted) "Đã đăng lại lên Threads cá nhân!" else "Đã hủy đăng lại!", android.widget.Toast.LENGTH_SHORT).show()
                                            },
                                            onSave = {
                                                post.userSaved = !post.userSaved
                                                android.widget.Toast.makeText(context, if (post.userSaved) "Đã lưu bài viết vào hồ sơ!" else "Đã xóa bài viết khỏi danh sách lưu!", android.widget.Toast.LENGTH_SHORT).show()
                                            },
                                            onCommentClick = {
                                                activePostForComments = post
                                            },
                                            onTranslateClick = {
                                                activePostForTranslate = post
                                                coroutineScope.launch {
                                                    isGeneratingAI = true
                                                    val trans = GeminiService.generateResponse(
                                                        "Dịch câu sau sang tiếng Anh lịch sự, tự nhiên, kiểu mxh Threads: \"${post.content}\""
                                                    )
                                                    post.content = "$trans\n\n🌐 *[Dịch bởi AI]*"
                                                    isGeneratingAI = false
                                                    android.widget.Toast.makeText(context, "Dịch AI hoàn tất!", android.widget.Toast.LENGTH_SHORT).show()
                                                }
                                            },
                                            onSummarizeClick = {
                                                coroutineScope.launch {
                                                    isGeneratingAI = true
                                                    val summary = GeminiService.generateResponse(
                                                        "Tóm tắt ngắn gọn bài đăng Threads sau bằng 1 dòng cực chất, trẻ trung: \"${post.content}\""
                                                    )
                                                    isGeneratingAI = false
                                                    AlertDialogBuilder(context, "AI Tóm Tắt 🤖", summary)
                                                }
                                            },
                                            onImageClick = { color ->
                                                imageFullscreenUrl = color
                                            }
                                        )
                                    }
                                }
                            }
                        }
                        "Search" -> {
                            // --- THREADS SEARCH SYSTEM ---
                            var searchQueryText by remember { mutableStateOf("") }
                            val filteredSearchPosts = remember(searchQueryText) {
                                if (searchQueryText.isBlank()) threadPosts else threadPosts.filter {
                                    it.content.contains(searchQueryText, ignoreCase = true) ||
                                            it.authorName.contains(searchQueryText, ignoreCase = true) ||
                                            it.authorUsername.contains(searchQueryText, ignoreCase = true)
                                }
                            }

                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                OutlinedTextField(
                                    value = searchQueryText,
                                    onValueChange = { searchQueryText = it },
                                    label = { Text("Tìm kiếm Threads, hashtags, người dùng...") },
                                    leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null, tint = threadsSubTextColor) },
                                    trailingIcon = {
                                        if (searchQueryText.isNotEmpty()) {
                                            IconButton(onClick = { searchQueryText = "" }) {
                                                Icon(Icons.Filled.Close, contentDescription = null, tint = threadsTextColor)
                                            }
                                        }
                                    },
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedTextColor = threadsTextColor,
                                        unfocusedTextColor = threadsTextColor,
                                        focusedBorderColor = primaryColor,
                                        unfocusedBorderColor = threadsBorderColor
                                    ),
                                    modifier = Modifier.fillMaxWidth(),
                                    singleLine = true
                                )

                                if (searchQueryText.isEmpty()) {
                                    // Trending topics & search history
                                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                        Text("Xu Hướng Chủ Đề 🔥", color = threadsTextColor, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                        Row(
                                            modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            listOf("#ShopeeLive", "#GlowGlowSon", "#KOC_ChotDeal", "#ThoiTrangThuDong", "#LunarTech耳机").forEach { hashtag ->
                                                Box(
                                                    modifier = Modifier
                                                        .clip(RoundedCornerShape(16.dp))
                                                        .background(threadsSurfaceColor)
                                                        .border(1.dp, threadsBorderColor, RoundedCornerShape(16.dp))
                                                        .clickable { searchQueryText = hashtag.replace("#", "") }
                                                        .padding(horizontal = 12.dp, vertical = 6.dp)
                                                ) {
                                                    Text(hashtag, color = primaryColor, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                                }
                                            }
                                        }
                                    }

                                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text("Lịch sử tìm kiếm 🕒", color = threadsTextColor, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                            TextButton(onClick = { searchHistory.clear() }) {
                                                Text("Xóa hết", color = primaryColor, fontSize = 12.sp)
                                            }
                                        }
                                        if (searchHistory.isEmpty()) {
                                            Text("Chưa có lịch sử tìm kiếm nào.", color = threadsSubTextColor, fontSize = 11.sp)
                                        } else {
                                            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                                items(searchHistory) { history ->
                                                    Box(
                                                        modifier = Modifier
                                                            .clip(RoundedCornerShape(8.dp))
                                                            .background(threadsSurfaceColor)
                                                            .clickable { searchQueryText = history }
                                                            .padding(horizontal = 10.dp, vertical = 6.dp)
                                                    ) {
                                                        Text(history, color = threadsTextColor, fontSize = 11.sp)
                                                    }
                                                }
                                            }
                                        }
                                    }

                                    // Suggested creators to follow
                                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                        Text("Gợi Ý Theo Dõi 👥", color = threadsTextColor, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                        suggestedFollows.forEachIndexed { index, account ->
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .clip(RoundedCornerShape(12.dp))
                                                    .background(threadsSurfaceColor)
                                                    .padding(10.dp),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Box(
                                                        modifier = Modifier
                                                            .size(40.dp)
                                                            .clip(CircleShape)
                                                            .background(primaryColor),
                                                        contentAlignment = Alignment.Center
                                                    ) {
                                                        Text(account["avatar"]!!, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                                    }
                                                    Spacer(modifier = Modifier.width(10.dp))
                                                    Column {
                                                        Text(account["name"]!!, color = threadsTextColor, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                                        Text("@${account["username"]!!}", color = threadsSubTextColor, fontSize = 10.sp)
                                                    }
                                                }

                                                val isFollowed = account["isFollowed"] == "true"
                                                Button(
                                                    onClick = {
                                                        val updatedMap = account.toMutableMap()
                                                        if (isFollowed) {
                                                            updatedMap["isFollowed"] = "false"
                                                            android.widget.Toast.makeText(context, "Đã hủy theo dõi ${account["name"]}", android.widget.Toast.LENGTH_SHORT).show()
                                                        } else {
                                                            updatedMap["isFollowed"] = "true"
                                                            android.widget.Toast.makeText(context, "Đã theo dõi ${account["name"]}", android.widget.Toast.LENGTH_SHORT).show()
                                                        }
                                                        suggestedFollows[index] = updatedMap
                                                    },
                                                    colors = ButtonDefaults.buttonColors(
                                                        containerColor = if (isFollowed) Color.Gray else primaryColor
                                                    ),
                                                    shape = RoundedCornerShape(8.dp),
                                                    contentPadding = PaddingValues(horizontal = 12.dp),
                                                    modifier = Modifier.height(30.dp)
                                                ) {
                                                    Text(if (isFollowed) "Đang follow" else "Follow", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                                }
                                            }
                                        }
                                    }
                                } else {
                                    // Search Results list
                                    Text("Kết quả tìm kiếm cho: \"$searchQueryText\"", color = threadsSubTextColor, fontSize = 12.sp)
                                    LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.weight(1f)) {
                                        items(filteredSearchPosts) { post ->
                                            ThreadPostCard(
                                                post = post,
                                                threadsSurfaceColor = threadsSurfaceColor,
                                                threadsTextColor = threadsTextColor,
                                                threadsSubTextColor = threadsSubTextColor,
                                                threadsBorderColor = threadsBorderColor,
                                                primaryColor = primaryColor,
                                                onLike = { post.likesCount++ },
                                                onRepost = {},
                                                onSave = {},
                                                onCommentClick = { activePostForComments = post },
                                                onTranslateClick = {},
                                                onSummarizeClick = {},
                                                onImageClick = {}
                                            )
                                        }
                                    }
                                }
                            }
                        }
                        "Create" -> {
                            // --- THREADS POST CREATION SYSTEM ---
                            var postText by remember { mutableStateOf("") }
                            var inputLocation by remember { mutableStateOf("") }
                            var showPollBuilder by remember { mutableStateOf(false) }

                            // Poll builder parameters
                            var pollQues by remember { mutableStateOf("") }
                            var pollOpt1 by remember { mutableStateOf("") }
                            var pollOpt2 by remember { mutableStateOf("") }

                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .verticalScroll(rememberScrollState())
                                    .padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("Tạo Thread Mới ✍️", color = threadsTextColor, fontWeight = FontWeight.Black, fontSize = 18.sp)
                                    Text("${postText.length}/500", color = if (postText.length > 500) Color.Red else threadsSubTextColor, fontSize = 12.sp)
                                }

                                Row(verticalAlignment = Alignment.Top) {
                                    Box(
                                        modifier = Modifier
                                            .size(40.dp)
                                            .clip(CircleShape)
                                            .background(primaryColor),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(loggedInUser["avatar"]!!, color = Color.White, fontWeight = FontWeight.Bold)
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                        Text(loggedInUser["name"]!!, color = threadsTextColor, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                        
                                        OutlinedTextField(
                                            value = postText,
                                            onValueChange = { if (it.length <= 600) postText = it },
                                            placeholder = { Text("Có gì mới? Chia sẻ ý kiến, sản phẩm, tin đồn...") },
                                            colors = OutlinedTextFieldDefaults.colors(
                                                focusedTextColor = threadsTextColor,
                                                unfocusedTextColor = threadsTextColor,
                                                focusedBorderColor = primaryColor,
                                                unfocusedBorderColor = Color.Transparent
                                            ),
                                            modifier = Modifier.fillMaxWidth().height(120.dp),
                                            singleLine = false
                                        )
                                    }
                                }

                                // Interactive Poll Builder inside Post Creator
                                if (showPollBuilder) {
                                    Card(
                                        colors = CardDefaults.cardColors(containerColor = threadsSurfaceColor),
                                        border = BorderStroke(1.dp, threadsBorderColor),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                            Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                                                Text("Thiết lập bình chọn 📊", color = threadsTextColor, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                                IconButton(onClick = { showPollBuilder = false }, modifier = Modifier.size(24.dp)) {
                                                    Icon(Icons.Filled.Close, contentDescription = null, tint = Color.Red, modifier = Modifier.size(16.dp))
                                                }
                                            }
                                            OutlinedTextField(
                                                value = pollQues,
                                                onValueChange = { pollQues = it },
                                                label = { Text("Câu hỏi") },
                                                colors = OutlinedTextFieldDefaults.colors(focusedTextColor = threadsTextColor, unfocusedTextColor = threadsTextColor),
                                                modifier = Modifier.fillMaxWidth()
                                            )
                                            OutlinedTextField(
                                                value = pollOpt1,
                                                onValueChange = { pollOpt1 = it },
                                                label = { Text("Lựa chọn 1") },
                                                colors = OutlinedTextFieldDefaults.colors(focusedTextColor = threadsTextColor, unfocusedTextColor = threadsTextColor),
                                                modifier = Modifier.fillMaxWidth()
                                            )
                                            OutlinedTextField(
                                                value = pollOpt2,
                                                onValueChange = { pollOpt2 = it },
                                                label = { Text("Lựa chọn 2") },
                                                colors = OutlinedTextFieldDefaults.colors(focusedTextColor = threadsTextColor, unfocusedTextColor = threadsTextColor),
                                                modifier = Modifier.fillMaxWidth()
                                            )
                                        }
                                    }
                                }

                                OutlinedTextField(
                                    value = inputLocation,
                                    onValueChange = { inputLocation = it },
                                    label = { Text("Thêm vị trí phát hành (Tùy chọn)") },
                                    leadingIcon = { Icon(Icons.Filled.LocationOn, contentDescription = null, tint = primaryColor) },
                                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = threadsTextColor, unfocusedTextColor = threadsTextColor),
                                    modifier = Modifier.fillMaxWidth(),
                                    singleLine = true
                                )

                                // AI WRITING COMPANION SUITE
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = primaryColor.copy(alpha = 0.08f)),
                                    border = BorderStroke(1.dp, primaryColor.copy(alpha = 0.3f)),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                        Text("AI Trợ Lý Viết Bài ✨🤖", color = primaryColor, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                        
                                        OutlinedTextField(
                                            value = aiWritingPrompt,
                                            onValueChange = { aiWritingPrompt = it },
                                            placeholder = { Text("Mô tả ý tưởng (ví dụ: viết bài sale tai nghe cực hot)...") },
                                            colors = OutlinedTextFieldDefaults.colors(focusedTextColor = threadsTextColor, unfocusedTextColor = threadsTextColor),
                                            modifier = Modifier.fillMaxWidth().height(54.dp),
                                            singleLine = true
                                        )

                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Button(
                                                onClick = {
                                                    if (aiWritingPrompt.isNotBlank()) {
                                                        coroutineScope.launch {
                                                            isGeneratingAI = true
                                                            val aiRes = GeminiService.generateResponse(
                                                                "Hãy đóng vai là một nhà sáng tạo nội dung cực trẻ trung trên mxh Threads. Viết bài đăng Threads dài khoảng 2-3 câu kêu gọi mọi người mua sản phẩm về: \"$aiWritingPrompt\". Nhớ kèm emoji sinh động và các hashtag liên quan!"
                                                            )
                                                            postText = aiRes
                                                            isGeneratingAI = false
                                                        }
                                                    }
                                                },
                                                modifier = Modifier.weight(1f),
                                                colors = ButtonDefaults.buttonColors(containerColor = primaryColor),
                                                shape = RoundedCornerShape(8.dp)
                                            ) {
                                                Text("AI Viết Hộ 🚀", fontSize = 11.sp, color = Color.White)
                                            }

                                            Button(
                                                onClick = {
                                                    if (postText.isNotBlank()) {
                                                        coroutineScope.launch {
                                                            isGeneratingAI = true
                                                            val aiRes = GeminiService.generateResponse(
                                                                "Sửa chính tả, định dạng thêm các hashtag hot và emojis cho bài viết sau để đăng mxh Threads thật cuốn hút: \"$postText\""
                                                            )
                                                            postText = aiRes
                                                            isGeneratingAI = false
                                                        }
                                                    }
                                                },
                                                modifier = Modifier.weight(1f),
                                                colors = ButtonDefaults.buttonColors(containerColor = threadsSurfaceColor),
                                                border = BorderStroke(1.dp, threadsBorderColor),
                                                shape = RoundedCornerShape(8.dp)
                                            ) {
                                                Text("AI Sửa Lỗi + Hashtag", fontSize = 11.sp, color = threadsTextColor)
                                            }
                                        }
                                    }
                                }

                                // Interactive attachment icons row
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                                        IconButton(onClick = {
                                            android.widget.Toast.makeText(context, "Chọn nhiều ảnh mẫu thành công!", android.widget.Toast.LENGTH_SHORT).show()
                                        }) { Icon(Icons.Filled.Image, contentDescription = null, tint = Color.Green) }
                                        
                                        IconButton(onClick = {
                                            android.widget.Toast.makeText(context, "Chọn video clip phát trực quan!", android.widget.Toast.LENGTH_SHORT).show()
                                        }) { Icon(Icons.Filled.Movie, contentDescription = null, tint = Color.Blue) }
                                        
                                        IconButton(onClick = { showPollBuilder = true }) {
                                            Icon(Icons.Filled.Poll, contentDescription = null, tint = Color.Cyan)
                                        }

                                        IconButton(onClick = {
                                            postText += " 😍🔥🛍️"
                                        }) { Icon(Icons.Filled.EmojiEmotions, contentDescription = null, tint = Color.Yellow) }
                                    }

                                    // Action buttons for draft & Post
                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        Button(
                                            onClick = {
                                                draftPostContent.value = postText
                                                android.widget.Toast.makeText(context, "Đã lưu nháp thành công!", android.widget.Toast.LENGTH_SHORT).show()
                                            },
                                            colors = ButtonDefaults.buttonColors(containerColor = threadsSurfaceColor),
                                            border = BorderStroke(1.dp, threadsBorderColor),
                                            shape = RoundedCornerShape(8.dp)
                                        ) {
                                            Text("Lưu nháp", color = threadsTextColor, fontSize = 11.sp)
                                        }

                                        Button(
                                            onClick = {
                                                if (postText.isNotBlank()) {
                                                    val finalPoll = if (showPollBuilder && pollQues.isNotBlank()) {
                                                        ThreadPoll("poll_${System.currentTimeMillis()}", pollQues, listOf(pollOpt1, pollOpt2), mutableListOf(0, 0))
                                                    } else null

                                                    val newThread = ThreadPost(
                                                        id = "thread_${System.currentTimeMillis()}",
                                                        authorName = loggedInUser["name"]!!,
                                                        authorUsername = loggedInUser["username"]!!,
                                                        authorAvatar = loggedInUser["avatar"]!!,
                                                        isVerified = true,
                                                        content = postText,
                                                        timestamp = "Vừa xong",
                                                        likesCount = 0,
                                                        commentsCount = 0,
                                                        location = if (inputLocation.isNotBlank()) inputLocation else null,
                                                        poll = finalPoll
                                                    )

                                                    threadPosts.add(0, newThread)
                                                    postText = ""
                                                    inputLocation = ""
                                                    showPollBuilder = false
                                                    pollQues = ""
                                                    pollOpt1 = ""
                                                    pollOpt2 = ""
                                                    currentSubTab = "Home"
                                                    android.widget.Toast.makeText(context, "Đã đăng bài viết mới lên Threads! 🚀", android.widget.Toast.LENGTH_LONG).show()
                                                }
                                            },
                                            colors = ButtonDefaults.buttonColors(containerColor = primaryColor),
                                            shape = RoundedCornerShape(8.dp),
                                            enabled = postText.isNotBlank() && postText.length <= 500
                                        ) {
                                            Text("Đăng", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                        }
                                    }
                                }
                            }
                        }
                        "Notification" -> {
                            // --- THREADS NOTIFICATION / ACTIVITY ---
                            var activeNotificationFilter by remember { mutableStateOf("all") } // "all", "replies", "mentions", "likes"
                            val filteredNotifications = remember(activeNotificationFilter) {
                                if (activeNotificationFilter == "all") notifications else notifications.filter { it.type == activeNotificationFilter }
                            }

                            Column(modifier = Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                                Text("Hoạt Động / Thông Báo 🔔", color = threadsTextColor, fontWeight = FontWeight.Black, fontSize = 18.sp)

                                // Filters
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    listOf("all" to "Tất cả", "replies" to "Phản hồi", "mentions" to "Lượt nhắc", "like" to "Thích").forEach { (filterType, label) ->
                                        val isSelected = activeNotificationFilter == filterType
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(12.dp))
                                                .background(if (isSelected) primaryColor else threadsSurfaceColor)
                                                .clickable { activeNotificationFilter = filterType }
                                                .padding(horizontal = 12.dp, vertical = 6.dp)
                                        ) {
                                            Text(label, color = if (isSelected) Color.White else threadsTextColor, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }

                                Divider(color = threadsBorderColor, thickness = 0.5.dp)

                                LazyColumn(
                                    verticalArrangement = Arrangement.spacedBy(12.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    itemsIndexed(filteredNotifications) { index, notify ->
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clip(RoundedCornerShape(12.dp))
                                                .background(threadsSurfaceColor)
                                                .padding(12.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(36.dp)
                                                        .clip(CircleShape)
                                                        .background(primaryColor),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Text(notify.senderAvatar, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                                }
                                                Spacer(modifier = Modifier.width(12.dp))
                                                Column {
                                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                                        Text(notify.senderName, color = threadsTextColor, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                                        Spacer(modifier = Modifier.width(6.dp))
                                                        Text(notify.timestamp, color = threadsSubTextColor, fontSize = 9.sp)
                                                    }
                                                    Text(notify.content, color = threadsTextColor, fontSize = 11.sp)
                                                }
                                            }

                                            if (notify.type == "follow") {
                                                Button(
                                                    onClick = {
                                                        val updated = notify.copy(actionCompleted = true)
                                                        notifications[index] = updated
                                                        android.widget.Toast.makeText(context, "Đã đồng ý follow chéo!", android.widget.Toast.LENGTH_SHORT).show()
                                                    },
                                                    colors = ButtonDefaults.buttonColors(
                                                        containerColor = if (notify.actionCompleted) Color.Gray else primaryColor
                                                    ),
                                                    shape = RoundedCornerShape(8.dp),
                                                    contentPadding = PaddingValues(horizontal = 12.dp),
                                                    modifier = Modifier.height(28.dp)
                                                ) {
                                                    Text(if (notify.actionCompleted) "Đã theo dõi" else "Xác nhận", fontSize = 10.sp, color = Color.White)
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        "Profile" -> {
                            // --- THREADS USER PROFILE SCREEN ---
                            var activeProfileTab by remember { mutableStateOf("Threads") } // "Threads", "Replies", "Likes", "Saved"
                            var showEditProfileDialog by remember { mutableStateOf(false) }

                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .verticalScroll(rememberScrollState())
                                    .padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                // Profile Details Header
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(loggedInUser["name"]!!, color = threadsTextColor, fontWeight = FontWeight.Black, fontSize = 22.sp)
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Icon(Icons.Filled.Verified, contentDescription = null, tint = Color(0xFF2196F3), modifier = Modifier.size(18.dp))
                                        }
                                        Text("@${loggedInUser["username"]!!}", color = threadsTextColor, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                                        Text(loggedInUser["bio"]!!, color = threadsTextColor, fontSize = 12.sp)
                                        Text(loggedInUser["website"]!!, color = primaryColor, fontSize = 11.sp, textDecoration = TextDecoration.Underline)
                                    }

                                    Box(
                                        modifier = Modifier
                                            .size(72.dp)
                                            .clip(CircleShape)
                                            .background(primaryColor),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(loggedInUser["avatar"]!!, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 26.sp)
                                    }
                                }

                                Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                                    Text("${loggedInUser["followers"]!!} người theo dõi", color = threadsSubTextColor, fontSize = 11.sp)
                                    Text("•", color = threadsSubTextColor, fontSize = 11.sp)
                                    Text("${loggedInUser["following"]!!} đang theo dõi", color = threadsSubTextColor, fontSize = 11.sp)
                                }

                                // Interactive Follow private setting
                                var isPrivateFollowEnabled by remember { mutableStateOf(false) }
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(threadsSurfaceColor)
                                        .padding(horizontal = 12.dp, vertical = 6.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("Bảo mật theo dõi riêng tư (Private):", color = threadsTextColor, fontSize = 11.sp)
                                    Switch(
                                        checked = isPrivateFollowEnabled,
                                        onCheckedChange = { isPrivateFollowEnabled = it },
                                        colors = SwitchDefaults.colors(checkedIconColor = primaryColor)
                                    )
                                }

                                // Edit Profile action button
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Button(
                                        onClick = { showEditProfileDialog = true },
                                        colors = ButtonDefaults.buttonColors(containerColor = threadsSurfaceColor),
                                        border = BorderStroke(1.dp, threadsBorderColor),
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text("Chỉnh sửa hồ sơ ⚙️", color = threadsTextColor, fontSize = 12.sp)
                                    }

                                    Button(
                                        onClick = { currentSubTab = "Settings" },
                                        colors = ButtonDefaults.buttonColors(containerColor = threadsSurfaceColor),
                                        border = BorderStroke(1.dp, threadsBorderColor),
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text("Cài đặt hệ thống 🔒", color = threadsTextColor, fontSize = 12.sp)
                                    }
                                }

                                Divider(color = threadsBorderColor, thickness = 0.5.dp)

                                // Profile tabs
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    listOf("Threads", "Replies", "Likes", "Saved").forEach { pTab ->
                                        val isSel = activeProfileTab == pTab
                                        Column(
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            modifier = Modifier
                                                .weight(1f)
                                                .clickable { activeProfileTab = pTab }
                                                .padding(vertical = 8.dp)
                                        ) {
                                            Text(
                                                pTab,
                                                color = if (isSel) primaryColor else threadsTextColor,
                                                fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal,
                                                fontSize = 12.sp
                                            )
                                            if (isSel) {
                                                Box(modifier = Modifier.size(24.dp, 2.dp).background(primaryColor))
                                            }
                                        }
                                    }
                                }

                                // Tabs rendering content
                                Column(verticalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                                    val profileDisplayThreads = when (activeProfileTab) {
                                        "Threads" -> threadPosts.filter { it.authorUsername == loggedInUser["username"] }
                                        "Saved" -> threadPosts.filter { it.userSaved }
                                        "Likes" -> threadPosts.filter { it.userLiked }
                                        else -> emptyList()
                                    }

                                    if (profileDisplayThreads.isEmpty()) {
                                        Box(
                                            modifier = Modifier.fillMaxWidth().height(140.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text("Chưa có bài viết nào trong mục này.", color = threadsSubTextColor, fontSize = 12.sp)
                                        }
                                    } else {
                                        profileDisplayThreads.forEach { post ->
                                            ThreadPostCard(
                                                post = post,
                                                threadsSurfaceColor = threadsSurfaceColor,
                                                threadsTextColor = threadsTextColor,
                                                threadsSubTextColor = threadsSubTextColor,
                                                threadsBorderColor = threadsBorderColor,
                                                primaryColor = primaryColor,
                                                onLike = { post.likesCount++ },
                                                onRepost = {},
                                                onSave = {},
                                                onCommentClick = { activePostForComments = post },
                                                onTranslateClick = {},
                                                onSummarizeClick = {},
                                                onImageClick = {}
                                            )
                                        }
                                    }
                                }
                            }

                            // PROFILE EDITING DIALOG
                            if (showEditProfileDialog) {
                                var editName by remember { mutableStateOf(loggedInUser["name"]!!) }
                                var editBio by remember { mutableStateOf(loggedInUser["bio"]!!) }
                                var editWeb by remember { mutableStateOf(loggedInUser["website"]!!) }

                                AlertDialog(
                                    onDismissRequest = { showEditProfileDialog = false },
                                    title = { Text("Chỉnh sửa hồ sơ cá nhân", color = Color.White, fontWeight = FontWeight.Bold) },
                                    text = {
                                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                            OutlinedTextField(
                                                value = editName,
                                                onValueChange = { editName = it },
                                                label = { Text("Họ & Tên") },
                                                colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White),
                                                modifier = Modifier.fillMaxWidth()
                                            )
                                            OutlinedTextField(
                                                value = editBio,
                                                onValueChange = { editBio = it },
                                                label = { Text("Bio / Giới thiệu") },
                                                colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White),
                                                modifier = Modifier.fillMaxWidth()
                                            )
                                            OutlinedTextField(
                                                value = editWeb,
                                                onValueChange = { editWeb = it },
                                                label = { Text("Website Link") },
                                                colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White),
                                                modifier = Modifier.fillMaxWidth()
                                            )
                                        }
                                    },
                                    confirmButton = {
                                        Button(
                                            onClick = {
                                                loggedInUser = loggedInUser.toMutableMap().apply {
                                                    this["name"] = editName
                                                    this["bio"] = editBio
                                                    this["website"] = editWeb
                                                }
                                                showEditProfileDialog = false
                                                android.widget.Toast.makeText(context, "Hồ sơ đã được cập nhật thành công!", android.widget.Toast.LENGTH_SHORT).show()
                                            },
                                            colors = ButtonDefaults.buttonColors(containerColor = primaryColor)
                                        ) {
                                            Text("Lưu Thay Đổi", color = Color.White)
                                        }
                                    },
                                    dismissButton = {
                                        TextButton(onClick = { showEditProfileDialog = false }) { Text("Hủy", color = Color.Gray) }
                                    },
                                    containerColor = Color(0xFF1E1E1E)
                                )
                            }
                        }
                        "Messages" -> {
                            // --- THREADS MESSAGING SYSTEM ---
                            if (activeChatId == null) {
                                // Chats Hub Screen List
                                Column(modifier = Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                                    Text("Tin Nhắn Cá Nhân & Nhóm 💬", color = threadsTextColor, fontWeight = FontWeight.Black, fontSize = 18.sp)
                                    
                                    LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.weight(1f)) {
                                        items(directChats) { chat ->
                                            val lastMsg = chat.messages.lastOrNull()
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .clip(RoundedCornerShape(12.dp))
                                                    .background(threadsSurfaceColor)
                                                    .clickable { activeChatId = chat.id }
                                                    .padding(12.dp),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                                    Box(
                                                        modifier = Modifier
                                                            .size(44.dp)
                                                            .clip(CircleShape)
                                                            .background(Color(chat.avatarColor)),
                                                        contentAlignment = Alignment.Center
                                                    ) {
                                                        Text(chat.chatName.take(2).uppercase(), color = Color.White, fontWeight = FontWeight.Bold)
                                                    }
                                                    Spacer(modifier = Modifier.width(12.dp))
                                                    Column {
                                                        Text(chat.chatName, color = threadsTextColor, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                                        Text(
                                                            text = if (chat.isTyping) "Đang nhập tin..." else lastMsg?.content ?: "Chưa có tin nhắn mới",
                                                            color = if (chat.isTyping) Color.Green else threadsSubTextColor,
                                                            fontSize = 11.sp,
                                                            maxLines = 1,
                                                            overflow = TextOverflow.Ellipsis
                                                        )
                                                    }
                                                }
                                                
                                                Column(horizontalAlignment = Alignment.End) {
                                                    Text(lastMsg?.timestamp ?: "", color = threadsSubTextColor, fontSize = 9.sp)
                                                    if (lastMsg?.isSeen == false) {
                                                        Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(primaryColor))
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            } else {
                                // ACTIVE CHAT WINDOW SCREEN
                                val activeChat = directChats.find { it.id == activeChatId }!!
                                var messageText by remember { mutableStateOf("") }

                                Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                                    // Chat Header
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            IconButton(onClick = { activeChatId = null }) {
                                                Icon(Icons.Filled.ArrowBack, contentDescription = null, tint = threadsTextColor)
                                            }
                                            Box(
                                                modifier = Modifier
                                                    .size(36.dp)
                                                    .clip(CircleShape)
                                                    .background(Color(activeChat.avatarColor)),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(activeChat.chatName.take(2).uppercase(), color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                            }
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Column {
                                                Text(activeChat.chatName, color = threadsTextColor, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                                Text(if (activeChat.isTyping) "Đang soạn tin nhắn..." else "Trực tuyến", color = if (activeChat.isTyping) Color.Green else threadsSubTextColor, fontSize = 10.sp)
                                            }
                                        }

                                        IconButton(onClick = {
                                            android.widget.Toast.makeText(context, "Thực hiện cuộc gọi trực tiếp!", android.widget.Toast.LENGTH_SHORT).show()
                                        }) {
                                            Icon(Icons.Filled.Phone, contentDescription = null, tint = primaryColor)
                                        }
                                    }

                                    Divider(color = threadsBorderColor, thickness = 0.5.dp, modifier = Modifier.padding(vertical = 8.dp))

                                    // Chat Messages list scroll view
                                    LazyColumn(
                                        modifier = Modifier.weight(1f),
                                        verticalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        items(activeChat.messages) { msg ->
                                            val isUser = msg.sender == "You"
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
                                            ) {
                                                Card(
                                                    colors = CardDefaults.cardColors(
                                                        containerColor = if (isUser) primaryColor else threadsSurfaceColor
                                                    ),
                                                    shape = RoundedCornerShape(12.dp)
                                                ) {
                                                    Column(modifier = Modifier.padding(10.dp)) {
                                                        if (!isUser) {
                                                            Text(msg.sender, color = primaryColor, fontWeight = FontWeight.Bold, fontSize = 9.sp)
                                                        }
                                                        Text(msg.content, color = if (isUser) Color.White else threadsTextColor, fontSize = 12.sp)
                                                        Spacer(modifier = Modifier.height(2.dp))
                                                        Row(
                                                            verticalAlignment = Alignment.CenterVertically,
                                                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                                                        ) {
                                                            Text(msg.timestamp, color = if (isUser) Color.White.copy(alpha = 0.7f) else threadsSubTextColor, fontSize = 8.sp)
                                                            if (isUser) {
                                                                Icon(Icons.Filled.CheckCircle, contentDescription = null, tint = Color.White.copy(alpha = 0.8f), modifier = Modifier.size(10.dp))
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }

                                    // Input controls
                                    Row(
                                        modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        OutlinedTextField(
                                            value = messageText,
                                            onValueChange = { messageText = it },
                                            placeholder = { Text("Nhập tin nhắn...") },
                                            colors = OutlinedTextFieldDefaults.colors(focusedTextColor = threadsTextColor, unfocusedTextColor = threadsTextColor),
                                            modifier = Modifier.weight(1f).height(48.dp),
                                            shape = RoundedCornerShape(24.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        IconButton(onClick = {
                                            if (messageText.isNotBlank()) {
                                                val newMsg = DirectMessage("msg_${System.currentTimeMillis()}", "You", messageText, "Vừa xong")
                                                activeChat.messages.add(newMsg)
                                                messageText = ""

                                                // Trigger AI auto responder simulation after brief delay
                                                coroutineScope.launch {
                                                    activeChat.isTyping = true
                                                    delay(2500)
                                                    activeChat.isTyping = false
                                                    val aiReplyStr = GeminiService.generateResponse(
                                                        "Bạn là ${activeChat.chatName}. Hãy soạn tin nhắn trả lời siêu thân thiện, ngắn gọn và có tâm cho tin nhắn vừa nhận từ khách hàng: \"${newMsg.content}\" trên ứng dụng mua sắm."
                                                    )
                                                    activeChat.messages.add(DirectMessage("msg_ai_${System.currentTimeMillis()}", activeChat.chatName, aiReplyStr, "Vừa xong"))
                                                }
                                            }
                                        }) {
                                            Icon(Icons.Filled.Send, contentDescription = null, tint = primaryColor)
                                        }
                                    }
                                }
                            }
                        }
                        "Settings" -> {
                            // --- THREADS DETAILED SETTINGS SCREEN ---
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .verticalScroll(rememberScrollState())
                                    .padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                Text("Cài Đặt Hệ Thống & Bảo Mật 🔒", color = threadsTextColor, fontWeight = FontWeight.Black, fontSize = 18.sp)

                                // 2FA toggle
                                Row(
                                    modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(8.dp)).background(threadsSurfaceColor).padding(12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text("Bảo mật 2 lớp (2FA)", color = threadsTextColor, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                        Text("Xác minh danh tính qua OTP điện thoại khi đăng nhập", color = threadsSubTextColor, fontSize = 10.sp)
                                    }
                                    Switch(checked = is2FAEnabled, onCheckedChange = { is2FAEnabled = it })
                                }

                                // Dark mode toggle
                                Row(
                                    modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(8.dp)).background(threadsSurfaceColor).padding(12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text("Giao diện tối (Dark Mode)", color = threadsTextColor, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                        Text("Tiết kiệm pin & thân thiện với mắt ban đêm", color = threadsSubTextColor, fontSize = 10.sp)
                                    }
                                    Switch(checked = isDarkMode, onCheckedChange = { isDarkMode = it })
                                }

                                // Language Selector
                                Row(
                                    modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(8.dp)).background(threadsSurfaceColor).padding(12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text("Ngôn ngữ (Language)", color = threadsTextColor, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                        Text(if (currentLanguage == "vi") "Tiếng Việt" else "English", color = threadsSubTextColor, fontSize = 10.sp)
                                    }
                                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                        TextButton(onClick = { currentLanguage = "vi" }) { Text("VI", color = if (currentLanguage == "vi") primaryColor else threadsTextColor) }
                                        TextButton(onClick = { currentLanguage = "en" }) { Text("EN", color = if (currentLanguage == "en") primaryColor else threadsTextColor) }
                                    }
                                }

                                // Devices & Login logs simulation
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = threadsSurfaceColor),
                                    border = BorderStroke(1.dp, threadsBorderColor)
                                ) {
                                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                        Text("Nhật ký thiết bị đăng nhập 📱", color = threadsTextColor, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                        Text("• Android Emulator (Thiết bị này) - Active Now", color = Color.Green, fontSize = 11.sp)
                                        Text("• Web Chrome - Hà Nội, Việt Nam - 2 giờ trước", color = threadsSubTextColor, fontSize = 11.sp)
                                    }
                                }

                                // Password Changer simulation
                                var passChg1 by remember { mutableStateOf("") }
                                var passChg2 by remember { mutableStateOf("") }
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = threadsSurfaceColor),
                                    border = BorderStroke(1.dp, threadsBorderColor)
                                ) {
                                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                        Text("Đổi mật khẩu bảo mật 🔑", color = threadsTextColor, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                        OutlinedTextField(
                                            value = passChg1,
                                            onValueChange = { passChg1 = it },
                                            placeholder = { Text("Mật khẩu mới") },
                                            colors = OutlinedTextFieldDefaults.colors(focusedTextColor = threadsTextColor, unfocusedTextColor = threadsTextColor),
                                            modifier = Modifier.fillMaxWidth()
                                        )
                                        OutlinedTextField(
                                            value = passChg2,
                                            onValueChange = { passChg2 = it },
                                            placeholder = { Text("Xác nhận mật khẩu mới") },
                                            colors = OutlinedTextFieldDefaults.colors(focusedTextColor = threadsTextColor, unfocusedTextColor = threadsTextColor),
                                            modifier = Modifier.fillMaxWidth()
                                        )
                                        Button(
                                            onClick = {
                                                if (passChg1.isNotBlank() && passChg1 == passChg2) {
                                                    passChg1 = ""
                                                    passChg2 = ""
                                                    android.widget.Toast.makeText(context, "Mật khẩu đã được thay đổi an toàn!", android.widget.Toast.LENGTH_SHORT).show()
                                                } else {
                                                    android.widget.Toast.makeText(context, "Mật khẩu không khớp hoặc rỗng!", android.widget.Toast.LENGTH_SHORT).show()
                                                }
                                            },
                                            colors = ButtonDefaults.buttonColors(containerColor = primaryColor),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Text("Cập Nhật Mật Khẩu", color = Color.White)
                                        }
                                    }
                                }

                                Button(
                                    onClick = {
                                        isLoggedIn = false
                                        currentSubTab = "Home"
                                        android.widget.Toast.makeText(context, "Đã đăng xuất!", android.widget.Toast.LENGTH_SHORT).show()
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text("Đăng Xuất Tài Khoản 👋", color = Color.White, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                        "Admin" -> {
                            // --- ADMIN CONTROL & MODERATION DASHBOARD ---
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .verticalScroll(rememberScrollState())
                                    .padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                Text("Quản Trị Hệ Thống Admin 🛠️", color = threadsTextColor, fontWeight = FontWeight.Black, fontSize = 18.sp)

                                // Stats grid
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                                    Card(modifier = Modifier.weight(1f), colors = CardDefaults.cardColors(containerColor = threadsSurfaceColor)) {
                                        Column(modifier = Modifier.padding(12.dp)) {
                                            Text("Tổng Users", color = threadsSubTextColor, fontSize = 11.sp)
                                            Text("1,421", color = primaryColor, fontWeight = FontWeight.Black, fontSize = 18.sp)
                                        }
                                    }
                                    Card(modifier = Modifier.weight(1f), colors = CardDefaults.cardColors(containerColor = threadsSurfaceColor)) {
                                        Column(modifier = Modifier.padding(12.dp)) {
                                            Text("Bài Viết", color = threadsSubTextColor, fontSize = 11.sp)
                                            Text("4,892", color = threadsTextColor, fontWeight = FontWeight.Black, fontSize = 18.sp)
                                        }
                                    }
                                    Card(modifier = Modifier.weight(1f), colors = CardDefaults.cardColors(containerColor = threadsSurfaceColor)) {
                                        Column(modifier = Modifier.padding(12.dp)) {
                                            Text("Online", color = threadsSubTextColor, fontSize = 11.sp)
                                            Text("382", color = Color.Green, fontWeight = FontWeight.Black, fontSize = 18.sp)
                                        }
                                    }
                                }

                                // Security specifications list (XSS, JWT info)
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = threadsSurfaceColor),
                                    border = BorderStroke(1.dp, threadsBorderColor)
                                ) {
                                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                        Text("Thông số bảo mật hệ thống 🔒", color = threadsTextColor, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                        Text("• Mã hoá Token: JWT HS256", color = threadsTextColor, fontSize = 11.sp)
                                        Text("• Chống XSS: Bộ lọc HTML Content Purifier", color = threadsTextColor, fontSize = 11.sp)
                                        Text("• Chống CSRF: Token xác minh phiên động", color = threadsTextColor, fontSize = 11.sp)
                                        Text("• Chống SQL Injection: ORM Prisma Prepared Statements", color = threadsTextColor, fontSize = 11.sp)
                                    }
                                }

                                // Mod logs
                                Text("Báo Cáo Vi Phạm Đang Chờ 🚨", color = threadsTextColor, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                adminReports.forEachIndexed { idx, report ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(threadsSurfaceColor)
                                            .padding(12.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column {
                                            Text("Người bị báo cáo: ${report["reportedUser"]!!}", color = threadsTextColor, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                            Text("Lý do: ${report["reason"]!!}", color = threadsSubTextColor, fontSize = 11.sp)
                                            Text("Trạng thái: ${report["status"]!!}", color = if (report["status"] == "Đã xử lý") Color.Green else Color.Yellow, fontSize = 10.sp)
                                        }

                                        if (report["status"] != "Đã xử lý") {
                                            Button(
                                                onClick = {
                                                    val updated = report.toMutableMap()
                                                    updated["status"] = "Đã xử lý"
                                                    adminReports[idx] = updated
                                                    android.widget.Toast.makeText(context, "Đã ban/khoá người dùng vi phạm!", android.widget.Toast.LENGTH_SHORT).show()
                                                },
                                                colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
                                                contentPadding = PaddingValues(horizontal = 10.dp),
                                                modifier = Modifier.height(28.dp),
                                                shape = RoundedCornerShape(8.dp)
                                            ) {
                                                Text("Ban User", fontSize = 10.sp, color = Color.White)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // --- COMMENTS SHEETS DIALOG ---
    if (activePostForComments != null) {
        val post = activePostForComments!!
        var commentInputText by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { activePostForComments = null },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Luồng Ý Kiến (${post.comments.size}) 💬", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth().height(340.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // List of nested comments
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(post.comments) { comment ->
                            NestedCommentItem(comment = comment, depth = 0, primaryColor = primaryColor)
                        }
                    }

                    // Comment input
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = commentInputText,
                            onValueChange = { commentInputText = it },
                            placeholder = { Text("Bình luận ý kiến của bạn...") },
                            colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White),
                            modifier = Modifier.weight(1f).height(48.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        IconButton(onClick = {
                            if (commentInputText.isNotBlank()) {
                                val newComment = ThreadComment(
                                    id = "comment_${System.currentTimeMillis()}",
                                    authorName = loggedInUser["name"]!!,
                                    authorUsername = loggedInUser["username"]!!,
                                    authorAvatar = loggedInUser["avatar"]!!,
                                    content = commentInputText,
                                    timestamp = "Vừa xong"
                                )
                                post.comments.add(newComment)
                                commentInputText = ""
                                android.widget.Toast.makeText(context, "Bình luận của bạn đã được đăng tải!", android.widget.Toast.LENGTH_SHORT).show()
                            }
                        }) {
                            Icon(Icons.Filled.Send, contentDescription = null, tint = primaryColor)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { activePostForComments = null }) { Text("Đóng", color = Color.LightGray) }
            },
            containerColor = Color(0xFF1E1E1E)
        )
    }

    // --- FULLSCREEN IMAGE PREVIEW ---
    if (imageFullscreenUrl != null) {
        AlertDialog(
            onDismissRequest = { imageFullscreenUrl = null },
            confirmButton = {
                TextButton(onClick = { imageFullscreenUrl = null }) { Text("Đóng", color = Color.White) }
            },
            title = { Text("Ảnh Chi Tiết Threads 🖼️", color = Color.White) },
            text = {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(240.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.Black),
                    contentAlignment = Alignment.Center
                ) {
                    Text("[ Mock Image Fullscreen Preview ]", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            containerColor = Color(0xFF1E1E1E)
        )
    }

    // --- GLOBAL AI LOADER OVERLAY ---
    if (isGeneratingAI) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.6f)),
            contentAlignment = Alignment.Center
        ) {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF2C2C2C)),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    CircularProgressIndicator(color = primaryColor)
                    Text("AI đang xử lý thông minh... 🤖✨", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
            }
        }
    }
}

// --- COMPOSE HELPER VIEWS ---

@Composable
fun ThreadPostCard(
    post: ThreadPost,
    threadsSurfaceColor: Color,
    threadsTextColor: Color,
    threadsSubTextColor: Color,
    threadsBorderColor: Color,
    primaryColor: Color,
    onLike: () -> Unit,
    onRepost: () -> Unit,
    onSave: () -> Unit,
    onCommentClick: () -> Unit,
    onTranslateClick: () -> Unit,
    onSummarizeClick: () -> Unit,
    onImageClick: (String) -> Unit
) {
    var localLikes by remember { mutableStateOf(post.likesCount) }
    var userLikedLocal by remember { mutableStateOf(post.userLiked) }
    var isVideoPlaying by remember { mutableStateOf(false) }

    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = threadsSurfaceColor),
        border = BorderStroke(0.5.dp, threadsBorderColor),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            // Author info line
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(primaryColor),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(post.authorAvatar, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(post.authorName, color = threadsTextColor, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            if (post.isVerified) {
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(Icons.Filled.Verified, contentDescription = null, tint = Color(0xFF2196F3), modifier = Modifier.size(14.dp))
                            }
                        }
                        Text("@${post.authorUsername}", color = threadsSubTextColor, fontSize = 10.sp)
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(post.timestamp, color = threadsSubTextColor, fontSize = 10.sp)
                    // Dropdown mock or actions
                    IconButton(onClick = onSummarizeClick, modifier = Modifier.size(24.dp)) {
                        Icon(Icons.Filled.AutoAwesome, contentDescription = null, tint = primaryColor, modifier = Modifier.size(16.dp))
                    }
                }
            }

            // Post main body content
            Text(post.content, color = threadsTextColor, fontSize = 13.sp, lineHeight = 18.sp)

            // Hashtags list
            if (post.hashtags.isNotEmpty()) {
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.fillMaxWidth()) {
                    post.hashtags.forEach { tag ->
                        Text("#$tag", color = primaryColor, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            // Interactive Polls Rendering
            if (post.poll != null) {
                val poll = post.poll
                var userVoteIndex by remember { mutableStateOf(poll.userVotedIndex) }
                val totalVotes = remember(poll.votes) { poll.votes.sum() }

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = threadsBorderColor)
                ) {
                    Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(poll.question, color = threadsTextColor, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        
                        poll.options.forEachIndexed { idx, opt ->
                            val currentVotes = poll.votes[idx]
                            val percent = if (totalVotes > 0) (currentVotes * 100) / totalVotes else 0
                            val isSelected = userVoteIndex == idx

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSelected) primaryColor.copy(alpha = 0.2f) else threadsSurfaceColor)
                                    .clickable {
                                        if (userVoteIndex == null) {
                                            poll.votes[idx] += 1
                                            userVoteIndex = idx
                                            poll.userVotedIndex = idx
                                        }
                                    }
                                    .padding(8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(opt, color = threadsTextColor, fontSize = 11.sp)
                                if (userVoteIndex != null) {
                                    Text("$percent% ($currentVotes)", color = primaryColor, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }

            // GIF preview
            if (post.gifUrl != null) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(110.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFF2C2C2C)),
                    contentAlignment = Alignment.Center
                ) {
                    Text("GIF: ${post.gifUrl} ⚡🎞️", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }

            // Mock Interactive Video Player Card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(130.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color.Black)
            ) {
                if (isVideoPlaying) {
                    val progressAnim = rememberInfiniteTransition()
                    val barWidth by progressAnim.animateFloat(
                        initialValue = 0.1f,
                        targetValue = 0.9f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(2000, easing = LinearEasing),
                            repeatMode = RepeatMode.Restart
                        )
                    )
                    Column(
                        modifier = Modifier.fillMaxSize().padding(12.dp),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(Color.Red)
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text("VIDEO ĐANG CHẠY 🎬", color = Color.White, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                            }
                            IconButton(onClick = { isVideoPlaying = false }, modifier = Modifier.size(20.dp)) {
                                Icon(Icons.Filled.PauseCircle, contentDescription = null, tint = Color.White)
                            }
                        }

                        // Simulated timeline progress bar
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(4.dp)
                                .clip(RoundedCornerShape(2.dp))
                                .background(Color.Gray)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(barWidth)
                                    .fillMaxHeight()
                                    .background(primaryColor)
                            )
                        }
                    }
                } else {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(Icons.Filled.PlayArrow, contentDescription = null, tint = Color.White, modifier = Modifier.size(36.dp))
                        Text("Phát video trải nghiệm 🎥", color = Color.White, fontSize = 11.sp)
                        Button(
                            onClick = { isVideoPlaying = true },
                            colors = ButtonDefaults.buttonColors(containerColor = primaryColor),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                            modifier = Modifier.height(26.dp)
                        ) {
                            Text("Xem Ngay", fontSize = 9.sp, color = Color.White)
                        }
                    }
                }
            }

            // Location Indicator
            if (post.location != null) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.LocationOn, contentDescription = null, tint = primaryColor, modifier = Modifier.size(12.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(post.location, color = threadsSubTextColor, fontSize = 10.sp)
                }
            }

            // AI Action panel
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(onClick = onTranslateClick) {
                    Text("Dịch bằng AI 🌐", color = primaryColor, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }

            Divider(color = threadsBorderColor, thickness = 0.5.dp)

            // Interaction buttons row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    // Like button
                    IconButton(onClick = {
                        userLikedLocal = !userLikedLocal
                        localLikes += if (userLikedLocal) 1 else -1
                        onLike()
                    }) {
                        Icon(
                            imageVector = if (userLikedLocal) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                            contentDescription = "Like",
                            tint = if (userLikedLocal) Color.Red else threadsTextColor,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    // Comment button
                    IconButton(onClick = onCommentClick) {
                        Icon(Icons.Outlined.Comment, contentDescription = "Bình luận", tint = threadsTextColor, modifier = Modifier.size(18.dp))
                    }

                    // Repost button
                    IconButton(onClick = onRepost) {
                        Icon(Icons.Outlined.Repeat, contentDescription = "Đăng lại", tint = threadsTextColor, modifier = Modifier.size(18.dp))
                    }

                    // Share button
                    val context = LocalContext.current
                    IconButton(onClick = {
                        android.widget.Toast.makeText(context, "Đã sao chép liên kết bài viết Threads!", android.widget.Toast.LENGTH_SHORT).show()
                    }) {
                        Icon(Icons.Outlined.Share, contentDescription = "Chia sẻ", tint = threadsTextColor, modifier = Modifier.size(18.dp))
                    }
                }

                // Save bookmark button
                IconButton(onClick = onSave) {
                    Icon(
                        imageVector = if (post.userSaved) Icons.Filled.Bookmark else Icons.Outlined.BookmarkBorder,
                        contentDescription = "Save",
                        tint = if (post.userSaved) primaryColor else threadsTextColor,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            // Likes & comments counts display
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("$localLikes lượt thích", color = threadsSubTextColor, fontSize = 11.sp)
                Text("•", color = threadsSubTextColor, fontSize = 11.sp)
                Text("${post.comments.size} bình luận", color = threadsSubTextColor, fontSize = 11.sp)
                if (post.repostsCount > 0) {
                    Text("•", color = threadsSubTextColor, fontSize = 11.sp)
                    Text("${post.repostsCount} lượt đăng lại", color = threadsSubTextColor, fontSize = 11.sp)
                }
            }
        }
    }
}

@Composable
fun NestedCommentItem(
    comment: ThreadComment,
    depth: Int,
    primaryColor: Color
) {
    var likesCountLocal by remember { mutableStateOf(comment.likesCount) }
    var userLikedLocal by remember { mutableStateOf(comment.userLiked) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = (depth * 14).dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            verticalAlignment = Alignment.Top
        ) {
            // Indentation line indicator
            if (depth > 0) {
                Box(
                    modifier = Modifier
                        .width(2.dp)
                        .height(36.dp)
                        .background(Color.Gray.copy(alpha = 0.3f))
                )
                Spacer(modifier = Modifier.width(6.dp))
            }

            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(primaryColor),
                contentAlignment = Alignment.Center
            ) {
                Text(comment.authorAvatar, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 10.sp)
            }

            Spacer(modifier = Modifier.width(8.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(comment.authorName, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("@${comment.authorUsername}", color = Color.Gray, fontSize = 9.sp)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(comment.timestamp, color = Color.Gray, fontSize = 8.sp)
                }
                Text(comment.content, color = Color.LightGray, fontSize = 11.sp)

                // Quick reply & like row
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.padding(top = 2.dp)
                ) {
                    TextButton(
                        onClick = {
                            userLikedLocal = !userLikedLocal
                            likesCountLocal += if (userLikedLocal) 1 else -1
                            comment.userLiked = userLikedLocal
                            comment.likesCount = likesCountLocal
                        },
                        contentPadding = PaddingValues(0.dp),
                        modifier = Modifier.height(20.dp)
                    ) {
                        Text(
                            "Thích (${likesCountLocal})",
                            color = if (userLikedLocal) Color.Red else Color.Gray,
                            fontSize = 9.sp
                        )
                    }

                    TextButton(
                        onClick = {
                            // Quick reply creator action
                        },
                        contentPadding = PaddingValues(0.dp),
                        modifier = Modifier.height(20.dp)
                    ) {
                        Text("Phản hồi", color = Color.Gray, fontSize = 9.sp)
                    }
                }
            }
        }

        // Render sub comments recursively
        comment.replies.forEach { reply ->
            NestedCommentItem(comment = reply, depth = depth + 1, primaryColor = primaryColor)
        }
    }
}

// Dialog helper to render text responses nicely in an AlertDialog
fun AlertDialogBuilder(context: android.content.Context, title: String, message: String) {
    val builder = android.app.AlertDialog.Builder(context, android.R.style.Theme_DeviceDefault_Dialog_Alert)
    builder.setTitle(title)
        .setMessage(message)
        .setPositiveButton("Tuyệt vời") { dialog, _ -> dialog.dismiss() }
        .show()
}
