package com.crescentflare.piratesgame.page.views

import android.annotation.TargetApi
import android.content.Context
import android.content.res.Resources
import android.graphics.Rect
import android.os.Build
import android.util.AttributeSet
import android.util.TypedValue
import android.view.View
import android.view.ViewGroup
import com.crescentflare.piratesgame.R
import com.crescentflare.piratesgame.components.utility.NavigationBarComponent

/**
 * Page view: a view belonging to the component activity, provides layout for a content view and navigation bar components with optional translucency
 */
class ComponentActivityView: ViewGroup {

    // --
    // Members
    // --

    var contentView: View? = null
        set(contentView) {
            if (field != null) {
                removeView(field)
            }
            field = contentView
            addView(field, 0)
        }

    var actionBarView: View? = null
        set(actionBarView) {
            if (field != null) {
                removeView(field)
            }
            field = actionBarView
            addView(field)
            solidTopBar = field != null && !((field as? NavigationBarComponent)?.translucent == true)
        }

    var navigationBarView: View? = null
        set(navigationBarView) {
            if (field != null) {
                removeView(field)
            }
            field = navigationBarView
            addView(field)
            solidBottomBar = field != null && !((field as? NavigationBarComponent)?.translucent == true)
        }

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
            actionBarView?.measure(MeasureSpec.makeMeasureSpec(width, MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec(totalTopBarHeight, MeasureSpec.EXACTLY))
            navigationBarView?.measure(MeasureSpec.makeMeasureSpec(width, MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec(bottomBarHeight, MeasureSpec.EXACTLY))
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
        actionBarView?.layout(0, 0, right - left, totalTopBarHeight)
        navigationBarView?.layout(0, bottom - top - bottomBarHeight, right - left, bottom - top)
        contentView?.layout(0, topContentInset, right - left, bottom - top - topContentInset - bottomContentInset)
    }


    // --
    // Helper
    // --

    private fun getActionBarHeight(): Int {
        val typedValue = TypedValue()
        return if (context.theme.resolveAttribute(R.attr.actionBarSize, typedValue, true)) TypedValue.complexToDimensionPixelSize(typedValue.data, Resources.getSystem().displayMetrics) else 0
    }

}
