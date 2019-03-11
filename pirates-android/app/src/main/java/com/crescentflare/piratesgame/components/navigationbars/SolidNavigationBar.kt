package com.crescentflare.piratesgame.components.navigationbars

import android.annotation.TargetApi
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.os.Build
import android.support.v4.content.ContextCompat
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import com.crescentflare.piratesgame.R
import com.crescentflare.piratesgame.components.containers.FrameContainerView
import com.crescentflare.piratesgame.components.containers.LinearContainerView
import com.crescentflare.piratesgame.components.utility.NavigationBarComponent
import com.crescentflare.piratesgame.components.utility.ViewletUtil
import com.crescentflare.unilayout.views.UniTextView
import com.crescentflare.viewletcreator.binder.ViewletBinder
import com.crescentflare.viewletcreator.ViewletCreator
import com.crescentflare.unilayout.views.UniView
import com.crescentflare.viewletcreator.ViewletLoader
import com.crescentflare.viewletcreator.binder.ViewletAnnotationBinder
import com.crescentflare.viewletcreator.binder.ViewletRef
import com.crescentflare.viewletcreator.utility.ViewletMapUtil



/**
 * Navigation bar: a simple navigation bar, the default for apps
 */
class SolidNavigationBar : FrameContainerView, NavigationBarComponent {

    // --
    // Statics
    // --

    companion object {

        // --
        // Static: reference to layout resource
        // --

        const val layoutResource = R.raw.solid_navigation_bar


        // --
        // Static: viewlet integration
        // --

        val viewlet: ViewletCreator.Viewlet = object : ViewletCreator.Viewlet {

            override fun create(context: Context): View {
                return SolidNavigationBar(context)
            }

            override fun update(view: View, attributes: Map<String, Any>, parent: ViewGroup?, binder: ViewletBinder?): Boolean {
                if (view is SolidNavigationBar) {
                    // Apply text
                    view.title = ViewletUtil.localizedString(
                        view.context,
                        ViewletMapUtil.optionalString(attributes, "localizedTitle", null),
                        ViewletMapUtil.optionalString(attributes, "title", null)
                    )

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
    // Bound views
    // --

    @ViewletRef("title")
    private var titleView: UniTextView? = null

    @ViewletRef("subContainer")
    private var subContainer: LinearContainerView? = null


    // --
    // Members
    // --

    override val translucent = false

    override val lightContent: Boolean
        get() {
            val backgroundDrawable = background
            if (backgroundDrawable is ColorDrawable) {
                val result = pickBestForegroundColor(backgroundDrawable.color, Color.WHITE, Color.BLACK)
                return result == Color.WHITE
            }
            return true
        }


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
        ViewletUtil.assertInflateOn(this, ViewletLoader.loadAttributes(context, layoutResource),null, ViewletAnnotationBinder(this))
    }


    // --
    // Configurable values
    // --

    override var statusBarInset: Int = 0
        set(statusBarInset) {
            field = statusBarInset
            setPadding(paddingLeft, statusBarInset, paddingRight, paddingBottom)
        }

    var title: String?
        get() = titleView?.text?.toString()
        set(title) {
            titleView?.text = title
        }

    override fun setBackgroundColor(color: Int) {
        subContainer?.setBackgroundColor(color)
        var newColor = color
        if (newColor == ContextCompat.getColor(context, R.color.colorPrimary)) {
            newColor = ContextCompat.getColor(context, R.color.colorPrimaryDark)
        } else {
            newColor = darkenColor(newColor)
        }
        super.setBackgroundColor(newColor)
        titleView?.setTextColor(ContextCompat.getColor(context, if (lightContent) R.color.textInverted else R.color.text ))
    }


    // --
    // Helpers
    // --

    private fun darkenColor(color: Int): Int {
        val shiftedAlpha = color.toLong() and 0xff000000
        val red = (color.toLong() and 0xff0000) shr 16
        val green = (color.toLong() and 0xff00) shr 8
        val blue = color.toLong() and 0xff
        val finalColor = shiftedAlpha or ((red * 3 / 4) shl 16) or ((green * 3 / 4) shl 8) or (blue * 3 / 4)
        return finalColor.toInt()
    }

    private fun pickBestForegroundColor(backgroundColor: Int, lightForegroundColor: Int, darkForegroundColor: Int): Int {
        val colorComponents = doubleArrayOf(
            (backgroundColor and 0xFF).toDouble() / 255.0,
            (backgroundColor and 0xFF00 shr 8).toDouble() / 255.0,
            (backgroundColor and 0xFF0000 shr 16).toDouble() / 255.0
        )
        for (i in colorComponents.indices) {
            if (colorComponents[i] <= 0.03928) {
                colorComponents[i] /= 12.92
            }
            colorComponents[i] = Math.pow((colorComponents[i] + 0.055) / 1.055, 2.4)
        }
        val intensity = 0.2126 * colorComponents[0] + 0.7152 * colorComponents[1] + 0.0722 * colorComponents[2]
        return if (intensity > 0.179) darkForegroundColor else lightForegroundColor
    }

}
