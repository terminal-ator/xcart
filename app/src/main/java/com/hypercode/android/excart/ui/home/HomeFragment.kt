package com.hypercode.android.excart.ui.home

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.Spinner
import android.widget.SpinnerAdapter
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.coroutines.toDeferred
import com.apollographql.apollo.exception.ApolloException
import com.example.FetchProductCompaniesQuery
import com.example.FetchProductQuery
import com.hypercode.android.excart.R
import com.hypercode.android.excart.apolloClient
import com.hypercode.android.excart.ui.productDetail.ProductDetailFragment
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import org.w3c.dom.Text

const val TAG = "Homefragment"

@AndroidEntryPoint
class HomeFragment : Fragment() {

    // call back for hosting activity
    interface Callbacks{
        fun onProductSelected(productID: String)
    }

    private var callbacks: Callbacks? = null
    private  val homeViewModel: HomeViewModel by viewModels()
    private lateinit var productRecyclerView: RecyclerView
    private lateinit var companyRecyclerView: RecyclerView
    private lateinit var products: List<FetchProductQuery.GetProduct?>
    private  var companies: List<FetchProductCompaniesQuery.GetProductCompany?>? = null

    private var adapter: ProductAdapter? = null
    private var companyAdapter: CompanyAdapter? = null
    private var selectedCompany = MutableLiveData<String>("all")

    override fun onAttach(context: Context) {
        super.onAttach(context)
        callbacks = context as Callbacks?
    }

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_home, container, false)

        productRecyclerView = root.findViewById(R.id.product_recycler) as RecyclerView
        productRecyclerView.layoutManager = LinearLayoutManager(context)
        val dividerItemDecoration  = DividerItemDecoration(productRecyclerView.context, LinearLayoutManager(context).orientation)
        productRecyclerView.addItemDecoration(dividerItemDecoration)

        companyRecyclerView = root.findViewById(R.id.company_recycler) as RecyclerView
        companyRecyclerView.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)

        homeViewModel.refreshProducts()

        homeViewModel.products.observe(
            viewLifecycleOwner,
            Observer {
                    if(it!=null){
                        Log.i(TAG,"Updating products: ${it}")
                        products = it
                        updateUI()
                    }
            }
        )

        homeViewModel.getCompanies().observe(viewLifecycleOwner,
        Observer {
            if(it!=null){
                Log.i(TAG,it.toString())
                val allCompany = FetchProductCompaniesQuery.GetProductCompany(_id = "all", name = "All")
                val finalList = listOf(allCompany) + it
                companies = finalList
                companyAdapter = CompanyAdapter(finalList)
                companyRecyclerView.adapter = companyAdapter
            }
        })

        selectedCompany.observe(viewLifecycleOwner,
        Observer {
            homeViewModel.refreshProducts(cmpyID = it)
            Log.i(TAG, it)
        })
        return root
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
            return products.size
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

    private inner class CompanyViewHolder(view: View): RecyclerView.ViewHolder(view), View.OnClickListener{

        val companyTextView: TextView = view.findViewById(R.id.company_name) as TextView
        var company: FetchProductCompaniesQuery.GetProductCompany? = null

        init {
            view.setOnClickListener(this)
            selectedCompany.observe(viewLifecycleOwner, Observer {
                if(company!=null){
                    if(it == company?._id){
                        view.setBackgroundColor(resources.getColor(R.color.selected))
                    }else{
                        view.setBackgroundColor(resources.getColor(R.color.unselected))
                    }
                }
            })
        }

        fun bind(company: FetchProductCompaniesQuery.GetProductCompany){
            this.company = company
            companyTextView.text = company.name
        }

        override fun onClick(v: View?) {
            selectedCompany.value = company?._id
        }

    }

    private inner class CompanyAdapter(val companies: List<FetchProductCompaniesQuery.GetProductCompany?>): RecyclerView.Adapter<CompanyViewHolder>(){
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CompanyViewHolder {
           val view = layoutInflater.inflate(R.layout.company_recycler_item, parent, false)
            return CompanyViewHolder(view)
        }

        override fun getItemCount(): Int = companies.size

        override fun onBindViewHolder(holder: CompanyViewHolder, position: Int) {
            val company = companies[position]
            if(company!=null)
                holder.bind(company)
        }

    }

}
