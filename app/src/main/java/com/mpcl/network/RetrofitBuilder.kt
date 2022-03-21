package com.mpcl.employee.Network

import android.content.Context
import com.google.gson.GsonBuilder
import com.mpcl.app.Constant
import com.mpcl.app.MyApp
import com.mpcl.util.AuthInterceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit


class RetrofitBuilder {
    companion object{
        private val retrofit:Retrofit by lazy {

            /*val context: Context = MyApp.getInstance().applicationContext
            val logging = HttpLoggingInterceptor()
            if (androidx.viewbinding.BuildConfig.DEBUG) {
                // development build
                logging.setLevel(HttpLoggingInterceptor.Level.BODY);
            } else {
                // production build
                logging.setLevel(HttpLoggingInterceptor.Level.BODY);
            }
            val authInterceptor = AuthInterceptor(context)
            val client = OkHttpClient.Builder()
                .addInterceptor(logging)
                .addInterceptor(authInterceptor)
                .connectTimeout(60, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS)
                .build()*/



            val gson = GsonBuilder()
                .setLenient()
                .create()
            Retrofit.Builder()
                //.client(client)
                .baseUrl(Constant.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                //.addConverterFactory(GsonConverterFactory.create(gson))
                .build()
        }

        val api:Api by lazy {
            retrofit.create(Api::class.java)
        }
    }

    fun apiClient(): Retrofit{

        val gson = GsonBuilder()
            .setLenient()
            .create()

        val interceptor = HttpLoggingInterceptor()
        interceptor.setLevel(HttpLoggingInterceptor.Level.BODY)
        val client = OkHttpClient.Builder().addInterceptor(interceptor).build()

        val retrofit = Retrofit.Builder()
            .baseUrl(Constant.BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()

        return retrofit

    }
}