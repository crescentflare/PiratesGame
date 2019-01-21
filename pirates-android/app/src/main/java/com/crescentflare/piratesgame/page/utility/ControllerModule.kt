package com.crescentflare.piratesgame.page.utility

import android.content.Context
import com.crescentflare.piratesgame.infrastructure.events.AppEvent

/**
 * Page utility: provides an interface for separating controller logic into modules
 */
interface ControllerModule {

    fun create(context: Context): ControllerModule
    fun catchEvent(event: AppEvent, sender: Any?): Boolean

}
