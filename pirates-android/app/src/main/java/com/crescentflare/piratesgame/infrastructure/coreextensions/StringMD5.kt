package com.crescentflare.piratesgame.infrastructure.coreextensions

import java.security.MessageDigest
import java.security.NoSuchAlgorithmException

/**
 * Core extension: extends string to calculate MD5
 */
fun String.md5(): String {
    // Try decoding
    try {
        // Obtain digest
        val messageDigest = MessageDigest.getInstance("MD5")
        val digest = messageDigest.digest(this.toByteArray())

        // Convert to hex and return
        val hexArray = "0123456789abcdef".toCharArray()
        val hexChars = CharArray(digest.size * 2)
        for (j in digest.indices) {
            val v = digest[j] % 256
            hexChars[j * 2] = hexArray[v.ushr(4)]
            hexChars[j * 2 + 1] = hexArray[v and 0x0F]
        }
        return String(hexChars)
    } catch (ignored: NoSuchAlgorithmException) {
    }

    // Return recognizable error when failed
    return "#error"
}
