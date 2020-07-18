package com.hypercode.android.excart.ui.productDetail

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.ProductQuery

class ProductDetailViewModel: ViewModel() {
     var product = MutableLiveData<ProductQuery.GetProduct>()
}