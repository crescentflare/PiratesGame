package com.crescentflare.piratesgame.gl.utility

import android.opengl.Matrix
import com.crescentflare.piratesgame.infrastructure.inflator.Inflators

/**
 * Scene utility: an easy to use matrix object
 */
class SceneMatrix {

    // --
    // Static: factory methods
    // --

    companion object {

        fun fromObject(value: Any?): SceneMatrix {
            if (value is String) {
                return SceneMatrix(value)
            } else if (value is Map<*, *>) {
                val result: Map<String, Any>? = Inflators.scene.mapUtil.asStringObjectMap(value)
                if (result != null) {
                    return SceneMatrix(result)
                }
            }
            return SceneMatrix()
        }

    }


    // --
    // Members
    // --

    private var internalFloatArray = FloatArray(16)
    private var modified = false
    var floatArray: FloatArray
        set(floatArray) {
            if (floatArray.size == 16) {
                internalFloatArray = floatArray
                modified = true
            }
        }
        get() = internalFloatArray


    // --
    // Initialization
    // --

    constructor() {
        Matrix.setIdentityM(internalFloatArray, 0)
    }

    constructor(value: Any?) {
        if (value is String) {
            initParse(value)
        } else if (value is Map<*, *>) {
            val result: Map<String, Any>? = Inflators.scene.mapUtil.asStringObjectMap(value)
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
        val typeSplit = string.split(":")
        Matrix.setIdentityM(internalFloatArray, 0)
        if (typeSplit.size == 2) {
            val valueSplit = typeSplit[1].split(",")
            when (typeSplit[0]) {
                "translate" -> setTranslate(arrayItemToFloat(valueSplit, 0), arrayItemToFloat(valueSplit, 1), arrayItemToFloat(valueSplit, 2))
                "rotate" -> setRotate(arrayItemToFloat(valueSplit, 0), arrayItemToFloat(valueSplit, 1), arrayItemToFloat(valueSplit, 2), arrayItemToFloat(valueSplit, 3))
                "scale" -> setScale(arrayItemToFloat(valueSplit, 0), arrayItemToFloat(valueSplit, 1), arrayItemToFloat(valueSplit, 2))
            }
            return
        }
    }

    private fun initParse(map: Map<String, Any>) {
        val mapUtil = Inflators.scene.mapUtil
        Matrix.setIdentityM(internalFloatArray, 0)
        when (mapUtil.optionalString(map, "type", "")) {
            "translate" -> setTranslate(mapUtil.optionalFloat(map, "x", 0f), mapUtil.optionalFloat(map, "y", 0f), mapUtil.optionalFloat(map, "z", 0f))
            "rotate" -> setRotate(mapUtil.optionalFloat(map, "angle", 0f), mapUtil.optionalFloat(map, "x", 0f), mapUtil.optionalFloat(map, "y", 0f), mapUtil.optionalFloat(map, "z", 0f))
            "scale" -> setScale(mapUtil.optionalFloat(map, "x", 0f), mapUtil.optionalFloat(map, "y", 0f), mapUtil.optionalFloat(map, "z", 0f))
        }
    }


    // --
    // Modify
    // --

    fun isIdentity(): Boolean {
        return !modified
    }

    fun setIdentity() {
        Matrix.setIdentityM(internalFloatArray, 0)
        modified = false
    }

    fun setTranslate(x: Float, y: Float, z: Float) {
        if (modified) {
            setIdentity()
        }
        Matrix.translateM(internalFloatArray, 0, x, y, z)
        modified = true
    }

    fun setRotate(angle: Float, x: Float, y: Float, z: Float) {
        if (modified) {
            setIdentity()
        }
        Matrix.rotateM(internalFloatArray, 0, angle, x, y, z)
        modified = true
    }

    fun setScale(x: Float, y: Float, z: Float) {
        if (modified) {
            setIdentity()
        }
        Matrix.scaleM(internalFloatArray, 0, x, y, z)
        modified = true
    }

    fun setFrustum(left: Float, right: Float, bottom: Float, top: Float, near: Float, far: Float) {
        if (modified) {
            setIdentity()
        }
        Matrix.frustumM(internalFloatArray, 0, left, right, bottom, top, near, far)
        modified = true
    }

    fun setOrtho(left: Float, right: Float, bottom: Float, top: Float, near: Float, far: Float) {
        if (modified) {
            setIdentity()
        }
        Matrix.orthoM(internalFloatArray, 0, left, right, bottom, top, near, far)
        modified = true
    }

    fun setInverted(source: SceneMatrix) {
        if (modified) {
            setIdentity()
        }
        Matrix.invertM(internalFloatArray, 0, source.internalFloatArray, 0)
        modified = true
    }

    fun setMultiplied(lhs: SceneMatrix, rhs: SceneMatrix) {
        if (modified) {
            setIdentity()
        }
        Matrix.multiplyMM(internalFloatArray, 0, lhs.internalFloatArray, 0, rhs.internalFloatArray, 0)
        modified = true
    }


    // --
    // Helper
    // --

    private fun arrayItemToFloat(list: List<String>, index: Int, defaultValue: Float = 0f): Float {
        if (index >= 0 && index < list.size) {
            return list[index].toFloatOrNull() ?: defaultValue
        }
        return defaultValue
    }

}
