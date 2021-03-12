package com.hypercode.android.excart.data

import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.room.Room
import com.example.FetchProductCompaniesQuery
import com.example.FetchProductQuery
import com.example.ProductQuery
import com.hypercode.android.excart.data.model.ProductDataSource
import com.hypercode.android.excart.data.model.Sku
import com.hypercode.android.excart.database.DatabaseRespository
import com.hypercode.android.excart.database.ExcartDatabase
import java.util.concurrent.Executors
import javax.inject.Inject

private const val TAG = "Product Repository"
class ProductRepository @Inject constructor(
    private val dataSource: ProductDataSource
) {

    private val executor = Executors.newSingleThreadExecutor()

    private val database = DatabaseRespository.get().database

    private val skuDao = database.skuDao()

   suspend fun fetchProducts(cmpyID: String): List<FetchProductQuery.GetProduct?>? {
       val result =  dataSource.fetchProducts(cmpyID = cmpyID)
       return result
   }
    suspend fun fetchProduct(productID: String): ProductQuery.GetProduct? {
        val result = dataSource.fetchSkus(productID)
        return result
    }

    suspend fun fetchCompanies():List<FetchProductCompaniesQuery.GetProductCompany?>?{
        return dataSource.fetchProductCompanies()
    }


    fun getSku(id:String): LiveData<Sku?> = skuDao.getSku(id)

    fun addSku(sku: Sku){
        executor.execute{
            skuDao.addSku(sku)
        }
    }

    fun updateSku(sku:Sku){
        executor.execute{
            skuDao.updateSku(sku)
        }
    }

    fun updateOrInsert(sku:Sku){
        executor.execute{
            val existingSKU = skuDao.getSimpleSku(sku.code)
            Log.d(TAG,existingSKU.toString())
            if(existingSKU!=null){
                updateSku(sku)
            }else{
                addSku(sku)
            }
        }
    }
}