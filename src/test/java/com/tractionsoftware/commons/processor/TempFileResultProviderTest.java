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

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for {@link TempFileResultProvider}.
 */
public final class TempFileResultProviderTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(TempFileResultProviderTest.class);

    private static TempFileResult.Helper makeHelper() {
        return new TempFileResult.Helper() {
            @Override public TempFileResource.Factory tempFiles() { return LocalTempFileService.DEFAULT_FACTORY; }
            @Override public String getName() { return "provider-test"; }
            @Override public String getSuggestedTempFileExtension() { return "txt"; }
            @Override public Logger getLogger() { return LOGGER; }
        };
    }

    @Test
    void getInstance_returnsNonNull() {
        ResultProvider provider = TempFileResultProvider.getInstance(makeHelper());
        assertNotNull(provider);
    }

    @Test
    void getEmptyResult_returnsNewResult() throws Exception {
        ResultProvider provider = TempFileResultProvider.getInstance(makeHelper());
        Result result = provider.getEmptyResult();
        assertNotNull(result);
        result.release();
    }

    @Test
    void getEmptyResult_calledTwice_returnsDifferentInstances() throws Exception {
        ResultProvider provider = TempFileResultProvider.getInstance(makeHelper());
        Result r1 = provider.getEmptyResult();
        Result r2 = provider.getEmptyResult();
        assertNotSame(r1, r2);
        r1.release();
        r2.release();
    }

}
