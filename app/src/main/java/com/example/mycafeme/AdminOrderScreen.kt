@file:OptIn(ExperimentalMaterial3Api::class)
package com.example.mycafeme

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController

// ── โทนสีกาแฟมินิมอล ───────────────────────────────────────
private val Cream      = Color(0xFFFAF6F1)
private val Espresso   = Color(0xFF2C1A0E)
private val Latte      = Color(0xFF8B5E3C)
private val LatteLight = Color(0xFFD4A97A)
private val CardBg     = Color(0xFFFFFFFF)

// สีสำหรับสถานะออเดอร์
private val StatusPending   = Color(0xFFD97706) // สีส้ม (รอรับออเดอร์ - 1)
private val StatusPreparing = Color(0xFF2563EB) // สีน้ำเงิน (กำลังเตรียม/ชำระแล้ว - 2)
private val StatusCompleted = Color(0xFF16A34A) // สีเขียว (เสร็จสิ้น)
private val StatusCancelled = Color(0xFFDC2626) // สีแดง (ยกเลิก - 3)

@Composable
fun AdminOrderScreen(navController: NavController, viewModel: AppViewModel) {
    val orders by viewModel.orders.collectAsState()

    var showStatusDialog by remember { mutableStateOf(false) }
    var selectedOrder by remember { mutableStateOf<OrderData?>(null) }
    var statusExpanded by remember { mutableStateOf(false) }
    var selectedStatus by remember { mutableStateOf("") }

    val statusOptions = listOf("1", "2", "3")

    LaunchedEffect(Unit) {
        viewModel.fetchOrders()
    }

    if (showStatusDialog && selectedOrder != null) {
        AlertDialog(
            onDismissRequest = { showStatusDialog = false },
            title = { Text("เปลี่ยนสถานะออเดอร์", color = Espresso, fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("ออเดอร์: #${selectedOrder!!.id.take(8)}", color = Latte)

                    @OptIn(ExperimentalMaterial3Api::class)
                    ExposedDropdownMenuBox(
                        expanded = statusExpanded,
                        onExpandedChange = { statusExpanded = !statusExpanded }
                    ) {
                        OutlinedTextField(
                            value = getAdminStatusLabel(selectedStatus),
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("สถานะใหม่") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = statusExpanded) },
                            modifier = Modifier.menuAnchor().fillMaxWidth(),
                            colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors()
                        )
                        ExposedDropdownMenu(expanded = statusExpanded, onDismissRequest = { statusExpanded = false }) {
                            statusOptions.forEach { status ->
                                DropdownMenuItem(
                                    text = { Text(getAdminStatusLabel(status)) },
                                    onClick = {
                                        selectedStatus = status
                                        statusExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        // viewModel.updateOrderStatus(selectedOrder!!.id, selectedStatus)
                        showStatusDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Espresso)
                ) { Text("อัปเดต", color = Color.White) }
            },
            dismissButton = { TextButton(onClick = { showStatusDialog = false }) { Text("ยกเลิก", color = Latte) } }
        )
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("จัดการออเดอร์", fontSize = 17.sp, fontWeight = FontWeight.SemiBold, color = Espresso)
                        Text("Admin Dashboard", fontSize = 12.sp, color = Latte)
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
        if (orders.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Outlined.ReceiptLong, contentDescription = null, modifier = Modifier.size(48.dp), tint = LatteLight)
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("ยังไม่มีออเดอร์เข้ามา", color = Latte, fontSize = 15.sp)
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    Text("ออเดอร์ทั้งหมด (${orders.size})", fontSize = 13.sp, color = Latte, fontWeight = FontWeight.Medium, modifier = Modifier.padding(bottom = 4.dp))
                }

                items(orders) { order ->
                    OrderAdminCard(
                        order = order,
                        onClickDetail = {
                            navController.navigate("admin_order_detail/${order.id}")
                        },
                        onChangeStatus = {
                            selectedOrder = order
                            selectedStatus = order.status ?: "1"
                            showStatusDialog = true
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun OrderAdminCard(order: OrderData, onClickDetail: () -> Unit, onChangeStatus: () -> Unit) {
    val currentStatus = order.status ?: "1"
    val statusColor = when(currentStatus) {
        "1" -> StatusPending     // ส้ม
        "2" -> StatusPreparing   // น้ำเงิน
        "3" -> StatusCancelled   // แดง
        else -> LatteLight
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClickDetail() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CardBg),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .background(statusColor.copy(alpha = 0.15f), RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Outlined.Receipt, contentDescription = null, tint = statusColor, modifier = Modifier.size(24.dp))
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                // 👈 1. เพิ่มชื่อร้านค้าไว้บรรทัดแรกสุด ให้เห็นชัดๆ
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Outlined.Storefront, contentDescription = "Cafe", modifier = Modifier.size(14.dp), tint = Espresso)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        // 💡 หมายเหตุ: ต้องเพิ่ม cafeName ใน OrderData ด้วยนะครับ
                        text = order.cafeName ?: "ไม่ระบุชื่อร้าน",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Espresso,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.height(2.dp))

                // ข้อมูลออเดอร์เดิม เลื่อนลงมาเป็นบรรทัดรอง
                Text(text = "ออเดอร์ #${order.id.take(8)}", fontSize = 12.sp, color = Color.Gray)
                Text(text = "ยอดสุทธิ: ${order.netPrice} บาท", fontSize = 13.sp, color = Latte)

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = statusColor.copy(alpha = 0.1f),
                    modifier = Modifier
                        .padding(top = 8.dp)
                        .clickable { onChangeStatus() }
                ) {
                    Text(
                        text = getAdminStatusLabel(currentStatus),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = statusColor,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Icon(Icons.Outlined.ChevronRight, contentDescription = "ดูรายละเอียด", tint = LatteLight)
        }
    }
}

fun getAdminStatusLabel(status: String): String {
    return when(status) {
        "0" -> "ตะกร้าสินค้า"
        "1" -> "รอรับออเดอร์"
        "2" -> "กำลังเตรียม"
        "3" -> "ยกเลิก"
        else -> "สถานะ: $status"
    }
}