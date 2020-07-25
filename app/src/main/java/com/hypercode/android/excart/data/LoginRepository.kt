package com.hypercode.android.excart.data

import android.content.Context
import android.preference.PreferenceManager
import com.hypercode.android.excart.data.model.LoggedInUser
import java.lang.IllegalStateException
import java.util.prefs.PreferencesFactory

/**
 * Class that requests authentication and user information from the remote data source and
 * maintains an in-memory cache of login status and user credentials information.
 */

private const val USER_TOKEN = "userToken"

class LoginRepository private  constructor( val context:Context , val dataSource: LoginDataSource) {

    // in-memory cache of the loggedInUser object
    var user: LoggedInUser? = null
        private set

    val isLoggedIn: Boolean
        get() = user != null

    init {
        // If user credentials will be cached in local storage, it is recommended it be encrypted
        // @see https://developer.android.com/training/articles/keystore
        user = null
        val prefs = PreferenceManager.getDefaultSharedPreferences(context)
        val token = prefs.getString(USER_TOKEN, "")!!
        if(token!=""){
            user = LoggedInUser(token,"TODO NAME")
        }
    }

    fun logout() {
        user = null
        PreferenceManager.getDefaultSharedPreferences(context)
            .edit()
            .putString(USER_TOKEN,"")
            .apply()
        dataSource.logout()
    }

    suspend fun login(username: String, password: String): Result<LoggedInUser> {
        // handle login
        val result = dataSource.login(username, password)

        if (result is Result.Success) {
            setLoggedInUser(result.data)
            PreferenceManager.getDefaultSharedPreferences(context)
                .edit()
                .putString(USER_TOKEN, result.data.userId)
                .apply()
        }

        return result
    }

    private fun setLoggedInUser(loggedInUser: LoggedInUser) {
        this.user = loggedInUser
        // If user credentials will be cached in local storage, it is recommended it be encrypted
        // @see https://developer.android.com/training/articles/keystore
    }
    companion object{
        private var INSTANCE: LoginRepository? = null
        fun initialize( context: Context ,dataSource: LoginDataSource){
            if(INSTANCE==null){
                INSTANCE = LoginRepository(context ,dataSource)
            }
        }
        fun get(): LoginRepository{
            return INSTANCE?: throw  IllegalStateException("LoginRepository must be initialized")
        }
    }
}
