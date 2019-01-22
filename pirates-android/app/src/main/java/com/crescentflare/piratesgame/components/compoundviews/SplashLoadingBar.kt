package com.crescentflare.piratesgame.components.compoundviews

import android.animation.Animator
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.annotation.TargetApi
import android.content.Context
import android.content.res.Resources
import android.os.Build
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.DecelerateInterpolator
import com.crescentflare.piratesgame.R
import com.crescentflare.piratesgame.components.basicviews.GradientView
import com.crescentflare.piratesgame.components.complexviews.PublisherLogo
import com.crescentflare.piratesgame.components.containers.FrameContainerView
import com.crescentflare.piratesgame.components.simpleviewlets.ImageViewlet
import com.crescentflare.piratesgame.components.utility.ViewletUtil
import com.crescentflare.piratesgame.infrastructure.events.AppEvent
import com.crescentflare.piratesgame.infrastructure.events.AppEventObserver
import com.crescentflare.piratesgame.infrastructure.uri.ImageURI
import com.crescentflare.unilayout.helpers.UniLayoutParams
import com.crescentflare.unilayout.views.UniImageView
import com.crescentflare.viewletcreator.ViewletCreator
import com.crescentflare.viewletcreator.ViewletLoader
import com.crescentflare.viewletcreator.binder.ViewletAnnotationBinder
import com.crescentflare.viewletcreator.binder.ViewletBinder
import com.crescentflare.viewletcreator.binder.ViewletRef
import com.crescentflare.viewletcreator.utility.ViewletMapUtil

/**
 * Compound view: the loading bar on the splash screen
 */
class SplashLoadingBar : FrameContainerView {

    // ---
    // Statics
    // ---

    companion object {

        // ---
        // Static: reference to layout resource
        // ---

        const val layoutResource = R.raw.splash_loading_bar


        // ---
        // Static: viewlet integration
        // ---

        val viewlet: ViewletCreator.Viewlet = object : ViewletCreator.Viewlet {

            override fun create(context: Context): View {
                return SplashLoadingBar(context)
            }

            override fun update(view: View, attributes: Map<String, Any>, parent: ViewGroup?, binder: ViewletBinder?): Boolean {
                if (view is SplashLoadingBar) {
                    // Apply state
                    view.autoAnimation = ViewletMapUtil.optionalBoolean(attributes, "autoAnimation", false)
                    view.progress = ViewletMapUtil.optionalFloat(attributes, "progress", 0f)

                    // Generic view properties
                    ViewletUtil.applyGenericViewAttributes(view, attributes)
                    return true
                }
                return false
            }

            override fun canRecycle(view: View, attributes: Map<String, Any>): Boolean {
                return view is SplashLoadingBar
            }

        }

    }


    // ---
    // Bound views
    // ---

    @ViewletRef("bar")
    private var barView: UniImageView? = null


    // ---
    // Members
    // ---

    private var currentProgress = 0f
    private var animation: ValueAnimator? = null


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
        ViewletUtil.assertInflateOn(this, ViewletLoader.loadAttributes(context, layoutResource),null, ViewletAnnotationBinder(this))
        barView?.setBackgroundResource(R.drawable.shape_splash_loading_bar)
    }


    // ---
    // Configurable values
    // ---

    var autoAnimation = false

    var progress: Float
        get() = currentProgress
        set(progress) {
            setProgress(progress, autoAnimation)
        }

    fun setProgress(progress: Float, animated: Boolean) {
        // Safeguard against non-changing operations
        if (progress == currentProgress) {
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

    // ---
    // Custom layout
    // ---

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
