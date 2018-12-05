package com.deftmove.home

import com.deftmove.carpooling.commonui.plugin.updateActionIcon
import com.deftmove.carpooling.interfaces.OpenCreateRideScreen
import com.deftmove.carpooling.interfaces.OpenProfilePublicScreen
import com.deftmove.carpooling.interfaces.OpenProfileScreen
import com.deftmove.carpooling.interfaces.OpenRideDetails
import com.deftmove.carpooling.interfaces.common.data.BadgeIconModel
import com.deftmove.carpooling.interfaces.notifications.model.NotificationConsts
import com.deftmove.carpooling.interfaces.plugin.Registerer
import com.deftmove.carpooling.interfaces.profile.service.ProfileService
import com.deftmove.carpooling.interfaces.pushnotification.model.PushNotificationEvents
import com.deftmove.carpooling.interfaces.repository.RideFeedApiPagingRepository
import com.deftmove.carpooling.interfaces.ride.model.RideForFeed
import com.deftmove.carpooling.interfaces.ride.service.RideService
import com.deftmove.carpooling.interfaces.user.CurrentUserManager
import com.deftmove.heart.common.event.DataEvent
import com.deftmove.heart.common.event.EventManager
import com.deftmove.heart.common.presenter.Presenter
import com.deftmove.heart.interfaces.common.ReactiveTransformer
import com.deftmove.heart.interfaces.common.presenter.PresenterAction
import com.deftmove.heart.interfaces.common.presenter.PresenterCommonAction
import com.deftmove.heart.interfaces.common.presenter.PresenterView
import com.deftmove.heart.interfaces.common.rx.doOnData
import com.deftmove.heart.interfaces.navigator.HeartNavigator
import timber.log.Timber

class HomePresenter(
    private val currentUserManager: CurrentUserManager,
    private val profileService: ProfileService,
    private val rideRepository: RideFeedApiPagingRepository,
    private val rideService: RideService,
    private val eventManager: EventManager,
    private val heartNavigator: HeartNavigator,
    private val registerer: Registerer,
    private val reactiveTransformer: ReactiveTransformer
) : Presenter<HomePresenter.View>() {

    override fun initialise() {

        commonView?.showContentLoading()

        currentUserManager.changes()
              .subscribeOn(reactiveTransformer.ioScheduler())
              .subscribeSafeWithShowingErrorContent { currentUser ->
                  currentUser.user?.avatarUrl?.let {
                      view?.showUserAvatarInToolbar(it)
                  }
              }

        //Update user model in background
        profileService.getCurrentUser()
              .subscribeOn(reactiveTransformer.ioScheduler())
              .subscribeSafeWithShowingErrorContent()

        commonView?.actions()
              ?.subscribeSafeWithShowingErrorContent { action ->
                  Timber.d("action: $action")
                  when (action) {
                      is PresenterCommonAction.ErrorRetryClicked -> {
                          commonView?.showContent()
                      }

                      is Action.LoadMoreRequested -> {
                          view?.showLoadMoreIndicator()
                      }

                      is Action.ToolbarAvatarClicked -> {
                          heartNavigator.getLauncher(OpenProfileScreen)
                                ?.startActivity()
                      }

                      is Action.MenuEditClicked -> {
                          heartNavigator.getLauncher(
                                OpenCreateRideScreen(
                                      role = action.rideForFeed.role,
                                      rideForUpdate = action.rideForFeed,
                                      updateRepeatingRides = action.updateRepeatingRides
                                )
                          )?.startActivity()
                      }

                      is Action.MenuCancelClicked -> {
                          cancelRideWithId(action.rideId, false)
                      }

                      is Action.MenuCancelAllRepeatingClicked -> {
                          cancelRideWithId(action.rideId, true)
                      }

                      is Action.DriverContactClicked -> {
                          action.driverPhoneNumber?.let { commonView?.callPhoneNumber(it) }
                      }

                      is Action.DriverAvatarClicked -> {
                          heartNavigator.getLauncher(
                                OpenProfilePublicScreen(action.userId)
                          )?.startActivity()
                      }

                      is Action.RideCardClicked -> {
                          heartNavigator.getLauncher(OpenRideDetails(action.rideId))
                                ?.startActivity()
                      }

                      is Action.RideCardInfoIconClicked -> {
                          view?.showInfoBottomSheet()
                      }

                      is Action.RideCardInfoBottomSheetActionButtonClicked -> {
                          view?.hideInfoBottomSheet()
                      }
                  }
              }

        commonView?.actions()
              ?.startWith(Action.RefreshRequested)
              ?.ofType(Action.RefreshRequested::class.java)
              ?.doOnNext { rideRepository.fetch() }
              ?.doOnError {
                  view?.hideSwipeToRefresh()
              }
              ?.subscribeSafeWithShowingErrorContent()

        eventManager.observe()
              .filter { dataEvent ->
                  dataEvent is PushNotificationEvents.InvitationCancelled ||
                        dataEvent is PushNotificationEvents.InvitationConfirmed ||
                        dataEvent is PushNotificationEvents.InvitationCreated ||
                        dataEvent is PushNotificationEvents.RideCreated ||
                        dataEvent is PushNotificationEvents.RideUpdated
              }
              .doOnNext { rideRepository.fetch() }
              .subscribeSafeWithShowingErrorContent()

        eventManager.observe()
              .ofType(DataEvent.UnreadNotificationsCountChanged::class.java)
              .doOnNext { unreadNotificationsCountChanged ->
                  changeNotificationBadgeVisibility(unreadNotificationsCountChanged.count)
              }
              .subscribeSafeWithShowingErrorContent()
    }

    private fun changeNotificationBadgeVisibility(count: Int) {
        registerer.updateActionIcon<BadgeIconModel.BadgeIconHome>(
              NotificationConsts.notificationIconTagHomeActionBar
        ) { model ->
            model.copy(badgeText = if (count >= 1) count.toString() else null)
        }
    }

    override fun viewIsVisible() {
        super.viewIsVisible()

        rideRepository.observe()
              .map { rideRepository.getAllItems() }
              .doOnNext { rides ->
                  updateList(rides)
              }
              .subscribeSafeWithShowingErrorContent()

        //Make sure that we get the latest version each time we back to ride feed
        rideRepository.fetch()
    }

    private fun cancelRideWithId(rideId: String, cancelRepeatingRides: Boolean) {
        rideService.cancelRide(rideId, cancelRepeatingRides)
              .doOnSubscribe { commonView?.showContentLoading() }
              .subscribeOn(reactiveTransformer.ioScheduler())
              .doOnData { cancelledRideId ->
                  commonView?.showContent()

                  rideRepository.removeByRideId(cancelledRideId)
              }
              .subscribeSafeResponseWithShowingErrorContent()
    }

    private fun updateList(rides: List<RideForFeed>) {
        view?.hideSwipeToRefresh()
        commonView?.showContent()

        if (rides.isEmpty()) {
            currentUserManager.getUserModel().let {
                view?.showEmpty(firstName = it?.firstName ?: "")
            }
        } else {
            view?.hideEmpty()
        }

        view?.showItems(rides)
    }

    interface View : PresenterView {
        fun showLoadMoreIndicator()
        fun showSwipeToRefresh()
        fun hideSwipeToRefresh()

        fun showEmpty(firstName: String)
        fun hideEmpty()
        fun showItems(items: List<RideForFeed>)

        fun showUserAvatarInToolbar(imageUrl: String)
        fun showInfoBottomSheet()
        fun hideInfoBottomSheet()
    }

    sealed class Action : PresenterAction {
        object RefreshRequested : Action()
        object LoadMoreRequested : Action()
        object ToolbarAvatarClicked : Action()
        data class MenuEditClicked(val rideForFeed: RideForFeed, val updateRepeatingRides: Boolean) : Action()
        data class MenuCancelClicked(val rideId: String) : Action()
        data class MenuCancelAllRepeatingClicked(val rideId: String) : Action()
        data class DriverContactClicked(val driverPhoneNumber: String?) : Action()
        data class DriverAvatarClicked(val userId: String) : Action()
        data class RideCardClicked(val rideId: String) : Action()
        object RideCardInfoIconClicked : Action()
        object RideCardInfoBottomSheetActionButtonClicked : Action()
    }
}
