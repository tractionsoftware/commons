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

import com.tractionsoftware.commons.util.Dimensions;

import java.util.Objects;

/**
 * A complete implementation of an {@link Icon} based upon an {@link IconFileResource} instance.
 *
 * @author Dave Shepperton
 */
public final class SimpleIcon implements Icon {

    private final IconFileResource iconFile;

    private final Dimensions<Integer> maxDimensions;

    public SimpleIcon(IconFileResource iconFile) {
        this(iconFile, null);
    }

    public SimpleIcon(IconFileResource iconFile, Dimensions<Integer> maxDimensions) {
        this.iconFile = iconFile;
        this.maxDimensions = maxDimensions;
    }

    @Override
    public final String toString() {
        StringBuilder ret = new StringBuilder();
        ret.append(getClass().getSimpleName());
        ret.append(":{");
        ret.append(iconFile);
        if (maxDimensions != null) {
            ret.append(", ");
            ret.append(maxDimensions);
        }
        ret.append("}");
        return ret.toString();
    }

    @Override
    public final boolean isValid() {
        return iconFile.isValid();
    }

    @Override
    public final String getFilename() {
        return iconFile.getFilename();
    }


    @Override
    public final String getContentId() {
        return iconFile.getContentId();
    }

    @Override
    public final String getDataUrl() {
        return iconFile.getDataUrl();
    }

    @Override
    public final Dimensions<Integer> getDimensions() {
        Dimensions<Integer> dimensions = iconFile.getDimensions(maxDimensions);
        if (dimensions == null) {
            return Dimensions.getInvalidInstanceInPixels();
        }
        return dimensions;
    }

    @Override
    public final IconFileResource getImageFileInfo() {
        return iconFile;
    }

    /**
     * This implementation is somewhat intelligent in that it won't attempt to retrieve or create a new {@link Icon} if
     * the requested maximum {@link Dimensions} are the same as the ones for this instance.
     */
    @Override
    public final Icon getScaled(Dimensions<Integer> newMaxDimensions) {
        if (Objects.equals(this.maxDimensions, newMaxDimensions)) {
            return this;
        }
        if (newMaxDimensions == null ||
            newMaxDimensions.equals(iconFile.getOriginalDimensions())) {
            return iconFile.getImage();
        }
        return iconFile.getImage(newMaxDimensions);
    }

    /**
     * This implementation is somewhat intelligent in that it won't attempt to retrieve or create a new {@link Icon} if
     * the requested {@link Dimensions} are the same as the ones for this instance, and otherwise makes a token attempt
     * to identify the closest matching available version of the file to use with
     * {@link ForwardingIcon#wrapWithForcedDimensions(Icon, Dimensions)}.
     */
    public final Icon withDimensions(Dimensions<Integer> newDimensions) {

        if (Objects.equals(getDimensions(), newDimensions)) {
            return this;
        }
        if (newDimensions == null ||
            newDimensions.equals(iconFile.getOriginalDimensions())) {
            return iconFile.getImage();
        }

        return ForwardingIcon.wrapWithForcedDimensions(iconFile.getImage(newDimensions), newDimensions);

    }

    @Override
    public final ImageResourceType getImageResourceType() {
        return iconFile.getImageResourceType();
    }

}
