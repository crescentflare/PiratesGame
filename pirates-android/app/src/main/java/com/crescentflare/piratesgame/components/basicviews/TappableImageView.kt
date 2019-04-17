package com.crescentflare.piratesgame.components.basicviews

import android.annotation.TargetApi
import android.content.Context
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.os.Build
import android.util.AttributeSet
import android.view.ViewGroup
import android.widget.ImageView
import com.crescentflare.jsoninflator.JsonInflatable
import com.crescentflare.jsoninflator.binder.InflatorBinder
import com.crescentflare.jsoninflator.utility.InflatorMapUtil
import com.crescentflare.piratesgame.components.containers.FrameContainerView
import com.crescentflare.piratesgame.components.simpleviewlets.ImageViewlet
import com.crescentflare.piratesgame.components.utility.ImageSource
import com.crescentflare.piratesgame.components.utility.ViewletUtil
import com.crescentflare.piratesgame.infrastructure.events.AppEvent
import com.crescentflare.piratesgame.infrastructure.events.AppEventObserver
import com.crescentflare.unilayout.helpers.UniLayoutParams
import com.crescentflare.unilayout.views.UniImageView

/**
 * Basic view: an image view with tap interaction
 */
class TappableImageView : FrameContainerView {

    // --
    // Static: viewlet integration
    // --

    companion object {

        val viewlet: JsonInflatable = object : JsonInflatable {

            override fun create(context: Context): Any {
                return TappableImageView(context)
            }

            override fun update(mapUtil: InflatorMapUtil, obj: Any, attributes: Map<String, Any>, parent: Any?, binder: InflatorBinder?): Boolean {
                if (obj is TappableImageView) {
                    // Apply image states
                    obj.source = ImageSource.fromObject(attributes["source"])
                    obj.highlightedSource = ImageSource.fromObject(attributes["highlightedSource"])

                    // Apply separate colorization
                    val highlightedColor = mapUtil.optionalColor(attributes, "highlightedColor", 0)
                    obj.highlightedColor = if (highlightedColor != 0) highlightedColor else null

                    // Generic view properties
                    ViewletUtil.applyGenericViewAttributes(mapUtil, obj, attributes)

                    // Event handling
                    obj.tapEvent = AppEvent.fromObject(attributes["tapEvent"])

                    // Forward event observer
                    if (parent is AppEventObserver) {
                        obj.eventObserver = parent
                    }
                    return true
                }
                return false
            }

            override fun canRecycle(mapUtil: InflatorMapUtil, obj: Any, attributes: Map<String, Any>): Boolean {
                return obj is TappableImageView
            }

        }

    }


    // --
    // Members
    // --

    private val normalImageView: UniImageView
    private val highlightedImageView: UniImageView


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
        // Add views
        normalImageView = UniImageView(context)
        highlightedImageView = UniImageView(context)
        addView(normalImageView)
        addView(highlightedImageView)

        // Apply image settings
        for (imageView in listOf(normalImageView, highlightedImageView)) {
            val layoutParams = UniLayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            layoutParams.horizontalGravity = 0.5f
            layoutParams.verticalGravity = 0.5f
            imageView.layoutParams = layoutParams
            imageView.scaleType = ImageView.ScaleType.CENTER_INSIDE
        }

        // Default state
        highlightedImageView.visibility = INVISIBLE
    }


    // --
    // Configurable values
    // --

    var source: ImageSource? = null
        set(source) {
            field = source
            ImageViewlet.applyImageSource(normalImageView, source)
        }

    var highlightedSource: ImageSource? = null
        set(highlightedSource) {
            field = highlightedSource
            ImageViewlet.applyImageSource(highlightedImageView, highlightedSource)
            updateState()
        }

    var highlightedColor: Int? = null
        set(highlightedColor) {
            field = highlightedColor
            val tintColor = highlightedColor ?: highlightedSource?.tintColor
            if (tintColor != null) {
                highlightedImageView.colorFilter = PorterDuffColorFilter(tintColor, PorterDuff.Mode.SRC_IN)
            } else {
                highlightedImageView.colorFilter = null
            }
            updateState()
        }


    // --
    // Interaction
    // --

    override fun drawableStateChanged() {
        super.drawableStateChanged()
        updateState()
    }


    // --
    // Helper
    // --

    private fun updateState() {
        var pressed = false
        for (checkState in drawableState) {
            if (checkState == android.R.attr.state_pressed) {
                pressed = true
                break
            }
        }
        val tintColor = if (pressed) highlightedColor ?: source?.tintColor else source?.tintColor
        normalImageView.visibility = if (!pressed || highlightedSource == null) VISIBLE else INVISIBLE
        highlightedImageView.visibility = if (pressed && highlightedSource != null) VISIBLE else INVISIBLE
        if (tintColor != null) {
            normalImageView.colorFilter = PorterDuffColorFilter(tintColor, PorterDuff.Mode.SRC_IN)
        } else {
            normalImageView.colorFilter = null
        }
    }

}
