package com.hypercode.android.excart.ui.dashboard

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.GetCartQuery

class DashboardViewModel : ViewModel() {

    var cartProducts: MutableLiveData<List<GetCartQuery.Product>> = MutableLiveData(mutableListOf())
}