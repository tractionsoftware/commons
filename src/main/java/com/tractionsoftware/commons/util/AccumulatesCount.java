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
 * An interface for an object that accumulates some sort of count.
 *
 * @author Dave Shepperton
 */
@Beta
@FunctionalInterface
public interface AccumulatesCount {

    /**
     * Returns the count that has been accumulated so far.
     *
     * @return the count that has been accumulated so far.
     */
    public long getCount();

    /**
     * Returns an {@link AccumulatesCount} whose {@link #getCount()} method will represent the count from the current
     * point onwards.
     *
     * @return an {@link AccumulatesCount} whose {@link #getCount()} method will represent the count from the current
     *     point onwards.
     */
    public default AccumulatesCount startingFromCurrentCount() {
        final long current = getCount();
        if (current == 0) {
            return this;
        }
        return () -> AccumulatesCount.this.getCount() - current;
    }

}
