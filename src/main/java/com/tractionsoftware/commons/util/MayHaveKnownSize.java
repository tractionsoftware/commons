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

package com.tractionsoftware.commons.util;

import com.google.common.annotations.Beta;

/**
 * An interface for an object that comes with a size which <i>may</i> be known.
 *
 * @author Dave Shepperton
 */
@Beta
@FunctionalInterface
public interface MayHaveKnownSize {

    /**
     * Returns the size of this object, if the size is known.
     *
     * @return the size of this object, if the size is known; {@link Long#MIN_VALUE} otherwise.
     */
    public long size();

    public default boolean hasSizeAtLeast(long lowerBound) {
        long size = size();
        if (size == Long.MIN_VALUE) {
            return false;
        }
        if (size >= lowerBound) {
            return true;
        }
        return false;
    }

    public default boolean hasSizeGreaterThan(long compare) {
        long size = size();
        if (size == Long.MIN_VALUE) {
            return false;
        }
        if (size > compare) {
            return true;
        }
        return false;
    }

    public default boolean hasSizeAtMost(long upperBound) {
        long size = size();
        if (size == Long.MIN_VALUE) {
            return false;
        }
        if (size <= upperBound) {
            return true;
        }
        return false;
    }

    public default boolean hasSizeLessThan(long compare) {
        long size = size();
        if (size == Long.MIN_VALUE) {
            return false;
        }
        if (size < compare) {
            return true;
        }
        return false;
    }

    public default boolean hasSizeBetween(long lowerBound, long upperBound) {
        long size = size();
        if (size == Long.MIN_VALUE) {
            return false;
        }
        if (size >= lowerBound && size <= upperBound) {
            return true;
        }
        return false;
    }

}
