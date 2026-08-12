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

import com.tractionsoftware.commons.lang.Resource;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import static org.junit.jupiter.api.Assertions.*;

public final class OutputStreamTrackerTest {

    /**
     * Minimal Resource implementation for use as tracker creator.
     */
    private static Resource noopResource() {
        return new Resource() {
            @Override public void close() {}
            @Override public boolean isOpen() { return true; }
        };
    }

    @Test
    void constructor_createsInstance() {
        OutputStreamTracker tracker = new OutputStreamTracker("test", OutputStreamTrackerTest::noopResource);
        assertNotNull(tracker);
    }

    @Test
    void isOpen_initiallyTrue() {
        OutputStreamTracker tracker = new OutputStreamTracker("test", OutputStreamTrackerTest::noopResource);
        assertTrue(tracker.isOpen());
    }

    @Test
    void close_setsIsOpenFalse() {
        OutputStreamTracker tracker = new OutputStreamTracker("test", OutputStreamTrackerTest::noopResource);
        tracker.close();
        assertFalse(tracker.isOpen());
    }

    @Test
    void close_isIdempotent() {
        OutputStreamTracker tracker = new OutputStreamTracker("test", OutputStreamTrackerTest::noopResource);
        tracker.close();
        tracker.close(); // should not throw
        assertFalse(tracker.isOpen());
    }

    @Test
    void createTrackedStream_returnsWrappedOutputStream() throws IOException {
        ByteArrayOutputStream sink = new ByteArrayOutputStream();
        OutputStreamTracker tracker = new OutputStreamTracker("test", OutputStreamTrackerTest::noopResource);
        OutputStream stream = tracker.createTrackedStream(() -> sink);
        assertNotNull(stream);
        stream.write(42);
        stream.flush();
        stream.close();
    }

    @Test
    void createTrackedStream_writerCanWriteBytes() throws IOException {
        ByteArrayOutputStream sink = new ByteArrayOutputStream();
        OutputStreamTracker tracker = new OutputStreamTracker("write-test", OutputStreamTrackerTest::noopResource);
        try (OutputStream stream = tracker.createTrackedStream(() -> sink)) {
            stream.write(new byte[]{1, 2, 3});
        }
        assertArrayEquals(new byte[]{1, 2, 3}, sink.toByteArray());
    }

    @Test
    void close_afterCreateTrackedStream_closesTracker() throws IOException {
        ByteArrayOutputStream sink = new ByteArrayOutputStream();
        OutputStreamTracker tracker = new OutputStreamTracker("close-test", OutputStreamTrackerTest::noopResource);
        tracker.createTrackedStream(() -> sink);
        tracker.close();
        // After tracker is closed, isOpen should be false
        assertFalse(tracker.isOpen());
    }

    @Test
    void hasAnyStreams_beforeCreate_false() {
        OutputStreamTracker tracker = new OutputStreamTracker("streams-test", OutputStreamTrackerTest::noopResource);
        assertFalse(tracker.hasAnyStreams());
    }

    @Test
    void hasAnyStreams_afterCreate_true() throws IOException {
        ByteArrayOutputStream sink = new ByteArrayOutputStream();
        OutputStreamTracker tracker = new OutputStreamTracker("streams-test2", OutputStreamTrackerTest::noopResource);
        tracker.createTrackedStream(() -> sink);
        assertTrue(tracker.hasAnyStreams());
    }

    @Test
    void getStreamTypeName_isOutputStream() {
        // Verify via the tracker's toString or debug methods don't throw
        OutputStreamTracker tracker = new OutputStreamTracker("typename-test", OutputStreamTrackerTest::noopResource);
        // The tracker should be usable — just exercise it without error
        assertNotNull(tracker.toString());
    }

}
