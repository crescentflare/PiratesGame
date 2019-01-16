package com.crescentflare.piratesgame.components.simpleviewlets

import android.content.Context
import android.support.v4.content.ContextCompat
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.crescentflare.piratesgame.R
import com.crescentflare.piratesgame.components.styling.AppFonts
import com.crescentflare.piratesgame.components.utility.MarkdownGenerator
import com.crescentflare.piratesgame.components.utility.ViewletUtil
import com.crescentflare.simplemarkdownparser.SimpleMarkdownConverter
import com.crescentflare.unilayout.views.UniTextView
import com.crescentflare.viewletcreator.ViewletCreator
import com.crescentflare.viewletcreator.binder.ViewletBinder
import com.crescentflare.viewletcreator.utility.ViewletMapUtil


/**
 * Basic view viewlet: a text view
 */
object TextViewlet {

    // ---
    // Viewlet instance
    // ---

    val viewlet: ViewletCreator.Viewlet = object : ViewletCreator.Viewlet {

        override fun create(context: Context): View {
            return UniTextView(context)
        }

        override fun update(view: View, attributes: Map<String, Any>, parent: ViewGroup, binder: ViewletBinder): Boolean {
            if (view is UniTextView) {
                // Text
                val maxLines = ViewletMapUtil.optionalInteger(attributes, "maxLines", 0)
                val defaultTextSize = view.getResources().getDimensionPixelSize(R.dimen.text)
                val markdownText = ViewletUtil.localizedString(
                    view.context,
                    ViewletMapUtil.optionalString(attributes, "localizedMarkdownText", null),
                    ViewletMapUtil.optionalString(attributes, "markdownText", null)
                )
                if (markdownText != null) {
                    val textInverted = ContextCompat.getColor(view.getContext(), R.color.textInverted)
                    val noColorization = ViewletMapUtil.optionalColor(attributes, "textColor", 0) == textInverted
                    val markdownString = SimpleMarkdownConverter.toSpannable(markdownText, MarkdownGenerator(view.getContext().applicationContext, noColorization))
                    view.setText(markdownString, TextView.BufferType.SPANNABLE)
                } else {
                    view.text = ViewletUtil.localizedString(
                        view.context,
                        ViewletMapUtil.optionalString(attributes, "localizedText", null),
                        ViewletMapUtil.optionalString(attributes, "text", null)
                    )
                }
                view.setTextSize(TypedValue.COMPLEX_UNIT_PX, ViewletMapUtil.optionalDimension(attributes, "textSize", defaultTextSize).toFloat())
                view.maxLines = if (maxLines == 0) Integer.MAX_VALUE else maxLines
                view.setTextColor(ViewletMapUtil.optionalColor(attributes,"textColor", ContextCompat.getColor(view.getContext(), R.color.text)))

                // Font
                view.typeface = AppFonts.instance.getTypeface(ViewletMapUtil.optionalString(attributes, "font", "normal"))

                // Text alignment
                val textAlignment = TextAlignment.fromString(ViewletMapUtil.optionalString(attributes, "textAlignment", ""))
                view.gravity = textAlignment.toGravity()

                // Generic view properties
                ViewletUtil.applyGenericViewAttributes(view, attributes)
                return true
            }
            return false
        }

        override fun canRecycle(view: View, attributes: Map<String, Any>): Boolean {
            return view is UniTextView
        }

    }


    // ---
    // Default view creation
    // ---

    fun newTextView(context: Context): UniTextView {
        val textView = UniTextView(context)
        textView.typeface = AppFonts.instance.getTypeface("normal")
        textView.setTextSize(TypedValue.COMPLEX_UNIT_PX, context.resources.getDimensionPixelSize(R.dimen.text).toFloat())
        textView.setTextColor(ContextCompat.getColor(context, R.color.text))
        return textView
    }


    // ---
    // Text alignment enum
    // ---

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
