package com.example.blinkit.user_fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.blinkit.R
import com.example.blinkit.adapters.AdapterOrderDeatils
import com.example.blinkit.databinding.FragmentOrderDetailsBinding
import com.example.blinkit.databinding.ItemViewCartProductsBinding
import com.example.blinkit.roomdb.CartProducts
import com.example.blinkit.utils.CartListner
import com.example.blinkit.utils.Utils
import com.example.blinkit.viewmodels.UserViewModel
import kotlinx.coroutines.launch


class OrderDetailsFragment : Fragment() {

    private lateinit var binding: FragmentOrderDetailsBinding

    private val viewModel: UserViewModel by viewModels()

    private lateinit var adapterOrderDetails: AdapterOrderDeatils

    private var cartListner : CartListner? = null

    private var status=0
    private var orderId=""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding= FragmentOrderDetailsBinding.inflate(inflater,container,false)

        onBackButtonClicked()

        getValues()

        settingStatus()

        getOrderedProducts()

        return binding.root
    }

    private fun getOrderedProducts() {
        lifecycleScope.launch {
            viewModel.orderedProducts(orderId).collect{cartList->
                adapterOrderDetails=AdapterOrderDeatils()
                binding.rvProductItems.adapter=adapterOrderDetails
                adapterOrderDetails.differ.submitList(cartList)
            }
        }
    }

    private fun settingStatus() {

        when (status) {
            0 -> {
                binding.iv1.backgroundTintList =
                    ContextCompat.getColorStateList(requireContext(), R.color.blue)
                binding.tv1.setTextColor(ContextCompat.getColor(requireContext(),R.color.blue))
            }

            1 -> {
                binding.iv1.backgroundTintList =
                    ContextCompat.getColorStateList(requireContext(), R.color.blue)
                binding.iv2.backgroundTintList =
                    ContextCompat.getColorStateList(requireContext(), R.color.blue)
                binding.view1.backgroundTintList =
                    ContextCompat.getColorStateList(requireContext(), R.color.blue)

                binding.tv1.setTextColor(ContextCompat.getColor(requireContext(),R.color.blue))
                binding.tv2.setTextColor(ContextCompat.getColor(requireContext(),R.color.blue))
            }

            2 -> {
                binding.iv1.backgroundTintList =
                    ContextCompat.getColorStateList(requireContext(), R.color.blue)
                binding.iv2.backgroundTintList =
                    ContextCompat.getColorStateList(requireContext(), R.color.blue)
                binding.view1.backgroundTintList =
                    ContextCompat.getColorStateList(requireContext(), R.color.blue)
                binding.iv3.backgroundTintList =
                    ContextCompat.getColorStateList(requireContext(), R.color.blue)
                binding.view2.backgroundTintList =
                    ContextCompat.getColorStateList(requireContext(), R.color.blue)

                binding.tv1.setTextColor(ContextCompat.getColor(requireContext(),R.color.blue))
                binding.tv2.setTextColor(ContextCompat.getColor(requireContext(),R.color.blue))
                binding.tv3.setTextColor(ContextCompat.getColor(requireContext(),R.color.blue))
            }

            3 -> {
                binding.iv1.backgroundTintList =
                    ContextCompat.getColorStateList(requireContext(), R.color.blue)
                binding.iv2.backgroundTintList =
                    ContextCompat.getColorStateList(requireContext(), R.color.blue)
                binding.view1.backgroundTintList =
                    ContextCompat.getColorStateList(requireContext(), R.color.blue)
                binding.iv3.backgroundTintList =
                    ContextCompat.getColorStateList(requireContext(), R.color.blue)
                binding.view2.backgroundTintList =
                    ContextCompat.getColorStateList(requireContext(), R.color.blue)
                binding.view3.backgroundTintList =
                    ContextCompat.getColorStateList(requireContext(), R.color.blue)
                binding.iv4.backgroundTintList =
                    ContextCompat.getColorStateList(requireContext(), R.color.blue)

                binding.tv1.setTextColor(ContextCompat.getColor(requireContext(),R.color.blue))
                binding.tv2.setTextColor(ContextCompat.getColor(requireContext(),R.color.blue))
                binding.tv3.setTextColor(ContextCompat.getColor(requireContext(),R.color.blue))
                binding.tv4.setTextColor(ContextCompat.getColor(requireContext(),R.color.blue))
            }
        }
    }

    private fun getValues() {
        val bundle=arguments
        status=bundle?.getInt("status")!!
        orderId=bundle.getString("orderId").toString()
    }

    private fun onBackButtonClicked() {
        binding.tbOrderDetail.setNavigationOnClickListener{
            findNavController().navigate(R.id.action_orderDetailsFragment_to_ordersFragment)
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
            Utils.showToast(requireContext(),"No more stock available...")
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
            productBinding.allProductCount.visibility=View.GONE
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
