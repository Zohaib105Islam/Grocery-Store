package com.example.blinkit.models

import com.example.blinkit.utils.Utils

data class Users(
    var uid : String? =null,
    val userName : String?=null,
    val userPhoneNumber : String? =null,
    val userEmail : String?=null,
    val userAddress : String? =null,
    val userPassword : String?=null,
    var userToken : String?=null,
)
