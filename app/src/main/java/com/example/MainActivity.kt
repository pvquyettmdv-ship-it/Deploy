package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.MainViewModel
import com.example.ui.screens.*
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                MainLayout(viewModel)
            }
        }
    }
}

@Composable
fun MainLayout(viewModel: MainViewModel) {
    val currentTab by viewModel.currentTab.collectAsState()

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            BottomNavigationBar(
                activeTab = currentTab,
                onTabSelected = { viewModel.setTab(it) }
            )
        },
        contentWindowInsets = WindowInsets.navigationBars
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = innerPadding.calculateBottomPadding())
        ) {
            Crossfade(
                targetState = currentTab,
                animationSpec = tween(durationMillis = 300)
            ) { tab ->
                when (tab) {
                    "Feed" -> FeedScreen(
                        viewModel = viewModel,
                        onProductClick = { productId ->
                            viewModel.selectProduct(productId)
                            viewModel.setTab("Shop")
                        }
                    )
                    "Shop" -> SocialSlideScreen(
                        viewModel = viewModel,
                        onEnterLivestream = {
                            viewModel.setTab("Live")
                        }
                    )
                    "Live" -> LiveStreamScreen(
                        viewModel = viewModel,
                        onProductClick = { productId ->
                            viewModel.selectProduct(productId)
                            viewModel.setTab("Shop")
                        }
                    )
                    "Chat" -> ChatScreen(
                        viewModel = viewModel,
                        onProductClick = { productId ->
                            viewModel.selectProduct(productId)
                            viewModel.setTab("Shop")
                        }
                    )
                    "Profile" -> ProfileScreen(
                        viewModel = viewModel
                    )
                }
            }
        }
    }
}

@Composable
fun BottomNavigationBar(
    activeTab: String,
    onTabSelected: (String) -> Unit
) {
    NavigationBar(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding(),
        tonalElevation = 8.dp,
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        val navItems = listOf(
            NavigationItemData("Feed", "Bản tin", Icons.Filled.DynamicFeed, Icons.Outlined.DynamicFeed),
            NavigationItemData("Shop", "SocialSlide", Icons.Filled.Slideshow, Icons.Outlined.Slideshow),
            NavigationItemData("Live", "Trực tiếp", Icons.Filled.LiveTv, Icons.Outlined.LiveTv),
            NavigationItemData("Chat", "Phòng Nhóm", Icons.Filled.Groups, Icons.Outlined.Groups),
            NavigationItemData("Profile", "Tôi", Icons.Filled.Person, Icons.Outlined.Person)
        )

        navItems.forEach { item ->
            val isSelected = activeTab == item.tabKey
            NavigationBarItem(
                selected = isSelected,
                onClick = { onTabSelected(item.tabKey) },
                icon = {
                    Icon(
                        imageVector = if (isSelected) item.activeIcon else item.inactiveIcon,
                        contentDescription = item.label
                    )
                },
                label = {
                    Text(
                        text = item.label,
                        fontSize = 11.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                    )
                },
                modifier = Modifier.testTag("nav_item_${item.tabKey.lowercase()}"),
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = MaterialTheme.colorScheme.primary,
                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    selectedTextColor = MaterialTheme.colorScheme.primary,
                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    indicatorColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        }
    }
}

data class NavigationItemData(
    val tabKey: String,
    val label: String,
    val activeIcon: ImageVector,
    val inactiveIcon: ImageVector
)
