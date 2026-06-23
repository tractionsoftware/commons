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
import com.tractionsoftware.commons.processor.Result;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

/**
 * Utility methods that encapsulate sending the content from a {@link Result} as an HTTP response.
 *
 * @author Dave Shepperton
 * @since 4.0
 */
public final class HttpResultSender {

    private HttpResultSender() {
    }

    public static final String CONTENT_DISPOSITION_HEADER_FORMAT_ATTACHMENT = "attachment; %s";

    public static final String CONTENT_DISPOSITION_HEADER_FILE_NAME_TOKEN_FORMAT_MODERN =
        "filename*=" + StandardCharsets.UTF_8.name() + "''%s";

    public static final String CONTENT_DISPOSITION_HEADER_FILE_NAME_TOKEN_FORMAT_CLASSIC = "filename=\"%s\"";

    public static interface ResponseWrapper {

        public void setContentLength(long byteSize);

        public void setContentType(MediaType mediaType);

        public void setHeader(String name, String value);

        public void addHeader(String name, String value);

        public default void setDefaultDownloadHeaders(String downloadFileName, MediaType contentType) {

            setHeader(HttpHeaders.CACHE_CONTROL, "no-store");
            setContentType(Objects.requireNonNullElse(contentType, MediaType.OCTET_STREAM));

            String urlEncFileName = URLUtil.getUrlEncoding(downloadFileName);
            addHeader(
                HttpHeaders.CONTENT_DISPOSITION,
                String.format(
                    CONTENT_DISPOSITION_HEADER_FORMAT_ATTACHMENT,
                    String.format(CONTENT_DISPOSITION_HEADER_FILE_NAME_TOKEN_FORMAT_CLASSIC, urlEncFileName)
                )
            );
            addHeader(
                HttpHeaders.CONTENT_DISPOSITION,
                String.format(
                    CONTENT_DISPOSITION_HEADER_FORMAT_ATTACHMENT,
                    String.format(CONTENT_DISPOSITION_HEADER_FILE_NAME_TOKEN_FORMAT_MODERN, urlEncFileName)
                )
            );

        }

        public void send(SizedInputStream input) throws IOException;

    }

    private static abstract class Sender {

        protected final ResponseWrapper response;

        protected final Result result;

        protected final MediaType contentType;

        protected Sender(ResponseWrapper response, Result result, MediaType contentType) {
            this.response = response;
            this.result = result;
            this.contentType = contentType;
        }

        public final void send() throws IOException {
            result.consume(this::sendImpl);
        }

        private final void sendImpl(SizedInputStream input) throws IOException {
            setResponseHeaders();
            response.send(input);
        }

        protected abstract void setResponseHeaders();

    }

    private static final class InlineSender extends Sender {

        private static final Sender getInstance(ResponseWrapper response, Result result, MediaType contentType) {
            return new InlineSender(response, result, contentType);
        }

        private InlineSender(ResponseWrapper response, Result result, MediaType contentType) {
            super(response, result, contentType);
        }

        @Override
        protected final void setResponseHeaders() {
            response.setContentType(contentType);
        }

    }

    private static final class DownloadSender extends Sender {

        private final String downloadFileName;

        private static final Sender getInstance(ResponseWrapper response, Result result, MediaType contentType, String downloadFileName) {
            return new DownloadSender(response, result, contentType, downloadFileName);
        }

        private DownloadSender(ResponseWrapper response, Result result, MediaType contentType, String downloadFileName) {
            super(response, result, contentType);
            this.downloadFileName = downloadFileName;
        }

        @Override
        protected final void setResponseHeaders() {
            response.setDefaultDownloadHeaders(downloadFileName, contentType);
        }

    }

    /**
     * Sends the given {@link Result} to the client in the normal "inline" fashion for HTTP responses, consuming it.
     *
     * @param response
     *     to which the result content should be written.
     * @param result
     *     the result to be sent and consumed
     * @param contentType
     *     the value that should be set for the Content-type HTTP header.
     * @throws IOException
     *     if there is a problem sending the result.
     */
    public static final void sendHttpInline(ResponseWrapper response, Result result, MediaType contentType)
        throws IOException {
        InlineSender.getInstance(response, result, contentType).send();
    }

    /**
     * Sends the given TemporaryResult to the client setting the appropriate HTTP headers for a file download, consuming
     * it.
     *
     * @param response
     *     to which the result content should be written.
     * @param result
     *     the result to be sent and consumed
     * @param contentType
     *     the value that should be set for the Content-type HTTP response header.
     * @param downloadFileName
     *     the filename that should be indicated as part of the Content-disposition HTTP response header.
     * @throws IOException
     *     if there is a problem sending the result.
     */
    public static final void sendHttpDownload(ResponseWrapper response, Result result, MediaType contentType, String downloadFileName)
        throws IOException {
        DownloadSender.getInstance(response, result, contentType, downloadFileName).send();
    }

}
