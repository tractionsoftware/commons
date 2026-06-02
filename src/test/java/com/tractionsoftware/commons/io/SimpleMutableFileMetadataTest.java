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

package com.tractionsoftware.commons.io;

import org.junit.jupiter.api.Test;

import java.net.URI;

import static org.junit.jupiter.api.Assertions.*;

class SimpleMutableFileMetadataTest {

    // ---------------------------------------------------------------------------
    // filename
    // ---------------------------------------------------------------------------

    @Test
    void getFilename_default_returnsNull() {
        SimpleMutableFileMetadata m = new SimpleMutableFileMetadata();
        assertNull(m.getFilename());
    }

    @Test
    void setGetFilename_roundTrips() {
        SimpleMutableFileMetadata m = new SimpleMutableFileMetadata();
        m.setFilename("report.pdf");
        assertEquals("report.pdf", m.getFilename());
    }

    // ---------------------------------------------------------------------------
    // description
    // ---------------------------------------------------------------------------

    @Test
    void setGetDescription_roundTrips() {
        SimpleMutableFileMetadata m = new SimpleMutableFileMetadata();
        m.setDescription("A test file");
        assertEquals("A test file", m.getDescription());
    }

    // ---------------------------------------------------------------------------
    // displayName
    // ---------------------------------------------------------------------------

    @Test
    void getDisplayName_fallsBackToFilename() {
        SimpleMutableFileMetadata m = new SimpleMutableFileMetadata();
        m.setFilename("file.txt");
        // displayName not set → should fall back to filename
        assertEquals("file.txt", m.getDisplayName());
    }

    @Test
    void setGetDisplayName_overridesFilename() {
        SimpleMutableFileMetadata m = new SimpleMutableFileMetadata();
        m.setFilename("file.txt");
        m.setDisplayName("My File");
        assertEquals("My File", m.getDisplayName());
    }

    // ---------------------------------------------------------------------------
    // URI
    // ---------------------------------------------------------------------------

    @Test
    void setGetURI_roundTrips() {
        SimpleMutableFileMetadata m = new SimpleMutableFileMetadata();
        URI uri = URI.create("urn:test:123");
        m.setURI(uri);
        assertEquals(uri, m.getURI());
    }

    // ---------------------------------------------------------------------------
    // equals / hashCode
    // ---------------------------------------------------------------------------

    @Test
    void equals_sameURI_returnsTrue() {
        SimpleMutableFileMetadata a = new SimpleMutableFileMetadata();
        SimpleMutableFileMetadata b = new SimpleMutableFileMetadata();
        URI uri = URI.create("urn:test:abc");
        a.setURI(uri);
        b.setURI(uri);
        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
    }

    @Test
    void equals_differentURI_returnsFalse() {
        SimpleMutableFileMetadata a = new SimpleMutableFileMetadata();
        SimpleMutableFileMetadata b = new SimpleMutableFileMetadata();
        a.setURI(URI.create("urn:test:1"));
        b.setURI(URI.create("urn:test:2"));
        assertNotEquals(a, b);
    }

    @Test
    void equals_notSameType_returnsFalse() {
        SimpleMutableFileMetadata m = new SimpleMutableFileMetadata();
        assertNotEquals(m, "not a metadata");
    }

    // ---------------------------------------------------------------------------
    // isSameFileData
    // ---------------------------------------------------------------------------

    @Test
    void isSameFileData_identicalObjects_returnsTrue() {
        SimpleMutableFileMetadata a = new SimpleMutableFileMetadata();
        a.setFilename("x.txt");
        a.setDescription("desc");
        assertTrue(a.isSameFileData(a));
    }

    @Test
    void isSameFileData_null_returnsFalse() {
        SimpleMutableFileMetadata a = new SimpleMutableFileMetadata();
        assertFalse(a.isSameFileData(null));
    }

    @Test
    void isSameFileData_differentFilename_returnsFalse() {
        SimpleMutableFileMetadata a = new SimpleMutableFileMetadata();
        a.setFilename("a.txt");
        SimpleMutableFileMetadata b = new SimpleMutableFileMetadata();
        b.setFilename("b.txt");
        assertFalse(a.isSameFileData(b));
    }
}
