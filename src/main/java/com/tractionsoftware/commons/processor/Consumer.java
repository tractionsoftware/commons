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

import com.tractionsoftware.commons.io.SizedInputStream;

import java.io.IOException;

/**
 * A specific Consumer used in the producer/consumer pattern.
 *
 * @param <X>
 *     the type of Exceptions produced by the {@link Producer}s used with this Consumer.
 * @author Dave Shepperton
 * @since 4.0
 */
public interface Consumer<X extends Exception> {

    /**
     * Offers a result, in the form of a {@link SizedInputStream}, to this Consumer to be consumed.
     *
     * @param input
     *     the InputStream to be consumed.
     * @throws X
     *     a super type for Exceptions that are thrown by a given consume method implementation in the event of a
     *     problem.
     */
    public void consume(SizedInputStream input) throws IOException, X;

}
