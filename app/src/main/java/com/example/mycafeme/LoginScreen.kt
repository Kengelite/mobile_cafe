package com.example.mycafeme

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.foundation.BorderStroke
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController

// ── โทนสีกาแฟมินิมอล ──────────────────────────────────────
private val Cream      = Color(0xFFFAF6F1)
private val Espresso   = Color(0xFF2C1A0E)
private val Latte      = Color(0xFF8B5E3C)
private val LatteLight = Color(0xFFD4A97A)
private val ErrorRed   = Color(0xFFB85C5C)

@Composable
fun LoginScreen(navController: NavController, viewModel: AppViewModel) {
    var username     by remember { mutableStateOf("") }
    var password     by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf("") }
    var showPassword by remember { mutableStateOf(false) }

    val isLoading by viewModel.isLoading.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Cream),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 36.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            // ── Logo / ชื่อแอป ──────────────────────────────
            Text(
                text = "☕",
                fontSize = 48.sp,
                modifier = Modifier.padding(bottom = 12.dp)
            )
            Text(
                text = "Cafe Hub",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = Espresso,
                letterSpacing = 1.sp
            )
            Text(
                text = "ยินดีต้อนรับกลับมา",
                fontSize = 14.sp,
                color = Latte,
                modifier = Modifier.padding(top = 4.dp, bottom = 40.dp)
            )

            // ── Email / Username ─────────────────────────────
            CafeTextField(
                value = username,
                onValueChange = { username = it; errorMessage = "" },
                label = "Email หรือ Username",
                leadingIcon = {
                    Icon(
                        Icons.Outlined.Email,
                        contentDescription = null,
                        tint = Latte,
                        modifier = Modifier.size(20.dp)
                    )
                }
            )

            Spacer(modifier = Modifier.height(14.dp))

            // ── Password ─────────────────────────────────────
            CafeTextField(
                value = password,
                onValueChange = { password = it; errorMessage = "" },
                label = "Password",
                leadingIcon = {
                    Icon(
                        Icons.Outlined.Lock,
                        contentDescription = null,
                        tint = Latte,
                        modifier = Modifier.size(20.dp)
                    )
                },
                trailingIcon = {
                    TextButton(
                        onClick = { showPassword = !showPassword },
                        contentPadding = PaddingValues(horizontal = 8.dp)
                    ) {
                        Text(
                            text = if (showPassword) "ซ่อน" else "แสดง",
                            fontSize = 12.sp,
                            color = Latte
                        )
                    }
                },
                visualTransformation = if (showPassword) VisualTransformation.None
                else PasswordVisualTransformation(),
                keyboardType = KeyboardType.Password
            )

            // ── Error message ─────────────────────────────────
            AnimatedError(message = errorMessage)

            Spacer(modifier = Modifier.height(28.dp))

            // ── Login Button ──────────────────────────────────
            Button(
                onClick = {
                    viewModel.login(
                        user = username,
                        pass = password,
                        onSuccess = { role ->
                            if (role == "admin") {
                                navController.navigate("admin_menu") {
                                    popUpTo("login") { inclusive = true }
                                }
                            } else {
                                navController.navigate("main") {
                                    popUpTo("login") { inclusive = true }
                                }
                            }
                        },
                        onError = { errorMessage = it }
                    )
                },
                enabled = !isLoading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Espresso,
                    contentColor   = Cream,
                    disabledContainerColor = Espresso.copy(alpha = 0.5f)
                ),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        color = Cream,
                        strokeWidth = 2.dp,
                        modifier = Modifier.size(22.dp)
                    )
                } else {
                    Text(
                        text = "เข้าสู่ระบบ",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = 0.5.sp
                    )
                }
            }

            // ── Divider เส้นบางๆ ──────────────────────────────
            Spacer(modifier = Modifier.height(32.dp))
            HorizontalDivider(color = LatteLight.copy(alpha = 0.4f), thickness = 1.dp)
            Spacer(modifier = Modifier.height(16.dp))


            OutlinedButton(
                onClick = {
                    // 👈 สั่งให้นำทางไปหน้าสมัครสมาชิก
                    navController.navigate("register")
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(14.dp),
                border = BorderStroke(1.dp, Espresso), // ขอบสีเข้มสไตล์ Espresso
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = Espresso
                )
            ) {
                Text(
                    text = "สมัครสมาชิก",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 0.5.sp
                )
            }

            // ── Divider เส้นบางๆ ──────────────────────────────
            Spacer(modifier = Modifier.height(32.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                HorizontalDivider(modifier = Modifier.weight(1f), color = LatteLight.copy(alpha = 0.4f), thickness = 1.dp)
                Text(" หรือ ", fontSize = 12.sp, color = Latte.copy(alpha = 0.5f), modifier = Modifier.padding(horizontal = 8.dp))
                HorizontalDivider(modifier = Modifier.weight(1f), color = LatteLight.copy(alpha = 0.4f), thickness = 1.dp)
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "© 2026 Cafe Hub",
                fontSize = 11.sp,
                color = Latte.copy(alpha = 0.4f)
            )

//            Text(
//                text = "© Cafe Hub",
//                fontSize = 12.sp,
//                color = Latte.copy(alpha = 0.5f)
//            )
        }
    }
}

// ── Reusable text field สไตล์กาแฟ ─────────────────────────
@Composable
private fun CafeTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    leadingIcon: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    keyboardType: KeyboardType = KeyboardType.Text
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label, fontSize = 13.sp, color = Latte) },
        leadingIcon = leadingIcon,
        trailingIcon = trailingIcon,
        visualTransformation = visualTransformation,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        singleLine = true,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor   = Latte,
            unfocusedBorderColor = LatteLight.copy(alpha = 0.6f),
            focusedLabelColor    = Latte,
            cursorColor          = Espresso,
            focusedTextColor     = Espresso,
            unfocusedTextColor   = Espresso,
            unfocusedContainerColor = Color.White,
            focusedContainerColor   = Color.White
        )
    )
}

// ── Error แบบเรียบๆ ────────────────────────────────────────
@Composable
private fun AnimatedError(message: String) {
    if (message.isNotEmpty()) {
        Spacer(modifier = Modifier.height(10.dp))
        Text(
            text = "⚠ $message",
            color = ErrorRed,
            fontSize = 13.sp,
            modifier = Modifier.fillMaxWidth()
        )
    }
}