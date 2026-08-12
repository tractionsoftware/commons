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

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;

import static com.tractionsoftware.commons.net.HostAddressUtil.IPAddressOutgoingRequestFilterMode.*;
import static org.junit.jupiter.api.Assertions.*;

class HostAddressUtilTest {

    // ---------------------------------------------------------------------------
    // parseIPAddress
    // ---------------------------------------------------------------------------

    @Test
    void parseIPAddress_validIPv4_wasAddressTrue() {
        var result = HostAddressUtil.parseIPAddress("192.168.1.1");
        assertTrue(result.wasAddress());
        assertNotNull(result.getParsed());
    }

    @Test
    void parseIPAddress_loopback_wasAddressTrue() {
        var result = HostAddressUtil.parseIPAddress("127.0.0.1");
        assertTrue(result.wasAddress());
    }

    @Test
    void parseIPAddress_validIPv6_wasAddressTrue() {
        var result = HostAddressUtil.parseIPAddress("::1");
        assertTrue(result.wasAddress());
    }

    @Test
    void parseIPAddress_hostname_wasAddressFalse() {
        var result = HostAddressUtil.parseIPAddress("example.com");
        assertFalse(result.wasAddress());
        assertNull(result.getParsed());
    }

    @Test
    void parseIPAddress_blank_wasAddressFalse() {
        assertFalse(HostAddressUtil.parseIPAddress("").wasAddress());
    }

    @Test
    void parseIPAddress_null_wasAddressFalse() {
        assertFalse(HostAddressUtil.parseIPAddress(null).wasAddress());
    }

    @Test
    void parseIPAddress_garbage_wasAddressFalse() {
        assertFalse(HostAddressUtil.parseIPAddress("not.an.ip!").wasAddress());
    }

    // ---------------------------------------------------------------------------
    // InetAddressWrapper — isLocal / isPrivate / isMulticast
    // ---------------------------------------------------------------------------

    @Test
    void inetAddressWrapper_loopback_isLocal() throws UnknownHostException {
        InetAddress loopback = InetAddress.getByName("127.0.0.1");
        var wrapper = new HostAddressUtil.InetAddressWrapper(loopback);
        assertTrue(wrapper.isLocal());
        assertFalse(wrapper.isPrivate());
        assertFalse(wrapper.isMulticast());
    }

    @Test
    void inetAddressWrapper_siteLocal_isLocal() throws UnknownHostException {
        InetAddress siteLocal = InetAddress.getByName("192.168.1.5");
        var wrapper = new HostAddressUtil.InetAddressWrapper(siteLocal);
        assertTrue(wrapper.isLocal());
    }

    @Test
    void inetAddressWrapper_anyLocal_isLocal() throws UnknownHostException {
        // isLocal() is isLoopbackAddress() || isAnyLocalAddress() || isSiteLocalAddress();
        // 0.0.0.0 exercises the isAnyLocalAddress() ("wildcard") disjunct.
        InetAddress anyLocal = InetAddress.getByName("0.0.0.0");
        var wrapper = new HostAddressUtil.InetAddressWrapper(anyLocal);
        assertTrue(wrapper.isLocal());
    }

    @Test
    void inetAddressWrapper_linkLocal_isPrivate() throws UnknownHostException {
        InetAddress linkLocal = InetAddress.getByName("169.254.1.1");
        var wrapper = new HostAddressUtil.InetAddressWrapper(linkLocal);
        assertTrue(wrapper.isPrivate());
        assertFalse(wrapper.isLocal());
    }

    @Test
    void inetAddressWrapper_multicast_isMulticast() throws UnknownHostException {
        InetAddress multicast = InetAddress.getByName("224.0.0.1");
        var wrapper = new HostAddressUtil.InetAddressWrapper(multicast);
        assertTrue(wrapper.isMulticast());
    }

    @Test
    void inetAddressWrapper_nullAddress_throws() {
        assertThrows(NullPointerException.class, () -> new HostAddressUtil.InetAddressWrapper(null));
    }

    @Test
    void inetAddressWrapper_get_returnsWrapped() throws UnknownHostException {
        InetAddress addr = InetAddress.getByName("8.8.8.8");
        var wrapper = new HostAddressUtil.InetAddressWrapper(addr);
        assertSame(addr, wrapper.get());
    }

    // ---------------------------------------------------------------------------
    // InetAddressWrapper.checkAllowedQ
    // ---------------------------------------------------------------------------

    @Test
    void checkAllowedQ_none_alwaysFalse() throws UnknownHostException {
        var wrapper = new HostAddressUtil.InetAddressWrapper(InetAddress.getByName("8.8.8.8"));
        assertFalse(wrapper.checkAllowedQ(NONE));
    }

    @Test
    void checkAllowedQ_any_trueForExternal() throws UnknownHostException {
        var wrapper = new HostAddressUtil.InetAddressWrapper(InetAddress.getByName("8.8.8.8"));
        assertTrue(wrapper.checkAllowedQ(ANY));
    }

    @Test
    void checkAllowedQ_any_trueForLoopback() throws UnknownHostException {
        var wrapper = new HostAddressUtil.InetAddressWrapper(InetAddress.getByName("127.0.0.1"));
        assertTrue(wrapper.checkAllowedQ(ANY));
    }

    @Test
    void checkAllowedQ_external_falseForLoopback() throws UnknownHostException {
        var wrapper = new HostAddressUtil.InetAddressWrapper(InetAddress.getByName("127.0.0.1"));
        assertFalse(wrapper.checkAllowedQ(EXTERNAL));
    }

    @Test
    void checkAllowedQ_external_trueForPublic() throws UnknownHostException {
        var wrapper = new HostAddressUtil.InetAddressWrapper(InetAddress.getByName("8.8.8.8"));
        assertTrue(wrapper.checkAllowedQ(EXTERNAL));
    }

    @Test
    void checkAllowedQ_internal_trueForLoopback() throws UnknownHostException {
        var wrapper = new HostAddressUtil.InetAddressWrapper(InetAddress.getByName("127.0.0.1"));
        assertTrue(wrapper.checkAllowedQ(INTERNAL));
    }

    @Test
    void checkAllowedQ_internal_falseForPublic() throws UnknownHostException {
        var wrapper = new HostAddressUtil.InetAddressWrapper(InetAddress.getByName("8.8.8.8"));
        assertFalse(wrapper.checkAllowedQ(INTERNAL));
    }

    @Test
    void checkAllowedQ_multicast_alwaysFalse() throws UnknownHostException {
        var wrapper = new HostAddressUtil.InetAddressWrapper(InetAddress.getByName("224.0.0.1"));
        assertFalse(wrapper.checkAllowedQ(ANY));
        assertFalse(wrapper.checkAllowedQ(INTERNAL));
        assertFalse(wrapper.checkAllowedQ(EXTERNAL));
        assertFalse(wrapper.checkAllowedQ(NONE));
    }

    // ---------------------------------------------------------------------------
    // InetAddressWrapper.checkAllowedX
    //
    // checkAllowedX's branch structure is NOT a mirror image of checkAllowedQ's:
    //
    //   - checkAllowedQ checks isMulticast() first, unconditionally, before the mode switch -- so multicast
    //     addresses are always denied by the same check regardless of mode.
    //   - checkAllowedX checks isMulticast() LAST, after the mode switch -- so for EXTERNAL/INTERNAL modes, a
    //     multicast address can trip the mode-specific throw before the multicast check is reached. The two
    //     methods agree on allow/deny outcomes in every case, but checkAllowedX's exception message doesn't
    //     always say "multicast" even when the address is multicast (see the INTERNAL+multicast case).
    // ---------------------------------------------------------------------------

    @Test
    void checkAllowedX_none_throwsIPAddressNotAllowedException() throws UnknownHostException {
        var wrapper = new HostAddressUtil.InetAddressWrapper(InetAddress.getByName("8.8.8.8"));
        IPAddressNotAllowedException ex = assertThrows(
            IPAddressNotAllowedException.class, () -> wrapper.checkAllowedX(NONE)
        );
        assertEquals("All IP address hosts disallowed.", ex.getMessage());
    }

    @Test
    void checkAllowedX_none_multicastAddress_stillThrowsBeforeReachingMulticastCheck() throws UnknownHostException {
        // NONE throws unconditionally at the top of the switch, so the multicast check at the bottom of the method
        // is unreachable for NONE -- the outcome (deny) matches checkAllowedQ(NONE), but for a different reason.
        var wrapper = new HostAddressUtil.InetAddressWrapper(InetAddress.getByName("224.0.0.1"));
        IPAddressNotAllowedException ex = assertThrows(
            IPAddressNotAllowedException.class, () -> wrapper.checkAllowedX(NONE)
        );
        assertEquals("All IP address hosts disallowed.", ex.getMessage());
    }

    @Test
    void checkAllowedX_external_loopback_throwsLocalPrivateMessage() throws UnknownHostException {
        var wrapper = new HostAddressUtil.InetAddressWrapper(InetAddress.getByName("127.0.0.1"));
        IPAddressNotAllowedException ex = assertThrows(
            IPAddressNotAllowedException.class, () -> wrapper.checkAllowedX(EXTERNAL)
        );
        assertEquals("Local/private IP address hosts disallowed.", ex.getMessage());
    }

    @Test
    void checkAllowedX_external_linkLocal_throwsLocalPrivateMessage() throws UnknownHostException {
        var wrapper = new HostAddressUtil.InetAddressWrapper(InetAddress.getByName("169.254.1.1"));
        IPAddressNotAllowedException ex = assertThrows(
            IPAddressNotAllowedException.class, () -> wrapper.checkAllowedX(EXTERNAL)
        );
        assertEquals("Local/private IP address hosts disallowed.", ex.getMessage());
    }

    @Test
    void checkAllowedX_external_public_doesNotThrow() throws UnknownHostException {
        var wrapper = new HostAddressUtil.InetAddressWrapper(InetAddress.getByName("8.8.8.8"));
        assertDoesNotThrow(() -> wrapper.checkAllowedX(EXTERNAL));
    }

    @Test
    void checkAllowedX_external_multicast_throwsMulticastMessage() throws UnknownHostException {
        // 224.0.0.1 is neither local nor private, so the EXTERNAL branch doesn't throw on its own condition; this
        // falls through to the trailing isMulticast() check, which throws with the "Multicast" message.
        var wrapper = new HostAddressUtil.InetAddressWrapper(InetAddress.getByName("224.0.0.1"));
        IPAddressNotAllowedException ex = assertThrows(
            IPAddressNotAllowedException.class, () -> wrapper.checkAllowedX(EXTERNAL)
        );
        assertEquals("Multicast IP address hosts disallowed.", ex.getMessage());
    }

    @Test
    void checkAllowedX_internal_loopback_doesNotThrow() throws UnknownHostException {
        var wrapper = new HostAddressUtil.InetAddressWrapper(InetAddress.getByName("127.0.0.1"));
        assertDoesNotThrow(() -> wrapper.checkAllowedX(INTERNAL));
    }

    @Test
    void checkAllowedX_internal_linkLocal_doesNotThrow() throws UnknownHostException {
        var wrapper = new HostAddressUtil.InetAddressWrapper(InetAddress.getByName("169.254.1.1"));
        assertDoesNotThrow(() -> wrapper.checkAllowedX(INTERNAL));
    }

    @Test
    void checkAllowedX_internal_public_throwsExternalMessage() throws UnknownHostException {
        var wrapper = new HostAddressUtil.InetAddressWrapper(InetAddress.getByName("8.8.8.8"));
        IPAddressNotAllowedException ex = assertThrows(
            IPAddressNotAllowedException.class, () -> wrapper.checkAllowedX(INTERNAL)
        );
        assertEquals("External IP address hosts disallowed.", ex.getMessage());
    }

    @Test
    void checkAllowedX_internal_multicast_throwsExternalMessage_notMulticastMessage() throws UnknownHostException {
        // 224.0.0.1 is not local/private, so for INTERNAL mode "!isLocal() && !isPrivate()" is true and the method
        // throws with the "External" message immediately -- it never reaches the trailing isMulticast() check. The
        // address IS multicast, but the exception message says "External", not "Multicast". The deny outcome
        // matches checkAllowedQ(INTERNAL) on this address, but the message is arguably misleading; flagging rather
        // than fixing.
        var wrapper = new HostAddressUtil.InetAddressWrapper(InetAddress.getByName("224.0.0.1"));
        IPAddressNotAllowedException ex = assertThrows(
            IPAddressNotAllowedException.class, () -> wrapper.checkAllowedX(INTERNAL)
        );
        assertEquals("External IP address hosts disallowed.", ex.getMessage());
    }

    @Test
    void checkAllowedX_any_public_doesNotThrow() throws UnknownHostException {
        var wrapper = new HostAddressUtil.InetAddressWrapper(InetAddress.getByName("8.8.8.8"));
        assertDoesNotThrow(() -> wrapper.checkAllowedX(ANY));
    }

    @Test
    void checkAllowedX_any_loopback_doesNotThrow() throws UnknownHostException {
        var wrapper = new HostAddressUtil.InetAddressWrapper(InetAddress.getByName("127.0.0.1"));
        assertDoesNotThrow(() -> wrapper.checkAllowedX(ANY));
    }

    @Test
    void checkAllowedX_any_multicast_throwsMulticastMessage() throws UnknownHostException {
        // ANY never throws inside the switch (it just breaks), so this is the one mode where the trailing
        // isMulticast() check is reliably what fires, and the message is the expected "Multicast" one.
        var wrapper = new HostAddressUtil.InetAddressWrapper(InetAddress.getByName("224.0.0.1"));
        IPAddressNotAllowedException ex = assertThrows(
            IPAddressNotAllowedException.class, () -> wrapper.checkAllowedX(ANY)
        );
        assertEquals("Multicast IP address hosts disallowed.", ex.getMessage());
    }

    // ---------------------------------------------------------------------------
    // IPAddressOutgoingRequestFilterMode.get
    // ---------------------------------------------------------------------------

    @Test
    void filterMode_get_byName() {
        assertEquals(ANY, HostAddressUtil.IPAddressOutgoingRequestFilterMode.get("ANY", NONE));
        assertEquals(NONE, HostAddressUtil.IPAddressOutgoingRequestFilterMode.get("NONE", ANY));
        assertEquals(INTERNAL, HostAddressUtil.IPAddressOutgoingRequestFilterMode.get("INTERNAL", NONE));
        assertEquals(EXTERNAL, HostAddressUtil.IPAddressOutgoingRequestFilterMode.get("EXTERNAL", NONE));
    }

    @Test
    void filterMode_get_trueFalseAliases() {
        assertEquals(ANY, HostAddressUtil.IPAddressOutgoingRequestFilterMode.get("true", NONE));
        assertEquals(ANY, HostAddressUtil.IPAddressOutgoingRequestFilterMode.get("yes", NONE));
        assertEquals(ANY, HostAddressUtil.IPAddressOutgoingRequestFilterMode.get("1", NONE));
        assertEquals(NONE, HostAddressUtil.IPAddressOutgoingRequestFilterMode.get("false", ANY));
        assertEquals(NONE, HostAddressUtil.IPAddressOutgoingRequestFilterMode.get("no", ANY));
        assertEquals(NONE, HostAddressUtil.IPAddressOutgoingRequestFilterMode.get("0", ANY));
        assertEquals(INTERNAL, HostAddressUtil.IPAddressOutgoingRequestFilterMode.get("local", NONE));
    }

    @Test
    void filterMode_get_singleCharTrueFalseAliases() {
        // A string-switch with multiple labels per case has a distinct comparison per label;
        // "t" and "f" are single-character aliases that are distinct branches from "true" and "false".
        assertEquals(ANY, HostAddressUtil.IPAddressOutgoingRequestFilterMode.get("t", NONE));
        assertEquals(NONE, HostAddressUtil.IPAddressOutgoingRequestFilterMode.get("f", ANY));
    }

    @Test
    void filterMode_get_unknownUsesDefault() {
        assertEquals(ANY, HostAddressUtil.IPAddressOutgoingRequestFilterMode.get("bogus", ANY));
        assertEquals(NONE, HostAddressUtil.IPAddressOutgoingRequestFilterMode.get(null, NONE));
    }

    // ---------------------------------------------------------------------------
    // isForTesting
    // ---------------------------------------------------------------------------

    @Test
    void isForTesting_null_throws() {
        assertThrows(NullPointerException.class, () -> HostAddressUtil.isForTesting(null));
    }

    @Test
    void isForTesting_dotTest_returnsTrue() {
        assertTrue(HostAddressUtil.isForTesting("myapp.test"));
        assertTrue(HostAddressUtil.isForTesting("myapp.TEST"));
    }

    @Test
    void isForTesting_dotExample_returnsTrue() {
        assertTrue(HostAddressUtil.isForTesting("foo.example"));
    }

    @Test
    void isForTesting_dotInvalid_returnsTrue() {
        assertTrue(HostAddressUtil.isForTesting("bad.invalid"));
    }

    @Test
    void isForTesting_dotLocalhost_returnsTrue() {
        assertTrue(HostAddressUtil.isForTesting("app.localhost"));
    }

    @Test
    void isForTesting_exampleCom_returnsTrue() {
        assertTrue(HostAddressUtil.isForTesting("example.com"));
        assertTrue(HostAddressUtil.isForTesting("sub.example.com"));
        assertTrue(HostAddressUtil.isForTesting("example.net"));
        assertTrue(HostAddressUtil.isForTesting("example.org"));
        assertTrue(HostAddressUtil.isForTesting("example.edu"));
    }

    @Test
    void isForTesting_realHost_returnsFalse() {
        assertFalse(HostAddressUtil.isForTesting("google.com"));
        assertFalse(HostAddressUtil.isForTesting("localhost"));
    }

    // ---------------------------------------------------------------------------
    // isOrIsInDomain
    // ---------------------------------------------------------------------------

    @Test
    void isOrIsInDomain_nullDomain_throws() {
        assertThrows(NullPointerException.class, () -> HostAddressUtil.isOrIsInDomain(null, "example.com"));
    }

    @Test
    void isOrIsInDomain_nullHost_throws() {
        assertThrows(NullPointerException.class, () -> HostAddressUtil.isOrIsInDomain("example.com", null));
    }

    @Test
    void isOrIsInDomain_exactMatch() {
        assertTrue(HostAddressUtil.isOrIsInDomain("example.com", "example.com"));
        assertTrue(HostAddressUtil.isOrIsInDomain("example.com", "EXAMPLE.COM"));
    }

    @Test
    void isOrIsInDomain_subdomain() {
        assertTrue(HostAddressUtil.isOrIsInDomain("example.com", "sub.example.com"));
        assertTrue(HostAddressUtil.isOrIsInDomain("example.com", "deep.sub.example.com"));
    }

    @Test
    void isOrIsInDomain_noMatch() {
        assertFalse(HostAddressUtil.isOrIsInDomain("example.com", "notexample.com"));
        assertFalse(HostAddressUtil.isOrIsInDomain("example.com", "example.org"));
    }

    @Test
    void isOrIsInDomain_partialSuffix_noMatch() {
        // "fakeexample.com" should NOT match domain "example.com"
        assertFalse(HostAddressUtil.isOrIsInDomain("example.com", "fakeexample.com"));
    }

    // ---------------------------------------------------------------------------
    // isOrIsInAnyDomain
    // ---------------------------------------------------------------------------

    @Test
    void isOrIsInAnyDomain_nullDomains_throws() {
        assertThrows(NullPointerException.class, () -> HostAddressUtil.isOrIsInAnyDomain(null, "h.com"));
    }

    @Test
    void isOrIsInAnyDomain_nullHost_throws() {
        assertThrows(NullPointerException.class, () -> HostAddressUtil.isOrIsInAnyDomain(List.of("a.com"), null));
    }

    @Test
    void isOrIsInAnyDomain_matchesOne() {
        assertTrue(HostAddressUtil.isOrIsInAnyDomain(List.of("a.com", "b.com"), "sub.b.com"));
    }

    @Test
    void isOrIsInAnyDomain_matchesNone() {
        assertFalse(HostAddressUtil.isOrIsInAnyDomain(List.of("a.com", "b.com"), "c.com"));
    }

    // ---------------------------------------------------------------------------
    // matchesDomainPattern
    // ---------------------------------------------------------------------------

    @Test
    void matchesDomainPattern_nullDomain_throws() {
        assertThrows(NullPointerException.class, () -> HostAddressUtil.matchesDomainPattern(null, "h.com"));
    }

    @Test
    void matchesDomainPattern_nullHost_throws() {
        assertThrows(NullPointerException.class, () -> HostAddressUtil.matchesDomainPattern("a.com", null));
    }

    @Test
    void matchesDomainPattern_exactMatch() {
        assertTrue(HostAddressUtil.matchesDomainPattern("example.com", "example.com"));
        assertTrue(HostAddressUtil.matchesDomainPattern("example.com", "EXAMPLE.COM"));
    }

    @Test
    void matchesDomainPattern_exactMatch_noSubdomain() {
        // Without wildcard, subdomain should NOT match
        assertFalse(HostAddressUtil.matchesDomainPattern("example.com", "sub.example.com"));
    }

    @Test
    void matchesDomainPattern_wildcardPrefix_matchesSubdomain() {
        assertTrue(HostAddressUtil.matchesDomainPattern("*.example.com", "sub.example.com"));
        assertTrue(HostAddressUtil.matchesDomainPattern("*.example.com", "example.com"));
    }

    @Test
    void matchesDomainPattern_wildcardPrefix_noMatch() {
        assertFalse(HostAddressUtil.matchesDomainPattern("*.example.com", "other.com"));
    }

    // ---------------------------------------------------------------------------
    // matchesAnyDomain
    // ---------------------------------------------------------------------------

    @Test
    void matchesAnyDomain_matchesWildcard() {
        assertTrue(HostAddressUtil.matchesAnyDomain(List.of("*.example.com", "other.org"), "sub.example.com"));
    }

    @Test
    void matchesAnyDomain_noMatch() {
        assertFalse(HostAddressUtil.matchesAnyDomain(List.of("a.com", "*.b.com"), "c.com"));
    }

    // ---------------------------------------------------------------------------
    // isIPAddressFilter
    // ---------------------------------------------------------------------------

    @Test
    void isIPAddressFilter_exactIP_true() {
        assertTrue(HostAddressUtil.isIPAddressFilter("192.168.1.1"));
        assertTrue(HostAddressUtil.isIPAddressFilter("127.0.0.1"));
    }

    @Test
    void isIPAddressFilter_classA_wildcard_true() {
        assertTrue(HostAddressUtil.isIPAddressFilter("10.*"));
    }

    @Test
    void isIPAddressFilter_classB_wildcard_true() {
        assertTrue(HostAddressUtil.isIPAddressFilter("192.168.*"));
    }

    @Test
    void isIPAddressFilter_classC_wildcard_true() {
        assertTrue(HostAddressUtil.isIPAddressFilter("192.168.1.*"));
    }

    @Test
    void isIPAddressFilter_hostname_false() {
        assertFalse(HostAddressUtil.isIPAddressFilter("example.com"));
    }

    @Test
    void isIPAddressFilter_partialWildcard_false() {
        assertFalse(HostAddressUtil.isIPAddressFilter("192.168.1.1.*"));
        assertFalse(HostAddressUtil.isIPAddressFilter("*.168.1.1"));
    }

    // ---------------------------------------------------------------------------
    // getIPAddressFilter
    // ---------------------------------------------------------------------------

    @Test
    void getIPAddressFilter_exactIP_matchesThatIP() throws UnknownHostException {
        Predicate<InetAddress> filter = HostAddressUtil.getIPAddressFilter("8.8.8.8");
        assertNotNull(filter);
        assertTrue(filter.test(InetAddress.getByName("8.8.8.8")));
        assertFalse(filter.test(InetAddress.getByName("8.8.4.4")));
    }

    @Test
    void getIPAddressFilter_classC_matchesSubnet() throws UnknownHostException {
        Predicate<InetAddress> filter = HostAddressUtil.getIPAddressFilter("192.168.1.*");
        assertNotNull(filter);
        assertTrue(filter.test(InetAddress.getByName("192.168.1.100")));
        assertFalse(filter.test(InetAddress.getByName("192.168.2.100")));
    }

    @Test
    void getIPAddressFilter_classB_matchesSubnet() throws UnknownHostException {
        Predicate<InetAddress> filter = HostAddressUtil.getIPAddressFilter("10.0.*");
        assertNotNull(filter);
        assertTrue(filter.test(InetAddress.getByName("10.0.1.5")));
        assertFalse(filter.test(InetAddress.getByName("10.1.0.5")));
    }

    @Test
    void getIPAddressFilter_classA_matchesSubnet() throws UnknownHostException {
        Predicate<InetAddress> filter = HostAddressUtil.getIPAddressFilter("10.*");
        assertNotNull(filter);
        assertTrue(filter.test(InetAddress.getByName("10.5.6.7")));
        assertFalse(filter.test(InetAddress.getByName("11.5.6.7")));
    }

    @Test
    void getIPAddressFilter_hostname_returnsNull() {
        assertNull(HostAddressUtil.getIPAddressFilter("example.com"));
    }

    // ---------------------------------------------------------------------------
    // getAnyIPAddressFilter
    // ---------------------------------------------------------------------------

    @Test
    void getAnyIPAddressFilter_multipleSpecs_matchesAny() throws UnknownHostException {
        Predicate<? super InetAddress> filter = HostAddressUtil.getAnyIPAddressFilter(
            Set.of("8.8.8.8", "192.168.1.*")
        );
        assertTrue(filter.test(InetAddress.getByName("8.8.8.8")));
        assertTrue(filter.test(InetAddress.getByName("192.168.1.50")));
        assertFalse(filter.test(InetAddress.getByName("1.2.3.4")));
    }

    @Test
    void getAnyIPAddressFilter_emptySet_neverMatches() throws UnknownHostException {
        Predicate<? super InetAddress> filter = HostAddressUtil.getAnyIPAddressFilter(Set.of());
        assertFalse(filter.test(InetAddress.getByName("8.8.8.8")));
    }

    // ---------------------------------------------------------------------------
    // getHostNameWhitelistOrBlacklistFilter
    // ---------------------------------------------------------------------------

    @Test
    void getHostNameWhitelistOrBlacklistFilter_bothNull_alwaysTrue() {
        Predicate<String> filter = HostAddressUtil.getHostNameWhitelistOrBlacklistFilter(null, null);
        assertTrue(filter.test("anything.com"));
    }

    @Test
    void getHostNameWhitelistOrBlacklistFilter_whitelist_usesWhitelist() {
        Predicate<String> filter = HostAddressUtil.getHostNameWhitelistOrBlacklistFilter(
            Set.of("allowed.com"), Set.of("blocked.com")
        );
        assertTrue(filter.test("allowed.com"));
        assertFalse(filter.test("other.com"));
        // blacklist is ignored when whitelist present
        assertFalse(filter.test("blocked.com"));
    }

    @Test
    void getHostNameWhitelistOrBlacklistFilter_blacklistOnly_negatesMatch() {
        Predicate<String> filter = HostAddressUtil.getHostNameWhitelistOrBlacklistFilter(
            null, Set.of("*.bad.com")
        );
        assertFalse(filter.test("evil.bad.com"));
        assertTrue(filter.test("good.com"));
    }

    // ---------------------------------------------------------------------------
    // getHostIPAddressWhitelistOrBlacklistFilter
    // ---------------------------------------------------------------------------

    @Test
    void getHostIPAddressWhitelistOrBlacklistFilter_bothNull_alwaysTrue() throws UnknownHostException {
        Predicate<? super InetAddress> filter = HostAddressUtil.getHostIPAddressWhitelistOrBlacklistFilter(null, null);
        assertTrue(filter.test(InetAddress.getByName("1.2.3.4")));
    }

    @Test
    void getHostIPAddressWhitelistOrBlacklistFilter_whitelist_usesWhitelist() throws UnknownHostException {
        Predicate<? super InetAddress> filter = HostAddressUtil.getHostIPAddressWhitelistOrBlacklistFilter(
            Set.of("8.8.8.8"), Set.of("1.1.1.1")
        );
        assertTrue(filter.test(InetAddress.getByName("8.8.8.8")));
        assertFalse(filter.test(InetAddress.getByName("1.1.1.1"))); // blacklist ignored
        assertFalse(filter.test(InetAddress.getByName("4.4.4.4")));
    }

    @Test
    void getHostIPAddressWhitelistOrBlacklistFilter_blacklistOnly_excludesListed() throws UnknownHostException {
        Predicate<? super InetAddress> filter = HostAddressUtil.getHostIPAddressWhitelistOrBlacklistFilter(
            null, Set.of("8.8.8.8")
        );
        assertFalse(filter.test(InetAddress.getByName("8.8.8.8")));
        assertTrue(filter.test(InetAddress.getByName("1.1.1.1")));
    }

    // ---------------------------------------------------------------------------
    // getAnyDomainFilter
    // ---------------------------------------------------------------------------

    @Test
    void getAnyDomainFilter_matchesPatterns() {
        Predicate<String> filter = HostAddressUtil.getAnyDomainFilter(List.of("*.example.com", "exact.org"));
        assertTrue(filter.test("sub.example.com"));
        assertTrue(filter.test("exact.org"));
        assertFalse(filter.test("other.net"));
    }

}
