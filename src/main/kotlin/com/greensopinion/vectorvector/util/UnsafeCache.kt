package com.greensopinion.vectorvector.util

internal class UnsafeCache<K,V>(
    private val maxCacheSize: Int
) : LinkedHashMap<K, V>(101,0.4f,true) {
    override fun removeEldestEntry(eldest: MutableMap.MutableEntry<K, V>?): Boolean {
        return size > maxCacheSize
    }
}