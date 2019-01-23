package com.crescentflare.piratesgame.page.activities

import android.annotation.TargetApi
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.support.v7.app.ActionBar
import android.support.v7.app.AppCompatActivity
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import com.crescentflare.piratesgame.components.simpleviewlets.SpacerViewlet
import com.crescentflare.piratesgame.page.utility.ActionBarComponent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

open class ComponentActivity : AppCompatActivity() {

    // ---
    // Members
    // ---

    var actionBarView: View?
        get() = activityView.actionBarView
        set(actionBarView) {
            activityView.actionBarView = actionBarView
            updateStatusBar()
        }

    var view: View?
        get() = activityView.contentView
        set(contentView) {
            activityView.contentView = contentView
        }

    private val activityView by lazy { ComponentActivityView(this) }


    // ---
    // Initialization
    // ---

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(activityView)
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
        val lightStatusIcons = (actionBarView as? ActionBarComponent)?.lightContent ?: true
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

    var actionBarView: View? = null
        set(actionBarView) {
            if (field != null) {
                removeView(field)
            }
            field = actionBarView
            addView(field)
            reducedHeight = 0
            if (field != null && !((field as? ActionBarComponent)?.translucent ?: false)) {
                reducedHeight = actionBarHeight
            }
        }

    var contentView: View? = null
        set(contentView) {
            if (field != null) {
                removeView(field)
            }
            field = contentView
            addView(field, 0)
        }

    private var actionBarHeight: Int
    private var reducedHeight = 0


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
            actionBarView?.measure(MeasureSpec.makeMeasureSpec(right, MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec(actionBarHeight, MeasureSpec.EXACTLY))
            contentView?.measure(MeasureSpec.makeMeasureSpec(right, MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec(bottom - reducedHeight, MeasureSpec.EXACTLY))
            setMeasuredDimension(width, height)
        } else {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        }
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        actionBarView?.layout(0, 0, right, actionBarHeight)
        contentView?.layout(0, reducedHeight, right, bottom)
    }

}
