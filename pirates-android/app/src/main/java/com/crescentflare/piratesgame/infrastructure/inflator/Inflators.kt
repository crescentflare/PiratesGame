package com.crescentflare.piratesgame.infrastructure.inflator

import com.crescentflare.jsoninflator.JsonInflator


/**
 * Inflator: a list of json inflators, like viewlet creator
 */
object Inflators {

    val module = JsonInflator("module")
    val scene = JsonInflator("type")
    val viewlet = JsonInflator("viewlet", "viewletStyle")

}
