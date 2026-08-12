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

import com.tractionsoftware.commons.io.SizedInputStream;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

/**
 * A minimal in-memory {@link Result} test double, shared by tests across multiple packages that previously each
 * defined their own copy of this class. Supports two modes:
 *
 * <ul>
 *     <li>Writable mode (the no-arg constructor): {@link #getOutputStream()} returns an internal buffer that can be
 *     written to (e.g. by a {@link Producer} or {@link Processor} via {@code Result.populate}/{@code
 *     getNextResult}), and the accumulated bytes can later be read back via {@link #getInputStream()} or
 *     {@link #getBytes()}.</li>
 *     <li>Pre-populated mode ({@link #ByteArrayResult(String)}): the Result's content is fixed at construction time
 *     from the given String (encoded as UTF-8). {@link #getOutputStream()} throws
 *     {@link UnsupportedOperationException} since this mode is meant to supply known, immutable content for reading
 *     only (e.g. as input to a sender or transformer under test).</li>
 * </ul>
 *
 * <p>Also tracks whether {@link #release()}, {@link #onPopulate(boolean)}, and {@link #onConsume(boolean)} were
 * invoked (and with what success value), so that tests which care about that lifecycle can assert on it via the
 * {@code is*Called()}/{@code is*Success()} accessors. Tests that don't care about the lifecycle can simply ignore
 * them.
 *
 * @author Dave Shepperton
 */
public final class ByteArrayResult extends Result {

    private final ByteArrayOutputStream buffer;

    private final byte[] fixedData;

    private boolean releaseCalled;

    private boolean onPopulateCalled;

    private boolean populated;

    private boolean onConsumeCalled;

    private boolean onConsumeSuccess;

    /**
     * Creates a writable, initially-empty ByteArrayResult whose {@link #getOutputStream()} can be written to.
     */
    public ByteArrayResult() {
        this.buffer = new ByteArrayOutputStream();
        this.fixedData = null;
    }

    /**
     * Creates a pre-populated, read-only ByteArrayResult backed by the UTF-8 encoding of the given content. Its
     * {@link #getOutputStream()} throws {@link UnsupportedOperationException} since the content is fixed.
     *
     * @param content
     *     the fixed content for this Result, encoded as UTF-8.
     */
    public ByteArrayResult(String content) {
        this.buffer = null;
        this.fixedData = content.getBytes(StandardCharsets.UTF_8);
    }

    /**
     * @return the bytes currently held by this Result: the fixed content if constructed from a String, or the bytes
     *     written so far to the writable buffer otherwise.
     */
    public byte[] getBytes() {
        return (fixedData != null) ? fixedData : buffer.toByteArray();
    }

    public boolean isReleaseCalled() {
        return releaseCalled;
    }

    public boolean isOnPopulateCalled() {
        return onPopulateCalled;
    }

    public boolean isPopulated() {
        return populated;
    }

    public boolean isOnConsumeCalled() {
        return onConsumeCalled;
    }

    public boolean isOnConsumeSuccess() {
        return onConsumeSuccess;
    }

    @Override
    public void release() {
        releaseCalled = true;
    }

    @Override
    protected OutputStream getOutputStream() {
        if (buffer == null) {
            throw new UnsupportedOperationException();
        }
        return buffer;
    }

    @Override
    protected SizedInputStream getInputStream() {
        byte[] data = getBytes();
        return SizedInputStream.forInputStream(new ByteArrayInputStream(data), data.length);
    }

    @Override
    protected void onPopulate(boolean success) {
        onPopulateCalled = true;
        populated = success;
    }

    @Override
    protected void onConsume(boolean success) {
        onConsumeCalled = true;
        onConsumeSuccess = success;
    }

}
