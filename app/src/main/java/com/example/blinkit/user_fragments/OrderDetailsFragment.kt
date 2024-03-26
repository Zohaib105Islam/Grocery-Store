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
import com.example.blinkit.adapters.AdapterCartProducts
import com.example.blinkit.databinding.FragmentOrderDetailsBinding
import com.example.blinkit.viewmodels.UserViewModel
import kotlinx.coroutines.launch


class OrderDetailsFragment : Fragment() {

    private lateinit var binding: FragmentOrderDetailsBinding

    private val viewModel: UserViewModel by viewModels()

    private lateinit var adapterCartProducts: AdapterCartProducts

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
                adapterCartProducts=AdapterCartProducts()
                binding.rvProductItems.adapter=adapterCartProducts
                adapterCartProducts.differ.submitList(cartList)
            }
        }
    }

    private fun settingStatus() {
//        val viewsToColor = listOf(
//            binding.iv1, binding.iv2, binding.view1,
//            binding.iv3, binding.view2, binding.view3, binding.iv4
//        )
//
//        when(status) {
//            in 0..3 -> {
//                viewsToColor.take(status + 1).forEach {
//                    it.backgroundTintList = ContextCompat.getColorStateList(requireContext(), R.color.blue)
//                }
//            }
//        }

        when(status){
            0->{
                binding.iv1.backgroundTintList=ContextCompat.getColorStateList(requireContext(),R.color.blue)
            }
            1->{
                binding.iv1.backgroundTintList=ContextCompat.getColorStateList(requireContext(),R.color.blue)
                binding.iv2.backgroundTintList=ContextCompat.getColorStateList(requireContext(),R.color.blue)
                binding.view1.backgroundTintList=ContextCompat.getColorStateList(requireContext(),R.color.blue)
            }
            2->{
                binding.iv1.backgroundTintList=ContextCompat.getColorStateList(requireContext(),R.color.blue)
                binding.iv2.backgroundTintList=ContextCompat.getColorStateList(requireContext(),R.color.blue)
                binding.view1.backgroundTintList=ContextCompat.getColorStateList(requireContext(),R.color.blue)
                binding.iv3.backgroundTintList=ContextCompat.getColorStateList(requireContext(),R.color.blue)
                binding.view2.backgroundTintList=ContextCompat.getColorStateList(requireContext(),R.color.blue)
            }
            3->{
                binding.iv1.backgroundTintList=ContextCompat.getColorStateList(requireContext(),R.color.blue)
                binding.iv2.backgroundTintList=ContextCompat.getColorStateList(requireContext(),R.color.blue)
                binding.view1.backgroundTintList=ContextCompat.getColorStateList(requireContext(),R.color.blue)
                binding.iv3.backgroundTintList=ContextCompat.getColorStateList(requireContext(),R.color.blue)
                binding.view2.backgroundTintList=ContextCompat.getColorStateList(requireContext(),R.color.blue)
                binding.view3.backgroundTintList=ContextCompat.getColorStateList(requireContext(),R.color.blue)
                binding.iv4.backgroundTintList=ContextCompat.getColorStateList(requireContext(),R.color.blue)
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

}
