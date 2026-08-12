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

class PropStoreLocatorTest {

    @Test
    void getSinglePropStoreOrLocator_nullDefaults_returnsLocalsDirectly() {
        PropStore<Void> locals = MapPropertyStore.createInstance(new HashMap<>());
        PropStore<Void> result = PropStoreLocator.getSinglePropStoreOrLocator(locals, null);
        assertSame(locals, result);
    }

    @Test
    void getSinglePropStoreOrLocator_nonNullDefaults_wrapsInLocator() {

        PropStore<Void> locals = MapPropertyStore.createInstance(new HashMap<>(Map.of("a", "1")));
        GetProperty defaults =
            MapPropertyStore.createInstance(new HashMap<>(Map.of("a", "default-1", "b", "default-2")));

        PropStore<Void> result = PropStoreLocator.getSinglePropStoreOrLocator(locals, defaults);

        assertInstanceOf(PropStoreLocator.class, result);
        assertEquals("1", result.getProperty("a"));
        assertEquals("default-2", result.getProperty("b"));

    }

    @Test
    void getSinglePropStoreOrLocator_nullLocals_throwsNullPointerException() {
        GetProperty defaults = MapPropertyStore.createInstance(new HashMap<>());
        assertThrows(NullPointerException.class, () -> PropStoreLocator.getSinglePropStoreOrLocator(null, defaults));
    }

    @Test
    void constructor_andGetProperty_fallsBackToDefaultsWhenLocalsMissing() {

        PropStore<Void> locals = MapPropertyStore.createInstance(new HashMap<>(Map.of("a", "1")));
        GetProperty defaults = MapPropertyStore.createInstance(new HashMap<>(Map.of("b", "2")));

        PropStoreLocator<Void> locator = new PropStoreLocator<>(locals, defaults);

        assertEquals("1", locator.getProperty("a"));
        assertEquals("2", locator.getProperty("b"));

    }

    @Test
    void toString_startsWithPropStorePrefix() {
        PropStore<Void> locals = MapPropertyStore.createInstance(new HashMap<>());
        GetProperty defaults = MapPropertyStore.createInstance(new HashMap<>());
        PropStoreLocator<Void> locator = new PropStoreLocator<>(locals, defaults);
        assertTrue(locator.toString().startsWith("PropStore: "));
    }

    @Test
    void commitChanges_delegatesToLocalsPropStore() {

        PropStore<Void> locals = MapPropertyStore.createInstance(new HashMap<>());
        GetProperty defaults = MapPropertyStore.createInstance(new HashMap<>());
        PropStoreLocator<Void> locator = new PropStoreLocator<>(locals, defaults);

        CommitResult result = locator.commitChanges(null);

        assertTrue(result.wasSuccessful());

    }

}
