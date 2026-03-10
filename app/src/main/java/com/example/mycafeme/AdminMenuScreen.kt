package com.example.mycafeme

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController

// ── โทนสีกาแฟมินิมอล (เหมือน LoginScreen) ─────────────────
private val Cream        = Color(0xFFFAF6F1)
private val Espresso     = Color(0xFF2C1A0E)
private val Latte        = Color(0xFF8B5E3C)
private val LatteLight   = Color(0xFFD4A97A)
private val CardBg       = Color(0xFFFFFFFF)
private val LogoutRed    = Color(0xFFB85C5C)

data class AdminMenuItem(
    val title: String,
    val icon: ImageVector,
    val route: String,
    val isDestructive: Boolean = false
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminMenuScreen(navController: NavController) {
    val menuItems = listOf(
        AdminMenuItem("Cafe",     Icons.Outlined.Home,         "admin_cafe"),
        AdminMenuItem("Category", Icons.Outlined.List,         "admin_category"),
        AdminMenuItem("Customer", Icons.Outlined.Person,       "admin_user_screen"),
//        AdminMenuItem("Menu",     Icons.Outlined.Menu,         "admin_menu_items"),
        AdminMenuItem("Order",    Icons.Outlined.ShoppingCart, "admin_order"),
        AdminMenuItem("Payment",  Icons.Outlined.CheckCircle,  "admin_payment"),
//        AdminMenuItem("Status",   Icons.Outlined.Info,         "admin_status"),
        AdminMenuItem("Logout",   Icons.Outlined.ExitToApp,    "logout", isDestructive = true)
    )

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "Admin Dashboard",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Espresso,
                            letterSpacing = 0.5.sp
                        )
                        Text(
                            text = "Cafe Hub",
                            fontSize = 12.sp,
                            color = Latte
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Cream
                )
            )
        },
        containerColor = Cream
    ) { padding ->

        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier
                .padding(padding)
                .padding(horizontal = 20.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalArrangement   = Arrangement.spacedBy(14.dp)
        ) {
            items(menuItems) { item ->
                AdminCard(item = item) {
                    if (item.route == "logout") {
                        navController.navigate("login") {
                            popUpTo("admin_menu") { inclusive = true }
                        }
                    } else {
                        navController.navigate(item.route)
                    }
                }
            }
        }
    }
}

@Composable
private fun AdminCard(
    item: AdminMenuItem,
    onClick: () -> Unit
) {
    val iconTint  = if (item.isDestructive) LogoutRed else Latte
    val textColor = if (item.isDestructive) LogoutRed else Espresso

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(110.dp)
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        colors = CardDefaults.cardColors(containerColor = CardBg)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // ไอคอนในกล่องพื้นครีม
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .background(
                        color = if (item.isDestructive) LogoutRed.copy(alpha = 0.08f)
                        else LatteLight.copy(alpha = 0.25f),
                        shape = RoundedCornerShape(10.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = item.icon,
                    contentDescription = item.title,
                    tint = iconTint,
                    modifier = Modifier.size(20.dp)
                )
            }

            Text(
                text = item.title,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = textColor,
                letterSpacing = 0.3.sp
            )
        }
    }
}