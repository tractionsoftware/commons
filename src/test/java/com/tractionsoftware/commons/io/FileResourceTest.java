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

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URI;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.security.DigestInputStream;
import java.util.Date;
import java.util.Iterator;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for {@link FileResource}'s default methods, exercised via a minimal hand-built implementation.
 */
class FileResourceTest {

    /**
     * Minimal configurable FileResource implementation used to exercise the interface's default methods.
     */
    private static class TestFileResource implements FileResource {

        private final String filename;

        private final String contentType;

        private final byte[] content;

        private boolean directory;

        private boolean throwOnGetInputStream;

        TestFileResource(String filename, String contentType, byte[] content) {
            this.filename = filename;
            this.contentType = contentType;
            this.content = content;
        }

        void setDirectory(boolean directory) {
            this.directory = directory;
        }

        void setThrowOnGetInputStream(boolean throwOnGetInputStream) {
            this.throwOnGetInputStream = throwOnGetInputStream;
        }

        @Nonnull
        @Override
        public FileResourceType getType() {
            return CommonFileResourceType.OTHER;
        }

        @Override
        public boolean isValid() {
            return true;
        }

        @Nonnull
        @Override
        public URI getURI() {
            return URI.create("file:///" + filename);
        }

        @Nonnull
        @Override
        public String getFilename() {
            return filename;
        }

        @Override
        public boolean isDirectory() {
            return directory;
        }

        @Nonnull
        @Override
        public SizedInputStream getInputStream() throws IOException {
            if (throwOnGetInputStream) {
                throw new IOException("simulated failure");
            }
            return SizedInputStream.forInputStream(new ByteArrayInputStream(content), content.length);
        }

        @Nullable
        @Override
        public String getDescription() {
            return "test description";
        }

        @Nullable
        @Override
        public String getContentType() {
            return contentType;
        }

        @Nullable
        @Override
        public String getContentId() {
            return "test-content-id";
        }

        @Override
        public long getByteSize() {
            return content.length;
        }

        @Nonnull
        @Override
        public Date getLastModified() {
            return new Date(0);
        }

        @Nonnull
        @Override
        public FileMetadata getMetadata() {
            return SimpleMutableFileMetadata.createFromFileName(filename);
        }

    }

    private TestFileResource resource;

    @BeforeEach
    void setUp() {
        resource = new TestFileResource("test.txt", "text/plain", "hello world".getBytes(StandardCharsets.UTF_8));
    }

    // ---------------------------------------------------------------------------
    // getPath
    // ---------------------------------------------------------------------------

    @Test
    void getPath_defaultImplementation_returnsURIPath() {
        assertEquals("/test.txt", resource.getPath());
    }

    // ---------------------------------------------------------------------------
    // getReader / readString
    // ---------------------------------------------------------------------------

    @Test
    void getReader_readsDecodedContent() throws IOException {
        try (BufferedReader reader = resource.getReader()) {
            assertEquals("hello world", reader.readLine());
        }
    }

    @Test
    void getReader_withExplicitCharset_readsDecodedContent() throws IOException {
        try (BufferedReader reader = resource.getReader(StandardCharsets.US_ASCII)) {
            assertEquals("hello world", reader.readLine());
        }
    }

    @Test
    void readString_returnsFullContent() throws IOException {
        assertEquals("hello world", resource.readString());
    }

    @Test
    void readString_withExplicitCharset_returnsFullContent() throws IOException {
        assertEquals("hello world", resource.readString(StandardCharsets.UTF_8));
    }

    // ---------------------------------------------------------------------------
    // getDigestInputStream / getMD5 / getMD5Hash / getPaddedMD5Hash
    // ---------------------------------------------------------------------------

    @Test
    void getDigestInputStream_returnsReadableStream() throws IOException {
        try (DigestInputStream digestStream = resource.getDigestInputStream()) {
            assertNotNull(digestStream);
            digestStream.readAllBytes();
        }
    }

    @Test
    void getMD5_nonEmptyFile_returnsNonEmptyHash() {
        byte[] hash = resource.getMD5();
        assertNotNull(hash);
        assertTrue(hash.length > 0);
    }

    @Test
    void getMD5Hash_nonEmptyFile_returnsNonBlankHexString() {
        String hash = resource.getMD5Hash();
        assertNotNull(hash);
        assertFalse(hash.isEmpty());
    }

    @Test
    void getPaddedMD5Hash_nonEmptyFile_returns32CharHexString() {
        String padded = resource.getPaddedMD5Hash();
        assertNotNull(padded);
        assertEquals(32, padded.length());
    }

    @Test
    void getMD5_whenGetInputStreamThrows_returnsNull() {
        resource.setThrowOnGetInputStream(true);
        assertNull(resource.getMD5());
    }

    // ---------------------------------------------------------------------------
    // getCharset
    // ---------------------------------------------------------------------------

    @Test
    void getCharset_contentTypeWithCharsetParam_usesThatCharset() {
        resource = new TestFileResource("test.txt", "text/plain; charset=ISO-8859-1", "x".getBytes(StandardCharsets.UTF_8));
        assertEquals(Charset.forName("ISO-8859-1"), resource.getCharset());
    }

    @Test
    void getCharset_contentTypeWithoutCharsetParam_defaultsToUtf8() {
        assertEquals(StandardCharsets.UTF_8, resource.getCharset());
    }

    @Test
    void getCharset_nullContentType_defaultsToUtf8() {
        resource = new TestFileResource("test.txt", null, "x".getBytes(StandardCharsets.UTF_8));
        assertEquals(StandardCharsets.UTF_8, resource.getCharset());
    }

    @Test
    void getCharset_unparseableContentType_defaultsToUtf8() {
        resource = new TestFileResource("test.txt", "not a valid content type!!", "x".getBytes(StandardCharsets.UTF_8));
        assertEquals(StandardCharsets.UTF_8, resource.getCharset());
    }

    // ---------------------------------------------------------------------------
    // getExtension
    // ---------------------------------------------------------------------------

    @Test
    void getExtension_returnsExtensionWithoutDot() {
        assertEquals("txt", resource.getExtension());
    }

    @Test
    void getExtension_noExtension_returnsNull() {
        resource = new TestFileResource("noext", "text/plain", "x".getBytes(StandardCharsets.UTF_8));
        assertNull(resource.getExtension());
    }

    // ---------------------------------------------------------------------------
    // getFormattedSize / getFormattedByteSize
    // ---------------------------------------------------------------------------

    @Test
    void getFormattedSize_smallFile_includesBytesUnit() {
        assertTrue(resource.getFormattedSize().endsWith("B"));
    }

    @Test
    void getFormattedByteSize_smallFile_omitsUnitSuffix() {
        assertFalse(resource.getFormattedByteSize().contains("B"));
    }

    // ---------------------------------------------------------------------------
    // isEmpty
    // ---------------------------------------------------------------------------

    @Test
    void isEmpty_nonEmptyFile_returnsFalse() {
        assertFalse(resource.isEmpty());
    }

    @Test
    void isEmpty_zeroByteFile_returnsTrue() {
        resource = new TestFileResource("empty.txt", "text/plain", new byte[0]);
        assertTrue(resource.isEmpty());
    }

    // ---------------------------------------------------------------------------
    // getDataUrl
    // ---------------------------------------------------------------------------

    @Test
    void getDataUrl_validFile_returnsDataUrl() {
        String dataUrl = resource.getDataUrl();
        assertNotNull(dataUrl);
        assertTrue(dataUrl.startsWith("data:"));
        assertTrue(dataUrl.contains("base64,"));
    }

    @Test
    void getDataUrl_whenGetInputStreamThrows_returnsNull() {
        resource.setThrowOnGetInputStream(true);
        assertNull(resource.getDataUrl());
    }

    // ---------------------------------------------------------------------------
    // isPersistent
    // ---------------------------------------------------------------------------

    @Test
    void isPersistent_defaultImplementation_returnsTrue() {
        assertTrue(resource.isPersistent());
    }

    // ---------------------------------------------------------------------------
    // getBase64
    // ---------------------------------------------------------------------------

    @Test
    void getBase64_directory_returnsNull() {
        resource.setDirectory(true);
        assertNull(resource.getBase64());
    }

    @Test
    void getBase64_validFile_returnsEncodedString() {
        assertNotNull(resource.getBase64());
    }

    @Test
    void getBase64_whenGetInputStreamThrows_returnsNull() {
        resource.setThrowOnGetInputStream(true);
        assertNull(resource.getBase64());
    }

    // ---------------------------------------------------------------------------
    // isImage
    // ---------------------------------------------------------------------------

    @Test
    void isImage_directory_returnsFalse() {
        resource.setDirectory(true);
        assertFalse(resource.isImage());
    }

    @Test
    void isImage_jpgExtension_returnsTrue() {
        resource = new TestFileResource("photo.jpg", null, "x".getBytes(StandardCharsets.UTF_8));
        assertTrue(resource.isImage());
    }

    @Test
    void isImage_plainTextFile_returnsFalse() {
        assertFalse(resource.isImage());
    }

    // ---------------------------------------------------------------------------
    // isText / isPlainText / isHtml
    // ---------------------------------------------------------------------------

    @Test
    void isText_plainTextContentType_returnsTrue() {
        assertTrue(resource.isText());
    }

    @Test
    void isText_nullContentType_returnsFalse() {
        resource = new TestFileResource("test.txt", null, "x".getBytes(StandardCharsets.UTF_8));
        assertFalse(resource.isText());
    }

    @Test
    void isPlainText_plainTextContentType_returnsTrue() {
        assertTrue(resource.isPlainText());
    }

    @Test
    void isPlainText_htmlContentType_returnsFalse() {
        resource = new TestFileResource("test.html", "text/html", "x".getBytes(StandardCharsets.UTF_8));
        assertFalse(resource.isPlainText());
    }

    @Test
    void isHtml_htmlContentType_returnsTrue() {
        resource = new TestFileResource("test.html", "text/html", "x".getBytes(StandardCharsets.UTF_8));
        assertTrue(resource.isHtml());
    }

    @Test
    void isHtml_plainTextContentType_returnsFalse() {
        assertFalse(resource.isHtml());
    }

    // ---------------------------------------------------------------------------
    // getLineIterator
    // ---------------------------------------------------------------------------

    @Test
    void getLineIterator_returnsLinesInOrder() throws IOException {
        resource = new TestFileResource("multi.txt", "text/plain", "line1\nline2\nline3".getBytes(StandardCharsets.UTF_8));
        Iterator<String> it = resource.getLineIterator();
        assertEquals("line1", it.next());
        assertEquals("line2", it.next());
        assertEquals("line3", it.next());
        assertFalse(it.hasNext());
    }

}
