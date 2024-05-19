package com.example.blinkit.viewmodels

import android.app.Application
import android.content.Context.MODE_PRIVATE
import android.content.SharedPreferences
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.map
import com.example.blinkit.api.ApiUtilities
import com.example.blinkit.models.Bestseller
import com.example.blinkit.models.Notification
import com.example.blinkit.models.NotificationData
import com.example.blinkit.models.Orders
import com.example.blinkit.models.Product
import com.example.blinkit.models.Users
import com.example.blinkit.roomdb.CartProducts
import com.example.blinkit.roomdb.CartProductsDao
import com.example.blinkit.roomdb.CartProductsDatabase
import com.example.blinkit.utils.Constants
import com.example.blinkit.utils.Utils
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.callbackFlow
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class UserViewModel(application: Application) : AndroidViewModel(application) {

    // product push key
    private val productRef = FirebaseDatabase.getInstance().getReference("Admins")
    private val newItemKey: String? = productRef.push().key

    // initialization
    val sharedPrefrences: SharedPreferences =
        application.getSharedPreferences("My_Pref", MODE_PRIVATE)
    val cartProductsDao: CartProductsDao =
        CartProductsDatabase.getDatabaseInstance(application).cartProductsDao()


    private val _paymentStatus = MutableStateFlow<Boolean>(false)
    val paymentStatus = _paymentStatus

    private val _userData = MutableLiveData<Users>()
    val userData: LiveData<Users> = _userData

    //Room DB
    suspend fun insertCartProduct(products: CartProducts) {
        cartProductsDao.insertCartProduct(products)
    }

    suspend fun updateCartProduct(products: CartProducts) {
        cartProductsDao.updateCartProduct(products)
    }

    fun getAllCartProducts(): LiveData<List<CartProducts>> {
        Log.d("CartData", "Cart Items ${cartProductsDao.getAllCartProducts()}")
        return cartProductsDao.getAllCartProducts()
    }

    suspend fun deleteCartProducts() {
        cartProductsDao.deleteCartPoducts()
    }

    suspend fun deleteCartProduct(productId: String) {
        cartProductsDao.deleteCartProduct(productId)
    }

    fun fetchAllProductsForCount(productId: String): LiveData<List<Pair<CartProducts, Int>>> {
        return cartProductsDao.getAllCartProducts().map { productList ->
            productList.map { product ->
                val count = cartProductsDao.getProductCountByRandomId(productId)
                Pair(product, count ?: 0)
                //Log.d("hhh","Count ${count}")
            }

        }
      //  Log.d("hhh","Count ${count!!}")
    }


    fun fetchTotalCartItemCount(): MutableLiveData<Int> {
        val totalItemCount = MutableLiveData<Int>()
        var productCount: LiveData<List<Int>> = cartProductsDao.getProductCount()

        productCount.observeForever { itemCountList ->
            // Check if the list is not empty and has at least one item
            if (!itemCountList.isNullOrEmpty()) {
                // Assuming you want the sum of all items in the list
                var sum = 0
                for (itemCount in itemCountList) {
                    sum += itemCount
                }
                totalItemCount.value = sum
            } else {
                // If the list is empty, set the total item count to 0
                totalItemCount.value = 0
            }
        }

        // totalItemCount.value=productCount.toString().toInt()
        // totalItemCount.value= sharedPrefrences.getInt("itemCount",0)
        return totalItemCount
    }


    // Firebase call
    fun fetchAllTheProducts(): Flow<List<Product>> = callbackFlow {
        Log.d("FirebaseData", "Fetch Called...")
        val db = FirebaseDatabase.getInstance().getReference("AllProductsDetails").child("AllProducts")

        val eventListner = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val products = ArrayList<Product>()
                for (product in snapshot.children) {
                    val prod = product.getValue(Product::class.java)
                    products.add(prod!!)
                    Log.d("FirebaseData", "Products Data: ${prod}")

                }
                trySend(products)
            }

            override fun onCancelled(error: DatabaseError) {
                Log.d("FirebaseData", "Fetch Called cancled ${error.message}")
            }

        }

        db.addValueEventListener(eventListner)

        awaitClose { db.removeEventListener(eventListner) }
    }

    fun getAllOrders(): Flow<List<Orders>> = callbackFlow {
        val db = FirebaseDatabase.getInstance().getReference("AllUsers").child(Utils.currentUser()!!).child("UserOrders")
            .orderByChild("OrderStatus")
        val eventListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val orderList = ArrayList<Orders>()
                for (orders in snapshot.children) {
                    val order = orders.getValue(Orders::class.java)
                    if (order?.orderingUserUid == Utils.currentUser()) {
                        orderList.add(order!!)
                    }
                }
                trySend(orderList)
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        }
        db.addValueEventListener(eventListener)
        awaitClose { db.removeEventListener(eventListener) }
    }

    fun orderedProducts(orderId: String): Flow<List<CartProducts>> = callbackFlow {
        val db =
            FirebaseDatabase.getInstance().getReference("AllUsers").child(Utils.currentUser()!!)
                .child("UserOrders").child(orderId)
        val eventListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val order = snapshot.getValue(Orders::class.java)
                trySend(order?.orderList!!)
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        }
        db.addValueEventListener(eventListener)
        awaitClose { db.removeEventListener(eventListener) }
    }

    fun getCategoryProduct(category: String): Flow<List<Product>> = callbackFlow {

        val db = FirebaseDatabase.getInstance().getReference("AllProductsDetails").child("ProductCategory/${category}")

        val products = ArrayList<Product>()
        val eventListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                Log.d("FirebaseData", "Data received: $snapshot")

                for (product in snapshot.children) {
                    val prod = product.getValue(Product::class.java)
                    prod?.let {
                        products.add(it)
                    }
                }
                Log.d("FirebaseData", "Products size: ${products.size}")
                Log.d("FirebaseData", "Snapshot value: $snapshot")
                trySend(products)
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle the error, log it, or take appropriate action
                Log.d("FirebaseError", "Error fetching data: ${error.message}")
                // Optionally, you can try sending an empty list to the flow to signal an error
                trySend(emptyList())
            }
        }

        db.addValueEventListener(eventListener)
        Log.d("FirebaseError", "Error fetching data: ${db}")

        awaitClose { db.removeEventListener(eventListener) }


    }

    fun updateItemCount(product: Product, itemCount: Int) {


        FirebaseDatabase.getInstance().getReference("Admins").child(product.adminUid!!).child("AllProducts")
            .child(product.productRandomId!!).child("itemCount").setValue(itemCount)

        FirebaseDatabase.getInstance().getReference("Admins").child(product.adminUid!!)
            .child("ProductCategory/${product.productCategory}").child(product.productRandomId!!)
            .child("itemCount").setValue(itemCount)

        FirebaseDatabase.getInstance().getReference("Admins").child(product.adminUid!!)
            .child("ProductType/${product.productType}").child(product.productRandomId!!)
            .child("itemCount").setValue(itemCount)


        FirebaseDatabase.getInstance().getReference("AllProductsDetails").child("AllProducts")
            .child(product.productRandomId!!).child("itemCount").setValue(itemCount)

        FirebaseDatabase.getInstance().getReference("AllProductsDetails")
            .child("ProductCategory/${product.productCategory}").child(product.productRandomId!!)
            .child("itemCount").setValue(itemCount)

        FirebaseDatabase.getInstance().getReference("AllProductsDetails")
            .child("ProductType/${product.productType}").child(product.productRandomId!!)
            .child("itemCount").setValue(itemCount)



    }

    fun saveProductsAfterOrder(stock: Int, product: CartProducts) {
//        productRef.child("AllProducts").child(product.productRandomId!!).child("itemCount").setValue(0)
//        productRef.child("ProductCategory /${product.productCategory}").child(product.productRandomId!!)
//            .child("itemCount").setValue(0)
//        productRef.child("ProductType /${product.productType}").child(product.productRandomId!!)
//            .child("itemCount").setValue(0)


        FirebaseDatabase.getInstance().getReference("Admins").child(product.adminUid!!).child("AllProducts")
            .child(product.productRandomId!!).child("itemCount").setValue(0)

        FirebaseDatabase.getInstance().getReference("Admins").child(product.adminUid!!)
            .child("ProductCategory/${product.productCategory}").child(product.productRandomId!!)
            .child("itemCount").setValue(0)

        FirebaseDatabase.getInstance().getReference("Admins").child(product.adminUid!!)
            .child("ProductType/${product.productType}").child(product.productRandomId!!)
            .child("itemCount").setValue(0)


        FirebaseDatabase.getInstance().getReference("AllProductsDetails").child("AllProducts")
            .child(product.productRandomId!!).child("itemCount").setValue(0)

        FirebaseDatabase.getInstance().getReference("AllProductsDetails")
            .child("ProductCategory/${product.productCategory}").child(product.productRandomId!!)
            .child("itemCount").setValue(0)

        FirebaseDatabase.getInstance().getReference("AllProductsDetails")
            .child("ProductType/${product.productType}").child(product.productRandomId!!)
            .child("itemCount").setValue(0)











//        productRef.child("AllProducts").child(product.productRandomId!!).child("productStock")
//            .setValue(stock)
//        productRef.child("ProductCategory /${product.productCategory}").child(product.productRandomId!!)
//            .child("productStock").setValue(stock)
//        productRef.child("ProductType /${product.productType}").child(product.productRandomId!!)
//            .child("productStock").setValue(stock)



        FirebaseDatabase.getInstance().getReference("Admins").child(product.adminUid!!).child("AllProducts")
            .child(product.productRandomId!!).child("productStock").setValue(stock)

        FirebaseDatabase.getInstance().getReference("Admins").child(product.adminUid!!)
            .child("ProductCategory/${product.productCategory}").child(product.productRandomId!!)
            .child("productStock").setValue(stock)

        FirebaseDatabase.getInstance().getReference("Admins").child(product.adminUid!!)
            .child("ProductType/${product.productType}").child(product.productRandomId!!)
            .child("productStock").setValue(stock)


        FirebaseDatabase.getInstance().getReference("AllProductsDetails").child("AllProducts")
            .child(product.productRandomId!!).child("productStock").setValue(stock)

        FirebaseDatabase.getInstance().getReference("AllProductsDetails")
            .child("ProductCategory/${product.productCategory}").child(product.productRandomId!!)
            .child("productStock").setValue(stock)

        FirebaseDatabase.getInstance().getReference("AllProductsDetails")
            .child("ProductType/${product.productType}").child(product.productRandomId!!)
            .child("productStock").setValue(stock)





    }

    fun saveUserAddress(address: String) {
        FirebaseDatabase.getInstance().getReference("AllUsers").child(Utils.currentUser()!!)
            .child("UserInfo").child("userAddress")
            .setValue(address)
    }


    fun logOutUser(){
        FirebaseAuth.getInstance().signOut()
    }

    fun getUserAddress(): Flow<List<Users>> = callbackFlow {
        val db = FirebaseDatabase.getInstance().getReference("AllUsers").child(Utils.currentUser()!!)
            .child("UserInfo")
            //.child("userAddress")

        val users = ArrayList<Users>()

        val eventListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
//                if (snapshot.exists()) {
//                    val address : Users? = snapshot.getValue(Users::class.java)
//                    callback(address)
//                } else {
//                    callback(null)
//                }
                for (user in snapshot.children) {
                    val user1 = user.getValue(Users::class.java)
                    user1?.let {
                        users.add(it)
                    }
                }
                trySend(users)
            }

            override fun onCancelled(error: DatabaseError) {
                trySend(emptyList())
            }

        }

        db.addValueEventListener(eventListener)

        awaitClose { db.removeEventListener(eventListener) }

    }

        fun fetchUserDataFromDatabase() {
            FirebaseDatabase.getInstance().getReference("AllUsers").child(Utils.currentUser()!!)
                .child("UserInfo")
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        // Convert DataSnapshot to Users object
                        val user = snapshot.getValue(Users::class.java)
                        // Update LiveData with the fetched user data
                        _userData.postValue(user!!)
                    }

                    override fun onCancelled(error: DatabaseError) {
                        // Handle error
                    }
                })
        }



    fun saveOrderedProducts(orders: Orders) {

        FirebaseDatabase.getInstance().getReference("Admins").child(orders.adminUid!!).child("AdminOrders")
            .child(orders.orderId!!).setValue(orders)
        FirebaseDatabase.getInstance().getReference("AllProductsDetails").child("AllOrders")
            .child(orders.orderId!!).setValue(orders)
        FirebaseDatabase.getInstance().getReference("AllUsers").child(orders.orderingUserUid!!).child("UserOrders")
            .child(orders.orderId!!).setValue(orders)
    }

    fun fetchProductType() : Flow<List<Bestseller>> = callbackFlow {
        val db = FirebaseDatabase.getInstance().getReference("AllProductsDetails").child("ProductType")


        Log.d("BBBB","ProductType : ${db}")

        val eventListener = object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                val  productTypeList = ArrayList<Bestseller>()

                for (productType in snapshot.children){

                    val productTypeName =productType.key
                    val productList=ArrayList<Product>()

                    for (products in productType.children){
                        val product=products.getValue(Product::class.java)
                        productList.add(product!!)
                        Log.d("BBBB","ProductTypeProduct : ${productList}")
                    }
                    val bestseller = Bestseller(productType =productTypeName , products =productList)
                    productTypeList.add(bestseller)
                }
                trySend(productTypeList)
                Log.d("BBBB","ProductTypeList : ${productTypeList}")
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        }
        db.addValueEventListener(eventListener)
        awaitClose { db.removeEventListener(eventListener) }
    }


    // shared prefrences
    fun savingCartItemCount(itemCount: Int) {
        sharedPrefrences.edit().putInt("itemCount", itemCount).apply()
    }

    fun saveAddressStatus() {
        sharedPrefrences.edit().putBoolean("addressStatus", true).apply()
    }

    fun getAddressStatus(): MutableLiveData<Boolean> {
        val status = MutableLiveData<Boolean>()
        status.value = sharedPrefrences.getBoolean("addressStatus", false)
        return status
    }


    // retrofit
    suspend fun checkPayment(headers: Map<String, String>) {
        val res = ApiUtilities.statusApi.checkStatus(
            headers,
            Constants.MERCHANT_ID,
            Constants.merchantTransactionId
        )
        _paymentStatus.value = res.body() != null && res.body()!!.success

        Log.d("ApiResponse", res.toString())

    }

     fun sendNotification(orders: Orders, title: String, message: String) {

        // Log.d("NotiApp", "adminUid : ${adminUid}")
        // orders.adminUid=adminUid

        val getToken =
            FirebaseDatabase.getInstance().getReference("Admins").child(orders.adminUid!!).child("AdminInfo")
                .child("adminToken").get()
        getToken.addOnCompleteListener { task ->
            val token = task.result.getValue(String::class.java)
           // val token = "cx_yaL35QhyG-fd8LKQ9es:APA91bHHM1LzgU3qBBIJ35szVOTTr9mHql2lRCqOuOAl8sPjXp5IruHxUWSd5mSF6MXDODR7sIZSuHIgBAIHZlUSmQX-zj4NfhtrquW000Q7cQXagO7uukcMXyfIEEXZzlVU7s6m4xxI"
            Log.d("NotiApp1", "Token : ${token}")
            val notification = Notification(token, NotificationData(title, message))

            Log.d("NotiApp2", "Send Notification: ${notification}")

            ApiUtilities.notificationApi.sendNotification(notification)
                .enqueue(object : Callback<Notification> {
                    override fun onResponse(
                        call: Call<Notification>,
                        response: Response<Notification>
                    ) {
                        if (response.isSuccessful) {
                            Log.d("NotiApp3", "Send Notification")
//                            Log.d("NotiApp", "Send Notification: ${token}")
//                            Log.d("NotiApp", "Send Notification: ${notification}")
                        }
                    }

                    override fun onFailure(call: Call<Notification>, t: Throwable) {
                        Log.d("NotiApp4", "Send not  Notification , error")
                    }

                })

        }


    }


}