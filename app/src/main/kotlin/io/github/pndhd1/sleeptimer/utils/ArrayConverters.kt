package io.github.pndhd1.sleeptimer.utils

import java.nio.ByteBuffer

fun List<Int>.toByteArray(): ByteArray {
    val buffer = ByteBuffer.allocate(size * Int.SIZE_BYTES)
    forEach { buffer.putInt(it) }
    return buffer.array()
}

fun ByteArray.toIntArray(): IntArray {
    val buffer = ByteBuffer.wrap(this)
    return IntArray(size / Int.SIZE_BYTES) { buffer.getInt() }
}
