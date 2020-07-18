package com.hypercode.android.excart.ui.dashboard

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ListView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.apollographql.apollo.coroutines.toDeferred
import com.example.GetCartQuery
import com.hypercode.android.excart.R
import com.hypercode.android.excart.apolloClient

private const val TAG = "CartFragment"
class DashboardFragment : Fragment() {

    private lateinit var dashboardViewModel: DashboardViewModel
    private var userid = "5f0182ee5b11301c873d9491"
    private lateinit var recyclerView: RecyclerView
    private var adapter: ProductAdapter? = null

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        dashboardViewModel =
                ViewModelProviders.of(this).get(DashboardViewModel::class.java)
        val root = inflater.inflate(R.layout.fragment_dashboard, container, false)
        recyclerView = root.findViewById(R.id.cart_recycler)
        recyclerView.layoutManager = LinearLayoutManager(context)
        dashboardViewModel.cartProducts.observe(viewLifecycleOwner, Observer {
            if(it.isNotEmpty()){
                Log.i(TAG,"Got products ${it.toString()}")
                adapter = ProductAdapter(it)
                recyclerView.adapter = adapter
            }
        })
        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        lifecycleScope.launchWhenResumed{
            val response = try{
                apolloClient.query(GetCartQuery(userid = userid )).toDeferred().await()
            }catch (e: Exception){
                Log.d(TAG, "Failed to access server", e)
                null
            }
            Log.i(TAG, "The response is ${response.toString()}")
            val products = response?.data?.getCart?.products?.filterNotNull()
            if(products!=null  && !response.hasErrors()){
                dashboardViewModel.cartProducts.value = products
                Log.i(TAG, "Got the products: $products")
            }else{
                Log.i(TAG, "No products found")
            }
        }
    }

    private inner class ProductHolder(view: View): RecyclerView.ViewHolder(view), View.OnClickListener{
        private val productNameTextView: TextView = view.findViewById(R.id.cart_product_name)
        private var product: GetCartQuery.Product? = null
        private val SkuListView: ListView = view.findViewById(R.id.cart_sku_list)

        init {
            view.setOnClickListener(this)
        }

        fun bind(product:GetCartQuery.Product){
            this.product = product
            productNameTextView.apply {
                text = product.name
            }
            val adapter = SkuAdapter(product.skus)
            SkuListView.adapter = adapter
        }

        override fun onClick(v: View?) {
           Log.i(TAG, "Product clicked")
            if(product!=null) {
                val action = DashboardFragmentDirections.cartToDetail(productId = product!!._id!!)
                findNavController().navigate(action)
            }
        }
    }

    private inner class ProductAdapter(var products: List<GetCartQuery.Product>):RecyclerView.Adapter<ProductHolder>(){
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductHolder {
            val view = layoutInflater.inflate(R.layout.layout_item_cart, parent, false)
            return ProductHolder(view)
        }

        override fun getItemCount(): Int {
            return products.size
        }

        override fun onBindViewHolder(holder: ProductHolder, position: Int) {
            val product = products[position]
            holder.bind(product)
        }

    }

    private inner class SkuAdapter(val skus: List<GetCartQuery.Sku>): BaseAdapter(){
        override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
            val returnView: View =
                convertView ?: layoutInflater.inflate(R.layout.item_cart_sku, parent, false)
            val skuNameTextView: TextView = returnView.findViewById(R.id.cart_sku_name)
            val skuQuantityTextView: TextView = returnView.findViewById(R.id.cart_sku_quantity)
            val sku = getItem(position)
            skuNameTextView.apply {
                text = sku.name
            }
            skuQuantityTextView.apply {
                text = sku.quantity.toString()
            }

            return returnView
        }

        override fun getItem(position: Int): GetCartQuery.Sku {
            return skus[position]
        }

        override fun getItemId(position: Int): Long {
            return position.toLong()
        }

        override fun getCount(): Int {
            return if(skus.size>3){
                3
            }else{
                skus.size
            }
        }

    }

}
