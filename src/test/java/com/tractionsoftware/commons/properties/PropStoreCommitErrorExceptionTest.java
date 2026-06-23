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

package com.tractionsoftware.commons.properties;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PropStoreCommitErrorExceptionTest {

    @Test
    void noArgConstructor_hasNullMessage() {
        PropStoreCommitErrorException ex = new PropStoreCommitErrorException();
        assertNull(ex.getMessage());
    }

    @Test
    void messageConstructor_retainsMessage() {
        PropStoreCommitErrorException ex = new PropStoreCommitErrorException("commit failed");
        assertEquals("commit failed", ex.getMessage());
    }

    @Test
    void causeConstructor_retainsCauseAndUsesItsToStringAsMessage() {
        Throwable cause = new IllegalStateException("io error");
        PropStoreCommitErrorException ex = new PropStoreCommitErrorException(cause);
        assertSame(cause, ex.getCause());
        assertEquals(cause.toString(), ex.getMessage());
    }

    @Test
    void messageAndCauseConstructor_retainsCauseAndMessage() {
        Throwable cause = new IllegalStateException("io error");
        PropStoreCommitErrorException ex = new PropStoreCommitErrorException("commit failed", cause);
        assertSame(cause, ex.getCause());
        assertEquals("commit failed", ex.getMessage());
    }

    @Test
    void getStatus_returnsErrorFailureStatus() {
        PropStoreCommitErrorException ex = new PropStoreCommitErrorException();
        assertEquals(CommitResult.StandardFailureStatus.ERROR, ex.getStatus());
        assertTrue(ex.getStatus().failed());
    }

}
