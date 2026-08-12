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

import java.util.Objects;

public final class CommitResults {

    private CommitResults() {
    }

    private static final class SuccessfulCommitResult implements CommitResult {

        final CommitResult.StandardSuccessStatus status;

        SuccessfulCommitResult(CommitResult.StandardSuccessStatus status) {
            this.status = status;
        }

        @Override
        public final boolean hadRequestedChanges() {
            return status.hadRequestedChanges();
        }

        @Override
        public final boolean didCommitChanges() {
            return status.didCommitChanges();
        }

        @Override
        public final boolean wasSuccessful() {
            return true;
        }

        @Override
        public final Status getStatus() {
            return status;
        }

        @Override
        public final PropStoreCommitException getFailureCause() {
            return null;
        }

    }

    private static final class FailedCommitResult implements CommitResult {

        private static final FailedCommitResult forFailure(boolean hadRequestedChanges, PropStoreCommitException failure) {
            Objects.requireNonNull(failure, "error");
            return new FailedCommitResult(hadRequestedChanges, failure);
        }

        private final boolean hadRequestedChanges;

        private final PropStoreCommitException failure;

        private FailedCommitResult(boolean hadRequestedChanges, PropStoreCommitException failure) {
            this.hadRequestedChanges = hadRequestedChanges;
            this.failure = failure;
        }

        @Override
        public final boolean hadRequestedChanges() {
            return hadRequestedChanges;
        }

        @Override
        public final boolean didCommitChanges() {
            return false;
        }

        @Override
        public final boolean wasSuccessful() {
            return false;
        }

        @Override
        public final Status getStatus() {
            return failure.getStatus();
        }

        @Override
        public final PropStoreCommitException getFailureCause() {
            return failure;
        }

    }

    public static final CommitResult RESULT_SUCCESSFUL =
        new SuccessfulCommitResult(CommitResult.StandardSuccessStatus.SUCCESS);

    public static final CommitResult RESULT_NO_CHANGES_REQUESTED =
        new SuccessfulCommitResult(CommitResult.StandardSuccessStatus.NO_CHANGES_REQUESTED);

    public static final CommitResult RESULT_NO_CHANGES_NEEDED =
        new SuccessfulCommitResult(CommitResult.StandardSuccessStatus.NO_CHANGES_NEEDED);

    /**
     * Produces a {@link CommitResult} carrying the given {@link PropStoreCommitException} as the cause of the failure.
     */
    public static final CommitResult forFailure(boolean hadRequestedChanges, PropStoreCommitException error) {
        return FailedCommitResult.forFailure(hadRequestedChanges, error);
    }

}
