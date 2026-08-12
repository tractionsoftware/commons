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

class DynamicForwardingGetPutPropertyTest {

    @Test
    void wrap_getProperty_delegatesToSuppliedGetPutProperty() {
        GetPutProperty backing = MapPropertyStore.createInstance(new HashMap<>(Map.of("a", "1")));
        GetPutProperty wrapped = DynamicForwardingGetPutProperty.wrap(() -> backing);
        assertEquals("1", wrapped.getProperty("a"));
    }

    @Test
    void wrap_putProperty_updatesSuppliedGetPutProperty() {
        Map<String,String> map = new HashMap<>();
        GetPutProperty backing = MapPropertyStore.createInstance(map);
        GetPutProperty wrapped = DynamicForwardingGetPutProperty.wrap(() -> backing);
        wrapped.putProperty("k", "v");
        assertEquals("v", map.get("k"));
    }

    @Test
    void constructor_reflectsCurrentSupplierValueOnEachCall() {

        GetPutProperty first = MapPropertyStore.createInstance(new HashMap<>(Map.of("a", "1")));
        GetPutProperty second = MapPropertyStore.createInstance(new HashMap<>(Map.of("a", "2")));
        boolean[] useSecond = {false};

        DynamicForwardingGetPutProperty wrapped =
            new DynamicForwardingGetPutProperty(() -> useSecond[0] ? second : first);

        assertEquals("1", wrapped.getProperty("a"));
        useSecond[0] = true;
        assertEquals("2", wrapped.getProperty("a"));

    }

    @Test
    void toString_includesProviderAndCurrentDelegate() {
        GetPutProperty backing = MapPropertyStore.createInstance(new HashMap<>(Map.of("a", "1")));
        DynamicForwardingGetPutProperty wrapped = new DynamicForwardingGetPutProperty(() -> backing);
        assertTrue(wrapped.toString().contains(backing.toString()));
    }

    @Test
    void delegate_nullSuppliedDelegate_throwsNullPointerException() {
        DynamicForwardingGetPutProperty wrapped = new DynamicForwardingGetPutProperty(() -> null);
        assertThrows(NullPointerException.class, () -> wrapped.getProperty("a"));
    }

}
