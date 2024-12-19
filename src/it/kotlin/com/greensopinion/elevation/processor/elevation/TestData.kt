package com.greensopinion.elevation.processor.elevation

import com.greensopinion.elevation.processor.metrics.SingletonMetricsProvider
import java.io.File

val testBlockStore = CachingBlockStore(
    FilesystemBlockStore(
        blockExtent = 6000,
        folder = File("../../data/tif")
    ),
    metricsProvider = SingletonMetricsProvider()
)