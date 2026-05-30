package com.example.dn_26.data.remote

import com.example.dn_26.BuildConfig
import com.example.dn_26.data.model.*
import kotlinx.serialization.json.Json
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Response
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query
import java.util.concurrent.TimeUnit

/**
 * Retrofit service interface for drone-related API endpoints.
 */
interface DroneApiService {

    @GET("drone/{drone_id}/state")
    suspend fun getDroneState(
        @Path("drone_id") droneId: String
    ): ApiResponse<DroneStateResponse>

    @GET("drone/{drone_id}/telemetry/latest")
    suspend fun getLatestTelemetry(
        @Path("drone_id") droneId: String
    ): ApiResponse<TelemetryResponse>

    @GET("drone/{drone_id}/telemetry/history")
    suspend fun getTelemetryHistory(
        @Path("drone_id") droneId: String,
        @Query("start_time") startTime: Long,
        @Query("end_time") endTime: Long,
        @Query("limit") limit: Int = 100
    ): ApiResponse<List<TelemetryResponse>>

    @POST("drone/{drone_id}/command")
    suspend fun sendCommand(
        @Path("drone_id") droneId: String,
        @Body command: DroneCommandRequest
    ): ApiResponse<DroneCommandResponse>

    @GET("drone/{drone_id}/battery")
    suspend fun getBatteryLevel(
        @Path("drone_id") droneId: String
    ): ApiResponse<Map<String, Int>>

    @POST("drone/{drone_id}/calibrate")
    suspend fun calibrate(
        @Path("drone_id") droneId: String
    ): ApiResponse<String>
}

/**
 * Retrofit service interface for AI analysis endpoints.
 */
interface AIApiService {

    @POST("analysis/telemetry")
    suspend fun analyzeData(
        @Body request: AIAnalysisRequest
    ): AIAnalysisResponse

    @POST("analysis/batch")
    suspend fun batchAnalyze(
        @Body requests: List<AIAnalysisRequest>
    ): List<AIAnalysisResponse>

    @POST("analysis/recommendations")
    suspend fun getRecommendations(
        @Body request: AIAnalysisRequest
    ): ApiResponse<List<String>>

    @POST("analysis/train")
    suspend fun trainModel(
        @Body trainingData: Map<String, Any>
    ): ApiResponse<String>
}

/**
 * Factory for creating Retrofit API clients.
 */
object ApiClient {

    private var droneApiService: DroneApiService? = null
    private var aiApiService: AIApiService? = null

    fun getDroneApiService(): DroneApiService {
        return droneApiService ?: createDroneApiService().also {
            droneApiService = it
        }
    }

    fun getAIApiService(): AIApiService {
        return aiApiService ?: createAIApiService().also {
            aiApiService = it
        }
    }

    private fun createDroneApiService(): DroneApiService {
        val okHttpClient = createOkHttpClient()
        val json = Json { ignoreUnknownKeys = true }

        val retrofit = Retrofit.Builder()
            .baseUrl(BuildConfig.API_BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(json.asConverterFactory(
                "application/json".toMediaType()
            ))
            .build()

        return retrofit.create(DroneApiService::class.java)
    }

    private fun createAIApiService(): AIApiService {
        val okHttpClient = createOkHttpClient()
        val json = Json { ignoreUnknownKeys = true }

        val retrofit = Retrofit.Builder()
            .baseUrl(BuildConfig.AI_API_BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(json.asConverterFactory(
                "application/json".toMediaType()
            ))
            .build()

        return retrofit.create(AIApiService::class.java)
    }

    private fun createOkHttpClient(): OkHttpClient {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = if (BuildConfig.DEBUG_MODE) {
                HttpLoggingInterceptor.Level.BODY
            } else {
                HttpLoggingInterceptor.Level.BASIC
            }
        }

        val authInterceptor = object : Interceptor {
            override fun intercept(chain: Interceptor.Chain): Response {
                val originalRequest = chain.request()

                val requestWithAuth = originalRequest.newBuilder()
                    .addHeader("Authorization", "Bearer ${BuildConfig.API_KEY}")
                    .addHeader("Content-Type", "application/json")
                    .addHeader("User-Agent", "DroneX-Pro/1.0.0")
                    .build()

                return chain.proceed(requestWithAuth)
            }
        }

        return OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .addInterceptor(authInterceptor)
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .writeTimeout(15, TimeUnit.SECONDS)
            .retryOnConnectionFailure(true)
            .build()
    }
}