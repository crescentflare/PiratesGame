package com.crescentflare.piratesgame.gl.scene

import android.content.Context
import com.crescentflare.jsoninflator.JsonInflatable
import com.crescentflare.jsoninflator.binder.InflatorBinder
import com.crescentflare.jsoninflator.utility.InflatorMapUtil
import com.crescentflare.piratesgame.gl.utility.SceneMatrix

/**
 * Scene object: a camera
 */
class SceneCameraObject(context: Context) : SceneObject(context) {

    // --
    // Static: inflatable integration
    // --

    companion object {

        val inflatable: JsonInflatable = object : JsonInflatable {

            override fun create(context: Context): Any {
                return SceneCameraObject(context)
            }

            override fun update(mapUtil: InflatorMapUtil, obj: Any, attributes: Map<String, Any>, parent: Any?, binder: InflatorBinder?): Boolean {
                if (obj is SceneCameraObject) {
                    // Apply projection
                    obj.near = mapUtil.optionalFloat(attributes, "near", 0f)
                    obj.far = mapUtil.optionalFloat(attributes, "far", 0f)

                    // Generic object properties
                    applyGenericObjectAttributes(mapUtil, obj, attributes)
                    return true
                }
                return false
            }

            override fun canRecycle(mapUtil: InflatorMapUtil, obj: Any, attributes: Map<String, Any>): Boolean {
                return obj is SceneCameraObject
            }

        }

    }


    // --
    // Members
    // --

    private var projectionMatrix = SceneMatrix()


    // --
    // Initialization
    // --

    init {
        // No implementation
    }


    // --
    // Configurable values
    // --

    var near: Float = 0f
    var far: Float = 0f


    // --
    // Get projection matrix
    // --

    fun getProjectionMatrix(width: Int, height: Int): SceneMatrix {
        if (near < far && near > 0f) {
            if (width > height) {
                val ratio = width.toFloat() / height
                projectionMatrix.setFrustum(-ratio, ratio, -1f, 1f, near, far)
            } else {
                val ratio = height.toFloat() / width
                projectionMatrix.setFrustum(-1f, 1f, -ratio, ratio, near, far)
            }
        } else {
            if (width > height) {
                val ratio = width.toFloat() / height
                projectionMatrix.setOrtho(-ratio, ratio, -1f, 1f, 0f, 1f)
            } else {
                val ratio = height.toFloat() / width
                projectionMatrix.setOrtho(-1f, 1f, -ratio, ratio, 0f, 1f)
            }
        }
        return projectionMatrix
    }

}
