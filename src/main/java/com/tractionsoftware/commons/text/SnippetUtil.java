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

package com.tractionsoftware.commons.text;

import com.google.common.base.Suppliers;
import com.tractionsoftware.commons.lang.ObjectsUtil;
import com.tractionsoftware.commons.lang.StringUtil;
import jakarta.annotation.Nonnull;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.StringEscapeUtils;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.PrimitiveIterator;
import java.util.function.Supplier;

public final class SnippetUtil {

    /*
     * Not instantiable.
     */
    private SnippetUtil() {
    }

    public static final String ELLIPSES_MULTI_CHARACTER = "...";

    public static final String ELLIPSES_SINGLE_CHARACTER = "…";

    private static final record TruncatedToString(Object object, int maxLength, String ellipses) {

        @Nonnull
        @Override
        public final String toString() {
            return truncate(ObjectsUtil.safeToString(object, "?"), maxLength, ellipses);
        }

    }

    /**
     * Computes a simple text snippet from the given HTML, no longer than the requested maximum length.
     *
     * @param html
     *     the HTML to be snippetized.
     * @param maximumLength
     *     the maximum length of the snippet.
     * @param ellipses
     *     the ellipses to use in the event the content is truncated.
     * @return a simple text snippet from the given HTML, no longer than the requested maximum length.
     */
    public static final String getTextSnippetFromHtml(String html, int maximumLength, String ellipses) {
        if (maximumLength <= 0 || StringUtils.isBlank(html)) {
            return "";
        }
        return getTextSnippetFromText(getUnescapedHtmlForSnippet(html, maximumLength), maximumLength, ellipses);
    }

    /**
     * Applies an HTML-to-text transformation appropriate for preparing the given HTML content to be snippetized.
     *
     * @param html
     *     the HTML to be turned into a text-only equivalent for snippetizing.
     * @param maximumLength
     *     the maximum length of the snippet.
     * @return a text-only version of the input HTML suitable for snippetizing.
     * @implNote We would like to avoid spending time applying HTML entity unescaping to text that is only going to
     *     be discarded. Since the unescape utility code we're using does not allow us to stop after we've got a desired
     *     amount of output, we take special pains to only unescape text if we know we need to, and then sending only as
     *     much HTML text as we think can possibly need to be unescaped to fulfill the requirement for the maximum
     *     snippet length.
     */
    private static final String getUnescapedHtmlForSnippet(String html, int maximumLength) {

        // Extract text by removing HTML tags and any
        // TeamPage-specific tokens which shouldn't appear in the
        // snippet.
        String htmlText = TextTransformerService.get().textExtractionSnippets().transform(html).trim();
        if (StringUtils.isBlank(htmlText)) {
            return htmlText;
        }

        // Maximum expected amount of a text-only string we'll need
        // for snippetizing.
        int maximumLengthOfText = maximumLength + 50;

        int firstAmpersand = htmlText.indexOf('&');
        if (firstAmpersand == -1) {
            if (htmlText.length() > maximumLengthOfText) {
                return htmlText.substring(0, maximumLengthOfText);
            }
            return htmlText;
        }

        String textHead = htmlText.substring(0, firstAmpersand);
        int textHeadLen = textHead.length();
        if (textHeadLen >= maximumLengthOfText) {
            return textHead.substring(0, maximumLengthOfText);
        }

        if (textHeadLen > 0) {
            // Take into account the text-only part we took from the
            // "head" of the string by removing that length from the
            // maximum required text length, and by removing that head
            // text from the remaining HTML text.
            maximumLengthOfText -= textHeadLen;
            htmlText = htmlText.substring(textHeadLen);
        }

        // We'd like to make sure that we supply enough possibly
        // entity-encoded text to get a sufficient amount of plain
        // text output. We go with a factor of 10 here, which is
        // probably overkill in practice, but safe to cover weird
        // corner cases.
        int maximumLengthOfRemainingHtmlText = maximumLengthOfText * 10;
        if (htmlText.length() > maximumLengthOfRemainingHtmlText) {
            htmlText = htmlText.substring(0, maximumLengthOfRemainingHtmlText);
        }

        String unescapedTail = StringEscapeUtils.unescapeHtml4(htmlText);
        if (unescapedTail.length() > maximumLengthOfText) {
            unescapedTail = unescapedTail.substring(0, maximumLengthOfText);
        }

        if (textHeadLen == 0) {
            return unescapedTail;
        }
        return textHead + unescapedTail;

    }

    public static final String getTextSnippetFromText(String text, int maximumLength) {
        return getTextSnippetFromText(text, maximumLength, ELLIPSES_MULTI_CHARACTER);
    }

    /**
     * Computes a simple text snippet from the given text, no longer than the requested maximum length.
     *
     * @param text
     *     the text to be snippetized.
     * @param maximumLength
     *     the maximum length of the snippet.
     * @param ellipses
     *     the ellipses to use in the event the content is truncated.
     * @return a simple text snippet from the given HTML no longer than the requested maximum length.
     */
    public static final String getTextSnippetFromText(String text, int maximumLength, String ellipses) {
        return truncateTokenized(text, maximumLength, ellipses);
    }

    /**
     * A method similar to {@link StringUtils#abbreviate(String, int)}, but with some slight variation in behavior,
     * including tolerance of apparently invalid input parameters. This version uses empty ellipses.
     *
     * <p>
     * Note that this truncation also normalizes and collapses whitespace, per
     * {@link StringUtil#collapseAndNormalizeWhitespace(String, boolean)}.
     *
     * @param text
     *     the text to be truncated. This really should be plain text (not HTML or even text with HTML entities), since
     *     this method does not do any of the parsing or tokenization that would be required for proper handling of
     *     HTML.
     * @param maximumLength
     *     the maximum length of the truncated output, including the space for the ellipses.
     * @return a truncated version of the input text which is as long as possible while still fitting into the requested
     *     maximum length.
     */
    public static final String truncate(String text, int maximumLength) {
        return truncate(text, maximumLength, "");
    }

    /**
     * A method similar to {@link StringUtils#abbreviate(String, int)}, but with some slight variation in behavior,
     * including tolerance of apparently invalid input parameters, and the ability to specify custom text for the
     * ellipses.
     *
     * <p>
     * Note that this truncation also normalizes and collapses whitespace, per
     * {@link StringUtil#collapseAndNormalizeWhitespace(String, boolean)}.
     *
     * @param text
     *     the text to be truncated. This really should be plain text (not HTML or even text with HTML entities), since
     *     this method does not do any of the parsing or tokenization that would be required for proper handling of
     *     HTML.
     * @param maximumLength
     *     the maximum length of the truncated output, including the space for the ellipses.
     * @param ellipses
     *     to use at the end of truncated output if necessary.
     * @return a truncated version of the input text which is as long as possible while still fitting into the requested
     *     maximum length, including any ellipses.
     */
    public static final String truncate(String text, int maximumLength, String ellipses) {
        return truncate(text, maximumLength, Suppliers.ofInstance(ellipses));
    }

    /**
     * A method similar to {@link StringUtils#abbreviate(String, int)}, but with some slight variation in behavior,
     * including tolerance of apparently invalid input parameters, and the ability to specify custom text for the
     * ellipses which is only retrieved when needed.
     *
     * <p>
     * Note that this truncation also normalizes and collapses whitespace, per
     * {@link StringUtil#collapseAndNormalizeWhitespace(String, boolean)}.
     *
     * @param text
     *     the text to be truncated. This really should be plain text (not HTML or even text with HTML entities), since
     *     this method does not do any of the parsing or tokenization that would be required for proper handling of
     *     HTML.
     * @param requestedMaximumLength
     *     the maximum length of the truncated output, including the space for the ellipses.
     * @param ellipsesProvider
     *     supplies the ellipses to use at the end of truncated output if necessary.
     * @return a truncated version of the input text which is as long as possible while still fitting into the requested
     *     maximum length, including any ellipses.
     */
    public static final String truncate(String text, int requestedMaximumLength, Supplier<String> ellipsesProvider) {

        if (requestedMaximumLength <= 0) {
            return "";
        }

        text = StringUtil.collapseAndNormalizeWhitespace(text, true);
        if (StringUtils.isEmpty(text)) {
            return "";
        }

        if (text.length() <= requestedMaximumLength) {
            return text;
        }

        String ellipses = StringUtils.defaultString(ellipsesProvider.get());
        int ellipsesLen = ellipses.length();
        if (ellipsesLen == requestedMaximumLength) {
            return ellipses;
        }
        if (ellipsesLen > requestedMaximumLength) {
            return ellipses.substring(requestedMaximumLength);
        }
        return text.substring(0, requestedMaximumLength - ellipses.length()) + ellipses;

    }

    /**
     * A fairly efficient version of truncation that is word/token-aware. This means that it does its best to truncate
     * along a word/token boundary, rather than in the middle of a word. The only exception to this is in the case of a
     * very long token, which is not the final token, failing to fit into the remaining space available for the maximum
     * requested length: if there are at least 12 characters still available before the ellipses have to be inserted but
     * the token is still too long to fit into that space, then that token is truncated to make it fit, resulting in a
     * truncation happening in the middle of the word/token.
     *
     * <p>
     * Note that this truncation also normalizes and collapses whitespace, per
     * {@link StringUtil#collapseAndNormalizeWhitespace(String, boolean)}.
     *
     * <p>
     * The tokenization of the input is performed by the {@link TextTokenizerService#getTokens(String)}, using whatever
     * tokenizer is available for this server.
     *
     * @param text
     *     the text to be truncated, which really must be plain text (not HTML or even text with HTML entities).
     * @param requestedMaximumLength
     *     the requested maximum length of the truncated output, including the space for the ellipses.
     * @param ellipses
     *     to use at the end of truncated output if necessary.
     * @return a truncated version of the input text which does its best to truncate at word/token boundary rather than
     *     in the middle of words, and which is as long as possible while still fitting into the requested maximum
     *     length, including any ellipses.
     */
    public static final String truncateTokenized(String text, int requestedMaximumLength, String ellipses) {
        return truncateTokenized(text, requestedMaximumLength, Suppliers.ofInstance(ellipses));
    }

    /**
     * A fairly efficient version of truncation that is word/token-aware. This means that it does its best to truncate
     * along a word/token boundary, rather than in the middle of a word. The only exception to this is in the case of a
     * very long token, which is not the final token, failing to fit into the remaining space available for the maximum
     * requested length: if there are at least 12 characters still available before the ellipses have to be inserted but
     * the token is still too long to fit into that space, then that token is truncated to make it fit, resulting in a
     * truncation happening in the middle of the word/token.
     *
     * <p>
     * Note that this truncation also normalizes and collapses whitespace, per
     * {@link StringUtil#collapseAndNormalizeWhitespace(String, boolean)}.
     *
     * <p>
     * The tokenization of the input is performed by the {@link TextTokenizerService#getTokens(String)}, using whatever
     * tokenizer is available for this server.
     *
     * @param text
     *     the text to be truncated, which really must be plain text (not HTML or even text with HTML entities).
     * @param requestedMaximumLength
     *     the requested maximum length of the truncated output, including the space for the ellipses.
     * @param ellipsesProvider
     *     supplies the ellipses to use at the end of truncated output if necessary.
     * @return a truncated version of the input text which does its best to truncate at word/token boundary rather than
     *     in the middle of words, and which is as long as possible while still fitting into the requested maximum
     *     length, including any ellipses.
     */
    public static final String truncateTokenized(String text, int requestedMaximumLength, Supplier<String> ellipsesProvider) {

        if (requestedMaximumLength <= 0) {
            return "";
        }

        text = StringUtil.collapseAndNormalizeWhitespace(text, true).trim();
        if (StringUtils.isEmpty(text)) {
            return "";
        }

        if (text.length() <= requestedMaximumLength) {
            return text;
        }

        String ellipses = StringUtils.defaultString(ellipsesProvider.get());
        int ellipsesLen = ellipses.length();
        if (ellipsesLen == requestedMaximumLength) {
            return ellipses;
        }
        if (ellipsesLen > requestedMaximumLength) {
            return ellipses.substring(requestedMaximumLength);
        }

        // This is a judgment call as to how much extra content we
        // need at the tail to be able to make sure we don't cut off
        // too much of the last token.
        text = StringUtils.abbreviate(text, requestedMaximumLength + 35);

        Iterable<String> tokens = TextTokenizerService.get().getTokens(text);

        // The available length takes the length of the ellipses into account.
        int availableLength = requestedMaximumLength - ellipses.length();
        StringBuilder result = new StringBuilder(requestedMaximumLength);

        for (String token : tokens) {
            int tokenLen = token.length();
            if (availableLength >= tokenLen) {
                result.append(token);
                availableLength -= tokenLen;
            }
            else {
                // If there's still at least 12 characters of space
                // available for a token (before the ellipses), but
                // there was a token that didn't fit in its entirety
                // even with that much space, just use ordinary
                // truncation to shorten it. This ensures we don't get
                // weird empty or very short output if very long
                // tokens appear in the input.
                if (availableLength >= 12) {
                    result.append(token, 0, availableLength);
                }
                break;
            }
        }

        result.append(ellipses);

        return result.toString();

    }

    /**
     * Truncates the given String such that the encoded bytes for the requested {@link Charset} will not exceed the
     * requested maximum length, adding the given ellipses if necessary. The idea is that the bytes required to encode
     * the resulting String in that Charset will definitely fit into the maximum byte length, including the ellipses.
     *
     * <p>
     * Unfortunately this requires scanning through the String, char value by char value. This is to ensure that all the
     * bytes required to encode the String will be properly counted; and also that the truncation doesn't happen between
     * two of the bytes required to represent a single logical character.
     *
     * @param text
     *     the String to truncate.
     * @param maximumByteSize
     *     the maximum number of bytes that are available to encode the given String using the given Charset.
     * @param charset
     *     the {@link Charset} to be used to encode the String.
     * @param ellipses
     *     the ellipses, if any, that should be appended to the resulting String at the point of truncation, if
     *     accommodated by the requested maximum byte length.
     * @return the given String such that the encoded bytes for the requested {@link Charset} will not exceed the
     *     requested maximum length, adding the given ellipses if necessary.
     */
    public static final String truncateEncodedBytes(String text, int maximumByteSize, Charset charset, String ellipses) {

        if (text == null) {
            return null;
        }

        if (maximumByteSize <= 0) {
            return "";
        }

        if (charset == null) {
            charset = StandardCharsets.UTF_8;
        }

        ellipses = StringUtils.defaultString(ellipses);

        int byteLen = 0;

        int ellipsesLen = ellipses.getBytes(charset).length;
        if (ellipsesLen > maximumByteSize) {
            ellipses = "";
            ellipsesLen = 0;
        }

        int totalMax = maximumByteSize - ellipsesLen;
        StringBuilder result = new StringBuilder(maximumByteSize);
        PrimitiveIterator.OfInt points = text.codePoints().iterator();

        while (points.hasNext()) {

            char[] oneCharacter = Character.toChars(points.nextInt());
            int characterByteLen = String.valueOf(oneCharacter).getBytes(charset).length;
            if (byteLen + characterByteLen > totalMax) {
                result.append(ellipses);
                // Un-comment this to do something with the total length outside of this loop.
//                byteLen += ellipsesLen;
                break;
            }

            byteLen += characterByteLen;
            result.append(oneCharacter);

        }

        return result.toString();

    }

    public static final Object truncatedToString(Object object) {
        return truncatedToString(object, 50);
    }

    public static final Object truncatedToString(Object object, int maxLength) {
        return truncatedToString(object, maxLength, ELLIPSES_MULTI_CHARACTER);
    }

    public static final Object truncatedToString(Object object, int maxLength, String ellipses) {
        return new TruncatedToString(object, maxLength, ellipses);
    }

}
