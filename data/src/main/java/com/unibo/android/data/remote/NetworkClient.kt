package com.unibo.android.data.remote

import com.unibo.android.data.local.SessionDataStore
import kotlinx.coroutines.runBlocking
import okhttp3.Authenticator
import okhttp3.Cookie
import okhttp3.CookieJar
import okhttp3.HttpUrl
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import okhttp3.Route
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object NetworkClient {

    private const val BASE_URL = "http://10.0.2.2:3010/api/"

    internal val cookieJar: CookieJar = object : CookieJar {
        private val store = mutableMapOf<String, List<Cookie>>()

        override fun saveFromResponse(url: HttpUrl, cookies: List<Cookie>) {
            store[url.host] = cookies
        }

        override fun loadForRequest(url: HttpUrl): List<Cookie> =
            store[url.host] ?: emptyList()
    }

    private var sessionDataStore: SessionDataStore? = null

    fun init(sessionDataStore: SessionDataStore) {
        this.sessionDataStore = sessionDataStore
    }

    val okHttpClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .cookieJar(cookieJar)
            .addInterceptor(
                HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BODY }
            )
            .authenticator(TokenAuthenticator(cookieJar, BASE_URL) {
                runBlocking { sessionDataStore?.setLoggedIn(false) }
            })
            .build()
    }

    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    val authApiService: AuthApiService by lazy { retrofit.create(AuthApiService::class.java) }
    val examApiService: ExamApiService by lazy { retrofit.create(ExamApiService::class.java) }
    val settingsApiService: SettingsApiService by lazy { retrofit.create(SettingsApiService::class.java) }
}

private class TokenAuthenticator(
    private val cookieJar: CookieJar,
    private val baseUrl: String,
    private val onRefreshFailed: () -> Unit
) : Authenticator {

    private val refreshClient: OkHttpClient by lazy {
        OkHttpClient.Builder().cookieJar(cookieJar).build()
    }

    override fun authenticate(route: Route?, response: Response): Request? {
        if (response.request.url.encodedPath.endsWith("/auth/refresh")) return null

        val refreshRequest = Request.Builder()
            .url("${baseUrl}auth/refresh")
            .post("".toRequestBody())
            .build()

        val refreshResponse = runCatching {
            refreshClient.newCall(refreshRequest).execute()
        }.getOrNull()

        return if (refreshResponse?.isSuccessful == true) {
            response.request
        } else {
            onRefreshFailed()
            null
        }
    }
}
