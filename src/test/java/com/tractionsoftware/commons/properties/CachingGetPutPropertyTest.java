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

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class CachingGetPutPropertyTest {

    private static MapPropertyStore<Void> store(Map<String, String> map) {
        return MapPropertyStore.createInstance(new HashMap<>(map));
    }

    // ---------------------------------------------------------------------------
    // wrapWithDefaultCache
    // ---------------------------------------------------------------------------

    @Test
    void wrapWithDefaultCache_returnsNonNull() {
        GetPutProperty cached = CachingGetPutProperty.wrapWithDefaultCache(store(Map.of("k", "v")));
        assertNotNull(cached);
    }

    @Test
    void wrapWithDefaultCache_getProperty_returnsValue() {
        GetPutProperty cached = CachingGetPutProperty.wrapWithDefaultCache(store(Map.of("k", "v")));
        assertEquals("v", cached.getProperty("k"));
    }

    @Test
    void wrapWithDefaultCache_putProperty_updatesDelegate() {
        MapPropertyStore<Void> delegate = store(Map.of());
        GetPutProperty cached = CachingGetPutProperty.wrapWithDefaultCache(delegate);
        cached.putProperty("key", "value");
        assertEquals("value", delegate.getProperty("key"));
    }

    // ---------------------------------------------------------------------------
    // wrapWithCache (explicit PropertyCache)
    // ---------------------------------------------------------------------------

    @Test
    void wrapWithCache_nullCache_throwsNPE() {
        assertThrows(NullPointerException.class,
            () -> CachingGetPutProperty.wrapWithCache(store(Map.of()), null));
    }

    @Test
    void wrapWithCache_readsValue() {
        PropertyCache cache = PropertyCache.createInstance();
        GetPutProperty cached = CachingGetPutProperty.wrapWithCache(store(Map.of("x", "42")), cache);
        assertEquals("42", cached.getProperty("x"));
    }

    // ---------------------------------------------------------------------------
    // toString
    // ---------------------------------------------------------------------------

    @Test
    void toString_startsWithGetPutProperty() {
        GetPutProperty cached = CachingGetPutProperty.wrapWithDefaultCache(store(Map.of()));
        assertTrue(cached.toString().startsWith("GetPutProperty:"),
            "toString should start with 'GetPutProperty:' but was: " + cached.toString());
    }

    // ---------------------------------------------------------------------------
    // getPropertyNames
    // ---------------------------------------------------------------------------

    @Test
    void getPropertyNames_returnsNamesFromDelegate() {
        GetPutProperty cached = CachingGetPutProperty.wrapWithDefaultCache(store(Map.of("a", "1", "b", "2")));
        assertTrue(cached.getPropertyNames().contains("a"));
        assertTrue(cached.getPropertyNames().contains("b"));
    }

    // ---------------------------------------------------------------------------
    // undefined property returns null
    // ---------------------------------------------------------------------------

    @Test
    void getProperty_undefined_returnsNull() {
        GetPutProperty cached = CachingGetPutProperty.wrapWithDefaultCache(store(Map.of()));
        assertNull(cached.getProperty("missing"));
    }

}
