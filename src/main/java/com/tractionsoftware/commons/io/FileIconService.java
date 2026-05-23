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

import com.tractionsoftware.commons.image.Icon;
import com.tractionsoftware.commons.image.IconFileResource;
import com.tractionsoftware.commons.image.SimpleIcon;
import com.tractionsoftware.commons.lang.JavaUtil;
import com.tractionsoftware.commons.lang.ObjectsUtil;
import com.tractionsoftware.commons.text.SnippetUtil;
import com.tractionsoftware.commons.util.Dimensions;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.util.Objects;
import java.util.function.Supplier;

public abstract class FileIconService {

    private static final Logger LOGGER = LoggerFactory.getLogger(FileIconService.class);

    public static final Icon NO_ICON = new SimpleIcon(
        new IconFileResource.InvalidIconFile(Icon.CommonImageResourceType.ICON_FILE_TYPE)
    );

    public static final FileIconService NONE = new FileIconService() {

        @Nonnull
        @Override
        protected final Icon getIconImpl(@Nonnull FileResource file, @Nullable Dimensions<Integer> maxDimensions) {
            return NO_ICON.getScaled(maxDimensions);
        }

        @Override
        protected String getIconStyleNameImpl(FileResource file) {
            return null;
        }

        @Override
        protected final URL getURLImpl(@Nonnull Icon icon) {
            return null;
        }

    };

    private static final Supplier<? extends FileIconService> instance = JavaUtil.lazyServiceLoader(
        FileIconService.class, NONE, LOGGER
    );

    public static final FileIconService get() {
        return instance.get();
    }

    @Nonnull
    public final Icon getIcon(@Nonnull FileResource file) {
        return getIcon(file, null);
    }

    @Nonnull
    public final Icon getIcon(@Nonnull FileResource file, Dimensions<Integer> maxDimensions) {
        Icon icon;
        try {
            icon = getIconImpl(file, maxDimensions);
        }
        catch (RuntimeException e) {
            LOGGER.warn("Failed to retrieve a file icon for " + ObjectsUtil.safeToString(file), e);
            icon = null;
        }
        return Objects.requireNonNullElse(icon, NO_ICON);
    }

    @Nullable
    public final String getIconStyleName(@Nonnull FileResource file) {
        try {
            return getIconStyleNameImpl(file);
        }
        catch (RuntimeException e) {
            LOGGER.warn("Failed to retrieve a style name for the icon for " + ObjectsUtil.safeToString(file), e);
            return null;
        }
    }

    @Nullable
    public URL getURL(@Nonnull Icon icon) {
        Icon.ImageResourceType type = icon.getImageResourceType();
        if (type != Icon.CommonImageResourceType.ICON_FILE_TYPE) {
            throw new IllegalArgumentException(
                "Expected type " + Icon.CommonImageResourceType.ICON_FILE_TYPE + ", got " + type + "."
            );
        }
        try {
            return getURLImpl(icon);
        }
        catch (RuntimeException e) {
            LOGGER.warn("Failed to construct a URL for icon {} ", SnippetUtil.truncatedToString(icon, 100), e);
            return null;
        }
    }

    @Nullable
    protected abstract Icon getIconImpl(FileResource file, @Nullable Dimensions<Integer> maxDimensions);

    @Nullable
    protected abstract String getIconStyleNameImpl(FileResource file);

    @Nullable
    protected abstract URL getURLImpl(@Nonnull Icon icon);

}
