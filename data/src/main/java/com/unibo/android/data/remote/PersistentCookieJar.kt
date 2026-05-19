package com.unibo.android.data.remote

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import okhttp3.Cookie
import okhttp3.CookieJar
import okhttp3.HttpUrl

internal class PersistentCookieJar(context: Context) : CookieJar {

    private val prefs = context.getSharedPreferences("okhttp_cookies", Context.MODE_PRIVATE)
    private val gson = Gson()
    private val cache = mutableMapOf<String, MutableList<Cookie>>()

    init {
        prefs.all.forEach { (host, value) ->
            if (value is String) {
                val cookies = deserialize(host, value)
                if (cookies.isNotEmpty()) cache[host] = cookies.toMutableList()
            }
        }
    }

    @Synchronized
    override fun saveFromResponse(url: HttpUrl, cookies: List<Cookie>) {
        val host = url.host
        val current = cache.getOrPut(host) { mutableListOf() }
        cookies.forEach { new ->
            current.removeAll { it.name == new.name }
            current.add(new)
        }
        prefs.edit().putString(host, serialize(current)).apply()
    }

    @Synchronized
    override fun loadForRequest(url: HttpUrl): List<Cookie> {
        val now = System.currentTimeMillis()
        return cache[url.host]?.filter { cookie ->
            !cookie.persistent || cookie.expiresAt > now
        } ?: emptyList()
    }

    @Synchronized
    fun clear() {
        cache.clear()
        prefs.edit().clear().apply()
    }

    private fun serialize(cookies: List<Cookie>): String = gson.toJson(
        cookies.map { c ->
            mapOf(
                "name"       to c.name,
                "value"      to c.value,
                "domain"     to c.domain,
                "path"       to c.path,
                "expiresAt"  to c.expiresAt.toString(),
                "secure"     to c.secure.toString(),
                "httpOnly"   to c.httpOnly.toString(),
                "persistent" to c.persistent.toString()
            )
        }
    )

    private fun deserialize(host: String, json: String): List<Cookie> = runCatching {
        val type = object : TypeToken<List<Map<String, String>>>() {}.type
        val list: List<Map<String, String>> = gson.fromJson(json, type)
        list.mapNotNull { m ->
            runCatching {
                Cookie.Builder()
                    .name(m["name"]!!)
                    .value(m["value"]!!)
                    .hostOnlyDomain(m["domain"]!!)
                    .path(m["path"] ?: "/")
                    .expiresAt(m["expiresAt"]!!.toLong())
                    .apply {
                        if (m["secure"] == "true") secure()
                        if (m["httpOnly"] == "true") httpOnly()
                    }
                    .build()
            }.getOrNull()
        }
    }.getOrDefault(emptyList())
}
