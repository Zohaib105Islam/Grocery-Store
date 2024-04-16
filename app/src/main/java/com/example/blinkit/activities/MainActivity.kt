package com.example.blinkit.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import com.example.blinkit.adapters.AdapterCartProducts
import com.example.blinkit.databinding.ActivityMainBinding
import com.example.blinkit.databinding.BsCartProductsBinding
import com.example.blinkit.databinding.ItemViewCartProductsBinding
import com.example.blinkit.databinding.ItemViewProductBinding
import com.example.blinkit.models.Product
import com.example.blinkit.roomdb.CartProducts
import com.example.blinkit.utils.CartListner
import com.example.blinkit.utils.Utils
import com.example.blinkit.viewmodels.UserViewModel
import com.google.android.material.bottomsheet.BottomSheetDialog
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity(), CartListner {

    private val binding: ActivityMainBinding by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }

    val viewModel: UserViewModel by viewModels()

    private lateinit var bsCartProductsBinding: BsCartProductsBinding

    private lateinit var bs: BottomSheetDialog

    private lateinit var cartProductList: List<CartProducts>
    private lateinit var adapterCartProducts: AdapterCartProducts

    private var cartListner: CartListner? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        bsCartProductsBinding = BsCartProductsBinding.inflate(layoutInflater)

        onCartClicked()

        getAllCartProducts()

        onNextButtonClicked()
        onBottomSheetNextButtonClicked()

        getTotalItemCountInCart()
//================================================================


//=================================================================

        binding.crashBtn.setOnClickListener {
            throw RuntimeException("Test Crash") // Force a crash
        }


    }

    //=========================================================================================
    private fun getAllCartProducts() {
        viewModel.getAllCartProducts().observe(this) {
            if (!it.isEmpty()) {
                cartProductList = it
                binding.allCart.visibility = View.VISIBLE
                // Now that cartProductList is available, set the adapter for the BottomSheetDialog
                setBottomSheetAdapter(cartProductList)
            }

        }
    }

    private fun setBottomSheetAdapter(cartList: List<CartProducts>) {
        adapterCartProducts = AdapterCartProducts(
            ::onIncrementButtonClicked,
            ::onDecrementButtonClicked,
        )
        bsCartProductsBinding.rvProductsItems.adapter = adapterCartProducts
        adapterCartProducts.differ.submitList(cartList)
    }

    private fun onCartClicked() {
        binding.allItemCart.setOnClickListener {

            bs = BottomSheetDialog(this)

            // Check if the view already has a parent, and if so, remove it
            val parent = bsCartProductsBinding.root.parent as? ViewGroup
            parent?.removeView(bsCartProductsBinding.root)

            bs.setContentView(bsCartProductsBinding.root)
            bsCartProductsBinding.tvNumberOfProductCount.text = binding.tvNumberOfProductCount.text

            // Now that cartProductList is available, set the adapter for the BottomSheetDialog
            setBottomSheetAdapter(cartProductList)

            bs.show()
            Log.d("CartProducts", cartProductList.toString())


        }
    }

    private fun getTotalItemCountInCart() {
        viewModel.fetchTotalCartItemCount().observe(this) {
            if (it > 0) {
                binding.allCart.visibility = View.VISIBLE
                binding.tvNumberOfProductCount.text = it.toString()
                bsCartProductsBinding.tvNumberOfProductCount.text = it.toString()
            } else {
                binding.allCart.visibility = View.GONE

            }

        }
    }


//===========================================================================================




    private fun onBottomSheetNextButtonClicked() {
        bsCartProductsBinding.btnNext.setOnClickListener {
            startActivity(Intent(this, OrderPlaceActivity::class.java))
        }
    }

    private fun onNextButtonClicked() {
        binding.btnNext.setOnClickListener {
            startActivity(Intent(this, OrderPlaceActivity::class.java))
        }
    }







    override fun showCartLayout(itemCount: Int) {
        val previousCount = binding.tvNumberOfProductCount.text.toString().toInt()
        val updatedCount = previousCount + itemCount

        if (updatedCount > 0) {
            binding.allCart.visibility = View.VISIBLE
            binding.tvNumberOfProductCount.text = updatedCount.toString()
        } else {
            binding.tvNumberOfProductCount.text = "0"
            binding.allCart.visibility = View.GONE
        }
    }

    override fun savingCartItemCount(itemCount: Int) {
        viewModel.fetchTotalCartItemCount().observe(this) {
            viewModel.savingCartItemCount(it + itemCount)
        }

    }

    override fun hideCartLayout() {
        binding.allCart.visibility = View.GONE
        binding.tvNumberOfProductCount.text = "0"
    }


    fun onIncrementButtonClicked(
        product: CartProducts,
        productBinding: ItemViewCartProductsBinding
    ) {
        var itemCountInc = productBinding.tvProductCount.text.toString().toInt()
        itemCountInc++


        if (product.productStock!! + 1 > itemCountInc) {
            productBinding.tvProductCount.text = itemCountInc.toString()

            cartListner?.showCartLayout(1)

            // step 2
            product.productCount = itemCountInc
            lifecycleScope.launch {
                cartListner?.savingCartItemCount(1)
                saveProductInRoomDb(product)
                // viewModel.updateItemCount(product,itemCountInc)
            }
        } else {
            Utils.showToast(this, "No more stock available...")
        }
    }

    fun onDecrementButtonClicked(
        product: CartProducts,
        productBinding: ItemViewCartProductsBinding
    ) {
        var itemCountDec = productBinding.tvProductCount.text.toString().toInt()
        itemCountDec--


        // step 2
        product.productCount = itemCountDec
        lifecycleScope.launch {
            cartListner?.savingCartItemCount(-1)
            saveProductInRoomDb(product)
            //  viewModel.updateItemCount(product,itemCountDec)
        }

        if (itemCountDec > 0) {
            productBinding.tvProductCount.text = itemCountDec.toString()
        } else {
            lifecycleScope.launch { viewModel.deleteCartProduct(product.productRandomId!!) }

            viewModel.fetchTotalCartItemCount().observe(this) {
                if (it > 0) {
                    binding.allCart.visibility = View.VISIBLE
                    binding.tvNumberOfProductCount.text = it.toString()
                    bsCartProductsBinding.tvNumberOfProductCount.text = it.toString()
                } else {
                    binding.allCart.visibility = View.GONE
                    //lifecycleScope.launch { viewModel.deleteCartProduct(product.productRandomId!!) }
                    bs.dismiss()

                }

            }
//            productBinding.allProductCount.visibility=View.VISIBLE
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
            productPrice = product.productPrice.toString(),
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
//    override fun onAttach(context: Context) {
//        super.onAttach(context)
//
//        if (context is CartListner){
//            cartListner=context
//        }
//        else{
//            throw ClassCastException("Please implement cart listener")
//        }


