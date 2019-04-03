package com.crescentflare.piratesgame.components.simpleviewlets

import android.content.Context
import android.graphics.PorterDuff
import android.support.v4.content.ContextCompat
import com.crescentflare.piratesgame.R
import com.crescentflare.piratesgame.components.utility.ViewletUtil

import com.crescentflare.unilayout.views.UniSpinnerView
import com.crescentflare.jsoninflator.JsonInflatable
import com.crescentflare.jsoninflator.binder.InflatorBinder
import com.crescentflare.jsoninflator.utility.InflatorMapUtil

/**
 * Basic view viewlet: a waiting spinner
 */
object SpinnerViewlet {

    // --
    // Viewlet instance
    // --

    val viewlet: JsonInflatable = object : JsonInflatable {

        override fun create(context: Context): Any {
            return UniSpinnerView(context)
        }

        override fun update(mapUtil: InflatorMapUtil, obj: Any, attributes: Map<String, Any>, parent: Any?, binder: InflatorBinder?): Boolean {
            if (obj is UniSpinnerView) {
                // Style
                val colorStyle = ColorStyle.fromString(mapUtil.optionalString(attributes, "colorStyle", ""))
                if (colorStyle == ColorStyle.Inverted) {
                    obj.indeterminateDrawable.setColorFilter(-0x1, PorterDuff.Mode.MULTIPLY)
                } else {
                    obj.indeterminateDrawable.setColorFilter(ContextCompat.getColor(obj.context, R.color.colorPrimary), PorterDuff.Mode.MULTIPLY)
                }

                // Generic view properties
                ViewletUtil.applyGenericViewAttributes(mapUtil, obj, attributes)
                return true
            }
            return false
        }

        override fun canRecycle(mapUtil: InflatorMapUtil, obj: Any, attributes: Map<String, Any>): Boolean {
            return obj is UniSpinnerView
        }

    }


    // --
    // Scale type enum
    // --

    enum class ColorStyle(val value: String) {

        Normal("normal"),
        Inverted("inverted");

        companion object {

            fun fromString(string: String?): ColorStyle {
                for (enum in ColorStyle.values()) {
                    if (enum.value == string) {
                        return enum
                    }
                }
                return Normal
            }

        }

    }

}
