package org.cryptimeleon.incentive.app.data.network

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.Interceptor
import okhttp3.Response
import org.cryptimeleon.incentive.app.data.StoreSelectionRepository

/**
 * Since we do not know the store at which the user wants to send requests, we send them to some
 * dummy url first. This interceptor changes the dummy url to a url valid for the store selected by
 * the user.
 */
class StoreInterceptor(private val storeSelectionRepository: StoreSelectionRepository) :
    Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        // Get current store path
        val storeUrl =
            runBlocking { storeSelectionRepository.currentStore.first().url }.toHttpUrl()

        var request = chain.request()
        val url = request.url.newBuilder()
            .host(storeUrl.host)
            .port(storeUrl.port)
            .scheme(storeUrl.scheme)
            .encodedPath(storeUrl.encodedPath)
            .addEncodedPathSegments(request.url.encodedPath)
            .build()
        request = request.newBuilder()
            .url(url)
            .build()
        return chain.proceed(request)
    }
}