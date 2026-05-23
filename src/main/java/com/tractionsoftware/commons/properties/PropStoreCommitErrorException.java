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
 * A {@link PropStoreCommitException} thrown to indicate that there was an unexpected error -- e.g., an
 * {@link java.io.IOException} attempting to write to a file -- while attempting to commit the requested changes.
 *
 * @author Dave Shepperton
 */
public class PropStoreCommitErrorException extends PropStoreCommitException {

    @Serial
    private static final long serialVersionUID = -1750765107889445915L;

    /**
     * Constructs a new PropStoreCommitErrorException with no detail message.
     */
    public PropStoreCommitErrorException() {
        super();
    }

    /**
     * Constructs a new PropStoreCommitErrorException with the specified detail message.
     *
     * @param message
     *     the detail message. The detail message is saved for later retrieval by the {@link #getMessage()} method.
     */
    public PropStoreCommitErrorException(String message) {
        super(message);
    }

    /**
     * Constructs a new PropStoreCommitErrorException with the specified cause and a detail message of
     * <tt>(cause==null ? null : cause.toString())</tt> (which
     * typically contains the class and detail message of
     * <tt>cause</tt>). This constructor is useful for exceptions that
     * are little more than wrappers for other throwables.
     *
     * @param cause
     *     the underlying cause.
     */
    public PropStoreCommitErrorException(Throwable cause) {
        super(cause);
    }

    /**
     * Constructs a new PropStoreCommitErrorException with the specified cause and a detail message of
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
    public PropStoreCommitErrorException(String message, Throwable cause) {
        super(cause);
    }

    @Override
    public CommitResult.Status getStatus() {
        return CommitResult.StandardFailureStatus.ERROR;
    }

}
