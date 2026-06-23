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

package com.tractionsoftware.commons.processor;

import com.tractionsoftware.commons.io.*;
import jakarta.annotation.Nonnull;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Objects;

/**
 * A simple Result implementation backed by a {@link TempFileResource}. Instances of this class may be created via its
 * static factory methods, but in general those methods are invoked indirectly via a suitable
 * {@link TempFileResultProvider} instance.
 *
 * @author Dave Shepperton
 * @see TempFileResultProvider
 * @since 4.0
 */
public final class TempFileResult extends Result {

    /**
     * This is an interface for a simple helper object which provides data necessary to create TempFileResult
     * instances.
     *
     * @author Dave Shepperton
     */
    public static interface Helper {

        /**
         * The {@link TempFileResource.Factory factory} required to create {@link TempFileResource}s.
         *
         * @return {@link TempFileResource.Factory factory} required to create {@link TempFileResource}s.
         */
        @Nonnull
        public TempFileResource.Factory tempFiles();

        /**
         * A name associated with the purpose of the code that produced using this Helper. This is used as part of the
         * name of the temporary file on disk so as to make it easy to recognize the purpose of such files if they live
         * on after a request (do to an error or due to the debug writer having been enabled as a signal to preserve
         * these files).
         *
         * @return a name associated with the purpose of the code that produced this Helper.
         */
        public String getName();

        /**
         * A suggested extension for the name of the temporary file, presumably reflecting the type of data with which
         * the client code will populate the Result instance.
         *
         * @return a suggested extension for the name of the temporary file, if one is desired; null otherwise.
         */
        public String getSuggestedTempFileExtension();

        /**
         * The {@link Logger} representing the diagnostic logger associated with the code that produced this Helper, if
         * any. If this Logger instance is enabled at the time the TempFileResult instance is being cleaned up, the
         * underlying temporary file on disk will not be deleted, and message will be printed to this logger indicating
         * the name, including the path, of the file on disk, enabling a developer to access it for diagnostic
         * purposes.
         *
         * @return {@link Logger} representing the diagnostic logger associated with the code that produced this Helper,
         *     if any; null otherwise.
         */
        public Logger getLogger();

    }

    /**
     * Returns a new empty TempFileResult instance ready to be populated and later consumed. It will be guaranteed to be
     * readable and writable by the current thread, although no guarantees are made about how much space will be
     * available for write operations.
     *
     * @param helper
     *     the Helper object that provides important objects and values used to create the TempFileResult, including
     *     creating the underlying {@link TempFileResource}.
     * @return a new empty TempFileResult instance ready to be populated and later consumed.
     * @throws IOException
     *     if there is a problem instantiating the {@link TempFileResource} underlying the TempFileResult instance being
     *     created.
     */
    public static final TempFileResult getInstance(Helper helper) throws IOException {

        String ext = getExtension(helper.getSuggestedTempFileExtension());
        String fileName = FileNameUtil.getGoodNormalizedFileName(
            helper.getName() + FileNameUtil.EXTENSION_SEPARATOR_CHAR + ext, null, null, false
        );
        TempFileResource tempFile = helper.tempFiles().create(
            SimpleMutableFileMetadata.createFromFileName(fileName), null, helper.getLogger()
        );

        if (tempFile.hadError()) {
            throw new IOException("Temp file setup failed: " + tempFile.getErrorMessage());
        }

        return new TempFileResult(tempFile, helper.getLogger());

    }

    /**
     * Creates and returns a new TempFileResult instance wrapping an existing TempFile.
     *
     * @param tempFile
     *     the {@link TempFileResource} instance to be wrapped.
     * @param logger
     *     the {@link Logger} to be used for the new TempFileResult instance.
     * @return a new TempFileResult instance wrapping an existing {@link TempFileResource}.
     * @throws RuntimeException
     *     if the given {@link TempFileResource} instance is not valid.
     */
    public static final TempFileResult getInstanceForExistingTempFile(@Nonnull TempFileResource tempFile, Logger logger)
        throws RuntimeException {
        Objects.requireNonNull(tempFile, "temp file");
        if (tempFile.hadError()) {
            throw new RuntimeException("Invalid temp file: " + tempFile);
        }
        return new TempFileResult(tempFile, logger);
    }

    /**
     * A helper method to determine the final file extension to be used for a new {@link TempFileResource}.
     *
     * @param suggestedExtension
     *     the suggested file extension, or null if no file extension has been suggested.
     * @return either the concatenation of "tmp" and the trimmed version of the suggested file extension -- e.g.,
     *     "tmp.txt" -- or just "tmp" if no suggested file extension was provided.
     */
    private static final String getExtension(String suggestedExtension) {
        if (StringUtils.isBlank(suggestedExtension)) {
            return "tmp";
        }
        return "tmp" + FileNameUtil.EXTENSION_SEPARATOR_CHAR + suggestedExtension.trim();
    }

    /**
     * The underlying {@link TempFileResource} for this instance.
     */
    private final TempFileResource tempFile;

    /**
     * A logger for information about this instance. This main purpose of this logger is so that when cleaning up after
     * this instance, the temporary file can be left on disk if this logger is enabled.
     */
    private final Logger logger;

    /**
     * This field indicates that the underlying temporary file has been closed and deleted if necessary.
     */
    private volatile boolean released;

    /**
     * Constructs a TempFileResult using the given {@link TempFileResource} as the underlying data store or source, and
     * the given NamedLogWriter as a logger for information about this instance.
     *
     * @param tempFile
     *     the {@link TempFileResource} to use as the underlying data store or source.
     * @param logger
     *     the logger for information about this instance.
     */
    private TempFileResult(TempFileResource tempFile, Logger logger) {
        this.tempFile = tempFile;
        this.logger = logger;
        this.released = false;
    }

    /**
     * This implementation closes and deletes the {@link TempFileResource} if necessary.
     */
    @Override
    public synchronized final void release() {
        if (checkOrSetReleased()) {
            return;
        }
        releaseIfNecessary(true);
    }

    /**
     * Returns an InputStream for the underlying temporary file.
     */
    @Override
    protected final SizedInputStream getInputStream() throws IOException {
        if (released) {
            throw new IllegalStateException("This Result has already been released.");
        }
        return tempFile.getInputStream();
    }

    /**
     * Returns a TractionOutputStream for the underlying temporary file, which is managed by the
     * {@link TempFileResource}.
     */
    @Override
    protected final OutputStream getOutputStream() throws IOException {
        if (released) {
            throw new IllegalStateException("This Result has already been released.");
        }
        return tempFile.getOutputStream();
    }

    /**
     * After a TempFileResult has been populated, regardless of whether the populate operation was successful, the
     * streams for its underlying temporary file must be closed. If the populate operation did not complete
     * successfully, the location of this instance's underlying temporary file on disk will be written to Debug.log in
     * case someone wants to inspect its contents for diagnostic purposes.
     */
    @Override
    protected final void onPopulate(boolean success) {
        IOUtil.close(tempFile);
        if (!success) {
            logTempFileResultLocation(true);
        }
    }

    /**
     * After a TempFileResult has been successfully consumed, its underlying temporary file on disk should be deleted
     * unless it has an enabled NamedLogWriter in its debugWriter field, in which case the location of its underlying
     * temporary file on disk will be logged to that NamedLogWriter. If the consume operation did not complete
     * successfully, the location of this instance's underlying temporary file on disk will be written to Debug.log in
     * case someone wants to inspect its contents for diagnostic purposes
     */
    @Override
    protected final void onConsume(boolean success) {
        if (checkOrSetReleased()) {
            return;
        }
        releaseIfNecessary(success);
    }

    private final boolean checkOrSetReleased() {
        if (released) {
            // Nothing to do here.
            return true;
        }
        released = true;
        return false;
    }

    private final void releaseIfNecessary(boolean forSuccessfulConsumption) {
        if (forSuccessfulConsumption) {
            deleteTempFileIfDebuggingNotEnabled();
        }
        else {
            logTempFileResultLocation(true);
        }
    }

    /**
     * If this instance has an enabled NamedLogWriter in its debugWriter field, writes a message to that NamedLogWriter
     * indicating the location of its underlying temporary file on disk; otherwise, deletes the temporary file.
     */
    private final void deleteTempFileIfDebuggingNotEnabled() {
        if (logger.isDebugEnabled()) {
            logTempFileResultLocation(false);
            return;
        }
        tempFile.delete();
    }

    /**
     * Writes a message indicating the location of this instance's underlying temporary file on disk to the given
     * PrintWriter.
     *
     * @param forError
     *     if this is associated with an error.
     */
    private final void logTempFileResultLocation(boolean forError) {
        if (forError) {
            logger.error("See {}", tempFile);
        }
        else {
            logger.debug("See {}", tempFile);
        }
    }

}
