package com.crescentflare.piratesgame.page.utility

import android.support.v7.app.AppCompatActivity
import com.crescentflare.piratesgame.infrastructure.events.AppEvent

/**
 * Page utility: provides an interface for separating controller logic into modules
 */
interface ControllerModule {

    fun create(activity: AppCompatActivity)
    fun catchEvent(event: AppEvent, sender: Any?): Boolean

}
