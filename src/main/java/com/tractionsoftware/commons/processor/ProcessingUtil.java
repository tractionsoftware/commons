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

import com.tractionsoftware.commons.io.IOUtil;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Iterator;

/**
 * @author Dave Shepperton
 *
 * @since 4.0
 */
public final class ProcessingUtil {

    private ProcessingUtil() {
    }

    public static final <X extends Exception> Result getResultFromChainedProcessors(Producer<X> first, Iterator<Processor<X>> processors) throws IOException, X {
        Result result = getPopulatedResult(first);
        while (processors.hasNext()) {
            result = result.getNextResult(processors.next());
        }
        return result;
    }

    public static final <X extends Exception> Result getPopulatedResult(Producer<X> producer) throws IOException, X {
        Result result = producer.getResultProvider().getEmptyResult();
        result.populate(producer);
        return result;
    }

    public static final Consumer<IOException> getOutputStreamConsumer(final OutputStream outputStream) {
        return inputStream -> IOUtil.copyToEOF(inputStream, outputStream);
    }

}
