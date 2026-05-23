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
import org.apache.commons.lang3.ObjectUtils;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Locale;
import java.util.Objects;

/**
 * A {@link PrintWriter} that dispenses with the thread-safe mechanisms used by the default PrintWriter implementation
 * methods. This can be useful in cases in which thread-safety is not required, either because a {@link Writer} or
 * {@link OutputStream} being wrapped already implements it, or all I/O operations for the PrintWriter are definitely
 * going to be confined to a single execution thread.
 *
 * @author Dave Shepperton
 */
@Beta
public final class SingleThreadPrintWriter extends AbstractCustomPrintWriter {

    public static SingleThreadPrintWriter createInstance(Writer out) {
        return createInstance(out, false);
    }

    public static SingleThreadPrintWriter createInstance(Writer out, boolean autoFlush) {
        Objects.requireNonNull(out, "Writer");
        if (out instanceof PrintWriter) {
            throw new IllegalArgumentException("Wrapping an existing PrintWriter.");
        }
        return new SingleThreadPrintWriter(out, autoFlush);
    }

    public static SingleThreadPrintWriter createUtf8Instance(OutputStream out) {
        return createInstance(out, null);
    }

    public static SingleThreadPrintWriter createInstance(OutputStream out, Charset charset) {
        return createInstance(out, charset, false);
    }

    public static SingleThreadPrintWriter createInstance(OutputStream out, Charset charset, boolean autoFlush) {
        Objects.requireNonNull(out, "OutputStream");
        return createInstance(
            new OutputStreamWriter(IOUtil.getBufferedOutputStream(out), charset),
            autoFlush
        );
    }

    public static SingleThreadPrintWriter createUtf8Instance(File file) throws IOException {
        return createInstance(file, null);
    }

    public static SingleThreadPrintWriter createInstance(File file, Charset charset) throws IOException {
        return createInstance(file, charset, false);
    }

    public static SingleThreadPrintWriter createInstance(File file, Charset charset, boolean autoFlush)
        throws IOException {
        Objects.requireNonNull(file, "File");
        return createInstance(
            Files.newBufferedWriter(file.toPath(), ObjectUtils.getIfNull(charset, StandardCharsets.UTF_8)),
            autoFlush
        );
    }

    private SingleThreadPrintWriter(Writer out, boolean autoFlush) {
        super(out, autoFlush);
    }

    @Override
    public void write(int c) {
        try {
            checkOpenX();
            out.write(c);
        }
        catch (InterruptedIOException e) {
            Thread.currentThread().interrupt();
        }
        catch (IOException e) {
            setError();
        }
    }

    @Override
    public void write(char[] buf, int off, int len) {
        try {
            checkOpenX();
            out.write(buf, off, len);
        }
        catch (InterruptedIOException e) {
            Thread.currentThread().interrupt();
        }
        catch (IOException e) {
            setError();
        }
    }

    @Override
    public void write(String s, int off, int len) {
        try {
            checkOpenX();
            out.write(s, off, len);
        }
        catch (InterruptedIOException e) {
            Thread.currentThread().interrupt();
        }
        catch (IOException e) {
            setError();
        }
    }

    @Override
    public void println() {
        try {
            checkOpenX();
            printlnImpl();
        }
        catch (InterruptedIOException e) {
            Thread.currentThread().interrupt();
        }
        catch (IOException e) {
            setError();
        }
    }

    @Override
    public void println(boolean x) {
        printlnOther(String.valueOf(x));
    }

    @Override
    public void println(char x) {
        printlnOther(String.valueOf(x));
    }

    @Override
    public void println(int x) {
        printlnOther(String.valueOf(x));
    }

    @Override
    public void println(long x) {
        printlnOther(String.valueOf(x));
    }

    @Override
    public void println(float x) {
        printlnOther(String.valueOf(x));
    }

    @Override
    public void println(double x) {
        printlnOther(String.valueOf(x));
    }

    @Override
    public void println(char[] x) {
        print(x);
        println();
    }

    @Override
    public void println(String x) {
        print(x);
        println();
    }

    @Override
    public void println(Object x) {
        String s = String.valueOf(x);
        print(s);
        println();
    }

    @Override
    public SingleThreadPrintWriter printf(String format, Object... args) {
        return format(format, args);
    }

    @Override
    public SingleThreadPrintWriter printf(Locale l, String format, Object... args) {
        return format(l, format, args);
    }

    @Override
    public SingleThreadPrintWriter format(String format, Object... args) {
        try {
            checkOpenX();
            getDefaultFormatter().format(Locale.getDefault(), format, args);
            autoFlush();
        }
        catch (InterruptedIOException e) {
            Thread.currentThread().interrupt();
        }
        catch (IOException e) {
            setError();
        }
        return this;
    }

    @Override
    public SingleThreadPrintWriter format(Locale l, String format, Object... args) {
        try {
            checkOpenX();
            getFormatter(l).format(l, format, args);
            autoFlush();
        }
        catch (InterruptedIOException e) {
            Thread.currentThread().interrupt();
        }
        catch (IOException e) {
            setError();
        }
        return this;
    }

    @Override
    public SingleThreadPrintWriter append(CharSequence csq) {
        write(String.valueOf(csq));
        return this;
    }

    @Override
    public SingleThreadPrintWriter append(CharSequence csq, int start, int end) {
        if (csq == null) csq = "null";
        return append(csq.subSequence(start, end));
    }

    @Override
    public SingleThreadPrintWriter append(char c) {
        write(c);
        return this;
    }

    private void printlnImpl() throws IOException {
        write(System.lineSeparator());
        autoFlush();
    }

    private void printlnOther(String s) {
        print(s + System.lineSeparator());
        autoFlush();
    }

}
