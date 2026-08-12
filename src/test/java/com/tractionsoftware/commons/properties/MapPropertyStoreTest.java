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
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public final class MapPropertyStoreTest {

    // =====================================================================
    // Construction
    // =====================================================================

    @Test
    void defaultConstructor_createsEmptyStore() {
        MapPropertyStore<Void> store = MapPropertyStore.createDefaultInstance();
        assertTrue(store.isEmpty());
    }

    @Test
    void namedConstructor_setsName() {
        MapPropertyStore<Void> store = MapPropertyStore.createNamedInstance("mystore", new LinkedHashMap<>());
        assertEquals("mystore", store.getName());
    }

    @Test
    void nullName_usesDefault() {
        MapPropertyStore<Void> store = MapPropertyStore.createDefaultInstance();
        assertNotNull(store.getName());
    }

    @Test
    void nullMap_throwsNPE() {
        assertThrows(NullPointerException.class, () -> MapPropertyStore.createNamedInstance("test", null));
    }

    @Test
    void createInstance_works() {
        Map<String,String> map = Map.of("a", "1");
        MapPropertyStore<Void> store = MapPropertyStore.createInstance(map);
        assertEquals("1", store.getProperty("a"));
    }

    @Test
    void fromUnknownMap_convertsKeys() {
        Map<Object,Object> raw = new LinkedHashMap<>();
        raw.put("key", "val");
        MapPropertyStore<Void> store = MapPropertyStore.createInstanceFromUnknownMapCopy("test", raw);
        assertEquals("val", store.getProperty("key"));
    }

    // =====================================================================
    // get / has / put / remove
    // =====================================================================

    @Test
    void getProperty_present_returnsValue() {
        MapPropertyStore<Void> store = MapPropertyStore.createDefaultInstance();
        store.putProperty("x", "42");
        assertEquals("42", store.getProperty("x"));
    }

    @Test
    void getProperty_absent_returnsNull() {
        MapPropertyStore<Void> store = MapPropertyStore.createDefaultInstance();
        assertNull(store.getProperty("x"));
    }

    @Test
    void hasProperty_present_true() {
        MapPropertyStore<Void> store = MapPropertyStore.createDefaultInstance();
        store.putProperty("k", "v");
        assertTrue(store.hasProperty("k"));
    }

    @Test
    void hasProperty_absent_false() {
        assertFalse(MapPropertyStore.createDefaultInstance().hasProperty("k"));
    }

    @Test
    void hasProperty_null_false() {
        assertFalse(MapPropertyStore.createDefaultInstance().hasProperty(null));
    }

    @Test
    void putProperty_nullValue_removesKey() {
        MapPropertyStore<Void> store = MapPropertyStore.createDefaultInstance();
        store.putProperty("k", "v");
        store.putProperty("k", null);  // removeOnNullValuePut = true
        assertFalse(store.hasProperty("k"));
    }

    @Test
    void removeProperty_removesKey() {
        MapPropertyStore<Void> store = MapPropertyStore.createDefaultInstance();
        store.putProperty("k", "v");
        store.removeProperty("k");
        assertNull(store.getProperty("k"));
    }

    @Test
    void clearAll_removesAll() {
        MapPropertyStore<Void> store = MapPropertyStore.createDefaultInstance();
        store.putProperty("a", "1");
        store.putProperty("b", "2");
        store.clearAll();
        assertTrue(store.isEmpty());
    }

    @Test
    void clearLocalProperties_removesAll() {
        MapPropertyStore<Void> store = MapPropertyStore.createDefaultInstance();
        store.putProperty("x", "y");
        assertTrue(store.clearLocalProperties());
        assertTrue(store.isEmpty());
    }

    // =====================================================================
    // updateMap
    // =====================================================================

    @Test
    void updateMap_replacesAll() {
        Map<String,String> initial = new LinkedHashMap<>(Map.of("a", "1", "b", "2"));
        MapPropertyStore<Void> store = MapPropertyStore.createNamedInstance("test", initial);
        store.updateMap(Map.of("c", "3"));
        assertNull(store.getProperty("a"));
        assertNull(store.getProperty("b"));
        assertEquals("3", store.getProperty("c"));
    }

    // =====================================================================
    // getProperties
    // =====================================================================

    @Test
    void getProperties_returnsRequestedKeys() {
        MapPropertyStore<Void> store = MapPropertyStore.createDefaultInstance();
        store.putProperty("a", "1");
        store.putProperty("b", "2");
        Map<String,String> result = store.getProperties(List.of("a", "b", "missing"));
        assertEquals("1", result.get("a"));
        assertEquals("2", result.get("b"));
        assertNull(result.get("missing"));
    }

    @Test
    void getProperties_nullNames_returnsEmpty() {
        MapPropertyStore<Void> store = MapPropertyStore.createDefaultInstance();
        assertTrue(store.getProperties(null).isEmpty());
    }

    // =====================================================================
    // commitChanges
    // =====================================================================

    @Test
    void commitChanges_alwaysSucceeds() {
        MapPropertyStore<Void> store = MapPropertyStore.createDefaultInstance();
        CommitResult result = store.commitChanges(null);
        assertNotNull(result);
        assertTrue(result.wasSuccessful());
    }

    // =====================================================================
    // toString / getPropertyNames
    // =====================================================================

    @Test
    void toString_notNull() {
        assertNotNull(MapPropertyStore.createDefaultInstance().toString());
    }

    @Test
    void getPropertyNames_reflectsCurrentState() {
        MapPropertyStore<Void> store = MapPropertyStore.createDefaultInstance();
        store.putProperty("x", "1");
        store.putProperty("y", "2");
        assertTrue(store.getPropertyNames().contains("x"));
        assertTrue(store.getPropertyNames().contains("y"));
    }

    // =====================================================================
    // removeOnNullValuePut=false variant
    // =====================================================================

    @Test
    void putNullValue_keepNullWhenFlagFalse() {
        MapPropertyStore<Void> store = MapPropertyStore.createNamedInstance("test", new LinkedHashMap<>(), false);
        store.putProperty("k", null);
        assertTrue(store.hasProperty("k"));
    }

}
