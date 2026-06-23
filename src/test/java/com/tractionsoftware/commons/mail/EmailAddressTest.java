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

package com.tractionsoftware.commons.mail;

import com.tractionsoftware.commons.net.URLUtil;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Dave Shepperton
 */
public class EmailAddressTest {

    // ---------------------------------------------------------------------------
    // constructor
    // ---------------------------------------------------------------------------

    @Test
    public void constructor_nullAddress_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new EmailAddress(null, "Dave"));
    }

    @Test
    public void constructor_blankFriendlyName_trimsToNull() {
        EmailAddress address = new EmailAddress("shep@tractionsoftware.com", "   ");
        assertNull(address.getFriendlyName());
    }

    @Test
    public void constructor_friendlyNameWithSurroundingWhitespace_trimsValue() {
        EmailAddress address = new EmailAddress("shep@tractionsoftware.com", "  Dave  ");
        assertEquals("Dave", address.getFriendlyName());
    }

    @Test
    public void constructor_nullFriendlyName_remainsNull() {
        EmailAddress address = new EmailAddress("shep@tractionsoftware.com", null);
        assertNull(address.getFriendlyName());
    }

    // ---------------------------------------------------------------------------
    // equals
    // ---------------------------------------------------------------------------

    @Test
    public void equals_notAnEmailAddress_returnsFalse() {
        EmailAddress address = new EmailAddress("shep@tractionsoftware.com", "Dave");
        assertNotEquals(address, "shep@tractionsoftware.com");
    }

    @Test
    public void equals_null_returnsFalse() {
        EmailAddress address = new EmailAddress("shep@tractionsoftware.com", "Dave");
        assertNotEquals(null, address);
    }

    @Test
    public void equals_sameAddressAndFriendlyName_returnsTrue() {
        EmailAddress a = new EmailAddress("shep@tractionsoftware.com", "Dave");
        EmailAddress b = new EmailAddress("shep@tractionsoftware.com", "Dave");
        assertEquals(a, b);
    }

    @Test
    public void equals_sameAddressBothNullFriendlyName_returnsTrue() {
        EmailAddress a = new EmailAddress("shep@tractionsoftware.com", null);
        EmailAddress b = new EmailAddress("shep@tractionsoftware.com", null);
        assertEquals(a, b);
    }

    @Test
    public void equals_differentAddress_returnsFalse() {
        EmailAddress a = new EmailAddress("shep@tractionsoftware.com", "Dave");
        EmailAddress b = new EmailAddress("admin@example.com", "Dave");
        assertNotEquals(a, b);
    }

    @Test
    public void equals_sameAddressDifferentFriendlyName_returnsFalse() {
        EmailAddress a = new EmailAddress("shep@tractionsoftware.com", "Dave");
        EmailAddress b = new EmailAddress("shep@tractionsoftware.com", "Someone Else");
        assertNotEquals(a, b);
    }

    @Test
    public void equals_sameAddressOneNullFriendlyName_returnsFalse() {
        EmailAddress a = new EmailAddress("shep@tractionsoftware.com", "Dave");
        EmailAddress b = new EmailAddress("shep@tractionsoftware.com", null);
        assertNotEquals(a, b);
    }

    // ---------------------------------------------------------------------------
    // hashCode
    // ---------------------------------------------------------------------------

    @Test
    public void hashCode_equalInstances_haveEqualHashCodes() {
        EmailAddress a = new EmailAddress("shep@tractionsoftware.com", "Dave");
        EmailAddress b = new EmailAddress("shep@tractionsoftware.com", "Dave");
        assertEquals(a.hashCode(), b.hashCode());
    }

    // ---------------------------------------------------------------------------
    // toString / getAddressWithFriendlyName
    // ---------------------------------------------------------------------------

    @Test
    public void toString_delegatesToGetAddressWithFriendlyName() {
        EmailAddress address = new EmailAddress("shep@tractionsoftware.com", "Dave");
        assertEquals(address.getAddressWithFriendlyName(), address.toString());
    }

    @Test
    public void getAddressWithFriendlyName_withFriendlyName_includesQuotedNameAndAddress() {
        EmailAddress address = new EmailAddress("shep@tractionsoftware.com", "Dave");
        assertEquals("\"Dave\" <shep@tractionsoftware.com>", address.getAddressWithFriendlyName());
    }

    @Test
    public void getAddressWithFriendlyName_withoutFriendlyName_returnsAddressOnly() {
        EmailAddress address = new EmailAddress("shep@tractionsoftware.com", null);
        assertEquals("shep@tractionsoftware.com", address.getAddressWithFriendlyName());
    }

    // ---------------------------------------------------------------------------
    // getDisplayName / hasFriendlyName
    // ---------------------------------------------------------------------------

    @Test
    public void hasFriendlyName_nullFriendlyName_returnsFalse() {
        EmailAddress address = new EmailAddress("shep@tractionsoftware.com", null);
        assertFalse(address.hasFriendlyName());
    }

    @Test
    public void hasFriendlyName_nonNullFriendlyName_returnsTrue() {
        EmailAddress address = new EmailAddress("shep@tractionsoftware.com", "Dave");
        assertTrue(address.hasFriendlyName());
    }

    @Test
    public void getDisplayName_withFriendlyName_returnsFriendlyName() {
        EmailAddress address = new EmailAddress("shep@tractionsoftware.com", "Dave");
        assertEquals("Dave", address.getDisplayName());
    }

    @Test
    public void getDisplayName_withoutFriendlyName_returnsAddress() {
        EmailAddress address = new EmailAddress("shep@tractionsoftware.com", null);
        assertEquals("shep@tractionsoftware.com", address.getDisplayName());
    }

    // ---------------------------------------------------------------------------
    // getAddress / getUrl
    // ---------------------------------------------------------------------------

    @Test
    public void getAddress_returnsConstructorValue() {
        EmailAddress address = new EmailAddress("shep@tractionsoftware.com", "Dave");
        assertEquals("shep@tractionsoftware.com", address.getAddress());
    }

    @Test
    public void getUrl_returnsMailtoUrlWithEncodedAddress() {
        EmailAddress address = new EmailAddress("shep@tractionsoftware.com", "Dave");
        String expected = "mailto:" + URLUtil.getUrlEncoding("shep@tractionsoftware.com");
        assertEquals(expected, address.getUrl());
        assertTrue(address.getUrl().startsWith("mailto:"));
    }

}
