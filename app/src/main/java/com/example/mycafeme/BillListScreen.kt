@file:OptIn(ExperimentalMaterial3Api::class)
package com.example.mycafeme

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ReceiptLong
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

@Composable
fun OrderHistoryScreen(navController: NavController, viewModel: AppViewModel) {
    val bills by viewModel.bills.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.fetchBills()
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("ประวัติการสั่งซื้อ", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Espresso) },
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
            if (isLoading && bills.isEmpty()) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center), color = Latte)
            } else if (bills.isEmpty()) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(Icons.Default.ReceiptLong, null, modifier = Modifier.size(64.dp), tint = Latte.copy(alpha = 0.3f))
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("ยังไม่มีประวัติการสั่งซื้อ", color = Latte)
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(bills) { bill ->
                        HistoryCard(bill) {
                            // นำทางไปหน้าดีเทลโดยใช้ชื่อใหม่
                            navController.navigate("order_detail/${bill.id}")
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryCard(bill: BillListData, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(20.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = "ออเดอร์: ${bill.id.takeLast(8)}", fontWeight = FontWeight.Bold, color = Espresso)
                Text(text = bill.date, fontSize = 12.sp, color = Color.Gray)
                Spacer(modifier = Modifier.height(8.dp))
                StatusBadge(bill.status)
            }
            Text(text = "${bill.totalPrice} ฿", fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, color = Latte)
        }
    }
}

@Composable
fun StatusBadge(status: String) {
    val (text, color) = when(status) {
        "1" -> "รอการชำระ" to Color(0xFFEF6C00)
        "2" -> "ชำระแล้ว" to Color(0xFF2E7D32)
        "3" -> "ยกเลิก" to Color(0xFFC62828)
        else -> "รอดำเนินการ" to Color.Gray
    }

    Surface(
        color = color.copy(alpha = 0.1f),
        shape = RoundedCornerShape(8.dp)
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = color
        )
    }
}