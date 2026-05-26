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

public final class OutputStreamLimitExceededException extends IOException {

    @Serial
    private static final long serialVersionUID = -2929362180320846748L;

    public OutputStreamLimitExceededException(long maximumBytes, long bytesAttempted) {
        super("Attempted to write " + bytesAttempted + "B > " + maximumBytes + "B.");
    }

}
