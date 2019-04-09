package com.crescentflare.piratesgame.page.activities

import android.content.Context
import android.content.Intent
import android.os.Bundle
import com.crescentflare.jsoninflator.binder.InflatorMapBinder
import com.crescentflare.piratesgame.infrastructure.appconfig.CustomAppConfigManager
import com.crescentflare.piratesgame.infrastructure.events.AppEvent
import com.crescentflare.piratesgame.infrastructure.events.AppEventObserver
import com.crescentflare.piratesgame.infrastructure.events.AppEventType
import com.crescentflare.piratesgame.infrastructure.inflator.Inflators
import com.crescentflare.piratesgame.infrastructure.tools.EventReceiverTool
import com.crescentflare.piratesgame.page.modules.ControllerModule
import com.crescentflare.piratesgame.page.storage.Page
import com.crescentflare.piratesgame.page.storage.PageCache
import com.crescentflare.piratesgame.page.storage.PageLoader
import com.crescentflare.piratesgame.page.storage.PageLoaderContinuousCompletion

/**
 * Page activity: a generic activity that can be used together with json inflation
 */
class PageActivity : NavigationActivity(), AppEventObserver, PageLoaderContinuousCompletion {

    // --
    // Statics: new instance
    // --

    companion object {

        val pageParam = "page"

        fun newInstance(context: Context, pageJson: String): Intent {
            val intent = Intent(context, PageActivity::class.java)
            intent.putExtra(pageParam, pageJson)
            return intent
        }

    }


    // --
    // Members
    // --

    private var pageJson = "splash.json" // Default, when launching the app
    private var pageLoader: PageLoader? = null
    private var hotReloadPageUrl = ""
    private var modules = mutableListOf<ControllerModule>()
    private var isResumed = false


    // --
    // Initialization
    // --

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        pageJson = intent.getStringExtra(pageParam) ?: pageJson
    }


    // --
    // Lifecycle
    // --

    override fun onResume() {
        // Update modules
        super.onResume()
        isResumed = true
        for (module in modules) {
            module.onResume()
        }

        // Check for page and event updates
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
        // Update modules
        super.onPause()
        isResumed = false
        for (module in modules) {
            module.onPause()
        }

        // Stop page and event updates
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
        // Update modules
        updateModules(page.modules)

        // Update layout
        val binder = InflatorMapBinder()
        inflateLayout(page.layout, binder)
        for (module in modules) {
            module.onPageUpdated(page, binder)
        }
    }


    // --
    // Helper
    // --

    private fun updateModules(moduleItems: Any?) {
        // Check if modules are the same before and after the update, then they can be updated instead of re-created
        var canRecycle = false
        val items = Inflators.module.attributesForNestedInflatableList(moduleItems)
        if (items.size == modules.size) {
            canRecycle = true
            for (i in items.indices) {
                if (!Inflators.module.canRecycle(modules[i], items[i])) {
                    canRecycle = false
                    break
                }
            }
        }

        // Update modules
        if (canRecycle) {
            var moduleIndex = 0
            for (item in items) {
                if (moduleIndex < modules.size) {
                    val module = modules[moduleIndex]
                    Inflators.module.inflateOn(module, item, null, null)
                    moduleIndex++
                }
            }
        } else {
            // First clear all modules
            modules.clear()

            // Add modules
            for (item in items) {
                val result = Inflators.module.inflate(this, item, null, null)
                if (result is ControllerModule) {
                    if (isResumed) {
                        result.onResume()
                    }
                    modules.add(result)
                }
            }
        }
    }

}
