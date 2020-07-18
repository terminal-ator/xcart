package com.hypercode.android.excart.ui.home

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.coroutines.toDeferred
import com.apollographql.apollo.exception.ApolloException
import com.example.FetchProductQuery
import com.hypercode.android.excart.R
import com.hypercode.android.excart.apolloClient
import com.hypercode.android.excart.ui.productDetail.ProductDetailFragment
import kotlinx.coroutines.launch
import org.w3c.dom.Text

const val TAG = "Homefragment"
class HomeFragment : Fragment() {

    // call back for hosting activity
    interface Callbacks{
        fun onProductSelected(productID: String)
    }

    private var callbacks: Callbacks? = null


    private lateinit var homeViewModel: HomeViewModel
    private lateinit var productRecyclerView: RecyclerView
    private lateinit var products: List<FetchProductQuery.GetProduct?>

    private var adapter: ProductAdapter? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        callbacks = context as Callbacks?
    }

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        homeViewModel =
                ViewModelProviders.of(this).get(HomeViewModel::class.java)
        val root = inflater.inflate(R.layout.fragment_home, container, false)
        productRecyclerView = root.findViewById(R.id.product_recycler) as RecyclerView
        productRecyclerView.layoutManager = LinearLayoutManager(context)
        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        lifecycleScope.launchWhenResumed{
            val response =
                try {
                    apolloClient.query(FetchProductQuery(companyID = 2)).toDeferred().await()
                }catch (e: ApolloException){
                    Log.d("HomeFragment", "Failed", e)
                    null
                }

            val fetchProducts = response?.data?.getProducts?.filterNotNull()
            if(fetchProducts!=null && !response.hasErrors() ){
                products = fetchProducts;
                updateUI();
            }

        }
    }

    private inner class ProductHolder(view: View):RecyclerView.ViewHolder(view), View.OnClickListener{
        val productNameTextView: TextView = view.findViewById(R.id.text_product_name)
        var product: FetchProductQuery.GetProduct? = null

        init{
            view.setOnClickListener(this)
        }

        fun bind(bindProduct: FetchProductQuery.GetProduct){
            product = bindProduct
            productNameTextView.text = product!!.name
        }
        override fun onClick(v: View?) {
            Log.d(TAG, "Clicked Viewholder")
            if(product!=null)
                product?._id.let {
                    if (it != null) {
                        val action = HomeFragmentDirections.listToDetail(productId = it)
                        findNavController().navigate(action)
                    }
                }
        }

    }

    private inner class ProductAdapter(var products: List<FetchProductQuery.GetProduct?>):
        RecyclerView.Adapter<ProductHolder>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductHolder {
           val view  =  layoutInflater.inflate(R.layout.layout_item_product, parent, false)
            return ProductHolder(view)
        }

        override fun getItemCount(): Int {
            return products.size;
        }

        override fun onBindViewHolder(holder: ProductHolder, position: Int) {
            val product = products[position]
            if (product != null) {
                holder.bind(product)
            }
        }

    }

    private fun updateUI(){
        adapter = ProductAdapter(products)
        productRecyclerView.adapter = adapter
    }

    override fun onDetach() {
        super.onDetach()
        callbacks = null
    }

}
