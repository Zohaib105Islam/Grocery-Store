package com.example.blinkit.roomdb

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update

@Dao
interface CartProductsDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCartProduct(products: CartProducts)
    @Update
    fun updateCartProduct(products: CartProducts)

    @Query("SELECT productCount FROM CartProducts")
    fun getProductCount(): LiveData<List<Int>>

    @Query("SELECT * FROM CartProducts")
    fun getAllCartProducts(): LiveData<List<CartProducts>>

//    @Query("SELECT FROM CartProducts WHERE productCount= :productCount")
//    fun getOneProductCount(productCount :Int)
@Query("SELECT productCount FROM CartProducts WHERE productRandomId = :productId")
fun getProductCountByRandomId(productId: String): Int?


    @Query("DELETE FROM CartProducts WHERE productRandomId= :productId")
    suspend fun deleteCartProduct(productId :String)

    @Query("DELETE FROM CartProducts")
    suspend fun deleteCartPoducts()
}