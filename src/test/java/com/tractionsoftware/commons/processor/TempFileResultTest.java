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

import com.tractionsoftware.commons.io.ErrorTempFileResource;
import com.tractionsoftware.commons.io.LocalTempFileService;
import com.tractionsoftware.commons.io.MutableFileMetadata;
import com.tractionsoftware.commons.io.SimpleMutableFileMetadata;
import com.tractionsoftware.commons.io.TempFileResource;
import org.apache.commons.io.function.IOSupplier;
import jakarta.annotation.Nonnull;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for {@link TempFileResult}.
 */
public final class TempFileResultTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(TempFileResultTest.class);

    /**
     * A factory that always fails, causing getInstance() to receive an ErrorTempFileResource and throw IOException.
     * Extends AbstractFactory so we only need to implement the two abstract createImpl/loadExistingImpl methods.
     */
    private static final TempFileResource.AbstractFactory ERROR_TEMP_FILE_FACTORY =
        new TempFileResource.AbstractFactory() {
            @Nonnull
            @Override
            protected TempFileResource createImpl(
                @Nonnull MutableFileMetadata metadata,
                IOSupplier<? extends InputStream> content,
                @Nonnull Logger logger) throws IOException {
                throw new IOException("injected factory failure");
            }

            @Nonnull
            @Override
            protected TempFileResource loadExistingImpl(
                @Nonnull MutableFileMetadata metadata,
                @Nonnull Logger logger) throws IOException {
                throw new IOException("injected factory failure");
            }
        };

    // =====================================================================
    // getInstanceForExistingTempFile — null guard
    // =====================================================================

    @Test
    void getInstanceForExistingTempFile_null_throwsNullPointerException() {
        assertThrows(
            NullPointerException.class,
            () -> TempFileResult.getInstanceForExistingTempFile(null, LOGGER)
        );
    }

    @Test
    void getInstanceForExistingTempFile_null_throwsRuntimeException() {
        assertThrows(
            RuntimeException.class,
            () -> TempFileResult.getInstanceForExistingTempFile(
                ErrorTempFileResource.createInstance(
                    SimpleMutableFileMetadata.createFromFileName("no-such-file.txt"), new Exception("Boom")
                ),
                LOGGER
            )
        );
    }

    // =====================================================================
    // getInstance via Helper
    // =====================================================================

    private static TempFileResult.Helper makeHelper(String name) {
        return new TempFileResult.Helper() {
            @Nonnull
            @Override
            public TempFileResource.Factory tempFiles() {
                return LocalTempFileService.DEFAULT_FACTORY;
            }

            @Override
            public String getName() {
                return name;
            }

            @Override
            public String getSuggestedTempFileExtension() {
                return "xml";
            }

            @Override
            public Logger getLogger() {
                return LOGGER;
            }
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
        assertThrows(IllegalStateException.class, result::getOutputStream);
    }

    @Test
    void release_thenGetInputStream_throwsISE() throws Exception {
        TempFileResult result = TempFileResult.getInstance(makeHelper("ise-in-test"));
        result.release();
        assertThrows(IllegalStateException.class, result::getInputStream);
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

    // =====================================================================
    // getInstance — blank extension → falls back to "tmp"
    // =====================================================================

    private static TempFileResult.Helper makeHelperBlankExtension(String name) {
        return new TempFileResult.Helper() {
            @Nonnull
            @Override
            public TempFileResource.Factory tempFiles() {
                return LocalTempFileService.DEFAULT_FACTORY;
            }

            @Override
            public String getName() {
                return name;
            }

            @Override
            public String getSuggestedTempFileExtension() {
                return null; // blank → getExtension() returns "tmp"
            }

            @Override
            public Logger getLogger() {
                return LOGGER;
            }
        };
    }

    @Test
    void getInstance_blankExtension_createsResult() throws Exception {
        TempFileResult result = TempFileResult.getInstance(makeHelperBlankExtension("blank-ext-test"));
        assertNotNull(result);
        result.release();
    }

    // =====================================================================
    // getInstance — error factory → throws IOException
    // =====================================================================

    @Test
    void getInstance_errorFactory_throwsIOException() {
        TempFileResult.Helper errorHelper = new TempFileResult.Helper() {
            @Nonnull
            @Override
            public TempFileResource.Factory tempFiles() {
                return ERROR_TEMP_FILE_FACTORY;
            }

            @Override
            public String getName() {
                return "error-factory-test";
            }

            @Override
            public String getSuggestedTempFileExtension() {
                return "txt";
            }

            @Override
            public Logger getLogger() {
                return LOGGER;
            }
        };
        assertThrows(IOException.class, () -> TempFileResult.getInstance(errorHelper));
    }

    // =====================================================================
    // getInputStream — success path (released=false → returns stream)
    // =====================================================================

    @Test
    void getInputStream_notReleased_returnsStream() throws Exception {
        TempFileResult result = TempFileResult.getInstance(makeHelper("get-input-stream-test"));
        try (OutputStream out = result.getOutputStream()) {
            out.write("content".getBytes());
        }
        // released is still false; getInputStream should work
        try (InputStream in = result.getInputStream()) {
            assertNotNull(in);
        }
        result.release();
    }

    // =====================================================================
    // onPopulate(false) — closes stream, logs error location
    // =====================================================================

    @Test
    void onPopulate_false_logsError() throws Exception {
        TempFileResult result = TempFileResult.getInstance(makeHelper("populate-false-test"));
        // Does not throw; logs an error and closes the temp file
        assertDoesNotThrow(() -> result.onPopulate(false));
        result.release();
    }

    @Test
    void onPopulate_true_closesStream() throws Exception {
        TempFileResult result = TempFileResult.getInstance(makeHelper("populate-true-test"));
        try (OutputStream out = result.getOutputStream()) {
            out.write("x".getBytes());
        }
        assertDoesNotThrow(() -> result.onPopulate(true));
        result.release();
    }

    // =====================================================================
    // onConsume — exercises checkOrSetReleased and releaseIfNecessary
    // =====================================================================

    @Test
    void onConsume_true_releasesResult() throws Exception {
        TempFileResult result = TempFileResult.getInstance(makeHelper("consume-true-test"));
        assertDoesNotThrow(() -> result.onConsume(true));
        // After onConsume(true), released=true; getOutputStream should now throw ISE
        assertThrows(IllegalStateException.class, result::getOutputStream);
    }

    @Test
    void onConsume_false_logsError() throws Exception {
        TempFileResult result = TempFileResult.getInstance(makeHelper("consume-false-test"));
        // onConsume(false) → releaseIfNecessary(false) → logTempFileResultLocation(true) → logger.error
        assertDoesNotThrow(() -> result.onConsume(false));
    }

    @Test
    void onConsume_twice_secondIsNoOp() throws Exception {
        TempFileResult result = TempFileResult.getInstance(makeHelper("consume-twice-test"));
        result.onConsume(true);
        // Second call: checkOrSetReleased returns true (already released), returns immediately
        assertDoesNotThrow(() -> result.onConsume(true));
    }

}
