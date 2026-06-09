// PLEASE DO NOT DELETE THIS LINE - make copyright depends on it.
package com.tractionsoftware.commons.util.function;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SupplierUtilTest {

    // ---------------------------------------------------------------------------
    // forConstantValue
    // ---------------------------------------------------------------------------

    @Test
    void forConstantValue_returnsValue() {
        assertEquals("hello", SupplierUtil.forConstantValue("hello").get());
    }

    @Test
    void forConstantValue_nullValue_returnsNull() {
        assertNull(SupplierUtil.forConstantValue(null).get());
    }

    @Test
    void forConstantValue_resultsAreCacheable() {
        assertTrue(SupplierUtil.forConstantValue("x").resultsAreCacheable());
    }

    @Test
    void forConstantValue_toString_containsValue() {
        String str = SupplierUtil.forConstantValue("abc").toString();
        assertTrue(str.contains("abc"), "toString should mention the value: " + str);
    }

    // ---------------------------------------------------------------------------
    // nullValueSupplier
    // ---------------------------------------------------------------------------

    @Test
    void nullValueSupplier_returnsNull() {
        assertNull(SupplierUtil.nullValueSupplier().get());
    }

    @Test
    void nullValueSupplier_isCacheable() {
        assertTrue(SupplierUtil.nullValueSupplier().resultsAreCacheable());
    }

    // ---------------------------------------------------------------------------
    // safeGet
    // ---------------------------------------------------------------------------

    @Test
    void safeGet_success_returnsValue() {
        assertEquals("value", SupplierUtil.safeGet(() -> "value"));
    }

    @Test
    void safeGet_throws_returnsNull() {
        assertNull(SupplierUtil.safeGet(() -> { throw new RuntimeException("boom"); }));
    }

    @Test
    void safeGet_withDefault_success_returnsValue() {
        assertEquals("x", SupplierUtil.safeGet(() -> "x", "default"));
    }

    @Test
    void safeGet_withDefault_throws_returnsDefault() {
        assertEquals("default", SupplierUtil.safeGet(
            () -> { throw new IllegalStateException("fail"); },
            "default"
        ));
    }

    @Test
    void safeGet_noArg_nullSupplier_throws() {
        assertDoesNotThrow(() -> SupplierUtil.safeGet(null));
    }

}
