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

import java.net.URI;
import java.util.Objects;

public abstract class FileMetadataBasedFileResource<F extends FileMetadata> implements FileResource {

    protected final F metadata;

    public FileMetadataBasedFileResource(@Nonnull F metadata) {
        Objects.requireNonNull(metadata, "metadata");
        this.metadata = metadata;
    }

    @Nonnull
    @Override
    public URI getURI() {
        URI uri = metadata.getURI();
        Objects.requireNonNull(uri, "metadata URI");
        return uri;
    }

    @Nonnull
    @Override
    public String getFilename() {
        String fileName = metadata.getFilename();
        Objects.requireNonNull(fileName, "metadata file name");
        return fileName;
    }

    @Nullable
    @Override
    public String getDescription() {
        return metadata.getDescription();
    }

    @Nullable
    @Override
    public String getContentType() {
        return metadata.getContentType();
    }

    @Nullable
    @Override
    public String getContentId() {
        return metadata.getContentId();
    }

    @Nonnull
    @Override
    public FileMetadata getMetadata() {
        return metadata.toReadOnly();
    }

    @Nonnull
    @Override
    public FileResourceType getType() {
        return Objects.requireNonNullElse(metadata.getResourceType(), CommonFileResourceType.OTHER);
    }

}
