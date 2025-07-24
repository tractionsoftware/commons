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

package com.tractionsoftware.commons.properties;

import com.google.common.collect.ForwardingObject;

import java.util.Set;

/**
 * A ForwardingObject implementation of {@link ReadOnlyPropertyMap}.
 *
 * @author Dave Shepperton
 */
public abstract class ForwardingReadOnlyPropertyMap extends ForwardingObject implements ReadOnlyPropertyMap {

    /**
     * Specifically returns a ReadOnlyPropertyMap.
     */
    @Override
    protected abstract ReadOnlyPropertyMap delegate();

    @Override
    public final String getValue(String name) {
        return delegate().getValue(name);
    }

    @Override
    public final Set<String> getNames() {
        return delegate().getNames();
    }

}
