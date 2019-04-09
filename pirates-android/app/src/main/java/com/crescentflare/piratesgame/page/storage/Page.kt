package com.crescentflare.piratesgame.page.storage

import com.crescentflare.jsoninflator.utility.InflatorMapUtil
import com.crescentflare.piratesgame.infrastructure.coreextensions.md5
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.lang.Exception

/**
 * Page storage: a single page item
 */
class Page {

    // --
    // Members
    // --

    var loadedData = emptyMap<String, Any>()
    val hash: String
    private val mapUtil = InflatorMapUtil()


    // --
    // Initialization
    // --

    constructor(jsonString: String) {
        var resultHash = "unknown"
        val type = object : TypeToken<Map<String, Any>>() {
        }.type
        try {
            val result = Gson().fromJson<Map<String, Any>>(jsonString, type)
            if (result != null) {
                loadedData = result
                resultHash = jsonString.md5()
            }
        } catch (ignored: Exception) {
            // No implementation
        }
        hash = resultHash
    }

    constructor(map: Map<String, Any>, hash: String = "unknown") {
        loadedData = map
        this.hash = hash
    }


    // --
    // Extract data
    // --

    val modules: List<Map<String, Any>>?
        get() {
            return optionalObjectMapList(loadedData, "modules")
        }

    val layout: Map<String, Any>?
        get() {
            val dataSetMap = mapUtil.asStringObjectMap(loadedData["dataSets"])
            if (dataSetMap != null) {
                val layout = mapUtil.asStringObjectMap(dataSetMap["layout"])
                if (layout != null) {
                    return layout
                }
            }
            return null
        }


    // --
    // Helper
    // --

    @Suppress("unchecked_cast")
    private fun optionalObjectMapList(map: Map<String, Any>, key: String): List<Map<String, Any>>? {
        val objectList = mapUtil.optionalObjectList(map, key)
        return if (objectList.size > 0 && mapUtil.asStringObjectMap(objectList[0]) == null) null else objectList as List<Map<String, Any>>
    }

}
