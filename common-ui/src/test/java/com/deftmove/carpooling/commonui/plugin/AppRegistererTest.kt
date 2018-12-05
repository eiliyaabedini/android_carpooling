package com.deftmove.carpooling.commonui.plugin

import com.deftmove.carpooling.interfaces.common.data.BadgeIconModel
import com.nhaarman.mockitokotlin2.mock
import io.reactivex.rxkotlin.ofType
import org.junit.Assert
import org.junit.Test

class AppRegistererTest {

    private val registerer: AppRegisterer = AppRegisterer()

    @Test
    fun `when register a model observe has to be notified`() {
        val subscription = registerer.observeActionIcons().test()

        repeat(10) {
            registerer.registerActionIcon(mock())
        }

        subscription
              .assertValueCount(10)
    }

    @Test
    fun `when registering models with different types then filtered observe has to get all the types`() {
        val subscription = registerer.observeActionIcons()
              .ofType<BadgeIconModel.BadgeIconPublicProfile>()
              .test()

        makeShuffleMocks(5, 10)
              .forEach {
                  registerer.registerActionIcon(it)
              }

        subscription
              .assertValueCount(10)
    }

    @Test
    fun `when registering models with different types then get items with an specific type should return correct items`() {
        makeShuffleMocks(35, 15)
              .forEach {
                  registerer.registerActionIcon(it)
              }

        Assert.assertEquals(registerer.getActionIcons<BadgeIconModel.BadgeIconPublicProfile>().size, 15)
    }

    private fun makeShuffleMocks(homeNumber: Int, publicProfileNumber: Int): List<BadgeIconModel> {
        return (0 until homeNumber).map { mock<BadgeIconModel.BadgeIconHome>() } +
              (0 until publicProfileNumber).map { mock<BadgeIconModel.BadgeIconPublicProfile>() }
                    .shuffled()
    }
}
