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

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.tractionsoftware.commons.util.CollectionUtil;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.AssertionsKt.assertNull;

/**
 * @author Dave Shepperton
 */
public final class BasicPropertyCollectionsTest {

    private static final GetProperty simpleGetProperty() {

        return new GetProperty() {

            @Override
            public final String getProperty(String name) {
                if ("foo".equals(name)) {
                    return "bar";
                }
                return null;
            }

            @Override
            public final Set<String> getPropertyNames() {
                return ImmutableSet.of("foo");
            }

        };

    }

    private static final PutProperty simplePutProperty() {
        final Map<String,String> map = new LinkedHashMap<>();
        return (name, value) -> CollectionUtil.putOrRemove(map, name, value);
    }

    private static final GetPutProperty simpleGetPutProperty() {

        final Map<String,String> map = new LinkedHashMap<>();

        return new GetPutProperty() {

            @Override
            public final String getProperty(String name) {
                return map.get(name);
            }

            @Override
            public final Set<String> getPropertyNames() {
                return Collections.unmodifiableSet(map.keySet());
            }

            @Override
            public final void putProperty(String name, String value) {
                CollectionUtil.putOrRemove(map, name, value);
            }

        };

    }

    @Test
    public void testGetPropertyToReadOnlyReturnsSelf() {
        GetProperty fromMap = simpleGetProperty();
        assertSame(fromMap, fromMap.toReadOnly());
    }

    @Test
    public void testGetPutPropertyToReadOnlyNotGetPutProperty() {
        GetProperty readOnly = simpleGetPutProperty().toReadOnly();
        assertFalse(readOnly instanceof GetPutProperty);
    }

    @Test
    public void testGetPutPropertyToWriteOnlyNotGetProperty() {
        PutProperty writeOnly = simpleGetPutProperty().toWriteOnly();
        assertFalse(writeOnly instanceof GetProperty);
    }

    @Test
    public void testGetPutPropertyToReadWriteReturnsSelf() {
        GetPutProperty fromMap = simpleGetPutProperty();
        assertSame(fromMap, fromMap.toReadWrite());
    }

    @Test
    public void testPutPropertyToWriteOnlyReturnsSelf() {
        PutProperty fromMap = simplePutProperty();
        assertSame(fromMap, fromMap.toWriteOnly());
    }

    @Test
    public void testPropStoreToReadOnlyNotGetPutProperty() {
        GetProperty readOnly = new MapPropertyStore<>().toReadOnly();
        assertFalse(readOnly instanceof GetPutProperty);
    }

    @Test
    public void testPropStoreToWriteOnlyNotGetProperty() {
        PutProperty writeOnly = new MapPropertyStore<>().toWriteOnly();
        assertFalse(writeOnly instanceof GetProperty);
    }

    @Test
    public void testPropStoreToReadWriteNotPropStore() {
        GetPutProperty readWriteOnly = new MapPropertyStore<>().toReadWrite();
        assertFalse(readWriteOnly instanceof PropStore);
    }

    @Test
    public void testLocalsIsSameObjectForLeafGetProperty() {
        GetProperty get = simpleGetProperty();
        assertSame(get, get.getLocals());
    }

    @Test
    public void testDefaultsIsNullForLeafGetProperty() {
        assertNull(simpleGetProperty().getDefaults());
    }

    @Test
    public void testNoLocalPropertyFromDefaults() {
        GetProperty locals = SimpleProperties.emptyGetProperty();
        GetProperty defaults = SimpleProperties.asGetProperty(ImmutableMap.of("foo", "bar"));
        assertNull(locals.withDefaults(defaults).getLocalProperty("foo"));
    }

    @Test
    public void testEmptyWithNonEmptyDefaultsIsNotEmpty() {
        GetProperty locals = SimpleProperties.emptyGetProperty();
        GetProperty defaults = SimpleProperties.asGetProperty(ImmutableMap.of("foo", "bar"));
        assertFalse(locals.withDefaults(defaults).isEmpty());
    }

    @Test
    public void testNamespaceWithOnePropertyIsNotEmpty() {
        GetProperty get = SimpleProperties.asGetProperty(ImmutableMap.of("foo_bar", "baz"));
        assertFalse(get.getNamespace("foo").isEmpty());
    }

    /**
     * For the "foo" namespace, the property in the wrapped store called "foo" can be requested via null or empty
     * string; but null or empty String is not included in the published property names for the namespace in that case.
     * This means that {@link GetProperty#isEmpty()} returns true if that's the only property.
     */
    @Test
    public void testSingleExactNamespacePropertyNameIsEmpty() {
        GetProperty get = SimpleProperties.asGetProperty(ImmutableMap.of("foo", "baz"));
        assertTrue(get.getNamespace("foo").isEmpty());
    }

    @Test
    public void testNestedDefaultChainGivesNullForLocalPropertyWithDefault() {
        GetProperty a = SimpleProperties.asGetProperty(ImmutableMap.of("test", "a"));
        GetProperty b = SimpleProperties.asGetProperty(ImmutableMap.of("test", "b"));
        GetProperty c = SimpleProperties.asGetProperty(ImmutableMap.of("test", "c"));
        GetProperty d = SimpleProperties.asGetProperty(ImmutableMap.of("test", "d"));
        GetProperty main = SimpleProperties.emptyGetProperty();
        GetProperty test = main.withDefaults(a.withDefaults(b.withDefaults(c.withDefaults(d))));
        assertNull(test.getLocalProperty("test"));
    }

    @Test
    public void testNestedDefaultChainGivesNullForLocalsGetPropertyWithDefault() {
        GetProperty a = SimpleProperties.asGetProperty(ImmutableMap.of("test", "a"));
        GetProperty b = SimpleProperties.asGetProperty(ImmutableMap.of("test", "b"));
        GetProperty c = SimpleProperties.asGetProperty(ImmutableMap.of("test", "c"));
        GetProperty d = SimpleProperties.asGetProperty(ImmutableMap.of("test", "d"));
        GetProperty main = SimpleProperties.emptyGetProperty();
        GetProperty test = main.withDefaults(a.withDefaults(b.withDefaults(c.withDefaults(d))));
        assertNull(test.getLocals().getProperty("test"));
    }

    @Test
    public void testNestedDefaultChainGivesHighestLevelDefault() {
        GetProperty a = SimpleProperties.asGetProperty(ImmutableMap.of("test", "a"));
        GetProperty b = SimpleProperties.asGetProperty(ImmutableMap.of("test", "b"));
        GetProperty c = SimpleProperties.asGetProperty(ImmutableMap.of("test", "c"));
        GetProperty d = SimpleProperties.asGetProperty(ImmutableMap.of("test", "d"));
        GetProperty main = SimpleProperties.emptyGetProperty();
        GetProperty test = main.withDefaults(a.withDefaults(b.withDefaults(c.withDefaults(d))));
        assertEquals("a", test.getProperty("test"));
    }

}
