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
    @Test fun acceptsSmallestValidTotalAndAvailableMemory() {
        assertTrue(hasValidRamReading(1L, 0L))
        assertTrue(hasValidRamReading(1L, 1L))
    }

    @Test fun handlesLongLimitsWithoutOverflow() {
        assertTrue(hasValidRamReading(Long.MAX_VALUE, Long.MAX_VALUE))
        assertTrue(hasValidRamReading(Long.MAX_VALUE, 0L))
        assertFalse(hasValidRamReading(Long.MAX_VALUE, Long.MIN_VALUE))
        assertFalse(hasValidRamReading(Long.MIN_VALUE, 0L))
    }
}
