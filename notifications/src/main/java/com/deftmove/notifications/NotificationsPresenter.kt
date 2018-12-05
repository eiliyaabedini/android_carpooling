package com.deftmove.notifications

import com.deftmove.carpooling.interfaces.OpenRideDetails
import com.deftmove.carpooling.interfaces.notifications.model.NotificationDataModel
import com.deftmove.carpooling.interfaces.notifications.model.NotificationModel
import com.deftmove.carpooling.interfaces.notifications.repository.NotificationsApiPagingRepository
import com.deftmove.carpooling.interfaces.notifications.service.NotificationsService
import com.deftmove.heart.common.event.DataEvent
import com.deftmove.heart.common.event.EventManager
import com.deftmove.heart.common.presenter.Presenter
import com.deftmove.heart.interfaces.common.ReactiveTransformer
import com.deftmove.heart.interfaces.common.presenter.PresenterAction
import com.deftmove.heart.interfaces.common.presenter.PresenterCommonAction
import com.deftmove.heart.interfaces.common.presenter.PresenterView
import com.deftmove.heart.interfaces.navigator.HeartNavigator
import io.reactivex.rxkotlin.plusAssign
import timber.log.Timber
import java.util.concurrent.TimeUnit

class NotificationsPresenter(
    private val notificationsRepository: NotificationsApiPagingRepository,
    private val notificationsService: NotificationsService,
    private val eventManager: EventManager,
    private val heartNavigator: HeartNavigator,
    private val reactiveTransformer: ReactiveTransformer
) : Presenter<NotificationsPresenter.View>() {

    private val items: MutableList<NotificationModel> = mutableListOf()

    override fun initialise() {

        commonView?.showContentLoading()

        commonView?.actions()
              ?.subscribeOn(reactiveTransformer.ioScheduler())
              ?.subscribeSafeWithShowingErrorContent { action ->
                  Timber.d("action:%s", action)
                  when (action) {
                      is PresenterCommonAction.ErrorRetryClicked -> {
                          commonView?.showContent()
                      }

                      is Action.NotificationItemClicked -> {
                          action.dataModels.find { it.key == "ride_id" }?.let { notificationDataModel ->
                              heartNavigator.getLauncher(
                                    OpenRideDetails(notificationDataModel.value)
                              )?.startActivity()
                          }
                      }
                  }
              }

        commonView?.actions()
              ?.startWith(Action.RefreshRequested)
              ?.ofType(Action.RefreshRequested::class.java)
              ?.doOnNext { notificationsRepository.fetchNotifications() }
              ?.doOnError {
                  view?.hideSwipeToRefresh()
              }
              ?.subscribeSafeWithShowingErrorContent()

        eventManager.observe()
              .filter { dataEvent ->
                  dataEvent is DataEvent.NotificationReceived ||
                        dataEvent is DataEvent.NotificationsSeen
              }
              .doOnNext { notificationsRepository.fetchNotifications() }
              .subscribeSafeWithShowingErrorContent()

        //Mark everything seen
        notificationsService.markAllNotificationsAsSeen()
              .subscribeOn(reactiveTransformer.ioScheduler())
              .delay(DELAY_TO_MARK_EVERYTHING_SEEN, TimeUnit.MILLISECONDS)
              .subscribeSafeResponseWithShowingErrorContent()
    }

    override fun viewIsVisible() {
        super.viewIsVisible()

        disposablesVisibleView += notificationsRepository.observe()
              .map { notificationsRepository.getAllItems() }
              .doOnNext { list ->
                  items.clear()
                  items.addAll(list)

                  updateList()

                  view?.hideSwipeToRefresh()
              }
              .doOnError { commonView?.showContentError() }
              .subscribe()
    }

    private fun updateList() {
        commonView?.showContent()

        view?.hideSwipeToRefresh()
        view?.showItems(items)
    }

    interface View : PresenterView {
        fun showSwipeToRefresh()
        fun hideSwipeToRefresh()
        fun showItems(items: List<NotificationModel>)
    }

    sealed class Action : PresenterAction {
        data class NotificationItemClicked(val dataModels: List<NotificationDataModel>) : Action()
        object RefreshRequested : Action()
    }

    companion object {
        private const val DELAY_TO_MARK_EVERYTHING_SEEN: Long = 3000
    }
}
