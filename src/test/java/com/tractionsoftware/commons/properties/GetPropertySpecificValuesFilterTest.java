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

import com.google.common.collect.ImmutableMap;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

public final class GetPropertySpecificValuesFilterTest {

    private static GetProperty props(String key, String value) {
        return SimpleProperties.asGetProperty(ImmutableMap.of(key, value));
    }

    /** allowed=true: values in the set are the ONLY allowed ones */
    @Test
    void allowedValues_matchingValue_returnsFalse_notFiltered() {
        var filter = new GetPropertySpecificValuesFilter("status", Set.of("active"), true);
        // test() returns true when the property is FILTERED OUT (not allowed)
        assertFalse(filter.test(props("status", "active")));
    }

    @Test
    void allowedValues_nonMatchingValue_returnsTrue_filtered() {
        var filter = new GetPropertySpecificValuesFilter("status", Set.of("active"), true);
        assertTrue(filter.test(props("status", "inactive")));
    }

    /** allowed=false: values in the set are the ONLY disallowed ones */
    @Test
    void disallowedValues_matchingValue_returnsTrue_filtered() {
        var filter = new GetPropertySpecificValuesFilter("status", Set.of("banned"), false);
        assertTrue(filter.test(props("status", "banned")));
    }

    @Test
    void disallowedValues_nonMatchingValue_returnsFalse_notFiltered() {
        var filter = new GetPropertySpecificValuesFilter("status", Set.of("banned"), false);
        assertFalse(filter.test(props("status", "active")));
    }

    @Test
    void missingProperty_treatedAsNullValue_withAllowedSet_filtered() {
        var filter = new GetPropertySpecificValuesFilter("status", Set.of("active"), true);
        // null is not in the allowed set → filtered out
        assertTrue(filter.test(SimpleProperties.emptyGetProperty()));
    }

    @Test
    void missingProperty_treatedAsNullValue_withDisallowedSet_notFiltered() {
        var filter = new GetPropertySpecificValuesFilter("status", Set.of("banned"), false);
        // null is not in the disallowed set → not filtered
        assertFalse(filter.test(SimpleProperties.emptyGetProperty()));
    }

    @Test
    void multipleAllowedValues_matchAny() {
        var filter = new GetPropertySpecificValuesFilter("color", Set.of("red", "blue", "green"), true);
        assertFalse(filter.test(props("color", "red")));
        assertFalse(filter.test(props("color", "blue")));
        assertTrue(filter.test(props("color", "purple")));
    }

}
