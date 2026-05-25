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
import com.tractionsoftware.commons.lang.StringUtil;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * This is effectively a test {@link SimplePropertyNameMapper} and {@link PropStore#getPrefix(String)}.
 *
 * @author Andy Keller, Dave Shepperton
 */
public class PropPrefixTest {

    private MapPropertyStore<?> mapStore;
    private PropStore<?> mexico;

    @BeforeEach
    public void setUp() {
        // we use a LinkedHashMap to preserve ordering so we can test getPropertyNames
        Map<String,String> map = new LinkedHashMap<>();
        map.put("xyz", "123");
        map.put("abc", "456");
        map.put("0", "t");
        map.put("1", "f");

        mapStore = new MapPropertyStore<>("test", map);
        mexico = mapStore.getPrefix("mexico");
    }

    @Test
    public void test1() {
        Assertions.assertNull(mexico.getProperty("xyz"));
    }

    @Test
    public void test2() {
        Assertions.assertEquals("123", mexico.getProperty("mexico_xyz"));
    }

    @Test
    public void test3() {
        Assertions.assertEquals(
            ImmutableSet.of("mexico_xyz", "mexico_abc", "mexico_0", "mexico_1"),
            mexico.getPropertyNames()
        );
    }

    @Test
    public void test4() {
        Assertions.assertEquals("123", mexico.fullyQualify("mexico_123"));
    }

    @Test
    public void test5() {
        Assertions.assertNull(mexico.fullyQualify("123"));
    }

    @Test
    public void test6() {
        Assertions.assertNull(mexico.fullyQualify("mexico"));
    }

    @Test
    public void test7() {
        Assertions.assertEquals("", mexico.fullyQualify("mexico_"));
    }

    @Test
    public void test8() {
        Assertions.assertEquals("x", mexico.fullyQualify("mexico_x"));
    }

    @Test
    public void test9() {
        mexico.putProperty("mexico_abc", "678");
        Assertions.assertEquals("678", mexico.getProperty("mexico_abc"));
        Assertions.assertEquals("678", mapStore.getProperty("abc"));
    }

    @Test
    public void test_prefixWithNs() {

        Map<String,String> useMap = new LinkedHashMap<>();
        useMap.put("attachments_count", "123");
        GetPutProperty gp = SimpleProperties.asGetPutProperty(useMap);

        // phase 1: make "attachments_count" accessible as "edit_attachments_count"
        gp = gp.getPrefix("edit");
        Assertions.assertEquals("123", gp.getProperty("edit_attachments_count"));
        // property names should reflect this
        Assertions.assertEquals("edit_attachments_count", StringUtil.join(gp.getPropertyNames(), ","));

        // phase 2: make "edit_attachments_count" accessible as "count"
        gp = gp.getNamespace("edit_attachments");
        Assertions.assertEquals("123", gp.getProperty("count"));
        // property names should reflect this
        Assertions.assertEquals("count", StringUtil.join(gp.getPropertyNames(), ","));

    }

    @Test
    public void test_nsWithPrefix() {

        Map<String,String> useMap = new LinkedHashMap<>();
        useMap.put("edit_attachments_count", "123");
        GetPutProperty gp = SimpleProperties.asGetPutProperty(useMap);

        // phase 1: make "edit_attachments_count" accessible as "count"
        gp = gp.getNamespace("edit_attachments");
        Assertions.assertEquals("123", gp.getProperty("count"));
        // property names should reflect this
        Assertions.assertEquals("count", StringUtil.join(gp.getPropertyNames(), ","));

        // phase 2: make "count" accessible as "attachments_count"
        gp = gp.getPrefix("attachments");
        Assertions.assertEquals("123", gp.getProperty("attachments_count"));
        // property names should reflect this
        Assertions.assertEquals("attachments_count", StringUtil.join(gp.getPropertyNames(), ","));

    }

}
