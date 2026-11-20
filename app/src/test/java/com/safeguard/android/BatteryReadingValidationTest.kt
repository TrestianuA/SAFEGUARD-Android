package com.safeguard.android

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class BatteryReadingValidationTest {
    @Test fun rejectsInvalidLevelAndScale() {
        assertNull(batteryPercentage(-1, 100))
        assertNull(batteryPercentage(50, 0))
        assertNull(batteryPercentage(50, -1))
        assertNull(batteryPercentage(101, 100))
    }

    @Test fun acceptsBoundaryAndTypicalReadings() {
        assertEquals(0, batteryPercentage(0, 100))
        assertEquals(50, batteryPercentage(50, 100))
        assertEquals(100, batteryPercentage(100, 100))
        assertEquals(33, batteryPercentage(1, 3))
    }

    @Test fun calculatesWithoutIntegerOverflow() {
        assertEquals(100, batteryPercentage(Int.MAX_VALUE, Int.MAX_VALUE))
        assertEquals(49, batteryPercentage(Int.MAX_VALUE / 2, Int.MAX_VALUE))
        assertNull(batteryPercentage(Int.MAX_VALUE, 1))
    }
}
