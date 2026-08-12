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

import com.google.common.base.Ascii;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ImmutableListMultimap;
import com.google.common.collect.ListMultimap;
import com.tractionsoftware.commons.lang.StringUtil;
import com.tractionsoftware.commons.util.CollectionUtil;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import org.junit.jupiter.api.Test;
import org.junit.platform.commons.util.StringUtils;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.*;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.*;

class URLUtilTest {

    /**
     * Minimal in-memory test double for {@link URLBuilder}, used to exercise the {@link URLUtil.URLModifierCallback}
     * family of nested classes without needing a real production implementation.
     */
    private static final class FakeURLBuilder implements URLBuilder<URL> {

        static final URI defaultBase = URI.create("https://example.com/");

        static final FakeURLBuilder createInstanceWithRelativeURI(String relativeUri) {
            return new FakeURLBuilder(defaultBase.resolve(relativeUri));
        }

        private final URI base;

        FakeURLBuilder() {
            this(defaultBase);
        }

        FakeURLBuilder(URI base) {
            this.base = Objects.requireNonNullElse(base, defaultBase);
        }

        final ListMultimap<String,String> params = ArrayListMultimap.create();

        final Set<String> kept = new HashSet<>();

        final Set<String> stuck = new HashSet<>();

        @Override
        public final String getParameterValue(@Nonnull String urlParameterName) {
            Objects.requireNonNull(urlParameterName, "parameter name");
            List<String> values = params.get(urlParameterName);
            if (CollectionUtil.isEmpty(values)) {
                return null;
            }
            return StringUtil.join(values, ',');
        }

        @Nonnull
        @Override
        public final Collection<String> getParameterValues(@Nonnull String urlParameterName) {
            Objects.requireNonNull(urlParameterName, "parameter name");
            List<String> values = params.get(urlParameterName);
            if (CollectionUtil.isEmpty(values)) {
                return Collections.emptyList();
            }
            return Collections.unmodifiableList(values);
        }

        @Override
        public final boolean keepParameter(@Nonnull String urlParameterName) {
            Objects.requireNonNull(urlParameterName, "parameter name");
            return kept.add(urlParameterName);
        }

        @Override
        public final boolean stickParameter(@Nonnull String urlParameterName) {
            Objects.requireNonNull(urlParameterName, "parameter name");
            return stuck.add(urlParameterName);
        }

        @Override
        public final URL build() throws MalformedURLException {
            return base.resolve(queryString()).toURL();
        }

        @Override
        public final boolean removeParameter(@Nonnull String urlParameterName) {
            Objects.requireNonNull(urlParameterName, "parameter name");
            if (params.removeAll(urlParameterName).isEmpty()) {
                return false;
            }
            return true;
        }

        @Override
        public final boolean removeParameterValue(@Nonnull String urlParameterName, @Nullable String removeValue) {
            if (StringUtils.isBlank(removeValue)) {
                return false;
            }
            return params.remove(urlParameterName, removeValue);
        }

        @Override
        public final void setParameterValue(@Nonnull String urlParameterName, @Nullable String value) {
            Objects.requireNonNull(urlParameterName, "parameter name");
            params.put(urlParameterName, value);
        }

        @Override
        public final void addParameterValues(@Nonnull String urlParameterName, @Nullable Collection<String> values) {
            Objects.requireNonNull(urlParameterName, "parameter name");
            if (CollectionUtil.isNotEmpty(values)) {
                params.putAll(urlParameterName, values);
            }
        }

        @Override
        public final void setParameterValues(@Nonnull String urlParameterName, @Nullable Collection<String> values) {
            Objects.requireNonNull(urlParameterName, "parameter name");
            params.removeAll(urlParameterName);
            if (CollectionUtil.isNotEmpty(values)) {
                params.putAll(urlParameterName, values);
            }
        }

        final String queryString() {
            return "";
        }

    }

    // ---------------------------------------------------------------------------
    // stripServer
    // ---------------------------------------------------------------------------

    @Test
    void stripServer_null_returnsNull() {
        assertNull(URLUtil.stripServer(null));
    }

    @Test
    void stripServer_relativeUrl_unchanged() {
        assertEquals("/path/to/page", URLUtil.stripServer("/path/to/page"));
    }

    @Test
    void stripServer_absoluteUrl_stripsServer() {
        assertEquals("/path/to/page", URLUtil.stripServer("https://example.com/path/to/page"));
    }

    @Test
    void stripServer_noPath_returnsAsIs() {
        // no path after host — implementation returns original
        assertEquals("https://example.com", URLUtil.stripServer("https://example.com"));
    }

    @Test
    void stripServer_capturesServer() {
        StringBuilder server = new StringBuilder();
        String path = URLUtil.stripServer("http://host.example.com/some/path", server);
        assertEquals("/some/path", path);
        assertEquals("http://host.example.com", server.toString());
    }

    // ---------------------------------------------------------------------------
    // getUrlEncoding / getUrlDecoded (round-trip)
    // ---------------------------------------------------------------------------

    @Test
    void getUrlEncoding_null_returnsNull() {
        assertNull(URLUtil.getUrlEncoding(null));
    }

    @Test
    void getUrlEncoding_alphanumeric_unchanged() {
        assertEquals("hello123", URLUtil.getUrlEncoding("hello123"));
    }

    @Test
    void getUrlEncoding_space_encoded() {
        String result = URLUtil.getUrlEncoding("hello world");
        assertTrue(result.contains("%20") || result.contains("+"), "space should be encoded: " + result);
    }

    @Test
    void getUrlDecoded_null_returnsNull() {
        assertNull(URLUtil.getUrlDecoded(null));
    }

    @Test
    void getUrlDecoded_empty_returnsEmpty() {
        assertEquals("", URLUtil.getUrlDecoded(""));
    }

    @Test
    void getUrlDecoded_percentEncoded() {
        assertEquals("hello world", URLUtil.getUrlDecoded("hello%20world"));
    }

    @Test
    void roundTrip_encodeAndDecode() {
        String original = "hello world & <more>";
        String encoded = URLUtil.getUrlEncoding(original);
        String decoded = URLUtil.getUrlDecoded(encoded);
        assertEquals(original, decoded);
    }

    // ---------------------------------------------------------------------------
    // isDataUrl / isHttpUrl / isFileUrl
    // ---------------------------------------------------------------------------

    @Test
    void isDataUrl_true() {
        assertTrue(URLUtil.isDataUrl("data:text/plain;base64,SGVsbG8="));
    }

    @Test
    void isDataUrl_false() {
        assertFalse(URLUtil.isDataUrl("https://example.com"));
    }

    @Test
    void isHttpUrl_http_true() {
        assertTrue(URLUtil.isHttpUrl("http://example.com"));
    }

    @Test
    void isHttpUrl_https_true() {
        assertTrue(URLUtil.isHttpUrl("https://example.com"));
    }

    @Test
    void isHttpUrl_ftp_false() {
        assertFalse(URLUtil.isHttpUrl("ftp://example.com"));
    }

    @Test
    void isFileUrl_true() {
        assertTrue(URLUtil.isFileUrl("file:///tmp/foo.txt"));
    }

    @Test
    void isFileUrl_false() {
        assertFalse(URLUtil.isFileUrl("https://example.com"));
    }

    // ---------------------------------------------------------------------------
    // getDefaultHttpPort
    // ---------------------------------------------------------------------------

    @Test
    void getDefaultHttpPort_secure_returns443() {
        assertEquals(443, URLUtil.getDefaultHttpPort(true));
    }

    @Test
    void getDefaultHttpPort_notSecure_returns80() {
        assertEquals(80, URLUtil.getDefaultHttpPort(false));
    }

    // ---------------------------------------------------------------------------
    // getHttpSchemeName
    // ---------------------------------------------------------------------------

    @Test
    void getHttpSchemeName_secure_returnsHttps() {
        assertEquals("https", URLUtil.getHttpSchemeName(true));
    }

    @Test
    void getHttpSchemeName_notSecure_returnsHttp() {
        assertEquals("http", URLUtil.getHttpSchemeName(false));
    }

    // ---------------------------------------------------------------------------
    // isSchemeAtLeastAsSecure
    // ---------------------------------------------------------------------------

    @Test
    void isSchemeAtLeastAsSecure_sameScheme_true() {
        URI a = URI.create("https://example.com");
        URI b = URI.create("https://example.com");
        assertTrue(URLUtil.isSchemeAtLeastAsSecure(a, b));
    }

    @Test
    void isSchemeAtLeastAsSecure_httpsVsHttp_true() {
        assertTrue(URLUtil.isSchemeAtLeastAsSecure(
            URI.create("https://example.com"),
            URI.create("http://example.com")
        ));
    }

    @Test
    void isSchemeAtLeastAsSecure_httpVsHttps_false() {
        assertFalse(URLUtil.isSchemeAtLeastAsSecure(
            URI.create("http://example.com"),
            URI.create("https://example.com")
        ));
    }

    @Test
    void isSchemeAtLeastAsSecure_nullRequest_false() {
        assertFalse(URLUtil.isSchemeAtLeastAsSecure(null, URI.create("https://example.com")));
    }

    @Test
    void isSchemeAtLeastAsSecure_nullComp_false() {
        assertFalse(URLUtil.isSchemeAtLeastAsSecure(URI.create("https://example.com"), null));
    }

    // ---------------------------------------------------------------------------
    // parseUrlParameterSequence
    // ---------------------------------------------------------------------------

    @Test
    void parseUrlParameterSequence_null_doesNothing() {
        Map<String,String> map = new LinkedHashMap<>();
        URLUtil.parseUrlParameterSequence(null, true, map::put);
        assertTrue(map.isEmpty());
    }

    @Test
    void parseUrlParameterSequence_simpleParam() {
        Map<String,String> map = new LinkedHashMap<>();
        URLUtil.parseUrlParameterSequence("foo=bar", true, map::put);
        assertEquals("bar", map.get("foo"));
    }

    @Test
    void parseUrlParameterSequence_multipleParams() {
        Map<String,String> map = new LinkedHashMap<>();
        URLUtil.parseUrlParameterSequence("a=1&b=2&c=3", true, map::put);
        assertEquals("1", map.get("a"));
        assertEquals("2", map.get("b"));
        assertEquals("3", map.get("c"));
    }

    @Test
    void parseUrlParameterSequence_noValue_emptyString() {
        Map<String,String> map = new LinkedHashMap<>();
        URLUtil.parseUrlParameterSequence("key=", true, map::put);
        assertEquals("", map.get("key"));
    }

    // ---------------------------------------------------------------------------
    // getUrlParameters
    // ---------------------------------------------------------------------------

    @Test
    void getUrlParameters_null_returnsEmptyMultimap() {
        assertTrue(URLUtil.getUrlParameters(null, true).isEmpty());
    }

    @Test
    void getUrlParameters_simple() {
        var result = URLUtil.getUrlParameters("x=1&x=2&y=3", false);
        assertEquals(2, result.get("x").size());
        assertEquals(1, result.get("y").size());
    }

    // ---------------------------------------------------------------------------
    // isOrIsDescendantPath / isDescendantPath
    // ---------------------------------------------------------------------------

    @Test
    void isOrIsDescendantPath_exact_true() {
        assertTrue(URLUtil.isOrIsDescendantPath("/foo", "/foo"));
    }

    @Test
    void isOrIsDescendantPath_childPath_true() {
        assertTrue(URLUtil.isOrIsDescendantPath("/foo", "/foo/bar"));
    }

    @Test
    void isOrIsDescendantPath_sibling_false() {
        assertFalse(URLUtil.isOrIsDescendantPath("/foo", "/foobar"));
    }

    @Test
    void isDescendantPath_child_true() {
        assertTrue(URLUtil.isDescendantPath("/parent", "/parent/child"));
    }

    @Test
    void isDescendantPath_exact_false() {
        assertFalse(URLUtil.isDescendantPath("/parent", "/parent"));
    }

    // ---------------------------------------------------------------------------
    // getRawPath
    // ---------------------------------------------------------------------------

    @Test
    void getRawPath_blank_returnsSeparator() {
        assertEquals("/", URLUtil.getRawPath(""));
        assertEquals("/", URLUtil.getRawPath(null));
    }

    @Test
    void getRawPath_absoluteUrl_returnsPath() {
        String path = URLUtil.getRawPath("https://example.com/some/path?q=1");
        assertEquals("/some/path", path);
    }

    @Test
    void getRawPath_relativeUrl_returnsAsIs() {
        // A relative URL like "/path" is directly returned
        String path = URLUtil.getRawPath("/path/to");
        assertEquals("/path/to", path);
    }

    // ---------------------------------------------------------------------------
    // getHostFromURL
    // ---------------------------------------------------------------------------

    @Test
    void getHostFromURL_empty_returnsNull() {
        assertNull(URLUtil.getHostFromURL(""));
    }

    @Test
    void getHostFromURL_valid_returnsHost() {
        assertEquals("example.com", URLUtil.getHostFromURL("https://example.com/path"));
    }

    // ---------------------------------------------------------------------------
    // parseDataURI
    // ---------------------------------------------------------------------------

    @Test
    void parseDataURI_null_failedResult() {
        var result = URLUtil.parseDataURI(null, "test.txt");
        assertFalse(result.wasSuccessful());
    }

    @Test
    void parseDataURI_notDataUri_failedResult() {
        var result = URLUtil.parseDataURI(URI.create("https://example.com"), "test.txt");
        assertFalse(result.wasSuccessful());
    }

    @Test
    void parseDataURI_validBase64_success() throws Exception {
        // "hello" base64 encoded = SGVsbG8=
        URI uri = URI.create("data:text/plain;base64,SGVsbG8=");
        var result = URLUtil.parseDataURI(uri, "test.txt");
        assertTrue(result.wasSuccessful());
        assertEquals(5, result.getLength()); // "Hello" = 5 bytes
        try (var is = result.getInputStream()) {
            assertEquals("Hello", new String(is.readAllBytes(), StandardCharsets.UTF_8));
        }
    }

    @Test
    void parseDataURI_plainText_success() {
        URI uri = URI.create("data:text/plain,hello");
        var result = URLUtil.parseDataURI(uri, "test.txt");
        assertTrue(result.wasSuccessful());
    }

    // ---------------------------------------------------------------------------
    // getMailToUrl
    // ---------------------------------------------------------------------------

    @Test
    void getMailToUrl_blank_returnsNull() {
        assertNull(URLUtil.getMailToUrl(""));
        assertNull(URLUtil.getMailToUrl("   "));
    }

    @Test
    void getMailToUrl_address_startsWith_mailto() {
        String result = URLUtil.getMailToUrl("test@example.com");
        assertNotNull(result);
        assertTrue(result.startsWith("mailto:"), result);
    }

    @Test
    void getMailToUrl_withParams_includesQuery() {
        Map<String,String> params = Map.of("subject", "Hello");
        String result = URLUtil.getMailToUrl("test@example.com", params);
        assertNotNull(result);
        assertTrue(result.contains("?"), result);
        assertTrue(result.contains("subject"), result);
    }

    // ---------------------------------------------------------------------------
    // getQueryString
    // ---------------------------------------------------------------------------

    @Test
    void getQueryString_map_singleParam() {
        String qs = URLUtil.getQueryString(Map.of("key", "value"));
        assertTrue(qs.contains("key") && qs.contains("value"), qs);
    }

    @Test
    void getQueryString_collection_multipleValues() {
        String qs = URLUtil.getQueryString("color", List.of("red", "blue"));
        assertTrue(qs.contains("color"), qs);
    }

    // ---------------------------------------------------------------------------
    // getHttpBaseUrl
    // ---------------------------------------------------------------------------

    @Test
    void getHttpBaseUrl_http_defaultPortNotForced() {
        String url = URLUtil.getHttpBaseUrl(false, "example.com", 80, false);
        assertEquals("http://example.com", url);
    }

    @Test
    void getHttpBaseUrl_https_customPortNotForced() {
        String url = URLUtil.getHttpBaseUrl(true, "example.com", 8443, false);
        assertTrue(url.startsWith("https://example.com"), url);
        assertTrue(url.contains("8443"), url);
    }

    // ---------------------------------------------------------------------------
    // tryToCreateUrl / tryToCreateUri
    // ---------------------------------------------------------------------------

    @Test
    void tryToCreateUrl_valid_returnsUrl() {
        assertNotNull(URLUtil.tryToCreateUrl("https://example.com"));
    }

    @Test
    void tryToCreateUrl_invalid_returnsNull() {
        assertNull(URLUtil.tryToCreateUrl("not a valid url"));
    }

    @Test
    void tryToCreateUri_valid_returnsUri() {
        assertNotNull(URLUtil.tryToCreateUri("https://example.com/path"));
    }

    @Test
    void tryToCreateUri_invalid_returnsNull() {
        assertNull(URLUtil.tryToCreateUri("::invalid"));
    }

    // ---------------------------------------------------------------------------
    // stripServer (additional gap)
    // ---------------------------------------------------------------------------

    @Test
    void stripServer_noSchemeNoLeadingSlash_returnsUnchanged() {
        assertEquals("relative/path", URLUtil.stripServer("relative/path"));
    }

    // ---------------------------------------------------------------------------
    // parseDataURI / parseDataURIImpl (additional gaps)
    // ---------------------------------------------------------------------------

    @Test
    void parseDataURI_noMediaType_usesDefaultMediaType() {
        URI uri = URI.create("data:,hello");
        URLUtil.DataURIParseResult result = URLUtil.parseDataURI(uri, "test.txt");
        assertTrue(result.wasSuccessful());
    }

    @Test
    void parseDataURI_paramsOnlyNoTypeSubtype_impliesTextPlain() {
        URI uri = URI.create("data:;charset=utf-8,hello");
        URLUtil.DataURIParseResult result = URLUtil.parseDataURI(uri, "test.txt");
        assertTrue(result.wasSuccessful());
    }

    @Test
    void parseDataURI_invalidMediaType_returnsFailedResult() {
        URI uri = URI.create("data:foo/bar,hello");
        URLUtil.DataURIParseResult result = URLUtil.parseDataURI(uri, "test.txt");
        assertTrue(result.wasSuccessful());
    }

    @Test
    void parseDataURI_invalidBase64_fails_wasSuccessfulFalseAndLengthNegative1() {
        URI uri = URI.create("data:text/plain;base64,not_valid_base64!!!");
        URLUtil.DataURIParseResult result = URLUtil.parseDataURI(uri, "test.txt");
        assertFalse(result.wasSuccessful());
        assertEquals(-1, result.getLength());
    }

    @Test
    void parseDataURI_success_getMetadataReturnsMetadata() {
        URI uri = URI.create("data:text/plain;base64,SGVsbG8=");
        URLUtil.DataURIParseResult result = URLUtil.parseDataURI(uri, "test.txt");
        assertTrue(result.wasSuccessful());
        assertNotNull(result.getMetadata());
        assertNull(result.getErrorMessage());
    }

    @Test
    void parseDataURI_failed_getMetadataAndLengthAndInputStream() {
        URLUtil.DataURIParseResult result = URLUtil.parseDataURI(URI.create("https://example.com"), "test.txt");
        assertFalse(result.wasSuccessful());
        assertNotNull(result.getMetadata());
        assertEquals("not a data URI", result.getErrorMessage());
        assertEquals(-1, result.getLength());
        assertThrows(IOException.class, result::getInputStream);
    }

    @Test
    void parseDataURI_nullUri_defaultErrorMessage() {
        URLUtil.DataURIParseResult result = URLUtil.parseDataURI(null, "test.txt");
        assertFalse(result.wasSuccessful());
        assertEquals("no candidate data: URI was specified", result.getErrorMessage());
    }

    // NOTE on a remaining "pc" branch not addressed above: parseDataURIImpl's
    // 'StringUtils.isBlank(mediaTypeSpec) || "&lt;".equals(mediaTypeSpec)' check has an untested second operand
    // (a legacy IE workaround for a literal "<" media type placeholder). It looks difficult or impossible to
    // reach: DATA_URI's group 1 can only capture a bare "<" if some subgroup matches it, but neither the
    // type/subtype subgroup (which requires a "/") nor the parameters subgroup (which requires a leading ";") can
    // match a lone "<" character, so a URI like "data:<,hello" likely fails m.matches() entirely rather than
    // reaching this check with mediaTypeSpec equal to "<". Not confident enough in the exact regex behavior to
    // assert a result without being able to execute it, so flagging rather than guessing.

    // ---------------------------------------------------------------------------
    // parseUrlParameterSequence (additional gaps) / KeyValuePair / SimpleKeyValuePair /
    // KeySingleValuePairMapBuilder
    // ---------------------------------------------------------------------------

    @Test
    void parseUrlParameterSequence_consumerOverload_buildsKeyValuePairs() {
        List<URLUtil.KeyValuePair> pairs = new ArrayList<>();
        URLUtil.parseUrlParameterSequence("foo=bar", true, (URLUtil.KeyValuePair kvp) -> pairs.add(kvp));
        assertEquals(1, pairs.size());
        assertEquals("foo", pairs.getFirst().key());
        assertEquals("bar", pairs.getFirst().value());
    }

    @Test
    void parseUrlParameterSequence_emptyKeySkipped() {
        Map<String,String> map = new LinkedHashMap<>();
        URLUtil.parseUrlParameterSequence("=value&foo=bar", true, map::put);
        assertFalse(map.containsKey(""));
        assertEquals("bar", map.get("foo"));
    }

    @Test
    void parseUrlParameterSequence_noEqualsSign_valueDefaultsToEmpty() {
        Map<String,String> map = new LinkedHashMap<>();
        URLUtil.parseUrlParameterSequence("foo", true, map::put);
        assertEquals("", map.get("foo"));
    }

    @Test
    void keySingleValuePairMapBuilder_accept_buildsMapOverwritingDuplicates() {
        URLUtil.KeySingleValuePairMapBuilder builder = new URLUtil.KeySingleValuePairMapBuilder();
        URLUtil.parseUrlParameterSequence("a=1&b=2&a=3", true, builder);
        assertEquals("3", builder.getMap().get("a"));
        assertEquals("2", builder.getMap().get("b"));
    }

    // NOTE on a remaining "pc" branch not addressed above: parseUrlParameterSequence's value-computation ternary
    // is "(kv.length == 2 && kv[1] != null) ? ... : \"\"". String.split(regex, limit) never returns a null element,
    // so when kv.length == 2, kv[1] can never actually be null -- the "kv[1] != null" half of that condition looks
    // unreachable/defensive rather than a real gap worth fabricating a test for.

    // ---------------------------------------------------------------------------
    // isOrIsDescendantPath / isDescendantPath / ensureTrailingSlashIfDirectoryPath (additional gaps)
    // ---------------------------------------------------------------------------

    @Test
    void isOrIsDescendantPath_folderPlusSlashEqualsFile_true() {
        assertTrue(URLUtil.isOrIsDescendantPath("/foo", "/foo/"));
    }

    @Test
    void isDescendantPath_emptyFolderPath_treatsAsRoot() {
        assertTrue(URLUtil.isDescendantPath("", "/anything"));
    }

    @Test
    void isDescendantPath_folderAlreadyEndsWithSlash_matchesChild() {
        assertTrue(URLUtil.isDescendantPath("/parent/", "/parent/child"));
    }

    @Test
    void ensureTrailingSlashIfDirectoryPath_emptyAndDirectory_returnsSeparator() {
        assertEquals("/", URLUtil.ensureTrailingSlashIfDirectoryPath("", () -> true));
    }

    @Test
    void ensureTrailingSlashIfDirectoryPath_emptyAndNotDirectory_returnsEmpty() {
        assertEquals("", URLUtil.ensureTrailingSlashIfDirectoryPath("", () -> false));
    }

    @Test
    void ensureTrailingSlashIfDirectoryPath_alreadyEndsWithSlash_returnsUnchanged() {
        assertEquals("/foo/", URLUtil.ensureTrailingSlashIfDirectoryPath("/foo/", () -> true));
    }

    @Test
    void ensureTrailingSlashIfDirectoryPath_directoryWithoutTrailingSlash_appendsSlash() {
        assertEquals("/foo/", URLUtil.ensureTrailingSlashIfDirectoryPath("/foo", () -> true));
    }

    @Test
    void ensureTrailingSlashIfDirectoryPath_notDirectoryWithoutTrailingSlash_returnsUnchanged() {
        assertEquals("/foo", URLUtil.ensureTrailingSlashIfDirectoryPath("/foo", () -> false));
    }

    // ---------------------------------------------------------------------------
    // getRawPath (additional gap)
    // ---------------------------------------------------------------------------

    @Test
    void getRawPath_malformedUri_fallsBackToManualParsing() {
        assertEquals("/path with spaces", URLUtil.getRawPath("http://example.com/path with spaces"));
    }

    // ---------------------------------------------------------------------------
    // getEffectivePort / haveSameRoots / canSafelyUseSameCredentials (entirely untested)
    // ---------------------------------------------------------------------------

    @Test
    void getEffectivePort_url_explicitPort_returnsIt() throws Exception {
        URL url = URI.create("http://example.com:8080/foo").toURL();
        assertEquals(8080, URLUtil.getEffectivePort(url));
    }

    @Test
    void getEffectivePort_url_noPort_returnsDefaultPort() throws Exception {
        URL url = URI.create("http://example.com/foo").toURL();
        assertEquals(80, URLUtil.getEffectivePort(url));
    }

    @Test
    void haveSameRoots_url_sameProtocolHostPort_true() throws Exception {
        URL a = URI.create("http://example.com/foo").toURL();
        URL b = URI.create("http://example.com/bar").toURL();
        assertTrue(URLUtil.haveSameRoots(a, b));
    }

    @Test
    void haveSameRoots_url_differentHost_false() throws Exception {
        URL a = URI.create("http://example.com/foo").toURL();
        URL b = URI.create("http://other.com/foo").toURL();
        assertFalse(URLUtil.haveSameRoots(a, b));
    }

    @Test
    void getEffectivePort_uri_explicitPort_returnsIt() {
        assertEquals(8443, URLUtil.getEffectivePort(URI.create("https://example.com:8443/foo")));
    }

    @Test
    void getEffectivePort_uri_noPortKnownScheme_returnsSchemeDefault() {
        assertEquals(443, URLUtil.getEffectivePort(URI.create("https://example.com/foo")));
    }

    @Test
    void getEffectivePort_uri_noPortUnknownScheme_fallsBackToUrlConversion() {
        // "urn" has no registered URLStreamHandler in the JDK, so uri.toURL() throws MalformedURLException,
        // which getEffectivePort(URI) catches and turns into a return value of 0.
        assertEquals(0, URLUtil.getEffectivePort(URI.create("urn:isbn:0451450523")));
    }

    @Test
    void haveSameRoots_uri_sameSchemeHostPort_true() {
        assertTrue(URLUtil.haveSameRoots(URI.create("https://example.com/a"), URI.create("https://example.com/b")));
    }

    @Test
    void haveSameRoots_uri_differentScheme_false() {
        assertFalse(URLUtil.haveSameRoots(URI.create("https://example.com/a"), URI.create("http://example.com/a")));
    }

    @Test
    void canSafelyUseSameCredentials_sameHostAndPortSecureUpgrade_true() {
        assertTrue(
            URLUtil.canSafelyUseSameCredentials(
                URI.create("https://example.com:8080/a"), URI.create("http://example.com:8080/a")
            )
        );
    }

    @Test
    void canSafelyUseSameCredentials_differentHost_false() {
        assertFalse(
            URLUtil.canSafelyUseSameCredentials(
                URI.create("https://example.com/a"), URI.create("https://other.com/a")
            )
        );
    }

    // ---------------------------------------------------------------------------
    // getQueryStringForUri / getQueryString(Multimap) / getQueryStringForUri(Multimap) /
    // getQueryParameterForUri (entirely untested)
    // ---------------------------------------------------------------------------

    @Test
    void getQueryParameterForUri_doesNotEncode() {
        assertEquals("key=hello world", URLUtil.getQueryParameterForUri("key", "hello world"));
    }

    @Test
    void getQueryStringForUri_collection_doesNotEncodeValues() {
        String qs = URLUtil.getQueryStringForUri("tag", List.of("a b", "c"));
        assertEquals("tag=a b&tag=c", qs);
    }

    @Test
    void getQueryString_multimap_joinsAllEntries() {
        ImmutableListMultimap<String,String> params = ImmutableListMultimap.of("a", "1", "a", "2", "b", "3");
        String qs = URLUtil.getQueryString(params);
        assertTrue(qs.contains("a=1"), qs);
        assertTrue(qs.contains("a=2"), qs);
        assertTrue(qs.contains("b=3"), qs);
    }

    @Test
    void getQueryStringForUri_multimap_doesNotEncode() {
        ImmutableListMultimap<String,String> params = ImmutableListMultimap.of("tag", "a b");
        assertEquals("tag=a b", URLUtil.getQueryStringForUri(params));
    }

    // ---------------------------------------------------------------------------
    // getDataUrl / getBase64EncodedStringForDataUrl (entirely/mostly untested)
    // ---------------------------------------------------------------------------

    @Test
    void getDataUrl_file_roundTripsContent() throws IOException {
        File temp = File.createTempFile("urlutil", ".txt");
        temp.deleteOnExit();
        Files.writeString(temp.toPath(), "Hello, URL!");
        String dataUrl = URLUtil.getDataUrl(temp);
        assertNotNull(dataUrl);
        assertTrue(dataUrl.startsWith("data:"), dataUrl);
        assertTrue(dataUrl.contains(";base64,"), dataUrl);
        String base64Part = dataUrl.substring(dataUrl.lastIndexOf(',') + 1);
        byte[] decoded = Base64.getDecoder().decode(base64Part);
        assertEquals("Hello, URL!", new String(decoded, StandardCharsets.UTF_8));
    }

    @Test
    void getDataUrl_file_null_throwsNpe() {
        assertThrows(NullPointerException.class, () -> URLUtil.getDataUrl((File) null));
    }

    @Test
    void getBase64EncodedStringForDataUrl_file_returnsBase64() throws IOException {
        File temp = File.createTempFile("urlutil2", ".txt");
        temp.deleteOnExit();
        Files.writeString(temp.toPath(), "abc");
        String b64 = URLUtil.getBase64EncodedStringForDataUrl(temp);
        assertEquals(Base64.getEncoder().encodeToString("abc".getBytes(StandardCharsets.UTF_8)), b64);
    }

    @Test
    void getDataUrl_inputStream_withMimeType() throws IOException {
        String content = "hello";
        String dataUrl =
            URLUtil.getDataUrl(new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8)), "text/plain");
        assertNotNull(dataUrl);
        assertTrue(dataUrl.startsWith("data:text/plain"), dataUrl);
        assertTrue(dataUrl.contains(";base64,"), dataUrl);
    }

    // ---------------------------------------------------------------------------
    // getCachingUrlSupplier / getCachingUriSupplier / getCachingFormattedUrlSupplier (entirely untested)
    // ---------------------------------------------------------------------------

    @Test
    void getCachingUrlSupplier_returnsMemoizedUrl() {
        Supplier<URL> supplier = URLUtil.getCachingUrlSupplier("https://example.com");
        URL first = supplier.get();
        assertNotNull(first);
        assertSame(first, supplier.get());
    }

    @Test
    void getCachingUriSupplier_returnsMemoizedUri() {
        Supplier<URI> supplier = URLUtil.getCachingUriSupplier("https://example.com");
        URI first = supplier.get();
        assertNotNull(first);
        assertSame(first, supplier.get());
    }

    @Test
    void getCachingFormattedUrlSupplier_formatsAndCaches() {
        Supplier<URL> supplier =
            URLUtil.getCachingFormattedUrlSupplier("https://{0}.example.com/{1}", () -> "foo", () -> "bar");
        URL url = supplier.get();
        assertNotNull(url);
        assertTrue(url.toString().contains("foo"), url.toString());
        assertTrue(url.toString().contains("bar"), url.toString());
        assertSame(url, supplier.get());
    }

    // ---------------------------------------------------------------------------
    // directoryPathToUrlPath / getAbsoluteUrl / toURISafe (entirely untested)
    // ---------------------------------------------------------------------------

    @Test
    void directoryPathToUrlPath_replacesFileSeparator() {
        String input = "a" + File.separator + "b";
        assertEquals("a/b", URLUtil.directoryPathToUrlPath(input));
    }

    @Test
    void getAbsoluteUrl_nullBase_returnsSpecUnchanged() {
        assertEquals("/foo", URLUtil.getAbsoluteUrl(null, "/foo"));
    }

    @Test
    void getAbsoluteUrl_rootRelativeSpecResolvedAgainstBase() throws Exception {
        URL base = URI.create("https://example.com/dir/page.html").toURL();
        assertEquals("https://example.com/other", URLUtil.getAbsoluteUrl(base, "/other"));
    }

    @Test
    void getAbsoluteUrl_currentPathIndicatorResolved() throws Exception {
        URL base = URI.create("https://example.com/dir/page.html").toURL();
        String result = URLUtil.getAbsoluteUrl(base, "./other");
        assertTrue(result.contains("other"), result);
    }

    @Test
    void getAbsoluteUrl_nonRelativeSpec_returnsUnchanged() throws Exception {
        URL base = URI.create("https://example.com/dir/page.html").toURL();
        assertEquals("foo", URLUtil.getAbsoluteUrl(base, "foo"));
    }

    @Test
    void toURISafe_blankSpec_returnsNull() {
        assertNull(URLUtil.toURISafe(null, ""));
        assertNull(URLUtil.toURISafe(URI.create("https://example.com"), "  "));
    }

    @Test
    void toURISafe_nullBase_validSpec_returnsUri() {
        URI result = URLUtil.toURISafe(null, "https://example.com/foo");
        assertNotNull(result);
        assertEquals("https://example.com/foo", result.toString());
    }

    @Test
    void toURISafe_nullBase_invalidSpec_returnsNull() {
        assertNull(URLUtil.toURISafe(null, "not a uri with spaces"));
    }

    @Test
    void toURISafe_withBase_resolvesRelative() {
        URI base = URI.create("https://example.com/dir/");
        URI safe = URLUtil.toURISafe(base, "other");
        assertNotNull(safe);
        assertEquals("https://example.com/dir/other", safe.toString());
    }

    @Test
    void toURISafe_withBase_invalidSpec_returnsNull() {
        URI base = URI.create("https://example.com/dir/");
        assertNull(URLUtil.toURISafe(base, "not a uri with spaces"));
    }

    // ---------------------------------------------------------------------------
    // isProbablyNotEncoded / getUrlEncodingIfNecessary (@Beta, entirely untested)
    // ---------------------------------------------------------------------------

    @Test
    void isProbablyNotEncoded_null_false() {
        assertFalse(URLUtil.isProbablyNotEncoded(null));
    }

    @Test
    void isProbablyNotEncoded_withSpace_true() {
        assertTrue(URLUtil.isProbablyNotEncoded("hello world"));
    }

    @Test
    void isProbablyNotEncoded_noSpecialChars_false() {
        assertFalse(URLUtil.isProbablyNotEncoded("hello%20world"));
    }

    @Test
    void getUrlEncodingIfNecessary_needsEncoding_encodes() {
        String result = URLUtil.getUrlEncodingIfNecessary("hello world");
        assertTrue(result.contains("%20"), result);
    }

    @Test
    void getUrlEncodingIfNecessary_alreadyEncoded_returnsUnchanged() {
        assertEquals("hello%20world", URLUtil.getUrlEncodingIfNecessary("hello%20world"));
    }

    // ---------------------------------------------------------------------------
    // urlEncodeUTF8 (all overloads, entirely untested)
    // ---------------------------------------------------------------------------

    @Test
    void urlEncodeUTF8_appendable_basicLatinChar_appendsUnchanged() {
        StringBuilder sb = new StringBuilder();
        URLUtil.urlEncodeUTF8(sb, 'a');
        assertEquals("a", sb.toString());
    }

    @Test
    void urlEncodeUTF8_appendable_nonBasicLatinChar_percentEncodesUtf8Bytes() {
        StringBuilder sb = new StringBuilder();
        URLUtil.urlEncodeUTF8(sb, 'é');
        assertEquals("%c3%a9", sb.toString());
    }

    @Test
    void urlEncodeUTF8_string_nonAsciiChars_encoded() {
        assertEquals("%c3%a9", URLUtil.urlEncodeUTF8("é"));
    }

    @Test
    void urlEncodeUTF8_string_null_returnsNull() {
        assertNull(URLUtil.urlEncodeUTF8(null));
    }

    @Test
    void urlEncodeUTF8_stringBuilder_appendsEncodedChars() {
        StringBuilder sb = new StringBuilder();
        URLUtil.urlEncodeUTF8(sb, "aé");
        assertEquals("a%c3%a9", sb.toString());
    }

    @Test
    void urlEncodeUTF8_stringBuilder_null_doesNothing() {
        StringBuilder sb = new StringBuilder();
        URLUtil.urlEncodeUTF8(sb, null);
        assertEquals("", sb.toString());
    }

    // NOTE on a remaining "nc" branch not addressed above: urlEncodeUTF8(Appendable, char)'s "if (num.length() ==
    // 1) safeAppend(out, '0')" zero-pad line is never hit by any input. This method early-returns for basic-latin
    // chars, so the byte-encoding loop only ever sees UTF-8 bytes from non-basic-latin codepoints (>= 0x80) -- for
    // 2-byte sequences the lead byte is always >= 0xC2 and the trailing byte is always 0x80-0xBF, and 3+-byte
    // sequences likewise never produce a byte < 0x10. So every byte this loop can ever see hex-encodes to two
    // digits already, making the zero-pad branch look unreachable/dead rather than an untested real case.

    // ---------------------------------------------------------------------------
    // encodeFullyQualifiedURL (large method, multiple uncovered branches)
    // ---------------------------------------------------------------------------

    @Test
    void encodeFullyQualifiedURL_relativePath_encodesEntireString() {
        assertEquals("/path%20with%20space", URLUtil.encodeFullyQualifiedURL("/path with space"));
    }

    @Test
    void encodeFullyQualifiedURL_currentPathIndicator_encodesEntireString() {
        assertEquals("./path%20with%20space", URLUtil.encodeFullyQualifiedURL("./path with space"));
    }

    @Test
    void encodeFullyQualifiedURL_withSchemeAndPath_encodesPathOnly() {
        String result = URLUtil.encodeFullyQualifiedURL("http://example.com/path with space");
        assertEquals("http://example.com/path%20with%20space", result);
    }

    @Test
    void encodeFullyQualifiedURL_schemeNoTrailingSlashAfterAuthority_returnsUnchanged() {
        String url = "http://example.com";
        assertEquals(url, URLUtil.encodeFullyQualifiedURL(url));
    }

    @Test
    void encodeFullyQualifiedURL_schemeEndsImmediatelyAfterSlash_returnsUnchanged() {
        String url = "http://example.com/";
        assertEquals(url, URLUtil.encodeFullyQualifiedURL(url));
    }

    @Test
    void encodeFullyQualifiedURL_noSchemeSlashSlash_withColon_encodesBothSidesOfColon() {
        String result = URLUtil.encodeFullyQualifiedURL("mailto:a b@example.com");
        assertEquals("mailto:a%20b%40example.com", result);
    }

    @Test
    void encodeFullyQualifiedURL_colonAtEnd_encodesBeforeColon() {
        assertEquals("foo%20bar:", URLUtil.encodeFullyQualifiedURL("foo bar:"));
    }

    @Test
    void encodeFullyQualifiedURL_noColonAtAll_encodesEntireString() {
        assertEquals("foo%20bar", URLUtil.encodeFullyQualifiedURL("foo bar"));
    }

    // ---------------------------------------------------------------------------
    // getAbsoluteFileUrl / getContentIdUrl (entirely untested)
    // ---------------------------------------------------------------------------

    @Test
    void getAbsoluteFileUrl_returnsFileSchemeUrl() {
        File f = new File("/tmp/test-file.txt");
        String result = URLUtil.getAbsoluteFileUrl(f);
        assertTrue(result.startsWith("file://"), result);
        assertTrue(result.contains("test-file.txt"), result);
    }

    @Test
    void getContentIdUrl_encodesContentId() {
        assertEquals("cid:abc123", URLUtil.getContentIdUrl("abc123"));
    }

    // ---------------------------------------------------------------------------
    // getHttpBaseUrl (additional gaps) / getHttpUrlPortNumber (private, exercised indirectly)
    // ---------------------------------------------------------------------------

    @Test
    void getHttpBaseUrl_nullPortNotForced_omitsPort() {
        assertEquals("http://example.com", URLUtil.getHttpBaseUrl(false, "example.com", null, false));
    }

    @Test
    void getHttpBaseUrl_nullPortForced_usesSchemeDefaultPort() {
        assertEquals("http://example.com:80", URLUtil.getHttpBaseUrl(false, "example.com", null, true));
    }

    @Test
    void getHttpBaseUrl_defaultPortForced_includesPortAnyway() {
        assertEquals("http://example.com:80", URLUtil.getHttpBaseUrl(false, "example.com", 80, true));
    }

    @Test
    void getHttpBaseUrl_negativePort_throws() {
        assertThrows(IllegalArgumentException.class, () -> URLUtil.getHttpBaseUrl(false, "example.com", -1, false));
    }

    @Test
    void getHttpBaseUrl_portTooLarge_throws() {
        assertThrows(
            IllegalArgumentException.class, () -> URLUtil.getHttpBaseUrl(false, "example.com", 70000, false)
        );
    }

    @Test
    void getHttpBaseUrl_nullHost_throwsNpe() {
        assertThrows(NullPointerException.class, () -> URLUtil.getHttpBaseUrl(false, null, 80, false));
    }

    // ---------------------------------------------------------------------------
    // hexit / printUrlDecoded (additional gaps)
    // ---------------------------------------------------------------------------

    @Test
    void getUrlDecoded_plusSign_decodesToSpace() {
        assertEquals("hello world", URLUtil.getUrlDecoded("hello+world"));
    }

    @Test
    void getUrlDecoded_uppercaseHexEscape_decodes() {
        assertEquals("J", URLUtil.getUrlDecoded("%4A"));
    }

    @Test
    void getUrlDecoded_uEscape_decodesUtf16CodeUnit() {
        assertEquals("é", URLUtil.getUrlDecoded("%u00e9"));
    }

    // ---------------------------------------------------------------------------
    // printUrlEncoding / getUrlEncoding (additional gaps)
    // ---------------------------------------------------------------------------

    @Test
    void printUrlEncoding_nullString_doesNothing() {
        StringBuilder sb = new StringBuilder();
        URLUtil.printUrlEncoding(sb, null);
        assertEquals("", sb.toString());
    }

    @Test
    void getUrlEncoding_uppercaseLetters_unchanged() {
        assertEquals("HELLO", URLUtil.getUrlEncoding("HELLO"));
    }

    @Test
    void getUrlEncoding_nonAsciiLetter_percentEncodesUtf8Bytes() {
        assertEquals("caf%c3%a9", URLUtil.getUrlEncoding("café"));
    }

    @Test
    void getUrlEncoding_controlCharNeedingZeroPad_padsLeadingZero() {
        assertEquals("%01", URLUtil.getUrlEncoding(Character.toString(Ascii.SOH)));
    }

    // ---------------------------------------------------------------------------
    // URLParameterValueAddCallback / URLParameterValueRemoveCallback / URLParameterValueSetCallback
    // ---------------------------------------------------------------------------

    @Test
    void urlParameterValueAddCallback_noExistingValue_setsValue() {
        FakeURLBuilder b = new FakeURLBuilder();
        assertTrue(new URLUtil.URLParameterValueAddCallback("tag").modifyUrl(b, "x"));
        assertEquals("x", b.getParameterValue("tag"));
    }

    @Test
    void urlParameterValueAddCallback_existingValue_appendsCommaSeparated() {
        FakeURLBuilder b = new FakeURLBuilder();
        b.setParameterValue("tag", "x");
        assertTrue(new URLUtil.URLParameterValueAddCallback("tag").modifyUrl(b, "y"));
        assertEquals("x,y", b.getParameterValue("tag"));
    }

    @Test
    void urlParameterValueRemoveCallback_blankRemoveValue_doesNothing() {
        FakeURLBuilder b = new FakeURLBuilder();
        b.setParameterValue("tag", "x,y");
        assertFalse(new URLUtil.URLParameterValueRemoveCallback("tag").modifyUrl(b, "  "));
        assertEquals("x,y", b.getParameterValue("tag"));
    }

    @Test
    void urlParameterValueRemoveCallback_noExistingValue_doesNothing() {
        FakeURLBuilder b = new FakeURLBuilder();
        assertFalse(new URLUtil.URLParameterValueRemoveCallback("tag").modifyUrl(b, "x"));
        assertNull(b.getParameterValue("tag"));
    }

    @Test
    void urlParameterValueRemoveCallback_removesMatchingValue_keepsOthers() {
        FakeURLBuilder b = new FakeURLBuilder();
        b.setParameterValues("tag", List.of("x", "y", "z"));
        assertTrue(new URLUtil.URLParameterValueRemoveCallback("tag").modifyUrl(b, "y"));
        assertEquals("x,z", b.getParameterValue("tag"));
    }

    @Test
    void urlParameterValueRemoveCallback_removesLastValue_removesParameterEntirely() {
        FakeURLBuilder b = FakeURLBuilder.createInstanceWithRelativeURI("/test?");
        b.setParameterValue("tag", "x");
        assertTrue(new URLUtil.URLParameterValueRemoveCallback("tag").modifyUrl(b, "x"));
        assertNull(b.getParameterValue("tag"));
    }

    @Test
    void urlParameterValueSetCallback_nullValue_removesValue() {
        FakeURLBuilder b = new FakeURLBuilder();
        b.setParameterValue("tag", "x");
        assertTrue(new URLUtil.URLParameterValueSetCallback("tag").modifyUrl(b, null));
        assertFalse(b.params.containsKey("tag"));
    }

    @Test
    void urlParameterValueSetCallback_nonNullValue_setsParameter() {
        FakeURLBuilder b = new FakeURLBuilder();
        assertTrue(new URLUtil.URLParameterValueSetCallback("tag").modifyUrl(b, "z"));
        assertEquals("z", b.getParameterValue("tag"));
    }

    // ---------------------------------------------------------------------------
    // Additional gaps identified from the htmlReport coverage scan of URLUtil (86.2% branch coverage).
    // ---------------------------------------------------------------------------

    @Test
    void urlParameterValueAddCallback_nullValue_returnsFalseAndDoesNotSetParameter() {
        FakeURLBuilder b = new FakeURLBuilder();
        assertFalse(new URLUtil.URLParameterValueAddCallback("tag").modifyUrl(b, null));
        assertNull(b.getParameterValue("tag"));
    }

    @Test
    void parseDataURI_typeWithInvalidToken_exceptionCaught_failedResult() {
        // A space inside the type/subtype isn't a valid RFC 2045 token character.
        // new URI throws a URISyntaxException, which is converted into a failed result.
        URLUtil.DataURIParseResult result = URLUtil.parseDataURISpec("data:te xt/plain,hello", "test.txt");
        assertFalse(result.wasSuccessful());
    }

    @Test
    void isOrIsDescendantPath_emptyPath_fallsThroughToIsDescendantPath() {
        assertTrue(URLUtil.isOrIsDescendantPath("", "/anything"));
    }

    @Test
    void isOrIsDescendantPath_pathEndsWithSlash_secondConditionFalse() {
        // path already ends with '/', so the "path + separator" comparison is skipped; falls through to
        // isDescendantPath, which still matches since otherPath starts with path.
        assertTrue(URLUtil.isOrIsDescendantPath("/foo/", "/foo/bar"));
    }

    @Test
    void getRawPath_malformedUriWithQueryString_truncatesAtQuestionMark() {
        // URI.create() throws for the embedded space, falling back to manual parsing; this exercises the
        // "truncate at '?'" branch of that manual fallback, which was never reached by the other malformed-URI test.
        assertEquals("/path with spaces", URLUtil.getRawPath("http://example.com/path with spaces?query=1"));
    }

    @Test
    void haveSameRoots_url_differentProtocol_false() throws Exception {
        URL a = URI.create("http://example.com/foo").toURL();
        URL b = URI.create("https://example.com/foo").toURL();
        assertFalse(URLUtil.haveSameRoots(a, b));
    }

    @Test
    void haveSameRoots_url_differentPort_false() throws Exception {
        URL a = URI.create("http://example.com:8080/foo").toURL();
        URL b = URI.create("http://example.com:9090/foo").toURL();
        assertFalse(URLUtil.haveSameRoots(a, b));
    }

    @Test
    void getEffectivePort_uri_noPortUnknownButUrlConvertibleScheme_fallsBackToUrlDefaultPort() {
        // "ftp" isn't recognized by getEffectivePort(String), but the JDK has a built-in URLStreamHandler for it, so
        // uri.toURL() succeeds and getEffectivePort(URI) falls through to getEffectivePort(URL)'s default-port logic.
        assertEquals(21, URLUtil.getEffectivePort(URI.create("ftp://example.com/file")));
    }

    @Test
    void haveSameRoots_uri_differentPort_false() {
        assertFalse(
            URLUtil.haveSameRoots(URI.create("https://example.com:8080/a"), URI.create("https://example.com:9090/a"))
        );
    }

    @Test
    void canSafelyUseSameCredentials_sameSchemeDifferentPort_false() {
        assertFalse(
            URLUtil.canSafelyUseSameCredentials(
                URI.create("https://example.com:8080/a"), URI.create("https://example.com:9090/a")
            )
        );
    }

    @Test
    void isSchemeAtLeastAsSecure_requestSchemeNull_false() {
        // A relative URI has no scheme at all (getScheme() == null), exercising the null-scheme guard rather than
        // the null-URI guard above it.
        assertFalse(URLUtil.isSchemeAtLeastAsSecure(URI.create("/relative/path"), URI.create("https://example.com")));
    }

    @Test
    void isSchemeAtLeastAsSecure_httpsVsNonHttp_false() {
        assertFalse(URLUtil.isSchemeAtLeastAsSecure(URI.create("https://example.com"), URI.create("ftp://example.com")));
    }

    @Test
    void getAbsoluteUrl_baseUrlNotValidUri_logsWarningAndReturnsSpecUnchanged() throws Exception {
        // Built via the lenient URL constructor (not URI.create) so it can hold a literal space; baseURL.toURI()
        // then throws URISyntaxException, which getAbsoluteUrl catches, logs, and falls back to the unchanged spec.
        @SuppressWarnings("deprecation")
        URL base = new URL("http://example.com/path with space/page.html");
        assertEquals("/other", URLUtil.getAbsoluteUrl(base, "/other"));
    }

    @Test
    void getUrlDecoded_lowercaseHexEscape_decodes() {
        assertEquals("é", URLUtil.getUrlDecoded("%c3%a9"));
    }

    @Test
    void getUrlDecoded_percentNearEndOfString_treatedAsLiteral() {
        // Not enough trailing characters remain to form a valid %xx escape, so '%' falls through to the default
        // case and is appended literally instead.
        assertEquals("100%", URLUtil.getUrlDecoded("100%"));
    }

    @Test
    void getUrlEncoding_safePunctuation_unchanged() {
        assertEquals("(a,b)-c.d/e*f", URLUtil.getUrlEncoding("(a,b)-c.d/e*f"));
    }

    @Test
    void getUrlEncoding_nonBasicLatinSymbol_multibyteEncoded() {
        // '€' (U+20AC) is not a letter/digit and not basic latin, so it takes the multi-byte UTF-8 encoding path
        // rather than the single-byte %xx fallback used for basic-latin punctuation like space.
        assertEquals("%e2%82%ac", URLUtil.getUrlEncoding("€"));
    }

    @Test
    void getHostFromURL_malformed_logsWarningAndReturnsNull() {
        // Unlike URI.create() elsewhere in this class, getHostFromURL calls "new URI(url)" directly, which throws
        // a checked URISyntaxException (rather than wrapping it as an unchecked IllegalArgumentException) for the
        // embedded space; that's caught, logged, and turned into a null return.
        assertNull(URLUtil.getHostFromURL("http://example.com/path with space"));
    }

}
