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
 * An interface for an object that can produce instances of one or more Result classes.
 *
 * @author Dave Shepperton
 * @since 4.0
 */
public interface ResultProvider {

    /**
     * Create and return a new empty Result instance ready to be populated and later consumed.
     *
     * <p>
     * Classes implementing this method <em>must</em> return a Result instance for which it has been established, using
     * all reasonable measures available, that attempts to write to and read from the underlying data source will not
     * have any reason to fail unexpectedly.
     *
     * @return a new empty Result instance ready to be populated and later consumed.
     * @throws IOException
     *     if there is a problem creating the Result, e.g., reserving space in memory, in a database, or on a disk.
     */
    public Result getEmptyResult() throws IOException;

}
