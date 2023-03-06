package org.cryptimeleon.incentive.app.data.network

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response
import org.cryptimeleon.incentive.app.data.StoreSelectionRepository

class StoreInterceptor(private val storeSelectionRepository: StoreSelectionRepository) :
    Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        // Get current store path
        val storePath =
            runBlocking { storeSelectionRepository.currentStore.first().firstPathSegment }

        var request = chain.request()
        val url = request.url.newBuilder()
            .setPathSegment(0, storePath)
            .build()
        request = request.newBuilder()
            .url(url)
            .build()
        return chain.proceed(request)
    }
}