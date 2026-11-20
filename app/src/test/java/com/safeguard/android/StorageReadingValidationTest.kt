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
}
