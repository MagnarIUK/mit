package com.magnariuk.mittest.util.util

import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.security.MessageDigest

/**
 * Повертає значення в пікселях, наприклад: 25.px -> "25px".
 */
val Int.px: String
    get() = "${this}px"

/**
 * Повертає значення в відсотках, наприклад: 25.px -> "25%".
 */
val Int.p: String
    get() = "${this}%"

val Long.mb: String get() {
    val kb = 1024
    val mb = 1024 * kb
    val gb = 1024 * mb

    return when {
        this >= gb -> "%.2f GB".format(this / gb.toDouble())
        this >= mb -> "%.2f MB".format(this / mb.toDouble())
        this >= kb -> "%.2f KB".format(this / kb.toDouble())
        else -> "$this bytes"
    }
}

/**
 * Повертає значення в секундах, наприклад: 1.2.s -> "1.2s".
 */
val Double.s: String get() = "${this}s"

/**
 * Повертає значення в hex кольорі, наприклад: "d3d3d3".p -> "#d3d3d3".
 */
val String.hex: String get() = "#${this}"

/*fun calculateHash(_files: MutableMap<String, InputStream>): String {
    val files = _files
    val messageDigest = MessageDigest.getInstance("SHA-256")
    files.forEach { (fileName, inputStream) ->
        val buffer = ByteArray(1024)
        var bytesRead: Int

        while (inputStream.read(buffer).also { bytesRead = it } != -1) {
            messageDigest.update(buffer, 0, bytesRead)
        }

        inputStream.close()
    }

    val hashBytes = messageDigest.digest()

    return hashBytes.joinToString("") { "%02x".format(it) }
}
*/
@Deprecated(message = "Воно зламане, не чіпати")
fun MutableMap<String, InputStream>.calculateHash(): String {
    val messageDigest = MessageDigest.getInstance("SHA-256")

    val newStreams = mutableMapOf<String, InputStream>()

    this.forEach { (key, inputStream) ->
        val buffer = ByteArray(1024)
        val outputStream = ByteArrayOutputStream()

        var bytesRead: Int
        while (inputStream.read(buffer).also { bytesRead = it } != -1) {
            messageDigest.update(buffer, 0, bytesRead)
            outputStream.write(buffer, 0, bytesRead)
        }

        inputStream.close()

        newStreams[key] = outputStream.toByteArray().inputStream()
    }

    this.clear()
    this.putAll(newStreams)

    val hashBytes = messageDigest.digest()
    return hashBytes.joinToString("") { "%02x".format(it) }
}


