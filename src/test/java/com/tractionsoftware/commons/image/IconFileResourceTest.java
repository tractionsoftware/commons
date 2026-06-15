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

package com.tractionsoftware.commons.image;

import com.tractionsoftware.commons.io.CommonFileResourceType;
import com.tractionsoftware.commons.io.FileResourceType;
import com.tractionsoftware.commons.util.Dimensions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URI;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

public final class IconFileResourceTest {

    private static IconFileResource.InvalidIconFileResource createInvalidInstance() {
        return new IconFileResource.InvalidIconFileResource(CommonFileResourceType.OTHER);
    }

    // InvalidIconFileResource

    @Test
    void invalid_isValid_false() {
        assertFalse(createInvalidInstance().isValid());
    }

    @Test
    void invalid_isDirectory_false() {
        assertFalse(createInvalidInstance().isDirectory());
    }

    @Test
    void invalid_getFilename_empty() {
        assertEquals("", createInvalidInstance().getFilename());
    }

    @Test
    void invalid_getPath_empty() {
        assertEquals("", createInvalidInstance().getPath());
    }

    @Test
    void invalid_getURI_iconInvalid() {
        URI uri = createInvalidInstance().getURI();
        assertNotNull(uri);
        assertEquals("icon:invalid", uri.toString());
    }

    @Test
    void invalid_getByteSize_zero() {
        assertEquals(0L, createInvalidInstance().getByteSize());
    }

    @Test
    void invalid_getLastModified_epochZero() {
        Date d = createInvalidInstance().getLastModified();
        assertNotNull(d);
        assertEquals(0L, d.getTime());
    }

    @Test
    void invalid_getContentType_null() {
        assertNull(createInvalidInstance().getContentType());
    }

    @Test
    void invalid_getContentId_null() {
        assertNull(createInvalidInstance().getContentId());
    }

    @Test
    void invalid_getDisplayName_null() {
        assertNull(createInvalidInstance().getDisplayName());
    }

    @Test
    void invalid_getDescription_null() {
        assertNull(createInvalidInstance().getDescription());
    }

    @Test
    void invalid_toDebugString_none() {
        assertEquals("NONE", createInvalidInstance().toDebugString());
    }

    @Test
    void invalid_getInputStream_throwsFileNotFoundException() {
        assertThrows(IOException.class, () -> createInvalidInstance().getInputStream().close());
    }

    @Test
    void invalid_getType_returnsResourceType() {
        FileResourceType type = createInvalidInstance().getType();
        assertSame(CommonFileResourceType.OTHER, type);
    }

    @Test
    void invalid_getImageResourceType_returnsResourceType() {
        FileResourceType type = createInvalidInstance().getImageResourceType();
        assertSame(CommonFileResourceType.OTHER, type);
    }

    @Test
    void invalid_isText_false() {
        assertFalse(createInvalidInstance().isText());
    }

    @Test
    void invalid_isPlainText_false() {
        assertFalse(createInvalidInstance().isPlainText());
    }

    @Test
    void invalid_isHtml_false() {
        assertFalse(createInvalidInstance().isHtml());
    }

    @Test
    void invalid_getOriginalDimensions_Minus1Square() {
        Dimensions<Integer> dimensions = createInvalidInstance().getOriginalDimensions();
        assertEquals(-1, dimensions.getWidth());
        assertEquals(-1, dimensions.getHeight());
    }

    @Test
    void invalid_getDimensionsWithMax_null() {
        assertNull(createInvalidInstance().getDimensions(Dimensions.getInstanceInPixels(100, 100)));
    }

    @Test
    void invalid_getImage_returnsSimpleIcon() {
        Icon img = createInvalidInstance().getImage();
        assertNotNull(img);
        assertFalse(img.isValid());
    }

    @Test
    void invalid_getImageWithMax_returnsSimpleIcon() {
        Icon img = createInvalidInstance().getImage(Dimensions.getInstanceInPixels(64, 64));
        assertNotNull(img);
        assertFalse(img.isValid());
    }

    @Test
    void differentResourceTypes_differentObjects() {
        var imageIcon = new IconFileResource.InvalidIconFileResource(CommonFileResourceType.OTHER);
        var docIcon = new IconFileResource.InvalidIconFileResource(CommonFileResourceType.ICON_FILE_TYPE);
        assertNotSame(imageIcon, docIcon);
        assertEquals(CommonFileResourceType.OTHER, imageIcon.getType());
        assertEquals(CommonFileResourceType.ICON_FILE_TYPE, docIcon.getType());
    }

}
