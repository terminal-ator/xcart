package com.hypercode.android.excart.ui.productDetail

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.*
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.apollographql.apollo.coroutines.toDeferred
import com.example.AddSkuToCartMutation
import com.example.ProductQuery
import com.example.type.AddSkuInput
import com.example.type.SkuCodes
import com.hypercode.android.excart.R
import com.hypercode.android.excart.authApolloClient
import com.hypercode.android.excart.data.model.Sku
import com.hypercode.android.excart.databinding.FragmentProductDetailBinding
import dagger.hilt.android.AndroidEntryPoint

const val TAG = "ProductDetailFragment"

@AndroidEntryPoint
class ProductDetailFragment: Fragment() {

    val args: ProductDetailFragmentArgs by navArgs()
    private val productDetailViewModel: ProductDetailViewModel by viewModels()
    private lateinit var recyclerView: RecyclerView
    private lateinit var productHeading: TextView
    private lateinit var loading: ProgressBar
    private lateinit var parentLayout: ConstraintLayout
    private var adapter: SkuAdapter? = null
//    private  var product: ProductQuery.GetProduct? = null?
    private var skuCodes: MutableList<SkuCodes> = mutableListOf()
    private var codeMap: MutableMap<String, Int> = mutableMapOf()
    private lateinit var binding: FragmentProductDetailBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_product_detail, container, false)
        recyclerView = root.findViewById(R.id.sku_recycler) as RecyclerView
        productHeading = root.findViewById(R.id.tv_product_name) as TextView
        loading = root.findViewById(R.id.product_loading) as ProgressBar
        parentLayout = root.findViewById(R.id.detail_container) as ConstraintLayout
        recyclerView.layoutManager = LinearLayoutManager(context)
        (recyclerView.layoutManager as LinearLayoutManager).stackFromEnd = true
        val dividerItemDecoration = DividerItemDecoration(
            recyclerView.context,
            LinearLayoutManager(context).orientation
        )
        recyclerView.addItemDecoration(dividerItemDecoration)
        parentLayout.visibility = View.GONE
        loading.visibility = View.VISIBLE

        productDetailViewModel.getProduct(args.productId).observe(
            viewLifecycleOwner,
            Observer {
                product ->
                    if(product!=null){
                        productDetailViewModel.product.postValue(product)
                        productHeading.apply {
                            text = product.name
                        }
                        adapter = SkuAdapter(product.skus)
                        recyclerView.adapter = adapter
                        loading.visibility = View.GONE
                        parentLayout.visibility = View.VISIBLE
                    }

            }
        )

        binding = FragmentProductDetailBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.product_detail_menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when(item.itemId){
            R.id.product_detail_menu_save -> {
                saveProduct()
                true
            }else -> {
                super.onOptionsItemSelected(item)
            }
        }
    }

    fun addSku(sku_code: String, quantity: Int){
        codeMap[sku_code] = quantity
        Log.i(TAG, codeMap.toString());

    }

    private fun saveProduct(){
        if(codeMap.isNotEmpty()){
            var skuCodes: MutableList<SkuCodes> = mutableListOf()
            codeMap.forEach{
                    (key,value)->
                skuCodes.add(SkuCodes(sku_code = key, quantity = value))
                val sku = Sku(code = key, quantity = value)
                productDetailViewModel.productRepository.updateOrInsert(sku)
            }
            var mutationInput = AddSkuInput(
                // TODO Remove static assoc company
                assoc_company = 2,
                product_id = args.productId,
                // TODO Remove static user id
                user_id = "5f0182ee5b11301c873d9491",
                skus = skuCodes
            )
            Log.i(TAG, mutationInput.toString())
            lifecycleScope.launchWhenResumed{
                val response = try {
                    authApolloClient().mutate(AddSkuToCartMutation(input = mutationInput)).toDeferred().await()
                }catch (e: Exception){
                    Log.d(TAG, "Failed to add to cart", e)
                    null
                }
                val cart = response?.data?.addSkuToCart
                if(cart == null || response.hasErrors()){

                    return@launchWhenResumed
                }else{
                    findNavController().popBackStack()
                }
            }
        }
    }

    private inner class SkuHolder(view: View): RecyclerView.ViewHolder(view){
        val skuName: TextView = view.findViewById(R.id.item_sku_name)
        val quantityEditText: EditText = view.findViewById(R.id.edit_quantity);
        private var sku: ProductQuery.Sku? = null
        fun bind(sku: ProductQuery.Sku){
            this.sku = sku
            skuName.apply {
                text = sku.name
            }
            if(sku!=null){
                val dbSku = productDetailViewModel.productRepository.getSku(sku.code!!)
                dbSku.observe(viewLifecycleOwner, Observer {
                    nSku -> if(nSku!=null){
                       quantityEditText.setText(nSku.quantity.toString())
                    }
                })
            }
            quantityEditText.addTextChangedListener(object: TextWatcher{
                override fun afterTextChanged(s: Editable?) {
                    try{
                        val quantity: Int = s.toString().toInt();
                        addSku(sku.code!!, quantity)
                    }catch (e: Exception){
                        Log.d(TAG,"Failed to update for zero quantity",e)
                        addSku(sku.code!!, 0)
                    }
                }

                override fun beforeTextChanged(
                    s: CharSequence?,
                    start: Int,
                    count: Int,
                    after: Int
                ) {
                }

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                }

            })

        }
    }

    private inner class SkuAdapter(var skus: List<ProductQuery.Sku>):RecyclerView.Adapter<SkuHolder>(){
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SkuHolder {
            val view = layoutInflater.inflate(R.layout.layout_item_sku, parent, false)
            return SkuHolder(view)
        }

        override fun getItemCount(): Int {
            return skus.size
        }

        override fun onBindViewHolder(holder: SkuHolder, position: Int) {
            val sku = skus[position]
            holder.bind(sku)
        }

    }
}