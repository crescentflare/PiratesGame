package com.crescentflare.piratesgame.gl.scene

import android.content.Context
import android.graphics.Color
import com.crescentflare.jsoninflator.JsonInflatable
import com.crescentflare.jsoninflator.binder.InflatorBinder
import com.crescentflare.jsoninflator.utility.InflatorMapUtil
import com.crescentflare.piratesgame.infrastructure.inflator.Inflators

/**
 * Scene object: the root container of the entire scene
 */
class SceneRootObject(context: Context) : SceneObject(context) {

    // --
    // Static: inflatable integration
    // --

    companion object {

        val inflatable: JsonInflatable = object : JsonInflatable {

            override fun create(context: Context): Any {
                return SceneRootObject(context)
            }

            override fun update(mapUtil: InflatorMapUtil, obj: Any, attributes: Map<String, Any>, parent: Any?, binder: InflatorBinder?): Boolean {
                if (obj is SceneRootObject) {
                    // Apply camera
                    val cameraItem = Inflators.scene.attributesForNestedInflatable(attributes["camera"])
                    obj.context?.let {
                        obj.camera = Inflators.scene.inflate(it, cameraItem, obj) as? SceneCameraObject
                    } ?: run {
                        obj.camera = null
                    }

                    // Apply clear color
                    obj.clearColor = mapUtil.optionalColor(attributes, "clearColor", Color.BLACK)

                    // Generic object properties
                    applyGenericObjectAttributes(mapUtil, obj, attributes)
                    return true
                }
                return false
            }

            override fun canRecycle(mapUtil: InflatorMapUtil, obj: Any, attributes: Map<String, Any>): Boolean {
                return obj is SceneRootObject
            }

        }

    }


    // --
    // Initialization
    // --

    init {
        // No implementation
    }


    // --
    // Configurable values
    // --

    var camera: SceneCameraObject? = null
    var clearColor: Int = Color.BLACK

}
