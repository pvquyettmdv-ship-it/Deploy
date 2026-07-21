package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectDragGestures
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
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

// --- Data Models for SocialSlide System ---

data class SocialSlide(
    val title: String,
    val subtitle: String? = null,
    val contentPoints: List<String> = emptyList(),
    val highlightStat: String? = null,
    val highlightStatLabel: String? = null,
    val layoutType: String = "standard", // "title", "bullets", "stat_focus"
    val accentColor: Long = 0xFF00E5FF
)

data class SlideComment(
    val id: String,
    val authorName: String,
    val authorAvatar: String,
    val text: String,
    val timestamp: String
)

data class SlideDeck(
    val id: String,
    val title: String,
    val authorName: String,
    val authorUsername: String,
    val authorAvatar: String,
    val category: String,
    val description: String,
    val viewsCount: Int,
    var likesCount: Int,
    val slides: List<SocialSlide>,
    var userLiked: Boolean = false,
    var userBookmarked: Boolean = false,
    val comments: MutableList<SlideComment> = mutableListOf()
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SocialSlideScreen(
    viewModel: MainViewModel,
    onEnterLivestream: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current

    // System States
    var isDarkTheme by remember { mutableStateOf(true) }
    var selectedCategory by remember { mutableStateOf("Tất cả") }
    var searchQuery by remember { mutableStateOf("") }
    
    // Core Colors based on Dark Mode
    val primaryColor = Color(0xFF00E5FF) // Cynical/Electric Teal
    val appBgColor = if (isDarkTheme) Color(0xFF0D0D0C) else Color(0xFFF7F9FB)
    val cardBgColor = if (isDarkTheme) Color(0xFF161618) else Color(0xFFFFFFFF)
    val textColor = if (isDarkTheme) Color.White else Color(0xFF1A1A1A)
    val subTextColor = if (isDarkTheme) Color(0xFF9E9E9E) else Color(0xFF616161)
    val borderColor = if (isDarkTheme) Color(0xFF232325) else Color(0xFFE2E8F0)

    // Slides Repository State
    val slideDecks = remember {
        mutableStateListOf(
            SlideDeck(
                id = "deck_1",
                title = "SocialSlide Platform Pitch Deck",
                authorName = "Nguyễn Minh",
                authorUsername = "nguyenminh_dev",
                authorAvatar = "NM",
                category = "Kinh doanh",
                description = "Giải pháp chia sẻ Slide thuyết trình tương tác trực tuyến 3.0 với lớp dữ liệu UI/UX đồng bộ tiên tiến nhất.",
                viewsCount = 1420,
                likesCount = 380,
                slides = listOf(
                    SocialSlide(
                        title = "SocialSlide",
                        subtitle = "Kỷ Nguyên Chia Sẻ Tri Thức Thuyết Trình Tức Thời 🚀",
                        contentPoints = listOf(
                            "Được phát triển bởi đội ngũ kỹ sư UI/UX đồng bộ tiên tiến",
                            "Mô phỏng trải nghiệm đọc mượt mà vượt trội từ shareslide.net",
                            "Tích hợp trực tiếp Trí tuệ nhân tạo AI tạo Slide chỉ trong 10 giây"
                        ),
                        layoutType = "title",
                        accentColor = 0xFF00E5FF
                    ),
                    SocialSlide(
                        title = "Tại sao lại chọn SocialSlide?",
                        subtitle = "Những thách thức của việc trình bày slide truyền thống",
                        contentPoints = listOf(
                            "⚡ Tải trang tức thời với cấu trúc Jetpack Compose tối ưu cực hạn",
                            "📱 Responsive hoàn hảo từ Mobile cầm tay đến Tablet & PC",
                            "💬 Thảo luận, tương tác trực tiếp trên từng trang slide theo thời gian thực"
                        ),
                        layoutType = "bullets",
                        accentColor = 0xFF8E24AA
                    ),
                    SocialSlide(
                        title = "Sức Mạnh Của Trải Nghiệm",
                        subtitle = "Đột phá về mặt tương tác và tỷ lệ chuyển đổi tri thức",
                        highlightStat = "98%",
                        highlightStatLabel = "Khách hàng hài lòng với tốc độ đọc mượt mà SlideIn",
                        layoutType = "stat_focus",
                        accentColor = 0xFFFFAB40
                    ),
                    SocialSlide(
                        title = "Khởi Đầu Hành Trình Ngay",
                        subtitle = "Tự do chia sẻ, tự do kết nối và lan tỏa tri thức",
                        contentPoints = listOf(
                            "Tham gia cộng đồng hàng nghìn KOC, chuyên gia công nghệ",
                            "Sử dụng AI soạn bài & gợi ý nội dung thông minh",
                            "Cảm ơn bạn đã theo dõi và đồng hành!"
                        ),
                        layoutType = "bullets",
                        accentColor = 0xFF00E5FF
                    )
                ),
                comments = mutableStateListOf(
                    SlideComment("sc1", "Trần Long", "TL", "UI trình đọc slide đỉnh thực sự, vuốt cực nhạy!", "2 phút trước"),
                    SlideComment("sc2", "Chanh Beauty", "CB", "Có tính năng này thì KOC chia sẻ tài liệu tiện quá shop ơi 😍", "15 phút trước")
                )
            ),
            SlideDeck(
                id = "deck_2",
                title = "Cẩm Nang Lập Trình Jetpack Compose M3",
                authorName = "Hoàng Sơn Review",
                authorUsername = "son_techie",
                authorAvatar = "HS",
                category = "Công nghệ",
                description = "Chi tiết các nguyên tắc thiết kế Material Design 3, Spacing Grid, Accessibility và cách tối ưu hóa hiệu năng render UI.",
                viewsCount = 890,
                likesCount = 210,
                slides = listOf(
                    SocialSlide(
                        title = "Jetpack Compose M3",
                        subtitle = "Bộ công cụ xây dựng UI Android hiện đại, mượt mà bậc nhất 📱",
                        contentPoints = listOf(
                            "Khai báo UI thông minh thông qua các hàm Kotlin composable",
                            "Hỗ trợ đầy đủ bộ màu động Dynamic Colors theo hệ điều hành",
                            "Xây dựng theo triết lý Material Design 3 nâng cấp"
                        ),
                        layoutType = "title",
                        accentColor = 0xFF4CAF50
                    ),
                    SocialSlide(
                        title = "Tiêu Chuẩn Accessibility (WCAG)",
                        subtitle = "Đảm bảo ứng dụng phục vụ tốt nhất cho mọi đối tượng khách hàng",
                        highlightStat = "48dp",
                        highlightStatLabel = "Kích thước touch target tối thiểu cho mọi nút bấm",
                        layoutType = "stat_focus",
                        accentColor = 0xFFFF1744
                    ),
                    SocialSlide(
                        title = "Tối Ưu Trạng Thế Sizing",
                        subtitle = "Thích ứng linh hoạt với mọi kích cỡ màn hình",
                        contentPoints = listOf(
                            "Không sử dụng các kích thước pixel cứng cố định",
                            "Sử dụng BoxWithConstraints đo lường không gian thực tế",
                            "Tích hợp WindowSizeClasses cho máy tính bảng và màn hình gập"
                        ),
                        layoutType = "bullets",
                        accentColor = 0xFF2196F3
                    )
                ),
                comments = mutableStateListOf(
                    SlideComment("sc3", "Linh Đan", "LD", "Slide tóm tắt rất súc tích, dễ học!", "1 giờ trước")
                )
            ),
            SlideDeck(
                id = "deck_3",
                title = "Xu Hướng Trí Tuệ Nhân Tạo AI 2026",
                authorName = "VibeCart Tech",
                authorUsername = "vibecart_tech",
                authorAvatar = "VC",
                category = "Giáo dục",
                description = "Đánh giá các mô hình ngôn ngữ lớn, xu hướng tự động hóa quy trình công việc và cách ứng dụng AI tăng hiệu suất doanh nghiệp.",
                viewsCount = 2300,
                likesCount = 650,
                slides = listOf(
                    SocialSlide(
                        title = "Trí Tuệ Nhân Tạo 2026",
                        subtitle = "Những bước chuyển dịch vĩ mô trong kỷ nguyên số 🤖",
                        contentPoints = listOf(
                            "Mô hình đa phương thức (Multimodal) xử lý video, giọng nói thời gian thực",
                            "Sự bùng nổ của các tác nhân AI Agent tự vận hành tác vụ phức tạp",
                            "Cá nhân hóa trải nghiệm khách hàng tối đa"
                        ),
                        layoutType = "title",
                        accentColor = 0xFF651FFF
                    ),
                    SocialSlide(
                        title = "Hiệu Suất Tăng Trưởng",
                        subtitle = "AI tối ưu hóa mọi khâu vận hành sản xuất",
                        highlightStat = "10x",
                        highlightStatLabel = "Tốc độ xử lý thông tin & kiến tạo slide thuyết trình",
                        layoutType = "stat_focus",
                        accentColor = 0xFF00E676
                    )
                )
            )
        )
    }

    // Active Deck Reader Overlay State
    var activeReaderDeck by remember { mutableStateOf<SlideDeck?>(null) }
    var activeSlideIndex by remember { mutableStateOf(0) }
    var isAutoplayEnabled by remember { mutableStateOf(false) }
    var autoplaySpeedMs by remember { mutableStateOf(4000L) }
    var showThumbnailGrid by remember { mutableStateOf(false) }

    // AI Helper state
    var showAiAssistantSheet by remember { mutableStateOf(false) }
    var aiQueryText by remember { mutableStateOf("") }
    val aiChatHistory = remember { mutableStateListOf<Pair<String, String>>() }
    var isAiAnalyzing by remember { mutableStateOf(false) }

    // AI Slide Generator State
    var showAiGeneratorSheet by remember { mutableStateOf(false) }
    var aiGeneratorTopic by remember { mutableStateOf("") }
    var aiGeneratorStyle by remember { mutableStateOf("Premium Dark 🌌") } // "Minimal ⚪", "Premium Dark 🌌", "Cyberpunk ⚡"
    var isGeneratingSlideDeck by remember { mutableStateOf(false) }

    // Detail comments state
    var newCommentText by remember { mutableStateOf("") }

    // Auto-play timer implementation
    LaunchedEffect(isAutoplayEnabled, activeSlideIndex, activeReaderDeck) {
        if (isAutoplayEnabled && activeReaderDeck != null) {
            delay(autoplaySpeedMs)
            val deck = activeReaderDeck!!
            if (activeSlideIndex < deck.slides.lastIndex) {
                activeSlideIndex++
            } else {
                activeSlideIndex = 0 // loop
            }
        }
    }

    // Main UI Box
    Box(modifier = Modifier.fillMaxSize()) {
        // Feed Catalog List View
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(appBgColor)
        ) {
            // Advanced Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Brush.linearGradient(listOf(Color(0xFF00E5FF), Color(0xFF7C4DFF)))),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("S", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "SocialSlide",
                            color = textColor,
                            fontWeight = FontWeight.Black,
                            fontSize = 22.sp
                        )
                    }
                    Text(
                        text = "Hệ thống trình đọc & kiến tạo slide UI/UX đồng bộ",
                        color = subTextColor,
                        fontSize = 11.sp
                    )
                }

                // Theme switch & Create Button Group
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    IconButton(
                        onClick = { isDarkTheme = !isDarkTheme },
                        modifier = Modifier.background(cardBgColor, CircleShape)
                    ) {
                        Icon(
                            imageVector = if (isDarkTheme) Icons.Outlined.LightMode else Icons.Outlined.DarkMode,
                            contentDescription = "Doi giao dien",
                            tint = textColor
                        )
                    }

                    Button(
                        onClick = { showAiGeneratorSheet = true },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = primaryColor,
                            contentColor = Color.Black
                        ),
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Icon(Icons.Filled.AutoAwesome, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Tạo AI", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
            }

            // Horizontal Category Tabs
            val categories = listOf("Tất cả", "Công nghệ", "Kinh doanh", "Thiết kế", "Giáo dục")
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(categories) { cat ->
                    val isSelected = selectedCategory == cat
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(if (isSelected) primaryColor else cardBgColor)
                            .border(1.dp, if (isSelected) Color.Transparent else borderColor, RoundedCornerShape(20.dp))
                            .clickable { selectedCategory = cat }
                            .padding(horizontal = 14.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = cat,
                            color = if (isSelected) Color.Black else textColor,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }
                }
            }

            // Search bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Tìm slide, tác giả, chủ đề thuyết trình...", fontSize = 13.sp) },
                leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Filled.Close, contentDescription = null)
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .height(52.dp),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = textColor,
                    unfocusedTextColor = textColor,
                    focusedContainerColor = cardBgColor,
                    unfocusedContainerColor = cardBgColor,
                    focusedBorderColor = primaryColor,
                    unfocusedBorderColor = borderColor
                ),
                singleLine = true
            )

            // Slide list catalog
            val filteredDecks = slideDecks.filter { deck ->
                (selectedCategory == "Tất cả" || deck.category == selectedCategory) &&
                        (deck.title.contains(searchQuery, ignoreCase = true) || deck.description.contains(searchQuery, ignoreCase = true))
            }

            if (filteredDecks.isEmpty()) {
                // Empty State
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.Slideshow,
                        contentDescription = null,
                        tint = subTextColor.copy(alpha = 0.5f),
                        modifier = Modifier.size(72.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Không tìm thấy slide phù hợp",
                        color = textColor,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Hãy nhập từ khóa khác hoặc click 'Tạo AI' để sinh slide mới!",
                        color = subTextColor,
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Featured Banner Card mimicking premium slide reader
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(130.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(
                                    Brush.linearGradient(
                                        colors = listOf(Color(0xFF003049), Color(0xFFD62828), Color(0xFFF77F00))
                                    )
                                )
                                .clickable {
                                    activeReaderDeck = slideDecks[0]
                                    activeSlideIndex = 0
                                }
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(16.dp),
                                verticalArrangement = Arrangement.SpaceBetween
                            ) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(Color.White.copy(alpha = 0.25f))
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text("NỔI BẬT HÔM NAY ⭐️", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 10.sp)
                                }

                                Column {
                                    Text(
                                        text = "Kiến tạo slide chuyên nghiệp bằng AI",
                                        color = Color.White,
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 17.sp
                                    )
                                    Text(
                                        text = "Đọc tài liệu với công nghệ SlideIn tương tác mượt mà",
                                        color = Color.White.copy(alpha = 0.85f),
                                        fontSize = 11.sp
                                    )
                                }
                            }
                        }
                    }

                    // Header for catalog
                    item {
                        Text(
                            text = "Khám phá tài liệu (${filteredDecks.size})",
                            color = textColor,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            modifier = Modifier.padding(bottom = 4.dp)
                        )
                    }

                    items(filteredDecks) { deck ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    activeReaderDeck = deck
                                    activeSlideIndex = 0
                                },
                            colors = CardDefaults.cardColors(containerColor = cardBgColor),
                            border = BorderStroke(1.dp, borderColor),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                // Top author row
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(36.dp)
                                            .clip(CircleShape)
                                            .background(primaryColor.copy(alpha = 0.2f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = deck.authorAvatar,
                                            color = primaryColor,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = deck.authorName,
                                            color = textColor,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp
                                        )
                                        Text(
                                            text = "@${deck.authorUsername}",
                                            color = subTextColor,
                                            fontSize = 11.sp
                                        )
                                    }
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(primaryColor.copy(alpha = 0.15f))
                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Text(
                                            text = deck.category,
                                            color = primaryColor,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 10.sp
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                // Mini representation of slides
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(110.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(
                                            Brush.linearGradient(
                                                colors = listOf(
                                                    Color(deck.slides.firstOrNull()?.accentColor ?: 0xFF1E1E1E).copy(alpha = 0.9f),
                                                    Color(0xFF232526)
                                                )
                                            )
                                        )
                                        .padding(14.dp)
                                ) {
                                    Column(verticalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxSize()) {
                                        Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                                            Text(
                                                text = deck.slides.firstOrNull()?.title ?: deck.title,
                                                color = Color.White,
                                                fontWeight = FontWeight.ExtraBold,
                                                fontSize = 14.sp,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis,
                                                modifier = Modifier.weight(1f)
                                            )
                                            Icon(Icons.Filled.OpenInNew, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                                        }
                                        
                                        Text(
                                            text = deck.slides.firstOrNull()?.subtitle ?: "",
                                            color = Color.White.copy(alpha = 0.7f),
                                            fontSize = 11.sp,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )

                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(4.dp))
                                                    .background(Color.Black.copy(alpha = 0.5f))
                                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                                            ) {
                                                Text(
                                                    text = "${deck.slides.size} TRANG SLIDE",
                                                    color = Color.White,
                                                    fontSize = 9.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                            Text(
                                                text = "SocialSlide UI/UX",
                                                color = Color.White.copy(alpha = 0.4f),
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                Text(
                                    text = deck.title,
                                    color = textColor,
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 15.sp
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = deck.description,
                                    color = subTextColor,
                                    fontSize = 12.sp,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )

                                Spacer(modifier = Modifier.height(12.dp))

                                // Social feedback bar
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(Icons.Filled.Visibility, contentDescription = null, tint = subTextColor, modifier = Modifier.size(16.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(text = "${deck.viewsCount}", color = subTextColor, fontSize = 12.sp)
                                        }
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                imageVector = if (deck.userLiked) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                                                contentDescription = null,
                                                tint = if (deck.userLiked) Color.Red else subTextColor,
                                                modifier = Modifier.size(16.dp).clickable {
                                                    deck.userLiked = !deck.userLiked
                                                    deck.likesCount += if (deck.userLiked) 1 else -1
                                                }
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(text = "${deck.likesCount}", color = subTextColor, fontSize = 12.sp)
                                        }
                                    }

                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = if (deck.userBookmarked) Icons.Filled.Bookmark else Icons.Outlined.BookmarkBorder,
                                            contentDescription = null,
                                            tint = if (deck.userBookmarked) primaryColor else subTextColor,
                                            modifier = Modifier.size(18.dp).clickable {
                                                deck.userBookmarked = !deck.userBookmarked
                                                android.widget.Toast.makeText(context, if (deck.userBookmarked) "Đã lưu vào bộ sưu tập!" else "Đã xóa khỏi bộ sưu tập!", android.widget.Toast.LENGTH_SHORT).show()
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // --- INTERACTIVE SLIDE READER OVERLAY VIEW (shareslide.net SlideIn style) ---
        activeReaderDeck?.let { deck ->
            var dragOffset by remember { mutableStateOf(0f) }
            val readerBgColor = if (isDarkTheme) Color(0xFF070707) else Color(0xFFF1F3F5)
            val readerCardColor = if (isDarkTheme) Color(0xFF131315) else Color(0xFFFFFFFF)

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(readerBgColor)
                    .pointerInput(Unit) {
                        detectDragGestures(
                            onDragEnd = {
                                if (dragOffset > 100f) {
                                    if (activeSlideIndex > 0) activeSlideIndex--
                                } else if (dragOffset < -100f) {
                                    if (activeSlideIndex < deck.slides.lastIndex) activeSlideIndex++
                                }
                                dragOffset = 0f
                            },
                            onDrag = { _, dragAmount ->
                                dragOffset += dragAmount.x
                            }
                        )
                    }
                    .statusBarsPadding()
            ) {
                Column(modifier = Modifier.fillMaxSize()) {
                    // Top Action controls
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        IconButton(onClick = { activeReaderDeck = null }) {
                            Icon(Icons.Filled.ArrowBack, contentDescription = "Back", tint = textColor)
                        }

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = deck.title,
                                color = textColor,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 14.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.widthIn(max = 180.dp)
                            )
                            Text(
                                text = "Trang ${activeSlideIndex + 1} trên ${deck.slides.size}",
                                color = subTextColor,
                                fontSize = 11.sp
                            )
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            IconButton(onClick = { isAutoplayEnabled = !isAutoplayEnabled }) {
                                Icon(
                                    imageVector = if (isAutoplayEnabled) Icons.Filled.PauseCircle else Icons.Filled.PlayCircle,
                                    contentDescription = "Autoplay",
                                    tint = if (isAutoplayEnabled) primaryColor else textColor
                                )
                            }
                            IconButton(onClick = { showThumbnailGrid = !showThumbnailGrid }) {
                                Icon(
                                    imageVector = Icons.Filled.GridView,
                                    contentDescription = "Grid preview",
                                    tint = if (showThumbnailGrid) primaryColor else textColor
                                )
                            }
                            IconButton(onClick = { showAiAssistantSheet = true }) {
                                Icon(Icons.Filled.AutoAwesome, contentDescription = "AI Assistant", tint = primaryColor)
                            }
                        }
                    }

                    // Progress Bar
                    val progress = (activeSlideIndex + 1).toFloat() / deck.slides.size.toFloat()
                    LinearProgressIndicator(
                        progress = progress,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(4.dp),
                        color = primaryColor,
                        trackColor = borderColor
                    )

                    // The Slide Frame Container with SlideIn / SlideOut Animation
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        AnimatedContent(
                            targetState = activeSlideIndex,
                            transitionSpec = {
                                if (targetState > initialState) {
                                    (slideInHorizontally { width -> width } + fadeIn()).togetherWith(
                                        slideOutHorizontally { width -> -width } + fadeOut())
                                } else {
                                    (slideInHorizontally { width -> -width } + fadeIn()).togetherWith(
                                        slideOutHorizontally { width -> width } + fadeOut())
                                }.using(
                                    SizeTransform(clip = false)
                                )
                            },
                            label = "SlideTransition"
                        ) { index ->
                            val slide = deck.slides[index]
                            val accentColorToken = Color(slide.accentColor)

                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .aspectRatio(1.33f)
                                    .clip(RoundedCornerShape(20.dp))
                                    .border(1.5.dp, accentColorToken.copy(alpha = 0.5f), RoundedCornerShape(20.dp)),
                                colors = CardDefaults.cardColors(containerColor = readerCardColor),
                                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .drawBehind {
                                            drawCircle(
                                                color = accentColorToken.copy(alpha = 0.05f),
                                                radius = size.width * 0.4f,
                                                center = Offset(size.width * 0.9f, size.height * 0.1f)
                                            )
                                            drawCircle(
                                                color = Color.White.copy(alpha = 0.02f),
                                                radius = size.width * 0.3f,
                                                center = Offset(size.width * 0.1f, size.height * 0.8f)
                                            )
                                        }
                                        .padding(24.dp)
                                ) {
                                    when (slide.layoutType) {
                                        "title" -> {
                                            Column(
                                                modifier = Modifier.fillMaxSize(),
                                                verticalArrangement = Arrangement.Center,
                                                horizontalAlignment = Alignment.CenterHorizontally
                                            ) {
                                                Box(
                                                    modifier = Modifier
                                                        .clip(RoundedCornerShape(8.dp))
                                                        .background(accentColorToken.copy(alpha = 0.15f))
                                                        .padding(horizontal = 12.dp, vertical = 6.dp)
                                                ) {
                                                    Text(
                                                        text = "SocialSlide Core 3.0",
                                                        color = accentColorToken,
                                                        fontWeight = FontWeight.Bold,
                                                        fontSize = 10.sp
                                                    )
                                                }

                                                Spacer(modifier = Modifier.height(14.dp))

                                                Text(
                                                    text = slide.title,
                                                    color = textColor,
                                                    fontWeight = FontWeight.Black,
                                                    fontSize = 24.sp,
                                                    textAlign = TextAlign.Center
                                                )

                                                Spacer(modifier = Modifier.height(10.dp))

                                                slide.subtitle?.let {
                                                    Text(
                                                        text = it,
                                                        color = subTextColor,
                                                        fontSize = 13.sp,
                                                        textAlign = TextAlign.Center
                                                    )
                                                }
                                            }
                                        }
                                        "stat_focus" -> {
                                            Row(
                                                modifier = Modifier.fillMaxSize(),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Column(modifier = Modifier.weight(1.2f)) {
                                                    Text(
                                                        text = slide.title,
                                                        color = accentColorToken,
                                                        fontWeight = FontWeight.Black,
                                                        fontSize = 20.sp
                                                    )
                                                    Spacer(modifier = Modifier.height(8.dp))
                                                    slide.subtitle?.let {
                                                        Text(
                                                            text = it,
                                                            color = textColor,
                                                            fontWeight = FontWeight.Bold,
                                                            fontSize = 13.sp
                                                        )
                                                    }
                                                    Spacer(modifier = Modifier.height(8.dp))
                                                    Text(
                                                        text = slide.highlightStatLabel ?: "",
                                                        color = subTextColor,
                                                        fontSize = 11.sp
                                                    )
                                                }

                                                Column(
                                                    modifier = Modifier.weight(0.8f),
                                                    horizontalAlignment = Alignment.CenterHorizontally,
                                                    verticalArrangement = Arrangement.Center
                                                ) {
                                                    Box(
                                                        modifier = Modifier
                                                            .size(90.dp)
                                                            .clip(CircleShape)
                                                            .background(accentColorToken.copy(alpha = 0.1f))
                                                            .border(2.dp, accentColorToken, CircleShape),
                                                        contentAlignment = Alignment.Center
                                                    ) {
                                                        Text(
                                                            text = slide.highlightStat ?: "",
                                                            color = accentColorToken,
                                                            fontWeight = FontWeight.Black,
                                                            fontSize = 28.sp
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                        else -> {
                                            Column(
                                                modifier = Modifier.fillMaxSize(),
                                                verticalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                Column {
                                                    Text(
                                                        text = slide.title,
                                                        color = accentColorToken,
                                                        fontWeight = FontWeight.Black,
                                                        fontSize = 20.sp
                                                    )
                                                    slide.subtitle?.let {
                                                        Text(
                                                            text = it,
                                                            color = subTextColor,
                                                            fontSize = 11.sp
                                                        )
                                                    }
                                                }

                                                Column(
                                                    verticalArrangement = Arrangement.spacedBy(8.dp),
                                                    modifier = Modifier.padding(vertical = 8.dp)
                                                ) {
                                                    slide.contentPoints.forEach { point ->
                                                        Row(verticalAlignment = Alignment.Top) {
                                                            Icon(
                                                                imageVector = Icons.Filled.CheckCircle,
                                                                contentDescription = null,
                                                                tint = accentColorToken,
                                                                modifier = Modifier
                                                                    .size(16.dp)
                                                                    .padding(top = 2.dp)
                                                            )
                                                            Spacer(modifier = Modifier.width(8.dp))
                                                            Text(
                                                                text = point,
                                                                color = textColor,
                                                                fontSize = 12.sp,
                                                                lineHeight = 16.sp
                                                            )
                                                        }
                                                    }
                                                }

                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.SpaceBetween,
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Text(
                                                        text = "SocialSlide Platform",
                                                        color = subTextColor.copy(alpha = 0.5f),
                                                        fontSize = 9.sp,
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                    Text(
                                                        text = "${activeSlideIndex + 1} / ${deck.slides.size}",
                                                        color = accentColorToken,
                                                        fontSize = 10.sp,
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Interactive Bottom Navigation / Thumbnail Preview row
                    AnimatedVisibility(visible = showThumbnailGrid) {
                        LazyRow(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(readerCardColor)
                                .border(0.5.dp, borderColor)
                                .padding(vertical = 12.dp),
                            contentPadding = PaddingValues(horizontal = 16.dp),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            itemsIndexed(deck.slides) { i, s ->
                                val isSelected = i == activeSlideIndex
                                Box(
                                    modifier = Modifier
                                        .width(80.dp)
                                        .aspectRatio(1.33f)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (isSelected) Color(s.accentColor).copy(alpha = 0.25f) else borderColor.copy(alpha = 0.5f))
                                        .border(2.dp, if (isSelected) Color(s.accentColor) else Color.Transparent, RoundedCornerShape(8.dp))
                                        .clickable { activeSlideIndex = i }
                                        .padding(6.dp)
                                ) {
                                    Text(
                                        text = s.title,
                                        color = textColor,
                                        fontSize = 8.sp,
                                        fontWeight = FontWeight.Bold,
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Box(
                                        modifier = Modifier
                                            .align(Alignment.BottomEnd)
                                            .size(14.dp)
                                            .clip(CircleShape)
                                            .background(Color.Black.copy(alpha = 0.6f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text("${i + 1}", color = Color.White, fontSize = 7.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }

                    // Next / Prev control Buttons Row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Button(
                            onClick = { if (activeSlideIndex > 0) activeSlideIndex-- },
                            enabled = activeSlideIndex > 0,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = cardBgColor,
                                contentColor = textColor,
                                disabledContainerColor = cardBgColor.copy(alpha = 0.5f)
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.width(110.dp)
                        ) {
                            Icon(Icons.Filled.ArrowBackIos, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Trang trước", fontSize = 11.sp)
                        }

                        IconButton(
                            onClick = {
                                coroutineScope.launch {
                                    android.widget.Toast.makeText(context, "Đang chuẩn bị tải PDF cho \"${deck.title}\"...", android.widget.Toast.LENGTH_SHORT).show()
                                    delay(1200)
                                    android.widget.Toast.makeText(context, "Tải Slide PDF thành công! 📥", android.widget.Toast.LENGTH_SHORT).show()
                                }
                            },
                            modifier = Modifier.background(primaryColor.copy(alpha = 0.15f), CircleShape)
                        ) {
                            Icon(Icons.Filled.Download, contentDescription = "Download Slide", tint = primaryColor)
                        }

                        Button(
                            onClick = { if (activeSlideIndex < deck.slides.lastIndex) activeSlideIndex++ },
                            enabled = activeSlideIndex < deck.slides.lastIndex,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = primaryColor,
                                contentColor = Color.Black,
                                disabledContainerColor = primaryColor.copy(alpha = 0.5f)
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.width(110.dp)
                        ) {
                            Text("Trang sau", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(Icons.Filled.ArrowForwardIos, contentDescription = null, modifier = Modifier.size(14.dp))
                        }
                    }

                    // Interactive comment feedback section
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(0.7f)
                            .padding(16.dp),
                        colors = CardDefaults.cardColors(containerColor = readerCardColor),
                        border = BorderStroke(1.dp, borderColor)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = "Thảo luận bài thuyết trình 💬",
                                color = textColor,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )

                            Spacer(modifier = Modifier.height(6.dp))

                            LazyColumn(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                if (deck.comments.isEmpty()) {
                                    item {
                                        Text(
                                            text = "Chưa có bình luận nào. Hãy chia sẻ cảm nghĩ của bạn!",
                                            color = subTextColor,
                                            fontSize = 11.sp,
                                            modifier = Modifier.padding(vertical = 12.dp)
                                        )
                                    }
                                } else {
                                    items(deck.comments) { comment ->
                                        Row(verticalAlignment = Alignment.Top, modifier = Modifier.padding(vertical = 4.dp)) {
                                            Box(
                                                modifier = Modifier
                                                    .size(24.dp)
                                                    .clip(CircleShape)
                                                    .background(primaryColor.copy(alpha = 0.2f)),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(comment.authorAvatar, color = primaryColor, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                            }
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Column {
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Text(comment.authorName, color = textColor, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                                    Spacer(modifier = Modifier.width(6.dp))
                                                    Text(comment.timestamp, color = subTextColor, fontSize = 9.sp)
                                                }
                                                Text(comment.text, color = textColor, fontSize = 11.sp)
                                            }
                                        }
                                    }
                                }
                            }

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
                            ) {
                                OutlinedTextField(
                                    value = newCommentText,
                                    onValueChange = { newCommentText = it },
                                    placeholder = { Text("Viết bình luận...", fontSize = 11.sp) },
                                    modifier = Modifier.weight(1f).height(44.dp),
                                    shape = RoundedCornerShape(8.dp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedTextColor = textColor,
                                        unfocusedTextColor = textColor,
                                        focusedBorderColor = primaryColor,
                                        unfocusedBorderColor = borderColor
                                    ),
                                    singleLine = true
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                IconButton(
                                    onClick = {
                                        if (newCommentText.isNotBlank()) {
                                            deck.comments.add(
                                                SlideComment(
                                                    id = "sc_new_${System.currentTimeMillis()}",
                                                    authorName = "Bạn (KOC)",
                                                    authorAvatar = "ME",
                                                    text = newCommentText,
                                                    timestamp = "Vừa xong"
                                                )
                                            )
                                            newCommentText = ""
                                            android.widget.Toast.makeText(context, "Đã gửi bình luận!", android.widget.Toast.LENGTH_SHORT).show()
                                        }
                                    },
                                    modifier = Modifier.background(primaryColor, CircleShape).size(36.dp)
                                ) {
                                    Icon(Icons.Filled.Send, contentDescription = null, tint = Color.Black, modifier = Modifier.size(16.dp))
                                }
                            }
                        }
                    }
                }
            }
        }

        // --- AI ASSISTANT BOTTOM SHEET SIDEBAR PANEL ---
        if (showAiAssistantSheet && activeReaderDeck != null) {
            val deck = activeReaderDeck!!
            ModalBottomSheet(
                onDismissRequest = { showAiAssistantSheet = false },
                containerColor = cardBgColor,
                dragHandle = { BottomSheetDefaults.DragHandle(color = borderColor) }
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .height(400.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.AutoAwesome, contentDescription = null, tint = primaryColor)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "AI Trợ Lý Thuyết Trình 🤖",
                                color = textColor,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                        }
                        IconButton(onClick = { showAiAssistantSheet = false }) {
                            Icon(Icons.Filled.Close, contentDescription = null, tint = textColor)
                        }
                    }

                    Text(
                        text = "Phân tích và đặt câu hỏi về slide: \"${deck.title}\"",
                        color = subTextColor,
                        fontSize = 11.sp
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        if (aiChatHistory.isEmpty()) {
                            item {
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = borderColor.copy(alpha = 0.3f)),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(12.dp)) {
                                        Text(
                                            text = "Gợi ý câu hỏi:",
                                            color = textColor,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp
                                        )
                                        Spacer(modifier = Modifier.height(6.dp))
                                        listOf(
                                            "Tóm tắt nội dung chính của slide này?",
                                            "Đối tượng người đọc chính của tài liệu này là ai?",
                                            "Lời khuyên cải thiện nội dung slide tốt hơn?"
                                        ).forEach { query ->
                                            Text(
                                                text = "• $query",
                                                color = primaryColor,
                                                fontSize = 11.sp,
                                                modifier = Modifier
                                                    .clickable {
                                                        aiQueryText = query
                                                    }
                                                    .padding(vertical = 4.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        } else {
                            items(aiChatHistory) { chat ->
                                Column(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                                    Text(
                                        text = "Bạn: ${chat.first}",
                                        color = textColor,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Card(
                                        colors = CardDefaults.cardColors(containerColor = borderColor.copy(alpha = 0.2f)),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text(
                                            text = chat.second,
                                            color = textColor,
                                            fontSize = 11.sp,
                                            modifier = Modifier.padding(8.dp),
                                            lineHeight = 16.sp
                                        )
                                    }
                                }
                            }
                        }
                    }

                    if (isAiAnalyzing) {
                        LinearProgressIndicator(modifier = Modifier.fillMaxWidth(), color = primaryColor)
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = aiQueryText,
                            onValueChange = { aiQueryText = it },
                            placeholder = { Text("Đặt câu hỏi cho AI...", fontSize = 12.sp) },
                            modifier = Modifier.weight(1f),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = textColor,
                                unfocusedTextColor = textColor,
                                focusedBorderColor = primaryColor,
                                unfocusedBorderColor = borderColor
                            )
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        IconButton(
                            onClick = {
                                if (aiQueryText.isNotBlank()) {
                                    val userQ = aiQueryText
                                    aiQueryText = ""
                                    isAiAnalyzing = true
                                    coroutineScope.launch {
                                        val slideContext = deck.slides.joinToString("\n") { slide ->
                                            "Slide Title: ${slide.title}, Subtitle: ${slide.subtitle}, Content: ${slide.contentPoints.joinToString()}"
                                        }
                                        val prompt = "Dưới đây là nội dung một slide thuyết trình:\n$slideContext\n\nHãy trả lời câu hỏi sau của người dùng một cách ngắn gọn, chuyên nghiệp và lịch sự: \"$userQ\""
                                        val response = GeminiService.generateResponse(prompt)
                                        aiChatHistory.add(Pair(userQ, response))
                                        isAiAnalyzing = false
                                    }
                                }
                            },
                            modifier = Modifier.background(primaryColor, CircleShape)
                        ) {
                            Icon(Icons.Filled.Send, contentDescription = null, tint = Color.Black)
                        }
                    }
                }
            }
        }

        // --- AI POWERED PRESENTATION GENERATOR SHEET ---
        if (showAiGeneratorSheet) {
            ModalBottomSheet(
                onDismissRequest = { showAiGeneratorSheet = false },
                containerColor = cardBgColor,
                dragHandle = { BottomSheetDefaults.DragHandle(color = borderColor) }
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .navigationBarsPadding()
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.AutoAwesome, contentDescription = null, tint = primaryColor)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Kiến Tạo Slide Bằng AI ✨",
                                color = textColor,
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp
                            )
                        }
                        IconButton(onClick = { showAiGeneratorSheet = false }) {
                            Icon(Icons.Filled.Close, contentDescription = null, tint = textColor)
                        }
                    }

                    Text(
                        text = "Chỉ cần nhập chủ đề, AI của SocialSlide sẽ biên soạn nội dung và định dạng slide tự động.",
                        color = subTextColor,
                        fontSize = 11.sp
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text("Chủ đề Slide", color = textColor, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(
                        value = aiGeneratorTopic,
                        onValueChange = { aiGeneratorTopic = it },
                        placeholder = { Text("Ví dụ: Chiến lược marketing đồ uống, Kế hoạch quản lý tài chính...", fontSize = 12.sp) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = textColor,
                            unfocusedTextColor = textColor,
                            focusedBorderColor = primaryColor,
                            unfocusedBorderColor = borderColor
                        )
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text("Phong cách giao diện", color = textColor, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("Minimal ⚪", "Premium Dark 🌌", "Cyberpunk ⚡").forEach { style ->
                            val isSelected = aiGeneratorStyle == style
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSelected) primaryColor else borderColor.copy(alpha = 0.5f))
                                    .clickable { aiGeneratorStyle = style }
                                    .padding(horizontal = 12.dp, vertical = 8.dp)
                            ) {
                                Text(
                                    text = style,
                                    color = if (isSelected) Color.Black else textColor,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    if (isGeneratingSlideDeck) {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            CircularProgressIndicator(color = primaryColor)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("AI đang phân tích tri thức & định dạng slide...", color = primaryColor, fontSize = 12.sp)
                        }
                    } else {
                        Button(
                            onClick = {
                                if (aiGeneratorTopic.isNotBlank()) {
                                    isGeneratingSlideDeck = true
                                    coroutineScope.launch {
                                        val prompt = """
                                            Hãy soạn thảo một bài thuyết trình slide cực kỳ chi tiết, súc tích và chuyên nghiệp về chủ đề: "$aiGeneratorTopic".
                                            Bài thuyết trình phải có đúng 4 slide. Hãy trả về chính xác định dạng văn bản thô theo cấu trúc phân tách sau đây để tôi có thể parse được dễ dàng bằng code:
                                            
                                            [SLIDE 1]
                                            Tiêu đề: ...
                                            Phụ đề: ...
                                            
                                            [SLIDE 2]
                                            Tiêu đề: ...
                                            Nội dung 1: ...
                                            Nội dung 2: ...
                                            Nội dung 3: ...
                                            
                                            [SLIDE 3]
                                            Tiêu đề: ...
                                            Nội dung 1: ...
                                            Nội dung 2: ...
                                            Số liệu: ...
                                            Nhãn số liệu: ...
                                            
                                            [SLIDE 4]
                                            Tiêu đề: ...
                                            Nội dung 1: ...
                                            Nội dung 2: ...
                                            Nội dung 3: ...
                                            
                                            Lưu ý: Hãy viết bằng Tiếng Việt. Mỗi dòng nội dung cực kỳ súc tích, ngắn gọn để hiển thị vừa vặn trên slide di động. Đừng viết quá dài dòng.
                                        """.trimIndent()

                                        val response = GeminiService.generateResponse(prompt)
                                        val generatedSlides = mutableListOf<SocialSlide>()
                                        try {
                                            val slideBlocks = response.split("[SLIDE")
                                            for (i in 1..4) {
                                                if (i < slideBlocks.size) {
                                                    val block = slideBlocks[i]
                                                    val lines = block.lines().map { it.trim() }.filter { it.isNotEmpty() }
                                                    
                                                    var title = "Slide $i"
                                                    var subtitle: String? = null
                                                    val contentPoints = mutableListOf<String>()
                                                    var highlightStat: String? = null
                                                    var highlightStatLabel: String? = null
                                                    var layoutType = "standard"

                                                    lines.forEach { line ->
                                                        when {
                                                            line.startsWith("Tiêu đề:") -> title = line.removePrefix("Tiêu đề:").trim()
                                                            line.startsWith("Phụ đề:") -> subtitle = line.removePrefix("Phụ đề:").trim()
                                                            line.startsWith("Nội dung") -> contentPoints.add(line.substringAfter(":").trim())
                                                            line.startsWith("Số liệu:") -> highlightStat = line.removePrefix("Số liệu:").trim()
                                                            line.startsWith("Nhãn số liệu:") -> highlightStatLabel = line.removePrefix("Nhãn số liệu:").trim()
                                                        }
                                                    }

                                                    if (i == 1) {
                                                        layoutType = "title"
                                                    } else if (highlightStat != null) {
                                                        layoutType = "stat_focus"
                                                    } else {
                                                        layoutType = "bullets"
                                                    }

                                                    val accent = when (aiGeneratorStyle) {
                                                        "Minimal ⚪" -> 0xFF9E9E9E
                                                        "Cyberpunk ⚡" -> 0xFFFF0055
                                                        else -> 0xFF00E5FF
                                                    }

                                                    generatedSlides.add(
                                                        SocialSlide(
                                                            title = title,
                                                            subtitle = subtitle,
                                                            contentPoints = contentPoints,
                                                            highlightStat = highlightStat,
                                                            highlightStatLabel = highlightStatLabel,
                                                            layoutType = layoutType,
                                                            accentColor = accent
                                                        )
                                                    )
                                                }
                                            }
                                        } catch (e: Exception) {
                                            generatedSlides.add(SocialSlide("Slide 1", aiGeneratorTopic, listOf("Nội dung slide được sinh bởi AI thành công!"), layoutType = "title"))
                                        }

                                        if (generatedSlides.isEmpty()) {
                                            generatedSlides.add(SocialSlide("Slide 1: $aiGeneratorTopic", "Tổng quan về chủ đề thuyết trình", listOf("Phân tích tổng hợp từ AI"), layoutType = "title"))
                                        }

                                        slideDecks.add(
                                            0,
                                            SlideDeck(
                                                id = "deck_ai_${System.currentTimeMillis()}",
                                                title = aiGeneratorTopic,
                                                authorName = "AI Creator 🤖",
                                                authorUsername = "ai_slide_generator",
                                                authorAvatar = "AI",
                                                category = "Công nghệ",
                                                description = "Slide thuyết trình được tự động sinh bởi AI Trợ lý thông minh dựa trên tri thức lớn.",
                                                viewsCount = 1,
                                                likesCount = 0,
                                                slides = generatedSlides
                                            )
                                        )

                                        isGeneratingSlideDeck = false
                                        showAiGeneratorSheet = false
                                        android.widget.Toast.makeText(context, "Đã tạo Slide bằng AI thành công! 🎉", android.widget.Toast.LENGTH_LONG).show()
                                    }
                                } else {
                                    android.widget.Toast.makeText(context, "Vui lòng nhập chủ đề slide!", android.widget.Toast.LENGTH_SHORT).show()
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = primaryColor, contentColor = Color.Black),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Tiến Hành Tạo Slide ✨", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}
