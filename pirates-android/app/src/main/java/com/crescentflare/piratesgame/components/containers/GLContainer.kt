package com.crescentflare.piratesgame.components.containers

import android.content.Context
import android.opengl.GLES20
import android.opengl.GLSurfaceView
import android.util.AttributeSet
import com.crescentflare.jsoninflator.JsonInflatable
import com.crescentflare.jsoninflator.binder.InflatorBinder
import com.crescentflare.jsoninflator.utility.InflatorMapUtil
import com.crescentflare.piratesgame.components.utility.ViewletUtil
import com.crescentflare.piratesgame.gl.scene.SceneRootObject
import com.crescentflare.piratesgame.gl.utility.SceneMatrix
import com.crescentflare.piratesgame.infrastructure.inflator.Inflators
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

/**
 * Container view: contains a graphic library surface (like OpenGL)
 */
class GLContainer @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null) : GLSurfaceView(context, attrs) {

    // --
    // Static: viewlet integration
    // --

    companion object {

        val viewlet: JsonInflatable = object : JsonInflatable {

            override fun create(context: Context): Any {
                return GLContainer(context)
            }

            override fun update(mapUtil: InflatorMapUtil, obj: Any, attributes: Map<String, Any>, parent: Any?, binder: InflatorBinder?): Boolean {
                if (obj is GLContainer) {
                    // Apply scene
                    obj.modifyScene {
                        val scene = mapUtil.asStringObjectMap(attributes["scene"])
                        var inflateScene = (scene ?: emptyMap()).toMutableMap()
                        if (Inflators.scene.findInflatableNameInAttributes(scene) != "root") {
                            inflateScene = mutableMapOf(
                                Pair("type", "root"),
                                Pair("recycling", true),
                                Pair("items", listOf(scene ?: emptyMap()))
                            )
                        }
                        Inflators.scene.inflateOn(it, inflateScene, null)
                    }

                    // Generic view properties
                    ViewletUtil.applyGenericViewAttributes(mapUtil, obj, attributes)
                    return true
                }
                return false
            }

            override fun canRecycle(mapUtil: InflatorMapUtil, obj: Any, attributes: Map<String, Any>): Boolean {
                return obj is GLContainer
            }

        }

    }


    // --
    // Members
    // --

    private val scene = SceneRootObject(context)
    private val renderer: Renderer
    private val viewMatrix = SceneMatrix()
    private val cameraMatrix = SceneMatrix()
    private val lockScene = Any()


    // --
    // Initialization
    // --

    init {
        setEGLContextClientVersion(2)
        renderer = Renderer()
        setRenderer(renderer)
    }


    // --
    // Thread-safe access to scene
    // --

    fun modifyScene(modify: (SceneRootObject) -> Unit) {
        synchronized(lockScene) {
            modify(scene)
        }
    }


    // --
    // Renderer class
    // --

    private inner class Renderer : GLSurfaceView.Renderer {

        var surfaceWidth = 0
        var surfaceHeight = 0

        override fun onSurfaceCreated(unused: GL10, config: EGLConfig) {
        }

        override fun onDrawFrame(unused: GL10) {
            val color = scene.clearColor
            GLES20.glClearColor(((color and 0xff0000) shr 16).toFloat() / 255f, ((color and 0xff00) shr 8).toFloat() / 255f, (color and 0xff).toFloat() / 255f, ((color.toLong() and 0xff000000) shr 24).toFloat() / 255f)
            GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT or GLES20.GL_DEPTH_BUFFER_BIT)
            synchronized(lockScene) {
                scene.camera?.let {
                    viewMatrix.setInverted(it.matrix)
                    cameraMatrix.setMultiplied(it.getProjectionMatrix(surfaceWidth, surfaceHeight), viewMatrix)
                    scene.dispatchDraw(cameraMatrix)
                } ?: run {
                    cameraMatrix.setIdentity()
                    scene.dispatchDraw(cameraMatrix)
                }
            }
        }

        override fun onSurfaceChanged(unused: GL10, width: Int, height: Int) {
            GLES20.glViewport(0, 0, width, height)
            surfaceWidth = width
            surfaceHeight = height
        }

    }

}
