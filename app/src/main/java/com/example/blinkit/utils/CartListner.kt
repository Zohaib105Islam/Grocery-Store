package com.example.blinkit.utils

interface CartListner {
    fun showCartLayout(itemCount: Int){}

    fun savingCartItemCount(itemCount: Int){}

    fun hideCartLayout(){}
}