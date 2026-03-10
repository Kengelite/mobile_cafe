package com.example.mycafeme

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminCategoryScreen(navController: NavController, viewModel: AppViewModel) {

    // 1. 👈 ดึงข้อมูลจริงจาก ViewModel (แทนที่ Mock เดิม)
    val categories by viewModel.categories.collectAsState()

    var showAddDialog by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var selectedCategory by remember { mutableStateOf<CategoryData?>(null) }
    var categoryNameText by remember { mutableStateOf("") }

    // 2. 👈 สั่งให้โหลดข้อมูลจาก API ทันทีที่เปิดหน้านี้
    LaunchedEffect(Unit) {
        viewModel.fetchCategories()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("จัดการหมวดหมู่", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = null)
                    }
                },
                actions = {
                    IconButton(onClick = {
                        categoryNameText = ""
                        showAddDialog = true
                    }) {
                        Icon(Icons.Default.Add, contentDescription = "Add")
                    }
                }
            )
        }
    ) { padding ->
        // แสดง List ข้อมูลที่ได้มาจาก API
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(categories) { item ->
                CategoryItem(
                    category = item,
                    onEdit = {
                        selectedCategory = item
                        categoryNameText = item.name
                        showEditDialog = true
                    },
                    onDelete = {
                        selectedCategory = item
                        showDeleteDialog = true
                    }
                )
            }
        }
    }

    // --- Modal: เพิ่มข้อมูล (ต่อ API) ---
    if (showAddDialog) {
        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text("เพิ่มหมวดหมู่ใหม่") },
            text = {
                OutlinedTextField(
                    value = categoryNameText,
                    onValueChange = { categoryNameText = it },
                    label = { Text("ชื่อหมวดหมู่") },
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(onClick = {
                    if (categoryNameText.isNotBlank()) {
                        // 3. 👈 เรียกใช้ฟังก์ชันใน ViewModel เพื่อส่งข้อมูลไป Node.js
                        viewModel.addCategory(categoryNameText)
                        showAddDialog = false
                    }
                }) { Text("เพิ่ม") }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) { Text("ยกเลิก") }
            }
        )
    }

    // --- Modal: แก้ไข (ต่อ API) ---
    if (showEditDialog && selectedCategory != null) {
        AlertDialog(
            onDismissRequest = { showEditDialog = false },
            title = { Text("แก้ไขหมวดหมู่") },
            text = {
                Column {
                    Text("รหัส: ${selectedCategory?.id}", style = MaterialTheme.typography.bodySmall)
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = categoryNameText,
                        onValueChange = { categoryNameText = it },
                        label = { Text("ชื่อหมวดหมู่") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(onClick = {
                    // 4. 👈 ส่ง ID และชื่อใหม่ไป Update ที่ Database
                    viewModel.updateCategory(selectedCategory!!.id, categoryNameText)
                    showEditDialog = false
                }) { Text("บันทึก") }
            },
            dismissButton = {
                TextButton(onClick = { showEditDialog = false }) { Text("ยกเลิก") }
            }
        )
    }

    // --- Modal: ยืนยันการลบ (ต่อ API) ---
    if (showDeleteDialog && selectedCategory != null) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            icon = { Icon(Icons.Default.Warning, contentDescription = null, tint = Color.Red) },
            title = { Text("ยืนยันการลบ") },
            text = { Text("คุณต้องการลบหมวดหมู่ '${selectedCategory?.name}' ใช่หรือไม่?") },
            confirmButton = {
                Button(
                    onClick = {
                        // 5. 👈 สั่งลบข้อมูลผ่าน API
                        viewModel.deleteCategory(selectedCategory!!.id)
                        showDeleteDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                ) { Text("ลบข้อมูล") }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) { Text("ยกเลิก") }
            }
        )
    }
}

@Composable
fun CategoryItem(category: CategoryData, onEdit: () -> Unit, onDelete: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
    ) {
        Row(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(category.name, fontWeight = FontWeight.Bold)
                Text("ID: ${category.id}", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
            }
            IconButton(onClick = onEdit) {
                Icon(Icons.Default.Edit, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = null, tint = Color.Red)
            }
        }
    }
}