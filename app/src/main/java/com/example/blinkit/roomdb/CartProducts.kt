package com.example.blinkit.roomdb

import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity(tableName = "CartProducts")
data class CartProducts(

    @PrimaryKey
    var itemPushKey: String="random",

    var productRandomId: String? =null,

    var productTitle: String? = null,
    var productQuantity: String? = null,
    var productPrice: String? = null,
    var productCount: Int? = null,
    var productStock: Int? = null,
    var productCategory: String? = null,

    var adminUid: String? = null,
    var productImage: String ? = null,

    var productType: String?=null,
) {
}