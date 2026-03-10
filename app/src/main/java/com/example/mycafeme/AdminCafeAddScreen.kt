@file:OptIn(ExperimentalMaterial3Api::class)
package com.example.mycafeme

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
private val LatteLight = Color(0xFFD4A97A)
private val CardBg     = Color(0xFFFFFFFF)

@Composable
fun AdminCafeAddScreen(navController: NavController, viewModel: AppViewModel) {
    // State สำหรับฟอร์มร้านค้าใหม่
    var name      by remember { mutableStateOf("") }
    var location  by remember { mutableStateOf("") }
    var rating    by remember { mutableStateOf("0.0") }
    var openTime  by remember { mutableStateOf("08:00:00") }
    var closeTime by remember { mutableStateOf("20:00:00") }

    var showOpenPicker by remember { mutableStateOf(false) }
    var showClosePicker by remember { mutableStateOf(false) }

    // ── ระบบนาฬิกา (Time Picker) ──
    if (showOpenPicker) {
        MyTimePickerDialog(openTime, { showOpenPicker = false }, { h, m -> openTime = String.format("%02d:%02d:00", h, m); showOpenPicker = false })
    }
    if (showClosePicker) {
        MyTimePickerDialog(closeTime, { showClosePicker = false }, { h, m -> closeTime = String.format("%02d:%02d:00", h, m); showClosePicker = false })
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("เพิ่มร้านค้าใหม่", fontSize = 17.sp, fontWeight = FontWeight.SemiBold, color = Espresso)
                        Text("Cafe Hub", fontSize = 12.sp, color = Latte)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Outlined.ArrowBack, contentDescription = "กลับ", tint = Espresso)
                    }
                },
                actions = {
                    TextButton(
                        onClick = {
                            // 👈 ส่งข้อมูลไปบันทึก
                            viewModel.addCafe(name, location, openTime, closeTime, rating)
                            navController.popBackStack()
                        },
                        // ป้องกันการกดบันทึกถ้ายังกรอกชื่อร้านไม่ครบ
                        enabled = name.isNotBlank() && location.isNotBlank()
                    ) {
                        Text("สร้างร้าน", color = if (name.isNotBlank() && location.isNotBlank()) Latte else Color.Gray, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Cream)
            )
        },
        containerColor = Cream
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(vertical = 16.dp)
        ) {
            item {
                Text("ข้อมูลร้านใหม่", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = Latte, modifier = Modifier.padding(bottom = 4.dp))
            }

            item {
                Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = CardBg)) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {

                        OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("ชื่อร้าน", fontSize = 13.sp, color = Latte) }, leadingIcon = { Icon(Icons.Outlined.Home, null, tint = Latte, modifier = Modifier.size(18.dp)) }, singleLine = true, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Latte, unfocusedBorderColor = LatteLight.copy(alpha = 0.6f)))
                        OutlinedTextField(value = location, onValueChange = { location = it }, label = { Text("ที่อยู่", fontSize = 13.sp, color = Latte) }, leadingIcon = { Icon(Icons.Outlined.LocationOn, null, tint = Latte, modifier = Modifier.size(18.dp)) }, singleLine = true, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Latte, unfocusedBorderColor = LatteLight.copy(alpha = 0.6f)))
                        OutlinedTextField(value = rating, onValueChange = { rating = it }, label = { Text("คะแนนร้านเริ่มต้น (0.0 - 5.0)", fontSize = 13.sp, color = Latte) }, leadingIcon = { Icon(Icons.Outlined.Star, null, tint = Latte, modifier = Modifier.size(18.dp)) }, singleLine = true, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Latte, unfocusedBorderColor = LatteLight.copy(alpha = 0.6f)))

                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            OutlinedTextField(value = openTime.take(5), onValueChange = {}, label = { Text("เปิด", fontSize = 12.sp, color = Latte) }, leadingIcon = { Icon(Icons.Outlined.Schedule, null, modifier = Modifier.size(18.dp), tint = Latte) }, modifier = Modifier.weight(1f).clickable { showOpenPicker = true }, enabled = false, readOnly = true, shape = RoundedCornerShape(12.dp), colors = OutlinedTextFieldDefaults.colors(disabledTextColor = Espresso, disabledBorderColor = LatteLight.copy(alpha = 0.6f), disabledLabelColor = Latte, disabledLeadingIconColor = Latte, disabledContainerColor = Color.Transparent))
                            OutlinedTextField(value = closeTime.take(5), onValueChange = {}, label = { Text("ปิด", fontSize = 12.sp, color = Latte) }, leadingIcon = { Icon(Icons.Outlined.Schedule, null, modifier = Modifier.size(18.dp), tint = Latte) }, modifier = Modifier.weight(1f).clickable { showClosePicker = true }, enabled = false, readOnly = true, shape = RoundedCornerShape(12.dp), colors = OutlinedTextFieldDefaults.colors(disabledTextColor = Espresso, disabledBorderColor = LatteLight.copy(alpha = 0.6f), disabledLabelColor = Latte, disabledLeadingIconColor = Latte, disabledContainerColor = Color.Transparent))
                        }
                    }
                }
            }
        }
    }
}