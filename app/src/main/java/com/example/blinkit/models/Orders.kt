package com.example.blinkit.models

import com.example.blinkit.roomdb.CartProducts
import java.util.Calendar

data class Orders(
    val orderId: String? = null,
    val orderList: List<CartProducts>? = null,
    val userAddress: String? = null,
    val orderStatus: Int? = 0,
    val orderDate: String? = null,
    val orderingUserUid: String? = null,
    var adminUid: String? = null,
)
