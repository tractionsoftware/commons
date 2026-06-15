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

package com.tractionsoftware.commons.util;

import org.junit.jupiter.api.Test;

import java.util.Date;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

public final class ReloadingSupplierTest {

    /**
     * A simple concrete ReloadingSupplier for testing: tracks a "resource" via an AtomicReference
     * and simulates modification by bumping a lastModified date.
     */
    private static final class StringReloadingSupplier extends ReloadingSupplier<String> {

        private final AtomicReference<String> resource;
        private final AtomicReference<Date> lastModified;
        private final AtomicInteger reloadCount = new AtomicInteger(0);

        StringReloadingSupplier(String initial, Date initialLastModified) {
            super(null, new Date(0)); // start with epoch so first call triggers reload
            this.resource = new AtomicReference<>(initial);
            this.lastModified = new AtomicReference<>(initialLastModified);
        }

        void setResource(String value, Date newLastModified) {
            resource.set(value);
            lastModified.set(newLastModified);
        }

        @Override
        public String toDebugString() {
            return "StringReloadingSupplier{" + resource.get() + "}";
        }

        @Override
        protected Date getResourceLastModifiedDate() {
            return lastModified.get();
        }

        @Override
        protected String reload() {
            reloadCount.incrementAndGet();
            return resource.get();
        }

    }

    @Test
    void get_firstCall_loadsResource() {
        Date modTime = new Date(1000L);
        StringReloadingSupplier supplier = new StringReloadingSupplier("hello", modTime);
        assertEquals("hello", supplier.get());
        assertEquals(1, supplier.reloadCount.get());
    }

    @Test
    void get_secondCall_noReload_whenNotChanged() {
        Date modTime = new Date(1000L);
        StringReloadingSupplier supplier = new StringReloadingSupplier("hello", modTime);
        supplier.get(); // first call triggers reload
        supplier.get(); // second call — lastLoadTime now matches resourceLastModified
        assertEquals(1, supplier.reloadCount.get());
    }

    @Test
    void get_afterChange_reloadsNewValue() {
        Date modTime1 = new Date(1000L);
        StringReloadingSupplier supplier = new StringReloadingSupplier("v1", modTime1);
        assertEquals("v1", supplier.get()); // loads v1

        Date modTime2 = new Date(2000L);
        supplier.setResource("v2", modTime2);
        assertEquals("v2", supplier.get()); // detects change → reloads
        assertEquals(2, supplier.reloadCount.get());
    }

    @Test
    void get_noChangeAfterReload_stillCaches() {
        Date modTime = new Date(5000L);
        StringReloadingSupplier supplier = new StringReloadingSupplier("data", modTime);
        supplier.get(); // loads
        supplier.get();
        supplier.get();
        assertEquals(1, supplier.reloadCount.get()); // reloaded only once
    }

    @Test
    void toDebugString_returnsNonNull() {
        StringReloadingSupplier supplier = new StringReloadingSupplier("x", new Date(1L));
        assertNotNull(supplier.toDebugString());
    }

}
