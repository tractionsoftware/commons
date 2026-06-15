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

import static org.junit.jupiter.api.Assertions.*;

public final class PropertyNameMappingGetPutPropertyTest {

    private MapPropertyStore<Void> store;

    @BeforeEach
    void setUp() {
        store = new MapPropertyStore<>();
    }

    // =====================================================================
    // wrapInNamespace — get / put
    // namespace: store has "ns_key", callers use "key"
    // =====================================================================

    @Test
    void wrapInNamespace_getProperty_translatesKey() {
        store.putProperty("ns_key", "val");
        GetPutProperty ns = PropertyNameMappingGetPutProperty.wrapInNamespace(store, "ns");
        assertEquals("val", ns.getProperty("key"));
    }

    @Test
    void wrapInNamespace_putProperty_writesToPrefixedKey() {
        GetPutProperty ns = PropertyNameMappingGetPutProperty.wrapInNamespace(store, "ns");
        ns.putProperty("key", "val");
        // put("key") maps to store.put("ns_key")
        assertEquals("val", store.getProperty("ns_key"));
    }

    @Test
    void wrapInNamespace_customSeparator() {
        GetPutProperty ns = PropertyNameMappingGetPutProperty.wrapInNamespace(store, "ns", '.');
        ns.putProperty("key", "val");
        assertEquals("val", store.getProperty("ns.key"));
    }

    // =====================================================================
    // wrapInPrefix — get / put
    // prefix: store has plain "key", callers use "pfx_key"
    // =====================================================================

    @Test
    void wrapInPrefix_getProperty_translatesKey() {
        store.putProperty("key", "val");
        GetPutProperty pfx = PropertyNameMappingGetPutProperty.wrapInPrefix(store, "pfx");
        assertEquals("val", pfx.getProperty("pfx_key"));
    }

    @Test
    void wrapInPrefix_putProperty_writesToPlainKey() {
        GetPutProperty pfx = PropertyNameMappingGetPutProperty.wrapInPrefix(store, "pfx");
        pfx.putProperty("pfx_key", "val");
        // put("pfx_key") strips prefix → store.put("key")
        assertEquals("val", store.getProperty("key"));
    }

    @Test
    void wrapInPrefix_customSeparator() {
        GetPutProperty pfx = PropertyNameMappingGetPutProperty.wrapInPrefix(store, "pfx", '.');
        pfx.putProperty("pfx.key", "val");
        assertEquals("val", store.getProperty("key"));
    }

    // =====================================================================
    // applyPropertyNameMapper — null safety
    // =====================================================================

    @Test
    void applyPropertyNameMapper_nullProps_returnsNull() {
        PropertyNameMapper mapper = SimplePropertyNameMapper.getNamespaceInstanceWithDefaultSeparator("ns");
        assertNull(PropertyNameMappingGetPutProperty.applyPropertyNameMapper(null, mapper));
    }

    @Test
    void applyPropertyNameMapper_nullMapper_returnsSameProps() {
        assertSame(store, PropertyNameMappingGetPutProperty.applyPropertyNameMapper(store, null));
    }

    // =====================================================================
    // Inverse-mapper unwrapping
    // =====================================================================

    @Test
    void applyInverseMapper_unwrapsToOriginal() {
        GetPutProperty ns = PropertyNameMappingGetPutProperty.wrapInNamespace(store, "ns");
        PropertyNameMapper prefixMapper = SimplePropertyNameMapper.getPrefixInstanceWithDefaultSeparator("ns");
        GetPutProperty result = PropertyNameMappingGetPutProperty.applyPropertyNameMapper(ns, prefixMapper);
        assertSame(store, result);
    }

    // =====================================================================
    // hasProperty / getPropertyNames
    // =====================================================================

    @Test
    void wrapInNamespace_hasProperty_true() {
        store.putProperty("ns_key", "val");
        GetPutProperty ns = PropertyNameMappingGetPutProperty.wrapInNamespace(store, "ns");
        assertTrue(ns.hasProperty("key"));
    }

    @Test
    void wrapInNamespace_hasProperty_false() {
        GetPutProperty ns = PropertyNameMappingGetPutProperty.wrapInNamespace(store, "ns");
        assertFalse(ns.hasProperty("missing"));
    }

    @Test
    void wrapInNamespace_getPropertyNames_stripsPrefix() {
        store.putProperty("ns_key", "val");
        GetPutProperty ns = PropertyNameMappingGetPutProperty.wrapInNamespace(store, "ns");
        // published name removes "ns_" → "key"
        assertTrue(ns.getPropertyNames().contains("key"), ns.getPropertyNames().toString());
    }

    // =====================================================================
    // toString
    // =====================================================================

    @Test
    void toString_containsGetPutProperty() {
        GetPutProperty ns = PropertyNameMappingGetPutProperty.wrapInNamespace(store, "ns");
        assertTrue(ns.toString().contains("GetPutProperty"), ns.toString());
    }

    // =====================================================================
    // Double-wrap composition
    // =====================================================================

    @Test
    void doubleWrap_get_composesMappers() {
        // Store has "a_b_key"; callers use "key" through two namespace layers
        store.putProperty("a_b_key", "v");
        GetPutProperty nsA = PropertyNameMappingGetPutProperty.wrapInNamespace(store, "a");
        GetPutProperty nsB = PropertyNameMappingGetPutProperty.wrapInNamespace(nsA, "b");
        assertEquals("v", nsB.getProperty("key"));
    }

    @Test
    void doubleWrap_put_composesMappers() {
        GetPutProperty nsA = PropertyNameMappingGetPutProperty.wrapInNamespace(store, "a");
        GetPutProperty nsB = PropertyNameMappingGetPutProperty.wrapInNamespace(nsA, "b");
        nsB.putProperty("key", "val");
        assertEquals("val", store.getProperty("a_b_key"));
    }

}
