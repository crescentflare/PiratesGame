package com.crescentflare.piratesgame.components.containers

import android.annotation.TargetApi
import android.content.Context
import android.content.res.Resources
import android.graphics.Rect
import android.os.Build
import android.util.AttributeSet
import android.util.TypedValue
import android.view.View
import android.view.ViewGroup
import com.crescentflare.jsoninflator.JsonInflatable
import com.crescentflare.jsoninflator.binder.InflatorBinder
import com.crescentflare.jsoninflator.utility.InflatorMapUtil
import com.crescentflare.piratesgame.R
import com.crescentflare.piratesgame.components.utility.NavigationBarComponent
import com.crescentflare.piratesgame.components.utility.ViewletUtil
import com.crescentflare.piratesgame.infrastructure.events.AppEvent
import com.crescentflare.piratesgame.infrastructure.events.AppEventObserver
import com.crescentflare.piratesgame.infrastructure.inflator.Inflators
import java.lang.ref.WeakReference

/**
 * Container view: contains the layout framework of a page, a content view and navigation bar components with optional translucency
 */
class NavigationContainerView: ViewGroup, AppEventObserver {

    // --
    // Static: viewlet integration
    // --

    companion object {

        val viewlet: JsonInflatable = object : JsonInflatable {
            override fun create(context: Context): Any {
                return NavigationContainerView(context)
            }

            override fun update(mapUtil: InflatorMapUtil, obj: Any, attributes: Map<String, Any>, parent: Any?, binder: InflatorBinder?): Boolean {
                if (obj is NavigationContainerView) {
                    // Set content
                    val contentItem = Inflators.viewlet.attributesForNestedInflatable(attributes["content"])
                    val recycling = mapUtil.optionalBoolean(attributes, "recycling", false)
                    if (recycling && Inflators.viewlet.canRecycle(obj.contentView, contentItem)) {
                        val contentView = obj.contentView
                        if (contentView != null && contentItem != null) {
                            Inflators.viewlet.inflateOn(contentView, contentItem, null, binder)
                            ViewletUtil.applyLayoutAttributes(mapUtil, contentView, contentItem)
                            ViewletUtil.bindRef(mapUtil, contentView, contentItem, binder)
                        }
                    } else {
                        // First empty content
                        obj.contentView = null

                        // Set content view
                        val result = Inflators.viewlet.inflate(obj.context, contentItem, obj, binder)
                        if (result is View && contentItem != null) {
                            obj.contentView = result
                            ViewletUtil.applyLayoutAttributes(mapUtil, result, contentItem)
                            ViewletUtil.bindRef(mapUtil, result, contentItem, binder)
                        }
                    }

                    // Set top bar
                    val topBarItem = Inflators.viewlet.attributesForNestedInflatable(attributes["topBar"])
                    if (recycling && Inflators.viewlet.canRecycle(obj.topBarView, topBarItem)) {
                        val topBarView = obj.topBarView
                        if (topBarView != null && topBarItem != null) {
                            Inflators.viewlet.inflateOn(topBarView, topBarItem, null, binder)
                            ViewletUtil.applyLayoutAttributes(mapUtil, topBarView, topBarItem)
                            ViewletUtil.bindRef(mapUtil, topBarView, topBarItem, binder)
                        }
                    } else {
                        // First empty top bar
                        obj.topBarView = null

                        // Set top bar view
                        val result = Inflators.viewlet.inflate(obj.context, topBarItem, obj, binder)
                        if (result is View && topBarItem != null) {
                            obj.topBarView = result
                            ViewletUtil.applyLayoutAttributes(mapUtil, result, topBarItem)
                            ViewletUtil.bindRef(mapUtil, result, topBarItem, binder)
                        }
                    }

                    // Set bottom bar
                    val bottomBarItem = Inflators.viewlet.attributesForNestedInflatable(attributes["bottomBar"])
                    if (recycling && Inflators.viewlet.canRecycle(obj.bottomBarView, bottomBarItem)) {
                        val bottomBarView = obj.bottomBarView
                        if (bottomBarView != null && bottomBarItem != null) {
                            Inflators.viewlet.inflateOn(bottomBarView, bottomBarItem, null, binder)
                            ViewletUtil.applyLayoutAttributes(mapUtil, bottomBarView, bottomBarItem)
                            ViewletUtil.bindRef(mapUtil, bottomBarView, bottomBarItem, binder)
                        }
                    } else {
                        // First empty bottom bar
                        obj.bottomBarView = null

                        // Set bottom bar view
                        val result = Inflators.viewlet.inflate(obj.context, bottomBarItem, obj, binder)
                        if (result is View && bottomBarItem != null) {
                            obj.bottomBarView = result
                            ViewletUtil.applyLayoutAttributes(mapUtil, result, bottomBarItem)
                            ViewletUtil.bindRef(mapUtil, result, bottomBarItem, binder)
                        }
                    }

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
                return obj is NavigationContainerView
            }
        }
    }


    // --
    // Members
    // --

    private var eventObserverReference : WeakReference<AppEventObserver>? = null
    private val actionBarHeight: Int
    private var solidTopBar = false
    private var solidBottomBar = false


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
        actionBarHeight = getActionBarHeight()
    }


    // --
    // Configurable values
    // --

    var eventObserver: AppEventObserver?
        get() = eventObserverReference?.get()
        set(newValue) {
            eventObserverReference = if (newValue != null) {
                WeakReference(newValue)
            } else {
                null
            }
        }

    var contentView: View? = null
        set(contentView) {
            if (field != null) {
                removeView(field)
            }
            field = contentView
            if (field != null) {
                addView(field, 0)
            }
        }

    var topBarView: View? = null
        set(topBarView) {
            if (field != null) {
                removeView(field)
            }
            field = topBarView
            if (field != null) {
                addView(field)
            }
            solidTopBar = field != null && !((field as? NavigationBarComponent)?.translucent == true)
        }

    var bottomBarView: View? = null
        set(bottomBarView) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) { // Ignore on older Android versions
                if (field != null) {
                    removeView(field)
                }
                field = bottomBarView
                if (field != null) {
                    addView(field)
                }
                solidBottomBar = field != null && !((field as? NavigationBarComponent)?.translucent == true)
            }
        }


    // --
    // Interaction
    // --

    override fun observedEvent(event: AppEvent, sender: Any?) {
        eventObserver?.observedEvent(event, sender)
    }


    // --
    // Handle insets
    // --

    val safeInsets: Rect
        get() = Rect(0, if (!solidTopBar) transparentStatusBarHeight + actionBarHeight else 0, 0, if (!solidBottomBar) transparentNavigationBarHeight else 0)

    val transparentStatusBarHeight: Int
        get() = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && Resources.getSystem().displayMetrics.widthPixels < Resources.getSystem().displayMetrics.heightPixels) rootWindowInsets.stableInsetTop else 0

    private val transparentNavigationBarHeight: Int
        get() = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && Resources.getSystem().displayMetrics.widthPixels < Resources.getSystem().displayMetrics.heightPixels) rootWindowInsets.stableInsetBottom else 0


    // --
    // Custom layout
    // --

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val width = MeasureSpec.getSize(widthMeasureSpec)
        val height = MeasureSpec.getSize(heightMeasureSpec)
        val widthMode = MeasureSpec.getMode(widthMeasureSpec)
        val heightMode = MeasureSpec.getMode(heightMeasureSpec)
        if (widthMode == MeasureSpec.EXACTLY && heightMode == MeasureSpec.EXACTLY) {
            val totalTopBarHeight = actionBarHeight + transparentStatusBarHeight
            val bottomBarHeight = transparentNavigationBarHeight
            val topContentInset = if (solidTopBar) totalTopBarHeight else 0
            val bottomContentInset = if (solidBottomBar) bottomBarHeight else 0
            (topBarView as? NavigationBarComponent)?.statusBarInset = transparentStatusBarHeight
            topBarView?.measure(MeasureSpec.makeMeasureSpec(width, MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec(totalTopBarHeight, MeasureSpec.EXACTLY))
            bottomBarView?.measure(MeasureSpec.makeMeasureSpec(width, MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec(bottomBarHeight, MeasureSpec.EXACTLY))
            contentView?.measure(MeasureSpec.makeMeasureSpec(width, MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec(height - topContentInset - bottomContentInset, MeasureSpec.EXACTLY))
            setMeasuredDimension(width, height)
        } else {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        }
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        val totalTopBarHeight = actionBarHeight + transparentStatusBarHeight
        val bottomBarHeight = transparentNavigationBarHeight
        val topContentInset = if (solidTopBar) totalTopBarHeight else 0
        val bottomContentInset = if (solidBottomBar) bottomBarHeight else 0
        (topBarView as? NavigationBarComponent)?.statusBarInset = transparentStatusBarHeight
        topBarView?.layout(0, 0, right - left, totalTopBarHeight)
        bottomBarView?.layout(0, bottom - top - bottomBarHeight, right - left, bottom - top)
        contentView?.layout(0, topContentInset, right - left, bottom - top - bottomContentInset)
    }


    // --
    // Helper
    // --

    private fun getActionBarHeight(): Int {
        val typedValue = TypedValue()
        return if (context.theme.resolveAttribute(R.attr.actionBarSize, typedValue, true)) TypedValue.complexToDimensionPixelSize(typedValue.data, Resources.getSystem().displayMetrics) else 0
    }

}
