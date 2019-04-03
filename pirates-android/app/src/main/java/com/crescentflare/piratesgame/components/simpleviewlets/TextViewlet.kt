package com.crescentflare.piratesgame.components.simpleviewlets

import android.content.Context
import android.support.v4.content.ContextCompat
import android.util.TypedValue
import android.view.Gravity
import android.widget.TextView
import com.crescentflare.piratesgame.R
import com.crescentflare.piratesgame.components.styling.AppFonts
import com.crescentflare.piratesgame.components.utility.MarkdownGenerator
import com.crescentflare.piratesgame.components.utility.ViewletUtil
import com.crescentflare.simplemarkdownparser.SimpleMarkdownConverter
import com.crescentflare.unilayout.views.UniTextView
import com.crescentflare.jsoninflator.JsonInflatable
import com.crescentflare.jsoninflator.binder.InflatorBinder
import com.crescentflare.jsoninflator.utility.InflatorMapUtil


/**
 * Basic view viewlet: a text view
 */
object TextViewlet {

    // --
    // Viewlet instance
    // --

    val viewlet: JsonInflatable = object : JsonInflatable {

        override fun create(context: Context): Any {
            return UniTextView(context)
        }

        override fun update(mapUtil: InflatorMapUtil, obj: Any, attributes: Map<String, Any>, parent: Any?, binder: InflatorBinder?): Boolean {
            if (obj is UniTextView) {
                // Text
                val maxLines = mapUtil.optionalInteger(attributes, "maxLines", 0)
                val defaultTextSize = obj.resources.getDimensionPixelSize(R.dimen.text)
                val markdownText = ViewletUtil.localizedString(
                    obj.context,
                    mapUtil.optionalString(attributes, "localizedMarkdownText", null),
                    mapUtil.optionalString(attributes, "markdownText", null)
                )
                if (markdownText != null) {
                    val textInverted = ContextCompat.getColor(obj.context, R.color.textInverted)
                    val noColorization = mapUtil.optionalColor(attributes, "textColor", 0) == textInverted
                    val markdownString = SimpleMarkdownConverter.toSpannable(markdownText, MarkdownGenerator(obj.context.applicationContext, noColorization))
                    obj.setText(markdownString, TextView.BufferType.SPANNABLE)
                } else {
                    obj.text = ViewletUtil.localizedString(
                        obj.context,
                        mapUtil.optionalString(attributes, "localizedText", null),
                        mapUtil.optionalString(attributes, "text", null)
                    )
                }
                obj.setTextSize(TypedValue.COMPLEX_UNIT_PX, mapUtil.optionalDimension(attributes, "textSize", defaultTextSize).toFloat())
                obj.maxLines = if (maxLines == 0) Integer.MAX_VALUE else maxLines
                obj.setTextColor(mapUtil.optionalColor(attributes,"textColor", ContextCompat.getColor(obj.context, R.color.text)))

                // Font
                obj.typeface = AppFonts.getTypeface(mapUtil.optionalString(attributes, "font", "normal"))

                // Text alignment
                val textAlignment = TextAlignment.fromString(mapUtil.optionalString(attributes, "textAlignment", ""))
                obj.gravity = textAlignment.toGravity()

                // Generic view properties
                ViewletUtil.applyGenericViewAttributes(mapUtil, obj, attributes)
                return true
            }
            return false
        }

        override fun canRecycle(mapUtil: InflatorMapUtil, obj: Any, attributes: Map<String, Any>): Boolean {
            return obj is UniTextView
        }

    }


    // --
    // Default view creation
    // --

    fun newTextView(context: Context): UniTextView {
        val textView = UniTextView(context)
        textView.typeface = AppFonts.getTypeface("normal")
        textView.setTextSize(TypedValue.COMPLEX_UNIT_PX, context.resources.getDimensionPixelSize(R.dimen.text).toFloat())
        textView.setTextColor(ContextCompat.getColor(context, R.color.text))
        return textView
    }


    // --
    // Text alignment enum
    // --

    enum class TextAlignment(val value: String) {

        Left("left"),
        Center("center"),
        Right("right");

        fun toGravity(): Int {
            return when(this) {
                Center -> Gravity.CENTER
                Right -> Gravity.RIGHT
                else -> Gravity.LEFT
            }
        }

        companion object {

            fun fromString(string: String?): TextAlignment {
                for (enum in TextAlignment.values()) {
                    if (enum.value == string) {
                        return enum
                    }
                }
                return Left
            }

        }

    }

}
