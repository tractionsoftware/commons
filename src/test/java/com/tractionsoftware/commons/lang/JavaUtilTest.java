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

package com.tractionsoftware.commons.lang;

import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public final class JavaUtilTest {

    @Test
    public final void test_stringNull() {
        assertEquals(0, JavaUtil.getApproximateInternalByteSize((String) null));
    }

    @Test
    public final void test_stringEmpty() {
        assertEquals(8, JavaUtil.getApproximateInternalByteSize(""));
    }

    @Test
    public final void test_stringSize1() {
        assertEquals(6 + 8, JavaUtil.getApproximateInternalByteSize("foo"));
    }

    @Test
    public final void test_stringSize2() {
        assertEquals(
            16 + 8,
            // 6 chars + 2 chars for the double-width 😂 glyph
            JavaUtil.getApproximateInternalByteSize("LOLOL 😂")
        );
    }

    @Test
    public final void test_mapSizeNull() {
        assertEquals(0, JavaUtil.getApproximateInternalByteSize((Map<String,String>) null));
    }

    @Test
    public final void test_mapSizeEmpty() {
        assertEquals(16, JavaUtil.getApproximateInternalByteSize(Map.of()));
    }

    @Test
    public final void test_mapSize1() {
        assertEquals(
            // Map overhead = 16
            // Entries overhead = 1 * 16 = 16
            // Key = 5 * 2 + 8 = 18
            // Value = 2 * 2 + 8 = 12
            16 + 16 + 18 + 12,
            JavaUtil.getApproximateInternalByteSize(Map.of("smile", "😃"))
        );
    }

    @Test
    public final void test_mapSize2() {
        assertEquals(
            // Map overhead = 16
            // Entries overhead = 2 * 16 = 32
            // Key 1 = 1 * 2 + 8 = 10
            // Value 1 = 8 * 2 + 8 = 24
            // Key 2 = 3 * 2 + 8 = 14
            // Value 1 = 4 * 2 + 8 = 16
            16 + 32 + 10 + 24 + 14 + 16,
            JavaUtil.getApproximateInternalByteSize(
                Map.of("a", "alphabet", "bee", "buzz")
            )
        );
    }

    @Test
    public final void test_collectionSizeNull() {
        assertEquals(0, JavaUtil.getApproximateInternalByteSize((Collection<String>) null));
    }

    @Test
    public final void test_collectionSizeEmpty() {
        assertEquals(16, JavaUtil.getApproximateInternalByteSize(Collections.emptyList()));
    }

    @Test
    public final void test_collectionSize1() {
        assertEquals(
            // Collection overhead = 16
            // Element = 3 * 2 + 8 = 14
            16 + 14,
            JavaUtil.getApproximateInternalByteSize(List.of("foo"))
        );
    }

    @Test
    public final void test_collectionSize2() {
        assertEquals(
            // Collection overhead = 16
            // Element 1 = 1 * 2 + 8 = 14
            // Element 2 = 3 * 2 + 8 = 14
            // Element 3 = 3 * 2 + 8 = 14
            // Element 4 = 2 * 2 + 8 = 12
            16 + 10 + 14 + 14 + 12,
            JavaUtil.getApproximateInternalByteSize(List.of("a", "bee", "see", "😅"))
        );
    }

}
