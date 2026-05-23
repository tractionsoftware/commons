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

package com.tractionsoftware.commons.html;

import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableMap;
import com.google.common.net.MediaType;
import com.tractionsoftware.commons.io.StringWriteUtil;
import com.tractionsoftware.commons.lang.NativeTypeConversion;
import com.tractionsoftware.commons.lang.StringUtil;
import com.tractionsoftware.commons.text.CharBasedFilteringTextMapper;
import com.tractionsoftware.commons.text.TextWrapUtil;
import com.tractionsoftware.commons.util.CollectionsUtil;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class HtmlUtil {

    /**
     * Not instantiable.
     */
    private HtmlUtil() {
    }

    private static final Logger LOGGER = LoggerFactory.getLogger(HtmlUtil.class);

    private static final Pattern HTML_HEADING_START_TAG = Pattern.compile(
        "<([Hh])([123456])(\\s.+)?>"
    );

    public static enum SimpleHtmlEntity {

        QUOTATION_MARK('"', "quot"),

        GREATER_THAN('>', "gt"),

        LESS_THAN('<', "lt"),

        AMPERSAND('&', "amp"),

        NON_BREAKING_SPACE(' ', "nbsp");

        public static final SimpleHtmlEntity getForLiteral(char c) {
            return switch (c) {
                case '>' -> SimpleHtmlEntity.GREATER_THAN;
                case '<' -> SimpleHtmlEntity.LESS_THAN;
                case '&' -> SimpleHtmlEntity.AMPERSAND;
                default -> null;
            };
        }

        public static final SimpleHtmlEntity getForTagAttributeValue(char c) {
            return switch (c) {
                case '"' -> QUOTATION_MARK;
                case '>' -> GREATER_THAN;
                case '<' -> LESS_THAN;
                case '&' -> AMPERSAND;
                default -> null;
            };
        }

        public static final SimpleHtmlEntity getForClassicConversion(char c) {
            return switch (c) {
                case '"' -> QUOTATION_MARK;
                case '>' -> GREATER_THAN;
                case '<' -> LESS_THAN;
                case '&' -> AMPERSAND;
                case ' ', StringUtil.CHAR_NON_BREAKING_SPACE -> NON_BREAKING_SPACE;
                default -> null;
            };
        }

        public static final SimpleHtmlEntity get(char c) {
            return switch (c) {
                case '"' -> QUOTATION_MARK;
                case '>' -> GREATER_THAN;
                case '<' -> LESS_THAN;
                case '&' -> AMPERSAND;
                case StringUtil.CHAR_NON_BREAKING_SPACE -> NON_BREAKING_SPACE;
                default -> null;
            };
        }

        public static final SimpleHtmlEntity get(String s, int index) {

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

        public static final String encodeForLiteral(char c) {
            SimpleHtmlEntity entity = getForLiteral(c);
            if (entity == null) {
                return null;
            }
            return entity.encoding;
        }

        public static final String encodeForTagAttributeValue(char c) {
            SimpleHtmlEntity entity = getForTagAttributeValue(c);
            if (entity == null) {
                return null;
            }
            return entity.encoding;
        }

        public static final String encodeForClassicHtmlText(char c) {
            SimpleHtmlEntity entity = getForClassicConversion(c);
            if (entity == null) {
                return null;
            }
            return entity.encoding;
        }

        private final char value;

        private final String name;

        private final String encoding;

        private SimpleHtmlEntity(char value, String name) {
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

        public static final String escapeAmpersand(char c) {
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
        public final Appendable append(CharSequence csq) {
            appendLiteralText(csq);
            return this;
        }

        @Override
        public final Appendable append(CharSequence csq, int start, int end) {
            appendLiteralText(csq.subSequence(start, end));
            return this;
        }

        @Override
        public final Appendable append(char c) {
            String literalReplacement = encodeForLiteral(c);
            if (literalReplacement != null) {
                StringWriteUtil.safeAppend(out, literalReplacement);
            }
            else {
                StringWriteUtil.safeAppend(out, c);
            }
            return this;
        }

        private final void appendLiteralText(CharSequence text) {
            if (StringUtils.isNotEmpty(text)) {
                CharBasedFilteringTextMapper.replace(out, text, this::encodeForLiteral);
            }
        }

        private final String encodeForLiteral(char c) {
            if (c == StringUtil.CHAR_ZERO_WIDTH_SPACE) {
                return preferredZeroWidthSpace;
            }
            return SimpleHtmlEntity.encodeForLiteral(c);
        }

    }

    public static final String TAG_NAME_SCRIPT = "script";

    public static final String TAG_NAME_LINK = "link";

    public static final String TAG_NAME_STYLE = "style";

    public static final String TAG_NAME_A = "a";

    public static final String TAG_NAME_IMG = "img";

    public static final String INLINE_COMMENT_START = "<!--";

    public static final String INLINE_COMMENT_END = "-->";

    public static final String INLINE_COMMENT_END_WITH_SCRIPT_OR_STYLE_COMMENT = "//" + INLINE_COMMENT_END;

    public static final String ATTRIBUTE_NAME_TYPE = "type";

    public static final String LINK_ATTRIBUTE_NAME_REL = "rel";

    public static final String LINK_ATTRIBUTE_NAME_HREF = "href";

    public static final String STYLE_ATTRIBUTE_NAME_MEDIA = "media";

    public static final String ATTRIBUTE_NAME_SRC = "src";

    public static final String ATTRIBUTE_NAME_WIDTH = "width";

    public static final String ATTRIBUTE_NAME_HEIGHT = "height";

    public static final String ATTRIBUTE_NAME_IMAGE_DIALOG_SRC = "data-image-src";

    public static final String ATTRIBUTE_NAME_IMAGE_DIALOG_TITLE = "data-title";

    public static final String TYPE_JAVASCRIPT = MediaType.TEXT_JAVASCRIPT_UTF_8.withoutParameters().toString();

    public static final String LINK_REL_STYLESHEET = "stylesheet";

    public static final String LINK_REL_ALTERNATE_STYLESHEET = "alternate " + LINK_REL_STYLESHEET;

    public static final String TAG_BR = "<BR>";

    public static final String DEFAULT_NON_SPACE_BREAK_HTML = "<wbr>";

    public static final String ZERO_WIDTH_SPACE_ENTITY_ENCODING = "&#8203;";

    public static final String ATTRIBUTE_NAME_CLASS = "class";

    public static final String ATTRIBUTE_NAME_TARGET = "target";

    public static final String ATTRIBUTE_VALUE_TARGET_NEW_WINDOW_NAME = "_blank";

    /**
     * , . / \ | - % ) &amp; &gt; &lt; &quot;
     */
    private static final Pattern NON_SPACE_BREAK_OPPORTUNITIES =
        Pattern.compile("([,./\\\\|\\-%)]|&(amp|gt|lt|quot);)(^\\s)");

    private static final Splitter CLASS_ATTRIBUTE_SPLITTER = Splitter.on(Pattern.compile("\\s+"));

    /**
     * Applies the minimal amount of entity-encoding necessary for the given plain text to appear in literal form in an
     * HTML document. This requires entity-encoding greater than, less than, and ampersand characters.
     *
     * @param text
     *     some plain text that will be appearing in an HTML document.
     * @return a version of the given plain text with any tag delimiters and ampersands entity-encoded; or null if the
     *     given text is null.
     */
    public static final String getLiteralText(CharSequence text) {
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
    public static final String getLiteralTextWithLineBreaks(CharSequence text) {
        if (StringUtils.isEmpty(text)) {
            return "";
        }
        return StringUtil.join(StringUtil.getLines(getLiteralText(text)).iterator(), TAG_BR);
    }

    public static final void printLiteralTextWithLineBreaks(Appendable out, CharSequence text) {
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
    public static final String getTagAttributeValue(CharSequence text) {
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
     * Inserts the preferred non-space optional break HTML sequence for the UserAgent being used for the current request
     * where appropriate in the given text.
     *
     * @param text
     *     the text into which the non-space optional break HTML should be inserted. This must really be pure text and
     *     not contain any markup, since markup might be corrupted by this insertion process.
     * @return the given text with the preferred non-space optional break HTML sequence for the UserAgent being used for
     *     the current request inserted where appropriate.
     */
    public static final String getHtmlWithNonSpaceBreaks(String text, Supplier<String> nonSpaceBreaksHtml) {
        return NON_SPACE_BREAK_OPPORTUNITIES.matcher(text).replaceAll("$1" + nonSpaceBreaksHtml.get() + "$3");
    }

    public static final String getTagAttribute(String name, String value) {
        if (value == null) {
            return name;
        }
        return name + "=\"" + getTagAttributeValue(value) + "\"";
    }

    public static final boolean containsClassName(String classAttr, String className) {

        classAttr = StringUtils.trimToNull(classAttr);
        if (classAttr == null) {
            return false;
        }
        className = StringUtils.trimToNull(className);
        if (className == null) {
            return false;
        }

        for (String classAttrSub : CLASS_ATTRIBUTE_SPLITTER.split(classAttr)) {
            if (className.equals(classAttrSub)) {
                return true;
            }
        }
        return false;

    }

    /**
     * Converts all occurrences of the ampersand character to HTML ampersand entities.
     */
    public static final String escapeAmps(String str) {
        if (StringUtils.isEmpty(str)) {
            return str;
        }
        return Objects.toString(
            CharBasedFilteringTextMapper.replace(str, SimpleHtmlEntity::escapeAmpersand),
            null
        );
    }

    /**
     * Returns " checked" if on is true or "" if on is false
     */
    public static final String chk(boolean on) {
        return (on) ? " checked" : "";
    }

    /**
     * Returns " selected" if on is true or "" if on is false
     */
    public static final String sel(boolean on) {
        return (on) ? " selected" : "";
    }

    public static final void printLiteralText(Appendable out, CharSequence text) {
        if (StringUtils.isNotEmpty(text)) {
            CharBasedFilteringTextMapper.replace(out, text, SimpleHtmlEntity::encodeForLiteral);
        }
    }

    public static final void printTagAttribute(Appendable out, String name, String value) {
        try {
            out.append(name);
            if (value != null) {
                out.append("=\"");
                printTagAttributeValue(out, value);
                out.append('"');
            }
        }
        catch (IOException e) {
            LOGGER.error("Failed to write to " + out, e);
        }
    }

    public static final void printTagAttributeValue(Appendable out, String text) {
        if (StringUtils.isNotEmpty(text)) {
            CharBasedFilteringTextMapper.replace(out, text, SimpleHtmlEntity::encodeForTagAttributeValue);
        }
    }

    public static final void printBeginSelect(PrintWriter out, String name) {
        out.print("<select name=\"");
        out.print(name);
        out.print("\">");
    }

    public static final void printBeginSelect(PrintWriter out, String name, String attributes) {
        out.print("<select name=\"");
        printTagAttributeValue(out, name);
        out.print("\" ");
        if (attributes != null) {
            out.print(attributes);
        }
        out.print(" ");
        out.print(">");
    }

    public static final void printBeginSelect(PrintWriter out, String name, Map<String,String> attributes) {
        printStartTag(out, "select", attributes);
    }

    public static final void printBeginSelect(PrintWriter out, String name, String attributes, boolean disabled) {
        out.print("<select name=\"");
        out.print(name);
        out.print("\" ");
        if (attributes != null) {
            out.print(attributes);
        }
        out.print(" ");
        if (disabled) {
            out.print("disabled");
        }
        out.print(">");
    }

    public static final void printOption(PrintWriter out, String text, String value, boolean selected) {
        out.print("<option value=\"");
        out.print(getTagAttributeValue(value));
        out.print((selected) ? "\" selected" : "\"");
        out.print(">");
        out.print(text);
        out.print("</option>");
    }

    public static final void printOption(PrintWriter out, String text, String value, boolean selected, String attrs) {
        out.print("<option value=\"");
        printTagAttributeValue(out, value);
        out.print('"');
        if (selected) {
            out.print(" selected");
        }
        if (attrs != null) {
            out.print(' ');
            out.print(attrs);
        }
        out.print(">");
        printLiteralText(out, text);
        out.print("</option>");
    }

    public static final void printOption(PrintWriter out, String text, String value, boolean selected, Map<String,String> attributes) {
        if (selected || value != null) {
            Map<String,String> useAttributes = new LinkedHashMap<>();
            if (value != null) {
                useAttributes.put("value", value);
            }
            if (!CollectionsUtil.isNullOrEmpty(attributes)) {
                useAttributes.putAll(attributes);
            }
            if (selected) {
                useAttributes.put("selected", null);
            }
            attributes = useAttributes;
        }
        printStartTag(out, "option", attributes);
        printLiteralText(out, text);
        printEndTag(out, "option");
    }

    public static final void printOption(PrintWriter out, String name, boolean selected) {
        printOption(out, name, null, selected, ImmutableMap.of());
    }

    public static final void printEndSelect(PrintWriter out) {
        printEndTag(out, "select");
    }

    public static final void printBeginOptionGroup(PrintWriter out, String label, String value) {
        printBeginOptionGroup(out, label, value, null);
    }

    public static final void printBeginOptionGroup(PrintWriter out, String label, String value, String className) {
        out.print("<optgroup label=\"");
        out.print(label);
        out.print("\"");
        if (className != null) {
            out.print(" class=\"");
            out.print(className);
            out.print("\"");
        }
        out.print(">");
    }

    public static final void printEndOptionGroup(PrintWriter out) {
        out.print("</optgroup>");
    }

    public static final void printBeginLink(PrintWriter out, String url, Map<String,String> otherAttributes) {
        out.print("<a ");
        printTagAttribute(out, "href", url);
        for (Map.Entry<String,String> attr : otherAttributes.entrySet()) {
            out.print(' ');
            String attrName = attr.getKey();
            if ("href".equals(attrName)) {
                continue;
            }
            printTagAttribute(out, attrName, attr.getValue());
        }
        out.print(">");
    }

    public static final void printLink(PrintWriter out, String url, String linkText, Map<String,String> otherAttributes) {
        printBeginLink(out, url, otherAttributes);
        printLiteralText(out, linkText);
        out.print("</a>");
    }

    public static final void printStartTag(PrintWriter out, String tagName, Map<String,String> attributes) {
        StringUtil.checkNotBlankX(tagName, "tag name");
        out.print('<');
        out.print(tagName);
        if (attributes != null) {
            for (Map.Entry<String,String> attr : attributes.entrySet()) {
                out.print(' ');
                printTagAttribute(out, attr.getKey(), attr.getValue());
            }
        }
        out.print('>');
    }

    public static final void printEndTag(PrintWriter out, String tagName) {
        StringUtil.checkNotBlankX(tagName, "tag name");
        out.print("</");
        out.print(tagName);
        out.print('>');
    }

    public static final void printInputTag(PrintWriter out, String name, String value, String type) {
        Map<String,String> attributes = new LinkedHashMap<>();
        if (type != null) {
            attributes.put("type", type);
        }
        if (name != null) {
            attributes.put("name", name);
        }
        if (value != null) {
            attributes.put("value", value);
        }
        printStartTag(out, "input", attributes);
    }

    public static final void printMetaTag(PrintWriter out, Map<String,String> attributes) {
        printStartTag(out, "META", attributes);
    }

    public static final void printOpenGraphMetaTag(PrintWriter out, String ogPropertyType, String content) {
        printMetaTag(
            out,
            ImmutableMap.of(
                "property", "og:" + ogPropertyType,
                "content", content
            )
        );
    }

    public static final Appendable getLiteralAppendable(Appendable out, String preferredZeroWidthSpace) {
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

    public static final String createComment(String text) {
        return INLINE_COMMENT_START + " " + text + " " + INLINE_COMMENT_END;
    }

    public static final int getApparentHeadingLevel(String tagText) {
        Matcher m = HTML_HEADING_START_TAG.matcher(tagText);
        if (m.matches()) {
            return NativeTypeConversion.stringToInt(m.group(2), 0);
        }
        return 0;
    }

}
