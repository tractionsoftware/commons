// PLEASE DO NOT DELETE THIS LINE - make copyright depends on it.
package com.tractionsoftware.commons.lang;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ObjectUtilTest {

    // ---------------------------------------------------------------------------
    // safeToString
    // ---------------------------------------------------------------------------

    @Test
    void safeToString_null_returnsNull() {
        assertNull(ObjectUtil.safeToString(null));
    }

    @Test
    void safeToString_value_returnsToString() {
        assertEquals("42", ObjectUtil.safeToString(42));
    }

    @Test
    void safeToString_withDefault_null_returnsDefault() {
        assertEquals("default", ObjectUtil.safeToString(null, "default"));
    }

    @Test
    void safeToString_throws_returnsDefault() {
        Object bad = new Object() {
            @Override
            public String toString() { throw new RuntimeException("boom"); }
        };
        assertEquals("fallback", ObjectUtil.safeToString(bad, "fallback"));
    }

    // ---------------------------------------------------------------------------
    // safeClassNameToString
    // ---------------------------------------------------------------------------

    @Test
    void safeClassNameToString_null_returnsLiteralNull() {
        Object result = ObjectUtil.safeClassNameToString(null);
        assertEquals("[null value]", result.toString());
    }

    @Test
    void safeClassNameToString_object_returnsClassName() {
        Object result = ObjectUtil.safeClassNameToString("hello");
        assertTrue(result.toString().contains("String"), "expected class name: " + result);
    }

    // ---------------------------------------------------------------------------
    // safeToStringObject
    // ---------------------------------------------------------------------------

    @Test
    void safeToStringObject_object_wrapsToString() {
        Object wrapper = ObjectUtil.safeToStringObject("world");
        assertEquals("world", wrapper.toString());
    }

    @Test
    void safeToStringObject_nullObject_returnsQuestionMark() {
        Object wrapper = ObjectUtil.safeToStringObject((Object) null);
        assertEquals("?", wrapper.toString());
    }

    @Test
    void safeToStringObject_supplier_returnsSuppliedString() {
        Object wrapper = ObjectUtil.safeToStringObject(() -> "dynamic");
        assertEquals("dynamic", wrapper.toString());
    }

    @Test
    void safeToStringObject_supplierNull_returnsDefault() {
        Object wrapper = ObjectUtil.safeToStringObject((java.util.function.Supplier<String>) null);
        assertEquals("?", wrapper.toString());
    }

    @Test
    void safeToStringObject_supplierThrows_returnsDefault() {
        Object wrapper = ObjectUtil.safeToStringObject(() -> { throw new RuntimeException("fail"); }, "safe");
        assertEquals("safe", wrapper.toString());
    }

    // ---------------------------------------------------------------------------
    // requireInstanceOf
    // ---------------------------------------------------------------------------

    @Test
    void requireInstanceOf_matchingType_noThrow() {
        assertDoesNotThrow(() -> ObjectUtil.requireInstanceOf("hello", String.class));
    }

    @Test
    void requireInstanceOf_wrongType_throwsIllegalArgument() {
        assertThrows(IllegalArgumentException.class,
            () -> ObjectUtil.requireInstanceOf(42, String.class));
    }

    @Test
    void requireInstanceOf_withMessage_wrongType_throwsWithMessage() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
            () -> ObjectUtil.requireInstanceOf(42, String.class, "must be String"));
        assertEquals("must be String", ex.getMessage());
    }

    @Test
    void requireInstanceOf_null_throwsIllegalArgument() {
        // null is not an instance of any class
        assertThrows(IllegalArgumentException.class,
            () -> ObjectUtil.requireInstanceOf(null, String.class));
    }

    // ---------------------------------------------------------------------------
    // castIfAssignmentCompatible
    // ---------------------------------------------------------------------------

    @Test
    void castIfAssignmentCompatible_null_returnsNull() {
        assertNull(ObjectUtil.castIfAssignmentCompatible(null, String.class));
    }

    @Test
    void castIfAssignmentCompatible_matching_returnsCast() {
        Object obj = "hello";
        String result = ObjectUtil.castIfAssignmentCompatible(obj, String.class);
        assertEquals("hello", result);
    }

    @Test
    void castIfAssignmentCompatible_nonMatching_returnsNull() {
        assertNull(ObjectUtil.castIfAssignmentCompatible(42, String.class));
    }

    // ---------------------------------------------------------------------------
    // getCombinedHash
    // ---------------------------------------------------------------------------

    @Test
    void getCombinedHash_isConsistent() {
        int h1 = ObjectUtil.getCombinedHash(1, "a");
        int h2 = ObjectUtil.getCombinedHash(1, "a");
        assertEquals(h1, h2);
    }

    @Test
    void getCombinedHash_nullObject_handlesGracefully() {
        // null object should not throw
        assertDoesNotThrow(() -> ObjectUtil.getCombinedHash(0, null));
    }

    @Test
    void getCombinedHash_differsByObject() {
        int h1 = ObjectUtil.getCombinedHash(1, "a");
        int h2 = ObjectUtil.getCombinedHash(1, "b");
        assertNotEquals(h1, h2);
    }

    // ---------------------------------------------------------------------------
    // toStringOrNull
    // ---------------------------------------------------------------------------

    @Test
    void toStringOrNull_null_returnsNull() {
        assertNull(ObjectUtil.toStringOrNull(null));
    }

    @Test
    void toStringOrNull_value_returnsString() {
        assertEquals("99", ObjectUtil.toStringOrNull(99));
    }

    // ---------------------------------------------------------------------------
    // toStringArray / safeToStringArray
    // ---------------------------------------------------------------------------

    @Test
    void toStringArray_empty_returnsEmpty() {
        assertEquals(0, ObjectUtil.toStringArray(List.of()).length);
    }

    @Test
    void toStringArray_values_convertsAll() {
        String[] arr = ObjectUtil.toStringArray(List.of(1, 2, 3));
        assertArrayEquals(new String[]{"1", "2", "3"}, arr);
    }

    @Test
    void safeToStringArray_throwingToString_doesNotPropagate() {
        Object bad = new Object() {
            @Override public String toString() { throw new RuntimeException("bad"); }
        };
        // should not throw
        assertDoesNotThrow(() -> ObjectUtil.safeToStringArray(List.of(bad)));
    }

    // ---------------------------------------------------------------------------
    // getMatchedOrDefault
    // ---------------------------------------------------------------------------

    @Test
    void getMatchedOrDefault_matches_returnsValue() {
        String result = ObjectUtil.getMatchedOrDefault("hello", s -> s.startsWith("h"), () -> "default");
        assertEquals("hello", result);
    }

    @Test
    void getMatchedOrDefault_noMatch_returnsDefault() {
        String result = ObjectUtil.getMatchedOrDefault("world", s -> s.startsWith("h"), () -> "default");
        assertEquals("default", result);
    }

}
