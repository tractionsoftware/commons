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

import com.google.common.annotations.Beta;
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
import com.tractionsoftware.commons.net.URLUtil;
import com.tractionsoftware.commons.properties.*;
import com.tractionsoftware.commons.text.NumberFormats;
import com.tractionsoftware.commons.text.SnippetUtil;
import com.tractionsoftware.commons.util.Dimensions;
import jakarta.annotation.Nonnull;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.net.URL;
import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * A generic and mutable implementation of {@link FileMetadata}.
 *
 * @author Andy Keller, Dave Shepperton
 */
public class SimpleMutableFileMetadata implements MutableFileMetadata, ComplexProperty {

    private static final Logger LOGGER = LoggerFactory.getLogger(SimpleMutableFileMetadata.class);

    /**
     * Used as the name of a property for the file name, in the context of {@link #saveInstance(GetPutProperty)},
     * {@link #LOADER the Loader}, and the {@link GetPutProperty} returned by {@link #asGetPutProperty(FileResource)}.
     */
    public static final String FNAME = "fname";

    /**
     * Used as the name of a property for the file name extension, in the context of the {@link GetPutProperty} returned
     * by {@link #asGetPutProperty(FileResource)}.
     */
    public static final String FEXTENSION = "fextension";

    /**
     * Used as the name of a property for the description, in the context of {@link #saveInstance(GetPutProperty)},
     * {@link #LOADER the Loader}, and the {@link GetPutProperty} returned by {@link #asGetPutProperty(FileResource)}.
     */
    public static final String DESC = "desc";

    /**
     * Used as the name of a property for the content type, in the context of {@link #saveInstance(GetPutProperty)},
     * {@link #LOADER the Loader}, and the {@link GetPutProperty} returned by {@link #asGetPutProperty(FileResource)}.
     */
    public static final String MIMETYPE = "mimetype";

    /**
     * Used as the name of a property for the file resource path, in the context of
     * {@link #saveInstance(GetPutProperty)}, {@link #LOADER the Loader}, and the {@link GetPutProperty} returned by
     * {@link #asGetPutProperty(FileResource)}. It has this name for historical reasons.
     *
     * <p>
     * Notice that this property does not necessarily represent a real local path in the host file system. See
     * {@link #getFileResourcePath()} and {@link #setFileResourcePath(String)}.
     */
    public static final String LOCALFNAME = "localfname";

    /**
     * Used as the name of a property for the file's serial number, in the context of
     * {@link #saveInstance(GetPutProperty)}, {@link #LOADER the Loader}, and the {@link GetPutProperty} returned by
     * {@link #asGetPutProperty(FileResource)}.
     */
    public static final String NUMBER = "number";

    /**
     * Used as the name of a property to indicate whether the FileData is a reference to a persisted file, in the
     * context of {@link #saveInstance(GetPutProperty)}, {@link #LOADER the Loader}, and the {@link GetPutProperty}
     * returned by {@link #asGetPutProperty(FileResource)}.
     */
    public static final String ISREF = "isref";

    /**
     * Used as the name of a property for the Content-ID, in the context of {@link #saveInstance(GetPutProperty)},
     * {@link #LOADER the Loader}, and the {@link GetPutProperty} returned by {@link #asGetPutProperty(FileResource)}.
     */
    public static final String CID = "cid";

    /**
     * Used as the name of a property for the Content-Location, in the context of {@link #saveInstance(GetPutProperty)},
     * {@link #LOADER the Loader}, and the {@link GetPutProperty} returned by {@link #asGetPutProperty(FileResource)}.
     */
    public static final String CLOC = "cloc";

    /**
     * Used as the name of a property for the Content-Base, in the context of {@link #saveInstance(GetPutProperty)},
     * {@link #LOADER the Loader}, and the {@link GetPutProperty} returned by {@link #asGetPutProperty(FileResource)}.
     */
    public static final String CBASE = "cbase";

    /**
     * Used as the name of a read-only property referring to a relative URL that can be used to refer to the file, in
     * the context of the {@link GetPutProperty} returned by {@link #asGetPutProperty(FileResource)}.
     */
    public static final String URL = "url";

    /**
     * Used as the name a read-only property referring to a formatted representation of the file's size, in the context
     * of the {@link GetPutProperty} returned by {@link #asGetPutProperty(FileResource)}.
     */
    public static final String SIZE = "size";

    public static final String BYTESIZE = "bytesize";

    public static final String ERROR = "error";

    public static final String IMAGE = "image";

    public static final String IMAGE_WIDTH = "imagewidth";

    public static final String IMAGE_HEIGHT = "imageheight";

    public static final String ICON_URL = "iconurl";

    public static final String ICON_WIDTH = "iconwidth";

    public static final String ICON_HEIGHT = "iconheight";

    public static final String DISPLAYNAME = "displayname";

    public static final ComplexProperty.Loader<SimpleMutableFileMetadata> LOADER = (GetProperty namespace) -> {
        if (!namespace.hasProperty(FNAME)) {
            // The file name is the only required property.
            return null;
        }
        SimpleMutableFileMetadata loaded = new SimpleMutableFileMetadata();
        loaded.getLoadSaveProperties().putAllProperties(namespace);
        return loaded;
    };

    private static final Set<String> LOAD_SAVE_BASE_PROPERTY_NAMES = ImmutableSet.of(
        FNAME, DESC, MIMETYPE, LOCALFNAME, ISREF, NUMBER, ERROR, DISPLAYNAME, CLOC
    );

    /**
     * Copies the source {@link FileMetadata} to another {@link MutableFileMetadata}.
     */
    public static void copy(FileMetadata source, MutableFileMetadata destination) {

        if (source == null || destination == null) {
            return;
        }

        destination.setFilename(source.getFilename());
        destination.setDescription(source.getDescription());
        destination.setContentType(source.getContentType());
        destination.setFileResourcePath(source.getFileResourcePath());
        destination.setReferenceToPersistedFile(source.isReferenceToPersistedFile());
        destination.setNumber(source.getNumber());
        destination.setContentId(source.getContentId());
        destination.setContentLocation(source.getContentLocation());
        destination.setContentBase(source.getContentBase());

    }

    /**
     * Creates a new FileData populated by copying all properties from the given {@link FileMetadata}.
     *
     * @param source
     *     the {@link FileMetadata} from which all properties should be copied.
     * @return a new FileData populated by copying all properties from the given {@link FileMetadata}, if a non-null
     *     instance has been specified, and its {@link FileMetadata#getFilename()} method returns a non-null value; null
     *     otherwise.
     */
    public static final SimpleMutableFileMetadata createCopy(FileMetadata source) {

        if (source == null) {
            return null;
        }

        String fname = source.getFilename();
        if (fname == null) {
            return null;
        }

        if (source instanceof SimpleMutableFileMetadata metadata) {
            return metadata.getCopy();
        }

        return makeCopy(source);

    }

    /**
     * Creates a new FileData by copying properties of the given {@link FileResource}. If it is a {@link FileMetadata},
     * this method defers to {@link #createCopy(FileMetadata)}. Otherwise, it copies properties in a straightforward
     * way.
     *
     * @param fileResource
     *     the {@link FileResource} whose properties should be used to create a {@link SimpleMutableFileMetadata}.
     * @return a new FileData representing the properties of the given {@link FileResource}.
     */
    public static final SimpleMutableFileMetadata createFromFileInfo(FileResource fileResource) {

        if (fileResource instanceof FileMetadata fileData) {
            return createCopy(fileData);
        }

        SimpleMutableFileMetadata ret =
            createFromFileNameAndContentType(fileResource.getFilename(), fileResource.getContentType());
        ret.setFileResourcePath(fileResource.getPath());

        try {
            ret.setContentId(fileResource.getContentId());
        }
        catch (UnsupportedOperationException e) {
            // This can happen in rare cases. Since we want to copy the Content-ID when it's present, we have to handle
            // this UnsupportedOperationException (basically ignoring it and moving on).
            if (LOGGER.isInfoEnabled()) {
                LOGGER.info("Content-ID not supported for " + fileResource, e);
            }
        }

        ret.setDescription(fileResource.getDescription());

        ret.setReferenceToPersistedFile(fileResource.isPersistent());

        return ret;

    }

    public static final SimpleMutableFileMetadata createForIconFileInfo(IconFileResource iconFile) {
        SimpleMutableFileMetadata ret = createFromFileInfo(iconFile);
        ret.setDisplayName(iconFile.getDisplayName());
        GetPutProperty props = ret.asGetPutProperty(iconFile);
        props.getProperty(SIZE);
        props.getProperty(BYTESIZE);
        props.getProperty(IMAGE_WIDTH);
        props.getProperty(IMAGE_HEIGHT);
        return ret;
    }

    private static final SimpleMutableFileMetadata makeCopy(FileMetadata source) {
        SimpleMutableFileMetadata copy = new SimpleMutableFileMetadata();
        SimpleMutableFileMetadata.copy(source, copy);
        return copy;
    }

    public static final SimpleMutableFileMetadata createFromFileNameAndContentType(String fileName, String contentType) {
        SimpleMutableFileMetadata ret = new SimpleMutableFileMetadata();
        ret.setFilename(fileName);
        ret.setContentType(contentType);
        return ret;
    }

    public static final SimpleMutableFileMetadata createFromFileName(String fileName) {
        SimpleMutableFileMetadata ret = new SimpleMutableFileMetadata();
        ret.setFilename(fileName);
        String ext = FileNameUtil.getExtension(fileName, null);
        if (StringUtils.isNotBlank(ext)) {
            String contentType = MediaTypeUtil.getContentTypeFromExtension(ext).toString();
            if (StringUtils.isNotBlank(contentType)) {
                ret.setContentType(contentType);
            }
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
    public static final SimpleMutableFileMetadata createInstanceForUnnamedResource(String suggestedFilename, String contentType, Supplier<? extends InputStream> inputSupplier) {

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
                contentType = MediaTypeUtil.getContentTypeFromExtension(fileExtension).toString();
            }
        }

        return createFromFileNameAndContentType(getDefaultFileName(suggestedFilename, fileExtension), contentType);

    }

    @Beta
    private static final String guessFileExtensionFromContents(Supplier<? extends InputStream> inputSupplier) {

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

    public static final String getDefaultFileName(String suggestedFileName, String fileExtension) {
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

    private final class ReadOnlyView implements FileMetadata {

        @Override
        public String getFilename() {
            return SimpleMutableFileMetadata.this.getFilename();
        }

        @Override
        public String getFileResourcePath() {
            return SimpleMutableFileMetadata.this.getFileResourcePath();
        }

        @Override
        public final String getDescription() {
            return SimpleMutableFileMetadata.this.getDescription();
        }

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

        @Override
        public final String getContentId() {
            return SimpleMutableFileMetadata.this.getContentId();
        }

        @Override
        public final String getContentLocation() {
            return SimpleMutableFileMetadata.this.getContentLocation();
        }

        @Override
        public final String getContentBase() {
            return SimpleMutableFileMetadata.this.getContentBase();
        }

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
                case FNAME -> SimpleMutableFileMetadata.this.fileName;
                case DESC -> SimpleMutableFileMetadata.this.description;
                case MIMETYPE -> SimpleMutableFileMetadata.this.contentType;
                case LOCALFNAME -> SimpleMutableFileMetadata.this.fileResourcePath;
                case ISREF -> NativeTypeConversion.booleanToString(
                    SimpleMutableFileMetadata.this.isReferenceToPersistedFile
                );
                case NUMBER -> Integer.toString(SimpleMutableFileMetadata.this.number);
                case CID -> SimpleMutableFileMetadata.this.contentId;
                case CLOC -> SimpleMutableFileMetadata.this.contentLocation;
                case CBASE -> SimpleMutableFileMetadata.this.contentBase;
                case ERROR -> SimpleMutableFileMetadata.this.errorMessage;
                case DISPLAYNAME -> SimpleMutableFileMetadata.this.displayName;
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
                base,
                Collections.unmodifiableSet(SimpleMutableFileMetadata.this.extendedProperties.keySet())
            );
        }

        @Override
        public final void putProperty(String name, String value) {

            switch (name) {

            case FNAME:
                SimpleMutableFileMetadata.this.fileName = value;
                break;

            case DESC:
                SimpleMutableFileMetadata.this.description = value;
                break;

            case MIMETYPE:
                SimpleMutableFileMetadata.this.contentType = value;
                break;

            case LOCALFNAME:
                SimpleMutableFileMetadata.this.fileResourcePath = value;
                break;

            case ISREF:
                SimpleMutableFileMetadata.this.isReferenceToPersistedFile =
                    NativeTypeConversion.stringToBoolean(value, false);
                break;

            case NUMBER:
                SimpleMutableFileMetadata.this.number = NativeTypeConversion.stringToInt(value, -1);
                break;

            case CID:
                SimpleMutableFileMetadata.this.contentId = value;
                break;

            case CLOC:
                SimpleMutableFileMetadata.this.contentLocation = value;
                break;

            case CBASE:
                SimpleMutableFileMetadata.this.contentBase = value;
                break;

            case ERROR:
                SimpleMutableFileMetadata.this.errorMessage = value;
                break;

            case DISPLAYNAME:
                SimpleMutableFileMetadata.this.displayName = value;
                break;

            case URL:
            case FEXTENSION:
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
                case DISPLAYNAME -> SimpleMutableFileMetadata.this.getDisplayName();
                case FNAME -> SimpleMutableFileMetadata.this.getFilename();
                case DESC -> SimpleMutableFileMetadata.this.getDescription();
                case MIMETYPE -> SimpleMutableFileMetadata.this.getContentType();
                case LOCALFNAME -> SimpleMutableFileMetadata.this.getFileResourcePath();
                case NUMBER -> Integer.toString(SimpleMutableFileMetadata.this.getNumber());
                case ISREF ->
                    NativeTypeConversion.booleanToString(SimpleMutableFileMetadata.this.isReferenceToPersistedFile());
                case CID -> SimpleMutableFileMetadata.this.getContentId();
                case CLOC -> SimpleMutableFileMetadata.this.getContentLocation();
                case CBASE -> SimpleMutableFileMetadata.this.getContentBase();
                case ERROR -> SimpleMutableFileMetadata.this.getErrorMessage();
                case FEXTENSION -> SimpleMutableFileMetadata.this.getExtension();
                case URL -> SimpleMutableFileMetadata.this.getUrl();
                case null, default -> null;
            };
        }

        @Override
        public final Set<String> getPropertyNames() {
            return ImmutableSet.of(
                FNAME, DESC, MIMETYPE, LOCALFNAME, ISREF, NUMBER, ERROR, DISPLAYNAME, FEXTENSION, URL
            );
        }

        @Override
        public final void putProperty(String name, String value) {
            switch (name) {
            case DISPLAYNAME -> SimpleMutableFileMetadata.this.setDisplayName(value);
            case FNAME -> SimpleMutableFileMetadata.this.setFilename(value);
            case DESC -> SimpleMutableFileMetadata.this.setDescription(value);
            case MIMETYPE -> SimpleMutableFileMetadata.this.setContentType(value);
            case LOCALFNAME -> SimpleMutableFileMetadata.this.setFileResourcePath(value);
            case NUMBER -> SimpleMutableFileMetadata.this.setNumber(NativeTypeConversion.stringToInt(
                value,
                SimpleMutableFileMetadata.this.number
            ));
            case ISREF ->
                SimpleMutableFileMetadata.this.setReferenceToPersistedFile(NativeTypeConversion.stringToBoolean(
                    value,
                    SimpleMutableFileMetadata.this.isReferenceToPersistedFile
                ));
            case CID -> SimpleMutableFileMetadata.this.setContentId(value);
            case CLOC -> SimpleMutableFileMetadata.this.setContentLocation(value);
            case CBASE -> SimpleMutableFileMetadata.this.setContentBase(value);
            case ERROR -> SimpleMutableFileMetadata.this.errorMessage = value;
            case FEXTENSION -> SimpleMutableFileMetadata.this.setExtension(value);
            case URL -> SimpleMutableFileMetadata.this.setUrl(value);
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
                case IMAGE -> NativeTypeConversion.booleanToString(SimpleMutableFileMetadata.this.appearsToBeImage());
                case SIZE, BYTESIZE, ICON_URL, ICON_WIDTH, ICON_HEIGHT, IMAGE_WIDTH, IMAGE_HEIGHT ->
                    SimpleMutableFileMetadata.this.getExtendedProperties()
                        .computeIfAbsent(name, this::computeExtendedProperty);
                case null -> null;
                default -> getOtherExtendedProperty(name);
            };
        }

        @Override
        public final void putProperty(String name, String value) {
            switch (name) {
            case SIZE, BYTESIZE, ICON_URL, ICON_WIDTH, ICON_HEIGHT, IMAGE, IMAGE_WIDTH, IMAGE_HEIGHT ->
                // not supported
                LOGGER.warn(
                    "FileData putProperty does not support setting the " + name + " property.",
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
                SIZE, IMAGE, IMAGE_WIDTH, IMAGE_HEIGHT, ICON_URL, ICON_WIDTH, ICON_HEIGHT
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
                case SIZE -> NumberFormats.getFormattedByteSize(byteSize());
                case BYTESIZE -> Long.toString(byteSize());
                case ICON_URL -> iconUrl.get();
                case ICON_WIDTH -> Integer.toString(icon.get().getWidth());
                case ICON_HEIGHT -> Integer.toString(icon.get().getHeight());
                case IMAGE_WIDTH -> imageDimensions().getWidth().toString();
                case IMAGE_HEIGHT -> imageDimensions().getHeight().toString();
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

        private final Dimensions<Integer> readImageDimensions() {
            FileResource fileResource = getFileInfo.get();
            if (fileResource == null) {
                return Dimensions.getInvalidInstanceInPixels();
            }
            if (fileResource instanceof IconFileResource iconFile) {
                return iconFile.getOriginalDimensions();
            }
            return fileResource.getImage().getDimensions();
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
            return propName + "=" + SnippetUtil.truncate(propValue, 100);
        };
    }

    /**
     * See {@link #getFilename()} and {@link #setFilename(String)}.
     */
    private String fileName = null;

    /**
     * See {@link #getFileResourcePath()} and {@link #setFileResourcePath(String)}.
     */
    private String fileResourcePath = null;

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
            FNAME, MIMETYPE, LOCALFNAME, NUMBER, ISREF, ERROR, CID, CLOC, CBASE
        );
    }

    @Override
    public final boolean equals(Object other) {
        if (!(other instanceof SimpleMutableFileMetadata)) {
            return false;
        }
        if (Objects.equals(getFileResourcePath(), ((SimpleMutableFileMetadata) other).getFileResourcePath())) {
            return true;
        }
        return false;
    }

    @Override
    public final int hashCode() {
        return Objects.hash(getFileResourcePath());
    }

    public final boolean isSameFileData(SimpleMutableFileMetadata metadata) {
        if (metadata == null) {
            return false;
        }
        if (Objects.equals(fileName, metadata.fileName) &&
            Objects.equals(fileResourcePath, metadata.fileResourcePath) &&
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

    public final String getDisplayName() {
        return Objects.toString(displayName, getFilename());
    }

    public final void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    @Override
    public final String getFilename() {
        return fileName;
    }

    @Override
    public final void setFilename(String fileName) {
        this.fileName = fileName;
    }

    @Override
    public final String getFileResourcePath() {
        return fileResourcePath;
    }

    @Override
    public final void setFileResourcePath(String fileResourcePath) {
        this.fileResourcePath = FileNameUtil.platformIndependentPath(fileResourcePath);
    }

    @Override
    public final String getDescription() {
        return description;
    }

    @Override
    public final void setDescription(String description) {
        this.description = description;
    }

    @Override
    public final String getContentType() {
        return contentType;
    }

    @Override
    public final void setContentType(String contentType) {
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
    public final void setContentId(String contentId) {
        this.contentId = contentId;
    }

    @Override
    public final String getContentLocation() {
        return contentLocation;
    }

    @Override
    public final void setContentLocation(String contentLocation) {
        this.contentLocation = contentLocation;
    }

    @Override
    public final String getContentBase() {
        return contentBase;
    }

    @Override
    public FileMetadata toReadOnly() {
        return new ReadOnlyView();
    }

    @Override
    public final void setContentBase(String contentBase) {
        this.contentBase = contentBase;
    }

    @Override
    public final void saveInstance(GetPutProperty namespace) {
        namespace.putAllProperties(getLoadSaveProperties());
    }

    private final GetPutProperty getLoadSaveProperties() {
        return new LoadSaveProperties();
    }

    public final void copyTo(MutableFileMetadata copy) {
        copy(this, copy);
    }

    public final SimpleMutableFileMetadata getCopy() {
        SimpleMutableFileMetadata copy = makeCopy(this);
        if (extendedProperties != null) {
            copy.extendedProperties = new HashMap<>(extendedProperties);
        }
        return copy;
    }

    private final GetPutProperty getBaseProperties() {
        return new BaseGetPutProperty();
    }

    public final GetPutProperty asGetPutProperty(FileResource fileResource) {
        return getBaseProperties().withDefaults(new ExtendedGetPutProperty(Suppliers.ofInstance(fileResource)));
    }

    /**
     * Returns true if this FileData represents a place-holder for a file that could not be received or stored (e.g.,
     * when a temporary file would have been created for an upload, but the upload failed). This should generally
     * correspond to {@link TempFile#hadError()}.
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
     * correspond to {@link TempFile#getErrorMessage()}.
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

    /**
     * Returns the URL for the file, which will be the same as the {@link #getFileResourcePath() file resource path}.
     *
     * @return the URL for the file, which will be the same as the {@link #getFileResourcePath() file resource path}.
     */
    public final String getUrl() {
        return URLUtil.getUrlEncoding(fileResourcePath);
    }

    /**
     * Sets the URL for this FileData. Only the path component of the URL will be used, and it will be URL decoded.
     *
     * @param url
     *     the new URL to be applied.
     */
    public final void setUrl(String url) {
        if (url == null) {
            setFileResourcePath(null);
        }
        else {
            setFileResourcePath(URLUtil.getUrlDecoded(URLUtil.getPath(url)));
        }
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
        if (getNumber() < 0) {
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

}
