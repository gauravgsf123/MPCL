package com.mpcl.custom

import android.content.Context
import android.graphics.Typeface
import android.util.AttributeSet
import androidx.core.content.ContextCompat
import com.google.android.material.textfield.TextInputEditText
import com.mpcl.R


class RegularTextInputEditText(context: Context, attrs: AttributeSet?) : TextInputEditText(
    context,
    attrs
) {
    private var editText: TextInputEditText? = null
    init {
        val typeface = Typeface.createFromAsset(
            getContext().assets,
            "font/kameron_regular.ttf"
        )
        setTypeface(typeface) //function used to set font

        setTextColor(ContextCompat.getColor(context, R.color.black))
        setPadding(30, 30, 15, 15)
        maxLines = 1
        gravity = TEXT_ALIGNMENT_CENTER or TEXT_ALIGNMENT_TEXT_START
    }

}