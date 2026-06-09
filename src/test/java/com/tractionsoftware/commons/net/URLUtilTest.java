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

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class URLUtilTest {

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
    // getEffectivePort(String)
    // ---------------------------------------------------------------------------

    @Test
    void getEffectivePort_http_returns80() {
        assertEquals(80, URLUtil.getEffectivePort("http"));
        assertEquals(80, URLUtil.getEffectivePort("HTTP"));
    }

    @Test
    void getEffectivePort_https_returns443() {
        assertEquals(443, URLUtil.getEffectivePort("https"));
    }

    @Test
    void getEffectivePort_unknown_returnsMinusOne() {
        assertEquals(-1, URLUtil.getEffectivePort("ftp"));
        assertEquals(-1, URLUtil.getEffectivePort((String) null));
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
    // getPath
    // ---------------------------------------------------------------------------

    @Test
    void getPath_blank_returnsSeparator() {
        assertEquals("/", URLUtil.getPath(""));
        assertEquals("/", URLUtil.getPath(null));
    }

    @Test
    void getPath_absoluteUrl_returnsPath() {
        String path = URLUtil.getPath("https://example.com/some/path?q=1");
        assertEquals("/some/path", path);
    }

    @Test
    void getPath_relativeUrl_returnsAsIs() {
        // A relative URL like "/path" is directly returned
        String path = URLUtil.getPath("/path/to");
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
    void parseDataURI_plainText_success() throws Exception {
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
    // getRsFromUrl
    // ---------------------------------------------------------------------------

    @Test
    void getRsFromUrl_null_returnsNull() {
        assertNull(URLUtil.getRsFromUrl(null));
    }

    @Test
    void getRsFromUrl_noRs_returnsNull() {
        assertNull(URLUtil.getRsFromUrl("/some/other/path"));
    }

    @Test
    void getRsFromUrl_rsPathPrefix() {
        assertEquals("cdt", URLUtil.getRsFromUrl("/rs/cdt"));
    }

    @Test
    void getRsFromUrl_tractionRsPathPrefix() {
        assertEquals("cdt", URLUtil.getRsFromUrl("/traction/rs/cdt"));
    }

    @Test
    void getRsFromUrl_tractionRsQueryParam() {
        assertEquals("cdt", URLUtil.getRsFromUrl("/traction/rs?cdt"));
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

}
