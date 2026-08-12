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

import com.tractionsoftware.commons.config.ConfigurationException;
import com.tractionsoftware.commons.io.IOUtil;
import com.tractionsoftware.commons.lang.*;
import com.tractionsoftware.commons.properties.GetProperty;
import com.tractionsoftware.commons.properties.SimpleProperties;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.StringEscapeUtils;
import org.htmlcleaner.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.function.BiFunction;

/**
 * A wrapper class to bridge the HtmlCleaner API for generating syntactically valid HTML or XML documents from HTML.
 *
 * <p>
 * Use {@link #getInstance(GetProperty)} method to retrieve an instance with a given set of properties.
 *
 * @property CleanerMode=[Document]/Fragment
 *
 *     Indicates what mode this instance should use. If the mode is "Document", the input should be a complete document,
 *     and the output will be a complete and valid document. If the mode is "Fragment", the input should be an HTML
 *     fragment only -- something that would normally appear inside of a BODY tag -- and the output will be the cleaned
 *     version of that same markup.
 * @property SerializationMode=SIMPLE_HTML|SIMPLE_XML|PRETTY_XML|COMPACT_XML
 *     |BROWSER_COMPACT_XML|TEXT_ONLY|TEXT_ONLY_WITH_HTML_ENTITIES|CUSTOM
 *
 *     Indicates the manner in which the output will be serialized.
 * @property custom_serializer_class=
 *
 *     If the SerializationMode= property is set to "CUSTOM", this property must be set to the specification of a Class
 *     to be used as the custom {@link Serializer} for any operations performed by the instance being configured.
 *
 *     <p>
 *     The class must extend Serializer; if it has a constructor that accepts a {@link CleanerProperties} and a
 *     {@link GetProperty}, that must be visible; or if it does not have such a constructor, it must have a visible
 *     constructor that accepts just a CleanerProperties. If any of these conditions is not met, the configuration will
 *     be considered invalid, and a {@link ConfigurationException} will be raised.
 * @property serializer_custom_normalizeSpaces=[true]|false
 *
 *     If the SerializationMode property is set to "TEXT_ONLY" or "TEXT_ONLY_WITH_HTML_ENTITIES", this optional property
 *     may be used to indicate whether space-like characters should be "normalized" via
 *     {@link StringUtil#normalizeAlternativeWhitespace(String)}. Since this is usually a desirable behavior for
 *     HTML-to-text transformation, the default value of this property is "true". This behavior is applied before the
 *     "collapse" behavior described below, so that the spaces that are normalized can also be collapsed.
 * @property serializer_custom_collapseWhitespace=true|false
 *
 *     If the SerializationMode property is set to "TEXT_ONLY" or "TEXT_ONLY_WITH_HTML_ENTITIES", this optional property
 *     may be used to indicate whether whitespace should be "collapsed" via
 *     {@link StringUtil#collapseAndNormalizeWhitespace(String, boolean)}, which transforms runs of one or more
 *     whitespace characters to single ordinary spaces. The default value of this property is "false" for the
 *     "TEXT_ONLY" mode and "true" for the "TEXT_ONLY_WITH_HTML_ENTITIES" mode. This is so that pure text-only
 *     renderings will, by default, be left as true to their original form as possible, but text-only renderings
 *     intended for HTML will not contain what is usually redundant whitespace, particularly after entities are
 *     collapsed and normalized (see serializer_custom_normalizeSpaces=true|false above).
 * @property serializer_custom_*
 *
 *     If the SerializationMode property is set to "CUSTOM", any properties in the "serializer_custom" namespace will be
 *     passed as {@link GetProperty} to the {@link Serializer}'s constructor, if there is a visible constructor that
 *     accepts both a {@link CleanerProperties} and a GetProperty.
 * @property AdvancedXmlEscape=[true]/false
 * @property UseCdataForScriptAndStyle=[true]/false
 * @property TranslateSpecialEntities=[true]/false
 * @property RecognizeUnicodeChars=[true]/false
 * @property OmitUnknownTags=true/[false]
 * @property TreatUnknownTagsAsContent=true/[false]
 * @property OmitDeprecatedTags=true/[false]
 * @property TreatDeprecatedTagsAsContent=true/[false]
 * @property OmitComments=true/[false]
 * @property OmitXmlDeclaration=true/[false]
 * @property OmitDoctypeDeclaration=[true]/false
 * @property OmitHtmlEnvelope=[true]/false
 * @property UseEmptyElementTags=[true]/false
 * @property AllowMultiWordAttributes=[true]/false
 * @property AllowHtmlInsideAttributes=[true]/false
 * @property IgnoreQuestAndExclam=[true]/false
 * @property NamespacesAware=[true]/false
 * @property KeepHeadWhitespace=[true]/false
 * @property AddNewlineToHeadAndBody=[true]/false
 * @property HyphenReplacementInComment=
 * @property PruneTags=
 *
 *     A list of tags to be pruned from the output.
 * @property AllowTags=
 *
 *     A list of tags to be allowed in the output.
 * @property BooleanAttributeValues=
 * @property DeserializeEntities=true|false
 *
 * @author Dave Shepperton
 */
public final class HtmlCleanerAdapter {

    private static final Logger LOGGER = LoggerFactory.getLogger(HtmlCleanerAdapter.class);

    private static final String PROP_NAMESPACE_SERIALIZER_CUSTOM = "serializer_custom";

    private static final String PROP_NAME_SERIALIZER_NORMALIZE_SPACES = "normalizeSpaces";

    private static final String PROP_NAME_SERIALIZER_COLLAPSE_WHITESPACE = "collapseWhitespace";

    private static final String FRAGMENT_PREFIX = "<html><body><div id=\"clean-target\">";

    private static final String FRAGMENT_SUFFIX = "</div></body></html>";

    public static enum SerializationMode {

        SIMPLE_HTML((props, _) -> new SimpleHtmlSerializer(props)),

        SIMPLE_XML((props, _) -> new SimpleXmlSerializer(props)),

        PRETTY_XML((props, _) -> new PrettyXmlSerializer(props)),

        COMPACT_XML((props, _) -> new CompactXmlSerializer(props)),

        BROWSER_COMPACT_XML((props, _) -> new BrowserCompactXmlSerializer(props)),

        TEXT_ONLY((props, otherProperties) -> new TextOnlySerializer(
            props, getTextOnlySerializationParametersForPureText(otherProperties))
        ),

        TEXT_ONLY_WITH_HTML_ENTITIES(
            (props, otherProperties) ->
                new TextOnlySerializer(props, getTextOnlySerializationParametersForHtml(otherProperties))
        ),

        CUSTOM((props, otherProperties) -> {
            String customTypeName = SimpleProperties.loadString(otherProperties, "class");
            if (StringUtils.isBlank(customTypeName)) {
                throw new ConfigurationException("Missing class= property for custom Serializer.");
            }
            try {
                Class<? extends Serializer> customType = Class.forName(customTypeName).asSubclass(Serializer.class);
                try {
                    return customType.getConstructor(CleanerProperties.class, GetProperty.class)
                        .newInstance(props, otherProperties);
                }
                catch (NoSuchMethodException ignore) {
                }
                return customType.getConstructor(CleanerProperties.class).newInstance(props);
            }
            catch (Exception e) {
                throw new ConfigurationException(
                    "There was a problem loading or instantiating the class '" +
                    customTypeName +
                    "'", e
                );
            }
        });

        private final BiFunction<CleanerProperties,GetProperty,Serializer> serializerCreator;

        private SerializationMode(BiFunction<CleanerProperties,GetProperty,Serializer> serializerCreator) {
            this.serializerCreator = serializerCreator;
        }

        public static final SerializationMode get(String str, SerializationMode defaultMode) {
            return EnumUtil.enumFromString(SerializationMode.class, str, defaultMode);
        }

        public final Serializer getSerializer(CleanerProperties props, GetProperty otherProperties) {
            return serializerCreator.apply(props, otherProperties);
        }

    }

    public static enum CleanMode {

        DOCUMENT,

        FRAGMENT;

        public static final CleanMode get(String str, CleanMode defaultMode) {
            return EnumUtil.enumFromString(CleanMode.class, str, defaultMode);
        }

    }

    private static enum CleanerPropertySetter {

        AdvancedXmlEscape(),
        UseCdataForScriptAndStyle(),
        TranslateSpecialEntities(),
        RecognizeUnicodeChars(),
        OmitUnknownTags(),
        TreatUnknownTagsAsContent(),
        OmitDeprecatedTags(),
        TreatDeprecatedTagsAsContent(),
        OmitComments(),
        OmitXmlDeclaration(),
        OmitDoctypeDeclaration(),
        OmitHtmlEnvelope(),
        UseEmptyElementTags(),
        AllowMultiWordAttributes(),
        AllowHtmlInsideAttributes(),
        IgnoreQuestAndExclam(),
        NamespacesAware(),
        KeepHeadWhitespace(),
        AddNewlineToHeadAndBody(),
        HyphenReplacementInComment(),
        PruneTags(),
        AllowTags(),
        BooleanAttributeValues(),
        DeserializeEntities();

        public final void apply(CleanerProperties props, String value) {
            switch (this) {
            case AdvancedXmlEscape -> props.setAdvancedXmlEscape(NativeTypeConversion.stringToBoolean(value, true));
            case UseCdataForScriptAndStyle ->
                props.setUseCdataForScriptAndStyle(NativeTypeConversion.stringToBoolean(value, true));
            case TranslateSpecialEntities ->
                props.setTranslateSpecialEntities(NativeTypeConversion.stringToBoolean(value, true));
            case RecognizeUnicodeChars ->
                props.setRecognizeUnicodeChars(NativeTypeConversion.stringToBoolean(value, true));
            case OmitUnknownTags -> props.setOmitUnknownTags(NativeTypeConversion.stringToBoolean(value, false));
            case TreatUnknownTagsAsContent ->
                props.setTreatUnknownTagsAsContent(NativeTypeConversion.stringToBoolean(value, false));
            case OmitDeprecatedTags -> props.setOmitDeprecatedTags(NativeTypeConversion.stringToBoolean(value, false));
            case TreatDeprecatedTagsAsContent ->
                props.setTreatDeprecatedTagsAsContent(NativeTypeConversion.stringToBoolean(value, false));
            case OmitComments -> props.setOmitComments(NativeTypeConversion.stringToBoolean(value, false));
            case OmitXmlDeclaration -> props.setOmitXmlDeclaration(NativeTypeConversion.stringToBoolean(value, false));
            case OmitDoctypeDeclaration ->
                props.setOmitDoctypeDeclaration(NativeTypeConversion.stringToBoolean(value, true));
            case OmitHtmlEnvelope -> props.setOmitHtmlEnvelope(NativeTypeConversion.stringToBoolean(value, true));
            case UseEmptyElementTags -> props.setUseEmptyElementTags(NativeTypeConversion.stringToBoolean(value, true));
            case AllowMultiWordAttributes ->
                props.setAllowMultiWordAttributes(NativeTypeConversion.stringToBoolean(value, true));
            case AllowHtmlInsideAttributes ->
                props.setAllowHtmlInsideAttributes(NativeTypeConversion.stringToBoolean(value, true));
            case IgnoreQuestAndExclam ->
                props.setIgnoreQuestAndExclam(NativeTypeConversion.stringToBoolean(value, true));
            case NamespacesAware -> props.setNamespacesAware(NativeTypeConversion.stringToBoolean(value, true));
            case KeepHeadWhitespace ->
                props.setKeepWhitespaceAndCommentsInHead(NativeTypeConversion.stringToBoolean(value, true));
            case AddNewlineToHeadAndBody ->
                props.setAddNewlineToHeadAndBody(NativeTypeConversion.stringToBoolean(value, true));
            case HyphenReplacementInComment -> props.setHyphenReplacementInComment(value);
            case PruneTags -> props.setPruneTags(value);
            case AllowTags -> props.setAllowTags(value);
            case BooleanAttributeValues -> props.setBooleanAttributeValues(value);
            case DeserializeEntities -> props.setDeserializeEntities(NativeTypeConversion.stringToBoolean(value, true));
            default -> {
            }
            }
        }

    }

    private static final HtmlCleanerAdapter HTML2XHTML;

    static {

        CleanerProperties props = new CleanerProperties();
        props.setTranslateSpecialEntities(true);
        props.setTransResCharsToNCR(true);
        props.setTranslateSpecialEntities(true);
        props.setOmitComments(true);
        props.setAdvancedXmlEscape(true);
        props.setOmitDoctypeDeclaration(false);

        try {
            HTML2XHTML = createInstance(SerializationMode.SIMPLE_XML, props, CleanMode.DOCUMENT, null);
        }
        catch (ConfigurationException e) {
            System.err.println("Failed to set up HTML-to-XHTML transformer:");
            e.printStackTrace(System.err);
            System.err.flush();
            // We can't really run without this.
            throw new RuntimeException(e);
        }

    }

    private static enum TextOnlyTagSerializationMode {
        NONE,
        LINE_BREAK,
        CHILDREN,
        CHILDREN_WITH_SPACE_AFTER,
        CHILDREN_WITH_SPACES_SURROUNDING,
        IMG_ALT
    }

    private static final class TextOnlySerializationParameters {

        /**
         * Indicates whether this "text-only" rendering actually has to be HTML-safe. If so, tags will still be omitted,
         * but runs of text that may contain tag delimiters will be entity-encoded Conversely, if the rendering is not
         * supposed to be HTML-safe (i.e., must actually be text-only), we must decode any entities that come from the
         * HTML.
         */
        private final boolean outputHtml;

        /**
         * Indicates whether {@link StringUtil#normalizeAlternativeWhitespace(String)} will be applied to the text
         * before it is rendered in the output, replacing any occurrences of certain "alternative" space characters with
         * normal spaces.
         */
        private final boolean normalizeSpaces;

        /**
         * Indicates whether {@link StringUtil#collapseAndNormalizeWhitespace(String, boolean)} will be applied to the
         * text before it is rendered in the output, replacing any run of one or more whitespace characters with a
         * single normal space.
         */
        private final boolean collapseWhitespace;

        private boolean wasPadded = false;

        private TextOnlySerializationParameters(boolean outputHtml, boolean normalizeSpaces, boolean collapseWhitespace) {
            this.outputHtml = outputHtml;
            this.normalizeSpaces = normalizeSpaces;
            this.collapseWhitespace = collapseWhitespace;
        }

        public final boolean shouldOutputHtml() {
            return outputHtml;
        }

        public final boolean shouldNormalizeSpaces() {
            return normalizeSpaces;
        }

        public final boolean shouldCollapseWhitespace() {
            return collapseWhitespace;
        }

    }

    private static final TextOnlySerializationParameters getTextOnlySerializationParametersForPureText(GetProperty otherProperties) {
        return new TextOnlySerializationParameters(
            false,
            SimpleProperties.loadBoolean(otherProperties, PROP_NAME_SERIALIZER_NORMALIZE_SPACES, true),
            SimpleProperties.loadBoolean(otherProperties, PROP_NAME_SERIALIZER_COLLAPSE_WHITESPACE, false)
        );
    }

    private static final TextOnlySerializationParameters getTextOnlySerializationParametersForHtml(GetProperty otherProperties) {
        return new TextOnlySerializationParameters(
            true,
            SimpleProperties.loadBoolean(otherProperties, PROP_NAME_SERIALIZER_NORMALIZE_SPACES, true),
            SimpleProperties.loadBoolean(otherProperties, PROP_NAME_SERIALIZER_COLLAPSE_WHITESPACE, true)
        );
    }

    private static final class TextOnlySerializer extends Serializer {

        private final TextOnlySerializationParameters params;

        private TextOnlySerializer(CleanerProperties props, TextOnlySerializationParameters params) {
            super(props);
            this.params = params;
        }

        @Override
        protected final void serialize(TagNode tagNode, Writer writer) throws IOException {

            List<? extends BaseToken> tagChildren = tagNode.getAllChildren();
            if (isMinimizedTagSyntax(tagNode)) {
                return;
            }

            for (BaseToken item : tagChildren) {

                if (item instanceof ContentNode contentNode) {
                    serializeContent(writer, contentNode.getContent());
                    continue;
                }

                if (!(item instanceof TagNode childTag)) {
                    continue;
                }

                TextOnlyTagSerializationMode tagMode = getTagMode(childTag);

                switch (tagMode) {
                case NONE -> {
                }
                case LINE_BREAK -> writer.write('\n');
                case CHILDREN -> serialize(childTag, writer);
                case CHILDREN_WITH_SPACE_AFTER -> {
                    serialize(childTag, writer);
                    writer.write(' ');
                    params.wasPadded = true;
                }
                case CHILDREN_WITH_SPACES_SURROUNDING -> {
                    if (params.wasPadded) {
                        writer.write(' ');
                    }
                    serialize(childTag, writer);
                    writer.write(' ');
                    params.wasPadded = true;
                }
                case IMG_ALT -> {
                    String alt = getAltText(item);
                    if (StringUtils.isNotBlank(alt)) {
                        writer.write(alt);
                        params.wasPadded = false;
                    }
                }
                }

            }

        }

        private final TextOnlyTagSerializationMode getTagMode(TagNode tag) {

            switch (StringUtils.defaultString(tag.getName()).toLowerCase()) {
            case "style", "script", "input", "select" -> {
                return TextOnlyTagSerializationMode.NONE;
            }
            case "br" -> {
                if (params.shouldCollapseWhitespace()) {
                    return TextOnlyTagSerializationMode.CHILDREN_WITH_SPACE_AFTER;
                }
                if (params.shouldOutputHtml()) {
                    return TextOnlyTagSerializationMode.CHILDREN;
                }
                return TextOnlyTagSerializationMode.LINE_BREAK;
            }
            case "title" -> {
                if (params.shouldCollapseWhitespace()) {
                    return TextOnlyTagSerializationMode.CHILDREN_WITH_SPACE_AFTER;
                }
                return TextOnlyTagSerializationMode.CHILDREN;
            }
            case "li", "td", "dt", "dd", "p", "div", "blockquote", "h1", "h2", "h3", "h4", "h5", "h6", "pre", "address",
                 "center", "dir", "menu" -> {
                if (params.shouldCollapseWhitespace()) {
                    return TextOnlyTagSerializationMode.CHILDREN_WITH_SPACES_SURROUNDING;
                }
                return TextOnlyTagSerializationMode.CHILDREN_WITH_SPACE_AFTER;
            }
            case "img" -> {
                return TextOnlyTagSerializationMode.IMG_ALT;
            }
            default -> {
                return TextOnlyTagSerializationMode.CHILDREN;
            }
            }

        }

        private final String getAltText(BaseToken item) {
            if (item instanceof TagNode tag && "img".equals(tag.getName())) {
                return tag.getAttributeByName("alt");
            }
            return null;
        }

        private final boolean isMinimizedTagSyntax(TagNode tagNode) {
            TagInfo tagInfo = props.getTagInfoProvider().getTagInfo(tagNode.getName());
            return tagInfo != null && !tagNode.hasChildren() && tagInfo.isEmptyTag();
        }

        private final void serializeContent(Writer writer, String content) throws IOException {

            boolean normalize = params.shouldNormalizeSpaces();

            if (params.shouldCollapseWhitespace()) {
                content = StringUtil.collapseAndNormalizeWhitespace(content, normalize);
            }
            else if (normalize) {
                content = StringUtil.normalizeAlternativeWhitespace(content);
            }

            if (content.isEmpty()) {
                return;
            }

            if (params.shouldOutputHtml()) {
                // If this is for an HTML document, we must assume
                // that text node values that contain tag delimiters
                // will have to be entity-encoded to be HTML-safe.
                if (StringUtils.containsAny(content, '<', '>')) {
                    content = HtmlUtil.getLiteralText(content);
                }
            }
            else {
                // If this is not for HTML, we assume it's really
                // supposed to be 100% text-only, and that HTML
                // entities should be decoded.
                content = StringEscapeUtils.unescapeHtml4(content);
            }

            writer.write(content);

        }

    }

    /**
     * Returns an HtmlCleanerAdapter instance that creates a valid simple XML serialization of an entire HTML document,
     * including an XML declaration and an HTML DOCTYPE declaration, if one is present in the original.
     *
     * @return an HtmlCleanerAdapter instance that creates a valid simple XML serialization of an entire HTML document,
     *     including an XML declaration and an HTML DOCTYPE declaration, if one is present in the original.
     */
    public static final HtmlCleanerAdapter cleanHtml() {
        return HTML2XHTML;
    }

    private final HtmlCleaner cleaner;

    private final Serializer serializer;

    private final CleanMode cleanMode;

    /**
     * Creates and returns a new HtmlCleanerAdapter instance with the given set of properties.
     *
     * @param props
     *     the configuration properties to use to configure this instance.
     * @return the HtmlCleanerAdapter instance created.
     * @throws ConfigurationException
     *     if one is raised while attempting to create or set up the {@link HtmlCleaner} instance or the
     *     {@link Serializer} to be used for
     */
    public static final HtmlCleanerAdapter getInstance(GetProperty props) {
        CleanerProperties cleanerProps = new CleanerProperties();
        for (CleanerPropertySetter setter : EnumSet.allOf(CleanerPropertySetter.class)) {
            String value = props.getProperty(setter.name());
            if (value != null) {
                setter.apply(cleanerProps, value);
            }
        }
        cleanerProps.setCharset(StandardCharsets.UTF_8.name());
        SerializationMode serializationMode = SerializationMode.get(
            props.getProperty("SerializationMode"), SerializationMode.SIMPLE_XML
        );
        CleanMode cleanMode = CleanMode.get(props.getProperty("CleanMode"), CleanMode.DOCUMENT);
        return createInstance(serializationMode, cleanerProps, cleanMode, getOtherProperties(props));
    }

    private static final HtmlCleanerAdapter createInstance(SerializationMode serializationMode, CleanerProperties props, CleanMode cleanMode, GetProperty otherProperties)
        throws ConfigurationException {
        HtmlCleaner cleaner = new HtmlCleaner(props);
        return new HtmlCleanerAdapter(
            cleaner,
            serializationMode.getSerializer(cleaner.getProperties(), otherProperties),
            cleanMode
        );
    }

    public static final HtmlCleanerAdapter createInstance(HtmlCleaner cleaner, Serializer serializer, CleanMode cleanMode) {
        Objects.requireNonNull(cleaner, "HtmlCleaner");
        Objects.requireNonNull(serializer, "Serializer");
        Objects.requireNonNull(cleanMode, "CleanMode");
        return new HtmlCleanerAdapter(cleaner, serializer, cleanMode);
    }

    private static final GetProperty getOtherProperties(GetProperty baseConfig) {
        Map<String,String> otherProperties = new HashMap<>();
        baseConfig.getNamespace(PROP_NAMESPACE_SERIALIZER_CUSTOM).copyTo(otherProperties);
        if (otherProperties.isEmpty()) {
            return null;
        }
        return SimpleProperties.asGetProperty(otherProperties);
    }

    private HtmlCleanerAdapter(HtmlCleaner cleaner, Serializer serializer, CleanMode cleanMode) {
        this.cleaner = cleaner;
        this.serializer = serializer;
        this.cleanMode = cleanMode;
    }

    private final TagNode getTagNode(InputStream input) throws IOException {
        return cleaner.clean(input, StandardCharsets.UTF_8.name());
    }

    private final TagNode getTagNodeFragment(InputStream input) throws IOException {
        return getTagNode(IOUtil.getSequenceInputStream(
            new ByteArrayInputStream(FRAGMENT_PREFIX.getBytes(
                StandardCharsets.UTF_8)),
            input,
            new ByteArrayInputStream(FRAGMENT_SUFFIX.getBytes(
                StandardCharsets.UTF_8))
        ));
    }

    /**
     * Cleans the HTML document represented by the InputStream, writing the new document to the given OutputStream. Both
     * reading and writing use the UTF-8 character set.
     *
     * @param input
     *     the bytes of a UTF-8 HTML document or document fragment to be cleaned.
     * @param output
     *     the sink for the new UTF-8 HTML document or document fragment representing the clean version of the input
     *     document.
     * @throws IOException
     *     if there is a problem reading or writing the document.
     */
    public final void clean(InputStream input, OutputStream output) throws IOException {
        if (cleanMode == CleanMode.FRAGMENT) {
            cleanFragment(input, output);
        }
        else {
            cleanDocument(input, output);
        }
    }

    /**
     * Cleans the HTML document represented by the Reader, writing the new document to the given Writer.
     *
     * @param input
     *     a Reader that provides the characters for the HTML document or document fragment to be cleaned.
     * @param output
     *     the sink for the new HTML document or document fragment representing the clean version of the input
     *     document.
     * @throws IOException
     *     if there is a problem reading or writing the document.
     */
    public final void clean(Reader input, Writer output) throws IOException {
        if (cleanMode == CleanMode.FRAGMENT) {
            cleanFragment(input, output);
        }
        else {
            cleanDocument(input, output);
        }
    }

    private final void cleanDocument(InputStream input, OutputStream output) throws IOException {
        TagNode doc = getTagNode(input);
        if (doc == null) {
            LOGGER.warn("HtmlCleaner did not return a document node for  cleanup.");
            return;
        }
        serializer.writeToStream(doc, IOUtil.getNoCloseOutputStream(output), StandardCharsets.UTF_8.name());
    }

    private final void cleanDocument(Reader input, Writer output) throws IOException {
        TagNode doc = cleaner.clean(input);
        if (doc == null) {
            LOGGER.warn("HtmlCleaner did not return a document node for cleanup.");
            return;
        }
        serializer.write(doc, IOUtil.getNoCloseWriter(output), StandardCharsets.UTF_8.name());
    }

    private final void cleanFragment(InputStream input, OutputStream output) throws IOException {
        TagNode doc = getTagNodeFragment(input);
        if (doc == null) {
            LOGGER.warn("HtmlCleaner did not return a document node for fragment cleanup.");
            return;
        }
        TagNode cleanTargetContents = doc.findElementByAttValue("id", "clean-target", true, false);
        serializer.writeToStream(
            cleanTargetContents,
            IOUtil.getNoCloseOutputStream(output),
            StandardCharsets.UTF_8.name(),
            true
        );
    }

    private final void cleanFragment(Reader input, Writer output) throws IOException {
        TagNode doc = getTagNodeFragment(new ByteArrayInputStream(IOUtils.toByteArray(input, StandardCharsets.UTF_8)));
        if (doc == null) {
            LOGGER.warn("HtmlCleaner did not return a document node for fragment cleanup.");
            return;
        }
        TagNode cleanTargetContents = doc.findElementByAttValue("id", "clean-target", true, false);
        serializer.write(cleanTargetContents, IOUtil.getNoCloseWriter(output), StandardCharsets.UTF_8.name(), true);
    }

}
