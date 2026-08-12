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

package com.tractionsoftware.commons.xml;

import com.tractionsoftware.commons.config.Configuration;
import com.tractionsoftware.commons.properties.SimpleProperties;
import com.tractionsoftware.commons.text.TextTransformationException;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.URL;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for {@link XslFileTextTransformer} and the abstract {@link JaxpXsltTextTransformer} base.
 */
public final class XslFileTextTransformerTest {

    private static final String IDENTITY_XSL_RESOURCE = "com/tractionsoftware/commons/xml/identity.xsl";

    private static String xslFilePath;
    private static XslFileTextTransformer transformer;

    @BeforeAll
    static void setUpClass() throws Exception {
        URL resource = XslFileTextTransformerTest.class.getClassLoader().getResource(IDENTITY_XSL_RESOURCE);
        assertNotNull(resource, "identity.xsl test resource must exist");
        xslFilePath = new java.io.File(resource.toURI()).getAbsolutePath();

        Configuration config = SimpleProperties.asConfiguration(
            Map.of("xsl_file_path", xslFilePath), "test"
        );
        transformer = new XslFileTextTransformer(config);
    }

    // =====================================================================
    // Construction / configuration
    // =====================================================================

    @Test
    void getConfiguration_returnsConfig() {
        assertNotNull(transformer.getConfiguration());
        assertEquals(xslFilePath, transformer.getConfiguration().getProperty("xsl_file_path"));
    }

    @Test
    void constructor_missingXslFilePath_throws() {
        Configuration empty = SimpleProperties.getEmptyConfiguration("empty");
        assertThrows(Exception.class, () -> new XslFileTextTransformer(empty));
    }

    @Test
    void constructor_nullConfig_throws() {
        assertThrows(NullPointerException.class, () -> new XslFileTextTransformer(null));
    }

    @Test
    void toDebugString_containsFilePath() {
        String debug = transformer.toDebugString();
        assertNotNull(debug);
        assertTrue(debug.contains("identity.xsl"), debug);
    }

    // =====================================================================
    // transform(CharSequence, Appendable) — identity round-trip
    // =====================================================================

    @Test
    void transform_charSequence_simpleXml_roundTrip() throws Exception {
        String xml = "<root><child>hello</child></root>";
        StringBuilder sb = new StringBuilder();
        transformer.transform(xml, sb);
        String result = sb.toString();
        assertTrue(result.contains("hello"), result);
        assertTrue(result.contains("root"), result);
    }

    @Test
    void transform_charSequence_null_producesNoOutput() throws Exception {
        StringBuilder sb = new StringBuilder();
        transformer.transform((CharSequence) null, sb);
        assertEquals("", sb.toString());
    }

    @Test
    void transform_charSequence_invalidXml_throws() {
        assertThrows(TextTransformationException.class,
            () -> transformer.transform("not xml <<<", new StringBuilder()));
    }

    // =====================================================================
    // transform(Reader, Writer)
    // =====================================================================

    @Test
    void transform_readerWriter_simpleXml() throws Exception {
        String xml = "<doc><item>42</item></doc>";
        StringWriter sw = new StringWriter();
        transformer.transform(new StringReader(xml), sw);
        assertTrue(sw.toString().contains("42"), sw.toString());
    }

    @Test
    void transform_readerWriter_invalidXml_throws() {
        assertThrows(TextTransformationException.class,
            () -> transformer.transform(new StringReader("<bad"), new StringWriter()));
    }

    // =====================================================================
    // Multiple invocations (thread-safe Templates cache)
    // =====================================================================

    @Test
    void transform_calledMultipleTimes_consistent() throws Exception {
        String xml = "<x>1</x>";
        StringBuilder first = new StringBuilder();
        StringBuilder second = new StringBuilder();
        transformer.transform(xml, first);
        transformer.transform(xml, second);
        // Both results should contain the same content
        assertTrue(first.toString().contains("1"));
        assertEquals(first.toString(), second.toString());
    }

}
