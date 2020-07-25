package com.hypercode.android.excart.ui.home

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.apollographql.apollo.ApolloClient
import com.example.FetchProductQuery
import com.hypercode.android.excart.data.ProductRepository
import kotlinx.coroutines.launch


class HomeViewModel @ViewModelInject constructor(
    private val productRepository: ProductRepository
) : ViewModel() {

    fun getProducts():LiveData<List<FetchProductQuery.GetProduct?>?>{
        val result = MutableLiveData<List<FetchProductQuery.GetProduct?>?>()
        viewModelScope.launch {
            result.value = productRepository.fetchProducts()
        }
        return result
    }
}