package com.example.mycafeme

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController

import coil.compose.AsyncImage
import androidx.compose.ui.layout.ContentScale


// ── โทนสีกาแฟมินิมอล ──────────────────────────────────────
private val Cream      = Color(0xFFFAF6F1)
private val Espresso   = Color(0xFF2C1A0E)
private val Latte      = Color(0xFF8B5E3C)
private val HeartRed   = Color(0xFFE57373) // สีแดงนวลๆ สำหรับหัวใจ

@Composable
fun HomeScreen(navController: NavController, viewModel: AppViewModel) {
    val cafes by viewModel.cafes.collectAsState()
    val role by viewModel.userRole.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.fetchCafes()
    }

    Scaffold(
        containerColor = Cream
    ) { padding ->
        if (isLoading && cafes.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Latte)
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    Column(modifier = Modifier.padding(bottom = 8.dp)) {
                        Text(
                            text = if (role == "admin") "ระบบจัดการร้านค้า" else "ค้นหาร้านกาแฟ",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = Espresso
                        )
                        Text(
                            text = if (role == "admin") "ดูภาพรวมร้านค้าทั้งหมดในระบบ" else "เลือกสั่งกาแฟจากร้านโปรดของคุณ",
                            fontSize = 14.sp,
                            color = Latte
                        )
                    }
                }

                items(cafes) { cafe ->
                    // 👈 1. ส่งสถานะและ Callback สำหรับกดหัวใจเข้าไป
                    CafeCard(
                        cafe = cafe,
                        onFavoriteClick = {
                            // 👈 ตรงนี้ไว้เรียก viewModel.toggleFavorite(cafe.id) ในอนาคต
                            // ตอนนี้ทำ Mockup เปลี่ยนสถานะในเครื่องไปก่อนครับ
                            cafe.isFavorite = !cafe.isFavorite
                        },
                        onClick = {
                            navController.navigate("menu_list/${cafe.id}")
                        }
                    )
                }

                item { Spacer(modifier = Modifier.height(20.dp)) }
            }
        }
    }
}

// 💡 อย่าลืม import พวกนี้ไว้ด้านบนสุดของไฟล์นะครับ

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CafeCard(cafe: CafeData, onFavoriteClick: () -> Unit, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(120.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // ── ส่วนแสดงรูปภาพ ──
                if (!cafe.img.isNullOrEmpty()) {
                    //  1. นำ Base URL มาต่อกับชื่อรูป
                    //  เปลี่ยนพอร์ต 3000 และชื่อโฟลเดอร์ให้ตรงกับของพี่นะครับ
                    val baseUrl = "http://10.0.2.2:3520/uploads/"
                    val fullImageUrl = baseUrl + cafe.img

                    AsyncImage(
                        model = fullImageUrl, //  2. ใช้ URL เต็มที่ต่อกันแล้ว
                        contentDescription = "ภาพของร้าน ${cafe.name}",
                        modifier = Modifier
                            .size(85.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(Latte.copy(alpha = 0.1f)),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    //  2. ถ้าร้านไม่มีรูป ให้แสดงตัวอักษรย่อเหมือนเดิม (ระบบ Fallback)
                    Box(
                        modifier = Modifier
                            .size(85.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(Latte.copy(alpha = 0.1f)),
                        contentAlignment = Alignment.Center
                    ) {
                        val firstChar = if (cafe.name.isNotEmpty()) cafe.name.take(1) else "?"
                        Text(text = firstChar, fontSize = 32.sp, fontWeight = FontWeight.Bold, color = Latte)
                    }
                }

                Spacer(modifier = Modifier.width(16.dp))

                // ส่วนข้อมูลร้าน (เหมือนเดิม)
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = cafe.name,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Espresso,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.Star, null, tint = Color(0xFFFFC107), modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = cafe.rating ?: "0.0", fontSize = 14.sp, color = Espresso)
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = cafe.location,
                        fontSize = 13.sp,
                        color = Color.Gray,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Icon(
                    imageVector = Icons.Default.KeyboardArrowRight,
                    contentDescription = null,
                    tint = Latte.copy(alpha = 0.3f),
                    modifier = Modifier.padding(end = 32.dp)
                )
            }

            // ปุ่มหัวใจมุมขวาบน (เหมือนเดิม)
            IconButton(
                onClick = onFavoriteClick,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(4.dp)
            ) {
                Icon(
                    imageVector = if (cafe.isFavorite) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                    contentDescription = "Favorite",
                    tint = if (cafe.isFavorite) HeartRed else Latte.copy(alpha = 0.5f),
                    modifier = Modifier.size(22.dp)
                )
            }
        }
    }
}


