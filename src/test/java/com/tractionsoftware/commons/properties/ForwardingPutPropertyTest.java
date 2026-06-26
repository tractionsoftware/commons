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

import com.tractionsoftware.commons.properties.ForwardingPutProperty.DynamicForwardingPutProperty;
import com.tractionsoftware.commons.properties.ForwardingPutProperty.StaticForwardingPutProperty;
import jakarta.annotation.Nonnull;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class ForwardingPutPropertyTest {

    // ====================================================================
    // Test helpers
    // ====================================================================

    /**
     * A minimal concrete subclass of ForwardingPutProperty that overrides nothing beyond {@link #delegate()}, used to
     * exercise the default (non-overridden) implementations declared directly on the abstract base class -- in
     * particular {@link ForwardingPutProperty#toString()}, which every production subclass overrides.
     */
    private static final class BareForwardingPutProperty extends ForwardingPutProperty {

        private final PutProperty props;

        private BareForwardingPutProperty(PutProperty props) {
            this.props = props;
        }

        @Nonnull
        @Override
        protected PutProperty delegate() {
            return props;
        }

    }

    private static MapPropertyStore<Object> newStore() {
        return new MapPropertyStore<>("test", new LinkedHashMap<>());
    }

    // ====================================================================
    // StaticForwardingPutProperty
    // ====================================================================

    @Test
    public void staticForwarding_delegatesPutProperty() {
        MapPropertyStore<Object> store = newStore();
        PutProperty fwd = new StaticForwardingPutProperty(store);
        fwd.putProperty("a", "1");
        assertEquals("1", store.getProperty("a"));
    }

    @Test
    public void staticForwarding_toString() {
        MapPropertyStore<Object> store = newStore();
        PutProperty fwd = new StaticForwardingPutProperty(store);
        assertEquals("PutProperty: stat fwd {" + store + "}", fwd.toString());
    }

    @Test
    public void staticForwarding_delegatesGetName() {
        MapPropertyStore<Object> store = newStore();
        PutProperty fwd = new StaticForwardingPutProperty(store);
        assertEquals("test", fwd.getName());
    }

    @Test
    public void staticForwarding_delegatesFullyQualify() {
        MapPropertyStore<Object> store = newStore();
        PutProperty fwd = new StaticForwardingPutProperty(store);
        // MapPropertyStore does not override fullyQualify, so the PropertyCollection default (identity) applies.
        assertEquals("a", fwd.fullyQualify("a"));
    }

    @Test
    public void staticForwarding_delegatesPutBooleanProperty() {
        MapPropertyStore<Object> store = newStore();
        PutProperty fwd = new StaticForwardingPutProperty(store);
        fwd.putBooleanProperty("b", true);
        assertEquals("true", store.getProperty("b"));
    }

    @Test
    public void staticForwarding_delegatesPutIntProperty() {
        MapPropertyStore<Object> store = newStore();
        PutProperty fwd = new StaticForwardingPutProperty(store);
        fwd.putIntProperty("i", 42);
        assertEquals("42", store.getProperty("i"));
    }

    @Test
    public void staticForwarding_delegatesPutLongProperty() {
        MapPropertyStore<Object> store = newStore();
        PutProperty fwd = new StaticForwardingPutProperty(store);
        fwd.putLongProperty("l", 42L);
        assertEquals("42", store.getProperty("l"));
    }

    @Test
    public void staticForwarding_delegatesPutDoubleProperty() {
        MapPropertyStore<Object> store = newStore();
        PutProperty fwd = new StaticForwardingPutProperty(store);
        fwd.putDoubleProperty("d", 1.5);
        assertEquals("1.5", store.getProperty("d"));
    }

    @Test
    public void staticForwarding_delegatesRemoveProperty() {
        MapPropertyStore<Object> store = newStore();
        store.putProperty("a", "1");
        PutProperty fwd = new StaticForwardingPutProperty(store);
        fwd.removeProperty("a");
        assertNull(store.getProperty("a"));
    }

    @Test
    public void staticForwarding_delegatesClearLocalProperties() {
        MapPropertyStore<Object> store = newStore();
        store.putProperty("a", "1");
        PutProperty fwd = new StaticForwardingPutProperty(store);
        assertTrue(fwd.clearLocalProperties());
        assertNull(store.getProperty("a"));
    }

    @Test
    public void staticForwarding_delegatesPutAllProperties() {
        MapPropertyStore<Object> store = newStore();
        PutProperty fwd = new StaticForwardingPutProperty(store);
        MapPropertyStore<Object> source = newStore();
        source.putProperty("a", "1");
        source.putProperty("b", "2");
        fwd.putAllProperties(source);
        assertEquals("1", store.getProperty("a"));
        assertEquals("2", store.getProperty("b"));
    }

    @Test
    public void staticForwarding_delegatesPutProperties_withExceptions() {
        MapPropertyStore<Object> store = newStore();
        PutProperty fwd = new StaticForwardingPutProperty(store);
        MapPropertyStore<Object> source = newStore();
        source.putProperty("a", "1");
        source.putProperty("b", "2");
        fwd.putProperties(source, List.of("b"));
        assertEquals("1", store.getProperty("a"));
        assertNull(store.getProperty("b"));
    }

    @Test
    public void wrapFactory_returnsStaticForwardingPutProperty() {
        MapPropertyStore<Object> store = newStore();
        PutProperty fwd = ForwardingPutProperty.wrap(store);
        assertInstanceOf(StaticForwardingPutProperty.class, fwd);
        fwd.putProperty("a", "1");
        assertEquals("1", store.getProperty("a"));
    }

    // ====================================================================
    // DynamicForwardingPutProperty
    // ====================================================================

    @Test
    public void dynamicForwarding_delegatesToCurrentSupplierResult() {
        MapPropertyStore<Object> storeA = newStore();
        MapPropertyStore<Object> storeB = newStore();
        List<MapPropertyStore<Object>> current = new ArrayList<>();
        current.add(storeA);
        PutProperty fwd = new DynamicForwardingPutProperty(current::getFirst);

        fwd.putProperty("k", "1");
        assertEquals("1", storeA.getProperty("k"));
        assertNull(storeB.getProperty("k"));

        // Swap which instance the supplier returns; the forwarding wrapper must follow it dynamically rather than
        // caching the first delegate it saw.
        current.set(0, storeB);
        fwd.putProperty("k", "2");
        assertEquals("1", storeA.getProperty("k"));
        assertEquals("2", storeB.getProperty("k"));
    }

    @Test
    public void dynamicForwarding_nullSupplierResult_throwsNpeOnUse() {
        PutProperty fwd = new DynamicForwardingPutProperty(() -> null);
        assertThrows(NullPointerException.class, () -> fwd.putProperty("k", "v"));
    }

    @Test
    public void dynamicForwarding_toString_containsDelegateToString() {
        MapPropertyStore<Object> store = newStore();
        PutProperty fwd = new DynamicForwardingPutProperty(() -> store);
        String result = fwd.toString();
        assertTrue(result.contains(store.toString()));
        assertTrue(result.startsWith("PutProperty dyn fwd {"));
    }

    @Test
    public void wrapFactory_returnsDynamicForwardingPutProperty() {
        MapPropertyStore<Object> store = newStore();
        PutProperty fwd = ForwardingPutProperty.wrap(() -> store);
        assertInstanceOf(DynamicForwardingPutProperty.class, fwd);
        fwd.putProperty("a", "1");
        assertEquals("1", store.getProperty("a"));
    }

    // ====================================================================
    // ForwardingPutProperty base class defaults (via BareForwardingPutProperty)
    // ====================================================================

    @Test
    public void baseClass_defaultToString() {
        MapPropertyStore<Object> store = newStore();
        PutProperty fwd = new BareForwardingPutProperty(store);
        assertEquals("PutProperty: fwd {" + store + "}", fwd.toString());
    }

    // ====================================================================
    // wrapInNamespace / wrapInPrefix / applyPropertyNameMapper
    // ====================================================================

    @Test
    public void applyPropertyNameMapper_nullMapper_returnsPropsUnchanged() {
        MapPropertyStore<Object> store = newStore();
        PutProperty result = ForwardingPutProperty.applyPropertyNameMapper(store, null);
        assertSame(store, result);
    }

    @Test
    public void applyPropertyNameMapper_nullProps_returnsNull() {
        PropertyNameMapper mapper = SimplePropertyNameMapper.getNamespaceInstanceWithDefaultSeparator("ns");
        assertNull(ForwardingPutProperty.applyPropertyNameMapper(null, mapper));
    }

    @Test
    public void wrapInNamespace_prependsNamespaceToStoredKey() {
        MapPropertyStore<Object> store = newStore();
        PutProperty wrapped = ForwardingPutProperty.wrapInNamespace(store, "ns");
        wrapped.putProperty("a", "1");
        assertEquals("1", store.getProperty("ns_a"));
        assertNull(store.getProperty("a"));
    }

    @Test
    public void wrapInNamespace_customSeparator() {
        MapPropertyStore<Object> store = newStore();
        PutProperty wrapped = ForwardingPutProperty.wrapInNamespace(store, "ns", '.');
        wrapped.putProperty("a", "1");
        assertEquals("1", store.getProperty("ns.a"));
    }

    @Test
    public void wrapInNamespace_nullOrEmptyName_writesToSpaceNameItself() {
        MapPropertyStore<Object> store = newStore();
        PutProperty wrapped = ForwardingPutProperty.wrapInNamespace(store, "ns");
        // Per SimplePropertyNameMapper.addPrefix, a null or empty requested name maps to the bare space name.
        wrapped.putProperty(null, "1");
        assertEquals("1", store.getProperty("ns"));
    }

    @Test
    public void wrapInNamespace_getName_delegatesThrough() {
        MapPropertyStore<Object> store = newStore();
        PutProperty wrapped = ForwardingPutProperty.wrapInNamespace(store, "ns");
        assertEquals("test", wrapped.getName());
    }

    @Test
    public void wrapInNamespace_fullyQualify_returnsActualPropertyName() {
        MapPropertyStore<Object> store = newStore();
        PutProperty wrapped = ForwardingPutProperty.wrapInNamespace(store, "ns");
        // MapPropertyStore's own fullyQualify is the identity default, so the only transformation visible here is
        // the name-mapper's getActualPropertyName.
        assertEquals("ns_a", wrapped.fullyQualify("a"));
    }

    @Test
    public void wrapInPrefix_removesPrefixFromStoredKey() {
        MapPropertyStore<Object> store = newStore();
        PutProperty wrapped = ForwardingPutProperty.wrapInPrefix(store, "px");
        wrapped.putProperty("px_a", "1");
        assertEquals("1", store.getProperty("a"));
    }

    @Test
    public void wrapInPrefix_customSeparator() {
        MapPropertyStore<Object> store = newStore();
        PutProperty wrapped = ForwardingPutProperty.wrapInPrefix(store, "px", '.');
        wrapped.putProperty("px.a", "1");
        assertEquals("1", store.getProperty("a"));
    }

    @Test
    public void wrapInPrefix_nameNotStartingWithPrefix_writesUnderNullKey() {
        // Characterizes the actual (surprising) current behavior: SimplePropertyNameMapper.removePrefix returns
        // null -- not the original name -- when the requested name does not start with "prefix" + separator. That
        // null is then used directly as the key passed to the delegate's putProperty.
        MapPropertyStore<Object> store = newStore();
        PutProperty wrapped = ForwardingPutProperty.wrapInPrefix(store, "px");
        wrapped.putProperty("somethingElse", "1");
        assertEquals("1", store.getProperty(null));
    }

    @Test
    public void wrapInPrefix_nameExactlyEqualToPrefix_usesNullKey() {
        MapPropertyStore<Object> store = newStore();
        PutProperty wrapped = ForwardingPutProperty.wrapInPrefix(store, "px");
        wrapped.putProperty("px", "1");
        assertEquals("1", store.getProperty(null));
    }

    @Test
    public void applyPropertyNameMapper_simpleWrap_putPropertyUsesMappedName() {
        MapPropertyStore<Object> store = newStore();
        PropertyNameMapper mapper = SimplePropertyNameMapper.getNamespaceInstanceWithDefaultSeparator("ns");
        PutProperty wrapped = ForwardingPutProperty.applyPropertyNameMapper(store, mapper);
        wrapped.putProperty("a", "1");
        assertEquals("1", store.getProperty("ns_a"));
    }

    @Test
    public void applyPropertyNameMapper_isInverseOfExistingWrapper_unwrapsToOriginalDelegate() {
        MapPropertyStore<Object> store = newStore();
        PutProperty wrapped = ForwardingPutProperty.wrapInNamespace(store, "ns");
        PropertyNameMapper inverseMapper = SimplePropertyNameMapper.getPrefixInstanceWithDefaultSeparator("ns");
        PutProperty result = ForwardingPutProperty.applyPropertyNameMapper(wrapped, inverseMapper);
        // The namespace("ns") mapper and the prefix("ns") mapper (same space, same separator) are true inverses, so
        // applying one to a PutProperty already wrapped with the other should fully unwrap back to the original,
        // un-namespaced store rather than nesting another wrapper.
        assertSame(store, result);
    }

    @Test
    public void applyPropertyNameMapper_composeBranch_doubleAppliesFirstMapper() {
        MapPropertyStore<Object> store = newStore();
        PutProperty wrappedA = ForwardingPutProperty.wrapInNamespace(store, "a");
        PutProperty wrappedAB = ForwardingPutProperty.wrapInNamespace(wrappedA, "b");

        wrappedAB.putProperty("x", "1");

        assertEquals("1", store.getProperty("a_b_x"));
    }

    // ====================================================================
    // PropertyNameMappingPutProperty.putAllProperties / putProperties (reached via wrapInNamespace)
    // ====================================================================

    @Test
    public void nameMappingPutProperty_putAllProperties_mapsEachName() {
        MapPropertyStore<Object> store = newStore();
        PutProperty wrapped = ForwardingPutProperty.wrapInNamespace(store, "ns");
        MapPropertyStore<Object> source = newStore();
        source.putProperty("a", "1");
        source.putProperty("b", "2");

        wrapped.putAllProperties(source);

        assertEquals("1", store.getProperty("ns_a"));
        assertEquals("2", store.getProperty("ns_b"));
    }

    @Test
    public void nameMappingPutProperty_putAllProperties_nullSource_doesNothing() {
        MapPropertyStore<Object> store = newStore();
        PutProperty wrapped = ForwardingPutProperty.wrapInNamespace(store, "ns");
        wrapped.putAllProperties(null);
        assertTrue(store.getPropertyNames().isEmpty());
    }

    @Test
    public void nameMappingPutProperty_putProperties_filtersExceptionsByUnmappedSourceName() {
        MapPropertyStore<Object> store = newStore();
        PutProperty wrapped = ForwardingPutProperty.wrapInNamespace(store, "ns");
        MapPropertyStore<Object> source = newStore();
        source.putProperty("a", "1");
        source.putProperty("b", "2");

        // The exceptions are matched against the source's own (unmapped) property names, not the mapped names that
        // end up written to the delegate.
        wrapped.putProperties(source, List.of("b"));

        assertEquals("1", store.getProperty("ns_a"));
        assertNull(store.getProperty("ns_b"));
    }

    @Test
    public void nameMappingPutProperty_putProperties_nullSource_doesNothing() {
        MapPropertyStore<Object> store = newStore();
        PutProperty wrapped = ForwardingPutProperty.wrapInNamespace(store, "ns");
        wrapped.putProperties(null, List.of("anything"));
        assertTrue(store.getPropertyNames().isEmpty());
    }

    @Test
    public void nameMappingPutProperty_toString_includesNameMapperAndDelegate() {
        MapPropertyStore<Object> store = newStore();
        PutProperty wrapped = ForwardingPutProperty.wrapInNamespace(store, "ns");
        String result = wrapped.toString();
        assertTrue(result.startsWith("PutProperty name-mapper ["));
        assertTrue(result.contains(store.toString()));
    }

}
