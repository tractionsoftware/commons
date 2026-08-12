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

import com.tractionsoftware.commons.lang.JavaUtil;
import com.tractionsoftware.commons.lang.Resource;
import com.tractionsoftware.commons.lang.ResourceUtil;
import jakarta.annotation.Nonnull;

import java.io.*;
import java.nio.charset.Charset;
import java.util.Objects;

/**
 * Common super-class for {@link TempFileResource} implementations.
 *
 * @author Dave Shepperton
 */
public abstract class AbstractTempFileResource extends FileMetadataBasedFileResource<MutableFileMetadata> implements
    TempFileResource {

    private static final IllegalStateException alreadyReturned(String name) {
        throw new IllegalStateException(name + " already returned.");
    }

    private JavaUtil.CleanupTargetWrapper<OutputStream> currentOutput;

    private JavaUtil.CleanupTargetWrapper<PrintWriter> currentPrintWriter;

    private JavaUtil.CleanupTargetWrapper<InputStreamTracker> inputStreams;

    private final long sizeLimit;

    protected AbstractTempFileResource(MutableFileMetadata metadata) {
        this(metadata, -1L);
    }

    protected AbstractTempFileResource(MutableFileMetadata metadata, long sizeLimit) {
        super(metadata);
        this.sizeLimit = sizeLimit;
    }

    @Override
    public final String toString() {
        return getClass().getSimpleName() +
               ":{" +
               getFilename() +
               " - " +
               getToStringIdentifier() +
               "}";
    }

    @Override
    public final void flush() throws IOException {
        if (currentPrintWriter != null) {
            currentPrintWriter.get().flush();
        }
        else if (currentOutput != null) {
            currentOutput.get().flush();
        }
    }

    @Override
    public final void close() throws IOException {

        RuntimeException firstError = null;
        try {
            closeInput();
        }
        catch (RuntimeException e) {
            firstError = e;
        }

        flushOutputQuietly();
        try {
            closeOutput();
        }
        catch (IOException e) {
            if (firstError != null) {
                e.addSuppressed(firstError);
            }
            throw e;
        }
        catch (RuntimeException e) {
            if (firstError != null) {
                firstError.addSuppressed(e);
                throw firstError;
            }
            throw e;
        }

    }

    private final void closeInput() {
        if (inputStreams != null) {
            inputStreams.close();
            inputStreams = null;
        }
    }

    private final void closeOutput() throws IOException {
        if (currentPrintWriter != null) {
            currentPrintWriter.close();
        }
        else if (currentOutput != null) {
            currentOutput.close();
        }
    }

    private final void flushOutputQuietly() {
        try {
            flush();
        }
        catch (IOException | RuntimeException e) {
            LOGGER.warn("Unexpected issue flushing output for {}", this, e);
        }
    }

    private final void closeOutputQuietly() {
        try {
            closeOutput();
        }
        catch (IOException | RuntimeException e) {
            LOGGER.warn("Unexpected issue closing output for {}", this, e);
        }
    }

    @Override
    public String getErrorMessage() {
        return null;
    }

    @Nonnull
    @Override
    public final SizedInputStream getInputStream() throws IOException {
        if (hasAnyInputStreams()) {
            checkNoOutputStreamX();
        }
        return new SizedInputStream(createInputStream()) {
            @Override
            public final long size() {
                return AbstractTempFileResource.this.getByteSize();
            }
        };
    }

    @Override
    public final BufferedReader getReader(Charset charset) throws IOException {
        return IOUtil.getBufferedReader(getInputStream(), Objects.requireNonNullElseGet(charset, this::getCharset));
    }

    @Override
    public final OutputStream getOutputStream() throws IOException {
        if (currentOutput == null) {
            checkNoPrintWriterX();
            checkNoOutputStreamX();
            checkNoInputStreamX();
            currentOutput = JavaUtil.CleanupTargetWrapper.create(this, createOutputStream());
        }
        return currentOutput.get();
    }

    @Override
    public final PrintWriter getUtf8PrintWriter() throws IOException {
        if (currentPrintWriter == null) {
            checkNoOutputStreamX();
            currentPrintWriter = JavaUtil.CleanupTargetWrapper.create(
                this, SingleThreadPrintWriter.createUtf8Instance(getOutputStream())
            );
        }
        return currentPrintWriter.get();
    }

    @Override
    public final boolean delete() {

        closeInput();
        closeOutputQuietly();

        try {
            doDelete();
            return true;
        }
        catch (IOException e) {
            LOGGER.warn("Failed to delete temporary file {}", this, e);
            return false;
        }

    }

    @Override
    public final void save() throws IOException {
        flush();
        closeOutputQuietly();
        doSave();
    }

    protected String getToStringIdentifier() {
        return getURI().toString();
    }

    protected abstract InputStream createRawInputStream() throws IOException;

    protected abstract OutputStream createRawOutputStream() throws IOException;

    protected abstract void doSave() throws IOException;

    /**
     * Actually delete the underlying resource if it still exists.
     *
     * @throws IOException
     *     if the operation was attempted but failed, but not when the operation was a no-op (e.g., if the underlying
     *     resource does not exist, possibly because it has already been deleted).
     */
    protected abstract void doDelete() throws IOException;

    /**
     * This is for the implementation to apply whatever changes have now been committed to the {@link OutputStream}
     * created by {@link #createRawOutputStream()}.
     */
    protected abstract void onRealOutputStreamClosed();

    protected Resource createTracker() {
        return ResourceUtil.NO_OP_RESOURCE;
    }

    private final InputStream createInputStream() throws IOException {
        if (inputStreams == null) {
            inputStreams = JavaUtil.CleanupTargetWrapper.create(
                this, new InputStreamTracker(toString(), this::createTracker)
            );
        }
        return inputStreams.get().createTrackedStream(this::createRawInputStream);
    }

    private final OutputStream createOutputStream() throws IOException {

        OutputStream output = createRawOutputStream();

        try {
            output = IOUtil.getSizeLimitingOutputStream(output, getSizeLimit(), true);
            LOGGER.debug("Opened OutputStream for {}", this);
            return IOUtil.getCloseNotifyingOutputStream(
                output, this::onBeforeCloseOutput, this::onAfterCloseOutput
            );
        }
        catch (RuntimeException | Error e) {
            IOUtil.close(output);
            throw e;
        }

    }

    private final void onBeforeCloseOutput() {
        if (currentPrintWriter != null) {
            currentPrintWriter.get().flush();
            currentPrintWriter = null;
        }
        currentOutput = null;
    }

    private final void onAfterCloseOutput() {
        LOGGER.debug("Closed OutputStream for {}", this);
        onRealOutputStreamClosed();
    }

    public final long getSizeLimit() {
        return sizeLimit;
    }

    protected final boolean hasAnyInputStreams() {
        if (inputStreams == null) {
            return false;
        }
        return inputStreams.get().hasAnyStreams();
    }

    protected final void checkNoInputStreamX() throws IllegalStateException {
        if (hasAnyInputStreams()) {
            throw alreadyReturned("InputStream");
        }
    }

    protected final void checkNoOutputStreamX() throws IllegalStateException {
        if (currentOutput != null) {
            throw alreadyReturned("OutputStream");
        }
    }

    protected final void checkNoPrintWriterX() throws IllegalStateException {
        if (currentPrintWriter != null) {
            throw alreadyReturned("PrintWriter");
        }
    }

    protected final boolean checkNoInputStreamQ() {
        if (hasAnyInputStreams()) {
            LOGGER.warn("InputStream still for {} open.", this, alreadyReturned("OutputStream"));
            return false;
        }
        return true;
    }

    protected final boolean checkNoOutputStreamQ() {
        if (currentOutput != null) {
            LOGGER.warn("OutputStream still for {} open.", this, alreadyReturned("OutputStream"));
            return false;
        }
        return true;
    }

    protected final boolean checkNoPrintWriterQ() {
        if (currentPrintWriter != null) {
            LOGGER.warn("PrintWriter still for {} open.", this, alreadyReturned("PrintWriter"));
            return false;
        }
        return true;
    }

}
