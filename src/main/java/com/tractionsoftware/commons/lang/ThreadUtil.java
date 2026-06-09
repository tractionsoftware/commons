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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Comparator;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * Some utility methods related to {@link Thread}s.
 *
 * @author Dave Shepperton
 */
public final class ThreadUtil {

    private ThreadUtil() {
    }

    private static final Logger LOGGER = LoggerFactory.getLogger(ThreadUtil.class);

    private static final class CountDownLatchRunnable implements Runnable {

        private final CountDownLatch latch;

        private CountDownLatchRunnable(CountDownLatch latch) {
            this.latch = latch;
        }

        @Override
        public final void run() {
            latch.countDown();
        }

    }

    public static final boolean runInSeparateThreadWaitingForCompletion(Runnable task) {
        return runInSeparateThreadWaitingForCompletion(task, -1, null);
    }

    public static final boolean runInSeparateThreadWaitingForCompletion(Runnable task, int timeOut, TimeUnit timeOutUnit) {

        CountDownLatch taskThreadComplete = new CountDownLatch(1);
        Thread thread = runInSeparateThread(task, taskThreadComplete);

        try {
            if (timeOut == -1) {
                taskThreadComplete.await();
                return true;
            }
            boolean finished = taskThreadComplete.await(timeOut, timeOutUnit);
            if (finished) {
                return true;
            }
            thread.interrupt();
        }
        catch (InterruptedException e) {
            LOGGER.warn("Thread interrupted while waiting for synchronous request completion.", e);
        }
        return false;

    }

    public static final Thread runInSeparateThread(Runnable task) {
        return runInSeparateThread(task, (String) null);
    }

    public static final Thread runInSeparateThread(Runnable task, String name) {
        Thread thread = createThread(task, name);
        thread.start();
        return thread;
    }

    public static final Thread createThread(Runnable task, String name) {
        Thread t = new Thread(task);
        if (name != null) {
            tryToSetThreadName(t, name);
        }
        return t;
    }

    public static final Thread runInSeparateThread(Runnable task, CountDownLatch onCompletion) {
        return runInSeparateThread(task, onCompletion, null);
    }

    public static final Thread runInSeparateThread(Runnable task, CountDownLatch onCompletion, String name) {
        return runInSeparateThread(getRunnableWithCountDownOnCompletion(task, onCompletion), name);
    }

    private static final Runnable getRunnableWithCountDownOnCompletion(Runnable task, CountDownLatch taskThreadComplete) {
        return getRunnableAndRunnableOnFinally(task, new CountDownLatchRunnable(taskThreadComplete));
    }

    private static final Runnable getRunnableAndRunnableOnFinally(final Runnable task, final Runnable onCompletionTask) {
        return () -> {
            try {
                task.run();
            }
            finally {
                onCompletionTask.run();
            }
        };
    }

    /**
     * Tries to {@link Thread#sleep(long) make the current sleep} for the requested amount of time (in milliseconds).
     *
     * <p>
     * An {@link InterruptedException} raised if
     * {@link Thread#interrupt() another Thread interrupts the current Thread}.
     *
     * @param millis
     *     the number of milliseconds to try to sleep.
     * @return the value of {@link Thread#isInterrupted()} for the current {@link Thread}.
     */
    public static final boolean tryToSleep(long millis) {
        try {
            Thread.sleep(millis);
        }
        catch (InterruptedException e) {
            LOGGER.info("Sleep interrupted", e);
        }
        return Thread.currentThread().isInterrupted();
    }

    public static final void tryToSetCurrentThreadName(String name) {
        tryToSetThreadName(Thread.currentThread(), name);
    }

    public static final void tryToSetThreadName(Thread thread, String name) {
        try {
            thread.setName(name);
        }
        catch (SecurityException e) {
            LOGGER.warn("Failed to set name of Thread {} to {}", thread, name, e);
        }
    }

    /**
     * Returns a {@link Comparator} that orders {@link Thread}s by {@link Thread#threadId() their numeric IDs}.
     *
     * @return a {@link Comparator} that orders {@link Thread}s by {@link Thread#threadId() their numeric IDs}.
     */
    public static final Comparator<Thread> threadIdOrder() {
        return Comparator.nullsFirst(Comparator.comparing(Thread::threadId));
    }

}
