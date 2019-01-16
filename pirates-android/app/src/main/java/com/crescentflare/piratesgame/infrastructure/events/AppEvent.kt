package com.crescentflare.piratesgame.infrastructure.events

import com.crescentflare.piratesgame.infrastructure.coreextensions.urlDecode
import com.crescentflare.viewletcreator.utility.ViewletMapUtil

/**
 * Event system: defines an event with optional parameters
 */
class AppEvent {

    // ---
    // Members
    // ---

    var type = AppEventType.Unknown
    var parameters = mutableMapOf<String, String>()
    var pathComponents = emptyList<String>()


    // ---
    // Initialization
    // ---

    constructor(value: Any?) {
        if (value is String) {
            initParse(value)
        } else if (value is Map<*, *>) {
            val result: Map<String, Any>? = ViewletMapUtil.asStringObjectMap(value)
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
            type = AppEventType.fromString(checkString.substring(0, schemeMarker))
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
        type = AppEventType.fromString(map["type"] as? String ?: "")
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


    // ---
    // Extract values
    // ---

    val fullPath: String
        get() = pathComponents.joinToString("/")

}
