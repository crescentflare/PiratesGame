package com.crescentflare.piratesgame.page.activities

import android.annotation.TargetApi
import android.content.Context
import android.content.res.Resources
import android.graphics.Rect
import android.os.Build
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.AttributeSet
import android.util.TypedValue
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import com.crescentflare.piratesgame.R
import com.crescentflare.piratesgame.page.utility.NavigationBarComponent
import com.crescentflare.piratesgame.page.views.ComponentActivityView

/**
 * Page activity: the base class providing an easy way to set up navigation bar components
 */
abstract class ComponentActivity : AppCompatActivity() {

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
            val displayMetrics = Resources.getSystem().displayMetrics
            val transparentBar = displayMetrics.widthPixels < displayMetrics.heightPixels
            window.setFlags(if (transparentBar) WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS else 0, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)
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
