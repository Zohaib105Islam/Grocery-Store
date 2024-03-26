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
                adapterBestSeller=AdapterBestSeller(::onSeeAllButtonClicked)
                binding.rvBestSeller.adapter=adapterBestSeller
                Utils.showToast(requireContext(),"Best seller products")
                adapterBestSeller.differ.submitList(it)
            }
        }
    }

    fun onSeeAllButtonClicked(productType: Bestseller){

        val bsSeeAllBinding = BsSeeAllBinding.inflate(LayoutInflater.from(requireContext()))

        val bs=BottomSheetDialog(requireContext())
        bs.setContentView(bsSeeAllBinding.root)

        adapterProduct= AdapterProduct(::onAddBtnClicked,::onIncrementButtonClicked,::onDecrementButtonClicked)
        bsSeeAllBinding.rvProducts.adapter=adapterProduct
        adapterProduct.differ.submitList(productType.products)
        bs.show()

    }
    fun onAddBtnClicked(product: Product, productBinding: ItemViewProductBinding){

        productBinding.tvAddBtn.visibility= View.GONE
        productBinding.allProductCount.visibility=View.VISIBLE

        //  step 1
        var itemCount= productBinding.tvProductCount.text.toString().toInt()
        itemCount++
        productBinding.tvProductCount.text = itemCount.toString()

        cartListner?.showCartLayout(1)

        // step 2
        product.itemCount=itemCount
        lifecycleScope.launch {
            cartListner?.savingCartItemCount(1)
            saveProductInRoomDb(product)
            viewModel.updateItemCount(product,itemCount)
        }


    }

    fun onIncrementButtonClicked(product: Product, productBinding: ItemViewProductBinding){
        var itemCountInc= productBinding.tvProductCount.text.toString().toInt()
        itemCountInc++

        if (product.productStock!! + 1 > itemCountInc){
            productBinding.tvProductCount.text = itemCountInc.toString()

            cartListner?.showCartLayout(1)

            // step 2
            product.itemCount=itemCountInc
            lifecycleScope.launch {
                cartListner?.savingCartItemCount(1)
                saveProductInRoomDb(product)
                viewModel.updateItemCount(product,itemCountInc)
            }
        }
        else{
            Utils.showToast(requireContext(),"No more stock available...")
        }
    }

    fun onDecrementButtonClicked(product: Product, productBinding: ItemViewProductBinding){
        var itemCountDec= productBinding.tvProductCount.text.toString().toInt()
        itemCountDec--

        // step 2
        product.itemCount=itemCountDec
        lifecycleScope.launch {
            cartListner?.savingCartItemCount(-1)
            saveProductInRoomDb(product)
            viewModel.updateItemCount(product,itemCountDec)
        }

        if (itemCountDec > 0){
            productBinding.tvProductCount.text = itemCountDec.toString()
        }
        else{
            lifecycleScope.launch { viewModel.deleteCartProduct(product.productRandomId!!) }
            productBinding.tvAddBtn.visibility= View.VISIBLE
            productBinding.allProductCount.visibility=View.GONE
            productBinding.tvProductCount.text = "0"
        }
        cartListner?.showCartLayout(-1)




    }

    fun saveProductInRoomDb(product: Product) {

        val cartProduct = CartProducts(
            itemPushKey = product.itemPushKey!!,
            productRandomId = product.productRandomId!!,
            productTitle = product.productTitle,
            productQuantity = product.productQuantity.toString() + product.productUnit.toString(),
            productPrice = "Rs" + "${product.productPrice}",
            productCount = product.itemCount,
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