package com.example.blinkit.user_fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.denzcoskun.imageslider.models.SlideModel
import com.example.blinkit.databinding.FragmentProductDetailsBinding
import com.example.blinkit.models.Product
import com.example.blinkit.roomdb.CartProducts
import com.example.blinkit.utils.CartListner
import com.example.blinkit.utils.Utils
import com.example.blinkit.viewmodels.UserViewModel
import kotlinx.coroutines.launch

class ProductDetailsFragment : Fragment() {

    private lateinit var binding: FragmentProductDetailsBinding

    val viewModel : UserViewModel by viewModels()

    private var cartListner : CartListner? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding= FragmentProductDetailsBinding.inflate(inflater,container,false)


        getProductDetails()


        return binding.root
    }

    private fun getProductDetails() {
        val bundle=arguments
        val  productRandomId=bundle?.getString("productRandomId")
        val  itemPushKey=bundle?.getString("itemPushKey")
        val  productTitle=bundle?.getString("productTitle")
        val  productQuantity=bundle?.getString("productQuantity")
        val  productUnit=bundle?.getString("productUnit")
        val  productPrice=bundle?.getString("productPrice")
        val  productStock=bundle?.getString("productStock")
        val productCategory =bundle?.getString("productCategory")
        val  productType=bundle?.getString("productType")
        val  itemCount=bundle?.getString("itemCount")
        val  adminUid=bundle?.getString("adminUid")
        val  productImageUris=bundle?.getStringArrayList("productImageUris")
      //  val timestamp  =bundle?.getString("timestamp")


        binding.tbProductDetail.title=productCategory

        // Convert the list of image URLs to a list of SlideModel objects
        val slideModels = productImageUris?.map { SlideModel(it) } ?: emptyList()
// Set the image list using the converted list of SlideModel objects
        binding.ivImageSlider.setImageList(slideModels)
        //binding.ivImageSlider.setImageList(productImageUris)

        binding.tvProductTitle.text=productTitle
        binding.tvProductPrice.text="Rs"+productPrice
        binding.productUnit.text=productQuantity + productUnit

        val product=Product(productRandomId,itemPushKey,productTitle,productQuantity?.toInt(),productUnit,productPrice?.toInt(),productStock?.toInt(),productCategory,productType,itemCount?.toInt(),adminUid,productImageUris
            //,timestamp!!.toLong()
                )

        binding.addBtn.setOnClickListener {
            onAddBtnClicked(product)
        }
    }

    fun onAddBtnClicked(product: Product){


        cartListner?.showCartLayout(1)
        Utils.showToast(requireContext(),"Product added into cart")

        // step 2
        lifecycleScope.launch {
            cartListner?.savingCartItemCount(1)
            saveProductInRoomDb(product,1)
            // viewModel.updateItemCount(product,itemCount)
        }


    }

    fun saveProductInRoomDb(product: Product, itemCount : Int) {

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
           // timestamp = product.timestamp!!,

        )
        lifecycleScope.launch { viewModel.insertCartProduct(cartProduct) }

    }


}