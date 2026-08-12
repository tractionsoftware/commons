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

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for {@link CommitResults} and the inner result implementations it produces.
 */
class CommitResultsTest {

    // -------------------------------------------------------------------------
    // RESULT_SUCCESSFUL (StandardSuccessStatus.SUCCESS: hadChanges=true, didCommit=true)
    // -------------------------------------------------------------------------

    @Test
    void resultSuccessful_wasSuccessful_true() {
        assertTrue(CommitResults.RESULT_SUCCESSFUL.wasSuccessful());
    }

    @Test
    void resultSuccessful_hadRequestedChanges_true() {
        assertTrue(CommitResults.RESULT_SUCCESSFUL.hadRequestedChanges());
    }

    @Test
    void resultSuccessful_didCommitChanges_true() {
        assertTrue(CommitResults.RESULT_SUCCESSFUL.didCommitChanges());
    }

    @Test
    void resultSuccessful_getStatus_isSuccess() {
        assertEquals(CommitResult.StandardSuccessStatus.SUCCESS, CommitResults.RESULT_SUCCESSFUL.getStatus());
    }

    @Test
    void resultSuccessful_getFailureCause_null() {
        assertNull(CommitResults.RESULT_SUCCESSFUL.getFailureCause());
    }

    // -------------------------------------------------------------------------
    // RESULT_NO_CHANGES_REQUESTED (StandardSuccessStatus.NO_CHANGES_REQUESTED: hadChanges=false, didCommit=false)
    // -------------------------------------------------------------------------

    @Test
    void resultNoChangesRequested_wasSuccessful_true() {
        assertTrue(CommitResults.RESULT_NO_CHANGES_REQUESTED.wasSuccessful());
    }

    @Test
    void resultNoChangesRequested_hadRequestedChanges_false() {
        assertFalse(CommitResults.RESULT_NO_CHANGES_REQUESTED.hadRequestedChanges());
    }

    @Test
    void resultNoChangesRequested_didCommitChanges_false() {
        assertFalse(CommitResults.RESULT_NO_CHANGES_REQUESTED.didCommitChanges());
    }

    @Test
    void resultNoChangesRequested_getStatus_isNoChangesRequested() {
        assertEquals(
            CommitResult.StandardSuccessStatus.NO_CHANGES_REQUESTED,
            CommitResults.RESULT_NO_CHANGES_REQUESTED.getStatus()
        );
    }

    // -------------------------------------------------------------------------
    // RESULT_NO_CHANGES_REQUESTED (StandardSuccessStatus.NO_CHANGES_NEEDED: hadChanges=true, didCommit=false)
    // -------------------------------------------------------------------------

    @Test
    void resultNoChangesNeeded_wasSuccessful_true() {
        assertTrue(CommitResults.RESULT_NO_CHANGES_NEEDED.wasSuccessful());
    }

    @Test
    void resultNoChangesNeeded_hadRequestedChanges_false() {
        assertTrue(CommitResults.RESULT_NO_CHANGES_NEEDED.hadRequestedChanges());
    }

    @Test
    void resultNoChangesNeeded_didCommitChanges_false() {
        assertFalse(CommitResults.RESULT_NO_CHANGES_NEEDED.didCommitChanges());
    }

    @Test
    void resultNoChangesNeeded_getStatus_isNoChangesNeeded() {
        assertEquals(
            CommitResult.StandardSuccessStatus.NO_CHANGES_NEEDED,
            CommitResults.RESULT_NO_CHANGES_NEEDED.getStatus()
        );
    }

    // -------------------------------------------------------------------------
    // forFailure — FailedCommitResult
    // -------------------------------------------------------------------------

    @Test
    void forFailure_hadRequestedChanges_true_whenPassedTrue() {
        CommitResult r = CommitResults.forFailure(true, new PropStoreCommitErrorException("err"));
        assertTrue(r.hadRequestedChanges());
    }

    @Test
    void forFailure_hadRequestedChanges_false_whenPassedFalse() {
        CommitResult r = CommitResults.forFailure(false, new PropStoreCommitErrorException("err"));
        assertFalse(r.hadRequestedChanges());
    }

    @Test
    void forFailure_didCommitChanges_alwaysFalse() {
        CommitResult r = CommitResults.forFailure(true, new PropStoreCommitErrorException("err"));
        assertFalse(r.didCommitChanges());
    }

    @Test
    void forFailure_wasSuccessful_false() {
        CommitResult r = CommitResults.forFailure(true, new PropStoreCommitErrorException("err"));
        assertFalse(r.wasSuccessful());
    }

    @Test
    void forFailure_getStatus_returnsExceptionStatus() {
        PropStoreCommitErrorException ex = new PropStoreCommitErrorException("err");
        CommitResult r = CommitResults.forFailure(true, ex);
        assertEquals(CommitResult.StandardFailureStatus.ERROR, r.getStatus());
    }

    @Test
    void forFailure_getFailureCause_returnsException() {
        PropStoreCommitErrorException ex = new PropStoreCommitErrorException("err");
        CommitResult r = CommitResults.forFailure(true, ex);
        assertSame(ex, r.getFailureCause());
    }

    @Test
    void forFailure_nullException_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> CommitResults.forFailure(true, null));
    }

    // -------------------------------------------------------------------------
    // CommitResult.Status.success() — default method: !failed()
    // -------------------------------------------------------------------------

    @Test
    void standardSuccessStatus_success_returnsTrue() {
        // failed() returns false for all success statuses; success() = !failed()
        assertTrue(CommitResult.StandardSuccessStatus.SUCCESS.success());
        assertTrue(CommitResult.StandardSuccessStatus.NO_CHANGES_REQUESTED.success());
        assertTrue(CommitResult.StandardSuccessStatus.NO_CHANGES_NEEDED.success());
    }

    @Test
    void standardFailureStatus_success_returnsFalse() {
        // failed() returns true for all failure statuses
        assertFalse(CommitResult.StandardFailureStatus.ERROR.success());
        assertFalse(CommitResult.StandardFailureStatus.INVALID_CHANGES.success());
        assertFalse(CommitResult.StandardFailureStatus.READ_ONLY_STORE.success());
    }

    // -------------------------------------------------------------------------
    // StandardSuccessStatus enum field accessors
    // -------------------------------------------------------------------------

    @Test
    void noChangesNeeded_hadRequestedChanges_true_didCommitChanges_false() {
        // NO_CHANGES_NEEDED: changes were requested but the store was already up to date
        assertTrue(CommitResult.StandardSuccessStatus.NO_CHANGES_NEEDED.hadRequestedChanges());
        assertFalse(CommitResult.StandardSuccessStatus.NO_CHANGES_NEEDED.didCommitChanges());
    }

    @Test
    void noChangesRequested_hadRequestedChanges_false_didCommitChanges_false() {
        assertFalse(CommitResult.StandardSuccessStatus.NO_CHANGES_REQUESTED.hadRequestedChanges());
        assertFalse(CommitResult.StandardSuccessStatus.NO_CHANGES_REQUESTED.didCommitChanges());
    }

}
