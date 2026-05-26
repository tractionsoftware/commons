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

package com.tractionsoftware.commons.io;

import com.tractionsoftware.commons.lang.JavaUtil;
import com.tractionsoftware.commons.lang.Resource;
import org.apache.commons.io.function.IORunnable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;

public abstract class IOResourceTracker<C extends Closeable> implements Resource {

    static final Logger LOGGER = LoggerFactory.getLogger(IOResourceTracker.class);

    @FunctionalInterface
    public static interface Creator<C extends Closeable> {

        public C create() throws IOException;

    }

    private static final class Streams<C extends Closeable> implements Resource {

        private final List<C> list = new LinkedList<>();

        private boolean closed;

        private final String sourceIdentifier;

        public Streams(String sourceIdentifier) {
            this.sourceIdentifier = sourceIdentifier;
        }

        @Override
        public synchronized final void close() {
            if (!setClosed() || list.isEmpty()) {
                return;
            }
            for (C resource : list) {
                IOUtil.close(resource);
            }
            list.clear();
        }

        @Override
        public synchronized final boolean isOpen() {
            return !closed;
        }

        public synchronized final int size() {
            return list.size();
        }

        public synchronized final boolean isEmpty() {
            return list.isEmpty();
        }

        public synchronized final C createAndTrack(Creator<C> creator, IOResourceTracker<C> tracker)
            throws IOException {

            if (isClosed()) {
                throw new IllegalStateException("closed");
            }

            C rawResource = creator.create();

            try {
                LOGGER.debug(getClass().getName(), " created a new stream for ", sourceIdentifier, ".");
                C trackedResource = tracker.getTrackedStream(rawResource, sourceIdentifier);
                list.add(trackedResource);
                return tracker.getCloseNotifyingStream(trackedResource, () -> this.remove(trackedResource));
            }
            catch (RuntimeException e) {
                LOGGER.warn("Failed to create wrapped stream for {}", sourceIdentifier, e);
                IOUtil.close(rawResource);
                throw e;
            }

        }

        private synchronized final void remove(C trackedResource) {
            list.remove(trackedResource);
        }

        private final boolean setClosed() {
            if (closed) {
                return false;
            }
            closed = true;
            return true;
        }

    }

    private final Supplier<? extends Resource> trackerCreator;

    private final JavaUtil.CleanupTargetWrapper<Streams<C>> streams;

    public IOResourceTracker(String sourceIdentifier, Supplier<? extends Resource> trackerCreator) {
        Objects.requireNonNull(trackerCreator, "tracker creator");
        this.trackerCreator = trackerCreator;
        this.streams = JavaUtil.CleanupTargetWrapper.create(this, new Streams<>(sourceIdentifier));
    }

    @Override
    public final String toString() {
        return getClass().getName() + " (" + streams.get().size() + ")";
    }

    @Override
    public final boolean isOpen() {
        return streams.get().isOpen();
    }

    @Override
    public final void close() {
        streams.close();
    }

    public final C createTrackedStream(Creator<C> creator) throws IOException {
        return streams.get().createAndTrack(creator, this);
    }

    public final boolean hasAnyStreams() {
        if (streams.get().isEmpty()) {
            return false;
        }
        return true;
    }

    protected abstract String getStreamTypeName();

    protected abstract C getTrackedStream(C stream, String sourceIdentifier);

    protected abstract C getCloseNotifyingStream(C trackedStream, IORunnable onAfterClose);

    protected final Resource createTracker() {
        return trackerCreator.get();
    }

}
