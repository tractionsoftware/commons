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

package com.tractionsoftware.commons.processor;

import java.io.IOException;

/**
 * An implementation of ResultProvider that creates instances of the TempFileResult class.
 *
 * @author Dave Shepperton
 * @see TempFileResult
 * @since 4.0
 */
public final class TempFileResultProvider implements ResultProvider {

    public static final ResultProvider getInstance(TempFileResult.Helper helper) {
        return new TempFileResultProvider(helper);
    }

    private final TempFileResult.Helper helper;

    private TempFileResultProvider(TempFileResult.Helper helper) {
        this.helper = helper;
    }

    @Override
    public final Result getEmptyResult() throws IOException {
        return TempFileResult.getInstance(helper);
    }

}
