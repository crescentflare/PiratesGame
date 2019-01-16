package com.crescentflare.piratesgame.infrastructure.events

/**
 * Event system: a protocol to implement for components sending events which can be tagged by label
 */
interface AppEventLabeledSender {

    val senderLabel: String?

}
