package com.example.blinkit.models


data class Product(
    var productRandomId : String? =null,
    var itemPushKey: String? = null,
    var productTitle : String? = null,
    var productQuantity : Int? = null,
    var productUnit : String? = null,
    var productPrice : Int? = null,
    var productStock : Int? = null,
    var productCategory : String? = null,
    var productType : String? = null,
    var itemCount : Int? = null,
    var adminUid : String? = null,
    var productImageUris : ArrayList<String?> ? = null,
//    var timestamp: Long? = null,
)
