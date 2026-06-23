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

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests {@link AbstractPropertyValueMappingGetPutProperty} and {@link PropertyValueMappingGetPutProperty} together,
 * since the latter's only purpose is to provide a concrete, instantiable subclass of the former.
 */
class PropertyValueMappingGetPutPropertyTest {

    /**
     * Minimal concrete {@link TextTransformer} (it has two abstract methods, so it cannot be supplied as a lambda).
     * Uppercases text via the {@code CharSequence}/{@code Appendable} overload, the only one exercised here.
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
        GetPutProperty source = MapPropertyStore.createInstance(new HashMap<>(Map.of("a", "1")));
        GetPutProperty mapped = PropertyValueMappingGetPutProperty.applyPropertyValueTransformers(
            source, (name, value) -> value == null ? null : value + "-r", (name, value) -> value
        );
        assertEquals("1-r", mapped.getProperty("a"));
    }

    @Test
    void putProperty_appliesWriteValueMapper() {
        Map<String,String> backing = new HashMap<>();
        GetPutProperty source = MapPropertyStore.createInstance(backing);
        GetPutProperty mapped = PropertyValueMappingGetPutProperty.applyPropertyValueTransformers(
            source, (name, value) -> value, (name, value) -> value == null ? null : value + "-w"
        );
        mapped.putProperty("a", "1");
        assertEquals("1-w", backing.get("a"));
    }

    @Test
    void getPropertyNames_delegatesToWrappedProps() {
        GetPutProperty source = MapPropertyStore.createInstance(new HashMap<>(Map.of("a", "1", "b", "2")));
        GetPutProperty mapped = PropertyValueMappingGetPutProperty.applyPropertyValueTransformers(
            source, (name, value) -> value, (name, value) -> value
        );
        assertEquals(source.getPropertyNames(), mapped.getPropertyNames());
    }

    @Test
    void toString_includesGetPutPropertyPrefix() {
        GetPutProperty source = MapPropertyStore.createInstance(new HashMap<>(Map.of("a", "1")));
        GetPutProperty mapped = PropertyValueMappingGetPutProperty.applyPropertyValueTransformers(
            source, (name, value) -> value, (name, value) -> value
        );
        assertTrue(mapped.toString().startsWith("GetPutProperty: "));
    }

    @Test
    void wrapWithValueTransformers_appliesReadAndWriteTransformers() {

        Map<String,String> backing = new HashMap<>(Map.of("a", "x"));
        GetPutProperty source = MapPropertyStore.createInstance(backing);
        TextTransformer upper = new UppercaseTextTransformer();

        GetPutProperty mapped = PropertyValueMappingGetPutProperty.wrapWithValueTransformers(source, upper, upper);

        assertEquals("X", mapped.getProperty("a"));
        mapped.putProperty("a", "y");
        assertEquals("Y", backing.get("a"));

    }

    @Test
    void wrapWithValueTransformers_bothNullTransformersAndNullProps_returnsNull() {
        assertNull(PropertyValueMappingGetPutProperty.wrapWithValueTransformers(null, null, null));
    }

    @Test
    void applyPropertyValueTransformers_functionOverload_appliesMappers() {

        Map<String,String> backing = new HashMap<>(Map.of("a", "x"));
        GetPutProperty source = MapPropertyStore.createInstance(backing);

        GetPutProperty mapped = PropertyValueMappingGetPutProperty.applyPropertyValueTransformers(
            source,
            (java.util.function.Function<String,String>) value -> value + "-r",
            (java.util.function.Function<String,String>) value -> value + "-w"
        );

        assertEquals("x-r", mapped.getProperty("a"));
        mapped.putProperty("b", "y");
        assertEquals("y-w", backing.get("b"));

    }

    @Test
    void applyPropertyValueTransformers_biFunctionOverload_appliesMappers() {

        Map<String,String> backing = new HashMap<>(Map.of("a", "x"));
        GetPutProperty source = MapPropertyStore.createInstance(backing);

        GetPutProperty mapped = PropertyValueMappingGetPutProperty.applyPropertyValueTransformers(
            source, (name, value) -> name + ":" + value, (name, value) -> name + ":" + value
        );

        assertEquals("a:x", mapped.getProperty("a"));
        mapped.putProperty("b", "y");
        assertEquals("b:y", backing.get("b"));

    }

}
