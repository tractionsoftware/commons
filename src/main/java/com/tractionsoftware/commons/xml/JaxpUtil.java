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

import com.google.common.annotations.Beta;
import com.tractionsoftware.commons.io.FileUtil;
import com.tractionsoftware.commons.io.IOUtil;
import com.tractionsoftware.commons.io.SizedInputStream;
import com.tractionsoftware.commons.lang.JavaUtil;
import com.tractionsoftware.commons.processor.Consumer;
import com.tractionsoftware.commons.processor.Result;
import com.tractionsoftware.commons.properties.GetPutProperty;
import com.tractionsoftware.commons.properties.MapPropertyStore;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import org.apache.commons.io.output.AppendableWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.*;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.*;
import java.util.*;
import java.util.function.Supplier;

/**
 * Utility and adapter methods and classes associated with <a href="http://wikipedia.org/wiki/JAXP" title="Java API for
 * XML Processing">JAXP</a>.
 *
 * <p>
 * See also the packages javax.xml.parsers, javax.xml.transform, org.w3c.dom, org.xml.sax.
 *
 * @author Dave Shepperton
 * @since 4.0
 */
public final class JaxpUtil {

    /*
     * Not instantiable.
     */
    private JaxpUtil() {
    }

    private static final Logger LOGGER = LoggerFactory.getLogger(JaxpUtil.class);

    private static final Supplier<? extends Customizations> customizations =
        JavaUtil.lazyServiceLoader(Customizations.class, (Customizations) null, LOGGER);

    /**
     * An ErrorListener that throws RuntimeExceptions wrapping the original {@link TransformerException}s.
     */
    public static final ErrorListener THROW_RUNTIME_EXCEPTION_ON_ERROR_LISTENER = new ErrorListener() {

        @Override
        public final void error(TransformerException e) {
            throw new RuntimeException(e);
        }

        @Override
        public void fatalError(TransformerException e) {
            throw new RuntimeException(e);
        }

        @Override
        public void warning(TransformerException e) {
            throw new RuntimeException(e);
        }

    };

    /**
     * A Consumer that converts a Result directly into some sort of JAXP object.
     *
     * @param <T>
     *     the type of object being generated from the Result.
     * @param <X>
     *     the type of JaxpException thrown during object generation.
     * @author Dave Shepperton
     */
    private static abstract class Xml2JaxpObjectConsumer<T, X extends JaxpException> implements Consumer<X> {

        private volatile T generated;

        /**
         * Consumes the input stream by creating the appropriate type of JAXP object.
         */
        @Override
        public final void consume(SizedInputStream xmlInputStream) throws IOException, X {
            try {
                generated = generate(xmlInputStream);
            }
            catch (Exception e) {
                handleException(e);
            }
        }

        protected abstract T generate(InputStream xmlInputStream) throws Exception;

        protected abstract void handleException(Exception e) throws IOException, X;

        public final T getGenerated() {
            if (generated == null) {
                throw new IllegalStateException();
            }
            return generated;
        }

    }

    /**
     * A Consumer class that consumes an InputStream and converts it to a Transformer object.
     *
     * @author Dave Shepperton
     * @since 4.0
     */
    public static final class Xsl2TransformerConsumer
        extends Xml2JaxpObjectConsumer<Transformer,JaxpTransformerCreationException> {

        private final ErrorListener listener;

        public Xsl2TransformerConsumer(ErrorListener listener) {
            this.listener = listener;
        }

        /**
         * Creates a Transformer from the InputStream.
         */
        @Override
        protected final Transformer generate(InputStream xslInputStream) throws Exception {
            return JaxpUtil.getTransformer(xslInputStream, listener);
        }

        @Override
        protected final void handleException(Exception e) throws IOException, JaxpTransformerCreationException {
            JaxpTransformerCreationException.handle(e);
        }

    }

    /**
     * A Consumer class that consumes an InputStream and converts it to a Templates object.
     *
     * @author Dave Shepperton
     * @since 4.0
     */
    public static final class Xsl2TemplatesConsumer
        extends Xml2JaxpObjectConsumer<Templates,JaxpTransformerCreationException> {

        /**
         * Creates a Templates object from the InputStream.
         */
        @Override
        protected final Templates generate(InputStream xslInputStream) throws Exception {
            return JaxpUtil.getTemplates(xslInputStream);
        }

        @Override
        protected final void handleException(Exception e) throws IOException, JaxpTransformerCreationException {
            JaxpTransformerCreationException.handle(e);
        }

    }

    /**
     * A Consumer class that consumes an InputStream and converts it to a Document object.
     *
     * @author Dave Shepperton
     * @since 4.0
     */
    public static final class Xml2DocumentConsumer
        extends Xml2JaxpObjectConsumer<Document,JaxpDocumentCreationException> {

        /**
         * Creates a Transformer from the InputStream.
         */
        @Override
        protected final Document generate(InputStream xslInputStream) throws Exception {
            return JaxpUtil.getDocument(xslInputStream);
        }

        @Override
        protected final void handleException(Exception e) throws IOException, JaxpDocumentCreationException {
            JaxpDocumentCreationException.handle(e);
        }

    }

    /**
     * Returns an ErrorListener that logs stack traces to the given {@link Logger}.
     */
    public static final ErrorListener getLoggingErrorListener(Logger logger) {
        Objects.requireNonNull(logger, "logger");
        return new ErrorListener() {
            @Override
            public final void error(TransformerException e) {
                logger.error("", e);
            }

            @Override
            public final void fatalError(TransformerException e) {
                logger.error("", e);
            }

            @Override
            public final void warning(TransformerException e) {
                logger.warn("", e);
            }
        };
    }

    /**
     * Uses the given InputStream, interpreted as UTF-8 character data, to create an XML Document.
     *
     * @param xmlInput
     *     the InputStream for the UTF-8 character data representing an XML document.
     * @return the resulting XML Document.
     * @throws SAXException
     *     if an error is encountered during XML parsing, such as in the case of the input not representing a valid XML
     *     document.
     * @throws IOException
     *     if there is a problem reading from the InputStream.
     * @throws ParserConfigurationException
     *     if there is a serious configuration error with the XML parser.
     */
    public static final Document getDocument(InputStream xmlInput)
        throws SAXException, IOException, ParserConfigurationException {
        return getDocument(IOUtil.getBufferedUtf8Reader(xmlInput));
    }

    /**
     * Uses the character data from the given Reader to create an XML Document.
     *
     * @param xmlReader
     *     the Reader for the XML character data.
     * @return the resulting XML Document.
     * @throws SAXException
     *     if an error is encountered during XML parsing, such as in the case of the input not representing a valid XML
     *     document.
     * @throws IOException
     *     if there is a problem reading from the Reader.
     * @throws ParserConfigurationException
     *     if there is a serious configuration error with the XML parser.
     */
    public static final Document getDocument(Reader xmlReader)
        throws SAXException, IOException, ParserConfigurationException {
        return getDocumentBuilder().parse(getInputSource(xmlReader));
    }

    /**
     * Creates an InputSource for the XML InputStream, using the UTF-8 character set to interpret the stream.
     *
     * @param xmlInput
     *     the InputStream for the UTF-8 character data representing an XML document.
     * @return the resulting InputSource.
     */
    public static final InputSource getInputSource(InputStream xmlInput) {
        return getInputSource(IOUtil.getBufferedUtf8Reader(xmlInput));
    }

    /**
     * Creates an InputSource for the XML Reader.
     *
     * @param xmlReader
     *     the Reader for the character data representing an XML document.
     * @return the resulting InputSource.
     */
    public static final InputSource getInputSource(Reader xmlReader) {
        return new InputSource(xmlReader);
    }

    /**
     * Creates an InputSource from a String that represents an XML document.
     *
     * @param xml
     *     a String that represents an XML document.
     * @return the resulting InputSource.
     */
    public static final InputSource getInputSource(String xml) {
        return new InputSource(xml);
    }

    /**
     * Creates an XMLReader that has the SAX namespace and Apache XML xinclude features.
     *
     * @return an XMLReader that has the SAX namespace and Apache XML xinclude features.
     * @throws ParserConfigurationException
     *     if there is a serious configuration error with the XML parser.
     * @throws SAXException
     *     if an error is encountered while creating the XMLReader. It is not likely that an Exception of this type will
     *     ever be thrown by this method, but since SAXException is a super-type for various Exceptions used in SAX,
     *     there may be some SAX-implementation reasons for it. This Exception may be removed from this method's
     *     signature at some point in the future.
     */
    public static final XMLReader getXMLReader() throws ParserConfigurationException, SAXException {
        SAXParserFactory spf = SAXParserFactory.newInstance();
        spf.setFeature("http://xml.org/sax/features/namespaces", true);
        spf.setFeature("http://apache.org/xml/features/xinclude", true);
        return spf.newSAXParser().getXMLReader();
    }

    /**
     * Returns a new XML Document which is the result of applying an XSLT transformation, using the given file as the
     * source of the XSL document, to the given input Document.
     *
     * @param document
     *     the input XML Document.
     * @param xsl
     *     the file containing the XSL document that will be used to create the Transformer to be applied to the input
     *     Document.
     * @return a new XML Document which is the result of applying an XSLT transformation, using the given file as the
     *     source of the XSL document, to the given input Document.
     * @throws TransformerConfigurationException
     *     if there is a serious configuration error related to the Transformer.
     * @throws ParserConfigurationException
     *     if there is a serious configuration error with the XML parser.
     * @throws SAXException
     *     if an error is encountered during XML parsing, such as in the case of the input not representing a valid XML
     *     document.
     * @throws IOException
     *     if there is a problem reading the File.
     */
    public static final Document getTransformedDocument(Document document, File xsl)
        throws TransformerConfigurationException, ParserConfigurationException, SAXException, IOException {
        return getTransformedDocument(document, xsl, null);
    }

    /**
     * Returns a new XML Document which is the result of applying an XSLT transformation, using the given file as the
     * source of the XSL document, to the given input Document.
     *
     * @param document
     *     the input XML Document.
     * @param xsl
     *     the file containing the XSL document that will be used to create the Transformer to be applied to the input
     *     Document.
     * @param listener
     *     an optional {@link ErrorListener} that should be used to handle any errors encountered during the
     *     transformation process when using the Transformer returned by this method.
     * @return a new XML Document which is the result of applying an XSLT transformation, using the given file as the
     *     source of the XSL document, to the given input Document.
     * @throws TransformerConfigurationException
     *     if there is a serious configuration error related to the Transformer.
     * @throws ParserConfigurationException
     *     if there is a serious configuration error with the XML parser.
     * @throws SAXException
     *     if an error is encountered during XML parsing, such as in the case of the input not representing a valid XML
     *     document.
     * @throws IOException
     *     if there is a problem reading the File.
     */
    public static final Document getTransformedDocument(Document document, File xsl, ErrorListener listener)
        throws TransformerConfigurationException, ParserConfigurationException, SAXException, IOException {
        DOMResult domResult = new DOMResult();
        Transformer transformer = getTransformer(xsl);
        try {
            transformer.transform(new DOMSource(document), domResult);
        }
        catch (TransformerException e) {
            return null;
        }
        return (Document) domResult.getNode();
    }

    /**
     * Returns a new XML Document which is the result of applying an XSLT transformation to the given input Document.
     *
     * @param document
     *     the input XML Document.
     * @return a new XML Document which is the result of applying an XSLT transformation, using the given file as the
     *     source of the XSL document, to the given input Document.
     * @throws TransformerConfigurationException
     *     if there is a serious configuration error related to the Transformer.
     * @throws ParserConfigurationException
     *     if there is a serious configuration error with the XML parser.
     */
    public static final Document getTransformedDocument(Document document)
        throws TransformerException, TransformerConfigurationException, ParserConfigurationException {
        return getTransformedDocument(document, (ErrorListener) null);
    }

    /**
     * Returns a new XML Document which is the result of applying an XSLT transformation, using the given file as the
     * source of the XSL document, to the given input Document.
     *
     * @param document
     *     the input XML Document.
     * @param listener
     *     an optional {@link ErrorListener} that should be used to handle any errors encountered during the
     *     transformation process when using the Transformer returned by this method.
     * @return a new XML Document which is the result of applying an XSLT transformation, using the given file as the
     *     source of the XSL document, to the given input Document.
     * @throws TransformerConfigurationException
     *     if there is a serious configuration error related to the Transformer.
     * @throws ParserConfigurationException
     *     if there is a serious configuration error with the XML parser.
     */
    public static final Document getTransformedDocument(Document document, ErrorListener listener)
        throws TransformerException, TransformerConfigurationException, ParserConfigurationException {
        return getTransformedDocument(document, getTransformer(listener));
    }

    /**
     * Returns a new XML Document which is the result of applying the given Transformer to the given input Document.
     *
     * @param document
     *     the input XML Document.
     * @param transformer
     *     the Transformer to be applied.
     * @return a new XML Document which is the result of applying the given Transformer to the given input Document.
     * @throws TransformerException
     *     if there is a problem applying the transformation.
     * @throws ParserConfigurationException
     *     if there is a serious configuration error with the XML parser.
     */
    public static final Document getTransformedDocument(Document document, Transformer transformer)
        throws TransformerException, ParserConfigurationException {
        Document resultDocument = getDocumentBuilder().newDocument();
        DOMResult domResult = new DOMResult(resultDocument);
        transformer.transform(new DOMSource(document), domResult);
        return (Document) domResult.getNode();
    }

    /**
     * Returns a Transformer created by interpreting the given InputStream as a UTF-8 XSL document.
     *
     * @param xslInput
     *     the InputStream for the UTF-8 character data representing an XSL document.
     * @return a Transformer created by interpreting the given InputStream as a UTF-8 XSL document.
     * @throws ParserConfigurationException
     *     if there is a serious configuration error with the XML parser.
     * @throws SAXException
     *     if an error is encountered during XML parsing, such as in the case of the input not representing a valid XML
     *     document.
     * @throws IOException
     *     if there is a problem reading from the InputStream.
     * @throws TransformerConfigurationException
     *     if there is a serious configuration error related to the Transformer.
     */
    public static final Transformer getTransformer(InputStream xslInput)
        throws ParserConfigurationException, SAXException, IOException, TransformerConfigurationException {
        return getTransformer(xslInput, null);
    }

    /**
     * Returns a DocumentBuilderFactory instance that is namespace-aware.
     *
     * @return a DocumentBuilderFactory instance that is namespace-aware.
     * @throws ParserConfigurationException
     *     if there is a serious configuration error with the XML parser.
     */
    public static final DocumentBuilderFactory getNamespaceAwareDocumentBuilderFactory()
        throws ParserConfigurationException {
        DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
        builderFactory.setNamespaceAware(true);
        builderFactory.setValidating(false);
        builderFactory.setFeature("http://xml.org/sax/features/validation", false);
        builderFactory.setFeature("http://apache.org/xml/features/validation/schema", false);
        return builderFactory;
    }

    @Beta
    public static interface Customizations {

        public EntityResolver getPreferredResolver();

    }

    /**
     * Returns a new DocumentBuilder instance based on a namespace-aware DocumentBuilderFactory that uses the
     * {@link Customizations#getPreferredResolver() configured Customizations' preferred EntityResovler}.
     *
     * @return a new DocumentBuilder instance that uses
     *     {@link Customizations#getPreferredResolver() configured Customizations' preferred EntityResovler}.
     * @throws ParserConfigurationException
     *     if there is a serious configuration error with the XML parser.
     */
    public static final DocumentBuilder getDocumentBuilder() throws ParserConfigurationException {
        DocumentBuilder builder = getNamespaceAwareDocumentBuilderFactory().newDocumentBuilder();
        EntityResolver resolver = customizations.get().getPreferredResolver();
        if (resolver != null) {
            builder.setEntityResolver(resolver);
        }
        return builder;
    }

    /**
     * Returns a Transformer created by interpreting the given InputStream as a UTF-8 XSL document.
     *
     * @param xslInput
     *     the InputStream for the UTF-8 character data representing an XSL document.
     * @param listener
     *     an optional {@link ErrorListener} that should be used to handle any errors encountered during the
     *     transformation process when using the Transformer returned by this method.
     * @return a Transformer created by interpreting the given InputStream as a UTF-8 XSL document.
     * @throws ParserConfigurationException
     *     if there is a serious configuration error with the XML parser.
     * @throws SAXException
     *     if an error is encountered during XML parsing, such as in the case of the input not representing a valid XML
     *     document.
     * @throws IOException
     *     if there is a problem reading from the InputStream.
     * @throws TransformerConfigurationException
     *     if there is a serious configuration error related to the Transformer.
     */
    public static final Transformer getTransformer(InputStream xslInput, ErrorListener listener)
        throws ParserConfigurationException, SAXException, IOException, TransformerConfigurationException {
        return getTransformer(getDocumentBuilder().parse(getInputSource(xslInput)), listener);
    }

    /**
     * Returns a Transformer created using the given File as the source the XSL document.
     *
     * @param xsl
     *     the File containing the XSL document from which the Transformer is to be created.
     * @return a Transformer created by interpreting the contents of the given File as a UTF-8 XSL document.
     * @throws ParserConfigurationException
     *     if there is a serious configuration error with the XML parser.
     * @throws SAXException
     *     if an error is encountered during XML parsing, such as in the case of the input not representing a valid XML
     *     document.
     * @throws IOException
     *     if there is a problem reading from the InputStream.
     * @throws TransformerConfigurationException
     *     if there is a serious configuration error related to the Transformer.
     */
    public static final Transformer getTransformer(File xsl)
        throws ParserConfigurationException, SAXException, IOException, TransformerConfigurationException {
        return getTransformer(xsl, null);
    }

    /**
     * Returns a Transformer created using the given File as the source the XSL document.
     *
     * @param xsl
     *     the File containing the XSL document from which the Transformer is to be created.
     * @param listener
     *     an optional {@link ErrorListener} that should be used to handle any errors encountered during the
     *     transformation process when using the Transformer returned by this method.
     * @return a Transformer created by interpreting the contents of the given File as a UTF-8 XSL document.
     * @throws ParserConfigurationException
     *     if there is a serious configuration error with the XML parser.
     * @throws SAXException
     *     if an error is encountered during XML parsing, such as in the case of the input not representing a valid XML
     *     document.
     * @throws IOException
     *     if there is a problem reading from the File.
     * @throws TransformerConfigurationException
     *     if there is a serious configuration error related to the Transformer.
     */
    public static final Transformer getTransformer(File xsl, ErrorListener listener)
        throws ParserConfigurationException, SAXException, IOException, TransformerConfigurationException {
        InputSource inputSource = getInputSource(FileUtil.getBufferedUtf8Reader(xsl));
        inputSource.setSystemId(xsl.toURI().toString());
        return getTransformer(getDocumentBuilder().parse(xsl), listener);
    }

    /**
     * Returns a Transformer created by treating the given Document as an XSL document.
     *
     * @param xslDoc
     *     the input XSL Document.
     * @param listener
     *     an optional {@link ErrorListener} that should be used to handle any errors encountered during the
     *     transformation process when using the Transformer returned by this method.
     * @return a Transformer created by treating the given Document as an XSL document.
     * @throws TransformerConfigurationException
     *     if there is a serious configuration error related to the Transformer.
     */
    public static final Transformer getTransformer(Document xslDoc, ErrorListener listener)
        throws TransformerConfigurationException {
        Transformer transformer = TransformerFactory.newInstance().newTransformer(new DOMSource(xslDoc));
        if (transformer == null) {
            throw new RuntimeException("Transformer could not be created.");
        }
        setErrorListener(transformer, listener);
        return transformer;
    }

    /**
     * Returns the default Transformer.
     *
     * @return the default Transformer.
     * @throws TransformerConfigurationException
     *     if there is a serious configuration error related to the Transformer.
     */
    public static final Transformer getTransformer() throws TransformerConfigurationException {
        return getTransformer((ErrorListener) null);
    }

    /**
     * Returns the default Transformer.
     *
     * @param listener
     *     an optional {@link ErrorListener} that should be used to handle any errors encountered during the
     *     transformation process when using the Transformer returned by this method.
     * @return the default Transformer.
     * @throws TransformerConfigurationException
     *     if there is a serious configuration error related to the Transformer.
     */
    public static final Transformer getTransformer(ErrorListener listener) throws TransformerConfigurationException {
        Transformer transformer = TransformerFactory.newInstance().newTransformer();
        if (transformer == null) {
            throw new RuntimeException("Transformer could not be created.");
        }
        if (listener != null) {
            setErrorListener(transformer, listener);
        }
        return transformer;
    }

    /**
     * Writes the given XML Document to the given Appendable.
     *
     * @param document
     *     the XML Document to be written.
     * @param out
     *     for writing the Document.
     * @throws TransformerConfigurationException
     *     if there is a serious configuration error related to the Transformer used as part of the writing process.
     * @throws TransformerException
     *     if there is a problem applying the Transformer used as part of the writing process.
     */
    public static final void writeXml(Document document, Appendable out) throws TransformerException {
        if (out instanceof Writer w) {
            writeXml(document, w);
            return;
        }
        if (out instanceof OutputStream outStream) {
            writeXml(document, outStream);
            return;
        }
        Objects.requireNonNull(document, "document");
        Objects.requireNonNull(out, "Appendable");
        writeXml(document, new AppendableWriter<>(out));
    }

    /**
     * Writes the given XML Document to the given OutputStream.
     *
     * @param document
     *     the XML Document to be written.
     * @param out
     *     the OutputStream for writing the Document.
     * @throws TransformerConfigurationException
     *     if there is a serious configuration error related to the Transformer used as part of the writing process.
     * @throws TransformerException
     *     if there is a problem applying the Transformer used as part of the writing process.
     */
    public static final void writeXml(Document document, OutputStream out)
        throws TransformerConfigurationException, TransformerException {
        Objects.requireNonNull(document, "document");
        Objects.requireNonNull(out, "output stream");
        TransformerFactory.newInstance().newTransformer().transform(new DOMSource(document), new StreamResult(out));
    }

    /**
     * Writes the given XML Document to the given Writer.
     *
     * @param document
     *     the XML Document to be written.
     * @param out
     *     the Writer for writing the Document.
     * @throws TransformerConfigurationException
     *     if there is a serious configuration error related to the Transformer used as part of the writing process.
     * @throws TransformerException
     *     if there is a problem applying the Transformer used as part of the writing process.
     */
    public static final void writeXml(Document document, Writer out) throws TransformerException {
        Objects.requireNonNull(document, "document");
        Objects.requireNonNull(out, "writer");
        TransformerFactory.newInstance().newTransformer().transform(new DOMSource(document), new StreamResult(out));
    }

    /**
     * Sets the ErrorListener on the given Transformer to on that will log stack traces to the given NamedLogWriter when
     * it is notified of an error.
     *
     * @param transformer
     *     the Transformer to be modified.
     * @param listener
     *     an optional {@link ErrorListener} that should be used to handle any errors encountered during the
     *     transformation process when using the Transformer returned by this method.
     */
    public static final void setErrorListener(Transformer transformer, ErrorListener listener) {
        if (listener != null) {
            transformer.setErrorListener(listener);
        }
    }

    /**
     * Returns a Transformer created by interpreting the contents of the given Result as a UTF-8 XSL document. The
     * Result will be consumed.
     *
     * @param result
     *     the Result whose contents are to be interpreted as a UTF-8 XSL document.
     * @param listener
     *     an optional {@link ErrorListener} that should be used to handle any errors encountered during the
     *     transformation process when using the Transformer returned by this method.
     * @return a Transformer created by interpreting the contents of the given Result as a UTF-8 XSL document.
     * @throws IOException
     *     if there is a problem reading the contents of the Result.
     * @throws ParserConfigurationException
     *     if there is a serious configuration error with the XML parser.
     * @throws SAXException
     *     if an error is encountered during XML parsing, such as in the case of the input not representing a valid XML
     *     document.
     * @throws TransformerConfigurationException
     *     if there is a serious configuration error related to the Transformer.
     */
    public static final Transformer getTransformer(Result result, ErrorListener listener)
        throws IOException, TransformerConfigurationException, SAXException, ParserConfigurationException {
        Xsl2TransformerConsumer consumer = new Xsl2TransformerConsumer(listener);
        try {
            result.consume(consumer);
        }
        catch (JaxpTransformerCreationException e) {
            e.throwOriginal();
        }
        Transformer transformer = consumer.getGenerated();
        if (listener != null) {
            transformer.setErrorListener(listener);
        }
        return transformer;
    }

    /**
     * Returns a Templates object representing a compiled version of XSL document created by interpreting the contents
     * of the given Result as a UTF-8 XSL document.
     *
     * @param result
     *     the Result whose contents are to be interpreted as a UTF-8 XSL document.
     * @return a Templates object representing a compiled version of XSL document created by interpreting the contents
     *     of the given Result as a UTF-8 XSL document.
     * @throws IOException
     *     if there is a problem reading the contents of the Result.
     * @throws ParserConfigurationException
     *     if there is a serious configuration error with the XML parser.
     * @throws SAXException
     *     if an error is encountered during XML parsing, such as in the case of the input not representing a valid XML
     *     document.
     * @throws TransformerConfigurationException
     *     if there is a serious configuration error related to the Templates object.
     */
    public static final Templates getTemplates(Result result)
        throws IOException, TransformerConfigurationException, SAXException, ParserConfigurationException {
        Xsl2TemplatesConsumer consumer = new Xsl2TemplatesConsumer();
        try {
            result.consume(consumer);
        }
        catch (JaxpTransformerCreationException e) {
            e.throwOriginal();
        }
        return consumer.getGenerated();
    }

    /**
     * Returns a Templates object representing a compiled version of XSL document created by interpreting the contents
     * of the given Result as a UTF-8 XSL document.
     *
     * @param result
     *     the Result whose contents are to be interpreted as a UTF-8 XSL document.
     * @return a Templates object representing a compiled version of XSL document created by interpreting the contents
     *     of the given Result as a UTF-8 XSL document.
     * @throws IOException
     *     if there is a problem reading the contents of the Result.
     */
    public static final Document getDocument(Result result)
        throws SAXException, IOException, ParserConfigurationException {
        Xml2DocumentConsumer consumer = new Xml2DocumentConsumer();
        try {
            result.consume(consumer);
        }
        catch (JaxpDocumentCreationException e) {
            e.throwOriginal();
        }
        return consumer.getGenerated();
    }

    /**
     * Returns a Templates object representing a compiled version of XSL document contained in the given file.
     *
     * @param xsl
     *     the File containing the XSL document from which the Templates object is to be created.
     * @return a Templates object representing a compiled version of XSL document contained in the given file.
     * @throws ParserConfigurationException
     *     if there is a serious configuration error with the XML parser.
     * @throws SAXException
     *     if an error is encountered during XML parsing, such as in the case of the input not representing a valid XML
     *     document.
     * @throws IOException
     *     if there is a problem reading from the given File.
     * @throws TransformerConfigurationException
     *     if there is a serious configuration error related to the Transformer.
     */
    public static final Templates getTemplates(File xsl)
        throws TransformerConfigurationException, IOException, SAXException, ParserConfigurationException {
        InputSource inputSource = getInputSource(FileUtil.getBufferedUtf8Reader(xsl));
        inputSource.setSystemId(xsl.toURI().toString());
        return getTemplates(getDocumentBuilder().parse(xsl));
    }

    /**
     * Returns a Templates object representing a compiled version of XSL document contained in the given file.
     *
     * @param xslInput
     *     the InputStream for the UTF-8 character data representing an XSL document.
     * @return a Templates object representing a compiled version of XSL document created by interpreting the given
     *     InputStream as a UTF-8 XSL document.
     * @throws ParserConfigurationException
     *     if there is a serious configuration error with the XML parser.
     * @throws SAXException
     *     if an error is encountered during XML parsing, such as in the case of the input not representing a valid XML
     *     document.
     * @throws IOException
     *     if there is a problem reading from the InputStream.
     * @throws TransformerConfigurationException
     *     if there is a serious configuration error related to the Transformer.
     */
    public static final Templates getTemplates(InputStream xslInput)
        throws TransformerConfigurationException, IOException, SAXException, ParserConfigurationException {
        return getTemplates(getDocumentBuilder().parse(getInputSource(xslInput)));
    }

    /**
     * Returns a Templates object created by treating the given Document as an XSL document, representing a compiled
     * version of XSL document contained in the given file.
     *
     * @param xslDoc
     *     the input XSL Document.
     * @return a Templates object created by treating the given Document as an XSL document, representing a compiled
     *     version of XSL document contained in the given file.
     * @throws TransformerConfigurationException
     *     if there is a serious configuration error related to the Transformer.
     */
    public static final Templates getTemplates(Document xslDoc) throws TransformerConfigurationException {
        return TransformerFactory.newInstance().newTemplates(new DOMSource(xslDoc));
    }

    /**
     * Returns a new {@link Transformer} from the {@link Templates} object.
     *
     * @param templates
     *     the {@link Templates} instance to use to produce the new {@link Transformer} instance.
     * @return a new {@link Transformer} from the {@link Templates} object.
     * @throws TransformerConfigurationException
     *     if there is a serious configuration error related to the {@link Transformer}.
     */
    public static final Transformer getTransformer(@Nonnull Templates templates)
        throws TransformerConfigurationException {
        return getTransformer(templates, (ErrorListener) null);
    }

    /**
     * Returns a new {@link Transformer} from the {@link Templates} object.
     *
     * @param templates
     *     the {@link Templates} instance to use to produce the new {@link Transformer} instance.
     * @param logger
     *     a {@link Logger} that should be used to log any errors encountered during the transformation process when
     *     using the {@link Transformer} returned by this method.
     * @return a new {@link Transformer} from the {@link Templates} object.
     * @throws TransformerConfigurationException
     *     if there is a serious configuration error related to the {@link Transformer}.
     */
    public static final Transformer getTransformer(@Nonnull Templates templates, @Nonnull Logger logger)
        throws TransformerConfigurationException {
        return getTransformer(templates, getLoggingErrorListener(logger));
    }

    /**
     * Returns a new {@link Transformer} from the {@link Templates} object.
     *
     * @param templates
     *     the {@link Templates} instance to use to produce the new {@link Transformer} instance.
     * @param listener
     *     an optional {@link ErrorListener} that should be used to handle any errors encountered during the
     *     transformation process when using the {@link Transformer} returned by this method.
     * @return a new {@link Transformer} from the {@link Templates} object.
     * @throws TransformerConfigurationException
     *     if there is a serious configuration error related to the {@link Transformer}.
     */
    public static final Transformer getTransformer(@Nonnull Templates templates, @Nullable ErrorListener listener)
        throws TransformerConfigurationException {
        Objects.requireNonNull(templates, "templates");
        Transformer transformer = templates.newTransformer();
        if (listener != null) {
            setErrorListener(transformer, listener);
        }
        return transformer;
    }

    public static final GetPutProperty getAttributesAsGetPutProperty(Element elem) {
        return MapPropertyStore.createNamedInstance(elem.getNodeName(), getAttributesAsMap(elem)).toReadWrite();
    }

    public static final Map<String,String> getAttributesAsMap(Element elem) {
        Map<String,String> map = new HashMap<>();
        NamedNodeMap attributes = elem.getAttributes();
        int len = attributes.getLength();
        for (int i = 0; i < len; i++) {
            Node attr = attributes.item(i);
            map.put(attr.getNodeName(), attr.getNodeValue());
        }
        return map;
    }

    public static final void safelyTraverseDescendants(Node root, java.util.function.Consumer<? super Node> visitor) {
        Queue<Node> queue = new LinkedList<>();
        addChildren(root, queue);
        Node next = queue.poll();
        while (next != null) {
            visitor.accept(next);
            addChildren(next, queue);
            next = queue.poll();
        }
    }

    private static final void addChildren(Node node, Collection<Node> nodes) {
        NodeList children = node.getChildNodes();
        int len = children.getLength();
        for (int i = 0; i < len; i++) {
            nodes.add(children.item(i));
        }
    }

}
