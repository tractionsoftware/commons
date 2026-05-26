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

import java.io.*;
import java.net.URI;
import java.util.Date;
import java.util.Objects;

public final class ErrorTempFileResource extends FileMetadataBasedFileResource<MutableFileMetadata> implements
    TempFileResource {

    public final static ErrorTempFileResource createInstance(MutableFileMetadata metadata, Exception error) {
        Objects.requireNonNull(metadata, "metadata");
        Objects.requireNonNull(metadata.getFilename(), "file name from metadata");
        return new ErrorTempFileResource(metadata, error);
    }

    private final Exception error;

    private final Date date;

    private ErrorTempFileResource(MutableFileMetadata metadata, Exception error) {
        super(metadata);
        this.error = error;
        this.date = new Date();
    }

    @Override
    public final boolean delete() {
        return true;
    }

    @Override
    public final void save() throws IOException {
        throw fileNotFoundException();
    }

    @Override
    public final boolean exists() {
        return false;
    }

    @Override
    public final String getErrorMessage() {
        return error.getMessage();
    }

    @Override
    public final boolean hadError() {
        return true;
    }

    @Override
    public final OutputStream getOutputStream() throws IOException {
        throw fileNotFoundException();
    }

    @Override
    public final PrintWriter getUtf8PrintWriter() throws IOException {
        throw fileNotFoundException();
    }

    @Override
    public final void flush() {
    }

    @Override
    public final void close() {
    }

    @Override
    public final void setContent(InputStream input) throws IOException {
        TempFileResource.super.setContent(input);
    }

    @Override
    public final boolean isValid() {
        return false;
    }

    @Nonnull
    @Override
    public final URI getURI() {
        return URI.create(getURISpec());
    }

    @Nonnull
    @Override
    public final SizedInputStream getInputStream() throws IOException {
        throw fileNotFoundException();
    }

    @Override
    public final long getByteSize() {
        return 0;
    }

    @Nonnull
    @Override
    public final Date getLastModified() {
        return date;
    }

    private final String getURISpec() {
        String ext = getExtension();
        if (ext == null) {
            return URI_ID_ERROR;
        }
        return URI_ID_ERROR + "." + ext;
    }

    /**
     * Throws a {@link FileNotFoundException} shared by certain methods as a way of asserting that the underlying file
     * doesn't actually exist.
     */
    private final FileNotFoundException fileNotFoundException() {
        return new FileNotFoundException("This temporary file doesn't exist.");
    }

}
