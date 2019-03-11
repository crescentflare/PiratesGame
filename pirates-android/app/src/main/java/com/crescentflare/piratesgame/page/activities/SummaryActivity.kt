package com.crescentflare.piratesgame.page.activities

import android.os.Bundle
import android.support.v4.content.ContextCompat
import com.crescentflare.piratesgame.R
import com.crescentflare.piratesgame.components.navigationbars.TransparentNavigationBar
import com.crescentflare.piratesgame.components.containers.FrameContainerView
import com.crescentflare.piratesgame.components.navigationbars.SolidNavigationBar
import com.crescentflare.piratesgame.components.utility.ViewletUtil
import com.crescentflare.piratesgame.infrastructure.appconfig.CustomAppConfigManager
import com.crescentflare.piratesgame.infrastructure.coreextensions.localized
import com.crescentflare.piratesgame.infrastructure.events.AppEvent
import com.crescentflare.piratesgame.infrastructure.events.AppEventObserver
import com.crescentflare.piratesgame.infrastructure.events.AppEventType
import com.crescentflare.piratesgame.infrastructure.tools.EventReceiverTool
import com.crescentflare.piratesgame.page.modules.shared.AlertModule
import com.crescentflare.piratesgame.page.modules.splash.SplashLoaderModule
import com.crescentflare.piratesgame.page.storage.Page
import com.crescentflare.piratesgame.page.storage.PageLoader
import com.crescentflare.piratesgame.page.storage.PageLoaderContinuousCompletion
import com.crescentflare.piratesgame.page.modules.ControllerModule
import com.crescentflare.piratesgame.page.modules.shared.NavigationModule
import com.crescentflare.piratesgame.page.storage.PageCache
import com.crescentflare.viewletcreator.binder.ViewletMapBinder

/**
 * Activity: the main screen showing the summary of the player state
 */
class SummaryActivity : ComponentActivity(), AppEventObserver, PageLoaderContinuousCompletion {

    // --
    // Members
    // --

    private val pageJson = "summary.json"
    private var pageLoader: PageLoader? = null
    private var hotReloadPageUrl = ""
    private val containerView by lazy { FrameContainerView(this) }
    private var modules = mutableListOf<ControllerModule>()


    // --
    // Initialization
    // --

    override fun onCreate(savedInstanceState: Bundle?) {
        // Set action bar
        super.onCreate(savedInstanceState)
        val actionBar = SolidNavigationBar(this)
        val navigationBar = SolidNavigationBar(this)
        actionBar.setBackgroundColor(ContextCompat.getColor(this, R.color.primary))
        actionBar.title = title.toString()
        actionBarView = actionBar
        navigationBar.setBackgroundColor(ContextCompat.getColor(this, R.color.primary))
        navigationBarView = navigationBar

        // Set container
        containerView.eventObserver = this
        view = containerView

        // Add modules
        modules.add(AlertModule())
        modules.add(NavigationModule())
        for (module in modules) {
            module.onCreate(this)
        }
    }


    // --
    // Lifecycle
    // --

    override fun onResume() {
        super.onResume()
        val wasHotReloadPageUrl = hotReloadPageUrl
        if (CustomAppConfigManager.currentConfig().devServerUrl.isNotEmpty() && CustomAppConfigManager.currentConfig().enablePageHotReload) {
            hotReloadPageUrl = CustomAppConfigManager.currentConfig().devServerUrl
            if (!hotReloadPageUrl.startsWith("http")) {
                hotReloadPageUrl = "http://$hotReloadPageUrl"
            }
            hotReloadPageUrl = "$hotReloadPageUrl/pages/$pageJson"
        } else {
            hotReloadPageUrl = ""
        }
        if (wasHotReloadPageUrl != hotReloadPageUrl || pageLoader == null) {
            pageLoader = PageLoader(this, if (hotReloadPageUrl.isNotEmpty()) hotReloadPageUrl else pageJson)
            PageCache.removeEntry(wasHotReloadPageUrl)
            PageCache.removeEntry(pageJson)
        }
        EventReceiverTool.addObserver(this)
        startContinuousPageLoad()
    }

    override fun onPause() {
        super.onPause()
        EventReceiverTool.removeObserver(this)
        stopContinuousPageLoad()
    }


    // --
    // Interaction
    // --

    override fun observedEvent(event: AppEvent, sender: Any?) {
        // Handle pull to refresh event registration
        if (event.standardType == AppEventType.RegisterPullToRefresh) {
            if (sender is AppEvent) {
                if (event.fullPath == "add") {
                    addSwipeRefreshEvent(sender)
                } else if (event.fullPath == "remove") {
                    removeSwipeRefreshEvent(sender)
                }
            }
        }

        // Handle modules
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
        pageLoader?.startLoadingContinuously(this)
    }

    private fun stopContinuousPageLoad() {
        pageLoader?.stopLoadingContinuously()
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
