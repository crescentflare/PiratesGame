package com.crescentflare.piratesgame.components.compoundviews

import android.animation.Animator
import android.animation.ValueAnimator
import android.annotation.TargetApi
import android.content.Context
import android.os.Build
import android.util.AttributeSet
import android.view.animation.DecelerateInterpolator
import com.crescentflare.piratesgame.R
import com.crescentflare.piratesgame.components.containers.FrameContainerView
import com.crescentflare.piratesgame.components.utility.ViewletUtil
import com.crescentflare.unilayout.helpers.UniLayoutParams
import com.crescentflare.unilayout.views.UniImageView
import com.crescentflare.jsoninflator.JsonInflatable
import com.crescentflare.jsoninflator.JsonLoader
import com.crescentflare.jsoninflator.binder.InflatableRef
import com.crescentflare.jsoninflator.binder.InflatorAnnotationBinder
import com.crescentflare.jsoninflator.binder.InflatorBinder
import com.crescentflare.jsoninflator.utility.InflatorMapUtil

/**
 * Compound view: the loading bar on the splash screen
 */
class SplashLoadingBar : FrameContainerView {

    // --
    // Statics
    // --

    companion object {

        // --
        // Static: reference to layout resource
        // --

        const val layoutResource = R.raw.splash_loading_bar


        // --
        // Static: Animation duration
        // --

        const val animationDuration: Long = 250


        // --
        // Static: viewlet integration
        // --

        val viewlet: JsonInflatable = object : JsonInflatable {

            override fun create(context: Context): Any {
                return SplashLoadingBar(context)
            }

            override fun update(mapUtil: InflatorMapUtil, obj: Any, attributes: Map<String, Any>, parent: Any?, binder: InflatorBinder?): Boolean {
                if (obj is SplashLoadingBar) {
                    // Apply state
                    obj.autoAnimation = mapUtil.optionalBoolean(attributes, "autoAnimation", false)
                    obj.progress = mapUtil.optionalFloat(attributes, "progress", 0f)

                    // Generic view properties
                    ViewletUtil.applyGenericViewAttributes(mapUtil, obj, attributes)
                    return true
                }
                return false
            }

            override fun canRecycle(mapUtil: InflatorMapUtil, obj: Any, attributes: Map<String, Any>): Boolean {
                return obj is SplashLoadingBar
            }

        }

    }


    // --
    // Bound views
    // --

    @InflatableRef("bar")
    private var barView: UniImageView? = null


    // --
    // Members
    // --

    private var currentProgress = 0f
    private var animation: ValueAnimator? = null


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

    var autoAnimation = false

    var progress: Float
        get() = currentProgress
        set(progress) {
            setProgress(progress, autoAnimation)
        }

    fun setProgress(progress: Float, animated: Boolean) {
        // Safeguard against non-changing operations
        if (progress == currentProgress && animation == null) {
            return
        }

        // Stop existing animation if it's busy
        animation?.cancel()
        animation = null

        // Change state with optional animation
        if (animated) {
            ViewletUtil.waitViewLayout(this, {
                val oldProgress = currentProgress
                val animation = ValueAnimator.ofFloat(oldProgress, Math.min(progress, 1f))
                this.animation = animation
                animation.duration = animationDuration
                animation.addUpdateListener {
                    val value = it.animatedValue
                    if (value is Float) {
                        currentProgress = value
                        layoutBar()
                        barView?.invalidate()
                    }
                }
                animation.addListener(object : Animator.AnimatorListener {
                    override fun onAnimationStart(animation: Animator) {
                        // No implementation
                    }

                    override fun onAnimationRepeat(animation: Animator) {
                        // No implementation
                    }

                    override fun onAnimationEnd(animation: Animator) {
                        this@SplashLoadingBar.animation = null
                    }

                    override fun onAnimationCancel(animation: Animator) {
                        this@SplashLoadingBar.animation = null
                    }
                })
                animation.interpolator = DecelerateInterpolator()
                animation.start()
            }, {
                setProgress(progress, false)
            })
        } else {
            currentProgress = Math.min(progress, 1f)
            layoutBar()
        }
    }


    // --
    // Custom layout
    // --

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
        layoutBar()
    }

    private fun layoutBar() {
        val layoutParams = barView?.layoutParams
        if (layoutParams is UniLayoutParams) {
            val y = (height - layoutParams.height - paddingTop - paddingBottom) / 2 + paddingTop
            val maxWidth = width - paddingLeft - paddingRight - layoutParams.leftMargin - layoutParams.rightMargin
            barView?.layout(paddingLeft + layoutParams.leftMargin, y, paddingLeft + layoutParams.leftMargin + (progress * maxWidth).toInt(), y + layoutParams.height)
        }
    }

}
