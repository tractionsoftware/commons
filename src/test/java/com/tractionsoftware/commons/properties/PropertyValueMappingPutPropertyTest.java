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
 * Tests {@link PropertyValueMappingPutProperty}, which had no dedicated coverage even though its sibling classes
 * ({@link PropertyValueMappingGetProperty}, {@link PropertyValueMappingGetPutProperty}) do.
 */
class PropertyValueMappingPutPropertyTest {

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
    void putProperty_appliesWriteValueMapper_biFunctionOverload() {
        Map<String,String> backing = new HashMap<>();
        PutProperty source = MapPropertyStore.createInstance(backing);
        PutProperty mapped = PropertyValueMappingPutProperty.applyPropertyValueTransformer(
            source, (name, value) -> name + ":" + value
        );
        mapped.putProperty("a", "1");
        assertEquals("a:1", backing.get("a"));
    }

    @Test
    void putProperty_appliesWriteValueMapper_functionOverload() {
        Map<String,String> backing = new HashMap<>();
        PutProperty source = MapPropertyStore.createInstance(backing);
        PutProperty mapped = PropertyValueMappingPutProperty.applyPropertyValueTransformer(
            source, (java.util.function.Function<String,String>) value -> value + "-w"
        );
        mapped.putProperty("a", "1");
        assertEquals("1-w", backing.get("a"));
    }

    @Test
    void wrapWithValueTransformer_appliesTransformerToWrittenValues() {
        Map<String,String> backing = new HashMap<>();
        PutProperty source = MapPropertyStore.createInstance(backing);
        PutProperty mapped = PropertyValueMappingPutProperty.wrapWithValueTransformer(source, new UppercaseTextTransformer());
        mapped.putProperty("a", "hello");
        assertEquals("HELLO", backing.get("a"));
    }

    @Test
    void wrapWithValueTransformer_nullProps_returnsNull() {
        assertNull(PropertyValueMappingPutProperty.wrapWithValueTransformer(null, new UppercaseTextTransformer()));
    }

    @Test
    void wrapWithValueTransformer_nullTransformer_returnsInputUnchanged() {
        PutProperty source = MapPropertyStore.createInstance(new HashMap<>());
        PutProperty mapped = PropertyValueMappingPutProperty.wrapWithValueTransformer(source, null);
        assertSame(source, mapped);
    }

    @Test
    void applyPropertyValueTransformer_functionOverload_nullProps_returnsNull() {
        assertNull(PropertyValueMappingPutProperty.applyPropertyValueTransformer(
            null, (java.util.function.Function<String,String>) value -> value
        ));
    }

    @Test
    void applyPropertyValueTransformer_functionOverload_nullMapper_returnsInputUnchanged() {
        PutProperty source = MapPropertyStore.createInstance(new HashMap<>());
        PutProperty mapped = PropertyValueMappingPutProperty.applyPropertyValueTransformer(
            source, (java.util.function.Function<String,String>) null
        );
        assertSame(source, mapped);
    }

    @Test
    void applyPropertyValueTransformer_biFunctionOverload_nullProps_returnsNull() {
        assertNull(PropertyValueMappingPutProperty.applyPropertyValueTransformer(
            null, (java.util.function.BiFunction<String,String,String>) (name, value) -> value
        ));
    }

    @Test
    void applyPropertyValueTransformer_biFunctionOverload_nullMapper_returnsInputUnchanged() {
        PutProperty source = MapPropertyStore.createInstance(new HashMap<>());
        PutProperty mapped = PropertyValueMappingPutProperty.applyPropertyValueTransformer(
            source, (java.util.function.BiFunction<String,String,String>) null
        );
        assertSame(source, mapped);
    }

    @Test
    void toString_includesPutPropertyPrefix() {
        PutProperty source = MapPropertyStore.createInstance(new HashMap<>());
        PutProperty mapped = PropertyValueMappingPutProperty.applyPropertyValueTransformer(
            source, (name, value) -> value
        );
        assertTrue(mapped.toString().startsWith("PutProperty: "));
    }

}
