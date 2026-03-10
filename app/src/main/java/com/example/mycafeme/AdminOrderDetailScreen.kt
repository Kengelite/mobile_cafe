@file:OptIn(ExperimentalMaterial3Api::class)
package com.example.mycafeme

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController

private val Cream      = Color(0xFFFAF6F1)
private val Espresso   = Color(0xFF2C1A0E)
private val Latte      = Color(0xFF8B5E3C)
private val CardBg     = Color(0xFFFFFFFF)

@Composable
fun AdminOrderDetailScreen(navController: NavController, viewModel: AppViewModel, orderId: String) {
    val orderDetails by viewModel.orderDetails.collectAsState()

    // โหลดข้อมูลรายละเอียดออเดอร์ทันทีที่เข้ามา
    LaunchedEffect(orderId) {
        viewModel.fetchOrderDetails(orderId)
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("รายละเอียดออเดอร์", fontSize = 17.sp, fontWeight = FontWeight.SemiBold, color = Espresso)
                        Text("#${orderId.take(8)}", fontSize = 12.sp, color = Latte)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Outlined.ArrowBack, contentDescription = "กลับ", tint = Espresso)
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Cream)
            )
        },
        containerColor = Cream
    ) { padding ->
        if (orderDetails.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text("กำลังโหลดรายการสินค้า...", color = Latte)
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(vertical = 16.dp)
            ) {
                item {
                    Text("สินค้าที่สั่ง", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = Latte, modifier = Modifier.padding(bottom = 4.dp))
                }

                items(orderDetails) { detail ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = CardBg),
                        elevation = CardDefaults.cardElevation(0.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp).fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // จำนวน (Quantity)
                            Box(
                                modifier = Modifier.size(36.dp).background(Cream, RoundedCornerShape(8.dp)),
                                contentAlignment = Alignment.Center
                            ) { Text("${detail.quantity}x", fontWeight = FontWeight.Bold, color = Espresso) }

                            Spacer(modifier = Modifier.width(12.dp))

                            // ชื่อเมนู
                            Column(modifier = Modifier.weight(1f)) {
                                Text(text = detail.menuName ?: "เมนูไม่ระบุ", fontSize = 15.sp, fontWeight = FontWeight.Medium, color = Espresso)
                            }

                            // ราคา (คำนวณราคาต่อชิ้น * จำนวนมาให้เลยก็ได้จากหลังบ้าน)
                            Text(text = "${detail.price} ฿", fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = Latte)
                        }
                    }
                }
            }
        }
    }
}