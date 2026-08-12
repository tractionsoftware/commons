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

package com.tractionsoftware.commons.codec;

import com.tractionsoftware.commons.text.CharBasedFilteringTextMapper;
import org.apache.commons.lang3.StringUtils;

/**
 * Provides a handful of helpers related to JavaScript encoding.
 *
 * @author Andy Keller, Dave Shepperton
 */
public final class JavaScriptEncodingUtil {

    public static final String JAVASCRIPT_FILE_EXTENSION = ".js";

    public enum CharacterRequiringEscaping {

        APOSTROPHE('\'', true),

        QUOTATION_MARK('"', true),

        BACKSLASH('\\'),

        SLASH('/'),

        NEW_LINE('n'),

        CARRIAGE_RETURN('r'),

        TAB('t'),

        FORM_FEED('f'),

        BACKSPACE('b');

        public static CharacterRequiringEscaping get(char c) {
            return switch (c) {
                case '\'' -> APOSTROPHE;
                case '"' -> QUOTATION_MARK;
                case '\\' -> BACKSLASH;
                case '/' -> SLASH;
                case '\n' -> NEW_LINE;
                case '\r' -> CARRIAGE_RETURN;
                case '\t' -> TAB;
                case '\f' -> FORM_FEED;
                default -> null;
            };
        }

        public static String getRequiredEscapeSequence(char c) {
            CharacterRequiringEscaping value = get(c);
            if (value == null) {
                return null;
            }
            return value.escapeSequence;
        }

        private final String escapeSequence;

        private final boolean quotationMark;

        CharacterRequiringEscaping(char escapingChar) {
            this(escapingChar, false);
        }

        CharacterRequiringEscaping(char escapingChar, boolean quotationMark) {
            this.escapeSequence = "\\" + escapingChar;
            this.quotationMark = quotationMark;
        }

        @Override
        public final String toString() {
            return name() + " (" + escapeSequence + ")";
        }

        public final String getEscapeSequence() {
            return escapeSequence;
        }

        public final boolean isQuotationMark() {
            return quotationMark;
        }

    }

    private JavaScriptEncodingUtil() {
    }

    /**
     * Returns a version of the given text with all occurrences of certain characters escaped to ensure that the result
     * is a valid JavaScript literal. Specifically, the following characters are escaped:
     *
     * <ul>
     * <li>apostrophe (a.k.a., single quotation mark)</li>
     * <li>quotation mark (i.e., double quotation mark)</li>
     * <li>backslash</li>
     * <li>slash</li>
     * <li>new line</li>
     * <li>carriage return</li>
     * <li>tab</li>
     * <li>form feed</li>
     * <li>backspace</li>
     * </ul>
     *
     * @param text
     *     the input text.
     * @return a version of the given text with a transformation applied escaping any appearances of the following
     *     characters to make the result a valid JavaScript literal.
     */
    public static String getJavascriptLiteral(CharSequence text) {
        return getJavascriptLiteral(text, false);
    }

    /**
     * Returns a version of the given text with all occurrences of certain characters escaped or entity encoded to
     * ensure that the result is both a valid JavaScript literal and suitable for inclusion in an HTML document.
     *
     * <p>
     * Specifically, the following characters are escaped:
     *
     * <ul>
     * <li>apostrophe (a.k.a., single quotation mark)</li>
     * <li>quotation mark (i.e., double quotation mark)</li>
     * <li>backslash</li>
     * <li>slash</li>
     * <li>new line</li>
     * <li>carriage return</li>
     * <li>tab</li>
     * <li>form feed</li>
     * <li>backspace</li>
     * </ul>
     *
     * <p>And the following characters are entity encoded:
     *
     * <ul>
     * <li>less than</li>
     * <li>greater than</li>
     * <li>ampersand</li>
     * </ul>
     *
     * @param text
     *     the input text.
     * @return a version of the given text with all occurrences of certain characters escaped or entity encoded the
     *     following characters to make the result both a valid JavaScript literal and suitable for inclusion in an HTML
     *     document.
     */
    public static String getHtmlCompatibleJavascriptLiteral(CharSequence text) {
        return getJavascriptLiteral(text, true);
    }

    /**
     * Returns a version of the given text with all occurrences of certain characters escaped to ensure that the result
     * is a valid JSON literal; and, if an optional HTML-compatible version is requested, entity encoded to ensure that
     * the result is also suitable for inclusion in an HTML document.
     *
     * <p>
     * Specifically, the following characters are escaped:
     *
     * <ul>
     * <li>apostrophe (a.k.a., single quotation mark)</li>
     * <li>quotation mark (i.e., double quotation mark)</li>
     * <li>backslash</li>
     * <li>slash</li>
     * <li>new line</li>
     * <li>carriage return</li>
     * <li>tab</li>
     * <li>form feed</li>
     * <li>backspace</li>
     * </ul>
     *
     * <p>And the following characters are entity encoded if an HTML-compatible version is requested:
     *
     * <ul>
     * <li>less than</li>
     * <li>greater than</li>
     * <li>ampersand</li>
     * </ul>
     *
     * @param text
     *     the input text.
     * @param htmlCompatible
     *     indicating whether the
     * @return a version of the given text with all occurrences of certain characters escaped or entity encoded the
     *     following characters to make the result both a valid JSON literal and suitable for inclusion in an HTML
     *     document.
     */
    public static String getJavascriptLiteral(CharSequence text, boolean htmlCompatible) {
        if (text == null) {
            return null;
        }
        if (text.isEmpty()) {
            return "";
        }
        if (htmlCompatible) {
            return CharBasedFilteringTextMapper.replace(text, JavaScriptEncodingUtil::getHtmlCompatibleLiteralReplacement);
        }
        return CharBasedFilteringTextMapper.replace(text, CharacterRequiringEscaping::getRequiredEscapeSequence);
    }

    public static void printJavascriptLiteral(Appendable out, CharSequence text) {
        printJavascriptLiteral(out, text, false);
    }

    public static void printHtmlCompatibleJavascriptLiteral(Appendable out, CharSequence text) {
        printJavascriptLiteral(out, text, true);
    }

    public static void printJavascriptLiteral(Appendable out, CharSequence text, boolean htmlCompatible) {
        if (StringUtils.isNotEmpty(text)) {
            if (htmlCompatible) {
                CharBasedFilteringTextMapper.replace(text, out, JavaScriptEncodingUtil::getHtmlCompatibleLiteralReplacement);
            }
            else {
                CharBasedFilteringTextMapper.replace(text, out, CharacterRequiringEscaping::getRequiredEscapeSequence);
            }
        }
    }

    private static String getHtmlCompatibleLiteralReplacement(char c) {
        return switch (c) {
            case '<' -> HtmlEncodingUtil.SimpleHtmlEntity.LESS_THAN.getEncoding();
            case '>' -> HtmlEncodingUtil.SimpleHtmlEntity.GREATER_THAN.getEncoding();
            case '&' -> HtmlEncodingUtil.SimpleHtmlEntity.AMPERSAND.getEncoding();
            default -> CharacterRequiringEscaping.getRequiredEscapeSequence(c);
        };
    }

}
