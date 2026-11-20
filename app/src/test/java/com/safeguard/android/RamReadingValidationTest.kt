package com.safeguard.android

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class RamReadingValidationTest {
    @Test fun rejectsZeroOrNegativeTotal() {
        assertFalse(hasValidRamReading(0L, 0L))
        assertFalse(hasValidRamReading(-1L, 0L))
    }

    @Test fun rejectsNegativeOrExcessAvailableMemory() {
        assertFalse(hasValidRamReading(1024L, -1L))
        assertFalse(hasValidRamReading(1024L, 1025L))
    }

    @Test fun acceptsBoundaryAndOrdinaryReadings() {
        assertTrue(hasValidRamReading(1024L, 0L))
        assertTrue(hasValidRamReading(1024L, 512L))
        assertTrue(hasValidRamReading(1024L, 1024L))
    }
}
