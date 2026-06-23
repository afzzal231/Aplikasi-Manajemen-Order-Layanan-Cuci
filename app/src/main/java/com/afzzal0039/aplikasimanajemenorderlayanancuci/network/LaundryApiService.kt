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
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Query

private val moshi = Moshi.Builder()
    .add(KotlinJsonAdapterFactory())
    .build()

private val retrofit = Retrofit.Builder()
    .addConverterFactory(MoshiConverterFactory.create(moshi))
    .baseUrl("https://apilaundry-production.up.railway.app/")
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
        @Part("nama_pelanggan") namaPelanggan: RequestBody,
        @Part("paket_layanan") paketLayanan: RequestBody,
        @Part("berat") berat: RequestBody,
        @Part("total_harga") totalHarga: RequestBody,
        @Part("estimasi_selesai") estimasiSelesai: RequestBody,
        @Part("is_jaket") isJaket: RequestBody,
        @Part("is_sprei") isSprei: RequestBody,
        @Part image: MultipartBody.Part
    ): OpStatus

    @FormUrlEncoded
    @POST("api_order.php")
    suspend fun updateOrder(
        @Header("Authorization") userId: String,
        @Field("action") action: String,
        @Field("id") orderId: Int,
        @Field("nama_pelanggan") namaPelanggan: String,
        @Field("paket_layanan") paketLayanan: String,
        @Field("berat") berat: Float,
        @Field("total_harga") totalHarga: Int,
        @Field("estimasi_selesai") estimasiSelesai: String,
        @Field("is_jaket") isJaket: Int,
        @Field("is_sprei") isSprei: Int
    ): OpStatus

    @FormUrlEncoded
    @POST("api_order.php")
    suspend fun updateTrashStatus(
        @Header("Authorization") userId: String,
        @Field("action") action: String,
        @Field("id") orderId: Int
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