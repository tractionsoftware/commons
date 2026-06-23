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

import static org.junit.jupiter.api.Assertions.*;

public final class MayHaveKnownSizeTest {

    private static MayHaveKnownSize withSize(long size) {
        return () -> size;
    }

    // ---------------------------------------------------------------------------
    // unknown size (Long.MIN_VALUE sentinel)
    // ---------------------------------------------------------------------------

    @Test
    void unknownSize_hasSizeAtLeast_returnsFalse() {
        assertFalse(withSize(Long.MIN_VALUE).hasSizeAtLeast(0));
    }

    @Test
    void unknownSize_hasSizeGreaterThan_returnsFalse() {
        assertFalse(withSize(Long.MIN_VALUE).hasSizeGreaterThan(Long.MIN_VALUE));
    }

    @Test
    void unknownSize_hasSizeAtMost_returnsFalse() {
        assertFalse(withSize(Long.MIN_VALUE).hasSizeAtMost(Long.MAX_VALUE));
    }

    @Test
    void unknownSize_hasSizeLessThan_returnsFalse() {
        assertFalse(withSize(Long.MIN_VALUE).hasSizeLessThan(Long.MAX_VALUE));
    }

    @Test
    void unknownSize_hasSizeBetween_returnsFalse() {
        assertFalse(withSize(Long.MIN_VALUE).hasSizeBetween(Long.MIN_VALUE, Long.MAX_VALUE));
    }

    // ---------------------------------------------------------------------------
    // hasSizeAtLeast
    // ---------------------------------------------------------------------------

    @Test
    void hasSizeAtLeast_sizeEqualsLowerBound_returnsTrue() {
        assertTrue(withSize(10).hasSizeAtLeast(10));
    }

    @Test
    void hasSizeAtLeast_sizeAboveLowerBound_returnsTrue() {
        assertTrue(withSize(11).hasSizeAtLeast(10));
    }

    @Test
    void hasSizeAtLeast_sizeBelowLowerBound_returnsFalse() {
        assertFalse(withSize(9).hasSizeAtLeast(10));
    }

    // ---------------------------------------------------------------------------
    // hasSizeGreaterThan
    // ---------------------------------------------------------------------------

    @Test
    void hasSizeGreaterThan_sizeAboveCompare_returnsTrue() {
        assertTrue(withSize(11).hasSizeGreaterThan(10));
    }

    @Test
    void hasSizeGreaterThan_sizeEqualsCompare_returnsFalse() {
        assertFalse(withSize(10).hasSizeGreaterThan(10));
    }

    @Test
    void hasSizeGreaterThan_sizeBelowCompare_returnsFalse() {
        assertFalse(withSize(9).hasSizeGreaterThan(10));
    }

    // ---------------------------------------------------------------------------
    // hasSizeAtMost
    // ---------------------------------------------------------------------------

    @Test
    void hasSizeAtMost_sizeEqualsUpperBound_returnsTrue() {
        assertTrue(withSize(10).hasSizeAtMost(10));
    }

    @Test
    void hasSizeAtMost_sizeBelowUpperBound_returnsTrue() {
        assertTrue(withSize(9).hasSizeAtMost(10));
    }

    @Test
    void hasSizeAtMost_sizeAboveUpperBound_returnsFalse() {
        assertFalse(withSize(11).hasSizeAtMost(10));
    }

    // ---------------------------------------------------------------------------
    // hasSizeLessThan
    // ---------------------------------------------------------------------------

    @Test
    void hasSizeLessThan_sizeBelowCompare_returnsTrue() {
        assertTrue(withSize(9).hasSizeLessThan(10));
    }

    @Test
    void hasSizeLessThan_sizeEqualsCompare_returnsFalse() {
        assertFalse(withSize(10).hasSizeLessThan(10));
    }

    @Test
    void hasSizeLessThan_sizeAboveCompare_returnsFalse() {
        assertFalse(withSize(11).hasSizeLessThan(10));
    }

    // ---------------------------------------------------------------------------
    // hasSizeBetween
    // ---------------------------------------------------------------------------

    @Test
    void hasSizeBetween_sizeAtLowerBound_returnsTrue() {
        assertTrue(withSize(1).hasSizeBetween(1, 10));
    }

    @Test
    void hasSizeBetween_sizeAtUpperBound_returnsTrue() {
        assertTrue(withSize(10).hasSizeBetween(1, 10));
    }

    @Test
    void hasSizeBetween_sizeInsideBounds_returnsTrue() {
        assertTrue(withSize(5).hasSizeBetween(1, 10));
    }

    @Test
    void hasSizeBetween_sizeBelowLowerBound_returnsFalse() {
        assertFalse(withSize(0).hasSizeBetween(1, 10));
    }

    @Test
    void hasSizeBetween_sizeAboveUpperBound_returnsFalse() {
        assertFalse(withSize(11).hasSizeBetween(1, 10));
    }

}
