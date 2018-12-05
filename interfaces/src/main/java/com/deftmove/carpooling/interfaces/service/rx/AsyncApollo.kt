package com.deftmove.carpooling.interfaces.service.rx

import com.apollographql.apollo.ApolloCall
import com.apollographql.apollo.rx2.Rx2Apollo
import com.deftmove.heart.interfaces.ResponseResult
import io.reactivex.Observable
import io.reactivex.Single

sealed class AsyncApollo {
    companion object {
        fun <T> from(call: ApolloCall<T>): Observable<ResponseResult<T>> {
            return Rx2Apollo.from(call)
                  .responseToResponseResult()
        }

        fun <T> fromSingle(call: ApolloCall<T>): Single<ResponseResult<T>> {
            return Rx2Apollo.from(call)
                  .singleOrError()
                  .responseToResponseResult()
        }
    }
}
