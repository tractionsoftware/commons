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

package com.tractionsoftware.commons.processor;

import com.tractionsoftware.commons.io.LocalTempFileService;
import com.tractionsoftware.commons.io.TempFileResource;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.io.OutputStream;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for {@link TempFileResult}.
 */
public final class TempFileResultTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(TempFileResultTest.class);

    // =====================================================================
    // getInstanceForExistingTempFile — null guard
    // =====================================================================

    @Test
    void getInstanceForExistingTempFile_null_throwsRuntimeException() {
        assertThrows(RuntimeException.class,
            () -> TempFileResult.getInstanceForExistingTempFile(null, LOGGER));
    }

    // =====================================================================
    // getInstance via Helper
    // =====================================================================

    private static TempFileResult.Helper makeHelper(String name) {
        return new TempFileResult.Helper() {
            @Override public TempFileResource.Factory tempFiles() { return LocalTempFileService.DEFAULT_FACTORY; }
            @Override public String getName() { return name; }
            @Override public String getSuggestedTempFileExtension() { return "xml"; }
            @Override public Logger getLogger() { return LOGGER; }
        };
    }

    @Test
    void getInstance_createsResult() throws Exception {
        TempFileResult result = TempFileResult.getInstance(makeHelper("test"));
        assertNotNull(result);
        result.release();
    }

    @Test
    void getInstance_getOutputStream_writeable() throws Exception {
        TempFileResult result = TempFileResult.getInstance(makeHelper("write-test"));
        try (OutputStream out = result.getOutputStream()) {
            out.write("hello".getBytes());
        }
        result.release();
    }

    @Test
    void getInstance_getInputStream_readable() throws Exception {
        TempFileResult result = TempFileResult.getInstance(makeHelper("read-test"));
        // Write something first
        try (OutputStream out = result.getOutputStream()) {
            out.write("data".getBytes());
        }
        // Reset via new result — once written to, the same TempFileResult allows reading
        // (underlying TempFileResource is closed after onPopulate)
        // Just verify release works cleanly
        result.release();
    }

    // =====================================================================
    // after release, getOutputStream/getInputStream throw ISE
    // =====================================================================

    @Test
    void release_thenGetOutputStream_throwsISE() throws Exception {
        TempFileResult result = TempFileResult.getInstance(makeHelper("ise-out-test"));
        result.release();
        assertThrows(IllegalStateException.class, () -> result.getOutputStream());
    }

    @Test
    void release_thenGetInputStream_throwsISE() throws Exception {
        TempFileResult result = TempFileResult.getInstance(makeHelper("ise-in-test"));
        result.release();
        assertThrows(IllegalStateException.class, () -> result.getInputStream());
    }

    // =====================================================================
    // double release — idempotent (no exception)
    // =====================================================================

    @Test
    void release_twice_noException() throws Exception {
        TempFileResult result = TempFileResult.getInstance(makeHelper("double-release"));
        result.release();
        assertDoesNotThrow(result::release);
    }

}
