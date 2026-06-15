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

import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

public final class OneTimeRunnableTest {

    @Test
    void run_executesTask() {
        AtomicInteger counter = new AtomicInteger(0);
        OneTimeRunnable otr = OneTimeRunnable.getInstance(counter::incrementAndGet);
        otr.run();
        assertEquals(1, counter.get());
    }

    @Test
    void run_secondInvocation_throwsIllegalState() {
        AtomicInteger counter = new AtomicInteger(0);
        OneTimeRunnable otr = OneTimeRunnable.getInstance(counter::incrementAndGet);
        otr.run();
        assertThrows(IllegalStateException.class, otr::run);
    }

    @Test
    void run_secondInvocation_doesNotRunTaskAgain() {
        AtomicInteger counter = new AtomicInteger(0);
        OneTimeRunnable otr = OneTimeRunnable.getInstance(counter::incrementAndGet);
        otr.run();
        try {
            otr.run();
        } catch (IllegalStateException e) {
            // expected
        }
        assertEquals(1, counter.get());
    }

    @Test
    void getInstance_nullTask_throwsNPE() {
        assertThrows(NullPointerException.class, () -> OneTimeRunnable.getInstance(null));
    }

    @Test
    void run_taskThrowsRuntimeException_propagates() {
        OneTimeRunnable otr = OneTimeRunnable.getInstance(() -> { throw new RuntimeException("test error"); });
        assertThrows(RuntimeException.class, otr::run);
    }

    @Test
    void run_afterTaskException_subsequentRunStillThrowsIllegalState() {
        // The first run() set hasRun=true before executing; if task throws, it's still "run"
        AtomicInteger counter = new AtomicInteger(0);
        OneTimeRunnable otr = OneTimeRunnable.getInstance(() -> {
            counter.incrementAndGet();
            throw new RuntimeException("fail");
        });
        try {
            otr.run();
        } catch (RuntimeException e) {
            // expected
        }
        assertThrows(IllegalStateException.class, otr::run);
    }

    @Test
    void subclass_runImpl_isCalledOnce() {
        AtomicInteger counter = new AtomicInteger(0);
        OneTimeRunnable otr = new OneTimeRunnable() {
            @Override
            protected void runImpl() {
                counter.incrementAndGet();
            }
        };
        otr.run();
        assertEquals(1, counter.get());
        assertThrows(IllegalStateException.class, otr::run);
    }

}
