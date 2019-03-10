package com.crescentflare.piratesgame.components.navigationbars

import android.annotation.TargetApi
import android.content.Context
import android.graphics.Color
import android.os.Build
import android.support.v4.content.ContextCompat
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import com.crescentflare.piratesgame.R
import com.crescentflare.piratesgame.components.utility.NavigationBarComponent
import com.crescentflare.piratesgame.components.utility.ViewletUtil
import com.crescentflare.viewletcreator.binder.ViewletBinder
import com.crescentflare.viewletcreator.ViewletCreator
import com.crescentflare.unilayout.views.UniView
import com.crescentflare.viewletcreator.utility.ViewletMapUtil


/**
 * Navigation bar: a simple navigation bar, the default for apps
 */
class SolidNavigationBar : UniView, NavigationBarComponent {

    // --
    // Static: viewlet integration
    // --

    companion object {

        val viewlet: ViewletCreator.Viewlet = object : ViewletCreator.Viewlet {

            override fun create(context: Context): View {
                return SolidNavigationBar(context)
            }

            override fun update(view: View, attributes: Map<String, Any>, parent: ViewGroup?, binder: ViewletBinder?): Boolean {
                if (view is SolidNavigationBar) {
                    // Bar properties
                    view.lightContent = ViewletMapUtil.optionalBoolean(attributes, "lightContent", false)

                    // Generic view properties
                    ViewletUtil.applyGenericViewAttributes(view, attributes)
                    return true
                }
                return false
            }

            override fun canRecycle(view: View, attributes: Map<String, Any>): Boolean {
                return view is SolidNavigationBar
            }

        }

    }


    // --
    // Members
    // --

    override val translucent = false


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
    // Configurable values
    // --

    override var lightContent = false
        set(lightContent) {
            field = lightContent
            setBackgroundColor(ContextCompat.getColor(context, if (lightContent) R.color.primary else Color.WHITE))
        }

}
