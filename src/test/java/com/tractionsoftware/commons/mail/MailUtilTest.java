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
import org.apache.commons.lang3.StringUtils;

import jakarta.mail.Header;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

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

}
