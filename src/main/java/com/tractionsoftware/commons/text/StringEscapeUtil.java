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

import com.google.common.base.CharMatcher;
import com.tractionsoftware.commons.lang.StringUtil;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Objects;
import java.util.function.UnaryOperator;

public final class StringEscapeUtil {

    /**
     * Not instantiable.
     */
    private StringEscapeUtil() {
    }

    public static final String DEFAULT_ESCAPED_CHARS = "\r\n,\\";

    public static final CharMatcher DEFAULT_ESCAPED_CHARS_MATCHER = CharMatcher.anyOf(DEFAULT_ESCAPED_CHARS);

    private static final Logger LOGGER = LoggerFactory.getLogger(StringEscapeUtil.class);

    private static abstract class AbstractMultiCharEscapingStringMapper {

        private final CharMatcher escape;

        private AbstractMultiCharEscapingStringMapper(CharMatcher escape) {
            this.escape = escape;
        }

        protected final boolean isEscape(char c) {
            if (escape.matches(c)) {
                return true;
            }
            return false;
        }

    }

    private static final class MultiCharEscapingStringMapper extends AbstractMultiCharEscapingStringMapper
        implements StringUtil.CharToStringMapper {

        private MultiCharEscapingStringMapper(CharMatcher escape) {
            super(escape);
        }

        @Override
        public final String getReplacement(char c) {
            if (isEscape(c)) {
                return StringEscapeUtil.getEscapeSequence(c);
            }
            return null;
        }

    }

    private static final class MultiCharUnescapingStringMapper extends AbstractMultiCharEscapingStringMapper
        implements StringUtil.CharMapper {

        private MultiCharUnescapingStringMapper(CharMatcher escape) {
            super(escape);
        }

        @Override
        public final char getReplacement(char c) {
            if (isEscape(c)) {
                return StringEscapeUtil.getLiteralCharacter(c);
            }
            return c;
        }

    }

    /**
     * @param escapeChars
     *     unescapes all characters in this string.
     */
    public static final String unescapeMultipleCharacters(CharSequence str, CharSequence escapeChars) {
        return unescapeMultipleCharacters(str, CharMatcher.anyOf(escapeChars));
    }

    public static final String unescapeMultipleCharacters(CharSequence str, CharMatcher shouldEscape) {

        if (str == null) {
            return null;
        }
        if (str.isEmpty()) {
            return "";
        }

        int len = str.length();
        StringBuilder sb = null;

        for (int i = 0; i < len; i++) {

            char c = str.charAt(i);

            if (c != '\\') {
                if (sb != null) {
                    sb.append(c);
                }
                continue;
            }

            i++;
            if (i >= len) {
                if (sb != null) {
                    sb.append(c);
                }
                continue;
            }

            // found a possible escape
            char possiblyEscaped = str.charAt(i);
            char literalChar = switch (possiblyEscaped) {
                case 't' -> '\t';
                case 'r' -> '\r';
                case 'n' -> '\n';
                case 'f' -> '\f';
                default -> possiblyEscaped;
            };

            if (shouldEscape.matches(literalChar)) {
                // one of the
                if (sb == null) {
                    sb = new StringBuilder(len);
                    sb.append(str, 0, i - 1);
                }
                sb.append(literalChar);
                continue;
            }

            if (sb != null) {
                // Keep the \, which is different than java properties file which drops a single \ before unknown
                // characters, for example, \a -> a in java where \a -> \a for us. I'm not sure it really matters, but
                // that's the way it used to be so we keep it that way until we decide that it makes a difference.
                // [ajm 23.Mar.2006]
                sb.append('\\');
                sb.append(possiblyEscaped);
            }

        }

        if (sb == null) {
            return str.toString();
        }
        return sb.toString();

    }

    public static final void unescapeMultipleCharacters(Appendable out, CharSequence str, String escaped) {

        if (StringUtils.isEmpty(str)) {
            return;
        }

        try {

            int len = str.length();

            for (int i = 0; i < len; i++) {

                char c = str.charAt(i);

                if (c != '\\') {
                    out.append(c);
                    continue;
                }

                i++;
                if (i >= len) {
                    out.append(c);
                    continue;
                }

                // found a possible escape
                char possiblyEscaped = str.charAt(i);
                char literalChar = switch (possiblyEscaped) {
                    case 't' -> '\t';
                    case 'r' -> '\r';
                    case 'n' -> '\n';
                    case 'f' -> '\f';
                    default -> possiblyEscaped;
                };

                if (escaped.indexOf(literalChar) >= 0) {
                    // one of the
                    out.append(literalChar);
                    continue;
                }

                // Keep the \, which is different than java properties file which drops a single \ before unknown
                // characters, for example, \a -> a in java where \a -> \a for us. I'm not sure it really matters, but
                // that's the way it used to be so we keep it that way until we decide that it makes a difference.
                // [ajm 23.Mar.2006]
                out.append('\\');
                out.append(possiblyEscaped);

            }

        }
        catch (IOException e) {
            LOGGER.warn("unescapeMultipleCharacters failed", e);
        }

    }

    public static final String escapeMultipleCharacters(CharSequence str, CharSequence escape) {
        Objects.requireNonNull(escape, "escape");
        return escapeMultipleCharacters(str, CharMatcher.anyOf(escape));
    }

    public static final String escapeMultipleCharacters(CharSequence str, CharMatcher escapeMatcher) {
        return escapeMultipleCharactersImpl(str, escapeMapper(escapeMatcher));
    }

    public static final UnaryOperator<String> escaper(CharMatcher escapeMatcher) {
        MultiCharEscapingStringMapper escapeMapper = new MultiCharEscapingStringMapper(escapeMatcher);
        return (text) -> escapeMultipleCharactersImpl(text, escapeMapper);
    }

    public static final StringUtil.CharToStringMapper escapeMapper(CharMatcher escapeMatcher) {
        Objects.requireNonNull(escapeMatcher, "escape matcher");
        return new MultiCharEscapingStringMapper(escapeMatcher);
    }

    public static final void escapeMultipleCharacters(Appendable out, CharSequence str, CharSequence escape) {
        Objects.requireNonNull(escape, "escape");
        CharBasedFilteringTextMapper.replace(
            out,
            str,
            new MultiCharEscapingStringMapper(CharMatcher.anyOf(escape))
        );
    }

    public static final String unescapeChars(CharSequence str, char c) {
        if (str == null) {
            return null;
        }
        if (str.isEmpty()) {
            return "";
        }
        return StringUtil.findReplace(str.toString(), "\\" + c, String.valueOf(c));
    }

    public static final String escapeChars(CharSequence str, char c) {
        if (str == null) {
            return null;
        }
        if (str.isEmpty()) {
            return "";
        }
        return StringUtil.findReplace(str.toString(), String.valueOf(c), "\\" + c);
    }

    public static final String getEscapeSequence(char literalChar) {
        return switch (literalChar) {
            case '\t' -> "\\t";
            case '\r' -> "\\r";
            case '\n' -> "\\n";
            case '\f' -> "\\f";
            default -> "\\" + literalChar;
        };
    }

    public static final char getLiteralCharacter(char escapedChar) {
        return switch (escapedChar) {
            case 't' -> '\t';
            case 'r' -> '\r';
            case 'n' -> '\n';
            case 'f' -> '\f';
            default -> escapedChar;
        };
    }

    private static final String escapeMultipleCharactersImpl(CharSequence str, StringUtil.CharToStringMapper escapeMapper) {
        return CharBasedFilteringTextMapper.replace(str, escapeMapper);
    }

}
