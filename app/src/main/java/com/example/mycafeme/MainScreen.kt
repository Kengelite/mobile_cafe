package com.example.mycafeme

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

@Composable
fun MainScreen(navController: NavController, viewModel: AppViewModel) {
    var selectedItem by remember { mutableStateOf(0) }
    val role by viewModel.userRole.collectAsState()

    // 1. แยกรายการเมนูและไอคอนให้ชัดเจนตาม Role
    val items = if (role == "admin") {
        listOf("จัดการร้าน", "ออเดอร์ลูกค้า", "โปรไฟล์")
    } else {
        listOf("หน้าหลัก", "ออเดอร์", "บิลของฉัน", "โปรไฟล์")
    }

    val icons = if (role == "admin") {
        listOf(
            Icons.Filled.Settings,    // จัดการร้าน
            Icons.Filled.ReceiptLong, // ออเดอร์ลูกค้า
            Icons.Filled.Assessment,  // รายงาน
            Icons.Filled.Person       // โปรไฟล์
        )
    } else {
        listOf(
            Icons.Filled.Home,         // หน้าหลัก
            Icons.Filled.ShoppingCart, // ออเดอร์
            Icons.Filled.ReceiptLong,     // บิลของฉัน
            Icons.Filled.Person        // โปรไฟล์
        )
    }

    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp
            ) {
                items.forEachIndexed { index, item ->
                    NavigationBarItem(
                        icon = { Icon(icons[index], contentDescription = item) },
                        label = { Text(item, style = MaterialTheme.typography.labelSmall) },
                        selected = selectedItem == index,
                        onClick = { selectedItem = index }
                    )
                }
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            val lastIndex = items.size - 1

            // 2. แยก Logic การแสดงผลหน้าจอตาม Role และ Index
            if (role == "admin") {
                // --- หน้าจอสำหรับ Admin ---
                when (selectedItem) {
                    0 -> AdminMenuScreen(navController) // หน้าแรก Admin คือจัดการร้าน
                    1 -> AdminOrderScreen(navController, viewModel)
                    2 -> CenterText("หน้ารายงานยอดขายรวม")
                    lastIndex -> ProfileScreen(navController, viewModel)
                }
            } else {
                // --- หน้าจอสำหรับ User (Customer) ---
                when (selectedItem) {
                    0 -> HomeScreen(navController, viewModel)// หน้าแรก User คือหน้าหลักเลือกซื้อ
                    1 -> CartScreen(navController, viewModel)
                    2 -> OrderHistoryScreen(navController, viewModel)
                    lastIndex -> ProfileScreen(navController, viewModel)
                }
            }
        }
    }
}

@Composable
fun CenterText(text: String) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(text, style = MaterialTheme.typography.titleMedium)
    }
}