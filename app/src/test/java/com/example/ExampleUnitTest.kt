package com.example

import com.example.data.ParkingRecord
import com.example.data.PreferredMapApp
import com.example.util.ParkingFormatters
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {

    @Test
    fun `formatCoordinates returns formatted lat lng or fallback`() {
        assertEquals(
            "37.497950, 127.027620",
            ParkingFormatters.formatCoordinates(37.49795, 127.02762),
        )
        assertEquals(
            "좌표 없음 (메모·사진 전용)",
            ParkingFormatters.formatCoordinates(null, null),
        )
    }

    @Test
    fun `formatAccuracy rounds meters`() {
        assertEquals("±6m", ParkingFormatters.formatAccuracy(5.7f))
        assertNull(ParkingFormatters.formatAccuracy(null))
    }

    @Test
    fun `formatElapsedDuration handles minutes hours and days`() {
        val base = 1_700_000_000_000L
        assertEquals("방금 주차", ParkingFormatters.formatElapsedDuration(base, base + 20_000L))
        assertEquals("15분 경과", ParkingFormatters.formatElapsedDuration(base, base + 15 * 60_000L))
        assertEquals("2시간 30분 경과", ParkingFormatters.formatElapsedDuration(base, base + 150 * 60_000L))
        assertEquals("2일 경과", ParkingFormatters.formatElapsedDuration(base, base + 48 * 3_600_000L))
    }

    @Test
    fun `parkingRecord hasCoordinates checks both latitude and longitude`() {
        val withCoords = ParkingRecord(latitude = 37.5, longitude = 127.0)
        val withoutCoords = ParkingRecord(latitude = null, longitude = null, memo = "지하 2층")
        assertTrue(withCoords.hasCoordinates)
        assertFalse(withoutCoords.hasCoordinates)
    }

    @Test
    fun `preferredMapApp fromName falls back to AUTO`() {
        assertEquals(PreferredMapApp.KAKAO, PreferredMapApp.fromName("KAKAO"))
        assertEquals(PreferredMapApp.NAVER, PreferredMapApp.fromName("NAVER"))
        assertEquals(PreferredMapApp.GOOGLE, PreferredMapApp.fromName("GOOGLE"))
        assertEquals(PreferredMapApp.AUTO, PreferredMapApp.fromName("UNKNOWN"))
        assertEquals(PreferredMapApp.AUTO, PreferredMapApp.fromName(null))
    }
}
