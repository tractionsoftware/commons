/*
 *
 *    Copyright 1996-2025 Traction Software, Inc.
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

import java.util.function.Function;

/**
 * An extension of the {@link Function} interface that supports caching via the addition of the
 * {@link #resultsAreCacheable()} method.
 *
 * @author Dave Shepperton
 */
public interface CacheSupportingFunction<T, R> extends Function<T,R> {

    /**
     * This method should return true if the result of the {@link #apply(Object)} method should be considered cacheable
     * for the current context. This essentially means that while the function result may not be a static value, it is
     * not dependent upon the function input, or on any external factors.
     *
     * @return true if the result of the {@link #apply(Object)} method should be considered cacheable for the current
     *     context; false otherwise.
     */
    public boolean resultsAreCacheable();

}
