package com.deftmove.authentication.register

import com.deftmove.authentication.firebase.FirebaseLinkCreator
import com.deftmove.carpooling.interfaces.OpenAuthenticationMagicTokenSentDialog
import com.deftmove.carpooling.interfaces.authentication.services.AuthenticationService
import com.deftmove.heart.interfaces.ResponseResult
import com.deftmove.heart.interfaces.common.ReactiveTransformer
import com.deftmove.heart.interfaces.common.presenter.PresenterAction
import com.deftmove.heart.interfaces.common.presenter.PresenterCommonAction
import com.deftmove.heart.interfaces.common.presenter.PresenterCommonView
import com.deftmove.heart.interfaces.navigator.HeartNavigator
import com.deftmove.heart.testhelper.TestReactiveTransformer
import com.deftmove.heart.testhelper.navigator.ActivityLauncherOpenTest
import com.deftmove.heart.testhelper.thenJust
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.reset
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.subjects.PublishSubject
import org.junit.Before
import org.junit.Test

class RegisterPresenterTest {

    private val actions: PublishSubject<PresenterAction> = PublishSubject.create()
    private val actionsCommon: PublishSubject<PresenterCommonAction> = PublishSubject.create()
    private val reactiveTransformer: ReactiveTransformer = TestReactiveTransformer()
    private val mockHeartNavigator: HeartNavigator = mock()
    private val authenticationService: AuthenticationService = mock {
        on(it.createMagicToken(any())) doReturn (Observable.just<ResponseResult<String>>(
              ResponseResult.Success("newMagicToken")
        ))
        on(it.putMagicTokenLink(any(), any())) doReturn (Observable.just<ResponseResult<Boolean>>(
              ResponseResult.Success(true)
        ))
    }
    private val firebaseLinkCreator: FirebaseLinkCreator = mock {
        on(it.createLink(any())) doReturn (Single.just("newMagicLink"))
    }

    private val mockView: RegisterPresenter.View = mock()

    private val mockCommonView: PresenterCommonView = mock {
        on(it.actions()) doReturn (actions.mergeWith(actionsCommon))
    }

    private val presenter: RegisterPresenter = RegisterPresenter(
          reactiveTransformer, authenticationService, firebaseLinkCreator, mockHeartNavigator
    )

    @Before
    fun setUp() {
        //        Timber.plant(TestDebugTree())
    }

    @Test
    fun `when retry in error screen clicked then show contents again`() {
        presenter.attachView(mockView, mockCommonView)
        presenter.initialise()

        reset(mockCommonView)

        actions.onNext(PresenterCommonAction.ErrorRetryClicked)

        verify(mockCommonView).showContent()
    }

    @Test
    fun `when next clicked and email is not valid then show error`() {
        presenter.attachView(mockView, mockCommonView)
        presenter.initialise()

        actions.onNext(RegisterPresenter.Action.NextClicked("notValidError@djgf."))

        verify(mockView).showEmailError()
    }

    @Test
    fun `when next clicked and email is not valid 2 then show error`() {
        presenter.attachView(mockView, mockCommonView)
        presenter.initialise()

        actions.onNext(RegisterPresenter.Action.NextClicked("fabian.mecklenburg@onlione.d"))

        verify(mockView).showEmailError()
    }

    @Test
    fun `when next clicked and email has plus then show error`() {
        presenter.attachView(mockView, mockCommonView)
        presenter.initialise()

        actions.onNext(RegisterPresenter.Action.NextClicked("fabian.mecklenburg1+3@onlione.com"))

        verify(mockView).showEmailError()
    }

    @Test
    fun `when next clicked and email is empty then show error`() {
        presenter.attachView(mockView, mockCommonView)
        presenter.initialise()

        actions.onNext(RegisterPresenter.Action.NextClicked(""))

        verify(mockView).showEmailError()
    }

    @Test
    fun `when next clicked and email is valid and process successfully then show complete dialog`() {
        presenter.attachView(mockView, mockCommonView)
        presenter.initialise()

        val activityLauncherOpenTest = ActivityLauncherOpenTest()
        whenever(mockHeartNavigator.getLauncher(OpenAuthenticationMagicTokenSentDialog("test@djgf.com")))
              .thenReturn(activityLauncherOpenTest)

        actions.onNext(RegisterPresenter.Action.NextClicked("test@djgf.com"))

        verify(authenticationService).createMagicToken("test@djgf.com")
        verify(firebaseLinkCreator).createLink("newMagicToken")
        verify(authenticationService).putMagicTokenLink("newMagicLink", "newMagicToken")
        activityLauncherOpenTest.test()
              .startActivityCalled()

        verify(mockCommonView).showContent()
    }

    @Test
    fun `when next clicked and email is valid and process failed then show error`() {
        whenever(authenticationService.putMagicTokenLink(any(), any())).thenReturn(
              Observable.just(
                    ResponseResult.Success(false)
              )
        )

        presenter.attachView(mockView, mockCommonView)
        presenter.initialise()

        actions.onNext(RegisterPresenter.Action.NextClicked("test@djgf.com"))

        verify(authenticationService).createMagicToken("test@djgf.com")
        verify(firebaseLinkCreator).createLink("newMagicToken")
        verify(authenticationService).putMagicTokenLink("newMagicLink", "newMagicToken")
        verify(mockCommonView).showContentError()
    }

    @Test
    fun `when next clicked and email is valid and createMagicToken throw error then show error`() {
        whenever(authenticationService.createMagicToken(any()))
              .thenJust(ResponseResult.Failure(mock()))

        presenter.attachView(mockView, mockCommonView)
        presenter.initialise()

        actions.onNext(RegisterPresenter.Action.NextClicked("test@djgf.com"))

        verify(mockCommonView).showContentError()
    }

    @Test
    fun `when next clicked and email is valid and putMagicTokenLink throw error then show error`() {
        whenever(authenticationService.putMagicTokenLink(any(), any()))
              .thenJust(ResponseResult.Failure(mock()))

        presenter.attachView(mockView, mockCommonView)
        presenter.initialise()

        actions.onNext(RegisterPresenter.Action.NextClicked("test@djgf.com"))

        verify(mockCommonView).showContentError()
    }
}
