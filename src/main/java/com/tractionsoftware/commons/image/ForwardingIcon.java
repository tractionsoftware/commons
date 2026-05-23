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
import java.util.function.Supplier;

public abstract class ForwardingIcon implements Icon {

    private static abstract class ForcedDimensionsIcon extends ForwardingIcon {

        private final Dimensions<Integer> forcedDimensions;

        private ForcedDimensionsIcon(Dimensions<Integer> forcedDimensions) {
            this.forcedDimensions = forcedDimensions;
        }

        @Override
        public final Dimensions<Integer> getDimensions() {
            return forcedDimensions;
        }

        @Override
        public final Icon withDimensions(Dimensions<Integer> nestedNewDimensions) {
            if (Objects.equals(forcedDimensions, nestedNewDimensions)) {
                return this;
            }
            return delegate().withDimensions(nestedNewDimensions);
        }

    }

    public static final Icon wrap(Icon icon) {

        return new ForwardingIcon() {

            @Override
            protected final Icon delegate() {
                return icon;
            }

        };

    }

    public static final Icon wrap(Supplier<? extends Icon> iconSupplier) {

        return new ForwardingIcon() {

            @Override
            protected final Icon delegate() {
                return iconSupplier.get();
            }

        };

    }

    public static final Icon wrapWithForcedDimensions(Icon icon, Dimensions<Integer> forcedDimensions) {

        //Debug.file.println("Creating version of ", icon, " with forced dimensions ", forcedDimensions);

        return new ForcedDimensionsIcon(forcedDimensions) {

            @Override
            protected final Icon delegate() {
                return icon;
            }

        };

    }

    protected abstract Icon delegate();

    @Override
    public boolean isValid() {
        return delegate().isValid();
    }

    @Override
    public String getFilename() {
        return delegate().getFilename();
    }

    @Override
    public Dimensions<Integer> getDimensions() {
        return delegate().getDimensions();
    }

    @Override
    public int getWidth() {
        return delegate().getWidth();
    }

    @Override
    public int getHeight() {
        return delegate().getHeight();
    }

    @Override
    public String getContentId() {
        return delegate().getContentId();
    }

    @Override
    public String getDataUrl() {
        return delegate().getDataUrl();
    }

    @Override
    public String getWidthHTML() {
        return delegate().getWidthHTML();
    }

    @Override
    public String getHeightHTML() {
        return delegate().getHeightHTML();
    }

    @Override
    public Icon getScaled(Dimensions<Integer> newMaxDimensions) {
        return delegate().getScaled(newMaxDimensions);
    }

    @Override
    public Icon withDimensions(Dimensions<Integer> newDimensions) {
        return delegate().getScaled(newDimensions);
    }

    @Override
    public IconFileResource getImageFileInfo() {
        return delegate().getImageFileInfo();
    }

    @Override
    public ImageResourceType getImageResourceType() {
        return delegate().getImageResourceType();
    }

}
