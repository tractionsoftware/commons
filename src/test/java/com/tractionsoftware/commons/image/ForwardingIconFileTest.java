/*
 *
 *    Copyright 1996-2026 Traction Software, Inc.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 *
 */

// PLEASE DO NOT DELETE THIS LINE - make copyright depends on it.

package com.tractionsoftware.commons.image;

import com.tractionsoftware.commons.io.CommonFileResourceType;
import com.tractionsoftware.commons.util.Dimensions;
import jakarta.annotation.Nonnull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.FileNotFoundException;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for {@link ForwardingIconFile}.
 */
public final class ForwardingIconFileTest {

    /** Minimal concrete subclass of {@link ForwardingIconFile}. */
    private static final class TestIconFile extends ForwardingIconFile {

        private final IconFileResource delegate;

        TestIconFile(IconFileResource delegate) {
            this.delegate = delegate;
        }

        @Nonnull
        @Override
        protected IconFileResource delegate() {
            return delegate;
        }

    }

    private TestIconFile forwardingFile;

    @BeforeEach
    void setUp() {
        forwardingFile = new TestIconFile(new IconFileResource.InvalidIconFileResource(CommonFileResourceType.OTHER));
    }

    // =====================================================================
    // Delegation via InvalidIconFileResource
    // =====================================================================

    @Test
    void isValid_delegatesToInvalid() {
        assertFalse(forwardingFile.isValid());
    }

    @Test
    void getType_delegatesType() {
        assertEquals(CommonFileResourceType.OTHER, forwardingFile.getType());
    }

    @Test
    void getURI_notNull() {
        assertNotNull(forwardingFile.getURI());
    }

    @Test
    void getInputStream_throwsFileNotFound() {
        assertThrows(FileNotFoundException.class, () -> forwardingFile.getInputStream());
    }

    @Test
    void getDisplayName_null() {
        assertNull(forwardingFile.getDisplayName());
    }

    @Test
    void getOriginalDimensions_mayBeNull() {
        // InvalidIconFileResource has no valid image, so null or invalid dimensions are expected
        Dimensions<Integer> dims = forwardingFile.getOriginalDimensions();
        // Could be null if not set; just ensure no exception
        assertTrue(dims == null || dims.getWidth() == -1);
    }

    @Test
    void getImageResourceType_delegates() {
        assertNotNull(forwardingFile.getImageResourceType());
    }

    @Test
    void toDebugString_notNull() {
        assertNotNull(forwardingFile.toDebugString());
    }

}
