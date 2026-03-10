@file:OptIn(ExperimentalMaterial3Api::class)
package com.example.mycafeme

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
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

// ── โทนสีกาแฟมินิมอล ───────────────────────────────────────
private val Cream      = Color(0xFFFAF6F1)
private val Espresso   = Color(0xFF2C1A0E)
private val Latte      = Color(0xFF8B5E3C)
private val CardBg     = Color(0xFFFFFFFF)
private val LogoutRed  = Color(0xFFB85C5C)
private val SaveGreen  = Color(0xFF4A7C59)
private val CancelGray = Color(0xFF9E9E9E)

@Composable
fun ProfileScreen(navController: NavController, viewModel: AppViewModel) {
    // 1. ดึงข้อมูลผู้ใช้จาก ViewModel (ข้อมูลจริงจากการ Login)
    val user by viewModel.currentUser.collectAsState()

    // 2. State สำหรับควบคุมโหมดการแก้ไข
    var isEditMode by remember { mutableStateOf(false) }

    // 3. State สำหรับฟอร์มแก้ไข (เก็บข้อมูลไว้ในฟอร์มก่อนกดบันทึก)
    var nameInput by remember { mutableStateOf("") }
    var emailInput by remember { mutableStateOf("") }
    var phoneInput by remember { mutableStateOf("") }

    // 4. เมื่อข้อมูล user เปลี่ยน (หรือเปิดหน้ามาครั้งแรก) ให้เอาข้อมูลมาลงใน TextField
    LaunchedEffect(user) {
        user?.let {
            nameInput = it.name ?: ""
            emailInput = it.email ?: ""
            phoneInput = it.phone ?: ""
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("ข้อมูลส่วนตัว", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Espresso) },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Cream)
            )
        },
        containerColor = Cream
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(20.dp))

            // ── Avatar ──
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .clip(CircleShape)
                    .background(Latte.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Outlined.Person, null, Modifier.size(50.dp), tint = Latte)
            }

            Spacer(modifier = Modifier.height(16.dp))

            // ── ส่วนแสดงชื่อ (หรือช่องแก้ไขชื่อ) ──
            if (isEditMode) {
                OutlinedTextField(
                    value = nameInput,
                    onValueChange = { nameInput = it },
                    label = { Text("ชื่อ-นามสกุล") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Latte)
                )
            } else {
                Text(text = nameInput, fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Espresso)
                Text(text = "สมาชิกระดับพรีเมียม", fontSize = 13.sp, color = Latte)
            }

            Spacer(modifier = Modifier.height(32.dp))

            // ── Card ข้อมูลรายละเอียด ──
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = CardBg),
                elevation = CardDefaults.cardElevation(0.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    EditableProfileItem(Icons.Outlined.Email, "อีเมลติดต่อ", emailInput, isEditMode) { emailInput = it }
                    EditableProfileItem(Icons.Outlined.Phone, "เบอร์โทรศัพท์", phoneInput, isEditMode) { phoneInput = it }

                    // ส่วนโชว์ประเภทการรับของ (ปกติจะไม่อนุญาตให้เปลี่ยนผ่านหน้าโปรไฟล์ตรงๆ)
                    ProfileItem(Icons.Outlined.Storefront, "ประเภทการรับบริการหลัก", user?.receiveType ?: "Dine-in")
                }
            }

            Spacer(modifier = Modifier.height(40.dp))

            // ── ปุ่ม Action ด้านล่าง ──
            if (!isEditMode) {
                // โหมดปกติ: แก้ไข กับ ออกจากระบบ
                Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    Button(
                        onClick = { isEditMode = true },
                        modifier = Modifier.fillMaxWidth().height(55.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Espresso)
                    ) {
                        Icon(Icons.Outlined.Edit, null, Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("แก้ไขข้อมูลส่วนตัว", fontWeight = FontWeight.SemiBold)
                    }

                    TextButton(
                        onClick = {
                            viewModel.logout {
                                navController.navigate("login") { popUpTo(0) }
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("ออกจากระบบ", color = LogoutRed, fontWeight = FontWeight.Medium)
                    }
                }
            } else {
                // โหมดแก้ไข: บันทึก กับ ยกเลิก
                Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    Button(
                        onClick = {
                            // 👈 ส่ง ID จริงของ User ไปเพื่อบันทึก
                            user?.id?.let { id ->
                                viewModel.updateProfile(
                                    id = id,
                                    name = nameInput,
                                    phone = phoneInput,
                                    email = emailInput,
//                                    role = user?.role ?: "user", // ส่ง role เดิมกลับไป
                                    receiveType = user?.receiveType ?: "Dine-in" // ส่งค่าเดิมที่ดึงมาจาก DB
                                ) {
                                    isEditMode = false
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth().height(55.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = SaveGreen)
                    ) {
                        Text("บันทึกการเปลี่ยนแปลง", fontWeight = FontWeight.Bold)
                    }

                    OutlinedButton(
                        onClick = { isEditMode = false },
                        modifier = Modifier.fillMaxWidth().height(55.dp),
                        shape = RoundedCornerShape(16.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, CancelGray)
                    ) {
                        Text("ยกเลิก", color = CancelGray)
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

@Composable
fun EditableProfileItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String,
    isEditMode: Boolean,
    onValueChange: (String) -> Unit
) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
        Icon(icon, null, tint = Latte, modifier = Modifier.size(24.dp))
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(text = label, fontSize = 12.sp, color = Color.Gray)
            if (isEditMode) {
                TextField(
                    value = value,
                    onValueChange = onValueChange,
                    modifier = Modifier.fillMaxWidth(),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent
                    ),
                    textStyle = LocalTextStyle.current.copy(fontSize = 15.sp, color = Espresso)
                )
            } else {
                Text(text = value, fontSize = 15.sp, color = Espresso, fontWeight = FontWeight.Medium)
            }
        }
    }
}

@Composable
fun ProfileItem(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, value: String) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
        Icon(icon, null, tint = Latte, modifier = Modifier.size(24.dp))
        Spacer(modifier = Modifier.width(16.dp))
        Column {
            Text(text = label, fontSize = 12.sp, color = Color.Gray)
            Text(text = value, fontSize = 15.sp, color = Espresso, fontWeight = FontWeight.Medium)
        }
    }
}