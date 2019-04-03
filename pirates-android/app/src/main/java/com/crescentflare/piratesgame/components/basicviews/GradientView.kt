package com.crescentflare.piratesgame.components.basicviews

import android.annotation.TargetApi
import android.content.Context
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.util.AttributeSet
import com.crescentflare.piratesgame.components.utility.ViewletUtil
import com.crescentflare.unilayout.views.UniView
import com.crescentflare.jsoninflator.JsonInflatable
import com.crescentflare.jsoninflator.binder.InflatorBinder
import com.crescentflare.jsoninflator.utility.InflatorMapUtil

/**
 * Basic view: a simple background with a gradient
 */
class GradientView : UniView {

    // --
    // Static: viewlet integration
    // --

    companion object {

        val viewlet: JsonInflatable = object : JsonInflatable {

            override fun create(context: Context): Any {
                return GradientView(context)
            }

            override fun update(mapUtil: InflatorMapUtil, obj: Any, attributes: Map<String, Any>, parent: Any?, binder: InflatorBinder?): Boolean {
                if (obj is GradientView) {
                    // Generic view properties
                    ViewletUtil.applyGenericViewAttributes(mapUtil, obj, attributes)

                    // Gradient properties
                    obj.startColor = mapUtil.optionalColor(attributes, "startColor", 0)
                    obj.endColor = mapUtil.optionalColor(attributes, "endColor", 0)
                    obj.angle = mapUtil.optionalInteger(attributes, "angle", 0)
                    return true
                }
                return false
            }

            override fun canRecycle(mapUtil: InflatorMapUtil, obj: Any, attributes: Map<String, Any>): Boolean {
                return obj is GradientView
            }

        }

    }


    // --
    // Members
    // --

    private val gradientDrawable = GradientDrawable()


    // --
    // Initialization
    // --

    @JvmOverloads
    constructor(
            context: Context,
            attrs: AttributeSet? = null,
            defStyleAttr: Int = 0)
            : super(context, attrs, defStyleAttr)

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    constructor(
            context: Context,
            attrs: AttributeSet?,
            defStyleAttr: Int,
            defStyleRes: Int)
            : super(context, attrs, defStyleAttr, defStyleRes)

    init {
        background = gradientDrawable
    }


    // --
    // Configurable values
    // --

    var startColor: Int = 0
        set(startColor) {
            field = startColor
            updateState()
        }

    var endColor: Int = 0
        set(endColor) {
            field = endColor
            updateState()
        }

    var angle: Int = 0
        set(angle) {
            field = angle
            updateState()
        }


    // --
    // State update
    // --

    private fun updateState() {
        gradientDrawable.colors = intArrayOf(startColor, endColor)
        gradientDrawable.orientation = when(angle % 360) {
            in 0..44 -> GradientDrawable.Orientation.TOP_BOTTOM
            in 45..89 -> GradientDrawable.Orientation.TR_BL
            in 90..134 -> GradientDrawable.Orientation.RIGHT_LEFT
            in 135..179 -> GradientDrawable.Orientation.BR_TL
            in 180..224 -> GradientDrawable.Orientation.BOTTOM_TOP
            in 225..269 -> GradientDrawable.Orientation.BL_TR
            in 270..314 -> GradientDrawable.Orientation.LEFT_RIGHT
            in 315..359 -> GradientDrawable.Orientation.TL_BR
            else -> GradientDrawable.Orientation.TOP_BOTTOM
        }
        gradientDrawable.gradientRadius = angle.toFloat()
        background = gradientDrawable
    }

}
