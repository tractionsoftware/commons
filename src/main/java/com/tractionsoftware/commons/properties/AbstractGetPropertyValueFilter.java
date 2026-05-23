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

import java.util.function.Predicate;

/**
 * A Filter for GetProperty objects whose skip method returns true if
 * a given named property is not allowed.
 *
 * @author Dave Shepperton
 */
public abstract class AbstractGetPropertyValueFilter implements Predicate<GetProperty> {

    protected final String propName;

    /**
     * Constructs a new PropertyValueFilter.
     *
     * @param propName the name of the property to examine.
     */
    public AbstractGetPropertyValueFilter(String propName) {
        this.propName = propName;
    }

    @Override
    public final boolean test(GetProperty get) {
        if (allowed(SimpleProperties.loadString(get, propName, null))) {
            return false;
        }
        return true;
    }

    protected abstract boolean allowed(String actualValue);

}
