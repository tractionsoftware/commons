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
import java.io.InputStream;
import java.io.OutputStream;

/**
 * A simple abstraction for a processor that uses some input to produce some output.
 *
 * <p>
 * It will generally be used by a Result instance, as a way to prevent client code from having to deal with creating and
 * managing the InputStream and OutputStream for the process input and output, but may also be used independently.
 *
 * @param <X>
 *     the type of Exceptions produced by the {@link Producer#produce(OutputStream)} methods of the {@link Producer}s
 *     used by this Processor.
 * @author Dave Shepperton
 * @since 4.0
 */
public interface Processor<X extends Exception> extends HasResultProvider {

    /**
     * This method is invoked when client code wants this Processor to use a particular InputStream to produce new data
     * written to a given {@link OutputStream}. It will generally be invoked by a
     * {@link Result#getNextResult(Processor)}, which has access to the InputStream from one {@link Result} representing
     * the input to this process as well as the OutputStream from another new empty Result representing the output from
     * this process.
     *
     * @param input
     *     the InputStream representing the input for this process.
     * @param output
     *     the {@link OutputStream} representing the sink for output from this process.
     * @throws IOException
     *     if there is problem reading from the InputStream or writing to the {@link OutputStream}.
     * @throws X
     *     a super-type for the Exceptions that can be thrown by this type of {@link Processor}.
     */
    public void process(InputStream input, OutputStream output) throws IOException, X;

}
