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

import java.io.IOException;
import java.io.Serial;

/**
 * An {@link IOException} that is raised when a stream size limit has been exceeded -- either reading or writing too
 * many bytes.
 *
 * @author Dave Shepperton
 */
public final class StreamSizeLimitExceededException extends IOException {

    @Serial
    private static final long serialVersionUID = -4169472154436382298L;

    public static final StreamSizeLimitExceededException forWrite(long maximumBytes, long bytesAttempted) {
        return new StreamSizeLimitExceededException(
            "Attempted to write " + bytesAttempted + "B > " + maximumBytes + "B."
        );
    }

    public static final StreamSizeLimitExceededException forRead(long maximumBytes, long bytesAttempted) {
        return new StreamSizeLimitExceededException(
            "Attempted to read " + bytesAttempted + "B > " + maximumBytes + "B."
        );
    }

    private StreamSizeLimitExceededException(String message) {
        super(message);
    }

}
