@file:OptIn(ExperimentalMaterial3Api::class)
package com.example.mycafeme

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import kotlinx.coroutines.launch

private val Cream      = Color(0xFFFAF6F1)
private val Espresso   = Color(0xFF2C1A0E)
private val Latte      = Color(0xFF8B5E3C)
private val CardBg     = Color(0xFFFFFFFF)

@Composable
fun MenuListScreen(navController: NavController, viewModel: AppViewModel, cafeId: String) {
    val menus by viewModel.menus.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    val cafes by viewModel.cafes.collectAsState()
    val currentCafe = cafes.find { it.id == cafeId }

    // 👈 สร้างตัวแปรสำหรับควบคุมการแจ้งเตือน (Snackbar)
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(cafeId) {
        viewModel.fetchMenus(cafeId)
    }

    Scaffold(
        // 👈 แปะ Snackbar ไว้ใน Scaffold เพื่อให้มันโผล่ขึ้นมาตรงขอบล่างของจอ
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("สั่งเครื่องดื่ม", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Espresso) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Espresso)
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Cream)
            )
        },
        containerColor = Cream,
        floatingActionButton = {
            ExtendedFloatingActionButton(
                // 👈 เปลี่ยนจาก TODO เป็นคำสั่งนี้ครับ
                onClick = { navController.navigate("cart") },
                containerColor = Espresso,
                contentColor = Color.White,
                icon = { Icon(Icons.Default.ShoppingCart, null) },
                text = { Text("ดูตะกร้าสินค้า") }
            )
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center), color = Latte)
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    if (currentCafe != null) {
                        item {
                            CafeHeader(cafe = currentCafe)
                            Spacer(modifier = Modifier.height(16.dp))
                            HorizontalDivider(color = Latte.copy(alpha = 0.2f), thickness = 1.dp)
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                    }

                    if (menus.isEmpty()) {
                        item {
                            Text(
                                text = "ยังไม่มีเมนูในขณะนี้",
                                modifier = Modifier.fillMaxWidth().padding(top = 40.dp),
                                textAlign = TextAlign.Center,
                                color = Latte
                            )
                        }
                    } else {
                        items(menus) { menu ->
                            MenuCard(menu) { finalQuantity ->
                                //  เมื่อกด "เพิ่ม" ให้ยิง API ไปหา Node.js
                                viewModel.addToCart(
                                    cafeId = cafeId,
                                    menuId = menu.id,
                                    quantity = finalQuantity,
                                    price = menu.price.toDoubleOrNull() ?: 0.0,
                                    onSuccess = {
                                        // ยิงสำเร็จ โชว์แจ้งเตือนสวยๆ
                                        coroutineScope.launch {
                                            snackbarHostState.showSnackbar("เพิ่ม ${menu.name} ลงตะกร้าแล้ว!")
                                        }
                                    },
                                    onError = { errorMsg ->
                                        // ยิงไม่สำเร็จ โชว์ข้อผิดพลาด
                                        coroutineScope.launch {
                                            snackbarHostState.showSnackbar("$errorMsg")
                                        }
                                    }
                                )
                            }
                        }
                    }

                    item { Spacer(modifier = Modifier.height(80.dp)) }
                }
            }
        }
    }
}

// ── ส่วนหัวโชว์ข้อมูลร้าน ──
@Composable
fun CafeHeader(cafe: CafeData) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(90.dp)
                .clip(CircleShape)
                .background(Latte.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            val initial = if (cafe.name.isNotEmpty()) cafe.name.take(1) else "?"
            Text(text = initial, fontSize = 36.sp, fontWeight = FontWeight.Bold, color = Latte)
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(text = cafe.name, fontSize = 24.sp, fontWeight = FontWeight.ExtraBold, color = Espresso)
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = cafe.location,
            fontSize = 14.sp,
            color = Color.Gray,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 24.dp)
        )
        Spacer(modifier = Modifier.height(8.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.background(CardBg, RoundedCornerShape(12.dp)).padding(horizontal = 12.dp, vertical = 6.dp)
        ) {
            Icon(Icons.Filled.Star, contentDescription = "Rating", tint = Color(0xFFFFC107), modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text(text = cafe.rating ?: "0.0", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Espresso)
        }
    }
}

// ── ส่วนการ์ดเมนู ──
@Composable
fun MenuCard(menu: MenuData, onAddClick: (Int) -> Unit) {
    var quantity by rememberSaveable { mutableIntStateOf(1) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = CardBg),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(65.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Latte.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                val initial = if (menu.name.isNotEmpty()) menu.name.take(1) else "?"
                Text(text = initial, fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Latte)
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(text = menu.name, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Espresso)
                Text(text = "${menu.price} ฿", fontSize = 14.sp, color = Latte, fontWeight = FontWeight.SemiBold)

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    IconButton(
                        onClick = { if (quantity > 1) quantity-- },
                        modifier = Modifier.size(30.dp).background(if (quantity > 1) Cream else Cream.copy(alpha = 0.4f), CircleShape),
                        enabled = quantity > 1
                    ) {
                        Icon(Icons.Default.Remove, null, modifier = Modifier.size(16.dp), tint = if (quantity > 1) Espresso else Color.Gray)
                    }

                    Text(text = quantity.toString(), fontWeight = FontWeight.ExtraBold, fontSize = 16.sp, color = Espresso)

                    IconButton(
                        onClick = { quantity++ },
                        modifier = Modifier.size(30.dp).background(Cream, CircleShape)
                    ) {
                        Icon(Icons.Default.Add, null, modifier = Modifier.size(16.dp), tint = Espresso)
                    }
                }
            }

            Button(
                onClick = {
                    onAddClick(quantity)
                    quantity = 1 // รีเซ็ตจำนวนให้กลับมาเป็น 1 ทันทีหลังกดปุ่ม
                },
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Espresso),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Text("เพิ่ม", fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}