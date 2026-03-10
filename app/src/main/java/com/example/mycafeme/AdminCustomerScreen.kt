@file:OptIn(ExperimentalMaterial3Api::class)
package com.example.mycafeme

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController

private val Cream      = Color(0xFFFAF6F1)
private val Espresso   = Color(0xFF2C1A0E)
private val Latte      = Color(0xFF8B5E3C)
private val LatteLight = Color(0xFFD4A97A)
private val CardBg     = Color(0xFFFFFFFF)
private val DeleteRed  = Color(0xFFB85C5C)
private val AdminBlue  = Color(0xFF4A7C59)

@Composable
fun AdminCustomerScreen(navController: NavController, viewModel: AppViewModel) {
    val customers by viewModel.customers.collectAsState()

    // State ควบคุม Dialogs
    var showAddEditDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var isEditMode by remember { mutableStateOf(false) }
    var selectedCustomer by remember { mutableStateOf<CustomerData?>(null) }

    // State สำหรับฟอร์มกรอกข้อมูล
    var nameInput by remember { mutableStateOf("") }
    var phoneInput by remember { mutableStateOf("") }
    var emailInput by remember { mutableStateOf("") }
    var pwdInput by remember { mutableStateOf("") } // ใช้เฉพาะตอน Add

    // State สำหรับ Dropdowns
    var roleExpanded by remember { mutableStateOf(false) }
    var selectedRole by remember { mutableStateOf("user") }
    val roleOptions = listOf("user", "admin")

    var receiveTypeExpanded by remember { mutableStateOf(false) }
    var selectedReceiveType by remember { mutableStateOf("Dine-in") }
    val receiveTypeOptions = listOf("Dine-in", "Takeaway")

    LaunchedEffect(Unit) {
        viewModel.fetchCustomers()
    }

    // ── Dialog: เพิ่ม / แก้ไข ลูกค้า ──
    if (showAddEditDialog) {
        AlertDialog(
            onDismissRequest = { showAddEditDialog = false },
            title = { Text(if (isEditMode) "แก้ไขข้อมูลลูกค้า" else "เพิ่มลูกค้าใหม่", color = Espresso, fontWeight = FontWeight.Bold) },
            text = {
                // ใช้ LazyColumn เผื่อจอมือถือเล็ก จะได้เลื่อนกรอกได้
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    item {
                        OutlinedTextField(value = nameInput, onValueChange = { nameInput = it }, label = { Text("ชื่อ-นามสกุล") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                    }
                    item {
                        OutlinedTextField(value = phoneInput, onValueChange = { phoneInput = it }, label = { Text("เบอร์โทรศัพท์") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone), singleLine = true, modifier = Modifier.fillMaxWidth())
                    }
                    item {
                        OutlinedTextField(value = emailInput, onValueChange = { emailInput = it }, label = { Text("อีเมล") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email), singleLine = true, modifier = Modifier.fillMaxWidth())
                    }

                    // รหัสผ่าน โชว์เฉพาะตอน "สร้างใหม่"
                    if (!isEditMode) {
                        item {
                            OutlinedTextField(value = pwdInput, onValueChange = { pwdInput = it }, label = { Text("รหัสผ่าน") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                        }
                    }

                    // Dropdown: Role
                    item {
                        ExposedDropdownMenuBox(expanded = roleExpanded, onExpandedChange = { roleExpanded = !roleExpanded }) {
                            OutlinedTextField(value = selectedRole.uppercase(), onValueChange = {}, readOnly = true, label = { Text("สิทธิ์ (Role)") }, trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = roleExpanded) }, modifier = Modifier.menuAnchor().fillMaxWidth(), colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors())
                            ExposedDropdownMenu(expanded = roleExpanded, onDismissRequest = { roleExpanded = false }) {
                                roleOptions.forEach { role ->
                                    DropdownMenuItem(text = { Text(role.uppercase()) }, onClick = { selectedRole = role; roleExpanded = false })
                                }
                            }
                        }
                    }

                    // Dropdown: Receive Type
                    item {
                        ExposedDropdownMenuBox(expanded = receiveTypeExpanded, onExpandedChange = { receiveTypeExpanded = !receiveTypeExpanded }) {
                            OutlinedTextField(value = selectedReceiveType, onValueChange = {}, readOnly = true, label = { Text("ประเภทการรับของ") }, trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = receiveTypeExpanded) }, modifier = Modifier.menuAnchor().fillMaxWidth(), colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors())
                            ExposedDropdownMenu(expanded = receiveTypeExpanded, onDismissRequest = { receiveTypeExpanded = false }) {
                                receiveTypeOptions.forEach { type ->
                                    DropdownMenuItem(text = { Text(type) }, onClick = { selectedReceiveType = type; receiveTypeExpanded = false })
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (isEditMode && selectedCustomer != null) {
                            viewModel.updateCustomer(selectedCustomer!!.id, nameInput, phoneInput, emailInput, selectedRole, selectedReceiveType)
                        } else {
                            viewModel.addCustomer(nameInput, phoneInput, emailInput, pwdInput, selectedRole, selectedReceiveType)
                        }
                        showAddEditDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Latte),
                    // ป้องกันปุ่มถ้ายอมกรอกไม่ครบ
                    enabled = nameInput.isNotBlank() && phoneInput.isNotBlank() && (isEditMode || pwdInput.isNotBlank())
                ) { Text("บันทึก") }
            },
            dismissButton = { TextButton(onClick = { showAddEditDialog = false }) { Text("ยกเลิก") } }
        )
    }

    // ── Dialog: ยืนยันการลบ ──
    if (showDeleteDialog && selectedCustomer != null) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("ยืนยันการลบบัญชี", fontWeight = FontWeight.Bold, color = Espresso) },
            text = { Text("คุณต้องการลบลูกค้า '${selectedCustomer!!.name}' ใช่หรือไม่?", color = Latte) },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteCustomer(selectedCustomer!!.id)
                        showDeleteDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = DeleteRed)
                ) { Text("ลบ", color = Color.White) }
            },
            dismissButton = { TextButton(onClick = { showDeleteDialog = false }) { Text("ยกเลิก", color = Latte) } },
            containerColor = Cream
        )
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("จัดการลูกค้า", fontSize = 17.sp, fontWeight = FontWeight.SemiBold, color = Espresso)
                        Text("Cafe Hub", fontSize = 12.sp, color = Latte)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) { Icon(Icons.Outlined.ArrowBack, "กลับ", tint = Espresso) }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Cream)
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    // เคลียร์ฟอร์มก่อนเปิด Add Mode
                    isEditMode = false
                    nameInput = ""; phoneInput = ""; emailInput = ""; pwdInput = ""
                    selectedRole = "user"; selectedReceiveType = "Dine-in"
                    showAddEditDialog = true
                },
                containerColor = Espresso,
                contentColor = Cream,
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(Icons.Outlined.Add, "เพิ่มลูกค้า")
            }
        },
        containerColor = Cream
    ) { padding ->
        if (customers.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Outlined.Group, contentDescription = null, modifier = Modifier.size(48.dp), tint = LatteLight)
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("ไม่พบข้อมูลลูกค้า", color = Latte, fontSize = 15.sp)
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item { Text("${customers.size} บัญชี", fontSize = 13.sp, color = Latte, fontWeight = FontWeight.Medium, modifier = Modifier.padding(bottom = 4.dp)) }

                items(customers) { customer ->
                    CustomerAdminCard(
                        customer = customer,
                        onEdit = {
                            // ดึงข้อมูลเก่ามาใส่ฟอร์ม เปิด Edit Mode
                            isEditMode = true
                            selectedCustomer = customer
                            nameInput = customer.name
                            phoneInput = customer.phone
                            emailInput = customer.email
                            selectedRole = customer.role
                            selectedReceiveType = customer.receiveType ?: "Dine-in"
                            showAddEditDialog = true
                        },
                        onDelete = {
                            selectedCustomer = customer
                            showDeleteDialog = true
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun CustomerAdminCard(customer: CustomerData, onEdit: () -> Unit, onDelete: () -> Unit) {
    val isAdmin = customer.role.lowercase() == "admin"
    val roleColor = if (isAdmin) AdminBlue else LatteLight

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CardBg),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier.size(44.dp).background(roleColor.copy(alpha = 0.15f), RoundedCornerShape(22.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(if (isAdmin) Icons.Outlined.AdminPanelSettings else Icons.Outlined.Person, contentDescription = null, tint = roleColor, modifier = Modifier.size(24.dp))
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(text = customer.name, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = Espresso)
                Text(text = "${customer.email} | ${customer.phone}", fontSize = 12.sp, color = Latte)

                Row(modifier = Modifier.padding(top = 6.dp), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Surface(shape = RoundedCornerShape(4.dp), color = roleColor.copy(alpha = 0.1f)) {
                        Text(text = customer.role.uppercase(), fontSize = 10.sp, fontWeight = FontWeight.Bold, color = roleColor, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                    }
                    if (!customer.receiveType.isNullOrEmpty()) {
                        Surface(shape = RoundedCornerShape(4.dp), color = Cream) {
                            Text(text = customer.receiveType, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Latte, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                        }
                    }
                }
            }

            IconButton(onClick = onEdit, modifier = Modifier.size(36.dp)) {
                Icon(Icons.Outlined.Edit, contentDescription = "แก้ไขข้อมูล", tint = Latte, modifier = Modifier.size(20.dp))
            }
            IconButton(onClick = onDelete, modifier = Modifier.size(36.dp)) {
                Icon(Icons.Outlined.Delete, contentDescription = "ลบ", tint = DeleteRed, modifier = Modifier.size(20.dp))
            }
        }
    }
}