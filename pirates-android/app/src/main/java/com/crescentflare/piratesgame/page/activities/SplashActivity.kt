package com.crescentflare.piratesgame.page.activities

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.crescentflare.piratesgame.components.containers.FrameContainerView
import com.crescentflare.piratesgame.components.utility.ViewletUtil
import com.crescentflare.piratesgame.infrastructure.events.AppEvent
import com.crescentflare.piratesgame.infrastructure.events.AppEventObserver
import com.crescentflare.piratesgame.page.modules.shared.AlertModule
import com.crescentflare.piratesgame.page.storage.Page
import com.crescentflare.piratesgame.page.storage.PageLoader
import com.crescentflare.piratesgame.page.storage.PageLoaderContinuousCompletion
import com.crescentflare.piratesgame.page.utility.ControllerModule
import com.crescentflare.viewletcreator.binder.ViewletMapBinder

class SplashActivity : AppCompatActivity(), AppEventObserver, PageLoaderContinuousCompletion {

    // ---
    // Members
    // ---

    //private val pageLoader by lazy { PageLoader(this, "http://192.168.1.169:1313/pages/splash.json") }
    private val pageLoader by lazy { PageLoader(this, "splash.json") }
    private val containerView by lazy { FrameContainerView(this) }
    private var modules = mutableListOf<ControllerModule>()


    // ---
    // Initialization
    // ---

    override fun onCreate(savedInstanceState: Bundle?) {
        // Initialize view
        super.onCreate(savedInstanceState)
        containerView.eventObserver = this
        setContentView(containerView)

        // Add module
        modules.add(AlertModule().create(this))
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
    }

}
