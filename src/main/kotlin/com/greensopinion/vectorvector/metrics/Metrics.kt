package com.greensopinion.vectorvector.metrics

interface Metrics {
    fun addCount(name: String, count: Int = 1)
    fun addCount(name: String, count: Boolean) {
        addCount(name, if (count) 1 else 0)
    }
}