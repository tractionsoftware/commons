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

package com.tractionsoftware.commons.io;

import com.google.common.annotations.Beta;
import com.tractionsoftware.commons.util.DefaultFormatterProvider;
import com.tractionsoftware.commons.util.FormatterProvider;
import org.apache.commons.io.function.IORunnable;

import java.io.*;
import java.util.Formatter;
import java.util.Locale;

/**
 * A skeleton class for a customized {@link PrintWriter}. This is necessary at least in part because some of
 * PrintWriter's internals that would be useful to access to modify its default behaviors are private.
 *
 * @author Dave Shepperton
 */
@Beta
public abstract class AbstractCustomPrintWriter extends PrintWriter {

    private FormatterProvider formatterProvider;

    protected final boolean autoFlush;

    public AbstractCustomPrintWriter(Writer out, boolean autoFlush) {
        super(out, autoFlush);
        this.autoFlush = autoFlush;
    }

    public AbstractCustomPrintWriter(OutputStream out, boolean autoFlush) {
        super(out, autoFlush);
        this.autoFlush = autoFlush;
    }

    @Override
    public void flush() {
        try {
            checkOpenX();
            out.flush();
        }
        catch (IOException e) {
            setError();
        }
    }

    @Override
    public void close() {
        if (out != null) {
            try {
                out.close();
                out = null;
            }
            catch (IOException e) {
                setError();
            }
        }
    }

    /**
     * Flushes if auto flush is enabled.
     */
    protected final void autoFlush() {
        if (autoFlush) {
            flush();
        }
    }

    /**
     * Checks whether this AbstractCustomPrintWriter is still open, {@link #setError() setting the error condition} if
     * not
     *
     * @return true if this AbstractCustomPrintWriter is still open; false otherwise.
     */
    protected final boolean checkOpenQ() {
        if (out == null) {
            setError();
            return false;
        }
        return true;
    }

    /**
     * Checks whether this AbstractCustomPrintWriter is still open.
     *
     * @throws IOException
     *     if this AbstractCustomPrintWriter is no longer open.
     */
    protected final void checkOpenX() throws IOException {
        if (out == null) {
            throw new IOException("Stream closed");
        }
    }

    protected final Formatter getDefaultFormatter() {
        return formatterProvider().getDefault();
    }

    protected final Formatter getFormatter(Locale l) {
        return formatterProvider().get(l);
    }

    protected final void doOperation(IORunnable operation) {
        try {
            checkOpenX();
            operation.run();
        }
        catch (InterruptedIOException e) {
            Thread.currentThread().interrupt();
        }
        catch (IOException e) {
            setError();
        }
    }

    private final FormatterProvider formatterProvider() {
        if (formatterProvider == null) {
            formatterProvider = DefaultFormatterProvider.getInstance(this);
        }
        return formatterProvider;
    }

}
