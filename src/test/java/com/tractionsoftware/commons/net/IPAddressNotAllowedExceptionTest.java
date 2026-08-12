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

package com.tractionsoftware.commons.net;

import org.junit.jupiter.api.Test;

import java.security.GeneralSecurityException;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests {@link IPAddressNotAllowedException}. Only the single-{@code String}-arg constructor is exercised via
 * production code paths (via {@link HostAddressUtil.InetAddressWrapper#checkAllowedX}), so this file covers all
 * three constructor overloads directly.
 */
class IPAddressNotAllowedExceptionTest {

    @Test
    void isGeneralSecurityException() {
        assertInstanceOf(GeneralSecurityException.class, new IPAddressNotAllowedException("msg"));
    }

    @Test
    void messageConstructor_setsMessage_nullCause() {
        IPAddressNotAllowedException ex = new IPAddressNotAllowedException("disallowed");
        assertEquals("disallowed", ex.getMessage());
        assertNull(ex.getCause());
    }

    @Test
    void causeConstructor_setsCause_messageDerivedFromCause() {
        Throwable cause = new IllegalStateException("root cause");
        IPAddressNotAllowedException ex = new IPAddressNotAllowedException(cause);
        assertSame(cause, ex.getCause());
        // Throwable(Throwable cause) derives the message from cause.toString() when no explicit message is given.
        assertEquals(cause.toString(), ex.getMessage());
    }

    @Test
    void causeConstructor_nullCause_messageIsNull() {
        IPAddressNotAllowedException ex = new IPAddressNotAllowedException((Throwable) null);
        assertNull(ex.getCause());
        assertNull(ex.getMessage());
    }

    @Test
    void messageAndCauseConstructor_setsBoth() {
        Throwable cause = new IllegalStateException("root cause");
        IPAddressNotAllowedException ex = new IPAddressNotAllowedException("disallowed", cause);
        assertEquals("disallowed", ex.getMessage());
        assertSame(cause, ex.getCause());
    }

}
