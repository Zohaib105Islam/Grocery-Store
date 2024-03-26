package com.example.blinkit.api

import com.example.blinkit.models.CheckStatus
import com.example.blinkit.models.Notification
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.HeaderMap
import retrofit2.http.Headers
import retrofit2.http.POST
import retrofit2.http.Path

interface ApiInterface {

    @GET("apis/pg/v1/status/{merchantId}/{transactionId}")
    suspend fun checkStatus(
        @HeaderMap headers : Map<String,String> ,
        @Path("merchantId") merchantId: String,
        @Path("transactionId") transactionId:String,
    ):Response<CheckStatus>


    @Headers(
        "Content-Type: application/json",
    "Authorization: key=AAAA5i75LoI:APA91bHElvHPUcNtN2usuLICBfZpWJfgbf_0xKjfQZVXl0NS2vf5GiegeKeUvjJswm8ELaAwRNSOf1MaozQB12VAbpS0hQNT84t9tnIFcrTISELE_DOXi8o3kuKeXesImVghM0kM7MJH"
    )

    @POST("fcm/send")
    fun sendNotification(@Body notification: Notification) : Call<Notification>

}