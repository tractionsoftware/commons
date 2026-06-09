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

package com.tractionsoftware.commons.processor;

import com.tractionsoftware.commons.io.SizedInputStream;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;

public final class ProcessingUtilTest {

    // =====================================================================
    // getOutputStreamConsumer
    // =====================================================================

    @Test
    void getOutputStreamConsumer_copiesInputToOutput() throws IOException {
        byte[] input = "Hello World".getBytes(StandardCharsets.UTF_8);
        ByteArrayOutputStream sink = new ByteArrayOutputStream();

        Consumer<IOException> consumer = ProcessingUtil.getOutputStreamConsumer(sink);
        assertNotNull(consumer);

        try (SizedInputStream sis = SizedInputStream.forInputStream(new ByteArrayInputStream(input), input.length)) {
            consumer.consume(sis);
        }

        assertArrayEquals(input, sink.toByteArray());
    }

    @Test
    void getOutputStreamConsumer_emptyInput_producesEmptyOutput() throws IOException {
        ByteArrayOutputStream sink = new ByteArrayOutputStream();
        Consumer<IOException> consumer = ProcessingUtil.getOutputStreamConsumer(sink);

        try (SizedInputStream sis = SizedInputStream.forInputStream(new ByteArrayInputStream(new byte[0]), 0)) {
            consumer.consume(sis);
        }

        assertEquals(0, sink.size());
    }

    // =====================================================================
    // getPopulatedResult — uses a simple in-memory ResultProvider
    // =====================================================================

    /**
     * A minimal in-memory Result for testing, backed by a ByteArrayOutputStream.
     */
    static final class ByteArrayResult extends Result {

        private final ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        private boolean populated = false;

        public byte[] getBytes() {
            return buffer.toByteArray();
        }

        @Override
        public void release() {}

        @Override
        protected OutputStream getOutputStream() {
            return buffer;
        }

        @Override
        protected SizedInputStream getInputStream() {
            byte[] data = buffer.toByteArray();
            return SizedInputStream.forInputStream(new ByteArrayInputStream(data), data.length);
        }

        @Override
        protected void onPopulate(boolean success) {
            this.populated = success;
        }

        @Override
        protected void onConsume(boolean success) {}

    }

    /** A minimal ResultProvider that produces ByteArrayResult instances. */
    static final class ByteArrayResultProvider implements ResultProvider {
        @Override
        public Result getEmptyResult() {
            return new ByteArrayResult();
        }
    }

    /** A minimal Producer that writes a fixed string. */
    static final class StringProducer implements Producer<IOException> {

        private final String content;

        StringProducer(String content) {
            this.content = content;
        }

        @Override
        public ResultProvider getResultProvider() {
            return new ByteArrayResultProvider();
        }

        @Override
        public void produce(OutputStream output) throws IOException {
            output.write(content.getBytes(StandardCharsets.UTF_8));
        }

        @Override
        public void produce(Writer out) throws IOException {
            out.write(content);
        }

        @Override
        public void produce(PrintWriter out) {
            out.print(content);
        }
    }

    @Test
    void getPopulatedResult_writesProducerOutput() throws IOException {
        StringProducer producer = new StringProducer("test content");
        Result result = ProcessingUtil.getPopulatedResult(producer);
        assertNotNull(result);

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Consumer<IOException> consumer = ProcessingUtil.getOutputStreamConsumer(out);
        result.consume(consumer);

        assertEquals("test content", out.toString(StandardCharsets.UTF_8));
    }

    @Test
    void getPopulatedResult_emptyContent_works() throws IOException {
        StringProducer producer = new StringProducer("");
        Result result = ProcessingUtil.getPopulatedResult(producer);
        assertNotNull(result);

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        result.consume(ProcessingUtil.getOutputStreamConsumer(out));
        assertEquals(0, out.size());
    }

    // =====================================================================
    // getResultFromChainedProcessors
    // =====================================================================

    /** A Processor that uppercases input. */
    static final class UpperCaseProcessor implements Processor<IOException> {

        @Override
        public ResultProvider getResultProvider() {
            return new ByteArrayResultProvider();
        }

        @Override
        public void process(java.io.InputStream input, OutputStream output) throws IOException {
            byte[] data = input.readAllBytes();
            String s = new String(data, StandardCharsets.UTF_8).toUpperCase();
            output.write(s.getBytes(StandardCharsets.UTF_8));
        }
    }

    @Test
    void getResultFromChainedProcessors_noProcessors_returnsOriginalContent() throws IOException {
        StringProducer producer = new StringProducer("hello");
        Result result = ProcessingUtil.getResultFromChainedProcessors(
            producer, Collections.<Processor<IOException>>emptyList().iterator()
        );
        assertNotNull(result);

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        result.consume(ProcessingUtil.getOutputStreamConsumer(out));
        assertEquals("hello", out.toString(StandardCharsets.UTF_8));
    }

    @Test
    void getResultFromChainedProcessors_withOneProcessor_transformsContent() throws IOException {
        StringProducer producer = new StringProducer("hello");
        Result result = ProcessingUtil.getResultFromChainedProcessors(
            producer, Collections.<Processor<IOException>>singletonList(new UpperCaseProcessor()).iterator()
        );

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        result.consume(ProcessingUtil.getOutputStreamConsumer(out));
        assertEquals("HELLO", out.toString(StandardCharsets.UTF_8));
    }

}
