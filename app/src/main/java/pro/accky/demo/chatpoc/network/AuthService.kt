package pro.accky.demo.chatpoc.network

import com.squareup.moshi.Moshi
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST

inline fun <reified T> Retrofit.create(): T = create(T::class.java)

object AuthService : IAuthService by IAuthService.service

data class FirebaseToken(
    val token: String
)

interface IAuthService {
    companion object {
        private const val url = "http://www.chatPOC-dev.us-west-2.elasticbeanstalk.com/"

        private fun create(): IAuthService {
            val moshi = Moshi.Builder().build()
            val converterFactory = MoshiConverterFactory.create(moshi)
            val interceptor = HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BASIC)
            val okHttpClient = OkHttpClient.Builder().addInterceptor(interceptor).build()

            return Retrofit.Builder()
                .baseUrl(url)
                .addConverterFactory(converterFactory)
                .client(okHttpClient)
                .build()
                .create()
        }

        val service by lazy { create() }
    }

    @FormUrlEncoded
    @POST("auth")
    fun getAuthToken(@Field("user_id") user_id: String): Call<FirebaseToken> // "user_id=3"
}