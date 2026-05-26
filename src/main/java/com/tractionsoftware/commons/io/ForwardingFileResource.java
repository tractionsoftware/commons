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

import com.google.common.collect.ForwardingObject;
import com.tractionsoftware.commons.image.Icon;
import com.tractionsoftware.commons.util.Dimensions;
import jakarta.annotation.Nonnull;

import java.io.IOException;
import java.util.Date;

/**
 * A base {@link FileResource} implementation for implementing the decorator pattern. The {@link #delegate()} method returns
 * the FileInfo being decorated.
 *
 * @author Dave Shepperton
 */
public abstract class ForwardingFileResource extends ForwardingObject implements FileResource {

    @Nonnull
    @Override
    protected abstract FileResource delegate();

    @Override
    public boolean isValid() {
        return delegate().isValid();
    }

    @Nonnull
    @Override
    public SizedInputStream getInputStream() throws IOException, IllegalStateException {
        return delegate().getInputStream();
    }

    @Nonnull
    @Override
    public String getFilename() {
        return delegate().getFilename();
    }

    @Override
    public String getDescription() {
        return delegate().getDescription();
    }

    @Override
    public String getContentType() {
        return delegate().getContentType();
    }

    @Override
    public String getContentId() {
        return delegate().getContentId();
    }

    @Override
    public long getByteSize() {
        return delegate().getByteSize();
    }

    @Nonnull
    @Override
    public String getFormattedSize() {
        return delegate().getFormattedSize();
    }

    @Nonnull
    @Override
    public Date getLastModified() {
        return delegate().getLastModified();
    }

    @Nonnull
    @Override
    public FileMetadata getMetadata() {
        return delegate().getMetadata();
    }

    @Override
    public boolean isPersistent() {
        return delegate().isPersistent();
    }

    @Override
    public Icon getImage(Dimensions<Integer> maxDimensions) {
        return delegate().getImage(maxDimensions);
    }

    @Nonnull
    @Override
    public FileResourceType getType() {
        return delegate().getType();
    }

}
