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

import java.util.TreeSet;

import static org.junit.jupiter.api.Assertions.*;

class AbstractMapPropertyLoaderTest {

    private static final class JoinedNamesLoader extends AbstractMapPropertyLoader<String> {

        JoinedNamesLoader() {
            super(String.class);
        }

        @Override
        protected String load(GetProperty props) {
            return String.join(",", new TreeSet<>(props.getPropertyNames()));
        }

    }

    @Test
    void apply_parsesRawValueIntoMapAndDelegatesToLoad() {
        JoinedNamesLoader loader = new JoinedNamesLoader();
        assertEquals("a,b", loader.apply("propName", "a=1,b=2"));
    }

    @Test
    void apply_loadCanReadIndividualPropertiesFromParsedMap() {
        AbstractMapPropertyLoader<String> loader = new AbstractMapPropertyLoader<>(String.class) {
            @Override
            protected String load(GetProperty props) {
                return props.getProperty("a");
            }
        };
        assertEquals("1", loader.apply("propName", "a=1,b=2"));
    }

    @Test
    void cast_returnsValueWhenAssignmentCompatible() {
        JoinedNamesLoader loader = new JoinedNamesLoader();
        assertEquals("hello", loader.cast("hello"));
    }

}
