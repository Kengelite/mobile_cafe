@file:OptIn(ExperimentalMaterial3Api::class)
package com.example.mycafeme

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController

private val Cream      = Color(0xFFFAF6F1)
private val Espresso   = Color(0xFF2C1A0E)
private val Latte      = Color(0xFF8B5E3C)
private val CardBg     = Color(0xFFFFFFFF)
private val DangerRed  = Color(0xFFE57373)

@Composable
fun CartScreen(navController: NavController, viewModel: AppViewModel) {
    val cartItems by viewModel.cartItems.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.fetchCartItems()
    }

    val subTotal = cartItems.sumOf { it.price * it.quantity }
    val serviceFee = if (cartItems.isNotEmpty()) 10.0 else 0.0
    val netTotal = subTotal + serviceFee

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("ตะกร้าสินค้า", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Espresso) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Espresso)
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Cream)
            )
        },
        containerColor = Cream
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center), color = Latte)
            } else if (cartItems.isEmpty()) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("🛒", fontSize = 64.sp)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("ตะกร้าของคุณยังว่างเปล่า", color = Latte, fontWeight = FontWeight.Medium)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(cartItems) { item ->
                        CartItemCard(
                            item = item,
                            onRemove = {
                                viewModel.removeCartItem(item.detailId)
                            }
                        )
                    }

                    item {
                        Spacer(modifier = Modifier.height(16.dp))
                        CartSummaryFooter(subTotal, serviceFee, netTotal) {
                            // TODO: ยืนยันสั่งซื้อ
                        }
                        Spacer(modifier = Modifier.height(32.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun CartItemCard(item: CartItemData, onRemove: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CardBg),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // รูปย่อชื่อเมนู
            Box(
                modifier = Modifier.size(55.dp).clip(RoundedCornerShape(12.dp)).background(Latte.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                val initial = if (item.menuName.isNotEmpty()) item.menuName.take(1) else "?"
                Text(text = initial, fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Latte)
            }

            Spacer(modifier = Modifier.width(16.dp))

            // รายละเอียดชื่อและจำนวน (แบบ Fix เลข)
            Column(modifier = Modifier.weight(1f)) {
                Text(text = item.menuName, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Espresso)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "${item.price} ฿", fontSize = 14.sp, color = Latte)
                    Text(text = "  •  จำนวน: ${item.quantity}", fontSize = 13.sp, color = Color.Gray)
                }
            }

            // 👈 เหลือแค่ปุ่มลบปุ่มเดียวครับ
            IconButton(
                onClick = onRemove,
                modifier = Modifier.background(DangerRed.copy(alpha = 0.1f), RoundedCornerShape(12.dp))
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Remove",
                    tint = DangerRed,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
fun CartSummaryFooter(subTotal: Double, serviceFee: Double, netTotal: Double, onCheckoutClick: () -> Unit) {
    Surface(color = CardBg, shadowElevation = 16.dp, shape = RoundedCornerShape(24.dp)) {
        Column(modifier = Modifier.padding(24.dp).fillMaxWidth()) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("ค่าอาหารรวม", color = Color.Gray, fontSize = 14.sp)
                Text("${subTotal} ฿", fontWeight = FontWeight.Medium, color = Espresso, fontSize = 14.sp)
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("ค่าบริการ", color = Color.Gray, fontSize = 14.sp)
                Text("${serviceFee} ฿", fontWeight = FontWeight.Medium, color = Espresso, fontSize = 14.sp)
            }
            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = Cream, thickness = 2.dp)
            Spacer(modifier = Modifier.height(12.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("ยอดสุทธิ", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Espresso)
                Text("${netTotal} ฿", fontWeight = FontWeight.ExtraBold, fontSize = 22.sp, color = Latte)
            }
            Spacer(modifier = Modifier.height(20.dp))
            Button(
                onClick = onCheckoutClick,
                modifier = Modifier.fillMaxWidth().height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Espresso),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text("ยืนยันการสั่งซื้อ", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
            }
        }
    }
}