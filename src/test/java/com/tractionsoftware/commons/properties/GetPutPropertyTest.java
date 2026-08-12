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

import com.tractionsoftware.commons.text.TextTransformer;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.util.LinkedHashMap;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Covers the default method implementations in {@link GetPutProperty}.
 */
class GetPutPropertyTest {

    private static MapPropertyStore<Object> createGenericMapPropertyStore() {
        return MapPropertyStore.createDefaultInstance();
    }

    /**
     * TextTransformer that uppercases its input. TextTransformer has two abstract methods so it cannot be expressed as
     * a lambda.
     */
    private static final class UppercaseTransformer implements TextTransformer {

        @Override
        public final void transform(CharSequence text, Appendable out) throws IOException {
            out.append(text == null ? "" : text.toString().toUpperCase());
        }

        @Override
        public final void transform(@Nonnull Reader in, @Nonnull Writer out) {
            throw new UnsupportedOperationException("not used");
        }

    }

    /**
     * Identity transformer (passes text through unchanged).
     */
    private static final class IdentityTransformer implements TextTransformer {

        @Override
        public final void transform(@Nullable CharSequence text, @Nonnull Appendable out) throws IOException {
            if (text != null) {
                out.append(text);
            }
        }

        @Override
        public final void transform(@Nonnull Reader in, @Nonnull Writer out) throws IOException {
            in.transferTo(out);
        }

    }

    /**
     * Minimal concrete PutProperty for testing the {@link PutProperty#clearLocalProperties()} default.
     * Does not override {@code clearLocalProperties()}, so the {@code PutProperty} default (returns
     * {@code false}) is invoked. This exercises the contract for third-party library users who implement
     * {@code PutProperty} without overriding the method — a case that no in-repo implementation covers.
     */
    private static final class ClearLocalPropertiesTestPutProperty implements PutProperty {

        @Nonnull
        @Override
        public final String toString() {
            return "MinimalPutProperty";
        }

        @Override
        public final void putProperty(String name, String value) {
            // stub — not needed for clearLocalProperties() test
        }

    }

    // -------------------------------------------------------------------------
    // appendToListProperty
    // -------------------------------------------------------------------------

    @Test
    void appendToListProperty_nullValue_noChange() {
        MapPropertyStore<Object> s = createGenericMapPropertyStore();
        s.appendToListProperty("key", null);
        assertNull(s.getProperty("key"));
    }

    @Test
    void appendToListProperty_noExistingValue_setsValue() {
        MapPropertyStore<Object> s = createGenericMapPropertyStore();
        s.appendToListProperty("key", "first");
        assertEquals("first", s.getProperty("key"));
    }

    @Test
    void appendToListProperty_existingValue_appendsWithComma() {
        MapPropertyStore<Object> s = createGenericMapPropertyStore();
        s.putProperty("key", "a");
        s.appendToListProperty("key", "b");
        assertEquals("a,b", s.getProperty("key"));
    }

    // -------------------------------------------------------------------------
    // appendToProperty
    // -------------------------------------------------------------------------

    @Test
    void appendToProperty_nullValue_noChange() {
        MapPropertyStore<Object> s = createGenericMapPropertyStore();
        s.appendToProperty("key", null, ",");
        assertNull(s.getProperty("key"));
    }

    @Test
    void appendToProperty_nullExistingValue_setsNewValue() {
        MapPropertyStore<Object> s = createGenericMapPropertyStore();
        s.appendToProperty("key", "first", ",");
        assertEquals("first", s.getProperty("key"));
    }

    @Test
    void appendToProperty_emptyNewValue_keepsExisting() {
        MapPropertyStore<Object> s = createGenericMapPropertyStore();
        s.putProperty("key", "existing");
        s.appendToProperty("key", "", ",");
        assertEquals("existing", s.getProperty("key"));
    }

    @Test
    void appendToProperty_emptySeparator_concatenatesDirectly() {
        MapPropertyStore<Object> s = createGenericMapPropertyStore();
        s.putProperty("key", "hello");
        s.appendToProperty("key", "world", "");
        assertEquals("helloworld", s.getProperty("key"));
    }

    @Test
    void appendToProperty_nullSeparator_concatenatesDirectly() {
        MapPropertyStore<Object> s = createGenericMapPropertyStore();
        s.putProperty("key", "hello");
        s.appendToProperty("key", "world", null);
        assertEquals("helloworld", s.getProperty("key"));
    }

    @Test
    void appendToProperty_withSeparator_appendsSeparatorAndValue() {
        MapPropertyStore<Object> s = createGenericMapPropertyStore();
        s.putProperty("key", "a");
        s.appendToProperty("key", "b", "::");
        assertEquals("a::b", s.getProperty("key"));
    }

    // -------------------------------------------------------------------------
    // toReadWrite
    // -------------------------------------------------------------------------

    @Test
    void toReadWrite_returnsWrappedObject() {
        MapPropertyStore<Object> s = createGenericMapPropertyStore();
        GetPutProperty rw = s.toReadWrite();
        assertNotSame(s, rw);
        assertFalse(rw instanceof PropStore);
    }

    // -------------------------------------------------------------------------
    // withDefaults
    // -------------------------------------------------------------------------

    @Test
    void withDefaults_missingKey_fallsBackToDefault() {
        MapPropertyStore<Object> primary = createGenericMapPropertyStore();
        MapPropertyStore<Object> defaults = createGenericMapPropertyStore();
        defaults.putProperty("fallback", "value");
        GetPutProperty combined = primary.withDefaults(defaults);
        assertEquals("value", combined.getProperty("fallback"));
    }

    @Test
    void withDefaults_primaryTakesPrecedence() {
        MapPropertyStore<Object> primary = createGenericMapPropertyStore();
        primary.putProperty("key", "primary");
        MapPropertyStore<Object> defaults = createGenericMapPropertyStore();
        defaults.putProperty("key", "default");
        GetPutProperty combined = primary.withDefaults(defaults);
        assertEquals("primary", combined.getProperty("key"));
    }

    @Test
    void withDefaults_nullDefaults_returnsSelf() {
        MapPropertyStore<Object> s = createGenericMapPropertyStore();
        GetPutProperty result = s.withDefaults(null);
        assertSame(s, result);
    }

    // -------------------------------------------------------------------------
    // withCache
    // -------------------------------------------------------------------------

    @Test
    void withCache_noArg_returnsCachingWrapper() {
        MapPropertyStore<Object> s = createGenericMapPropertyStore();
        s.putProperty("k", "v");
        GetPutProperty cached = s.withCache();
        assertNotNull(cached);
        assertEquals("v", cached.getProperty("k"));
    }

    @Test
    void withCache_withPropertyCache_returnsCachingWrapper() {
        MapPropertyStore<Object> s = createGenericMapPropertyStore();
        s.putProperty("k", "v");
        PropertyCache cache = PropertyCache.createInstance();
        GetPutProperty cached = s.withCache(cache);
        assertNotNull(cached);
        assertEquals("v", cached.getProperty("k"));
    }

    // -------------------------------------------------------------------------
    // toReadOnly
    // -------------------------------------------------------------------------

    @Test
    void toReadOnly_returnsGetProperty() {
        MapPropertyStore<Object> s = createGenericMapPropertyStore();
        s.putProperty("ro", "val");
        GetProperty ro = s.toReadOnly();
        assertNotNull(ro);
        assertEquals("val", ro.getProperty("ro"));
    }

    // -------------------------------------------------------------------------
    // toWriteOnly
    // -------------------------------------------------------------------------

    @Test
    void toWriteOnly_returnsPutProperty() {
        MapPropertyStore<Object> s = createGenericMapPropertyStore();
        PutProperty wo = s.toWriteOnly();
        assertNotNull(wo);
        wo.putProperty("wo", "value");
        // Verify value was written to the underlying store
        assertEquals("value", s.getProperty("wo"));
    }

    // -------------------------------------------------------------------------
    // getNamespace(String)
    // -------------------------------------------------------------------------

    @Test
    void getNamespace_string_prefixesKeys() {
        // Default separator is '_', so getNamespace("ns").getProperty("key") looks for "ns_key"
        MapPropertyStore<Object> s = createGenericMapPropertyStore();
        s.putProperty("ns_key", "val");
        GetPutProperty ns = s.getNamespace("ns");
        assertEquals("val", ns.getProperty("key"));
    }

    // -------------------------------------------------------------------------
    // getNamespace(String, char)
    // -------------------------------------------------------------------------

    @Test
    void getNamespace_stringChar_customSeparator_removesPrefixForLoad() {
        MapPropertyStore<Object> s = createGenericMapPropertyStore();
        s.putProperty("ns/key", "val");
        GetPutProperty ns = s.getNamespace("ns", '/');
        assertEquals("val", ns.getProperty("key"));
    }

    // -------------------------------------------------------------------------
    // getPrefix(String)
    // -------------------------------------------------------------------------

    @Test
    void getPrefix_string_removesPrefixForLoad() {
        MapPropertyStore<Object> s = createGenericMapPropertyStore();
        s.putProperty("bar", "XYZ");
        assertEquals("XYZ", s.getPrefix("foo").getProperty("foo_bar"));
    }

    @Test
    void getPrefix_string_addsPrefixForSave() {
        MapPropertyStore<Object> s = createGenericMapPropertyStore();
        s.getPrefix("foo").putProperty("foo_bar", "XYZ");
        assertEquals(Set.of("bar"), s.getPropertyNames());
    }

    // -------------------------------------------------------------------------
    // getPrefix(String, char)
    // -------------------------------------------------------------------------

    @Test
    void getPrefix_stringChar_customSeparator_removesPrefixForLoad() {
        MapPropertyStore<Object> s = createGenericMapPropertyStore();
        s.putProperty("bar", "XYZ");
        assertEquals("XYZ", s.getPrefix("foo", '/').getProperty("foo/bar"));
    }

    @Test
    void getPrefix_stringChar_customSeparator_addsPrefixForSave() {
        MapPropertyStore<Object> s = createGenericMapPropertyStore();
        s.getPrefix("foo", '/').putProperty("foo/bar", "XYZ");
        assertEquals(Set.of("bar"), s.getPropertyNames());
    }

    // -------------------------------------------------------------------------
    // clearLocalProperties
    // -------------------------------------------------------------------------

    @Test
    void clearLocalProperties_viaMapPropertyStore_removesAllProperties() {
        // MapPropertyStore overrides clearLocalProperties() — exercises the override.
        MapPropertyStore<Object> s = createGenericMapPropertyStore();
        s.putProperty("a", "1");
        s.putProperty("b", "2");
        assertTrue(s.clearLocalProperties());
        assertNull(s.getProperty("a"));
        assertNull(s.getProperty("b"));
    }

    @Test
    void clearLocalProperties_defaultImpl_removesAllProperties() {
        // CachingGetPutProperty does NOT override clearLocalProperties(),
        // so the GetPutProperty interface default implementation runs.
        MapPropertyStore<Object> s = createGenericMapPropertyStore();
        s.putProperty("x", "1");
        s.putProperty("y", "2");
        GetPutProperty cached = s.withCache(); // CachingGetPutProperty — no override
        assertTrue(cached.clearLocalProperties());
        assertNull(s.getProperty("x"));
        assertNull(s.getProperty("y"));
    }

    // -------------------------------------------------------------------------
    // transformingValues(Function, Function)
    // -------------------------------------------------------------------------

    @Test
    void transformingValues_function_readTransformApplied() {
        MapPropertyStore<Object> s = createGenericMapPropertyStore();
        s.putProperty("key", "hello");
        GetPutProperty upper = s.transformingValues(String::toUpperCase, v -> v);
        assertEquals("HELLO", upper.getProperty("key"));
    }

    @Test
    void transformingValues_function_writeTransformApplied() {
        MapPropertyStore<Object> s = createGenericMapPropertyStore();
        GetPutProperty lower = s.transformingValues(v -> v, String::toLowerCase);
        lower.putProperty("key", "WORLD");
        assertEquals("world", s.getProperty("key"));
    }

    // -------------------------------------------------------------------------
    // transformingValues(TextTransformer, TextTransformer)
    // -------------------------------------------------------------------------

    @Test
    void transformingValues_textTransformer_readTransformApplied() {
        MapPropertyStore<Object> s = createGenericMapPropertyStore();
        s.putProperty("key", "hello");
        GetPutProperty view = s.transformingValues(new UppercaseTransformer(), new IdentityTransformer());
        assertEquals("HELLO", view.getProperty("key"));
    }

    // -------------------------------------------------------------------------
    // PutProperty default methods via toWriteOnly()
    //
    // GetPutProperty.toWriteOnly() returns ForwardingPutProperty.wrap(this), which is a pure PutProperty
    // (not a GetPutProperty). Methods that ForwardingPutProperty does not override fall through to the
    // PutProperty interface defaults, giving us coverage of those default implementations.
    // -------------------------------------------------------------------------

    @Test
    void putObjectPropertyAsString_nonNull_convertsViaObjectsToString() {
        // PutProperty.putObjectPropertyAsString() default: putProperty(name, Objects.toString(value, null))
        MapPropertyStore<Object> s = createGenericMapPropertyStore();
        PutProperty wo = s.toWriteOnly();
        wo.putObjectPropertyAsString("num", 42);
        assertEquals("42", s.getProperty("num"));
    }

    @Test
    void putObjectPropertyAsString_null_storesNull() {
        MapPropertyStore<Object> s = createGenericMapPropertyStore();
        PutProperty wo = s.toWriteOnly();
        wo.putObjectPropertyAsString("key", null);
        assertNull(s.getProperty("key"));
    }

    @Test
    void accept_biConsumer_delegatesToPutProperty() {
        // PutProperty.accept() default: putProperty(name, value)
        MapPropertyStore<Object> s = createGenericMapPropertyStore();
        PutProperty wo = s.toWriteOnly();
        wo.accept("k", "v");
        assertEquals("v", s.getProperty("k"));
    }

    @Test
    void putProperty_getNamespace_string_routesThroughPutPropertyDefault() {
        // PutProperty.getNamespace(String) default: ForwardingPutProperty.wrapInNamespace(this, space)
        // Distinct from GetPutProperty.getNamespace(String) which uses PropertyNameMappingGetPutProperty.
        MapPropertyStore<Object> s = createGenericMapPropertyStore();
        PutProperty wo = s.toWriteOnly();           // ForwardingPutProperty
        PutProperty ns = wo.getNamespace("ns");     // calls PutProperty.getNamespace default
        ns.putProperty("key", "val");
        assertEquals("val", s.getProperty("ns_key"));
    }

    @Test
    void putProperty_getNamespace_stringChar_routesThroughPutPropertyDefault() {
        MapPropertyStore<Object> s = createGenericMapPropertyStore();
        PutProperty ns = s.toWriteOnly().getNamespace("ns", '/');
        ns.putProperty("key", "val");
        assertEquals("val", s.getProperty("ns/key"));
    }

    @Test
    void putProperty_getPrefix_string_routesThroughPutPropertyDefault() {
        // PutProperty.getPrefix(String) default: ForwardingPutProperty.wrapInPrefix(this, prefix)
        MapPropertyStore<Object> s = createGenericMapPropertyStore();
        PutProperty pfx = s.toWriteOnly().getPrefix("ns");   // calls PutProperty.getPrefix default
        pfx.putProperty("ns_key", "val");
        assertEquals("val", s.getProperty("key"));
    }

    @Test
    void putProperty_getPrefix_stringChar_routesThroughPutPropertyDefault() {
        MapPropertyStore<Object> s = createGenericMapPropertyStore();
        PutProperty pfx = s.toWriteOnly().getPrefix("ns", '/');
        pfx.putProperty("ns/key", "val");
        assertEquals("val", s.getProperty("key"));
    }

    @Test
    void transformingValuesOnWrite_function_transformsWrittenValues() {
        // PutProperty.transformingValuesOnWrite(Function) default
        MapPropertyStore<Object> s = createGenericMapPropertyStore();
        PutProperty upper = s.toWriteOnly().transformingValuesOnWrite(String::toUpperCase);
        upper.putProperty("key", "hello");
        assertEquals("HELLO", s.getProperty("key"));
    }

    @Test
    void transformingValuesOnWrite_textTransformer_transformsWrittenValues() {
        // PutProperty.transformingValuesOnWrite(TextTransformer) default
        MapPropertyStore<Object> s = createGenericMapPropertyStore();
        PutProperty upper = s.toWriteOnly().transformingValuesOnWrite(new UppercaseTransformer());
        upper.putProperty("key", "hello");
        assertEquals("HELLO", s.getProperty("key"));
    }

    @Test
    void clearLocalProperties_minimalPutPropertyImpl_returnsFalse() {
        // PutProperty.clearLocalProperties() default returns false and does nothing.
        // All in-repo PutProperty implementations shadow this default, so MinimalPutProperty
        // is the only way to reach it. This documents the contract for external implementors.
        assertFalse(new ClearLocalPropertiesTestPutProperty().clearLocalProperties());
    }

}
