package com.crescentflare.piratesgame.gl.shaders

import android.opengl.GLES20

/**
 * Shaders: get programs for shader combinations (includes caching)
 */
object ShaderPrograms {

    // --
    // Cached programs
    // --

    private val cachedPrograms = mutableListOf<CachedProgram>()


    // --
    // Get a program, create if needed
    // --

    fun getProgram(vertexShader: Shaders.ShaderItem?, fragmentShader: Shaders.ShaderItem?): Int {
        return ShaderPrograms.getProgram(vertexShader?.reference, fragmentShader?.reference)
    }

    fun getProgram(vertexShaderReference: Int?, fragmentShaderReference: Int?): Int {
        // First try to find a cached program
        for (cachedProgram in cachedPrograms) {
            if (cachedProgram.vertexShaderReference == vertexShaderReference && cachedProgram.fragmentShaderReference == fragmentShaderReference) {
                return cachedProgram.programReference
            }
        }

        // Create program and cache it
        val programReference = GLES20.glCreateProgram().also {
            if (vertexShaderReference != null) {
                GLES20.glAttachShader(it, vertexShaderReference)
            }
            if (fragmentShaderReference != null) {
                GLES20.glAttachShader(it, fragmentShaderReference)
            }
            GLES20.glLinkProgram(it)
        }
        cachedPrograms.add(CachedProgram(vertexShaderReference, fragmentShaderReference, programReference))
        return programReference
    }


    // --
    // Cached program helper class
    // --

    private class CachedProgram(val vertexShaderReference: Int?, val fragmentShaderReference: Int?, val programReference: Int)

}
