package com.crescentflare.piratesgame.page.activities

import android.content.res.Configuration
import android.content.res.Resources
import android.os.Build
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.support.v4.widget.SwipeRefreshLayout
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.view.WindowManager
import com.crescentflare.dynamicappconfig.activity.ManageAppConfigActivity
import com.crescentflare.dynamicappconfig.manager.AppConfigStorage
import com.crescentflare.jsoninflator.binder.InflatorBinder
import com.crescentflare.piratesgame.R
import com.crescentflare.piratesgame.components.utility.NavigationBarComponent
import com.crescentflare.piratesgame.infrastructure.events.AppEvent
import com.crescentflare.piratesgame.infrastructure.events.AppEventObserver
import com.crescentflare.piratesgame.components.containers.NavigationContainerView
import com.crescentflare.piratesgame.components.utility.ViewletUtil
import com.crescentflare.piratesgame.infrastructure.inflator.Inflators

/**
 * Page activity: the base class providing an easy way to set up navigation bar components
 */
abstract class NavigationActivity : AppCompatActivity(), SwipeRefreshLayout.OnRefreshListener {

    // --
    // Members
    // --

    private val swipeRefreshEvents = mutableListOf<AppEvent>()
    private val swipeRefreshView by lazy { SwipeRefreshLayout(this) }
    private val activityView by lazy { NavigationContainerView(this) }


    // --
    // Initialization
    // --

    override fun onCreate(savedInstanceState: Bundle?) {
        // Prepare content view
        super.onCreate(savedInstanceState)
        swipeRefreshView.addView(activityView)
        setContentView(swipeRefreshView)
        swipeRefreshView.isEnabled = false
        swipeRefreshView.setOnRefreshListener(this)
        swipeRefreshView.setColorSchemeColors(
            ContextCompat.getColor(this, R.color.primary),
            ContextCompat.getColor(this, R.color.secondary)
        )

        // Set fullscreen flags, status and navigation bars are handled in the activity instead
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val displayMetrics = Resources.getSystem().displayMetrics
            val transparentBar = displayMetrics.widthPixels < displayMetrics.heightPixels
            window.setFlags(if (transparentBar) WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS else 0, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)
        }
    }


    // --
    // Lifecycle
    // --

    override fun onConfigurationChanged(newConfig: Configuration?) {
        super.onConfigurationChanged(newConfig)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val displayMetrics = Resources.getSystem().displayMetrics
            val transparentBar = displayMetrics.widthPixels < displayMetrics.heightPixels
            window.setFlags(if (transparentBar) WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS else 0, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)
        }
    }

    override fun onResume() {
        super.onResume()
        updateStatusBar()
    }


    // --
    // Inflation
    // --

    protected fun inflateLayout(layout: Map<String, Any>?, binder: InflatorBinder) {
        // First inflate
        var inflateLayout = (layout ?: emptyMap()).toMutableMap()
        if (Inflators.viewlet.findInflatableNameInAttributes(layout) != "navigationContainer") {
            inflateLayout = mutableMapOf(
                Pair("viewlet", "navigationContainer"),
                Pair("recycling", true),
                Pair("content", layout ?: emptyMap()),
                Pair("topBar", mapOf(
                    Pair("viewlet", "transparentNavigationBar"),
                    Pair("width", "stretchToParent")
                ))
            )
        } else if (!inflateLayout.containsKey("topBar")) {
            inflateLayout["topBar"] = mapOf(
                Pair("viewlet", "transparentNavigationBar"),
                Pair("width", "stretchToParent")
            )
        }
        ViewletUtil.assertInflateOn(activityView, inflateLayout, binder)
        activityView.eventObserver = this as? AppEventObserver

        // Update for possible bar changes
        val topBarView = activityView.topBarView
        updateStatusBar()
        if (topBarView != null && AppConfigStorage.instance.isInitialized) {
            topBarView.setOnLongClickListener {
                ManageAppConfigActivity.startWithResult(this, 0)
                true
            }
        }
    }


    // --
    // Handle swipe to refresh
    // --

    protected fun addSwipeRefreshEvent(appEvent: AppEvent) {
        if (!swipeRefreshEvents.contains(appEvent)) {
            swipeRefreshEvents.add(appEvent)
        }
        swipeRefreshView.isEnabled = swipeRefreshEvents.size > 0
    }

    protected fun removeSwipeRefreshEvent(appEvent: AppEvent) {
        swipeRefreshEvents.remove(appEvent)
        swipeRefreshView.isEnabled = swipeRefreshEvents.size > 0
    }

    override fun onRefresh() {
        if (this is AppEventObserver) {
            for (event in swipeRefreshEvents) {
                this.observedEvent(event, swipeRefreshView)
            }
        }
    }

    protected fun stopRefreshing() {
        swipeRefreshView.isRefreshing = false
    }


    // --
    // Handle status bar
    // --

    private fun updateStatusBar() {
        val lightStatusIcons = (activityView.topBarView as? NavigationBarComponent)?.lightContent ?: true
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val decor = window.decorView
            if (!lightStatusIcons) {
                decor.systemUiVisibility = decor.systemUiVisibility or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
            } else {
                decor.systemUiVisibility -= decor.systemUiVisibility and View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
            }
        }
    }

}
