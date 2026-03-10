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
private val StatusPending   = Color(0xFFD97706) // สีส้ม (รอรับออเดอร์)
private val StatusPreparing = Color(0xFF2563EB) // สีน้ำเงิน (กำลังเตรียม)
private val StatusCompleted = Color(0xFF16A34A) // สีเขียว (เสร็จสิ้น)
private val StatusCancelled = Color(0xFFDC2626) // สีแดง (ยกเลิก)

@Composable
fun AdminOrderScreen(navController: NavController, viewModel: AppViewModel) {
    val orders by viewModel.orders.collectAsState()

    // State สำหรับ Dialog เปลี่ยนสถานะ
    var showStatusDialog by remember { mutableStateOf(false) }
    var selectedOrder by remember { mutableStateOf<OrderData?>(null) }

    var statusExpanded by remember { mutableStateOf(false) }
    var selectedStatus by remember { mutableStateOf("") }

    // ตัวเลือกสถานะทั้งหมด
    val statusOptions = listOf("pending", "preparing", "completed", "cancelled")

    LaunchedEffect(Unit) {
        viewModel.fetchOrders()
    }

    // ── Dialog: เปลี่ยนสถานะออเดอร์ ──
    if (showStatusDialog && selectedOrder != null) {
        AlertDialog(
            onDismissRequest = { showStatusDialog = false },
            title = { Text("เปลี่ยนสถานะออเดอร์", color = Espresso, fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("ออเดอร์: #${selectedOrder!!.id.take(8)}", color = Latte)

                    ExposedDropdownMenuBox(
                        expanded = statusExpanded,
                        onExpandedChange = { statusExpanded = !statusExpanded }
                    ) {
                        OutlinedTextField(
                            value = getStatusLabel(selectedStatus),
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
                                    text = { Text(getStatusLabel(status)) },
                                    onClick = { selectedStatus = status; statusExpanded = false }
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.updateOrderStatus(selectedOrder!!.id, selectedStatus)
                        showStatusDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Latte)
                ) { Text("อัปเดต") }
            },
            dismissButton = { TextButton(onClick = { showStatusDialog = false }) { Text("ยกเลิก") } }
        )
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("จัดการออเดอร์", fontSize = 17.sp, fontWeight = FontWeight.SemiBold, color = Espresso)
                        Text("Cafe Hub", fontSize = 12.sp, color = Latte)
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

                // 👈 เรียกใช้ OrderAdminCard ตรงนี้เลย สะอาดและเป็นระเบียบ
                items(orders) { order ->
                    OrderAdminCard(
                        order = order,
                        onClickDetail = {
                            // นำทางไปหน้า Detail
                            navController.navigate("admin_order_detail/${order.id}")
                        },
                        onChangeStatus = {
                            // เปิดหน้าต่างเปลี่ยนสถานะ
                            selectedOrder = order
                            selectedStatus = order.status ?: "pending"
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
    // กำหนดสีตามสถานะ (เช็ค null ไว้เผื่อด้วย)
    val currentStatus = order.status ?: "pending"
    val statusColor = when(currentStatus.lowercase()) {
        "pending" -> StatusPending
        "preparing" -> StatusPreparing
        "completed" -> StatusCompleted
        "cancelled" -> StatusCancelled
        else -> LatteLight
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClickDetail() }, // 👈 กดที่การ์ดเพื่อไปหน้า Detail
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CardBg),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // ── ไอคอนบิล ──
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .background(statusColor.copy(alpha = 0.15f), RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Outlined.Receipt, contentDescription = null, tint = statusColor, modifier = Modifier.size(24.dp))
            }

            Spacer(modifier = Modifier.width(14.dp))

            // ── ข้อมูลออเดอร์ ──
            Column(modifier = Modifier.weight(1f)) {
                Text(text = "ออเดอร์ #${order.id.take(8)}", fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = Espresso)
                // 👈 ใช้ netPrice ให้ตรงกับฐานข้อมูล
                Text(text = "ยอดสุทธิ: ${order.netPrice} บาท", fontSize = 13.sp, color = Latte)

                // ป้าย Tag บอกสถานะ (กดที่ป้ายเพื่อเปลี่ยนสถานะ)
                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = statusColor.copy(alpha = 0.1f),
                    modifier = Modifier
                        .padding(top = 4.dp)
                        .clickable { onChangeStatus() }
                ) {
                    Text(
                        text = getStatusLabel(currentStatus),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = statusColor,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            // ไอคอนลูกศรชี้ขวาเพื่อบอกว่ากดเข้าไปดูรายละเอียดได้
            Icon(Icons.Outlined.ChevronRight, contentDescription = "ดูรายละเอียด", tint = LatteLight)
        }
    }
}

// ฟังก์ชันช่วยแปลงภาษาอังกฤษเป็นไทย
fun getStatusLabel(status: String): String {
    return when {
        // ดักจับด้วย ID จากฐานข้อมูล
        status.contains("000000000001") || status.lowercase() == "pending" -> "รอรับออเดอร์"
        status.contains("000000000002") || status.lowercase() == "preparing" || status.lowercase() == "processing" -> "กำลังเตรียม"
        status.contains("000000000003") || status.lowercase() == "completed" -> "เสร็จสิ้น"

        // เผื่อพี่มีสถานะยกเลิกในอนาคต (สมมติว่าเป็น ID 0004)
        status.contains("000000000004") || status.lowercase() == "cancelled" -> "ยกเลิก"

        else -> status // ถ้าไม่ตรงกับอะไรเลยให้โชว์ค่าเดิม
    }
}