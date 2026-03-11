package com.example.mycafeme
import com.google.gson.annotations.SerializedName
// 1. กล่องเก็บข้อมูลร้านกาแฟ (id ใช้เป็น UUID แบบ String ตามที่คุณออกแบบ Database ไว้)
data class CafeData(
    @SerializedName("Cafe_ID")
    val id: String,

    @SerializedName("Cafe_Name")
    val name: String,

    @SerializedName("Cafe_OpenTime")
    val openTime: String,

    @SerializedName("Cafe_CloseTime")
    val closeTime: String,

    @SerializedName("Cafe_Location")
    val location: String, // ผมเปลี่ยนชื่อจาก category เป็น location ให้สอดคล้องกับข้อมูลจริงนะครับ

    @SerializedName("Cafe_Rating")
    val rating: String,


    @SerializedName("img")
    val img: String?, // เผื่อไว้ใช้โหลดรูปภาพในอนาคต (ใส่ ? ไว้เผื่อค่ามันเป็น null)


    var isFavorite: Boolean = false
)

//  ตรวจสอบไฟล์ LoginResponse.kt ของพี่
data class LoginResponse(
    val success: Boolean,
    val message: String,
    val role: String,
    val token: String?,
    //  เพิ่มบรรทัดนี้ลงไปเพื่อให้ Android รู้จักคำว่า .data
    val data: CustomerData?
)

data class CafeResponse(
    val success: Boolean,
    val data: List<CafeData>
)


// 2. กล่องรับส่งข้อมูลตอน Login
data class LoginRequest(
    val username: String,
    val password: String
)

//data class LoginResponse(
//    val message: String,
//    val role: String,
//    val token: String
//)


data class CategoryData(
    @SerializedName("category_Id")
    val id: String,
    @SerializedName("category_Name")
    val name: String
)

data class CategoryResponse(
    val success: Boolean,
    val data: List<CategoryData>
)

data class SimpleResponse(
    val success: Boolean,
    val message: String
)

data class MenuData(
    //  เพิ่ม alternate ดักจับทั้ง Menu_Id และ Menu_ID
    @SerializedName(value = "Menu_Id", alternate = ["Menu_ID", "menu_id"])
    val id: String,

    @SerializedName(value = "Menu_Name", alternate = ["menu_name"])
    val name: String,

    @SerializedName(value = "Menu_Price", alternate = ["menu_price"])
    val price: String,

    // ฐานข้อมูลพี่ใช้ Cafe_Cafe_ID เลยต้องดักไว้ด้วยครับ
    @SerializedName(value = "Cafe_Cafe_ID", alternate = ["Cafe_ID", "cafe_id"])
    val cafeId: String? ,// ใส่ ? เผื่อไว้กันเหนียว


    @SerializedName("Category_Image")
    val CategoryImage: String?,


    @SerializedName("category_category_Id")
    val categoryId: String?

)


data class CustomerData(
    @SerializedName("Customer_Id") val id: String,
    @SerializedName("Customer_Name") val name: String,
    @SerializedName("Customer_Phone") val phone: String,
    @SerializedName("email") val email: String,
    @SerializedName("role") val role: String,
    @SerializedName("Customer_ReceiveType") val receiveType: String?
)


data class CustomerResponse(val success: Boolean, val data: List<CustomerData>)

data class MenuResponse(val success: Boolean, val data: List<MenuData>)



data class OrderData(
    @SerializedName(value = "Order_Id", alternate = ["id", "order_id"])
    val id: String,

    @SerializedName(value = "Order_NetPrice", alternate = ["net_price", "total_price"])
    val netPrice: String, //  ใช้ netPrice ให้ตรงกับฐานข้อมูล

    @SerializedName(value = "Order_Date", alternate = ["order_date"])
    val orderDate: String?,

    @SerializedName(value = "Status_Status_ID", alternate = ["status", "status_id"])
    val status: String? //  ใช้ status ดึงข้อมูล Status_Status_ID
)

data class OrderResponse(val success: Boolean, val data: List<OrderData>)

data class OrderDetailData(
    @SerializedName(value = "OrderDetail_Id", alternate = ["id"]) val id: String,
    @SerializedName(value = "OrderDetail_Quantity", alternate = ["quantity"]) val quantity: String,
    @SerializedName(value = "OrderDetail_Price", alternate = ["price"]) val price: String,
    @SerializedName(value = "Menu_Name", alternate = ["menu_name"]) val menuName: String?
)

data class OrderDetailResponse(val success: Boolean, val data: List<OrderDetailData>)

data class GenericResponse(
    val success: Boolean,
    val message: String? = null // ใส่ ? ไว้เผื่อบางที Node.js ไม่ได้ส่ง message มาจะได้ไม่พัง
)

data class CartRequest(
    @SerializedName("Customer_Id") val customerId: String,
    @SerializedName("Cafe_ID") val cafeId: String,
    @SerializedName("Menu_Id") val menuId: String,
    @SerializedName("Quantity") val quantity: Int,
    @SerializedName("price") val price: Double
)

data class CartItemData(
    @SerializedName("OrderDetail_Id") val detailId: String,
    @SerializedName("Menu_Name") val menuName: String,
    @SerializedName("OrderDetail_Price") val price: Double,
    @SerializedName("OrderDetail_Quantity") val quantity: Int
)

// 2. คลาสรับก้อนข้อมูลจาก API
data class CartResponse(
    val success: Boolean,
    val items: List<CartItemData> = emptyList(), // รายการอาหาร
    val message: String? = null
)

// กล่องรับรายการบิล (หน้า History)
data class BillResponse(
    val success: Boolean,
    val bills: List<BillListData> = emptyList()
)

data class BillListData(
    @SerializedName("Order_Id") val id: String,
    @SerializedName("Order_Date") val date: String,
    @SerializedName("Order_NetPrice") val totalPrice: Double,
    @SerializedName("Order_status") val status: String
)

// กล่องรับรายละเอียดเมนูในบิล (หน้า Detail)
data class BillDetailResponse(
    val success: Boolean,
    val items: List<CartItemData> = emptyList() // ใช้ CartItemData ร่วมกันได้เลยครับ เพราะโครงสร้างเหมือนกัน
)