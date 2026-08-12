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
import com.tractionsoftware.commons.config.ConfiguredObject;
import com.tractionsoftware.commons.text.TextTransformationException;
import com.tractionsoftware.commons.text.TextTransformer;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import java.io.*;
import java.util.Objects;

/**
 * A {@link TextTransformer} that uses JAXP to execute an XSLT transformation based upon an XSL document. The input text
 * for its transform methods must constitute a valid XML document.
 *
 * @author Dave Shepperton
 */
public abstract class JaxpXsltTextTransformer implements TextTransformer, ConfiguredObject {

    protected static final Logger LOGGER = LoggerFactory.getLogger(JaxpXsltTextTransformer.class);

    protected static Document createDocument(@Nonnull Reader in) throws IOException, TextTransformationException {
        Objects.requireNonNull(in, "input");
        try {
            return JaxpUtil.getDocument(in);
        }
        catch (SAXException | ParserConfigurationException | RuntimeException e) {
            throw new TextTransformationException("Failed to create an XML document from the input text", e);
        }
    }

    protected final Configuration config;

    public JaxpXsltTextTransformer(Configuration config) {
        Objects.requireNonNull(config, "configuration");
        this.config = config;
    }

    @Override
    public final void transform(@Nullable CharSequence text, @Nonnull Appendable out)
        throws IOException, TextTransformationException {
        if (text != null) {
            transform(createDocument(new StringReader(text.toString())), out);
        }
    }

    @Override
    public final void transform(@Nonnull Reader in, @Nonnull Writer out)
        throws IOException, TextTransformationException {
        transform(createDocument(in), out);
    }

    @Nonnull
    @Override
    public final Configuration getConfiguration() {
        return config;
    }

    protected final void transform(@Nonnull Document inputDoc, @Nonnull Appendable out)
        throws IOException, TextTransformationException {
        Document transformedDoc;
        try {
            transformedDoc = JaxpUtil.getTransformedDocument(inputDoc, getJaxpTransformer());
        }
        catch (TransformerException | ParserConfigurationException e) {
            throw new TextTransformationException("Failed to create the transformed XML document", e);
        }
        try {
            JaxpUtil.writeXml(transformedDoc, out);
        }
        catch (TransformerException e) {
            throw new TextTransformationException("Failed to write the transformed XML document", e);
        }
    }

    /**
     * Returns the {@link Transformer} to use for the XSLT. Since the Transformer class's documentation indicates that
     * Transformer instances "may not be used in multiple threads running concurrently," for the sake of safety,
     * implementations should either return a new object for each invocation of this method, or otherwise ensure that
     * only allow one client can actually use it at a time (if multi-threaded performance is not a concern).
     *
     * @return the {@link Transformer} to use for the XSLT.
     */
    @Nonnull
    protected abstract Transformer getJaxpTransformer() throws IOException;

}
