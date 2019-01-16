package com.crescentflare.piratesgame.infrastructure.events

/**
 * Event system: a protocol to observe for events like button taps
 */
interface AppEventObserver {

    fun observedEvent(event: AppEvent, sender: Any?)

}
