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

import java.io.InputStream;
import java.util.function.Supplier;

public final class InputStreamTracker extends IOResourceTracker<InputStream> {

    public InputStreamTracker(String sourceIdentifier, Supplier<? extends Resource> trackerCreator) {
        super(sourceIdentifier, trackerCreator);
    }

    @Override
    protected final String getStreamTypeName() {
        return "InputStream";
    }

    @Override
    protected final InputStream getTrackedStream(InputStream stream, String sourceIdentifier) {
        return IOUtil.getTrackedInputStream(stream, this::createTracker, sourceIdentifier);
    }

    @Override
    protected final InputStream getCloseNotifyingStream(InputStream trackedStream, IORunnable onAfterClose) {
        return IOUtil.getCloseNotifyingInputStream(trackedStream, null, onAfterClose);
    }

}
