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
import com.crescentflare.unilayout.containers.UniFrameContainer
import com.crescentflare.viewletcreator.ViewletCreator
import com.crescentflare.viewletcreator.binder.ViewletBinder
import com.crescentflare.viewletcreator.utility.ViewletMapUtil
import java.lang.ref.WeakReference


/**
 * Container view: basic layout container for overlapping views
 */
class FrameContainerView : UniFrameContainer, AppEventObserver, AppEventLabeledSender {

    // ---
    // Statics
    // ---

    companion object {

        // ---
        // Static: viewlet integration
        // ---

        val viewlet: ViewletCreator.Viewlet = object : ViewletCreator.Viewlet {
            override fun create(context: Context): View {
                return FrameContainerView(context)
            }

            override fun update(view: View, attributes: Map<String, Any>, parent: ViewGroup?, binder: ViewletBinder?): Boolean {
                if (view is FrameContainerView) {
                    // Set container name
                    val frameContainer = view
                    frameContainer.contentDescription = ViewletMapUtil.optionalString(attributes, "containerName", null)

                    // Create or update children
                    ViewletUtil.createSubviews(frameContainer, frameContainer, attributes, attributes["items"], binder)

                    // Generic view properties
                    ViewletUtil.applyGenericViewAttributes(view, attributes)

                    // Event handling
                    frameContainer.tapEvent = AppEvent.fromObject(attributes["tapEvent"])

                    // Forward event observer
                    if (parent is AppEventObserver) {
                        frameContainer.eventObserver = parent
                    }
                    return true
                }
                return false
            }

            override fun canRecycle(view: View, attributes: Map<String, Any>): Boolean {
                return view is FrameContainerView
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
        // No implementation
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
                        val currentTapEvent = this@FrameContainerView.tapEvent
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