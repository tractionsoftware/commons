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

import com.google.common.collect.ImmutableSet;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;

/**
 * @author Dave Shepperton
 */
public final class PropNamespaceTest {

    private MapPropertyStore<?> store;
    private PropStore<?> mexico;

    @BeforeEach
    public void setUp() {
        // we use a LinkedHashMap to preserve ordering so we can test
        // getPropertyNames
        LinkedHashMap<String,String> map = new LinkedHashMap<>();
        map.put("mexico_xyz", "123");
        map.put("mexico_abc", "456");
        map.put("mexico_0", "t");
        map.put("mexico_1", "f");
        map.put(null, "x");
        map.put("mexico", "y");

        store = new MapPropertyStore<>("test", map);
        mexico = store.getNamespace("mexico");
    }

    @Test
    public void test1() {
        Assertions.assertNull(mexico.getProperty("mexico_xyz"));
    }

    @Test
    public void test2() {
        Assertions.assertEquals("123", mexico.getProperty("xyz"));
    }

    @Test
    public void test3() {
        Assertions.assertEquals(ImmutableSet.of("xyz", "abc", "0", "1"), mexico.getPropertyNames());
    }

    @Test
    public void test4() {
        Assertions.assertEquals("mexico_123", mexico.fullyQualify("123"));
    }

    @Test
    public void test5() {
        Assertions.assertEquals("mexico", mexico.fullyQualify(null));
    }

    @Test
    public void test6() {
        Assertions.assertEquals("mexico", mexico.fullyQualify(""));
    }

    @Test
    public void test7() {
        Assertions.assertEquals("mexico_x", mexico.fullyQualify("x"));
    }

    @Test
    public void test8() {
        mexico.putProperty("abc", "678");
        Assertions.assertEquals("678", mexico.getProperty("abc"));
        Assertions.assertEquals("678", store.getProperty("mexico_abc"));
    }

}
