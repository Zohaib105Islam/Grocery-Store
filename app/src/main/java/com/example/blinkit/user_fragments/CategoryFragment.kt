package com.example.blinkit.user_fragments

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.blinkit.R
import com.example.blinkit.adapters.AdapterProduct
import com.example.blinkit.databinding.FragmentCategoryBinding
import com.example.blinkit.databinding.ItemViewProductBinding
import com.example.blinkit.models.Product
import com.example.blinkit.roomdb.CartProducts
import com.example.blinkit.utils.CartListner
import com.example.blinkit.utils.Utils
import com.example.blinkit.viewmodels.UserViewModel
import kotlinx.coroutines.launch

class CategoryFragment : Fragment() {

    private lateinit var binding: FragmentCategoryBinding

    private var category: String? =null

    private val viewModel: UserViewModel by viewModels()

    private lateinit var adapterProduct : AdapterProduct

    private var cartListner : CartListner? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentCategoryBinding.inflate(inflater , container, false)

        getCategoryProduct()

        setToolbarTitle()

        onNavigationIconClick()
        fetchCategoryProduct()
        onSearchMenuClick()

        return binding.root
    }

    private fun onNavigationIconClick() {
        binding.tbCategoryTitle.setNavigationOnClickListener {
            findNavController().navigate(R.id.action_categoryFragment_to_homeFragment)
        }
    }

    private fun onSearchMenuClick() {
        binding.tbCategoryTitle.setOnMenuItemClickListener { menuItem->
            when(menuItem.itemId){
                R.id.searchMenu ->{
                    findNavController().navigate(R.id.action_categoryFragment_to_searchFragment)
                    true
                }
                else ->{false}
            }
        }
    }

    private fun fetchCategoryProduct() {
        binding.shimmerViewContainer.visibility = View.VISIBLE
        if (category != null) {
        lifecycleScope.launch {
            viewModel.getCategoryProduct(category!!).collect{

                if (it.isEmpty()){
                    binding.rvProducts.visibility=View.GONE
                    binding.tvText.visibility=View.VISIBLE
                }
                else{
                    binding.rvProducts.visibility=View.VISIBLE
                    binding.tvText.visibility=View.GONE
                }

                adapterProduct = AdapterProduct(::onAddBtnClicked, ::onIncrementButtonClicked, ::onDecrementButtonClicked)
                binding.rvProducts.adapter=adapterProduct
                adapterProduct.differ.submitList(it)
                //adapterProduct.originalList = it as ArrayList<Product>
                binding.shimmerViewContainer.visibility = View.GONE
            }
        }
        }else{
            Utils.showToast(requireContext(),"Category is null")
           // binding.shimmerViewContainer.visibility = View.GONE
        }

    }

    private fun setToolbarTitle() {
        binding.tbCategoryTitle.title=category
    }

    private fun getCategoryProduct() {
        val bundle = arguments
        category=bundle?.getString("category")
    }

    fun onAddBtnClicked(product: Product , productBinding: ItemViewProductBinding){

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

    fun onIncrementButtonClicked(product: Product,productBinding: ItemViewProductBinding){
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

    fun onDecrementButtonClicked(product: Product,productBinding: ItemViewProductBinding){
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
            productRandomId  = product.productRandomId!!,
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


}