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

import java.util.Arrays;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;

import static org.junit.jupiter.api.Assertions.*;

class ResourceUtilTest {

    private static final class CountingAutoCloseable implements AutoCloseable {

        private final AtomicInteger closeCount = new AtomicInteger();

        private final boolean throwOnClose;

        CountingAutoCloseable() {
            this(false);
        }

        CountingAutoCloseable(boolean throwOnClose) {
            this.throwOnClose = throwOnClose;
        }

        @Override
        public void close() throws Exception {
            closeCount.incrementAndGet();
            if (throwOnClose) {
                throw new Exception("close failed");
            }
        }

        int getCloseCount() {
            return closeCount.get();
        }

    }

    // ---------------------------------------------------------------------------
    // createAutoCloseableAdapter
    // ---------------------------------------------------------------------------

    @Test
    void createAutoCloseableAdapter_runsRunnableOnClose() throws Exception {
        AtomicInteger ran = new AtomicInteger();
        AutoCloseable adapter = ResourceUtil.createAutoCloseableAdapter(ran::incrementAndGet);
        adapter.close();
        assertEquals(1, ran.get());
    }

    @Test
    void createAutoCloseableAdapter_runsRunnableEachTimeCloseIsCalled() throws Exception {
        AtomicInteger ran = new AtomicInteger();
        AutoCloseable adapter = ResourceUtil.createAutoCloseableAdapter(ran::incrementAndGet);
        adapter.close();
        adapter.close();
        // unlike Resource, a bare AutoCloseable adapter has no idempotency guard of its own.
        assertEquals(2, ran.get());
    }

    @Test
    void createAutoCloseableAdapter_nullRunnable_throwsOnClose() {
        AutoCloseable adapter = ResourceUtil.createAutoCloseableAdapter(null);
        assertThrows(NullPointerException.class, adapter::close);
    }

    // ---------------------------------------------------------------------------
    // createResource
    // ---------------------------------------------------------------------------

    @Test
    void createResource_isOpenInitially_andClosesWrapped() throws Exception {
        CountingAutoCloseable wrapped = new CountingAutoCloseable();
        Resource resource = ResourceUtil.createResource(wrapped);
        assertTrue(resource.isOpen());
        resource.close();
        assertFalse(resource.isOpen());
        assertEquals(1, wrapped.getCloseCount());
    }

    @Test
    void createResource_close_isIdempotent() throws Exception {
        CountingAutoCloseable wrapped = new CountingAutoCloseable();
        Resource resource = ResourceUtil.createResource(wrapped);
        resource.close();
        resource.close();
        assertEquals(1, wrapped.getCloseCount());
    }

    @Test
    void createResource_nullAutoCloseable_doesNotThrow() {
        Resource resource = ResourceUtil.createResource(null);
        assertTrue(resource.isOpen());
        assertDoesNotThrow(resource::close);
        assertFalse(resource.isOpen());
    }

    @Test
    void createResource_wrappedThrowsOnClose_doesNotPropagate() throws Exception {
        CountingAutoCloseable wrapped = new CountingAutoCloseable(true);
        Resource resource = ResourceUtil.createResource(wrapped);
        assertDoesNotThrow(resource::close);
        assertEquals(1, wrapped.getCloseCount());
        assertFalse(resource.isOpen());
    }

    // ---------------------------------------------------------------------------
    // createResourceWithTaskOnClose
    // ---------------------------------------------------------------------------

    @Test
    void createResourceWithTaskOnClose_runsTaskExactlyOnceOnClose() {
        AtomicInteger ran = new AtomicInteger();
        Resource resource = ResourceUtil.createResourceWithTaskOnClose(ran::incrementAndGet);
        assertTrue(resource.isOpen());
        resource.close();
        resource.close();
        assertEquals(1, ran.get());
        assertFalse(resource.isOpen());
    }

    @Test
    void createResourceWithTaskOnClose_nullRunnable_throwsAtConstruction() {
        assertThrows(NullPointerException.class, () -> ResourceUtil.createResourceWithTaskOnClose(null));
    }

    // ---------------------------------------------------------------------------
    // tryToAcquireLock
    // ---------------------------------------------------------------------------

    private static final class FakeLock implements Lock {

        private final boolean acquire;

        private final AtomicInteger unlockCount = new AtomicInteger();

        FakeLock(boolean acquire) {
            this.acquire = acquire;
        }

        @Override
        public void lock() {
        }

        @Override
        public void lockInterruptibly() {
        }

        @Override
        public boolean tryLock() {
            return acquire;
        }

        @Override
        public boolean tryLock(long time, TimeUnit unit) {
            return acquire;
        }

        @Override
        public void unlock() {
            unlockCount.incrementAndGet();
        }

        @Override
        public java.util.concurrent.locks.Condition newCondition() {
            throw new UnsupportedOperationException();
        }

        int getUnlockCount() {
            return unlockCount.get();
        }

    }

    @Test
    void tryToAcquireLock_success_returnsResourceThatUnlocksOnClose() throws Exception {
        FakeLock lock = new FakeLock(true);
        Resource resource = ResourceUtil.tryToAcquireLock(lock, 1, TimeUnit.SECONDS);
        assertTrue(resource.isOpen());
        assertEquals(0, lock.getUnlockCount());
        resource.close();
        assertEquals(1, lock.getUnlockCount());
        assertFalse(resource.isOpen());
    }

    @Test
    void tryToAcquireLock_failure_throwsInterruptedException() {
        FakeLock lock = new FakeLock(false);
        InterruptedException thrown = assertThrows(
            InterruptedException.class,
            () -> ResourceUtil.tryToAcquireLock(lock, 1, TimeUnit.SECONDS)
        );
        assertEquals("Failed to acquire lock.", thrown.getMessage());
        assertEquals(0, lock.getUnlockCount());
    }

    // ---------------------------------------------------------------------------
    // createNoOpSuccessfulResult
    // ---------------------------------------------------------------------------

    @Test
    void createNoOpSuccessfulResult_wasSuccessful_andResourceIsNoOp() {
        Resource.LoadAttemptResult result = ResourceUtil.createNoOpSuccessfulResult();
        assertTrue(result.wasSuccessful());
        assertNull(result.getErrorType());
        assertNull(result.getFatalException());
        assertNotNull(result.getResource());
        assertSame(ResourceUtil.NO_OP_RESOURCE, result.getResource());
        assertDoesNotThrow(result.getResource()::close);
    }

    // ---------------------------------------------------------------------------
    // createSuccessfulResult(AutoCloseable)
    // ---------------------------------------------------------------------------

    @Test
    void createSuccessfulResult_singleCloseable_closesOnResourceClose() throws Exception {
        CountingAutoCloseable wrapped = new CountingAutoCloseable();
        Resource.LoadAttemptResult result = ResourceUtil.createSuccessfulResult((AutoCloseable) wrapped);
        assertTrue(result.wasSuccessful());
        Resource resource = result.getResource();
        assertNotNull(resource);
        assertTrue(resource.isOpen());
        resource.close();
        assertEquals(1, wrapped.getCloseCount());
        assertFalse(resource.isOpen());
    }

    // ---------------------------------------------------------------------------
    // createSuccessfulResult(Iterable<? extends AutoCloseable>)
    // ---------------------------------------------------------------------------

    @Test
    void createSuccessfulResult_iterableOfCloseables_closesAllOnResourceClose() throws Exception {
        CountingAutoCloseable first = new CountingAutoCloseable();
        CountingAutoCloseable second = new CountingAutoCloseable();
        Resource.LoadAttemptResult result = ResourceUtil.createSuccessfulResult(Arrays.asList(first, second));
        assertTrue(result.wasSuccessful());
        Resource resource = result.getResource();
        assertNotNull(resource);
        resource.close();
        assertEquals(1, first.getCloseCount());
        assertEquals(1, second.getCloseCount());
    }

    @Test
    void createSuccessfulResult_iterableOfCloseables_oneThrowsDoesNotPreventOthersFromClosing() throws Exception {
        CountingAutoCloseable first = new CountingAutoCloseable(true);
        CountingAutoCloseable second = new CountingAutoCloseable();
        Resource.LoadAttemptResult result = ResourceUtil.createSuccessfulResult(Arrays.asList(first, second));
        Resource resource = result.getResource();
        assertDoesNotThrow(resource::close);
        assertEquals(1, first.getCloseCount());
        assertEquals(1, second.getCloseCount());
    }

    // ---------------------------------------------------------------------------
    // createFailedResult
    // ---------------------------------------------------------------------------

    @Test
    void createFailedResult_carriesErrorTypeAndException_resourceIsNull() {
        RuntimeException error = new RuntimeException("setup failed");
        Resource.LoadAttemptResult result = ResourceUtil.createFailedResult(Resource.LoadErrorType.SETUP, error);
        assertFalse(result.wasSuccessful());
        assertEquals(Resource.LoadErrorType.SETUP, result.getErrorType());
        assertSame(error, result.getFatalException());
        assertNull(result.getResource());
    }

    @Test
    void createFailedResult_nullErrorType_throwsNPE() {
        assertThrows(NullPointerException.class, () -> ResourceUtil.createFailedResult(null, new RuntimeException("x")));
    }

    @Test
    void createFailedResult_nullException_throwsNPE() {
        assertThrows(NullPointerException.class, () -> ResourceUtil.createFailedResult(Resource.LoadErrorType.OPEN, null));
    }

}
