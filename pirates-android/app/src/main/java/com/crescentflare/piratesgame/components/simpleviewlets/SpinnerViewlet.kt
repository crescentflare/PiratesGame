package com.crescentflare.piratesgame.components.simpleviewlets

import android.content.Context
import android.graphics.PorterDuff
import android.support.v4.content.ContextCompat
import android.view.View
import android.view.ViewGroup

import com.crescentflare.piratesgame.R
import com.crescentflare.piratesgame.components.utility.ViewletUtil
import com.crescentflare.unilayout.views.UniSpinnerView
import com.crescentflare.viewletcreator.ViewletCreator
import com.crescentflare.viewletcreator.binder.ViewletBinder
import com.crescentflare.viewletcreator.utility.ViewletMapUtil

/**
 * Basic view viewlet: a waiting spinner
 */
object SpinnerViewlet {

    // ---
    // Viewlet instance
    // ---

    val viewlet: ViewletCreator.Viewlet = object : ViewletCreator.Viewlet {

        override fun create(context: Context): View {
            return UniSpinnerView(context)
        }

        override fun update(view: View, attributes: Map<String, Any>, parent: ViewGroup, binder: ViewletBinder): Boolean {
            if (view is UniSpinnerView) {
                // Style
                if (ViewletMapUtil.optionalString(attributes, "style", "") == "inverted") {
                    view.indeterminateDrawable.setColorFilter(-0x1, PorterDuff.Mode.MULTIPLY)
                } else {
                    view.indeterminateDrawable.setColorFilter(ContextCompat.getColor(view.getContext(), R.color.colorPrimary), PorterDuff.Mode.MULTIPLY)
                }

                // Generic view properties
                ViewletUtil.applyGenericViewAttributes(view, attributes)
                return true
            }
            return false
        }

        override fun canRecycle(view: View, attributes: Map<String, Any>): Boolean {
            return view is UniSpinnerView
        }

    }

}
