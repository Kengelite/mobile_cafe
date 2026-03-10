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

// ── โทนสีกาแฟมินิมอล ───────────────────────────────────────
private val Cream      = Color(0xFFFAF6F1)
private val Espresso   = Color(0xFF2C1A0E)
private val Latte      = Color(0xFF8B5E3C)
private val LatteLight = Color(0xFFD4A97A)
private val CardBg     = Color(0xFFFFFFFF)
private val DeleteRed  = Color(0xFFB85C5C)

@Composable
fun CafeAdminScreen(navController: NavController, viewModel: AppViewModel) {
    val cafes by viewModel.cafes.collectAsState()

    // 👈 1. เพิ่ม State สำหรับจัดการ Dialog ยืนยันการลบ
    var showDeleteDialog by remember { mutableStateOf(false) }
    var selectedCafe by remember { mutableStateOf<CafeData?>(null) }

    LaunchedEffect(Unit) { viewModel.fetchCafes() }

    // 👈 2. เพิ่ม UI หน้าต่างยืนยันการลบ
    if (showDeleteDialog && selectedCafe != null) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = {
                Text("ยืนยันการลบ", fontWeight = FontWeight.Bold, color = Espresso)
            },
            text = {
                Text("คุณต้องการลบร้าน '${selectedCafe?.name}' ใช่หรือไม่? ข้อมูลนี้จะไม่สามารถกู้คืนได้", color = Latte)
            },
            confirmButton = {
                Button(
                    onClick = {
                        // สั่งลบจริงผ่าน ViewModel
                        selectedCafe?.let { viewModel.deleteCafe(it.id) }
                        showDeleteDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = DeleteRed)
                ) { Text("ลบร้าน", color = Color.White) }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("ยกเลิก", color = Latte)
                }
            },
            containerColor = Cream,
            shape = RoundedCornerShape(16.dp)
        )
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "จัดการร้านค้า",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Espresso,
                            letterSpacing = 0.5.sp
                        )
                        Text(text = "Cafe Hub", fontSize = 12.sp, color = Latte)
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
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    // 👈 สั่งไปหน้าเพิ่มร้านใหม่
                    navController.navigate("admin_cafe_add")
                },
                containerColor = Espresso,
                contentColor = Cream,
                shape = RoundedCornerShape(16.dp),
                elevation = FloatingActionButtonDefaults.elevation(0.dp)
            ) {
                Icon(Icons.Outlined.Add, contentDescription = "เพิ่มร้าน")
            }
        },
        containerColor = Cream
    ) { padding ->

        if (cafes.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("☕", fontSize = 36.sp)
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("ยังไม่มีร้านค้า", color = Latte, fontSize = 15.sp)
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    Text(
                        text = "${cafes.size} ร้าน",
                        fontSize = 13.sp,
                        color = Latte,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                }
                items(cafes) { cafe ->
                    CafeAdminItem(
                        cafe = cafe,
                        onEdit = { navController.navigate("admin_cafe_edit/${cafe.id}") },
                        onDelete = {
                            // 👈 3. เมื่อกดปุ่มถังขยะ ให้โชว์ Dialog แทนการเรียก API ตรงๆ
                            selectedCafe = cafe
                            showDeleteDialog = true
                        }
                    )
                }
            }
        }
    }
}

// ... ส่วน CafeAdminItem ของพี่อยู่ข้างล่าง คงเดิมได้เลยครับ ...
@Composable
fun CafeAdminItem(cafe: CafeData, onEdit: () -> Unit, onDelete: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(0.dp),
        colors = CardDefaults.cardColors(containerColor = CardBg)
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 14.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .background(LatteLight.copy(alpha = 0.25f), RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) { Text("☕", fontSize = 20.sp) }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(text = cafe.name, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = Espresso)
                Spacer(modifier = Modifier.height(3.dp))
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(text = "⭐ ${cafe.rating}", fontSize = 12.sp, color = Latte)
                    Text(text = "·", fontSize = 12.sp, color = LatteLight)
                    Text(text = "#${cafe.id.take(6)}", fontSize = 12.sp, color = LatteLight)
                }
            }

            IconButton(onClick = onEdit, modifier = Modifier.size(36.dp)) {
                Icon(Icons.Outlined.Edit, contentDescription = "แก้ไข", tint = Latte, modifier = Modifier.size(18.dp))
            }
            IconButton(onClick = onDelete, modifier = Modifier.size(36.dp)) {
                Icon(Icons.Outlined.Delete, contentDescription = "ลบ", tint = DeleteRed, modifier = Modifier.size(18.dp))
            }
        }
    }
}