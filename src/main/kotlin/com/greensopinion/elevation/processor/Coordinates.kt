package com.greensopinion.elevation.processor

data class Coordinates(val latitude: Double, val longitude: Double) {
    override fun toString(): String = "$latitude,$longitude"
}