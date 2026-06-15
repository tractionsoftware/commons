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
import com.tractionsoftware.commons.config.Configuration;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public final class SimplePropertiesTest {

    private static GetProperty props(Map<String,String> map) {
        return SimpleProperties.asGetProperty(map);
    }

    private static GetProperty props(String key, String value) {
        return props(ImmutableMap.of(key, value));
    }

    // =====================================================================
    // emptyGetProperty / emptyConfiguration / getEmptyConfiguration
    // =====================================================================

    @Test
    void emptyGetProperty_returnsNull() {
        assertNull(SimpleProperties.emptyGetProperty().getProperty("x"));
    }

    @Test
    void emptyGetProperty_isEmpty() {
        assertTrue(SimpleProperties.emptyGetProperty().isEmpty());
    }

    @Test
    void emptyConfiguration_hasNoProperties() {
        Configuration cfg = SimpleProperties.emptyConfiguration();
        assertNotNull(cfg);
        assertNull(cfg.getProperty("anything"));
    }

    @Test
    void getEmptyConfiguration_setsName() {
        Configuration cfg = SimpleProperties.getEmptyConfiguration("myname");
        assertEquals("myname", cfg.getName());
    }

    // =====================================================================
    // asGetProperty / asConfiguration / asPropStore
    // =====================================================================

    @Test
    void asGetProperty_map_returnsValues() {
        GetProperty gp = SimpleProperties.asGetProperty(Map.of("k", "v"));
        assertEquals("v", gp.getProperty("k"));
    }

    @Test
    void asConfiguration_setsName() {
        Configuration cfg = SimpleProperties.asConfiguration(Map.of("a", "b"), "testcfg");
        assertEquals("testcfg", cfg.getName());
        assertEquals("b", cfg.getProperty("a"));
    }

    @Test
    void asPropStore_readWrite() {
        MapPropertyStore<Void> store = new MapPropertyStore<>();
        store.putProperty("x", "1");
        assertEquals("1", store.getProperty("x"));
    }

    // =====================================================================
    // loadString
    // =====================================================================

    @Test
    void loadString_present_returnsValue() {
        assertEquals("hello", SimpleProperties.loadString(props("k", "hello"), "k"));
    }

    @Test
    void loadString_absent_returnsNull() {
        assertNull(SimpleProperties.loadString(SimpleProperties.emptyGetProperty(), "k"));
    }

    @Test
    void loadString_withDefault_absent_returnsDefault() {
        assertEquals("dflt", SimpleProperties.loadString(SimpleProperties.emptyGetProperty(), "k", "dflt"));
    }

    @Test
    void loadString_withDefault_present_returnsValue() {
        assertEquals("val", SimpleProperties.loadString(props("k", "val"), "k", "dflt"));
    }

    @Test
    void loadTrimmedOrEmpty_trims() {
        assertEquals("hello", SimpleProperties.loadTrimmedOrEmpty(props("k", "  hello  "), "k"));
    }

    @Test
    void loadTrimmedOrEmpty_absent_returnsEmpty() {
        assertEquals("", SimpleProperties.loadTrimmedOrEmpty(SimpleProperties.emptyGetProperty(), "k"));
    }

    @Test
    void loadTrimmedOrNull_trims() {
        assertEquals("hello", SimpleProperties.loadTrimmedOrNull(props("k", "  hello  "), "k"));
    }

    @Test
    void loadTrimmedOrNull_blank_returnsNull() {
        assertNull(SimpleProperties.loadTrimmedOrNull(props("k", "   "), "k"));
    }

    // =====================================================================
    // loadInt / loadLong / loadBoolean / loadDouble
    // =====================================================================

    @Test
    void loadInt_valid_returnsInt() {
        assertEquals(42, SimpleProperties.loadInt(props("n", "42"), "n"));
    }

    @Test
    void loadInt_absent_returnsMinusOne() {
        assertEquals(-1, SimpleProperties.loadInt(SimpleProperties.emptyGetProperty(), "n"));
    }

    @Test
    void loadInt_withDefault_absent_returnsDefault() {
        assertEquals(99, SimpleProperties.loadInt(SimpleProperties.emptyGetProperty(), "n", 99));
    }

    @Test
    void loadLong_valid_returnsLong() {
        assertEquals(Long.MAX_VALUE, SimpleProperties.loadLong(props("n", String.valueOf(Long.MAX_VALUE)), "n", 0L));
    }

    @Test
    void loadBoolean_true_returnsTrue() {
        assertTrue(SimpleProperties.loadBoolean(props("b", "true"), "b"));
    }

    @Test
    void loadBoolean_false_returnsFalse() {
        assertFalse(SimpleProperties.loadBoolean(props("b", "false"), "b"));
    }

    @Test
    void loadBoolean_absent_returnsDefault() {
        assertFalse(SimpleProperties.loadBoolean(SimpleProperties.emptyGetProperty(), "b"));
        assertTrue(SimpleProperties.loadBoolean(SimpleProperties.emptyGetProperty(), "b", true));
    }

    @Test
    void loadDouble_valid_returnsDouble() {
        assertEquals(3.14, SimpleProperties.loadDouble(props("d", "3.14"), "d", 0.0), 0.001);
    }

    // =====================================================================
    // saveString / saveInt / saveBoolean
    // =====================================================================

    @Test
    void saveString_storesValue() {
        MapPropertyStore<Void> store = new MapPropertyStore<>();
        SimpleProperties.saveString(store, "k", "hello");
        assertEquals("hello", store.getProperty("k"));
    }

    @Test
    void saveString_unless_skipIfMatch() {
        MapPropertyStore<Void> store = new MapPropertyStore<>();
        SimpleProperties.saveString(store, "k", "hello", "hello"); // unless == value, so not saved
        assertNull(store.getProperty("k"));
    }

    @Test
    void saveInt_storesValue() {
        MapPropertyStore<Void> store = new MapPropertyStore<>();
        SimpleProperties.saveInt(store, "n", 7);
        assertEquals(7, SimpleProperties.loadInt(store, "n"));
    }

    @Test
    void saveBoolean_storesTrue() {
        MapPropertyStore<Void> store = new MapPropertyStore<>();
        SimpleProperties.saveBoolean(store, "flag", true);
        assertTrue(SimpleProperties.loadBoolean(store, "flag"));
    }

    // =====================================================================
    // loadListSingleProperty / loadSetSingleProperty
    // =====================================================================

    @Test
    void loadListSingleProperty_commaDelimited() {
        List<String> list = SimpleProperties.loadListSingleProperty(props("items", "a,b,c"), "items");
        assertEquals(3, list.size());
        assertTrue(list.contains("a"));
        assertTrue(list.contains("c"));
    }

    @Test
    void loadListSingleProperty_absent_returnsEmpty() {
        assertTrue(SimpleProperties.loadListSingleProperty(SimpleProperties.emptyGetProperty(), "items").isEmpty());
    }

    @Test
    void loadSetSingleProperty_deduplicates() {
        var set = SimpleProperties.loadSetSingleProperty(props("items", "a,b,a,c"), "items");
        assertEquals(3, set.size());
    }

    // =====================================================================
    // loadFirstString
    // =====================================================================

    @Test
    void loadFirstString_firstDefined_returnsFirst() {
        GetProperty gp = props("b", "from-b");
        String val = SimpleProperties.loadFirstString(gp, List.of("a", "b", "c"), "dflt");
        assertEquals("from-b", val);
    }

    @Test
    void loadFirstString_noneDefined_returnsDefault() {
        String val = SimpleProperties.loadFirstString(SimpleProperties.emptyGetProperty(), List.of("a", "b"), "dflt");
        assertEquals("dflt", val);
    }

    // =====================================================================
    // getName
    // =====================================================================

    @Test
    void getName_namedStore_returnsName() {
        MapPropertyStore<Void> store = new MapPropertyStore<>("mystore", new java.util.HashMap<>());
        assertEquals("mystore", SimpleProperties.getName(store));
    }

    @Test
    void getName_anonymous_returnsGenericName() {
        assertEquals("Empty", SimpleProperties.getName(SimpleProperties.emptyGetProperty()));
    }

}
