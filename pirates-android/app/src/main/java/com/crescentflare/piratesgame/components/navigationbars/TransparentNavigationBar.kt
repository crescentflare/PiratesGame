package com.crescentflare.piratesgame.components.navigationbars

import android.annotation.TargetApi
import android.content.Context
import android.os.Build
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import com.crescentflare.piratesgame.components.utility.NavigationBarComponent
import com.crescentflare.piratesgame.components.utility.ViewletUtil
import com.crescentflare.viewletcreator.binder.ViewletBinder
import com.crescentflare.viewletcreator.ViewletCreator
import com.crescentflare.unilayout.views.UniView
import com.crescentflare.viewletcreator.utility.ViewletMapUtil


/**
 * Navigation bar: an invisible bar, used if no navigation bar is specified
 */
class TransparentNavigationBar : UniView, NavigationBarComponent {

    // --
    // Static: viewlet integration
    // --

    companion object {

        val viewlet: ViewletCreator.Viewlet = object : ViewletCreator.Viewlet {

            override fun create(context: Context): View {
                return TransparentNavigationBar(context)
            }

            override fun update(view: View, attributes: Map<String, Any>, parent: ViewGroup?, binder: ViewletBinder?): Boolean {
                if (view is TransparentNavigationBar) {
                    // Bar properties
                    view.lightContent = ViewletMapUtil.optionalBoolean(attributes, "lightContent", false)

                    // Generic view properties
                    ViewletUtil.applyGenericViewAttributes(view, attributes)
                    return true
                }
                return false
            }

            override fun canRecycle(view: View, attributes: Map<String, Any>): Boolean {
                return view is TransparentNavigationBar
            }

        }

    }


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
        // No implementation
    }


    // --
    // Configurable values
    // --

    override var lightContent = false
    override var statusBarInset: Int = 0

}
