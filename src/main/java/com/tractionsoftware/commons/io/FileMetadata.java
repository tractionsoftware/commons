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

import com.tractionsoftware.commons.image.ImageUtil;
import jakarta.annotation.Nonnull;

import java.net.URI;
import java.util.Objects;

/**
 * Represents a collection of logical metadata for a file resource. This includes properties such as the file's publicly
 * advertised name, its media type (i.e., "Content-Type" or "mime type"), various "Content-*" headers that are
 * associated with an original MIME part (i.e., email attachment) from which the file was created, whether it has been
 * "persisted" to its final storage location or is a temporary file, and other properties. The underlying file may be a
 * file on disk, or in any other repository.
 *
 * <p>
 * Contrast with {@link FileResource} which provides access to the actual file (i.e., the file's actual data, wherever
 * that may be stored).
 *
 * @author Andy Keller, Dave Shepperton
 * @see MutableFileMetadata
 * @see SimpleMutableFileMetadata
 */
public interface FileMetadata {

    /**
     * Returns the public name for the file resource. It must be a valid cross-platform file name, even if it does not
     * correspond to the name of a real file on disk.
     *
     * @return the public name for the file resource.
     */
    public String getFilename();

    /**
     * Returns the {@link URI} that defines the location of the file in a store of some sort. This may be a file: URI or
     * something else.
     *
     * @return the {@link URI} that defines the location of the file in a store of some sort. This may be a file: URI or
     *     something else.
     */
    public URI getURI();

    /**
     * Returns the specification for the {@link URI} that defines the location of the file in a store of some sort. This
     * may be a file: URI or something else.
     *
     * @return the specification {@link URI} that defines the location of the file in a store of some sort. This may be
     *     a file: URI or something else.
     */
    public default String getURISpec() {
        return Objects.toString(getURI(), null);
    }

    /**
     * Returns a text description of the file.
     *
     * @return a text description of the file, if one is available; null otherwise.
     */
    public String getDescription();

    /**
     * Returns the media type designation -- "Content-Type" or "mime type" -- that was associated with this file as it
     * was originally created. This may come from the "Content-Type" header in a MIME message part (i.e., an email
     * attachment), the "Content-Type" HTTP request header, or some other source. If no media type was specified at the
     * time the file was being created, this method may still return a value if a media type was automatically
     * determined when the file was being persisted. See:
     *
     * <ul>
     * <li><a href="https://tools.ietf.org/html/rfc1341">RFC 1341</a>
     * <li><a href="https://tools.ietf.org/html/rfc1049">RFC 1049</a>
     * <li><a href="https://www.iana.org/assignments/media-types/media-types.xhtml">IANA Media Types</a>
     * </ul>
     *
     * @return the "Content-Type" for the file if one is known or can be determined for this FileMetadata; null
     *     otherwise.
     */
    public String getContentType();

    /**
     * Returns the serial number for the file in the context of a list of files. This is generally used for an assigned
     * and immutable numeric identifier for a file; more specifically, it usually corresponds to an attachment's ID.
     *
     * @return the serial number for the file in the context of a list of files; -1 otherwise.
     */
    public int getNumber();

    /**
     * Returns true if this FileMetadata represents a reference to a "persisted" file, such as an attachment or shared file
     * that has been stored in the appropriate repository, as opposed to a temporary file.
     *
     * @return true if this FileMetadata represents a reference to a "persisted" file; false otherwise.
     */
    public boolean isReferenceToPersistedFile();

    /**
     * Returns true if this FileMetadata represents a reference to a temp file. This is as opposed to a "persisted" file,
     * such as an attachment or shared file that has been stored in the appropriate repository.
     *
     * <p>
     * This default implementation returns {@code !isReferenceToPersistedFile()}, which must always be true by
     * definition. Subclasses should not generally override it.
     *
     * @return true if this FileMetadata represents a reference to a temp file; false otherwise.
     */
    public default boolean isReferenceToTempFile() {
        return !isReferenceToPersistedFile();
    }

    /**
     * If this file came from a MIME message part (i.e., an email attachment), this method returns the value of the
     * "Content-ID" header for that part. See <a href="https://tools.ietf.org/html/rfc2392">RFC 2392</a>.
     *
     * @return the value of the "Content-Id" header associated with this file, if one has been specified; null
     *     otherwise.
     */
    public String getContentId();

    /**
     * If this file came from a MIME message part (i.e., an email attachment), this method returns the value of the
     * "Content-Location" header for that part. See <a href="https://tools.ietf.org/html/rfc2557">RFC 2557</a>.
     *
     * @return the value of the "Content-Location" header associated with this file, if one has been specified; null
     *     otherwise.
     */
    public String getContentLocation();

    /**
     * Returns the "Content-Base" header that was associated with this file as it was originally created from a MIME
     * message part (i.e., an email attachment). The "Content-Base" header is meant to serve as a base URL so that
     * relative URLs in the "Content-Location" header can be fully qualified. See <a
     * href="https://tools.ietf.org/html/rfc2110">RFC 2110</a>.
     *
     * @return the "Content-Base" header that was associated with this file as it was originally created from a MIME
     *     message part (i.e., an email attachment).
     */
    public String getContentBase();

    /**
     * Returns true if this {@link FileMetadata} has a minimally valid file name.
     *
     * @return true if this {@link FileMetadata} has a minimally valid file name.
     * @see FileNameUtil#isMinimallyValidFileName(String)
     */
    public default boolean hasValidFileName() {
        return FileNameUtil.isMinimallyValidFileName(getFilename());
    }

    /**
     * Returns the file name extension, if any, based upon {@link #getFilename() the currently set file name}.
     *
     * <p>
     * This default implementation defers to {@link FileNameUtil#getExtension(String, String)}, which should be suitable
     * for all implementations.
     *
     * @return the file name extension, if any, based upon {@link #getFilename() the currently set file name}, if any;
     *     null or blank otherwise.
     */
    public default String getExtension() {
        return FileNameUtil.getExtension(getFilename(), null);
    }

    public default boolean appearsToBeImage() {
        if (ImageUtil.isImage(getExtension(), getContentType())) {
            return true;
        }
        return false;
    }

    public FileResourceType getResourceType();

    /**
     * Returns a view of this FileMetadata that offers read-only access to properties.
     *
     * @return a FileMetadata that provides a view of this FileMetadata that offers read-only access to properties.
     */
    @Nonnull
    public FileMetadata toReadOnly();

    /**
     * Creates a new {@link MutableFileMetadata} populated by copying all properties from this FileMetadata.
     *
     * @return a new {@link MutableFileMetadata} populated by copying all properties from this FileMetadata.
     */
    public default MutableFileMetadata mutableCopy() {
        return SimpleMutableFileMetadata.createCopy(this);
    }

    /**
     * Copies the source {@link FileMetadata} to an {@link MutableFileMetadata}.
     *
     * @param destination
     *     a {@link MutableFileMetadata} to which the metadata from this FileMetadata will be copied.
     */
    public default void copyTo(MutableFileMetadata destination) {

        if (destination == null) {
            return;
        }

        destination.setFilename(getFilename());
        destination.setDescription(getDescription());
        destination.setContentType(getContentType());
        destination.setURI(getURI());
        destination.setReferenceToPersistedFile(isReferenceToPersistedFile());
        destination.setNumber(getNumber());
        destination.setContentId(getContentId());
        destination.setContentLocation(getContentLocation());
        destination.setContentBase(getContentBase());

    }

}
