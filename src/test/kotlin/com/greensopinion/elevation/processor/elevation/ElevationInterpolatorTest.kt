package com.greensopinion.elevation.processor.elevation

import com.greensopinion.elevation.processor.Elevation
import com.greensopinion.elevation.processor.ElevationTile
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.mockito.Mockito.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

class ElevationInterpolatorTest {

    @Test
    fun `applies bilinear interpolation between points`() {
        val tile = mock<ElevationTile>().also {
            doReturn(Elevation(10.0)).whenever(it).get(0, 0)
            doReturn(Elevation(20.0)).whenever(it).get(1, 0)
            doReturn(Elevation(30.0)).whenever(it).get(1, 1)
            doReturn(Elevation(40.0)).whenever(it).get(0, 1)
        }
        assertThat(ElevationInterpolator(tile).get(Offset(0.0,0.0))).isEqualTo(Elevation(10.0))
        assertThat(ElevationInterpolator(tile).get(Offset(1.0,0.0))).isEqualTo(Elevation(20.0))
        assertThat(ElevationInterpolator(tile).get(Offset(1.0,1.0))).isEqualTo(Elevation(30.0))
        assertThat(ElevationInterpolator(tile).get(Offset(0.0,1.0))).isEqualTo(Elevation(40.0))
        assertThat(ElevationInterpolator(tile).get(Offset(0.5,0.0))).isEqualTo(Elevation(15.0))
        assertThat(ElevationInterpolator(tile).get(Offset(0.0,0.5))).isEqualTo(Elevation(25.0))
        assertThat(ElevationInterpolator(tile).get(Offset(0.5,0.5))).isEqualTo(Elevation(25.0))
    }
}