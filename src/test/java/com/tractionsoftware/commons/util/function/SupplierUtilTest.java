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

package com.tractionsoftware.commons.util.function;

import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

public final class SupplierUtilTest {

    // =====================================================================
    // forConstantValue
    // =====================================================================

    @Test
    void forConstantValue_alwaysReturnsSameValue() {
        var supplier = SupplierUtil.forConstantValue("hello");
        assertEquals("hello", supplier.get());
        assertEquals("hello", supplier.get());
    }

    @Test
    void forConstantValue_null_returnsNull() {
        var supplier = SupplierUtil.<String>forConstantValue(null);
        assertNull(supplier.get());
    }

    @Test
    void forConstantValue_isCacheable() {
        assertTrue(SupplierUtil.forConstantValue("x").resultsAreCacheable());
    }

    @Test
    void forConstantValue_toString_containsValue() {
        String s = SupplierUtil.forConstantValue("myvalue").toString();
        assertTrue(s.contains("myvalue"), s);
    }

    // =====================================================================
    // nullValueSupplier
    // =====================================================================

    @Test
    void nullValueSupplier_returnsNull() {
        assertNull(SupplierUtil.nullValueSupplier().get());
    }

    @Test
    void nullValueSupplier_isCacheable() {
        assertTrue(SupplierUtil.nullValueSupplier().resultsAreCacheable());
    }

    // =====================================================================
    // safeGet
    // =====================================================================

    @Test
    void safeGet_normalSupplier_returnsValue() {
        assertEquals("result", SupplierUtil.safeGet(() -> "result"));
    }

    @Test
    void safeGet_throwingSupplier_returnsNull() {
        assertNull(SupplierUtil.safeGet(() -> { throw new RuntimeException("fail"); }));
    }

    @Test
    void safeGet_throwingSupplier_returnsDefaultValue() {
        assertEquals("fallback", SupplierUtil.safeGet(
            () -> { throw new RuntimeException("fail"); }, "fallback"
        ));
    }

    @Test
    void safeGet_normalSupplier_withDefault_returnsValue() {
        assertEquals("real", SupplierUtil.safeGet(() -> "real", "default"));
    }

    @Test
    void safeGet_nullSupplier_throwsNPE() {
        assertEquals((Object) null, SupplierUtil.safeGet(null));
    }

    @Test
    void safeGet_calledOnlyOnce() {
        AtomicInteger count = new AtomicInteger(0);
        SupplierUtil.safeGet(() -> { count.incrementAndGet(); return "x"; });
        assertEquals(1, count.get());
    }

}
