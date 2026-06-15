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

import com.google.common.collect.HashBiMap;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public final class BiMapPropertyNameMapperTest {

    // internal name → external name
    // internal: "firstName", external: "first_name"
    private BiMapPropertyNameMapper mapper;
    private BiMapPropertyNameMapper inverseMapper;

    @BeforeEach
    void setUp() {
        var bimap = HashBiMap.<String,String>create();
        bimap.put("firstName", "first_name");
        bimap.put("lastName", "last_name");
        mapper = new BiMapPropertyNameMapper(bimap);
        // inverse: external→internal (i.e. looking up by published name → property name)
        inverseMapper = new BiMapPropertyNameMapper(bimap.inverse());
    }

    @Test
    void getActualPropertyName_fromExternal_returnsInternal() {
        // "first_name" (external/requested) → "firstName" (internal/property)
        assertEquals("firstName", mapper.getActualPropertyName("first_name"));
    }

    @Test
    void getActualPropertyName_unknown_returnsNull() {
        assertNull(mapper.getActualPropertyName("unknown"));
    }

    @Test
    void getPublishedName_fromInternal_returnsExternal() {
        assertEquals("first_name", mapper.getPublishedName("firstName"));
    }

    @Test
    void getPublishedName_unknown_returnsNull() {
        assertNull(mapper.getPublishedName("unknown"));
    }

    @Test
    void isInverseOf_withActualInverse_true() {
        assertTrue(mapper.isInverseOf(inverseMapper));
    }

    @Test
    void isInverseOf_withSelf_false() {
        assertFalse(mapper.isInverseOf(mapper));
    }

    @Test
    void isInverseOf_withNonBiMapMapper_false() {
        PropertyNameMapper other = SimplePropertyNameMapper.getNamespaceInstanceWithDefaultSeparator("ns");
        assertFalse(mapper.isInverseOf(other));
    }

}
