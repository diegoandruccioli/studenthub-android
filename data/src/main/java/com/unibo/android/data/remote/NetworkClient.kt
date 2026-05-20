package com.unibo.android.data.remote

import android.content.Context
import android.util.Log
import com.unibo.android.data.local.SessionDataStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.Authenticator
import okhttp3.CookieJar
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

    private var appContext: Context? = null
    private var sessionDataStore: SessionDataStore? = null

    fun init(context: Context, sessionDataStore: SessionDataStore) {
        appContext = context.applicationContext
        this.sessionDataStore = sessionDataStore
    }

    internal val cookieJar: PersistentCookieJar by lazy {
        PersistentCookieJar(appContext ?: error("NetworkClient.init() non chiamato"))
    }

    fun clearCookies() = cookieJar.clear()

    val okHttpClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .cookieJar(cookieJar)
            .addInterceptor(
                HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BODY }
            )
            .authenticator(TokenAuthenticator(cookieJar, BASE_URL) {
                CoroutineScope(Dispatchers.IO).launch { sessionDataStore?.setLoggedIn(false) }
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
    val gamificationApiService: GamificationApiService by lazy { retrofit.create(GamificationApiService::class.java) }
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
            Log.d("NetworkClient", "Token scaduto. Tentativo di refresh via cookie...")
            refreshClient.newCall(refreshRequest).execute()
        }.getOrNull()

        return if (refreshResponse?.isSuccessful == true) {
            Log.d("NetworkClient", "Refresh riuscito. Riesecuzione chiamata originale...")
            // OkHttp re-inietta i cookie aggiornati dal CookieJar nella nuova istanza della request
            response.request
        } else {
            Log.e("NetworkClient", "Refresh fallito (Sessione scaduta). Logout in corso.")
            onRefreshFailed()
            null
        }
    }
}
