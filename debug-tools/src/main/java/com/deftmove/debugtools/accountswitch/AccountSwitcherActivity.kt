package com.deftmove.debugtools.accountswitch

import android.widget.ArrayAdapter
import android.widget.Spinner
import androidx.appcompat.widget.SwitchCompat
import com.deftmove.debugtools.R
import com.deftmove.heart.common.ui.ActivityWithPresenter
import com.deftmove.heart.common.ui.ScreenBucket
import com.deftmove.heart.common.ui.extension.bindView
import com.deftmove.heart.interfaces.common.ui.ScreenBucketModel
import com.jakewharton.rxbinding3.widget.itemSelections
import io.reactivex.rxkotlin.plusAssign
import org.koin.androidx.scope.currentScope

class AccountSwitcherActivity : ActivityWithPresenter() {
    override fun getCurrentScreenBucket(): ScreenBucket<*> {
        return object : ScreenBucket<AccountSwitcherPresenter.View>(
              ScreenBucketModel(
                    scope = currentScope,
                    viewLayout = R.layout.activity_account_switcher_debug
              )
        ) {
            override fun getPresenterView(): AccountSwitcherPresenter.View {
                return object : AccountSwitcherPresenter.View {
                    override fun updateAccountsList(accounts: List<String>) {

                        runOnUiThread {
                            val userAdapter = ArrayAdapter<String>(baseContext, android.R.layout.simple_spinner_item)
                            userAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                            userAdapter.addAll(accounts)

                            accountsSpinner.adapter = userAdapter

                            disposables += accountsSpinner.itemSelections()
                                  .skipInitialValue()
                                  .skip(1)
                                  .doOnNext { index ->
                                      actions.onNext(
                                            AccountSwitcherPresenter.Action.AccountSelected(
                                                  index,
                                                  fastLoginSwitch.isChecked
                                            )
                                      )
                                  }
                                  .subscribe()
                        }
                    }
                }
            }
        }
    }

    private val accountsSpinner: Spinner by bindView(R.id.account_switcher_spinner_accounts)
    private val fastLoginSwitch: SwitchCompat by bindView(R.id.account_switcher_fast_login_switch)
}
