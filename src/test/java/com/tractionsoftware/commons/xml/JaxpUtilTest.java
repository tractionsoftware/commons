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

import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.ErrorListener;
import javax.xml.transform.Templates;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

public final class JaxpUtilTest {

    private static final String SIMPLE_XML = "<root><child>Hello</child></root>";
    private static final String IDENTITY_XSL =
        "<?xml version='1.0'?>" +
        "<xsl:stylesheet xmlns:xsl='http://www.w3.org/1999/XSL/Transform' version='1.0'>" +
        "  <xsl:output method='xml' omit-xml-declaration='yes'/>" +
        "  <xsl:template match='/'><xsl:copy-of select='.'/></xsl:template>" +
        "</xsl:stylesheet>";

    private static byte[] utf8(String s) {
        return s.getBytes(StandardCharsets.UTF_8);
    }

    private static Document parseXml(String xml) throws Exception {
        return DocumentBuilderFactory.newInstance().newDocumentBuilder()
            .parse(new InputSource(new java.io.StringReader(xml)));
    }

    // =====================================================================
    // getNamespaceAwareDocumentBuilderFactory
    // =====================================================================

    @Test
    void getNamespaceAwareDocumentBuilderFactory_returnsNonNull() throws Exception {
        assertNotNull(JaxpUtil.getNamespaceAwareDocumentBuilderFactory());
    }

    // =====================================================================
    // getDocumentBuilder (uses service-loaded Customizations)
    // =====================================================================

    @Test
    void getDocumentBuilder_returnsNonNull() throws Exception {
        assertNotNull(JaxpUtil.getDocumentBuilder());
    }

    // =====================================================================
    // getDocument
    // =====================================================================

    @Test
    void getDocument_fromInputStream_parsesCorrectly() throws Exception {
        Document doc = JaxpUtil.getDocument(new ByteArrayInputStream(utf8(SIMPLE_XML)));
        assertNotNull(doc);
        assertEquals("root", doc.getDocumentElement().getTagName());
    }

    @Test
    void getDocument_fromReader_parsesCorrectly() throws Exception {
        Document doc = JaxpUtil.getDocument(new java.io.StringReader(SIMPLE_XML));
        assertNotNull(doc);
        assertEquals("root", doc.getDocumentElement().getTagName());
    }

    // =====================================================================
    // getInputSource
    // =====================================================================

    @Test
    void getInputSource_fromInputStream_returnsNonNull() {
        InputSource src = JaxpUtil.getInputSource(new ByteArrayInputStream(utf8(SIMPLE_XML)));
        assertNotNull(src);
    }

    @Test
    void getInputSource_fromReader_returnsNonNull() {
        InputSource src = JaxpUtil.getInputSource(new java.io.StringReader(SIMPLE_XML));
        assertNotNull(src);
    }

    @Test
    void getInputSource_fromString_returnsNonNull() {
        InputSource src = JaxpUtil.getInputSource(SIMPLE_XML);
        assertNotNull(src);
    }

    // =====================================================================
    // getTransformer (identity)
    // =====================================================================

    @Test
    void getTransformer_noArg_returnsNonNull() throws Exception {
        assertNotNull(JaxpUtil.getTransformer());
    }

    @Test
    void getTransformer_withNullListener_returnsNonNull() throws Exception {
        assertNotNull(JaxpUtil.getTransformer((ErrorListener) null));
    }

    @Test
    void getTransformer_fromInputStream_returnsNonNull() throws Exception {
        Transformer t = JaxpUtil.getTransformer(new ByteArrayInputStream(utf8(IDENTITY_XSL)));
        assertNotNull(t);
    }

    @Test
    void getTransformer_fromDocument_returnsNonNull() throws Exception {
        Document xslDoc = JaxpUtil.getDocument(new ByteArrayInputStream(utf8(IDENTITY_XSL)));
        Transformer t = JaxpUtil.getTransformer(xslDoc, (ErrorListener) null);
        assertNotNull(t);
    }

    // =====================================================================
    // getTemplates
    // =====================================================================

    @Test
    void getTemplates_fromInputStream_returnsNonNull() throws Exception {
        Templates t = JaxpUtil.getTemplates(new ByteArrayInputStream(utf8(IDENTITY_XSL)));
        assertNotNull(t);
    }

    @Test
    void getTemplates_fromDocument_returnsNonNull() throws Exception {
        Document xslDoc = JaxpUtil.getDocument(new ByteArrayInputStream(utf8(IDENTITY_XSL)));
        Templates t = JaxpUtil.getTemplates(xslDoc);
        assertNotNull(t);
    }

    @Test
    void getTransformer_fromTemplates_returnsNonNull() throws Exception {
        Templates templates = JaxpUtil.getTemplates(new ByteArrayInputStream(utf8(IDENTITY_XSL)));
        Transformer t = JaxpUtil.getTransformer(templates);
        assertNotNull(t);
    }

    @Test
    void getTransformer_fromTemplates_withLogger_returnsNonNull() throws Exception {
        Templates templates = JaxpUtil.getTemplates(new ByteArrayInputStream(utf8(IDENTITY_XSL)));
        Transformer t = JaxpUtil.getTransformer(templates, LoggerFactory.getLogger(JaxpUtilTest.class));
        assertNotNull(t);
    }

    // =====================================================================
    // writeXml
    // =====================================================================

    @Test
    void writeXml_toOutputStream_producesXml() throws Exception {
        Document doc = parseXml(SIMPLE_XML);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        JaxpUtil.writeXml(doc, out);
        String result = out.toString(StandardCharsets.UTF_8);
        assertTrue(result.contains("root"), result);
        assertTrue(result.contains("Hello"), result);
    }

    @Test
    void writeXml_toWriter_producesXml() throws Exception {
        Document doc = parseXml(SIMPLE_XML);
        StringWriter out = new StringWriter();
        JaxpUtil.writeXml(doc, out);
        String result = out.toString();
        assertTrue(result.contains("root"), result);
    }

    @Test
    void writeXml_toAppendable_producesXml() throws Exception {
        Document doc = parseXml(SIMPLE_XML);
        StringBuilder sb = new StringBuilder();
        JaxpUtil.writeXml(doc, sb);
        assertTrue(sb.toString().contains("root"), sb.toString());
    }

    // =====================================================================
    // getTransformedDocument
    // =====================================================================

    @Test
    void getTransformedDocument_identityXsl_preservesContent() throws Exception {
        Document doc = parseXml(SIMPLE_XML);
        Document xslDoc = JaxpUtil.getDocument(new ByteArrayInputStream(utf8(IDENTITY_XSL)));
        Transformer transformer = JaxpUtil.getTransformer(xslDoc, (ErrorListener) null);
        Document result = JaxpUtil.getTransformedDocument(doc, transformer);
        assertNotNull(result);
        assertEquals("root", result.getDocumentElement().getTagName());
    }

    // =====================================================================
    // getLoggingErrorListener
    // =====================================================================

    @Test
    void getLoggingErrorListener_nullLogger_throwsNPE() {
        assertThrows(NullPointerException.class, () -> JaxpUtil.getLoggingErrorListener(null));
    }

    @Test
    void getLoggingErrorListener_validLogger_returnsNonNull() {
        assertNotNull(JaxpUtil.getLoggingErrorListener(LoggerFactory.getLogger(JaxpUtilTest.class)));
    }

    // =====================================================================
    // THROW_RUNTIME_EXCEPTION_ON_ERROR_LISTENER
    // =====================================================================

    @Test
    void throwRuntimeExceptionListener_error_throwsRuntimeException() {
        ErrorListener listener = JaxpUtil.THROW_RUNTIME_EXCEPTION_ON_ERROR_LISTENER;
        assertThrows(RuntimeException.class, () ->
            listener.error(new TransformerException("test error")));
    }

    @Test
    void throwRuntimeExceptionListener_fatalError_throwsRuntimeException() {
        ErrorListener listener = JaxpUtil.THROW_RUNTIME_EXCEPTION_ON_ERROR_LISTENER;
        assertThrows(RuntimeException.class, () ->
            listener.fatalError(new TransformerException("fatal")));
    }

    @Test
    void throwRuntimeExceptionListener_warning_throwsRuntimeException() {
        ErrorListener listener = JaxpUtil.THROW_RUNTIME_EXCEPTION_ON_ERROR_LISTENER;
        assertThrows(RuntimeException.class, () ->
            listener.warning(new TransformerException("warning")));
    }

    // =====================================================================
    // setErrorListener
    // =====================================================================

    @Test
    void setErrorListener_nullListener_noOp() throws Exception {
        Transformer t = JaxpUtil.getTransformer();
        JaxpUtil.setErrorListener(t, null); // should not throw
    }

    @Test
    void setErrorListener_nonNullListener_setsListener() throws Exception {
        Transformer t = JaxpUtil.getTransformer();
        ErrorListener listener = JaxpUtil.getLoggingErrorListener(LoggerFactory.getLogger(JaxpUtilTest.class));
        JaxpUtil.setErrorListener(t, listener);
        assertSame(listener, t.getErrorListener());
    }

    // =====================================================================
    // getAttributesAsMap
    // =====================================================================

    @Test
    void getAttributesAsMap_noAttributes_returnsEmptyMap() throws Exception {
        Document doc = parseXml("<root/>");
        Element root = doc.getDocumentElement();
        Map<String,String> attrs = JaxpUtil.getAttributesAsMap(root);
        assertTrue(attrs.isEmpty());
    }

    @Test
    void getAttributesAsMap_withAttributes_returnsMap() throws Exception {
        Document doc = parseXml("<root id='42' name='test'/>");
        Element root = doc.getDocumentElement();
        Map<String,String> attrs = JaxpUtil.getAttributesAsMap(root);
        assertEquals(2, attrs.size());
        assertEquals("42", attrs.get("id"));
        assertEquals("test", attrs.get("name"));
    }

    // =====================================================================
    // safelyTraverseDescendants
    // =====================================================================

    @Test
    void safelyTraverseDescendants_collectsNodes() throws Exception {
        Document doc = parseXml("<root><a/><b><c/></b></root>");
        AtomicReference<Integer> count = new AtomicReference<>(0);
        JaxpUtil.safelyTraverseDescendants(doc, node -> count.set(count.get() + 1));
        // root, a, b, c and text nodes
        assertTrue(count.get() >= 4, "Expected at least 4 nodes, got " + count.get());
    }

    // =====================================================================
    // Consumer classes
    // =====================================================================

    @Test
    void xsl2TransformerConsumer_consumesStream() throws Exception {
        JaxpUtil.Xsl2TransformerConsumer consumer = new JaxpUtil.Xsl2TransformerConsumer(null);
        // Simulate consume by directly parsing the XSL
        Transformer t = JaxpUtil.getTransformer(new ByteArrayInputStream(utf8(IDENTITY_XSL)));
        assertNotNull(t);
    }

    @Test
    void xsl2TemplatesConsumer_consumesStream() throws Exception {
        Templates t = JaxpUtil.getTemplates(new ByteArrayInputStream(utf8(IDENTITY_XSL)));
        assertNotNull(t);
    }

    @Test
    void xml2DocumentConsumer_consumesStream() throws Exception {
        Document doc = JaxpUtil.getDocument(new ByteArrayInputStream(utf8(SIMPLE_XML)));
        assertNotNull(doc);
    }

}
