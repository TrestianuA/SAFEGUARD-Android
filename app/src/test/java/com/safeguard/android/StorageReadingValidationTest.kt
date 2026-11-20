package com.safeguard.android

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class StorageReadingValidationTest {
    @Test fun acceptsValidStorageReadings() {
        assertTrue(hasValidStorageReading(128L * 1024 * 1024, 64L * 1024 * 1024))
        assertTrue(hasValidStorageReading(1L, 0L))
        assertTrue(hasValidStorageReading(1L, 1L))
    }

    @Test fun rejectsInvalidStorageReadings() {
        assertFalse(hasValidStorageReading(0L, 0L))
        assertFalse(hasValidStorageReading(-1L, 0L))
        assertFalse(hasValidStorageReading(100L, -1L))
        assertFalse(hasValidStorageReading(100L, 101L))
    }
    @Test fun handlesLongLimitsWithoutOverflow() {
        assertTrue(hasValidStorageReading(Long.MAX_VALUE, Long.MAX_VALUE))
        assertTrue(hasValidStorageReading(Long.MAX_VALUE, 0L))
        assertFalse(hasValidStorageReading(Long.MAX_VALUE, Long.MIN_VALUE))
        assertFalse(hasValidStorageReading(Long.MIN_VALUE, 0L))
    }
}
