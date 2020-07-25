package com.hypercode.android.excart.data.model

import android.util.Log
import com.apollographql.apollo.coroutines.toDeferred
import com.apollographql.apollo.exception.ApolloException
import com.apollographql.apollo.fetcher.ApolloResponseFetchers
import com.example.FetchProductQuery
import com.example.ProductQuery
import com.hypercode.android.excart.authApolloClient
import java.util.concurrent.Executor
import javax.inject.Inject

private const val TAG="Product Data Source"

class ProductDataSource @Inject constructor() {

     suspend fun fetchProducts(companyID: Int = 2):List<FetchProductQuery.GetProduct?>?{
        val response = try{
            authApolloClient().query(FetchProductQuery(companyID))
                .responseFetcher(ApolloResponseFetchers.CACHE_FIRST)
                .toDeferred().await()
        }catch (e: ApolloException){
            Log.d(TAG, "Failed to fetch products", e)
            null
        }
        return response?.data?.getProducts
    }

    suspend fun fetchSkus(productID: String):ProductQuery.GetProduct?{
        val response = try{
            authApolloClient().query(ProductQuery(productID)).toDeferred().await()
        }catch (e: ApolloException){
            Log.d(TAG, "Failed to fetch skus", e)
            null
        }
        return response?.data?.getProduct
    }
}