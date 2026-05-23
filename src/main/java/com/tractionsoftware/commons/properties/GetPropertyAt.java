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

import java.util.Objects;
import java.util.Set;

public interface GetPropertyAt {

    public Object getPropertyAt(int i);

    public default boolean contains(Object object) {
        for (int propNumber : getPropertyNumbers()) {
            if (Objects.equals(object, getPropertyAt(propNumber))) {
                return true;
            }
        }
        return false;
    }

    public default boolean hasPropertyAt(int i) {
        if (getPropertyAt(i) == null) {
            return false;
        }
        return true;
    }

    public Set<Integer> getPropertyNumbers();

    public default GetProperty asGetProperty() {
        return PropertyAdapters.getPropertyAtAsGetProperty(this);
    }

    public default int size() {
        return getPropertyNumbers().size();
    }

    public default boolean isEmpty() {
        return getPropertyNumbers().isEmpty();
    }

}
