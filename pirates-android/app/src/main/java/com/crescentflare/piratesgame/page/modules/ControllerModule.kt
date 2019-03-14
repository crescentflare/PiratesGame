package com.crescentflare.piratesgame.page.modules

import android.content.Context
import com.crescentflare.piratesgame.infrastructure.events.AppEvent
import com.crescentflare.piratesgame.page.storage.Page
import com.crescentflare.viewletcreator.binder.ViewletMapBinder

/**
 * Page module: provides an interface for separating controller logic into modules
 */
interface ControllerModule {

    val eventType: String

    fun onCreate(context: Context)
    fun onPageUpdated(page: Page, binder: ViewletMapBinder)
    fun catchEvent(event: AppEvent, sender: Any?): Boolean

}
