package com.crescentflare.piratesgame.gl.scene

import android.content.Context
import android.graphics.Color
import android.opengl.GLES20
import com.crescentflare.jsoninflator.JsonInflatable
import com.crescentflare.jsoninflator.binder.InflatorBinder
import com.crescentflare.jsoninflator.utility.InflatorMapUtil
import com.crescentflare.piratesgame.gl.shaders.ShaderPrograms
import com.crescentflare.piratesgame.gl.shaders.Shaders
import com.crescentflare.piratesgame.gl.utility.SceneMatrix
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import java.nio.ShortBuffer

/**
 * Scene object: a shape mesh
 */
class SceneMeshObject(context: Context) : SceneObject(context) {

    // --
    // Static: inflatable integration
    // --

    companion object {

        val inflatable: JsonInflatable = object : JsonInflatable {

            override fun create(context: Context): Any {
                return SceneMeshObject(context)
            }

            override fun update(mapUtil: InflatorMapUtil, obj: Any, attributes: Map<String, Any>, parent: Any?, binder: InflatorBinder?): Boolean {
                if (obj is SceneMeshObject) {
                    // Apply mesh
                    obj.color = mapUtil.optionalColor(attributes, "color", Color.TRANSPARENT)
                    obj.vertices = mapUtil.optionalFloatList(attributes, "vertices")
                    obj.indices = mapUtil.optionalIntegerList(attributes, "indices")

                    // Generic object properties
                    applyGenericObjectAttributes(mapUtil, obj, attributes)
                    return true
                }
                return false
            }

            override fun canRecycle(mapUtil: InflatorMapUtil, obj: Any, attributes: Map<String, Any>): Boolean {
                return obj is SceneMeshObject
            }

        }

    }


    // --
    // Members
    // --

    private val meshDimensions = 3
    private var colorArray = floatArrayOf(0f, 0f, 0f, 0f)
    private var vertexBuffer = FloatBuffer.allocate(0)
    private var indexBuffer = ShortBuffer.allocate(0)
    private var positionHandle: Int = 0
    private var colorHandle: Int = 0
    private var matrixHandle: Int = 0


    // --
    // Initialization
    // --

    init {
        // No implementation
    }


    // --
    // Configurable values
    // --

    var color: Int = 0
        set(color) {
            field = color
            colorArray = floatArrayOf(((color and 0xff0000) shr 16).toFloat() / 255f, ((color and 0xff00) shr 8).toFloat() / 255f, (color and 0xff).toFloat() / 255f, ((color.toLong() and 0xff000000) shr 24).toFloat() / 255f)
        }

    var vertices: List<Float> = emptyList()
        set(vertices) {
            field = vertices
            vertexBuffer = ByteBuffer.allocateDirect(vertices.size * 4).run {
                order(ByteOrder.nativeOrder())
                asFloatBuffer().apply {
                    put(vertices.toFloatArray())
                    position(0)
                }
            }
        }

    var indices: List<Int> = emptyList()
        set(indices) {
            field = indices
            indexBuffer = ByteBuffer.allocateDirect(indices.size * 2).run {
                order(ByteOrder.nativeOrder())
                asShortBuffer().apply {
                    val shortArray = ShortArray(indices.size)
                    var index = 0
                    for (element in indices)
                        shortArray[index++] = element.toShort()
                    put(shortArray)
                    position(0)
                }
            }
        }


    // --
    // Rendering
    // --

    override fun draw(parentMatrix: SceneMatrix) {
        val program = ShaderPrograms.getProgram(Shaders.vertex.simple, Shaders.fragment.color)
        GLES20.glUseProgram(program)
        positionHandle = GLES20.glGetAttribLocation(program, "vPosition").also {
            val vertexCount: Int = vertices.size / meshDimensions
            val vertexStride: Int = meshDimensions * 4
            GLES20.glEnableVertexAttribArray(it)
            GLES20.glVertexAttribPointer(
                it,
                meshDimensions,
                GLES20.GL_FLOAT,
                false,
                vertexStride,
                vertexBuffer
            )
            matrixHandle = GLES20.glGetUniformLocation(program, "uMVPMatrix").also { matrixHandle ->
                val resultMatrix = multipliedWithParentMatrix(matrix, parentMatrix)
                GLES20.glUniformMatrix4fv(matrixHandle, 1, false, resultMatrix.floatArray, 0)
            }
            colorHandle = GLES20.glGetUniformLocation(program, "vColor").also { colorHandle ->
                GLES20.glUniform4fv(colorHandle, 1, colorArray, 0)
            }
            if (indices.isNotEmpty()) {
                GLES20.glDrawElements(GLES20.GL_TRIANGLES, indices.size, GLES20.GL_UNSIGNED_SHORT, indexBuffer)
            } else {
                GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, vertexCount)
            }
            GLES20.glDisableVertexAttribArray(it)
        }
    }

}
