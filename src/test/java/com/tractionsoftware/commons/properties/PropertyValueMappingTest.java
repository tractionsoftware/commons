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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.UnaryOperator;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * @author Dave Shepperton
 */
public final class PropertyValueMappingTest {

    private static final UnaryOperator<String> DASH_ADDER = new UnaryOperator<>() {

        @Override
        public final String toString() {
            return "dash-adder";
        }

        @Override
        public final String apply(String value) {
            if (value == null) {
                return null;
            }
            return "-" + value + "-";
        }

    };

    private static final UnaryOperator<String> DASH_REMOVER = new UnaryOperator<>() {

        @Override
        public final String toString() {
            return "dash-remover";
        }

        @Override
        public final String apply(String value) {
            if (value == null) {
                return null;
            }
            if (value.length() >= 2 && value.startsWith("-") && value.endsWith("-")) {
                return value.substring(1, value.length() - 1);
            }
            return value;
        }

    };

    private MapPropertyStore<?> mapStore;
    private PropStore<?> changedValues;

    @BeforeEach
    public void setUp() {
        // we use a LinkedHashMap to preserve ordering so we can test getPropertyNames
        Map<String,String> map = new LinkedHashMap<>();
        map.put("foo", "xyz");
        map.put("bar", "abc");
        map.put("baz", null);

        mapStore = new MapPropertyStore<>("test", map);
        changedValues = mapStore.transformingValues(DASH_ADDER, DASH_REMOVER);
    }

    @Test
    public void test1() {
        assertEquals("-xyz-", changedValues.getProperty("foo"));
    }

    @Test
    public void test2() {
        assertEquals("-abc-", changedValues.getProperty("bar"));
    }

    @Test
    public void test3() {
        assertEquals(mapStore.getPropertyNames(), changedValues.getPropertyNames());
    }

    @Test
    public void test4() {
        changedValues.putProperty("abc", "-678-");
        assertEquals("-678-", changedValues.getProperty("abc"));
    }

    @Test
    public void test5() {
        changedValues.putProperty("abc", "-678-");
        assertEquals("678", mapStore.getProperty("abc"));
    }

    @Test
    public void test6() {
        assertNull(changedValues.getProperty("baz"));
    }

}
