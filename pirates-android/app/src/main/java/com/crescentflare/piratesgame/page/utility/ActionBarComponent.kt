package com.crescentflare.piratesgame.page.utility

import android.content.Context
import com.crescentflare.piratesgame.infrastructure.events.AppEvent
import com.crescentflare.piratesgame.page.storage.Page
import com.crescentflare.viewletcreator.binder.ViewletMapBinder

/**
 * Page utility: action bar components should implement this
 * Note: a bar having light content means that a text will be more readable with a white color than black
 */
interface ActionBarComponent {

    val lightContent: Boolean
    val translucent: Boolean

}
