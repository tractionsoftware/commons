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

package com.tractionsoftware.commons.lang;

import com.google.common.annotations.Beta;

/**
 * A thin extension of {@link AutoCloseable} representing a loaded resource. It may wrap a traditional resource, such as
 * a file descriptor or open socket, or may instead be an encapsulation for the cleanup required after an operation has
 * completed.
 *
 * @author Dave Shepperton
 */
public interface Resource extends AutoCloseable {

    /**
     * For a failed attempt to set up and open a resource, represents the phase when the error occurred.
     *
     * @author Dave Shepperton
     * @see LoadAttemptResult#getErrorType()
     */
    @Beta
    public static enum LoadErrorType {

        /**
         * The attempt to set up the resource failed. This can mean that the resource couldn't be identified, or
         * couldn't be retrieved.
         */
        SETUP,

        /**
         * The attempt to open the resource failed. For traditional resources, such as a file descriptor or open socket,
         * this would generally imply that the error happened while literally opening the underlying I/O connection.
         */
        OPEN,

        /**
         * The attempt to set up and open the resource may have succeeded (or would have if it had been allowed to
         * proceed), but it was vetoed. This can mean that it was not applicable or appropriate in the current context.
         * This value mainly provides another category of error usually (but not always) ignored, since the implication
         * is that the failure should be "silently" ignored. (Nonetheless, this type of error can be handled in any way
         * deemed appropriate for a given implementation.)
         */
        CONFIRM;

        /**
         * Returns a {@link LoadAttemptResult} carrying the given fatal error and this LoadErrorType.
         *
         * @param error
         *     the {@link RuntimeException} representing the fatal error.
         * @return a {@link LoadAttemptResult} carrying the given fatal error and this LoadErrorType.
         */
        public final LoadAttemptResult createFailedAttemptResult(RuntimeException error) {
            return ResourceUtil.createFailedResult(this, error);
        }

    }

    /**
     * Represents the result of an attempt to load. It extends {@link AutoCloseable} so that it can be used with a
     * try-with-resources statement, facilitating a safe and consistent pattern for working with resources.
     *
     * @author Dave Shepperton
     */
    public static interface LoadAttemptResult {

        /**
         * Returns true if the {@link Resource} was loaded successfully and is ready for use.
         *
         * @return true if the {@link Resource} was loaded successfully and is ready for use; false otherwise.
         */
        default boolean wasSuccessful() {
            if (getErrorType() == null) {
                return true;
            }
            return false;
        }

        /**
         * Returns the {@link Resource} that was loaded, if loading was successful.
         *
         * @return the {@link Resource} that was loaded, if loading was successful.
         */
        Resource getResource();

        /**
         * Returns the {@link LoadErrorType} indicating the category of error that prevented the load attempt from
         * succeeding, if the attempt failed.
         *
         * @return the {@link LoadErrorType} indicating the category of error that prevented the load attempt from
         *     succeeding, if the attempt failed; null otherwise.
         */
        LoadErrorType getErrorType();

        /**
         * Returns a {@link RuntimeException} associated with the error that prevented the load attempt from succeeding,
         * if the attempt failed.
         *
         * @return a {@link RuntimeException} associated with the error that prevented the load attempt from succeeding,
         *     if the attempt failed; null otherwise.
         */
        RuntimeException getFatalException();

    }

    /**
     * Releases or closes the underlying resource. This may involve actually closing a real resource, such as a file
     * descriptor or socket, or simply reverting some changes that may have been made to facilitate execution of child
     * tags. Implementations must be idempotent, and must not throw any Exceptions.
     */
    @Override
    public void close();

    /**
     * Returns true if this Resource is still considered to be open. This method should return true until the
     * {@link #close()} method has been invoked at least once.
     *
     * @return true if this Resource is still considered open.
     */
    public boolean isOpen();

    public default boolean isClosed() {
        return !isOpen();
    }

}
