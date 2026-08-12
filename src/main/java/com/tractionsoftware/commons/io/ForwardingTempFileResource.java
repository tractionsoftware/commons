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

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;

public abstract class ForwardingTempFileResource extends ForwardingFileResource implements TempFileResource {

    @Nonnull
    @Override
    protected abstract TempFileResource delegate();

    @Override
    public boolean delete() {
        return delegate().delete();
    }

    @Override
    public void save() throws IOException {
        delegate().save();
    }

    @Override
    public boolean exists() {
        return delegate().exists();
    }

    @Override
    public String getErrorMessage() {
        return delegate().getErrorMessage();
    }

    @Override
    public OutputStream getOutputStream() throws IOException {
        return delegate().getOutputStream();
    }

    @Override
    public PrintWriter getUtf8PrintWriter() throws IOException {
        return delegate().getUtf8PrintWriter();
    }

    @Override
    public void flush() throws IOException {
        delegate().flush();
    }

    @Override
    public void close() throws IOException {
        delegate().close();
    }

    @Nonnull
    @Override
    public String getPath() {
        return delegate().getPath();
    }

}
