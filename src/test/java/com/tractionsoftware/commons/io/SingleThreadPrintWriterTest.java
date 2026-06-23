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

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.*;

class SingleThreadPrintWriterTest {

    /**
     * A Writer that records flush() invocations, to characterize autoFlush behavior.
     */
    private static class FlushCountingWriter extends Writer {

        private final StringWriter delegate = new StringWriter();

        int flushCount = 0;

        @Override
        public void write(char[] cbuf, int off, int len) {
            delegate.write(cbuf, off, len);
        }

        @Override
        public void flush() {
            flushCount++;
        }

        @Override
        public void close() {
        }

        String getContents() {
            return delegate.toString();
        }

    }

    /**
     * A Writer whose write(char[], int, int) always throws InterruptedIOException, to characterize how
     * SingleThreadPrintWriter handles interruption during I/O.
     */
    private static class InterruptingWriter extends Writer {

        @Override
        public void write(char[] cbuf, int off, int len) throws IOException {
            throw new InterruptedIOException("simulated interruption");
        }

        @Override
        public void flush() {
        }

        @Override
        public void close() {
        }

    }

    // ---------------------------------------------------------------------------
    // createInstance(Writer[, autoFlush])
    // ---------------------------------------------------------------------------

    @Test
    void createInstance_nullWriter_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> SingleThreadPrintWriter.createInstance((Writer) null));
    }

    @Test
    void createInstance_wrappingExistingPrintWriter_throwsIllegalArgumentException() {
        PrintWriter existing = new PrintWriter(new StringWriter());
        assertThrows(IllegalArgumentException.class, () -> SingleThreadPrintWriter.createInstance(existing));
    }

    @Test
    void createInstance_validWriter_writesContent() {
        StringWriter sw = new StringWriter();
        SingleThreadPrintWriter writer = SingleThreadPrintWriter.createInstance(sw);
        writer.print("hello");
        assertEquals("hello", sw.toString());
    }

    // ---------------------------------------------------------------------------
    // createInstance(OutputStream, Charset[, autoFlush]) / createUtf8Instance(OutputStream)
    // ---------------------------------------------------------------------------

    @Test
    void createInstance_outputStream_nullOutputStream_throwsNullPointerException() {
        assertThrows(
            NullPointerException.class,
            () -> SingleThreadPrintWriter.createInstance((java.io.OutputStream) null, StandardCharsets.UTF_8)
        );
    }

    @Test
    void createInstance_outputStream_nullCharset_throwsNullPointerException() {
        assertThrows(
            NullPointerException.class,
            () -> SingleThreadPrintWriter.createInstance(new ByteArrayOutputStream(), null)
        );
    }

    @Test
    void createUtf8Instance_outputStream_writesUtf8Content() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        SingleThreadPrintWriter writer = SingleThreadPrintWriter.createUtf8Instance(baos);
        writer.print("café");
        writer.flush();
        assertEquals("café", baos.toString(StandardCharsets.UTF_8));
    }

    // ---------------------------------------------------------------------------
    // createInstance(File, Charset[, autoFlush]) / createUtf8Instance(File)
    // ---------------------------------------------------------------------------

    @Test
    void createInstance_file_nullFile_throwsNullPointerException() {
        assertThrows(
            NullPointerException.class,
            () -> SingleThreadPrintWriter.createInstance((File) null, StandardCharsets.UTF_8)
        );
    }

    @Test
    void createInstance_file_nullCharset_defaultsToUtf8(@TempDir Path tempDir) throws IOException {
        File file = tempDir.resolve("output.txt").toFile();
        SingleThreadPrintWriter writer = SingleThreadPrintWriter.createInstance(file, null);
        try {
            writer.print("café");
        }
        finally {
            writer.close();
        }
        assertEquals("café", Files.readString(file.toPath(), StandardCharsets.UTF_8));
    }

    @Test
    void createUtf8Instance_file_writesContent(@TempDir Path tempDir) throws IOException {
        File file = tempDir.resolve("utf8output.txt").toFile();
        SingleThreadPrintWriter writer = SingleThreadPrintWriter.createUtf8Instance(file);
        try {
            writer.print("hello file");
        }
        finally {
            writer.close();
        }
        assertEquals("hello file", Files.readString(file.toPath(), StandardCharsets.UTF_8));
    }

    // ---------------------------------------------------------------------------
    // write(int) / write(char[], int, int) / write(String, int, int)
    // ---------------------------------------------------------------------------

    @Test
    void write_intChar_writesSingleCharacter() {
        StringWriter sw = new StringWriter();
        SingleThreadPrintWriter writer = SingleThreadPrintWriter.createInstance(sw);
        writer.write('Q');
        assertEquals("Q", sw.toString());
    }

    @Test
    void write_charArrayRange_writesSubsequence() {
        StringWriter sw = new StringWriter();
        SingleThreadPrintWriter writer = SingleThreadPrintWriter.createInstance(sw);
        writer.write("abcdef".toCharArray(), 1, 3);
        assertEquals("bcd", sw.toString());
    }

    @Test
    void write_stringRange_writesSubsequence() {
        StringWriter sw = new StringWriter();
        SingleThreadPrintWriter writer = SingleThreadPrintWriter.createInstance(sw);
        writer.write("abcdef", 2, 3);
        assertEquals("cde", sw.toString());
    }

    // ---------------------------------------------------------------------------
    // println(...) overloads
    // ---------------------------------------------------------------------------

    @Test
    void println_noArg_writesLineSeparatorOnly() {
        StringWriter sw = new StringWriter();
        SingleThreadPrintWriter writer = SingleThreadPrintWriter.createInstance(sw);
        writer.println();
        assertEquals(System.lineSeparator(), sw.toString());
    }

    @Test
    void println_boolean_writesValueAndLineSeparator() {
        StringWriter sw = new StringWriter();
        SingleThreadPrintWriter writer = SingleThreadPrintWriter.createInstance(sw);
        writer.println(true);
        assertEquals("true" + System.lineSeparator(), sw.toString());
    }

    @Test
    void println_char_writesValueAndLineSeparator() {
        StringWriter sw = new StringWriter();
        SingleThreadPrintWriter writer = SingleThreadPrintWriter.createInstance(sw);
        writer.println('z');
        assertEquals("z" + System.lineSeparator(), sw.toString());
    }

    @Test
    void println_int_writesValueAndLineSeparator() {
        StringWriter sw = new StringWriter();
        SingleThreadPrintWriter writer = SingleThreadPrintWriter.createInstance(sw);
        writer.println(42);
        assertEquals("42" + System.lineSeparator(), sw.toString());
    }

    @Test
    void println_long_writesValueAndLineSeparator() {
        StringWriter sw = new StringWriter();
        SingleThreadPrintWriter writer = SingleThreadPrintWriter.createInstance(sw);
        writer.println(42L);
        assertEquals("42" + System.lineSeparator(), sw.toString());
    }

    @Test
    void println_float_writesValueAndLineSeparator() {
        StringWriter sw = new StringWriter();
        SingleThreadPrintWriter writer = SingleThreadPrintWriter.createInstance(sw);
        writer.println(1.5f);
        assertEquals("1.5" + System.lineSeparator(), sw.toString());
    }

    @Test
    void println_double_writesValueAndLineSeparator() {
        StringWriter sw = new StringWriter();
        SingleThreadPrintWriter writer = SingleThreadPrintWriter.createInstance(sw);
        writer.println(1.5d);
        assertEquals("1.5" + System.lineSeparator(), sw.toString());
    }

    @Test
    void println_charArray_writesValueAndLineSeparator() {
        StringWriter sw = new StringWriter();
        SingleThreadPrintWriter writer = SingleThreadPrintWriter.createInstance(sw);
        writer.println("abc".toCharArray());
        assertEquals("abc" + System.lineSeparator(), sw.toString());
    }

    @Test
    void println_string_writesValueAndLineSeparator() {
        StringWriter sw = new StringWriter();
        SingleThreadPrintWriter writer = SingleThreadPrintWriter.createInstance(sw);
        writer.println("hello");
        assertEquals("hello" + System.lineSeparator(), sw.toString());
    }

    @Test
    void println_object_writesValueAndLineSeparator() {
        StringWriter sw = new StringWriter();
        SingleThreadPrintWriter writer = SingleThreadPrintWriter.createInstance(sw);
        writer.println(Integer.valueOf(7));
        assertEquals("7" + System.lineSeparator(), sw.toString());
    }

    @Test
    void println_nullObject_writesNullStringAndLineSeparator() {
        StringWriter sw = new StringWriter();
        SingleThreadPrintWriter writer = SingleThreadPrintWriter.createInstance(sw);
        writer.println((Object) null);
        assertEquals("null" + System.lineSeparator(), sw.toString());
    }

    // ---------------------------------------------------------------------------
    // printf / format
    // ---------------------------------------------------------------------------

    @Test
    void printf_stringFormat_writesFormattedOutput() {
        StringWriter sw = new StringWriter();
        SingleThreadPrintWriter writer = SingleThreadPrintWriter.createInstance(sw);
        SingleThreadPrintWriter result = writer.printf("%d-%s", 3, "x");
        assertSame(writer, result);
        assertEquals("3-x", sw.toString());
    }

    @Test
    void printf_withLocale_writesFormattedOutput() {
        StringWriter sw = new StringWriter();
        SingleThreadPrintWriter writer = SingleThreadPrintWriter.createInstance(sw);
        SingleThreadPrintWriter result = writer.printf(Locale.US, "%d-%s", 3, "x");
        assertSame(writer, result);
        assertEquals("3-x", sw.toString());
    }

    @Test
    void format_stringFormat_returnsThisAndWritesFormattedOutput() {
        StringWriter sw = new StringWriter();
        SingleThreadPrintWriter writer = SingleThreadPrintWriter.createInstance(sw);
        SingleThreadPrintWriter result = writer.format("[%s]", "v");
        assertSame(writer, result);
        assertEquals("[v]", sw.toString());
    }

    @Test
    void format_withLocale_returnsThisAndWritesFormattedOutput() {
        StringWriter sw = new StringWriter();
        SingleThreadPrintWriter writer = SingleThreadPrintWriter.createInstance(sw);
        SingleThreadPrintWriter result = writer.format(Locale.US, "[%s]", "v");
        assertSame(writer, result);
        assertEquals("[v]", sw.toString());
    }

    // ---------------------------------------------------------------------------
    // append(...)
    // ---------------------------------------------------------------------------

    @Test
    void append_charSequence_writesContentAndReturnsThis() {
        StringWriter sw = new StringWriter();
        SingleThreadPrintWriter writer = SingleThreadPrintWriter.createInstance(sw);
        SingleThreadPrintWriter result = writer.append("abc");
        assertSame(writer, result);
        assertEquals("abc", sw.toString());
    }

    @Test
    void append_charSequenceRange_writesSubsequence() {
        StringWriter sw = new StringWriter();
        SingleThreadPrintWriter writer = SingleThreadPrintWriter.createInstance(sw);
        writer.append("abcdef", 1, 4);
        assertEquals("bcd", sw.toString());
    }

    @Test
    void append_nullCharSequenceRange_writesSubsequenceOfNullLiteral() {
        StringWriter sw = new StringWriter();
        SingleThreadPrintWriter writer = SingleThreadPrintWriter.createInstance(sw);
        writer.append((CharSequence) null, 0, 2);
        assertEquals("nu", sw.toString());
    }

    @Test
    void append_char_writesSingleCharAndReturnsThis() {
        StringWriter sw = new StringWriter();
        SingleThreadPrintWriter writer = SingleThreadPrintWriter.createInstance(sw);
        SingleThreadPrintWriter result = writer.append('Z');
        assertSame(writer, result);
        assertEquals("Z", sw.toString());
    }

    // ---------------------------------------------------------------------------
    // autoFlush behavior
    // ---------------------------------------------------------------------------

    @Test
    void autoFlush_enabled_flushesAfterPrintln() {
        FlushCountingWriter fcw = new FlushCountingWriter();
        SingleThreadPrintWriter writer = SingleThreadPrintWriter.createInstance(fcw, true);
        writer.println("line");
        assertEquals("line" + System.lineSeparator(), fcw.getContents());
        assertTrue(fcw.flushCount > 0);
    }

    @Test
    void autoFlush_disabled_doesNotFlushAfterPrintln() {
        FlushCountingWriter fcw = new FlushCountingWriter();
        SingleThreadPrintWriter writer = SingleThreadPrintWriter.createInstance(fcw, false);
        writer.println("line");
        assertEquals("line" + System.lineSeparator(), fcw.getContents());
        assertEquals(0, fcw.flushCount);
    }

    // ---------------------------------------------------------------------------
    // closed-stream behavior
    // ---------------------------------------------------------------------------

    @Test
    void closedStream_write_doesNotThrowAndSetsErrorState() {
        StringWriter sw = new StringWriter();
        SingleThreadPrintWriter writer = SingleThreadPrintWriter.createInstance(sw);
        writer.close();
        assertDoesNotThrow(() -> writer.write('x'));
        assertTrue(writer.checkError());
    }

    @Test
    void closedStream_println_doesNotThrowAndSetsErrorState() {
        StringWriter sw = new StringWriter();
        SingleThreadPrintWriter writer = SingleThreadPrintWriter.createInstance(sw);
        writer.close();
        assertDoesNotThrow(() -> writer.println("test"));
        assertTrue(writer.checkError());
    }

    // ---------------------------------------------------------------------------
    // InterruptedIOException handling
    // ---------------------------------------------------------------------------

    @Test
    void interruptedIOException_onWriteInt_setsInterruptFlagWithoutSettingError() {
        SingleThreadPrintWriter writer = SingleThreadPrintWriter.createInstance(new InterruptingWriter());
        try {
            writer.write('a');
            assertTrue(Thread.currentThread().isInterrupted());
            assertFalse(writer.checkError());
        }
        finally {
            Thread.interrupted(); // clear the interrupt flag so it doesn't leak into other tests
        }
    }

    @Test
    void interruptedIOException_onWriteCharArray_setsInterruptFlagWithoutSettingError() {
        SingleThreadPrintWriter writer = SingleThreadPrintWriter.createInstance(new InterruptingWriter());
        try {
            writer.write(new char[] { 'a', 'b' }, 0, 2);
            assertTrue(Thread.currentThread().isInterrupted());
            assertFalse(writer.checkError());
        }
        finally {
            Thread.interrupted();
        }
    }

    @Test
    void interruptedIOException_onPrintlnString_setsInterruptFlagWithoutSettingError() {
        SingleThreadPrintWriter writer = SingleThreadPrintWriter.createInstance(new InterruptingWriter());
        try {
            assertDoesNotThrow(() -> writer.println("test"));
            assertTrue(Thread.currentThread().isInterrupted());
            assertFalse(writer.checkError());
        }
        finally {
            Thread.interrupted();
        }
    }

}
