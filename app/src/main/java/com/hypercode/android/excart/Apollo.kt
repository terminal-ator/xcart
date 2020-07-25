package com.hypercode.android.excart

import android.content.Context
import android.os.Looper
import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.api.Operation
import com.apollographql.apollo.api.ResponseField
import com.apollographql.apollo.cache.normalized.CacheKey
import com.apollographql.apollo.cache.normalized.CacheKeyResolver
import com.apollographql.apollo.cache.normalized.lru.EvictionPolicy
import com.apollographql.apollo.cache.normalized.lru.LruNormalizedCache
import com.apollographql.apollo.cache.normalized.lru.LruNormalizedCacheFactory
import com.apollographql.apollo.cache.normalized.sql.SqlNormalizedCacheFactory
import com.hypercode.android.excart.data.LoginRepository
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import java.lang.IllegalStateException
import javax.inject.Inject
import javax.inject.Singleton

const val SERVER_URL = "http://unraidone.duckdns.org:4000"

val apolloClient = ApolloClient.builder().serverUrl(SERVER_URL)
                            .build()

private class AuthInterceptor: Interceptor{
    override fun intercept(chain: Interceptor.Chain): Response {
       val request = chain.request().newBuilder().addHeader("Authorization",LoginRepository.get().user!!.userId)
           .build()

        return chain.proceed(request)
    }
}

class ApolloSQLFactory private constructor( val context: Context){
    val sqlNormalizedCache = SqlNormalizedCacheFactory(context,"apollo.db")
    val memoryFirstThenSqlCache = LruNormalizedCacheFactory(
        EvictionPolicy.builder().maxSizeBytes(10*1024).build()
    ).chain(sqlNormalizedCache)

    companion object{
        private var INSTANCE: ApolloSQLFactory? =null
        fun initialize(context: Context){
            if(INSTANCE==null){
                INSTANCE = ApolloSQLFactory(context)
            }
        }
        fun get(): ApolloSQLFactory{
            return INSTANCE?:throw IllegalStateException("Apolllo SQL Factory must be initialized")
        }
    }

}

val resolver: CacheKeyResolver = object : CacheKeyResolver(){
    override fun fromFieldArguments(
        field: ResponseField,
        variables: Operation.Variables
    ): CacheKey {
       return CacheKey.from(field.resolveArgument("_id",variables) as String)
    }

    override fun fromFieldRecordSet(field: ResponseField, recordSet: Map<String, Any>): CacheKey {
       return CacheKey.from(recordSet["id"] as String)
    }
}


private var instance: ApolloClient? = null
fun authApolloClient(): ApolloClient{
    check(Looper.myLooper() == Looper.getMainLooper()){
        "Only main thread can get the apolloClient instance"
    }
    if(instance!=null){
        return instance!!
    }
    instance = ApolloClient.builder().serverUrl(SERVER_URL)
        .normalizedCache(ApolloSQLFactory.get().sqlNormalizedCache)
        .okHttpClient(
        OkHttpClient.Builder()
            .addInterceptor(AuthInterceptor())
            .build()
    ).build()
    return instance!!
}
