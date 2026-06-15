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
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

public final class DynamicForwardingGetPropertyTest {

    private static GetProperty props(String key, String value) {
        return SimpleProperties.asGetProperty(ImmutableMap.of(key, value));
    }

    @Test
    void wrap_delegatesToSupplier() {
        GetProperty delegate = props("x", "hello");
        GetProperty wrapped = DynamicForwardingGetProperty.wrap(() -> delegate);
        assertEquals("hello", wrapped.getProperty("x"));
    }

    @Test
    void wrap_dynamicSupplier_reflectsCurrentDelegate() {
        AtomicReference<GetProperty> ref = new AtomicReference<>(props("k", "v1"));
        GetProperty wrapped = DynamicForwardingGetProperty.wrap(ref::get);

        assertEquals("v1", wrapped.getProperty("k"));
        ref.set(props("k", "v2"));
        assertEquals("v2", wrapped.getProperty("k"));
    }

    @Test
    void wrap_nullFromSupplier_treatsAsEmpty() {
        // DynamicForwardingGetProperty uses requireNonNullElseGet(provider.get(), emptyGetProperty)
        GetProperty wrapped = DynamicForwardingGetProperty.wrap(() -> null);
        assertNull(wrapped.getProperty("anything"));
    }

    @Test
    void getPropertyNames_delegatesToCurrent() {
        GetProperty delegate = props("a", "1");
        GetProperty wrapped = DynamicForwardingGetProperty.wrap(() -> delegate);
        assertTrue(wrapped.getPropertyNames().contains("a"));
    }

    @Test
    void toString_containsDynFwd() {
        GetProperty wrapped = DynamicForwardingGetProperty.wrap(() -> SimpleProperties.emptyGetProperty());
        String s = wrapped.toString();
        assertTrue(s.contains("dyn fwd"), s);
    }

    @Test
    void hasProperty_delegatesToCurrent() {
        GetProperty delegate = props("p", "v");
        GetProperty wrapped = DynamicForwardingGetProperty.wrap(() -> delegate);
        assertTrue(wrapped.hasProperty("p"));
        assertFalse(wrapped.hasProperty("q"));
    }

    @Test
    void isEmpty_emptyDelegate_true() {
        GetProperty wrapped = DynamicForwardingGetProperty.wrap(SimpleProperties::emptyGetProperty);
        assertTrue(wrapped.isEmpty());
    }

}
