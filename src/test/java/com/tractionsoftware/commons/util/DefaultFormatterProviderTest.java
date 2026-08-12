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

import java.util.Formatter;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.*;

public final class DefaultFormatterProviderTest {

    @Test
    void getInstance_nullAppendable_throws() {
        assertThrows(NullPointerException.class, () -> DefaultFormatterProvider.getInstance(null));
    }

    @Test
    void getInstance_nonNull_returnsInstance() {
        assertNotNull(DefaultFormatterProvider.getInstance(new StringBuilder()));
    }

    @Test
    void getDefault_returnsNonNull() {
        DefaultFormatterProvider p = DefaultFormatterProvider.getInstance(new StringBuilder());
        assertNotNull(p.getDefault());
    }

    @Test
    void getDefault_formatsToAppendable() {
        StringBuilder sb = new StringBuilder();
        DefaultFormatterProvider p = DefaultFormatterProvider.getInstance(sb);
        p.getDefault().format("%s", "hello");
        assertEquals("hello", sb.toString());
    }

    @Test
    void getDefault_sameLazyFormatter_cachedOnSameLocale() {
        DefaultFormatterProvider p = DefaultFormatterProvider.getInstance(new StringBuilder());
        Formatter f1 = p.getDefault();
        Formatter f2 = p.getDefault();
        // Same Locale → same formatter instance returned
        assertSame(f1, f2);
    }

    @Test
    void get_withLocale_returnsNonNull() {
        DefaultFormatterProvider p = DefaultFormatterProvider.getInstance(new StringBuilder());
        assertNotNull(p.get(Locale.US));
    }

    @Test
    void get_specificLocale_formatsWithThatLocale() {
        StringBuilder sb = new StringBuilder();
        DefaultFormatterProvider p = DefaultFormatterProvider.getInstance(sb);
        p.get(Locale.US).format("%.2f", 1.5);
        assertTrue(sb.toString().contains("1.50") || sb.toString().contains("1,50"),
                   "Expected locale-formatted float: " + sb);
    }

    @Test
    void get_differentLocale_createsNewFormatter() {
        DefaultFormatterProvider p = DefaultFormatterProvider.getInstance(new StringBuilder());
        Formatter f1 = p.get(Locale.US);
        Formatter f2 = p.get(Locale.GERMAN);
        assertNotSame(f1, f2);
    }

    @Test
    void get_sameLocale_returnsCachedFormatter() {
        DefaultFormatterProvider p = DefaultFormatterProvider.getInstance(new StringBuilder());
        Formatter f1 = p.get(Locale.US);
        Formatter f2 = p.get(Locale.US);
        assertSame(f1, f2);
    }

}
