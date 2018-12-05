package com.deftmove.home.delegate

import android.content.Context
import com.deftmove.heart.common.ui.factory.DialogFactory
import com.deftmove.heart.interfaces.common.LocalStorageManager
import com.deftmove.home.delegate.ShowLicenceDelegateImp.Companion.SHOWED_LICENCE_DIALOG
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.never
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import org.junit.Test

class ShowLicenceDelegateImpTest {
    private val mockLocalStorageManager: LocalStorageManager = mock()
    private val mockDialogFactory: DialogFactory = mock()
    private val mockContext: Context = mock()

    private val delegate = ShowLicenceDelegateImp(mockLocalStorageManager, mockDialogFactory)

    @Test
    fun `when licence is already shown then do not show dialog`() {
        whenever(mockLocalStorageManager.getBoolean(SHOWED_LICENCE_DIALOG, false)).thenReturn(true)

        delegate.checkAndShowIfNeeded(mockContext)

        verify(mockDialogFactory, never()).showNormalDialog(any(), any())
    }

    @Test
    fun `when licence is not shown before then show dialog`() {
        whenever(mockLocalStorageManager.getBoolean(SHOWED_LICENCE_DIALOG, false)).thenReturn(false)

        delegate.checkAndShowIfNeeded(mockContext)

        verify(mockDialogFactory).showNormalDialog(any(), any())
    }
}
