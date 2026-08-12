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
import org.apache.commons.io.function.IORunnable;

import java.io.OutputStream;
import java.util.function.Supplier;

public final class OutputStreamTracker extends IOResourceTracker<OutputStream> {

    public OutputStreamTracker(String sourceIdentifier, Supplier<? extends Resource> trackerCreator) {
        super(sourceIdentifier, trackerCreator);
    }

    @Override
    protected final String getStreamTypeName() {
        return "OutputStream";
    }

    @Override
    protected final OutputStream getTrackedStream(OutputStream stream, String sourceIdentifier) {
        return IOUtil.getTrackedOutputStream(stream, this::createTracker, sourceIdentifier);
    }

    @Override
    protected final OutputStream getCloseNotifyingStream(OutputStream trackedStream, IORunnable onAfterClose) {
        return IOUtil.getCloseNotifyingOutputStream(trackedStream, null, onAfterClose);
    }

}
