package com.mpcl.custom

import android.content.Context
import android.graphics.Typeface
import android.text.TextPaint
import android.util.AttributeSet
import androidx.core.content.ContextCompat
import com.google.android.material.textfield.TextInputLayout
import com.mpcl.R

class RegularTextInputLayout(context: Context, attrs: AttributeSet?) : TextInputLayout(
    context,
    attrs
) {
    var regularTypeface = Typeface.createFromAsset(
        getContext().assets,
        "font/kameron_regular.ttf"
    )

    init {

        //setBackgroundColor(ContextCompat.getColor(context, R.color.white));
        boxBackgroundMode = BOX_BACKGROUND_OUTLINE
        boxBackgroundColor = ContextCompat.getColor(context, R.color.transparent)
        boxStrokeColor = ContextCompat.getColor(context,R.color.black)
        boxStrokeWidthFocused = 2
        errorIconDrawable = null
        setErrorTextAppearance(R.style.WelcomeErrorAppearance)


        //setBoxCornerRadii(5,5,5,5);


        setBoxCornerRadii(5f,5f,5f,5f);
        val editText = RegularTextInputEditText(context,attrs)
        if (editText != null) {
            editText.typeface = regularTypeface
            editText.setTextColor(ContextCompat.getColor(context, R.color.black))
        }
        try {


            // Retrieve the CollapsingTextHelper Field
            val cthf = TextInputLayout::class.java.getDeclaredField("mCollapsingTextHelper")
            cthf.isAccessible = true

            // Retrieve an instance of CollapsingTextHelper and its TextPaint
            val cth = cthf[this]
            val tpf = cth.javaClass.getDeclaredField("mTextPaint")
            tpf.isAccessible = true


            // Apply your Typeface to the CollapsingTextHelper TextPaint
            (tpf[cth] as TextPaint).typeface = regularTypeface
        } catch (ignored: Exception) {
            // Nothing to do
        }
    }
}