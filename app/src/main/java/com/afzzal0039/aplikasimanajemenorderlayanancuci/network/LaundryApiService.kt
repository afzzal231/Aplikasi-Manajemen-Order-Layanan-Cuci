package com.afzzal0039.aplikasimanajemenorderlayanancuci.network

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import com.afzzal0039.aplikasimanajemenorderlayanancuci.model.OpStatus
import com.afzzal0039.aplikasimanajemenorderlayanancuci.model.Order
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Query

private const val BASE_URL = "http://10.0.2.2/laundry_api/"

private val moshi = Moshi.Builder()
    .add(KotlinJsonAdapterFactory())
    .build()

private val retrofit = Retrofit.Builder()
    .addConverterFactory(MoshiConverterFactory.create(moshi))
    .baseUrl(BASE_URL)
    .build()

interface LaundryApiService {

    @GET("api_order.php")
    suspend fun getOrders(
        @Header("Authorization") userId: String
    ): List<Order>

    @Multipart
    @POST("api_order.php")
    suspend fun postOrder(
        @Header("Authorization") userId: String,
        @Part("nama_layanan") namaLayanan: RequestBody,
        @Part("keterangan") keterangan: RequestBody,
        @Part image: MultipartBody.Part
    ): OpStatus

    @DELETE("api_order.php")
    suspend fun deleteOrder(
        @Header("Authorization") userId: String,
        @Query("id") orderId: Int
    ): OpStatus
}

object LaundryApi {
    val retrofitService: LaundryApiService by lazy {
        retrofit.create(LaundryApiService::class.java)
    }
}