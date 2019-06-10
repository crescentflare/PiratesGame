package com.crescentflare.piratesgame.gl.shaders

import android.annotation.SuppressLint
import android.content.Context
import android.opengl.GLES20
import com.crescentflare.piratesgame.R
import java.io.InputStream
import java.io.InputStreamReader

/**
 * Shaders: vertex and fragment shaders
 * Note: suppress static field leak warning, the context is set through the base application class
 */
@SuppressLint("StaticFieldLeak")
object Shaders {

    // --
    // Vertex shaders (shapes)
    // --

    class VertexShaders {

        val simple = ShaderItem(R.raw.shader_vertex_simple, GLES20.GL_VERTEX_SHADER)

        val shaderLookup = mapOf(
            Pair("simple", simple)
        )

        fun getShader(name: String?): Int {
            if (name != null) {
                val shader = shaderLookup[name]
                if (shader != null) {
                    return shader.reference
                }
            }
            return simple.reference
        }

    }

    val vertex = VertexShaders()


    // --
    // Fragment shaders (color)
    // --

    class FragmentShaders {

        val color = ShaderItem(R.raw.shader_fragment_color, GLES20.GL_FRAGMENT_SHADER)

        val shaderLookup = mapOf(
            Pair("color", color)
        )

        fun getShader(name: String?): Int {
            if (name != null) {
                val shader = shaderLookup[name]
                if (shader != null) {
                    return shader.reference
                }
            }
            return color.reference
        }

    }

    val fragment = FragmentShaders()


    // --
    // Set application context for loading resources
    // --

    private var context: Context? = null

    fun setContext(context: Context) {
        this.context = context
    }


    // --
    // Font helper class
    // --

    class ShaderItem constructor(private val resource: Int, private val type: Int) {

        val reference: Int
            get() {
                // Return cached reference if it's there
                val checkLoadedReference = loadedReference
                if (checkLoadedReference != null) {
                    return checkLoadedReference
                }

                // Load and compile shader when needed
                val loadContext = context
                if (resource != 0 && loadContext != null) {
                    val stream = loadContext.resources.openRawResource(resource)
                    val shaderCode = readFromInputStream(stream)
                    if (shaderCode != null) {
                        val compiledShader = GLES20.glCreateShader(type).also { shader ->
                            GLES20.glShaderSource(shader, shaderCode)
                            GLES20.glCompileShader(shader)
                        }
                        loadedReference = compiledShader
                        return compiledShader
                    }
                }

                // Return an empty shader if it failed
                val emptyShader = GLES20.glCreateShader(GLES20.GL_VERTEX_SHADER)
                loadedReference = emptyShader
                return emptyShader
            }

        private var loadedReference: Int? = null

        private fun readFromInputStream(stream: InputStream): String? {
            val bufferSize = 1024
            val buffer = CharArray(bufferSize)
            val output = StringBuilder()
            var result: String? = null
            try {
                val streamReader = InputStreamReader(stream, "UTF-8")
                while (true) {
                    val rsz = streamReader.read(buffer, 0, buffer.size)
                    if (rsz < 0) {
                        break
                    }
                    output.append(buffer, 0, rsz)
                }
                result = output.toString()
            } catch (ignored: Exception) {
            }
            stream.close()
            return result
        }

    }

}
