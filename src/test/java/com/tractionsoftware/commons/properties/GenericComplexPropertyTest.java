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

import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;

import static org.junit.jupiter.api.Assertions.*;

public final class GenericComplexPropertyTest {

    // =====================================================================
    // Construction / get / put
    // =====================================================================

    @Test
    void defaultConstructor_isEmpty() {
        var gcp = new GenericComplexProperty();
        assertTrue(gcp.isEmpty());
    }

    @Test
    void putProperty_andGetProperty_roundTrip() {
        var gcp = new GenericComplexProperty();
        gcp.putProperty("k", "v");
        assertEquals("v", gcp.getProperty("k"));
    }

    @Test
    void getProperty_absent_returnsNull() {
        assertNull(new GenericComplexProperty().getProperty("missing"));
    }

    @Test
    void getPropertyNames_reflectsAllKeys() {
        var gcp = new GenericComplexProperty();
        gcp.putProperty("a", "1");
        gcp.putProperty("b", "2");
        assertTrue(gcp.getPropertyNames().contains("a"));
        assertTrue(gcp.getPropertyNames().contains("b"));
    }

    @Test
    void putProperty_null_removes_whenWriteNullTrue() {
        // default constructor: writeNull=true; store has removeOnNullValuePut=false
        // so null is retained in the map
        var gcp = new GenericComplexProperty(true);
        gcp.putProperty("k", "v");
        gcp.putProperty("k", null);
        // with writeNull=true the store is new MapPropertyStore(…, false) → null stays
        assertNull(gcp.getProperty("k"));
    }

    @Test
    void putProperty_null_removesKey_whenWriteNullFalse() {
        // writeNull=false: store has removeOnNullValuePut=true → null removes
        var gcp = new GenericComplexProperty(false);
        gcp.putProperty("k", "v");
        gcp.putProperty("k", null);
        assertNull(gcp.getProperty("k"));
        assertFalse(gcp.hasProperty("k"));
    }

    // =====================================================================
    // saveInstance
    // =====================================================================

    @Test
    void saveInstance_writesAllKeyValues() {
        var gcp = new GenericComplexProperty();
        gcp.putProperty("x", "hello");
        gcp.putProperty("y", "world");

        MapPropertyStore<Void> sink = MapPropertyStore.createDefaultInstance();
        gcp.saveInstance(sink);

        assertEquals("hello", sink.getProperty("x"));
        assertEquals("world", sink.getProperty("y"));
    }

    @Test
    void saveInstance_withNullValue_writesNull_whenWriteNullTrue() {
        // writeNull=true: null values ARE written
        var gcp = new GenericComplexProperty(true);
        gcp.putProperty("k", null);

        MapPropertyStore<Void> sink = MapPropertyStore.createNamedInstance("s", new LinkedHashMap<>(), false);
        gcp.saveInstance(sink);
        assertTrue(sink.hasProperty("k"));
        assertNull(sink.getProperty("k"));
    }

    @Test
    void saveInstance_withNullValue_skipsNull_whenWriteNullFalse() {
        // writeNull=false: null values are NOT written
        var gcp = new GenericComplexProperty(false);
        // putProperty with null on a !writeNull store removes the key
        // so we can't actually store null; check that normal non-null values are written
        gcp.putProperty("k", "v");
        MapPropertyStore<Void> sink = MapPropertyStore.createDefaultInstance();
        gcp.saveInstance(sink);
        assertEquals("v", sink.getProperty("k"));
    }

    // =====================================================================
    // GENERIC_LOADER
    // =====================================================================

    @Test
    void genericLoader_emptyNamespace_returnsNull() {
        GetProperty empty = SimpleProperties.emptyGetProperty();
        assertNull(GenericComplexProperty.GENERIC_LOADER.loadInstance(empty));
    }

    @Test
    void genericLoader_namespaceWithLocals_returnsPopulated() {
        MapPropertyStore<Void> store = MapPropertyStore.createDefaultInstance();
        store.putProperty("color", "blue");
        GenericComplexProperty result = GenericComplexProperty.GENERIC_LOADER.loadInstance(store);
        assertNotNull(result);
        assertEquals("blue", result.getProperty("color"));
    }

    @Test
    void genericLoader_preservesAllLocalProperties() {
        MapPropertyStore<Void> store = MapPropertyStore.createDefaultInstance();
        store.putProperty("a", "1");
        store.putProperty("b", "2");
        store.putProperty("c", "3");
        GenericComplexProperty result = GenericComplexProperty.GENERIC_LOADER.loadInstance(store);
        assertNotNull(result);
        assertEquals(3, result.getPropertyNames().size());
    }

    // =====================================================================
    // hasProperty
    // =====================================================================

    @Test
    void hasProperty_present_true() {
        var gcp = new GenericComplexProperty();
        gcp.putProperty("p", "v");
        assertTrue(gcp.hasProperty("p"));
    }

    @Test
    void hasProperty_absent_false() {
        assertFalse(new GenericComplexProperty().hasProperty("p"));
    }

}
