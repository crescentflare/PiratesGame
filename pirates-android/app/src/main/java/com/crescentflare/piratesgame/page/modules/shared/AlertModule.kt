package com.crescentflare.piratesgame.page.modules.shared

import android.app.AlertDialog
import android.content.Context
import com.crescentflare.piratesgame.infrastructure.events.AppEvent
import com.crescentflare.piratesgame.page.utility.ControllerModule
import java.lang.ref.WeakReference

/**
 * Shared module: catches alert events to show popup dialogs
 */
class AlertModule: ControllerModule {

    // ---
    // Members
    // ---

    private val eventType = "alert"
    private var context: WeakReference<Context>? = null


    // ---
    // Initialization
    // ---

    override fun create(context: Context): ControllerModule {
        this.context = WeakReference(context)
        return this
    }


    // ---
    // Event handling
    // ---

    override fun catchEvent(event: AppEvent, sender: Any?): Boolean {
        if (event.rawType == eventType) {
            val context = this.context?.get()
            if (context != null) {
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
