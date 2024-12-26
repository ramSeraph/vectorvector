package com.greensopinion.vectorvector.util

import java.io.Closeable

fun closeSafely(closeables: Collection<Closeable>) {
    val exceptions = mutableListOf<Exception>()
    closeables.forEach {
        try {
            it.close()
        } catch (e: Exception) {
            exceptions.add(e)
        }
    }
    if (exceptions.isNotEmpty()) {
        val first = exceptions.removeFirst()
        exceptions.forEach {
            first.addSuppressed(it)
        }
        throw first
    }
}