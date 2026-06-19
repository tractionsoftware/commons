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

import com.google.common.base.Suppliers;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import com.google.common.net.MediaType;
import com.tractionsoftware.commons.image.Icon;
import com.tractionsoftware.commons.image.IconFileResource;
import com.tractionsoftware.commons.image.ImageUtil;
import com.tractionsoftware.commons.lang.NativeTypeConversion;
import com.tractionsoftware.commons.lang.StringUtil;
import com.tractionsoftware.commons.net.MediaTypeUtil;
import com.tractionsoftware.commons.properties.*;
import com.tractionsoftware.commons.text.NumberFormats;
import com.tractionsoftware.commons.util.Dimensions;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * A generic and mutable implementation of {@link FileMetadata}. Because it is a {@link ComplexProperty}, it is a
 * convenient class to use when serialization and deserialization is needed.
 *
 * @author Andy Keller, Dave Shepperton
 */
public class SimpleMutableFileMetadata implements MutableFileMetadata, ComplexProperty {

    private static final Logger LOGGER = LoggerFactory.getLogger(SimpleMutableFileMetadata.class);

    /**
     * Used as the name of a property for the file name, in the context of {@link #saveInstance(GetPutProperty)},
     * {@link #LOADER the Loader}, and the {@link GetPutProperty} returned by {@link #asGetPutProperty(FileResource)}.
     */
    public static final String PROP_NAME_FILE_NAME = "fname";

    /**
     * Used as the name of a property for the file name extension, in the context of the {@link GetPutProperty} returned
     * by {@link #asGetPutProperty(FileResource)}.
     */
    public static final String PROP_NAME_EXTENSION = "fextension";

    /**
     * Used as the name of a property for the description, in the context of {@link #saveInstance(GetPutProperty)},
     * {@link #LOADER the Loader}, and the {@link GetPutProperty} returned by {@link #asGetPutProperty(FileResource)}.
     */
    public static final String PROP_NAME_DESCRIPTION = "desc";

    /**
     * Used as the name of a property for the content type, in the context of {@link #saveInstance(GetPutProperty)},
     * {@link #LOADER the Loader}, and the {@link GetPutProperty} returned by {@link #asGetPutProperty(FileResource)}.
     */
    public static final String PROP_NAME_MIMETYPE = "mimetype";

    /**
     * Used as the name of a property for the file resource path, in the context of
     * {@link #saveInstance(GetPutProperty)}, {@link #LOADER the Loader}, and the {@link GetPutProperty} returned by
     * {@link #asGetPutProperty(FileResource)}. It has this name for historical reasons.
     *
     * <p>
     * Notice that this property does not necessarily represent a real local path in the host file system. See
     * {@link #getURI()} and {@link #setURI(URI)}.
     */
    public static final String PROP_NAME_URI = "localfname";

    /**
     * Used as the name of a property for the file's serial number, in the context of
     * {@link #saveInstance(GetPutProperty)}, {@link #LOADER the Loader}, and the {@link GetPutProperty} returned by
     * {@link #asGetPutProperty(FileResource)}.
     */
    public static final String PROP_NAME_NUMBER = "number";

    /**
     * Used as the name of a property to indicate whether a FileMetadata is a reference to a persisted file, in the
     * context of {@link #saveInstance(GetPutProperty)}, {@link #LOADER the Loader}, and the {@link GetPutProperty}
     * returned by {@link #asGetPutProperty(FileResource)}.
     */
    public static final String PROP_NAME_REFERENCE_TO_PERSISTED_FILE = "isref";

    /**
     * Used as the name of a property for the Content-ID, in the context of {@link #saveInstance(GetPutProperty)},
     * {@link #LOADER the Loader}, and the {@link GetPutProperty} returned by {@link #asGetPutProperty(FileResource)}.
     */
    public static final String PROP_NAME_CID = "cid";

    /**
     * Used as the name of a property for the Content-Location, in the context of {@link #saveInstance(GetPutProperty)},
     * {@link #LOADER the Loader}, and the {@link GetPutProperty} returned by {@link #asGetPutProperty(FileResource)}.
     */
    public static final String PROP_NAME_CONTENT_LOCATION = "cloc";

    /**
     * Used as the name of a property for the Content-Base, in the context of {@link #saveInstance(GetPutProperty)},
     * {@link #LOADER the Loader}, and the {@link GetPutProperty} returned by {@link #asGetPutProperty(FileResource)}.
     */
    public static final String PROP_NAME_CONTENT_BASE = "cbase";

    /**
     * Used as the name a read-only property referring to a formatted representation of the file's size, in the context
     * of the {@link GetPutProperty} returned by {@link #asGetPutProperty(FileResource)}.
     */
    public static final String PROP_NAME_FORMATTED_SIZE = "size";

    public static final String PROP_NAME_BYTESIZE = "bytesize";

    public static final String PROP_NAME_ERROR = "error";

    public static final String PROP_NAME_IMAGE = "image";

    public static final String PROP_NAME_IMAGE_WIDTH = "imagewidth";

    public static final String PROP_NAME_IMAGE_HEIGHT = "imageheight";

    public static final String PROP_NAME_ICON_URL = "iconurl";

    public static final String PROP_NAME_ICON_WIDTH = "iconwidth";

    public static final String PROP_NAME_ICON_HEIGHT = "iconheight";

    public static final String PROP_NAME_DISPLAY_NAME = "displayname";

    public static final ComplexProperty.Loader<SimpleMutableFileMetadata> LOADER = (GetProperty namespace) -> {
        if (!namespace.hasProperty(PROP_NAME_FILE_NAME)) {
            // The file name is the only required property.
            return null;
        }
        SimpleMutableFileMetadata loaded = new SimpleMutableFileMetadata();
        loaded.getLoadSaveProperties().putAllProperties(namespace);
        return loaded;
    };

    private static final Set<String> LOAD_SAVE_BASE_PROPERTY_NAMES = ImmutableSet.of(
        PROP_NAME_FILE_NAME,
        PROP_NAME_DESCRIPTION, PROP_NAME_MIMETYPE, PROP_NAME_URI,
        PROP_NAME_REFERENCE_TO_PERSISTED_FILE, PROP_NAME_NUMBER,
        PROP_NAME_ERROR, PROP_NAME_DISPLAY_NAME, PROP_NAME_CONTENT_LOCATION
    );

    /**
     * Creates a new FileData by copying properties of the given {@link FileResource}. If it is a {@link FileMetadata},
     * this method defers to {@link FileMetadata#mutableCopy()}. Otherwise, it copies properties in a straightforward
     * way.
     *
     * @param fileResource
     *     the {@link FileResource} whose properties should be used to create a {@link SimpleMutableFileMetadata}.
     * @return a new FileData representing the properties of the given {@link FileResource}.
     */
    @Nonnull
    public static final SimpleMutableFileMetadata createCopyFromFileInfo(@Nonnull FileResource fileResource) {
        Objects.requireNonNull(fileResource, "file resource");
        SimpleMutableFileMetadata metadata = new SimpleMutableFileMetadata();
        fileResource.getMetadata().copyTo(metadata);
        return metadata;
    }

    @Nonnull
    public static final SimpleMutableFileMetadata createForIconFileInfo(@Nonnull IconFileResource iconFile) {
        Objects.requireNonNull(iconFile, "icon file");
        SimpleMutableFileMetadata ret = createCopyFromFileInfo(iconFile);
        ret.setDisplayName(iconFile.getDisplayName());
        GetPutProperty props = ret.asGetPutProperty(iconFile);
        props.getProperty(PROP_NAME_FORMATTED_SIZE);
        props.getProperty(PROP_NAME_BYTESIZE);
        props.getProperty(PROP_NAME_IMAGE_WIDTH);
        props.getProperty(PROP_NAME_IMAGE_HEIGHT);
        return ret;
    }

    @Nonnull
    public static final SimpleMutableFileMetadata createCopy(@Nonnull FileMetadata source) {
        Objects.requireNonNull(source, "source");
        SimpleMutableFileMetadata copy = new SimpleMutableFileMetadata();
        source.copyTo(copy);
        return copy;
    }

    @Nonnull
    public static final SimpleMutableFileMetadata createFromFileNameAndContentType(@Nullable String fileName, @Nullable String contentType) {
        SimpleMutableFileMetadata ret = new SimpleMutableFileMetadata();
        ret.setFilename(fileName);
        ret.setContentType(contentType);
        return ret;
    }

    @Nonnull
    public static final SimpleMutableFileMetadata createFromFileName(@Nullable String fileName) {
        SimpleMutableFileMetadata ret = new SimpleMutableFileMetadata();
        ret.setFilename(fileName);
        String ext = FileNameUtil.getExtension(fileName, null);
        if (StringUtils.isNotBlank(ext)) {
            ret.setContentType(MediaTypeUtil.getContentTypeFromExtension(ext));
        }
        return ret;
    }

    /**
     * Attempts to use the supplied information, and the optional content of the file resource, to construct a file name
     * and/or make a guess at the appropriate content-type, if one is not supplied, and creates a
     * {@link SimpleMutableFileMetadata} carrying that file name and content-type.
     *
     * @param suggestedFilename
     *     the suggested file name, without an extension.
     * @param contentType
     *     the content-type, if that's already known.
     * @param inputSupplier
     *     an optional {@link Supplier} for an {@link InputStream} for retrieving the file resource's content. The
     *     argument for this parameter can be null, but if it is not null, unlike the general case of a Supplier, each
     *     invocation of {@link Supplier#get()} should really return a new InputStream instance in case multiple
     *     attempts are required to read the contents from the beginning to guess at a content-type.
     * @return a {@link SimpleMutableFileMetadata} carrying the file name and content-type based upon the given
     *     suggested name, content type and content.
     */
    @Nonnull
    public static final SimpleMutableFileMetadata createInstanceForUnnamedResource(@Nullable String suggestedFilename, @Nullable String contentType, @Nullable Supplier<? extends InputStream> inputSupplier) {

        String fileExtension = null;
        if (StringUtils.isNotBlank(contentType)) {
            fileExtension = MediaTypeUtil.getExtensionFromContentType(contentType);
        }

        if (StringUtils.isBlank(fileExtension)) {
            fileExtension = guessFileExtensionFromContents(inputSupplier);
            if (fileExtension == null) {
                contentType = MediaType.OCTET_STREAM.toString();
                fileExtension = "";
            }
            else {
                contentType = Objects.toString(
                    MediaTypeUtil.getContentTypeFromExtension(fileExtension), null
                );
            }
        }

        return createFromFileNameAndContentType(getDefaultFileName(suggestedFilename, fileExtension), contentType);

    }

    @Nonnull
    public static final String getDefaultFileName(@Nullable String suggestedFileName, @Nullable String fileExtension) {
        StringBuilder ret = new StringBuilder();
        if (StringUtils.isBlank(suggestedFileName)) {
            ret.append("file");
        }
        else {
            ret.append(suggestedFileName);
        }
        if (StringUtils.isNotBlank(fileExtension)) {
            ret.append(".");
            ret.append(fileExtension);
        }
        return ret.toString();
    }

    @Nullable
    private static final String guessFileExtensionFromContents(@Nullable Supplier<? extends InputStream> inputSupplier) {

        if (inputSupplier == null) {
            return null;
        }

        // Guess image.
        try (InputStream input = inputSupplier.get()) {
            return ImageUtil.getImageFileExtensionFromContents(input);
        }
        catch (Exception e) {
            LOGGER.debug("Unexpected problem determining whether this file is an image", e);
        }

        // No other guesses right now.
        return null;

    }

    private final class ReadOnlyView implements FileMetadata {

        @Nullable
        @Override
        public String getFilename() {
            return SimpleMutableFileMetadata.this.getFilename();
        }

        @Nullable
        @Override
        public URI getURI() {
            return SimpleMutableFileMetadata.this.getURI();
        }

        @Nullable
        @Override
        public final String getDescription() {
            return SimpleMutableFileMetadata.this.getDescription();
        }

        @Nullable
        @Override
        public final String getContentType() {
            return SimpleMutableFileMetadata.this.getContentType();
        }

        @Override
        public final int getNumber() {
            return SimpleMutableFileMetadata.this.getNumber();
        }

        @Override
        public final boolean isReferenceToPersistedFile() {
            return SimpleMutableFileMetadata.this.isReferenceToPersistedFile();
        }

        @Nullable
        @Override
        public final String getContentId() {
            return SimpleMutableFileMetadata.this.getContentId();
        }

        @Nullable
        @Override
        public final String getContentLocation() {
            return SimpleMutableFileMetadata.this.getContentLocation();
        }

        @Nullable
        @Override
        public final String getContentBase() {
            return SimpleMutableFileMetadata.this.getContentBase();
        }

        @Nullable
        @Override
        public final FileResourceType getResourceType() {
            return SimpleMutableFileMetadata.this.getResourceType();
        }

        @Nonnull
        @Override
        public final FileMetadata toReadOnly() {
            return this;
        }

    }

    private final class LoadSaveProperties implements GetPutProperty {

        @Nonnull
        @Override
        public final String toString() {
            return "LoadSaveProperties for FileData:{" + SimpleMutableFileMetadata.this + "}";
        }

        @Override
        public final String getProperty(String name) {

            return switch (name) {
                case PROP_NAME_FILE_NAME -> SimpleMutableFileMetadata.this.fileName;
                case PROP_NAME_DESCRIPTION -> SimpleMutableFileMetadata.this.description;
                case PROP_NAME_MIMETYPE -> SimpleMutableFileMetadata.this.contentType;
                case PROP_NAME_URI -> Objects.toString(SimpleMutableFileMetadata.this.uri, null);
                case PROP_NAME_REFERENCE_TO_PERSISTED_FILE -> NativeTypeConversion.booleanToString(
                    SimpleMutableFileMetadata.this.isReferenceToPersistedFile
                );
                case PROP_NAME_NUMBER -> Integer.toString(SimpleMutableFileMetadata.this.number);
                case PROP_NAME_CID -> SimpleMutableFileMetadata.this.contentId;
                case PROP_NAME_CONTENT_LOCATION -> SimpleMutableFileMetadata.this.contentLocation;
                case PROP_NAME_CONTENT_BASE -> SimpleMutableFileMetadata.this.contentBase;
                case PROP_NAME_ERROR -> SimpleMutableFileMetadata.this.errorMessage;
                case PROP_NAME_DISPLAY_NAME -> SimpleMutableFileMetadata.this.displayName;
                case null -> null;
                default -> (SimpleMutableFileMetadata.this.extendedProperties == null) ?
                    null : SimpleMutableFileMetadata.this.extendedProperties.get(name);
            };

        }

        @Override
        public final Set<String> getPropertyNames() {
            Set<String> base = SimpleMutableFileMetadata.LOAD_SAVE_BASE_PROPERTY_NAMES;
            if (SimpleMutableFileMetadata.this.extendedProperties == null) {
                return base;
            }
            return Sets.union(
                base, Collections.unmodifiableSet(SimpleMutableFileMetadata.this.extendedProperties.keySet())
            );
        }

        @Override
        public final void putProperty(String name, String value) {

            switch (name) {

            case PROP_NAME_FILE_NAME:
                SimpleMutableFileMetadata.this.fileName = value;
                break;

            case PROP_NAME_DESCRIPTION:
                SimpleMutableFileMetadata.this.description = value;
                break;

            case PROP_NAME_MIMETYPE:
                SimpleMutableFileMetadata.this.contentType = value;
                break;

            case PROP_NAME_URI:
                SimpleMutableFileMetadata.this.setURISpec(value);
                break;

            case PROP_NAME_REFERENCE_TO_PERSISTED_FILE:
                SimpleMutableFileMetadata.this.isReferenceToPersistedFile =
                    NativeTypeConversion.stringToBoolean(value, false);
                break;

            case PROP_NAME_NUMBER:
                SimpleMutableFileMetadata.this.number = NativeTypeConversion.stringToInt(value, -1);
                break;

            case PROP_NAME_CID:
                SimpleMutableFileMetadata.this.contentId = value;
                break;

            case PROP_NAME_CONTENT_LOCATION:
                SimpleMutableFileMetadata.this.contentLocation = value;
                break;

            case PROP_NAME_CONTENT_BASE:
                SimpleMutableFileMetadata.this.contentBase = value;
                break;

            case PROP_NAME_ERROR:
                SimpleMutableFileMetadata.this.errorMessage = value;
                break;

            case PROP_NAME_DISPLAY_NAME:
                SimpleMutableFileMetadata.this.displayName = value;
                break;

            case PROP_NAME_EXTENSION:
            case null:
                // ignore
                break;

            default:
                SimpleMutableFileMetadata.this.getExtendedProperties().put(name, value);
                break;

            }

        }

    }

    private final class BaseGetPutProperty implements GetPutProperty {

        private BaseGetPutProperty() {
        }

        @Nonnull
        @Override
        public final String toString() {
            return "BaseGetPutProperty for FileData:{" + SimpleMutableFileMetadata.this + "}";
        }

        @Override
        public final String getProperty(String name) {
            return switch (name) {
                case PROP_NAME_DISPLAY_NAME -> SimpleMutableFileMetadata.this.getDisplayName();
                case PROP_NAME_FILE_NAME -> SimpleMutableFileMetadata.this.getFilename();
                case PROP_NAME_DESCRIPTION -> SimpleMutableFileMetadata.this.getDescription();
                case PROP_NAME_MIMETYPE -> SimpleMutableFileMetadata.this.getContentType();
                case PROP_NAME_URI -> SimpleMutableFileMetadata.this.getURISpec();
                case PROP_NAME_NUMBER -> Integer.toString(SimpleMutableFileMetadata.this.getNumber());
                case PROP_NAME_REFERENCE_TO_PERSISTED_FILE ->
                    NativeTypeConversion.booleanToString(SimpleMutableFileMetadata.this.isReferenceToPersistedFile());
                case PROP_NAME_CID -> SimpleMutableFileMetadata.this.getContentId();
                case PROP_NAME_CONTENT_LOCATION -> SimpleMutableFileMetadata.this.getContentLocation();
                case PROP_NAME_CONTENT_BASE -> SimpleMutableFileMetadata.this.getContentBase();
                case PROP_NAME_ERROR -> SimpleMutableFileMetadata.this.getErrorMessage();
                case PROP_NAME_EXTENSION -> SimpleMutableFileMetadata.this.getExtension();
                case null, default -> null;
            };
        }

        @Override
        public final Set<String> getPropertyNames() {
            return ImmutableSet.of(
                PROP_NAME_FILE_NAME, PROP_NAME_DESCRIPTION,
                PROP_NAME_MIMETYPE, PROP_NAME_URI,
                PROP_NAME_REFERENCE_TO_PERSISTED_FILE, PROP_NAME_NUMBER,
                PROP_NAME_ERROR, PROP_NAME_DISPLAY_NAME, PROP_NAME_EXTENSION
            );
        }

        @Override
        public final void putProperty(String name, String value) {
            switch (name) {
            case PROP_NAME_DISPLAY_NAME -> SimpleMutableFileMetadata.this.setDisplayName(value);
            case PROP_NAME_FILE_NAME -> SimpleMutableFileMetadata.this.setFilename(value);
            case PROP_NAME_DESCRIPTION -> SimpleMutableFileMetadata.this.setDescription(value);
            case PROP_NAME_MIMETYPE -> SimpleMutableFileMetadata.this.setContentType(value);
            case PROP_NAME_URI -> SimpleMutableFileMetadata.this.setURISpec(value);
            case PROP_NAME_NUMBER -> SimpleMutableFileMetadata.this.setNumber(NativeTypeConversion.stringToInt(
                value,
                SimpleMutableFileMetadata.this.number
            ));
            case PROP_NAME_REFERENCE_TO_PERSISTED_FILE ->
                SimpleMutableFileMetadata.this.setReferenceToPersistedFile(NativeTypeConversion.stringToBoolean(
                    value,
                    SimpleMutableFileMetadata.this.isReferenceToPersistedFile
                ));
            case PROP_NAME_CID -> SimpleMutableFileMetadata.this.setContentId(value);
            case PROP_NAME_CONTENT_LOCATION -> SimpleMutableFileMetadata.this.setContentLocation(value);
            case PROP_NAME_CONTENT_BASE -> SimpleMutableFileMetadata.this.setContentBase(value);
            case PROP_NAME_ERROR -> SimpleMutableFileMetadata.this.errorMessage = value;
            case PROP_NAME_EXTENSION -> SimpleMutableFileMetadata.this.setExtension(value);
            case null, default -> {
            }
            }

        }

    }

    /**
     * A {@link GetPutProperty} implementation backed by a combination of the enclosing FileData instance and a
     * {@link FileResource} supplied to allow extended properties to be filled in and retrieved on-demand for read-only
     * access.
     */
    private final class ExtendedGetPutProperty implements GetPutProperty {

        private final Supplier<? extends FileResource> getFileInfo;

        private final Supplier<Icon> icon = Suppliers.memoize(this::loadIcon);

        private final Supplier<String> iconUrl = Suppliers.memoize(this::loadIconUrl);

        private Dimensions<Integer> imageDimensions = null;

        private long byteSize = Long.MIN_VALUE;

        private ExtendedGetPutProperty(Supplier<? extends FileResource> getFileInfo) {
            this.getFileInfo = getFileInfo;
        }

        @Nonnull
        @Override
        public final String toString() {
            return "ExtendedGetPutProperty for FileData:{" + SimpleMutableFileMetadata.this + "}";
        }

        @Override
        public final String getProperty(String name) {
            return switch (name) {
                case PROP_NAME_DISPLAY_NAME, PROP_NAME_FILE_NAME, PROP_NAME_DESCRIPTION,
                     PROP_NAME_MIMETYPE, PROP_NAME_URI, PROP_NAME_NUMBER, PROP_NAME_REFERENCE_TO_PERSISTED_FILE,
                     PROP_NAME_CID, PROP_NAME_CONTENT_LOCATION, PROP_NAME_CONTENT_BASE,
                     PROP_NAME_ERROR, PROP_NAME_EXTENSION -> null;
                case PROP_NAME_IMAGE ->
                    NativeTypeConversion.booleanToString(SimpleMutableFileMetadata.this.appearsToBeImage());
                case PROP_NAME_FORMATTED_SIZE, PROP_NAME_BYTESIZE, PROP_NAME_ICON_URL, PROP_NAME_ICON_WIDTH,
                     PROP_NAME_ICON_HEIGHT,
                     PROP_NAME_IMAGE_WIDTH,
                     PROP_NAME_IMAGE_HEIGHT -> SimpleMutableFileMetadata.this.getExtendedProperties()
                    .computeIfAbsent(name, this::computeExtendedProperty);
                case null -> null;
                default -> getOtherExtendedProperty(name);
            };
        }

        @Override
        public final void putProperty(String name, String value) {
            switch (name) {
            case PROP_NAME_DISPLAY_NAME, PROP_NAME_FILE_NAME, PROP_NAME_DESCRIPTION,
                 PROP_NAME_MIMETYPE, PROP_NAME_URI, PROP_NAME_NUMBER, PROP_NAME_REFERENCE_TO_PERSISTED_FILE,
                 PROP_NAME_CID, PROP_NAME_CONTENT_LOCATION, PROP_NAME_CONTENT_BASE,
                 PROP_NAME_ERROR, PROP_NAME_EXTENSION,
                 PROP_NAME_FORMATTED_SIZE, PROP_NAME_BYTESIZE, PROP_NAME_ICON_URL, PROP_NAME_ICON_WIDTH,
                 PROP_NAME_ICON_HEIGHT, PROP_NAME_IMAGE,
                 PROP_NAME_IMAGE_WIDTH, PROP_NAME_IMAGE_HEIGHT ->
                // not supported
                LOGGER.warn(
                    "SimpleMutableFileMetadata::putProperty does not support setting the {} property.",
                    name,
                    new UnsupportedOperationException()
                );
            case null -> {
            }
            default -> SimpleMutableFileMetadata.this.getExtendedProperties().put(name, value);
            }
        }

        @Override
        public final Set<String> getPropertyNames() {
            Set<String> base = ImmutableSet.of(
                PROP_NAME_FORMATTED_SIZE, PROP_NAME_IMAGE, PROP_NAME_IMAGE_WIDTH, PROP_NAME_IMAGE_HEIGHT,
                PROP_NAME_ICON_URL, PROP_NAME_ICON_WIDTH,
                PROP_NAME_ICON_HEIGHT
            );
            if (SimpleMutableFileMetadata.this.extendedProperties == null) {
                return base;
            }
            return Sets.union(
                base,
                Collections.unmodifiableSet(SimpleMutableFileMetadata.this.extendedProperties.keySet())
            );
        }

        private final String getOtherExtendedProperty(String name) {
            if (SimpleMutableFileMetadata.this.extendedProperties == null) {
                return null;
            }
            return SimpleMutableFileMetadata.this.extendedProperties.get(name);
        }

        private final String computeExtendedProperty(String name) {
            return switch (name) {
                case PROP_NAME_FORMATTED_SIZE -> NumberFormats.getFormattedByteSize(byteSize());
                case PROP_NAME_BYTESIZE -> Long.toString(byteSize());
                case PROP_NAME_ICON_URL -> iconUrl.get();
                case PROP_NAME_ICON_WIDTH -> Integer.toString(icon.get().getWidth());
                case PROP_NAME_ICON_HEIGHT -> Integer.toString(icon.get().getHeight());
                case PROP_NAME_IMAGE_WIDTH -> imageDimensions().getWidth().toString();
                case PROP_NAME_IMAGE_HEIGHT -> imageDimensions().getHeight().toString();
                default -> "";
            };
        }

        private final Icon loadIcon() {
            return FileIconService.get().getIcon(getFileInfo.get());
        }

        private final String loadIconUrl() {
            URL urlObj = FileIconService.get().getURL(icon.get());
            if (urlObj == null) {
                return null;
            }
            return urlObj.toString();
        }

        /**
         * Retrieves the {@link Dimensions} of the underlying image file, if the file appears to be an image and its
         * dimensions can be retrieved.
         *
         * @return the {@link Dimensions} of the underlying image file, if the file appears to be an image and its
         *     dimensions can be retrieved;
         *     {@link Dimensions#getInvalidInstanceInPixels() an invalid Dimensions instance} otherwise.
         */
        private final Dimensions<Integer> imageDimensions() {
            if (imageDimensions == null) {
                if (SimpleMutableFileMetadata.this.appearsToBeImage()) {
                    this.imageDimensions = Objects.requireNonNullElseGet(
                        readImageDimensions(), Dimensions::getInvalidInstanceInPixels
                    );
                }
                else {
                    this.imageDimensions = Dimensions.getInvalidInstanceInPixels();
                }
            }
            return imageDimensions;
        }

        @Nullable
        private final Dimensions<Integer> readImageDimensions() {
            FileResource fileResource = getFileInfo.get();
            if (fileResource == null) {
                return null;
            }
            if (fileResource instanceof IconFileResource iconFile) {
                return iconFile.getOriginalDimensions();
            }
            Icon image = fileResource.getImage();
            if (image == null) {
                return null;
            }
            return image.getDimensions();
        }

        private final long byteSize() {
            if (byteSize == Long.MIN_VALUE) {
                FileResource fileResource = getFileInfo.get();
                if (fileResource == null) {
                    byteSize = 0;
                }
                else {
                    byteSize = fileResource.getByteSize();
                }
            }
            return byteSize;
        }

    }

    private static final Function<String,String> toStringProperty(GetProperty baseProperties) {
        return (String propName) -> {
            String propValue = baseProperties.getProperty(propName);
            if (StringUtils.isBlank(propValue)) {
                return null;
            }
            return propName + "=" + StringUtil.truncate(propValue, 100);
        };
    }

    /**
     * See {@link #getFilename()} and {@link #setFilename(String)}.
     */
    private String fileName = null;

    /**
     * See {@link #getURI()} and {@link #setURI(URI)}.
     */
    private URI uri = null;

    /**
     * See {@link #getDescription()} and {@link #setDescription(String)}.
     */
    private String description = "";

    /**
     * See {@link #getContentType()} and {@link #setContentType(String)}.
     */
    private String contentType = null;

    /**
     * See {@link #getNumber()} and {@link #setNumber(int)}.
     */
    private int number = 0;

    /**
     * See {@link #isReferenceToPersistedFile()} and {@link #setReferenceToPersistedFile(boolean)}.
     */
    private boolean isReferenceToPersistedFile = false;

    /**
     * See {@link #getContentId()} and {@link #setContentId(String)}.
     */
    private String contentId = null;

    /**
     * See {@link #getContentLocation()} and {@link #setContentLocation(String)}.
     */
    private String contentLocation = null;

    /**
     * See {@link #getContentBase()} and {@link #setContentBase(String)}.
     */
    private String contentBase = null;

    private FileResourceType resourceType = null;

    /**
     * Tracks any error message associated with an attempt to find and reserve a temporary location for the file when it
     * was initially received or created. See {@link #hasError()} and {@link #getErrorMessage()}.
     */
    private String errorMessage = null;

    private String displayName = null;

    /**
     * Used to store other arbitrary properties. See {@link #asGetPutProperty(FileResource)}.
     */
    private HashMap<String,String> extendedProperties = null;

    /**
     * Constructs a new FileData.
     */
    public SimpleMutableFileMetadata() {
    }

    @Nonnull
    @Override
    public final String toString() {
        return StringUtil.join(getToStringProperties(), ", ");
    }

    @Override
    public final boolean equals(Object other) {
        if (!(other instanceof SimpleMutableFileMetadata)) {
            return false;
        }
        if (Objects.equals(getURI(), ((SimpleMutableFileMetadata) other).getURI())) {
            return true;
        }
        return false;
    }

    @Override
    public final int hashCode() {
        return Objects.hash(getURI());
    }

    public final boolean isSameFileData(SimpleMutableFileMetadata metadata) {
        if (metadata == null) {
            return false;
        }
        if (Objects.equals(fileName, metadata.fileName) &&
            Objects.equals(uri, metadata.uri) &&
            Objects.equals(description, metadata.description) &&
            Objects.equals(this.contentType, metadata.contentType) &&
            (number == metadata.number) &&
            (isReferenceToPersistedFile == metadata.isReferenceToPersistedFile) &&
            Objects.equals(contentId, metadata.contentId) &&
            Objects.equals(contentLocation, metadata.contentLocation) &&
            Objects.equals(contentBase, metadata.contentBase) &&
            Objects.equals(errorMessage, metadata.errorMessage) &&
            Objects.equals(extendedProperties, metadata.extendedProperties)) {
            return true;
        }
        return false;
    }

    @Nullable
    public final String getDisplayName() {
        return Objects.toString(displayName, getFilename());
    }

    public final void setDisplayName(@Nullable String displayName) {
        this.displayName = displayName;
    }

    @Nullable
    @Override
    public final String getFilename() {
        return fileName;
    }

    @Override
    public final void setFilename(@Nullable String fileName) {
        this.fileName = fileName;
    }

    @Nullable
    @Override
    public final URI getURI() {
        return uri;
    }

    @Override
    public final void setURI(@Nullable URI uri) {
        this.uri = uri;
    }

    @Nullable
    @Override
    public final String getDescription() {
        return description;
    }

    @Override
    public final void setDescription(@Nullable String description) {
        this.description = description;
    }

    @Nullable
    @Override
    public final String getContentType() {
        return contentType;
    }

    @Override
    public final void setContentType(@Nullable String contentType) {
        this.contentType = contentType;
    }

    @Override
    public final int getNumber() {
        return number;
    }

    /**
     * Sets the sequence number for this FileData, if it is possible to do so. The number can be set as long as this
     * FileData is not {@link #isReferenceToPersistedFile() a reference to a persisted file} which already has a
     * non-zero number. (This prevents accidentally re-numbering a file that has a persistent numeric identifier, such
     * as an existing attachment to an existing entry.)
     */
    @Override
    public final void setNumber(int number) {
        if (checkCanSetNumber(number)) {
            this.number = number;
        }
    }

    /**
     * Returns true if this FileData represents metadata for a file that is has been persisted, as opposed to a
     * temporary file resource.
     */
    @Override
    public final boolean isReferenceToPersistedFile() {
        return isReferenceToPersistedFile;
    }

    @Override
    public final void setReferenceToPersistedFile(boolean isReferenceToPersistedFile) {
        this.isReferenceToPersistedFile = isReferenceToPersistedFile;
    }

    @Override
    public final String getContentId() {
        return contentId;
    }

    @Override
    public final void setContentId(@Nullable String contentId) {
        this.contentId = contentId;
    }

    @Override
    public final String getContentLocation() {
        return contentLocation;
    }

    @Override
    public final void setContentLocation(@Nullable String contentLocation) {
        this.contentLocation = contentLocation;
    }

    @Override
    public final void setContentBase(@Nullable String contentBase) {
        this.contentBase = contentBase;
    }

    @Override
    public final void setResourceType(@Nullable FileResourceType resourceType) {
        this.resourceType = resourceType;
    }

    @Override
    public final String getContentBase() {
        return contentBase;
    }

    @Override
    public FileResourceType getResourceType() {
        return resourceType;
    }

    @Nonnull
    @Override
    public FileMetadata toReadOnly() {
        return new ReadOnlyView();
    }

    @Override
    public final void saveInstance(GetPutProperty namespace) {
        namespace.putAllProperties(getLoadSaveProperties());
    }

    @Nonnull
    @Override
    public final SimpleMutableFileMetadata mutableCopy() {
        SimpleMutableFileMetadata copy = createCopy(this);
        if (extendedProperties != null) {
            copy.extendedProperties = new HashMap<>(extendedProperties);
        }
        return copy;
    }

    private final GetPutProperty getBaseProperties() {
        return new BaseGetPutProperty();
    }

    public final GetPutProperty asGetPutProperty(FileResource fileResource) {
        return getExtendedProperties(fileResource).withDefaults(getBaseProperties());
    }

    private final GetPutProperty getExtendedProperties(FileResource fileResource) {
        return new ExtendedGetPutProperty(Suppliers.ofInstance(fileResource));
    }

    /**
     * Returns true if this FileData represents a place-holder for a file that could not be received or stored (e.g.,
     * when a temporary file would have been created for an upload, but the upload failed). This should generally
     * correspond to {@link TempFileResource#hadError()}.
     *
     * @return true if this FileData represents a place-holder for a file that could not be received; false otherwise.
     */
    public final boolean hasError() {
        if (errorMessage == null) {
            return false;
        }
        return true;
    }

    /**
     * Returns an error message if this FileData represents a place-holder for a file that could not be received (e.g.,
     * when a temporary file would have been created for an upload, but the upload failed). This should generally
     * correspond to {@link TempFileResource#getErrorMessage()}.
     *
     * @return an error message if this FileData represents a place-holder for a file that could not be received (e.g.,
     *     could not be stored as a temporary file).
     */
    public final String getErrorMessage() {
        return errorMessage;
    }

    public final void setErrorMessage(String error) {
        this.errorMessage = error;
    }

    private final HashMap<String,String> getExtendedProperties() {
        if (extendedProperties == null) {
            extendedProperties = new HashMap<>();
        }
        return extendedProperties;
    }

    @Override
    public final void setMissingMutableMetadata(FileMetadata metadata) {
        if (StringUtils.isBlank(getFilename())) {
            setFilename(metadata.getFilename());
        }
        if (StringUtils.isBlank(getDescription())) {
            setDescription(metadata.getDescription());
        }
        if (StringUtils.isBlank(getContentType())) {
            setContentType(metadata.getContentType());
        }
        if (getNumber() == 0) {
            setNumber(metadata.getNumber());
        }
        if (StringUtils.isBlank(getContentId())) {
            setContentId(metadata.getContentId());
        }
        if (StringUtils.isBlank(getContentLocation())) {
            setContentLocation(metadata.getContentLocation());
        }
        if (StringUtils.isBlank(getContentBase())) {
            setContentBase(metadata.getContentBase());
        }
    }

    private final boolean checkCanSetNumber(int number) {
        if (isReferenceToPersistedFile && number != this.number && this.number != 0) {
            LOGGER.warn(
                "Attempted to change the number of an existing persisted and numbered file",
                new IllegalArgumentException()
            );
            return false;
        }
        return true;
    }

    private final Iterator<String> getToStringProperties() {
        return getToStringPropertyNames().stream()
            .map(toStringProperty())
            .iterator();
    }

    private final Function<String,String> toStringProperty() {
        return toStringProperty(getBaseProperties().toReadOnly());
    }

    private final List<String> getToStringPropertyNames() {
        return ImmutableList.of(
            PROP_NAME_FILE_NAME, PROP_NAME_MIMETYPE, PROP_NAME_URI, PROP_NAME_NUMBER,
            PROP_NAME_REFERENCE_TO_PERSISTED_FILE, PROP_NAME_ERROR, PROP_NAME_CID, PROP_NAME_CONTENT_LOCATION,
            PROP_NAME_CONTENT_BASE
        );
    }

    private final GetPutProperty getLoadSaveProperties() {
        return new LoadSaveProperties();
    }

}
