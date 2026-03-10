@file:OptIn(ExperimentalMaterial3Api::class)
package com.example.mycafeme

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
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
fun OrderDetailScreen(navController: NavController, viewModel: AppViewModel, orderId: String) {
    val items by viewModel.selectedBillItems.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    LaunchedEffect(orderId) {
        viewModel.fetchBillDetails(orderId)
    }

    val subTotal = items.sumOf { it.price * it.quantity }
    val netTotal = subTotal + 10.0

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("รายละเอียดออเดอร์", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Espresso) },
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
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item {
                        Text(text = "รายการอาหาร", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Espresso)
                    }

                    items(items) { item ->
                        OrderDetailItemCard(item)
                    }

                    item {
                        Spacer(modifier = Modifier.height(16.dp))
                        OrderDetailSummary(subTotal, 10.0, netTotal)
                        Spacer(modifier = Modifier.height(32.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun OrderDetailItemCard(item: CartItemData) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = item.menuName, fontWeight = FontWeight.Bold, color = Espresso)
                Text(text = "จำนวน: ${item.quantity}", fontSize = 13.sp, color = Color.Gray)
            }
            Text(text = "${item.price * item.quantity} ฿", fontWeight = FontWeight.Bold, color = Latte)
        }
    }
}

@Composable
fun OrderDetailSummary(subTotal: Double, serviceFee: Double, netTotal: Double) {
    Surface(
        color = Color.White,
        shape = RoundedCornerShape(24.dp),
        shadowElevation = 2.dp
    ) {
        Column(modifier = Modifier.padding(24.dp).fillMaxWidth()) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("ยอดรวม", color = Color.Gray, fontSize = 14.sp)
                Text("${subTotal} ฿", color = Espresso)
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("ค่าบริการ", color = Color.Gray, fontSize = 14.sp)
                Text("${serviceFee} ฿", color = Espresso)
            }
            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = Cream, thickness = 2.dp)
            Spacer(modifier = Modifier.height(12.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("รวมสุทธิ", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Espresso)
                Text("${netTotal} ฿", fontWeight = FontWeight.ExtraBold, fontSize = 20.sp, color = Latte)
            }
        }
    }
}