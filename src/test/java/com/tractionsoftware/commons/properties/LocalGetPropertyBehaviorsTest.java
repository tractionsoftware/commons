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
import com.google.common.collect.ImmutableSet;
import com.tractionsoftware.commons.lang.StringUtil;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

public final class LocalGetPropertyBehaviorsTest {

    @Test
    public void testLocalsDoesNotContainDefaults() {
        GetProperty propsMain = SimpleProperties.asGetProperty(ImmutableMap.of());
        GetProperty propsDefaults = SimpleProperties.asGetProperty(ImmutableMap.of("foo", "bar"));
        GetProperty combined = propsMain.withDefaults(propsDefaults);
        Assertions.assertNull(combined.getLocals().getProperty("foo"));
    }

    @Test
    public void testLocalsNamesDoNotContain() {

        Set<String> mainNames = ImmutableSet.of("a", "b", "c", "d", "e");
        Map<String,String> map = new LinkedHashMap<>();
        for (String name : mainNames) {
            map.put(name, StringUtil.getRandomSequence(10, "abcdefghijklmnopqrstuvwxyz"));
        }

        GetProperty propsMain = SimpleProperties.asGetProperty(map);
        GetProperty propsDefaults = SimpleProperties.asGetProperty(ImmutableMap.of("z", "foo"));
        GetProperty combined = propsMain.withDefaults(propsDefaults);

        Assertions.assertEquals(mainNames, combined.getLocals().getPropertyNames());

    }

}
