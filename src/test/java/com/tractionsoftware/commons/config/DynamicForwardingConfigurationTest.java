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

package com.tractionsoftware.commons.config;

import com.tractionsoftware.commons.properties.SimpleProperties;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

public final class DynamicForwardingConfigurationTest {

    static Configuration cfg(String name, Map<String, String> props) {
        return SimpleProperties.asConfiguration(props, name);
    }

    static Configuration cfg(String name) {
        return SimpleProperties.getEmptyConfiguration(name);
    }

    // =====================================================================
    // Basic delegation
    // =====================================================================

    @Test
    void getProperty_delegatesToSupplier() {
        Configuration delegate = cfg("delegate", Map.of("key", "value"));
        DynamicForwardingConfiguration dfc = new DynamicForwardingConfiguration(() -> delegate);
        assertEquals("value", dfc.getProperty("key"));
    }

    @Test
    void getName_delegatesToSupplier() {
        Configuration delegate = cfg("my-config");
        DynamicForwardingConfiguration dfc = new DynamicForwardingConfiguration(() -> delegate);
        assertEquals("my-config", dfc.getName());
    }

    @Test
    void getPath_delegatesToSupplier() {
        Configuration delegate = cfg("my-config");
        DynamicForwardingConfiguration dfc = new DynamicForwardingConfiguration(() -> delegate);
        assertEquals(delegate.getPath(), dfc.getPath());
    }

    @Test
    void getTemplateSettings_delegatesToSupplier() {
        Configuration delegate = cfg("my-config");
        DynamicForwardingConfiguration dfc = new DynamicForwardingConfiguration(() -> delegate);
        assertEquals(delegate.getTemplateSettings(), dfc.getTemplateSettings());
    }

    // =====================================================================
    // Dynamic supplier — supplier returns different configs over time
    // =====================================================================

    @Test
    void getProperty_dynamicSupplier_reflectsCurrentDelegate() {
        AtomicReference<Configuration> ref = new AtomicReference<>(cfg("v1", Map.of("x", "1")));
        DynamicForwardingConfiguration dfc = new DynamicForwardingConfiguration(ref::get);

        assertEquals("1", dfc.getProperty("x"));

        // Switch delegate
        ref.set(cfg("v2", Map.of("x", "2")));
        assertEquals("2", dfc.getProperty("x"));
    }

    // =====================================================================
    // Null delegate throws NPE
    // =====================================================================

    @Test
    void nullDelegate_throwsNPE_onPropertyAccess() {
        DynamicForwardingConfiguration dfc = new DynamicForwardingConfiguration(() -> null);
        assertThrows(NullPointerException.class, () -> dfc.getProperty("anything"));
    }

    // =====================================================================
    // toString
    // =====================================================================

    @Test
    void toString_containsDynFwd() {
        DynamicForwardingConfiguration dfc = new DynamicForwardingConfiguration(() -> cfg("test"));
        String s = dfc.toString();
        assertNotNull(s);
        assertTrue(s.contains("dyn fwd"), s);
    }

    // =====================================================================
    // ForwardingConfiguration.wrap(Supplier) factory method
    // =====================================================================

    @Test
    void wrap_supplier_returnsDynamicForwarding() {
        Configuration delegate = cfg("wrapped", Map.of("a", "b"));
        Configuration wrapped = ForwardingConfiguration.wrap(() -> delegate);
        assertNotNull(wrapped);
        assertEquals("b", wrapped.getProperty("a"));
    }

    // =====================================================================
    // ForwardingConfiguration.withNewName
    // =====================================================================

    @Test
    void withNewName_differentName_createsNewConfig() {
        Configuration original = cfg("old-name");
        Configuration renamed = ForwardingConfiguration.withNewName(original, "new-name");
        assertEquals("new-name", renamed.getName());
    }

    @Test
    void withNewName_sameName_returnsSameInstance() {
        Configuration original = cfg("same");
        assertSame(original, ForwardingConfiguration.withNewName(original, "same"));
    }

    @Test
    void withNewName_null_returnsNull() {
        assertNull(ForwardingConfiguration.withNewName(null, "any"));
    }

}
