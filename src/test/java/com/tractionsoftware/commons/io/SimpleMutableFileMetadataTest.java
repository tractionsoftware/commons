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

import com.tractionsoftware.commons.image.AbstractIconFile;
import com.tractionsoftware.commons.properties.MapPropertyStore;
import com.tractionsoftware.commons.properties.GetPutProperty;
import com.tractionsoftware.commons.text.NumberFormats;
import com.tractionsoftware.commons.util.Dimensions;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.Date;
import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.*;

class SimpleMutableFileMetadataTest {

    // ---------------------------------------------------------------------------
    // test doubles
    // ---------------------------------------------------------------------------

    /**
     * A simple, directly-implemented {@link FileResource} test double, backed by an in-memory byte array.
     */
    private static class TestFileResource implements FileResource {

        private final String filename;

        private final String contentType;

        private final byte[] contents;

        private final FileMetadata metadata;

        TestFileResource(String filename, String contentType, byte[] contents) {
            this(filename, contentType, contents, null);
        }

        TestFileResource(String filename, String contentType, byte[] contents, FileMetadata metadata) {
            this.filename = filename;
            this.contentType = contentType;
            this.contents = contents == null ? new byte[0] : contents;
            this.metadata = metadata;
        }

        @Override
        public FileResourceType getType() {
            return CommonFileResourceType.OTHER;
        }

        @Override
        public boolean isValid() {
            return true;
        }

        @Override
        public URI getURI() {
            return URI.create("test:" + filename);
        }

        @Override
        public String getFilename() {
            return filename;
        }

        @Override
        public boolean isDirectory() {
            return false;
        }

        @Override
        public SizedInputStream getInputStream() throws IOException {
            return SizedInputStream.forInputStream(new ByteArrayInputStream(contents), contents.length);
        }

        @Override
        public String getDescription() {
            return null;
        }

        @Override
        public String getContentType() {
            return contentType;
        }

        @Override
        public String getContentId() {
            return null;
        }

        @Override
        public long getByteSize() {
            return contents.length;
        }

        @Override
        public Date getLastModified() {
            return new Date(0);
        }

        @Override
        public FileMetadata getMetadata() {
            if (metadata != null) {
                return metadata;
            }
            return SimpleMutableFileMetadata.createFromFileNameAndContentType(filename, contentType);
        }

    }

    /**
     * An {@link AbstractIconFile}-based test double backed by the real "heron-320x219.jpg" test resource. Note that
     * {@link #getMetadata()} is deliberately overridden with a plain, directly-constructed
     * {@link SimpleMutableFileMetadata} rather than relying on {@link AbstractIconFile}'s default implementation, which
     * defers back to {@code SimpleMutableFileMetadata.createForIconFileInfo(this)}; relying on the
     * default here would cause infinite recursion, since {@code createForIconFileInfo} itself calls
     * {@code iconFile.getMetadata()} (via {@code createCopyFromFileInfo}).
     */
    private static class HeronIconFileResource extends AbstractIconFile {

        private final byte[] contents;

        private final String filename;

        private final String displayName;

        HeronIconFileResource(byte[] contents, String filename, String displayName) {
            // Supplying the known, preferred dimensions up front avoids any dependence on actual JPEG decoding for
            // getOriginalDimensions(), since AbstractIconFile caches whatever is passed to this constructor.
            super(Dimensions.getInstanceInPixels(320, 219));
            this.contents = contents;
            this.filename = filename;
            this.displayName = displayName;
        }

        @Override
        protected boolean isUnderlyingFileValid() {
            return true;
        }

        @Override
        public SizedInputStream getInputStream() throws IOException {
            return SizedInputStream.forInputStream(new ByteArrayInputStream(contents), contents.length);
        }

        @Override
        public FileResourceType getType() {
            return CommonFileResourceType.ICON_FILE_TYPE;
        }

        @Override
        public URI getURI() {
            return URI.create("test:" + filename);
        }

        @Override
        public String getFilename() {
            return filename;
        }

        @Override
        public String getContentType() {
            return "image/jpeg";
        }

        @Override
        public String getContentId() {
            return null;
        }

        @Override
        public long getByteSize() {
            return contents.length;
        }

        @Override
        public Date getLastModified() {
            return new Date(0);
        }

        @Override
        public String getDisplayName() {
            return displayName;
        }

        @Override
        public FileResourceType getImageResourceType() {
            return CommonFileResourceType.ICON_FILE_TYPE;
        }

        @Override
        public String toDebugString() {
            return "HeronIconFileResource:" + filename;
        }

        @Override
        public FileMetadata getMetadata() {
            SimpleMutableFileMetadata metadata = new SimpleMutableFileMetadata();
            metadata.setFilename(filename);
            metadata.setContentType("image/jpeg");
            return metadata;
        }

    }

    private static byte[] loadHeronBytes() {
        try (InputStream input = SimpleMutableFileMetadataTest.class.getResourceAsStream("heron-320x219.jpg")) {
            assertNotNull(input, "Test resource heron-320x219.jpg could not be found on the classpath");
            return input.readAllBytes();
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

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

    // ---------------------------------------------------------------------------
    // createFromFileName
    // ---------------------------------------------------------------------------

    @Test
    void createFromFileName_setsFilename() {
        SimpleMutableFileMetadata m = SimpleMutableFileMetadata.createFromFileName("report.pdf");
        assertEquals("report.pdf", m.getFilename());
    }

    @Test
    void createFromFileName_setsContentTypeFromExtension() {
        SimpleMutableFileMetadata m = SimpleMutableFileMetadata.createFromFileName("photo.png");
        assertNotNull(m.getContentType());
        assertTrue(m.getContentType().contains("png") || m.getContentType().contains("image"),
            "Expected image content type but got: " + m.getContentType());
    }

    @Test
    void createFromFileName_noExtension_contentTypeIsNull() {
        SimpleMutableFileMetadata m = SimpleMutableFileMetadata.createFromFileName("noext");
        // No extension → no content type derived
        assertNull(m.getContentType());
    }

    // ---------------------------------------------------------------------------
    // createFromFileNameAndContentType
    // ---------------------------------------------------------------------------

    @Test
    void createFromFileNameAndContentType_setsFilenameAndContentType() {
        SimpleMutableFileMetadata m = SimpleMutableFileMetadata.createFromFileNameAndContentType("file.pdf", "application/pdf");
        assertEquals("file.pdf", m.getFilename());
        assertEquals("application/pdf", m.getContentType());
    }

    @Test
    void createFromFileNameAndContentType_nullArgs_fieldsNull() {
        SimpleMutableFileMetadata m = SimpleMutableFileMetadata.createFromFileNameAndContentType(null, null);
        assertNull(m.getFilename());
        assertNull(m.getContentType());
    }

    // ---------------------------------------------------------------------------
    // createInstanceForUnnamedResource
    // ---------------------------------------------------------------------------

    @Test
    void createInstanceForUnnamedResource_noSuggestedFilename_usesDefault() {
        SimpleMutableFileMetadata m = SimpleMutableFileMetadata.createInstanceForUnnamedResource(null, "text/plain", null);
        assertNotNull(m.getFilename());
        assertFalse(m.getFilename().isEmpty());
    }

    @Test
    void createInstanceForUnnamedResource_withSuggestedFilename_usesIt() {
        SimpleMutableFileMetadata m = SimpleMutableFileMetadata.createInstanceForUnnamedResource("upload", "image/png", null);
        assertNotNull(m.getFilename());
        assertTrue(m.getFilename().startsWith("upload"),
            "Expected filename to start with 'upload' but got: " + m.getFilename());
    }

    // ---------------------------------------------------------------------------
    // getDefaultFileName
    // ---------------------------------------------------------------------------

    @Test
    void getDefaultFileName_blankSuggested_returnsFileWithExtension() {
        assertEquals("file.txt", SimpleMutableFileMetadata.getDefaultFileName("", "txt"));
    }

    @Test
    void getDefaultFileName_withSuggested_appendsExtension() {
        assertEquals("report.pdf", SimpleMutableFileMetadata.getDefaultFileName("report", "pdf"));
    }

    @Test
    void getDefaultFileName_noExtension_returnsJustFileName() {
        assertEquals("myfile", SimpleMutableFileMetadata.getDefaultFileName("myfile", null));
    }

    // ---------------------------------------------------------------------------
    // LOADER
    // ---------------------------------------------------------------------------

    @Test
    void loader_missingFilename_returnsNull() {
        MapPropertyStore<Void> store = MapPropertyStore.createInstance(new HashMap<>());
        SimpleMutableFileMetadata loaded = SimpleMutableFileMetadata.LOADER.loadInstance(store.toReadOnly());
        assertNull(loaded);
    }

    @Test
    void loader_withFilename_loadsMetadata() {
        MapPropertyStore<Void> store = MapPropertyStore.createInstance(new HashMap<>());
        store.putProperty(SimpleMutableFileMetadata.PROP_NAME_FILE_NAME, "loaded.txt");
        store.putProperty(SimpleMutableFileMetadata.PROP_NAME_MIMETYPE, "text/plain");
        SimpleMutableFileMetadata loaded = SimpleMutableFileMetadata.LOADER.loadInstance(store.toReadOnly());
        assertNotNull(loaded);
        assertEquals("loaded.txt", loaded.getFilename());
    }

    // ---------------------------------------------------------------------------
    // saveInstance
    // ---------------------------------------------------------------------------

    @Test
    void saveInstance_writesFilenameToStore() {
        SimpleMutableFileMetadata m = new SimpleMutableFileMetadata();
        m.setFilename("saved.txt");
        MapPropertyStore<Void> store = MapPropertyStore.createInstance(new HashMap<>());
        m.saveInstance(store);
        assertEquals("saved.txt", store.getProperty(SimpleMutableFileMetadata.PROP_NAME_FILE_NAME));
    }

    // ---------------------------------------------------------------------------
    // mutableCopy
    // ---------------------------------------------------------------------------

    @Test
    void mutableCopy_returnsIndependentCopy() {
        SimpleMutableFileMetadata original = new SimpleMutableFileMetadata();
        original.setFilename("original.txt");
        original.setDescription("desc");
        SimpleMutableFileMetadata copy = original.mutableCopy();
        assertEquals("original.txt", copy.getFilename());
        copy.setFilename("changed.txt");
        // Original should be unaffected
        assertEquals("original.txt", original.getFilename());
    }

    // ---------------------------------------------------------------------------
    // setNumber / checkCanSetNumber
    // ---------------------------------------------------------------------------

    @Test
    void setNumber_normalCase_setsNumber() {
        SimpleMutableFileMetadata m = new SimpleMutableFileMetadata();
        m.setNumber(5);
        assertEquals(5, m.getNumber());
    }

    @Test
    void setNumber_persistedFileWithExistingNumber_doesNotChange() {
        SimpleMutableFileMetadata m = new SimpleMutableFileMetadata();
        m.setNumber(3);
        m.setReferenceToPersistedFile(true);
        // Attempt to change - should be silently refused (isRef=true, number!=0, newNumber!=oldNumber)
        m.setNumber(7);
        assertEquals(3, m.getNumber());
    }

    @Test
    void setNumber_persistedFileWithZeroNumber_allows() {
        // number is 0 (default), so renumbering should be allowed even if persisted
        SimpleMutableFileMetadata m = new SimpleMutableFileMetadata();
        m.setReferenceToPersistedFile(true);
        m.setNumber(10);
        assertEquals(10, m.getNumber());
    }

    // ---------------------------------------------------------------------------
    // hasError / getErrorMessage / setErrorMessage
    // ---------------------------------------------------------------------------

    @Test
    void hasError_noError_returnsFalse() {
        SimpleMutableFileMetadata m = new SimpleMutableFileMetadata();
        assertFalse(m.hasError());
    }

    @Test
    void hasError_withError_returnsTrue() {
        SimpleMutableFileMetadata m = new SimpleMutableFileMetadata();
        m.setErrorMessage("something went wrong");
        assertTrue(m.hasError());
    }

    @Test
    void getErrorMessage_noError_returnsNull() {
        SimpleMutableFileMetadata m = new SimpleMutableFileMetadata();
        assertNull(m.getErrorMessage());
    }

    @Test
    void getErrorMessage_withError_returnsMessage() {
        SimpleMutableFileMetadata m = new SimpleMutableFileMetadata();
        m.setErrorMessage("upload failed");
        assertEquals("upload failed", m.getErrorMessage());
    }

    // ---------------------------------------------------------------------------
    // setMissingMutableMetadata
    // ---------------------------------------------------------------------------

    @Test
    void setMissingMutableMetadata_fillsBlankFields() {
        SimpleMutableFileMetadata target = new SimpleMutableFileMetadata();
        SimpleMutableFileMetadata source = SimpleMutableFileMetadata.createFromFileNameAndContentType("fallback.pdf", "application/pdf");
        target.setMissingMutableMetadata(source);
        assertEquals("fallback.pdf", target.getFilename());
        assertEquals("application/pdf", target.getContentType());
    }

    @Test
    void setMissingMutableMetadata_doesNotOverwriteExistingFields() {
        SimpleMutableFileMetadata target = SimpleMutableFileMetadata.createFromFileNameAndContentType("original.txt", "text/plain");
        SimpleMutableFileMetadata source = SimpleMutableFileMetadata.createFromFileNameAndContentType("fallback.pdf", "application/pdf");
        target.setMissingMutableMetadata(source);
        assertEquals("original.txt", target.getFilename());
        assertEquals("text/plain", target.getContentType());
    }

    // ---------------------------------------------------------------------------
    // setContentId / setContentLocation / setContentBase
    // ---------------------------------------------------------------------------

    @Test
    void setGetContentId_roundTrips() {
        SimpleMutableFileMetadata m = new SimpleMutableFileMetadata();
        m.setContentId("<msg123@example.com>");
        assertEquals("<msg123@example.com>", m.getContentId());
    }

    @Test
    void setGetContentLocation_roundTrips() {
        SimpleMutableFileMetadata m = new SimpleMutableFileMetadata();
        m.setContentLocation("https://example.com/file.pdf");
        assertEquals("https://example.com/file.pdf", m.getContentLocation());
    }

    @Test
    void setGetContentBase_roundTrips() {
        SimpleMutableFileMetadata m = new SimpleMutableFileMetadata();
        m.setContentBase("https://example.com/");
        assertEquals("https://example.com/", m.getContentBase());
    }

    // ---------------------------------------------------------------------------
    // setResourceType
    // ---------------------------------------------------------------------------

    @Test
    void setGetResourceType_roundTrips() {
        SimpleMutableFileMetadata m = new SimpleMutableFileMetadata();
        m.setResourceType(CommonFileResourceType.OTHER);
        assertEquals(CommonFileResourceType.OTHER, m.getResourceType());
    }

    // ---------------------------------------------------------------------------
    // toReadOnly
    // ---------------------------------------------------------------------------

    @Test
    void toReadOnly_returnsReadOnlyView() {
        SimpleMutableFileMetadata m = SimpleMutableFileMetadata.createFromFileNameAndContentType("report.pdf", "application/pdf");
        FileMetadata readOnly = m.toReadOnly();
        assertNotNull(readOnly);
        assertEquals("report.pdf", readOnly.getFilename());
        assertEquals("application/pdf", readOnly.getContentType());
    }

    @Test
    void toReadOnly_calledTwiceOnSameMutableInstance_returnsDifferentInstances() {
        // Each top-level call to toReadOnly() on the mutable instance constructs a new ReadOnlyView.
        SimpleMutableFileMetadata m = SimpleMutableFileMetadata.createFromFileNameAndContentType("a.txt", "text/plain");
        FileMetadata first = m.toReadOnly();
        FileMetadata second = m.toReadOnly();
        assertNotSame(first, second);
        // But they should be equivalent in terms of the data they expose.
        assertEquals(first.getFilename(), second.getFilename());
        assertEquals(first.getContentType(), second.getContentType());
    }

    @Test
    void toReadOnly_onReadOnlyView_returnsSameInstance() {
        // A ReadOnlyView's own toReadOnly() returns itself (this), unlike the top-level mutable toReadOnly().
        SimpleMutableFileMetadata m = SimpleMutableFileMetadata.createFromFileNameAndContentType("a.txt", "text/plain");
        FileMetadata readOnly = m.toReadOnly();
        assertSame(readOnly, readOnly.toReadOnly());
        assertSame(readOnly.toReadOnly(), readOnly.toReadOnly());
    }

    // ---------------------------------------------------------------------------
    // setReferenceToPersistedFile
    // ---------------------------------------------------------------------------

    @Test
    void setGetReferenceToPersistedFile_roundTrips() {
        SimpleMutableFileMetadata m = new SimpleMutableFileMetadata();
        assertFalse(m.isReferenceToPersistedFile());
        m.setReferenceToPersistedFile(true);
        assertTrue(m.isReferenceToPersistedFile());
    }

    // ---------------------------------------------------------------------------
    // createCopyFromFileInfo
    // ---------------------------------------------------------------------------

    @Test
    void createCopyFromFileInfo_copiesExpectedFields() {
        SimpleMutableFileMetadata source = new SimpleMutableFileMetadata();
        source.setFilename("source.txt");
        source.setDescription("a description");
        source.setContentType("text/plain");
        source.setURI(URI.create("urn:test:source"));
        source.setReferenceToPersistedFile(true);
        source.setNumber(42);
        source.setContentId("<cid@example.com>");
        source.setContentLocation("https://example.com/loc");
        source.setContentBase("https://example.com/");
        // These two are deliberately NOT part of copyTo()'s contract.
        source.setDisplayName("Display Name");
        source.setResourceType(CommonFileResourceType.OTHER);

        TestFileResource fileResource = new TestFileResource("source.txt", "text/plain", new byte[0], source);
        SimpleMutableFileMetadata copy = SimpleMutableFileMetadata.createCopyFromFileInfo(fileResource);

        assertEquals("source.txt", copy.getFilename());
        assertEquals("a description", copy.getDescription());
        assertEquals("text/plain", copy.getContentType());
        assertEquals(URI.create("urn:test:source"), copy.getURI());
        assertTrue(copy.isReferenceToPersistedFile());
        assertEquals(42, copy.getNumber());
        assertEquals("<cid@example.com>", copy.getContentId());
        assertEquals("https://example.com/loc", copy.getContentLocation());
        assertEquals("https://example.com/", copy.getContentBase());
    }

    @Test
    void createCopyFromFileInfo_doesNotCopyDisplayNameOrResourceType() {
        SimpleMutableFileMetadata source = new SimpleMutableFileMetadata();
        source.setFilename("source.txt");
        source.setDisplayName("Display Name");
        source.setResourceType(CommonFileResourceType.OTHER);

        TestFileResource fileResource = new TestFileResource("source.txt", null, new byte[0], source);
        SimpleMutableFileMetadata copy = SimpleMutableFileMetadata.createCopyFromFileInfo(fileResource);

        // displayName falls back to filename when not explicitly set, so it should equal the filename, not "Display Name".
        assertEquals("source.txt", copy.getDisplayName());
        assertNull(copy.getResourceType());
    }

    @Test
    void createCopyFromFileInfo_returnsNewIndependentInstance() {
        TestFileResource fileResource = new TestFileResource("x.txt", "text/plain", new byte[0]);
        SimpleMutableFileMetadata copy1 = SimpleMutableFileMetadata.createCopyFromFileInfo(fileResource);
        SimpleMutableFileMetadata copy2 = SimpleMutableFileMetadata.createCopyFromFileInfo(fileResource);
        assertNotSame(copy1, copy2);
        copy1.setFilename("changed.txt");
        assertEquals("x.txt", copy2.getFilename());
    }

    @Test
    void createCopyFromFileInfo_nullArgument_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> SimpleMutableFileMetadata.createCopyFromFileInfo(null));
    }

    // ---------------------------------------------------------------------------
    // createForIconFileInfo
    // ---------------------------------------------------------------------------

    @Test
    void createForIconFileInfo_setsDisplayNameFromIconFile() {
        byte[] heronBytes = loadHeronBytes();
        HeronIconFileResource heron = new HeronIconFileResource(heronBytes, "heron.jpg", "A Heron Photo");
        SimpleMutableFileMetadata metadata = SimpleMutableFileMetadata.createForIconFileInfo(heron);
        assertEquals("A Heron Photo", metadata.getDisplayName());
        assertEquals("heron.jpg", metadata.getFilename());
    }

    @Test
    void createForIconFileInfo_displayNameFallsBackToFilename_whenIconFileHasNoDisplayName() {
        byte[] heronBytes = loadHeronBytes();
        HeronIconFileResource heron = new HeronIconFileResource(heronBytes, "heron.jpg", null);
        SimpleMutableFileMetadata metadata = SimpleMutableFileMetadata.createForIconFileInfo(heron);
        // setDisplayName(null) was called, so getDisplayName() falls back to the filename.
        assertEquals("heron.jpg", metadata.getDisplayName());
    }

    @Test
    void createForIconFileInfo_populatesByteSizeAndFormattedSize() {
        byte[] heronBytes = loadHeronBytes();
        HeronIconFileResource heron = new HeronIconFileResource(heronBytes, "heron.jpg", "Heron");
        SimpleMutableFileMetadata metadata = SimpleMutableFileMetadata.createForIconFileInfo(heron);

        GetPutProperty props = metadata.asGetPutProperty(heron);
        assertEquals(Long.toString(heronBytes.length), props.getProperty(SimpleMutableFileMetadata.PROP_NAME_BYTESIZE));
        assertEquals(
            NumberFormats.getFormattedByteSize(heronBytes.length),
            props.getProperty(SimpleMutableFileMetadata.PROP_NAME_FORMATTED_SIZE)
        );
    }

    @Test
    void createForIconFileInfo_populatesImageWidthAndHeight() {
        byte[] heronBytes = loadHeronBytes();
        HeronIconFileResource heron = new HeronIconFileResource(heronBytes, "heron.jpg", "Heron");
        SimpleMutableFileMetadata metadata = SimpleMutableFileMetadata.createForIconFileInfo(heron);

        GetPutProperty props = metadata.asGetPutProperty(heron);
        assertEquals("320", props.getProperty(SimpleMutableFileMetadata.PROP_NAME_IMAGE_WIDTH));
        assertEquals("219", props.getProperty(SimpleMutableFileMetadata.PROP_NAME_IMAGE_HEIGHT));
    }

    @Test
    void createForIconFileInfo_nullArgument_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> SimpleMutableFileMetadata.createForIconFileInfo(null));
    }

    // ---------------------------------------------------------------------------
    // createInstanceForUnnamedResource / guessFileExtensionFromContents
    // ---------------------------------------------------------------------------

    @Test
    void createInstanceForUnnamedResource_imageContents_guessesExtensionAndContentType() {
        byte[] heronBytes = loadHeronBytes();
        SimpleMutableFileMetadata m = SimpleMutableFileMetadata.createInstanceForUnnamedResource(
            "photo", null, () -> new ByteArrayInputStream(heronBytes)
        );
        assertNotNull(m.getFilename());
        assertTrue(m.getFilename().startsWith("photo."), "Expected a guessed extension but got: " + m.getFilename());
        assertNotNull(m.getContentType());
        assertTrue(
            m.getContentType().toLowerCase().contains("jp"),
            "Expected a JPEG-ish content type but got: " + m.getContentType()
        );
    }

    @Test
    void createInstanceForUnnamedResource_nonImageContents_fallsBackToOctetStream() {
        byte[] textBytes = "this is plain text, not an image".getBytes();
        SimpleMutableFileMetadata m = SimpleMutableFileMetadata.createInstanceForUnnamedResource(
            "document", null, () -> new ByteArrayInputStream(textBytes)
        );
        assertEquals("document", m.getFilename());
        assertEquals("application/octet-stream", m.getContentType());
    }

    @Test
    void createInstanceForUnnamedResource_contentTypeSupplied_doesNotInspectContents() {
        // Since a usable contentType is supplied up front, guessFileExtensionFromContents should never be invoked,
        // so the (intentionally bogus, non-image) inputSupplier content should be irrelevant.
        SimpleMutableFileMetadata m = SimpleMutableFileMetadata.createInstanceForUnnamedResource(
            "doc", "application/pdf", () -> new ByteArrayInputStream("not actually a pdf".getBytes())
        );
        assertEquals("doc.pdf", m.getFilename());
        assertEquals("application/pdf", m.getContentType());
    }

    @Test
    void createInstanceForUnnamedResource_nullInputSupplier_fallsBackToOctetStream() {
        SimpleMutableFileMetadata m = SimpleMutableFileMetadata.createInstanceForUnnamedResource("doc", null, null);
        assertEquals("doc", m.getFilename());
        assertEquals("application/octet-stream", m.getContentType());
    }

    @Test
    void createInstanceForUnnamedResource_supplierThrowsOnGet_handledGracefully() {
        // guessFileExtensionFromContents() catches Exception broadly; a Supplier whose get() throws should not
        // propagate, and the result should fall back to octet-stream just like the null-input-supplier case.
        SimpleMutableFileMetadata m = SimpleMutableFileMetadata.createInstanceForUnnamedResource(
            "doc", null, () -> {
                throw new RuntimeException("boom");
            }
        );
        assertEquals("doc", m.getFilename());
        assertEquals("application/octet-stream", m.getContentType());
    }

    // ---------------------------------------------------------------------------
    // getExtendedProperties() code paths (via asGetPutProperty)
    // ---------------------------------------------------------------------------

    @Test
    void asGetPutProperty_imageProperty_reflectsOwnAppearsToBeImage_notSuppliedFileResource() {
        // PROP_NAME_IMAGE is gated by the *outer* SimpleMutableFileMetadata's own appearsToBeImage(), based on its
        // own filename/contentType -- not on the supplied FileResource's image-ness.
        SimpleMutableFileMetadata imageMetadata = SimpleMutableFileMetadata.createFromFileNameAndContentType("photo.png", "image/png");
        TestFileResource nonImageFile = new TestFileResource("doc.txt", "text/plain", "hello".getBytes());
        GetPutProperty props = imageMetadata.asGetPutProperty(nonImageFile);
        assertEquals("true", props.getProperty(SimpleMutableFileMetadata.PROP_NAME_IMAGE));

        SimpleMutableFileMetadata nonImageMetadata = SimpleMutableFileMetadata.createFromFileNameAndContentType("doc.txt", "text/plain");
        GetPutProperty props2 = nonImageMetadata.asGetPutProperty(nonImageFile);
        assertEquals("false", props2.getProperty(SimpleMutableFileMetadata.PROP_NAME_IMAGE));
    }

    @Test
    void asGetPutProperty_byteSizeAndFormattedSize_fromSuppliedFileResource() {
        byte[] contents = "hello world".getBytes();
        SimpleMutableFileMetadata metadata = new SimpleMutableFileMetadata();
        TestFileResource fileResource = new TestFileResource("x.txt", "text/plain", contents);
        GetPutProperty props = metadata.asGetPutProperty(fileResource);
        assertEquals(Long.toString(contents.length), props.getProperty(SimpleMutableFileMetadata.PROP_NAME_BYTESIZE));
        assertEquals(
            NumberFormats.getFormattedByteSize(contents.length),
            props.getProperty(SimpleMutableFileMetadata.PROP_NAME_FORMATTED_SIZE)
        );
    }

    @Test
    void asGetPutProperty_nullFileResource_byteSizeIsZero() {
        SimpleMutableFileMetadata metadata = new SimpleMutableFileMetadata();
        GetPutProperty props = metadata.asGetPutProperty(null);
        assertEquals("0", props.getProperty(SimpleMutableFileMetadata.PROP_NAME_BYTESIZE));
        assertEquals(
            NumberFormats.getFormattedByteSize(0),
            props.getProperty(SimpleMutableFileMetadata.PROP_NAME_FORMATTED_SIZE)
        );
    }

    @Test
    void asGetPutProperty_imageWidthHeight_nonImage_areInvalid() {
        SimpleMutableFileMetadata metadata = SimpleMutableFileMetadata.createFromFileNameAndContentType("doc.txt", "text/plain");
        TestFileResource fileResource = new TestFileResource("doc.txt", "text/plain", new byte[0]);
        GetPutProperty props = metadata.asGetPutProperty(fileResource);
        assertEquals("-1", props.getProperty(SimpleMutableFileMetadata.PROP_NAME_IMAGE_WIDTH));
        assertEquals("-1", props.getProperty(SimpleMutableFileMetadata.PROP_NAME_IMAGE_HEIGHT));
    }

    @Test
    void asGetPutProperty_imageWidthHeight_iconFileResource_usesOriginalDimensions() {
        byte[] heronBytes = loadHeronBytes();
        // The outer metadata must itself appear to be an image for imageDimensions() to inspect the file resource.
        SimpleMutableFileMetadata metadata = SimpleMutableFileMetadata.createFromFileNameAndContentType("heron.jpg", "image/jpeg");
        HeronIconFileResource heron = new HeronIconFileResource(heronBytes, "heron.jpg", "Heron");
        GetPutProperty props = metadata.asGetPutProperty(heron);
        assertEquals("320", props.getProperty(SimpleMutableFileMetadata.PROP_NAME_IMAGE_WIDTH));
        assertEquals("219", props.getProperty(SimpleMutableFileMetadata.PROP_NAME_IMAGE_HEIGHT));
    }

    @Test
    void asGetPutProperty_iconWidthHeightUrl_defaultFileIconService_areInvalidOrNull() {
        // In this test environment, FileIconService.get() resolves to FileIconService.NONE (no ServiceLoader
        // registration is present), which always returns an icon with -1/-1 dimensions and a null URL, regardless of
        // the supplied FileResource.
        SimpleMutableFileMetadata metadata = new SimpleMutableFileMetadata();
        TestFileResource fileResource = new TestFileResource("x.txt", "text/plain", new byte[0]);
        GetPutProperty props = metadata.asGetPutProperty(fileResource);
        assertEquals("-1", props.getProperty(SimpleMutableFileMetadata.PROP_NAME_ICON_WIDTH));
        assertEquals("-1", props.getProperty(SimpleMutableFileMetadata.PROP_NAME_ICON_HEIGHT));
        assertNull(props.getProperty(SimpleMutableFileMetadata.PROP_NAME_ICON_URL));
    }

    @Test
    void asGetPutProperty_putComputedProperty_isNoOpAndDoesNotThrow() {
        SimpleMutableFileMetadata metadata = new SimpleMutableFileMetadata();
        TestFileResource fileResource = new TestFileResource("x.txt", "text/plain", new byte[0]);
        GetPutProperty props = metadata.asGetPutProperty(fileResource);
        String beforeByteSize = props.getProperty(SimpleMutableFileMetadata.PROP_NAME_BYTESIZE);
        assertDoesNotThrow(() -> props.putProperty(SimpleMutableFileMetadata.PROP_NAME_BYTESIZE, "999999"));
        // The computed property is unaffected by the attempted put.
        assertEquals(beforeByteSize, props.getProperty(SimpleMutableFileMetadata.PROP_NAME_BYTESIZE));
    }

    @Test
    void asGetPutProperty_putAndGetArbitraryProperty_roundTrips() {
        SimpleMutableFileMetadata metadata = new SimpleMutableFileMetadata();
        TestFileResource fileResource = new TestFileResource("x.txt", "text/plain", new byte[0]);
        GetPutProperty props = metadata.asGetPutProperty(fileResource);
        assertNull(props.getProperty("customProp"));
        props.putProperty("customProp", "customValue");
        assertEquals("customValue", props.getProperty("customProp"));
    }

    @Test
    void asGetPutProperty_getPropertyNames_includesBasePropertiesAndExtendedNames() {
        SimpleMutableFileMetadata metadata = new SimpleMutableFileMetadata();
        TestFileResource fileResource = new TestFileResource("x.txt", "text/plain", new byte[0]);
        GetPutProperty props = metadata.asGetPutProperty(fileResource);
        props.putProperty("customProp", "customValue");
        assertTrue(props.getPropertyNames().contains(SimpleMutableFileMetadata.PROP_NAME_FILE_NAME));
        assertTrue(props.getPropertyNames().contains(SimpleMutableFileMetadata.PROP_NAME_IMAGE));
        assertTrue(props.getPropertyNames().contains(SimpleMutableFileMetadata.PROP_NAME_ICON_URL));
        assertTrue(props.getPropertyNames().contains("customProp"));
    }

}
