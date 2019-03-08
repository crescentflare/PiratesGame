package com.crescentflare.piratesgame.components.utility

/**
 * Component utility: action or navigation bar components should implement this
 * Note: a bar having light content means that a text will be more readable with a white color than black
 */
interface NavigationBarComponent {

    val lightContent: Boolean
    val translucent: Boolean

}
