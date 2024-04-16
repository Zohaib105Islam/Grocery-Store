package com.example.blinkit.activities

import android.app.AlertDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import com.example.blinkit.adapters.AdapterCartProducts
import com.example.blinkit.databinding.ActivityOrderPlaceBinding
import com.example.blinkit.databinding.AddressLayoutBinding
import com.example.blinkit.databinding.ItemViewCartProductsBinding
import com.example.blinkit.models.Orders
import com.example.blinkit.roomdb.CartProducts
import com.example.blinkit.utils.CartListner
import com.example.blinkit.utils.Constants
import com.example.blinkit.utils.Utils
import com.example.blinkit.viewmodels.UserViewModel
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
//===================1=================================
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
//==================1 end=================================
//==================2 ===============================================
    private fun onPlaceOrderClicked() {
        binding.btnNext.setOnClickListener{
            com.example.blinkit.utils.Utils.showDialog(this,"Processing")
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

    }

val phonePayView = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){

      if (it.resultCode == RESULT_OK){
          checkStatus()
      }

    else{
        com.example.blinkit.utils.Utils.showToast(this,"Network error ")
          Utils.hideDialog()
          /// //==================== here API not working that why condition is false and we can done same work in if and else condition
         // checkStatus()
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
                    com.example.blinkit.utils.Utils.showToast(this@OrderPlaceActivity,"Payment done Successfully")

                    // save order in database , delete product
                    saveOrder()
                    deleteCartProducts()
                    com.example.blinkit.utils.Utils.hideDialog()

                    startActivity(Intent(this@OrderPlaceActivity,MainActivity::class.java))
                    finish()
                }
                else{
//==================== here API not working that why condition is false and we can done same work in if and else condition
                    saveOrder()
                    deleteCartProducts()
                    com.example.blinkit.utils.Utils.hideDialog()
                    startActivity(Intent(this@OrderPlaceActivity,MainActivity::class.java))
                    finish()
//=====================================================================
                   // com.example.blinkit.utils.Utils.hideDialog()
                    com.example.blinkit.utils.Utils.showToast(this@OrderPlaceActivity,"Payment ! done")
                }
            }
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


                    viewModel.sendNotification(order,"Ordered","Some products has been ordered...",adminUid)
                    //com.example.blinkit.utils.Utils.showToast(this,"Noti sent")

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

    private fun deleteCartProducts() {
        lifecycleScope.launch {
            viewModel.deleteCartProducts()
            viewModel.savingCartItemCount(0)
            cartListner?.hideCartLayout()
        }

    }

//===========2 end===========================================






    private fun backButtonClick() {
        binding.tbCheckout.setNavigationOnClickListener {
            finish()
        }
    }

    private fun getAllCartProducts() {
        viewModel.getAllCartProducts().observe(this){cartProductList->

            adapterCartProducts= AdapterCartProducts(
                ::onIncrementButtonClicked,
                ::onDecrementButtonClicked
            )
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

    fun onIncrementButtonClicked(product: CartProducts, productBinding: ItemViewCartProductsBinding){
        var itemCountInc= productBinding.tvProductCount.text.toString().toInt()
        itemCountInc++

        if (product.productStock!! + 1 > itemCountInc){
            productBinding.tvProductCount.text = itemCountInc.toString()

            cartListner?.showCartLayout(1)

            // step 2
            product.productCount=itemCountInc
            lifecycleScope.launch {
                cartListner?.savingCartItemCount(1)
                saveProductInRoomDb(product)
                // viewModel.updateItemCount(product,itemCountInc)
            }
        }
        else{
            Utils.showToast(this,"No more stock available...")
        }
    }

    fun onDecrementButtonClicked(product: CartProducts, productBinding: ItemViewCartProductsBinding){
        var itemCountDec= productBinding.tvProductCount.text.toString().toInt()
        itemCountDec--

        // step 2
        product.productCount=itemCountDec
        lifecycleScope.launch {
            cartListner?.savingCartItemCount(-1)
            saveProductInRoomDb(product)
            //  viewModel.updateItemCount(product,itemCountDec)
        }

        if (itemCountDec > 0){
            productBinding.tvProductCount.text = itemCountDec.toString()
        }
        else{
            lifecycleScope.launch { viewModel.deleteCartProduct(product.productRandomId!!) }
            // productBinding.tvAddBtn.visibility= View.VISIBLE
            productBinding.allProductCount.visibility= View.GONE
            productBinding.tvProductCount.text = "0"
        }
        cartListner?.showCartLayout(-1)




    }

    fun saveProductInRoomDb(product: CartProducts) {

        val cartProduct = CartProducts(
            itemPushKey = product.itemPushKey!!,
            productRandomId = product.productRandomId!!,
            productTitle = product.productTitle,
            productQuantity = product.productQuantity,
            // productPrice = "Rs" + "${product.productPrice}",
            productPrice = product.productPrice,
            productCount = product.productCount,
            productStock = product.productStock,
            productImage = product.productImage,
            productCategory = product.productCategory,
            adminUid = product.adminUid,
            productType = product.productType,
        )
        lifecycleScope.launch { viewModel.insertCartProduct(cartProduct) }

    }
}