package com.crescentflare.piratesgame.components.utility

import android.content.Context
import com.crescentflare.piratesgame.infrastructure.coreextensions.urlDecode
import com.crescentflare.piratesgame.infrastructure.coreextensions.urlEncode
import com.crescentflare.viewletcreator.utility.ViewletMapUtil

/**
 * Defines the source of an image (an internal or external image)
 */
class ImageSource {

    // ---
    // Static: factory method
    // ---

    companion object {

        fun fromObject(value: Any?): ImageSource? {
            if (value is String) {
                return ImageSource(value)
            } else if (value is Map<*, *>) {
                val result: Map<String, Any>? = ViewletMapUtil.asStringObjectMap(value)
                if (result != null) {
                    return ImageSource(result)
                }
            }
            return null
        }

    }


    // ---
    // Members
    // ---

    var type = Type.Unknown
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
            type = Type.fromString(checkString.substring(0, schemeMarker))
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
        type = Type.fromString(map["type"] as? String)
        val path = map["path"] ?: map["name"]
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

    val fullURI: String
        get() {
            var uri = "$type://$fullPath"
            if (parameters.size > 0) {
                var firstParam = true
                for ((key, value) in parameters) {
                    uri += if (firstParam) "?" else "&"
                    uri += key.urlEncode() + "=" + value.urlEncode()
                    firstParam = false
                }
            }
            return uri
        }

    val fullPath: String
        get() = pathComponents.joinToString("/")

    val tintColor: Int
        get() = ViewletMapUtil.optionalColor(parameters.toMap(), "colorize", 0)

    val threePatch: Int
        get() = ViewletMapUtil.optionalDimension(parameters.toMap(), "threePatch", -1)

    val ninePatch: Int
        get() = ViewletMapUtil.optionalDimension(parameters.toMap(), "ninePatch", -1)

    val onlinePath: String?
        get() = if (type == Type.OnlineImage || type == Type.SecureOnlineImage) fullURI else null


    // ---
    // Helper
    // ---

    fun getInternalImageResource(context: Context): Int {
        return context.resources.getIdentifier(fullPath, "drawable", context.packageName)
    }


    // ---
    // Type enum
    // ---

    enum class Type(val value: String) {

        Unknown("unknown"),
        OnlineImage("http"),
        SecureOnlineImage("https"),
        InternalImage("app");

        companion object {

            fun fromString(string: String?): Type {
                for (enum in Type.values()) {
                    if (enum.value == string) {
                        return enum
                    }
                }
                return Unknown
            }

        }

    }

}
