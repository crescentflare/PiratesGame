package com.crescentflare.piratesgame.components.simpleviewlets

import android.annotation.TargetApi
import android.content.Context
import android.content.res.Resources
import android.os.Build
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import com.crescentflare.piratesgame.R
import android.util.TypedValue
import com.crescentflare.piratesgame.components.utility.ViewletUtil
import com.crescentflare.viewletcreator.utility.ViewletMapUtil
import com.crescentflare.viewletcreator.binder.ViewletBinder
import com.crescentflare.viewletcreator.ViewletCreator
import com.crescentflare.unilayout.views.UniView


/**
 * Basic view viewlet: a spacing element
 */
class SpacerViewlet : UniView {

    // ---
    // Statics
    // ---

    companion object {

        // ---
        // Static: viewlet integration
        // ---

        val viewlet: ViewletCreator.Viewlet = object : ViewletCreator.Viewlet {

            override fun create(context: Context): View {
                return SpacerViewlet(context)
            }

            override fun update(view: View, attributes: Map<String, Any>, parent: ViewGroup?, binder: ViewletBinder?): Boolean {
                if (view is SpacerViewlet) {
                    // Apply take width and height
                    view.takeWidth = TakeSize.fromString(ViewletMapUtil.optionalString(attributes, "takeWidth", null))
                    view.takeHeight = TakeSize.fromString(ViewletMapUtil.optionalString(attributes, "takeHeight", null))

                    // Generic view properties
                    ViewletUtil.applyGenericViewAttributes(view, attributes)
                    return true
                }
                return false
            }

            override fun canRecycle(view: View, attributes: Map<String, Any>): Boolean {
                return view is SpacerViewlet
            }

        }


        // ---
        // Static: helpers
        // ---

        fun getActionBarHeight(context: Context): Int {
            val typedValue = TypedValue()
            return if (context.theme.resolveAttribute(R.attr.actionBarSize, typedValue, true)) TypedValue.complexToDimensionPixelSize(typedValue.data, Resources.getSystem().displayMetrics) else 0
        }

        fun getNavigationBarHeight(context: Context): Int {
            // Check if a bar is present (with a workaround for emulators because it doesn't use the configuration correctly)
            val id = context.resources.getIdentifier("config_showNavigationBar", "bool", "android")
            var hasNavigationBar = false
            if (id > 0) {
                hasNavigationBar = context.resources.getBoolean(id)
                if (Build.HARDWARE.toLowerCase().equals("ranchu") || Build.MODEL.toLowerCase().contains("android sdk built for")) { // Force true for common emulators
                    hasNavigationBar = true
                }
            }

            // Return height if available
            if (hasNavigationBar) {
                val resourceId = context.resources.getIdentifier("navigation_bar_height", "dimen", "android")
                return if (resourceId > 0) context.resources.getDimensionPixelSize(resourceId) else 0
            }
            return 0
        }

    }


    // ---
    // Members
    // ---

    var takeWidth = TakeSize.None
    var takeHeight = TakeSize.None


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
        // No implementation
    }


    // ---
    // Custom layout
    // ---

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        // Basic size
        var width = paddingLeft + paddingRight
        var height = paddingTop + paddingBottom
        val widthSpec = MeasureSpec.getMode(widthMeasureSpec)
        val heightSpec = MeasureSpec.getMode(heightMeasureSpec)

        // Apply sizes taken from standard controls
        if (takeHeight == TakeSize.TopBar) {
            height += getActionBarHeight(context)
        } else if (takeHeight == TakeSize.BottomBar) {
            height += getNavigationBarHeight(context)
        }

        // Apply limits and return result
        if (widthSpec == MeasureSpec.EXACTLY) {
            width = MeasureSpec.getSize(widthMeasureSpec)
        } else if (widthSpec == MeasureSpec.AT_MOST) {
            width = Math.min(width, MeasureSpec.getSize(widthMeasureSpec))
        }
        if (heightSpec == MeasureSpec.EXACTLY) {
            height = MeasureSpec.getSize(heightMeasureSpec)
        } else if (heightSpec == MeasureSpec.AT_MOST) {
            height = Math.min(height, MeasureSpec.getSize(heightMeasureSpec))
        }
        setMeasuredDimension(width, height)
    }


    // ---
    // Take size enum
    // ---

    enum class TakeSize(val value: String) {

        None(""),
        TopBar("topBar"),
        BottomBar("bottomBar");

        companion object {

            fun fromString(string: String?): TakeSize {
                for (enum in TakeSize.values()) {
                    if (enum.value == string) {
                        return enum
                    }
                }
                return None
            }

        }

    }

}
