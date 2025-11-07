package com.veilguard.vpn.api

import com.veilguard.vpn.data.model.*
import retrofit2.Response
import retrofit2.http.*

interface ApiService {
    @FormUrlEncoded
    @POST("token")
    suspend fun login(
        @Field("username") username: String,
        @Field("password") password: String
    ): Response<AuthResponse>
    
    @POST("register")
    suspend fun register(@Body request: RegisterRequest): Response<User>
    
    @GET("api/v1/vpn/servers")
    suspend fun getServers(): Response<List<Server>>
    
    @GET("api/v1/trials/check/{device_id}")
    suspend fun checkTrialEligibility(
        @Path("device_id") deviceId: String
    ): Response<TrialEligibility>
    
    @POST("api/v1/trials/start")
    suspend fun startTrial(@Body request: TrialRequest): Response<Trial>
    
    @GET("api/v1/subscriptions/plans")
    suspend fun getSubscriptionPlans(): Response<List<SubscriptionPlan>>
    
    @POST("api/v1/subscriptions")
    suspend fun createSubscription(
        @Header("Authorization") token: String,
        @Body request: Map<String, String>
    ): Response<Subscription>
    
    @GET("api/v1/users/me")
    suspend fun getCurrentUser(
        @Header("Authorization") token: String
    ): Response<User>
}
