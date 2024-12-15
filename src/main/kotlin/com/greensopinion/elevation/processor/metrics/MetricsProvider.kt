package com.greensopinion.elevation.processor.metrics

interface MetricsProvider {
    fun get() : Metrics
}