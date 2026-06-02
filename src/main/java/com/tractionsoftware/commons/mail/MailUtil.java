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

import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableListMultimap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Multimap;
import com.google.common.net.MediaType;
import com.tractionsoftware.commons.io.ByteBufferInputStream;
import com.tractionsoftware.commons.io.FileResource;
import com.tractionsoftware.commons.lang.StringUtil;
import com.tractionsoftware.commons.text.StringEscapeUtil;
import com.tractionsoftware.commons.util.AbstractLazyLoadingIterator;
import com.tractionsoftware.commons.util.CollectionsUtil;
import com.tractionsoftware.commons.lang.ObjectUtil;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;

import jakarta.activation.DataSource;
import jakarta.annotation.Nonnull;
import jakarta.mail.*;
import jakarta.mail.internet.AddressException;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.internet.MimeUtility;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.text.CharacterIterator;
import java.text.StringCharacterIterator;
import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * Utility methods for dealing with data related to email messages.
 *
 * @author Chris Nuzum, Andy Keller, Dave Shepperton
 */
public final class MailUtil {

    private MailUtil() {
    }

    public static final String MIME_TYPE_NAME_MULTIPART = "multipart";

    public static final MediaType MIME_TYPE_MULTIPART_MIXED = MediaType.create(MIME_TYPE_NAME_MULTIPART, "mixed");

    public static final MediaType MIME_TYPE_MULTIPART_ALTERNATIVE =
        MediaType.create(MIME_TYPE_NAME_MULTIPART, "alternative");

    public static final char HEADER_DELIMITER_CHAR = ':';

    private static final Logger LOGGER = LoggerFactory.getLogger(MailUtil.class);

    /**
     * A simple implementation of {@link EmailHeaders} based upon a {@link Multimap} that contains the mappings from
     * lowercase header names to the {@link List} of {@link Header}s, and a simple {@link Iterable} that offers access
     * to the raw header lines.
     *
     * @author Dave Shepperton
     */
    private static final class SimpleDecodedEmailHeaders implements EmailHeaders {

        private final Iterable<String> rawHeaderLines;

        private final Multimap<String,Header> decodedHeaders;

        private SimpleDecodedEmailHeaders(Multimap<String,Header> decodedHeaders, Iterable<String> rawHeaderLines) {
            this.decodedHeaders = decodedHeaders;
            this.rawHeaderLines = rawHeaderLines;
        }

        @Override
        public final String toString() {
            return getClass().getSimpleName();
        }

        @Override
        public final List<String> getHeaders(String name) {
            if (name == null) {
                return ImmutableList.of();
            }
            return decodedHeaders.get(name.toLowerCase()).stream().map(Header::getValue).collect(Collectors.toList());
        }

        @Nonnull
        @Override
        public final Iterator<Header> iterator() {
            return Iterables.concat(decodedHeaders.values()).iterator();
        }

        @Override
        public final Iterable<String> getRawHeaderLines() {
            return Iterables.unmodifiableIterable(rawHeaderLines);
        }

    }

    private static final class DynamicMessageEmailHeaders implements EmailHeaders {

        private final MimeMessage message;

        private DynamicMessageEmailHeaders(MimeMessage message) {
            this.message = message;
        }

        @Override
        public final String toString() {
            return getClass().getSimpleName() + ":" + message;
        }

        @Override
        public final List<String> getHeaders(String name) {
            try {
                String[] headers = message.getHeader(name);
                if (headers != null) {
                    return Arrays.stream(headers).map(MailUtil::getRfc2047DecodedText).collect(Collectors.toList());
                }
            }
            catch (MessagingException e) {
                LOGGER.warn("Failed to retrieve mail message headers {}", name, e);
            }
            return ImmutableList.of();
        }

        @Override
        public final List<String> getRawHeaderLines() {
            try {
                return MailUtil.getRawHeaderLines(message);
            }
            catch (MessagingException e) {
                LOGGER.warn(
                    "Failed to retrieve all raw header lines from {}", ObjectUtil.safeToStringObject(message), e
                );
            }
            return ImmutableList.of();
        }

        @Nonnull
        @Override
        public final Iterator<Header> iterator() {
            return getRawHeaderLines().stream().map(MailUtil::encodedHeaderLineToHeader).iterator();
        }

    }

    private static abstract class ReadOnlyAdapterDataSource implements DataSource {

        @Override
        public final OutputStream getOutputStream() {
            throw new UnsupportedOperationException("This is a read-only DataSource.");
        }

    }

    private static final class InputStreamDataSource extends ReadOnlyAdapterDataSource {

        @FunctionalInterface
        private static interface InputStreamCreator {

            public InputStream create() throws IOException;

        }

        private static final InputStreamCreator textToInputStreamCreator(final String text, final Charset charset) {

            return new InputStreamCreator() {

                @Override
                public final String toString() {
                    return "InputStreamCreator for bytes";
                }

                @Override
                public final InputStream create() {
                    return ByteBufferInputStream.createInstance(text, charset);
                }

            };

        }

        private static final InputStreamCreator bytesToInputStreamCreator(final byte[] data) {

            return new InputStreamCreator() {

                @Override
                public final String toString() {
                    return "InputStreamCreator for bytes";
                }

                @Override
                public final InputStream create() {
                    return ByteBufferInputStream.createInstance(data);
                }

            };

        }

        private static final InputStreamCreator fileToInputStreamCreator(FileResource file) {

            return new InputStreamCreator() {

                @Override
                public final String toString() {
                    return "InputStreamCreator for bytes";
                }

                @Override
                public final InputStream create() throws IOException {
                    return file.getInputStream();
                }

            };

        }

        public static final InputStreamDataSource createInstance(String name, byte[] data, String contentType) {
            return new InputStreamDataSource(name, bytesToInputStreamCreator(data), contentType);
        }

        public static final InputStreamDataSource createInstance(String name, String text, Charset charset, String contentType) {
            return new InputStreamDataSource(name, textToInputStreamCreator(text, charset), contentType);
        }

        public static final InputStreamDataSource createInstance(FileResource file) {
            return new InputStreamDataSource(file.getFilename(), fileToInputStreamCreator(file), file.getContentType());
        }

        public static final InputStreamDataSource createInstance(String name, Supplier<? extends InputStream> input, String type) {
            return new InputStreamDataSource(name, input::get, type);
        }

        private final InputStreamCreator input;

        private final String name;

        private final String type;

        private InputStreamDataSource(String name, InputStreamCreator input, String type) {
            Objects.requireNonNull(input, "InputStream creator");
            this.name = name;
            this.input = input;
            this.type = type;
        }

        @Override
        public final InputStream getInputStream() throws IOException {
            return input.create();
        }

        @Override
        public final String getContentType() {
            return type;
        }

        @Override
        public final String getName() {
            return StringUtils.defaultString(name);
        }

    }

    private static final class EmailAddressesSpecTokenizer extends AbstractLazyLoadingIterator<String> {

        private static enum SequenceType {
            NONE,
            NORMAL,
            LITERAL,
            ADDRESS
        }

        private static enum ScanResult {
            IGNORE,
            TOKEN,
            TOKEN_SEPARATOR,
            TOKEN_LITERAL_START,
            TOKEN_LITERAL,
            TOKEN_LITERAL_END,
            TOKEN_ADDRESS_START,
            TOKEN_ADDRESS,
            TOKEN_ADDRESS_END
        }

        private static final boolean isTokenSeparator(char c) {
            if (Character.isWhitespace(c) || c == ',' || c == ';') {
                return true;
            }
            return false;
        }

        private final CharacterIterator chars;

        private EmailAddressesSpecTokenizer(String emailAddressesSpec) {
            this.chars = new StringCharacterIterator(emailAddressesSpec);
        }

        protected final String loadNextValue() {

            char c = chars.current();

            if (c == CharacterIterator.DONE) {
                // Input exhausted.
                return null;
            }

            StringBuilder token = new StringBuilder();
            SequenceType seqType = SequenceType.NONE;
            ScanResult scanResult;

            do {

                scanResult = scan(seqType, c);

                boolean append;
                boolean finished;

                switch (scanResult) {
                case IGNORE -> {
                    append = false;
                    finished = false;
                    seqType = SequenceType.NONE;
                }
                case TOKEN_SEPARATOR -> {
                    append = false;
                    finished = true;
                    seqType = SequenceType.NONE;
                }
                case TOKEN_LITERAL_START, TOKEN_LITERAL -> {
                    append = true;
                    finished = false;
                    seqType = SequenceType.LITERAL;
                }
                case TOKEN_ADDRESS_START, TOKEN_ADDRESS -> {
                    append = true;
                    finished = false;
                    seqType = SequenceType.ADDRESS;
                }
                case TOKEN_LITERAL_END, TOKEN_ADDRESS_END -> {
                    append = true;
                    finished = true;
                    seqType = SequenceType.NONE;
                }
                default -> {
                    append = true;
                    finished = false;
                    seqType = SequenceType.NORMAL;
                }
                }

                if (append) {
                    token.append(c);
                }
                // Always advance.
                c = chars.next();
                if (finished) {
                    break;
                }

            } while (c != CharacterIterator.DONE);

            if (c == CharacterIterator.DONE) {
                // Input has been exhausted.
                if (scanResult == ScanResult.IGNORE) {
                    return null;
                }
                if (scanResult == ScanResult.TOKEN_LITERAL) {
                    token.append('"');
                }
                else if (scanResult == ScanResult.TOKEN_ADDRESS) {
                    token.append('>');
                }
            }

            return token.toString();

        }

        private final ScanResult scan(SequenceType seqType, char c) {
            return switch (c) {
                case '"' -> switch (seqType) {
                    case LITERAL -> ScanResult.TOKEN_LITERAL_END;
                    case ADDRESS -> ScanResult.TOKEN_ADDRESS;
                    case NONE, NORMAL -> ScanResult.TOKEN_LITERAL_START;
                };
                case '<' -> switch (seqType) {
                    case LITERAL, ADDRESS -> ScanResult.TOKEN_ADDRESS;
                    case NONE, NORMAL -> ScanResult.TOKEN_ADDRESS_START;
                };
                case '>' -> switch (seqType) {
                    case ADDRESS -> ScanResult.TOKEN_ADDRESS_END;
                    case LITERAL -> ScanResult.TOKEN_LITERAL;
                    case NONE, NORMAL -> ScanResult.TOKEN; // A very weird case.
                };
                default -> switch (seqType) {
                    case ADDRESS -> ScanResult.TOKEN_ADDRESS;
                    case LITERAL -> ScanResult.TOKEN_LITERAL;
                    case NONE -> isTokenSeparator(c) ? ScanResult.IGNORE : ScanResult.TOKEN;
                    default -> isTokenSeparator(c) ? ScanResult.TOKEN_SEPARATOR : ScanResult.TOKEN;
                };
            };
        }

    }

    private static final class EmailAddressListBuilder {

        private final ImmutableList.Builder<EmailAddress> list = ImmutableList.builder();

        private final Joiner joiner = StringUtil.getNullSkippingJoiner(' ');

        private final List<String> friendlyNameParts = new ArrayList<>();

        private EmailAddressListBuilder() {
        }

        public void onFriendlyNamePart(String friendlyNamePart) {
            if (!friendlyNamePart.isEmpty()) {
                friendlyNameParts.add(friendlyNamePart);
            }
        }

        public void onAddress(String address) {

            if (address.isEmpty()) {
                if (!friendlyNameParts.isEmpty()) {
                    // Friendly name followed by no email address: ignore (including discarding the name, which doesn't
                    // apparently go with any address).
                    friendlyNameParts.clear();
                }
                return;
            }

            list.add(new EmailAddress(address, useFriendlyName()));

        }

        public final List<EmailAddress> build() {
            return list.build();
        }

        private final String useFriendlyName() {
            if (friendlyNameParts.isEmpty()) {
                return null;
            }
            String friendlyName = joiner.join(friendlyNameParts);
            friendlyNameParts.clear();
            return friendlyName;
        }

    }

    /**
     * Returns an {@link EmailHeaders} object based upon the given raw header lines.
     *
     * @param rawHeaderLines
     *     the raw header lines, which have not yet had any required RFC 2047 decoding applied.
     * @return an {@link EmailHeaders} objects based upon the given raw header lines.
     */
    public static final EmailHeaders createEmailHeadersFromRawLines(Iterable<String> rawHeaderLines) {
        return new SimpleDecodedEmailHeaders(headerLinesToDecodedHeaderMap(rawHeaderLines), rawHeaderLines);
    }

    /**
     * Returns an {@link EmailHeaders} object based upon the given {@link Header}s, decoding the header values as
     * necessary if requested.
     *
     * @param headers
     *     a {@link Multimap} containing
     * @param tryToDecode
     *     indicates whether RFC 2047 decoding should be applied to each header value.
     * @return an {@link EmailHeaders} object based upon the given {@link Header}s.
     */
    public static final EmailHeaders createEmailHeaders(Multimap<String,Header> headers, boolean tryToDecode) {
        ImmutableList.Builder<String> rawHeaderLines = ImmutableList.builder();
        headers.values()
            .forEach((Header header) -> rawHeaderLines.add(StringUtils.defaultString(headerToString(header))));
        if (tryToDecode) {
            return new SimpleDecodedEmailHeaders(getRfc2047DecodedHeaders(headers), rawHeaderLines.build());
        }
        return new SimpleDecodedEmailHeaders(headers, rawHeaderLines.build());
    }

    public static final EmailHeaders createEmailHeaders(Iterable<Header> headers, boolean tryToDecode) {
        return createEmailHeaders(MailUtil.headersToHeaderMap(headers), true);
    }

    public static final EmailHeaders createEmailHeaders(MimeMessage message) throws MessagingException {
        return createEmailHeadersFromRawLines(getRawHeaderLines(message));
    }

    public static final EmailHeaders createDynamicEmailHeaders(MimeMessage message) {
        return new DynamicMessageEmailHeaders(message);
    }

    public static final List<String> getRawHeaderLines(MimeMessage message) throws MessagingException {
        Enumeration<?> headerLinesEncoded = message.getAllHeaderLines();
        ImmutableList.Builder<String> listBuilder = ImmutableList.builder();
        while (headerLinesEncoded.hasMoreElements()) {
            listBuilder.add(Objects.toString(headerLinesEncoded.nextElement(), ""));
        }
        return listBuilder.build();
    }

    public static final Header getRfc2047DecodedHeader(Header header) {
        if (header == null) {
            return null;
        }
        if (headerRequiresRfc2047Decoding(header)) {
            String originalValue = header.getValue();
            String decodedValue = getRfc2047DecodedText(originalValue);
            if (!Objects.equals(decodedValue, originalValue)) {
                return new Header(header.getName(), decodedValue);
            }
        }
        return header;
    }

    public static final String getRfc2047DecodedText(String text) {
        if (StringUtils.isBlank(text)) {
            return null;
        }
        try {
            return MimeUtility.decodeText(text);
        }
        catch (UnsupportedEncodingException e) {
            LOGGER.error("Failed to MIME-decode text {}", StringUtil.truncatedToStringForLog(text), e);
        }
        return text;
    }

    /**
     * Creates a {@link Header} from a header line.
     *
     * @param headerLine
     *     the header line.
     * @param tryToDecode
     *     indicates whether RFC 2047 decoding should be attempted on the given header line.
     * @param defaultHeader
     *     creates a default {@link Header} in case one cannot be created successfully.
     * @return a {@link Header} from a header line, if it contains the ":" delimiter; the default supplier Header
     *     otherwise.
     */
    public static final Header parseHeaderLine(String headerLine, boolean tryToDecode, Function<String,Header> defaultHeader) {

        if (headerLine == null) {
            if (defaultHeader == null) {
                return null;
            }
            return defaultHeader.apply(null);
        }

        if (tryToDecode) {
            headerLine = cvtHeaderLineFromRfc2047(headerLine);
        }

        int delimiterIdx = headerLine.indexOf(HEADER_DELIMITER_CHAR);
        if (delimiterIdx == -1) {
            if (defaultHeader == null) {
                return null;
            }
            return defaultHeader.apply(headerLine);
        }

        String name = StringUtil.getTrimmedSubstring(headerLine, 0, delimiterIdx);
        String value;
        if (delimiterIdx + 1 > headerLine.length()) {
            value = "";
        }
        else {
            value = StringUtil.getTrimmedSubstring(headerLine, delimiterIdx + 1);
        }
        return new Header(name, value);

    }

    /**
     * Creates a {@link Header} from the given header line via {@link #parseHeaderLine(String, boolean, Function)},
     * without applying RFC 2047 decoding, and using {@link #invalidHeaderLineToHeader(String)} to create a suitable
     * default Header in case parsing fails.
     *
     * @param headerLine
     *     the header line.
     * @return a {@link Header} from the given header line, without applying RFC 2047 decoding, if a Header can be
     *     parsed successfully; {@link #invalidHeaderLineToHeader(String) a suitable default Header otherwise}.
     */
    public static final Header headerLineToHeader(String headerLine) {
        return headerLineToHeader(headerLine, false);
    }

    public static final Header encodedHeaderLineToHeader(String headerLine) {
        return headerLineToHeader(headerLine, true);
    }

    /**
     * Creates a {@link Header} from the given header line via {@link #parseHeaderLine(String, boolean, Function)},
     * applying RFC 2047 decoding if requested, and using {@link #invalidHeaderLineToHeader(String)} to create a
     * suitable default Header in case parsing fails.
     *
     * @param headerLine
     *     the header line.
     * @param tryToDecode
     *     indicates whether RFC 2047 decoding should be applied to the given header line.
     * @return a {@link Header} from the given header line, applying RFC 2047 decoding if requested, if a Header can be
     *     parsed successfully; {@link #invalidHeaderLineToHeader(String) a suitable default Header otherwise}.
     */
    public static final Header headerLineToHeader(String headerLine, boolean tryToDecode) {
        return parseHeaderLine(headerLine, false, MailUtil::invalidHeaderLineToHeader);
    }

    /**
     * Creates a {@link Header} with the empty String for its name and the given invalid header line text as its value,
     * replacing null with the empty String, and trimming other values.
     *
     * @param invalidHeaderLine
     *     the (presumably invalid) header line.
     * @return a {@link Header} with the empty String for its name and the given invalid header line text as its value.
     */
    public static final Header invalidHeaderLineToHeader(String invalidHeaderLine) {
        return new Header("", StringUtils.trimToEmpty(invalidHeaderLine));
    }

    /**
     * Creates a "Friendly Name &lt;address&gt;" String from the given address and name. The name field will have its
     * backslash and double quote characters escaped, because they have to be, even though most mail readers do not seem
     * to correctly interpret escaped backslashes and double quotation marks.
     */
    public static final String makeFriendlyNameAddress(String address, String name) {
        address = StringUtils.trimToNull(address);
        if (address == null) {
            return null;
        }
        name = StringUtils.trimToNull(name);
        if (name == null) {
            return address;
        }
        return "\"" + StringEscapeUtil.escapeMultipleCharacters(name, "\\\"") + "\" <" + address + ">";
    }

    public static final String makeFriendlyNameAddress(InternetAddress address) {
        if (address == null) {
            return null;
        }
        return makeFriendlyNameAddress(address.getAddress(), address.getPersonal());
    }

    /**
     * Encodes the friendly name portion of a single email address, if one is present.
     *
     * @param address
     *     the address whose friendly name is to be encoded.
     * @return if the address is null or whitespace, the empty string; otherwise, if the address contains a friendly
     *     name, the concatenation of the trimmed and encoded friendly name, a single space, and the address itself, set
     *     off from the friendly name by the usual &lt; &gt;; if no friendly name is contained, just the address,
     *     without any extra whitespace or the &lt; &gt; delimiters.
     */
    public static final String encodeFriendlyNameInAddress(String address) {
        address = StringUtils.trimToNull(address);
        if (address == null) {
            return "";
        }
        int startaddr = address.indexOf('<');
        if (startaddr > 0) {
            String friendly = StringUtil.getTrimmedSubstring(address, 0, startaddr);
            try {
                friendly = MimeUtility.encodeText(friendly, StandardCharsets.UTF_8.name(), null);
            }
            catch (UnsupportedEncodingException uee) {
                friendly = "";
            }
            int endaddr = address.indexOf('>', startaddr);
            if (endaddr != -1 && address.length() > (startaddr + 1)) {
                address = StringUtil.getTrimmedSubstring(address, startaddr + 1, endaddr);
                if (!friendly.isEmpty()) {
                    address = friendly + " <" + address + ">";
                }
            }
        }
        return address;
    }

    /**
     * Analyzes the given email address encoding which may or may not include a friendly name portion, and identifies
     * the raw email address component. This is generally the portion between the &lt; and &gt; delimiters, or the
     * entire string if no standard friendly name encoding is identified.
     *
     * @return if the address is null or whitespace, the empty string; otherwise, the trimmed email address portion of
     *     the address string, namely, the part between the &lt; and &gt; delimiters.
     */
    public static final String getRawAddressFromFriendlyEncoding(String addressEncoding) {

        if (StringUtils.isBlank(addressEncoding)) {
            return "";
        }

        int startaddr = addressEncoding.indexOf('<');
        if (startaddr != -1) {
            int endaddr = addressEncoding.indexOf('>', startaddr);
            if (endaddr != -1) {
                return StringUtil.getTrimmedSubstring(addressEncoding, startaddr + 1, endaddr);
            }
        }

        return addressEncoding;

    }

    /**
     * Parses the friendly name from an address string such as
     *
     * <pre>
     * "Bob Johnson" <bob@example.com>
     * </pre>
     *
     * <pre>
     * Betty Jackson <betty@example.com>
     * </pre>
     *
     * <p>
     * and returns just the friendly name portion of the string, such as
     *
     * <pre>
     * Bob Johnson
     * </pre>
     *
     * <pre>
     * Betty Jackson
     * </pre>
     *
     * <p>
     * If no such friendly name can be identified -- that is, if there is only an address present, or if the argument is
     * null -- the empty string is returned. In general, to get the final value of the friendly name, we start with
     * everything to the left of the less-than for the address delimiter, and remove double quotation marks.
     *
     * @return the string identified as described.
     */
    public static final String getFriendlyNameFromFriendlyEncoding(String addressEncoding) {

        if (StringUtils.isBlank(addressEncoding)) {
            return "";
        }

        int startDelimiter = addressEncoding.indexOf('<');

        if (startDelimiter != -1) {
            return StringUtil.removeAll(addressEncoding.substring(0, startDelimiter), "\"").trim();
        }

        return "";

    }

    /**
     * Uses {@link MimeUtility#decodeText(String)} to decode a header line from RFC 2047 if
     * {@link #headerLineRequiresRfc2047Decoding(String) we deem that it is required}.
     */
    public static final String cvtHeaderLineFromRfc2047(String headerLine) {
        try {
            if (headerLineRequiresRfc2047Decoding(headerLine)) {
                // See akj1233 [rmf 06.Apr.2004]
                return MimeUtility.decodeText(headerLine);
            }
        }
        catch (UnsupportedEncodingException e) {
            LOGGER.error("Failed to encode header", e);
        }
        return headerLine;
    }

    /**
     * Attempts to separate and identify a friendly name in an email address encoding, creating an {@link EmailAddress}
     * from the result.
     *
     * @param addressMaybeWithFriendlyName
     *     the address encoding to parse.
     * @return the {@link EmailAddress} with the address and the friendly name parsed from the encoding.
     */
    public static final EmailAddress parseFromAddressWithOptionalFriendlyName(String addressMaybeWithFriendlyName) {
        return new EmailAddress(
            getRawAddressFromFriendlyEncoding(addressMaybeWithFriendlyName),
            getFriendlyNameFromFriendlyEncoding(addressMaybeWithFriendlyName)
        );
    }

    /**
     * Parses email addresses, with separated address plus friendly name (when included in the encoding).
     *
     * @param emailAddressesSpec
     *     the email addresses to parse.
     * @return a List with the resulting EmailAddress objects.
     */
    public static final List<EmailAddress> getEmailAddresses(String emailAddressesSpec) {

        if (StringUtils.isEmpty(emailAddressesSpec)) {
            return ImmutableList.of();
        }

        EmailAddressListBuilder builder = new EmailAddressListBuilder();

        for (String token : getEmailAddressTokens(emailAddressesSpec)) {

            if (StringUtil.startsWith(token, '<')) {
                String address;
                if (StringUtil.endsWith(token, '>')) {
                    // Remove "<" and ">" delimiters.
                    address = StringUtil.getTrimmedSubstring(token, 1, token.length() - 1);
                }
                else {
                    // Remove "<" delimiter; missing ">" delimiter.
                    address = StringUtil.getTrimmedSubstring(token, 1);
                }
                // This address may be empty the token was just, e.g., "<  >", but notify anyway because the builder
                // needs to know that if it has a friendly name that was waiting for an address, it doesn't have one.
                builder.onAddress(address);
                continue;
            }

            // For a token that doesn't have <> delimiters, see if it looks like an email address, anyway.

            // Tokens that are quoted have to be treated as friendly names.
            if (StringUtil.isDoubleQuoted(token)) {
                builder.onFriendlyNamePart(StringUtil.extractQuotation(token).trim());
                continue;
            }

            // Take a guess that if it contains "@", and is not quoted, it's an email address.
            if (token.indexOf('@') != -1) {
                builder.onAddress(token);
            }
            else {
                builder.onFriendlyNamePart(token);
            }

        }

        return builder.build();

    }

    /**
     * Sets headers on the given {@link Message} to indicate to the recipient client and mail systems that the message
     * was automatically generated, and that automatic responses to this message should be suppressed.
     *
     * <p>
     * Currently, this method sets the "Auto-Submitted" email header, as well as the Microsoft Exchange specific
     * "X-Auto-Response-Suppress" header. Additionally, if the server configuration specifies that it should do so, it
     * also sets the header
     *
     * <pre>
     * Return-Path: <>
     * </pre>
     *
     * <p>
     * Per <a href="https://tools.ietf.org/html/rfc3834#section-2">RFC 3834 section 2</a>, specifying an empty return
     * path is intended to prevent responders on destination servers from generating replies:
     *
     * <blockquote>
     * Responders MUST NOT generate any response for which the destination of that response would be a null address
     * (e.g., an address for which SMTP MAIL FROM or Return-Path is <>), since the response would not be delivered to a
     * useful destination.
     * </blockquote>
     *
     * <p>
     * This header is not supposed to be set by the email client, but rather by the SMTP server, and in fact many SMTP
     * servers will not accept messages that already have a Return-Path header, or at least one that doesn't match the
     * sender. Sometimes, even when such a message can be sent, a mismatched Return-Path and sender address may cause
     * the message to be treated as spam. Nonetheless, because it can help to avoid automatically generated responses
     * being sent back to TeamPage, it is supported. But because the behavior can prevent messages from being sent or
     * received, a server administrator must opt into it by changing the appropriate setting.
     *
     * @param message
     *     the automatically generated email {@link Message}.
     * @throws MessagingException
     *     if there is a problem setting the headers.
     */
    public static final void setHeadersForAutomaticallyGeneratedMessage(Message message, boolean shouldSetNullReturnPath)
        throws MessagingException {
        if (shouldSetNullReturnPath) {
            setNullReturnPath(message);
        }
        message.setHeader(
            EmailHeaders.NAME_AUTO_SUBMITTED, EmailHeaders.AUTO_SUBMITTED_VALUE_AUTO_GENERATED
        );
        setHeadersForAutomaticallyGeneratedMessageExchange(message);
    }

    /**
     * Sets headers on the given {@link Message} to indicate to the recipient client and mail systems that the message
     * was automatically generated in reply to another message, and that automatic responses to this message should be
     * suppressed.
     *
     * <p>
     * Currently, this method sets the "Auto-Submitted" email header, as well as the Microsoft Exchange specific
     * "X-Auto-Response-Suppress" header. Additionally, if the server configuration specifies that it should do so, it
     * also sets the header
     *
     * <pre>
     * Return-Path: <>
     * </pre>
     *
     * <p>
     * Per <a href="https://tools.ietf.org/html/rfc3834#section-2">RFC 3834 section 2</a>, specifying an empty return
     * path is intended to prevent responders on destination servers from generating replies:
     *
     * <blockquote>
     * Responders MUST NOT generate any response for which the destination of that response would be a null address
     * (e.g., an address for which SMTP MAIL FROM or Return-Path is <>), since the response would not be delivered to a
     * useful destination.
     * </blockquote>
     *
     * <p>
     * This header is not supposed to be set by the email client, but rather by the SMTP server, and in fact many SMTP
     * servers will not accept messages that already have a Return-Path header, or at least one that doesn't match the
     * sender. Sometimes, even when such a message can be sent, a mismatched Return-Path and sender address may cause
     * the message to be treated as spam. Nonetheless, because it can help to avoid automatically generated responses
     * being sent back to TeamPage, it is supported. But because the behavior can prevent messages from being sent or
     * received, a server administrator must opt into it by changing the appropriate setting.
     *
     * <p>
     * Note that this method is mainly intended for use with "bounce" messages automatically generated by TeamPage in
     * reply to incoming messages that cannot be processed (e.g., because the sending user lacked permission to create
     * the corresponding entry in the target space).
     *
     * @param message
     *     the automatically generated email {@link Message}.
     * @throws MessagingException
     *     if there is a problem setting the headers.
     */
    public static final void setHeadersForAutomaticallyGeneratedReplyMessage(Message message, boolean shouldSetNullReturnPath)
        throws MessagingException {
        if (shouldSetNullReturnPath) {
            setNullReturnPath(message);
        }
        setHeadersForAutomaticallyGeneratedMessageExchange(message);
        message.setHeader(EmailHeaders.NAME_AUTO_SUBMITTED, EmailHeaders.AUTO_SUBMITTED_VALUE_AUTO_REPLIED);
    }

    /**
     * Sets the "X-Auto-Response-Suppress" header on the given automatically generated {@link Message}.
     *
     * <p>
     * This is a Microsoft Exchange specific header. See <a
     * href="http://msdn.microsoft.com/en-us/library/ee219609(v=exchg.80).aspx">this page</a> for some information.
     *
     * @param message
     *     the automatically generated email {@link Message}.
     * @throws MessagingException
     *     if there is a problem setting the header.
     */
    public static final void setHeadersForAutomaticallyGeneratedMessageExchange(Message message)
        throws MessagingException {
        message.setHeader(
            EmailHeaders.NAME_X_AUTO_RESPONSE_SUPPRESS,
            EmailHeaders.X_AUTO_RESPONSE_SUPPRESS_VALUE_ALL
        );
    }

    /**
     * Sets the "Return-Path" header to the following value:
     *
     * <pre>
     * <>
     * </pre>
     *
     * <p>
     * Quoting from RFC 3834 (<a href="https://tools.ietf.org/html/rfc3834">RFC 3834</a>) which covers "Recommendations
     * for Automatic Responses to Electronic Mail":
     *
     * <blockquote>
     * Responders MUST NOT generate any response for which the destination of that response would be a null address
     * (e.g., an address for which SMTP MAIL FROM or Return-Path is <>), since the response would not be delivered to a
     * useful destination. Responders MAY refuse to generate responses for addresses commonly used as return addresses
     * by responders - e.g., those with local- parts matching "owner-*", "*-request", "MAILER-DAEMON", etc. Responders
     * are encouraged to check the destination address for validity before generating the response, to avoid generating
     * responses that cannot be delivered or are unlikely to be useful.
     * </blockquote>
     *
     * @param message
     *     the automatically generated email {@link Message}.
     * @throws MessagingException
     *     if the header cannot be set.
     */
    public static final void setNullReturnPath(Message message) throws MessagingException {
        message.setHeader(EmailHeaders.NAME_RETURN_PATH, "<>");
    }

    /**
     * Retrieves the Address objects representing the requested type of recipients of the given javax.mail.Message.
     *
     * @param message
     *     the javax.mail.Message whose recipients are to be retrieved.
     * @param type
     *     the type of recipients to be retrieved.
     * @return either an Address[] representing the requested recipients, or null if there was an error retrieving those
     *     recipients. If there was an error, diagnostic information will be added to the traction.log and/or debug.log
     *     files.
     */
    public static final List<Address> getRecipients(Message message, Message.RecipientType type) {
        try {
            Address[] ret = message.getRecipients(type);
            if (ret == null) {
                return null;
            }
            return Arrays.asList(ret);
        }
        catch (Exception e) {
            LOGGER.warn("Unable to retrieve recipients of type {}", type, e);
        }
        return null;
    }

    /**
     * Retrieves the Address objects representing any recipients of the message from the auxiliary fields "Delivered-To"
     * and "X-Original-To" that are not already in the given To or Cc recipients. Since Bcc recipients are not preserved
     * on a "Bcc" field on a message that is actually delivered to a recipient, this method can be helpful to retrieve
     * the Address representing the recipient of a message when that message was delivered to a mailbox because the
     * sender specified an address of that mailbox in a Bcc field.
     *
     * @param headers
     *     the EmailHeaders from the received message.
     * @return the Address objects that were parsed from the auxiliary headers, if any; null otherwise.
     */
    public static final SequencedSet<Address> getOtherRecipients(EmailHeaders headers) {

        SequencedSet<Address> toAndCc = new LinkedHashSet<>();
        CollectionsUtil.copy(headers.getAddresses(EmailHeaders.NAME_TO), toAndCc);
        CollectionsUtil.copy(headers.getAddresses(EmailHeaders.NAME_CC), toAndCc);

        SequencedSet<Address> ret = new LinkedHashSet<>();
        addIfNotInToOrCc(headers.getAddresses(EmailHeaders.NAME_DELIVERED_TO), toAndCc, ret);
        addIfNotInToOrCc(headers.getAddresses(EmailHeaders.NAME_X_ORIGINAL_TO), toAndCc, ret);
        return CollectionsUtil.unmodifiableSequencedSet(ret);

    }

    public static final Address getDummyEmailAddress() throws AddressException {
        return new InternetAddress("badaddress@unrecognized.domain");
    }

    public static final List<InternetAddress> parseAddresses(String addressListSpec) throws AddressException {
        if (StringUtils.isBlank(addressListSpec)) {
            return null;
        }
        return Arrays.asList(InternetAddress.parse(addressListSpec));
    }

    public static final List<InternetAddress> safeParseAddresses(String addressListSpec) {
        try {
            return parseAddresses(addressListSpec);
        }
        catch (AddressException e) {
            LOGGER.warn(
                "Unable to parse email addresses from {}", StringUtil.truncatedToStringForLog(addressListSpec), e
            );
            return null;
        }
    }

    /**
     * Returns true if an automatic response to a message with the given headers would not be advisable. This is based
     * upon whether there are any headers that indicate that the message was generated automatically, or that the
     * sending system requested that the receiving system should not generate an automatic response.
     *
     * @param headers
     *     the headers to be tested.
     * @return true true if an automatic response to a message with the given headers would not be advisable; false if
     *     an automatic response would be okay.
     */
    public static final boolean shouldSuppressAutomaticResponse(EmailHeaders headers) {
        String autoSubmitted = headers.getHeader(EmailHeaders.NAME_AUTO_SUBMITTED);
        if (autoSubmitted != null && !autoSubmitted.equals("no")) {
            return true;
        }
        String returnPath = headers.getHeader(EmailHeaders.NAME_RETURN_PATH);
        if ("<>".equals(returnPath)) {
            return true;
        }
        String suppressAutoResponses = headers.getHeader(EmailHeaders.NAME_X_AUTO_RESPONSE_SUPPRESS);
        if ("AutoReply".equals(suppressAutoResponses) || "All".equals(suppressAutoResponses)) {
            return true;
        }
        return false;
    }

    public static final void dumpNamedHeaders(StringBuilder buffer, EmailHeaders headers, String headerName) {
        buffer.append(headerName);
        List<String> values = headers.getHeaders(headerName);
        if (CollectionsUtil.isEmpty(values)) {
            buffer.append(": [no headers with this name]");
            return;
        }
        buffer.append(": ");
        StringUtil.getNullSkippingJoiner(" | ").appendTo(buffer, values);
    }

    public static final String getDomain(String address) {
        if (address == null) {
            return null;
        }
        address = address.trim();
        int at = address.indexOf("@");
        if (at == -1 || at == address.length() - 1) {
            return "";
        }
        return address.substring(at + 1);
    }

    /**
     * Delegates to {@link MimeUtility#encodeText(String, String, String)} a RFC 822 "text" token into mail-safe form as
     * per RFC 2047, using the UTF-8 character set.
     *
     * <p>
     * The purpose of this method is to facilitate using this feature without having to litter client code with
     * boilerplate catch clauses for the {@link UnsupportedEncodingException} thrown by the underlying method, when it
     * is nearly guaranteed that will never happen, especially when the UTF-8 character set and no special encoding will
     * be used in more or less every case.
     *
     * @return the safely encoded text token.
     */
    public static final String getSafelyEncodedToken(String text) {
        return getSafelyEncodedToken(text, StandardCharsets.UTF_8.name());
    }

    /**
     * Delegates to {@link MimeUtility#encodeText(String, String, String)} a RFC 822 "text" token into mail-safe form as
     * per RFC 2047, using the requested character set if possible.
     *
     * <p>
     * The purpose of this method is to facilitate using this feature without having to litter client code with
     * boilerplate catch clauses for the {@link UnsupportedEncodingException} thrown by the underlying method, when it
     * is nearly guaranteed that will never happen.
     *
     * @return the safely encoded text token, if possible; the original text otherwise.
     */
    public static final String getSafelyEncodedToken(String text, String charsetName) {
        if (StringUtils.isBlank(text)) {
            return text;
        }
        try {
            return MimeUtility.encodeText(text, charsetName, null);
        }
        catch (UnsupportedEncodingException e) {
            LOGGER.warn(
                "Failed to safely encode text {} for charset {}",
                StringUtil.truncatedToStringForLog(text),
                charsetName,
                e
            );
        }
        return null;
    }

    public static final String getFriendlyNameWithSafelyEncodedDisplayName(String address, String displayName) {
        if (StringUtils.isBlank(displayName)) {
            return address;
        }
        return MailUtil.makeFriendlyNameAddress(address, getSafelyEncodedToken(displayName));
    }

    public static final void closeFolder(Folder folder, boolean expunge) {

        if (folder == null || !folder.isOpen()) {
            return;
        }

        Object debugFolder = ObjectUtil.safeToStringObject(folder);
        LOGGER.debug("Closing the folder {}", debugFolder);
        try {
            folder.close(expunge);
            if (expunge) {
                LOGGER.debug("Successfully closed the folder {}, expunging deleted messages.", debugFolder);
            }
            else {
                LOGGER.debug("Successfully closed the folder {}, leaving deleted messages.", debugFolder);
            }
        }
        catch (Exception e) {
            LOGGER.warn("Failed to close the folder {}", debugFolder, e);
        }

    }

    public static final void closeStore(Store store) {

        if (store == null || !store.isConnected()) {
            return;
        }

        Object debugStore = ObjectUtil.safeToStringObject(store);
        LOGGER.debug("Closing the store {}", debugStore);
        try {
            store.close();
            LOGGER.debug("Successfully closed the store {}", debugStore);
        }
        catch (Exception e) {
            LOGGER.warn("Failed to close the store {}", debugStore, e);
        }

    }

    public static final void closeTransport(Transport transport) {

        if (transport == null || !transport.isConnected()) {
            return;
        }

        Object debugTransport = ObjectUtil.safeToStringObject(transport);
        LOGGER.debug("Closing the transport {}", debugTransport);
        try {
            transport.close();
            LOGGER.debug("Successfully closed the transport {}", debugTransport);
        }
        catch (Exception e) {
            LOGGER.warn("Failed to close the transport {}", debugTransport, e);
        }

    }

    public static final String headerToString(Header header) {
        if (header == null) {
            return null;
        }
        return header.getName() + ": " + header.getValue();
    }

    public static final Multimap<String,Header> headerLinesToDecodedHeaderMap(Iterable<String> rawHeaderLines) {
        return headersToHeaderMap(Iterables.transform(rawHeaderLines, MailUtil::encodedHeaderLineToHeader));
    }

    public static final Multimap<String,Header> headerLinesToHeaderMap(Iterable<String> headerLines) {
        return headersToHeaderMap(Iterables.transform(headerLines, MailUtil::headerLineToHeader));
    }

    public static final Multimap<String,Header> headersToHeaderMap(Iterable<Header> headers) {
        ImmutableListMultimap.Builder<String,Header> builder = ImmutableListMultimap.builder();
        for (Header header : headers) {
            builder.put(header.getName().toLowerCase(), header);
        }
        return builder.build();
    }

    public static final Multimap<String,Header> getRfc2047DecodedHeaders(Multimap<String,Header> headersEncoded) {
        return headersToHeaderMap(Iterables.transform(headersEncoded.values(), MailUtil::getRfc2047DecodedHeader));
    }

    /**
     * Creates a DataSource from a String.
     *
     * @param text
     *     the text to be used as the DataSource.
     * @param charset
     *     the {@link Charset} that should be used to convert the string to raw bytes.
     * @param type
     *     The content type for the data source.
     * @return a DataSource based upon the bytes of the string using the specified {@link Charset}.
     */
    public static final DataSource getDataSource(String name, String text, Charset charset, String type) {
        return InputStreamDataSource.createInstance(name, text, charset, type);
    }

    /**
     * Creates a DataSource from a byte array.
     *
     * @param data
     *     the exact bytes to use as the data source.
     * @param type
     *     the content type for the data source.
     * @return a DataSource backed by the given byte array.
     */
    public static final DataSource getDataSource(String name, byte[] data, String type) {
        Objects.requireNonNull(data, "data");
        return InputStreamDataSource.createInstance(name, data, type);
    }

    /**
     * Creates a DataSource that will use
     * {@link FileResource#getInputStream() the InputStream for the given file resource}. The {@link InputStream}
     * retrieval is deferred until it is needed.
     *
     * @param file
     *     representing the file resource to be used.
     * @return a DataSource based upon the content of the given file resource.
     */
    public static final DataSource getDataSource(FileResource file) {
        Objects.requireNonNull(file, "file");
        return InputStreamDataSource.createInstance(file);
    }

    private static final boolean headerLineRequiresRfc2047Decoding(String headerLine) {
        if (headerLineMatches(headerLine, EmailHeaders.NAME_SUBJECT) ||
            headerLineMatches(headerLine, EmailHeaders.NAME_CONTENT_DESCRIPTION)) {
            return true;
        }
        return false;
    }

    private static final boolean headerRequiresRfc2047Decoding(Header header) {
        String name = header.getName();
        if (EmailHeaders.NAME_SUBJECT.equals(name) || EmailHeaders.NAME_CONTENT_DESCRIPTION.equals(name)) {
            return true;
        }
        return false;
    }

    /**
     * Returns true if the given header line appears to have a name matching the requested name.
     *
     * @param headerLine
     *     the header line in which the match may appear.
     * @param headerName
     *     the header name to match.
     * @return true if the given header line appears to have a name matching the requested name; false otherwise.
     */
    private static final boolean headerLineMatches(String headerLine, String headerName) {
        if (Strings.CI.startsWith(headerLine, headerName + HEADER_DELIMITER_CHAR)) {
            return true;
        }
        return false;
    }

    private static final Iterable<String> getEmailAddressTokens(String emailAddressesSpec) {
        return () -> new EmailAddressesSpecTokenizer(emailAddressesSpec);
    }

    private static final void addIfNotInToOrCc(List<? extends Address> addresses, Set<? super Address> toAndCc, Set<? super Address> addTo) {
        if (addresses == null) {
            return;
        }
        for (Address address : addresses) {
            if (toAndCc.contains(address)) {
                continue;
            }
            addTo.add(address);
        }
    }

}
