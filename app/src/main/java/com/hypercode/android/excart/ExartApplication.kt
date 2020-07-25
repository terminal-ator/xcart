package com.hypercode.android.excart

import android.app.Application
import com.hypercode.android.excart.data.LoginDataSource
import com.hypercode.android.excart.data.LoginRepository
import com.hypercode.android.excart.database.DatabaseRespository
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class ExartApplication: Application() {

    override fun onCreate() {
        super.onCreate()
        LoginRepository.initialize(applicationContext,LoginDataSource())
        DatabaseRespository.initialize(applicationContext)
        ApolloSQLFactory.initialize(applicationContext)
    }
}