package com.crescentflare.piratesgame.components.navigationbars

import android.annotation.TargetApi
import android.content.Context
import android.graphics.Color
import android.os.Build
import android.util.AttributeSet
import android.view.View
import com.crescentflare.piratesgame.components.utility.NavigationBarComponent
import com.crescentflare.piratesgame.components.utility.ViewletUtil
import com.crescentflare.jsoninflator.JsonInflatable
import com.crescentflare.jsoninflator.JsonLoader
import com.crescentflare.jsoninflator.binder.InflatableRef
import com.crescentflare.jsoninflator.binder.InflatorAnnotationBinder
import com.crescentflare.jsoninflator.binder.InflatorBinder
import com.crescentflare.jsoninflator.utility.InflatorMapUtil
import com.crescentflare.piratesgame.R
import com.crescentflare.piratesgame.components.containers.FrameContainerView


/**
 * Navigation bar: an invisible bar, used if no navigation bar is specified, optionally set the status bar color
 */
class TransparentNavigationBar : FrameContainerView, NavigationBarComponent {

    // --
    // Statics
    // --

    companion object {

        // --
        // Static: reference to layout resource
        // --

        private const val layoutResource = R.raw.transparent_navigation_bar


        // --
        // Static: viewlet integration
        // --

        val viewlet: JsonInflatable = object : JsonInflatable {

            override fun create(context: Context): Any {
                return TransparentNavigationBar(context)
            }

            override fun update(mapUtil: InflatorMapUtil, obj: Any, attributes: Map<String, Any>, parent: Any?, binder: InflatorBinder?): Boolean {
                if (obj is TransparentNavigationBar) {
                    // Bar properties
                    obj.lightContent = mapUtil.optionalBoolean(attributes, "lightContent", false)
                    obj.statusBarColor = mapUtil.optionalColor(attributes, "statusBarColor", Color.TRANSPARENT)

                    // Generic view properties
                    ViewletUtil.applyGenericViewAttributes(mapUtil, obj, attributes)
                    return true
                }
                return false
            }

            override fun canRecycle(mapUtil: InflatorMapUtil, obj: Any, attributes: Map<String, Any>): Boolean {
                return obj is TransparentNavigationBar
            }

        }

    }


    // --
    // Bound views
    // --

    @InflatableRef("statusContainer")
    private var statusContainer: View? = null


    // --
    // Members
    // --

    override val translucent = true


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
        ViewletUtil.assertInflateOn(this, JsonLoader.instance.loadAttributes(context, layoutResource),null, InflatorAnnotationBinder(this))
    }


    // --
    // Configurable values
    // --

    var statusBarColor: Int = Color.TRANSPARENT
        set(statusBarColor) {
            field = statusBarColor
            statusContainer?.setBackgroundColor(statusBarColor)
        }

    override var lightContent = false

    override var statusBarInset: Int = 0
        set(statusBarInset) {
            field = statusBarInset
            statusContainer?.layoutParams?.height = statusBarInset
            requestLayout()
        }

}
