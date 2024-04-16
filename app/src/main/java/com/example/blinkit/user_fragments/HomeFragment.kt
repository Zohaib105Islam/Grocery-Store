package com.example.blinkit.user_fragments

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.blinkit.R
import com.example.blinkit.adapters.AdapterBestSeller
import com.example.blinkit.utils.Constants
import com.example.blinkit.adapters.AdapterCategory
import com.example.blinkit.adapters.AdapterProduct
import com.example.blinkit.databinding.BsSeeAllBinding
import com.example.blinkit.databinding.FragmentHomeBinding
import com.example.blinkit.databinding.ItemViewProductBinding
import com.example.blinkit.models.Bestseller
import com.example.blinkit.models.Category
import com.example.blinkit.models.Product
import com.example.blinkit.roomdb.CartProducts
import com.example.blinkit.utils.CartListner
import com.example.blinkit.utils.Utils
import com.example.blinkit.viewmodels.UserViewModel
import com.google.android.material.bottomsheet.BottomSheetDialog
import kotlinx.coroutines.launch

class HomeFragment : Fragment() {

    private lateinit var binding : FragmentHomeBinding

    val viewModel : UserViewModel by viewModels()

    private lateinit var adapterBestSeller: AdapterBestSeller

    private lateinit var adapterProduct: AdapterProduct
    private var cartListner : CartListner? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentHomeBinding.inflate(layoutInflater,container,false)

        // Set products into category Rv
        setAllCategories()

        navigateToSearchFragment()

        onProfileClicked()

        fetchBestseller()

        return binding.root
    }

    private fun fetchBestseller() {
        lifecycleScope.launch {
            viewModel.fetchProductType().collect{

                if (it.isEmpty()){
                    binding.rvBestSeller.visibility=View.GONE
                    binding.tvText.visibility=View.VISIBLE
                }
                else{
                    binding.rvBestSeller.visibility=View.VISIBLE
                    binding.tvText.visibility=View.GONE
                }



                adapterBestSeller=AdapterBestSeller(::onSeeAllButtonClicked)
                binding.rvBestSeller.adapter=adapterBestSeller
               // Utils.showToast(requireContext(),"Best seller products")
                adapterBestSeller.differ.submitList(it)
                binding.shimmerViewContainer.visibility = View.GONE
            }
        }
    }

    fun onSeeAllButtonClicked(productType: Bestseller){

        val bsSeeAllBinding = BsSeeAllBinding.inflate(LayoutInflater.from(requireContext()))

        val bs=BottomSheetDialog(requireContext())
        bs.setContentView(bsSeeAllBinding.root)

        adapterProduct= AdapterProduct(
            ::onAddBtnClicked,

        )
        bsSeeAllBinding.rvProducts.adapter=adapterProduct
        adapterProduct.differ.submitList(productType.products)
        bs.show()

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

    private fun onProfileClicked() {
        binding.ivProfile.setOnClickListener{
            findNavController().navigate(R.id.action_homeFragment_to_profileFragment)
        }

    }


    private fun navigateToSearchFragment()  {
        binding.searchCv.setOnClickListener{
            findNavController().navigate(R.id.action_homeFragment_to_searchFragment)
        }
    }

    private fun setAllCategories() {
        val categoryList = ArrayList<Category>()

        for(i in 0 until Constants.allProductsCategoryIcon.size){
            categoryList.add(Category(Constants.allProductsCategory[i], Constants.allProductsCategoryIcon[i]))
        }

        binding.rvCategories.adapter = AdapterCategory(categoryList, ::onCategoryIconClicked)
    }

     fun onCategoryIconClicked(category: Category){
         val bundle = Bundle()
         bundle.putString("category", category.title)
         findNavController().navigate(R.id.action_homeFragment_to_categoryFragment, bundle)
     }




}