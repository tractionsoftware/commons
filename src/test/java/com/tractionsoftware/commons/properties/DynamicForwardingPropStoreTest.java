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

class DynamicForwardingPropStoreTest {

    @Test
    void wrap_getProperty_delegatesToSuppliedPropStore() {
        PropStore<Void> backing = MapPropertyStore.createInstance(new HashMap<>(Map.of("a", "1")));
        PropStore<Void> wrapped = DynamicForwardingPropStore.wrap(() -> backing);
        assertEquals("1", wrapped.getProperty("a"));
    }

    @Test
    void wrap_commitChanges_delegatesToSuppliedPropStore() {
        PropStore<Void> backing = MapPropertyStore.createInstance(new HashMap<>());
        PropStore<Void> wrapped = DynamicForwardingPropStore.wrap(() -> backing);
        CommitResult result = wrapped.commitChanges(null);
        assertTrue(result.wasSuccessful());
    }

    @Test
    void toString_includesProviderAndCurrentDelegate() {
        PropStore<Void> backing = MapPropertyStore.createInstance(new HashMap<>());
        DynamicForwardingPropStore<Void> wrapped = new DynamicForwardingPropStore<>(() -> backing);
        assertTrue(wrapped.toString().contains(backing.toString()));
    }

    @Test
    void delegate_nullSuppliedDelegate_throwsNullPointerException() {
        DynamicForwardingPropStore<Void> wrapped = new DynamicForwardingPropStore<>(() -> null);
        assertThrows(NullPointerException.class, () -> wrapped.getProperty("a"));
    }

}
