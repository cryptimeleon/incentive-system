package org.cryptimeleon.incentive.app.util

sealed class SLE<T>(
    val data: T? = null,
    val message: String? = null
) {
    class Success<T>(data: T) : SLE<T>(data)
    class Loading<T> : SLE<T>()
    class Error<T>(message: String? = null) : SLE<T>(message = message)
}