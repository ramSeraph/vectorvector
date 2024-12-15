package com.greensopinion.elevation.processor.metrics

import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger

class DefaultMetrics : Metrics {
    private val countByName = ConcurrentHashMap<String, AtomicInteger>()

    override fun addCount(name: String, count: Int) {
        val counter = countByName[name] ?: countByName.computeIfAbsent(name) { AtomicInteger() }
        counter.addAndGet(count)
    }

    fun getAndReset() = countByName.toMap().also { countByName.clear() }
}