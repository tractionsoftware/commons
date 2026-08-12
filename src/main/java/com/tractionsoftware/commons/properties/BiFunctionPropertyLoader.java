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

package com.tractionsoftware.commons.properties;

import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * An {@link AbstractPropertyLoader} whose {@link #apply(String, String)} method forwards to another
 * {@link BiFunction}'s {@link Function#apply(Object)} method.
 *
 * @author Dave Shepperton
 */
public final class BiFunctionPropertyLoader<T> extends AbstractPropertyLoader<T> {

    private final BiFunction<String,String,? extends T> provider;

    public BiFunctionPropertyLoader(Class<T> type, BiFunction<String,String,? extends T> provider) {
        super(type);
        this.provider = provider;
    }

    @Override
    public final T apply(String name, String rawValue) {
        return provider.apply(name, rawValue);
    }

}
