package com.example.mycafeme

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

// 1. เปลี่ยนจาก ViewModel เป็น AndroidViewModel เพื่อใช้ Context สำหรับ SessionManager
class AppViewModel(application: Application) : AndroidViewModel(application) {

    private val sessionManager = SessionManager(application)

    // ── ข้อมูลผู้ใช้ปัจจุบัน (ดึงจาก Cache ทันทีที่เปิดแอป) ──
    private val _currentUser = MutableStateFlow<CustomerData?>(sessionManager.getUser())
    val currentUser: StateFlow<CustomerData?> = _currentUser.asStateFlow()

    private val _userRole = MutableStateFlow(sessionManager.getRole() ?: "")
    val userRole: StateFlow<String> = _userRole.asStateFlow()

    // ── รายการข้อมูลต่างๆ (เหมือนเดิม) ──
    private val _cafes = MutableStateFlow<List<CafeData>>(emptyList())
    val cafes: StateFlow<List<CafeData>> = _cafes

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _categories = MutableStateFlow<List<CategoryData>>(emptyList())
    val categories: StateFlow<List<CategoryData>> = _categories

    private val _menus = MutableStateFlow<List<MenuData>>(emptyList())
    val menus: StateFlow<List<MenuData>> = _menus

    private val _customers = MutableStateFlow<List<CustomerData>>(emptyList())
    val customers: StateFlow<List<CustomerData>> = _customers

    private val _orders = MutableStateFlow<List<OrderData>>(emptyList())
    val orders: StateFlow<List<OrderData>> = _orders

    private val _orderDetails = MutableStateFlow<List<OrderDetailData>>(emptyList())
    val orderDetails: StateFlow<List<OrderDetailData>> = _orderDetails
    // ฟังก์ชันยิง API Login
    // แก้ไขบรรทัดประกาศฟังก์ชัน


    private val _cartItems = MutableStateFlow<List<CartItemData>>(emptyList())
    val cartItems: StateFlow<List<CartItemData>> = _cartItems.asStateFlow()



    private val _bills = MutableStateFlow<List<BillListData>>(emptyList())
    val bills: StateFlow<List<BillListData>> = _bills.asStateFlow()

    private val _selectedBillItems = MutableStateFlow<List<CartItemData>>(emptyList())
    val selectedBillItems: StateFlow<List<CartItemData>> = _selectedBillItems.asStateFlow()


    fun login(user: String, pass: String, onSuccess: (String) -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val response = RetrofitClient.apiService.login(LoginRequest(user, pass))

                // 👈 เช็คทั้ง success และเช็คว่าข้อมูล user ต้องไม่เป็น null
                if (response.success && response.data != null) {

                    sessionManager.saveUser(response.data, response.role)
                    _currentUser.value = response.data
                    _userRole.value = response.role

                    onSuccess(response.role)
                } else {
                    onError("Username หรือ Password ไม่ถูกต้อง")
                }
            } catch (e: Exception) {
                // Error ที่พี่เจอจะถูกดักจับที่นี่
                android.util.Log.e("API_ERROR", "Login failed", e)
                onError("เข้าสู่ระบบไม่สำเร็จ: ข้อมูลผู้ใช้ว่างเปล่า")
            } finally {
                _isLoading.value = false
            }
        }
    }


    // ── ฟังก์ชันอัปเดตโปรไฟล์ (อัปเดตทั้ง DB และ State ในแอป) ──
    fun updateProfile(id: String, name: String, phone: String, email: String,receiveType :String, onComplete: () -> Unit) {
        viewModelScope.launch {
            try {
                val body = mapOf(
                    "Customer_Name" to name,
                    "Customer_Phone" to phone,
                    "email" to email,
                    "receiveType" to receiveType
                )
                val response = RetrofitClient.apiService.updateCustomer(id, body)
                if (response.success) {
                    //  4. อัปเดต State และ Cache ทันทีเพื่อให้หน้า Profile เปลี่ยนตาม
                    val updatedUser = _currentUser.value?.copy(
                        name = name,    // 👈 เปลี่ยนจาก Customer_Name เป็น name
                        phone = phone,  // 👈 เปลี่ยนจาก Customer_Phone เป็น phone
                        email = email,
                        receiveType = receiveType
                    )
                    updatedUser?.let {
                        _currentUser.value = it
                        sessionManager.saveUser(it, _userRole.value)
                    }
                    onComplete()
                }
            } catch (e: Exception) {
                android.util.Log.e("UPDATE_PROFILE", "Failed to update", e)
            }
        }
    }

    // ── Logout ล้าง Cache ──
    fun logout(onSuccess: () -> Unit) {
        sessionManager.clearSession() // 👈 5. ลบข้อมูลในเครื่อง
        _currentUser.value = null
        _userRole.value = ""
        onSuccess()
    }


    // ฟังก์ชันยิง API ดึงข้อมูลร้าน (ดึงตามสเต็ปหลังจาก Login ผ่าน)
    fun fetchCafes() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                // 1. รับก้อน Response ทั้งหมดมา
                val response = RetrofitClient.apiService.getCafes()

                // 2. เช็คว่า success เป็น true ไหม แล้วค่อยดึง .data มาใช้
                if (response.success) {
                    _cafes.value = response.data
                }
            } catch (e: Exception) {
                // โชว์ Error ใน Logcat ถ้าดึงข้อมูลพัง
                android.util.Log.e("API_ERROR", "ดึงข้อมูลร้านค้าไม่สำเร็จ", e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    // 1. ดึงข้อมูลหมวดหมู่ทั้งหมด
    fun fetchCategories() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val response = RetrofitClient.apiService.getCategories()
                if (response.success) {
                    _categories.value = response.data
                }
            } catch (e: Exception) {
                android.util.Log.e("API_ERROR", "Fetch Categories Failed", e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    // 2. เพิ่มหมวดหมู่ใหม่
    fun addCategory(name: String) {
        viewModelScope.launch {
            try {
                // ส่งเป็น Map ให้ตรงกับ JSON: { "category_Name": "..." }
                val response = RetrofitClient.apiService.addCategory(mapOf("category_Name" to name))
                if (response.success) {
                    fetchCategories() // เพิ่มเสร็จแล้วดึงข้อมูลใหม่มาโชว์ทันที
                }
            } catch (e: Exception) {
                android.util.Log.e("API_ERROR", "Add Category Failed", e)
            }
        }
    }

    // 3. แก้ไขหมวดหมู่
    fun updateCategory(id: String, name: String) {
        viewModelScope.launch {
            try {
                val response = RetrofitClient.apiService.updateCategory(id, mapOf("category_Name" to name))
                if (response.success) {
                    fetchCategories() // แก้ไขเสร็จแล้วดึงข้อมูลใหม่
                }
            } catch (e: Exception) {
                android.util.Log.e("API_ERROR", "Update Category Failed", e)
            }
        }
    }

    // 4. ลบหมวดหมู่
    fun deleteCategory(id: String) {
        viewModelScope.launch {
            try {
                val response = RetrofitClient.apiService.deleteCategory(id)
                if (response.success) {
                    fetchCategories() // ลบเสร็จแล้วดึงข้อมูลใหม่
                }
            } catch (e: Exception) {
                android.util.Log.e("API_ERROR", "Delete Category Failed", e)
            }
        }
    }


    // 👈 2. ฟังก์ชันดึงเมนูเฉพาะร้าน (Cafe_ID)
    fun fetchMenus(cafeId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val response = RetrofitClient.apiService.getMenusByCafe(cafeId)
                if (response.success) {
                    _menus.value = response.data
                }
            } catch (e: Exception) {
                android.util.Log.e("API_ERROR", "ดึงเมนูไม่สำเร็จ", e)
                _menus.value = emptyList() // ถ้าพังให้ล้างลิสต์
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun addCafe(name: String, location: String, open: String, close: String, rating: String) {
        viewModelScope.launch {
            try {
                val body = mapOf(
                    "Cafe_Name" to name,
                    "Cafe_Location" to location,
                    "Cafe_OpenTime" to open,
                    "Cafe_CloseTime" to close,
                    "Cafe_Rating" to rating
                )
                // ยิงไปที่ API สำหรับ POST
                val response = RetrofitClient.apiService.addCafe(body)
                if (response.success) {
                    fetchCafes() // เพิ่มเสร็จ รีเฟรชลิสต์ทันที
                }
            } catch (e: Exception) {
                android.util.Log.e("API_ERROR", "Add Cafe Failed", e)
            }
        }
    }


    fun updateCafe(id: String, name: String, location: String, open: String, close: String, rating: String) {
        viewModelScope.launch {
            try {
                val body = mapOf(
                    "Cafe_Name" to name,
                    "Cafe_Location" to location,
                    "Cafe_OpenTime" to open,
                    "Cafe_CloseTime" to close,
                    "Cafe_Rating" to rating
                )
                val response = RetrofitClient.apiService.updateCafe(id, body)
                if (response.success) {
                    fetchCafes()
                }
            } catch (e: Exception) {
                android.util.Log.e("API_ERROR", "Update Failed", e)
            }
        }
    }

    fun deleteCafe(id: String) {
        viewModelScope.launch {
            try {
                val response = RetrofitClient.apiService.deleteCafe(id)
                if (response.success) {
                    fetchCafes() // ลบเสร็จให้ดึงข้อมูลใหม่มาโชว์ทันที
                }
            } catch (e: Exception) {
                android.util.Log.e("API_ERROR", "ลบร้านไม่สำเร็จ", e)
            }
        }
    }

    // 👈 4. ฟังก์ชันจัดการเมนู (บันทึกทันทีที่กดเพิ่ม/ลบ/แก้)
    fun addMenu(cafeId: String, name: String, price: String, categoryId: String) {
        viewModelScope.launch {
            try {
                val body = mapOf(
                    "Menu_Name" to name,
                    "Menu_Price" to price,
                    "Cafe_ID" to cafeId,
                    "category_category_Id" to categoryId // 👈 ส่งหมวดหมู่ไปด้วย
                )
                val response = RetrofitClient.apiService.addMenu(body)
                if (response.success) {
                    fetchMenus(cafeId)
                }
            } catch (e: Exception) {
                android.util.Log.e("API_ERROR", "Add Menu Failed", e)
            }
        }
    }

    fun updateMenu(menuId: String, cafeId: String, name: String, price: String, categoryId: String) {
        viewModelScope.launch {
            try {
                val body = mapOf(
                    "Menu_Name" to name,
                    "Menu_Price" to price,
                    "category_category_Id" to categoryId // อัปเดตหมวดหมู่ด้วย
                )
                val response = RetrofitClient.apiService.updateMenu(menuId, body)
                if (response.success) {
                    fetchMenus(cafeId) // โหลดข้อมูลใหม่หลังจากแก้เสร็จ
                }
            } catch (e: Exception) {
                android.util.Log.e("API_ERROR", "Update Menu Failed", e)
            }
        }
    }

    fun deleteMenu(menuId: String, cafeId: String) {
        viewModelScope.launch {
            try {
                val response = RetrofitClient.apiService.deleteMenu(menuId)
                if (response.success) fetchMenus(cafeId) // รีเฟรชลิสต์เมนูทันที
            } catch (e: Exception) { /* Log error */ }
        }
    }


    fun fetchCustomers() {
        viewModelScope.launch {
            try {
                val response = RetrofitClient.apiService.getCustomers()
                if (response.success) {
                    _customers.value = response.data
                }
            } catch (e: Exception) {
                android.util.Log.e("API_ERROR", "Fetch Customers Failed", e)
            }
        }
    }

    fun updateCustomerRole(customerId: String, newRole: String) {
        viewModelScope.launch {
            try {
                val response = RetrofitClient.apiService.updateCustomerRole(customerId, mapOf("role" to newRole))
                if (response.success) fetchCustomers()
            } catch (e: Exception) {
                android.util.Log.e("API_ERROR", "Update Role Failed", e)
            }
        }
    }

    fun deleteCustomer(customerId: String) {
        viewModelScope.launch {
            try {
                val response = RetrofitClient.apiService.deleteCustomer(customerId)
                if (response.success) fetchCustomers()
            } catch (e: Exception) {
                android.util.Log.e("API_ERROR", "Delete Customer Failed", e)
            }
        }
    }

    fun addCustomer(name: String, phone: String, email: String, pwd: String, role: String, receiveType: String) {
        viewModelScope.launch {
            try {
                val body = mapOf(
                    "Customer_Name" to name, "Customer_Phone" to phone,
                    "email" to email, "pwd" to pwd,
                    "role" to role, "Customer_ReceiveType" to receiveType
                )
                val response = RetrofitClient.apiService.addCustomer(body)
                if (response.success) fetchCustomers()
            } catch (e: Exception) { android.util.Log.e("API_ERROR", "Add Customer Failed", e) }
        }
    }

    // แก้ฟังก์ชัน Update ให้ส่งไปครบทุกค่า
    fun updateCustomer(id: String, name: String, phone: String, email: String, role: String, receiveType: String) {
        viewModelScope.launch {
            try {
                val body = mapOf(
                    "Customer_Name" to name, "Customer_Phone" to phone,
                    "email" to email, "role" to role, "Customer_ReceiveType" to receiveType
                )
                val response = RetrofitClient.apiService.updateCustomer(id, body)
                if (response.success) fetchCustomers()
            } catch (e: Exception) { android.util.Log.e("API_ERROR", "Update Customer Failed", e) }
        }
    }

    fun fetchOrders() {
        viewModelScope.launch {
            try {
                val response = RetrofitClient.apiService.getOrders()
                if (response.success) _orders.value = response.data
            } catch (e: Exception) { android.util.Log.e("API", "Fetch Orders Failed", e) }
        }
    }

    fun fetchOrderDetails(orderId: String) {
        viewModelScope.launch {
            try {
                // ล้างค่าเก่าก่อนโหลดของใหม่
                _orderDetails.value = emptyList()
                val response = RetrofitClient.apiService.getOrderDetails(orderId)
                if (response.success) _orderDetails.value = response.data
            } catch (e: Exception) { android.util.Log.e("API", "Fetch Details Failed", e) }
        }
    }

    fun updateOrderStatus(orderId: String, newStatus: String) {
        viewModelScope.launch {
            try {
                // ส่งคำว่า "Status" ไปให้ตรงกับที่ Node.js รอรับ
                val body = mapOf("Status" to newStatus)

                val response = RetrofitClient.apiService.updateOrderStatus(orderId, body)
                if (response.success) {
                    fetchOrders() // อัปเดตเสร็จ โหลดข้อมูลใหม่มาโชว์ทันที!
                }
            } catch (e: Exception) {
                android.util.Log.e("API_ERROR", "Update Order Status Failed", e)
            }
        }
    }

    fun registerCustomer(name: String, phone: String, email: String, pwd: String, receiveType: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            try {
                val body = mapOf(
                    "Customer_Name" to name,
                    "Customer_Phone" to phone,
                    "email" to email,
                    "pwd" to pwd,
                    "role" to "user", // สมัครเองให้เป็น user เสมอ
                    "Customer_ReceiveType" to receiveType
                )
                val response = RetrofitClient.apiService.addCustomer(body)
                if (response.success) {
                    onSuccess() // สมัครสำเร็จให้ย้ายหน้า หรือโชว์ข้อความ
                }
            } catch (e: Exception) {
                android.util.Log.e("REGISTER_ERROR", "Registration failed", e)
            }
        }
    }


    // ภายใน class AppViewModel

    // สมมติว่าพี่มี API endpoint ชื่อ /api/cart/add
    fun addToCart(cafeId: String, menuId: String, quantity: Int,price :Double, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            try {
                // ดึง ID ลูกค้าปัจจุบัน (ต้องไม่เป็น null ถึงจะสั่งได้)
                val customerId = _currentUser.value?.id

                if (customerId == null) {
                    onError("กรุณาเข้าสู่ระบบก่อนสั่งอาหาร")
                    return@launch
                }

                // ข้อมูลที่ส่งให้ Node.js
                val body = CartRequest(
                    customerId = customerId,
                    cafeId = cafeId,
                    menuId = menuId,
                    quantity = quantity,
                    price = price
                )

                // ยิง API (อย่าลืมไปสร้างฟังก์ชันนี้ใน ApiService ด้วยนะครับ)
                val response = RetrofitClient.apiService.addToCart(body)

                if (response.success) {
                    onSuccess()
                } else {
                    onError(response.message ?: "ไม่สามารถเพิ่มลงตะกร้าได้")
                }
            } catch (e: Exception) {
                android.util.Log.e("API_ERROR", "Add to cart failed", e)
                onError("เกิดข้อผิดพลาดในการเชื่อมต่อ")
            }
        }
    }


    fun fetchCartItems() {
        viewModelScope.launch {
            try {
                // ดึง ID ลูกค้าที่ล็อกอินอยู่
                val customerId = _currentUser.value?.id
                if (customerId == null) return@launch
                print(customerId)
                _isLoading.value = true
                val response = RetrofitClient.apiService.getCartItems(customerId)

                if (response.success) {
                    _cartItems.value = response.items // ยัดของลงตะกร้าในแอป
                } else {
                    _cartItems.value = emptyList()
                }
            } catch (e: Exception) {
                android.util.Log.e("API_ERROR", "Fetch cart failed", e)
                _cartItems.value = emptyList()
            } finally {
                _isLoading.value = false
            }
        }
    }
    fun fetchBills() {
        viewModelScope.launch {
            val customerId = _currentUser.value?.id ?: return@launch
            val response = RetrofitClient.apiService.getBills(customerId)
            if (response.success) _bills.value = response.bills
        }
    }

    fun fetchBillDetails(orderId: String) {
        viewModelScope.launch {
            _selectedBillItems.value = emptyList() // ล้างค่าเก่าก่อนโหลดใหม่
            val response = RetrofitClient.apiService.getBillDetails(orderId)
            if (response.success) _selectedBillItems.value = response.items
        }
    }

    // ใน AppViewModel.kt

    // 👈 1. ฟังก์ชันอัปเดตจำนวนสินค้า (+ หรือ -)
    fun updateCartItemQuantity(detailId: String, newQuantity: Int) {
        viewModelScope.launch {
            try {
                val body = mapOf(
                    "OrderDetail_Id" to detailId,
                    "Quantity" to newQuantity
                )
                val response = RetrofitClient.apiService.updateCartQuantity(body)
                if (response.success) {
                    // อัปเดตสำเร็จ -> สั่งดึงข้อมูลตะกร้าใหม่ทันทีเพื่อรีเฟรชหน้าจอและยอดเงิน
                    fetchCartItems()
                }
            } catch (e: Exception) {
                android.util.Log.e("API_ERROR", "Update quantity failed", e)
            }
        }
    }

    // 👈 2. ฟังก์ชันลบสินค้าออกจากตะกร้า
    fun removeCartItem(detailId: String) {
        viewModelScope.launch {
            try {
                val response = RetrofitClient.apiService.deleteCartItem(detailId)
                if (response.success) {
                    // ลบสำเร็จ -> สั่งดึงข้อมูลใหม่
                    fetchCartItems()
                }
            } catch (e: Exception) {
                android.util.Log.e("API_ERROR", "Delete item failed", e)
            }
        }
    }






}