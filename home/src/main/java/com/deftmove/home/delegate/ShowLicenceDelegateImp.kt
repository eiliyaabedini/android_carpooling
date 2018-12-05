package com.deftmove.home.delegate

import android.content.Context
import com.deftmove.heart.common.ui.factory.DialogFactory
import com.deftmove.heart.interfaces.common.LocalStorageManager
import com.deftmove.home.R

class ShowLicenceDelegateImp(
    private val localStorageManager: LocalStorageManager,
    private val dialogFactory: DialogFactory
) : ShowLicenceDelegate {

    override fun checkAndShowIfNeeded(context: Context) {
        if (!isShownBefore()) {
            showDialog(context)
            setShown()
        }
    }

    private fun showDialog(context: Context) {
        dialogFactory.showNormalDialog(context, R.string.licence_description)
    }

    private fun isShownBefore(): Boolean {
        return localStorageManager.getBoolean(SHOWED_LICENCE_DIALOG)
    }

    private fun setShown() {
        localStorageManager.setBoolean(SHOWED_LICENCE_DIALOG, true)
    }

    companion object {
        const val SHOWED_LICENCE_DIALOG = "com.deftmove.home.delegate.ShowedLicenceDialog"
    }
}
