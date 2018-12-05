package com.deftmove.app

import android.app.Application
import android.view.View
import com.deftmove.app.fcm.FirebaseMessagingServiceDispatcher
import com.deftmove.authentication.di.authenticationModule
import com.deftmove.authentication.login.LoginWithMagicTokenActivity
import com.deftmove.authentication.register.RegisterActivity
import com.deftmove.carpooling.commonui.plugin.AppRegisterer
import com.deftmove.carpooling.interfaces.OpenAddressSearch
import com.deftmove.carpooling.interfaces.OpenAuthenticationMagicTokenSentDialog
import com.deftmove.carpooling.interfaces.OpenCreateReviewScreen
import com.deftmove.carpooling.interfaces.OpenCreateRideScreen
import com.deftmove.carpooling.interfaces.OpenDebugAccountSwitcherScreen
import com.deftmove.carpooling.interfaces.OpenDebugScreen
import com.deftmove.carpooling.interfaces.OpenEditProfileScreen
import com.deftmove.carpooling.interfaces.OpenLoginWithMagicTokenScreen
import com.deftmove.carpooling.interfaces.OpenNotificationsScreen
import com.deftmove.carpooling.interfaces.OpenProfilePublicScreen
import com.deftmove.carpooling.interfaces.OpenProfileScreen
import com.deftmove.carpooling.interfaces.OpenRepeatingScreen
import com.deftmove.carpooling.interfaces.OpenRideDetails
import com.deftmove.carpooling.interfaces.OpenRideFeedOrRegistrationScreen
import com.deftmove.carpooling.interfaces.OpenSignInScreen
import com.deftmove.carpooling.interfaces.OpenSplashScreen
import com.deftmove.carpooling.interfaces.OpenWayPointsScreen
import com.deftmove.carpooling.interfaces.authentication.login.LoginWithMagicTokenModel
import com.deftmove.carpooling.interfaces.common.data.BadgeIconModel
import com.deftmove.carpooling.interfaces.notifications.model.NotificationConsts
import com.deftmove.carpooling.interfaces.plugin.Registerer
import com.deftmove.carpooling.interfaces.ride.create.model.CreateRideAddWayPointModel
import com.deftmove.carpooling.interfaces.ride.create.model.CreateRideLaunchModel
import com.deftmove.carpooling.interfaces.ride.create.model.CreateRideReviewModel
import com.deftmove.carpooling.interfaces.ride.details.model.RideDetailsActivityModel
import com.deftmove.carpooling.interfaces.user.CurrentUserManager
import com.deftmove.carpooling.interfaces.user.UserStatusSyncer
import com.deftmove.debugtools.accountswitch.AccountSwitcherActivity
import com.deftmove.debugtools.di.debugModule
import com.deftmove.debugtools.matchmaker.MatchMakerActivity
import com.deftmove.heart.Heart
import com.deftmove.heart.common.event.DataEvent
import com.deftmove.heart.common.event.EventManager
import com.deftmove.heart.deeplink.HeartDeepLink
import com.deftmove.heart.errorhandler.HeartErrorHandler
import com.deftmove.heart.interfaces.common.TextUtils
import com.deftmove.heart.interfaces.dialog.DialogActivityModel
import com.deftmove.heart.interfaces.koin.Qualifiers
import com.deftmove.heart.interfaces.map.model.SearchAddressLaunchModel
import com.deftmove.heart.interfaces.navigator.ActivityLauncher
import com.deftmove.heart.interfaces.navigator.HeartNavigator
import com.deftmove.heart.maptools.HeartMap
import com.deftmove.heart.maptools.ui.HeartMapUI
import com.deftmove.heart.maptools.ui.search.SearchAddressActivity
import com.deftmove.heart.pushnotification.HeartPushNotification
import com.deftmove.home.HomeActivity
import com.deftmove.home.di.homeModule
import com.deftmove.notifications.NotificationsActivity
import com.deftmove.notifications.di.notificationsModule
import com.deftmove.onboarding.OnBoardingActivity
import com.deftmove.onboarding.di.onBoardingModule
import com.deftmove.profile.di.profileModule
import com.deftmove.profile.edit.ProfileEditActivity
import com.deftmove.profile.profile.ProfileActivity
import com.deftmove.profile.publicprofile.PublicProfileActivity
import com.deftmove.repeating.RepeatingActivity
import com.deftmove.repeating.di.repeatingModule
import com.deftmove.ride.create.CreateRideActivity
import com.deftmove.ride.details.RideDetailsActivity
import com.deftmove.ride.di.rideDetailsModule
import com.deftmove.ride.di.rideModule
import com.deftmove.ride.review.CreateRideReviewActivity
import com.deftmove.ride.waypoints.WayPointsActivity
import com.deftmove.services.di.servicesModule
import com.deftmove.services.network.AuthenticationInterceptor
import com.deftmove.services.network.HeadersInterceptor
import com.deftmove.services.user.AuthenticationAssurance
import com.deftmove.splash.SplashActivity
import com.deftmove.splash.di.splashModule
import com.miguelbcr.ui.rx_paparazzo2.RxPaparazzo
import okhttp3.logging.HttpLoggingInterceptor
import org.koin.core.KoinComponent
import org.koin.core.get
import org.koin.core.module.Module
import org.koin.dsl.module
import timber.log.Timber

class App : Application(), KoinComponent {

    private val userStatusSyncer: UserStatusSyncer by lazy { get<UserStatusSyncer>() }
    private val eventManager: EventManager by lazy { get<EventManager>() }
    private val textUtils: TextUtils by lazy { get<TextUtils>() }
    private val currentUserManager: CurrentUserManager by lazy { get<CurrentUserManager>() }
    private val heartNavigator: HeartNavigator by lazy { get<HeartNavigator>() }
    private val authenticationAssurance: AuthenticationAssurance by lazy { get<AuthenticationAssurance>() }
    private val firebaseMessagingServiceDispatcher: FirebaseMessagingServiceDispatcher by lazy { get<FirebaseMessagingServiceDispatcher>() }
    private val registerer: Registerer by lazy { get<Registerer>() }

    private val applicationModule: Module = module {
        single {
            FirebaseMessagingServiceDispatcher(
                  eventManager = get(),
                  heartNavigator = get(),
                  foregroundActivityService = get(),
                  internalPushNotificationManager = getKoin().getOrCreateScope(
                        Qualifiers.authenticatedUser.toString(),
                        Qualifiers.authenticatedUser
                  ).get(),
                  context = get(Qualifiers.applicationContext)
            )
        }

        single<Registerer> {
            AppRegisterer()
        }
    }

    override fun onCreate() {
        super.onCreate()

        Timber.plant(Timber.DebugTree())

        RxPaparazzo.register(this)

        Heart.bind(
              application = this,
              baseUrl = "https://carpool-staging.deftmove.com/api/",
              modules = listOf(
                    debugModule,
                    applicationModule,
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
              ) + BuildConfig.PLUGINS_REGISTERER.map { pluginRegisterer ->
                  pluginRegisterer.getKoinModule()
              },
              isTesting = BuildConfig.DEBUG
        )

        Heart.addOkHttpInterceptors(
              interceptors = listOf(
                    get<AuthenticationInterceptor>(),
                    HeadersInterceptor()
              ),
              debugLevel = HttpLoggingInterceptor.Level.BODY
        )

        HeartMap.bind(BuildConfig.CONFIG_Google_Map_API_KEY)
        HeartMapUI.bind(heartNavigator)

        HeartDeepLink.bind(
              isTesting = BuildConfig.DEBUG,
              dispatcher = { route ->
                  Timber.d("route:${route.route}, Value:${route.value}")
                  when (route.route) {
                      "auth" -> {
                          heartNavigator.getLauncher(
                                OpenLoginWithMagicTokenScreen(
                                      LoginWithMagicTokenModel(
                                            magicToken = route.value
                                      )
                                )
                          )?.startActivity()
                      }

                      else -> heartNavigator.getLauncher(OpenSplashScreen)?.startActivity()
                  }
              }
        )

        HeartErrorHandler.bind(
              isTesting = BuildConfig.DEBUG,
              dispatcher = { throwable ->
                  Timber.d("handle received errors from backend here")
              }
        )

        HeartPushNotification.bind(
              listOfKeys = listOf("ride_id", "conversation_id"),
              shouldReceivePushNotification = {
                  currentUserManager.isAuthenticated()
              },
              tokenRenewed = {
                  userStatusSyncer.notifyDeviceTokenChanged()
              },
              isTesting = true
        )

        eventManager.observe()
              .ofType(DataEvent.NotificationReceived::class.java)
              .doOnNext { notification ->
                  Timber.d("notification: $notification")
                  firebaseMessagingServiceDispatcher.onMessageReceived(
                        notificationKey = notification.key,
                        title = notification.title,
                        message = notification.message,
                        extraValues = notification.extraValues
                  )
              }
              .subscribe()

        authenticationAssurance.hashCode()

        registerNavigations()

        BuildConfig.PLUGINS_REGISTERER.forEach { pluginRegisterer ->
            pluginRegisterer.register(registerer, heartNavigator)
        }

        registerer.registerActionIcon(
              BadgeIconModel.BadgeIconHome(
                    order = 5,
                    badgeText = null,
                    iconResId = R.drawable.ic_notifications_black_24dp,
                    tagName = NotificationConsts.notificationIconTagHomeActionBar,
                    visibility = View.VISIBLE,
                    clickAction = {
                        heartNavigator.getLauncher(OpenNotificationsScreen)
                              ?.startActivity()
                    },
                    longClick = {
                        heartNavigator.getLauncher(OpenDebugScreen)
                              ?.startActivity()
                        true
                    }
              )
        )
    }

    private fun registerNavigations() {
        heartNavigator.registerNavigation(
              OpenSplashScreen::class.java
        ) { navigator, model ->
            ActivityLauncher.with(navigator.getActivity()).open(SplashActivity::class.java)
        }

        heartNavigator.registerNavigation(
              OpenAuthenticationMagicTokenSentDialog::class.java
        ) { navigator, model ->
            navigator.getDialogScreen(
                  DialogActivityModel(
                        imageDrawable = R.drawable.ic_inbox_registration,
                        title = textUtils.getString(
                              R.string.register_send_email_dialog_title,
                              model.emailAddress
                        ),
                        description = textUtils.getString(R.string.register_send_email_dialog_description),
                        secondDescription = textUtils.getString(R.string.register_send_email_dialog_second_description)
                  )
            )
        }

        heartNavigator.registerNavigation(
              OpenDebugScreen::class.java
        ) { navigator, model ->
            ActivityLauncher.with(navigator.getActivity()).open(MatchMakerActivity::class.java)
        }

        heartNavigator.registerNavigation(
              OpenDebugAccountSwitcherScreen::class.java
        ) { navigator, model ->
            ActivityLauncher.with(navigator.getActivity()).open(AccountSwitcherActivity::class.java)
        }

        heartNavigator.registerNavigation(
              OpenSignInScreen::class.java
        ) { navigator, model ->
            ActivityLauncher.with(navigator.getActivity())
                  .open(if (navigator.getActivity() is RegisterActivity) Unit::class.java else RegisterActivity::class.java)
                  .closeOtherActivities()
        }

        heartNavigator.registerNavigation(
              OpenRideFeedOrRegistrationScreen::class.java
        ) { navigator, model ->
            val clazz = when {
                !currentUserManager.isAuthenticated() -> RegisterActivity::class.java
                currentUserManager.isProfileComplete() -> HomeActivity::class.java
                else -> OnBoardingActivity::class.java
            }

            ActivityLauncher.with(navigator.getActivity()).open(clazz)
                  .closeOtherActivities()
        }

        heartNavigator.registerNavigation(
              OpenProfileScreen::class.java
        ) { navigator, model ->
            ActivityLauncher.with(navigator.getActivity()).open(ProfileActivity::class.java)
        }

        heartNavigator.registerNavigation(
              OpenProfilePublicScreen::class.java
        ) { navigator, model ->
            ActivityLauncher.with(navigator.getActivity()).open(PublicProfileActivity::class.java)
                  .addArgument(model.userId)
        }

        heartNavigator.registerNavigation(
              OpenEditProfileScreen::class.java
        ) { navigator, model ->
            ActivityLauncher.with(navigator.getActivity()).open(ProfileEditActivity::class.java)
        }

        heartNavigator.registerNavigation(
              OpenLoginWithMagicTokenScreen::class.java
        ) { navigator, model ->
            ActivityLauncher.with(navigator.getActivity()).open(LoginWithMagicTokenActivity::class.java)
                  .closeOtherActivities()
                  .addArgument(model.model)
        }

        heartNavigator.registerNavigation(
              OpenAddressSearch::class.java
        ) { navigator, model ->
            val launchModel = SearchAddressLaunchModel(addressTypeName = model.addressTypeName)
            ActivityLauncher.with(navigator.getActivity()).open(SearchAddressActivity::class.java).apply {
                addArgument(launchModel)
            }
        }

        heartNavigator.registerNavigation(
              OpenCreateRideScreen::class.java
        ) { navigator, model ->
            ActivityLauncher.with(navigator.getActivity()).open(CreateRideActivity::class.java)
                  .addArgument(
                        CreateRideLaunchModel(
                              role = model.role,
                              rideForUpdate = model.rideForUpdate,
                              updateRepeatingRides = model.updateRepeatingRides
                        )
                  )
        }

        heartNavigator.registerNavigation(
              OpenWayPointsScreen::class.java
        ) { navigator, model ->
            ActivityLauncher.with(navigator.getActivity()).open(WayPointsActivity::class.java)
                  .addArgument(CreateRideAddWayPointModel(model = model.model))
        }

        heartNavigator.registerNavigation(
              OpenCreateReviewScreen::class.java
        ) { navigator, model ->
            ActivityLauncher.with(navigator.getActivity()).open(CreateRideReviewActivity::class.java)
                  .addArgument(CreateRideReviewModel(model = model.model))
        }

        heartNavigator.registerNavigation(
              OpenRideDetails::class.java
        ) { navigator, model ->
            ActivityLauncher.with(navigator.getActivity()).open(RideDetailsActivity::class.java)
                  .addArgument(RideDetailsActivityModel(rideId = model.rideId))
        }

        heartNavigator.registerNavigation(
              OpenNotificationsScreen::class.java
        ) { navigator, model ->
            ActivityLauncher.with(navigator.getActivity()).open(NotificationsActivity::class.java)
        }

        heartNavigator.registerNavigation(
              OpenRepeatingScreen::class.java
        ) { navigator, model ->
            ActivityLauncher.with(navigator.getActivity()).open(RepeatingActivity::class.java)
                  .addArgument(model.repeatingRideModel)
        }

        BuildConfig.PLUGINS_REGISTERER.forEach { pluginRegisterer ->
            pluginRegisterer.registerNavigators(heartNavigator)
        }
    }
}
