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

package com.tractionsoftware.commons.image;

import com.tractionsoftware.commons.io.FileResource;
import com.tractionsoftware.commons.io.FileResourceType;
import com.tractionsoftware.commons.io.SizedInputStream;
import com.tractionsoftware.commons.util.Dimensions;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

import java.io.IOException;
import java.net.URI;
import java.util.Date;
import java.util.Objects;

/**
 * An {@link IconFileResource} implementation in terms of a generic
 * {@link com.tractionsoftware.commons.io.FileResource}. Note that since instance carries a FileInfo, unlike other
 * IconFile implementations, these are not safe to be shared between threads.
 *
 * @author Dave Shepperton
 */
public final class SimpleFileResourceIconFileAdapter extends AbstractIconFile {

    public static final SimpleFileResourceIconFileAdapter createInstance(FileResource file, FileResourceType fileResourceType) {
        Objects.requireNonNull(file, "FileResource");
        Objects.requireNonNull(fileResourceType, "file resource type");
        return new SimpleFileResourceIconFileAdapter(file, fileResourceType);
    }

    private final FileResource file;

    private final FileResourceType fileResourceType;

    private SimpleFileResourceIconFileAdapter(FileResource file, FileResourceType fileResourceType) {
        this.file = file;
        this.fileResourceType = fileResourceType;
    }

    @Override
    public final boolean equals(Object other) {
        if (other instanceof SimpleFileResourceIconFileAdapter otherAdapter &&
            file.equals(otherAdapter.file)) {
            return true;
        }
        return false;
    }

    @Override
    public final int hashCode() {
        return Objects.hash(file);
    }

    @Override
    public final String toString() {
        return getClass().getSimpleName() + ":{" + file + "}";
    }

    @Nonnull
    @Override
    public final FileResourceType getType() {
        return file.getType();
    }

    @Nonnull
    @Override
    public final String toDebugString() {
        StringBuilder ret = new StringBuilder();
        ret.append(getClass().getSimpleName());
        ret.append(":{");
        ret.append(file);
        Dimensions<Integer> originalDimensions = getOriginalDimensions();
        if (originalDimensions != null) {
            ret.append(" (");
            ret.append(originalDimensions);
            ret.append(")");
        }
        if (!isValid()) {
            ret.append(" [INVALID]");
        }
        ret.append("}");
        return ret.toString();
    }

    @Override
    protected final boolean isUnderlyingFileValid() {
        return file.isValid();
    }

    @Nonnull
    @Override
    public final URI getURI() {
        return file.getURI();
    }

    @Nonnull
    @Override
    public final String getPath() {
        return file.getPath();
    }

    @Nonnull
    @Override
    public final String getFilename() {
        return file.getFilename();
    }

    @Nullable
    @Override
    public final String getDataUrl() {
        return file.getDataUrl();
    }

    @Nonnull
    @Override
    public final SizedInputStream getInputStream() throws IOException {
        return file.getInputStream();
    }

    @Override
    public final String getContentType() {
        return file.getContentType();
    }

    @Override
    public final String getContentId() {
        return file.getContentId();
    }

    @Override
    public final long getByteSize() {
        return file.getByteSize();
    }

    @Nonnull
    @Override
    public final Date getLastModified() {
        return file.getLastModified();
    }

    @Override
    public final FileResourceType getImageResourceType() {
        return fileResourceType;
    }

}
