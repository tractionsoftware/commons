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

import com.tractionsoftware.commons.io.FileMetadata;
import com.tractionsoftware.commons.io.SimpleMutableFileMetadata;
import com.tractionsoftware.commons.text.NumberFormats;
import com.tractionsoftware.commons.util.Dimensions;

import java.io.BufferedReader;
import java.nio.charset.Charset;

/**
 * Base implementation of {@link IconFileResource} that provides caching of {@link Dimensions} used for
 * {@link #getOriginalDimensions()} and fairly universal implementations of various other IconFile methods.
 *
 * @author Dave Shepperton
 */
public abstract class AbstractIconFile implements IconFileResource {

    /**
     * Encapsulates a cached {@link Dimensions} object that can be initialized with a preferred value, and can be
     * updated as necessary.
     */
    private final class CachedDimensions {

        private Dimensions<Integer> dimensions;

        private boolean readOrSet = false;

        private CachedDimensions(Dimensions<Integer> preferredDimensions) {
            update(preferredDimensions);
        }

        public final synchronized Dimensions<Integer> get() {
            if (!readOrSet) {
                readOrSet = true;
                dimensions = AbstractIconFile.this.getInitDimensions();
            }
            return dimensions;
        }

        public final synchronized void update(Dimensions<Integer> newDimensions) {
            if (newDimensions != null &&
                newDimensions.getUnits() == Dimensions.Units.PIXELS &&
                newDimensions.hasAtLeastOneValidDimension()) {
                this.dimensions = newDimensions;
                readOrSet = true;
            }
            else {
                this.dimensions = null;
                readOrSet = false;
            }
        }

    }

    private final CachedDimensions dimensions;

    /**
     * Constructs a new AbstractIconFile instance without any preferred dimensions.
     */
    public AbstractIconFile() {
        this(null);
    }

    /**
     * Constructs a new AbstractIconFile instance with the given preferred dimensions, if any, set as the original
     * dimensions.
     *
     * @param preferredDimensions
     *     a {@link Dimensions.Units#PIXELS}-denominated {@link Dimensions} object representing the preferred value for
     *     the original dimensions of the image.
     */
    public AbstractIconFile(Dimensions<Integer> preferredDimensions) {
        this.dimensions = new CachedDimensions(preferredDimensions);
    }

    @Override
    public final boolean isValid() {
        if (!isUnderlyingFileValid()) {
            return false;
        }
        Dimensions<Integer> originalDimensions = getOriginalDimensions();
        if (originalDimensions == null || originalDimensions.hasAtLeastOneValidDimension()) {
            return true;
        }
        return false;
    }

    @Override
    public final Charset getCharset() {
        return null;
    }

    @Override
    public final BufferedReader getReader() {
        throw new UnsupportedOperationException("image data");
    }

    @Override
    public final BufferedReader getReader(Charset charset) {
        throw new UnsupportedOperationException("image data");
    }

    /**
     * This default implementation returns null. Subclasses should override it if a display name is available.
     */
    @Override
    public String getDisplayName() {
        return null;
    }

    /**
     * This default implementation returns null. Subclasses should override it if a description is available.
     */
    @Override
    public String getDescription() {
        return null;
    }

    /**
     * This implementation returns a {@link Dimensions} object which is cached on demand in a thread-safe fashion. It
     * may still return null if no dimensions could be determined or retrieved.
     */
    @Override
    public final Dimensions<Integer> getOriginalDimensions() {
        return dimensions.get();
    }

    /**
     * This default implementation defers to {@link SimpleMutableFileMetadata#createForIconFileInfo(IconFileResource)},
     * which was specifically designed to provide a convenient implementation for this method. It should be suitable for
     * all implementations unless the {@link FileMetadata} needs to carry special non-standard properties.
     */
    @Override
    public FileMetadata getMetadata() {
        return SimpleMutableFileMetadata.createForIconFileInfo(this);
    }

    @Override
    public final String getFormattedSize() {
        long byteSize = getByteSize();
        if (byteSize < 0) {
            return "?";
        }
        return NumberFormats.getFormattedByteSize(getByteSize());
    }

    /**
     * Updates the dimensions to the new specified {@link Dimensions}. If the Dimensions is null, not
     * {@link Dimensions.Units#PIXELS pixel}-denominated, or does not
     * {@link Dimensions#hasAtLeastOneValidDimension() have at least one valid dimension}, the currently cached
     * Dimensions, if any, will be cleared. This will effectively require the dimensions to be recomputed from the
     * backing image file the next time {@link #getOriginalDimensions()} is invoked, unless this method is invoked again
     * with valid Dimensions.
     *
     * @param newDimensions
     *     the new {@link Dimensions} to use.
     */
    protected final void updateDimensions(Dimensions<Integer> newDimensions) {
        dimensions.update(newDimensions);
    }

    /**
     * Returns true if this AbstractIconFile fulfills
     * {@link com.tractionsoftware.commons.io.FileResource#isValid() FileResource's basic definition for validity}.
     *
     * @return true if this AbstractIconFile fulfills
     *     {@link com.tractionsoftware.commons.io.FileResource#isValid() FileResource's basic definition for validity}.
     */
    protected abstract boolean isUnderlyingFileValid();

    /**
     * Reads or otherwise retrieves the {@link Dimensions} for the image encapsulated by this icon file resource. It is
     * used to initialize the Dimensions cached on this instance.
     *
     * <p>
     * This default implementation uses
     * {@link Dimensions#getImageDimensionsInPixels(com.tractionsoftware.commons.io.FileResource)}, passing this object
     * itself, which will use {@link #getInputStream() the InputStream} for the underlying resource. It should be
     * applicable to most implementations, but subclasses should override it as necessary, e.g., if the dimensions are
     * already known -- or to return null if they definitely cannot be known.
     *
     * @return the freshly retrieved {@link Dimensions} for the image encapsulated by this icon file resource, if they
     *     were already known or could be read or otherwise retrieved; null otherwise.
     */
    protected Dimensions<Integer> getInitDimensions() {
        return Dimensions.getImageDimensionsInPixels(this);
    }

}
