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
 * A {@link PropStoreCommitException} thrown to indicate that the request to commit property changes failed because the
 * store is in a read-only state (e.g., because it is an instance of {@link ImmutablePropStore} or was otherwise
 * produced via {@link PropStore#toImmutable()}).
 *
 * @author Dave Shepperton
 */
public class ReadOnlyPropStoreException extends PropStoreCommitException {

    @Serial
    private static final long serialVersionUID = -7001134440848641489L;

    /**
     * Constructs a new ReadOnlyPropStoreException with no detail message.
     */
    public ReadOnlyPropStoreException() {
        super();
    }

    /**
     * Constructs a new ReadOnlyPropStoreException with the specified detail message.
     *
     * @param message
     *     the detail message. The detail message is saved for later retrieval by the {@link #getMessage()} method.
     */
    public ReadOnlyPropStoreException(String message) {
        super(message);
    }

    /**
     * Constructs a new ReadOnlyPropStoreException with the specified cause and a detail message of
     * <tt>(cause==null ? null : cause.toString())</tt> (which
     * typically contains the class and detail message of
     * <tt>cause</tt>). This constructor is useful for exceptions that
     * are little more than wrappers for other throwables.
     *
     * @param cause
     *     the underlying cause.
     */
    public ReadOnlyPropStoreException(Throwable cause) {
        super(cause);
    }

    /**
     * Constructs a new ReadOnlyPropStoreException with the specified cause and a detail message of
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
    public ReadOnlyPropStoreException(String message, Throwable cause) {
        super(message, cause);
    }

    @Override
    public CommitResult.Status getStatus() {
        return CommitResult.StandardFailureStatus.READ_ONLY_STORE;
    }

}
