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
import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Dave Shepperton
 */
public final class ImmutablePropStoreTest {

    @Test
    public void testPutPropertyFailsWithUnsupportedOperationException() {
        Exception actual;
        try {
            SimpleProperties.asPropStore(new HashMap<>()).toImmutable().putProperty("foo", "bar");
            actual = null;
        }
        catch (UnsupportedOperationException e) {
            actual = e;
        }
        assertNotNull(actual);
    }

    @Test
    public void testCommitChangesFailsReadOnlyStore() {
        CommitResult result = SimpleProperties.asPropStore(new HashMap<>())
            .toImmutable()
            .commitChanges(new Object());
        assertEquals(CommitResult.StandardFailureStatus.READ_ONLY_STORE, result.getStatus());
    }

    // -------------------------------------------------------------------------
    // toString
    // -------------------------------------------------------------------------

    @Test
    public void toString_containsDelegate() {
        PropStore<Object> store = SimpleProperties.asPropStore(new HashMap<>()).toImmutable();
        String s = store.toString();
        assertNotNull(s);
        // ImmutablePropStore.toString() wraps the delegate description
        assertTrue(s.contains("immutable") || s.contains("PropStore"), "unexpected toString: " + s);
    }

    // -------------------------------------------------------------------------
    // clearLocalProperties — returns false (ImmutablePropStore override)
    // -------------------------------------------------------------------------

    @Test
    public void clearLocalProperties_returnsFalse() {
        PropStore<Object> store = SimpleProperties.asPropStore(new HashMap<>()).toImmutable();
        assertFalse(store.clearLocalProperties());
    }

    // -------------------------------------------------------------------------
    // toImmutable — returns self
    // -------------------------------------------------------------------------

    @Test
    public void toImmutable_returnsSelf() {
        PropStore<Object> store = SimpleProperties.asPropStore(new HashMap<>()).toImmutable();
        assertSame(store, store.toImmutable());
    }

    // -------------------------------------------------------------------------
    // Each overridden mutation method throws UnsupportedOperationException
    // -------------------------------------------------------------------------

    private static PropStore<Object> immutable() {
        return SimpleProperties.asPropStore(new HashMap<>()).toImmutable();
    }

    @Test
    public void putBooleanProperty_throwsUnsupportedOperationException() {
        assertThrows(UnsupportedOperationException.class, () -> immutable().putBooleanProperty("k", true));
    }

    @Test
    public void putIntProperty_throwsUnsupportedOperationException() {
        assertThrows(UnsupportedOperationException.class, () -> immutable().putIntProperty("k", 1));
    }

    @Test
    public void putLongProperty_throwsUnsupportedOperationException() {
        assertThrows(UnsupportedOperationException.class, () -> immutable().putLongProperty("k", 1L));
    }

    @Test
    public void putDoubleProperty_throwsUnsupportedOperationException() {
        assertThrows(UnsupportedOperationException.class, () -> immutable().putDoubleProperty("k", 1.0));
    }

    @Test
    public void removeProperty_throwsUnsupportedOperationException() {
        assertThrows(UnsupportedOperationException.class, () -> immutable().removeProperty("k"));
    }

    @Test
    public void putAllProperties_throwsUnsupportedOperationException() {
        MapPropertyStore<Object> src = MapPropertyStore.createDefaultInstance();
        src.putProperty("a", "1");
        assertThrows(UnsupportedOperationException.class, () -> immutable().putAllProperties(src));
    }

    @Test
    public void putProperties_throwsUnsupportedOperationException() {
        MapPropertyStore<Object> src = MapPropertyStore.createDefaultInstance();
        assertThrows(UnsupportedOperationException.class, () -> immutable().putProperties(src, null));
    }

    @Test
    public void copyFrom_map_throwsUnsupportedOperationException() {
        assertThrows(UnsupportedOperationException.class,
            () -> immutable().copyFrom(Map.of("k", "v")));
    }

    @Test
    public void appendToListProperty_throwsUnsupportedOperationException() {
        assertThrows(UnsupportedOperationException.class,
            () -> immutable().appendToListProperty("k", "v"));
    }

    @Test
    public void appendToProperty_throwsUnsupportedOperationException() {
        assertThrows(UnsupportedOperationException.class,
            () -> immutable().appendToProperty("k", "v", ","));
    }

}
