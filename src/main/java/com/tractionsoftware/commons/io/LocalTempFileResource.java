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

import java.io.*;
import java.util.Date;
import java.util.Objects;

final class LocalTempFileResource extends AbstractTempFileResource implements TempFileResource {

    private final File file;

    LocalTempFileResource(File file, MutableFileMetadata metadata, long sizeLimit) {
        super(metadata, sizeLimit);
        this.file = file;
    }

    @Override
    public final boolean equals(Object other) {
        if (other instanceof LocalTempFileResource otherFile) {
            return file.equals(otherFile.file);
        }
        return false;
    }

    @Override
    public final int hashCode() {
        return Objects.hash(file);
    }

    @Override
    public final boolean isValid() {
        if (FileUtil.isValidFileForRead(file)) {
            return true;
        }
        return false;
    }

    @Override
    protected final void doDelete() throws IOException {
        FileUtil.DeleteRequestStatus result = FileUtil.deleteOrDeleteOnExit(file);
        if (result.failed()) {
            throw new IOException("The file " + file + " could not be deleted (" + result + ")");
        }
    }

    @Override
    protected final void doSave() {
        // Nothing to do here.
    }

    @Override
    public final boolean exists() {
        return file.exists();
    }

    @Nonnull
    @Override
    public final Date getLastModified() {
        return new Date(file.lastModified());
    }

    @Override
    public final long getByteSize() {
        return file.length();
    }

    @Override
    protected final InputStream createRawInputStream() throws IOException {
        return FileUtil.getBufferedInputStream(file);
    }

    @Override
    protected final OutputStream createRawOutputStream() throws IOException {
        return FileUtil.getBufferedOutputStream(file);
    }

    @Override
    protected final void onRealOutputStreamClosed() {
        // Nothing to do here.
    }

}
