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

/**
 * Tests for {@link PropertyNameMappingGetProperty}.
 *
 * <p>Namespace semantics ("ns"):
 * <ul>
 *   <li>underlying store uses <em>prefixed</em> keys ("ns_key")</li>
 *   <li>callers use <em>plain</em> keys ("key")</li>
 *   <li>{@code wrapInNamespace(store,"ns").getProperty("key")} → {@code store.getProperty("ns_key")}</li>
 * </ul>
 *
 * <p>Prefix semantics ("pfx"):
 * <ul>
 *   <li>underlying store uses <em>plain</em> keys ("key")</li>
 *   <li>callers use <em>prefixed</em> keys ("pfx_key")</li>
 *   <li>{@code wrapInPrefix(store,"pfx").getProperty("pfx_key")} → {@code store.getProperty("key")}</li>
 * </ul>
 */
public final class PropertyNameMappingGetPropertyTest {

    private MapPropertyStore<Void> store;

    @BeforeEach
    void setUp() {
        store = MapPropertyStore.createDefaultInstance();
    }

    // =====================================================================
    // wrapInNamespace — basic get/has/names
    // =====================================================================

    @Test
    void wrapInNamespace_getProperty_translatesKey() {
        // store has "ns_key"; caller asks for "key"
        store.putProperty("ns_key", "value");
        GetProperty ns = PropertyNameMappingGetProperty.wrapInNamespace(store, "ns");
        assertEquals("value", ns.getProperty("key"));
    }

    @Test
    void wrapInNamespace_absent_returnsNull() {
        GetProperty ns = PropertyNameMappingGetProperty.wrapInNamespace(store, "ns");
        assertNull(ns.getProperty("missing"));
    }

    @Test
    void wrapInNamespace_callerUsesFullPrefixedName_returnsNull() {
        // Caller mistakenly uses "ns_key" → getActualPropertyName("ns_key") = "ns_ns_key" → absent
        store.putProperty("ns_key", "value");
        GetProperty ns = PropertyNameMappingGetProperty.wrapInNamespace(store, "ns");
        assertNull(ns.getProperty("ns_key"));
    }

    @Test
    void wrapInNamespace_hasProperty_true() {
        store.putProperty("ns_key", "value");
        GetProperty ns = PropertyNameMappingGetProperty.wrapInNamespace(store, "ns");
        assertTrue(ns.hasProperty("key"));
    }

    @Test
    void wrapInNamespace_hasProperty_false() {
        GetProperty ns = PropertyNameMappingGetProperty.wrapInNamespace(store, "ns");
        assertFalse(ns.hasProperty("missing"));
    }

    @Test
    void wrapInNamespace_getPropertyNames_stripsPrefix() {
        store.putProperty("ns_key", "value");
        GetProperty ns = PropertyNameMappingGetProperty.wrapInNamespace(store, "ns");
        // published name removes "ns_" → "key"
        assertTrue(ns.getPropertyNames().contains("key"), ns.getPropertyNames().toString());
    }

    @Test
    void wrapInNamespace_getPropertyNames_filtersOtherNamespaces() {
        store.putProperty("ns_key", "value");
        store.putProperty("other_key", "other");
        GetProperty ns = PropertyNameMappingGetProperty.wrapInNamespace(store, "ns");
        // "other_key" doesn't start with "ns_" → published name is null → filtered out
        assertFalse(ns.getPropertyNames().contains("other_key"), ns.getPropertyNames().toString());
    }

    @Test
    void wrapInNamespace_customSeparator() {
        store.putProperty("ns.key", "value");
        GetProperty ns = PropertyNameMappingGetProperty.wrapInNamespace(store, "ns", '.');
        assertEquals("value", ns.getProperty("key"));
    }

    // =====================================================================
    // wrapInPrefix — basic get/has/names
    // =====================================================================

    @Test
    void wrapInPrefix_getProperty_translatesKey() {
        // store has plain "key"; caller asks for "pfx_key"
        store.putProperty("key", "value");
        GetProperty pfx = PropertyNameMappingGetProperty.wrapInPrefix(store, "pfx");
        assertEquals("value", pfx.getProperty("pfx_key"));
    }

    @Test
    void wrapInPrefix_absent_returnsNull() {
        GetProperty pfx = PropertyNameMappingGetProperty.wrapInPrefix(store, "pfx");
        assertNull(pfx.getProperty("pfx_missing"));
    }

    @Test
    void wrapInPrefix_callerUsesPlainName_returnsNull() {
        // Caller uses "key" without prefix → getActualPropertyName("key") = null → null
        store.putProperty("key", "value");
        GetProperty pfx = PropertyNameMappingGetProperty.wrapInPrefix(store, "pfx");
        assertNull(pfx.getProperty("key"));
    }

    @Test
    void wrapInPrefix_getPropertyNames_addsPrefix() {
        store.putProperty("key", "value");
        GetProperty pfx = PropertyNameMappingGetProperty.wrapInPrefix(store, "pfx");
        // published name adds "pfx_" → "pfx_key"
        assertTrue(pfx.getPropertyNames().contains("pfx_key"), pfx.getPropertyNames().toString());
    }

    @Test
    void wrapInPrefix_hasProperty_true() {
        store.putProperty("key", "value");
        GetProperty pfx = PropertyNameMappingGetProperty.wrapInPrefix(store, "pfx");
        assertTrue(pfx.hasProperty("pfx_key"));
    }

    @Test
    void wrapInPrefix_customSeparator() {
        store.putProperty("key", "value");
        GetProperty pfx = PropertyNameMappingGetProperty.wrapInPrefix(store, "pfx", '.');
        assertEquals("value", pfx.getProperty("pfx.key"));
    }

    // =====================================================================
    // applyPropertyNameMapper — null safety
    // =====================================================================

    @Test
    void applyPropertyNameMapper_nullProps_returnsNull() {
        PropertyNameMapper mapper = SimplePropertyNameMapper.getNamespaceInstanceWithDefaultSeparator("ns");
        assertNull(PropertyNameMappingGetProperty.applyPropertyNameMapper(null, mapper));
    }

    @Test
    void applyPropertyNameMapper_nullMapper_returnsPropsSame() {
        assertSame(store, PropertyNameMappingGetProperty.applyPropertyNameMapper(store, null));
    }

    // =====================================================================
    // inverse-mapper unwrapping
    // =====================================================================

    @Test
    void applyInverseMapper_unwrapsToOriginal() {
        // Namespace wraps → prefix (its inverse) unwraps → back to original store
        store.putProperty("ns_key", "value");
        GetProperty ns = PropertyNameMappingGetProperty.wrapInNamespace(store, "ns");
        PropertyNameMapper prefixMapper = SimplePropertyNameMapper.getPrefixInstanceWithDefaultSeparator("ns");
        GetProperty result = PropertyNameMappingGetProperty.applyPropertyNameMapper(ns, prefixMapper);
        assertSame(store, result);
    }

    // =====================================================================
    // Double-wrap composition
    // =====================================================================

    @Test
    void doubleWrap_composesMappers() {
        // Wrap in namespace "a" then "b": store has "a_b_key", caller uses "key"
        store.putProperty("a_b_key", "value");
        GetProperty nsA = PropertyNameMappingGetProperty.wrapInNamespace(store, "a");
        GetProperty nsB = PropertyNameMappingGetProperty.wrapInNamespace(nsA, "b");
        assertEquals("value", nsB.getProperty("key"));
    }

    // =====================================================================
    // constructor and toString
    // =====================================================================

    @Test
    void constructor_directUse() {
        store.putProperty("ns_key", "v");
        PropertyNameMapper mapper = SimplePropertyNameMapper.getNamespaceInstanceWithDefaultSeparator("ns");
        PropertyNameMappingGetProperty wrapped = new PropertyNameMappingGetProperty(store, mapper);
        assertEquals("v", wrapped.getProperty("key"));
    }

    @Test
    void toString_containsGetProperty() {
        GetProperty ns = PropertyNameMappingGetProperty.wrapInNamespace(store, "ns");
        assertTrue(ns.toString().contains("GetProperty"), ns.toString());
    }

    // =====================================================================
    // Multiple values
    // =====================================================================

    @Test
    void wrapInNamespace_multipleKeys_allTranslated() {
        store.putProperty("ns_a", "1");
        store.putProperty("ns_b", "2");
        GetProperty ns = PropertyNameMappingGetProperty.wrapInNamespace(store, "ns");
        assertEquals("1", ns.getProperty("a"));
        assertEquals("2", ns.getProperty("b"));
    }

}
