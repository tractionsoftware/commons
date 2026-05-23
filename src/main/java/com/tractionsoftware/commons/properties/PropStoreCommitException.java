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
 * An exception that is thrown to indicate that there was a problem modifying the properties for an object.
 *
 * @author Dave Shepperton
 */
public abstract class PropStoreCommitException extends Exception {

    @Serial
    private static final long serialVersionUID = 1736055295714073126L;

    /**
     * Constructs a new PropStoreCommitException with no detail message.
     */
    public PropStoreCommitException() {
        super();
    }

    /**
     * Constructs a new PropStoreCommitException with the specified detail message.
     *
     * @param message
     *     the detail message. The detail message is saved for later retrieval by the {@link #getMessage()} method.
     */
    public PropStoreCommitException(String message) {
        super(message);
    }

    /**
     * Constructs a new PropStoreCommitException with the specified cause and a detail message of
     * <tt>(cause==null ? null : cause.toString())</tt> (which
     * typically contains the class and detail message of
     * <tt>cause</tt>). This constructor is useful for exceptions that
     * are little more than wrappers for other throwables.
     *
     * @param cause
     *     the underlying cause.
     */
    public PropStoreCommitException(Throwable cause) {
        super(cause);
    }

    /**
     * Constructs a new PropStoreCommitException with the specified cause and a detail message of
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
    public PropStoreCommitException(String message, Throwable cause) {
        super(cause);
    }

    public abstract CommitResult.Status getStatus();

}
