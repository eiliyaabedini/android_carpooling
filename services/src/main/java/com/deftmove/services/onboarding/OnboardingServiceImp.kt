package com.deftmove.services.onboarding

import com.apollographql.apollo.ApolloClient
import com.deftmove.carpooling.interfaces.authentication.model.UserGender
import com.deftmove.carpooling.interfaces.profile.model.UserProfileModel
import com.deftmove.carpooling.interfaces.service.OnboardingService
import com.deftmove.carpooling.interfaces.service.rx.AsyncApollo
import com.deftmove.carpooling.interfaces.user.CurrentUserManager
import com.deftmove.carpooling.onboarding.OnBoardUserMutation
import com.deftmove.carpooling.type.OnboardUserInput
import com.deftmove.heart.errorhandler.GenericErrorHandler
import com.deftmove.heart.interfaces.ResponseResult
import com.deftmove.heart.interfaces.common.ReactiveTransformer
import com.deftmove.heart.interfaces.common.rx.doOnData
import com.deftmove.heart.interfaces.common.rx.doOnFailure
import com.deftmove.heart.interfaces.common.rx.mapData
import com.deftmove.services.extension.convert
import com.deftmove.services.extension.toGraphQLGender
import io.reactivex.Observable

class OnboardingServiceImp(
    private val defaultApolloClient: ApolloClient,
    private val userManager: CurrentUserManager,
    private val errorHandler: GenericErrorHandler,
    private val reactiveTransformer: ReactiveTransformer
) : OnboardingService {

    override fun onboardUser(
        firstname: String,
        lastname: String,
        gender: UserGender,
        aboutMe: String?,
        phoneNumber: String
    ): Observable<ResponseResult<UserProfileModel>> {
        val mutation = OnBoardUserMutation.builder()
              .input(
                    OnboardUserInput.builder()
                          .firstname(firstname)
                          .lastname(lastname)
                          .gender(gender.toGraphQLGender())
                          .aboutMe(aboutMe)
                          .phoneNumber(phoneNumber)
                          .build()
              )
              .build()

        return AsyncApollo.from(defaultApolloClient.mutate(mutation))
              .subscribeOn(reactiveTransformer.ioScheduler())
              .mapData { it.onboardUser()!!.convert() }
              .doOnData { userManager.setUser(it.userModel) }
              .doOnFailure { errorHandler.handleThrowable(it) }
    }
}
