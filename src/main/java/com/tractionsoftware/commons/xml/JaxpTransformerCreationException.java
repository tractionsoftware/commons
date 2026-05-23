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

import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerConfigurationException;
import java.io.IOException;
import java.io.Serial;

/**
 * This {@link JaxpException} class wraps instances of the TransformerConfigurationException, SAXException and
 * ParserConfigurationException classes, which are produced by various methods involved in creating a Transformer or
 * Templates instance.
 *
 * @author Dave Shepperton
 * @since 4.0
 */
public final class JaxpTransformerCreationException extends JaxpException {

    @Serial
    private static final long serialVersionUID = 942519506435859852L;

    private static final Handler<JaxpTransformerCreationException> HANDLER =
        new Handler<>() {
            @Override
            protected final void handleCustom(Throwable cause) throws JaxpTransformerCreationException {
                if (cause instanceof TransformerConfigurationException) {
                    throw new JaxpTransformerCreationException((TransformerConfigurationException) cause);
                }
                if (cause instanceof SAXException) {
                    throw new JaxpTransformerCreationException((SAXException) cause);
                }
                if (cause instanceof ParserConfigurationException) {
                    throw new JaxpTransformerCreationException((ParserConfigurationException) cause);
                }
            }
        };

    /**
     * Throws an appropriate Exception or Error based on the given cause.
     *
     * @param cause
     *     the Throwable to handle.
     * @throws Error
     *     if the cause is an Error.
     * @throws RuntimeException
     *     if the cause is a RuntimeException.
     * @throws IOException
     *     if the cause is an IOException.
     * @throws JaxpTransformerCreationException
     *     if the cause is a TransformerConfigurationException, SAXException or ParserConfigurationException.
     * @throws RuntimeException
     *     if the cause is any other kind of Throwable or Exception.
     */
    public static final void handle(Throwable cause)
        throws IOException, JaxpTransformerCreationException {
        HANDLER.handle(cause);
    }

    private JaxpTransformerCreationException(TransformerConfigurationException cause) {
        super(cause);
    }

    private JaxpTransformerCreationException(SAXException cause) {
        super(cause);
    }

    private JaxpTransformerCreationException(ParserConfigurationException cause) {
        super(cause);
    }

    /**
     * Throws the original wrapped Exception.
     *
     * @throws TransformerConfigurationException
     *     if the original Exception was a TransformerConfigurationException.
     * @throws SAXException
     *     if the original Exception was a SAXException.
     * @throws ParserConfigurationException
     *     if the original Exception was a ParserConfigurationException.
     */
    public final void throwOriginal()
        throws TransformerConfigurationException, SAXException, ParserConfigurationException {
        Throwable cause = getCause();
        if (cause instanceof TransformerConfigurationException) {
            throw (TransformerConfigurationException) cause;
        }
        if (cause instanceof SAXException) {
            throw (SAXException) cause;
        }
        if (cause instanceof ParserConfigurationException) {
            throw (ParserConfigurationException) cause;
        }
    }

}
