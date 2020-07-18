package com.hypercode.android.excart.ui.productDetail

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.apollographql.apollo.coroutines.toDeferred
import com.apollographql.apollo.exception.ApolloException
import com.example.AddSkuToCartMutation
import com.example.ProductQuery
import com.example.type.AddSkuInput
import com.example.type.SkuCodes
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.hypercode.android.excart.MainActivity
import com.hypercode.android.excart.R
import com.hypercode.android.excart.apolloClient

const val TAG = "ProductDetailFragment"
class ProductDetailFragment: Fragment() {

    val args: ProductDetailFragmentArgs by navArgs()
    private lateinit var productDetailViewModel: ProductDetailViewModel
    private lateinit var recyclerView: RecyclerView
    private lateinit var saveButton: Button
    private var adapter: SkuAdapter? = null
//    private  var product: ProductQuery.GetProduct? = null?
    private var skuCodes: MutableList<SkuCodes> = mutableListOf()
    private var codeMap: MutableMap<String, Int> = mutableMapOf()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_product_detail, container, false)
        productDetailViewModel = ViewModelProviders.of(this).get(ProductDetailViewModel::class.java)
        recyclerView = root.findViewById(R.id.sku_recycler) as RecyclerView
        recyclerView.layoutManager = LinearLayoutManager(context)

        return root
    }

    fun addSku(sku_code: String, quantity: Int){
//        val skuCode = SkuCodes( sku_code = sku_code, quantity = quantity)
//        skuCodes.add(skuCode);
        codeMap[sku_code] = quantity
//        Log.i(TAG, skuCodes.toString());
        Log.i(TAG, codeMap.toString());

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val id = args.productId
        Log.i(TAG,"Got the id:, $id")
        lifecycleScope.launchWhenResumed{
            val response = try{
                apolloClient.query(ProductQuery(productID = id)).toDeferred().await()
            }catch (e: ApolloException){
                Log.d(TAG, "Failed", e)
                null
            }

            val product = response?.data?.getProduct
            if(product!=null && !response.hasErrors()){
                productDetailViewModel.product.postValue(product)
                var headingView: TextView = view.findViewById(R.id.tv_product_name)
                headingView.apply {
                    text = product.name
                }
                adapter = SkuAdapter(product.skus)
                recyclerView.adapter = adapter
                Log.d(TAG, "The products is : ${product.name}" )
            }
        }
        saveButton = view.findViewById(R.id.save_button)
        saveButton.setOnClickListener{

            if(codeMap.isNotEmpty()){

                var skuCodes: MutableList<SkuCodes> = mutableListOf()
                codeMap.forEach{
                    (key,value)->
                        skuCodes.add(SkuCodes(sku_code = key, quantity = value))
                }

                var mutationInput: AddSkuInput = AddSkuInput(
                    // TODO Remove static assoc company
                    assoc_company = 2,
                    product_id = args.productId,
                    // TODO Remove static user id
                    user_id = "5f0182ee5b11301c873d9491",
                    skus = skuCodes
                )

                Log.i(TAG, mutationInput.toString())
                saveButton.visibility = View.GONE
                lifecycleScope.launchWhenResumed{
                    val response = try {
                        apolloClient.mutate(AddSkuToCartMutation(input = mutationInput)).toDeferred().await()
                    }catch (e: Exception){
                        Log.d(TAG, "Failed to add to cart", e)
                        null
                    }
                    val cart = response?.data?.addSkuToCart
                    if(cart == null || response.hasErrors()){
                        saveButton.visibility = View.VISIBLE
                        return@launchWhenResumed
                    }else{
                        findNavController().popBackStack()
                    }
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
            quantityEditText.addTextChangedListener(object: TextWatcher{
                override fun afterTextChanged(s: Editable?) {
                    try{
                        val quantity: Int = s.toString().toInt();
                        addSku(sku.code!!, s.toString().toInt())
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