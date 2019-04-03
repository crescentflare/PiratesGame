package com.crescentflare.piratesgame.components.compoundviews

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.annotation.TargetApi
import android.content.Context
import android.os.Build
import android.util.AttributeSet
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import com.crescentflare.piratesgame.R
import com.crescentflare.piratesgame.components.basicviews.GradientView
import com.crescentflare.piratesgame.components.complexviews.PublisherLogo
import com.crescentflare.piratesgame.components.containers.FrameContainerView
import com.crescentflare.piratesgame.components.simpleviewlets.ImageViewlet
import com.crescentflare.piratesgame.components.utility.ViewletUtil
import com.crescentflare.piratesgame.infrastructure.events.AppEvent
import com.crescentflare.piratesgame.infrastructure.events.AppEventObserver
import com.crescentflare.piratesgame.components.utility.ImageSource
import com.crescentflare.unilayout.helpers.UniLayoutParams
import com.crescentflare.unilayout.views.UniImageView
import com.crescentflare.jsoninflator.JsonInflatable
import com.crescentflare.jsoninflator.JsonLoader
import com.crescentflare.jsoninflator.binder.InflatableRef
import com.crescentflare.jsoninflator.binder.InflatorAnnotationBinder
import com.crescentflare.jsoninflator.binder.InflatorBinder
import com.crescentflare.jsoninflator.utility.InflatorMapUtil

/**
 * Compound view: contains the publisher logo and background, handles animation
 */
class SplashAnimation : FrameContainerView {

    // --
    // Statics
    // --

    companion object {

        // --
        // Static: reference to layout resource
        // --

        const val layoutResource = R.raw.splash_animation


        // --
        // Static: viewlet integration
        // --

        val viewlet: JsonInflatable = object : JsonInflatable {

            override fun create(context: Context): Any {
                return SplashAnimation(context)
            }

            override fun update(mapUtil: InflatorMapUtil, obj: Any, attributes: Map<String, Any>, parent: Any?, binder: InflatorBinder?): Boolean {
                if (obj is SplashAnimation) {
                    // Apply background
                    obj.gradientColor = mapUtil.optionalColor(attributes, "gradientColor", 0)
                    obj.backgroundImage = ImageSource.fromObject(attributes["backgroundImage"])

                    // Apply state
                    obj.autoAnimation = mapUtil.optionalBoolean(attributes, "autoAnimation", false)
                    obj.on = mapUtil.optionalBoolean(attributes, "on", false)

                    // Generic view properties
                    ViewletUtil.applyGenericViewAttributes(mapUtil, obj, attributes)

                    // Event handling
                    obj.setOnEvent = AppEvent.fromObject(attributes["setOnEvent"])

                    // Forward event observer
                    if (parent is AppEventObserver) {
                        obj.eventObserver = parent
                    }
                    return true
                }
                return false
            }

            override fun canRecycle(mapUtil: InflatorMapUtil, obj: Any, attributes: Map<String, Any>): Boolean {
                return obj is SplashAnimation
            }

        }

    }


    // --
    // Bound views
    // --

    @InflatableRef("logo")
    private var logoView: PublisherLogo? = null

    @InflatableRef("backgroundGradient")
    private var backgroundGradientView: GradientView? = null

    @InflatableRef("backgroundImage")
    private var backgroundImageView: UniImageView? = null


    // --
    // Members
    // --

    private var currentOn = false


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
        backgroundGradientView?.alpha = 0.0f
        clipChildren = false
    }


    // --
    // Configurable values
    // --

    var setOnEvent: AppEvent? = null

    var gradientColor: Int
        get() = backgroundGradientView?.endColor ?: 0
        set(gradientColor) {
            backgroundGradientView?.endColor = gradientColor
        }

    var backgroundImage: ImageSource? = null
        set(backgroundImage) {
            field = backgroundImage
            if (backgroundImageView?.drawable != null || currentOn) {
                ImageViewlet.applyImageSource(backgroundImageView, backgroundImage)
            }
        }

    var autoAnimation = false

    var on: Boolean
        get() = currentOn
        set(on) {
            setOn(on, autoAnimation)
        }

    fun setOn(on: Boolean, animated: Boolean) {
        // Safeguard against non-changing operations
        if (on == currentOn) {
            return
        }

        // Change state with optional animation
        if (animated) {
            logoView?.setOn(on, true, {
                ImageViewlet.applyImageSource(backgroundImageView, backgroundImage)
            }, {
                // Animate
                val animation = AnimatorSet()
                animation.playTogether(
                    ValueAnimator.ofFloat(if (on) 0.5f else 0.15f, if (on) 0.15f else 0.5f).apply {
                        addUpdateListener {
                            val value = it.animatedValue
                            if (value is Float) {
                                (logoView?.layoutParams as? UniLayoutParams)?.verticalGravity = value
                                logoView?.requestLayout()
                            }
                        }
                    },
                    ObjectAnimator.ofFloat(backgroundGradientView, View.ALPHA, if (on) 0f else 1f, if (on) 1f else 0f)
                )
                animation.duration = 500
                animation.startDelay = 500
                animation.interpolator = AccelerateDecelerateInterpolator()
                animation.start()

                // Apply state
                val setOnEvent = this.setOnEvent
                currentOn = on
                if (setOnEvent != null && on) {
                    eventObserver?.observedEvent(setOnEvent, this)
                }
            })
        } else {
            val setOnEvent = this.setOnEvent
            currentOn = on
            logoView?.setOn(on, false)
            if (on) {
                ImageViewlet.applyImageSource(backgroundImageView, backgroundImage)
            }
            backgroundGradientView?.alpha = if (on) 1.0f else 0.0f
            if (setOnEvent != null && on) {
                eventObserver?.observedEvent(setOnEvent, this)
            }
        }
    }

}
