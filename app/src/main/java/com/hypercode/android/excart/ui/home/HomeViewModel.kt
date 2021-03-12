package com.hypercode.android.excart.ui.home

import android.util.Log
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.apollographql.apollo.ApolloClient
import com.example.FetchProductCompaniesQuery
import com.example.FetchProductQuery
import com.hypercode.android.excart.data.ProductRepository
import kotlinx.coroutines.launch

private const val TG = "HomeviewModel"

class HomeViewModel @ViewModelInject constructor(
    private val productRepository: ProductRepository
) : ViewModel() {


    private var _products = MutableLiveData<List<FetchProductQuery.GetProduct?>?>()
    val products: LiveData<List<FetchProductQuery.GetProduct?>?> get() = _products

    fun refreshProducts(cmpyID: String = "all"){
        viewModelScope.launch {
            val prods = productRepository.fetchProducts(cmpyID = cmpyID)
            Log.i(TG,prods.toString())
            _products.value = productRepository.fetchProducts(cmpyID = cmpyID)
        }

    }

    fun getCompanies(): LiveData<List<FetchProductCompaniesQuery.GetProductCompany?>?>{
        val result = MutableLiveData<List<FetchProductCompaniesQuery.GetProductCompany?>?>()
        viewModelScope.launch {
            result.value = productRepository.fetchCompanies()
        }
        return result

    }
}