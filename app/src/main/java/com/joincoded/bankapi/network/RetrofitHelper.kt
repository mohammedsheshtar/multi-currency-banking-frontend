//package com.joincoded.bankapi.network
//
//import android.content.Context
//import com.joincoded.bankapi.utils.Constants
//import com.joincoded.bankapi.utils.Constants.Companion.baseUrl
//import com.joincoded.bankapi.utils.TokenManager
//import okhttp3.OkHttpClient
//import retrofit2.Retrofit
//import retrofit2.converter.gson.GsonConverterFactory
//
//object RetrofitHelper {
//    private var retrofitInstance: Retrofit? = null
//    private var context: Context? = null
//
//    fun initialize(context: Context) {
//        this.context = context.applicationContext
//        retrofitInstance = null // Force recreation with new context
//    }
//
//    private fun getRetrofitInstance(): Retrofit {
//        if (retrofitInstance == null) {
//            val context = context ?: throw IllegalStateException("RetrofitHelper not initialized. Call initialize() first.")
//
//            val okHttpClient = OkHttpClient.Builder()
//                .addInterceptor(TokenInterceptor {
//                    TokenManager.getToken(context)
//                })
//                .build()
//
//            retrofitInstance = Retrofit.Builder()
//                .baseUrl(baseUrl)
//                .client(okHttpClient)
//                .addConverterFactory(GsonConverterFactory.create())
//                .build()
//        }
//        return retrofitInstance!!
//    }
//
//    val AccountApi: AccountApiService by lazy {
//        getRetrofitInstance().create(AccountApiService::class.java)
//    }
//
//    val AuthenticationApi: AuthenticationApiService by lazy {
//        getRetrofitInstance().create(AuthenticationApiService::class.java)
//    }
//
//    val MembershipApi: MembershipApiService by lazy {
//        getRetrofitInstance().create(MembershipApiService::class.java)
//    }
//
//    val ShopApi: ShopApiService by lazy {
//        getRetrofitInstance().create(ShopApiService::class.java)
//    }
//
//    val TransactionApi: TransactionApiService by lazy {
//        getRetrofitInstance().create(TransactionApiService::class.java)
//    }
//
//    val UserApi: UserApiService by lazy {
//        getRetrofitInstance().create(UserApiService::class.java)
//    }
//
//    val KycApi: KycApiService by lazy {
//        getRetrofitInstance().create(KycApiService::class.java)
//    }
//}

package com.joincoded.bankapi.network

import android.content.Context
import com.joincoded.bankapi.utils.Constants.Companion.baseUrl
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitHelper {
    private var retrofitInstance: Retrofit? = null
    private var context: Context? = null

    fun initialize(context: Context) {
        this.context = context.applicationContext
        retrofitInstance = null // Force recreation with new context
    }

    private fun getRetrofitInstance(): Retrofit {
        if (retrofitInstance == null) {
            val ctx = context ?: throw IllegalStateException("RetrofitHelper not initialized. Call initialize() first.")

            val okHttpClient = OkHttpClient.Builder()
                .addInterceptor(TokenInterceptor(ctx)) // Using Context-based TokenInterceptor
                .build()

            retrofitInstance = Retrofit.Builder()
                .baseUrl(baseUrl)
                .client(okHttpClient)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
        }
        return retrofitInstance!!
    }

    val AccountApi: AccountApiService by lazy {
        getRetrofitInstance().create(AccountApiService::class.java)
    }

    val AuthenticationApi: AuthenticationApiService by lazy {
        getRetrofitInstance().create(AuthenticationApiService::class.java)
    }

    val MembershipApi: MembershipApiService by lazy {
        getRetrofitInstance().create(MembershipApiService::class.java)
    }

    val ShopApi: ShopApiService by lazy {
        getRetrofitInstance().create(ShopApiService::class.java)
    }

    val TransactionApi: TransactionApiService by lazy {
        getRetrofitInstance().create(TransactionApiService::class.java)
    }

    val UserApi: UserApiService by lazy {
        getRetrofitInstance().create(UserApiService::class.java)
    }

    val KycApi: KycApiService by lazy {
        getRetrofitInstance().create(KycApiService::class.java)
    }
}
