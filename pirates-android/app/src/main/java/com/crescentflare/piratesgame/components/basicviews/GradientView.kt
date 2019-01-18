package com.crescentflare.piratesgame.components.basicviews

import android.annotation.TargetApi
import android.content.Context
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import com.crescentflare.piratesgame.components.utility.ViewletUtil
import com.crescentflare.unilayout.views.UniView
import com.crescentflare.viewletcreator.ViewletCreator
import com.crescentflare.viewletcreator.binder.ViewletBinder
import com.crescentflare.viewletcreator.utility.ViewletMapUtil

/**
 * Basic view: a simple background with a gradient
 */
class GradientView : UniView {

    // ---
    // Statics
    // ---

    companion object {

        // ---
        // Static: viewlet integration
        // ---

        val viewlet: ViewletCreator.Viewlet = object : ViewletCreator.Viewlet {

            override fun create(context: Context): View {
                return GradientView(context)
            }

            override fun update(view: View, attributes: Map<String, Any>?, parent: ViewGroup?, binder: ViewletBinder?): Boolean {
                if (view is GradientView) {
                    // Generic view properties
                    ViewletUtil.applyGenericViewAttributes(view, attributes)

                    // Gradient properties
                    view.startColor = ViewletMapUtil.optionalColor(attributes, "startColor", 0)
                    view.endColor = ViewletMapUtil.optionalColor(attributes, "endColor", 0)
                    view.angle = ViewletMapUtil.optionalInteger(attributes, "angle", 0)
                    return true
                }
                return false
            }

            override fun canRecycle(view: View, attributes: Map<String, Any>?): Boolean {
                return view is GradientView
            }

        }

    }


    // ---
    // Members
    // ---

    private val gradientDrawable = GradientDrawable()


    // ---
    // Initialization
    // ---

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


    // ---
    // Configurable values
    // ---

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


    // ---
    // State update
    // ---

    private fun updateState() {
        gradientDrawable.colors = intArrayOf(startColor, endColor)
        gradientDrawable.gradientRadius = angle.toFloat()
        background = gradientDrawable
    }

}
