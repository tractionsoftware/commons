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
import com.google.common.net.MediaType;
import com.tractionsoftware.commons.codec.MD5Util;
import com.tractionsoftware.commons.image.Icon;
import com.tractionsoftware.commons.image.ImageUtil;
import com.tractionsoftware.commons.net.MediaTypeUtil;
import com.tractionsoftware.commons.net.URLUtil;
import com.tractionsoftware.commons.text.NumberFormats;
import com.tractionsoftware.commons.util.Dimensions;
import org.apache.commons.io.LineIterator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.security.DigestInputStream;
import java.util.Date;
import java.util.Iterator;
import java.util.Objects;

/**
 * An abstraction for a file, including access to an {@link InputStream} that can be used to access the file's contents.
 * (Contrast this with {@link FileMetadata}, which encapsulates file metadata only.) The underlying file may be stored
 * in any medium or repository, and is not limited an ordinary file system.
 *
 * <p>
 * FileInfo objects are not intended to represent a dynamically generated resource, but there is nothing in this API
 * that rules that out. (The same may not be true of sub-interfaces.)
 *
 * <p>
 * FileInfo objects, like most other SDK objects, are only created on demand contingent upon the requesting user's
 * permission to access the underlying file, and should never be shared across request threads.
 *
 * @author Andy Keller, Dave Shepperton
 */
public interface FileResource {

    static final Logger LOGGER = LoggerFactory.getLogger(FileResource.class);

    /**
     * Returns true if this FileInfo is "valid." If a FileInfo instance is not valid, any method whose implementation
     * requires read or write access to the file's data or metadata, such as {@link #getInputStream()} or
     * {@link #getByteSize()}, may throw an Exception, or may return an value that would otherwise be invalid.
     *
     * <p>
     * The basic notion of validity requires that the file exists and its data and metadata are available. This may
     * involve checking whether the underlying file in a file system or file repository exists and can be read, or some
     * other implementation-dependent test. Subclasses representing special types of resources may override not just
     * this method but also this definition of validity.
     *
     * @return true if this FileInfo is "valid"; false otherwise.
     */
    public boolean isValid();

    public String getPath();

    /**
     * Returns the name of the file.
     */
    public String getFilename();

    /**
     * Returns true if this file is a directory.
     */
    public default boolean isDirectory() {
        return false;
    }

    /**
     * Returns an {@link InputStream} that can be used to access the contents of the underlying file.
     *
     * <p>
     * This should be available for all files, and implementations are responsible for wrapping any "raw" InputStream
     * objects as appropriate (e.g., with a {@link java.io.BufferedInputStream}) as may be necessary to optimize
     * performance.
     *
     * <p>
     * Although clients are responsible for closing all InputStreams retrieved via this method, in order to avoid
     * leaking file descriptors, implementations should take appropriate precautions to ensure that those instances are
     * still cleaned up when the FileInfo instance is being finalized.
     *
     * <p>
     * If the FileInfo implementation also supports write operations as well as read operations (e.g., via methods that
     * return {@link java.io.OutputStream}s or {@link java.io.Writer}s that can be used to modify the contents of the
     * underlying file resource), clients must dispose of the resources required for that write access before invoking
     * this method.
     *
     * @throws IOException
     *     if a problem is encountered while attempting to open the underlying file resource, or if the resource is a
     *     directory or other collection rather than a single file.
     * @throws IllegalStateException
     *     implementations must throw this Exception if the client has already obtained a {@link java.io.OutputStream},
     *     {@link java.io.Writer}, or other object that provides write access to the underlying file resource, and has
     *     not yet closed it.
     * @throws UnsupportedOperationException
     *     in some rare cases, if the resource may be {@link #isValid() valid}, but the implementation does not support
     *     directly reading the underlying data, possibly because it was never intended that should be required.
     */
    public SizedInputStream getInputStream() throws IOException;

    /**
     * Returns a {@link Reader} to read the underlying file's contents as text decoded using the {@link Charset} from
     * {@link #getCharset()}.
     *
     * <p>
     * This default implementation delegates to {@link #getReader(Charset)}, passing null. Subclasses should not need to
     * override it.
     *
     * <p>
     * Although clients are responsible for closing all Readers retrieved via this method, in order to avoid leaking
     * file descriptors, implementations should take appropriate precautions to ensure that those instances are still
     * cleaned up when the FileInfo instance is being finalized.
     *
     * @return a {@link Reader} to read underlying file's contents as text.
     * @throws IOException
     *     if one is raised while creating any necessary {@link InputStream} or {@link Reader} objects.
     * @throws IllegalStateException
     *     optionally if another method providing read/write access to the underlying file's contents has already been
     *     invoked and the resulting object has not yet been closed.
     * @throws UnsupportedOperationException
     *     optionally if this FileInfo is known to represent a non-text resource. Also in some rare cases, if the
     *     resource may be {@link #isValid() valid}, but the implementation does not support directly reading the
     *     underlying data, possibly because it was never intended that should be required. See
     *     {@link #getInputStream()}.
     */
    public default BufferedReader getReader() throws IOException {
        return getReader(null);
    }

    /**
     * Returns a {@link Reader} to read the underlying file's contents as text as decoded using the given
     * {@link Charset}.
     *
     * <p>
     * This default implementation delegates to {@link IOUtil#getBufferedReader(InputStream, Charset)}, passing
     * {@link #getInputStream() the InputStream}; and passing either the given Charset, or if that is null, the Charset
     * returned by {@link #getCharset() Charset} for this file resource. Subclasses should override it as necessary.
     *
     * <p>
     * Although clients are responsible for closing all Readers retrieved via this method, in order to avoid leaking
     * file descriptors, implementations should take appropriate precautions to ensure that those instances are still
     * cleaned up when the FileInfo instance is being finalized.
     *
     * @param charset
     *     a {@link Charset} to use in place of the one returned by {@link #getCharset()}. If the client knows the
     *     correct Charset to use to decode the text in this file, and suspects that {@link #getCharset()} may return
     *     the wrong value, it may be useful to supply the Charset, but most clients should use {@link #getReader()}.
     * @return a {@link Reader} to read underlying file's contents as text.
     * @throws IOException
     *     if one is raised while creating any necessary {@link InputStream} or {@link Reader} objects.
     * @throws IllegalStateException
     *     optionally if another method providing read/write access to the underlying file's contents has already been
     *     invoked and the resulting object has not yet been closed.
     * @throws UnsupportedOperationException
     *     optionally if this FileInfo is known to represent a non-text resource. Also, in some rare cases, if the
     *     resource may be {@link #isValid() valid}, but the implementation does not support directly reading the
     *     underlying data, possibly because it was never intended that should be required. See
     *     {@link #getInputStream()}.
     */
    public default BufferedReader getReader(Charset charset) throws IOException {
        return IOUtil.getBufferedReader(getInputStream(), Objects.requireNonNullElseGet(charset, this::getCharset));
    }

    /**
     * Returns the text from the underlying file, decoded using the file's {@link Charset} as per
     * {@link #getCharset()}.
     *
     * <p>
     * This default implementation delegates to {@link #readString(Charset)}, passing null for the Charset. Subclasses
     * should not need to override it.
     *
     * @return the text from the underlying file, decoded using the file's {@link Charset} as per {@link #getCharset()}.
     * @throws IOException
     *     if one is raised while attempting to read the file's contents.
     * @throws IllegalStateException
     *     optionally if another method providing read/write access to the underlying file's contents has already been
     *     invoked and the resulting object has not yet been closed.
     * @throws UnsupportedOperationException
     *     optionally if this FileInfo is known to represent a non-text resource. Also, in some rare cases, if the
     *     resource may be {@link #isValid() valid}, but the implementation does not support directly reading the
     *     underlying data, possibly because it was never intended that should be required. See
     *     {@link #getInputStream()}.
     */
    public default String readString() throws IOException {
        return readString(null);
    }

    /**
     * Returns the text from the underlying file, decoded using the given {@link Charset}.
     *
     * <p>
     * This default implementation gets a Reader from {@link #getReader(Charset)}, passing either the given Charset, or
     * if that is null, the Charset returned by {@link #getCharset() Charset} for this file resource. Subclasses should
     * override it as necessary.
     *
     * @param charset
     *     a {@link Charset} to use in place of the one returned by {@link #getCharset()}. If the client knows the
     *     correct Charset to use to decode the text in this file, and suspects that {@link #getCharset()} may return
     *     the wrong value, it may be useful to supply the Charset, but most clients should pass null for this argument
     *     or simply use {@link #readString()}.
     * @return the text from the underlying file, decoded using the given {@link Charset}.
     * @throws IOException
     *     if one is raised while attempting to read the file's contents.
     * @throws IllegalStateException
     *     optionally if another method providing read/write access to the underlying file's contents has already been
     *     invoked and the resulting object has not yet been closed.
     * @throws UnsupportedOperationException
     *     optionally if this FileInfo is known to represent a non-text resource. Also, in some rare cases, if the
     *     resource may be {@link #isValid() valid}, but the implementation does not support directly reading the
     *     underlying data, possibly because it was never intended that should be required. See
     *     {@link #getInputStream()}.
     */
    public default String readString(Charset charset) throws IOException {
        try (Reader reader = getReader(Objects.requireNonNullElseGet(charset, this::getCharset))) {
            return IOUtil.readContent(reader);
        }
    }

    /**
     * Returns a {@link DigestInputStream} that can be used to compute a digest for the contents of this file while
     * reading the contents.
     *
     * @return a {@link DigestInputStream} that can be used to compute a digest for the contents of this file while *
     *     reading it, if this file does not represent a directory; null otherwise.
     * @throws IOException
     *     if a problem is encountered while attempting to open the underlying file resource, or if the resource is a
     *     directory or other collection rather than a single file.
     * @throws IllegalStateException
     *     implementations must throw this Exception if the client has already obtained a {@link java.io.OutputStream},
     *     {@link java.io.Writer}, or other object that provides write access to the underlying file resource, and has
     *     not yet closed it.
     * @throws UnsupportedOperationException
     *     in some rare cases, if the resource may be {@link #isValid() valid}, but the implementation does not support
     *     directly reading the underlying data, possibly because it was never intended that should be required.
     * @see #getMD5()
     * @see #getMD5Hash()
     * @see #getPaddedMD5Hash()
     */
    public default DigestInputStream getDigestInputStream() throws IOException {
        return MD5Util.createDigestInputStream(this);
    }

    /**
     * Returns the MD5 hash bytes for the contents of this file; or, if the file is empty, its
     * {@link #getContentType() content type specification}.
     *
     * <p>
     * This default implementation uses {@link MD5Util#digest(FileResource)} and
     * {@link MD5Util.DigestResult#hashBytes()} on the result. Subclasses that can provide a more efficient
     * implementation should override it.
     *
     * @return if this File does not represent a directory, the MD5 hash bytes based on the contents of this file; or,
     *     if the file is empty, the MD5 hash bytes based on its {@link #getContentType() content type specification};
     *     null otherwise.
     * @see #getMD5Hash()
     * @see #getPaddedMD5Hash()
     */
    public default byte[] getMD5() {
        return MD5Util.digest(this).hashBytes();
    }

    /**
     * Computes a hash based on the contents of this file; or, if the file is empty, its
     * {@link #getContentType() content type specification}.
     *
     * <p>
     * This default implementation uses {@link MD5Util#digest(FileResource)} and
     * {@link MD5Util.DigestResult#hashString()} on the result. Subclasses that can provide a more efficient
     * implementation should override it.
     *
     * @return if this File does not represent a directory, a hash based on the contents of this file; or, if the file
     *     is empty, the {@link #getContentType() content type specification}; null otherwise.
     * @see #getMD5()
     * @see #getPaddedMD5Hash()
     */
    public default String getMD5Hash() {
        return MD5Util.digest(this).hashString();
    }

    /**
     * Identical to {@link #getMD5Hash()} except that the resulting string is formatted to match the formatting used by
     * most command line md5hash commands and similar utilities.
     *
     * <p>
     * This default implementation uses {@link MD5Util#digest(FileResource)} and
     * {@link MD5Util.DigestResult#paddedHashString()} on the result. Subclasses that can provide a more efficient
     * implementation should override it.
     *
     * @return the padded MD5 hash, similar to {@link #getMD5Hash()}.
     * @see #getMD5()
     * @see #getMD5Hash()
     */
    public default String getPaddedMD5Hash() {
        return MD5Util.digest(this).paddedHashString();
    }

    /**
     * Returns the {@link Charset} representing the encoding that should be used to decode the contents of this file as
     * text. The Charset returned by this method must be used by the {@link Reader} returned by {@link #getReader()}.
     *
     * <p>
     * This default implementation uses the "charset" parameter, if any, retrieved from
     * {@link #getContentType() this file resource's content type}, or {@link StandardCharsets#UTF_8 UTF-8} if no
     * charset is specified.
     *
     * @return the {@link Charset} representing the encoding that should be used to decode the contents of this file as
     *     text, if any; or null if the file resource's contents are known not to be textual.
     */
    public default Charset getCharset() {
        String contentTypeSpec = getContentType();
        if (contentTypeSpec != null) {
            try {
                return MediaType.parse(contentTypeSpec).charset().toJavaUtil().orElse(StandardCharsets.UTF_8);
            }
            catch (IllegalArgumentException e) {
                LOGGER.info("Can't parse content-type spec {}", contentTypeSpec);
            }
        }
        return StandardCharsets.UTF_8;
    }

    /**
     * Returns a published name for this file.
     *
     * <p>
     * This may be different from the logical file name from {@link #getFilename()}, but this default implementation
     * simply defers to that method. Subclasses that require a different value should override it.
     *
     * @return a published name for this file.
     */
    public default String getPublishedFilename() {
        return getFilename();
    }

    /**
     * Returns the extension from this File's file name (not including the dot).
     *
     * @return the extension from this File's file name.
     */
    public default String getExtension() {
        return FileNameUtil.getExtension(getFilename(), null);
    }

    /**
     * Returns a description of the file.
     */
    public String getDescription();

    /**
     * Returns the content-type of the file, if available.
     *
     * @return the content-type of the file, if available; null otherwise.
     */
    public String getContentType();

    /**
     * Returns a Content-ID to use for this file resource for at least the duration of the current request. Invocations
     * on other requests for a FileInfo object referring to the same underlying resource may return different values.
     *
     * @return a Content-ID to use for this file resource for at least the duration of the current request.
     * @throws UnsupportedOperationException
     *     in some rare cases, if the resource is {@link #isValid() valid}, but should not be included by CID.
     */
    public String getContentId();

    /**
     * Returns the size of the file in bytes, much like {@link java.io.File#length()}.
     *
     * <p>
     * This may require a request to the file system or a file repository service, which could be relatively slow
     * compared with other methods in this class.
     *
     * @return the size of the file in bytes, if it can be determined; or -1 (or some other negative number) otherwise.
     */
    public long getByteSize();

    /**
     * Returns an appropriately formatted representation of the size of this file in bytes as returned by
     * {@link #getByteSize()}.
     *
     * <ul>
     *
     * <li>If X is the size and X < 1024 bytes, it is returned with the size in bytes (B).
     *
     * <li>If 1024 bytes < X < 1024 kilobytes, it is returned with the size in kilobytes (KB).
     *
     * <li>If 1024 kilobytes < X, it is returned with the size in megabytes (MB).
     *
     * <li>If 1024 megabytes < X, it is returned with the size in gigabytes (GB).
     * </ul>
     *
     * @return an appropriately formatted representation of the size of this file in bytes as returned by
     *     {@link #getByteSize()}.
     */
    public default String getFormattedSize() {
        return NumberFormats.getFormattedByteSize(getByteSize());
    }

    /**
     * Returns a representation of the size of this file in bytes as returned by {@link #getByteSize()}, rendered
     * according to the number formatting rules for the current locale.
     *
     * <p>
     * For example, in English, there is a "," after every 3 digits; and in French, there is a " " (single space)
     * separator after every 3 digits.
     *
     * @return a representation of the size of this file in bytes as returned by {@link #getByteSize()}, rendered
     *     according to the number formatting rules for the current locale.
     * @see NumberFormats#getFormattedWholeNumber(long)
     */
    public default String getFormattedByteSize() {
        return NumberFormats.getFormattedWholeNumber(getByteSize());
    }

    public default boolean isEmpty() {
        if (getByteSize() == 0) {
            return true;
        }
        return false;
    }

    /**
     * Returns the last modified or creation date of this file, much like {@link java.io.File#lastModified()}.
     *
     * @return the last modified or creation date of this file, if such a {@link Date} is known; null otherwise.
     */
    public Date getLastModified();

    /**
     * Returns a {@link SimpleMutableFileMetadata} encapsulating the metadata for the file represented by this
     * FileInfo.
     *
     * @return a {@link SimpleMutableFileMetadata} encapsulating the metadata for the file represented by this FileInfo.
     */
    public FileMetadata getMetadata();

    /**
     * Returns a data: URI containing the content-type and Base64 encoded data representing the bytes contained in this
     * file resource, if available.
     *
     * @return a data: URI containing the content-type and Base64 encoded data representing the bytes contained in this
     *     file resource, if available; null otherwise.
     * @throws UnsupportedOperationException
     *     in some rare cases, if the resource may be {@link #isValid() valid}, but the implementation does not support
     *     directly reading the underlying data, possibly because it was never intended that should be required. See
     *     {@link #getInputStream()}.
     */
    public default String getDataUrl() {
        try {
            return URLUtil.getDataUrl(this);
        }
        catch (IOException e) {
            return null;
        }
    }

    /**
     * Returns true if this FileInfo represents a file that is backed by a persistent store, whether or not the resource
     * actually exists.
     *
     * <p>
     * The most common example of when this method would return false would be the case of a {@link TempFile}, but there
     * are other cases, such as the case of an external resource, whether or not TeamPage may have temporarily retrieved
     * it and cached it in memory.
     *
     * <p>
     * This default implementation returns true. Subclasses should override it if they represent file resources not
     * backed by data in a persistent store.
     *
     * @return true if this FileInfo represents a file that is backed by a persistent store; false otherwise.
     */
    public default boolean isPersistent() {
        return true;
    }

    /**
     * Returns a String representing the Base64 encoded bytes of this file's content.
     *
     * <p>
     * This implementation delegates to {@link URLUtil#getBase64EncodedStringForDataUrl(java.io.File)} using the
     * {@link InputStream} returned by {@link #getInputStream()}. Subclasses should not need to override it.
     *
     * @return a String representing the Base64 encoded bytes of this file's content, if this File instance represents a
     *     file and the file's contents can be read successfully; null otherwise.
     */
    public default String getBase64() {
        if (isDirectory()) {
            return null;
        }
        try (InputStream input = getInputStream()) {
            return URLUtil.getBase64EncodedStringForDataUrl(input);
        }
        catch (Exception e) {
            LOGGER.warn("Unable to Base64 encode the file {}", this, e);
        }
        return null;
    }

    /**
     * Returns true if this file appears to be an image.
     *
     * <p>
     * The default implementation is based upon whether either the file extension or content-type for this file are
     * known to be associated with images. Subclasses that can provide a more definite answer should override it.
     *
     * @return true if the file seems to be an image; false otherwise.
     */
    public default boolean isImage() {
        if (isDirectory()) {
            return false;
        }
        if (ImageUtil.isImage(getExtension(), getContentType())) {
            return true;
        }
        return false;
    }

    /**
     * Returns an unscaled {@link Icon} representing an interpretation of this file as an image.
     *
     * <p>
     * This may require a request to the file system or a file repository service, which could be relatively slow
     * compared with other methods in this class. Naturally, this method will not make sense for files that are not
     * images, and it is very likely that if {@link #isImage()} returns true that this method will return an Icon that
     * is not {@link Icon#isValid() valid}.
     *
     * <p>
     * This default implementation defers to {@link #getImage(Dimensions)}, passing null for the maximum
     * {@link Dimensions}. It should be suitable for all IconFile implementations.
     *
     * @return an unscaled {@link Icon} representing an interpretation of this file as an image, if possible; null
     *     otherwise.
     * @see #isImage()
     */
    public default Icon getImage() {
        return getImage(null);
    }

    /**
     * Returns a scaled {@link Icon} based on this IconFile, scaled if necessary to fit into the given
     * {@link Dimensions.Units#PIXELS}-denominated {@link Dimensions}, if any.
     *
     * @param maxDimensions
     *     an optional {@link Dimensions.Units#PIXELS}-denominated {@link Dimensions} that should limit the size of the
     *     returned {@link Icon}.
     * @return an {@link Icon} based on this IconFile, scaled if necessary to fit into the given
     *     {@link Dimensions.Units#PIXELS}-denominated {@link Dimensions}, if any; or null if no Icon can be directly
     *     created based upon this IconFile.
     */
    public Icon getImage(Dimensions<Integer> maxDimensions);

    public default String getIconStyleName() {
        return FileIconService.get().getIconStyleName(this);
    }

    public default Icon getIcon() {
        return getIcon(null);
    }

    /**
     * Returns an {@link Icon} representing a file icon for this file resource.
     *
     * <p>
     * This implementation simply delegates to {@code FileIconService.get().getIcon(this, maxDimensions)}. This should
     * be sufficient for most implementations, but can be overridden by subclasses as necessary.
     *
     * @param maxDimensions
     *     optional preferred maximum dimensions for the file icon.
     * @return an {@link Icon} for an image representing an icon for this file, if one can be determined; null
     *     otherwise.
     */
    public default Icon getIcon(Dimensions<Integer> maxDimensions) {
        return FileIconService.get().getIcon(this, maxDimensions);
    }

    /**
     * Returns true if this file's {@link #getContentType() content type} is a text type, ignoring any charset or other
     * optional parameters.
     *
     * <p>
     * The intention is that if this method returns true, methods like {@link #getReader()} can be used to obtain
     * character data from the file. This does not necessarily mean that the file is exactly a plain text document, but
     * rather just that it is expected to contain valid character data. Contrast with #isPlainText()
     *
     * <p>
     * This default implementation delegates to {@link MediaTypeUtil#isTextContentType(String)}, but subclasses may
     * override it as appropriate.
     *
     * @return true if this file's {@link #getContentType() content type} is a text type; false otherwise.
     */
    public default boolean isText() {
        return MediaTypeUtil.isTextContentType(getContentType());
    }

    /**
     * Returns true if this file's {@link #getContentType() content type} is plain text ("text/plain") ignoring any
     * charset or other optional parameters.
     *
     * <p>
     * Contrast this with {@link #isText()}.
     *
     * <p>
     * This default implementation delegates to {@link MediaTypeUtil#isTextPlainContentType(String)}, but subclasses may
     * override it as appropriate.
     *
     * @return true if this file's {@link #getContentType() content type} is a text type; false otherwise.
     */
    @Beta
    public default boolean isPlainText() {
        return MediaTypeUtil.isTextPlainContentType(getContentType());
    }

    /**
     * Returns true if this file's {@link #getContentType() content type} is HTML ("text/html"), ignoring any charset or
     * other optional parameters.
     *
     * <p>
     * This default implementation delegates to {@link MediaTypeUtil#isTextHtmlContentType(String)}, but subclasses may
     * override it as appropriate.
     *
     * @return true if this file's {@link #getContentType() content type} is HTML; false otherwise.
     */
    @Beta
    public default boolean isHtml() {
        return MediaTypeUtil.isTextHtmlContentType(getContentType());
    }

    /**
     * Returns an {@link Iterator} of Strings, each representing successive lines of text in this file. Clients should
     * invoke {@link #isText()} before invoking this method, or else risk getting invalid character data just like they
     * would in any other case when, e.g., attempting to interpret arbitrary binary data as character data.
     *
     * <p>
     * This default implementation returns a {@link LineIterator} backed by the {@link Reader} from
     * {@link #getReader()}. Subclasses may override it as necessary.
     *
     * @return an {@link Iterator} of Strings, each representing successive lines of text in this file. Some lines may
     *     be blank.
     * @throws IOException
     *     if one is raised attempting to access the contents of the file.
     */
    @Beta
    public default Iterator<String> getLineIterator() throws IOException {
        return new LineIterator(getReader());
    }

}
