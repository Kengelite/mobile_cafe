@file:OptIn(ExperimentalMaterial3Api::class)
package com.example.mycafeme

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController

private val Cream = Color(0xFFFAF6F1)
private val Espresso = Color(0xFF2C1A0E)
private val Latte = Color(0xFF8B5E3C)

@Composable
fun RegisterScreen(navController: NavController, viewModel: AppViewModel) {
    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var pwd by remember { mutableStateOf("") }

    var receiveTypeExpanded by remember { mutableStateOf(false) }
    var selectedReceiveType by remember { mutableStateOf("Dine-in") }
    val receiveOptions = listOf("Dine-in", "Takeaway")

    Scaffold(containerColor = Cream) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 30.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text("สร้างบัญชีใหม่", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = Espresso)
            Text("ร่วมเป็นส่วนหนึ่งของ Cafe Hub", fontSize = 14.sp, color = Latte)

            Spacer(modifier = Modifier.height(32.dp))

            // ฟิลด์กรอกข้อมูล
            RegisterTextField(name, { name = it }, "ชื่อ-นามสกุล", Icons.Outlined.Person)
            Spacer(modifier = Modifier.height(12.dp))
            RegisterTextField(phone, { phone = it }, "เบอร์โทรศัพท์", Icons.Outlined.Phone, KeyboardType.Phone)
            Spacer(modifier = Modifier.height(12.dp))
            RegisterTextField(email, { email = it }, "อีเมล", Icons.Outlined.Email, KeyboardType.Email)
            Spacer(modifier = Modifier.height(12.dp))
            RegisterTextField(pwd, { pwd = it }, "รหัสผ่าน", Icons.Outlined.Lock, KeyboardType.Password, isPassword = true)

            Spacer(modifier = Modifier.height(12.dp))

            // เลือกประเภทการรับของ
            ExposedDropdownMenuBox(
                expanded = receiveTypeExpanded,
                onExpandedChange = { receiveTypeExpanded = !receiveTypeExpanded }
            ) {
                OutlinedTextField(
                    value = selectedReceiveType,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("รูปแบบการรับบริการ") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = receiveTypeExpanded) },
                    modifier = Modifier.menuAnchor().fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
                ExposedDropdownMenu(expanded = receiveTypeExpanded, onDismissRequest = { receiveTypeExpanded = false }) {
                    receiveOptions.forEach { type ->
                        DropdownMenuItem(text = { Text(type) }, onClick = { selectedReceiveType = type; receiveTypeExpanded = false })
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = {
                    viewModel.registerCustomer(name, phone, email, pwd, selectedReceiveType) {
                        navController.navigate("login") {
                            popUpTo("register") { inclusive = true }
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth().height(55.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Espresso),
                enabled = name.isNotBlank() && phone.isNotBlank() && email.isNotBlank() && pwd.isNotBlank()
            ) {
                Text("สมัครสมาชิก", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Cream)
            }

            TextButton(onClick = { navController.popBackStack() }) {
                Text("มีบัญชีอยู่แล้ว? เข้าสู่ระบบ", color = Latte)
            }
        }
    }
}

@Composable
fun RegisterTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    keyboardType: KeyboardType = KeyboardType.Text,
    isPassword: Boolean = false
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        leadingIcon = { Icon(icon, null, tint = Latte) },
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        singleLine = true,
        visualTransformation = if (isPassword) PasswordVisualTransformation() else androidx.compose.ui.text.input.VisualTransformation.None,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = Espresso,
            unfocusedBorderColor = Latte.copy(alpha = 0.5f)
        )
    )
}