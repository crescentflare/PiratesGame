package com.crescentflare.piratesgame.page.modules.shared

import android.app.AlertDialog
import android.content.Context
import android.support.v7.app.AppCompatActivity
import com.crescentflare.jsoninflator.binder.InflatorMapBinder
import com.crescentflare.piratesgame.infrastructure.events.AppEvent
import com.crescentflare.piratesgame.page.storage.Page
import com.crescentflare.piratesgame.page.modules.ControllerModule
import java.lang.ref.WeakReference

/**
 * Shared module: catches alert events to show popup dialogs
 */
class AlertModule: ControllerModule {

    // --
    // Members
    // --

    override val eventType = "alert"
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
                val builder = AlertDialog.Builder(context)
                    .setTitle(event.parameters["title"] ?: "Alert")
                    .setMessage(event.parameters["text"] ?: "No text specified")
                    .setPositiveButton("OK", null)
                builder.show()
                return true
            }
        }
        return false
    }
}
