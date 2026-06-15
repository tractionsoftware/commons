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

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.Objects;

/**
 * A {@link FileResource} corresponding to a local file on disk. It extends {@link FileMetadataBasedFileResource} so
 * that things like {@link #getURI() its published URI} and {@link #getFilename() file name} do not need to correspond
 * to the physical file's location.
 *
 * @author Dave Shepperton
 */
public final class LocalFileResource extends FileMetadataBasedFileResource<FileMetadata> implements FileResource {

    public final static LocalFileResource createInstance(File file) {
        Objects.requireNonNull(file, "File");
        SimpleMutableFileMetadata metadata = SimpleMutableFileMetadata.createFromFileName(file.getName());
        metadata.setURI(file.toURI());
        metadata.ensureValidFilename();
        metadata.setResourceType(CommonFileResourceType.OTHER);
        return new LocalFileResource(file, metadata);
    }

    public final static LocalFileResource createInstance(File file, FileMetadata metadata) {
        Objects.requireNonNull(file, "File");
        Objects.requireNonNull(metadata, "FileMetadata");
        return new LocalFileResource(file, metadata);
    }

    private final File file;

    private LocalFileResource(File file, FileMetadata metadata) {
        super(metadata);
        this.file = file;
    }

    @Override
    public final boolean equals(Object other) {
        if (!(other instanceof LocalFileResource otherFile)) {
            return false;
        }
        if (file.equals(otherFile.file) && metadata.equals(otherFile.metadata)) {
            return true;
        }
        return false;
    }

    @Override
    public final int hashCode() {
        return Objects.hash(file, metadata);
    }

    @Override
    public final boolean isValid() {
        return file.exists() && file.canRead();
    }

    @Override
    public final boolean isDirectory() {
        return file.isDirectory();
    }

    @Nonnull
    @Override
    public final SizedInputStream getInputStream() throws IOException {
        return SizedInputStream.forInputStream(FileUtil.getBufferedInputStream(file), file.length());
    }

    @Override
    public final long getByteSize() {
        return file.length();
    }

    @Nonnull
    @Override
    public final Date getLastModified() {
        return new Date(file.lastModified());
    }

}
