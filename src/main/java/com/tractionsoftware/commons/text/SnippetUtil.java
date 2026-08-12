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
import com.tractionsoftware.commons.lang.StringUtil;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import org.apache.commons.lang3.StringUtils;

import java.util.function.Supplier;

/**
 * Snippetizing is effectively a word/token-aware version of truncation. This means that these methods do their best to
 * truncate along a word/token boundary, rather than in the middle of a word. Tokenization of the input is performed by
 * the {@link TextTokenizerService#getTokens(String)}, using whatever tokenizer is available for this server.
 *
 * <p>
 * This implementation is a reasonably efficient version which is code point aware. It normalizes and collapses
 * whitespace via {@link StringUtil#collapseAndNormalizeWhitespace(String, boolean)}.
 *
 * <p>
 * The only possible exception to the token-aware truncation is the case of a very long final token that can't fit into
 * the remaining available space of at least 12 characters. In that case, that long token is truncated to make it fit,
 * resulting in a truncation happening before the end of the token.
 *
 * @author Dave Shepperton
 */
public final class SnippetUtil {

    /*
     * Not instantiable.
     */
    private SnippetUtil() {
    }

    private static final int MIN_FINAL_TOKEN_SIZE_FOR_SIMPLE_TRUNCATION = 12;

    /**
     * Returns a snippet from the given text.
     *
     * @param text
     *     the text to be truncated, which really must be plain text (not HTML or even text with HTML entities).
     * @param requestedMaximumLength
     *     the requested maximum length of the truncated output in terms of code points, including the space for the
     *     ellipses.
     * @return a snippet of the given text.
     */
    @Nonnull
    public static final String getSnippet(@Nullable String text, int requestedMaximumLength) {
        return getSnippet(text, requestedMaximumLength, StringUtil.ELLIPSES_MULTI_CHARACTER);
    }

    /**
     * Returns a snippet from the given text.
     *
     * @param text
     *     the text to be truncated, which really must be plain text (not HTML or even text with HTML entities).
     * @param requestedMaximumLength
     *     the requested maximum length of the truncated output in terms of code points, including the space for the
     *     ellipses.
     * @param ellipses
     *     to use at the end of truncated output if necessary.
     * @return a truncated version of the input text which does its best to truncate at word/token boundary rather than
     *     in the middle of words, and which is as long as possible while still fitting into the requested maximum
     *     length, including any ellipses.
     */
    @Nonnull
    public static final String getSnippet(@Nullable String text, int requestedMaximumLength, @Nullable String ellipses) {
        return getSnippet(text, requestedMaximumLength, Suppliers.ofInstance(ellipses));
    }

    /**
     * A fairly efficient version of truncation that is word/token-aware. This means that it does its best to truncate
     * along a word/token boundary, rather than in the middle of a word. The only exception to this is in the case of a
     * very long token, which is not the final token, failing to fit into the remaining space available for the maximum
     * requested length: if there are at least 12 characters still available before the ellipses have to be inserted but
     * the token is still too long to fit into that space, then that token is truncated to make it fit, resulting in a
     * truncation happening in the middle of the word/token.
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
    @Nonnull
    public static final String getSnippet(@Nullable String text, int requestedMaximumLength, @Nullable Supplier<String> ellipsesProvider) {

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

        String ellipses;
        if (ellipsesProvider == null) {
            ellipses = StringUtil.ELLIPSES_MULTI_CHARACTER;
        }
        else {
            ellipses = StringUtils.defaultString(ellipsesProvider.get());
        }
        int ellipsesLen = ellipses.length();
        if (ellipsesLen == requestedMaximumLength) {
            return ellipses;
        }
        if (ellipsesLen > requestedMaximumLength) {
            return ellipses.substring(requestedMaximumLength);
        }

        // This is a judgment call as to how much extra content we need at the tail to be able to make sure we don't cut
        // off too much of the last token.
        text = StringUtils.abbreviate(text, requestedMaximumLength + 35);

        return truncateTokenizedImpl(requestedMaximumLength, ellipses, TextTokenizerService.get().getTokens(text));

    }

    private static final String truncateTokenizedImpl(int requestedMaximumLength, String ellipses, Iterable<String> tokens) {

        // The available length takes the length of the ellipses into account.
        int availableLength = requestedMaximumLength - ellipses.length();
        StringBuilder result = new StringBuilder(requestedMaximumLength);

        for (String token : tokens) {
            int tokenLen = token.codePointCount(0, token.length());
            if (availableLength >= tokenLen) {
                result.append(token);
                availableLength -= tokenLen;
            }
            else {
                // If there's still at least 12 characters of space available for a token (before the ellipses), but
                // there was a token that didn't fit in its entirety even with that much space, just use ordinary
                // truncation to shorten it. This ensures we don't get weird empty or very short output if very long
                // tokens appear in the input.
                if (availableLength >= MIN_FINAL_TOKEN_SIZE_FOR_SIMPLE_TRUNCATION) {
                    result.append(token, 0, availableLength);
                }
                break;
            }
        }

        result.append(ellipses);
        return result.toString();

    }

}
