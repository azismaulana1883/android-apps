package com.example.qcinline

import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.RequestBody
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

// Response Model
data class QCSubmitResponse(
    val message: String,
    val session_id: Int? = null,
    val error: String? = null
)

interface ApiService {
    @Multipart
    @POST("/api/qc-submit")
    suspend fun submitQC(
        @Part("sessionData") sessionData: RequestBody,
        @Part("defectsData") defectsData: RequestBody,
        @Part photos: List<MultipartBody.Part>
    ): QCSubmitResponse
}

object NetworkClient {
    // Diganti menggunakan IP Address Laptop Anda agar bisa diakses oleh HP Asli di jaringan WiFi yang sama
    private const val BASE_URL = "http://192.168.100.41:3000"

    private val logging = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val client = OkHttpClient.Builder()
        .addInterceptor(logging)
        .build()

    val apiService: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }
}
