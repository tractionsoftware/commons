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

import com.google.common.net.InetAddresses;
import com.tractionsoftware.commons.lang.EnumUtil;
import com.tractionsoftware.commons.lang.StringUtil;
import com.tractionsoftware.commons.util.CollectionUtil;
import com.tractionsoftware.commons.util.function.PredicateUtil;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Some helpers for parsing and interpreting {@link InetAddress} and other host addresses.
 */
public final class HostAddressUtil {

    private HostAddressUtil() {
    }

    public static final String LOOPBACK_HOST_NAME = "localhost";

    public static final String LOOPBACK_IP4_ADDRESS = "127.0.0.1";

    private static final String IP4_SEGMENT = "(25[0-5]|(2[0-4]|1?[0-9])?[0-9])";

    private static final Pattern IP4_ADDRESS_FILTER_CLASS_A_SUBNET = Pattern.compile(
        "^" + IP4_SEGMENT + "\\.\\*$"
    );

    private static final Pattern IP4_ADDRESS_FILTER_CLASS_B_SUBNET = Pattern.compile(
        "^" +
        IP4_SEGMENT + "\\." +
        IP4_SEGMENT + "\\.\\*$"
    );

    private static final Pattern IP4_ADDRESS_FILTER_CLASS_C_SUBNET = Pattern.compile(
        "^" +
        IP4_SEGMENT + "\\." +
        IP4_SEGMENT + "\\." +
        IP4_SEGMENT + "\\.\\*$"
    );

    private static final InetAddressParseResult FAILED_INET_ADDRESS_PARSE_RESULT = new InetAddressParseResult() {

        @Override
        public final boolean wasAddress() {
            return false;
        }

        @Override
        public final InetAddressWrapper getParsed() {
            return null;
        }

    };

    /**
     * Enumeration of the modes for filtering IP addresses to be used for a given type of outgoing requests, e.g.,
     * automatically captured resources to be attached to an entry. In cases in which administrators need to lock down
     * certain types of requests made by TeamPage, this is intended to provide a first level of blanket filtering, to be
     * complemented by other finer grained filtering, such as
     * {@link HostAddressUtil#getHostIPAddressWhitelistOrBlacklistFilter(Set, Set) whitelist or blacklist filtering} by
     * exact IP address or by {@link HostAddressUtil#isIPAddressFilter(String) other supported IP filter expressions}.
     */
    public static enum IPAddressOutgoingRequestFilterMode {

        /**
         * Requests to an IP address host never allowed.
         */
        NONE,

        /**
         * Requests to an IP address host allowed only when the address is internal -- i.e., to a subnet that is either
         * {@link InetAddressWrapper#isLocal() local} or {@link InetAddressWrapper#isPrivate() private}.
         */
        INTERNAL,

        /**
         * Requests to an IP address host allowed only when the address is not internal -- i.e., not to a subnet that is
         * either {@link InetAddressWrapper#isLocal() local} or {@link InetAddressWrapper#isPrivate() private}.
         */
        EXTERNAL,

        /**
         * Requests to an IP address host always allowed.
         */
        ANY;

        public static IPAddressOutgoingRequestFilterMode get(String str, IPAddressOutgoingRequestFilterMode defaultValue) {

            IPAddressOutgoingRequestFilterMode parsed = EnumUtil.enumFromString(
                IPAddressOutgoingRequestFilterMode.class, str, null
            );
            if (parsed != null) {
                return parsed;
            }

            return switch (StringUtils.lowerCase(str)) {
                case "t", "true", "1", "yes" -> ANY;
                case "f", "false", "0", "no" -> NONE;
                case "local" -> INTERNAL;
                case null, default -> defaultValue;
            };

        }

    }

    /**
     * Wraps an {@link InetAddress} to provide some additional useful functionality.
     */
    public static final class InetAddressWrapper {

        private final InetAddress address;

        public InetAddressWrapper(InetAddress address) {
            Objects.requireNonNull(address, "address");
            this.address = address;
        }

        /**
         * Returns true if this address is a local address. This can mean it is the loopback address (127.*.*.*), "any"
         * local address (i.e., a wildcard address whose integer value is 0), or a site-local address (i.e.,
         * 192.168.*.*).
         *
         * @return true if this address is a local address.
         */
        public final boolean isLocal() {
            if (address.isLoopbackAddress() || address.isAnyLocalAddress() || address.isSiteLocalAddress()) {
                return true;
            }
            return false;
        }

        /**
         * Returns true if this address represents a private sub-net, for either IPv4 or IPv6. This is equivalent to
         * {@link InetAddress#isLinkLocalAddress()}.
         *
         * @return true if this address represents a private sub-net, for either IPv4 or IPv6; false if not.
         */
        public final boolean isPrivate() {
            return address.isLinkLocalAddress();
        }

        /**
         * Returns true if this address is a multicast address.
         *
         * @return true if this address is a multicast address.
         */
        public final boolean isMulticast() {
            return address.isMulticastAddress();
        }

        /**
         * Returns the wrapped {@link InetAddress}.
         *
         * @return the wrapped {@link InetAddress}.
         */
        @Nonnull
        public final InetAddress get() {
            return address;
        }

        public final boolean checkAllowedQ(@Nonnull IPAddressOutgoingRequestFilterMode mode) {

            if (isMulticast()) {
                return false;
            }

            return switch (mode) {
                case NONE -> false;
                case EXTERNAL -> !isLocal() && !isPrivate();
                case INTERNAL -> isLocal() || isPrivate();
                case ANY -> true;
            };

        }

        public final void checkAllowedX(@Nonnull IPAddressOutgoingRequestFilterMode mode) throws IPAddressNotAllowedException {

            switch (mode) {

            case NONE:
                throw new IPAddressNotAllowedException("All IP address hosts disallowed.");

            case EXTERNAL:
                if (isLocal() || isPrivate()) {
                    throw new IPAddressNotAllowedException("Local/private IP address hosts disallowed.");
                }
                break;

            case INTERNAL:
                if (!isLocal() && !isPrivate()) {
                    throw new IPAddressNotAllowedException("External IP address hosts disallowed.");
                }
                break;

            case ANY:
                break;

            }

            if (isMulticast()) {
                throw new IPAddressNotAllowedException("Multicast IP address hosts disallowed.");
            }

        }

    }

    /**
     * Encapsulates the result of invoking {@link HostAddressUtil#parseIPAddress(String)}.
     */
    public interface InetAddressParseResult {

        /**
         * Returns true if the given text did indeed represent an IP address literal.
         *
         * @return true if the given text did indeed represent an IP address literal; false otherwise.
         */
        boolean wasAddress();

        /**
         * Returns an {@link InetAddressWrapper} representing the parsed address if the given text represented a valid
         * IP address. This will always return a non-null value if {@link #wasAddress()} returns true.
         *
         * @return an {@link InetAddressWrapper} representing the parsed address if the given text represented a valid
         *     IP address; null otherwise.
         */
        InetAddressWrapper getParsed();

    }

    /**
     * Represents a result for {@link HostAddressUtil#parseIPAddress(String)} representing success, in which a valid
     * {@link InetAddress} was recognized.
     */
    private static final class SuccessfulInetAddressParseResult implements InetAddressParseResult {

        private final InetAddressWrapper wrapper;

        public SuccessfulInetAddressParseResult(InetAddressWrapper wrapper) {
            this.wrapper = wrapper;
        }

        @Override
        public boolean wasAddress() {
            return true;
        }

        @Override
        public InetAddressWrapper getParsed() {
            return wrapper;
        }

    }

    private static final class IPAddressFilter implements Predicate<InetAddress> {

        public static IPAddressFilter createFromFilterSpec(String filterSpec) {

            Matcher matcher = IP4_ADDRESS_FILTER_CLASS_A_SUBNET.matcher(filterSpec);
            if (matcher.matches()) {
                return createClassASubnetFilter(matcher);
            }

            matcher = IP4_ADDRESS_FILTER_CLASS_B_SUBNET.matcher(filterSpec);
            if (matcher.matches()) {
                return createClassBSubnetFilter(matcher);
            }

            matcher = IP4_ADDRESS_FILTER_CLASS_C_SUBNET.matcher(filterSpec);
            if (matcher.matches()) {
                return createClassCSubnetFilter(matcher);
            }

            return null;

        }

        private static IPAddressFilter createClassASubnetFilter(Matcher matcher) {
            try {
                return new IPAddressFilter(new byte[] { (byte) Integer.parseInt(matcher.group(1)) });
            }
            catch (NumberFormatException e) {
                return null;
            }
        }

        private static IPAddressFilter createClassBSubnetFilter(Matcher matcher) {
            try {
                return new IPAddressFilter(
                    new byte[] { (byte) Integer.parseInt(matcher.group(1)), (byte) Integer.parseInt(matcher.group(3)) }
                );
            }
            catch (NumberFormatException e) {
                return null;
            }
        }

        private static IPAddressFilter createClassCSubnetFilter(Matcher matcher) {
            try {
                return new IPAddressFilter(
                    new byte[] {
                        (byte) Integer.parseInt(matcher.group(1)),
                        (byte) Integer.parseInt(matcher.group(3)),
                        (byte) Integer.parseInt(matcher.group(5))
                    }
                );
            }
            catch (NumberFormatException e) {
                return null;
            }
        }

        private final byte[] masks;

        private IPAddressFilter(byte[] masks) {
            this.masks = masks;
        }

        @Override
        public boolean test(InetAddress addr) {
            byte[] parts = addr.getAddress();
            if (parts.length < masks.length) {
                return false;
            }
            for (int i = 0; i < masks.length; i++) {
                if (masks[i] != parts[i]) {
                    return false;
                }
            }
            return true;
        }

    }

    /**
     * Attempts to parse the given text as an IP address literal.
     *
     * @param addressSpec
     *     the text which may be an IP address literal.
     * @return an {@link InetAddressParseResult} representing the result of the attempt to parse the given text as an IP
     *     address literal.
     */
    public static InetAddressParseResult parseIPAddress(@Nullable String addressSpec) {
        if (StringUtils.isEmpty(addressSpec)) {
            return FAILED_INET_ADDRESS_PARSE_RESULT;
        }
        try {
            return new SuccessfulInetAddressParseResult(new InetAddressWrapper(InetAddresses.forString(addressSpec)));
        }
        catch (RuntimeException e) {
            return FAILED_INET_ADDRESS_PARSE_RESULT;
        }
    }

    /**
     * Returns true if the given host appears to be a host reserved for testing according to
     * <a href="https://www.rfc-editor.org/rfc/rfc2606">RFC 2606</a>
     * or <a href="https://www.rfc-editor.org/rfc/rfc6761.html">RFC 6761</a>.
     *
     * @param host
     *     the host to examine.
     * @return true if the given host appears to be a host reserved for testing; false otherwise.
     * @throws NullPointerException
     *     if the host is null.
     */
    public static boolean isForTesting(String host) {

        Objects.requireNonNull(host, "host");

        // RFC 2606, Section 2
        if (Strings.CI.endsWith(host, ".test") ||
            Strings.CI.endsWith(host, ".example") ||
            Strings.CI.endsWith(host, ".invalid") ||
            Strings.CI.endsWith(host, ".localhost")) {
            return true;
        }

        // RFC 2606, Section 3
        // Also, example.edu is not listed in that spec, but works the same way, as of the time of writing.
        if (isOrIsInDomain("example.com", host) ||
            isOrIsInDomain("example.net", host) ||
            isOrIsInDomain("example.org", host) ||
            isOrIsInDomain("example.edu", host)) {
            return true;
        }

        return false;

    }

    /**
     * Returns true if the given host seems to be the same as or "in" the given domain.
     *
     * @param domain
     *     the domain.
     * @param host
     *     the host address.
     * @return true if the given host seems to be the same as or "in" the given domain; false otherwise.
     * @throws NullPointerException
     *     if either the domain or host is null.
     */
    public static boolean isOrIsInDomain(String domain, String host) {
        Objects.requireNonNull(domain, "domain");
        Objects.requireNonNull(host, "host");
        if (domain.equalsIgnoreCase(host)) {
            return true;
        }
        if (Strings.CI.endsWith(host, "." + domain)) {
            return true;
        }
        return false;
    }

    /**
     * Returns true if the given host seems to be the same as or "in" any of the given domains.
     *
     * @param domains
     *     the domains.
     * @param host
     *     the host address.
     * @return true if the given host seems to be the same as or "in" the given domain; false otherwise.
     * @throws NullPointerException
     *     if either the domains or host is null, or if any of the domains is null.
     */
    public static boolean isOrIsInAnyDomain(Iterable<String> domains, String host) {
        Objects.requireNonNull(domains, "domains");
        Objects.requireNonNull(host, "host");
        for (String domain : domains) {
            if (isOrIsInDomain(domain, host)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns true if the given host seems to match the domain pattern. If the domain pattern starts with "*.", this
     * means that the host {@link #isOrIsInDomain(String, String) is in the domain}. Otherwise, it means the host
     * exactly matches the domain.
     *
     * @param domainPattern
     *     the domain pattern.
     * @param host
     *     the host address.
     * @return true if the given host seems to be the same as or "in" the given domain; false otherwise.
     * @throws NullPointerException
     *     if either the domain or host is null.
     */
    public static boolean matchesDomainPattern(String domainPattern, String host) {
        Objects.requireNonNull(domainPattern, "domain");
        Objects.requireNonNull(host, "host");
        if (domainPattern.startsWith("*.")) {
            domainPattern = domainPattern.substring(2);
            return isOrIsInDomain(domainPattern, host);
        }
        if (domainPattern.equalsIgnoreCase(host)) {
            return true;
        }
        return false;
    }

    /**
     * Returns true if the given host matches any of the domain patterns. For any domain pattern starting with "*.",
     * this means that the host {@link #isOrIsInDomain(String, String) is in the domain}. Otherwise, it means the host
     * exactly matches the domain.
     *
     * @param domainPatterns
     *     the domain patterns.
     * @param host
     *     the host address.
     * @return true if the given host seems to be the same as or "in" the given domain; false otherwise.
     * @throws NullPointerException
     *     if either the domain patterns or host is null, or if any domain pattern is null.
     */
    public static boolean matchesAnyDomain(Iterable<String> domainPatterns, String host) {
        Objects.requireNonNull(domainPatterns, "domain patterns");
        Objects.requireNonNull(host, "host");
        for (String domainPattern : domainPatterns) {
            if (matchesDomainPattern(domainPattern, host)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns a {@link Predicate} that accepts host names that match any of the given domain patterns. See
     * {@link #matchesAnyDomain(Iterable, String)}
     *
     * @param domainPatterns
     *     the domain patterns.
     * @return a {@link Predicate} that accepts host names that match any of the given domain patterns.
     */
    public static Predicate<String> getAnyDomainFilter(Iterable<String> domainPatterns) {
        return host -> matchesAnyDomain(domainPatterns, host);
    }

    /**
     * Returns true if the given expression represents a valid and supported IP address filter. At this time, supported
     * filters are only IPv4 address filters of the following forms: an exact valid IP address; a wildcard filter for a
     * class A subnet (e.g., "57.*"); a wildcard filter for a class B subnet (e.g., "57.184.*"), and a wildcard filter
     * for a class C subnet (e.g., "57.184.99.*").
     *
     * @param filterSpec
     *     an expression which may or may not be a valid and supported IP address filter.
     * @return true if the given expression represents a valid and supported IP address filter; false otherwise.
     */
    public static boolean isIPAddressFilter(String filterSpec) {
        if (parseIPAddress(filterSpec).wasAddress() ||
            IP4_ADDRESS_FILTER_CLASS_A_SUBNET.matcher(filterSpec).matches() ||
            IP4_ADDRESS_FILTER_CLASS_B_SUBNET.matcher(filterSpec).matches() ||
            IP4_ADDRESS_FILTER_CLASS_C_SUBNET.matcher(filterSpec).matches()) {
            return true;
        }
        return false;
    }

    /**
     * Returns a {@link Predicate} that accepts {@link InetAddress}es that match any of the given filter expressions
     * that are valid IP address filters. At this time, supported filters are only IPv4 address filters of the following
     * forms: an exact valid IP address; a wildcard filter for a class A subnet (e.g., "57.*"); a wildcard filter for a
     * class B subnet (e.g., "57.184.*"), and a wildcard filter for a class C subnet (e.g., "57.184.99.*").
     *
     * @param filterSpecs
     *     the set of filter expressions, which may contain one or more valid IP address filter expressions.
     * @return a {@link Predicate} that accepts {@link InetAddress}es that match any of the given filter expressions
     *     that are valid IP address filters, if there is at least one valid IP address filter in the given set;
     *     otherwise, a Predicate that always returns false.
     */
    public static Predicate<? super InetAddress> getAnyIPAddressFilter(Set<String> filterSpecs) {
        List<Predicate<InetAddress>> all = new ArrayList<>();
        for (String filterSpec : filterSpecs) {
            CollectionUtil.addIfNotNull(getIPAddressFilter(filterSpec), all);
        }
        return PredicateUtil.or(all);
    }

    /**
     * Returns a {@link Predicate} that accepts {@link InetAddress}es that match the given filter expression, if it
     * represents a valid and supported filter. At this time, supported filters are only IPv4 address filters of the
     * following forms: an exact valid IP address; a wildcard filter for a class A subnet (e.g., "57.*"); a wildcard
     * filter for a class B subnet (e.g., "57.184.*"), and a wildcard filter for a class C subnet (e.g.,
     * "57.184.99.*").
     *
     * @param filterSpec
     *     the filter expression that may be a valid and supported IP address filter.
     * @return a {@link Predicate} that accepts {@link InetAddress}es that match the given filter expression, if it
     *     represents a valid and supported filter; null otherwise.
     */
    public static Predicate<InetAddress> getIPAddressFilter(String filterSpec) {
        InetAddressParseResult parsed = parseIPAddress(filterSpec);
        if (parsed.wasAddress()) {
            return parsed.getParsed().get()::equals;
        }
        return IPAddressFilter.createFromFilterSpec(filterSpec);
    }

    /**
     * Returns a {@link Predicate} that either uses a whitelist matching strategy or a blacklist matching strategy to
     * filter host names against domains. If the whitelist is not null, the whitelist matching strategy is used.
     * Otherwise, if the blacklist is not null, the blacklist matching strategy is used. If both ar null, this method
     * returns a vacuous Predicate is returned that always returns true.
     *
     * <p>
     * This is obviously intended to support the case in which <em>either</em> a whitelist or a blacklist is to be used,
     * but not both. To use both, clients should instead use {@link Predicate#or(Predicate)} to join the results of two
     * separate Predicates created via {@link #matchesAnyDomain(Iterable, String)}.
     *
     * @param domainWhitelist
     *     a whitelist, which if not null will be used for a whitelist filtering strategy.
     * @param domainBlacklist
     *     a whitelist, which if not null will be used for a blacklist filtering strategy.
     * @return a {@link Predicate} that either uses a whitelist matching strategy or a blacklist matching strategy to
     *     filter host names against domains, if either the supplied domain whitelist or domain blacklist is not null;
     *     otherwise, a vacuous Predicate that always returns true.
     */
    public static Predicate<String> getHostNameWhitelistOrBlacklistFilter(Set<String> domainWhitelist, Set<String> domainBlacklist) {
        if (domainWhitelist != null) {
            return getAnyDomainFilter(domainWhitelist);
        }
        if (domainBlacklist != null) {
            return getAnyDomainFilter(domainBlacklist).negate();
        }
        return host -> true;
    }

    /**
     * Returns a {@link Predicate} that either uses a whitelist matching strategy or a blacklist matching strategy to
     * filter {@link InetAddress}es against IP address filters. If the whitelist is not null, the whitelist matching
     * strategy is used. Otherwise, if the blacklist is not null, the blacklist matching strategy is used. If both ar
     * null, this method returns a vacuous Predicate is returned that always returns true.
     *
     * <p>
     * This is obviously intended to support the case in which <em>either</em> a whitelist or a blacklist is to be used,
     * but not both. To use both, clients should instead use {@link Predicate#or(Predicate)} to join the results of two
     * separate Predicates created via {@link #getAnyIPAddressFilter(Set)}.
     *
     * @param whitelistFilterSpecs
     *     a whitelist, which if not null will be used for a whitelist filtering strategy.
     * @param blacklistFilterSpecs
     *     a whitelist, which if not null will be used for a blacklist filtering strategy.
     * @return a {@link Predicate} that either uses a whitelist matching strategy or a blacklist matching strategy to
     *     filter {@link InetAddress}es against IP address filters, if either the supplied whitelist filter set or
     *     blacklist filter set is not null; otherwise, a vacuous Predicate that always returns true.
     */
    public static Predicate<? super InetAddress> getHostIPAddressWhitelistOrBlacklistFilter(Set<String> whitelistFilterSpecs, Set<String> blacklistFilterSpecs) {
        if (whitelistFilterSpecs != null) {
            return getAnyIPAddressFilter(whitelistFilterSpecs);
        }
        if (blacklistFilterSpecs != null) {
            return getAnyIPAddressFilter(blacklistFilterSpecs).negate();
        }
        return host -> true;
    }

}
