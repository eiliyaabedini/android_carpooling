package com.deftmove.services.authentication

import com.apollographql.apollo.ApolloClient
import com.deftmove.carpooling.authentication.GetCurrentCustomerQuery
import com.deftmove.carpooling.authentication.GetMagicTokenMutation
import com.deftmove.carpooling.authentication.LoginWithMagicTokenMutation
import com.deftmove.carpooling.authentication.PutMagicTokenLinkMutation
import com.deftmove.carpooling.interfaces.authentication.model.CurrentUserModel
import com.deftmove.carpooling.interfaces.authentication.model.CustomerModel
import com.deftmove.carpooling.interfaces.authentication.services.AuthenticationService
import com.deftmove.carpooling.interfaces.service.rx.AsyncApollo
import com.deftmove.carpooling.interfaces.user.CurrentUserManager
import com.deftmove.carpooling.interfaces.user.UserStatusSyncer
import com.deftmove.carpooling.profile.UpdateUserNoResponseMutation
import com.deftmove.carpooling.type.LoginWithMagicTokenInput
import com.deftmove.carpooling.type.MagicTokenInput
import com.deftmove.carpooling.type.MagicTokenLinkInput
import com.deftmove.carpooling.type.UpdateUserInput
import com.deftmove.heart.common.event.EventManager
import com.deftmove.heart.errorhandler.GenericErrorHandler
import com.deftmove.heart.interfaces.ResponseResult
import com.deftmove.heart.interfaces.common.ReactiveTransformer
import com.deftmove.heart.interfaces.common.rx.doOnData
import com.deftmove.heart.interfaces.common.rx.doOnFailure
import com.deftmove.heart.interfaces.common.rx.flatMapData
import com.deftmove.heart.interfaces.common.rx.mapData
import com.deftmove.services.extension.convert
import io.reactivex.Observable
import io.reactivex.Single

class AuthenticationServiceImp(
    private val defaultApolloClient: ApolloClient,
    private val errorHandler: GenericErrorHandler,
    private val eventManager: EventManager,
    private val currentUserManager: CurrentUserManager,
    private val userStatusSyncer: UserStatusSyncer,
    private val reactiveTransformer: ReactiveTransformer
) : AuthenticationService {

    override fun createMagicToken(email: String): Observable<ResponseResult<String>> {
        val mutation = GetMagicTokenMutation.builder()
              .input(MagicTokenInput.builder().email(email).build())
              .build()

        return AsyncApollo.from(defaultApolloClient.mutate(mutation))
              .subscribeOn(reactiveTransformer.ioScheduler())
              .mapData { it.magicToken!!.magicToken() }

    }

    override fun putMagicTokenLink(magicLink: String, magicToken: String): Observable<ResponseResult<Boolean>> {
        val mutation = PutMagicTokenLinkMutation.builder()
              .input(MagicTokenLinkInput.builder().magicLink(magicLink).magicToken(magicToken).build())
              .build()

        return AsyncApollo.from(defaultApolloClient.mutate(mutation))
              .subscribeOn(reactiveTransformer.ioScheduler())
              .mapData { it.putMagicTokenLink()!!.success() }
              .doOnFailure { errorHandler.handleThrowable(it) }
    }

    override fun loginWithMagicToken(magicToken: String): Single<ResponseResult<CurrentUserModel>> {
        val mutation = LoginWithMagicTokenMutation.builder()
              .input(LoginWithMagicTokenInput.builder().magicToken(magicToken).build())
              .build()

        return AsyncApollo.fromSingle(defaultApolloClient.mutate(mutation))
              .subscribeOn(reactiveTransformer.ioScheduler())
              .mapData { it.loginWithMagicToken()!!.convert() }
              .doOnData { response ->
                  currentUserManager.setCurrentUser(response)
                  userStatusSyncer.notifyUserLoggedIn()
              }
              .flatMapData { userModel -> getCurrentCustomer().mapData { userModel } }
              .doOnFailure { currentUserManager.clearCurrentUser() }
              .doOnFailure { errorHandler.handleThrowable(it) }
    }

    override fun getCurrentCustomer(): Single<ResponseResult<CustomerModel>> {
        val query = GetCurrentCustomerQuery.builder()
              .build()

        return AsyncApollo.fromSingle(defaultApolloClient.query(query))
              .subscribeOn(reactiveTransformer.ioScheduler())
              .mapData { it.currentCustomer()!!.convert() }
              .doOnData { customer ->
                  currentUserManager.setCustomer(customer)
              }
              .doOnFailure { errorHandler.handleThrowable(it) }
    }

    override fun logout(): Single<ResponseResult<Unit>> {
        val mutation = UpdateUserNoResponseMutation.builder()
              .input(
                    UpdateUserInput.builder()
                          .deviceToken(null)
                          .deviceType(null)
                          .build()
              )
              .build()

        return AsyncApollo.fromSingle(defaultApolloClient.mutate(mutation))
              .subscribeOn(reactiveTransformer.ioScheduler())
              .doOnData {
                  currentUserManager.clearCurrentUser()
                  userStatusSyncer.notifyUserLoggedOut()
              }
              .mapData { Unit }
              .doOnFailure { errorHandler.handleThrowable(it) }
    }
}
