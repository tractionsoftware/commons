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
import com.tractionsoftware.commons.io.SizedInputStream;
import org.apache.commons.io.function.IOConsumer;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * A Result encapsulates the output from a {@link Producer} or {@link Processor}, to be used as the input for a
 * {@link IOConsumer}, or for another Processor representing the next stage of a multi-stage process. All the operations
 * wrap any I/O operations in such a way that client code implementations of Producer, Consumer and Processor do not
 * have to worry about closing streams or disposing of records of intermediate results.
 *
 * @author Dave Shepperton
 * @since 4.0
 */
public abstract class Result {

    /**
     * Callers may invoke this method to populate this Result using the given {@link Producer}.
     *
     * @param producer
     *     the {@link Producer} that will be used to populate this Result.
     * @throws X
     *     the super-type for Exceptions that can be thrown by the {@link Producer}'s produce method.
     */
    public synchronized final <X extends Exception> void populate(Producer<X> producer) throws IOException, X {
        boolean success = false;
        try (OutputStream outputStream = getOutputStream()) {
            producer.produce(outputStream);
            success = true;
        }
        finally {
            onPopulate(success);
        }
    }

    /**
     * Callers may invoke this method to consume this Result using the given {@link Consumer}.
     *
     * @param consumer
     *     the {@link Consumer} that will be used to consume this Result.
     * @throws X
     *     the super-type for Exceptions that can be thrown by the {@link Consumer}'s consume method.
     */
    public synchronized final <X extends Exception> void consume(Consumer<X> consumer)
        throws IOException, X {
        boolean success = false;
        try (SizedInputStream input = getInputStream()) {
            consumer.consume(input);
            success = true;
        }
        finally {
            onConsume(success);
        }
    }

    /**
     * Clients may invoke this method to explicitly indicate that they are finished with this Result and that any
     * resources that it is occupying may be released.
     */
    public abstract void release();

    /**
     * Callers may invoke this method to consume this Result by passing the data it represents as the input to the given
     * {@link Processor}.
     *
     * @param processor
     *     the {@link Processor} to use to create the next Result.
     * @return the Result representing the output from the {@link Processor}.
     * @throws IOException
     *     if there is a problem reading from this Result's InputStream or writing to the new Result's
     *     {@link OutputStream}.
     * @throws X
     *     the super-type for Exceptions that can be thrown by the {@link Processor}'s process method.
     */
    public synchronized final <X extends Exception> Result getNextResult(Processor<X> processor) throws IOException, X {
        boolean success = false;
        try (InputStream inputStream = getInputStream()) {
            Result result = getNextResult(processor, inputStream);
            success = true;
            return result;
        }
        finally {
            onConsume(success);
        }
    }

    /**
     * Deletes this Result, releasing any resources that it occupied. This is the same as consuming the Result, but
     * without actually opening an InputStream.
     */
    public synchronized final void delete() {
        onConsume(true);
    }

    /**
     * This method encapsulates the part of the getNextResult procedure in which the InputStream for the source Result
     * to be consumed has already been created successfully. If the next Result instance and its {@link OutputStream}
     * can be created successfully, the process method of the given Processor will be invoked with the InputStream and
     * the next Result's OutputStream.
     *
     * @param processor
     *     the {@link Processor} whose process method is to be invoked.
     * @param inputStream
     *     the InputStream to use as the source of the Result data for the process method.
     * @return the Result instance newly populated by the {@link Processor}.
     * @throws IOException
     *     if there is a problem reading from this Result's InputStream or writing to the new Result's
     *     {@link OutputStream}.
     * @throws X
     *     the super-type for Exceptions that can be thrown by the {@link Processor}'s process method.
     */
    private synchronized final <X extends Exception> Result getNextResult(Processor<X> processor, InputStream inputStream)
        throws IOException, X {

        Result nextResult = processor.getResultProvider().getEmptyResult();
        OutputStream outputStream;
        try {
            outputStream = nextResult.getOutputStream();
        }
        catch (IOException e) {
            nextResult.onPopulate(false);
            throw e;
        }

        boolean success = false;
        try {
            processor.process(inputStream, outputStream);
            success = true;
        }
        finally {
            IOUtil.flush(outputStream);
            IOUtil.close(outputStream);
            nextResult.onPopulate(success);
        }
        return nextResult;

    }

    /**
     * Returns a {@link OutputStream} that the populate or {@link #getNextResult(Processor)} method may use to populate
     * the Result.
     *
     * @return a {@link OutputStream} that the populate or {@link #getNextResult(Processor)} method may use to populate
     *     the Result.
     * @throws IOException
     *     if there is a problem retrieving or creating the {@link OutputStream}.
     */
    protected abstract OutputStream getOutputStream() throws IOException;

    /**
     * Returns an InputStream that the consume or {@link #getNextResult(Processor)} method may use to retrieve the
     * content of this Result.
     *
     * @return an InputStream that the consume or {@link #getNextResult(Processor)} method may use to retrieve the
     *     content of this Result.
     * @throws IOException
     *     if there is a problem retrieving or creating the InputStream.
     */
    protected abstract SizedInputStream getInputStream() throws IOException;

    /**
     * Invoked when this Results {@link #populate(Producer)} method has been called, or when another Result's
     * {@link #getNextResult(Processor)} method has been called, to indicate that the attempt to populate this Result
     * has completed.
     *
     * @param success
     *     will be true to indicate that the attempt to populate this Result completed without error; false otherwise.
     */
    protected abstract void onPopulate(boolean success);

    /**
     * Invoked when this Result's {@link #consume(Consumer)} or {@link #getNextResult(Processor)} method has been
     * called, to indicate that the attempt to consume this Result has completed.
     *
     * @param success
     *     will be true to indicate that the attempt to consume this Result completed without error; false otherwise.
     */
    protected abstract void onConsume(boolean success);

}
