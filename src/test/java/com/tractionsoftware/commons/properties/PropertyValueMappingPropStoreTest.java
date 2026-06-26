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
 * Tests {@link PropertyValueMappingPropStore} (and, transitively, the shared logic in
 * {@link AbstractPropertyValueMappingGetPutProperty} and {@link AbstractPropertyValueMappingPropertyCollection} as
 * exercised through it). {@link PropertyValueMappingTest} already covers the {@code transformingValues(TextTransformer,
 * TextTransformer)} entry point via {@link PropStore#transformingValues}; this file fills in the remaining static
 * factory overloads, guard branches, {@code commitChanges} delegation, and {@code toString}.
 */
class PropertyValueMappingPropStoreTest {

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

    private static PropStore<Object> newStore(Map<String,String> backing) {
        return MapPropertyStore.createInstance(backing);
    }

    // ====================================================================
    // wrapWithValueTransformers(PropStore, TextTransformer, TextTransformer)
    // ====================================================================

    @Test
    void wrapWithValueTransformers_appliesReadAndWriteTransformers() {
        Map<String,String> backing = new HashMap<>(Map.of("a", "x"));
        PropStore<Object> source = newStore(backing);
        TextTransformer upper = new UppercaseTextTransformer();

        PropStore<Object> mapped = PropertyValueMappingPropStore.wrapWithValueTransformers(source, upper, upper);

        assertEquals("X", mapped.getProperty("a"));
        mapped.putProperty("a", "y");
        assertEquals("Y", backing.get("a"));
    }

    @Test
    void wrapWithValueTransformers_nullProps_returnsNull() {
        assertNull(PropertyValueMappingPropStore.wrapWithValueTransformers(null, new UppercaseTextTransformer(), null));
    }

    @Test
    void wrapWithValueTransformers_bothTransformersNull_returnsInputUnchanged() {
        PropStore<Object> source = newStore(new HashMap<>());
        PropStore<Object> mapped = PropertyValueMappingPropStore.wrapWithValueTransformers(source, null, null);
        assertSame(source, mapped);
    }

    @Test
    void wrapWithValueTransformers_onlyReadTransformerGiven_stillWraps() {
        Map<String,String> backing = new HashMap<>(Map.of("a", "x"));
        PropStore<Object> source = newStore(backing);
        PropStore<Object> mapped = PropertyValueMappingPropStore.wrapWithValueTransformers(
            source, new UppercaseTextTransformer(), null
        );
        assertEquals("X", mapped.getProperty("a"));
        mapped.putProperty("a", "y");
        // write side has no transformer, so the value passes through unchanged.
        assertEquals("y", backing.get("a"));
    }

    // ====================================================================
    // applyPropertyValueTransformers(PropStore, Function, Function)
    // ====================================================================

    @Test
    void applyPropertyValueTransformers_functionOverload_appliesMappers() {
        Map<String,String> backing = new HashMap<>(Map.of("a", "x"));
        PropStore<Object> source = newStore(backing);

        PropStore<Object> mapped = PropertyValueMappingPropStore.applyPropertyValueTransformers(
            source,
            (java.util.function.Function<String,String>) value -> value + "-r",
            (java.util.function.Function<String,String>) value -> value + "-w"
        );

        assertEquals("x-r", mapped.getProperty("a"));
        mapped.putProperty("b", "y");
        assertEquals("y-w", backing.get("b"));
    }

    @Test
    void applyPropertyValueTransformers_functionOverload_nullProps_returnsNull() {
        assertNull(PropertyValueMappingPropStore.applyPropertyValueTransformers(
            null, (java.util.function.Function<String,String>) value -> value, null
        ));
    }

    @Test
    void applyPropertyValueTransformers_functionOverload_bothMappersNull_returnsInputUnchanged() {
        PropStore<Object> source = newStore(new HashMap<>());
        PropStore<Object> mapped = PropertyValueMappingPropStore.applyPropertyValueTransformers(
            source, (java.util.function.Function<String,String>) null, (java.util.function.Function<String,String>) null
        );
        assertSame(source, mapped);
    }

    // ====================================================================
    // applyPropertyValueTransformers(PropStore, BiFunction, BiFunction)
    // ====================================================================

    @Test
    void applyPropertyValueTransformers_biFunctionOverload_appliesMappers() {
        Map<String,String> backing = new HashMap<>(Map.of("a", "x"));
        PropStore<Object> source = newStore(backing);

        PropStore<Object> mapped = PropertyValueMappingPropStore.applyPropertyValueTransformers(
            source, (name, value) -> name + ":" + value, (name, value) -> name + ":" + value
        );

        assertEquals("a:x", mapped.getProperty("a"));
        mapped.putProperty("b", "y");
        assertEquals("b:y", backing.get("b"));
    }

    @Test
    void applyPropertyValueTransformers_biFunctionOverload_nullProps_returnsNull() {
        assertNull(PropertyValueMappingPropStore.applyPropertyValueTransformers(
            null, (java.util.function.BiFunction<String,String,String>) (name, value) -> value, null
        ));
    }

    @Test
    void applyPropertyValueTransformers_biFunctionOverload_bothMappersNull_returnsInputUnchanged() {
        PropStore<Object> source = newStore(new HashMap<>());
        PropStore<Object> mapped = PropertyValueMappingPropStore.applyPropertyValueTransformers(
            source,
            (java.util.function.BiFunction<String,String,String>) null,
            (java.util.function.BiFunction<String,String,String>) null
        );
        assertSame(source, mapped);
    }

    @Test
    void applyPropertyValueTransformers_biFunctionOverload_onlyWriteMapperGiven_readFallsBackToIdentity() {
        Map<String,String> backing = new HashMap<>(Map.of("a", "x"));
        PropStore<Object> source = newStore(backing);

        PropStore<Object> mapped = PropertyValueMappingPropStore.applyPropertyValueTransformers(
            source,
            (java.util.function.BiFunction<String,String,String>) null,
            (name, value) -> name + ":" + value
        );

        // read side has no mapper, so getProperty falls back to BI_VALUE_IDENTITY (value passes through unchanged).
        assertEquals("x", mapped.getProperty("a"));
        mapped.putProperty("b", "y");
        assertEquals("b:y", backing.get("b"));
    }

    // ====================================================================
    // commitChanges / toString
    // ====================================================================

    @Test
    void commitChanges_delegatesToWrappedPropStore() {
        PropStore<Object> source = newStore(new HashMap<>());
        PropStore<Object> mapped = PropertyValueMappingPropStore.applyPropertyValueTransformers(
            source, (name, value) -> value, (name, value) -> value
        );
        // MapPropertyStore.commitChanges always returns this exact shared instance.
        assertSame(CommitResults.RESULT_SUCCESSFUL, mapped.commitChanges(new Object()));
    }

    @Test
    void toString_includesPropStorePrefix() {
        PropStore<Object> source = newStore(new HashMap<>());
        PropStore<Object> mapped = PropertyValueMappingPropStore.applyPropertyValueTransformers(
            source, (name, value) -> value, (name, value) -> value
        );
        assertTrue(mapped.toString().startsWith("PropStore: "));
    }

}
