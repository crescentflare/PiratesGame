package com.crescentflare.piratesgame.components.utility

import android.content.Context
import com.crescentflare.piratesgame.infrastructure.coreextensions.urlDecode
import com.crescentflare.piratesgame.infrastructure.coreextensions.urlEncode
import com.crescentflare.viewletcreator.utility.ViewletMapUtil

/**
 * Defines the source of an image (an internal or external image)
 */
class ImageSource(string: String?) {

    // ---
    // Members
    // ---

    var scheme = ""
    var parameters = mutableMapOf<String, String>()
    var pathComponents = emptyList<String>()


    // ---
    // Initialization
    // ---

    init {
        if (string != null) {
            // Extract scheme
            var checkString = string
            val schemeMarker = checkString.indexOf("://")
            if (schemeMarker >= 0) {
                scheme = checkString.substring(0, schemeMarker)
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
    }


    // ---
    // Extract values
    // ---

    val fullURI: String
        get() {
            var uri = "$scheme://$fullPath"
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
        get() = if (scheme == "http" || scheme == "https") fullURI else null


    // ---
    // Helper
    // ---

    fun getInternalImageResource(context: Context): Int {
        return context.resources.getIdentifier(fullPath, "drawable", context.packageName)
    }

}
