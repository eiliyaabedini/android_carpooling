package com.deftmove.carpooling.commonui.ui

import android.content.Context
import android.text.InputType
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import android.widget.TextView
import androidx.core.view.isGone
import com.deftmove.carpooling.commonui.R
import com.deftmove.heart.common.ui.extension.bindView
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.jakewharton.rxbinding3.view.clicks
import com.jakewharton.rxbinding3.widget.afterTextChangeEvents
import com.jakewharton.rxbinding3.widget.textChanges
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.plusAssign
import java.util.concurrent.TimeUnit

//https://medium.com/@elye.project/building-custom-component-with-kotlin-fc082678b080
class CustomTextInputLayout @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private val inputLayout: TextInputLayout by bindView(R.id.custom_input_layout_inputlayout)
    private val editText: TextInputEditText by bindView(R.id.custom_input_layout_edittext)
    private val hintBelowText: TextView by bindView(R.id.custom_input_layout_hint_below)
    private val overlayView: View by bindView(R.id.custom_input_layout_overlay_view)

    private val disposables = CompositeDisposable()

    init {
        LayoutInflater.from(context)
              .inflate(R.layout.custom_text_input_layout, this, true)

        checkAttributes(context, attrs)
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()

        disposables += editText.afterTextChangeEvents()
              .debounce(DELAY_DEBOUNCE_TEXT_CHANGE, TimeUnit.MILLISECONDS)
              .observeOn(AndroidSchedulers.mainThread())
              .subscribe {
                  setError("")
              }
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()

        disposables.clear()
    }

    private fun checkAttributes(context: Context, attrs: AttributeSet?) {
        attrs?.let {
            val typedArray = context.obtainStyledAttributes(
                  it,
                  R.styleable.CustomTextInputLayout, 0, 0
            )
            val hintResourceId: Int = typedArray
                  .getResourceId(
                        R.styleable.CustomTextInputLayout_text_input_hint,
                        0
                  )

            if (hintResourceId != 0) {
                inputLayout.hint = resources.getText(hintResourceId)
            }

            val hintBelow = resources.getText(
                  typedArray
                        .getResourceId(
                              R.styleable.CustomTextInputLayout_text_input_hint_below,
                              R.string.custom_text_input_default_hint_below
                        )
            )

            if (hintBelow.isBlank()) {
                hintBelowText.isGone = true
            } else {
                hintBelowText.isGone = false
                hintBelowText.text = hintBelow
            }

            val isEnabled = typedArray
                  .getBoolean(
                        R.styleable.CustomTextInputLayout_text_input_enable,
                        true
                  )
            inputLayout.isFocusable = isEnabled
            editText.isFocusable = isEnabled
            overlayView.isGone = isEnabled

            editText.inputType = typedArray
                  .getInt(
                        R.styleable.CustomTextInputLayout_text_input_input_type,
                        InputType.TYPE_CLASS_TEXT
                  )

            when (
                typedArray.getInt(
                      R.styleable.CustomTextInputLayout_text_input_drawable_type,
                      0
                )
                ) {
                0 -> {
                    editText.setCompoundDrawablesRelativeWithIntrinsicBounds(
                          0,
                          0,
                          0,
                          0
                    )
                }

                1 -> {
                    editText.setCompoundDrawablesRelativeWithIntrinsicBounds(
                          0,
                          0,
                          R.drawable.ic_arrow_drop_down_inputtint_24dp,
                          0
                    )
                }

                2 -> {
                    editText.setCompoundDrawablesRelativeWithIntrinsicBounds(
                          0,
                          0,
                          R.drawable.ic_lock_outline_black_24dp,
                          0
                    )
                }
            }

            val maxCounter = typedArray
                  .getInteger(
                        R.styleable.CustomTextInputLayout_text_input_max_counter,
                        0
                  )

            when (maxCounter) {
                0 -> {
                    inputLayout.isCounterEnabled = false
                    editText.setLines(1)
                    editText.isSingleLine = true
                }

                else -> {
                    inputLayout.counterMaxLength = maxCounter
                    inputLayout.isCounterEnabled = true
                    editText.setLines(MAX_NUMBER_OF_LINES)
                    editText.isSingleLine = false
                }
            }

            typedArray.recycle()
        }
    }

    fun textChanges(): Observable<CharSequence> = editText.textChanges()

    fun getText(): String = editText.text.toString()

    fun setText(text: String) {
        editText.setText(text)
    }

    fun setTextIfPossible(text: String?) {
        text?.let { editText.setText(it) }
    }

    fun setError(error: String) {
        inputLayout.error = error
    }

    fun disabledClicks(): Observable<Unit> = overlayView.clicks()

    companion object {
        private const val DELAY_DEBOUNCE_TEXT_CHANGE: Long = 300
        private const val MAX_NUMBER_OF_LINES: Int = 3
    }
}
