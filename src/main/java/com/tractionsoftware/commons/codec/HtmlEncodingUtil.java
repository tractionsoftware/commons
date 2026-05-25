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

import com.tractionsoftware.commons.io.StringWriteUtil;
import com.tractionsoftware.commons.lang.StringUtil;
import com.tractionsoftware.commons.text.CharBasedFilteringTextMapper;
import com.tractionsoftware.commons.text.TextWrapUtil;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Objects;
import java.util.regex.Pattern;

public final class HtmlEncodingUtil {

    /**
     * Not instantiable.
     */
    private HtmlEncodingUtil() {
    }

    private static final Logger LOGGER = LoggerFactory.getLogger(HtmlEncodingUtil.class);

    public enum SimpleHtmlEntity {

        QUOTATION_MARK('"', "quot"),

        GREATER_THAN('>', "gt"),

        LESS_THAN('<', "lt"),

        AMPERSAND('&', "amp"),

        NON_BREAKING_SPACE(' ', "nbsp");

        public static SimpleHtmlEntity getForLiteral(char c) {
            return switch (c) {
                case '>' -> SimpleHtmlEntity.GREATER_THAN;
                case '<' -> SimpleHtmlEntity.LESS_THAN;
                case '&' -> SimpleHtmlEntity.AMPERSAND;
                default -> null;
            };
        }

        public static SimpleHtmlEntity getForTagAttributeValue(char c) {
            return switch (c) {
                case '"' -> QUOTATION_MARK;
                case '>' -> GREATER_THAN;
                case '<' -> LESS_THAN;
                case '&' -> AMPERSAND;
                default -> null;
            };
        }

        public static SimpleHtmlEntity getForClassicConversion(char c) {
            return switch (c) {
                case '"' -> QUOTATION_MARK;
                case '>' -> GREATER_THAN;
                case '<' -> LESS_THAN;
                case '&' -> AMPERSAND;
                case ' ', StringUtil.CHAR_NON_BREAKING_SPACE -> NON_BREAKING_SPACE;
                default -> null;
            };
        }

        public static SimpleHtmlEntity get(char c) {
            return switch (c) {
                case '"' -> QUOTATION_MARK;
                case '>' -> GREATER_THAN;
                case '<' -> LESS_THAN;
                case '&' -> AMPERSAND;
                case StringUtil.CHAR_NON_BREAKING_SPACE -> NON_BREAKING_SPACE;
                default -> null;
            };
        }

        public static SimpleHtmlEntity get(String s, int index) {

            char c = s.charAt(index);
            if (Character.isSurrogate(c) || c != '&') {
                return null;
            }

            int remaining = s.length() - index;
            if (remaining < 4) {
                return null;
            }

            int start = index + 1;
            for (SimpleHtmlEntity entity : SimpleHtmlEntity.values()) {
                int len = entity.name.length();
                if (remaining > len &&
                    entity.name.equals(s.substring(start, start + len)) &&
                    s.charAt(start + len) == ';') {
                    return entity;
                }
            }
            return null;

        }

        public static String encodeForLiteral(char c) {
            SimpleHtmlEntity entity = getForLiteral(c);
            if (entity == null) {
                return null;
            }
            return entity.encoding;
        }

        public static String encodeForTagAttributeValue(char c) {
            SimpleHtmlEntity entity = getForTagAttributeValue(c);
            if (entity == null) {
                return null;
            }
            return entity.encoding;
        }

        public static String encodeForClassicHtmlText(char c) {
            SimpleHtmlEntity entity = getForClassicConversion(c);
            if (entity == null) {
                return null;
            }
            return entity.encoding;
        }

        private final char value;

        private final String name;

        private final String encoding;

        SimpleHtmlEntity(char value, String name) {
            this.value = value;
            this.name = name;
            this.encoding = "&" + name + ";";
        }

        public final void append(StringBuilder buff) {
            buff.append(encoding);
        }

        public final void print(PrintWriter out) {
            out.print(encoding);
        }

        public final void appendValue(StringBuilder buff) {
            buff.append(value);
        }

        public final String getEncoding() {
            return encoding;
        }

        public final int nameLength() {
            return name.length();
        }

        public static String escapeAmpersand(char c) {
            if (c == '&') {
                return AMPERSAND.encoding;
            }
            return null;
        }

    }

    private static final class HtmlLiteralAppendableWrapper implements Appendable {

        private final String preferredZeroWidthSpace;

        private final Appendable out;

        private HtmlLiteralAppendableWrapper(Appendable out, String preferredZeroWidthSpace) {
            this.out = out;
            this.preferredZeroWidthSpace = preferredZeroWidthSpace;
        }

        @Override
        public Appendable append(CharSequence csq) {
            appendLiteralText(csq);
            return this;
        }

        @Override
        public Appendable append(CharSequence csq, int start, int end) {
            appendLiteralText(csq.subSequence(start, end));
            return this;
        }

        @Override
        public Appendable append(char c) {
            String literalReplacement = encodeForLiteral(c);
            if (literalReplacement != null) {
                StringWriteUtil.safeAppend(out, literalReplacement);
            }
            else {
                StringWriteUtil.safeAppend(out, c);
            }
            return this;
        }

        private void appendLiteralText(CharSequence text) {
            if (StringUtils.isNotEmpty(text)) {
                CharBasedFilteringTextMapper.replace(out, text, this::encodeForLiteral);
            }
        }

        private String encodeForLiteral(char c) {
            if (c == StringUtil.CHAR_ZERO_WIDTH_SPACE) {
                return preferredZeroWidthSpace;
            }
            return SimpleHtmlEntity.encodeForLiteral(c);
        }

    }


    public static final String TAG_BR = "<BR>";

    public static final String DEFAULT_NON_SPACE_BREAK_HTML = "<wbr>";

    public static final String ZERO_WIDTH_SPACE_ENTITY_ENCODING = "&#8203;";

    /**
     * , . / \ | - % ) &amp; &gt; &lt; &quot;
     */
    private static final Pattern NON_SPACE_BREAK_OPPORTUNITIES =
        Pattern.compile("([,./\\\\|\\-%)]|&(amp|gt|lt|quot);)(^\\s)");

    /**
     * Applies the minimal amount of entity-encoding necessary for the given plain text to appear in literal form in an
     * HTML document. This requires entity-encoding greater than, less than, and ampersand characters.
     *
     * @param text
     *     some plain text that will be appearing in an HTML document.
     * @return a version of the given plain text with any tag delimiters and ampersands entity-encoded; or null if the
     *     given text is null.
     */
    public static String getLiteralText(CharSequence text) {
        if (text == null) {
            return null;
        }
        if (text.isEmpty()) {
            return "";
        }
        return CharBasedFilteringTextMapper.replace(text.toString(), SimpleHtmlEntity::encodeForLiteral);
    }

    /**
     * Converts text to HTML in the same manner as {@link #getLiteralText(CharSequence)}, but including BR tags in place
     * of line breaks. The line breaks are identified via {@link StringUtil#getLines(CharSequence)}, and the conversion
     * of the individual lines' text is does with getLiteralText.
     *
     * @param text
     *     the text to be converted.
     * @return some HTML representing the given text converted to HTML-safe text, plus substituting BR tags for line
     *     breaks.
     */
    public static String getLiteralTextWithLineBreaks(CharSequence text) {
        if (StringUtils.isEmpty(text)) {
            return "";
        }
        return StringUtil.join(StringUtil.getLines(getLiteralText(text)).iterator(), TAG_BR);
    }

    public static void printLiteralTextWithLineBreaks(Appendable out, CharSequence text) {
        if (StringUtils.isEmpty(text)) {
            return;
        }
        try {
            StringUtil.getNullSkippingJoiner(TAG_BR).appendTo(
                out,
                StringUtil.getLines(getLiteralText(text)).iterator()
            );
        }
        catch (IOException e) {
            // This is not possible.
            LOGGER.error("This exception should not happen", e);
        }
    }

    /**
     * Returns a version of the given text that is safe for an HTML attribute value. This requires entity-encoding
     * greater than, less than, double quotation mark, and ampersand characters.
     *
     * @param text
     *     the text to be encoded in an HTML-safe manner.
     * @return a version of the given text that is safe for an HTML attribute value.
     */
    public static String getTagAttributeValue(CharSequence text) {
        if (text == null) {
            return null;
        }
        if (text.isEmpty()) {
            return text.toString();
        }
        return Objects.toString(
            CharBasedFilteringTextMapper.replace(text, SimpleHtmlEntity::encodeForTagAttributeValue),
            null
        );
    }

    /**
     * Converts the given text to HTML, including the non-literal and usually unnecessary conversion spaces to
     * non-breaking spaces.
     *
     * <p>
     * This method exists to support a handful of classic forms and a few other old use cases. It should not be used for
     * new code.
     *
     * @param text
     *     the text to be converted to HTML.
     * @return a conversion of the given text to HTML, including the non-literal and usually unnecessary conversion
     *     spaces to non-breaking spaces.
     */
    public static String getClassicHtmlText(String text) {
        if (StringUtils.isBlank(text)) {
            return text;
        }
        return Objects.toString(
            CharBasedFilteringTextMapper.replace(text, SimpleHtmlEntity::encodeForClassicHtmlText),
            null
        );
    }

    /**
     * Reverses the encoding that is performed by {@link #getClassicHtmlText(String)}, translating the given HTML to
     * text, including the non-literal conversion of non-breaking space entities to ordinary spaces.
     *
     * @param html
     *     the HTML to be converted to text.
     * @return the decoded version of the string.
     */
    public static String getClassicTextHtml(String html) {

        if (StringUtils.isEmpty(html)) {
            return html;
        }

        int len = html.length();
        StringBuilder buff = null;
        for (int i = 0; i < len; i++) {
            SimpleHtmlEntity entity = SimpleHtmlEntity.get(html, i);
            if (entity == null) {
                if (buff != null) {
                    buff.append(html.charAt(i));
                }
            }
            else {
                if (buff == null) {
                    buff = new StringBuilder(len);
                    buff.append(html, 0, i);
                }
                entity.appendValue(buff);
                i += entity.nameLength() + 1;
            }
        }

        if (buff == null) {
            return html;
        }
        return buff.toString();

    }

    /**
     * Returns the preferred non-space optional break HTML sequence for the UserAgent being used for the current
     * request, defaulting to "<wbr>".
     *
     * @return the preferred non-space optional break HTML sequence for the UserAgent being used for the current
     *     request, defaulting to "<wbr>".
     */
    public static String getNonSpaceBreaksHtml() {
        return System.getProperty(
            "com.tractionsoftware.commons.codec.non_space_break_html", DEFAULT_NON_SPACE_BREAK_HTML
        );
    }

    /**
     * Inserts the preferred non-space optional break HTML sequence for the UserAgent being used for the current request
     * where appropriate in the given text.
     *
     * @param text
     *     the text into which the non-space optional break HTML should be inserted. This must really be pure text and
     *     not contain any markup, since markup might be corrupted by this insertion process.
     * @return the given text with the preferred non-space optional break HTML sequence for the UserAgent being used for
     *     the current request inserted where appropriate.
     */
    public static String getHtmlWithNonSpaceBreaks(String text) {
        if (text == null) {
            return null;
        }
        return NON_SPACE_BREAK_OPPORTUNITIES.matcher(text).replaceAll("$1" + getNonSpaceBreaksHtml() + "$3");
    }

    /**
     * Converts all occurrences of the ampersand character to HTML ampersand entities.
     */
    public static String escapeAmps(String str) {
        if (StringUtils.isEmpty(str)) {
            return str;
        }
        return Objects.toString(
            CharBasedFilteringTextMapper.replace(str, SimpleHtmlEntity::escapeAmpersand),
            null
        );
    }

    public static Appendable getLiteralAppendable(Appendable out, String preferredZeroWidthSpace) {
        Objects.requireNonNull(out, "Appendable");
        if (StringUtils.isBlank(preferredZeroWidthSpace)) {
            preferredZeroWidthSpace = TextWrapUtil.DEFAULT_ZERO_WIDTH_SPACE;
        }
        if (out instanceof HtmlLiteralAppendableWrapper literal &&
            Objects.equals(literal.preferredZeroWidthSpace, preferredZeroWidthSpace)) {
            return literal;
        }
        return new HtmlLiteralAppendableWrapper(out, preferredZeroWidthSpace);
    }

}
