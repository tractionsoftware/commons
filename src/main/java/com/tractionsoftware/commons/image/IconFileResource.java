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

import com.tractionsoftware.commons.io.CommonFileResourceType;
import com.tractionsoftware.commons.io.FileResource;
import com.tractionsoftware.commons.io.FileResourceType;
import com.tractionsoftware.commons.io.SizedInputStream;
import com.tractionsoftware.commons.util.Dimensions;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URI;
import java.util.Date;

/**
 * A special {@link FileResource} that is used to back {@link Icon} implementations. Since instances will frequently be
 * cached and otherwise shared between multiple threads, implementations should almost always be thread-safe, and not
 * carrying request-specific data. The main exception is {@link SimpleFileResourceIconFileAdapter}.
 *
 * @author Dave Shepperton
 */
public interface IconFileResource extends FileResource {

    /**
     * A simple type of IconFile representing an invalid instance, suitable for use as a placeholder.
     */
    public static final class InvalidIconFileResource extends AbstractIconFile {

        private final FileResourceType resourceType;

        public InvalidIconFileResource(FileResourceType resourceType) {
            super();
            this.resourceType = resourceType;
        }

        @Override
        protected final boolean isUnderlyingFileValid() {
            return false;
        }

        @Nonnull
        @Override
        public final SizedInputStream getInputStream() throws IOException {
            throw new FileNotFoundException();
        }

        @Nonnull
        @Override
        public final FileResourceType getType() {
            return resourceType;
        }

        @Nonnull
        @Override
        public final URI getURI() {
            return URI.create("icon:invalid");
        }

        @Nonnull
        @Override
        public final String getPath() {
            return "";
        }

        @Nonnull
        @Override
        public final String getFilename() {
            return "";
        }

        @Nullable
        @Override
        public final String getContentType() {
            return null;
        }

        @Nullable
        @Override
        public final String getContentId() {
            return null;
        }

        @Override
        public final long getByteSize() {
            return 0;
        }

        @Nonnull
        @Override
        public final Date getLastModified() {
            return new Date(0);
        }

        @Nullable
        @Override
        public final String getDisplayName() {
            return null;
        }

        @Nullable
        @Override
        public final String getDescription() {
            return null;
        }

        @Nonnull
        @Override
        public final String toDebugString() {
            return "NONE";
        }

        @Override
        public final FileResourceType getImageResourceType() {
            return resourceType;
        }

    }

    /**
     * Returns true if this icon file is valid. Validity for IconFiles generally requires that the basic
     * {@link FileResource} validity check be fulfilled, and also that the underlying resource is a valid and supported
     * type of image.
     *
     * @return true if this icon file instance is considered valid; false otherwise.
     */
    @Override
    public boolean isValid();

    /**
     * Returns false because all IconFileResource instances must represent files, not directories.
     */
    @Override
    public default boolean isDirectory() {
        return false;
    }

    @Override
    public default Icon getImage(Dimensions<Integer> maxDimensions) {
        return new SimpleIcon(this, maxDimensions);
    }

    /**
     * Returns a display name for this IconFile. This may simply be {@link #getFilename() its file name}.
     *
     * @return a display name for this IconFile.
     */
    public String getDisplayName();

    /**
     * Returns a {@link Dimensions.Units#PIXELS}-denominated {@link Dimensions} object representing the original
     * dimensions of the image encapsulated by this IconFile. This should usually be cached on the IconFile instance,
     * and therefore should not have to be cached on an {@link Icon} instance.
     *
     * <p>
     * If this instance is not {@link #isValid()}, this method will definitely return null, but can return null in
     * certain other cases. This is most likely to happen in the case of an
     * {@link CommonFileResourceType#EXTERNAL external resource} that cannot be retrieved by TeamPage in order to have
     * its dimensions inspected. In that case, the Icon instance
     * {@link Icon#isValid() will reflect that the resource should still be assumed to be valid}, and the missing
     * {@link Icon#getDimensions() Dimensions}, in which case it would be appropriate for clients to use
     * {@link Icon#withDimensions(Dimensions)} if appropriate display dimensions are known (e.g., from an SDL html.image
     * tag's width= and height= attributes).
     *
     * @return a {@link Dimensions.Units#PIXELS}-denominated {@link Dimensions} object representing the dimensions of
     *     the image encapsulated by this Icon, if available; null otherwise.
     */
    public Dimensions<Integer> getOriginalDimensions();

    /**
     * Returns a {@link Dimensions.Units#PIXELS}-denominated {@link Dimensions} object representing a proportionally
     * scaled version of the original dimensions of the of the image encapsulated by this IconFile which fit into the
     * given maximum dimensions.
     *
     * <p>
     * This default implementation uses {@link ImageUtil#getScaledDimensions(Dimensions, Dimensions)}, which should be
     * suitable for all implementations.
     *
     * @param maxDimensions
     *     a {@link Dimensions.Units#PIXELS}-denominated {@link Dimensions} object representing the requested maximum
     *     dimensions.
     * @return a {@link Dimensions.Units#PIXELS}-denominated {@link Dimensions} object representing a proportionally
     *     scaled version of the original dimensions of the of the image encapsulated by this IconFile which fit into
     *     the given maximum dimensions; or the original dimensions as-is if no maximum dimensions are specified, or if
     *     the original dimensions already fit into the maximum; or null if
     *     {@link #getOriginalDimensions() the original dimensions} are not available.
     * @see ImageUtil#getScaledDimensions(Dimensions, Dimensions)
     */
    public default Dimensions<Integer> getDimensions(Dimensions<Integer> maxDimensions) {
        return ImageUtil.getScaledDimensions(getOriginalDimensions(), maxDimensions);
    }

    /**
     * Returns the {@link FileResourceType} indicating the type of image resource this represents. This is required for
     * some {@link Icon} implementations.
     *
     * @return the {@link FileResourceType} indicating the type of image resource this represents.
     */
    public FileResourceType getImageResourceType();

    /**
     * Returns a more detailed descriptive String than {@code toString()}, suitable for debugging purposes.
     *
     * @return a more detailed descriptive String than {@code toString()}, suitable for debugging purposes.
     */
    @Nonnull
    public String toDebugString();

    /**
     * This implementation always returns false because icon files are image resources, and will never be text.
     */
    @Override
    public default boolean isText() {
        return false;
    }

    /**
     * This implementation always returns false because icon files are image resources, and will never be plain text.
     */
    @Override
    public default boolean isPlainText() {
        return false;
    }

    /**
     * This implementation always returns false because icon files are image resources, and will never be HTML.
     */
    @Override
    public default boolean isHtml() {
        return false;
    }

}
