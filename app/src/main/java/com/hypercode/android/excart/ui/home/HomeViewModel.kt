package com.hypercode.android.excart.ui.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.apollographql.apollo.ApolloClient
import com.example.FetchProductQuery



class HomeViewModel : ViewModel() {

    private val products = MutableLiveData<List<FetchProductQuery.GetProduct?>>()

    private val _text = MutableLiveData<String>().apply {
        value = "This is home Fragment"
    }

    val text: LiveData<String> = _text
}