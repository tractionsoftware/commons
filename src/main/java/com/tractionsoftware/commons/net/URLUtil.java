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

import com.google.common.annotations.Beta;
import com.google.common.base.CharMatcher;
import com.google.common.base.Splitter;
import com.google.common.base.Suppliers;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import com.google.common.net.MediaType;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import com.tractionsoftware.commons.codec.Base64Util;
import com.tractionsoftware.commons.io.*;
import com.tractionsoftware.commons.lang.EnhancedCharSequence;
import com.tractionsoftware.commons.lang.StringUtil;
import com.tractionsoftware.commons.util.CollectionUtil;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.text.MessageFormat;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * URL modification or processing utilities.
 *
 * @author Dave Shepperton
 */
public final class URLUtil {

    /**
     * Not instantiable.
     */
    private URLUtil() {
    }

    private static final Logger LOGGER = LoggerFactory.getLogger(URLUtil.class);

    public static final String SCHEME_NAME_HTTP = "http";

    public static final String SCHEME_NAME_HTTPS = "https";

    public static final String SCHEME_NAME_TRACTION = "traction";

    public static final String SCHEME_NAME_DATA = "data";

    public static final String SCHEME_NAME_CID = "cid";

    public static final String SCHEME_NAME_FILE = "file";

    public static final String SCHEME_NAME_MAILTO = "mailto";

    public static final String SCHEME_QUALIFIER = ":";

    public static final char SCHEME_QUALIFIER_CHAR = ':';

    public static final String SCHEME_RELATIVE_PREFIX = SCHEME_QUALIFIER_CHAR + "//";

    public static final String SCHEME_PREFIX_HTTP = SCHEME_NAME_HTTP + SCHEME_QUALIFIER_CHAR;

    public static final String SCHEME_PREFIX_HTTPS = SCHEME_NAME_HTTPS + SCHEME_QUALIFIER_CHAR;

    public static final String SCHEME_PREFIX_TRACTION = SCHEME_NAME_TRACTION + SCHEME_QUALIFIER_CHAR;

    public static final String SCHEME_PREFIX_DATA = SCHEME_NAME_DATA + SCHEME_QUALIFIER_CHAR;

    public static final String SCHEME_PREFIX_CID = SCHEME_NAME_CID + SCHEME_QUALIFIER_CHAR;

    public static final String SCHEME_PREFIX_FILE = SCHEME_NAME_FILE + SCHEME_QUALIFIER_CHAR;

    public static final String SCHEME_PREFIX_MAILTO = SCHEME_NAME_MAILTO + SCHEME_QUALIFIER_CHAR;

    public static final String DATA_URI_BASE_64_PARAMETER = "base64";

    public static final int DEFAULT_PORT_HTTP = 80;

    public static final int DEFAULT_PORT_HTTPS = 443;

    public static final String USERINFO_SEPARATOR = ":";

    public static final char USERINFO_SEPARATOR_CHAR = ':';

    public static final String PORT_QUALIFIER = ":";

    public static final char PORT_QUALIFIER_CHAR = ':';

    public static final String USERINFO_MARKER = "@";

    public static final String AUTHORITY_MARKER = "//";

    public static final String PROTOCOL_QUALIFIER_AND_AUTHORITY_MARKER = SCHEME_QUALIFIER + AUTHORITY_MARKER;

    private static final CharMatcher CHARS_USUALLY_NEED_ENCODNG = CharMatcher.anyOf(" \t\r\n");

    public static final Splitter PATH_PARTS = Splitter.on(URLUtil.PATH_SEPARATOR_CHAR);

    /**
     * <a href="http://en.wikipedia.org/wiki/Data_URI_scheme">Data URI scheme</a>
     *
     * <p>
     * The encoding is indicated by ";base64". If it is present the data is encoded as base64. Without it the data (as a
     * sequence of octets) is represented using ASCII encoding for octets inside the range of safe URL characters[5] and
     * using the standard %xx hex encoding of URLs for octets outside that range. If <MIME-type> is omitted, it defaults
     * to text/plain;charset=US-ASCII. (As a shorthand, the type can be omitted but the charset parameter supplied.)
     *
     * <p>
     * Capture group 1 is the optional media type. Capture group 2 is the main type. Capture group 3 contains the set of
     * any optional parameters (group 4 contains individual parameters). Capture group 5 contains the optional base64
     * encoding marker. Capture group 6 contains the data.
     */
    // data:[<media type>][;base64],<data>
    public static final Pattern DATA_URI =
        Pattern.compile(// optional leading whitespace
            "\\s*" +
            // data: scheme name
            SCHEME_PREFIX_DATA +
            // Group 1: media type
            "(" +
            // Group 2: type/subtype
            "([^;/]+/[^;]+)?" +
            // Group 3+4: parameters
            "((;[^;=]+=[^;=]+)+)?" +
            ")?" +
            // Group 5: base64 indicator
            "(;" + DATA_URI_BASE_64_PARAMETER + ")?" +
            // Comma delimits URI path from data
            "," +
            // Group 6: data
            "(.*)");

    public static final String HOST_PART_SEPARATOR = ".";

    public static final char HOST_PART_SEPARATOR_CHAR = '.';

    public static final String PATH_SEPARATOR = "/";

    public static final char PATH_SEPARATOR_CHAR = '/';

    public static final String QUERY_MARKER = "?";

    public static final char QUERY_MARKER_CHAR = '?';

    public static final String CURRENT_PATH_INDICATOR = ".";

    public static final char CURRENT_PATH_INDICATOR_CHAR = '.';

    public static final String PARAMETER_SET = "=";

    public static final char PARAMETER_SET_CHAR = '=';

    public static final String PARAMETER_DELIMITER = "&";

    public static final char PARAMETER_DELIMITER_CHAR = '&';

    public static final String ANCHOR_MARKER = "#";

    public static final char ANCHOR_MARKER_CHAR = '#';

    private static final MediaType DATA_URI_DEFAULT_MEDIA_TYPE =
        MediaType.PLAIN_TEXT_UTF_8.withCharset(StandardCharsets.US_ASCII);

    private static final ImmutableList<String> RS_URL_PREFIXES = ImmutableList.of(
        "/rs", "/traction/rs", "/traction/permalink"
    );

    private static final CharMatcher RS_URL_NEXT_CHAR_MATCHER = CharMatcher.anyOf(
        EnhancedCharSequence.getInstance(new char[] { PATH_SEPARATOR_CHAR, QUERY_MARKER_CHAR })
    );

    public static interface KeyValuePair {

        public String key();

        public String value();

    }

    public static final record SimpleKeyValuePair(@Nonnull String key, @Nonnull String value) implements KeyValuePair {

        public SimpleKeyValuePair {
            Objects.requireNonNull(key, "key");
            Objects.requireNonNull(value, "value");
        }

    }

    @FunctionalInterface
    public static interface URLModifierCallback<T> {

        @CanIgnoreReturnValue
        public boolean modifyUrl(@Nonnull URLBuilder<?> url, T value);

    }

    /**
     * Represents the result of an attempt to interpret a String as a data: URI.
     */
    @Beta
    public static interface DataURIParseResult {

        public boolean wasSuccessful();

        public FileMetadata getMetadata();

        public long getLength();

        public InputStream getInputStream() throws IOException;

        public String getErrorMessage();

    }

    /**
     * This {@link Consumer} for KeyValuePairs builds a String-to-String map for each KeyValuePair. When there is a
     * collision, the existing pair is overwritten (thus "single value").
     *
     * @author Dave Shepperton
     */
    public static final class KeySingleValuePairMapBuilder implements Consumer<KeyValuePair> {

        private final Map<String,String> map = new LinkedHashMap<>();

        @Override
        public final void accept(KeyValuePair pair) {
            map.put(pair.key(), pair.value());
        }

        public final Map<String,String> getMap() {
            return map;
        }

    }

    public static abstract class AbstractURLModifierCallback implements URLModifierCallback<String> {

        protected final String urlParameterName;

        public AbstractURLModifierCallback(String urlParameterName) {
            Objects.requireNonNull(urlParameterName, "URL parameter name");
            this.urlParameterName = urlParameterName;
        }

    }

    public static final class URLParameterValueAddCallback extends AbstractURLModifierCallback {

        public URLParameterValueAddCallback(String urlParameterName) {
            super(urlParameterName);
        }

        @Override
        public final boolean modifyUrl(@Nonnull URLBuilder<?> url, @Nullable String addValue) {
            if (addValue == null) {
                return false;
            }
            url.addParameterValue(urlParameterName, addValue);
            return true;
        }

    }

    public static final class URLParameterValueRemoveCallback extends AbstractURLModifierCallback {

        public URLParameterValueRemoveCallback(String urlParameterName) {
            super(urlParameterName);
        }

        @Override
        public final boolean modifyUrl(@Nonnull URLBuilder<?> url, @Nullable String removeValue) {
            return url.removeParameterValue(urlParameterName, removeValue);
        }

    }

    public static final class URLParameterValueSetCallback extends AbstractURLModifierCallback {

        public URLParameterValueSetCallback(String urlParameterName) {
            super(urlParameterName);
        }

        @Override
        public final boolean modifyUrl(@Nonnull URLBuilder<?> url, String setValue) {
            if (setValue == null) {
                return url.removeParameter(urlParameterName);
            }
            url.setParameterValue(urlParameterName, setValue);
            return true;
        }

    }

    private static final class SuccessfulDataURIParseResult implements DataURIParseResult {

        private final FileMetadata metadata;

        private final byte[] data;

        private SuccessfulDataURIParseResult(FileMetadata metadata, byte[] data) {
            this.metadata = metadata;
            this.data = data;
        }

        @Override
        public final boolean wasSuccessful() {
            return true;
        }

        @Override
        public final long getLength() {
            return data.length;
        }

        @Override
        public final FileMetadata getMetadata() {
            return metadata.toReadOnly();
        }

        @Override
        public final InputStream getInputStream() {
            return new ByteArrayInputStream(data);
        }

        @Override
        public final String getErrorMessage() {
            return null;
        }

    }

    private static final class FailedDataURIParseResult implements DataURIParseResult {

        private final String fileName;

        private final String errorMessage;

        private FailedDataURIParseResult(String fileName, String errorMessage) {
            this.fileName = fileName;
            this.errorMessage = errorMessage;
        }

        @Override
        public final boolean wasSuccessful() {
            return false;
        }

        @Override
        public final FileMetadata getMetadata() {
            SimpleMutableFileMetadata metadata = SimpleMutableFileMetadata.createFromFileName(fileName);
            metadata.setErrorMessage(getErrorMessage());
            return metadata;
        }

        @Override
        public final long getLength() {
            return -1;
        }

        @Override
        public final InputStream getInputStream() throws IOException {
            throw new IOException(getErrorMessage());
        }

        @Override
        public final String getErrorMessage() {
            return Objects.toString(errorMessage, "data URI parsing failed");
        }

    }

    /**
     * Returns the URL specified without a server, always starting with a "/".
     */
    public static final String stripServer(String url) {
        return stripServer(url, null);
    }

    /**
     * Returns the url specified without a server, always starting with a /
     *
     * @param server
     *     the server will be appended to this buffer
     */
    public static final String stripServer(String url, StringBuilder server) {
        if (url == null) {
            return null;
        }
        if (url.startsWith(PATH_SEPARATOR)) {
            return url;
        }
        int colonSlashSlash = url.indexOf(SCHEME_RELATIVE_PREFIX);
        if (colonSlashSlash == -1) {
            return url;
        }
        int slash = url.indexOf(PATH_SEPARATOR, colonSlashSlash + 3);
        if (slash != -1) {
            if (server != null) {
                server.append(url, 0, slash);
            }
            return url.substring(slash);
        }
        return url;
    }

    public static final DataURIParseResult parseDataURI(URI uri, String suggestedFilename) {
        return parseDataURIImpl(uri, suggestedFilename);
    }

    private static final DataURIParseResult parseDataURIImpl(URI uri, String suggestedFilename) {

        if (uri == null) {
            return new FailedDataURIParseResult(suggestedFilename, "no candidate data: URI was specified");
        }

        Matcher m = DATA_URI.matcher(uri.toString());
        if (!m.matches()) {
            return new FailedDataURIParseResult(suggestedFilename, "not a data URI");
        }

        // Capture group 1 is the full media type, with any parameters.
        String mediaTypeSpec = m.group(1);
        // Capture group 5 contains the optional base64 marker.
        String base64 = m.group(5);
        // Capture group 6 contains the data.
        String dataStr = m.group(6);

        MediaType mediaType;
        // defaults to text/plain;charset=US-ASCII
        if (StringUtils.isBlank(mediaTypeSpec) || "&lt;".equals(mediaTypeSpec)) {
            mediaType = DATA_URI_DEFAULT_MEDIA_TYPE;
        }
        else {
            // Capture group 2 is the media type type/subtype portion.
            // According to the IETF spec on data URIs, the charset
            // parameter can be specified without "text/plain", in
            // which case text/plain is implied.
            if (m.group(2) == null) {
                mediaTypeSpec = "text/plain" + mediaTypeSpec;
            }
            try {
                mediaTypeSpec = Strings.CS.removeEnd(mediaTypeSpec, ";");
                mediaType = MediaType.parse(mediaTypeSpec);
            }
            catch (Exception e) {
                return new FailedDataURIParseResult(suggestedFilename, e.getMessage());
            }
        }

        byte[] data;
        if (StringUtils.isBlank(base64)) {
            data = getUrlDecoded(dataStr).getBytes(mediaType.charset().or(StandardCharsets.US_ASCII));
        }
        else {
            // Even if this a text/plain type with a charset, if the base64 marker is present, that's how we'll decode
            // the text. There would be no reason to apply a charset encoding from the media type.
            data = Base64Util.getDecodedBytes(dataStr);
            if (data == null) {
                return new FailedDataURIParseResult(suggestedFilename, "base 64 decoding failed");
            }
        }

        // Since there's definitely a media type by now, it's unlikely that we'll need to supply an InputStream to allow
        // the contents to be examined, but there's no reason we can't, just in case.
        SimpleMutableFileMetadata metadata = SimpleMutableFileMetadata.createInstanceForUnnamedResource(
            suggestedFilename, mediaType.toString(), IOUtil.byteArrayInputStreamSupplier(data)
        );
        metadata.setContentLocation(uri.toString());
        return new SuccessfulDataURIParseResult(metadata, data);

    }

    /**
     * This method is designed to parse out the URL parameter name-value pairs from a query String such as
     * &foo=123&bar=456
     *
     * @param queryString
     *     the query string to be parsed.
     * @param decode
     *     whether to decode the parameter names and values (usually this is necessary).
     * @param callback
     *     to notify when a key-value pair is identified.
     */
    @Beta
    public static final void parseUrlParameterSequence(String queryString, boolean decode, Consumer<? super KeyValuePair> callback) {
        parseUrlParameterSequence(queryString, decode, (k, v) -> callback.accept(new SimpleKeyValuePair(k, v)));
    }

    /**
     * This method is designed to parse out the URL parameter name-value pairs from a query String such as
     * &foo=123&bar=456
     *
     * @param queryString
     *     the query string to be parsed.
     * @param decode
     *     whether to decode the parameter names and values (usually this is necessary).
     * @param callback
     *     to notify when a key-value pair is identified.
     */
    @Beta
    public static final void parseUrlParameterSequence(@Nullable String queryString, boolean decode, @Nonnull BiConsumer<? super String,? super String> callback) {
        if (StringUtils.isBlank(queryString)) {
            return;
        }
        Objects.requireNonNull(callback, "callback");
        for (String kvPair : queryString.split(PARAMETER_DELIMITER)) {
            String[] kv = kvPair.split(PARAMETER_SET, 2);
            if (!kv[0].isEmpty()) {
                String key = decode ? getUrlDecoded(kv[0]) : kv[0];
                String value = (kv.length == 2 && kv[1] != null) ? (decode ? getUrlDecoded(kv[1]) : kv[1]) : "";
                callback.accept(key, value);
            }
        }
    }

    @Beta
    public static final Multimap<String,String> getUrlParameters(@Nullable String queryString, boolean decode) {
        if (StringUtils.isBlank(queryString)) {
            return ImmutableMultimap.of();
        }
        ImmutableMultimap.Builder<String,String> builder = ImmutableMultimap.builder();
        parseUrlParameterSequence(queryString, decode, (k, v) -> builder.put(k, v));
        return builder.build();
    }

    public static final boolean isOrIsDescendantPath(@Nonnull String path, @Nonnull String otherPath) {
        if (path.equals(otherPath)) {
            return true;
        }
        if (!path.isEmpty() && path.charAt(path.length() - 1) != PATH_SEPARATOR_CHAR &&
            (path + PATH_SEPARATOR_CHAR).equals(otherPath)) {
            return true;
        }
        if (isDescendantPath(path, otherPath)) {
            return true;
        }
        return false;
    }

    public static final String ensureTrailingSlashIfDirectoryPath(@Nullable String filePath, @Nonnull BooleanSupplier isDirectory) {
        Objects.requireNonNull(isDirectory, "is directory");
        if (StringUtils.isEmpty(filePath)) {
            if (isDirectory.getAsBoolean()) {
                return PATH_SEPARATOR;
            }
            return "";
        }
        if (StringUtil.endsWith(filePath, PATH_SEPARATOR_CHAR)) {
            return filePath;
        }
        if (isDirectory.getAsBoolean()) {
            return filePath + PATH_SEPARATOR;
        }
        return filePath;
    }

    public static final boolean isDescendantPath(String folderPath, String filePath) {
        if (folderPath.isEmpty()) {
            folderPath = PATH_SEPARATOR; // ?
        }
        else if (folderPath.charAt(folderPath.length() - 1) != PATH_SEPARATOR_CHAR) {
            folderPath = folderPath + PATH_SEPARATOR_CHAR;
        }
        if (filePath.startsWith(folderPath)) {
            return true;
        }
        return false;
    }

    /**
     * Return the path portion of the URL in the given URL specification, without decoding it.
     *
     * @param urlSpec
     *     the URL specification.
     * @return the path portion of the URL in the given URL specification, or "/" if the given URL is null or blank.
     */
    @Beta
    public static final String getPath(String urlSpec) {

        if (StringUtils.isBlank(urlSpec)) {
            return PATH_SEPARATOR;
        }

        try {
            return StringUtils.defaultIfEmpty(URI.create(urlSpec).getRawPath(), PATH_SEPARATOR);
        }
        catch (RuntimeException e) {
            // ?
        }

        int protocolBase = urlSpec.indexOf(PROTOCOL_QUALIFIER_AND_AUTHORITY_MARKER);
        if (protocolBase != -1) {
            urlSpec = urlSpec.substring(protocolBase + 3);
        }
        int firstSlash = urlSpec.indexOf(PATH_SEPARATOR_CHAR);
        if (firstSlash > 0) {
            urlSpec = urlSpec.substring(firstSlash);
        }
        int q = urlSpec.indexOf(QUERY_MARKER_CHAR);
        if (q != -1) {
            urlSpec = urlSpec.substring(0, q);
        }
        return urlSpec;

    }

    public static final int getEffectivePort(URL url) {
        int portNumber = url.getPort();
        if (portNumber == -1) {
            return url.getDefaultPort();
        }
        return portNumber;
    }

    public static final boolean haveSameRoots(URL first, URL second) {
        if (Strings.CI.equals(first.getProtocol(), second.getProtocol()) &&
            Strings.CI.equals(first.getHost(), second.getHost()) &&
            getEffectivePort(first) == getEffectivePort(second)) {
            return true;
        }
        return false;
    }

    public static final int getEffectivePort(URI uri) {

        int port = uri.getPort();
        if (port != -1) {
            return port;
        }

        port = getEffectivePort(uri.getScheme());
        if (port != -1) {
            return port;
        }

        URL url;
        try {
            url = uri.toURL();
        }
        catch (MalformedURLException | IllegalArgumentException e) {
            LOGGER.warn("Can't transform URI to URL: {}", StringUtil.truncatedToStringForLog(uri, 100), e);
            return 0;
        }
        return getEffectivePort(url);

    }

    public static final int getEffectivePort(String schemeName) {
        return switch (StringUtils.lowerCase(schemeName)) {
            case SCHEME_NAME_HTTP -> DEFAULT_PORT_HTTP;
            case SCHEME_NAME_HTTPS -> DEFAULT_PORT_HTTPS;
            case null, default -> -1;
        };
    }

    public static final int getDefaultHttpPort(boolean secure) {
        if (secure) {
            return DEFAULT_PORT_HTTPS;
        }
        return DEFAULT_PORT_HTTP;
    }

    public static final boolean haveSameRoots(URI first, URI second) {
        if (Strings.CI.equals(first.getScheme(), second.getScheme()) &&
            Strings.CI.equals(first.getHost(), second.getHost()) &&
            getEffectivePort(first) == getEffectivePort(second)) {
            return true;
        }
        return false;
    }

    /**
     * Returns true if it appears that the same credentials could be safely used for the given request {@link URI} as
     * for the given comparison URI. This only works for HTTP or HTTPS URIs.
     *
     * <p>
     * This requires that the {@link URI#getHost()}s and {@link #getEffectivePort(URI) effective port}s match, but also
     * that the {@link URI#getScheme() scheme}s either match or that the scheme for the request URI be
     * {@link #isSchemeAtLeastAsSecure(URI, URI) at least as secure} as that for the comparison URI. The idea is that if
     * the schemes match exactly -- e.g., are both "https" -- then those are obviously equally secure. But if they don't
     * match, then the request URI must be "https" and the comparison URI must be "http", because that is the only way
     * we can reasonably assume that it's "safe" to use the same credentials for a request to the request URI as the
     * credentials we know apply to the comparison URI.
     *
     * @param requestUri
     *     the request URI, for which the caller wants to know if it's "safe" to use the same credentials that would be
     *     used to request the resource for the comparison URI.
     * @param compUri
     *     the comparison URI, for which presumably the correct credentials are already known.
     * @return true if it appears that the same credentials could be safely used for the given request {@link URI} as
     *     for the given comparison URI; false otherwise, including if either URI is null.
     */
    public static final boolean canSafelyUseSameCredentials(URI requestUri, URI compUri) {
        if (isSchemeAtLeastAsSecure(requestUri, compUri) &&
            Strings.CI.equals(requestUri.getHost(), compUri.getHost()) &&
            getEffectivePort(requestUri) == getEffectivePort(compUri)) {
            return true;
        }
        return false;
    }

    /**
     * Returns true if the scheme from the given request URI is at least as secure as the scheme from the other
     * comparison URI. This only works for HTTP or HTTPS URIs.
     *
     * <p>
     * The idea is that if the {@link URI#getScheme() scheme}s match exactly -- e.g., are both "https" -- then those are
     * obviously equally secure. But if they don't match, then the request URI must be "https" and the comparison URI
     * must be "http", because that is the only way we can reasonably assume that it's "safe" to use the same
     * credentials for a request to the request URI as the credentials we know apply to the comparison URI.
     *
     * @param requestUri
     *     the request URI, for which the caller wants to know if it's "safe" to use the same credentials that would be
     *     used to request the resource for the comparison URI.
     * @param compUri
     *     the comparison URI, for which presumably the correct credentials are already known.
     * @return true if the scheme from the given request URI is at least as secure as the scheme from the other
     *     comparison URI; false otherwise, including if either URI or either of their schemes is null.
     */
    public static final boolean isSchemeAtLeastAsSecure(URI requestUri, URI compUri) {
        if (requestUri == null || compUri == null) {
            return false;
        }
        String requestScheme = requestUri.getScheme();
        String compScheme = compUri.getScheme();
        if (requestScheme == null || compScheme == null) {
            return false;
        }
        if (requestScheme.equalsIgnoreCase(compScheme)) {
            return true;
        }
        if (SCHEME_NAME_HTTPS.equals(requestScheme) && SCHEME_NAME_HTTP.equalsIgnoreCase(compScheme)) {
            // If the request scheme is more secure than the comp scheme.
            return true;
        }
        return false;
    }

    public static final String getQueryString(String name, Collection<String> values) {
        return getJoinedQueryStringParts(
            values.stream().map(v -> getQueryParameter(name, v)).iterator()
        );
    }

    public static final String getQueryStringForUri(String name, Collection<String> values) {
        return getJoinedQueryStringParts(
            values.stream().map(v -> getQueryParameterForUri(name, v)).iterator()
        );
    }

    public static final String getQueryString(Map<String,String> params) {
        return getJoinedQueryStringParts(
            params.entrySet().stream().map(e -> getQueryParameter(e.getKey(), e.getValue())).iterator()
        );
    }

    public static final String getQueryString(Multimap<String,String> params) {
        return getJoinedQueryStringParts(
            params.asMap().entrySet().stream()
                .map(e -> getQueryString(e.getKey(), e.getValue()))
                .iterator()
        );
    }

    public static final String getQueryStringForUri(Multimap<String,String> params) {
        return getJoinedQueryStringParts(
            params.asMap().entrySet().stream()
                .map(e -> getQueryStringForUri(e.getKey(), e.getValue()))
                .iterator()
        );
    }

    public static final String getQueryParameter(String name, String value) {
        return getUrlEncoding(name) + PARAMETER_SET + getUrlEncoding(StringUtils.defaultString(value));
    }

    public static final String getQueryParameterForUri(String name, String value) {
        return name + PARAMETER_SET + StringUtils.defaultString(value);
    }

    public static final String getJoinedQueryStringParts(Iterator<String> parts) {
        return StringUtil.join(parts, PARAMETER_DELIMITER_CHAR);
    }

    public static final String getMailToUrl(String address) {
        return getMailToUrl(address, null);
    }

    public static final String getMailToUrl(String address, Map<String,String> params) {

        if (StringUtils.isBlank(address)) {
            return null;
        }

        StringBuilder ret = new StringBuilder(SCHEME_PREFIX_MAILTO);
        ret.append(getUrlEncoding(address));
        if (CollectionUtil.isNotEmpty(params)) {
            ret.append(QUERY_MARKER_CHAR);
            ret.append(getQueryString(params));
        }
        return ret.toString();

    }

    /**
     * Returns a data: URL for the contents of the given {@link File}.
     *
     * @param file
     *     the {@link File} whose contents should be represented in the URL.
     * @return a data: URL for the contents of the given {@link File}.
     * @throws IOException
     *     if one is raised attempting to open an {@link InputStream} for the given {@link File}, or reading from that
     *     InputStream.
     */
    public static final String getDataUrl(File file) throws IOException {
        Objects.requireNonNull(file, "File");
        return getDataUrl(LocalFileResource.createInstance(file));
    }

    public static final String getDataUrl(FileResource file) throws IOException {
        return getDataUrl(
            getBase64EncodedStringForDataUrl(file), MediaTypeUtil.getContentTypeFromExtension(file.getExtension())
        );
    }

    /**
     * Returns a base 64 encoded String representing the contents of the given {@link File}, suitable for use in a data:
     * URL.
     *
     * @param file
     *     the {@link File} whose contents should be read and returned as a base 64 encoded String.
     * @return a base 64 encoded String representing the contents of the given {@link File}, suitable for use in a data:
     *     URL.
     * @throws IOException
     *     if one is raised attempting to open an {@link InputStream} for the given {@link File}, or reading from that
     *     InputStream.
     */
    public static final String getBase64EncodedStringForDataUrl(File file) throws IOException {
        return getBase64EncodedStringForDataUrl(LocalFileResource.createInstance(file));
    }

    /**
     * Produces a data: scheme URL encoding the contents of the given {@link FileResource}. The URL will include the
     * Content-type/mime type of the File as provided by by {@link FileResource#getContentType()}.
     *
     * @param file
     *     the {@link FileResource} whose contents should be represented in the data: URL.
     * @return a data: scheme URL encoding the contents of the given {@link FileResource}.
     */
    public static final String getBase64EncodedStringForDataUrl(FileResource file) throws IOException {
        try (InputStream input = file.getInputStream()) {
            return getBase64EncodedStringForDataUrl(input);
        }
    }

    public static final String getBase64EncodedStringForDataUrl(InputStream input) throws IOException {
        return Base64Util.getEncodedString(IOUtil.readContentBytes(input));
    }

    /**
     * Produces a data: scheme URL encoding the data from the given {@link InputStream}.
     *
     * @param input
     *     the {@link InputStream} whose contents are to be encoded in the data: scheme URL.
     * @param mimeType
     *     the Content-type/mimetype for the contents of the given InputStream.
     * @return a data: scheme URL encoding the data from the given InputStream.
     * @throws IOException
     *     if one is raised while attempting to read the contents of the given {@link InputStream}.
     * @see #getBase64EncodedStringForDataUrl(InputStream)
     */
    public static final String getDataUrl(InputStream input, String mimeType) throws IOException {
        return getDataUrl(
            getBase64EncodedStringForDataUrl(input), MediaTypeUtil.parseMediaType(mimeType, MediaType.OCTET_STREAM)
        );
    }

    public static final Supplier<URL> getCachingUrlSupplier(final String urlSpec) {
        return Suppliers.memoize(() -> tryToCreateUrl(urlSpec));
    }

    public static final Supplier<URI> getCachingUriSupplier(final String uriSpec) {
        return Suppliers.memoize(() -> tryToCreateUri(uriSpec));
    }

    public static final Supplier<URL> getCachingFormattedUrlSupplier(String urlFormatSpec, final Supplier<?>... argSuppliers) {
        return Suppliers.memoize(() -> {
            List<Object> args = new ArrayList<>();
            for (Supplier<?> argSupplier : argSuppliers) {
                args.add(argSupplier.get());
            }
            return tryToCreateUrl(MessageFormat.format(urlFormatSpec, args.toArray(new Object[0])));
        });
    }

    public static final URL tryToCreateUrl(String urlSpec) {
        try {
            return URI.create(urlSpec).toURL();
        }
        catch (RuntimeException | MalformedURLException e) {
            LOGGER.error("Failed to parse URL spec {}", StringUtil.truncatedToStringForLog(urlSpec, 100), e);
            return null;
        }
    }

    public static final URI tryToCreateUri(String uriSpec) {
        try {
            return URI.create(uriSpec);
        }
        catch (RuntimeException e) {
            LOGGER.error("Failed to parse URI spec {}", StringUtil.truncatedToStringForLog(uriSpec, 100), e);
            return null;
        }
    }

    public static final String directoryPathToUrlPath(String directoryPath) {
        return StringUtil.findReplace(directoryPath, File.separator, PATH_SEPARATOR);
    }

    public static final String getAbsoluteUrl(URL baseURL, String urlSpec) {

        if (baseURL == null) {
            return urlSpec;
        }

        if (Strings.CS.startsWithAny(urlSpec, CURRENT_PATH_INDICATOR, PATH_SEPARATOR)) {
            try {
                return baseURL.toURI().resolve(urlSpec).toString();
            }
            catch (URISyntaxException e) {
                LOGGER.warn(
                    "Failed to produce an absolute URI/URL for {} plus {}",
                    StringUtil.truncatedToStringForLog(baseURL, 100),
                    StringUtil.truncatedToStringForLog(urlSpec, 100),
                    e
                );
            }
        }

        return urlSpec;

    }

    public static final URI toURISafe(URI baseURI, String uriSpec) {
        if (StringUtils.isBlank(uriSpec)) {
            return null;
        }
        if (baseURI == null) {
            try {
                return URI.create(uriSpec);
            }
            catch (RuntimeException e) {
                LOGGER.warn("Failed to parse URI spec {}", StringUtil.truncatedToStringForLog(uriSpec), e);
                return null;
            }
        }
        try {
            return baseURI.resolve(uriSpec);
        }
        catch (RuntimeException e) {
            LOGGER.warn(
                "Failed to resolve URI spec {} against base URI {}",
                StringUtil.truncatedToStringForLog(uriSpec, 100),
                StringUtil.truncatedToStringForLog(baseURI, 100),
                e
            );
            return null;
        }
    }

    public static final boolean isDataUrl(String urlSpec) {
        if (Strings.CI.startsWith(urlSpec, SCHEME_PREFIX_DATA)) {
            return true;
        }
        return false;
    }

    public static final boolean isFileUrl(String urlSpec) {
        if (Strings.CI.startsWith(urlSpec, SCHEME_PREFIX_FILE)) {
            return true;
        }
        return false;
    }

    public static final boolean isHttpUrl(String urlSpec) {
        if (Strings.CI.startsWithAny(urlSpec, SCHEME_PREFIX_HTTP, SCHEME_PREFIX_HTTPS)) {
            return true;
        }
        return false;
    }

    /**
     * Produces a data: scheme URL encoding the given String which is assumed to already represent a valid base 64
     * encoding for a file or other resource with the given Content-type/mimetype.
     *
     * @param base64Data
     *     the valid base 64 encoding for the data to be represented in the data: scheme URL.
     * @param contentType
     *     the Content-type/MIME type for the contents of the given base 64 encoding.
     * @return a data: scheme URL encoding the given String which is assumed to already represent a valid base 64
     *     encoding.
     */
    private static final String getDataUrl(String base64Data, MediaType contentType) {
        return SCHEME_PREFIX_DATA + contentType +
               ';' + DATA_URI_BASE_64_PARAMETER + ',' +
               base64Data;
    }

    public static final String getAbsoluteFileUrl(File file) {
        return SCHEME_PREFIX_FILE + "//" + FileNameUtil.platformIndependentPath(file.getAbsolutePath());
    }

    public static final String getContentIdUrl(String contentId) {
        return SCHEME_PREFIX_CID + getUrlEncoding(contentId);
    }

    public static final String getHttpSchemeName(boolean secure) {
        if (secure) {
            return SCHEME_NAME_HTTPS;
        }
        return SCHEME_NAME_HTTP;
    }

    public static final String getHttpBaseUrl(boolean secure, String host, Integer portNumber, boolean forcePortNumber) {

        Objects.requireNonNull(host, "host");
        if (portNumber != null) {
            if (portNumber < 0) {
                throw new IllegalArgumentException(portNumber + " < 0");
            }
            if (portNumber > 65535) {
                throw new IllegalArgumentException(portNumber + " > 65535");
            }
        }

        StringBuilder ret = new StringBuilder(50);
        String scheme = getHttpSchemeName(secure);
        ret.append(scheme)
            .append(SCHEME_RELATIVE_PREFIX)
            .append(host);

        Integer usePortNumber = getHttpUrlPortNumber(portNumber, forcePortNumber, scheme);
        if (usePortNumber != null) {
            ret.append(SCHEME_QUALIFIER_CHAR).append(usePortNumber);
        }

        return ret.toString();

    }

    /**
     * Some URLs in traction contain rapid selector expressions in a path-like link.  For example, /traction/rs/cdt is a
     * link to the evaluation of the rapid selector expression "cdt".
     *
     * <p>
     * At present, the following urls are equivalent and the rs expressions are correctly extracted:
     *
     * <pre>
     * /rs?cdt
     * /rs/cdt
     * /traction/rs?cdt
     * /traction/rs/cdt
     * /traction/permalink?cdt
     * /traction/permalink/cdt
     * </pre>
     */
    public static final String getRsFromUrl(String url) {

        if (StringUtils.isEmpty(url)) {
            return null;
        }

        url = stripServer(url);

        int len = url.length();
        for (String prefix : RS_URL_PREFIXES) {
            int prefixLen = prefix.length();
            if (len > prefix.length() && url.startsWith(prefix) &&
                RS_URL_NEXT_CHAR_MATCHER.matches(url.charAt(prefixLen))) {
                return url.substring(prefix.length() + 1);
            }
        }
        return null;

    }

    /**
     * Parse a name in the query string.
     */
    public static final String getUrlDecoded(String encodedStr) {
        if (StringUtils.isEmpty(encodedStr)) {
            return encodedStr;
        }
        StringBuilder st = new StringBuilder(encodedStr.length());
        printUrlDecoded(st, encodedStr);
        return st.toString();
    }

    public static final void printUrlDecoded(Appendable out, String encodedStr) {

        LOGGER.debug("decode() 0 {}", encodedStr);

        // before %xx decoding, make sure the incoming string is
        // proper UTF-8.  this should work around the IE behavior
        // described in AKJ359 [ajm 26.Sep.2003]
        //
        // update: on output, when rendering urls, we now just encode
        // everything to %xx as many times as necessary to get it to
        // work. [ajm 26.Sep.2003]
//  	str = Encoding.cvtEncoding(str, Encoding.ISO_8859_1, Encoding.UTF8);

        int l = encodedStr.length();
        byte[] enc = new byte[l / 3]; // worst case, all %xx
        int nenc = 0;

        char[] ca = encodedStr.toCharArray();
        for (int i = 0; i < l; i++) {
            char c = ca[i];
            switch (c) {
            case '+':

                // stop decoding + signs unless we know we're in a
                // query string!  fixes Server13747 and seems like the
                // right thing to do.  see the thread at
                // http://www.codecomments.com/archive227-2004-4-179644.html
                // which suggests that + should only be decode to ' '
                // when part of a query string. [ajm 26.Apr.2005]

                c = ' ';

                //$FALL-THROUGH$
            case '%':
                if (c != ' ') {
                    if (i + 2 < l && isHexit(ca[i + 1]) && isHexit(ca[i + 2])) {
                        // %xx
                        enc[nenc++] = (byte) (hexit(ca[i + 1]) * 16 + hexit(ca[i + 2]));
                        i += 2;
                        break;
                    }
                    else if (i + 5 < l &&
                             ca[i + 1] == 'u' &&
                             isHexit(ca[i + 2]) &&
                             isHexit(ca[i + 3]) &&
                             isHexit(ca[i + 4]) &&
                             isHexit(ca[i + 5])) {
                        byte[] b = new byte[2];
                        b[0] = (byte) (hexit(ca[i + 2]) * 16 + hexit(ca[i + 3]));
                        b[1] = (byte) (hexit(ca[i + 4]) * 16 + hexit(ca[i + 5]));
                        StringWriteUtil.appendUtf16Bytes(out, b);
                        i += 5;
                        break;
                    }
                }
                //$FALL-THROUGH$
            default:
                if (nenc > 0) {  // add any pending bytes
                    StringWriteUtil.appendUtf8Bytes(out, enc, 0, nenc);
                    nenc = 0;  // reset the buffer
                }
                StringWriteUtil.safeAppend(out, c);
            }
        }

        if (nenc > 0) {  // add any remaining bytes
            StringWriteUtil.appendUtf8Bytes(out, enc, 0, nenc);
        }

        LOGGER.debug("decode() 1 {}", out);

    }

    public static final String getUrlEncoding(String str) {
        if (str == null) {
            return null;
        }
        StringBuilder st = new StringBuilder(str.length() + 20);
        printUrlEncoding(st, str);
        return st.toString();
    }

    @Beta
    public static final boolean isProbablyNotEncoded(String str) {
        if (str == null) {
            return false;
        }
        if (CHARS_USUALLY_NEED_ENCODNG.indexIn(str) != -1) {
            return true;
        }
        return false;
    }

    @Beta
    public static final String getUrlEncodingIfNecessary(String str) {
        if (isProbablyNotEncoded(str)) {
            return getUrlEncoding(str);
        }
        return str;
    }

    public static final void printUrlEncoding(Appendable out, String str) {

        if (str == null) {
            return;
        }

        int len = str.length();

        for (int i = 0; i < len; ++i) {

            char c = str.charAt(i);

            switch (c) {

            // list all safe non-text characters here. we won't
            // encode these. the more we list, the easier our
            // urls will be for humans to read.

            case '(':
            case ')':
            case ',':
            case '.':
            case '-':
            case '/':
            case '*':
                StringWriteUtil.safeAppend(out, c);
                break;

            default:

                if (('a' <= c && c <= 'z') || ('A' <= c && c <= 'Z') || ('0' <= c && c <= '9')) {
                    // we also don't encode text characters
                    StringWriteUtil.safeAppend(out, c);
                }
                else {

                    // we encode everything else
                    if (Character.isLetterOrDigit(c) || !StringUtil.isBasicLatin(c)) {
                        // this properly handles multibyte character
                        // encoding
                        StringWriteUtil.safeAppend(out, urlEncodeUTF8(String.valueOf(c)));
                    }
                    else {
                        StringWriteUtil.safeAppend(out, '%');
                        String num = Integer.toString((c & 0xff), 16);
                        if (num.length() == 1) {
                            StringWriteUtil.safeAppend(out, "0");
                        }
                        StringWriteUtil.safeAppend(out, num);
                    }
                }
                break;
            }

        }

    }

    public static final void urlEncodeUTF8(Appendable out, char c) {

        if (StringUtil.isBasicLatin(c)) {
            StringWriteUtil.safeAppend(out, c);
            return;
        }

        // it seems like there should be a better approach to encoding a single character than creating a String and
        // calling getBytes, but i couldn't find anything better. [ajm 13.Dec.2009]
        byte[] bytes = String.valueOf(c).getBytes(StandardCharsets.UTF_8);
        for (byte b : bytes) {
            StringWriteUtil.safeAppend(out, '%');
            String num = Integer.toString(b & 0xff, 16);
            if (num.length() == 1) {
                StringWriteUtil.safeAppend(out, '0');
            }
            StringWriteUtil.safeAppend(out, num);
        }

    }

    public static final void urlEncodeUTF8(StringBuilder sb, String str) {
        if (str != null) {
            int sz = str.length();
            for (int i = 0; i < sz; i++) {
                urlEncodeUTF8(sb, str.charAt(i));
            }
        }
    }

    public static final String urlEncodeUTF8(String str) {
        if (str == null) {
            return null;
        }
        StringBuilder sb = new StringBuilder();
        urlEncodeUTF8(sb, str);
        return sb.toString();
    }

    public static final String encodeFullyQualifiedURL(String url) {

        String ret;
        // Okay to encode a server- or directory-relative URL in its entirety.
        if (Strings.CS.startsWithAny(url, CURRENT_PATH_INDICATOR, PATH_SEPARATOR)) {
            ret = getUrlEncoding(url);
        }
        // Otherwise, make sure we don't encode the protocol handler,
        // and for path-URLs, don't bother to encode anything between
        // the protocol:// and the subsequent /.
        //
        // See Help3844. [shep 31.Mar.2008]
        else {
            int colonSlashSlash = url.indexOf(SCHEME_RELATIVE_PREFIX);
            if (colonSlashSlash != -1) {
                int nextSlash = url.indexOf(PATH_SEPARATOR, colonSlashSlash + 3);
                if (nextSlash != -1) {
                    if (nextSlash == url.length() - 1) {
                        ret = url;
                    }
                    else {
                        ret =
                            url.substring(0, nextSlash) + PATH_SEPARATOR + getUrlEncoding(url.substring(nextSlash + 1));
                    }
                }
                else {
                    // Just use the original URL unencoded.
                    ret = url;
                }
            }
            else {
                int firstColon = url.indexOf(SCHEME_QUALIFIER_CHAR);
                if (firstColon != -1) {
                    if (firstColon == url.length() - 1) {
                        ret = getUrlEncoding(url.substring(0, url.length() - 1)) + SCHEME_QUALIFIER_CHAR;
                    }
                    else {
                        ret = getUrlEncoding(url.substring(0, firstColon)) +
                              SCHEME_QUALIFIER_CHAR +
                              getUrlEncoding(url.substring(firstColon + 1));
                    }
                }
                else {
                    ret = getUrlEncoding(url);
                }
            }
        }

        return ret;

    }

    /**
     * Returns null if url==null or no :// in url
     */
    public static final String getHostFromURL(String url) {
        if (StringUtils.isEmpty(url)) {
            return null;
        }
        try {
            return new URI(url).getHost();
        }
        catch (URISyntaxException e) {
            LOGGER.warn("Failed to parse URL {}", StringUtil.truncatedToStringForLog(url, 100), e);
        }
        return null;
    }

    private static final boolean isHexit(char c) {
        String legalChars = "0123456789abcdefABCDEF";
        return (legalChars.indexOf(c) != -1);
    }

    private static final int hexit(char c) {
        if (c >= '0' && c <= '9') {
            return c - '0';
        }
        if (c >= 'a' && c <= 'f') {
            return c - 'a' + 10;
        }
        if (c >= 'A' && c <= 'F') {
            return c - 'A' + 10;
        }
        // shouldn't happen, we're guarded by isHexit()
        return 0;
    }

    private static final Integer getHttpUrlPortNumber(Integer requestedPortNumber, boolean forcePortNumber, String scheme) {
        if (requestedPortNumber == null) {
            if (forcePortNumber) {
                return getEffectivePort(scheme);
            }
            return null;
        }
        if (forcePortNumber || requestedPortNumber != getEffectivePort(scheme)) {
            return requestedPortNumber;
        }
        return null;
    }

}
