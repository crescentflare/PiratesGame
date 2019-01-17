package com.crescentflare.piratesgame.components.containers

import android.annotation.TargetApi
import android.content.Context
import android.os.Build
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import com.crescentflare.piratesgame.components.utility.ViewletUtil
import com.crescentflare.piratesgame.infrastructure.events.AppEvent
import com.crescentflare.piratesgame.infrastructure.events.AppEventLabeledSender
import com.crescentflare.piratesgame.infrastructure.events.AppEventObserver
import com.crescentflare.unilayout.containers.UniLinearContainer
import com.crescentflare.viewletcreator.ViewletCreator
import com.crescentflare.viewletcreator.binder.ViewletBinder
import com.crescentflare.viewletcreator.utility.ViewletMapUtil
import java.lang.ref.WeakReference

/**
 * Container view: basic layout container for horizontally or vertically aligned views
 */
class LinearContainerView : UniLinearContainer, AppEventObserver, AppEventLabeledSender {

    // ---
    // Statics
    // ---

    companion object {

        // ---
        // Viewlet integration
        // ---

        val viewlet: ViewletCreator.Viewlet = object : ViewletCreator.Viewlet {
            override fun create(context: Context): View {
                return LinearContainerView(context)
            }

            override fun update(view: View, attributes: Map<String, Any>, parent: ViewGroup?, binder: ViewletBinder?): Boolean {
                if (view is LinearContainerView) {
                    // Orientation
                    val linearContainer = view
                    val orientationString = ViewletMapUtil.optionalString(attributes, "orientation", "")
                    if (orientationString == "horizontal") {
                        linearContainer.orientation = UniLinearContainer.HORIZONTAL
                    } else {
                        linearContainer.orientation = UniLinearContainer.VERTICAL
                    }

                    // Create or update children
                    ViewletUtil.createSubviews(linearContainer, linearContainer, attributes, attributes["items"], binder)

                    // Generic view properties
                    ViewletUtil.applyGenericViewAttributes(view, attributes)

                    // Event handling
                    linearContainer.tapEvent = AppEvent.fromObject(attributes["tapEvent"])

                    // Forward event observer
                    if (parent is AppEventObserver) {
                        linearContainer.eventObserver = parent
                    }
                    return true
                }
                return false
            }

            override fun canRecycle(view: View, attributes: Map<String, Any>): Boolean {
                return view is LinearContainerView
            }
        }
    }


    // ---
    // Members
    // ---

    private var eventObserverReference : WeakReference<AppEventObserver>? = null


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
        orientation = UniLinearContainer.VERTICAL
    }


    // ---
    // Configurable values
    // ---

    var eventObserver: AppEventObserver?
        get() = eventObserverReference?.get()
        set(newValue) {
            if (newValue != null) {
                eventObserverReference = WeakReference(newValue)
            } else {
                eventObserverReference = null
            }
        }

    var tapEvent: AppEvent? = null
        set(tapEvent) {
            field = tapEvent
            if (hasOnClickListeners()) {
                setOnClickListener(null)
            }
            if (this.tapEvent != null) {
                setOnClickListener(object : View.OnClickListener {
                    override fun onClick(view: View) {
                        val currentTapEvent = this@LinearContainerView.tapEvent
                        if (currentTapEvent != null) {
                            observedEvent(currentTapEvent, senderLabel)
                        }
                    }
                })
            }
        }


    // ---
    // Interaction
    // ---

    override val senderLabel: String?
        get() = contentDescription?.toString()

    override fun observedEvent(event: AppEvent, sender: Any?) {
        eventObserver?.observedEvent(event, sender)
    }

}