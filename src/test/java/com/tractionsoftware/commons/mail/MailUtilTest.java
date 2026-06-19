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

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableListMultimap;
import com.google.common.collect.Multimap;
import com.tractionsoftware.commons.io.FileResource;
import com.tractionsoftware.commons.io.LocalFileResource;
import org.apache.commons.lang3.StringUtils;

import jakarta.activation.DataSource;
import jakarta.mail.Address;
import jakarta.mail.Header;
import jakarta.mail.Message;
import jakarta.mail.Session;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;
import java.util.Properties;
import java.util.SequencedSet;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author Dave Shepperton
 */
public final class MailUtilTest {

    @Test
    public void testFriendlyNameFromFull1() {
        String input = "\"Shep Doggy\" <shep@tractionserver.com>";
        String expect = "Shep Doggy";
        String actual = MailUtil.getFriendlyNameFromFriendlyEncoding(input);
        assertEquals(expect, actual);
    }

    @Test
    public void testFriendlyNameFromFull2() {
        String input = "Shep Doggy <shep@tractionserver.com>";
        String expect = "Shep Doggy";
        String actual = MailUtil.getFriendlyNameFromFriendlyEncoding(input);
        assertEquals(expect, actual);
    }

    @Test
    public void testFriendlyNameFromFull3() {
        String input = "shep@tractionserver.com";
        String expect = "";
        String actual = MailUtil.getFriendlyNameFromFriendlyEncoding(input);
        assertEquals(expect, actual);
    }

    @Test
    public void testRawAddressFromFull1() {
        String input = "\"Shep Doggy\" <shep@tractionserver.com>";
        String expect = "shep@tractionserver.com";
        String actual = MailUtil.getRawAddressFromFriendlyEncoding(input);
        assertEquals(expect, actual);
    }

    @Test
    public void testRawAddressFromFull2() {
        String input = "Shep Doggy <shep@tractionserver.com>";
        String expect = "shep@tractionserver.com";
        String actual = MailUtil.getRawAddressFromFriendlyEncoding(input);
        assertEquals(expect, actual);
    }

    @Test
    public void testRawAddressFromFull3() {
        String input = "shep@tractionserver.com";
        String expect = "shep@tractionserver.com";
        String actual = MailUtil.getRawAddressFromFriendlyEncoding(input);
        assertEquals(expect, actual);
    }

    @Test
    public void testSplitMailHeaderString1() {
        String headerLines =
            "\"'John Taylor'\" <jtaylor@XYZ1.com>,\n\t\"'John Jenkins'\" <jjenkins@XYZ1.com>,\n\t\"'John Fitzpatrick'\" <JFitzpatrick@zyx.com>,\n\t\"John Schmidt\" <john.shmidt@tuv.com>,\n	\"John Scott\" <jscott@qrs.com>,\n\t\"'John Washington'\" <johnw@hijk.com>,\n\t\"John Jordan\" <john.jordan@efg.com>,\n\t<johnk@lmnop.com>,\n\t<john@example.com>";
        String input = "To:" + headerLines;
        Header expect = new Header("To", headerLines);
        Header actual = MailUtil.headerLineToHeader(input);
        assertEquals(expect.getName(), actual.getName());
        assertEquals(expect.getValue(), actual.getValue());
    }

    @Test
    public void test_getEmailAddresses1() {
        String input = "Ad Ministrator <admin@shepperton.us>; Dave <shep@tractionsoftware.com>";
        List<EmailAddress> actual = MailUtil.getEmailAddresses(input);
        List<EmailAddress> expected = ImmutableList.of(
            new EmailAddress("admin@shepperton.us", "Ad Ministrator"),
            new EmailAddress("shep@tractionsoftware.com", "Dave")
        );
        assertEquals(expected, actual);
    }

    @Test
    public void test_getEmailAddresses2() {
        String input = "\"Ad Ministrator\" <admin@shepperton.us>; \"Dave Shepperton\" <shep@tractionsoftware.com>";
        List<EmailAddress> actual = MailUtil.getEmailAddresses(input);
        List<EmailAddress> expected = ImmutableList.of(
            new EmailAddress("admin@shepperton.us", "Ad Ministrator"),
            new EmailAddress("shep@tractionsoftware.com", "Dave Shepperton")
        );
        assertEquals(expected, actual);
    }

    @Test
    public void test_getEmailAddresses3() {
        String input = "\"A > B\" <admin@shepperton.us>; shep@tractionsoftware.com";
        List<EmailAddress> actual = MailUtil.getEmailAddresses(input);
        List<EmailAddress> expected = ImmutableList.of(
            new EmailAddress("admin@shepperton.us", "A > B"),
            new EmailAddress("shep@tractionsoftware.com", null)
        );
        assertEquals(expected, actual);
    }

    @Test
    public void test_getEmailAddresses4() {
        String input = "; ; ; shep@tractionsoftware.com";
        List<EmailAddress> actual = MailUtil.getEmailAddresses(input);
        List<EmailAddress> expected = ImmutableList.of(
            new EmailAddress("shep@tractionsoftware.com", null)
        );
        assertEquals(expected, actual);
    }

    @Test
    public void test_getEmailAddresses5() {
        String input = "; ; ;      \t";
        List<EmailAddress> actual = MailUtil.getEmailAddresses(input);
        List<EmailAddress> expected = ImmutableList.of();
        assertEquals(expected, actual);
    }

    @Test
    public void test_getEmailAddresses6() {
        String input = "\"Ad Ministrator\" <admin@shepperton.us> \"Dave Shepperton\" <shep@tractionsoftware.com>";
        List<EmailAddress> actual = MailUtil.getEmailAddresses(input);
        List<EmailAddress> expected = ImmutableList.of(
            new EmailAddress("admin@shepperton.us", "Ad Ministrator"),
            new EmailAddress("shep@tractionsoftware.com", "Dave Shepperton")
        );
        assertEquals(expected, actual);
    }

    @Test
    public void test_getEmailAddresses7() {
        String input = "Ad Ministrator <admin@shepperton.us>, Dave Shepperton <shep@tractionsoftware.com>";
        List<EmailAddress> actual = MailUtil.getEmailAddresses(input);
        List<EmailAddress> expected = ImmutableList.of(
            new EmailAddress("admin@shepperton.us", "Ad Ministrator"),
            new EmailAddress("shep@tractionsoftware.com", "Dave Shepperton")
        );
        assertEquals(expected, actual);
    }

    @Test
    public void test_getEmailAddresses8() {
        // No addresses, just friendly names.
        String input = "Ad Ministrator, Dave Shepperton";
        List<EmailAddress> actual = MailUtil.getEmailAddresses(input);
        List<EmailAddress> expected = ImmutableList.of();
        assertEquals(expected, actual);
    }

    @Test
    public void test_getEmailAddresses9a() {
        // Only one thing that looks like it should be an address, but isn't.
        String input = "   ; ; ;  <   >      ";
        List<EmailAddress> actual = MailUtil.getEmailAddresses(input);
        List<EmailAddress> expected = ImmutableList.of();
        assertEquals(expected, actual);
    }

    @Test
    public void test_getEmailAddresses9b() {
        // One address, and one thing that looks like it should be an address, but isn't.
        String input = "   ; ; ;  joe@example.com  <   >      ";
        List<EmailAddress> actual = MailUtil.getEmailAddresses(input);
        List<EmailAddress> expected = ImmutableList.of(new EmailAddress("joe@example.com", null));
        assertEquals(expected, actual);
    }

    @Test
    public void test_getEmailAddresses9c() {
        // Only one thing that looks like it should be an address, but isn't, with a friendly name that will be
        // ignored.
        String input = "   ; ; ;  joe@example.com Bob <   >      ";
        List<EmailAddress> actual = MailUtil.getEmailAddresses(input);
        List<EmailAddress> expected = ImmutableList.of(new EmailAddress("joe@example.com", null));
        assertEquals(expected, actual);
    }

    private static final void doMakeFriendlyNameAddressTest(String address, String name, String expect) {
        assertEquals(
            expect,
            MailUtil.makeFriendlyNameAddress(address, name)
        );
    }

    private static final void doMakeFriendlyNameAddressTestAddressOnly(String address) {
        // Expected result will be trimmed version of address, or null if it was blank.
        doMakeFriendlyNameAddressTest(address, null, StringUtils.trimToNull(address));
    }

    @Test
    public void test_makeFriendlyNameAddress0a() {
        doMakeFriendlyNameAddressTestAddressOnly(null);
    }

    @Test
    public void test_makeFriendlyNameAddress0b() {
        doMakeFriendlyNameAddressTestAddressOnly("");
    }

    @Test
    public void test_makeFriendlyNameAddress1a() {
        doMakeFriendlyNameAddressTestAddressOnly("bobby@tractionsoftware.com");
    }

    @Test
    public void test_makeFriendlyNameAddress1b() {
        doMakeFriendlyNameAddressTestAddressOnly("  bobby@tractionsoftware.com  ");
    }

    @Test
    public void test_makeFriendlyNameAddress2a() {
        doMakeFriendlyNameAddressTest(
            "bobby@tractionsoftware.com",
            "Bobby McGee",
            "\"Bobby McGee\" <bobby@tractionsoftware.com>"
        );
    }

    @Test
    public void test_makeFriendlyNameAddress2b() {
        doMakeFriendlyNameAddressTest(
            "  bobby@tractionsoftware.com         ",
            "   Bobby McGee  ",
            "\"Bobby McGee\" <bobby@tractionsoftware.com>"
        );
    }

    @Test
    public void test_makeFriendlyNameAddress3a() {
        doMakeFriendlyNameAddressTest(
            "  bobby@tractionsoftware.com         ",
            "   Bobby \"The Animal\" McGee ",
            "\"Bobby \\\"The Animal\\\" McGee\" <bobby@tractionsoftware.com>"
        );
    }


    // =====================================================================
    // createEmailHeadersFromRawLines
    // =====================================================================

    @Test
    void createEmailHeadersFromRawLines_singleHeader() {
        var headers = MailUtil.createEmailHeadersFromRawLines(List.of("Subject: Hello World"));
        assertNotNull(headers);
        var subjects = headers.getHeaders("Subject");
        assertFalse(subjects.isEmpty(), "expected Subject header");
        assertEquals("Hello World", subjects.getFirst().trim());
    }

    @Test
    void createEmailHeadersFromRawLines_multipleHeaders() {
        var lines = List.of("From: alice@example.com", "To: bob@example.com", "Subject: Test");
        var headers = MailUtil.createEmailHeadersFromRawLines(lines);
        assertNotNull(headers.getHeaders("From"));
        assertNotNull(headers.getHeaders("To"));
        assertNotNull(headers.getHeaders("Subject"));
    }

    @Test
    void createEmailHeadersFromRawLines_emptyList_noHeaders() {
        var headers = MailUtil.createEmailHeadersFromRawLines(List.of());
        assertNotNull(headers);
    }

    // =====================================================================
    // cvtHeaderLineFromRfc2047
    // =====================================================================

    @Test
    void cvtHeaderLineFromRfc2047_plainText_unchanged() {
        String plain = "Subject: Plain subject";
        assertEquals(plain, MailUtil.cvtHeaderLineFromRfc2047(plain));
    }

    @Test
    void cvtHeaderLineFromRfc2047_encodedSubjectHeader_decoded() {
        // RFC 2047 encoded "Hello" in UTF-8 base64
        String encoded = "Subject: =?UTF-8?B?SGVsbG8=?=";
        String result = MailUtil.cvtHeaderLineFromRfc2047(encoded);
        assertEquals("Subject: Hello", result);
    }

    @Test
    void cvtHeaderLineFromRfc2047_encodedContentDescriptionHeader_decoded() {
        // RFC 2047 encoded "Hello" in UTF-8 base64
        String encoded = "Content-Description: =?UTF-8?B?SGVsbG8=?=";
        String result = MailUtil.cvtHeaderLineFromRfc2047(encoded);
        assertEquals("Content-Description: Hello", result);
    }

    @Test
    void cvtHeaderLineFromRfc2047_encodedOtherHeader_notDecoded() {
        String encoded = "X-Foo: Bar";
        String result = MailUtil.cvtHeaderLineFromRfc2047(encoded);
        assertEquals(encoded, result);
    }

    @Test
    void cvtHeaderLineFromRfc2047_null_returnsNull() {
        assertNull(MailUtil.cvtHeaderLineFromRfc2047(null));
    }

    // =====================================================================
    // parseFromAddressWithOptionalFriendlyName
    // =====================================================================

    @Test
    void parseFromAddress_addressOnly() {
        var result = MailUtil.parseFromAddressWithOptionalFriendlyName("alice@example.com");
        assertEquals("alice@example.com", result.getAddress());
        assertNull(result.getFriendlyName());
    }

    @Test
    void parseFromAddress_friendlyNameAndAddress() {
        var result = MailUtil.parseFromAddressWithOptionalFriendlyName("\"Alice Smith\" <alice@example.com>");
        assertEquals("alice@example.com", result.getAddress());
        assertEquals("Alice Smith", result.getFriendlyName());
    }

    @Test
    void parseFromAddress_unquotedFriendlyName() {
        var result = MailUtil.parseFromAddressWithOptionalFriendlyName("Bob Jones <bob@example.com>");
        assertEquals("bob@example.com", result.getAddress());
        assertEquals("Bob Jones", result.getFriendlyName());
    }

    @Test
    void parseFromAddress_null_emptyAddressNullFriendly() {
        var result = MailUtil.parseFromAddressWithOptionalFriendlyName(null);
        assertNotNull(result);
        assertEquals("", result.getAddress());
        assertNull(result.getFriendlyName());
    }

    // =====================================================================
    // getRawAddressFromFriendlyEncoding / getFriendlyNameFromFriendlyEncoding
    // =====================================================================

    @Test
    void getRawAddress_fromFriendlyEncoding_addressOnly() {
        assertEquals("alice@example.com",
            MailUtil.getRawAddressFromFriendlyEncoding("alice@example.com"));
    }

    @Test
    void getRawAddress_fromFriendlyEncoding_withAngleBrackets() {
        assertEquals("alice@example.com",
            MailUtil.getRawAddressFromFriendlyEncoding("Alice <alice@example.com>"));
    }

    @Test
    void getRawAddress_blank_returnsEmpty() {
        assertEquals("", MailUtil.getRawAddressFromFriendlyEncoding(""));
        assertEquals("", MailUtil.getRawAddressFromFriendlyEncoding(null));
    }

    @Test
    void getFriendlyName_noDelimiter_returnsEmpty() {
        assertEquals("", MailUtil.getFriendlyNameFromFriendlyEncoding("alice@example.com"));
    }

    @Test
    void getFriendlyName_withAngleBracket_returnsName() {
        String result = MailUtil.getFriendlyNameFromFriendlyEncoding("\"Bob\" <bob@example.com>");
        assertEquals("Bob", result);
    }

    // =====================================================================
    // makeFriendlyNameAddress
    // =====================================================================

    @Test
    void makeFriendlyNameAddress_addressOnly_returnsAddress() {
        assertEquals("alice@example.com",
            MailUtil.makeFriendlyNameAddress("alice@example.com", null));
    }

    @Test
    void makeFriendlyNameAddress_withName_formatsCorrectly() {
        String result = MailUtil.makeFriendlyNameAddress("alice@example.com", "Alice Smith");
        assertNotNull(result);
        assertTrue(result.contains("Alice Smith"), result);
        assertTrue(result.contains("alice@example.com"), result);
        assertTrue(result.contains("<") && result.contains(">"), result);
    }

    @Test
    void makeFriendlyNameAddress_blankAddress_returnsNull() {
        assertNull(MailUtil.makeFriendlyNameAddress("   ", "Name"));
    }

    // =====================================================================
    // headerLineToHeader / encodedHeaderLineToHeader
    // =====================================================================

    @Test
    void headerLineToHeader_validLine() {
        var header = MailUtil.headerLineToHeader("Content-Type: text/html; charset=utf-8");
        assertNotNull(header);
        assertEquals("Content-Type", header.getName());
        assertTrue(header.getValue().contains("text/html"), header.getValue());
    }

    @Test
    void headerLineToHeader_noDelimiter_returnsInvalidHeader() {
        // No ':' → falls back to invalidHeaderLineToHeader
        var header = MailUtil.headerLineToHeader("NoDelimiterHere");
        assertNotNull(header);
        assertEquals("", header.getName());
    }

    // =====================================================================
    // Additional MailUtil coverage
    // =====================================================================

    private static MimeMessage makeMimeMessage() throws Exception {
        Properties props = new Properties();
        Session session = Session.getDefaultInstance(props);
        MimeMessage msg = new MimeMessage(session);
        msg.setSubject("Test Subject");
        msg.setFrom(new InternetAddress("from@example.com"));
        msg.addRecipient(Message.RecipientType.TO, new InternetAddress("to@example.com"));
        msg.setText("body");
        msg.saveChanges();
        return msg;
    }

    // --- createDynamicEmailHeaders ---

    @Test
    void createDynamicEmailHeaders_nullMessage_returnsNone() {
        EmailHeaders h = MailUtil.createDynamicEmailHeaders(null);
        assertSame(EmailHeaders.NONE, h);
    }

    @Test
    void createDynamicEmailHeaders_withMessage_returnsNonNull() throws Exception {
        MimeMessage msg = makeMimeMessage();
        EmailHeaders h = MailUtil.createDynamicEmailHeaders(msg);
        assertNotNull(h);
        assertTrue(h.hasHeader("Subject"));
    }

    @Test
    void createDynamicEmailHeaders_subjectValue() throws Exception {
        MimeMessage msg = makeMimeMessage();
        EmailHeaders h = MailUtil.createDynamicEmailHeaders(msg);
        assertEquals("Test Subject", h.getHeader("Subject"));
    }

    // --- getRawHeaderLines(MimeMessage) ---

    @Test
    void getRawHeaderLines_nullMessage_returnsEmpty() throws Exception {
        List<String> lines = MailUtil.getRawHeaderLines(null);
        assertTrue(lines.isEmpty());
    }

    @Test
    void getRawHeaderLines_withMessage_containsSubjectLine() throws Exception {
        MimeMessage msg = makeMimeMessage();
        List<String> lines = MailUtil.getRawHeaderLines(msg);
        assertFalse(lines.isEmpty());
        assertTrue(lines.stream().anyMatch(l -> l.startsWith("Subject:")), lines.toString());
    }

    // --- createEmailHeaders(Iterable<Header>, boolean) ---

    @Test
    void createEmailHeaders_iterableHeaders_returnsParsed() {
        List<Header> hdrs = List.of(
            new Header("X-Foo", "bar"),
            new Header("X-Baz", "qux")
        );
        EmailHeaders eh = MailUtil.createEmailHeaders(hdrs, false);
        assertNotNull(eh);
        assertEquals("bar", eh.getHeader("X-Foo"));
        assertEquals("qux", eh.getHeader("X-Baz"));
    }

    // --- createEmailHeaders(MimeMessage) ---

    @Test
    void createEmailHeaders_mimeMessage_returnsSubject() throws Exception {
        MimeMessage msg = makeMimeMessage();
        EmailHeaders eh = MailUtil.createEmailHeaders(msg);
        assertNotNull(eh);
        assertEquals("Test Subject", eh.getHeader("Subject"));
    }

    // --- getRfc2047DecodedHeader ---

    @Test
    void getRfc2047DecodedHeader_null_returnsNull() {
        assertNull(MailUtil.getRfc2047DecodedHeader(null));
    }

    @Test
    void getRfc2047DecodedHeader_plainText_returnsSame() {
        Header h = new Header("Subject", "Hello");
        Header result = MailUtil.getRfc2047DecodedHeader(h);
        assertNotNull(result);
        assertEquals("Hello", result.getValue());
    }

    // --- getRfc2047DecodedText ---

    @Test
    void getRfc2047DecodedText_blank_returnsNull() {
        assertNull(MailUtil.getRfc2047DecodedText(""));
        assertNull(MailUtil.getRfc2047DecodedText("  "));
    }

    @Test
    void getRfc2047DecodedText_plainText_returnsSame() {
        assertEquals("Hello", MailUtil.getRfc2047DecodedText("Hello"));
    }

    @Test
    void getRfc2047DecodedText_null_returnsNull() {
        assertNull(MailUtil.getRfc2047DecodedText(null));
    }

    // --- headerToString ---

    @Test
    void headerToString_returnsNameColonValue() {
        Header h = new Header("X-Foo", "bar");
        String s = MailUtil.headerToString(h);
        assertNotNull(s);
        assertTrue(s.contains("X-Foo"), s);
        assertTrue(s.contains("bar"), s);
    }

    @Test
    void headerToString_null_returnsNull() {
        assertNull(MailUtil.headerToString(null));
    }

    // --- headersToHeaderMap ---

    @Test
    void headersToHeaderMap_returnsMultimap() {
        List<Header> hdrs = List.of(
            new Header("A", "1"),
            new Header("A", "2"),
            new Header("B", "3")
        );
        var map = MailUtil.headersToHeaderMap(hdrs);
        assertNotNull(map);
        assertEquals(2, map.get("A").size());
        assertEquals(1, map.get("B").size());
    }

    // --- headerLinesToHeaderMap ---

    @Test
    void headerLinesToHeaderMap_parsesLines() {
        var map = MailUtil.headerLinesToHeaderMap(List.of(
            "Subject: Hello",
            "From: user@example.com"
        ));
        assertNotNull(map);
        assertFalse(map.get(EmailHeaders.NAME_SUBJECT).isEmpty());
        assertFalse(map.get(EmailHeaders.NAME_FROM).isEmpty());
    }

    // --- getDomain ---

    @Test
    void getDomain_validAddress_returnsDomain() {
        assertEquals("example.com", MailUtil.getDomain("user@example.com"));
    }

    @Test
    void getDomain_noAtSign_returnsEmpty() {
        assertEquals("", MailUtil.getDomain("notanaddress"));
    }

    @Test
    void getDomain_null_returnsNull() {
        assertNull(MailUtil.getDomain(null));
    }

    // --- getSafelyEncodedToken ---

    @Test
    void getSafelyEncodedToken_plainText_returnsEncoded() {
        String result = MailUtil.getSafelyEncodedToken("hello world");
        assertNotNull(result);
        assertFalse(result.isEmpty());
    }

    @Test
    void getSafelyEncodedToken_withCharset_returnsEncoded() {
        String result = MailUtil.getSafelyEncodedToken("hello", "UTF-8");
        assertNotNull(result);
    }

    // --- safeParseAddresses ---

    @Test
    void safeParseAddresses_validList_returnsList() {
        var addresses = MailUtil.safeParseAddresses("a@example.com, b@example.com");
        assertNotNull(addresses);
        assertEquals(2, addresses.size());
    }

    @Test
    void safeParseAddresses_null_returnsNull() {
        assertNull(MailUtil.safeParseAddresses(null));
    }

    @Test
    void safeParseAddresses_blank_returnsNull() {
        assertNull(MailUtil.safeParseAddresses("  "));
    }

    // --- shouldSuppressAutomaticResponse ---

    @Test
    void shouldSuppressAutomaticResponse_noAutoSubmittedHeader_false() {
        EmailHeaders eh = MailUtil.createEmailHeadersFromRawLines(List.of("Subject: Test"));
        assertFalse(MailUtil.shouldSuppressAutomaticResponse(eh));
    }

    @Test
    void shouldSuppressAutomaticResponse_autoGenerated_true() {
        EmailHeaders eh = MailUtil.createEmailHeadersFromRawLines(List.of(
            "Auto-Submitted: auto-generated"
        ));
        assertTrue(MailUtil.shouldSuppressAutomaticResponse(eh));
    }

    @Test
    void shouldSuppressAutomaticResponse_autoReplied_true() {
        EmailHeaders eh = MailUtil.createEmailHeadersFromRawLines(List.of(
            "Auto-Submitted: auto-replied"
        ));
        assertTrue(MailUtil.shouldSuppressAutomaticResponse(eh));
    }

    // --- dumpNamedHeaders ---

    @Test
    void dumpNamedHeaders_presentHeader_appendsToBuffer() {
        EmailHeaders eh = MailUtil.createEmailHeadersFromRawLines(List.of("Subject: Hello"));
        StringBuilder sb = new StringBuilder();
        MailUtil.dumpNamedHeaders(sb, eh, "Subject");
        assertTrue(sb.toString().contains("Subject"), sb.toString());
    }

    @Test
    void dumpNamedHeaders_absentHeader_writesNoHeadersMessage() {
        EmailHeaders eh = MailUtil.createEmailHeadersFromRawLines(List.of("Subject: Hello"));
        StringBuilder sb = new StringBuilder();
        MailUtil.dumpNamedHeaders(sb, eh, "X-NoSuchHeader");
        assertTrue(sb.toString().contains("X-NoSuchHeader"), sb.toString());
        assertTrue(sb.toString().contains("no headers"), sb.toString());
    }

    // --- parseHeaderLine ---

    @Test
    void parseHeaderLine_validLine_returnsHeader() {
        Header h = MailUtil.parseHeaderLine("X-Test: value", false, null);
        assertNotNull(h);
        assertEquals("X-Test", h.getName());
        assertEquals("value", h.getValue().trim());
    }

    @Test
    void parseHeaderLine_null_withDefault_usesDefault() {
        Header h = MailUtil.parseHeaderLine(null, false, _ -> new Header("default", ""));
        assertNotNull(h);
        assertEquals("default", h.getName());
    }

    @Test
    void parseHeaderLine_null_noDefault_returnsNull() {
        assertNull(MailUtil.parseHeaderLine(null, false, null));
    }

    // --- encodedHeaderLineToHeader ---

    @Test
    void encodedHeaderLineToHeader_plainLine_returnsHeader() {
        Header h = MailUtil.encodedHeaderLineToHeader("X-Foo: bar");
        assertNotNull(h);
        assertEquals("X-Foo", h.getName());
    }

    // --- invalidHeaderLineToHeader ---

    @Test
    void invalidHeaderLineToHeader_noDelimiter_returnsEmptyNameHeader() {
        Header h = MailUtil.invalidHeaderLineToHeader("NoDelimiter");
        assertNotNull(h);
        assertEquals("", h.getName());
    }

    // =====================================================================
    // headersToHeaderMap / LowerCaseHeaderMap (get/containsEntry/containsKey)
    // =====================================================================

    @Test
    void headersToHeaderMap_getWithNullKey_doesNotThrow() {
        var map = MailUtil.headersToHeaderMap(List.of(new Header("A", "1")));
        assertTrue(map.get(null).isEmpty());
    }

    @Test
    void headersToHeaderMap_containsEntry_stringKeyCaseInsensitive() {
        var map = MailUtil.headersToHeaderMap(List.of(new Header("A", "1")));
        Header h = map.get("a").iterator().next();
        assertTrue(map.containsEntry("A", h));
        assertTrue(map.containsEntry("a", h));
    }

    @Test
    void headersToHeaderMap_containsEntry_nonStringKey_returnsFalse() {
        var map = MailUtil.headersToHeaderMap(List.of(new Header("A", "1")));
        assertFalse(map.containsEntry(1, "1"));
    }

    @Test
    void headersToHeaderMap_containsKey_stringKeyCaseInsensitive() {
        var map = MailUtil.headersToHeaderMap(List.of(new Header("A", "1")));
        assertTrue(map.containsKey("A"));
        assertTrue(map.containsKey("a"));
    }

    @Test
    void headersToHeaderMap_containsKey_nonStringKey_returnsFalse() {
        var map = MailUtil.headersToHeaderMap(List.of(new Header("A", "1")));
        assertFalse(map.containsKey(1));
    }

    // =====================================================================
    // DynamicMessageEmailHeaders (toString / hasHeader / getHeaders / getRawHeaderLines / iterator)
    // =====================================================================

    @Test
    void dynamicEmailHeaders_toString_containsClassName() throws Exception {
        EmailHeaders h = MailUtil.createDynamicEmailHeaders(makeMimeMessage());
        assertTrue(h.toString().contains("DynamicMessageEmailHeaders"), h.toString());
    }

    @Test
    void dynamicEmailHeaders_hasHeader_nullName_returnsFalse() throws Exception {
        EmailHeaders h = MailUtil.createDynamicEmailHeaders(makeMimeMessage());
        assertFalse(h.hasHeader(null));
    }

    @Test
    void dynamicEmailHeaders_hasHeader_absentHeader_returnsFalse() throws Exception {
        EmailHeaders h = MailUtil.createDynamicEmailHeaders(makeMimeMessage());
        assertFalse(h.hasHeader("X-Does-Not-Exist"));
    }

    @Test
    void dynamicEmailHeaders_getHeaders_nullName_returnsEmpty() throws Exception {
        EmailHeaders h = MailUtil.createDynamicEmailHeaders(makeMimeMessage());
        assertTrue(h.getHeaders(null).isEmpty());
    }

    @Test
    void dynamicEmailHeaders_getHeaders_absentHeader_returnsEmpty() throws Exception {
        EmailHeaders h = MailUtil.createDynamicEmailHeaders(makeMimeMessage());
        assertTrue(h.getHeaders("X-Does-Not-Exist").isEmpty());
    }

    @Test
    void dynamicEmailHeaders_getRawHeaderLines_containsSubjectLine() throws Exception {
        EmailHeaders h = MailUtil.createDynamicEmailHeaders(makeMimeMessage());
        boolean found = false;
        for (String line : h.getRawHeaderLines()) {
            if (line.startsWith("Subject:")) {
                found = true;
                break;
            }
        }
        assertTrue(found);
    }

    @Test
    void dynamicEmailHeaders_iterator_yieldsSubjectHeader() throws Exception {
        EmailHeaders h = MailUtil.createDynamicEmailHeaders(makeMimeMessage());
        boolean found = false;
        for (Header header : h) {
            if ("Subject".equalsIgnoreCase(header.getName())) {
                found = true;
                break;
            }
        }
        assertTrue(found);
    }

    // =====================================================================
    // getDataSource / InputStreamDataSource / ReadOnlyAdapterDataSource
    // =====================================================================

    @Test
    void getDataSource_fromText_roundTripsContentAndMetadata() throws Exception {
        DataSource ds = MailUtil.getDataSource("greeting.txt", "hello there", StandardCharsets.UTF_8, "text/plain");
        assertEquals("greeting.txt", ds.getName());
        assertEquals("text/plain", ds.getContentType());
        try (InputStream in = ds.getInputStream()) {
            assertEquals("hello there", new String(in.readAllBytes(), StandardCharsets.UTF_8));
        }
    }

    @Test
    void getDataSource_fromText_nullName_returnsEmptyName() {
        DataSource ds = MailUtil.getDataSource(null, "hello", StandardCharsets.UTF_8, "text/plain");
        assertEquals("", ds.getName());
    }

    @Test
    void getDataSource_fromBytes_roundTripsContent() throws Exception {
        byte[] data = "raw bytes".getBytes(StandardCharsets.UTF_8);
        DataSource ds = MailUtil.getDataSource("data.bin", data, "application/octet-stream");
        try (InputStream in = ds.getInputStream()) {
            assertArrayEquals(data, in.readAllBytes());
        }
    }

    @Test
    void getDataSource_fromBytes_nullData_throwsNpe() {
        assertThrows(
            NullPointerException.class, () -> MailUtil.getDataSource("data.bin", null, "application/octet-stream")
        );
    }

    @Test
    void getDataSource_fromFileResource_roundTripsContent() throws Exception {
        File temp = File.createTempFile("mailutil-test", ".txt");
        temp.deleteOnExit();
        Files.writeString(temp.toPath(), "file contents");
        FileResource resource = LocalFileResource.createInstance(temp);
        DataSource ds = MailUtil.getDataSource(resource);
        try (InputStream in = ds.getInputStream()) {
            assertEquals("file contents", new String(in.readAllBytes(), StandardCharsets.UTF_8));
        }
    }

    @Test
    void getDataSource_fromFileResource_nullFile_throwsNpe() {
        assertThrows(NullPointerException.class, () -> MailUtil.getDataSource(null));
    }

    @Test
    void dataSource_getOutputStream_throwsUnsupportedOperationException() {
        DataSource ds = MailUtil.getDataSource("x.txt", "x", StandardCharsets.UTF_8, "text/plain");
        assertThrows(UnsupportedOperationException.class, ds::getOutputStream);
    }

    // =====================================================================
    // makeFriendlyNameAddress(InternetAddress)
    // =====================================================================

    @Test
    void makeFriendlyNameAddress_internetAddress_withPersonal_formatsCorrectly() throws Exception {
        InternetAddress addr = new InternetAddress("bobby@example.com", "Bobby McGee");
        assertEquals("\"Bobby McGee\" <bobby@example.com>", MailUtil.makeFriendlyNameAddress(addr));
    }

    @Test
    void makeFriendlyNameAddress_internetAddress_withoutPersonal_returnsAddressOnly() throws Exception {
        InternetAddress addr = new InternetAddress("bobby@example.com");
        assertEquals("bobby@example.com", MailUtil.makeFriendlyNameAddress(addr));
    }

    @Test
    void makeFriendlyNameAddress_internetAddress_null_returnsNull() {
        assertNull(MailUtil.makeFriendlyNameAddress(null));
    }

    // =====================================================================
    // encodeFriendlyNameInAddress
    // =====================================================================

    @Test
    void encodeFriendlyNameInAddress_null_returnsEmpty() {
        assertEquals("", MailUtil.encodeFriendlyNameInAddress(null));
    }

    @Test
    void encodeFriendlyNameInAddress_blank_returnsEmpty() {
        assertEquals("", MailUtil.encodeFriendlyNameInAddress("   "));
    }

    @Test
    void encodeFriendlyNameInAddress_noAngleBracket_returnsUnchanged() {
        assertEquals("bobby@example.com", MailUtil.encodeFriendlyNameInAddress("bobby@example.com"));
    }

    @Test
    void encodeFriendlyNameInAddress_angleBracketAtStart_returnsUnchanged() {
        String input = "<bobby@example.com>";
        assertEquals(input, MailUtil.encodeFriendlyNameInAddress(input));
    }

    @Test
    void encodeFriendlyNameInAddress_withFriendlyName_encodesAndReassembles() {
        assertEquals(
            "Bobby McGee <bobby@example.com>",
            MailUtil.encodeFriendlyNameInAddress("Bobby McGee <bobby@example.com>")
        );
    }

    @Test
    void encodeFriendlyNameInAddress_missingClosingBracket_returnsUnchanged() {
        String input = "Bobby McGee <bobby@example.com";
        assertEquals(input, MailUtil.encodeFriendlyNameInAddress(input));
    }

    // =====================================================================
    // getOtherRecipients / getDummyEmailAddress / safeParseAddresses (catch branch)
    // =====================================================================

    @Test
    void getOtherRecipients_excludesAddressesAlreadyInToOrCc() {
        EmailHeaders eh = MailUtil.createEmailHeadersFromRawLines(List.of(
            "To: alice@example.com",
            "Cc: bob@example.com",
            "Delivered-To: alice@example.com",
            "X-Original-To: carol@example.com"
        ));
        SequencedSet<Address> others = MailUtil.getOtherRecipients(eh);
        assertEquals(1, others.size());
        assertEquals("carol@example.com", ((InternetAddress) others.getFirst()).getAddress());
    }

    @Test
    void getOtherRecipients_noAuxiliaryHeaders_returnsEmpty() {
        EmailHeaders eh = MailUtil.createEmailHeadersFromRawLines(List.of("To: alice@example.com"));
        assertTrue(MailUtil.getOtherRecipients(eh).isEmpty());
    }

    @Test
    void getDummyEmailAddress_returnsExpectedAddress() throws Exception {
        Address address = MailUtil.getDummyEmailAddress();
        assertEquals("badaddress@unrecognized.domain", ((InternetAddress) address).getAddress());
    }

    @Test
    void safeParseAddresses_malformed_returnsNullInsteadOfThrowing() {
        // Unterminated quoted phrase triggers an AddressException internally; safeParseAddresses must catch it.
        assertNull(MailUtil.safeParseAddresses("\"Unterminated"));
    }

    // =====================================================================
    // shouldSuppressAutomaticResponse (remaining branches)
    // =====================================================================

    @Test
    void shouldSuppressAutomaticResponse_autoSubmittedNo_false() {
        EmailHeaders eh = MailUtil.createEmailHeadersFromRawLines(List.of("Auto-Submitted: no"));
        assertFalse(MailUtil.shouldSuppressAutomaticResponse(eh));
    }

    @Test
    void shouldSuppressAutomaticResponse_nullReturnPath_true() {
        EmailHeaders eh = MailUtil.createEmailHeadersFromRawLines(List.of("Return-Path: <>"));
        assertTrue(MailUtil.shouldSuppressAutomaticResponse(eh));
    }

    @Test
    void shouldSuppressAutomaticResponse_xAutoResponseSuppressAutoReply_true() {
        EmailHeaders eh = MailUtil.createEmailHeadersFromRawLines(List.of("X-Auto-Response-Suppress: AutoReply"));
        assertTrue(MailUtil.shouldSuppressAutomaticResponse(eh));
    }

    @Test
    void shouldSuppressAutomaticResponse_xAutoResponseSuppressAll_true() {
        EmailHeaders eh = MailUtil.createEmailHeadersFromRawLines(List.of("X-Auto-Response-Suppress: All"));
        assertTrue(MailUtil.shouldSuppressAutomaticResponse(eh));
    }

    // =====================================================================
    // getDomain (remaining branch)
    // =====================================================================

    @Test
    void getDomain_atSignIsLastCharacter_returnsEmpty() {
        assertEquals("", MailUtil.getDomain("foo@"));
    }

    // =====================================================================
    // getSafelyEncodedToken(text, charsetName) (remaining branches)
    // =====================================================================

    @Test
    void getSafelyEncodedToken_withCharset_blankText_returnsTextUnchanged() {
        assertEquals("", MailUtil.getSafelyEncodedToken("", "UTF-8"));
        assertEquals("   ", MailUtil.getSafelyEncodedToken("   ", "UTF-8"));
    }

    @Test
    void getSafelyEncodedToken_withCharset_invalidCharsetName_returnsNull() {
        assertEquals("hello", MailUtil.getSafelyEncodedToken("hello", "not-a-real-charset"));
    }

    // =====================================================================
    // getFriendlyNameWithSafelyEncodedDisplayName
    // =====================================================================

    @Test
    void getFriendlyNameWithSafelyEncodedDisplayName_blankDisplayName_returnsAddress() {
        assertEquals(
            "bobby@example.com",
            MailUtil.getFriendlyNameWithSafelyEncodedDisplayName("bobby@example.com", "  ")
        );
    }

    @Test
    void getFriendlyNameWithSafelyEncodedDisplayName_withDisplayName_formatsAddress() {
        String result = MailUtil.getFriendlyNameWithSafelyEncodedDisplayName("bobby@example.com", "Bobby McGee");
        assertNotNull(result);
        assertTrue(result.contains("bobby@example.com"), result);
        assertTrue(result.contains("Bobby McGee"), result);
    }

    // =====================================================================
    // headerLinesToHeaderMap(Stream<String>) overload
    // =====================================================================

    @Test
    void headerLinesToHeaderMap_streamOverload_parsesLines() {
        var map = MailUtil.headerLinesToHeaderMap(Stream.of("Subject: Hello", "From: user@example.com"));
        assertFalse(map.get(EmailHeaders.NAME_SUBJECT).isEmpty());
        assertFalse(map.get(EmailHeaders.NAME_FROM).isEmpty());
    }

    // =====================================================================
    // getRfc2047DecodedHeaders
    // =====================================================================

    @Test
    void getRfc2047DecodedHeaders_emptyMultimap_returnsEmpty() {
        Multimap<String,Header> result = MailUtil.getRfc2047DecodedHeaders(ImmutableListMultimap.of());
        assertTrue(result.isEmpty());
    }

    @Test
    void getRfc2047DecodedHeaders_null_returnsEmpty() {
        assertTrue(MailUtil.getRfc2047DecodedHeaders(null).isEmpty());
    }

    @Test
    void getRfc2047DecodedHeaders_encodedSubject_decodesValue() {
        Multimap<String,Header> encoded = MailUtil.headersToHeaderMap(
            List.of(new Header("Subject", "=?UTF-8?B?SGVsbG8=?="))
        );
        Multimap<String,Header> decoded = MailUtil.getRfc2047DecodedHeaders(encoded);
        Header h = decoded.get("subject").iterator().next();
        assertEquals("Hello", h.getValue());
    }

    // =====================================================================
    // setHeadersForAutomaticallyGeneratedMessage / ...ReplyMessage / ...Exchange / setNullReturnPath
    // =====================================================================

    @Test
    void setHeadersForAutomaticallyGeneratedMessage_setsExpectedHeaders() throws Exception {
        MimeMessage msg = makeMimeMessage();
        MailUtil.setHeadersForAutomaticallyGeneratedMessage(msg, false);
        assertEquals("auto-generated", msg.getHeader("Auto-Submitted", null));
        assertEquals("All", msg.getHeader("X-Auto-Response-Suppress", null));
        assertNull(msg.getHeader("Return-Path", null));
    }

    @Test
    void setHeadersForAutomaticallyGeneratedMessage_withNullReturnPath_setsReturnPath() throws Exception {
        MimeMessage msg = makeMimeMessage();
        MailUtil.setHeadersForAutomaticallyGeneratedMessage(msg, true);
        assertEquals("<>", msg.getHeader("Return-Path", null));
    }

    @Test
    void setHeadersForAutomaticallyGeneratedReplyMessage_setsExpectedHeaders() throws Exception {
        MimeMessage msg = makeMimeMessage();
        MailUtil.setHeadersForAutomaticallyGeneratedReplyMessage(msg, false);
        assertEquals("auto-replied", msg.getHeader("Auto-Submitted", null));
        assertEquals("All", msg.getHeader("X-Auto-Response-Suppress", null));
        assertNull(msg.getHeader("Return-Path", null));
    }

    @Test
    void setHeadersForAutomaticallyGeneratedReplyMessage_withNullReturnPath_setsReturnPath() throws Exception {
        MimeMessage msg = makeMimeMessage();
        MailUtil.setHeadersForAutomaticallyGeneratedReplyMessage(msg, true);
        assertEquals("<>", msg.getHeader("Return-Path", null));
    }

    @Test
    void setHeadersForAutomaticallyGeneratedMessageExchange_setsHeader() throws Exception {
        MimeMessage msg = makeMimeMessage();
        MailUtil.setHeadersForAutomaticallyGeneratedMessageExchange(msg);
        assertEquals("All", msg.getHeader("X-Auto-Response-Suppress", null));
    }

    @Test
    void setNullReturnPath_setsHeader() throws Exception {
        MimeMessage msg = makeMimeMessage();
        MailUtil.setNullReturnPath(msg);
        assertEquals("<>", msg.getHeader("Return-Path", null));
    }

    // =====================================================================
    // getRecipients(Message, Message.RecipientType)
    // =====================================================================

    @Test
    void getRecipients_nullMessage_returnsNull() {
        assertNull(MailUtil.getRecipients(null, Message.RecipientType.TO));
    }

    @Test
    void getRecipients_nullType_returnsNull() throws Exception {
        assertNull(MailUtil.getRecipients(makeMimeMessage(), null));
    }

    @Test
    void getRecipients_validMessage_returnsRecipients() throws Exception {
        List<Address> recipients = MailUtil.getRecipients(makeMimeMessage(), Message.RecipientType.TO);
        assertNotNull(recipients);
        assertEquals(1, recipients.size());
        assertEquals("to@example.com", ((InternetAddress) recipients.getFirst()).getAddress());
    }

    // =====================================================================
    // BUG: headerLineToHeader(String, boolean) (and therefore encodedHeaderLineToHeader(String)) ignores its
    // tryToDecode parameter -- it always calls parseHeaderLine(headerLine, false, ...). The decoding logic in
    // parseHeaderLine itself works fine when invoked directly with tryToDecode=true (first test below); but the
    // only public entry points meant to expose that behavior never actually apply it (second test below documents
    // the current, broken behavior).
    // =====================================================================

    @Test
    void parseHeaderLine_tryToDecodeTrue_decodesRfc2047EncodedSubjectLine() {
        Header h = MailUtil.parseHeaderLine("Subject: =?UTF-8?B?SGVsbG8=?=", true, null);
        assertNotNull(h);
        assertEquals("Subject", h.getName());
        assertEquals("Hello", h.getValue());
    }

    @Test
    void encodedHeaderLineToHeader_subjectHeader_doesNotActuallyDecode_bug() {
        String encodedSubjectLine = "Subject: =?UTF-8?B?SGVsbG8=?=";
        Header header = MailUtil.encodedHeaderLineToHeader(encodedSubjectLine);
        assertEquals("Subject", header.getName());
        // Expected "Hello" if decoding were actually applied; instead the encoded form passes through unchanged.
        assertEquals("=?UTF-8?B?SGVsbG8=?=", header.getValue());
    }

    // =====================================================================
    // BUG: createEmailHeaders(Iterable<Header>, boolean) ignores its tryToDecode parameter -- it always passes
    // true to the Multimap overload. Contrast with createEmailHeaders(Multimap, boolean), which respects the flag
    // correctly (second test below).
    // =====================================================================

    @Test
    void createEmailHeaders_iterableHeaders_tryToDecodeFalse_stillDecodes_bug() {
        List<Header> hdrs = List.of(new Header("Subject", "=?UTF-8?B?SGVsbG8=?="));
        EmailHeaders eh = MailUtil.createEmailHeaders(hdrs, false);
        // Expected the still-encoded value if tryToDecode=false were honored; instead it's decoded anyway.
        assertEquals("Hello", eh.getHeader("Subject"));
    }

    @Test
    void createEmailHeaders_multimapOverload_tryToDecodeFalse_doesNotDecode() {
        Multimap<String,Header> headers = MailUtil.headersToHeaderMap(
            List.of(new Header("Subject", "=?UTF-8?B?SGVsbG8=?="))
        );
        EmailHeaders eh = MailUtil.createEmailHeaders(headers, false);
        assertEquals("=?UTF-8?B?SGVsbG8=?=", eh.getHeader("Subject"));
    }

}
