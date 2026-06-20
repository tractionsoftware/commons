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
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;

/**
 * These are some basic tests for the CachingObjectStore class.
 *
 * @author Dave Shepperton
 */
public class CachingGetPropertyTest {

    protected static final GetProperty singletonGetProperty(String name, String value) {
        return SimpleProperties.asGetProperty(ImmutableMap.of(name, value));
    }

    protected static final class TestLoaderObject {

        public static final PropertyLoader<TestLoaderObject> LOADER =
            new AbstractPropertyLoader<>(TestLoaderObject.class) {
                @Override
                public final TestLoaderObject apply(String name, String rawValue) {
                    return new TestLoaderObject(name, rawValue);
                }
            };

        /**
         * Assert two non-null objects with unequal values.
         *
         * @param o1
         *     first object.
         * @param o2
         *     second object.
         * @param v1
         *     the expected value for the first object.
         * @param v2
         *     the expected value for the second object.
         */
        public static final void assertNotNullNotEqualsAndValuesEquals(TestLoaderObject o1, TestLoaderObject o2, String v1, String v2) {
            assertNotNull(o1, "object 1");
            assertNotNull(o2, "object 2");
            assertNotEquals(o1, o2);
            assertEquals(v1, o1.val);
            assertEquals(v2, o2.val);
        }

        /**
         * Assert two non-null objects are equals but not identical.
         *
         * @param o1
         *     first object.
         * @param o2
         *     second object.
         */
        public static final void assertEqualsNotSame(TestLoaderObject o1, TestLoaderObject o2) {
            assertNotNull(o1, "object 1");
            assertNotNull(o2, "object 2");
            assertNotSame(o1, o2);
            assertEquals(o1, o2);
        }

        /**
         * Assert two non-null identical objects.
         *
         * @param o1
         *     first object.
         * @param o2
         *     second object.
         */
        public static final void assertSameNotNull(TestLoaderObject o1, TestLoaderObject o2) {
            assertNotNull(o1, "object 1");
            assertNotNull(o2, "object 2");
            assertSame(o1, o2);
        }

        private final String name;

        private final String val;

        TestLoaderObject(String name, String val) {
            this.name = name;
            this.val = val;
        }

        @Override
        public final boolean equals(Object obj) {
            if (obj instanceof TestLoaderObject otherObj &&
                Objects.equals(name, otherObj.name) &&
                Objects.equals(val, otherObj.val)) {
                return true;
            }
            return false;
        }

        @Override
        public final int hashCode() {
            return Objects.hash(val);
        }

        @Override
        public final String toString() {
            return "TestLoaderObject: " + name + "=" + val;
        }

    }

    protected static final GetProperty singletonCachingGetProperty(String name, String value) {
        return singletonGetProperty(name, value).withCache();
    }

    @Test
    public void testSameObjectOnMultipleCalls() {
        GetProperty store = singletonCachingGetProperty("test", "123");
        TestLoaderObject result1 = store.getProperty("test", TestLoaderObject.LOADER, true);
        TestLoaderObject result2 = store.getProperty("test", TestLoaderObject.LOADER, true);
        TestLoaderObject.assertSameNotNull(result1, result2);
    }

    @Test
    public void testDifferentObjectOnMultipleCallsSkippingCache() {
        GetProperty store = singletonCachingGetProperty("test", "123");
        TestLoaderObject result1 = store.getProperty("test", TestLoaderObject.LOADER, true);
        TestLoaderObject result2 = store.getProperty("test", TestLoaderObject.LOADER, false);
        TestLoaderObject.assertEqualsNotSame(result1, result2);
    }

    @Test
    public void testSameObjectAfterChangeNoOnReadValidation() {

        Map<String,String> map = new HashMap<>();
        map.put("test", "123");
        PropStore<?> rawStore = new MapPropertyStore<>("test", map);

        GetProperty store = rawStore.withCache(PropertyCache.createInstance());

        TestLoaderObject result1 = store.getProperty("test", TestLoaderObject.LOADER, true);
        map.put("test", "789");
        TestLoaderObject result2 = store.getProperty("test", TestLoaderObject.LOADER, true);

        // Without validate-on-read, the cache doesn't throw out the stale value.
        TestLoaderObject.assertSameNotNull(result1, result2);

    }

    @Test
    public void testDifferentObjectAfterChangeWithOnReadValidation() {

        Map<String,String> map = new HashMap<>();
        map.put("test", "123");
        PropStore<?> rawStore = new MapPropertyStore<>("test", map);

        GetProperty store = rawStore.withCache();

        TestLoaderObject result1 = store.getProperty("test", TestLoaderObject.LOADER, true);
        map.put("test", "789");
        TestLoaderObject result2 = store.getProperty("test", TestLoaderObject.LOADER, true);

        TestLoaderObject.assertNotNullNotEqualsAndValuesEquals(result1, result2, "123", "789");

    }

    @Test
    public void testSameAfterChangeAndRevertWithOnReadValidation() {

        Map<String,String> map = new HashMap<>();
        GetProperty store = new MapPropertyStore<>("test", map).withCache();

        map.put("test", "123");
        TestLoaderObject result1 = store.getProperty("test", TestLoaderObject.LOADER, true);

        // Change the mapping for "test", and then change it back.
        map.put("test", "789");
        map.put("test", "123");
        TestLoaderObject result2 = store.getProperty("test", TestLoaderObject.LOADER, true);

        // With a validate-on-read cache, since we didn't read the object before
        // the original mapping was restored, the second get will still read the
        // same result object as the first get.
        TestLoaderObject.assertSameNotNull(result1, result2);

    }

    @Test
    public void testEqualsAfterChangeTestRevertWithOnReadValidation() {

        Map<String,String> map = new HashMap<>();
        map.put("test", "123");
        PropStore<?> rawStore = new MapPropertyStore<>("test", map);

        GetProperty store = rawStore.withCache();

        TestLoaderObject result1 = store.getProperty("test", TestLoaderObject.LOADER, true);
        map.put("test", "789");
        // Throw away.
        store.getProperty("test", TestLoaderObject.LOADER, true);
        map.put("test", "123");
        TestLoaderObject result2 = store.getProperty("test", TestLoaderObject.LOADER, true);

        // These objects will not be the same instances, but should be equal.
        TestLoaderObject.assertEqualsNotSame(result1, result2);

    }

    @Test
    public void testDifferentObjectAndUnequalAfterChangeAndManualCacheClear() {

        Map<String,String> map = new HashMap<>();
        map.put("test", "123");
        PropStore<?> rawStore = new MapPropertyStore<>("test", map);

        PropertyCache cache = PropertyCache.createInstance();
        GetProperty store = rawStore.withCache(cache);

        TestLoaderObject result1 = store.getProperty("test", TestLoaderObject.LOADER, true);
        map.put("test", "789");
        cache.invalidate("test");
        TestLoaderObject result2 = store.getProperty("test", TestLoaderObject.LOADER, true);

        // The objects are not equal.
        TestLoaderObject.assertNotNullNotEqualsAndValuesEquals(result1, result2, "123", "789");

    }

    @Test
    public void testDifferentEqualObjectAfterManualCacheClear() {

        Map<String,String> map = new HashMap<>();
        map.put("test", "123");
        PropStore<?> rawStore = new MapPropertyStore<>("test", map);

        PropertyCache cache = PropertyCache.createInstance();
        GetProperty store = rawStore.withCache(cache);

        TestLoaderObject result1 = store.getProperty("test", TestLoaderObject.LOADER, true);
        cache.invalidate("test");
        TestLoaderObject result2 = store.getProperty("test", TestLoaderObject.LOADER, true);

        assertNotSame(result1, result2);
        assertEquals(result1, result2);

    }

    @Test
    public void testUnequalAfterChangeAndManualClearMulti() {

        Map<String,String> map = new HashMap<>();
        map.put("test", "123");
        PropStore<?> rawStore = new MapPropertyStore<>("test", map);

        PropertyCache cache = PropertyCache.createInstance();
        GetProperty store = rawStore.withCache(cache);

        TestLoaderObject result1 = store.getProperty("test", TestLoaderObject.LOADER, true);
        map.put("test", "789");

        cache.invalidate(ImmutableSet.of("test", "foo"));

        TestLoaderObject result2 = store.getProperty("test", TestLoaderObject.LOADER, true);

        // Without validate-on-read, but after manually clearing the value for the
        // "test" key, the second get will load a completely different object with
        // the correct new value.
        TestLoaderObject.assertNotNullNotEqualsAndValuesEquals(result1, result2, "123", "789");

    }

}
