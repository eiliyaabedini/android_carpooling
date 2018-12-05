package com.deftmove.home

import com.deftmove.carpooling.interfaces.plugin.Registerer
import com.deftmove.carpooling.interfaces.profile.service.ProfileService
import com.deftmove.carpooling.interfaces.repository.RideFeedApiPagingRepository
import com.deftmove.carpooling.interfaces.ride.service.RideService
import com.deftmove.carpooling.interfaces.user.CurrentUserManager
import com.deftmove.heart.common.event.EventManager
import com.deftmove.heart.interfaces.common.ReactiveTransformer
import com.deftmove.heart.interfaces.common.presenter.PresenterAction
import com.deftmove.heart.interfaces.common.presenter.PresenterCommonAction
import com.deftmove.heart.interfaces.common.presenter.PresenterCommonView
import com.deftmove.heart.interfaces.navigator.HeartNavigator
import com.deftmove.heart.testhelper.TestDebugTree
import com.deftmove.heart.testhelper.TestReactiveTransformer
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import io.reactivex.subjects.PublishSubject
import org.junit.Before
import org.junit.Test
import timber.log.Timber

class HomePresenterTest {

    private val actions: PublishSubject<PresenterAction> = PublishSubject.create()
    private val actionsCommon: PublishSubject<PresenterCommonAction> = PublishSubject.create()
    private val reactiveTransformer: ReactiveTransformer = TestReactiveTransformer()
    private val mockCurrentUserManager: CurrentUserManager = mock()
    private val mockProfileService: ProfileService = mock()
    private val mockRideFeedRepository: RideFeedApiPagingRepository = mock()
    private val mockRideService: RideService = mock()
    private val mockEventManager: EventManager = mock()
    private val mockHeartNavigator: HeartNavigator = mock()
    private val mockRegisterer: Registerer = mock()

    private val mockView: HomePresenter.View = mock()

    private val mockCommonView: PresenterCommonView = mock {
        on(it.actions()) doReturn (actions.mergeWith(actionsCommon))
    }

    private val presenter: HomePresenter = HomePresenter(
          currentUserManager = mockCurrentUserManager,
          profileService = mockProfileService,
          rideRepository = mockRideFeedRepository,
          rideService = mockRideService,
          eventManager = mockEventManager,
          heartNavigator = mockHeartNavigator,
          registerer = mockRegisterer,
          reactiveTransformer = reactiveTransformer
    )

    @Before
    fun setUp() {
        Timber.plant(TestDebugTree())
    }

    @Test
    fun `when retry in error screen clicked then show contents again`() {
        //TODO add tests here
        //        presenter.attachView(mockView, mockCommonView)
        //        presenter.initialise()
        //
        //        reset(mockCommonView)
        //
        //        actions.onNext(PresenterCommonAction.ErrorRetryClicked)
        //
        //        verify(mockCommonView).showContent()
    }

}
