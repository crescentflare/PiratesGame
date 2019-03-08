package com.crescentflare.piratesgame.components.complexviews

import android.animation.ObjectAnimator
import android.annotation.TargetApi
import android.content.Context
import android.content.res.Resources
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.os.Build
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import android.view.animation.DecelerateInterpolator
import android.view.animation.LinearInterpolator
import android.widget.ImageView
import com.crescentflare.piratesgame.R
import com.crescentflare.piratesgame.components.utility.ViewletUtil
import com.crescentflare.viewletcreator.ViewletCreator
import com.crescentflare.viewletcreator.binder.ViewletBinder

/**
 * Complex view: the publisher logo with effect animation
 * Note: requires no clip children on parent for logo effect
 */
class PublisherLogo : ViewGroup {

    // --
    // Static: viewlet integration
    // --

    companion object {

        val viewlet: ViewletCreator.Viewlet = object : ViewletCreator.Viewlet {

            override fun create(context: Context): View {
                return PublisherLogo(context)
            }

            override fun update(view: View, attributes: Map<String, Any>, parent: ViewGroup?, binder: ViewletBinder?): Boolean {
                if (view is PublisherLogo) {
                    // Generic view properties
                    ViewletUtil.applyGenericViewAttributes(view, attributes)
                    return true
                }
                return false
            }

            override fun canRecycle(view: View, attributes: Map<String, Any>): Boolean {
                return view is PublisherLogo
            }

        }

    }


    // --
    // Members
    // --

    private val baseLogo by lazy { ImageView(context) }
    private val logoEffect by lazy { ImageView(context) }
    private val logoFlashEffect by lazy { ImageView(context) }
    private val effectUpOffset: Int
    private val effectLeftOffset: Int
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
        // Add base logo
        baseLogo.setImageResource(R.drawable.logo_publisher)
        addView(baseLogo)

        // Add overlay effect
        logoEffect.alpha = 0f
        addView(logoEffect)
        logoFlashEffect.alpha = 0f
        addView(logoFlashEffect)

        // Pre-calculate offsets
        effectUpOffset = (-36 * Resources.getSystem().displayMetrics.density).toInt()
        effectLeftOffset = (-15 * Resources.getSystem().displayMetrics.density).toInt()

        // Don't clip children, the overlay effect is outside of the clipping space (make sure the parent also doesn't clip)
        clipChildren = false
        clipToPadding = false
    }


    // --
    // Configurable values
    // --

    var autoAnimation = false

    var on: Boolean
        get() = currentOn
        set(on) {
            setOn(on, autoAnimation)
        }

    fun setOn(on: Boolean, animated: Boolean, afterAssetLoad: (() -> Unit)? = null, completion: (() -> Unit)? = null) {
        // Safeguard against non-changing operations
        if (on == currentOn) {
            return
        }

        // Change state with optional animation
        if (animated) {
            ViewletUtil.waitViewLayout(this, {
                if (on) {
                    if (logoEffect.drawable == null) {
                        logoEffect.setImageResource(R.drawable.logo_publisher_effect)
                        logoFlashEffect.setImageResource(R.drawable.logo_publisher_effect)
                        logoFlashEffect.colorFilter = PorterDuffColorFilter(Color.WHITE, PorterDuff.Mode.SRC_IN)
                        afterAssetLoad?.invoke()
                    }
                    ViewletUtil.waitViewLayout(logoEffect, {
                        // Prepare effect visibility
                        logoEffect.alpha = 1f
                        logoFlashEffect.alpha = 0.8f

                        // Start animation
                        val animation = ObjectAnimator.ofFloat(logoFlashEffect, ALPHA, 0.8f, 0f)
                        animation.duration = 1000
                        animation.interpolator = DecelerateInterpolator()
                        animation.start()

                        // Apply state
                        currentOn = on
                        completion?.invoke()
                    }, {
                        setOn(on, false)
                    })
                } else {
                    // Prepare effect visibility
                    logoEffect.alpha = 1f

                    // Start animation
                    val animation = ObjectAnimator.ofFloat(logoEffect, ALPHA, 1f, 0f)
                    animation.duration = 1000
                    animation.interpolator = LinearInterpolator()
                    animation.start()

                    // Apply state
                    currentOn = on
                    completion?.invoke()
                }
            }, {
                setOn(on, false)
            })
        } else {
            currentOn = on
            if (on && logoEffect.drawable == null) {
                logoEffect.setImageResource(R.drawable.logo_publisher_effect)
                afterAssetLoad?.invoke()
            }
            logoEffect.alpha = if (on) 1f else 0f
            completion?.invoke()
        }
    }


    // --
    // Custom layout
    // --

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        // Determine the aspect ratio
        var wantAspectRatio = 0f
        baseLogo.drawable?.let { baseLogoDrawable ->
            wantAspectRatio = baseLogoDrawable.intrinsicWidth.toFloat() / baseLogoDrawable.intrinsicHeight.toFloat()
        }

        // Calculate the measured size
        if (wantAspectRatio > 0) {
            // Fetch the sizing specs
            val widthSpecMode = MeasureSpec.getMode(widthMeasureSpec)
            val heightSpecMode = MeasureSpec.getMode(heightMeasureSpec)
            val widthSpecSize = MeasureSpec.getSize(widthMeasureSpec)
            val heightSpecSize = MeasureSpec.getSize(heightMeasureSpec)
            var scaleWidth = false
            var scaleHeight = false

            // Determine if either width or height need to be scaled up or down
            if (widthSpecMode != MeasureSpec.EXACTLY || heightSpecMode != MeasureSpec.EXACTLY) {
                if (widthSpecMode == MeasureSpec.EXACTLY) {
                    scaleHeight = true
                } else if (heightSpecMode == MeasureSpec.EXACTLY) {
                    scaleWidth = true
                } else if (baseLogo.drawable != null) {
                    var reducedWidth = 1f
                    var reducedHeight = 1f
                    if (widthSpecMode == View.MeasureSpec.AT_MOST) {
                        reducedWidth = (widthSpecSize - paddingLeft - paddingRight) / baseLogo.drawable.intrinsicWidth.toFloat()
                    }
                    if (heightSpecMode == View.MeasureSpec.AT_MOST) {
                        reducedHeight = (heightSpecSize - paddingTop - paddingBottom) / baseLogo.drawable.intrinsicHeight.toFloat()
                    }
                    if (reducedWidth < reducedHeight && reducedWidth < 1f) {
                        scaleHeight = true
                    } else if (reducedHeight < 1f) {
                        scaleWidth = true
                    }
                }
            }

            // Apply scaling
            if (scaleWidth) {
                var limitWidth = 0xFFFFFFF
                if (widthSpecMode == MeasureSpec.AT_MOST) {
                    limitWidth = widthSpecSize
                }
                setMeasuredDimension(
                        Math.min(limitWidth, ((heightSpecSize - paddingTop - paddingBottom).toFloat() * wantAspectRatio).toInt() + paddingLeft + paddingRight),
                        heightSpecSize
                )
                return
            } else if (scaleHeight) {
                var limitHeight = 0xFFFFFFF
                if (heightSpecMode == MeasureSpec.AT_MOST) {
                    limitHeight = heightSpecSize
                }
                setMeasuredDimension(
                        widthSpecSize,
                        Math.min(limitHeight, ((widthSpecSize - paddingLeft - paddingRight).toFloat() / wantAspectRatio).toInt() + paddingTop + paddingBottom)
                )
                return
            }

            // If no scaling was applied set it to the size of the image
            baseLogo.drawable?.let { baseLogoDrawable ->
                setMeasuredDimension(
                        baseLogoDrawable.intrinsicWidth + paddingLeft + paddingRight,
                        baseLogoDrawable.intrinsicHeight + paddingTop + paddingBottom
                )
                return
            }
        }

        // If nothing was applied, just use the sizes given in the spec
        var width = 0
        var height = 0
        if (MeasureSpec.getMode(widthMeasureSpec) == MeasureSpec.EXACTLY) {
            width = MeasureSpec.getSize(widthMeasureSpec)
        }
        if (MeasureSpec.getMode(heightMeasureSpec) == MeasureSpec.EXACTLY) {
            height = MeasureSpec.getSize(heightMeasureSpec)
        }
        setMeasuredDimension(width, height)
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        // Determine scaling
        val scaleFactor = (right - left - paddingLeft - paddingRight).toFloat() / baseLogo.drawable.intrinsicWidth.toFloat()
        val scaledLogoWidth = right - left - paddingLeft - paddingRight
        val scaledLogoHeight = (baseLogo.drawable.intrinsicHeight.toFloat() * scaleFactor).toInt()
        val scaledEffectWidth = ((logoEffect.drawable?.intrinsicWidth?.toFloat() ?: 0f) * scaleFactor).toInt()
        val scaledEffectHeight = ((logoEffect.drawable?.intrinsicHeight?.toFloat() ?: 0f) * scaleFactor).toInt()

        // Layout on base logo
        val centerY = paddingTop + (bottom - top - paddingTop - paddingBottom) / 2
        baseLogo.layout(paddingLeft, centerY - scaledLogoHeight / 2, paddingLeft + scaledLogoWidth, centerY - scaledLogoHeight / 2 + scaledLogoHeight)

        // Layout on effect
        val scaledUpOffset = (effectUpOffset * scaleFactor).toInt()
        val scaledLeftOffset = (effectLeftOffset * scaleFactor).toInt()
        logoEffect.layout(paddingLeft + scaledLeftOffset, centerY - scaledLogoHeight / 2 + scaledUpOffset, paddingLeft + scaledLeftOffset + scaledEffectWidth, centerY - scaledLogoHeight / 2 + scaledUpOffset + scaledEffectHeight)
        logoFlashEffect.layout(paddingLeft + scaledLeftOffset, centerY - scaledLogoHeight / 2 + scaledUpOffset, paddingLeft + scaledLeftOffset + scaledEffectWidth, centerY - scaledLogoHeight / 2 + scaledUpOffset + scaledEffectHeight)
    }

}
