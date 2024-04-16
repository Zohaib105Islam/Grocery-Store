package com.example.blinkit.user_fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.blinkit.R
import com.example.blinkit.adapters.AdapterOrders
import com.example.blinkit.databinding.FragmentOrdersBinding
import com.example.blinkit.models.OrderedItems
import com.example.blinkit.viewmodels.UserViewModel
import kotlinx.coroutines.launch


class OrdersFragment : Fragment() {

    private lateinit var binding: FragmentOrdersBinding

    private val viewModel: UserViewModel by viewModels()

    private lateinit var adapterOrders: AdapterOrders

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding= FragmentOrdersBinding.inflate(inflater,container,false)

        onBackButtonClicked()

        getAllOrders()

        return binding.root
    }

    private fun getAllOrders() {
        lifecycleScope.launch {
            viewModel.getAllOrders().collect { orderList ->

                if (orderList.isEmpty()){
                    binding.rvOrders.visibility=View.GONE
                    binding.tvText.visibility=View.VISIBLE
                }
                else{
                    binding.rvOrders.visibility=View.VISIBLE
                    binding.tvText.visibility=View.GONE
                }

                if (orderList.isNotEmpty()) {
                    val orderedList = ArrayList<OrderedItems>()
                    for (orders in orderList) {

                        val title = StringBuilder()
                        var totalPrice = 0

                        for (products in orders.orderList!!) {
                            val price = products.productPrice?.substring(2)?.toInt()
                            val itemCount = products.productCount!!
                            totalPrice = totalPrice + ((price?.times(itemCount))!!)

                            title.append("${products.productCategory}, ")
                        }
                        val orderedItems = OrderedItems(
                            orders.orderId,
                            orders.orderDate,
                            orders.orderStatus,
                            title.toString(),
                            totalPrice
                        )
                        orderedList.add(orderedItems)
                    }

                    adapterOrders= AdapterOrders(requireContext(),::onOrderItemViewClicked)
                    binding.rvOrders.adapter=adapterOrders
                    adapterOrders.differ.submitList(orderedList)

                }
                binding.shimmerViewContainer.visibility = View.GONE
            }
        }

    }

    fun onOrderItemViewClicked(orderedItems: OrderedItems){

        val bundle=Bundle()
        bundle.putInt("status",orderedItems.itemStatus!!)
        bundle.putString("orderId",orderedItems.orderId)

        findNavController().navigate(R.id.action_ordersFragment_to_orderDetailsFragment,bundle)
    }

    private fun onBackButtonClicked() {
        binding.tbOrders.setNavigationOnClickListener{
            findNavController().navigate(R.id.action_ordersFragment_to_profileFragment)
        }


}
}