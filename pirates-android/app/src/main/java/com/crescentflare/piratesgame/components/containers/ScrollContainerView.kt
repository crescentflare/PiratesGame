package com.crescentflare.piratesgame.components.containers

import android.annotation.TargetApi
import android.content.Context
import android.os.Build
import android.os.Handler
import android.util.AttributeSet
import android.view.View
import com.crescentflare.piratesgame.components.utility.ViewletUtil
import com.crescentflare.piratesgame.infrastructure.events.AppEvent
import com.crescentflare.piratesgame.infrastructure.events.AppEventObserver
import com.crescentflare.piratesgame.infrastructure.events.AppEventType
import com.crescentflare.unilayout.containers.UniVerticalScrollContainer
import com.crescentflare.jsoninflator.JsonInflatable
import com.crescentflare.jsoninflator.binder.InflatorBinder
import com.crescentflare.jsoninflator.utility.InflatorMapUtil
import com.crescentflare.piratesgame.infrastructure.inflator.Inflators

import java.lang.ref.WeakReference

/**
 * Container view: a scroll view specifically made for the layout containers (like LinearContainerView)
 */
class ScrollContainerView : UniVerticalScrollContainer, AppEventObserver {

    // --
    // Static: viewlet integration
    // --

    companion object {

        val viewlet: JsonInflatable = object : JsonInflatable {

            override fun create(context: Context): Any {
                return ScrollContainerView(context)
            }

            override fun update(mapUtil: InflatorMapUtil, obj: Any, attributes: Map<String, Any>, parent: Any?, binder: InflatorBinder?): Boolean {
                if (obj is ScrollContainerView) {
                    // Set fill content mode
                    obj.isFillViewport = mapUtil.optionalBoolean(attributes, "fillContent", false)

                    // Set content
                    val item = Inflators.viewlet.attributesForNestedInflatable(attributes["item"])
                    val recycling = mapUtil.optionalBoolean(attributes, "recycling", false)
                    if (recycling && Inflators.viewlet.canRecycle(obj.contentView, item)) {
                        val contentView = obj.contentView
                        if (contentView != null && item != null) {
                            Inflators.viewlet.inflateOn(contentView, item, null)
                            ViewletUtil.applyLayoutAttributes(mapUtil, contentView, item)
                            ViewletUtil.bindRef(mapUtil, contentView, item, binder)
                        }
                    } else {
                        // First empty content
                        obj.contentView = null

                        // Set content view
                        val result = Inflators.viewlet.inflate(obj.context, item, obj, binder)
                        if (result is View && item != null) {
                            obj.addView(result)
                            ViewletUtil.applyLayoutAttributes(mapUtil, result, item)
                            ViewletUtil.bindRef(mapUtil, result, item, binder)
                        }
                    }

                    // Generic view properties
                    ViewletUtil.applyGenericViewAttributes(mapUtil, obj, attributes)

                    // Forward event observer
                    if (parent is AppEventObserver) {
                        obj.eventObserver = parent
                    }

                    // Set pull to refresh (after interaction listener is set)
                    obj.pullToRefreshEvent = AppEvent.fromObject(attributes["pullToRefreshEvent"])
                    return true
                }
                return false
            }

            override fun canRecycle(mapUtil: InflatorMapUtil, obj: Any, attributes: Map<String, Any>): Boolean {
                return obj is ScrollContainerView
            }

        }

    }


    // --
    // Members
    // --

    private var eventObserverReference : WeakReference<AppEventObserver>? = null
    private var atScrollTop = false
    private var lastScroll = -1


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
        clipToPadding = false
        Handler().postDelayed({
            onScrollStartChanged(scrollY == 0)
            lastScroll = scrollY
        }, 0)
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

    var pullToRefreshEvent: AppEvent? = null
        set(pullToRefreshEvent) {
            if (field != pullToRefreshEvent) {
                if (field != null && atScrollTop) {
                    val event = AppEvent(AppEventType.RegisterPullToRefresh.value + "://remove")
                    eventObserver?.observedEvent(event, field)
                }
                field = pullToRefreshEvent
                if (pullToRefreshEvent != null && atScrollTop) {
                    val event = AppEvent(AppEventType.RegisterPullToRefresh.value + "://add")
                    eventObserver?.observedEvent(event, field)
                }
            }
        }

    var contentView: View?
        get() {
            val childCount = childCount
            return if (childCount > 0) {
                getChildAt(0)
            } else null
        }
        set(contentView) {
            val childCount = childCount
            for (i in childCount - 1 downTo 0) {
                removeViewAt(i)
            }
            if (contentView != null) {
                addView(contentView, 0)
            }
        }


    // --
    // Interaction
    // --

    fun onScrollStartChanged(atScrollStart: Boolean) {
        atScrollTop = atScrollStart
        if (pullToRefreshEvent != null) {
            if (atScrollTop) {
                val event = AppEvent(AppEventType.RegisterPullToRefresh.value + "://add")
                eventObserver?.observedEvent(event, pullToRefreshEvent)
            } else {
                val event = AppEvent(AppEventType.RegisterPullToRefresh.value + "://remove")
                eventObserver?.observedEvent(event, pullToRefreshEvent)
            }
        }
    }

    override fun onScrollChanged(left: Int, top: Int, previousLeft: Int, previousTop: Int) {
        super.onScrollChanged(left, top, previousLeft, previousTop)
        if (top == 0 && lastScroll != 0) {
            onScrollStartChanged(true)
        } else if (top != 0 && lastScroll == 0) {
            onScrollStartChanged(false)
        }
        lastScroll = top
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        if (pullToRefreshEvent != null && atScrollTop) {
            val event = AppEvent(AppEventType.RegisterPullToRefresh.value + "://remove")
            eventObserver?.observedEvent(event, pullToRefreshEvent)
        }
    }

    override fun observedEvent(event: AppEvent, sender: Any?) {
        eventObserver?.observedEvent(event, sender)
    }

}
