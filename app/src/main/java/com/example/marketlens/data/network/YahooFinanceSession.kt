package com.example.marketlens.data.network

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import okhttp3.Cookie
import okhttp3.CookieJar
import okhttp3.HttpUrl
import okhttp3.OkHttpClient
import okhttp3.Request

interface YahooCrumbProvider {
    suspend fun getCrumb(forceRefresh: Boolean = false): String?
    fun invalidate()
}

class YahooFinanceSession(
    private val client: OkHttpClient,
    private val cookieJar: InMemoryCookieJar
) : YahooCrumbProvider {

    private val mutex = Mutex()

    @Volatile
    private var cachedCrumb: String? = null

    @Volatile
    private var cachedAtMs: Long = 0L

    override suspend fun getCrumb(forceRefresh: Boolean): String? = mutex.withLock {
        if (forceRefresh) clearSession()

        val now = System.currentTimeMillis()
        cachedCrumb
            ?.takeIf { now - cachedAtMs < CRUMB_TTL_MS }
            ?.let { return@withLock it }

        withContext(Dispatchers.IO) {
            runCatching {
                client.newCall(
                    Request.Builder()
                        .url(COOKIE_BOOTSTRAP_URL)
                        .get()
                        .build()
                ).execute().use { /* The cookie is useful even when this endpoint returns 404. */ }

                client.newCall(
                    Request.Builder()
                        .url(CRUMB_URL)
                        .get()
                        .build()
                ).execute().use { response ->
                    if (!response.isSuccessful) return@use null

                    response.body?.string()
                        ?.trim()
                        ?.takeIf(::isValidCrumb)
                        ?.also {
                            cachedCrumb = it
                            cachedAtMs = System.currentTimeMillis()
                        }
                }
            }.getOrNull()
        }
    }

    override fun invalidate() {
        clearSession()
    }

    private fun clearSession() {
        cachedCrumb = null
        cachedAtMs = 0L
        cookieJar.clear()
    }

    private fun isValidCrumb(value: String): Boolean {
        if (value.isBlank() || value.length > 256) return false
        val lower = value.lowercase()
        return !lower.contains("<html") && !lower.contains("too many requests")
    }

    private companion object {
        const val CRUMB_TTL_MS = 30 * 60 * 1000L
        const val COOKIE_BOOTSTRAP_URL = "https://fc.yahoo.com/"
        const val CRUMB_URL = "https://query1.finance.yahoo.com/v1/test/getcrumb"
    }
}

class InMemoryCookieJar : CookieJar {

    private val cookies = mutableListOf<Cookie>()

    @Synchronized
    override fun saveFromResponse(url: HttpUrl, cookies: List<Cookie>) {
        val now = System.currentTimeMillis()
        this.cookies.removeAll { stored ->
            stored.expiresAt <= now || cookies.any { incoming -> incoming.sameIdentityAs(stored) }
        }
        this.cookies += cookies.filter { it.expiresAt > now }
    }

    @Synchronized
    override fun loadForRequest(url: HttpUrl): List<Cookie> {
        val now = System.currentTimeMillis()
        cookies.removeAll { it.expiresAt <= now }
        return cookies.filter { it.matches(url) }
    }

    @Synchronized
    fun clear() {
        cookies.clear()
    }

    private fun Cookie.sameIdentityAs(other: Cookie): Boolean =
        name == other.name && domain == other.domain && path == other.path
}
