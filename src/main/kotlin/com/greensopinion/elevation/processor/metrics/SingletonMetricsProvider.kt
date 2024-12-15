package com.greensopinion.elevation.processor.metrics

class SingletonMetricsProvider : MetricsProvider {
    val metrics = DefaultMetrics()

    override fun get(): Metrics = metrics
}