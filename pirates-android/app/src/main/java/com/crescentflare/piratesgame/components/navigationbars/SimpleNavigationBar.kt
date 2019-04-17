package com.crescentflare.piratesgame.components.navigationbars

import android.annotation.TargetApi
import android.content.Context
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.support.v4.content.ContextCompat
import android.util.AttributeSet
import android.view.View
import com.crescentflare.jsoninflator.JsonInflatable
import com.crescentflare.jsoninflator.JsonLoader
import com.crescentflare.jsoninflator.binder.InflatableRef
import com.crescentflare.jsoninflator.binder.InflatorAnnotationBinder
import com.crescentflare.jsoninflator.binder.InflatorBinder
import com.crescentflare.jsoninflator.utility.InflatorMapUtil
import com.crescentflare.piratesgame.R
import com.crescentflare.piratesgame.components.basicviews.ButtonView
import com.crescentflare.piratesgame.components.containers.LinearContainerView
import com.crescentflare.piratesgame.components.simpleviewlets.ImageViewlet
import com.crescentflare.piratesgame.components.utility.ImageSource
import com.crescentflare.piratesgame.components.utility.NavigationBarComponent
import com.crescentflare.piratesgame.components.utility.ViewletUtil
import com.crescentflare.piratesgame.infrastructure.events.AppEvent
import com.crescentflare.piratesgame.infrastructure.events.AppEventObserver
import com.crescentflare.unilayout.views.UniImageView
import com.crescentflare.unilayout.views.UniTextView


/**
 * Navigation bar: a simple navigation bar, the default for apps
 */
class SimpleNavigationBar : LinearContainerView, NavigationBarComponent {

    // --
    // Statics
    // --

    companion object {

        // --
        // Static: reference to layout resource
        // --

        private const val layoutResource = R.raw.simple_navigation_bar


        // --
        // Static: viewlet integration
        // --

        val viewlet: JsonInflatable = object : JsonInflatable {

            override fun create(context: Context): Any {
                return SimpleNavigationBar(context)
            }

            override fun update(mapUtil: InflatorMapUtil, obj: Any, attributes: Map<String, Any>, parent: Any?, binder: InflatorBinder?): Boolean {
                if (obj is SimpleNavigationBar) {
                    // Apply text
                    obj.title = ViewletUtil.localizedString(
                        obj.context,
                        mapUtil.optionalString(attributes, "localizedTitle", null),
                        mapUtil.optionalString(attributes, "title", null)
                    )

                    // Apply actions
                    obj.backIcon = ImageSource.fromObject(attributes["backIcon"])
                    obj.menuActionIcon = ImageSource.fromObject(attributes["menuActionIcon"])
                    obj.menuActionText = ViewletUtil.localizedString(
                        obj.context,
                        mapUtil.optionalString(attributes, "localizedMenuActionText", null),
                        mapUtil.optionalString(attributes, "menuActionText", null)
                    )
                    obj.backEvent = AppEvent.fromObject(attributes["backEvent"])
                    obj.menuActionEvent = AppEvent.fromObject(attributes["menuActionEvent"])

                    // Generic view properties
                    ViewletUtil.applyGenericViewAttributes(mapUtil, obj, attributes)

                    // Forward event observer
                    if (parent is AppEventObserver) {
                        obj.eventObserver = parent
                    }
                    return true
                }
                return false
            }

            override fun canRecycle(mapUtil: InflatorMapUtil, obj: Any, attributes: Map<String, Any>): Boolean {
                return obj is SimpleNavigationBar
            }

        }

    }


    // --
    // Bound views
    // --

    @InflatableRef("statusContainer")
    private var statusContainerView: View? = null

    @InflatableRef("barContainer")
    private var barContainerView: LinearContainerView? = null

    @InflatableRef("title")
    private var titleView: UniTextView? = null

    @InflatableRef("backIcon")
    private var backIconView: View? = null

    @InflatableRef("backIconImage")
    private var backIconImageView: UniImageView? = null

    @InflatableRef("menuActionIcon")
    private var menuActionIconView: View? = null

    @InflatableRef("menuActionIconImage")
    private var menuActionIconImageView: UniImageView? = null

    @InflatableRef("menuActionText")
    private var menuActionTextView: ButtonView? = null

    @InflatableRef("backIconSpacer")
    private var backIconSpacerView: View? = null

    @InflatableRef("menuActionIconSpacer")
    private var menuActionIconSpacerView: View? = null

    @InflatableRef("menuActionTextSpacer")
    private var menuActionTextSpacerView: ButtonView? = null


    // --
    // Members
    // --

    override val translucent: Boolean
        get() {
            val backgroundDrawable = barContainerView?.background
            if (backgroundDrawable is ColorDrawable) {
                return backgroundDrawable.color.toLong() and 0xff000000 != 0xff000000
            }
            return true
        }

    override val lightContent: Boolean
        get() {
            val backgroundDrawable = barContainerView?.background
            if (backgroundDrawable is ColorDrawable) {
                return ViewletUtil.colorIntensity(backgroundDrawable.color) < 0.25
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
        ViewletUtil.assertInflateOn(this, JsonLoader.instance.loadAttributes(context, layoutResource),null, InflatorAnnotationBinder(this))
    }


    // --
    // Configurable values
    // --

    override var statusBarInset: Int = 0
        set(statusBarInset) {
            field = statusBarInset
            statusContainerView?.layoutParams?.height = statusBarInset
        }

    var title: String?
        get() = titleView?.text?.toString()
        set(title) {
            titleView?.text = title
        }

    var backIcon: ImageSource? = null
        set(icon) {
            field = icon
            ImageViewlet.applyImageSource(backIconImageView, icon)
            backIconView?.visibility = if (icon != null) VISIBLE else GONE
            backIconSpacerView?.visibility = if (icon != null) INVISIBLE else GONE
        }

    var menuActionIcon: ImageSource? = null
        set(icon) {
            field = icon
            ImageViewlet.applyImageSource(menuActionIconImageView, icon)
            menuActionIconView?.visibility = if (icon != null) VISIBLE else GONE
            menuActionIconSpacerView?.visibility = if (icon != null) INVISIBLE else GONE
        }

    var menuActionText: String? = null
        set(text) {
            field = text
            menuActionTextView?.text = text
            menuActionTextSpacerView?.text = text
            menuActionTextView?.visibility = if (text != null) VISIBLE else GONE
            menuActionTextSpacerView?.visibility = if (text != null) INVISIBLE else GONE
        }

    var backEvent: AppEvent? = null
        set(backEvent) {
            field = backEvent
            if (backEvent != null) {
                backIconView?.setOnClickListener {
                    val currentBackEvent = this@SimpleNavigationBar.backEvent
                    if (currentBackEvent != null) {
                        eventObserver?.observedEvent(currentBackEvent, this@SimpleNavigationBar)
                    }
                }
                backIconView?.setBackgroundResource(if (lightContent) R.drawable.navigation_bar_highlight else R.drawable.navigation_bar_highlight_inverted)
            } else {
                backIconView?.setOnClickListener(null)
                backIconView?.setBackgroundResource(0)
            }
        }

    var menuActionEvent: AppEvent? = null
        set(menuActionEvent) {
            field = menuActionEvent
            menuActionTextView?.tapEvent = menuActionEvent
            if (menuActionEvent != null) {
                menuActionIconView?.setOnClickListener {
                    val currentMenuActionEvent = this@SimpleNavigationBar.menuActionEvent
                    if (currentMenuActionEvent != null) {
                        eventObserver?.observedEvent(currentMenuActionEvent, this@SimpleNavigationBar)
                    }
                }
                menuActionIconView?.setBackgroundResource(if (lightContent) R.drawable.navigation_bar_highlight else R.drawable.navigation_bar_highlight_inverted)
            } else {
                menuActionIconView?.setOnClickListener(null)
                menuActionIconView?.setBackgroundResource(0)
            }
        }

    override fun setBackgroundColor(color: Int) {
        barContainerView?.setBackgroundColor(color)
        var newColor = color
        if (newColor == ContextCompat.getColor(context, R.color.colorPrimary)) {
            newColor = ContextCompat.getColor(context, R.color.colorPrimaryDark)
        } else {
            newColor = darkenColor(newColor)
        }
        statusContainerView?.setBackgroundColor(newColor)
        titleView?.setTextColor(ContextCompat.getColor(context, if (lightContent) R.color.textInverted else R.color.text))
        backIconImageView?.colorFilter = PorterDuffColorFilter(ContextCompat.getColor(context, if (lightContent) R.color.textInverted else R.color.text), PorterDuff.Mode.SRC_IN)
        menuActionIconImageView?.colorFilter = PorterDuffColorFilter(ContextCompat.getColor(context, if (lightContent) R.color.textInverted else R.color.text), PorterDuff.Mode.SRC_IN)
        menuActionTextView?.setColorStyle(if (lightContent) ButtonView.ColorStyle.NavigationBarInverted else ButtonView.ColorStyle.NavigationBar)
        if (backEvent != null) {
            backIconView?.setBackgroundResource(if (lightContent) R.drawable.navigation_bar_highlight else R.drawable.navigation_bar_highlight_inverted)
        }
        if (menuActionEvent != null) {
            menuActionIconView?.setBackgroundResource(if (lightContent) R.drawable.navigation_bar_highlight else R.drawable.navigation_bar_highlight_inverted)
        }
    }


    // --
    // Helpers
    // --

    private fun darkenColor(color: Int): Int {
        var alpha = (color.toLong() and 0xff000000) shr 24
        if (alpha < 255) {
            alpha = Math.min(alpha + (255 - alpha) * 128 / 255, 255)
        }
        val red = (color.toLong() and 0xff0000) shr 16
        val green = (color.toLong() and 0xff00) shr 8
        val blue = color.toLong() and 0xff
        val finalColor = (alpha shl 24) or ((red * 3 / 4) shl 16) or ((green * 3 / 4) shl 8) or (blue * 3 / 4)
        return finalColor.toInt()
    }

}
