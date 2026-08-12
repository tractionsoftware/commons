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

import com.tractionsoftware.commons.properties.GetProperty;
import jakarta.mail.Header;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public final class EmailHeadersTest {

    // Raw header lines for a simple test message
    private static final List<String> RAW_LINES = List.of(
        "Subject: Hello World",
        "From: sender@example.com",
        "To: recipient@example.com",
        "Message-ID: <12345@example.com>",
        "X-Custom: first",
        "X-Custom: second"
    );

    private EmailHeaders headers;

    @BeforeEach
    void setUp() {
        headers = MailUtil.createEmailHeadersFromRawLines(RAW_LINES);
    }

    // =====================================================================
    // NONE constant
    // =====================================================================

    @Test
    void none_getHeaders_returnsEmpty() {
        assertTrue(EmailHeaders.NONE.getHeaders("Subject").isEmpty());
    }

    @Test
    void none_hasHeader_false() {
        assertFalse(EmailHeaders.NONE.hasHeader("Subject"));
    }

    @Test
    void none_getRawHeaderLines_isEmpty() {
        assertFalse(EmailHeaders.NONE.getRawHeaderLines().iterator().hasNext());
    }

    @Test
    void none_iterator_isEmpty() {
        assertFalse(EmailHeaders.NONE.iterator().hasNext());
    }

    @Test
    void none_getHeader_null() {
        assertNull(EmailHeaders.NONE.getHeader("Subject"));
    }

    // =====================================================================
    // getHeaders (concrete implementation via createEmailHeadersFromRawLines)
    // =====================================================================

    @Test
    void getHeaders_presentName_returnsList() {
        List<String> values = headers.getHeaders("Subject");
        assertEquals(1, values.size());
        assertEquals("Hello World", values.get(0));
    }

    @Test
    void getHeaders_absentName_returnsEmpty() {
        assertTrue(headers.getHeaders("X-NoSuchHeader").isEmpty());
    }

    @Test
    void getHeaders_null_returnsEmpty() {
        assertTrue(headers.getHeaders(null).isEmpty());
    }

    @Test
    void getHeaders_multiValued_returnsAll() {
        List<String> values = headers.getHeaders("X-Custom");
        assertEquals(2, values.size());
        assertTrue(values.contains("first"));
        assertTrue(values.contains("second"));
    }

    // =====================================================================
    // getHeader (default method — concatenates multiple values)
    // =====================================================================

    @Test
    void getHeader_single_returnsValue() {
        assertEquals("Hello World", headers.getHeader("Subject"));
    }

    @Test
    void getHeader_absent_returnsNull() {
        assertNull(headers.getHeader("X-NoSuchHeader"));
    }

    @Test
    void getHeader_multiValued_returnsConcatenated() {
        String val = headers.getHeader("X-Custom");
        assertNotNull(val);
        assertTrue(val.contains("first"), val);
        assertTrue(val.contains("second"), val);
        assertTrue(val.contains(", "), val); // comma-space separator
    }

    // =====================================================================
    // hasHeader (default method)
    // =====================================================================

    @Test
    void hasHeader_present_true() {
        assertTrue(headers.hasHeader("Subject"));
    }

    @Test
    void hasHeader_absent_false() {
        assertFalse(headers.hasHeader("X-NoSuchHeader"));
    }

    @Test
    void hasHeader_null_false() {
        assertFalse(headers.hasHeader(null));
    }

    // =====================================================================
    // getRawHeaderLines
    // =====================================================================

    @Test
    void getRawHeaderLines_returnsIterable() {
        Iterable<String> raw = headers.getRawHeaderLines();
        assertNotNull(raw);
        List<String> list = new ArrayList<>();
        raw.forEach(list::add);
        assertFalse(list.isEmpty());
    }

    @Test
    void getRawHeaderLines_containsExpectedLine() {
        List<String> raw = new ArrayList<>();
        headers.getRawHeaderLines().forEach(raw::add);
        assertTrue(raw.stream().anyMatch(l -> l.contains("Subject")), raw.toString());
    }

    // =====================================================================
    // iterator
    // =====================================================================

    @Test
    void iterator_returnsHeaders() {
        Iterator<Header> it = headers.iterator();
        assertTrue(it.hasNext());
    }

    @Test
    void iterator_coversAllHeaders() {
        List<String> names = new ArrayList<>();
        headers.forEach(h -> names.add(h.getName()));
        assertTrue(names.contains("Subject"));
        assertTrue(names.contains("From"));
        assertTrue(names.contains("To"));
    }

    // =====================================================================
    // getAddresses (default method)
    // =====================================================================

    @Test
    void getAddresses_validFromHeader_returnsAddresses() {
        var addresses = headers.getAddresses("From");
        assertNotNull(addresses);
        assertFalse(addresses.isEmpty());
        assertEquals("sender@example.com", addresses.get(0).getAddress());
    }

    @Test
    void getAddresses_absentHeader_returnsNullOrEmpty() {
        var addresses = headers.getAddresses("X-NoSuchHeader");
        // safeParseAddresses on null returns null
        assertNull(addresses);
    }

    // =====================================================================
    // asGetProperty (default method)
    // =====================================================================

    @Test
    void asGetProperty_getProperty_returnsConcatenatedValue() {
        GetProperty gp = headers.asGetProperty();
        assertNotNull(gp);
        assertEquals("Hello World", gp.getProperty("Subject"));
    }

    @Test
    void asGetProperty_getProperty_absent_returnsNull() {
        GetProperty gp = headers.asGetProperty();
        assertNull(gp.getProperty("X-NoSuchHeader"));
    }

    @Test
    void asGetProperty_hasProperty_true() {
        assertTrue(headers.asGetProperty().hasProperty("Subject"));
    }

    @Test
    void asGetProperty_hasProperty_false() {
        assertFalse(headers.asGetProperty().hasProperty("X-NoSuchHeader"));
    }

    @Test
    void asGetProperty_getPropertyNames_containsAllHeaderNames() {
        var names = headers.asGetProperty().getPropertyNames();
        assertTrue(names.contains("Subject"));
        assertTrue(names.contains("From"));
        assertTrue(names.contains("To"));
    }

    // =====================================================================
    // asGetProperties (default method)
    // =====================================================================

    @Test
    void asGetProperties_getProperties_singleValue() {
        var gps = headers.asGetProperties();
        assertNotNull(gps);
        List<String> collected = new ArrayList<>();
        gps.getProperties("Subject", collected);
        assertEquals(List.of("Hello World"), collected);
    }

    @Test
    void asGetProperties_getProperties_multiValued_returnsAll() {
        var gps = headers.asGetProperties();
        List<String> collected = new ArrayList<>();
        gps.getProperties("X-Custom", collected);
        assertEquals(2, collected.size());
    }

    @Test
    void asGetProperties_toString_notNull() {
        assertNotNull(headers.asGetProperties().toString());
    }

    // =====================================================================
    // constants
    // =====================================================================

    @Test
    void nameConstants_notNull() {
        assertNotNull(EmailHeaders.NAME_MESSAGE_ID);
        assertNotNull(EmailHeaders.NAME_SUBJECT);
        assertNotNull(EmailHeaders.NAME_FROM);
        assertNotNull(EmailHeaders.NAME_TO);
        assertNotNull(EmailHeaders.NAME_CC);
        assertNotNull(EmailHeaders.NAME_BCC);
        assertNotNull(EmailHeaders.NAME_REPLY_TO);
        assertNotNull(EmailHeaders.NAME_IN_REPLY_TO);
        assertNotNull(EmailHeaders.NAME_REFERENCES);
    }

}
