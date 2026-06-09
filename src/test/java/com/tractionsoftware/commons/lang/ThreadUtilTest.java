// PLEASE DO NOT DELETE THIS LINE - make copyright depends on it.
package com.tractionsoftware.commons.lang;

import org.junit.jupiter.api.Test;

import java.util.Comparator;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.*;

class ThreadUtilTest {

    // ---------------------------------------------------------------------------
    // runInSeparateThreadWaitingForCompletion
    // ---------------------------------------------------------------------------

    @Test
    void runInSeparateThreadWaitingForCompletion_runsTask() {
        AtomicBoolean ran = new AtomicBoolean(false);
        boolean finished = ThreadUtil.runInSeparateThreadWaitingForCompletion(() -> ran.set(true));
        assertTrue(finished);
        assertTrue(ran.get());
    }

    @Test
    void runInSeparateThreadWaitingForCompletion_withTimeout_completesInTime() {
        AtomicBoolean ran = new AtomicBoolean(false);
        boolean finished = ThreadUtil.runInSeparateThreadWaitingForCompletion(
            () -> ran.set(true), 2, TimeUnit.SECONDS
        );
        assertTrue(finished);
        assertTrue(ran.get());
    }

    @Test
    void runInSeparateThreadWaitingForCompletion_withTimeout_timeoutReturnsFalse() {
        // Task that never completes → timeout fires
        boolean finished = ThreadUtil.runInSeparateThreadWaitingForCompletion(
            () -> {
                try { Thread.sleep(30_000); } catch (InterruptedException ignored) {}
            },
            50, TimeUnit.MILLISECONDS
        );
        assertFalse(finished);
    }

    // ---------------------------------------------------------------------------
    // runInSeparateThread
    // ---------------------------------------------------------------------------

    @Test
    void runInSeparateThread_returnsStartedThread() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        Thread t = ThreadUtil.runInSeparateThread(latch::countDown);
        assertTrue(latch.await(2, TimeUnit.SECONDS));
        assertNotNull(t);
    }

    @Test
    void runInSeparateThread_withName_setsName() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        Thread t = ThreadUtil.runInSeparateThread(latch::countDown, "test-thread");
        latch.await(2, TimeUnit.SECONDS);
        assertEquals("test-thread", t.getName());
    }

    @Test
    void runInSeparateThread_withLatch_countsDown() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        ThreadUtil.runInSeparateThread(() -> {}, latch);
        assertTrue(latch.await(2, TimeUnit.SECONDS));
    }

    // ---------------------------------------------------------------------------
    // createThread
    // ---------------------------------------------------------------------------

    @Test
    void createThread_withName_notYetStarted() {
        AtomicBoolean ran = new AtomicBoolean(false);
        Thread t = ThreadUtil.createThread(() -> ran.set(true), "my-thread");
        assertFalse(ran.get(), "task should not have run yet");
        assertEquals("my-thread", t.getName());
        // thread is alive only after start
        assertFalse(t.isAlive());
    }

    @Test
    void createThread_nullName_stillCreated() {
        Thread t = ThreadUtil.createThread(() -> {}, null);
        assertNotNull(t);
    }

    // ---------------------------------------------------------------------------
    // tryToSleep
    // ---------------------------------------------------------------------------

    @Test
    void tryToSleep_returnsNotInterrupted() {
        boolean interrupted = ThreadUtil.tryToSleep(1);
        assertFalse(interrupted);
    }

    // ---------------------------------------------------------------------------
    // tryToSetCurrentThreadName / tryToSetThreadName
    // ---------------------------------------------------------------------------

    @Test
    void tryToSetCurrentThreadName_setsName() {
        String original = Thread.currentThread().getName();
        try {
            ThreadUtil.tryToSetCurrentThreadName("test-name-xyz");
            assertEquals("test-name-xyz", Thread.currentThread().getName());
        }
        finally {
            Thread.currentThread().setName(original);
        }
    }

    // ---------------------------------------------------------------------------
    // threadIdOrder
    // ---------------------------------------------------------------------------

    @Test
    void threadIdOrder_nullsFirst() {
        Comparator<Thread> comp = ThreadUtil.threadIdOrder();
        // null < any thread
        assertTrue(comp.compare(null, Thread.currentThread()) < 0);
        assertTrue(comp.compare(Thread.currentThread(), null) > 0);
    }

    @Test
    void threadIdOrder_ordersById() throws InterruptedException {
        Thread[] threads = new Thread[3];
        CountDownLatch started = new CountDownLatch(3);
        CountDownLatch done = new CountDownLatch(1);
        for (int i = 0; i < 3; i++) {
            threads[i] = new Thread(() -> {
                started.countDown();
                try { done.await(); } catch (InterruptedException ignored) {}
            });
            threads[i].start();
        }
        started.await(2, TimeUnit.SECONDS);

        Comparator<Thread> comp = ThreadUtil.threadIdOrder();
        assertTrue(comp.compare(threads[0], threads[1]) < 0 || comp.compare(threads[0], threads[1]) > 0
                   || comp.compare(threads[0], threads[1]) == 0);
        // More precisely: ids should be ordered
        long id0 = threads[0].threadId();
        long id1 = threads[1].threadId();
        int expected = Long.compare(id0, id1);
        assertEquals(expected, comp.compare(threads[0], threads[1]));

        done.countDown();
        for (Thread t : threads) t.join(1000);
    }

}
