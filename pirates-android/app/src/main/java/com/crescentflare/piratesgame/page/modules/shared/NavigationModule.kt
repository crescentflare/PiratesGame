package com.crescentflare.piratesgame.page.modules.shared

import android.content.Context
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import com.crescentflare.jsoninflator.JsonInflatable
import com.crescentflare.jsoninflator.binder.InflatorBinder
import com.crescentflare.jsoninflator.binder.InflatorMapBinder
import com.crescentflare.jsoninflator.utility.InflatorMapUtil
import com.crescentflare.piratesgame.infrastructure.events.AppEvent
import com.crescentflare.piratesgame.page.activities.PageActivity
import com.crescentflare.piratesgame.page.modules.ControllerModule
import com.crescentflare.piratesgame.page.storage.Page
import java.lang.ref.WeakReference

/**
 * Shared module: handles navigation between screens in the app
 */
class NavigationModule: ControllerModule {

    // --
    // Static: inflatable integration
    // --

    companion object {

        val inflatable: JsonInflatable = object : JsonInflatable {

            override fun create(context: Context): Any {
                val module = NavigationModule()
                module.onCreate(context)
                return module
            }

            override fun update(mapUtil: InflatorMapUtil, obj: Any, attributes: Map<String, Any>, parent: Any?, binder: InflatorBinder?): Boolean {
                return obj is NavigationModule
            }

            override fun canRecycle(mapUtil: InflatorMapUtil, obj: Any, attributes: Map<String, Any>): Boolean {
                return obj is NavigationModule
            }

        }

    }


    // --
    // Members
    // --

    override val eventType = "navigate"
    private var context: WeakReference<Context>? = null


    // --
    // Lifecycle
    // --

    override fun onCreate(context: Context) {
        this.context = WeakReference(context)
    }

    override fun onResume() {
        // No implementation
    }

    override fun onPause() {
        // No implementation
    }


    // --
    // Page updates
    // --

    override fun onPageUpdated(page: Page, binder: InflatorMapBinder) {
        // No implementation
    }


    // --
    // Event handling
    // --

    override fun catchEvent(event: AppEvent, sender: Any?): Boolean {
        if (event.rawType == eventType) {
            val context = this.context?.get()
            if (context != null && (context as? AppCompatActivity)?.isFinishing == false) {
                // Handle back navigation
                if (event.fullPath == "back") {
                    context.finish()
                    return false
                }

                // Determine which activity to navigate to
                val openIntent: Intent? = when(event.fullPath) {
                    else -> PageActivity.newInstance(context, "${event.fullPath}.json")
                }

                // Handle navigation
                if (openIntent != null) {
                    val navigationType = NavigationType.fromString(event.parameters["type"])
                    when (navigationType) {
                        NavigationType.Push -> {
                            context.startActivity(openIntent)
                        }
                        NavigationType.Replace -> {
                            context.startActivity(openIntent)
                            context.finish()
                        }
                    }
                }
            }
        }
        return false
    }


    // --
    // Navigation type enum
    // --

    private enum class NavigationType(val value: String) {

        Push("push"),
        Replace("replace");

        companion object {

            fun fromString(string: String?): NavigationType {
                for (enum in NavigationType.values()) {
                    if (enum.value == string) {
                        return enum
                    }
                }
                return Push
            }

        }

    }

}
