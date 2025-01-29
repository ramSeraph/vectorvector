package com.greensopinion.vectorvector.downloader

class AlosFilenames {
    fun generate() : Set<String> {
        val names = mutableSetOf<String>()
        for (latitude in -90 until 90 step 5) {
            for (longitude in -180 until 180 step 5) {
                val bottomLeft = format(latitude,longitude)
                val topRight = format(latitude+5,longitude+5)
                names.add("${bottomLeft}_$topRight.zip")
            }
        }
        return names.toSet()
    }
}

private fun format(latitude: Int, longitude: Int) : String {
    val latitudePart = if (latitude < 0) "S${"%03d".format(-latitude)}" else "N${"%03d".format(latitude)}"
    val longitudePart = if (longitude < 0) "W${"%03d".format(-longitude)}" else "E${"%03d".format(longitude)}"
    return latitudePart + longitudePart
}