package com.hypercode.android.excart.data

import android.util.Log
import com.apollographql.apollo.coroutines.toDeferred
import com.example.SigninUserMutation
import com.hypercode.android.excart.apolloClient
import com.hypercode.android.excart.data.model.LoggedInUser
import java.io.IOException
import java.lang.Exception

/**
 * Class that handles authentication w/ login credentials and retrieves user information.
 */
class LoginDataSource {

    suspend fun login(username: String, password: String): Result<LoggedInUser> {
        val res = try{
            apolloClient.mutate(SigninUserMutation(username = username, password = password)).toDeferred().await()
        }catch (e: Exception){
            Log.i("LoginDataSource", "Failed to connect to grapqhl", e)
            null
        }

        val fetchUser = res?.data?.signinUser
        if(fetchUser!=null && fetchUser.successful && !res.hasErrors()){
            val user = LoggedInUser(fetchUser.token!!,"TODO display name")
            return Result.Success(user)
        }else{
            return Result.Error(IOException("Error Loggin in"))
        }

    }

    fun logout() {
        // TODO: revoke authentication
    }
}

