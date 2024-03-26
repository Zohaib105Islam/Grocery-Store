package com.example.blinkit.utils

import android.util.Log
import android.widget.Filter
import com.example.blinkit.adapters.AdapterProduct
import com.example.blinkit.models.Product
import java.util.Locale


class FilteringProducts(
    val adapter: AdapterProduct,
    val filter : ArrayList<Product>
) : Filter() {
    override fun performFiltering(constraint: CharSequence?): FilterResults {

        val result = FilterResults()

        Log.d("Search Tag", result.toString())


        if(!constraint.isNullOrEmpty()){
            val filterdList = ArrayList<Product>()
            val query = constraint.toString().trim().uppercase(Locale.getDefault()).split(" ")

            Log.d("Search Tag",query.toString())

            for (products in filter){
                if (query.any{
                    products.productTitle?.uppercase(Locale.getDefault())?.contains(it)  ==true ||
                    products.productCategory?.uppercase(Locale.getDefault())?.contains(it) == true ||
                    products.productPrice?.toString()?.uppercase(Locale.getDefault())?.contains(it) == true||
                    products.productType?.uppercase(Locale.getDefault())?.contains(it)== true
                    }){
                    filterdList.add(products)
                }
            }
            result.values = filterdList
            result.count  =filterdList.size
            Log.d("Search Tag","Searching working")
        }

        else{
            result.values = filter
            result.count  =filter.size
            Log.d("Search Tag","Searching not working")

        }

        return result
    }

    override fun publishResults(constraint: CharSequence?, results: FilterResults?) {

        adapter.differ.submitList(results?.values as ArrayList<Product>)
    }
}