package com.greensopinion.elevation.processor.metrics

import io.github.oshai.kotlinlogging.KLogger
import io.github.oshai.kotlinlogging.KotlinLogging
import java.time.Duration
import java.time.Instant
import java.util.concurrent.atomic.AtomicInteger

class Progress(
    private val total: Int,
    private val interval: Duration = Duration.ofSeconds(30),
    private val log: KLogger = KotlinLogging.logger { }
) {
    private val completed = AtomicInteger()
    private val processed = AtomicInteger()

    private val start = Instant.now()

    @Volatile
    private var last = start


    fun completedOne(processed: Boolean) {
        if (processed) {
            this.processed.incrementAndGet()
        }
        if (completed.incrementAndGet() % 1000 == 0) {
            val last = this.last
            val elapsed = Duration.between(last, Instant.now())
            if (elapsed > interval) {
                this.last = Instant.now()
                logEstimate()
            }
        }
    }

    private fun logEstimate() {
        val elapsed = Duration.between(start, Instant.now())
        val completed = this.completed.get()
        val processed = this.processed.get()
        val remaining = total - completed
        val estimation: String
        if (processed == 0) {
            estimation = "no tiles processed, cannot estimate"
        } else {
            val perSecond = processed.toDouble() / elapsed.toSeconds().toDouble()
            val estimated = Duration.ofSeconds((remaining / perSecond).toLong())
            estimation = estimated.toLogString()
        }
        log.info { "$completed completed, $remaining remaining" }
        log.info { "elapsed time: ${elapsed.toLogString()}" }
        log.info { "estimated time remaining: $estimation" }
    }
}

private fun Duration.toLogString() : String {
    val days = this.toDays()
    val hours = (this - Duration.ofDays(days)).toHours()
    val minutes = (this - Duration.ofDays(days)-Duration.ofHours(hours)).toMinutes()
    return "$days days $hours hours $minutes minutes"
}