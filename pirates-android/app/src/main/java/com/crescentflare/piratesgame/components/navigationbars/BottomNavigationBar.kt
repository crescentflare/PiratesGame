package com.crescentflare.piratesgame.components.navigationbars

import android.annotation.TargetApi
import android.content.Context
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.util.AttributeSet
import com.crescentflare.jsoninflator.JsonInflatable
import com.crescentflare.jsoninflator.binder.InflatorBinder
import com.crescentflare.jsoninflator.utility.InflatorMapUtil
import com.crescentflare.piratesgame.components.utility.NavigationBarComponent
import com.crescentflare.piratesgame.components.utility.ViewletUtil
import com.crescentflare.unilayout.views.UniView


/**
 * Navigation bar: a bar used at the bottom under the back, home and open application icons
 */
class BottomNavigationBar : UniView, NavigationBarComponent {

    // --
    // Static: viewlet integration
    // --

    companion object {

        val viewlet: JsonInflatable = object : JsonInflatable {

            override fun create(context: Context): Any {
                return BottomNavigationBar(context)
            }

            override fun update(mapUtil: InflatorMapUtil, obj: Any, attributes: Map<String, Any>, parent: Any?, binder: InflatorBinder?): Boolean {
                if (obj is BottomNavigationBar) {
                    ViewletUtil.applyGenericViewAttributes(mapUtil, obj, attributes)
                    return true
                }
                return false
            }

            override fun canRecycle(mapUtil: InflatorMapUtil, obj: Any, attributes: Map<String, Any>): Boolean {
                return obj is BottomNavigationBar
            }

        }

    }


    // --
    // Members
    // --

    override val lightContent: Boolean
        get() {
            val backgroundDrawable = background
            if (backgroundDrawable is ColorDrawable) {
                return ViewletUtil.colorIntensity(backgroundDrawable.color) < 0.25
            }
            return true
        }

    override val translucent: Boolean
        get() {
            val backgroundDrawable = background
            if (backgroundDrawable is ColorDrawable) {
                return backgroundDrawable.color.toLong() and 0xff000000 != 0xff000000
            }
            return true
        }

    override var statusBarInset: Int = 0


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
    // Color override
    // --

    override fun setBackgroundColor(color: Int) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O && ViewletUtil.colorIntensity(color) >= 0.25) {
            // Force to black, older versions can't support icon colorization
            super.setBackgroundColor((color.toLong() and 0xff000000).toInt())
            return
        }
        super.setBackgroundColor(color)
    }

}
