package com.example.blinkit.activities

import android.app.AlertDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.view.LayoutInflater
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import com.example.blinkit.adapters.AdapterCartProducts
import com.example.blinkit.adapters.AdapterProduct
import com.example.blinkit.databinding.ActivityOrderPlaceBinding
import com.example.blinkit.databinding.AddressLayoutBinding
import com.example.blinkit.models.Orders
import com.example.blinkit.models.Users
import com.example.blinkit.roomdb.CartProducts
import com.example.blinkit.utils.CartListner
import com.example.blinkit.utils.Constants
import com.example.blinkit.viewmodels.UserViewModel
import com.google.firebase.installations.Utils
import com.phonepe.intent.sdk.api.B2BPGRequest
import com.phonepe.intent.sdk.api.B2BPGRequestBuilder
import com.phonepe.intent.sdk.api.PhonePe
import com.phonepe.intent.sdk.api.PhonePeInitException
import com.phonepe.intent.sdk.api.models.PhonePeEnvironment
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.nio.charset.Charset
import java.security.MessageDigest

class OrderPlaceActivity : AppCompatActivity() {

    private val binding: ActivityOrderPlaceBinding by lazy {
        ActivityOrderPlaceBinding.inflate(layoutInflater)
    }

    private val viewModel : UserViewModel by viewModels()
    private lateinit var adapterCartProducts: AdapterCartProducts

    private lateinit var b2BPGRequest : B2BPGRequest

    private var cartListner: CartListner?=null

    private lateinit var adminUid: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        backButtonClick()
        onPlaceOrderClicked()
        initializePhonePay()

        getAllCartProducts()
    }

    private fun initializePhonePay() {
        val data=JSONObject()
        PhonePe.init(this,PhonePeEnvironment.UAT,Constants.MERCHANT_ID,"")

        data.put("merchantId",Constants.MERCHANT_ID)
        data.put("merchantTransactionId",Constants.merchantTransactionId)
        data.put("amount",200) // Long,Mandatory
        data.put("mobileNumber","8839990051") // string , optional
        data.put("callbackUrl","https://webhook.site/callback-ur") // string

        val paymentInstrument=JSONObject()
        paymentInstrument.put("type","UPI_INTENT")
        paymentInstrument.put("targetApp","com.phonepe.simulator")

        data.put("paymentInstrument",paymentInstrument)

        val deviceContext=JSONObject()
        deviceContext.put("deviceOS","ANDROID")
        data.put("deviceContext",deviceContext)

        val payloadBase64= Base64.encodeToString(
            data.toString().toByteArray(Charset.defaultCharset()),Base64.NO_WRAP
        )

        val checksum=sha256(payloadBase64 + Constants.apiEndPoint + Constants.SALT_KEY) + "###1";

        b2BPGRequest=B2BPGRequestBuilder()
            .setData(payloadBase64)
            .setChecksum(checksum)
            .setUrl(Constants.apiEndPoint)
            .build()

    }
    private fun sha256(input :String):String{
        val bytes=input.toByteArray(Charsets.UTF_8)
        val md= MessageDigest.getInstance("SHA-256")
        val digest=md.digest(bytes)
        return digest.fold("") { str, it-> str + "%02x".format(it)}
    }

    private fun onPlaceOrderClicked() {
        binding.btnNext.setOnClickListener{
            viewModel.getAddressStatus().observe(this){status->
                if (status){
                    // payment work
                    getPaymentView()

                }
                else{
                    val addressLayoutBinding =AddressLayoutBinding.inflate(LayoutInflater.from(this))

                    val alertDialog=AlertDialog.Builder(this)
                        .setView(addressLayoutBinding.root).create()
                    alertDialog.show()

                    addressLayoutBinding.btnAdd.setOnClickListener{
                        saveAddress(alertDialog,addressLayoutBinding)
                    }
                }

            }
        }
    }
val phonePayView = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){

      if (it.resultCode == RESULT_OK){
          checkStatus()
      }

    else{
        com.example.blinkit.utils.Utils.showToast(this,"Result not Ok ")
          /// //==================== here API not working that why condition is false and we can done same work in if and else condition
          checkStatus()
          ////=====================================================================

    }
}

    private fun checkStatus() {
        val xVerify=sha256("/pg/v1/status/${Constants.MERCHANT_ID}/${Constants.merchantTransactionId}${Constants.SALT_KEY}") + "###1"
        val headers = mapOf(
            "Content-Type" to "application/json",
            "X-VERIFY" to xVerify,
            "X-MERCHANT-ID" to Constants.MERCHANT_ID
        )

        lifecycleScope.launch {
            viewModel.checkPayment(headers)
            viewModel.paymentStatus.collect{status->
                if (status){
                    com.example.blinkit.utils.Utils.showToast(this@OrderPlaceActivity,"Payment done")

                    // save order in database , delete product
                    saveOrder()
                    deleteCartProducts()

                    startActivity(Intent(this@OrderPlaceActivity,MainActivity::class.java))
                    finish()
                }
                else{
//==================== here API not working that why condition is false and we can done same work in if and else condition
                    saveOrder()
                    deleteCartProducts()
                    startActivity(Intent(this@OrderPlaceActivity,MainActivity::class.java))
                    finish()
//=====================================================================
                    com.example.blinkit.utils.Utils.showToast(this@OrderPlaceActivity,"Payment not done")
                }
            }
        }

    }

    private fun deleteCartProducts() {
        lifecycleScope.launch {
            viewModel.deleteCartProducts()
            viewModel.savingCartItemCount(0)
            cartListner?.hideCartLayout()
        }

    }


    private fun saveOrder() {
        viewModel.getAllCartProducts().observe(this){cartProductsList ->

            if (!cartProductsList.isEmpty()){

                viewModel.getUserAddress { address ->
                    val order = Orders(
                        orderId =com.example.blinkit.utils.Utils.getRandomId(),
                        orderList = cartProductsList,
                        userAddress = address,
                        orderStatus = 0,
                        orderDate = com.example.blinkit.utils.Utils.getCurrentDate(),
                        orderingUserUid = com.example.blinkit.utils.Utils.currentUser(),

                    )
                    viewModel.saveOrderedProducts(order,adminUid)
                    com.example.blinkit.utils.Utils.showToast(this,"Save ordered products")

                    // Notification
                 //   lifecycleScope.launch {
//                    cartProductsList.getOrNull(0)?.adminUid?.let { adminUid ->
//                        viewModel.sendNotification(adminUid, "Ordered", "Some products have been ordered...")
//                    } ?: run {
//                        Log.e("OrderPlaceActivity", "Admin UID is null")
//                    }

                      viewModel.sendNotification(order,"Ordered","Some products has been ordered...",adminUid)
                    com.example.blinkit.utils.Utils.showToast(this,"Noti sent")

                    //   }

                }
                for(products in cartProductsList){
                    val count=products.productCount
                    val stock=products.productStock?.minus(count!!)
                    if (stock != null) {
                        viewModel.saveProductsAfterOrder(stock,products)
                    }
                }
            }

        }
    }

    private fun getPaymentView() {

        try {
            PhonePe.getImplicitIntent(this, b2BPGRequest, "com.phonepe.simulator")?.let {
                phonePayView.launch(it)
            } ?: run {
                com.example.blinkit.utils.Utils.showToast(this, "PhonePe Intent is null")
            }
        } catch (e: PhonePeInitException) {
            com.example.blinkit.utils.Utils.showToast(this, e.message.toString())
        }

//        try {
//            PhonePe.getImplicitIntent(this,b2BPGRequest,"com.phonepe.simulator")
//                .let {
//                    phonePayView.launch(it)
//                }
//        }
//        catch (e: PhonePeInitException){
//            com.example.blinkit.utils.Utils.showToast(this,e.message.toString())
//        }

    }

    private fun saveAddress(alertDialog: AlertDialog?, addressLayoutBinding: AddressLayoutBinding) {
        com.example.blinkit.utils.Utils.showDialog(this,"Processing...")

        val userPinCode=addressLayoutBinding.etPinCode.text.toString()
        val userPhoneNumber=addressLayoutBinding.etPhoneNumber.text.toString()
        val userState=addressLayoutBinding.etState.text.toString()
        val userDistrict=addressLayoutBinding.etDistrict.text.toString()
        val userAddress=addressLayoutBinding.etDescriptiveAddress.text.toString()

        val address = "$userPinCode, $userDistrict($userState), $userAddress, $userPhoneNumber"


        lifecycleScope.launch {
            viewModel.saveUserAddress(address)
            viewModel.saveAddressStatus()
        }
        com.example.blinkit.utils.Utils.showToast(this,"Saved...")
        alertDialog?.dismiss()
        com.example.blinkit.utils.Utils.hideDialog()

        // payment work
        getPaymentView()
    }

    private fun backButtonClick() {
        binding.tbCheckout.setNavigationOnClickListener {
            finish()
        }
    }

    private fun getAllCartProducts() {
        viewModel.getAllCartProducts().observe(this){cartProductList->

            adapterCartProducts= AdapterCartProducts()
            binding.rvCheckOutProducts.adapter=adapterCartProducts
            adapterCartProducts.differ.submitList(cartProductList)

            var totalPrice=0

            for (product in cartProductList){
                val price =product.productPrice?.substring(2)?.toInt()
                val itemCount = product.productCount!!
                totalPrice =totalPrice + ((price?.times(itemCount))!!)

                adminUid=product.adminUid!!

                Log.d("PriceDebug", "Price String: $price")
                Log.d("PriceDebug", "Item Count: $itemCount")
            }

            binding.tvSubTotal.text=totalPrice.toString()

            Log.d("PriceDebug", "totalPrice: $totalPrice")

            if (totalPrice < 200){
                binding.tvDeliveryCharge.text="Rs15"
                totalPrice+=15
            }
            binding.tvGrandTotal.text=totalPrice.toString()

        }
    }
}