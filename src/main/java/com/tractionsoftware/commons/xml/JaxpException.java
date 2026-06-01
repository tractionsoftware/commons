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
 * A super-type for Exceptions that wrap various Exception types produced by JAXP.
 *
 * @author Dave Shepperton
 */
public abstract class JaxpException extends Exception {

    @Serial
    private static final long serialVersionUID = 206264751349671854L;

    /**
     * An abstraction for an object which either re-throws or wraps Throwables raised during an attempt to perform a
     * JAXP-related operation.
     *
     * @param <X>
     *     a special type of Exception which will be produced by the {@link #handle(Throwable)} method when applicable.
     */
    protected static abstract class Handler<X extends Exception> {

        /**
         * Handles a Throwable either by re-throwing it or by wrapping it in an appropriate type of Exception and
         * throwing that.
         *
         * @param cause
         *     the Throwable to be handled.
         * @throws RuntimeException
         *     if the given Throwable is a RuntimeException (it will be re-thrown).
         * @throws Error
         *     if the given Throwable is an Error (it will be re-thrown).
         * @throws IOException
         *     if the given Throwable is an IOException (it will be re-thrown).
         * @throws X
         *     if the given Throwable is another type of Exception which this Handler prefers to wrap with a special
         *     type of Exception.
         * @throws RuntimeException
         *     wrapping the given Throwable in any other case.
         */
        public final void handle(Throwable cause)
            throws RuntimeException, Error, IOException, X {
            handleStandard(cause);
            handleCustom(cause);
            handleFallback(cause);
        }

        private final void handleStandard(Throwable cause) throws RuntimeException, Error, IOException {
            if (cause instanceof RuntimeException) {
                throw (RuntimeException) cause;
            }
            if (cause instanceof Error) {
                throw (Error) cause;
            }
            if (cause instanceof IOException) {
                throw (IOException) cause;
            }
        }

        /**
         * This method is intended to throw a special type of Exception to wrap the given Throwable when appropriate.
         *
         * @param cause
         *     the Throwable to be wrapped.
         * @throws X
         *     the special type of Exception produced by this Handler.
         */
        protected abstract void handleCustom(Throwable cause) throws X;

        private final void handleFallback(Throwable cause) throws RuntimeException {
            throw new RuntimeException(cause);
        }

    }

    protected JaxpException(SAXException cause) {
        super(cause);
    }

    protected JaxpException(ParserConfigurationException cause) {
        super(cause);
    }

    protected JaxpException(TransformerConfigurationException cause) {
        super(cause);
    }

}
