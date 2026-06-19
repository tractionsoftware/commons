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

import com.google.common.annotations.Beta;
import com.tractionsoftware.commons.io.IOUtil;

import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;

/**
 * Utilities related to {@link Resource}s.
 *
 * @author Dave Shepperton
 */
public final class ResourceUtil {

    private ResourceUtil() {
    }

    public static final Resource NO_OP_RESOURCE = new SimpleResource(null);

    /**
     * A simple implementation of {@link Resource} backed by an {@link AutoCloseable}, and which tracks its open/closed
     * status so that it will only attempt to close the wrapped resource once (even if that attempt fails).
     *
     * <p>
     * This implementation is thread safe, although if it is to be shared across threads, the wrapped resource should be
     * eligible for such sharing.
     *
     * @author Dave Shepperton
     */
    @Beta
    private static final class SimpleResource implements Resource {

        private final AutoCloseable wrapped;

        private boolean closed = false;

        private SimpleResource(AutoCloseable wrapped) {
            this.wrapped = wrapped;
        }

        /**
         * This implementation closes the {@link AutoCloseable} instance provided at construction via
         * {@link IOUtil#close(AutoCloseable)}.
         */
        @Override
        public synchronized void close() {
            if (!closed) {
                try {
                    IOUtil.close(wrapped);
                }
                finally {
                    closed = true;
                }
            }
        }

        @Override
        public synchronized boolean isOpen() {
            return !closed;
        }

    }

    private static final class RunnableOnClose implements Resource {

        private Runnable onClose;

        private RunnableOnClose(Runnable onClose) {
            Objects.requireNonNull(onClose, "on-close task");
            this.onClose = onClose;
        }

        @Override
        public synchronized void close() {
            if (onClose != null) {
                try {
                    onClose.run();
                }
                finally {
                    onClose = null;
                }
            }
        }

        @Override
        public synchronized boolean isOpen() {
            return (onClose != null);
        }

    }

    /**
     * A {@link Resource.LoadAttemptResult} implementation representing a failed attempt to set up and open a resource.
     *
     * @author Dave Shepperton
     */
    @Beta
    private static final class FailedLoadAttempt implements Resource.LoadAttemptResult {

        private final Resource.LoadErrorType errorType;

        private final RuntimeException fatal;

        public FailedLoadAttempt(Resource.LoadErrorType errorType, RuntimeException fatal) {
            Objects.requireNonNull(errorType, "LoadErrorType");
            Objects.requireNonNull(fatal, "fatal RuntimeException");
            this.errorType = errorType;
            this.fatal = fatal;
        }

        /**
         * This implementation always returns null.
         */
        @Override
        public Resource getResource() {
            return null;
        }

        @Override
        public Resource.LoadErrorType getErrorType() {
            return errorType;
        }

        /**
         * This implementation always returns the fatal {@link RuntimeException} provided at construction.
         */
        @Override
        public RuntimeException getFatalException() {
            return fatal;
        }

    }

    /**
     * A {@link Resource.LoadAttemptResult} implementation representing a successful attempt to set up and open a
     * resource.
     *
     * @author Dave Shepperton
     */
    @Beta
    private static final class SuccessfulLoadAttempt implements Resource.LoadAttemptResult {

        private final Resource resource;

        public SuccessfulLoadAttempt(Resource resource) {
            Objects.requireNonNull(resource, "Resource");
            this.resource = resource;
        }

        /**
         * This implementation always returns the wrapped {@link Resource}.
         */
        @Override
        public Resource getResource() {
            return resource;
        }

        /**
         * This implementation always returns null.
         */
        @Override
        public Resource.LoadErrorType getErrorType() {
            return null;
        }

        /**
         * This implementation always returns null.
         */
        @Override
        public RuntimeException getFatalException() {
            return null;
        }

    }

    private static final class RunnableAutoCloseableAdapter implements AutoCloseable {

        private final Runnable onClose;

        private RunnableAutoCloseableAdapter(Runnable onClose) {
            this.onClose = onClose;
        }

        @Override
        public void close() {
            onClose.run();
        }

    }

    public static AutoCloseable createAutoCloseableAdapter(Runnable onClose) {
        return new RunnableAutoCloseableAdapter(onClose);
    }

    public static Resource createResource(AutoCloseable loaded) {
        return new SimpleResource(loaded);
    }

    public static Resource createResourceWithTaskOnClose(Runnable onClose) {
        return new RunnableOnClose(onClose);
    }

    public static Resource tryToAcquireLock(Lock lock, long time, TimeUnit unit) throws InterruptedException {
        boolean locked = lock.tryLock(time, unit);
        if (locked) {
            return new RunnableOnClose(lock::unlock);
        }
        throw new InterruptedException("Failed to acquire lock.");
    }

    public static Resource.LoadAttemptResult createNoOpSuccessfulResult() {
        return new SuccessfulLoadAttempt(NO_OP_RESOURCE);
    }

    public static Resource.LoadAttemptResult createSuccessfulResult(AutoCloseable loaded) {
        return new SuccessfulLoadAttempt(new SimpleResource(loaded));
    }

    public static Resource.LoadAttemptResult createSuccessfulResult(Iterable<? extends AutoCloseable> loaded) {
        return new SuccessfulLoadAttempt(new SimpleResource(IOUtil.createCompoundCloseable(loaded)));
    }

    public static Resource.LoadAttemptResult createFailedResult(Resource.LoadErrorType errorType, RuntimeException error) {
        return new FailedLoadAttempt(errorType, error);
    }

}
