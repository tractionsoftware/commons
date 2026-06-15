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

import com.google.common.net.MediaType;
import com.tractionsoftware.commons.net.URLUtil;
import jakarta.annotation.Nullable;
import org.apache.commons.lang3.StringUtils;

import java.net.URI;
import java.util.Objects;
import java.util.function.Supplier;

public interface MutableFileMetadata extends FileMetadata {

    /**
     * Sets the logical public name of the file. This represents the name of the file so far as a user is concerned. It
     * is almost certain to differ from the URL, URI, file path, or other identifier that represents the underlying
     * resource.
     *
     * @param fileName
     *     the new logical name of the file.
     * @throws UnsupportedOperationException
     *     if the file name is considered a read-only property for this FileMetadata.
     */
    public void setFilename(@Nullable String fileName);

    /**
     * Sets the {@link URI} that defines the location of the file in a store of some sort. This may be a file: URI or
     * something else.
     *
     * @param uri
     *     a {@link URI} that defines the location of the file in a store of some sort. This may be a file: URI or
     *     something else.
     * @throws UnsupportedOperationException
     *     if the {@link URI} is a read-only property.
     */
    public void setURI(@Nullable URI uri);

    /**
     * Sets the {@link URI} that defines the location of the file in a store of some sort. This may be a file: URI or
     * something else.
     *
     * @param uriSpec
     *     the specification for {@link URI} that defines the location of the file in a store of some sort. This may be
     *     a file: URI or something else.
     * @throws UnsupportedOperationException
     *     if the {@link URI} is a read-only property.
     */
    public default void setURISpec(@Nullable String uriSpec) {
        if (uriSpec == null) {
            setURI(null);
        }
        else {
            setURI(URLUtil.tryToCreateUri(uriSpec));
        }
    }

    /**
     * Sets a text description of the file.
     *
     * @param description
     *     the new text description to use for this file.
     * @throws UnsupportedOperationException
     *     if the description is considered a read-only property for this FileMetadata.
     */
    public void setDescription(String description);

    /**
     * Sets the media type designation -- "Content-Type" or "mime type" -- that was associated with this file as it was
     * originally created. This may come from the "Content-Type" header in a MIME message part (i.e., an email
     * attachment), the "Content-Type" HTTP request header, or some other source. See:
     *
     * <ul>
     * <li><a href="https://tools.ietf.org/html/rfc1341">RFC 1341</a>
     * <li><a href="https://tools.ietf.org/html/rfc1049">RFC 1049</a>
     * <li><a href="https://www.iana.org/assignments/media-types/media-types.xhtml">IANA Media Types</a>
     * </ul>
     *
     * @param contentType
     *     the {@link MediaType} representing the "Content-Type" to be used for the file if one is known or can be
     *     determined for this FileMetadata; null otherwise.
     * @throws UnsupportedOperationException
     *     if the "Content-Type" is considered a read-only property for this FileMetadata.
     * @throws IllegalArgumentException
     *     if the given value is considered an invalid "Content-Type" for this FileMetadata.
     */
    public default void setContentType(MediaType contentType) {
        setContentType(Objects.toString(contentType, null));
    }

    /**
     * Sets the media type designation -- "Content-Type" or "mime type" -- that was associated with this file as it was
     * originally created. This may come from the "Content-Type" header in a MIME message part (i.e., an email
     * attachment), the "Content-Type" HTTP request header, or some other source. See:
     *
     * <ul>
     * <li><a href="https://tools.ietf.org/html/rfc1341">RFC 1341</a>
     * <li><a href="https://tools.ietf.org/html/rfc1049">RFC 1049</a>
     * <li><a href="https://www.iana.org/assignments/media-types/media-types.xhtml">IANA Media Types</a>
     * </ul>
     *
     * @param contentType
     *     the "Content-Type" to be used for the file if one is known or can be determined for this FileMetadata; null
     *     otherwise.
     * @throws UnsupportedOperationException
     *     if the "Content-Type" is considered a read-only property for this FileMetadata.
     * @throws IllegalArgumentException
     *     if the given value is considered an invalid "Content-Type" for this FileMetadata.
     */
    public void setContentType(String contentType);

    /**
     * Sets the serial number for the file in the context of a list of files. This is generally used for an assigned and
     * immutable numeric identifier for a file; more specifically, it usually corresponds to an attachment's ID.
     *
     * @param number
     *     the serial number to use for the file in the context of a list of files, or -1 to indicate that it has no
     *     such number.
     * @throws UnsupportedOperationException
     *     if the file's number is considered a read-only property for this FileMetadata.
     * @throws IllegalArgumentException
     *     if the given number is not a valid value for this FileMetadata.
     */
    public void setNumber(int number);

    /**
     * Sets the property whether this FileMetadata represents a reference to a "persisted" file, such as an attachment
     * or shared file that has been stored in the appropriate repository, as opposed to a temporary file.
     *
     * <p>
     * For some FileMetadata implementations, this is a read-only property so far as public SDK clients are concerned,
     * provided chiefly to support serialization and deserialization of lists of files via
     * {@link SimpleMutableFileMetadata} objects.
     *
     * @param isReference
     *     indicating whether this FileMetadata represents a reference to a "persisted" file, such as an attachment or
     *     shared file that has been stored in the appropriate repository, as opposed to a temporary file.
     * @throws UnsupportedOperationException
     *     if this is a read-only property for this FileMetadata.
     */
    public void setReferenceToPersistedFile(boolean isReference);

    /**
     * Sets the "Content-ID" header that was associated with this file as it was originally created from a MIME message
     * part (i.e., an email attachment). See <a href="https://tools.ietf.org/html/rfc2392">RFC 2392</a>.
     *
     * @param contentId
     *     the value of the "Content-ID" header that should be associated with this file.
     * @throws UnsupportedOperationException
     *     if the "Content-ID" is considered a read-only property for this FileMetadata.
     * @throws IllegalArgumentException
     *     if the given value is not considered valid for a "Content-ID" MIME header.
     */
    public void setContentId(String contentId);

    /**
     * Sets the "Content-Location" header that was associated with this file as it was originally created from a MIME
     * message part (i.e., an email attachment). See <a href="https://tools.ietf.org/html/rfc2557">RFC 2557</a>.
     *
     * @param contentLocation
     *     the value of the "Content-Location" header that should be associated with this file.
     * @throws UnsupportedOperationException
     *     if the "Content-Location" is considered a read-only property for this FileMetadata.
     * @throws IllegalArgumentException
     *     if the given value is not considered valid for a "Content-Location" MIME header.
     */
    public void setContentLocation(String contentLocation);

    /**
     * Sets the "Content-Base" header that was associated with this file as it was originally created from a MIME
     * message part (i.e., an email attachment). The "Content-Base" header is meant to serve as a base URL so that
     * relative URLs in the "Content-Location" header can be fully qualified. See <a
     * href="https://tools.ietf.org/html/rfc2110">RFC 2110</a>.
     *
     * @param contentBase
     *     the value of the "Content-Base" header that should be associated with this file.
     * @throws UnsupportedOperationException
     *     if the "Content-Base" is considered a read-only property for this FileMetadata.
     * @throws IllegalArgumentException
     *     if the given value is not considered valid for a "Content-Base" MIME header.
     */
    public void setContentBase(String contentBase);

    public default void ensureValidFilename() {
        ensureValidFilename(null);
    }

    public void setResourceType(FileResourceType resourceType);

    /**
     * Modifies the file name as necessary to make it "valid" and minimally normalized. This default implementation
     * delegates to {@link FileNameUtil#getMinimallyValidFileName(String, java.util.function.Supplier, String)} to
     * transform the {@link #getFilename() current file name}, passing the
     * {@link #getContentType() currently set Content-Type} and not requesting that a file extension be added to an
     * otherwise valid file name, and using {@link #setFilename(String)} to apply the result. It should be adequate for
     * all implementations.
     */
    public default void ensureValidFilename(Supplier<String> getDefaultBaseName) {
        setFilename(FileNameUtil.getMinimallyValidFileName(getFilename(), getDefaultBaseName, getContentType()));
    }

    /**
     * Sets the file name extension portion of the file name.
     *
     * <p>
     * This default implementation determines the new file name that incorporates the new file name extension, using
     * {@link FileNameUtil#getMinimallyValidFileName(String, Supplier, String)} to ensure the result will be valid, and
     * using {@link #setFilename(String)} to update the file name accordingly. It should be suitable for all
     * implementations.
     *
     * @param ext
     *     the new extension, or a blank or null String to remove the extension.
     */
    public default void setExtension(String ext) {
        String namePart = FileNameUtil.stripExtension(getFilename());
        if (StringUtils.isBlank(ext)) {
            // Remove the extension.
            setFilename(namePart);
        }
        else {
            // Ensure the file name is "minimally valid" even with the given extension.
            setFilename(
                FileNameUtil.getMinimallyValidFileName(
                    namePart + FileNameUtil.EXTENSION_SEPARATOR_CHAR + ext, null, getContentType()
                )
            );
        }
    }

    public void setMissingMutableMetadata(FileMetadata metadata);

}
