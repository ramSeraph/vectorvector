package com.greensopinion.vectorvector.downloader

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class AlosFilenamesTest {
    @Test
    fun `provides names`() {
        val names = AlosFilenames().generate()
        assertThat(names).contains(
            "N080W030_N085W025.zip",
            "N075W125_N080W120.zip",
            "N050W125_N055W120.zip",
            "S075W090_S070W085.zip",
            "S085W065_S080W060.zip",
            "N080W090_N085W085.zip",
            "N080W065_N085W060.zip",
            "N075W030_N080W025.zip",
            "N050W015_N055W010.zip"
        )
    }
}