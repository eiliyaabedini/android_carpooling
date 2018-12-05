package com.deftmove.carpooling.interfaces.service.rx

import com.apollographql.apollo.api.Error
import com.apollographql.apollo.api.Response
import com.apollographql.apollo.exception.ApolloHttpException
import com.deftmove.carpooling.interfaces.errors.BackendError
import com.deftmove.carpooling.interfaces.errors.BackendException
import com.deftmove.carpooling.interfaces.service.network.CacheMissException
import com.deftmove.heart.common.extension.logError
import com.deftmove.heart.interfaces.ResponseResult
import io.reactivex.Observable
import io.reactivex.Single
import timber.log.Timber

fun <T> Observable<Response<T>>.responseToResponseResult(): Observable<ResponseResult<T>> {
    return map { response ->
        handleResponse(response)
    }.onErrorReturn { throwable ->
        handleErrors(throwable)
    }
}

fun <T> Single<Response<T>>.responseToResponseResult(): Single<ResponseResult<T>> {
    return map { response ->
        handleResponse(response)
    }.onErrorReturn { throwable ->
        handleErrors(throwable)
    }
}

private fun <T> handleErrors(throwable: Throwable): ResponseResult.Failure<T> {
    // It might happen because of deprecated GraphQL schema.
    (throwable as? ApolloHttpException)?.let {
        if (it.code() == APOLLO_EXCEPTION_ERROR_CODE) {
            logError(it)
        }
    }

    return ResponseResult.Failure(throwable)
}

private fun <T> handleResponse(response: Response<T>): ResponseResult<T> {
    val errors = response.errors()
    val data = response.data()
    val fromCache = response.fromCache()

    Timber.i("request: %s", response.operation().name().name())

    return when {
        errors.isNotEmpty() -> {
            parseError(response.errors())
        }

        data != null -> ResponseResult.Success(data, response.fromCache())

        fromCache -> {
            // Data is null and response is from cache. This is a cache miss
            ResponseResult.Failure(CacheMissException(response.operation()))
        }

        else -> {
            // Data and errors are null but response is from network.
            // This shouldn't happen
            val exception = RuntimeException("data and errors were empty")
            logError(exception)
            ResponseResult.Failure(exception)
        }
    }
}

private fun <T> parseError(errors: List<Error>): ResponseResult.Failure<T> {
    return ResponseResult.Failure(BackendException(
          errors.map {
              BackendError(
                    message = it.message(),
                    type = it.customAttributes()["type"].toString(),
                    errorCode = it.customAttributes()["errorCode"].toString()
              )
          }
    ))
}

private const val APOLLO_EXCEPTION_ERROR_CODE: Int = 400
