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

import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * @author Dave Shepperton
 */
public final class ImmutablePropStoreTest {

    @Test
    public void testPutPropertyFailsWithUnsupportedOperationException() {
        Exception actual;
        try {
            SimpleProperties.asPropStore(new HashMap<>()).toImmutable().putProperty("foo", "bar");
            actual = null;
        }
        catch (UnsupportedOperationException e) {
            actual = e;
        }
        assertNotNull(actual);
    }

    @Test
    public void testCommitChangesFailsReadOnlyStore() {
        CommitResult result = SimpleProperties.asPropStore(new HashMap<>())
            .toImmutable()
            .commitChanges(new Object());
        assertEquals(CommitResult.StandardFailureStatus.READ_ONLY_STORE, result.getStatus());
    }

}
