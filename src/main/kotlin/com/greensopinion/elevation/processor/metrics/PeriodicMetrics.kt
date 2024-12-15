package com.greensopinion.elevation.processor.metrics

import io.github.oshai.kotlinlogging.KLogger
import io.github.oshai.kotlinlogging.KotlinLogging
import java.io.Closeable
import java.time.Duration
import java.time.Instant
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicLong
import java.util.concurrent.atomic.AtomicReference

class PeriodicMetrics(
    private val metrics: DefaultMetrics,
    private val interval: Duration,
    private val log: KLogger = KotlinLogging.logger { }
) : Closeable {
    private val worker = Executors.newSingleThreadScheduledExecutor()
    private var last = Instant.now()

    init {
        worker.scheduleWithFixedDelay(
            ::collectAndReport,
            interval.toMillis(),
            interval.toMillis(),
            TimeUnit.MILLISECONDS
        )
    }

    private fun collectAndReport() {
        val metrics = metrics.getAndReset()
        val now = Instant.now()
        val elapsed = Duration.between(last, now)
        last = now
        val report = metrics.entries.sortedBy { it.key }.joinToString(separator = "\n") {
            val value = it.value.get()
            val perSecond = (it.value.toDouble() / elapsed.toNanos().toDouble()) * TimeUnit.SECONDS.toNanos(1)
            "${it.key}=${value},${perSecond}/s"
        }
        log.info { report }
    }

    override fun close() {
        worker.shutdownNow()
    }
}