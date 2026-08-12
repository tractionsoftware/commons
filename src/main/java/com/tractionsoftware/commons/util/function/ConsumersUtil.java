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

package com.tractionsoftware.commons.util.function;

import java.util.function.Consumer;

/**
 * @author Dave Shepperton
 */
public final class ConsumersUtil {

    private ConsumersUtil() {
    }

    /**
     * Combines two {@link Consumer}s if they are both non-null via {@link Consumer#andThen(Consumer)}, but otherwise,
     * returns whichever Consumer is not null.
     *
     * @param first
     *     the first {@link Consumer}.
     * @param second
     *     the second {@link Consumer}.
     * @return Combines two {@link Consumer}s if they are both non-null via {@link Consumer#andThen(Consumer)}, but
     *     otherwise, returns whichever Consumer is not null.
     */
    public static final <T> Consumer<T> combine(Consumer<T> first, Consumer<T> second) {
        if (first == null) {
            return second;
        }
        if (second == null) {
            return first;
        }
        return first.andThen(second);
    }

}
