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

package com.tractionsoftware.commons.net.http.server;

import com.google.common.net.HttpHeaders;
import com.google.common.net.MediaType;
import com.tractionsoftware.commons.io.SizedInputStream;
import com.tractionsoftware.commons.net.URLUtil;
import com.tractionsoftware.commons.net.http.server.HttpResultSender.ResponseWrapper;
import com.tractionsoftware.commons.processor.ByteArrayResult;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Dave Shepperton
 */
public class HttpResultSenderTest {

    // ---------------------------------------------------------------------------
    // sendHttpInline
    // ---------------------------------------------------------------------------

    @Test
    public void sendHttpInline_setsContentTypeThenSendsInputStream() throws Exception {

        RecordingResponseWrapper response = new RecordingResponseWrapper();
        ByteArrayResult result = new ByteArrayResult("hello world");
        MediaType contentType = MediaType.PLAIN_TEXT_UTF_8;

        HttpResultSender.sendHttpInline(response, result, contentType);

        assertEquals(List.of("setContentType", "send"), response.calls);
        assertEquals(contentType, response.lastContentType);
        assertTrue(response.headersSet.isEmpty());
        assertTrue(response.headersAdded.isEmpty());
        assertNull(response.lastContentLength);

        assertNotNull(response.sentInput);
        assertEquals("hello world", new String(response.sentInput.readAllBytes(), StandardCharsets.UTF_8));

        assertTrue(result.isOnConsumeCalled());
        assertTrue(result.isOnConsumeSuccess());
        assertFalse(result.isReleaseCalled());

    }

    @Test
    public void sendHttpInline_withNullContentType_passesNullThrough() throws Exception {

        RecordingResponseWrapper response = new RecordingResponseWrapper();
        ByteArrayResult result = new ByteArrayResult("ignored");

        HttpResultSender.sendHttpInline(response, result, null);

        assertEquals(List.of("setContentType", "send"), response.calls);
        assertNull(response.lastContentType);

    }

    // ---------------------------------------------------------------------------
    // sendHttpDownload
    // ---------------------------------------------------------------------------

    @Test
    public void sendHttpDownload_setsDefaultDownloadHeadersThenSendsInputStream() throws Exception {

        RecordingResponseWrapper response = new RecordingResponseWrapper();
        ByteArrayResult result = new ByteArrayResult("downloadable content");
        MediaType contentType = MediaType.CSV_UTF_8;
        String downloadFileName = "report final.csv";

        HttpResultSender.sendHttpDownload(response, result, contentType, downloadFileName);

        assertEquals(List.of("setHeader", "setContentType", "addHeader", "addHeader", "send"), response.calls);

        assertEquals(1, response.headersSet.size());
        assertEquals(Map.entry(HttpHeaders.CACHE_CONTROL, "no-store"), response.headersSet.getFirst());

        assertEquals(contentType, response.lastContentType);

        String encodedFileName = URLUtil.getUrlEncoding(downloadFileName);
        assertEquals(2, response.headersAdded.size());
        assertEquals(
            Map.entry(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + encodedFileName + "\""),
            response.headersAdded.get(0)
        );
        assertEquals(
            Map.entry(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename*=UTF-8''" + encodedFileName),
            response.headersAdded.get(1)
        );

        assertNotNull(response.sentInput);
        assertEquals("downloadable content", new String(response.sentInput.readAllBytes(), StandardCharsets.UTF_8));

        assertTrue(result.isOnConsumeCalled());
        assertTrue(result.isOnConsumeSuccess());

    }

    @Test
    public void sendHttpDownload_withNullContentType_defaultsToOctetStream() throws Exception {

        RecordingResponseWrapper response = new RecordingResponseWrapper();
        ByteArrayResult result = new ByteArrayResult("ignored");

        HttpResultSender.sendHttpDownload(response, result, null, "file.bin");

        assertEquals(MediaType.OCTET_STREAM, response.lastContentType);

    }

    // ---------------------------------------------------------------------------
    // test doubles
    // ---------------------------------------------------------------------------

    /**
     * A {@link ResponseWrapper} that records the sequence of calls made to it, along with their arguments, so that
     * tests can verify exactly which methods {@link HttpResultSender} invokes for a given call. Deliberately does NOT
     * override the interface's default {@code setDefaultDownloadHeaders} method, so that calling it through
     * {@code sendHttpDownload} exercises (and is covered by) the real default-method implementation.
     */
    private static final class RecordingResponseWrapper implements ResponseWrapper {

        private final List<String> calls = new ArrayList<>();

        private final List<Map.Entry<String, String>> headersSet = new ArrayList<>();

        private final List<Map.Entry<String, String>> headersAdded = new ArrayList<>();

        private Long lastContentLength;

        private MediaType lastContentType;

        private SizedInputStream sentInput;

        @Override
        public void setContentLength(long byteSize) {
            calls.add("setContentLength");
            lastContentLength = byteSize;
        }

        @Override
        public void setContentType(MediaType mediaType) {
            calls.add("setContentType");
            lastContentType = mediaType;
        }

        @Override
        public void setHeader(String name, String value) {
            calls.add("setHeader");
            headersSet.add(Map.entry(name, value));
        }

        @Override
        public void addHeader(String name, String value) {
            calls.add("addHeader");
            headersAdded.add(Map.entry(name, value));
        }

        @Override
        public void send(SizedInputStream input) {
            calls.add("send");
            sentInput = input;
        }

    }

}
