package com.hypercode.android.excart.ui.productDetail

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ProductQuery
import com.hypercode.android.excart.data.ProductRepository
import kotlinx.coroutines.launch

class ProductDetailViewModel @ViewModelInject constructor(val productRepository: ProductRepository) : ViewModel() {
     var product = MutableLiveData<ProductQuery.GetProduct>()

     fun getProduct(productID: String): LiveData<ProductQuery.GetProduct>{
          val result = MutableLiveData<ProductQuery.GetProduct>()
          viewModelScope.launch {
               result.value = productRepository.fetchProduct(productID)
          }
          return result
     }
}