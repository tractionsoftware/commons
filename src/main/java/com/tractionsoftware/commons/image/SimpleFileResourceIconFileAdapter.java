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
import com.tractionsoftware.commons.io.SizedInputStream;
import com.tractionsoftware.commons.util.Dimensions;

import java.io.IOException;
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

    private final FileResource fileInfo;

    private final Icon.ImageResourceType imageResourceType;

    public SimpleFileResourceIconFileAdapter(FileResource fileInfo, Icon.ImageResourceType imageResourceType) {
        Objects.requireNonNull(fileInfo, "FileResource");
        Objects.requireNonNull(imageResourceType, "image resource type");
        this.fileInfo = fileInfo;
        this.imageResourceType = imageResourceType;
    }

    @Override
    public final boolean equals(Object other) {
        if (!(other instanceof SimpleFileResourceIconFileAdapter)) {
            return false;
        }
        if (fileInfo.equals(((SimpleFileResourceIconFileAdapter) other).fileInfo)) {
            return true;
        }
        return false;
    }

    @Override
    public final int hashCode() {
        return Objects.hash(fileInfo);
    }

    @Override
    public final String toString() {
        return getClass().getSimpleName() +
               ":{" +
               fileInfo +
               "}";
    }

    @Override
    public final String toDebugString() {
        StringBuilder ret = new StringBuilder();
        ret.append(getClass().getSimpleName());
        ret.append(":{");
        ret.append(fileInfo);
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
        return fileInfo.isValid();
    }

    @Override
    public String getPath() {
        return fileInfo.getPath();
    }

    @Override
    public final String getFilename() {
        return fileInfo.getFilename();
    }

    @Override
    public final String getDataUrl() {
        return fileInfo.getDataUrl();
    }

    @Override
    public final SizedInputStream getInputStream() throws IOException {
        return fileInfo.getInputStream();
    }

    @Override
    public final String getContentType() {
        return fileInfo.getContentType();
    }

    @Override
    public final String getContentId() {
        return fileInfo.getContentId();
    }

    @Override
    public final long getByteSize() {
        return fileInfo.getByteSize();
    }

    @Override
    public final Date getLastModified() {
        return fileInfo.getLastModified();
    }

    @Override
    public final Icon.ImageResourceType getImageResourceType() {
        return imageResourceType;
    }

}
