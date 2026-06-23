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

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class ComplexPropertyTest {

    private static final class SimpleComplexProperty implements ComplexProperty {

        private final Map<String,String> state;

        SimpleComplexProperty(Map<String,String> state) {
            this.state = state;
        }

        @Override
        public String toString() {
            return "SimpleComplexProperty" + state;
        }

        @Override
        public void saveInstance(GetPutProperty store) {
            for (Map.Entry<String,String> entry : state.entrySet()) {
                store.putProperty(entry.getKey(), entry.getValue());
            }
        }

    }

    @Test
    void saveInstance_putProperty_copiesAllPropertiesViaPutAllProperties() {

        ComplexProperty complex = new SimpleComplexProperty(Map.of("a", "1", "b", "2"));
        Map<String,String> target = new HashMap<>();
        PutProperty put = SimpleProperties.asPutProperty(target);

        complex.saveInstance(put);

        assertEquals("1", target.get("a"));
        assertEquals("2", target.get("b"));

    }

}
