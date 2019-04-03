package com.crescentflare.piratesgame.infrastructure.events

import com.crescentflare.piratesgame.infrastructure.coreextensions.urlDecode
import com.crescentflare.piratesgame.infrastructure.inflator.Inflators

/**
 * Event system: defines an event with optional parameters
 */
class AppEvent {

    // --
    // Static: factory method
    // --

    companion object {

        fun fromObject(value: Any?): AppEvent? {
            if (value is String) {
                return AppEvent(value)
            } else if (value is Map<*, *>) {
                val result: Map<String, Any>? = Inflators.viewlet.mapUtil.asStringObjectMap(value)
                if (result != null) {
                    return AppEvent(result)
                }
            }
            return null
        }

    }


    // --
    // Members
    // --

    var standardType = AppEventType.Unknown
    var rawType = "unknown"
    var parameters = mutableMapOf<String, String>()
    var pathComponents = emptyList<String>()


    // --
    // Initialization
    // --

    constructor(value: Any?) {
        if (value is String) {
            initParse(value)
        } else if (value is Map<*, *>) {
            val result: Map<String, Any>? = Inflators.viewlet.mapUtil.asStringObjectMap(value)
            initParse(result ?: mapOf())
        }
    }

    constructor(string: String) {
        initParse(string)
    }

    constructor(map: Map<String, Any>) {
        initParse(map)
    }

    private fun initParse(string: String) {
        // Extract scheme
        var checkString = string
        val schemeMarker = checkString.indexOf("://")
        if (schemeMarker >= 0) {
            rawType = checkString.substring(0, schemeMarker)
            standardType = AppEventType.fromString(rawType)
            checkString = checkString.substring(schemeMarker + 3)
        }

        // Extract parameters
        val paramMarker = checkString.indexOf('?')
        if (paramMarker >= 0) {
            // Get parameter string
            val paramString = checkString.substring(paramMarker + 1)
            checkString = checkString.substring(0, paramMarker)

            // Split into separate parameters and fill dictionary
            val paramItems = paramString.split("&")
            for (paramItem in paramItems) {
                val paramSet = paramItem.split("=")
                if (paramSet.size == 2) {
                    parameters[paramSet[0].urlDecode()] = paramSet[1].urlDecode()
                }
            }
        }

        // Finally set path to the remaining string
        pathComponents = checkString.split("/")
    }

    private fun initParse(map: Map<String, Any>) {
        rawType = (map["path"] ?: map["name"]) as? String ?: "unknown"
        standardType = AppEventType.fromString(rawType)
        val path = map["path"]
        if (path is String) {
            pathComponents = path.split("/")
        }
        for (key in map.keys) {
            if (key != "type" && key != "path") {
                val value = map[key]
                if (value is String) {
                    parameters[key] = value
                }
            }
        }
    }


    // --
    // Extract values
    // --

    val fullPath: String
        get() = pathComponents.joinToString("/")

}
