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

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import org.apache.commons.io.function.IOSupplier;
import org.slf4j.Logger;

import java.io.*;

/**
 * A simple interface representing a temporary file, with support for reading, writing, moving, deleting, etc.
 *
 * <p>
 * TempFiles do not guarantee support for concurrent access to both an {@link InputStream} and an {@link OutputStream}
 * for the same instance; nor to concurrent access to two different objects that both provide read or write access to
 * the temp file's contents.
 *
 * <p>
 * TempFiles, like other {@link FileResource}s, should be assumed to created only on demand contingent upon various
 * factors, and should never be shared across threads.
 */
public interface TempFile extends FileResource, Flushable, Closeable {

    public static interface Factory {

        @Nonnull
        public TempFile create(@Nonnull FileMetadata metadata, @Nullable Logger logger) throws IOException;

        @Nonnull
        public TempFile createWithContent(@Nonnull IOSupplier<? extends InputStream> getContent, @Nonnull FileMetadata metadata, @Nullable Logger logger)
            throws IOException;

    }

    /**
     * {@link TempFile}s are the canonical example of a non-persistent file resource, so this implementation returns
     * false.
     */
    @Override
    public default boolean isPersistent() {
        return false;
    }

    /**
     * Deletes the underlying file associated with this TempFile, if it exists, and releases any resources (e.g.,
     * memory, disk space, or a row in a database) that it had been occupying. This method will first close any open
     * resources ({@link InputStream}s, {@link OutputStream}s, etc.) before attempting to delete the file, as though the
     * {@link #close()} method had been invoked.
     *
     * <p>
     * This method is idempotent, and will not return false or raise any unexpected exceptions if it is invoked multiple
     * times for the same TempFile instance, or the same underlying resource.
     *
     * @return false if there was an error that prevented the deletion from being completed; true otherwise, including
     *     if the deletion was a no-op because the underlying resource has already been deleted.
     */
    public boolean delete();

    /**
     * Moves the contents of this temporary file to the destination represented by the given {@link File}, which may
     * represent either the exact file or a directory to which the file should be added.
     *
     * @param destination
     *     the destination. If this file exists and is a directory, the temporary file will be placed inside the
     *     directory, with the same name it currently has. Otherwise, the temporary file will be placed in the exact
     *     given file location (including if the target File exists but is not a directory).
     * @throws IOException
     *     if one is raised while attempting to move the temporary file into the given location.
     * @see FileUtil#getMoveDestination(File, String)
     */
    public abstract void move(File destination) throws IOException;

    /**
     * Saves any changes that have been made to this temporary file's properties using the set* methods, or writing to
     * its {@link OutputStream}. Clients that are modifying the TempFile's content or metadata should expect that it may
     * be necessary to invoke this method before the result of {@link #getMetadata()} and certain other methods will
     * reflect all the changes. Clients should also expect that invoking this method will have as a side effect the same
     * result as invoking {@link #close()}.
     *
     * @throws IOException
     *     if there is a problem committing any changes.
     */
    public abstract void save() throws IOException;

    /**
     * Returns true if the temp file actually exists.
     *
     * @return true if the temp file actually exists.
     */
    public abstract boolean exists();

    /**
     * Returns the error message, if any, associated with the attempt to create this temporary file, or to automatically
     * populate it (e.g., with the contents of an upload or received email attachment).
     *
     * @return the error message, if any, associated with the attempt to create this temporary file, or to automatically
     *     populate it (e.g., with the contents of an upload or received email attachment).
     */
    public abstract String getErrorMessage();

    /**
     * Returns true if the attempt to create this TempFile failed.
     *
     * @return true if the attempt to create this TempFile failed.
     */
    public default boolean hadError() {
        if (getErrorMessage() == null) {
            return false;
        }
        return true;
    }

    /**
     * Returns an {@link OutputStream} that allows the caller to write into the TempFile.
     *
     * <p>
     * Multiple invocations of this method will always return the same OutputStream until that stream is closed. This is
     * for the sake of convenience: if multiple methods need to write data to the OutputStream for a single TempFile,
     * the TempFile can just be passed around. Contrast this with {@link #getInputStream()}, which must produce a new
     * {@link InputStream} on each invocation.
     *
     * @return an {@link OutputStream} that allows the caller to write into the TempFile.
     * @throws IOException
     *     if one is raised while attempting to create the necessary {@link OutputStream}.
     * @throws IllegalStateException
     *     if another method has already been invoked that provides write access to the file (e.g., another call to
     *     {@code getOutputStream()}) and the resulting object has not been closed.
     */
    public abstract OutputStream getOutputStream() throws IOException;

    /**
     * Returns a {@link PrintWriter} that can be used to write UTF-8 text to this TempFile.
     *
     * <p>
     * Multiple invocations of this method will always return the same PrintWriter until that PrintWriter is closed.
     * This is for the sake of convenience, in case multiple methods need to write to the PrintWriter for a single
     * TempFile.
     *
     * @return a {@link PrintWriter} that can be used to write UTF-8 text to this TempFile.
     * @throws IOException
     *     if one is raised while attempting to create the necessary {@link OutputStream} for the {@link PrintWriter}.
     * @throws IllegalStateException
     *     if another method has already been invoked that provides write access to the file (e.g., another call to
     *     {@code getUtf8PrintWriter()}) and the resulting object has not been closed.
     */
    public abstract PrintWriter getUtf8PrintWriter() throws IOException;

    /**
     * Returns a path that can be used to refer to this temp file, likely not containing
     * {@link #getFilename() its logical file name}. It should not reflect details of the exact underlying storage
     * location, such as a real path to a file.
     *
     * @return a path that can be used to refer to this temp file, likely not containing
     *     {@link #getFilename() its logical file name}.
     */
    public abstract String getTempFilePath();

    /**
     * This method must flush the {@link OutputStream}s, if any, associated with this TempFile.
     */
    @Override
    public abstract void flush() throws IOException;

    /**
     * This method must close any open {@link InputStream} and {@link OutputStream}s associated with this TempFile, but
     * must NOT prevent additional read and write access to this TempFile via methods such as {@link #getInputStream()}
     * or {@link #getOutputStream()}.
     */
    @Override
    public abstract void close() throws IOException;

    public String getFileResourcePath();

    public void setFilename(String fileName);

    public int getNumber();

    public void setNumber(int number);

    public void setDescription(String description);

    public void setContentType(String contentType);

    public void setContentLocation(String contentLocation);

    public String getContentLocation();

    public String getContentBase();

    public void setMissingMutableMetadata(FileMetadata metadata);

}
