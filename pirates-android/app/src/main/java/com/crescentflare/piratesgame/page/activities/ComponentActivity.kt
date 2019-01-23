package com.crescentflare.piratesgame.page.activities

import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import com.crescentflare.piratesgame.components.simpleviewlets.SpacerViewlet
import com.crescentflare.piratesgame.page.utility.NavigationBarComponent

@SuppressLint("Registered")
open class ComponentActivity : AppCompatActivity() {

    // ---
    // Members
    // ---

    var view: View?
        get() = activityView.contentView
        set(contentView) {
            activityView.contentView = contentView
        }

    var actionBarView: View?
        get() = activityView.actionBarView
        set(actionBarView) {
            activityView.actionBarView = actionBarView
            updateStatusBar()
        }

    var navigationBarView: View?
        get() = activityView.navigationBarView
        set(navigationBarView) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) { // Ignore on older Android versions
                activityView.navigationBarView = navigationBarView
            }
        }

    private val activityView by lazy { ComponentActivityView(this) }


    // ---
    // Initialization
    // ---

    override fun onCreate(savedInstanceState: Bundle?) {
        // Prepare content view
        super.onCreate(savedInstanceState)
        setContentView(activityView)

        // Set fullscreen flags, status and navigation bars are handled in the activity instead
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            window.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)
        }
    }


    // ---
    // Lifecycle
    // ---

    override fun onResume() {
        super.onResume()
        updateStatusBar()
    }


    // ---
    // Handle status bar
    // ---

    private fun updateStatusBar() {
        val lightStatusIcons = (actionBarView as? NavigationBarComponent)?.lightContent ?: true
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val decor = window.decorView
            if (!lightStatusIcons) {
                decor.systemUiVisibility = decor.systemUiVisibility or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
            } else {
                decor.systemUiVisibility = decor.systemUiVisibility xor View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
            }
        }
    }

}

private class ComponentActivityView: ViewGroup {

    // ---
    // Members
    // ---

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
            useTopInset = field != null && !((field as? NavigationBarComponent)?.translucent == true)
        }

    var navigationBarView: View? = null
        set(navigationBarView) {
            if (field != null) {
                removeView(field)
            }
            field = navigationBarView
            addView(field)
            useBottomInset = field != null && !((field as? NavigationBarComponent)?.translucent == true)
        }

    private var actionBarHeight: Int
    private var useTopInset = false
    private var useBottomInset = false


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
        actionBarHeight = SpacerViewlet.getActionBarHeight(context)
    }


    // ---
    // Custom layout
    // ---

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val width = MeasureSpec.getSize(widthMeasureSpec)
        val height = MeasureSpec.getSize(heightMeasureSpec)
        val widthMode = MeasureSpec.getMode(widthMeasureSpec)
        val heightMode = MeasureSpec.getMode(heightMeasureSpec)
        if (widthMode == MeasureSpec.EXACTLY && heightMode == MeasureSpec.EXACTLY) {
            val totalTopBarHeight = actionBarHeight + if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) rootWindowInsets.stableInsetTop else 0
            val bottomBarHeight = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) rootWindowInsets.stableInsetBottom else 0
            val topInset = if (useTopInset) totalTopBarHeight else 0
            val bottomInset = if (useBottomInset) bottomBarHeight else 0
            actionBarView?.measure(MeasureSpec.makeMeasureSpec(width, MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec(totalTopBarHeight, MeasureSpec.EXACTLY))
            navigationBarView?.measure(MeasureSpec.makeMeasureSpec(width, MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec(bottomBarHeight, MeasureSpec.EXACTLY))
            contentView?.measure(MeasureSpec.makeMeasureSpec(width, MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec(height - topInset - bottomInset, MeasureSpec.EXACTLY))
            setMeasuredDimension(width, height)
        } else {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        }
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        val totalTopBarHeight = actionBarHeight + if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) rootWindowInsets.stableInsetTop else 0
        val bottomBarHeight = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) rootWindowInsets.stableInsetBottom else 0
        val topInset = if (useTopInset) totalTopBarHeight else 0
        val bottomInset = if (useBottomInset) bottomBarHeight else 0
        actionBarView?.layout(0, 0, right - left, totalTopBarHeight)
        navigationBarView?.layout(0, bottom - top - bottomBarHeight, right - left, bottom - top)
        contentView?.layout(0, topInset, right - left, bottom - top - topInset - bottomInset)
    }

}
