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

import com.tractionsoftware.commons.text.TextTransformationException;
import com.tractionsoftware.commons.text.TextTransformer;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests {@link AbstractPropertyValueMappingGetProperty} and {@link PropertyValueMappingGetProperty} together, since
 * the latter's only purpose is to provide a concrete, instantiable subclass of the former.
 */
class PropertyValueMappingGetPropertyTest {

    /**
     * Minimal concrete {@link TextTransformer}: {@code TextTransformer} is not a simple functional interface (it
     * declares two abstract methods), so it cannot be supplied as a lambda. This implementation uppercases text via
     * the {@code CharSequence}/{@code Appendable} overload, which is the only one exercised by
     * {@link PropertyValueMappingGetProperty#wrapWithValueTransformer}.
     */
    private static final class UppercaseTextTransformer implements TextTransformer {

        @Override
        public void transform(CharSequence text, Appendable out) throws IOException, TextTransformationException {
            out.append(text == null ? "" : text.toString().toUpperCase());
        }

        @Override
        public void transform(Reader in, Writer out) throws IOException, TextTransformationException {
            throw new UnsupportedOperationException("not used by this test");
        }

    }

    @Test
    void getProperty_appliesReadValueMapper() {
        GetProperty source = MapPropertyStore.createInstance(new HashMap<>(Map.of("a", "1")));
        GetProperty mapped = PropertyValueMappingGetProperty.applyPropertyValueTransformer(
            source, (name, value) -> value == null ? null : value + "-mapped"
        );
        assertEquals("1-mapped", mapped.getProperty("a"));
    }

    @Test
    void getPropertyNames_delegatesToWrappedProps() {
        GetProperty source = MapPropertyStore.createInstance(new HashMap<>(Map.of("a", "1", "b", "2")));
        GetProperty mapped = PropertyValueMappingGetProperty.applyPropertyValueTransformer(
            source, (name, value) -> value
        );
        assertEquals(Set.of("a", "b"), mapped.getPropertyNames());
    }

    @Test
    void getDefaults_withNoDefaults_returnsNull() {
        GetProperty source = MapPropertyStore.createInstance(new HashMap<>(Map.of("a", "1")));
        GetProperty mapped = PropertyValueMappingGetProperty.applyPropertyValueTransformer(
            source, (name, value) -> value
        );
        assertNull(mapped.getDefaults());
    }

    @Test
    void getDefaults_withDefaults_returnsRewrappedDefaultsWithSameMapper() {
        GetProperty defaults = MapPropertyStore.createInstance(new HashMap<>(Map.of("a", "default-1")));
        GetProperty source = MapPropertyStore.createInstance(new HashMap<>(Map.of("a", "1"))).withDefaults(defaults);
        GetProperty mapped = PropertyValueMappingGetProperty.applyPropertyValueTransformer(
            source, (name, value) -> value == null ? null : value + "-mapped"
        );
        assertEquals("default-1-mapped", mapped.getDefaults().getProperty("a"));
    }

    @Test
    void getLocals_returnsRewrappedLocalsWithSameMapper() {
        GetProperty source = MapPropertyStore.createInstance(new HashMap<>(Map.of("a", "1")));
        GetProperty mapped = PropertyValueMappingGetProperty.applyPropertyValueTransformer(
            source, (name, value) -> value == null ? null : value + "-mapped"
        );
        assertEquals("1-mapped", mapped.getLocals().getProperty("a"));
    }

    @Test
    void applyPropertyValueTransformer_functionOverload_appliesValueOnlyMapper() {
        GetProperty source = MapPropertyStore.createInstance(new HashMap<>(Map.of("a", "1")));
        GetProperty mapped = PropertyValueMappingGetProperty.applyPropertyValueTransformer(
            source, (java.util.function.Function<String,String>) value -> value + "-f"
        );
        assertEquals("1-f", mapped.getProperty("a"));
    }

    @Test
    void applyPropertyValueTransformer_nullProps_returnsNull() {
        assertNull(PropertyValueMappingGetProperty.applyPropertyValueTransformer(null, (name, value) -> value));
    }

    @Test
    void applyPropertyValueTransformer_nullMapper_returnsInputUnchanged() {
        GetProperty source = MapPropertyStore.createInstance(new HashMap<>(Map.of("a", "1")));
        GetProperty mapped = PropertyValueMappingGetProperty.applyPropertyValueTransformer(
            source, (java.util.function.BiFunction<String,String,String>) null
        );
        assertSame(source, mapped);
    }

    @Test
    void wrapWithValueTransformer_appliesTransformerToReadValues() {
        GetProperty source = MapPropertyStore.createInstance(new HashMap<>(Map.of("a", "hello")));
        GetProperty mapped = PropertyValueMappingGetProperty.wrapWithValueTransformer(source, new UppercaseTextTransformer());
        assertEquals("HELLO", mapped.getProperty("a"));
    }

    @Test
    void wrapWithValueTransformer_nullTransformer_returnsInputUnchanged() {
        GetProperty source = MapPropertyStore.createInstance(new HashMap<>(Map.of("a", "1")));
        GetProperty mapped = PropertyValueMappingGetProperty.wrapWithValueTransformer(source, null);
        assertSame(source, mapped);
    }

    @Test
    void toString_includesGetPropertyPrefix() {
        GetProperty source = MapPropertyStore.createInstance(new HashMap<>(Map.of("a", "1")));
        GetProperty mapped = PropertyValueMappingGetProperty.applyPropertyValueTransformer(
            source, (name, value) -> value
        );
        assertTrue(mapped.toString().startsWith("GetProperty: "));
    }

}
