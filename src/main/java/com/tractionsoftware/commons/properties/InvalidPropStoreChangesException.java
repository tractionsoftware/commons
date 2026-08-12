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

package com.tractionsoftware.commons.properties;

import java.io.Serial;

/**
 * A {@link PropStoreCommitException} thrown to indicate that one or more requested property changes -- either
 * individually, or in combination -- were not valid.
 *
 * @author Dave Shepperton
 */
public class InvalidPropStoreChangesException extends PropStoreCommitException {

    @Serial
    private static final long serialVersionUID = 3790970241072861816L;

    /**
     * Constructs a new InvalidPropStoreChangesException with no detail message.
     */
    public InvalidPropStoreChangesException() {
        super();
    }

    /**
     * Constructs a new InvalidPropStoreChangesException with the specified detail message.
     *
     * @param message
     *     the detail message. The detail message is saved for later retrieval by the {@link #getMessage()} method.
     */
    public InvalidPropStoreChangesException(String message) {
        super(message);
    }

    /**
     * Constructs a new InvalidPropStoreChangesException with the specified cause and a detail message of
     * <tt>(cause==null ? null : cause.toString())</tt> (which
     * typically contains the class and detail message of
     * <tt>cause</tt>). This constructor is useful for exceptions that
     * are little more than wrappers for other throwables.
     *
     * @param cause
     *     the underlying cause.
     */
    public InvalidPropStoreChangesException(Throwable cause) {
        super(cause);
    }

    /**
     * Constructs a new InvalidPropStoreChangesException with the specified cause and a detail message of
     * <tt>(cause==null ? null : cause.toString())</tt> (which
     * typically contains the class and detail message of
     * <tt>cause</tt>). This constructor is useful for exceptions that
     * are little more than wrappers for other throwables.
     *
     * @param message
     *     the detail message.
     * @param cause
     *     the underlying cause.
     */
    public InvalidPropStoreChangesException(String message, Throwable cause) {
        super(message, cause);
    }

    @Override
    public CommitResult.Status getStatus() {
        return CommitResult.StandardFailureStatus.INVALID_CHANGES;
    }

}
