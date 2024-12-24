package com.greensopinion.vectorvector.metrics

interface MetricsProvider {
    fun get() : Metrics
}