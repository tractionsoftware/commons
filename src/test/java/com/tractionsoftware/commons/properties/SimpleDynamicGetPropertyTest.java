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

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

public final class SimpleDynamicGetPropertyTest {

    // =====================================================================
    // ofStaticProperties
    // =====================================================================

    @Test
    void ofStaticProperties_getProperty_returnsValue() {
        var gp = SimpleDynamicGetProperty.ofStaticProperties(Map.of("k", "v"));
        assertEquals("v", gp.getProperty("k"));
    }

    @Test
    void ofStaticProperties_absent_returnsNull() {
        var gp = SimpleDynamicGetProperty.ofStaticProperties(Map.of());
        assertNull(gp.getProperty("missing"));
    }

    @Test
    void ofStaticProperties_backingMapChanges_reflectsChanges() {
        // "of" (not snapshotted) keeps a reference to the live map
        Map<String,String> live = new LinkedHashMap<>(Map.of("k", "v1"));
        var gp = SimpleDynamicGetProperty.ofStaticProperties(live);
        assertEquals("v1", gp.getProperty("k"));
        live.put("k", "v2");
        assertEquals("v2", gp.getProperty("k"));
    }

    @Test
    void ofStaticProperties_getPropertyNames_returnsKeys() {
        var gp = SimpleDynamicGetProperty.ofStaticProperties(Map.of("a", "1", "b", "2"));
        var names = gp.getPropertyNames();
        assertTrue(names.contains("a"));
        assertTrue(names.contains("b"));
    }

    // =====================================================================
    // ofSnapshottedStaticProperties
    // =====================================================================

    @Test
    void ofSnapshottedStaticProperties_snapshot_doesNotReflectChanges() {
        Map<String,String> live = new LinkedHashMap<>(Map.of("k", "v1"));
        var gp = SimpleDynamicGetProperty.ofSnapshottedStaticProperties(live);
        assertEquals("v1", gp.getProperty("k"));
        live.put("k", "v2");
        // snapshot was taken at construction — still "v1"
        assertEquals("v1", gp.getProperty("k"));
    }

    // =====================================================================
    // ofDynamicProperties (Supplier<String> values)
    // =====================================================================

    @Test
    void ofDynamicProperties_evaluatesSupplierOnGet() {
        AtomicReference<String> ref = new AtomicReference<>("initial");
        Map<String,java.util.function.Supplier<String>> map = Map.of("k", ref::get);
        var gp = SimpleDynamicGetProperty.ofDynamicProperties(map);
        assertEquals("initial", gp.getProperty("k"));
        ref.set("updated");
        assertEquals("updated", gp.getProperty("k"));
    }

    // =====================================================================
    // ofSnapshottedDynamicProperties
    // =====================================================================

    @Test
    void ofSnapshottedDynamicProperties_snapshotsSupplierReferences() {
        AtomicReference<String> ref = new AtomicReference<>("snap");
        Map<String,java.util.function.Supplier<String>> map = Map.of("k", ref::get);
        var gp = SimpleDynamicGetProperty.ofSnapshottedDynamicProperties(map);
        assertEquals("snap", gp.getProperty("k"));
        // The supplier reference is snapshotted but the supplier itself still evaluates live
        ref.set("changed");
        assertEquals("changed", gp.getProperty("k")); // supplier called live
    }

    // =====================================================================
    // createInstance / createSnapshotInstance (generic)
    // =====================================================================

    @Test
    void createInstance_withCustomEvaluator_appliesFunction() {
        Map<String,Integer> map = Map.of("n", 42);
        var gp = SimpleDynamicGetProperty.createInstance(map, Object::toString);
        assertEquals("42", gp.getProperty("n"));
    }

    @Test
    void createSnapshotInstance_nullProperties_throwsNPE() {
        assertThrows(
            NullPointerException.class, () ->
                SimpleDynamicGetProperty.createSnapshotInstance(null, Objects::toString)
        );
    }

}
