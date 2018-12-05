package com.deftmove.services.di

import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.api.CustomTypeAdapter
import com.apollographql.apollo.api.CustomTypeValue
import com.deftmove.carpooling.interfaces.authentication.network.AuthenticationManager
import com.deftmove.carpooling.interfaces.authentication.services.AuthenticationService
import com.deftmove.carpooling.interfaces.di.Qualifiers
import com.deftmove.carpooling.interfaces.feed.RidesFeedService
import com.deftmove.carpooling.interfaces.invitation.InvitationService
import com.deftmove.carpooling.interfaces.notifications.service.NotificationsService
import com.deftmove.carpooling.interfaces.profile.service.ProfileService
import com.deftmove.carpooling.interfaces.ride.details.service.RideDetailsService
import com.deftmove.carpooling.interfaces.ride.service.RideService
import com.deftmove.carpooling.interfaces.service.OnboardingService
import com.deftmove.carpooling.interfaces.user.CurrentUserManager
import com.deftmove.carpooling.interfaces.user.UserStatusSyncer
import com.deftmove.carpooling.interfaces.workmanager.WorkManagerFactory
import com.deftmove.carpooling.type.CustomType
import com.deftmove.services.authentication.AuthenticationServiceImp
import com.deftmove.services.extension.toDate
import com.deftmove.services.extension.toGraphQLString
import com.deftmove.services.feed.RidesFeedServiceImp
import com.deftmove.services.invitation.InvitationServiceImp
import com.deftmove.services.network.AuthenticationInterceptor
import com.deftmove.services.network.AuthenticationManagerImp
import com.deftmove.services.notifications.NotificationsServiceImp
import com.deftmove.services.onboarding.OnboardingServiceImp
import com.deftmove.services.profile.ProfileApi
import com.deftmove.services.profile.ProfileServiceImp
import com.deftmove.services.ride.RideServiceImp
import com.deftmove.services.ride.details.RideDetailsServiceImp
import com.deftmove.services.rx.Base64ImageProcessor
import com.deftmove.services.user.AuthenticationAssurance
import com.deftmove.services.user.CurrentUserManagerImp
import com.deftmove.services.user.UserStatusSyncerImp
import com.deftmove.services.workmanager.WorkManagerFactoryImp
import org.koin.core.module.Module
import org.koin.dsl.module
import retrofit2.Retrofit
import java.text.ParseException
import java.util.Date

val servicesModule: Module = module {

    factory { AuthenticationInterceptor(get()) }

    factory<AuthenticationManager> { AuthenticationManagerImp(get()) }

    factory<ProfileApi> {
        get<Retrofit>(com.deftmove.heart.interfaces.koin.Qualifiers.noCachingApiRETROFIT).create(ProfileApi::class.java)
    }

    factory(Qualifiers.defaultApollo) {
        ApolloClient.builder()
              .serverUrl(get<String>(com.deftmove.heart.interfaces.koin.Qualifiers.baseApiUrl))
              .okHttpClient(get(com.deftmove.heart.interfaces.koin.Qualifiers.noCachingApiOKHTTP))
              .addCustomTypeAdapter(CustomType.ID, get<CustomTypeAdapter<String>>(Qualifiers.apolloCustomAdapterId))
              .addCustomTypeAdapter(
                    CustomType.DATETIME,
                    get<CustomTypeAdapter<Date>>(Qualifiers.apolloCustomAdapterDateTime)
              )
              .build()
    }

    factory(Qualifiers.apolloCustomAdapterId) {
        object : CustomTypeAdapter<String> {
            override fun decode(value: CustomTypeValue<*>): String {
                return value.value as String
            }

            override fun encode(value: String): CustomTypeValue<*> {
                return CustomTypeValue.GraphQLString(value)
            }

        }
    }

    factory(Qualifiers.apolloCustomAdapterDateTime) {
        object : CustomTypeAdapter<Date> {
            override fun decode(value: CustomTypeValue<*>): Date {
                try {
                    return (value.value as String).toDate()
                } catch (e: ParseException) {
                    throw RuntimeException(e)
                }
            }

            override fun encode(value: Date): CustomTypeValue<*> {
                return CustomTypeValue.GraphQLString(value.toGraphQLString())
            }
        }
    }

    factory<AuthenticationService> {
        AuthenticationServiceImp(
              get(Qualifiers.defaultApollo),
              get(),
              get(),
              get(),
              get(),
              get()
        )
    }



    factory { Base64ImageProcessor(get(com.deftmove.heart.interfaces.koin.Qualifiers.applicationContext)) }

    factory<OnboardingService> { OnboardingServiceImp(get(Qualifiers.defaultApollo), get(), get(), get()) }
    factory<ProfileService> {
        ProfileServiceImp(
              get(),
              get(Qualifiers.defaultApollo),
              get(),
              get(),
              get()
        )
    }

    factory<RideService> { RideServiceImp(get(Qualifiers.defaultApollo), get(), get(), get(), get(), get()) }
    factory<RideDetailsService> { RideDetailsServiceImp(get(Qualifiers.defaultApollo), get(), get(), get()) }
    factory<RidesFeedService> { RidesFeedServiceImp(get(Qualifiers.defaultApollo), get(), get()) }
    factory<InvitationService> { InvitationServiceImp(get(Qualifiers.defaultApollo), get(), get(), get()) }
    factory<NotificationsService> { NotificationsServiceImp(get(Qualifiers.defaultApollo), get(), get(), get()) }

    single<CurrentUserManager> { CurrentUserManagerImp(get()) }
    factory<UserStatusSyncer> { UserStatusSyncerImp(get(), get()) }
    single { AuthenticationAssurance(get(), get(), get()) }


    factory<WorkManagerFactory> { WorkManagerFactoryImp() }
}
