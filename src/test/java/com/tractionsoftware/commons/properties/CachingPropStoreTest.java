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

class CachingPropStoreTest {

    private static MapPropertyStore<Void> store(Map<String, String> map) {
        return MapPropertyStore.createInstance(new HashMap<>(map));
    }

    // ---------------------------------------------------------------------------
    // wrapWithDefaultCache
    // ---------------------------------------------------------------------------

    @Test
    void wrapWithDefaultCache_returnsNonNull() {
        PropStore<Void> cached = CachingPropStore.wrapWithDefaultCache(store(Map.of("k", "v")));
        assertNotNull(cached);
    }

    @Test
    void wrapWithDefaultCache_getProperty_returnsValue() {
        PropStore<Void> cached = CachingPropStore.wrapWithDefaultCache(store(Map.of("k", "v")));
        assertEquals("v", cached.getProperty("k"));
    }

    @Test
    void wrapWithDefaultCache_putProperty_updatesDelegate() {
        MapPropertyStore<Void> delegate = store(Map.of());
        PropStore<Void> cached = CachingPropStore.wrapWithDefaultCache(delegate);
        cached.putProperty("key", "value");
        assertEquals("value", delegate.getProperty("key"));
    }

    // ---------------------------------------------------------------------------
    // wrapWithCache (explicit PropertyCache)
    // ---------------------------------------------------------------------------

    @Test
    void wrapWithCache_readsValue() {
        PropertyCache cache = PropertyCache.createInstance();
        PropStore<Void> cached = CachingPropStore.wrapWithCache(store(Map.of("x", "42")), cache);
        assertEquals("42", cached.getProperty("x"));
    }

    // ---------------------------------------------------------------------------
    // commitChanges — delegates to underlying PropStore
    // ---------------------------------------------------------------------------

    @Test
    void commitChanges_delegatesToDelegate() {
        MapPropertyStore<Void> delegate = store(Map.of("a", "1"));
        PropStore<Void> cached = CachingPropStore.wrapWithDefaultCache(delegate);
        // MapPropertyStore.commitChanges always returns a successful result
        CommitResult result = cached.commitChanges(null);
        assertNotNull(result);
        assertTrue(result.wasSuccessful());
    }

    // ---------------------------------------------------------------------------
    // toString
    // ---------------------------------------------------------------------------

    @Test
    void toString_startsWithPropStore() {
        PropStore<Void> cached = CachingPropStore.wrapWithDefaultCache(store(Map.of()));
        assertTrue(cached.toString().startsWith("PropStore:"),
            "toString should start with 'PropStore:' but was: " + cached.toString());
    }

    // ---------------------------------------------------------------------------
    // getPropertyNames
    // ---------------------------------------------------------------------------

    @Test
    void getPropertyNames_returnsNamesFromDelegate() {
        PropStore<Void> cached = CachingPropStore.wrapWithDefaultCache(store(Map.of("a", "1", "b", "2")));
        assertTrue(cached.getPropertyNames().contains("a"));
        assertTrue(cached.getPropertyNames().contains("b"));
    }

    // ---------------------------------------------------------------------------
    // undefined property returns null
    // ---------------------------------------------------------------------------

    @Test
    void getProperty_undefined_returnsNull() {
        PropStore<Void> cached = CachingPropStore.wrapWithDefaultCache(store(Map.of()));
        assertNull(cached.getProperty("missing"));
    }

}
