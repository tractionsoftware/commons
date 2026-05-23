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
import jakarta.annotation.Nonnull;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.Objects;

/**
 * An {@link InputStream} that wraps a {@link ByteBuffer}, which is created or retrieved on demand.
 *
 * @author Dave Shepperton
 */
@Beta
public final class ByteBufferInputStream extends InputStream {

    @FunctionalInterface
    public static interface ByteBufferCreator {

        public ByteBuffer create() throws IOException;

    }

    public static final ByteBufferInputStream createInstance(String text, Charset charset) {
        Objects.requireNonNull(text, "text");
        Objects.requireNonNull(charset, "Charset");
        return new ByteBufferInputStream(() -> charset.encode(text));
    }

    public static final ByteBufferInputStream createInstance(byte[] data) {
        return new ByteBufferInputStream(() -> ByteBuffer.wrap(data));
    }

    public static final ByteBufferInputStream createInstance(ByteBuffer data) {
        return new ByteBufferInputStream(() -> data);
    }

    private final ByteBufferCreator buffCreator;

    private ByteBuffer buff;

    private boolean closed = false;

    public ByteBufferInputStream(ByteBufferCreator buffCreator) {
        this.buffCreator = buffCreator;
    }

    @Override
    public final int read() throws IOException {
        if (!buff().hasRemaining()) {
            return -1;
        }
        return buff.get() & 0xFF;
    }

    @Override
    public final int read(@Nonnull byte[] bytes, int off, int len) throws IOException {
        if (!buff().hasRemaining()) {
            return -1;
        }
        len = Math.min(len, buff.remaining());
        buff.get(bytes, off, len);
        return len;
    }

    @Override
    public final void close() {
        if (!closed) {
            if (buff != null) {
                buff = null;
            }
            closed = true;
        }
    }

    private final ByteBuffer buff() throws IOException {
        if (buff == null) {
            if (closed) {
                throw new IOException("closed");
            }
            buff = buffCreator.create();
        }
        return buff;
    }

}
