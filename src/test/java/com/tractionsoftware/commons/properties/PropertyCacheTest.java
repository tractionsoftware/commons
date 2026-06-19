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
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

class PropertyCacheTest {

    // ---------------------------------------------------------------------------
    // CacheEntry
    // ---------------------------------------------------------------------------

    @Test
    void cacheEntry_nullInstance_get_returnsNull() {
        PropertyCache.CacheEntry<Object> entry = PropertyCache.CacheEntry.getInstance(null, null);
        assertNull(entry.get());
        assertSame(PropertyCache.CacheEntry.NULL_INSTANCE, entry);
    }

    @Test
    void cacheEntry_withValue_get_returnsValue() {
        PropertyCache.CacheEntry<String> entry = PropertyCache.CacheEntry.getInstance("hello", "raw");
        assertEquals("hello", entry.get());
    }

    @Test
    void cacheEntry_isValid_sameRawValue_returnsTrue() {
        PropertyCache.CacheEntry<String> entry = PropertyCache.CacheEntry.getInstance("v", "raw");
        assertTrue(entry.isValid("raw"));
    }

    @Test
    void cacheEntry_isValid_differentRawValue_returnsFalse() {
        PropertyCache.CacheEntry<String> entry = PropertyCache.CacheEntry.getInstance("v", "raw1");
        assertFalse(entry.isValid("raw2"));
    }

    @Test
    void cacheEntry_isValid_nullRawValue_withNull_returnsTrue() {
        PropertyCache.CacheEntry<String> entry = PropertyCache.CacheEntry.getInstance(null, null);
        assertTrue(entry.isValid(null));
    }

    // ---------------------------------------------------------------------------
    // createInstance factories
    // ---------------------------------------------------------------------------

    @Test
    void createInstance_returnsNonNull() {
        assertNotNull(PropertyCache.createInstance());
    }

    @Test
    void createInstanceValidatingOnRead_returnsNonNull() {
        assertNotNull(PropertyCache.createInstanceValidatingOnRead());
    }

    @Test
    void createInstanceWithStatsCollection_returnsNonNull() {
        assertNotNull(PropertyCache.createInstanceWithStatsCollection());
    }

    @Test
    void createInstance_booleans_returnsNonNull() {
        assertNotNull(PropertyCache.createInstance(false, false));
        assertNotNull(PropertyCache.createInstance(true, false));
        assertNotNull(PropertyCache.createInstance(false, true));
        assertNotNull(PropertyCache.createInstance(true, true));
    }

    // ---------------------------------------------------------------------------
    // toString
    // ---------------------------------------------------------------------------

    @Test
    void toString_containsPropertyCache() {
        assertTrue(PropertyCache.createInstance().toString().contains("PropertyCache"));
    }

    // ---------------------------------------------------------------------------
    // getOrLoadProperty — cache miss loads from delegate
    // ---------------------------------------------------------------------------

    @Test
    void getOrLoadProperty_cacheMiss_loadsFromStore() {
        MapPropertyStore<Void> store = MapPropertyStore.createInstance(Map.of("k", "v"));
        PropertyCache cache = PropertyCache.createInstance();
        AtomicInteger loadCount = new AtomicInteger(0);

        PropertyLoader<String> loader = new PropertyLoader<>() {
            @Override
            public String apply(String name, String rawValue) {
                loadCount.incrementAndGet();
                return rawValue;
            }

            @Override
            public String cast(Object o) {
                return (String) o;
            }
        };

        String result = cache.getOrLoadProperty(store.asFunction(), "k", loader);
        assertEquals("v", result);
        assertEquals(1, loadCount.get());
    }

    @Test
    void getOrLoadProperty_cacheHit_doesNotReload() {
        MapPropertyStore<Void> store = MapPropertyStore.createInstance(new HashMap<>(Map.of("k", "v")));
        PropertyCache cache = PropertyCache.createInstance();
        AtomicInteger loadCount = new AtomicInteger(0);

        PropertyLoader<String> loader = new PropertyLoader<>() {
            @Override
            public String apply(String name, String rawValue) {
                loadCount.incrementAndGet();
                return rawValue;
            }

            @Override
            public String cast(Object o) {
                return (String) o;
            }
        };

        cache.getOrLoadProperty(store.asFunction(), "k", loader);
        cache.getOrLoadProperty(store.asFunction(), "k", loader);
        assertEquals(1, loadCount.get()); // should only load once
    }

    @Test
    void getOrLoadProperty_missingProperty_returnsNull() {
        MapPropertyStore<Void> store = MapPropertyStore.createInstance(Map.of());
        PropertyCache cache = PropertyCache.createInstance();

        PropertyLoader<String> loader = new PropertyLoader<>() {
            @Override
            public String apply(String name, String rawValue) {
                return rawValue; // null
            }

            @Override
            public String cast(Object o) {
                return (String) o;
            }
        };

        String result = cache.getOrLoadProperty(store.asFunction(), "missing", loader);
        assertNull(result);
    }

    // ---------------------------------------------------------------------------
    // getOrLoadProperty — validate-on-read reloads when value changes
    // ---------------------------------------------------------------------------

    @Test
    void getOrLoadProperty_validateOnRead_reloadsWhenValueChanges() {
        Map<String, String> backing = new HashMap<>(Map.of("k", "v1"));
        MapPropertyStore<Void> store = MapPropertyStore.createInstance(backing);
        PropertyCache cache = PropertyCache.createInstanceValidatingOnRead();
        AtomicInteger loadCount = new AtomicInteger(0);

        PropertyLoader<String> loader = new PropertyLoader<>() {
            @Override
            public String apply(String name, String rawValue) {
                loadCount.incrementAndGet();
                return rawValue;
            }

            @Override
            public String cast(Object o) {
                return (String) o;
            }
        };

        assertEquals("v1", cache.getOrLoadProperty(store.asFunction(), "k", loader));
        assertEquals(1, loadCount.get());

        // Change value in store
        store.putProperty("k", "v2");

        // Validate-on-read should detect mismatch and reload
        assertEquals("v2", cache.getOrLoadProperty(store.asFunction(), "k", loader));
        assertEquals(2, loadCount.get());
    }

    // ---------------------------------------------------------------------------
    // invalidate / invalidateAll
    // ---------------------------------------------------------------------------

    @Test
    void invalidate_byName_causesReload() {
        MapPropertyStore<Void> store = MapPropertyStore.createInstance(new HashMap<>(Map.of("k", "v1")));
        PropertyCache cache = PropertyCache.createInstance();
        AtomicInteger loadCount = new AtomicInteger(0);

        PropertyLoader<String> loader = new PropertyLoader<>() {
            @Override
            public String apply(String name, String rawValue) {
                loadCount.incrementAndGet();
                return rawValue;
            }

            @Override
            public String cast(Object o) {
                return (String) o;
            }
        };

        cache.getOrLoadProperty(store.asFunction(), "k", loader);
        assertEquals(1, loadCount.get());

        cache.invalidate("k");
        store.putProperty("k", "v2");

        String result = cache.getOrLoadProperty(store.asFunction(), "k", loader);
        assertEquals("v2", result);
        assertEquals(2, loadCount.get());
    }

    @Test
    void invalidateAll_causesReloadOfAllKeys() {
        MapPropertyStore<Void> store = MapPropertyStore.createInstance(new HashMap<>(Map.of("a", "1", "b", "2")));
        PropertyCache cache = PropertyCache.createInstance();
        AtomicInteger loadCount = new AtomicInteger(0);

        PropertyLoader<String> loader = new PropertyLoader<>() {
            @Override
            public String apply(String name, String rawValue) {
                loadCount.incrementAndGet();
                return rawValue;
            }

            @Override
            public String cast(Object o) {
                return (String) o;
            }
        };

        cache.getOrLoadProperty(store.asFunction(), "a", loader);
        cache.getOrLoadProperty(store.asFunction(), "b", loader);
        assertEquals(2, loadCount.get());

        cache.invalidateAll();

        cache.getOrLoadProperty(store.asFunction(), "a", loader);
        cache.getOrLoadProperty(store.asFunction(), "b", loader);
        assertEquals(4, loadCount.get());
    }

    @Test
    void invalidate_byIterable_causesReloadOfSpecifiedKeys() {
        MapPropertyStore<Void> store = MapPropertyStore.createInstance(new HashMap<>(Map.of("a", "1", "b", "2")));
        PropertyCache cache = PropertyCache.createInstance();
        AtomicInteger loadCount = new AtomicInteger(0);

        PropertyLoader<String> loader = new PropertyLoader<>() {
            @Override
            public String apply(String name, String rawValue) {
                loadCount.incrementAndGet();
                return rawValue;
            }

            @Override
            public String cast(Object o) {
                return (String) o;
            }
        };

        cache.getOrLoadProperty(store.asFunction(), "a", loader);
        cache.getOrLoadProperty(store.asFunction(), "b", loader);
        assertEquals(2, loadCount.get());

        cache.invalidate(List.of("a"));

        // a should reload, b should not
        cache.getOrLoadProperty(store.asFunction(), "a", loader);
        cache.getOrLoadProperty(store.asFunction(), "b", loader);
        assertEquals(3, loadCount.get());
    }

}
