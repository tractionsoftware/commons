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

class StaticForwardingGetPropertyTest {

    private static MapPropertyStore<Void> store(Map<String, String> map) {
        return MapPropertyStore.createInstance(new HashMap<>(map));
    }

    // ---------------------------------------------------------------------------
    // StaticForwardingGetProperty.wrap
    // ---------------------------------------------------------------------------

    @Test
    void wrap_returnsNonNull() {
        GetProperty wrapped = StaticForwardingGetProperty.wrap(store(Map.of()));
        assertNotNull(wrapped);
    }

    @Test
    void wrap_getProperty_delegatesToDelegate() {
        GetProperty wrapped = StaticForwardingGetProperty.wrap(store(Map.of("key", "val")));
        assertEquals("val", wrapped.getProperty("key"));
    }

    @Test
    void wrap_getProperty_undefined_returnsNull() {
        GetProperty wrapped = StaticForwardingGetProperty.wrap(store(Map.of()));
        assertNull(wrapped.getProperty("missing"));
    }

    @Test
    void wrap_getPropertyNames_delegatesToDelegate() {
        GetProperty wrapped = StaticForwardingGetProperty.wrap(store(Map.of("a", "1", "b", "2")));
        assertTrue(wrapped.getPropertyNames().contains("a"));
        assertTrue(wrapped.getPropertyNames().contains("b"));
    }

    @Test
    void wrap_isStaticallySpecifiedDelegate_returnsTrue() {
        StaticForwardingGetProperty wrapped = new StaticForwardingGetProperty(store(Map.of()));
        assertTrue(wrapped.isStaticallySpecifiedDelegate());
    }

    @Test
    void toString_containsGetProperty() {
        GetProperty wrapped = StaticForwardingGetProperty.wrap(store(Map.of()));
        assertTrue(wrapped.toString().contains("GetProperty"),
            "toString should mention 'GetProperty' but was: " + wrapped.toString());
    }

    // ---------------------------------------------------------------------------
    // StaticForwardingGetPutProperty.wrap
    // ---------------------------------------------------------------------------

    @Test
    void wrapGetPut_returnsNonNull() {
        GetPutProperty wrapped = StaticForwardingGetPutProperty.wrap(store(Map.of()));
        assertNotNull(wrapped);
    }

    @Test
    void wrapGetPut_getProperty_delegatesToDelegate() {
        GetPutProperty wrapped = StaticForwardingGetPutProperty.wrap(store(Map.of("k", "v")));
        assertEquals("v", wrapped.getProperty("k"));
    }

    @Test
    void wrapGetPut_putProperty_updatesDelegate() {
        MapPropertyStore<Void> delegate = store(Map.of());
        GetPutProperty wrapped = StaticForwardingGetPutProperty.wrap(delegate);
        wrapped.putProperty("x", "99");
        assertEquals("99", delegate.getProperty("x"));
    }

    @Test
    void wrapGetPut_isStaticallySpecifiedDelegate_returnsTrue() {
        StaticForwardingGetPutProperty wrapped = new StaticForwardingGetPutProperty(store(Map.of()));
        assertTrue(wrapped.isStaticallySpecifiedDelegate());
    }

    @Test
    void wrapGetPut_toString_containsGetPutProperty() {
        GetPutProperty wrapped = StaticForwardingGetPutProperty.wrap(store(Map.of()));
        assertTrue(wrapped.toString().contains("GetPutProperty"),
            "toString should mention 'GetPutProperty' but was: " + wrapped.toString());
    }

}
