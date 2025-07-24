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

import java.util.*;

/**
 * Supports a chain of default {@link ReadOnlyPropertyMap}s within a
 * single instance. If a named property value is not found in one map,
 * the next map will be consulted, and so on. Thus, maps are
 * super-imposed on one another without having to instantiate a single
 * instance containing a merged view.
 *
 * @author Dave Shepperton
 */
public final class ChainedReadOnlyPropertyMap implements ReadOnlyPropertyMap {

    private final Iterable<ReadOnlyPropertyMap> maps;

    public ChainedReadOnlyPropertyMap(Collection<? extends ReadOnlyPropertyMap> maps) {
        this.maps = new ArrayList<>(maps);
    }

    public ChainedReadOnlyPropertyMap(ReadOnlyPropertyMap... maps) {
        this.maps = Arrays.asList(maps);
    }

    @Override
    public final String getValue(String name) {
        for (ReadOnlyPropertyMap m : maps) {
            String value = m.getValue(name);
            if (value != null) {
                return value;
            }
        }
        return null;
    }

    @Override
    public final Set<String> getNames() {
        Set<String> ret = new LinkedHashSet<String>();
        for (ReadOnlyPropertyMap m : maps) {
            ret.addAll(m.getNames());
        }
        return ret;
    }

}
