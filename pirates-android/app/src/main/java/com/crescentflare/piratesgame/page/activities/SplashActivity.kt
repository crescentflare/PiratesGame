package com.crescentflare.piratesgame.page.activities

import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.WindowManager
import com.crescentflare.piratesgame.components.actionbars.TransparentActionBar
import com.crescentflare.piratesgame.components.basicviews.GradientView
import com.crescentflare.piratesgame.components.containers.FrameContainerView
import com.crescentflare.piratesgame.components.utility.ViewletUtil
import com.crescentflare.piratesgame.infrastructure.events.AppEvent
import com.crescentflare.piratesgame.infrastructure.events.AppEventObserver
import com.crescentflare.piratesgame.page.modules.shared.AlertModule
import com.crescentflare.piratesgame.page.modules.splash.SplashLoaderModule
import com.crescentflare.piratesgame.page.storage.Page
import com.crescentflare.piratesgame.page.storage.PageLoader
import com.crescentflare.piratesgame.page.storage.PageLoaderContinuousCompletion
import com.crescentflare.piratesgame.page.utility.ControllerModule
import com.crescentflare.viewletcreator.binder.ViewletMapBinder

class SplashActivity : ComponentActivity(), AppEventObserver, PageLoaderContinuousCompletion {

    // ---
    // Members
    // ---

//    private val pageLoader by lazy { PageLoader(this, "http://192.168.1.175:1313/pages/splash.json") }
    private val pageLoader by lazy { PageLoader(this, "splash.json") }
    private val containerView by lazy { FrameContainerView(this) }
    private var modules = mutableListOf<ControllerModule>()


    // ---
    // Initialization
    // ---

    override fun onCreate(savedInstanceState: Bundle?) {
        // Set action bar
        super.onCreate(savedInstanceState)
        val actionBar = TransparentActionBar(this)
        actionBarView = actionBar

        // Set container
        containerView.eventObserver = this
        view = containerView

        // Set fullscreen flags
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            window.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)
        }

        // Add modules
        modules.add(AlertModule())
        modules.add(SplashLoaderModule())
        for (module in modules) {
            module.onCreate(this)
        }
    }


    // ---
    // Lifecycle
    // ---

    override fun onResume() {
        super.onResume()
        startContinuousPageLoad()
    }

    override fun onPause() {
        super.onPause()
        stopContinuousPageLoad()
    }


    // ---
    // Interaction
    // ---

    override fun observedEvent(event: AppEvent, sender: Any?) {
        for (module in modules) {
            if (module.catchEvent(event, sender)) {
                break
            }
        }
    }


    // --
    // Page loader integration
    // --

    private fun startContinuousPageLoad() {
        pageLoader.startLoadingContinuously(this)
    }

    private fun stopContinuousPageLoad() {
        pageLoader.stopLoadingContinuously()
    }

    override fun didUpdatePage(page: Page) {
        val binder = ViewletMapBinder()
        val inflateLayout = mapOf(
            Pair("viewlet", "frameContainer"),
            Pair("width", "stretchToParent"),
            Pair("height", "stretchToParent"),
            Pair("recycling", true),
            Pair("items", listOf(page.layout))
        )
        ViewletUtil.assertInflateOn(containerView, inflateLayout, binder)
        for (module in modules) {
            module.onPageUpdated(page, binder)
        }
    }

}
