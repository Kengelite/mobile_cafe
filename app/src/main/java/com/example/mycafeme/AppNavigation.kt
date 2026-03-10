package com.example.mycafeme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val viewModel: AppViewModel = viewModel()

    //  1. เช็คข้อมูล User จาก Cache ใน ViewModel
    val user by viewModel.currentUser.collectAsState()

    //  2. กำหนดหน้าแรก: ถ้ามี User อยู่แล้วไป "main" ถ้าไม่มีไป "login"
    val startScreen = if (user != null) "main" else "login"

    NavHost(
        navController = navController,
        startDestination = startScreen // ใช้ตัวแปรที่เช็คแล้ว
    ) {
        // --- หน้าหลัก ---
        composable("login") { LoginScreen(navController, viewModel) }

        composable("main") {
            MainScreen(navController = navController, viewModel = viewModel)
        }

        // --- ระบบ Admin ---
        composable("admin_menu") { AdminMenuScreen(navController) }
        composable("admin_cafe") { CafeAdminScreen(navController, viewModel) }
        composable("admin_category") { AdminCategoryScreen(navController, viewModel) }

        composable("admin_cafe_add") {
            AdminCafeAddScreen(navController, viewModel)
        }

        composable("admin_cafe_edit/{cafeId}") { backStackEntry ->
            val cafeId = backStackEntry.arguments?.getString("cafeId") ?: ""
            AdminCafeEditScreen(navController, viewModel, cafeId)
        }

        composable("admin_user_screen") {
            AdminCustomerScreen(navController, viewModel)
        }

        composable("admin_order") {
            AdminOrderScreen(navController, viewModel)
        }

        composable("admin_order_detail/{orderId}") { backStackEntry ->
            val orderId = backStackEntry.arguments?.getString("orderId") ?: ""
            AdminOrderDetailScreen(navController, viewModel, orderId)
        }

        // --- ระบบ Customer (User) ---
        composable("register") {
            RegisterScreen(navController, viewModel)
        }

        composable("profile") {
            ProfileScreen(navController, viewModel)
        }

        composable("menu_list/{cafeId}") { backStackEntry ->
            // รับค่า UUID ของร้านกาแฟที่ส่งมาจาก HomeScreen
            val cafeId = backStackEntry.arguments?.getString("cafeId") ?: ""

            // สั่งเปิดหน้า MenuListScreen พร้อมส่ง cafeId ไปดึงข้อมูล
            MenuListScreen(navController, viewModel, cafeId)
        }

        composable("menu_list/{cafeId}") { backStackEntry ->
            val cafeId = backStackEntry.arguments?.getString("cafeId") ?: ""
            MenuListScreen(navController, viewModel, cafeId)
        }

        // 👈 เติมบล็อกนี้ลงไป เพื่อสร้างเส้นทางไปหน้าตะกร้า
        composable("cart") {
            CartScreen(navController, viewModel)
        }

        // ใน NavHost
        composable("order_history") {
            OrderHistoryScreen(navController, viewModel)
        }

        composable("order_detail/{orderId}") { backStackEntry ->
            val orderId = backStackEntry.arguments?.getString("orderId") ?: ""
            OrderDetailScreen(navController, viewModel, orderId)
        }
    }
}