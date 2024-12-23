package com.greensopinion.elevation.processor.elevation

import com.google.common.cache.CacheBuilder
import com.google.common.cache.CacheLoader
import com.greensopinion.elevation.processor.ElevationTile
import com.greensopinion.elevation.processor.TileId

class CachingElevationDataStore(
    private val delegate: ElevationDataStore
) : ElevationDataStore {

    private val cache = CacheBuilder.newBuilder()
        .maximumSize(200)
        .build(object : CacheLoader<TileId, ElevationTile>() {
            override fun load(key: TileId) = delegate.get(key).materialize()
        })

    override fun get(tile: TileId): ElevationTile = cache.get(tile)
}