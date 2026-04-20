package com.unibo.android.ui.customs

import android.content.Context
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat
import androidx.core.content.withStyledAttributes
import com.unibo.android.ui.R
import com.unibo.android.ui.databinding.CustomButtonBinding

class CustomButton @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
) : FrameLayout(context, attrs) {

    private var binding : CustomButtonBinding? = null

    init {
        // Inflate del layout XML in questa vista
        binding = CustomButtonBinding.inflate(LayoutInflater.from(context), this, true)

        // Applica gli attributi XML, se presenti
        attrs?.let {
            context.withStyledAttributes(it, R.styleable.CustomButton) {
                val text = getString(R.styleable.CustomButton_buttonText)
                val icon = getDrawable(R.styleable.CustomButton_buttonIcon)
                setButtonText(text)

                icon?.let {
                    setButtonIcon(it)
                }

            }
        }
    }

    /** Metodo per impostare il testo dinamicamente */
    fun setButtonText(text: String?) {
        binding?.buttonText?.text = text
    }

    /** Metodo per impostare l'icona dinamicamente */
    fun setButtonIcon(@DrawableRes iconResId: Int) {
        ContextCompat.getDrawable(context, iconResId)?.let { setButtonIcon(it) }
    }

    private fun setButtonIcon(iconRes: Drawable) {
        binding?.apply {
            buttonIcon.setImageDrawable(iconRes)
            buttonIcon.visibility = VISIBLE
        }
    }
}
