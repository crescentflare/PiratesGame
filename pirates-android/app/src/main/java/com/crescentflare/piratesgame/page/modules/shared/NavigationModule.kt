package com.crescentflare.piratesgame.page.modules.shared

import android.content.Context
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import com.crescentflare.piratesgame.infrastructure.events.AppEvent
import com.crescentflare.piratesgame.page.activities.LevelActivity
import com.crescentflare.piratesgame.page.activities.SplashActivity
import com.crescentflare.piratesgame.page.activities.SummaryActivity
import com.crescentflare.piratesgame.page.modules.ControllerModule
import com.crescentflare.piratesgame.page.storage.Page
import com.crescentflare.viewletcreator.binder.ViewletMapBinder
import java.lang.ref.WeakReference

/**
 * Shared module: handles navigation between screens in the app
 */
class NavigationModule: ControllerModule {

    // --
    // Members
    // --

    override val eventType = "navigate"
    private var context: WeakReference<Context>? = null


    // --
    // Initialization
    // --

    override fun onCreate(context: Context) {
        this.context = WeakReference(context)
    }


    // --
    // Page updates
    // --

    override fun onPageUpdated(page: Page, binder: ViewletMapBinder) {
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
                val openActivityClass = when(event.fullPath) {
                    "splash" -> SplashActivity::class.java
                    "summary" -> SummaryActivity::class.java
                    "level" -> LevelActivity::class.java
                    else -> null
                }

                // Handle navigation
                if (openActivityClass != null) {
                    val navigationType = NavigationType.fromString(event.parameters["type"])
                    when (navigationType) {
                        NavigationType.Push -> {
                            context.startActivity(Intent(context, openActivityClass))
                        }
                        NavigationType.Replace -> {
                            context.startActivity(Intent(context, openActivityClass))
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
