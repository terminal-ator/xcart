package com.hypercode.android.excart.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.hypercode.android.excart.data.model.Sku
import java.lang.IllegalStateException

private const val DATABASE_NAME = "excart-db"

@Database(entities = [ Sku::class], version = 1)
abstract class ExcartDatabase: RoomDatabase() {
    abstract fun skuDao(): SkuDao
}

class DatabaseRespository private constructor(val context: Context){

    val database = Room.databaseBuilder(context, ExcartDatabase::class.java, DATABASE_NAME).build()

    companion object{
        private var INSTANCE: DatabaseRespository? = null
        fun initialize(context: Context){
            if(INSTANCE==null){
                INSTANCE = DatabaseRespository(context)
            }
        }
        fun get():DatabaseRespository{
            return INSTANCE?: throw IllegalStateException("Database repository must be initiated")
        }
    }
}