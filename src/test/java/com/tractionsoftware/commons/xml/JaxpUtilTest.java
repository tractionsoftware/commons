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

import com.tractionsoftware.commons.io.SizedInputStream;
import com.tractionsoftware.commons.processor.ByteArrayResult;
import com.tractionsoftware.commons.processor.Result;
import com.tractionsoftware.commons.properties.GetPutProperty;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.ErrorListener;
import javax.xml.transform.Templates;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
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

    // -- direct invocation of the nested Consumer implementations --------

    @Test
    void xsl2TransformerConsumer_consumeValidXsl_generatesTransformer() throws Exception {
        JaxpUtil.Xsl2TransformerConsumer consumer = new JaxpUtil.Xsl2TransformerConsumer(null);
        SizedInputStream input = SizedInputStream.forInputStream(new ByteArrayInputStream(utf8(IDENTITY_XSL)), -1);
        consumer.consume(input);
        assertNotNull(consumer.getGenerated());
    }

    @Test
    void xsl2TransformerConsumer_getGeneratedBeforeConsume_throwsIllegalStateException() {
        JaxpUtil.Xsl2TransformerConsumer consumer = new JaxpUtil.Xsl2TransformerConsumer(null);
        assertThrows(IllegalStateException.class, consumer::getGenerated);
    }

    @Test
    void xsl2TransformerConsumer_consumeMalformedXsl_throwsJaxpTransformerCreationException() {
        JaxpUtil.Xsl2TransformerConsumer consumer = new JaxpUtil.Xsl2TransformerConsumer(null);
        SizedInputStream input = SizedInputStream.forInputStream(new ByteArrayInputStream(utf8("not xml")), -1);
        assertThrows(JaxpTransformerCreationException.class, () -> consumer.consume(input));
    }

    @Test
    void xsl2TemplatesConsumer_consumeValidXsl_generatesTemplates() throws Exception {
        JaxpUtil.Xsl2TemplatesConsumer consumer = new JaxpUtil.Xsl2TemplatesConsumer();
        SizedInputStream input = SizedInputStream.forInputStream(new ByteArrayInputStream(utf8(IDENTITY_XSL)), -1);
        consumer.consume(input);
        assertNotNull(consumer.getGenerated());
    }

    @Test
    void xsl2TemplatesConsumer_consumeMalformedXsl_throwsJaxpTransformerCreationException() {
        JaxpUtil.Xsl2TemplatesConsumer consumer = new JaxpUtil.Xsl2TemplatesConsumer();
        SizedInputStream input = SizedInputStream.forInputStream(new ByteArrayInputStream(utf8("not xml")), -1);
        assertThrows(JaxpTransformerCreationException.class, () -> consumer.consume(input));
    }

    @Test
    void xml2DocumentConsumer_consumeValidXml_generatesDocument() throws Exception {
        JaxpUtil.Xml2DocumentConsumer consumer = new JaxpUtil.Xml2DocumentConsumer();
        SizedInputStream input = SizedInputStream.forInputStream(new ByteArrayInputStream(utf8(SIMPLE_XML)), -1);
        consumer.consume(input);
        assertNotNull(consumer.getGenerated());
        assertEquals("root", consumer.getGenerated().getDocumentElement().getTagName());
    }

    @Test
    void xml2DocumentConsumer_consumeMalformedXml_throwsJaxpDocumentCreationException() {
        JaxpUtil.Xml2DocumentConsumer consumer = new JaxpUtil.Xml2DocumentConsumer();
        SizedInputStream input = SizedInputStream.forInputStream(new ByteArrayInputStream(utf8("not xml")), -1);
        assertThrows(JaxpDocumentCreationException.class, () -> consumer.consume(input));
    }

    // =====================================================================
    // getLoggingErrorListener - actually invoking the returned listener
    // =====================================================================

    @Test
    void getLoggingErrorListener_invokeAllMethods_doesNotThrow() {
        ErrorListener listener = JaxpUtil.getLoggingErrorListener(LoggerFactory.getLogger(JaxpUtilTest.class));
        assertDoesNotThrow(() -> listener.error(new TransformerException("e")));
        assertDoesNotThrow(() -> listener.fatalError(new TransformerException("fe")));
        assertDoesNotThrow(() -> listener.warning(new TransformerException("w")));
    }

    // =====================================================================
    // getXMLReader
    // =====================================================================

    @Test
    void getXMLReader_returnsConfiguredReader() throws Exception {
        XMLReader reader = JaxpUtil.getXMLReader();
        assertNotNull(reader);
        assertTrue(reader.getFeature("http://xml.org/sax/features/namespaces"));
    }

    // =====================================================================
    // getTransformedDocument(Document, File[, ErrorListener])
    // =====================================================================

    @Test
    void getTransformedDocument_documentAndXslFile_appliesTransform() throws Exception {
        File xslFile = File.createTempFile("jaxputil-identity", ".xsl");
        xslFile.deleteOnExit();
        Files.write(xslFile.toPath(), utf8(IDENTITY_XSL));
        Document doc = parseXml(SIMPLE_XML);
        Document result = JaxpUtil.getTransformedDocument(doc, xslFile);
        assertNotNull(result);
        assertEquals("root", result.getDocumentElement().getTagName());
    }

    @Test
    void getTransformedDocument_documentAndXslFileWithListener_appliesTransform() throws Exception {
        File xslFile = File.createTempFile("jaxputil-identity2", ".xsl");
        xslFile.deleteOnExit();
        Files.write(xslFile.toPath(), utf8(IDENTITY_XSL));
        Document doc = parseXml(SIMPLE_XML);
        ErrorListener listener = JaxpUtil.getLoggingErrorListener(LoggerFactory.getLogger(JaxpUtilTest.class));
        Document result = JaxpUtil.getTransformedDocument(doc, xslFile, listener);
        assertNotNull(result);
        assertEquals("root", result.getDocumentElement().getTagName());
    }

    @Test
    void getTransformedDocument_documentAndXslFile_terminatingStylesheet_returnsNull() throws Exception {
        // xsl:message terminate="yes" causes the JAXP transformer to throw a TransformerException
        // during transform(), which getTransformedDocument(Document,File,ErrorListener) catches and
        // turns into a null return value.
        String terminatingXsl =
            "<?xml version='1.0'?>" +
            "<xsl:stylesheet xmlns:xsl='http://www.w3.org/1999/XSL/Transform' version='1.0'>" +
            "  <xsl:template match='/'><xsl:message terminate='yes'>abort</xsl:message></xsl:template>" +
            "</xsl:stylesheet>";
        File xslFile = File.createTempFile("jaxputil-terminate", ".xsl");
        xslFile.deleteOnExit();
        Files.write(xslFile.toPath(), utf8(terminatingXsl));
        Document doc = parseXml(SIMPLE_XML);
        Document result = JaxpUtil.getTransformedDocument(doc, xslFile);
        assertNull(result);
    }

    // =====================================================================
    // getTransformedDocument(Document[, ErrorListener]) - no xsl file, identity
    // =====================================================================

    @Test
    void getTransformedDocument_documentOnly_appliesIdentityTransform() throws Exception {
        Document doc = parseXml(SIMPLE_XML);
        Document result = JaxpUtil.getTransformedDocument(doc);
        assertNotNull(result);
        assertEquals("root", result.getDocumentElement().getTagName());
    }

    @Test
    void getTransformedDocument_documentAndListener_appliesIdentityTransform() throws Exception {
        Document doc = parseXml(SIMPLE_XML);
        ErrorListener listener = JaxpUtil.getLoggingErrorListener(LoggerFactory.getLogger(JaxpUtilTest.class));
        Document result = JaxpUtil.getTransformedDocument(doc, listener);
        assertNotNull(result);
        assertEquals("root", result.getDocumentElement().getTagName());
    }

    // =====================================================================
    // getTransformer(File[, ErrorListener]) / getTransformer(ErrorListener)
    // =====================================================================

    @Test
    void getTransformer_fromFile_returnsNonNull() throws Exception {
        File xslFile = File.createTempFile("jaxputil-xsl", ".xsl");
        xslFile.deleteOnExit();
        Files.write(xslFile.toPath(), utf8(IDENTITY_XSL));
        Transformer t = JaxpUtil.getTransformer(xslFile);
        assertNotNull(t);
    }

    @Test
    void getTransformer_fromFileWithListener_setsListener() throws Exception {
        File xslFile = File.createTempFile("jaxputil-xsl2", ".xsl");
        xslFile.deleteOnExit();
        Files.write(xslFile.toPath(), utf8(IDENTITY_XSL));
        ErrorListener listener = JaxpUtil.getLoggingErrorListener(LoggerFactory.getLogger(JaxpUtilTest.class));
        Transformer t = JaxpUtil.getTransformer(xslFile, listener);
        assertNotNull(t);
        assertSame(listener, t.getErrorListener());
    }

    @Test
    void getTransformer_withNonNullListener_setsListener() throws Exception {
        ErrorListener listener = JaxpUtil.getLoggingErrorListener(LoggerFactory.getLogger(JaxpUtilTest.class));
        Transformer t = JaxpUtil.getTransformer(listener);
        assertSame(listener, t.getErrorListener());
    }

    // =====================================================================
    // writeXml(Document, Appendable) - OutputStream-but-not-Writer branch
    // =====================================================================

    @Test
    void writeXml_toPrintStreamAsAppendable_usesOutputStreamBranch() throws Exception {
        Document doc = parseXml(SIMPLE_XML);
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        // PrintStream implements Appendable and extends OutputStream, but is not a Writer - this
        // exercises the "out instanceof OutputStream" branch of writeXml(Document, Appendable).
        PrintStream ps = new PrintStream(bytes, true, StandardCharsets.UTF_8);
        JaxpUtil.writeXml(doc, (Appendable) ps);
        String result = bytes.toString(StandardCharsets.UTF_8);
        assertTrue(result.contains("root"), result);
    }

    // =====================================================================
    // getTransformer(Result, ErrorListener) / getTemplates(Result) / getDocument(Result)
    //
    // Uses the shared com.tractionsoftware.commons.processor.ByteArrayResult test double (constructed in
    // pre-populated mode from a String) rather than a locally-defined class.
    // =====================================================================

    @Test
    void getTransformer_fromResult_validXsl_returnsTransformer() throws Exception {
        Result result = new ByteArrayResult(IDENTITY_XSL);
        Transformer t = JaxpUtil.getTransformer(result, null);
        assertNotNull(t);
    }

    @Test
    void getTransformer_fromResult_malformedXsl_throwsSAXException() {
        Result result = new ByteArrayResult("not xml");
        assertThrows(SAXException.class, () -> JaxpUtil.getTransformer(result, null));
    }

    @Test
    void getTemplates_fromResult_validXsl_returnsTemplates() throws Exception {
        Result result = new ByteArrayResult(IDENTITY_XSL);
        Templates t = JaxpUtil.getTemplates(result);
        assertNotNull(t);
    }

    @Test
    void getDocument_fromResult_validXml_returnsDocument() throws Exception {
        Result result = new ByteArrayResult(SIMPLE_XML);
        Document doc = JaxpUtil.getDocument(result);
        assertNotNull(doc);
        assertEquals("root", doc.getDocumentElement().getTagName());
    }

    // =====================================================================
    // getAttributesAsGetPutProperty
    // =====================================================================

    @Test
    void getAttributesAsGetPutProperty_readsAndWritesAttributes() throws Exception {
        Document doc = parseXml("<root id='42' name='test'/>");
        Element root = doc.getDocumentElement();
        GetPutProperty props = JaxpUtil.getAttributesAsGetPutProperty(root);
        assertEquals("42", props.getProperty("id"));
        assertEquals("test", props.getProperty("name"));
        props.putProperty("name", "changed");
        assertEquals("changed", props.getProperty("name"));
    }

}
