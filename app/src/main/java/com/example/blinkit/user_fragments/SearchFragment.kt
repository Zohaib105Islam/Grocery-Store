package com.example.blinkit.user_fragments

import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.blinkit.R
import com.example.blinkit.adapters.AdapterProduct
import com.example.blinkit.databinding.FragmentSearchBinding
import com.example.blinkit.databinding.ItemViewProductBinding
import com.example.blinkit.models.Product
import com.example.blinkit.roomdb.CartProducts
import com.example.blinkit.utils.CartListner
import com.example.blinkit.utils.Utils
import com.example.blinkit.viewmodels.UserViewModel
import kotlinx.coroutines.launch


class SearchFragment : Fragment() {

    private lateinit var binding: FragmentSearchBinding

    private lateinit var adapterProduct : AdapterProduct
    private var cartListner : CartListner? = null


    val viewModel : UserViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentSearchBinding.inflate(inflater , container , false)


        getAllTheProducts()

        backToHomeFragment()

        searchProducts()

        return binding.root
    }


    private fun searchProducts() {
        binding.etSearch.addTextChangedListener(object  : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val query = s.toString().trim()
                adapterProduct.filter?.filter(query)
                Utils.showToast(requireContext(),query)
            }

            override fun afterTextChanged(s: Editable?) {

            }

        })
    }

    private fun backToHomeFragment() {
        binding.searchCv.setOnClickListener{
            findNavController().navigate(R.id.action_searchFragment_to_homeFragment)
        }
    }

    private fun getAllTheProducts() {


        lifecycleScope.launch {
            viewModel.fetchAllTheProducts().collect{

                if (it.isEmpty()){
                    binding.rvProducts.visibility=View.GONE
                    binding.tvText.visibility=View.VISIBLE
                }
                else{
                    binding.rvProducts.visibility=View.VISIBLE
                    binding.tvText.visibility=View.GONE
                }

                adapterProduct = AdapterProduct(
                    ::onAddBtnClicked,

                )
                binding.rvProducts.adapter=adapterProduct
                adapterProduct.differ.submitList(it)
                adapterProduct.originalList = it as ArrayList<Product>
                binding.shimmerViewContainer.visibility = View.GONE

            }
        }
    }

    fun onAddBtnClicked(product: Product , productBinding: ItemViewProductBinding){


        cartListner?.showCartLayout(1)
        Utils.showToast(requireContext(),"Product added into cart")

        // step 2
        lifecycleScope.launch {
            cartListner?.savingCartItemCount(1)
            saveProductInRoomDb(product,1)
           // viewModel.updateItemCount(product,itemCount)
        }


    }

    fun saveProductInRoomDb(product: Product,itemCount : Int) {

        val cartProduct = CartProducts(
            itemPushKey = product.itemPushKey!!,
            productRandomId = product.productRandomId!!,
            productTitle = product.productTitle,
            productQuantity = product.productQuantity.toString() + product.productUnit.toString(),
            productPrice = "Rs" + "${product.productPrice}",
            //productCount = product.itemCount,
            productCount = itemCount,
            productStock = product.productStock,
            productImage = product.productImageUris?.get(0)!!,
            productCategory = product.productCategory,
            adminUid = product.adminUid,
            productType = product.productType,
        )
        lifecycleScope.launch { viewModel.insertCartProduct(cartProduct) }

    }

    override fun onAttach(context: Context) {
        super.onAttach(context)

        if (context is CartListner){
            cartListner=context
        }
        else{
            throw ClassCastException("Please implement cart listener")
        }

    }


}