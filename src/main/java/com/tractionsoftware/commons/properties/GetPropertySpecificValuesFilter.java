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

import java.util.HashSet;
import java.util.Set;

/**
 * An AbstractGetPropertyValueFilter that treats a property value as
 * allowed or disallowed based upon whether it appears in a Set of
 * values.
 *
 * @author Dave Shepperton
 */
public final class GetPropertySpecificValuesFilter extends AbstractGetPropertyValueFilter {

    private final Set<Object> specificPropValues;

    private final boolean allowed;

    /**
     * Constructs a new GetPropertySpecificValueFilter.
     *
     * @param propName
     *            the name of the property whose value is to be
     *            examined.
     * @param specificPropValues
     *            a Set of values representing either the only allowed
     *            values or the only disallowed values.
     * @param allowed
     *            true indicates that the specificPropValues are the
     *            only allowed values; false indicates that they are
     *            the only disallowed values.
     */
    public GetPropertySpecificValuesFilter(String propName, Set<? super String> specificPropValues, boolean allowed) {
        super(propName);
        this.specificPropValues = new HashSet<Object>(specificPropValues);
        this.allowed = allowed;
    }

    @Override
    protected final boolean allowed(String actualValue) {
        if (specificPropValues.contains(actualValue)) {
            return allowed;
        }
        return !allowed;
    }

}
