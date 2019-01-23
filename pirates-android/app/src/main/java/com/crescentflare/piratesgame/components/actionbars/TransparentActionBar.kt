package com.crescentflare.piratesgame.components.actionbars

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
import com.crescentflare.piratesgame.page.utility.ActionBarComponent
import com.crescentflare.viewletcreator.utility.ViewletMapUtil
import com.crescentflare.viewletcreator.binder.ViewletBinder
import com.crescentflare.viewletcreator.ViewletCreator
import com.crescentflare.unilayout.views.UniView


/**
 * Action bar: an invisible bar, used by default
 */
class TransparentActionBar : UniView, ActionBarComponent {

    // ---
    // Static: viewlet integration
    // ---

    companion object {

        val viewlet: ViewletCreator.Viewlet = object : ViewletCreator.Viewlet {

            override fun create(context: Context): View {
                return TransparentActionBar(context)
            }

            override fun update(view: View, attributes: Map<String, Any>, parent: ViewGroup?, binder: ViewletBinder?): Boolean {
                if (view is TransparentActionBar) {
                    // Bar properties
                    view.lightContent = ViewletMapUtil.optionalBoolean(attributes, "lightContent", false)

                    // Generic view properties
                    ViewletUtil.applyGenericViewAttributes(view, attributes)
                    return true
                }
                return false
            }

            override fun canRecycle(view: View, attributes: Map<String, Any>): Boolean {
                return view is TransparentActionBar
            }

        }

    }


    // ---
    // Members
    // ---

    override val translucent = true


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
    // Configurable values
    // ---

    override var lightContent = false

}
