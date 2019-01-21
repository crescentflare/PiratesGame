package com.crescentflare.piratesgame.components.utility

import android.graphics.Paint
import android.graphics.Typeface
import android.os.Parcel
import android.os.Parcelable
import android.text.TextPaint
import android.text.style.TypefaceSpan

/**
 * A span to be able to set a custom typeface
 */
class CustomTypefaceSpan(private val newType: Typeface) : TypefaceSpan("") {

    // ---
    // Custom implementation
    // ---

    override fun updateDrawState(paint: TextPaint) {
        applyCustomTypeFace(paint, newType)
    }

    override fun updateMeasureState(paint: TextPaint) {
        applyCustomTypeFace(paint, newType)
    }


    // ---
    // Parcelable creator
    // ---

    companion object {

        val CREATOR: Parcelable.Creator<CustomTypefaceSpan> = object : Parcelable.Creator<CustomTypefaceSpan> {

            override fun createFromParcel(source: Parcel): CustomTypefaceSpan {
                return CustomTypefaceSpan(Typeface.DEFAULT)
            }

            override fun newArray(size: Int): Array<CustomTypefaceSpan?> {
                return arrayOfNulls(size)
            }

        }

        private fun applyCustomTypeFace(paint: Paint, tf: Typeface) {
            // Remember old style
            val oldStyle: Int
            val old = paint.typeface
            oldStyle = old?.style ?: 0

            // Apply simulated style
            val fake = oldStyle and tf.style.inv()
            if (fake and Typeface.BOLD != 0) {
                paint.isFakeBoldText = true
            }
            if (fake and Typeface.ITALIC != 0) {
                paint.textSkewX = -0.25f
            }

            // Set typeface
            paint.typeface = tf
        }

    }

}
