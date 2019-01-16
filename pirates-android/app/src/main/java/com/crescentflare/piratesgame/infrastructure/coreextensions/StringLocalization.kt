package com.crescentflare.piratesgame.infrastructure.coreextensions

import android.content.Context

/**
 * Core extension: easily localize strings
 */
fun String.localized(context: Context): String {
    val key = this.toLowerCase()
    val id = context.resources.getIdentifier(key, "string", context.packageName)
    return if (id > 0) context.resources.getString(id) else this
}
