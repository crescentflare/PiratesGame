package com.crescentflare.piratesgame.gl.scene

import android.content.Context
import com.crescentflare.jsoninflator.JsonInflatable
import com.crescentflare.jsoninflator.binder.InflatorBinder
import com.crescentflare.jsoninflator.utility.InflatorMapUtil
import com.crescentflare.piratesgame.gl.utility.SceneMatrix
import com.crescentflare.piratesgame.infrastructure.inflator.Inflators
import java.lang.ref.WeakReference

/**
 * Scene object: an empty object, used as a container
 */
open class SceneObject(context: Context) {

    // --
    // Statics
    // --

    companion object {

        // --
        // Static: inflatable integration
        // --

        val inflatable: JsonInflatable = object : JsonInflatable {

            override fun create(context: Context): Any {
                return SceneObject(context)
            }

            override fun update(mapUtil: InflatorMapUtil, obj: Any, attributes: Map<String, Any>, parent: Any?, binder: InflatorBinder?): Boolean {
                if (obj is SceneObject) {
                    applyGenericObjectAttributes(mapUtil, obj, attributes)
                    return true
                }
                return false
            }

            override fun canRecycle(mapUtil: InflatorMapUtil, obj: Any, attributes: Map<String, Any>): Boolean {
                return obj is SceneObject
            }

        }


        // --
        // Static: inflator utilities
        // --

        fun applyGenericObjectAttributes(mapUtil: InflatorMapUtil, obj: SceneObject, attributes: Map<String, Any>) {
            // Apply matrix
            obj.matrix = SceneMatrix.fromObject(attributes["matrix"])

            // Apply child objects
            val items = Inflators.scene.attributesForNestedInflatableList(attributes["items"])
            obj.removeAllChildObjects()
            obj.context?.let { context ->
                for (item in items) {
                    (Inflators.scene.inflate(context, item, obj) as? SceneObject)?.let {
                        obj.addChildObject(it)
                    }
                }
            }
        }

    }


    // --
    // Members
    // --

    private var contextReference : WeakReference<Context>? = null
    private val internalChildObjects = mutableListOf<SceneObject>()
    private var internalParent: SceneObject? = null
    private var multipliedParentMatrix = SceneMatrix()


    // --
    // Initialization
    // --

    init {
        contextReference = WeakReference(context)
    }


    // --
    // Manage child objects
    // --

    val context: Context?
        get() = contextReference?.get()

    val childObjects: List<SceneObject>
        get() = internalChildObjects

    val parent: SceneObject?
        get() = internalParent

    fun addChildObject(childObject: SceneObject) {
        if (childObject == this || childObject.internalParent != null) {
            return
        }
        internalChildObjects.add(childObject)
        childObject.internalParent = this
    }

    fun insertChildObject(index: Int, childObject: SceneObject) {
        if (childObject == this || childObject.internalParent != null) {
            return
        }
        internalChildObjects.add(index, childObject)
        childObject.internalParent = this
    }

    fun removeChildObject(childObject: SceneObject) {
        if (childObject.internalParent == this) {
            internalChildObjects.remove(childObject)
            childObject.internalParent = null
        }
    }

    fun removeAllChildObjects() {
        for (childObject in internalChildObjects) {
            childObject.internalParent = null
        }
        internalChildObjects.clear()
    }


    // --
    // Configurable values
    // --

    var matrix = SceneMatrix()


    // --
    // Rendering
    // --

    fun dispatchDraw(parentMatrix: SceneMatrix) {
        val applyMatrix = multipliedWithParentMatrix(matrix, parentMatrix)
        draw(parentMatrix)
        for (child in childObjects) {
            child.dispatchDraw(applyMatrix)
        }
    }

    open fun draw(parentMatrix: SceneMatrix) {
        // No implementation
    }

    protected fun multipliedWithParentMatrix(matrix: SceneMatrix, parentMatrix: SceneMatrix): SceneMatrix {
        if (parentMatrix.isIdentity()) {
            return matrix
        }
        if (matrix.isIdentity()) {
            return parentMatrix
        }
        multipliedParentMatrix.setMultiplied(parentMatrix, matrix)
        return multipliedParentMatrix
    }

}
