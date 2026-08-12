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
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.Writer;

/**
 * /** A Traction-specific Producer used in the context of a producer/consumer pattern, or to represent the first stage
 * in a multi-step process.
 *
 * @param <X>
 *     the type of Exceptions produced by the {@link #produce(OutputStream)} method.
 * @author Dave Shepperton
 * @since 4.0
 */
public interface Producer<X extends Exception> extends HasResultProvider {

    /**
     * Produces output representing a {@link Result}.
     *
     * @param output
     *     to which the {@link Result}'s output should be written.
     * @throws IOException
     *     if one is raised while writing to the given {@link OutputStream}.
     * @throws X
     *     if another implementation-specific error is raised while attempting to produce the {@link Result} data.
     */
    public void produce(OutputStream output) throws IOException, X;

    /**
     * Produces output representing a {@link Result}.
     *
     * @param out
     *     to which the {@link Result}'s output should be written.
     * @throws IOException
     *     if one is raised while writing to the given {@link OutputStream}.
     * @throws X
     *     if another implementation-specific error is raised while attempting to produce the {@link Result} data.
     */
    public void produce(Writer out) throws IOException, X;

    /**
     * Produces output representing a {@link Result}.
     *
     * @param out
     *     to which the {@link Result}'s output should be written.
     * @throws IOException
     *     if one is raised while writing to the given {@link OutputStream}.
     * @throws X
     *     if another implementation-specific error is raised while attempting to produce the {@link Result} data.
     */
    public void produce(PrintWriter out) throws IOException, X;

}
