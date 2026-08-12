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

/**
 * Represents the result of an attempt to commit changes to a {@link PropStore} via
 * {@link PropStore#commitChanges(Object)}.
 *
 * @author Dave Shepperton
 */
public interface CommitResult {

    public static interface Status {

        public default boolean success() {
            return !failed();
        }

        public boolean failed();

    }

    public static enum StandardSuccessStatus implements Status {

        SUCCESS(true, true),

        NO_CHANGES_REQUESTED(false, false),

        NO_CHANGES_NEEDED(true, false);

        private final boolean hadRequestedChanges;

        private final boolean didCommitChanges;

        private StandardSuccessStatus(boolean hadRequestedChanges, boolean didCommitChanges) {
            this.hadRequestedChanges = hadRequestedChanges;
            this.didCommitChanges = didCommitChanges;
        }

        @Override
        public final boolean failed() {
            return false;
        }

        public final boolean hadRequestedChanges() {
            return hadRequestedChanges;
        }

        public final boolean didCommitChanges() {
            return didCommitChanges;
        }

    }

    public static enum StandardFailureStatus implements Status {

        INVALID_CHANGES,

        READ_ONLY_STORE,

        ERROR;

        @Override
        public final boolean failed() {
            return true;
        }

    }

    /**
     * Returns true if the commit attempt had any requested changes.
     *
     * @return true if the commit attempt had any requested changes; false if it is known that no changes were
     *     requested.
     */
    public boolean hadRequestedChanges();

    /**
     * Returns true if the commit attempt did actually successfully commit some changes. Because of the contract of
     * {@link PropStore#commitChanges(Object)} being all-or-nothing, this method should only ever return true if all
     * changes were committed, or at least if there was no failure.
     *
     * @return true if the commit attempt did actually successfully commit some changes; false if no changes were
     *     committed.
     */
    public boolean didCommitChanges();

    /**
     * Returns true if the operation was successful. Use {@link #getStatus()} to retrieve a more specific status; and if
     * {@link Status#failed() the Status represents a failure}, use {@link #getFailureCause()} to retrieve the cause of
     * the failure.
     *
     * @return true if the operation was successful; false if it failed.
     */
    public boolean wasSuccessful();

    /**
     * Returns a {@link Status} representing a slightly more specific classification for the result than success vs.
     * failure.
     *
     * @return a {@link Status} representing a slightly more specific classification for the result than success vs.
     *     failure.
     */
    public Status getStatus();

    /**
     * If this instance does not represent {@link #wasSuccessful() a successful commit operation}, this method returns
     * the {@link PropStoreCommitException} representing the cause of the failure.
     *
     * @return the {@link PropStoreCommitException} representing the cause of the failure, if this instance does not
     *     represent {@link #wasSuccessful() a successful commit operation}; null otherwise.
     */
    public PropStoreCommitException getFailureCause();

}
