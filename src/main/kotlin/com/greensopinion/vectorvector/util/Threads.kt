package com.greensopinion.vectorvector.util

import java.util.concurrent.ThreadFactory
import java.util.concurrent.atomic.AtomicInteger

fun newThreadFactory(namePrefix: String, daemon: Boolean = false): ThreadFactory = object : ThreadFactory {
    private val idSeed = AtomicInteger()

    override fun newThread(r: Runnable) = Thread(r).also {
        it.name = "$namePrefix-${idSeed.incrementAndGet()}"
        it.isDaemon = daemon
    }
}