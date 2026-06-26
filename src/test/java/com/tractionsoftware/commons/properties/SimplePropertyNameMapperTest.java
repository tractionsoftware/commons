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

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class SimplePropertyNameMapperTest {

    // ====================================================================
    // addPrefix
    // ====================================================================

    @Test
    public void addPrefix_twoArgOverload_usesDefaultSeparator() {
        assertEquals("foo_bar", SimplePropertyNameMapper.addPrefix("bar", "foo"));
    }

    @Test
    public void addPrefix_nullPrefix_returnsStrUnchanged() {
        assertEquals("bar", SimplePropertyNameMapper.addPrefix("bar", null, '_'));
    }

    @Test
    public void addPrefix_emptyPrefix_returnsStrUnchanged() {
        assertEquals("bar", SimplePropertyNameMapper.addPrefix("bar", "", '_'));
    }

    @Test
    public void addPrefix_nullStr_returnsBarePrefix() {
        assertEquals("foo", SimplePropertyNameMapper.addPrefix(null, "foo", '_'));
    }

    @Test
    public void addPrefix_emptyStr_returnsBarePrefix() {
        assertEquals("foo", SimplePropertyNameMapper.addPrefix("", "foo", '_'));
    }

    @Test
    public void addPrefix_nullSeparator_concatenatesWithNoSeparator() {
        assertEquals("foobar", SimplePropertyNameMapper.addPrefix("bar", "foo", null));
    }

    @Test
    public void addPrefix_nonNullSeparator_insertsSeparator() {
        assertEquals("foo.bar", SimplePropertyNameMapper.addPrefix("bar", "foo", '.'));
    }

    // ====================================================================
    // removePrefix
    // ====================================================================

    @Test
    public void removePrefix_nullPrefix_returnsStrUnchanged() {
        assertEquals("anything", SimplePropertyNameMapper.removePrefix("anything", null, '_'));
    }

    @Test
    public void removePrefix_emptyPrefix_returnsStrUnchanged() {
        assertEquals("anything", SimplePropertyNameMapper.removePrefix("anything", "", '_'));
    }

    @Test
    public void removePrefix_nullStr_returnsNull() {
        assertNull(SimplePropertyNameMapper.removePrefix(null, "foo", '_'));
    }

    @Test
    public void removePrefix_emptyStr_returnsEmptyStr() {
        assertEquals("", SimplePropertyNameMapper.removePrefix("", "foo", '_'));
    }

    @Test
    public void removePrefix_nullSeparator_strStartsWithPrefix_stripsPrefix() {
        assertEquals("bar", SimplePropertyNameMapper.removePrefix("foobar", "foo", null));
    }

    @Test
    public void removePrefix_nullSeparator_strExactlyEqualToPrefix_stripsToEmptyString() {
        assertEquals("", SimplePropertyNameMapper.removePrefix("foo", "foo", null));
    }

    @Test
    public void removePrefix_nullSeparator_strDoesNotStartWithPrefix_returnsNull() {
        assertNull(SimplePropertyNameMapper.removePrefix("baz", "foo", null));
    }

    @Test
    public void removePrefix_withSeparator_strStartsWithPrefixAndSeparator_stripsBoth() {
        assertEquals("bar", SimplePropertyNameMapper.removePrefix("foo.bar", "foo", '.'));
    }

    @Test
    public void removePrefix_withSeparator_strExactlyEqualToPrefix_returnsNull() {
        // No separator follows the prefix, so this does not match "prefix + separator" and the lookup fails -- it is
        // not treated the same as the empty-string/null sentinel case.
        assertNull(SimplePropertyNameMapper.removePrefix("foo", "foo", '.'));
    }

    @Test
    public void removePrefix_withSeparator_strDoesNotStartWithPrefix_returnsNull() {
        assertNull(SimplePropertyNameMapper.removePrefix("baz_bar", "foo", '_'));
    }

    @Test
    public void removePrefix_isInverseOfAddPrefix_forMatchingPrefixedString() {
        String prefixed = SimplePropertyNameMapper.addPrefix("bar", "foo", '_');
        assertEquals("bar", SimplePropertyNameMapper.removePrefix(prefixed, "foo", '_'));
    }

    // ====================================================================
    // ns / defaultNs
    // ====================================================================

    @Test
    public void ns_twoArg_usesDefaultSeparator() {
        assertEquals("space_name", SimplePropertyNameMapper.ns("space", "name"));
    }

    @Test
    public void ns_threeArg_usesGivenSeparator() {
        assertEquals("space.name", SimplePropertyNameMapper.ns("space", "name", '.'));
    }

    @Test
    public void ns_iterable_usesDefaultSeparator() {
        assertEquals("a_b_c", SimplePropertyNameMapper.ns(List.of("a", "b", "c")));
    }

    @Test
    public void ns_iterableWithSeparator_usesGivenSeparator() {
        assertEquals("a.b.c", SimplePropertyNameMapper.ns(List.of("a", "b", "c"), '.'));
    }

    @Test
    public void defaultNs_prependsDefaultNamespace() {
        assertEquals("default_name", SimplePropertyNameMapper.defaultNs("name"));
    }

    // ====================================================================
    // Namespace instance factories
    // ====================================================================

    @Test
    public void getNamespaceInstanceWithDefaultSeparator_nullSpace_returnsNull() {
        assertNull(SimplePropertyNameMapper.getNamespaceInstanceWithDefaultSeparator(null));
    }

    @Test
    public void getNamespaceInstanceWithDefaultSeparator_emptySpace_returnsNull() {
        assertNull(SimplePropertyNameMapper.getNamespaceInstanceWithDefaultSeparator(""));
    }

    @Test
    public void getNamespaceInstanceWithDefaultSeparator_defaultSpace_returnsSharedDefaultNsInstance() {
        PropertyNameMapper mapper = SimplePropertyNameMapper.getNamespaceInstanceWithDefaultSeparator("default");
        assertSame(SimplePropertyNameMapper.DEFAULT_NS, mapper);
    }

    @Test
    public void getNamespaceInstanceWithSeparator_defaultSpaceButNonDefaultSeparator_doesNotReturnSharedInstance() {
        PropertyNameMapper mapper = SimplePropertyNameMapper.getNamespaceInstanceWithSeparator("default", '.');
        assertNotSame(SimplePropertyNameMapper.DEFAULT_NS, mapper);
        assertEquals("default.name", mapper.getActualPropertyName("name"));
    }

    @Test
    public void getNamespaceInstanceWithNoSeparator_defaultSpace_doesNotReturnSharedInstance() {
        PropertyNameMapper mapper = SimplePropertyNameMapper.getNamespaceInstanceWithNoSeparator("default");
        assertNotSame(SimplePropertyNameMapper.DEFAULT_NS, mapper);
        assertEquals("defaultname", mapper.getActualPropertyName("name"));
    }

    @Test
    public void getNamespaceInstanceWithDefaultSeparator_getActualPropertyName_addsNamespacePrefix() {
        PropertyNameMapper mapper = SimplePropertyNameMapper.getNamespaceInstanceWithDefaultSeparator("ns");
        assertEquals("ns_a", mapper.getActualPropertyName("a"));
    }

    @Test
    public void getNamespaceInstanceWithDefaultSeparator_getPublishedName_removesNamespacePrefix() {
        PropertyNameMapper mapper = SimplePropertyNameMapper.getNamespaceInstanceWithDefaultSeparator("ns");
        assertEquals("a", mapper.getPublishedName("ns_a"));
    }

    @Test
    public void getNamespaceInstanceWithSeparator_usesGivenSeparator() {
        PropertyNameMapper mapper = SimplePropertyNameMapper.getNamespaceInstanceWithSeparator("ns", '.');
        assertEquals("ns.a", mapper.getActualPropertyName("a"));
    }

    @Test
    public void getNamespaceInstanceWithNoSeparator_concatenatesWithNoSeparator() {
        PropertyNameMapper mapper = SimplePropertyNameMapper.getNamespaceInstanceWithNoSeparator("ns");
        assertEquals("nsa", mapper.getActualPropertyName("a"));
    }

    // ====================================================================
    // Prefix instance factories
    // ====================================================================

    @Test
    public void getPrefixInstanceWithDefaultSeparator_nullPrefix_returnsNull() {
        assertNull(SimplePropertyNameMapper.getPrefixInstanceWithDefaultSeparator(null));
    }

    @Test
    public void getPrefixInstanceWithDefaultSeparator_emptyPrefix_returnsNull() {
        assertNull(SimplePropertyNameMapper.getPrefixInstanceWithDefaultSeparator(""));
    }

    @Test
    public void getPrefixInstanceWithDefaultSeparator_getActualPropertyName_removesPrefix() {
        PropertyNameMapper mapper = SimplePropertyNameMapper.getPrefixInstanceWithDefaultSeparator("px");
        assertEquals("a", mapper.getActualPropertyName("px_a"));
    }

    @Test
    public void getPrefixInstanceWithDefaultSeparator_getPublishedName_addsPrefix() {
        PropertyNameMapper mapper = SimplePropertyNameMapper.getPrefixInstanceWithDefaultSeparator("px");
        assertEquals("px_a", mapper.getPublishedName("a"));
    }

    @Test
    public void getPrefixInstanceWithSeparator_usesGivenSeparator() {
        PropertyNameMapper mapper = SimplePropertyNameMapper.getPrefixInstanceWithSeparator("px", '.');
        assertEquals("a", mapper.getActualPropertyName("px.a"));
    }

    @Test
    public void getPrefixInstanceWithNoSeparator_concatenatesWithNoSeparator() {
        PropertyNameMapper mapper = SimplePropertyNameMapper.getPrefixInstanceWithNoSeparator("px");
        assertEquals("a", mapper.getActualPropertyName("pxa"));
    }

    // ====================================================================
    // toString / equals / hashCode
    // ====================================================================

    @Test
    public void toString_reflectsActualPropertyNameFunction() {
        PropertyNameMapper mapper = SimplePropertyNameMapper.getNamespaceInstanceWithDefaultSeparator("ns");
        assertEquals("+prefix:ns_", mapper.toString());
    }

    @Test
    public void toString_prefixInstance_reflectsRemovePrefixFunction() {
        PropertyNameMapper mapper = SimplePropertyNameMapper.getPrefixInstanceWithDefaultSeparator("px");
        assertEquals("-prefix:px_", mapper.toString());
    }

    @Test
    public void equals_separatelyConstructedSameSpaceAndSeparator_areEqual() {
        PropertyNameMapper a = SimplePropertyNameMapper.getNamespaceInstanceWithDefaultSeparator("ns");
        PropertyNameMapper b = SimplePropertyNameMapper.getNamespaceInstanceWithDefaultSeparator("ns");
        assertNotSame(a, b);
        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
    }

    @Test
    public void equals_differentSpace_areNotEqual() {
        PropertyNameMapper a = SimplePropertyNameMapper.getNamespaceInstanceWithDefaultSeparator("ns1");
        PropertyNameMapper b = SimplePropertyNameMapper.getNamespaceInstanceWithDefaultSeparator("ns2");
        assertNotEquals(a, b);
    }

    @Test
    public void equals_namespaceVsPrefixSameSpace_areNotEqual() {
        // Same space/separator, but opposite roles for the AddPrefixFunction/RemovePrefixFunction -- not equal.
        PropertyNameMapper namespace = SimplePropertyNameMapper.getNamespaceInstanceWithDefaultSeparator("x");
        PropertyNameMapper prefix = SimplePropertyNameMapper.getPrefixInstanceWithDefaultSeparator("x");
        assertNotEquals(namespace, prefix);
    }

    @Test
    public void equals_againstUnrelatedType_returnsFalse() {
        PropertyNameMapper mapper = SimplePropertyNameMapper.getNamespaceInstanceWithDefaultSeparator("ns");
        assertNotEquals(mapper, "not a mapper");
    }

    // ====================================================================
    // isInverseOf
    // ====================================================================

    @Test
    public void isInverseOf_namespaceAndPrefix_sameSpaceAndSeparator_areInverses() {
        PropertyNameMapper namespace = SimplePropertyNameMapper.getNamespaceInstanceWithDefaultSeparator("ns");
        PropertyNameMapper prefix = SimplePropertyNameMapper.getPrefixInstanceWithDefaultSeparator("ns");
        assertTrue(namespace.isInverseOf(prefix));
        assertTrue(prefix.isInverseOf(namespace));
    }

    @Test
    public void isInverseOf_twoNamespaceInstances_areNotInverses() {
        PropertyNameMapper a = SimplePropertyNameMapper.getNamespaceInstanceWithDefaultSeparator("ns");
        PropertyNameMapper b = SimplePropertyNameMapper.getNamespaceInstanceWithDefaultSeparator("ns");
        assertFalse(a.isInverseOf(b));
    }

    @Test
    public void isInverseOf_namespaceAndPrefix_differentSpace_areNotInverses() {
        PropertyNameMapper namespace = SimplePropertyNameMapper.getNamespaceInstanceWithDefaultSeparator("ns1");
        PropertyNameMapper prefix = SimplePropertyNameMapper.getPrefixInstanceWithDefaultSeparator("ns2");
        assertFalse(namespace.isInverseOf(prefix));
    }

    @Test
    public void isInverseOf_againstNonSimplePropertyNameMapper_returnsFalse() {
        PropertyNameMapper namespace = SimplePropertyNameMapper.getNamespaceInstanceWithDefaultSeparator("ns");
        PropertyNameMapper other = MultiPropertyNameMapper.createInstance(namespace, namespace);
        assertFalse(namespace.isInverseOf(other));
    }

    // ====================================================================
    // compose
    // ====================================================================

    @Test
    public void compose_withNull_returnsThis() {
        PropertyNameMapper mapper = SimplePropertyNameMapper.getNamespaceInstanceWithDefaultSeparator("ns");
        assertSame(mapper, mapper.compose(null));
    }

    @Test
    public void compose_twoNamespaceInstances_combinesIntoNestedNamespacePrefix() {
        PropertyNameMapper a = SimplePropertyNameMapper.getNamespaceInstanceWithDefaultSeparator("a");
        PropertyNameMapper b = SimplePropertyNameMapper.getNamespaceInstanceWithDefaultSeparator("b");
        PropertyNameMapper combined = a.compose(b);
        assertEquals("a_b_x", combined.getActualPropertyName("x"));
    }

    @Test
    public void compose_twoPrefixInstances_combinesIntoNestedPrefixRemoval() {
        PropertyNameMapper a = SimplePropertyNameMapper.getPrefixInstanceWithDefaultSeparator("a");
        PropertyNameMapper b = SimplePropertyNameMapper.getPrefixInstanceWithDefaultSeparator("b");
        PropertyNameMapper combined = a.compose(b);
        assertEquals("x", combined.getActualPropertyName("a_b_x"));
    }

    @Test
    public void compose_mismatchedShapes_fallsBackToMultiPropertyNameMapper() {
        PropertyNameMapper namespace = SimplePropertyNameMapper.getNamespaceInstanceWithDefaultSeparator("a");
        PropertyNameMapper prefix = SimplePropertyNameMapper.getPrefixInstanceWithDefaultSeparator("b");
        PropertyNameMapper combined = namespace.compose(prefix);
        assertInstanceOf(MultiPropertyNameMapper.class, combined);
        // MultiPropertyNameMapper.getActualPropertyName applies its mappers in reverse order, so prefix("b") runs
        // first: removePrefix("x", "b", '_') doesn't match (no "b_" prefix on "x"), yielding null. Then namespace("a")
        // runs on that null: addPrefix(null, "a", '_') hits the str-is-empty branch and returns the bare prefix "a".
        // This characterizes the actual (non-obvious) behavior of composing mismatched mapper shapes, which is not a
        // supported/expected usage.
        assertEquals("a", combined.getActualPropertyName("x"));
    }

    @Test
    public void compose_withNonSimplePropertyNameMapper_fallsBackToMultiPropertyNameMapper() {
        PropertyNameMapper namespace = SimplePropertyNameMapper.getNamespaceInstanceWithDefaultSeparator("a");
        PropertyNameMapper other = new PropertyNameMapper() {

            @Override
            public String getActualPropertyName(String requestedName) {
                return requestedName + "-other";
            }

            @Override
            public String getPublishedName(String propertyName) {
                return propertyName;
            }

            @Override
            public boolean isInverseOf(PropertyNameMapper otherNameMapper) {
                return false;
            }

        };
        PropertyNameMapper combined = namespace.compose(other);
        assertInstanceOf(MultiPropertyNameMapper.class, combined);
        assertEquals("a_x-other", combined.getActualPropertyName("x"));
    }

}
