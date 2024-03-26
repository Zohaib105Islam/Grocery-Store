package com.example.blinkit.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import androidx.activity.viewModels
import androidx.lifecycle.MutableLiveData
import com.example.blinkit.adapters.AdapterCartProducts
import com.example.blinkit.databinding.ActivityMainBinding
import com.example.blinkit.databinding.BsCartProductsBinding
import com.example.blinkit.roomdb.CartProducts
import com.example.blinkit.utils.CartListner
import com.example.blinkit.viewmodels.UserViewModel
import com.google.android.material.bottomsheet.BottomSheetDialog

class MainActivity : AppCompatActivity() , CartListner{

    private val binding: ActivityMainBinding by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }

    val viewModel : UserViewModel by viewModels()

    private lateinit var bsCartProductsBinding : BsCartProductsBinding

    private lateinit var cartProductList: List<CartProducts>
    private lateinit var adapterCartProducts: AdapterCartProducts

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        bsCartProductsBinding = BsCartProductsBinding.inflate(layoutInflater)

        onCartClicked()

        getAllCartProducts()

        onNextButtonClicked()
        onBottomSheetNextButtonClicked()

        getTotalItemCountInCart()
    }

    private fun onBottomSheetNextButtonClicked() {
        bsCartProductsBinding.btnNext.setOnClickListener{
            startActivity(Intent(this,OrderPlaceActivity::class.java))
        }
    }

    private fun onNextButtonClicked() {
        binding.btnNext.setOnClickListener{
            startActivity(Intent(this,OrderPlaceActivity::class.java))
        }
    }

    private fun getAllCartProducts(){
        viewModel.getAllCartProducts().observe(this){
            if (!it.isEmpty()){
                cartProductList=it
                binding.allCart.visibility=View.VISIBLE
                // Now that cartProductList is available, set the adapter for the BottomSheetDialog
                setBottomSheetAdapter(cartProductList)
            }

    }
    }

    private fun onCartClicked() {
        binding.allItemCart.setOnClickListener{

            val bs=BottomSheetDialog(this)

            // Check if the view already has a parent, and if so, remove it
            val parent = bsCartProductsBinding.root.parent as? ViewGroup
            parent?.removeView(bsCartProductsBinding.root)

            bs.setContentView(bsCartProductsBinding.root)
            bsCartProductsBinding.tvNumberOfProductCount.text=binding.tvNumberOfProductCount.text

            // Now that cartProductList is available, set the adapter for the BottomSheetDialog
            setBottomSheetAdapter(cartProductList)

            bs.show()
            Log.d("CartProducts", cartProductList.toString())


        }
    }

    private fun setBottomSheetAdapter(cartList: List<CartProducts>) {
        adapterCartProducts = AdapterCartProducts()
        bsCartProductsBinding.rvProductsItems.adapter = adapterCartProducts
        adapterCartProducts.differ.submitList(cartList)
    }

    private fun getTotalItemCountInCart() {
        viewModel.fetchTotalCartItemCount().observe(this){
            if (it > 0){
                binding.allCart.visibility=View.VISIBLE
                binding.tvNumberOfProductCount.text=it.toString()
            }
            else{
                binding.allCart.visibility=View.GONE
            }
        }
        }

    override fun showCartLayout(itemCount: Int){
        val previousCount = binding.tvNumberOfProductCount.text.toString().toInt()
        val updatedCount = previousCount + itemCount

        if(updatedCount > 0){
            binding.allCart.visibility = View.VISIBLE
            binding.tvNumberOfProductCount.text=updatedCount.toString()
        }
        else{
            binding.allCart.visibility = View.GONE
            binding.tvNumberOfProductCount.text="0"
        }
    }

    override fun savingCartItemCount(itemCount: Int) {
        viewModel.fetchTotalCartItemCount().observe(this){
            viewModel.savingCartItemCount(it + itemCount)
        }

    }

    override fun hideCartLayout() {
        binding.allCart.visibility = View.GONE
        binding.tvNumberOfProductCount.text="0"
    }



}