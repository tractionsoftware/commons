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

import java.math.BigDecimal;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class NativeTypeConversionTest {

    // ---------------------------------------------------------------------------
    // stringToBoolean
    // ---------------------------------------------------------------------------

    @Test
    void stringToBoolean_null_returnsFalse() {
        assertFalse(NativeTypeConversion.stringToBoolean(null));
    }

    @Test
    void stringToBoolean_null_withDefault() {
        assertTrue(NativeTypeConversion.stringToBoolean(null, true));
    }

    @Test
    void stringToBoolean_trueString() {
        assertTrue(NativeTypeConversion.stringToBoolean("true"));
        assertTrue(NativeTypeConversion.stringToBoolean("TRUE"));
        assertTrue(NativeTypeConversion.stringToBoolean("True"));
    }

    @Test
    void stringToBoolean_falseString() {
        assertFalse(NativeTypeConversion.stringToBoolean("false"));
        assertFalse(NativeTypeConversion.stringToBoolean("FALSE"));
        assertFalse(NativeTypeConversion.stringToBoolean("f"));
        assertFalse(NativeTypeConversion.stringToBoolean("F"));
    }

    @Test
    void stringToBoolean_nonFalseStringIsTrue() {
        // any non-null non-"f"/"false" string is true
        assertTrue(NativeTypeConversion.stringToBoolean("yes"));
        assertTrue(NativeTypeConversion.stringToBoolean("boo"));
        assertTrue(NativeTypeConversion.stringToBoolean("nothing"));
        assertTrue(NativeTypeConversion.stringToBoolean("77"));
        assertTrue(NativeTypeConversion.stringToBoolean("1"));
        assertTrue(NativeTypeConversion.stringToBoolean("0"));
        assertTrue(NativeTypeConversion.stringToBoolean("-1"));
    }

    // ---------------------------------------------------------------------------
    // stringToInt
    // ---------------------------------------------------------------------------

    @Test
    void stringToInt_null_returnsMinValue() {
        assertEquals(Integer.MIN_VALUE, NativeTypeConversion.stringToInt(null));
    }

    @Test
    void stringToInt_valid() {
        assertEquals(42, NativeTypeConversion.stringToInt("42"));
        assertEquals(-7, NativeTypeConversion.stringToInt("-7"));
    }

    @Test
    void stringToInt_invalid_returnsDefault() {
        assertEquals(-1, NativeTypeConversion.stringToInt("abc", -1));
    }

    @Test
    void stringToInt_overflow_returnsDefault() {
        assertEquals(-1, NativeTypeConversion.stringToInt("99999999999", -1));
    }

    // ---------------------------------------------------------------------------
    // stringToLong
    // ---------------------------------------------------------------------------

    @Test
    void stringToLong_null_returnsMinValue() {
        assertEquals(Long.MIN_VALUE, NativeTypeConversion.stringToLong(null));
    }

    @Test
    void stringToLong_valid() {
        assertEquals(123456789012L, NativeTypeConversion.stringToLong("123456789012"));
    }

    @Test
    void stringToLong_invalid_returnsDefault() {
        assertEquals(-1L, NativeTypeConversion.stringToLong("nope", -1L));
    }

    // ---------------------------------------------------------------------------
    // stringToDouble
    // ---------------------------------------------------------------------------

    @Test
    void stringToDouble_null_returnsNaN() {
        assertTrue(Double.isNaN(NativeTypeConversion.stringToDouble(null)));
    }

    @Test
    void stringToDouble_valid() {
        assertEquals(3.14, NativeTypeConversion.stringToDouble("3.14"), 1e-10);
    }

    @Test
    void stringToDouble_invalid_returnsDefault() {
        assertEquals(-1.0, NativeTypeConversion.stringToDouble("bad", -1.0));
    }

    // ---------------------------------------------------------------------------
    // stringToByte / stringToShort / stringToFloat
    // ---------------------------------------------------------------------------

    @Test
    void stringToByte_valid() {
        assertEquals((byte) 10, NativeTypeConversion.stringToByte("10"));
    }

    @Test
    void stringToByte_blank_returnsMinValue() {
        assertEquals(Byte.MIN_VALUE, NativeTypeConversion.stringToByte(""));
    }

    @Test
    void stringToShort_valid() {
        assertEquals((short) 1000, NativeTypeConversion.stringToShort("1000"));
    }

    @Test
    void stringToShort_blank_returnsMinValue() {
        assertEquals(Short.MIN_VALUE, NativeTypeConversion.stringToShort("  "));
    }

    @Test
    void stringToFloat_valid() {
        assertEquals(1.5f, NativeTypeConversion.stringToFloat("1.5"), 1e-6f);
    }

    @Test
    void stringToFloat_blank_returnsNaN() {
        assertTrue(Float.isNaN(NativeTypeConversion.stringToFloat("")));
    }

    // ---------------------------------------------------------------------------
    // stringToBigDecimal
    // ---------------------------------------------------------------------------

    @Test
    void stringToBigDecimal_valid() {
        assertEquals(new BigDecimal("123.456"), NativeTypeConversion.stringToBigDecimal("123.456", null));
    }

    @Test
    void stringToBigDecimal_blank_returnsDefault() {
        BigDecimal def = BigDecimal.ONE;
        assertSame(def, NativeTypeConversion.stringToBigDecimal("", def));
    }

    @Test
    void stringToBigDecimal_invalid_returnsDefault() {
        assertNull(NativeTypeConversion.stringToBigDecimal("xyz", null));
    }

    // ---------------------------------------------------------------------------
    // stringToUuid
    // ---------------------------------------------------------------------------

    @Test
    void stringToUuid_valid() {
        UUID uuid = UUID.randomUUID();
        assertEquals(uuid, NativeTypeConversion.stringToUuid(uuid.toString(), null));
    }

    @Test
    void stringToUuid_blank_returnsNull() {
        assertNull(NativeTypeConversion.stringToUuid("", null));
    }

    @Test
    void stringToUuid_invalid_returnsDefault() {
        UUID def = UUID.randomUUID();
        assertEquals(def, NativeTypeConversion.stringToUuid("not-a-uuid", def));
    }

    // ---------------------------------------------------------------------------
    // iterableToString
    // ---------------------------------------------------------------------------

    @Test
    void iterableToString_null_returnsNull() {
        assertNull(NativeTypeConversion.iterableToString(null));
    }

    @Test
    void iterableToString_empty_returnsNull() {
        assertNull(NativeTypeConversion.iterableToString(List.of()));
    }

    @Test
    void iterableToString_singleElement() {
        assertEquals("a", NativeTypeConversion.iterableToString(List.of("a")));
    }

    @Test
    void iterableToString_multipleElements() {
        assertEquals("a,b,c", NativeTypeConversion.iterableToString(List.of("a", "b", "c")));
    }

    // ---------------------------------------------------------------------------
    // stringToList
    // ---------------------------------------------------------------------------

    @Test
    void stringToList_null_returnsFalse() {
        assertFalse(NativeTypeConversion.stringToList(null, new ArrayList<>()));
    }

    @Test
    void stringToList_simple() {
        List<String> list = new ArrayList<>();
        assertTrue(NativeTypeConversion.stringToList("a,b,c", list));
        assertEquals(List.of("a", "b", "c"), list);
    }

    @Test
    void stringToList_singleElement() {
        List<String> list = NativeTypeConversion.stringToList("hello");
        assertEquals(List.of("hello"), list);
    }

    // ---------------------------------------------------------------------------
    // mapToString / splitStringToMap / stringToMap
    // ---------------------------------------------------------------------------

    @Test
    void mapToString_null_returnsNull() {
        assertNull(NativeTypeConversion.mapToString(null));
    }

    @Test
    void mapToString_singleEntry() {
        Map<String,String> map = Map.of("key", "val");
        String s = NativeTypeConversion.mapToString(map);
        assertEquals("key=val", s);
    }

    @Test
    void mapToString_multipleEntries_sorted() {
        Map<String,String> map = new LinkedHashMap<>();
        map.put("b", "2");
        map.put("a", "1");
        String s = NativeTypeConversion.mapToString(map);
        assertEquals("a=1,b=2", s);
    }

    @Test
    void mapToString_emptyValueSkipped() {
        Map<String,String> map = new LinkedHashMap<>();
        map.put("k", "");
        map.put("x", "v");
        String s = NativeTypeConversion.mapToString(map);
        assertEquals("x=v", s);
    }

    @Test
    void splitStringToMap_null_returnsFalse() {
        assertFalse(NativeTypeConversion.splitStringToMap(null, new HashMap<>()));
    }

    @Test
    void splitStringToMap_nullMap_returnsFalse() {
        assertFalse(NativeTypeConversion.splitStringToMap("k=v", null));
    }

    @Test
    void splitStringToMap_simple() {
        Map<String,String> map = new HashMap<>();
        assertTrue(NativeTypeConversion.splitStringToMap("a=1,b=2", map));
        assertEquals("1", map.get("a"));
        assertEquals("2", map.get("b"));
    }

    @Test
    void splitStringToMap_flagOnly_emptyValue() {
        Map<String,String> map = new HashMap<>();
        NativeTypeConversion.splitStringToMap("flag", map);
        assertEquals("", map.get("flag"));
    }

    @Test
    void stringToMap_valid_returnsMap() {
        Map<String,String> map = NativeTypeConversion.stringToMap("x=1,y=2");
        assertNotNull(map);
        assertEquals("1", map.get("x"));
        assertEquals("2", map.get("y"));
    }

    @Test
    void stringToMap_null_returnsNull() {
        assertNull(NativeTypeConversion.stringToMap(null));
    }

    @Test
    void mapToString_roundTrip() {
        Map<String,String> original = new LinkedHashMap<>();
        original.put("name", "alice");
        original.put("role", "admin");
        String serialized = NativeTypeConversion.mapToString(original);
        Map<String,String> recovered = NativeTypeConversion.stringToMap(serialized);
        assertNotNull(recovered);
        assertEquals("alice", recovered.get("name"));
        assertEquals("admin", recovered.get("role"));
    }

    // ---------------------------------------------------------------------------
    // stringToStringList / stringToStringSet
    // ---------------------------------------------------------------------------

    @Test
    void stringToStringList_null_returnsDefault() {
        List<String> def = List.of("d");
        assertSame(def, NativeTypeConversion.stringToStringList(null, def));
    }

    @Test
    void stringToStringList_commaSeparated() {
        List<String> result = NativeTypeConversion.stringToStringList("p,q,r", null);
        assertEquals(List.of("p", "q", "r"), result);
    }

    @Test
    void stringToStringSet_deduplicates() {
        Set<String> result = NativeTypeConversion.stringToStringSet("a,b,a", null);
        assertEquals(Set.of("a", "b"), result);
    }

    // ---------------------------------------------------------------------------
    // stringToNumber
    // ---------------------------------------------------------------------------

    @Test
    void stringToNumber_int() {
        Number n = NativeTypeConversion.stringToNumber("int", "42", null);
        assertEquals(42, n.intValue());
    }

    @Test
    void stringToNumber_double() {
        Number n = NativeTypeConversion.stringToNumber("double", "3.14", null);
        assertEquals(3.14, n.doubleValue(), 1e-10);
    }

    @Test
    void stringToNumber_bigdecimal() {
        Number n = NativeTypeConversion.stringToNumber("bigdecimal", "9.99", null);
        assertEquals(new BigDecimal("9.99"), n);
    }

    @Test
    void stringToNumber_invalid_returnsDefault() {
        Number def = 99;
        assertSame(def, NativeTypeConversion.stringToNumber("int", "bad", def));
    }

    @Test
    void stringToNumber_unknownType_treatedAsDouble() {
        Number n = NativeTypeConversion.stringToNumber("unknown", "2.5", null);
        assertEquals(2.5, n.doubleValue(), 1e-10);
    }

    // ---------------------------------------------------------------------------
    // booleanToString
    // ---------------------------------------------------------------------------

    @Test
    void booleanToString_true() {
        assertEquals("true", NativeTypeConversion.booleanToString(true));
    }

    @Test
    void booleanToString_false() {
        assertEquals("false", NativeTypeConversion.booleanToString(false));
    }

}
