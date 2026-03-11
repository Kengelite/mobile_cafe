package com.example.mycafeme

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.DELETE


// 1. API Interface
interface CafeApiService {
    @POST("api/login")
    suspend fun login(@Body request: LoginRequest): LoginResponse

    // --- ส่วนของ Category (ที่ขาด POST ไป) ---
    @POST("api/categories")
    suspend fun addCategory(@Body name: Map<String, String>): SimpleResponse

    @GET("api/categories")
    suspend fun getCategories(): CategoryResponse

    @PUT("api/categories/{id}")
    suspend fun updateCategory(@Path("id") id: String, @Body name: Map<String, String>): SimpleResponse

    @DELETE("api/categories/{id}")
    suspend fun deleteCategory(@Path("id") id: String): SimpleResponse


    // --- ส่วนของ Cafe (สำหรับหน้าจัดการร้านค้า) ---
    @GET("api/cafes")
    suspend fun getCafes(): CafeResponse

    @POST("api/cafes")
    suspend fun addCafe(@Body cafeData: Map<String, String>): SimpleResponse

    @PUT("api/cafes/{id}")
    suspend fun updateCafe(@Path("id") id: String, @Body cafeData: Map<String, String>): SimpleResponse

    @DELETE("api/cafes/{id}")
    suspend fun deleteCafe(@Path("id") id: String): SimpleResponse



    @GET("api/cafes/{id}/menus")
    suspend fun getMenusByCafe(@Path("id") id: String): MenuResponse

    @POST("api/menus")
    suspend fun addMenu(@Body data: Map<String, String>): SimpleResponse

    @PUT("api/menus/{id}")
    suspend fun updateMenu(@Path("id") id: String, @Body data: Map<String, String>): SimpleResponse

    @DELETE("api/menus/{id}")
    suspend fun deleteMenu(@Path("id") id: String): SimpleResponse


    @GET("api/customers")
    suspend fun getCustomers(): CustomerResponse

    @PUT("api/customers/{id}/role")
    suspend fun updateCustomerRole(@Path("id") id: String, @Body data: Map<String, String>): SimpleResponse

    @DELETE("api/customers/{id}")
    suspend fun deleteCustomer(@Path("id") id: String): SimpleResponse

    @POST("api/customers")
    suspend fun addCustomer(@Body data: Map<String, String>): SimpleResponse

    @PUT("api/customers/{id}") // 👈 อัปเดตข้อมูลทั้งหมด ไม่ใช่แค่ role
    suspend fun updateCustomer(@Path("id") id: String, @Body data: Map<String, String?>): SimpleResponse



    @GET("api/orders")
    suspend fun getOrders(): OrderResponse

    @GET("api/orders/{id}/details") // ดึงรายการสินค้าข้างในออเดอร์
    suspend fun getOrderDetails(@Path("id") orderId: String): OrderDetailResponse


    @PUT("api/orders/{id}/status")
    suspend fun updateOrderStatus(
        @Path("id") id: String,
        @Body data: Map<String, String>
    ): SimpleResponse


    // ในไฟล์ ApiService.kt
    @POST("/api/cart/add")
    suspend fun addToCart(@Body body: CartRequest): GenericResponse //  เปลี่ยนตรงนี้


    // ส่ง Customer_Id ไปดึงตะกร้า
    @GET("/api/cart/{customerId}")
    suspend fun getCartItems(@Path("customerId") customerId: String): CartResponse


    @GET("/api/bills/{customerId}")
    suspend fun getBills(@Path("customerId") customerId: String): BillResponse

    // 👈 2. ดึงรายละเอียดในบิลแต่ละใบ
    @GET("/api/bill-details/{orderId}")
    suspend fun getBillDetails(@Path("orderId") orderId: String): BillDetailResponse


    @PUT("/api/cart/update")
    suspend fun updateCartQuantity(@Body body: Map<String, Any>): GenericResponse

    // ลบรายการ
    @DELETE("/api/cart/item/{id}")
    suspend fun deleteCartItem(@Path("id") detailId: String): GenericResponse


    @PUT("/api/orders/confirm")
    suspend fun confirmOrder(@Body body: @JvmSuppressWildcards Map<String, Any>): GenericResponse

}

object RetrofitClient {
    private const val BASE_URL = "http://10.0.2.2:3520/"

    val apiService: CafeApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(CafeApiService::class.java)
    }
}