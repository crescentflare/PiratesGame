package com.crescentflare.piratesgame.page.storage

import com.crescentflare.piratesgame.infrastructure.coreextensions.md5
import com.crescentflare.viewletcreator.utility.ViewletMapUtil
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

/**
 * Page storage: a single page item
 */
class Page {

    // ---
    // Members
    // ---

    var loadedData = emptyMap<String, Any>()
    val hash: String


    // ---
    // Initialization
    // ---

    constructor(jsonString: String) {
        var resultHash = "unknown"
        val type = object : TypeToken<Map<String, Any>>() {
        }.type
        val result = Gson().fromJson<Map<String, Any>>(jsonString, type)
        if (result != null) {
            loadedData = result
            resultHash = jsonString.md5()
        }
        hash = resultHash
    }

    constructor(map: Map<String, Any>, hash: String = "unknown") {
        loadedData = map
        this.hash = hash
    }


    // ---
    // Extract data
    // ---

    val layout: Map<String, Any>?
        get() {
            val dataSetMap = ViewletMapUtil.asStringObjectMap(loadedData["dataSets"])
            if (dataSetMap != null) {
                val layout = ViewletMapUtil.asStringObjectMap(dataSetMap["layout"])
                if (layout != null) {
                    return layout
                }
            }
            return null
        }

}
