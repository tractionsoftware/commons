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

import com.tractionsoftware.commons.lang.ObjectsUtil;

/**
 * A skeleton {@link PropertyLoader} that provides a simple
 * implementation of {@link #cast(Object)} using
 * {@link ObjectsUtil#castIfAssignmentCompatible(Object, Class)},
 * passing the supplied Object and a Class literal for the target
 * type. Most PropertyLoader implementations should extend this class.
 *
 * @author Dave Shepperton
 */
public abstract class AbstractPropertyLoader<T> implements PropertyLoader<T> {

    protected final Class<T> type;

    public AbstractPropertyLoader(Class<T> type) {
        this.type = type;
    }

    @Override
    public final T cast(Object value) {
        return ObjectsUtil.castIfAssignmentCompatible(value, type);
    }

}
