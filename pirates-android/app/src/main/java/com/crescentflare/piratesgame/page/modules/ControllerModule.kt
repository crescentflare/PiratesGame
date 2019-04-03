package com.crescentflare.piratesgame.page.modules

import android.content.Context
import com.crescentflare.jsoninflator.binder.InflatorMapBinder
import com.crescentflare.piratesgame.infrastructure.events.AppEvent
import com.crescentflare.piratesgame.page.storage.Page

/**
 * Page module: provides an interface for separating controller logic into modules
 */
interface ControllerModule {

    val eventType: String

    fun onCreate(context: Context)
    fun onPageUpdated(page: Page, binder: InflatorMapBinder)
    fun catchEvent(event: AppEvent, sender: Any?): Boolean

}
