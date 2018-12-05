package com.deftmove.app

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.api.CustomTypeAdapter
import com.deftmove.authentication.di.authenticationModule
import com.deftmove.authentication.firebase.FirebaseLinkCreator
import com.deftmove.authentication.login.LoginWithMagicTokenActivity
import com.deftmove.authentication.login.LoginWithMagicTokenPresenter
import com.deftmove.authentication.register.RegisterActivity
import com.deftmove.authentication.register.RegisterPresenter
import com.deftmove.carpooling.interfaces.authentication.network.AuthenticationManager
import com.deftmove.carpooling.interfaces.authentication.services.AuthenticationService
import com.deftmove.carpooling.interfaces.feed.RidesFeedService
import com.deftmove.carpooling.interfaces.invitation.InvitationService
import com.deftmove.carpooling.interfaces.notifications.repository.NotificationsApiPagingRepository
import com.deftmove.carpooling.interfaces.notifications.service.NotificationsService
import com.deftmove.carpooling.interfaces.plugin.Registerer
import com.deftmove.carpooling.interfaces.profile.service.ProfileService
import com.deftmove.carpooling.interfaces.repository.RideFeedApiPagingRepository
import com.deftmove.carpooling.interfaces.ride.details.service.RideDetailsService
import com.deftmove.carpooling.interfaces.ride.repository.WaypointsUpdateRepository
import com.deftmove.carpooling.interfaces.ride.service.RideService
import com.deftmove.carpooling.interfaces.service.OnboardingService
import com.deftmove.carpooling.interfaces.user.CurrentUserManager
import com.deftmove.carpooling.interfaces.user.UserStatusSyncer
import com.deftmove.carpooling.interfaces.workmanager.WorkManagerFactory
import com.deftmove.debugtools.accountswitch.AccountSwitcherPresenter
import com.deftmove.debugtools.di.debugModule
import com.deftmove.debugtools.matchmaker.MatchMakerActivity
import com.deftmove.debugtools.matchmaker.MatchMakerPresenter
import com.deftmove.heart.Heart
import com.deftmove.heart.common.presenter.Presenter
import com.deftmove.heart.errorhandler.HeartErrorHandler
import com.deftmove.heart.interfaces.common.ReactiveTransformer
import com.deftmove.heart.interfaces.koin.Qualifiers
import com.deftmove.heart.interfaces.navigator.HeartNavigator
import com.deftmove.heart.maptools.HeartMap
import com.deftmove.heart.testhelper.TestReactiveTransformer
import com.deftmove.home.HomeActivity
import com.deftmove.home.HomePresenter
import com.deftmove.home.delegate.ShowLicenceDelegate
import com.deftmove.home.di.homeModule
import com.deftmove.notifications.NotificationsActivity
import com.deftmove.notifications.NotificationsPresenter
import com.deftmove.notifications.di.notificationsModule
import com.deftmove.onboarding.OnBoardingActivity
import com.deftmove.onboarding.OnBoardingPresenter
import com.deftmove.onboarding.di.onBoardingModule
import com.deftmove.profile.di.profileModule
import com.deftmove.profile.edit.ProfileEditActivity
import com.deftmove.profile.edit.ProfileEditPresenter
import com.deftmove.profile.profile.ProfileActivity
import com.deftmove.profile.profile.ProfilePresenter
import com.deftmove.profile.publicprofile.PublicProfileActivity
import com.deftmove.profile.publicprofile.PublicProfilePresenter
import com.deftmove.repeating.RepeatingActivity
import com.deftmove.repeating.RepeatingActivityPresenter
import com.deftmove.repeating.di.repeatingModule
import com.deftmove.ride.create.CreateRideActivity
import com.deftmove.ride.create.CreateRidePresenter
import com.deftmove.ride.details.RideDetailsActivity
import com.deftmove.ride.details.RideDetailsPresenter
import com.deftmove.ride.di.rideDetailsModule
import com.deftmove.ride.di.rideModule
import com.deftmove.ride.review.CreateRideReviewActivity
import com.deftmove.ride.review.CreateRideReviewPresenter
import com.deftmove.ride.waypoints.WayPointsActivity
import com.deftmove.ride.waypoints.WayPointsActivityPresenter
import com.deftmove.services.di.servicesModule
import com.deftmove.services.network.AuthenticationInterceptor
import com.deftmove.services.profile.ProfileApi
import com.deftmove.services.rx.Base64ImageProcessor
import com.deftmove.services.user.AuthenticationAssurance
import com.deftmove.splash.SplashActivity
import com.deftmove.splash.SplashPresenter
import com.deftmove.splash.di.splashModule
import com.google.gson.Gson
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.core.qualifier.Qualifier
import org.koin.core.qualifier.named
import org.koin.test.KoinTest
import org.koin.test.get
import org.koin.test.inject
import org.koin.test.mock.declare
import org.koin.test.mock.declareMock
import java.util.Date

class KoinModulesTest : KoinTest {

    private val mockApplication: Application = mock {
        on { applicationContext } doReturn mock()
    }

    @Before
    fun setUp() {
        startKoin {
            modules(
                  listOf(
                        debugModule,
                        splashModule,
                        profileModule,
                        servicesModule,
                        homeModule,
                        authenticationModule,
                        onBoardingModule,
                        rideModule,
                        rideDetailsModule,
                        notificationsModule,
                        repeatingModule
                  )
            )
        }

        HeartErrorHandler.bind(
              isTesting = true,
              dispatcher = {}
        )

        Heart.bind(
              application = mockApplication,
              baseUrl = "http://www.google.com/",
              isTesting = true
        )
        HeartMap.bind("GOOGLE_API_KEY")

        declare { factory<ReactiveTransformer> { TestReactiveTransformer() } }
        declare { factory<Registerer> { mock() } }

        declareMock<HeartNavigator>()
        declareMock<SharedPreferences>()
        //        declareMock<PlacesClient>()
        declareMock<Gson>()
    }

    @After
    fun after() {
        stopKoin()
    }

    @Test
    fun testApplicationModule() {
        testInjection<Context>(true, Qualifiers.applicationContext)
        testInjection<Application>(true, Qualifiers.applicationInstance)
    }

    @Test
    fun testDebugModule() {
        testScopedInjection<MatchMakerActivity, Presenter<MatchMakerPresenter.View>>()
        testScopedInjection<MatchMakerActivity, Presenter<AccountSwitcherPresenter.View>>()
    }

    @Test
    fun testSplashModule() {
        testScopedInjection<SplashActivity, Presenter<SplashPresenter.View>>()
    }

    @Test
    fun testProfileModule() {
        testScopedInjection<ProfileActivity, Presenter<ProfilePresenter.View>>()
        testScopedInjection<ProfileEditActivity, Presenter<ProfileEditPresenter.View>>()
        testScopedInjection<PublicProfileActivity, Presenter<PublicProfilePresenter.View>>()
    }

    @Test
    fun testServicesModule() {
        testInjection<String>(true, Qualifiers.baseApiUrl)
        testInjection<AuthenticationManager>(false)
        testInjection<AuthenticationInterceptor>(false)
        testInjection<ProfileApi>(false)
        testInjection<ApolloClient>(false, com.deftmove.carpooling.interfaces.di.Qualifiers.defaultApollo)
        testInjection<CustomTypeAdapter<String>>(
              false,
              com.deftmove.carpooling.interfaces.di.Qualifiers.apolloCustomAdapterId
        )
        testInjection<CustomTypeAdapter<Date>>(
              false,
              com.deftmove.carpooling.interfaces.di.Qualifiers.apolloCustomAdapterDateTime
        )
        testInjection<Base64ImageProcessor>(false)
        testInjection<OnboardingService>(false)
        testInjection<ProfileService>(false)
        testInjection<RideService>(false)
        testInjection<RideDetailsService>(false)
        testInjection<RidesFeedService>(false)
        testInjection<InvitationService>(false)
        testInjection<NotificationsService>(false)
        testInjection<CurrentUserManager>(true)
        testInjection<UserStatusSyncer>(false)
        testInjection<AuthenticationAssurance>(true)
        testInjection<AuthenticationService>(false)
        testInjection<WorkManagerFactory>(false)
    }

    @Test
    fun testHomeModule() {
        testScopedInjection<HomeActivity, Presenter<HomePresenter.View>>()
        testScopedInjection<HomeActivity, ShowLicenceDelegate>()
        testScopedInjection<RideFeedApiPagingRepository>(Qualifiers.authenticatedUser)
    }

    @Test
    fun testAuthenticationModule() {
        testScopedInjection<RegisterActivity, Presenter<RegisterPresenter.View>>()
        testScopedInjection<LoginWithMagicTokenActivity, Presenter<LoginWithMagicTokenPresenter.View>>()
        testInjection<FirebaseLinkCreator>(false)
    }

    @Test
    fun testOnBoardingModule() {
        testScopedInjection<OnBoardingActivity, Presenter<OnBoardingPresenter.View>>()
    }

    @Test
    fun testRideModule() {
        testScopedInjection<CreateRideActivity, Presenter<CreateRidePresenter.View>>()
        testScopedInjection<WayPointsActivity, Presenter<WayPointsActivityPresenter.View>>()
        testScopedInjection<CreateRideReviewActivity, Presenter<CreateRideReviewPresenter.View>>()
        testInjection<WaypointsUpdateRepository>(true)
    }

    @Test
    fun testRideDetailsModule() {
        testScopedInjection<RideDetailsActivity, Presenter<RideDetailsPresenter.View>>()
    }

    @Test
    fun testNotificationsModule() {
        testScopedInjection<NotificationsActivity, Presenter<NotificationsPresenter.View>>()
        testScopedInjection<NotificationsApiPagingRepository>(Qualifiers.authenticatedUser)
    }

    @Test
    fun testRepeatingModule() {
        testScopedInjection<RepeatingActivity, Presenter<RepeatingActivityPresenter.View>>()
    }

    private inline fun <reified T> testInjection(
        isSingleton: Boolean,
        qualifier: Qualifier? = null
    ) {
        val injectedClassInstance: T by if (qualifier != null) inject<T>(qualifier) else inject()
        val getInjectedClassInstance: T = if (qualifier != null) get(qualifier) else get()

        assertNotNull(injectedClassInstance)

        if (isSingleton) {
            assertEquals(injectedClassInstance, getInjectedClassInstance)
        } else {
            assertNotEquals(injectedClassInstance, getInjectedClassInstance)
        }
    }

    private inline fun <reified S, reified T> testScopedInjection() {
        val injectedClassInstance: T by getKoin().getOrCreateScope(named<S>().toString(), named<S>()).inject()
        val getInjectedClassInstance: T = getKoin().getOrCreateScope(named<S>().toString(), named<S>()).get()

        assertNotNull(injectedClassInstance)

        assertEquals(injectedClassInstance, getInjectedClassInstance)
    }

    private inline fun <reified T> testScopedInjection(qualifier: Qualifier) {
        val injectedClassInstance: T by getKoin().getOrCreateScope(qualifier.toString(), qualifier).inject()
        val getInjectedClassInstance: T = getKoin().getOrCreateScope(qualifier.toString(), qualifier).get()

        assertNotNull(injectedClassInstance)

        assertEquals(injectedClassInstance, getInjectedClassInstance)
    }
}
