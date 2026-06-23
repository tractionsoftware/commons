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

import java.util.concurrent.atomic.AtomicLong;

import static org.junit.jupiter.api.Assertions.*;

public final class AccumulatesCountTest {

    @Test
    void startingFromCurrentCount_currentCountIsZero_returnsSameInstance() {
        AccumulatesCount counter = () -> 0;
        assertSame(counter, counter.startingFromCurrentCount());
    }

    @Test
    void startingFromCurrentCount_currentCountIsNonZero_returnsDifferentInstance() {
        AccumulatesCount counter = () -> 5;
        assertNotSame(counter, counter.startingFromCurrentCount());
    }

    @Test
    void startingFromCurrentCount_currentCountIsNonZero_tracksDeltaSinceSnapshot() {
        AtomicLong count = new AtomicLong(10);
        AccumulatesCount counter = count::get;

        AccumulatesCount delta = counter.startingFromCurrentCount();
        assertEquals(0, delta.getCount());

        count.addAndGet(7);
        assertEquals(7, delta.getCount());

        count.addAndGet(3);
        assertEquals(10, delta.getCount());
    }

    @Test
    void startingFromCurrentCount_calledAgainOnDelta_isRelativeToLatestSnapshot() {
        AtomicLong count = new AtomicLong(10);
        AccumulatesCount counter = count::get;

        AccumulatesCount firstDelta = counter.startingFromCurrentCount();
        count.addAndGet(7);
        assertEquals(7, firstDelta.getCount());

        // Taking a second snapshot from the (non-zero) first delta should track further movement
        // relative to wherever the first delta stood at that moment, not relative to the original counter.
        AccumulatesCount secondDelta = firstDelta.startingFromCurrentCount();
        count.addAndGet(4);
        assertEquals(11, firstDelta.getCount());
        assertEquals(4, secondDelta.getCount());
    }

}
