// PLEASE DO NOT DELETE THIS LINE - make copyright depends on it.
package com.tractionsoftware.commons.util;

import org.junit.jupiter.api.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

class OneTimeRunnableTest {

    @Test
    void getInstance_nullTask_throws() {
        assertThrows(NullPointerException.class, () -> OneTimeRunnable.getInstance(null));
    }

    @Test
    void run_executesTask() {
        AtomicInteger count = new AtomicInteger();
        OneTimeRunnable otr = OneTimeRunnable.getInstance(count::incrementAndGet);
        otr.run();
        assertEquals(1, count.get());
    }

    @Test
    void run_secondCall_throwsIllegalState() {
        OneTimeRunnable otr = OneTimeRunnable.getInstance(() -> {});
        otr.run();
        assertThrows(IllegalStateException.class, otr::run);
    }

    @Test
    void run_thirdCall_throwsIllegalState() {
        AtomicInteger count = new AtomicInteger();
        OneTimeRunnable otr = OneTimeRunnable.getInstance(count::incrementAndGet);
        otr.run();
        assertThrows(IllegalStateException.class, otr::run);
        assertThrows(IllegalStateException.class, otr::run);
        assertEquals(1, count.get());
    }

    @Test
    void run_concurrentCalls_onlyOneSucceeds() throws InterruptedException {
        AtomicInteger count = new AtomicInteger();
        OneTimeRunnable otr = OneTimeRunnable.getInstance(count::incrementAndGet);

        CountDownLatch start = new CountDownLatch(1);
        AtomicInteger successCount = new AtomicInteger();
        AtomicInteger failCount = new AtomicInteger();

        Runnable runner = () -> {
            try {
                start.await();
                otr.run();
                successCount.incrementAndGet();
            }
            catch (IllegalStateException e) {
                failCount.incrementAndGet();
            }
            catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        };

        Thread t1 = new Thread(runner);
        Thread t2 = new Thread(runner);
        t1.start();
        t2.start();
        start.countDown();
        t1.join(2000);
        t2.join(2000);

        assertEquals(1, successCount.get());
        assertEquals(1, failCount.get());
        assertEquals(1, count.get());
    }

    @Test
    void subclass_runImpl_called() {
        AtomicInteger count = new AtomicInteger();
        OneTimeRunnable otr = new OneTimeRunnable() {
            @Override
            protected void runImpl() {
                count.incrementAndGet();
            }
        };
        otr.run();
        assertEquals(1, count.get());
    }

}
