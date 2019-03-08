package com.crescentflare.piratesgame.components.simpleviewlets

import android.annotation.TargetApi
import android.content.Context
import android.os.Build
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import com.crescentflare.piratesgame.components.utility.ViewletUtil
import com.crescentflare.piratesgame.page.views.ComponentActivityView
import com.crescentflare.unilayout.views.UniView
import com.crescentflare.viewletcreator.ViewletCreator
import com.crescentflare.viewletcreator.binder.ViewletBinder
import com.crescentflare.viewletcreator.utility.ViewletMapUtil


/**
 * Basic view viewlet: a spacing element
 */
class SpacerViewlet : UniView {

    // --
    // Static: viewlet integration
    // --

    companion object {

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

    }


    // --
    // Members
    // --

    var takeWidth = TakeSize.None
    var takeHeight = TakeSize.None


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
        // No implementation
    }


    // --
    // Custom layout
    // --

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        // Basic size
        var width = paddingLeft + paddingRight
        var height = paddingTop + paddingBottom
        val widthSpec = MeasureSpec.getMode(widthMeasureSpec)
        val heightSpec = MeasureSpec.getMode(heightMeasureSpec)

        // Apply sizes taken from standard controls
        if (takeHeight == TakeSize.TopSafeArea) {
            height += getComponentView()?.safeInsets?.top ?: 0
        } else if (takeHeight == TakeSize.BottomSafeArea) {
            height += getComponentView()?.safeInsets?.bottom ?: 0
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


    // --
    // Helper
    // --

    private fun getComponentView(): ComponentActivityView? {
        var checkParent: View? = this
        for (i in 1..32) { // Maximum of 32 iterations
            if (checkParent == null) {
                return null
            }
            if (checkParent is ComponentActivityView) {
                return checkParent
            }
            checkParent = checkParent.parent as? View
        }
        return null
    }


    // --
    // Take size enum
    // --

    enum class TakeSize(val value: String) {

        None(""),
        TopSafeArea("topSafeArea"),
        BottomSafeArea("bottomSafeArea");

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
