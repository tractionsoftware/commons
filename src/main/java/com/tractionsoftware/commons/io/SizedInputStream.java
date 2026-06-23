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

import com.tractionsoftware.commons.util.MayHaveKnownSize;
import jakarta.annotation.Nonnull;

import java.io.FilterInputStream;
import java.io.InputStream;
import java.util.Objects;

public abstract class SizedInputStream extends FilterInputStream implements MayHaveKnownSize {

    @Nonnull
    public static final SizedInputStream forInputStream(@Nonnull InputStream input, long byteSize) {

        Objects.requireNonNull(input, "InputStream");

        return new SizedInputStream(input) {

            @Override
            public final long size() {
                return byteSize;
            }

        };

    }

    public SizedInputStream(InputStream input) {
        super(input);
    }

    /**
     * Returns the size of this {@link InputStream}, if the size is known.
     *
     * @return the size of this {@link InputStream}, if the size is known; {@link Long#MIN_VALUE} otherwise.
     */
    @Override
    public long size() {
        return Long.MIN_VALUE;
    }

}
