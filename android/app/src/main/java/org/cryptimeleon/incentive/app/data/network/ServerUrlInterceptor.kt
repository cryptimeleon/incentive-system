package org.cryptimeleon.incentive.app.data.network

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.Interceptor
import okhttp3.Response
import org.cryptimeleon.incentive.app.domain.IPreferencesRepository

class ServerUrlInterceptor(private val preferencesRepository: IPreferencesRepository) :
    Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val serverUrl =
            runBlocking {
                val url = preferencesRepository.serverUrl.first()
                url
            }.toHttpUrl()

        var request = chain.request()
        val url = request.url.newBuilder()
            .host(serverUrl.host)
            .port(serverUrl.port)
            .scheme(serverUrl.scheme)
            .build()
        request = request.newBuilder()
            .url(url)
            .build()
        return chain.proceed(request)
    }
}
