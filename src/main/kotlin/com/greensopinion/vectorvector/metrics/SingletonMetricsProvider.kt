package com.greensopinion.vectorvector.metrics

class SingletonMetricsProvider : MetricsProvider {
    val metrics = DefaultMetrics()

    override fun get(): Metrics = metrics
}