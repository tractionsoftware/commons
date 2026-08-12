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

import com.tractionsoftware.commons.config.Configuration;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

public final class PropertyAdaptersTest {

    // =====================================================================
    // wrapNullIsEmptyString (PutProperty adapter)
    // =====================================================================

    @Test
    void wrapNullIsEmptyString_nullValue_storesEmpty() {
        MapPropertyStore<Void> store = MapPropertyStore.createDefaultInstance();
        PutProperty wrapped = PropertyAdapters.wrapNullIsEmptyString(store);
        wrapped.putProperty("k", null);
        // empty string was written instead of null
        assertEquals("", store.getProperty("k"));
    }

    @Test
    void wrapNullIsEmptyString_nonNullValue_passesThrough() {
        MapPropertyStore<Void> store = MapPropertyStore.createDefaultInstance();
        PutProperty wrapped = PropertyAdapters.wrapNullIsEmptyString(store);
        wrapped.putProperty("k", "hello");
        assertEquals("hello", store.getProperty("k"));
    }

    // =====================================================================
    // wrapEmptyStringIsNull (PropStore adapter)
    // =====================================================================

    @Test
    void wrapEmptyStringIsNull_emptyValue_readsAsNull() {
        Map<String,String> map = new LinkedHashMap<>(Map.of("k", ""));
        MapPropertyStore<Void> store = MapPropertyStore.createInstance(map);
        PropStore<Void> wrapped = PropertyAdapters.wrapEmptyStringIsNull(store);
        assertNull(wrapped.getProperty("k"));
    }

    @Test
    void wrapEmptyStringIsNull_putEmpty_storesNull() {
        MapPropertyStore<Void> store = MapPropertyStore.createDefaultInstance();
        PropStore<Void> wrapped = PropertyAdapters.wrapEmptyStringIsNull(store);
        wrapped.putProperty("k", "");
        assertNull(store.getProperty("k"));
    }

    @Test
    void wrapEmptyStringIsNull_nonEmpty_passesThrough() {
        Map<String,String> map = new LinkedHashMap<>(Map.of("k", "value"));
        MapPropertyStore<Void> store = MapPropertyStore.createInstance(map);
        PropStore<Void> wrapped = PropertyAdapters.wrapEmptyStringIsNull(store);
        assertEquals("value", wrapped.getProperty("k"));
    }

    // =====================================================================
    // functionToGetProperty
    // =====================================================================

    @Test
    void functionToGetProperty_appliesFunction() {
        GetProperty gp = PropertyAdapters.functionToGetProperty(name -> "computed-" + name);
        assertEquals("computed-foo", gp.getProperty("foo"));
    }

    @Test
    void functionToGetProperty_nullFunction_returnsNull() {
        GetProperty gp = PropertyAdapters.functionToGetProperty(null);
        assertNull(gp.getProperty("anything"));
    }

    @Test
    void functionToGetProperty_withDomain_exposesPropertyNames() {
        GetProperty gp = PropertyAdapters.functionToGetProperty(
            name -> name.toUpperCase(),
            Set.of("a", "b", "c")
        );
        assertTrue(gp.getPropertyNames().contains("a"));
        assertTrue(gp.getPropertyNames().contains("b"));
    }

    @Test
    void functionToGetProperty_withoutDomain_emptyPropertyNames() {
        GetProperty gp = PropertyAdapters.functionToGetProperty(name -> "x");
        assertTrue(gp.getPropertyNames().isEmpty());
    }

    // =====================================================================
    // getPropertyAtAsGetProperty
    // =====================================================================

    @Test
    void getPropertyAtAsGetProperty_numericKey_returnsValue() {
        GetPropertyAt at = new GetPropertyAt() {
            @Override
            public Object getPropertyAt(int index) {
                return "item-" + index;
            }
            @Override
            public Set<Integer> getPropertyNumbers() {
                return Set.of(0, 1, 2);
            }
        };
        GetProperty gp = PropertyAdapters.getPropertyAtAsGetProperty(at);
        assertEquals("item-0", gp.getProperty("0"));
        assertEquals("item-1", gp.getProperty("1"));
    }

    @Test
    void getPropertyAtAsGetProperty_nonNumericKey_returnsNull() {
        GetPropertyAt at = new GetPropertyAt() {
            @Override public Object getPropertyAt(int index) { return "x"; }
            @Override public Set<Integer> getPropertyNumbers() { return Set.of(); }
        };
        GetProperty gp = PropertyAdapters.getPropertyAtAsGetProperty(at);
        assertNull(gp.getProperty("notanumber"));
    }

    @Test
    void getPropertyAtAsGetProperty_propertyNames_areStringifiedIndices() {
        GetPropertyAt at = new GetPropertyAt() {
            @Override public Object getPropertyAt(int index) { return null; }
            @Override public Set<Integer> getPropertyNumbers() { return Set.of(0, 5, 10); }
        };
        GetProperty gp = PropertyAdapters.getPropertyAtAsGetProperty(at);
        var names = gp.getPropertyNames();
        assertTrue(names.contains("0"));
        assertTrue(names.contains("5"));
        assertTrue(names.contains("10"));
    }

    @Test
    void getPropertyAtAsGetProperty_toString_notNull() {
        GetPropertyAt at = new GetPropertyAt() {
            @Override public Object getPropertyAt(int index) { return null; }
            @Override public Set<Integer> getPropertyNumbers() { return Set.of(); }
        };
        assertNotNull(PropertyAdapters.getPropertyAtAsGetProperty(at).toString());
    }

    // =====================================================================
    // getPropertyAsConfiguration
    // =====================================================================

    @Test
    void getPropertyAsConfiguration_delegatesGetProperty() {
        GetProperty gp = SimpleProperties.asGetProperty(Map.of("k", "v"));
        Configuration cfg = PropertyAdapters.getPropertyAsConfiguration(gp);
        assertEquals("v", cfg.getProperty("k"));
    }

    @Test
    void getPropertyAsConfiguration_withPath_returnsPath() {
        GetProperty gp = SimpleProperties.asGetProperty(Map.of());
        Configuration cfg = PropertyAdapters.getPropertyAsConfiguration(gp, "/some/path");
        assertEquals("/some/path", cfg.getPath());
    }

    @Test
    void getPropertyAsConfiguration_nullProps_throwsNPE() {
        assertThrows(NullPointerException.class, () ->
            PropertyAdapters.getPropertyAsConfiguration(null));
    }

    @Test
    void getPropertyAsConfiguration_noPath_returnsNull() {
        GetProperty gp = SimpleProperties.asGetProperty(Map.of());
        Configuration cfg = PropertyAdapters.getPropertyAsConfiguration(gp);
        assertNull(cfg.getPath());
    }

}
