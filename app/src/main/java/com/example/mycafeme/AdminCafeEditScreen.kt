@file:OptIn(ExperimentalMaterial3Api::class)
package com.example.mycafeme

import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream

// ── โทนสีกาแฟมินิมอล ───────────────────────────────────────
private val Cream      = Color(0xFFFAF6F1)
private val Espresso   = Color(0xFF2C1A0E)
private val Latte      = Color(0xFF8B5E3C)
private val LatteLight = Color(0xFFD4A97A)
private val CardBg     = Color(0xFFFFFFFF)
private val DeleteRed  = Color(0xFFB85C5C)

// ⚠️ ตั้งค่า BASE_URL ให้ตรงกับหลังบ้านของพี่
private const val BASE_IMAGE_URL = "http://10.0.2.2:3520/uploads/"

@Composable
fun AdminCafeEditScreen(navController: NavController, viewModel: AppViewModel, cafeId: String) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val cafes by viewModel.cafes.collectAsState()
    val menus by viewModel.menus.collectAsState()
    val categories by viewModel.categories.collectAsState()
    val cafe = cafes.find { it.id == cafeId }

    // State สำหรับฟอร์มร้านค้า
    var name      by remember { mutableStateOf(cafe?.name ?: "") }
    var location  by remember { mutableStateOf(cafe?.location ?: "") }
    var rating    by remember { mutableStateOf(cafe?.rating ?: "0.0") }
    var openTime  by remember { mutableStateOf(cafe?.openTime ?: "08:00:00") }
    var closeTime by remember { mutableStateOf(cafe?.closeTime ?: "20:00:00") }

    // ── ส่วนจัดการรูปภาพร้านค้า ──
    var cafeImageUri by remember { mutableStateOf<Uri?>(null) }
    var cafeImageFile by remember { mutableStateOf<File?>(null) }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            cafeImageUri = uri
            coroutineScope.launch(Dispatchers.IO) {
                cafeImageFile = createTempFileFromUri(context, uri)
            }
        }
    }

    // State ควบคุม Dialogs
    var showOpenPicker by remember { mutableStateOf(false) }
    var showClosePicker by remember { mutableStateOf(false) }
    var showAddMenuDialog by remember { mutableStateOf(false) }
    var showEditMenuDialog by remember { mutableStateOf(false) }
    var showDeleteMenuDialog by remember { mutableStateOf(false) }
    var selectedMenu by remember { mutableStateOf<MenuData?>(null) }

    // State สำหรับฟอร์มเมนู
    var menuNameInput by remember { mutableStateOf("") }
    var menuPriceInput by remember { mutableStateOf("") }
    var selectedCategoryId by remember { mutableStateOf("") }
    var categoryExpanded by remember { mutableStateOf(false) }

    LaunchedEffect(cafeId) {
        viewModel.fetchMenus(cafeId)
        viewModel.fetchCategories()
    }

    // ── ระบบนาฬิกา ──────────────────────────────
    if (showOpenPicker) {
        MyTimePickerDialog(openTime, { showOpenPicker = false }, { h, m -> openTime = String.format("%02d:%02d:00", h, m); showOpenPicker = false })
    }
    if (showClosePicker) {
        MyTimePickerDialog(closeTime, { showClosePicker = false }, { h, m -> closeTime = String.format("%02d:%02d:00", h, m); showClosePicker = false })
    }

    // ── หน้าต่าง เพิ่มเมนู / แก้ไขเมนู (ไม่มีรูปภาพ) ──────────────────────
    if (showAddMenuDialog || showEditMenuDialog) {
        val isEditMode = showEditMenuDialog
        AlertDialog(
            onDismissRequest = { showAddMenuDialog = false; showEditMenuDialog = false },
            title = { Text(if (isEditMode) "แก้ไขเมนู" else "เพิ่มเมนูใหม่", color = Espresso, fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = menuNameInput, onValueChange = { menuNameInput = it }, label = { Text("ชื่อเมนู") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = menuPriceInput, onValueChange = { if (it.isEmpty() || it.matches(Regex("^\\d*\\.?\\d*$"))) menuPriceInput = it }, label = { Text("ราคา (บาท)") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), singleLine = true, modifier = Modifier.fillMaxWidth())
                    ExposedDropdownMenuBox(expanded = categoryExpanded, onExpandedChange = { categoryExpanded = !categoryExpanded }) {
                        OutlinedTextField(
                            value = categories.find { it.id == selectedCategoryId }?.name ?: "เลือกหมวดหมู่",
                            onValueChange = {}, readOnly = true, label = { Text("หมวดหมู่") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryExpanded) },
                            modifier = Modifier.menuAnchor().fillMaxWidth(), colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors()
                        )
                        ExposedDropdownMenu(expanded = categoryExpanded, onDismissRequest = { categoryExpanded = false }) {
                            categories.forEach { category ->
                                DropdownMenuItem(text = { Text(category.name) }, onClick = { selectedCategoryId = category.id; categoryExpanded = false })
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (isEditMode && selectedMenu != null) {
                            viewModel.updateMenu(selectedMenu!!.id, cafeId, menuNameInput, menuPriceInput, selectedCategoryId)
                        } else {
                            viewModel.addMenu(cafeId, menuNameInput, menuPriceInput, selectedCategoryId)
                        }
                        showAddMenuDialog = false; showEditMenuDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Latte),
                    enabled = menuNameInput.isNotBlank() && menuPriceInput.isNotBlank() && selectedCategoryId.isNotBlank()
                ) { Text(if (isEditMode) "บันทึก" else "เพิ่ม") }
            },
            dismissButton = { TextButton(onClick = { showAddMenuDialog = false; showEditMenuDialog = false }) { Text("ยกเลิก") } }
        )
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("แก้ไขร้านค้า", fontSize = 17.sp, fontWeight = FontWeight.SemiBold, color = Espresso)
                        Text("Cafe Hub", fontSize = 12.sp, color = Latte)
                    }
                },
                navigationIcon = { IconButton(onClick = { navController.popBackStack() }) { Icon(Icons.Outlined.ArrowBack, "กลับ", tint = Espresso) } },
                actions = {
                    TextButton(onClick = {
                        viewModel.updateCafe(cafeId, name, location, openTime, closeTime, rating, cafeImageFile)
                        navController.popBackStack()
                    }) {
                        Text("บันทึก", color = Latte, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
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
            item { SectionLabel(text = "ข้อมูลร้าน") }

            item {
                CafeCard {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {

                        // ── ส่วนแสดงรูปภาพ (จุดสำคัญที่ทำให้รูปโชว์) ──
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(180.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Cream)
                                .clickable { launcher.launch("image/*") },
                            contentAlignment = Alignment.Center
                        ) {
                            // 👈 ลอจิกเช็คลำดับความสำคัญของรูป
                            val imageSource: Any? = when {
                                cafeImageFile != null -> cafeImageFile                // 1. รูปที่เพิ่งเลือกใหม่
                                cafeImageUri != null -> cafeImageUri                  // 2. รูปที่เพิ่งเลือกใหม่ (Uri)
                                !cafe?.img.isNullOrEmpty() -> BASE_IMAGE_URL + cafe?.img // 3. รูปเก่าจาก Server
                                else -> null                                          // 4. ไม่มีรูป
                            }

                            if (imageSource != null) {
                                AsyncImage(
                                    model = imageSource,
                                    contentDescription = "Cafe Image",
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            } else {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(Icons.Outlined.AddPhotoAlternate, null, modifier = Modifier.size(48.dp), tint = LatteLight)
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text("แตะเพื่อเปลี่ยนรูปร้าน", fontSize = 14.sp, color = Latte)
                                }
                            }
                        }

                        CafeEditField(value = name, onValueChange = { name = it }, label = "ชื่อร้าน", icon = Icons.Outlined.Home)
                        CafeEditField(value = location, onValueChange = { location = it }, label = "ที่อยู่", icon = Icons.Outlined.LocationOn)
                        CafeEditField(value = rating, onValueChange = { rating = it }, label = "คะแนนร้าน (0.0 - 5.0)", icon = Icons.Outlined.Star)
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            TimeDisplayField("เปิด", openTime.take(5), Modifier.weight(1f)) { showOpenPicker = true }
                            TimeDisplayField("ปิด", closeTime.take(5), Modifier.weight(1f)) { showClosePicker = true }
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(4.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    SectionLabel(text = "เมนูในร้าน")
                    OutlinedButton(
                        onClick = { menuNameInput = ""; menuPriceInput = ""; selectedCategoryId = ""; showAddMenuDialog = true },
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Latte)
                    ) {
                        Icon(Icons.Outlined.Add, null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("เพิ่มเมนู", fontSize = 13.sp)
                    }
                }
            }

            items(menus) { menu ->
                MenuAdminCard(
                    menu = menu,
                    onEdit = {
                        selectedMenu = menu
                        menuNameInput = menu.name
                        menuPriceInput = menu.price
                        selectedCategoryId = menu.categoryId ?: ""
                        showEditMenuDialog = true
                    },
                    onDelete = { selectedMenu = menu; showDeleteMenuDialog = true }
                )
            }
        }
    }
}

// ── ฟังก์ชันตัวช่วย (ใส่ private ไว้กันชื่อซ้ำ) ──────────────────────


@Composable
fun MyTimePickerDialog(initialTime: String, onDismiss: () -> Unit, onConfirm: (Int, Int) -> Unit) {
    val parts = initialTime.split(":")
    val hour = parts.getOrNull(0)?.toIntOrNull() ?: 8
    val minute = parts.getOrNull(1)?.toIntOrNull() ?: 0
    val state = rememberTimePickerState(initialHour = hour, initialMinute = minute, is24Hour = true)
    AlertDialog(onDismissRequest = onDismiss, confirmButton = { TextButton(onClick = { onConfirm(state.hour, state.minute) }) { Text("ตกลง", color = Latte) } }, dismissButton = { TextButton(onClick = onDismiss) { Text("ยกเลิก") } }, text = { Column(horizontalAlignment = Alignment.CenterHorizontally) { Text("เลือกเวลา", fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 16.dp)); TimePicker(state = state) } })
}

@Composable
private fun TimeDisplayField(label: String, time: String, modifier: Modifier, onClick: () -> Unit) {
    OutlinedTextField(value = time, onValueChange = {}, label = { Text(label, fontSize = 12.sp, color = Latte) }, leadingIcon = { Icon(Icons.Outlined.Schedule, null, modifier = Modifier.size(18.dp), tint = Latte) }, modifier = modifier.clickable { onClick() }, enabled = false, readOnly = true, shape = RoundedCornerShape(12.dp), colors = OutlinedTextFieldDefaults.colors(disabledTextColor = Espresso, disabledBorderColor = LatteLight.copy(alpha = 0.6f), disabledLabelColor = Latte, disabledLeadingIconColor = Latte, disabledContainerColor = Color.Transparent))
}

@Composable
private fun SectionLabel(text: String) { Text(text = text, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = Latte, modifier = Modifier.padding(bottom = 4.dp)) }

@Composable
private fun CafeCard(content: @Composable ColumnScope.() -> Unit) { Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = CardBg)) { Column(modifier = Modifier.padding(16.dp), content = content) } }

@Composable
private fun CafeEditField(value: String, onValueChange: (String) -> Unit, label: String, icon: androidx.compose.ui.graphics.vector.ImageVector) { OutlinedTextField(value = value, onValueChange = onValueChange, label = { Text(label, fontSize = 13.sp, color = Latte) }, leadingIcon = { Icon(icon, null, tint = Latte, modifier = Modifier.size(18.dp)) }, singleLine = true, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Latte, unfocusedBorderColor = LatteLight.copy(alpha = 0.6f))) }

@Composable
fun MenuAdminCard(menu: MenuData, onEdit: () -> Unit, onDelete: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp), colors = CardDefaults.cardColors(containerColor = CardBg)) {
        Row(modifier = Modifier.padding(16.dp).fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(40.dp).background(LatteLight.copy(alpha = 0.25f), RoundedCornerShape(10.dp)), contentAlignment = Alignment.Center) { Icon(Icons.Outlined.Menu, null, tint = Latte, modifier = Modifier.size(20.dp)) }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(menu.name, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Espresso)
                Text("${menu.price} บาท", fontSize = 13.sp, color = Latte)
            }
            IconButton(onClick = onEdit) { Icon(Icons.Outlined.Edit, null, tint = Latte, modifier = Modifier.size(18.dp)) }
            IconButton(onClick = onDelete) { Icon(Icons.Outlined.Delete, null, tint = DeleteRed, modifier = Modifier.size(18.dp)) }
        }
    }
}