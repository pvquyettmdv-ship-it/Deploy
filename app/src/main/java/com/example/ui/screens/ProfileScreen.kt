package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.*
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
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.Product
import com.example.ui.MainViewModel

@Composable
fun ProfileScreen(viewModel: MainViewModel) {
    val products by viewModel.allProducts.collectAsState()

    var isSellerMode by remember { mutableStateOf(false) } // Switches between Creator and Seller Center

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .verticalScroll(rememberScrollState())
    ) {
        // Upper Profile Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = if (isSellerMode) "Kênh người bán VibeCart" else "Hồ sơ của tôi",
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                color = MaterialTheme.colorScheme.onSurface
            )

            // Dynamic Mode switcher
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(MaterialTheme.colorScheme.primaryContainer)
                    .clickable { isSellerMode = !isSellerMode }
                    .padding(horizontal = 12.dp, vertical = 6.dp)
                    .testTag("switch_seller_mode")
            ) {
                Icon(
                    imageVector = if (isSellerMode) Icons.Filled.Storefront else Icons.Filled.Person,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = if (isSellerMode) "Seller Mode" else "Creator Mode",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }

        Divider(color = MaterialTheme.colorScheme.outlineVariant)

        if (!isSellerMode) {
            CreatorProfileView(viewModel)
        } else {
            SellerCenterView(viewModel, products)
        }
    }
}

@Composable
fun CreatorProfileView(viewModel: MainViewModel) {
    val posts by viewModel.allPosts.collectAsState()

    Column(modifier = Modifier.fillMaxWidth()) {
        // Cover Photo & Avatar
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(140.dp)
                .drawBehind {
                    drawRect(
                        brush = Brush.horizontalGradient(
                            colors = listOf(Color(0xFFC9D6FF), Color(0xFFE2E2E2))
                        )
                    )
                }
        ) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(start = 16.dp, bottom = 12.dp)
                    .size(64.dp)
                    .clip(CircleShape)
                    .border(2.dp, Color.White, CircleShape)
                    .background(Color.Gray),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "C", color = Color.White, fontWeight = FontWeight.ExtraBold, fontSize = 24.sp)
            }
        }

        // Details
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = "Chanh Beauty 🌸", fontWeight = FontWeight.Bold, fontSize = 18.sp)
            Text(text = "Social Commerce Creator. Chuyên gia review mỹ phẩm, skincare và xu hướng thời trang dạo phố.", color = Color.Gray, fontSize = 12.sp)
            Spacer(modifier = Modifier.height(12.dp))
            
            // Stats grid
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                ProfileStatBlock(num = "24K", label = "Theo dõi")
                ProfileStatBlock(num = "450K", label = "Lượt thích")
                ProfileStatBlock(num = "15%", label = "HH trung bình")
                ProfileStatBlock(num = "4.9★", label = "Đánh giá")
            }
        }

        Divider(modifier = Modifier.padding(vertical = 8.dp), color = MaterialTheme.colorScheme.outlineVariant)

        // KOL Affiliate Dashboard overview
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(text = "Chương trình KOL Affiliate 💰", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(text = "Hoa hồng đã tích lũy", fontSize = 11.sp, color = Color.Gray)
                        Text(text = "2,450,000đ", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = MaterialTheme.colorScheme.primary)
                    }
                    Column {
                        Text(text = "Số đơn giới thiệu", fontSize = 11.sp, color = Color.Gray)
                        Text(text = "142 đơn", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = MaterialTheme.colorScheme.primary)
                    }
                }
            }
        }

        // Creator posts portfolio header
        Text(
            text = "Bài đăng của tôi (${posts.size})",
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )

        // Show posts portfolio
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp)
        ) {
            posts.chunked(2).forEach { rowItems ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    rowItems.forEach { post ->
                        Card(
                            modifier = Modifier
                                .weight(1f)
                                .height(160.dp)
                                .padding(vertical = 4.dp),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .drawBehind {
                                        drawRect(
                                            brush = Brush.verticalGradient(
                                                colors = listOf(Color(0xFF8E2DE2), Color(0xFF4A00E0))
                                            )
                                        )
                                    }
                                    .padding(8.dp)
                            ) {
                                Icon(
                                    imageVector = if (post.postType == "video") Icons.Filled.PlayCircle else Icons.Filled.Photo,
                                    contentDescription = null,
                                    tint = Color.White.copy(alpha = 0.6f),
                                    modifier = Modifier
                                        .size(32.dp)
                                        .align(Alignment.Center)
                                )
                                Text(
                                    text = post.caption,
                                    color = Color.White,
                                    fontSize = 11.sp,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.align(Alignment.BottomStart)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ProfileStatBlock(num: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = num, fontWeight = FontWeight.ExtraBold, fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurface)
        Text(text = label, fontSize = 11.sp, color = Color.Gray)
    }
}

@Composable
fun SellerCenterView(viewModel: MainViewModel, products: List<Product>) {
    val ordersCount by viewModel.merchantOrdersCount.collectAsState()
    val totalRevenue by viewModel.merchantSales.collectAsState()
    val followersCount by viewModel.merchantFollowers.collectAsState()
    val convRate by viewModel.merchantConversionRate.collectAsState()
    val aiLoading by viewModel.aiLoading.collectAsState()

    var showAddProductDialog by remember { mutableStateOf(false) }
    var newProdName by remember { mutableStateOf("") }
    var newProdCat by remember { mutableStateOf("Fashion") }
    var newProdPrice by remember { mutableStateOf("") }
    var newProdDesc by remember { mutableStateOf("") }

    Column(modifier = Modifier.padding(16.dp)) {
        // Merchant Metrics Title
        Text(text = "Chỉ số doanh thu 📊", fontWeight = FontWeight.Bold, fontSize = 14.sp)
        Spacer(modifier = Modifier.height(12.dp))

        // Metrics KPI cards grid
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            KpiCard(title = "Doanh thu", num = "${totalRevenue.toInt()}đ", modifier = Modifier.weight(1f))
            KpiCard(title = "Đơn hàng", num = "$ordersCount", modifier = Modifier.weight(1f))
        }
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            KpiCard(title = "Khách hàng", num = "$followersCount L/quan", modifier = Modifier.weight(1f))
            KpiCard(title = "Tỉ lệ mua", num = "$convRate%", modifier = Modifier.weight(1f))
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Custom M3 statistics canvas Line chart drawing
        Text(text = "Biểu đồ tăng trưởng doanh số (7 ngày)", fontWeight = FontWeight.Bold, fontSize = 13.sp)
        Spacer(modifier = Modifier.height(12.dp))
        
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(140.dp)
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                .padding(16.dp)
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val chartWidth = size.width
                val chartHeight = size.height
                val points = listOf(0.1f, 0.3f, 0.25f, 0.6f, 0.45f, 0.8f, 1.0f) // Normalized growth metrics
                
                // Draw coordinate reference grid lines
                drawLine(
                    color = Color.Gray.copy(alpha = 0.2f),
                    start = Offset(0f, chartHeight / 2),
                    end = Offset(chartWidth, chartHeight / 2),
                    strokeWidth = 1.dp.toPx()
                )
                drawLine(
                    color = Color.Gray.copy(alpha = 0.2f),
                    start = Offset(0f, chartHeight),
                    end = Offset(chartWidth, chartHeight),
                    strokeWidth = 1.dp.toPx()
                )

                // Build line chart path
                val path = Path()
                val stepX = chartWidth / (points.size - 1)
                
                points.forEachIndexed { idx, pointVal ->
                    val x = idx * stepX
                    val y = chartHeight - (pointVal * chartHeight)
                    if (idx == 0) {
                        path.moveTo(x, y)
                    } else {
                        path.lineTo(x, y)
                    }
                    // Draw node circles
                    drawCircle(
                        color = Color(0xFFFF0055),
                        radius = 4.dp.toPx(),
                        center = Offset(x, y)
                    )
                }

                drawPath(
                    path = path,
                    color = Color(0xFFFF0055),
                    style = Stroke(width = 3.dp.toPx())
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Product Catalog Crud row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = "Quản lý kho hàng sản phẩm", fontWeight = FontWeight.Bold, fontSize = 14.sp)
            Button(
                onClick = { showAddProductDialog = true },
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.testTag("btn_seller_add_product")
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = "Thêm sản phẩm", fontSize = 11.sp)
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Local shop products list
        products.forEach { prod ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f))
                    .padding(10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = prod.name, fontWeight = FontWeight.Bold, fontSize = 13.sp, maxLines = 1)
                    Text(text = "Tồn kho: ${prod.stock} • Danh mục: ${prod.category}", color = Color.Gray, fontSize = 11.sp)
                }
                Text(text = "${prod.price.toInt()}đ", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold, fontSize = 13.sp)
            }
        }
    }

    // Add Product Dialog Form
    if (showAddProductDialog) {
        val isPriceValid = newProdPrice.toDoubleOrNull() != null && (newProdPrice.toDoubleOrNull() ?: 0.0) > 0.0
        val isNameValid = newProdName.isNotBlank()
        AlertDialog(
            onDismissRequest = { showAddProductDialog = false },
            confirmButton = {
                Button(
                    onClick = {
                        val priceVal = newProdPrice.toDoubleOrNull() ?: 0.0
                        viewModel.addProductBySeller(
                            newProdName,
                            newProdCat,
                            priceVal,
                            priceVal * 1.3,
                            newProdDesc
                        )
                        showAddProductDialog = false
                        // Reset
                        newProdName = ""
                        newProdPrice = ""
                        newProdDesc = ""
                    },
                    enabled = isNameValid && isPriceValid,
                    modifier = Modifier.testTag("btn_seller_submit_add")
                ) {
                    Text("Lưu sản phẩm")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddProductDialog = false }) {
                    Text("Hủy")
                }
            },
            title = { Text("Đăng bán sản phẩm mới") },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    val isNameBlank = newProdName.isBlank()
                    OutlinedTextField(
                        value = newProdName,
                        onValueChange = { newProdName = it },
                        label = { Text("Tên sản phẩm") },
                        isError = isNameBlank,
                        supportingText = {
                            if (isNameBlank) {
                                Text("Tên sản phẩm không được để trống", color = MaterialTheme.colorScheme.error, fontSize = 11.sp)
                            }
                        },
                        modifier = Modifier.fillMaxWidth().testTag("input_seller_prod_name")
                    )

                    val isPriceInvalid = newProdPrice.toDoubleOrNull() == null || (newProdPrice.toDoubleOrNull() ?: 0.0) <= 0.0
                    OutlinedTextField(
                        value = newProdPrice,
                        onValueChange = { newProdPrice = it },
                        label = { Text("Giá bán (VND)") },
                        isError = isPriceInvalid,
                        supportingText = {
                            if (isPriceInvalid) {
                                Text("Giá bán phải là số lớn hơn 0", color = MaterialTheme.colorScheme.error, fontSize = 11.sp)
                            }
                        },
                        modifier = Modifier.fillMaxWidth().testTag("input_seller_prod_price")
                    )

                    // AI Description helper trigger
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "Mô tả sản phẩm", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        TextButton(
                            onClick = {
                                if (newProdName.isNotBlank()) {
                                    viewModel.generateProductDescription(newProdName) { desc ->
                                        newProdDesc = desc
                                    }
                                }
                            },
                            modifier = Modifier.testTag("btn_seller_ai_desc")
                        ) {
                            Icon(Icons.Filled.AutoAwesome, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(text = "Tạo mô tả AI ✨", fontSize = 11.sp)
                        }
                    }

                    if (aiLoading) {
                        LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                    }

                    OutlinedTextField(
                        value = newProdDesc,
                        onValueChange = { newProdDesc = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(110.dp),
                        maxLines = 4
                    )
                }
            },
            shape = RoundedCornerShape(20.dp)
        )
    }
}

@Composable
fun KpiCard(title: String, num: String, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(text = title, fontSize = 11.sp, color = Color.Gray)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = num, fontWeight = FontWeight.Bold, fontSize = 15.sp)
        }
    }
}
