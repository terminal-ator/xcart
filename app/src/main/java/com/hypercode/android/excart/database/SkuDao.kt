package com.hypercode.android.excart.database

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.hypercode.android.excart.data.model.Sku

@Dao
interface SkuDao {

    @Query("SELECT * from sku")
    fun getSkus():LiveData<List<Sku>>

    @Query("Select * from sku where code=(:id)")
    fun getSku(id: String): LiveData<Sku?>

    @Query("Select * from sku where code=(:id)")
    fun getSimpleSku(id: String): Sku?

    @Update
    fun updateSku(sku: Sku)

    @Insert
    fun addSku(sku:Sku)
}