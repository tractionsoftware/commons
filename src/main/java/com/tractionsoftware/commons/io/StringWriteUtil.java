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

import com.google.common.base.CharMatcher;
import com.google.common.collect.ImmutableList;
import com.tractionsoftware.commons.lang.EnhancedCharSequence;
import com.tractionsoftware.commons.lang.ObjectUtil;
import com.tractionsoftware.commons.lang.StringUtil;
import org.apache.commons.io.output.TeeWriter;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.function.Consumer;

public final class StringWriteUtil {

    private static final Logger LOGGER = LoggerFactory.getLogger(StringWriteUtil.class.getName());

    private StringWriteUtil() {
    }

    private static final class CodePointChars {

        private final char[] chars;

        private final int end;

        private CodePointChars(char[] chars, int end) {
            this.chars = chars;
            this.end = end;
        }

        public static CodePointChars createInstance(int[] codePoints, int start, int end) {
            char[] chars = new char[2 * (end - start)];
            int nextIndex = 0;
            for (int i = start; i < end; i++) {
                char[] toChars = Character.toChars(codePoints[i]);
                int size = toChars.length;
                System.arraycopy(toChars, 0, chars, nextIndex, size);
                nextIndex += size;
            }
            return new CodePointChars(chars, nextIndex);
        }

        public void append(Appendable out) throws IOException {
            out.append(EnhancedCharSequence.getInstance(chars, 0, end));
        }

        public void write(PrintWriter out) {
            out.write(chars, 0, end);
        }

    }

    public static void safeAppend(StringBuilder buffer, Object o) {
        if (buffer == null || o == null) {
            return;
        }
        buffer.append(o);
    }

    public static void safeAppend(Appendable out, CharSequence str) {
        if (out == null || StringUtils.isEmpty(str)) {
            return;
        }
        try {
            out.append(str);
        }
        catch (IOException e) {
            LOGGER.warn("append failed", e);
        }
    }

    public static void safeAppend(Appendable out, CharSequence str, int start, int end) {
        if (out == null || StringUtils.isEmpty(str)) {
            return;
        }
        try {
            out.append(str, start, end);
        }
        catch (IOException e) {
            LOGGER.warn("append failed", e);
        }
    }

    public static void safeAppend(Appendable buffer, char c) {
        if (buffer == null) {
            return;
        }
        try {
            buffer.append(c);
        }
        catch (IOException e) {
            LOGGER.warn("append failed", e);
        }
    }

    /**
     * This utility method implements the commonly required retrieval of a String representing the output written to a
     * PrintWriter.
     *
     * @param callback
     *     allows the client to supply the {@link PrintWriter} to whatever code will be using it to write output.
     * @return the String representing the output written to the {@link PrintWriter} that is provided to the callback's
     *     {@link Consumer#accept(Object)} method.
     */
    public static String getPrintedString(Consumer<PrintWriter> callback) {
        StringWriter sw = new StringWriter();
        PrintWriter out = SingleThreadPrintWriter.createInstance(sw);
        callback.accept(out);
        out.flush();
        return sw.toString();
    }

    public static String getString(Consumer<StringBuilder> callback) {
        StringBuilder buff = new StringBuilder();
        callback.accept(buff);
        return buff.toString();
    }

    /**
     * This version does the same thing as {@link StringWriteUtil#getPrintedString(Consumer)}, but throws an Exception
     * of the given type if any RuntimeException caught as a result of invoking {@link Consumer#accept(Object)} on the
     * given {@link Consumer} is found to wrap an Exception of that type.
     *
     * @param callback
     *     allows the client to supply the {@link PrintWriter} to whatever code will be using it to write output.
     * @param exceptionType
     *     the type of Exception that will be thrown if {@link Consumer#accept(Object)} throws a RuntimeException that
     *     wraps an assignment-compatible type.
     * @return the String representing the output written to the {@link PrintWriter} that is provided to the callback's
     *     {@link Consumer#accept(Object)} method.
     * @throws X
     *     if one is wrapped by a RuntimeException thrown by {@link Consumer#accept(Object)}.
     */
    public static <X extends Exception> String getPrintedString(Consumer<PrintWriter> callback, Class<X> exceptionType)
        throws X {
        try {
            return getPrintedString(callback);
        }
        catch (RuntimeException e) {
            X eligible = ObjectUtil.castIfAssignmentCompatible(e.getCause(), exceptionType);
            if (eligible != null) {
                throw eligible;
            }
            throw e;
        }
    }

    /**
     * This version does the same thing as {@link StringWriteUtil#getPrintedString(Consumer)}, but never allows
     * Exceptions to propagate, and returns either the successfully generated String, or as much as was generated before
     * any Exception prevented the operation from completing.
     *
     * @param callback
     *     allows the client to supply the {@link PrintWriter} to whatever code will be using it to write output.
     * @return the String representing the output written to the {@link PrintWriter} that is provided to the callback's
     *     {@link Consumer#accept(Object)} method.
     */
    public static String getFullOrPartialPrintedString(Consumer<PrintWriter> callback) {
        StringWriter sw = new StringWriter();
        PrintWriter out = SingleThreadPrintWriter.createInstance(sw);
        try {
            callback.accept(out);
        }
        catch (RuntimeException e) {
            LOGGER.warn("Error caught while generating printed string via {}", callback, e);
        }
        out.flush();
        return sw.toString();
    }

    public static String getPrintedResultAndTee(Consumer<PrintWriter> callback, Writer... also) {
        StringWriter sw = new StringWriter();
        ImmutableList.Builder<Writer> writers = ImmutableList.builder();
        writers.add(sw);
        writers.add(also);
        PrintWriter out = SingleThreadPrintWriter.createInstance(new TeeWriter(writers.build()));
        callback.accept(out);
        out.flush();
        return sw.toString();
    }

    public static void appendCodePoints(StringBuilder buff, int[] codePoints, int start, int end) {
        for (int i = start; i < end; i++) {
            buff.appendCodePoint(codePoints[i]);
        }
    }

    public static void appendCodePoints(PrintWriter out, int[] codePoints, int start, int end) {
        CodePointChars.createInstance(codePoints, start, end).write(out);
    }

    public static void appendCodePoints(Appendable out, int[] codePoints, int start, int end) throws IOException {
        if (out instanceof StringBuilder buff) {
            appendCodePoints(buff, codePoints, start, end);
            return;
        }
        if (out instanceof PrintWriter pw) {
            appendCodePoints(pw, codePoints, start, end);
            return;
        }
        CodePointChars.createInstance(codePoints, start, end).append(out);
    }

    public static void appendTrimmed(StringBuilder buff, CharSequence str) {
        appendMatching(buff, str, CharMatcher.whitespace().negate());
    }

    public static void printTrimmed(PrintWriter out, CharSequence str) {
        printMatching(out, str, CharMatcher.whitespace().negate());
    }

    public static void appendTrimmed(Appendable out, CharSequence str) throws IOException {
        appendMatching(out, str, CharMatcher.whitespace().negate());
    }

    public static void appendMatching(StringBuilder buff, CharSequence str, CharMatcher matcher) {
        StringUtil.getMatchingRange(str, matcher).append(buff, str);
    }

    public static void printMatching(PrintWriter out, CharSequence str, CharMatcher matcher) {
        StringUtil.getMatchingRange(str, matcher).print(out, str);
    }

    public static void appendMatching(Appendable out, CharSequence str, CharMatcher matcher) throws IOException {
        StringUtil.getMatchingRange(str, matcher).append(out, str);
    }

    public static boolean appendTo(Object appendTo, String appendValue, Consumer<Object> onUpdate) throws IOException {
        if (appendTo instanceof Appendable out) {
            out.append(appendValue);
            return true;
        }
        if (appendTo instanceof CharSequence sequence) {
            onUpdate.accept(sequence + appendValue);
            return true;
        }
        return false;
    }

    public static boolean appendToSafe(Object appendTo, String appendValue, Consumer<Object> onUpdate) {
        try {
            return appendTo(appendTo, appendValue, onUpdate);
        }
        catch (IOException | RuntimeException e) {
            LOGGER.warn(
                "Failed to append {} to {}",
                StringUtil.truncatedToStringForLog(appendValue),
                ObjectUtil.safeToStringObject(appendTo),
                e
            );
        }
        return false;
    }

    public static final void appendUtf8Bytes(Appendable out, byte[] b, int offset, int length) {
        safeAppend(out, new String(b, offset, length, StandardCharsets.UTF_8));
    }

    public static final void appendUtf16Bytes(Appendable out, byte[] b) {
        safeAppend(out, new String(b, StandardCharsets.UTF_16));
    }

}
