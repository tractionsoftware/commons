/*
 *
 *    Copyright 1996-2025 Traction Software, Inc.
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

import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * A Runnable whose {@link #run()} method can only be executed once, with subsequent invocations throwing an
 * IllegalStateException.
 *
 * @author Dave Shepperton
 */
public abstract class OneTimeRunnable implements Runnable {

    private static final class DelegatingOneTimeRunnable extends OneTimeRunnable {

        private final Runnable task;

        private DelegatingOneTimeRunnable(Runnable task) {
            this.task = task;
        }

        @Override
        protected final void runImpl() {
            task.run();
        }

    }

    public static final OneTimeRunnable getInstance(Runnable task) {
        return new DelegatingOneTimeRunnable(Objects.requireNonNull(task));
    }

    private final AtomicBoolean hasRun = new AtomicBoolean(false);

    public OneTimeRunnable() {
    }

    @Override
    public final void run() {
        if (!hasRun.compareAndSet(false, true)) {
            throw new IllegalStateException("Already run.");
        }
        runImpl();
    }

    /**
     * Runs the task after it has been established that this instance has not yet been run.
     */
    protected abstract void runImpl();

}
