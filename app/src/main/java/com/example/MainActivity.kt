package com.example

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import coil.compose.AsyncImage
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.screens.*
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.SocialViewModel
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val viewModel: SocialViewModel = viewModel()
            val isDarkByPref by viewModel.isDarkMode.collectAsStateWithLifecycle()

            MyApplicationTheme(darkTheme = isDarkByPref) {
                MainAppContainer(viewModel)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainAppContainer(viewModel: SocialViewModel) {
    val coroutineScope = rememberCoroutineScope()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val isDarkByPref by viewModel.isDarkMode.collectAsStateWithLifecycle()
    val currentUser by viewModel.currentUser.collectAsStateWithLifecycle()

    var currentRoute by remember { mutableStateOf("feed") }
    var activeProfileUsername by remember { mutableStateOf<String?>(null) } // Target user to show on ProfileScreen
    var activeChatUsername by remember { mutableStateOf<String?>(null) }

    val handleNavigateToProfile: (String) -> Unit = { username ->
        activeProfileUsername = username
        currentRoute = "profile"
    }

    val handleNavigateToChat: (String) -> Unit = { username ->
        activeChatUsername = username
        currentRoute = "messages"
    }

    // Modal Drawer content
    val drawerContent = @Composable {
        ModalDrawerSheet(
            modifier = Modifier
                .width(290.dp)
                .fillMaxHeight(),
            drawerContainerColor = MaterialTheme.colorScheme.surface,
            drawerShape = RoundedCornerShape(topEnd = 16.dp, bottomEnd = 16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                // Drawer Header (Brand Logo & Name)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            currentRoute = "feed"
                            activeProfileUsername = null
                            coroutineScope.launch { drawerState.close() }
                        }
                        .padding(vertical = 12.dp, horizontal = 8.dp)
                        .testTag("drawer_header_logo")
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color.Transparent)
                            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(12.dp))
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.netvibe_logo),
                            contentDescription = "NetVibe Logo",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "NetVibe",
                            fontWeight = FontWeight.Black,
                            fontSize = 20.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "Mạng xã hội đa chiều",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
                        )
                    }
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outline, modifier = Modifier.padding(vertical = 8.dp))

                // Navigation Items
                val menuItems = listOf(
                    DrawerItemData("feed", "Bảng tin (Feed)", Icons.Default.Home, Icons.Outlined.Home),
                    DrawerItemData("search", "Tìm kiếm (Search)", Icons.Default.Search, Icons.Outlined.Search),
                    DrawerItemData("messages", "Hộp thư tin nhắn", Icons.Default.Forum, Icons.Outlined.Forum),
                    DrawerItemData("ai_chat", "Trợ lý AI Suy nghĩ Sâu", Icons.Default.Psychology, Icons.Outlined.Psychology),
                    DrawerItemData("profile", "Hồ sơ cá nhân", Icons.Default.Person, Icons.Outlined.Person),
                    DrawerItemData("settings", "Cài đặt & Tùy chọn", Icons.Default.Settings, Icons.Outlined.Settings)
                )

                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    menuItems.forEach { item ->
                        val isSelected = currentRoute == item.route && (item.route != "profile" || activeProfileUsername == null)
                        
                        NavigationDrawerItem(
                            label = { Text(item.title, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal, fontSize = 14.sp) },
                            selected = isSelected,
                            onClick = {
                                currentRoute = item.route
                                if (item.route == "profile") {
                                    activeProfileUsername = null // Reset to current user
                                }
                                coroutineScope.launch { drawerState.close() }
                            },
                            icon = {
                                Icon(
                                    imageVector = if (isSelected) item.filledIcon else item.outlinedIcon,
                                    contentDescription = item.title,
                                    tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
                                )
                            },
                            shape = RoundedCornerShape(12.dp),
                            colors = NavigationDrawerItemDefaults.colors(
                                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
                                unselectedContainerColor = Color.Transparent
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .testTag("drawer_item_${item.route}")
                        )
                    }
                }

                // Drawer Footer: Dark mode switcher & Copyright footer
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (isDarkByPref) Icons.Default.DarkMode else Icons.Default.LightMode,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Chế độ tối", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        }
                        Switch(
                            checked = isDarkByPref,
                            onCheckedChange = { viewModel.toggleDarkMode() },
                            modifier = Modifier
                                .scale(0.8f)
                                .testTag("drawer_dark_mode_toggle")
                        )
                    }
                }

                Text(
                    text = "Điều khoản • Bảo mật • Trợ giúp\nQuyết Phạm © 2026",
                    fontSize = 10.sp,
                    lineHeight = 14.sp,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp, bottom = 8.dp)
                )
            }
        }
    }

    // Modal navigation drawer wrapper
    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = drawerContent
    ) {
        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(
                    title = {
                        // Title/Logo clicks returns home
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clickable {
                                    currentRoute = "feed"
                                    activeProfileUsername = null
                                }
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(28.dp)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(Color.Transparent)
                            ) {
                                Image(
                                    painter = painterResource(id = R.drawable.netvibe_logo),
                                    contentDescription = "Logo",
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "NetVibe",
                                fontWeight = FontWeight.Black,
                                fontSize = 18.sp,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    },
                    navigationIcon = {
                        IconButton(
                            onClick = { coroutineScope.launch { drawerState.open() } },
                            modifier = Modifier.testTag("open_drawer_button")
                        ) {
                            Icon(Icons.Default.Menu, contentDescription = "Menu chính")
                        }
                    },
                    actions = {
                        // Quick Action: Top icon triggers deep-thinking AI chat!
                        IconButton(
                            onClick = { currentRoute = "ai_chat" },
                            modifier = Modifier.testTag("quick_ai_assist")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Psychology,
                                contentDescription = "AI Trợ lý",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }

                        // My Avatar clickable triggers my profile!
                        currentUser?.let { me ->
                            AsyncImage(
                                model = me.avatarUrl,
                                contentDescription = "My Profile",
                                modifier = Modifier
                                    .padding(end = 12.dp)
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .border(1.dp, MaterialTheme.colorScheme.primary, CircleShape)
                                    .clickable {
                                        activeProfileUsername = null
                                        currentRoute = "profile"
                                    },
                                contentScale = ContentScale.Crop
                            )
                        }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = MaterialTheme.colorScheme.background
                    )
                )
            },
            bottomBar = {
                // Mobile bottom bar menu: Home, Search, Add (+), Activity (Heart), Profile
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    contentColor = MaterialTheme.colorScheme.primary,
                    tonalElevation = 0.dp
                ) {
                    val isFeedActive = currentRoute == "feed"
                    NavigationBarItem(
                        selected = isFeedActive,
                        onClick = {
                            currentRoute = "feed"
                            activeProfileUsername = null
                        },
                        icon = { Icon(if (isFeedActive) Icons.Default.Home else Icons.Outlined.Home, contentDescription = "Trang chủ") },
                        label = { Text("Trang chủ", fontSize = 10.sp) },
                        modifier = Modifier.testTag("nav_feed")
                    )

                    val isSearchActive = currentRoute == "search"
                    NavigationBarItem(
                        selected = isSearchActive,
                        onClick = { currentRoute = "search" },
                        icon = { Icon(if (isSearchActive) Icons.Default.Search else Icons.Outlined.Search, contentDescription = "Tìm kiếm") },
                        label = { Text("Tìm kiếm", fontSize = 10.sp) },
                        modifier = Modifier.testTag("nav_search")
                    )

                    val isCreateActive = currentRoute == "create_post"
                    NavigationBarItem(
                        selected = isCreateActive,
                        onClick = { currentRoute = "create_post" },
                        icon = { Icon(if (isCreateActive) Icons.Default.AddCircle else Icons.Outlined.AddCircle, contentDescription = "Tạo bưu thiếp") },
                        label = { Text("Tạo bài", fontSize = 10.sp) },
                        modifier = Modifier.testTag("nav_create_post")
                    )

                    val isActivityActive = currentRoute == "activity"
                    NavigationBarItem(
                        selected = isActivityActive,
                        onClick = { currentRoute = "activity" },
                        icon = { Icon(if (isActivityActive) Icons.Default.Favorite else Icons.Outlined.FavoriteBorder, contentDescription = "Hoạt động") },
                        label = { Text("Hoạt động", fontSize = 10.sp) },
                        modifier = Modifier.testTag("nav_activity")
                    )

                    val isMyProfileActive = currentRoute == "profile" && activeProfileUsername == null
                    NavigationBarItem(
                        selected = isMyProfileActive,
                        onClick = {
                            activeProfileUsername = null
                            currentRoute = "profile"
                        },
                        icon = { Icon(if (isMyProfileActive) Icons.Default.Person else Icons.Outlined.Person, contentDescription = "Hồ sơ") },
                        label = { Text("Hồ sơ", fontSize = 10.sp) },
                        modifier = Modifier.testTag("nav_profile")
                    )
                }
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                when (currentRoute) {
                    "feed" -> FeedScreen(
                        viewModel = viewModel,
                        onNavigateToProfile = handleNavigateToProfile
                    )
                    "search" -> SearchScreen(
                        viewModel = viewModel,
                        onNavigateToProfile = handleNavigateToProfile
                    )
                    "create_post" -> CreatePostScreen(
                        viewModel = viewModel,
                        onPostCreated = { currentRoute = "feed" }
                    )
                    "activity" -> ActivityScreen(
                        viewModel = viewModel,
                        onNavigateToProfile = handleNavigateToProfile
                    )
                    "profile" -> ProfileScreen(
                        viewModel = viewModel,
                        targetUsername = activeProfileUsername,
                        onNavigateToProfile = handleNavigateToProfile,
                        onNavigateToChat = handleNavigateToChat,
                        onBack = if (activeProfileUsername != null) {
                            { activeProfileUsername = null }
                        } else null
                    )
                    "ai_chat" -> AIChatScreen(
                        viewModel = viewModel
                    )
                    "messages" -> MessagesScreen(
                        viewModel = viewModel,
                        initialChatUsername = activeChatUsername,
                        onChatOpened = { activeChatUsername = null }
                    )
                    "settings" -> SettingsScreen(
                        viewModel = viewModel
                    )
                }
            }
        }
    }
}

data class DrawerItemData(
    val route: String,
    val title: String,
    val filledIcon: ImageVector,
    val outlinedIcon: ImageVector
)


