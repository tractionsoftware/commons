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

import com.google.common.base.Suppliers;
import com.google.common.collect.ImmutableMap;
import com.tractionsoftware.commons.config.Configuration;
import com.tractionsoftware.commons.config.ConfiguredObject;
import com.tractionsoftware.commons.io.StringWriteUtil;
import com.tractionsoftware.commons.lang.NativeTypeConversion;
import com.tractionsoftware.commons.properties.SimpleProperties;
import com.tractionsoftware.commons.text.TextTransformationException;
import com.tractionsoftware.commons.text.TextTransformer;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import org.apache.commons.io.input.CharSequenceReader;
import org.apache.commons.io.output.StringBuilderWriter;
import org.apache.commons.lang3.StringUtils;

import java.io.*;
import java.util.function.Supplier;

/**
 * A {@link TextTransformer} implementation that uses an {@link HtmlCleanerAdapter}.
 *
 * @author Dave Shepperton
 */
public final class HtmlCleanerAdapterTransformer implements TextTransformer, ConfiguredObject {

    public static final HtmlCleanerAdapterTransformer getDefaultHtmlToTextInstance() {
        return defaultHtmlToText.get();
    }

    public static final HtmlCleanerAdapterTransformer getDefaultHtmlToHtmlCompatibleTextInstance() {
        return defaultHtmlToHtmlCompatibleText.get();
    }

    private static final HtmlCleanerAdapterTransformer loadDefaultHtmlToTextInstance() {
        return new HtmlCleanerAdapterTransformer(defaultHtmlToTextConfig());
    }

    private static final HtmlCleanerAdapterTransformer loadDefaultHtmlToHtmlCompatibleTextInstance() {
        return new HtmlCleanerAdapterTransformer(defaultHtmlToHtmlCompatibleTextConfig());
    }

    private static final Configuration defaultHtmlToTextConfig() {
        String trueStr = NativeTypeConversion.booleanToString(true);
        return SimpleProperties.asConfiguration(
            ImmutableMap.of(
                ConfiguredObject.DISPLAY_PROP_NAME_NAME,
                "HTML Cleaner HTML to Text",
                ConfiguredObject.PROP_NAME_DESCRIPTION,
                "Renders text from an HTML document or fragment using htmlcleaner.",
                "cleaner_TransResCharsToNCR", trueStr,
                "cleaner_TranslateSpecialEntities", trueStr,
                "cleaner_OmitComments", trueStr,
                "cleaner_AdvancedXmlEscape", trueStr,
                "cleaner_OmitXmlDeclaration", trueStr,
                "cleaner_DeserializeEntities", trueStr,
                "cleaner_CleanMode", HtmlCleanerAdapter.CleanMode.FRAGMENT.name(),
                "cleaner_SerializationMode", HtmlCleanerAdapter.SerializationMode.TEXT_ONLY.name()
            ),
            "html_to_text"
        );
    }

    private static final Configuration defaultHtmlToHtmlCompatibleTextConfig() {
        return SimpleProperties.asConfiguration(
            ImmutableMap.of(
                ConfiguredObject.DISPLAY_PROP_NAME_NAME,
                "HTML Cleaner HTML to Text with HTML Entities",
                ConfiguredObject.PROP_NAME_DESCRIPTION,
                "Renders the text from an HTML document or fragment cleaned using htmlcleaner, containing no HTML other than any entity-encoded runs of text necessary to represent special characters such as tag delimiters, ampersands, etc.",
                "cleaner_SerializationMode",
                HtmlCleanerAdapter.SerializationMode.TEXT_ONLY_WITH_HTML_ENTITIES.name()
            ),
            "html_to_html_text"
        ).withDefaults(getDefaultHtmlToTextInstance().getConfiguration());
    }

    private static final Supplier<HtmlCleanerAdapterTransformer> defaultHtmlToText = Suppliers.memoize(
        HtmlCleanerAdapterTransformer::loadDefaultHtmlToTextInstance
    );

    private static final Supplier<HtmlCleanerAdapterTransformer> defaultHtmlToHtmlCompatibleText = Suppliers.memoize(
        HtmlCleanerAdapterTransformer::loadDefaultHtmlToHtmlCompatibleTextInstance
    );

    private final Configuration config;

    private final HtmlCleanerAdapter cleaner;

    public HtmlCleanerAdapterTransformer(Configuration config) {
        this.config = config;
        cleaner = HtmlCleanerAdapter.getInstance(config.getNamespace("cleaner"));
    }

    @Override
    public final String transform(@Nullable CharSequence text) {
        if (text == null) {
            return "";
        }
        if (text.isEmpty()) {
            return text.toString();
        }
        try (CharSequenceReader reader = new CharSequenceReader(text)) {
            StringWriter writer = new StringWriter();
            transform(reader, writer);
            writer.flush();
            return writer.toString();
        }
        catch (IOException e) {
            // This should not happen.
            throw new RuntimeException(e);
        }
        catch (TextTransformationException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public final void transform(@Nullable CharSequence text, @Nonnull Appendable out)
        throws IOException, TextTransformationException {
        if (StringUtils.isEmpty(text)) {
            return;
        }
        try (StringReader reader = new StringReader(text.toString())) {
            if (out instanceof Writer w) {
                transform(reader, w);
            }
            else if (out instanceof StringBuilder buff) {
                StringBuilderWriter w = new StringBuilderWriter(buff);
                transform(reader, w);
                w.flush();
            }
            else {
                try (StringWriter sw = new StringWriter()) {
                    transform(reader, sw);
                    StringWriteUtil.safeAppend(out, sw.toString());
                }
                catch (IOException e) {
                    // For StringWriter close -- this should not happen.
                }
            }
        }
    }

    @Override
    public final void transform(@Nonnull Reader reader, @Nonnull Writer writer)
        throws IOException, TextTransformationException {
        cleaner.clean(reader, writer);
    }

    @Nonnull
    @Override
    public final Configuration getConfiguration() {
        return config;
    }

}
