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

package com.tractionsoftware.commons.lang;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ResourceTest {

    private static final class SimpleTestResource implements Resource {

        private boolean open = true;

        @Override
        public void close() {
            open = false;
        }

        @Override
        public boolean isOpen() {
            return open;
        }

    }

    // ---------------------------------------------------------------------------
    // Resource.isClosed() default method
    // ---------------------------------------------------------------------------

    @Test
    void isClosed_whenOpen_returnsFalse() {
        SimpleTestResource resource = new SimpleTestResource();
        assertTrue(resource.isOpen());
        assertFalse(resource.isClosed());
    }

    @Test
    void isClosed_afterClose_returnsTrue() {
        SimpleTestResource resource = new SimpleTestResource();
        resource.close();
        assertFalse(resource.isOpen());
        assertTrue(resource.isClosed());
    }

    // ---------------------------------------------------------------------------
    // Resource.LoadErrorType
    // ---------------------------------------------------------------------------

    @Test
    void loadErrorType_hasExpectedValues() {
        Resource.LoadErrorType[] values = Resource.LoadErrorType.values();
        assertEquals(3, values.length);
        assertEquals(Resource.LoadErrorType.SETUP, Resource.LoadErrorType.valueOf("SETUP"));
        assertEquals(Resource.LoadErrorType.OPEN, Resource.LoadErrorType.valueOf("OPEN"));
        assertEquals(Resource.LoadErrorType.CONFIRM, Resource.LoadErrorType.valueOf("CONFIRM"));
    }

    @Test
    void createFailedAttemptResult_delegatesToResourceUtil_andCarriesErrorTypeAndException() {
        RuntimeException error = new RuntimeException("boom");
        Resource.LoadAttemptResult result = Resource.LoadErrorType.OPEN.createFailedAttemptResult(error);
        assertNotNull(result);
        assertEquals(Resource.LoadErrorType.OPEN, result.getErrorType());
        assertSame(error, result.getFatalException());
        assertNull(result.getResource());
        assertFalse(result.wasSuccessful());
    }

    @Test
    void createFailedAttemptResult_eachErrorTypeProducesMatchingResult() {
        RuntimeException error = new RuntimeException("failure");
        for (Resource.LoadErrorType type : Resource.LoadErrorType.values()) {
            Resource.LoadAttemptResult result = type.createFailedAttemptResult(error);
            assertEquals(type, result.getErrorType());
            assertSame(error, result.getFatalException());
        }
    }

    // ---------------------------------------------------------------------------
    // Resource.LoadAttemptResult.wasSuccessful() default method
    // ---------------------------------------------------------------------------

    @Test
    void loadAttemptResult_wasSuccessful_trueWhenErrorTypeNull() {
        Resource resource = new SimpleTestResource();
        Resource.LoadAttemptResult result = new Resource.LoadAttemptResult() {

            @Override
            public Resource getResource() {
                return resource;
            }

            @Override
            public Resource.LoadErrorType getErrorType() {
                return null;
            }

            @Override
            public RuntimeException getFatalException() {
                return null;
            }

        };
        assertTrue(result.wasSuccessful());
        assertSame(resource, result.getResource());
    }

    @Test
    void loadAttemptResult_wasSuccessful_falseWhenErrorTypeNonNull() {
        Resource.LoadAttemptResult result = new Resource.LoadAttemptResult() {

            @Override
            public Resource getResource() {
                return null;
            }

            @Override
            public Resource.LoadErrorType getErrorType() {
                return Resource.LoadErrorType.SETUP;
            }

            @Override
            public RuntimeException getFatalException() {
                return new RuntimeException("setup failed");
            }

        };
        assertFalse(result.wasSuccessful());
    }

}
