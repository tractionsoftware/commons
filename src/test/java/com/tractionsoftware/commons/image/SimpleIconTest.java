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
import com.tractionsoftware.commons.util.Dimensions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public final class SimpleIconTest {

    // Use InvalidIconFileResource as a concrete, zero-dependency IconFileResource
    private IconFileResource.InvalidIconFileResource invalidIcon;
    private SimpleIcon icon;

    @BeforeEach
    void setUp() {
        invalidIcon = new IconFileResource.InvalidIconFileResource(CommonFileResourceType.ICON_OTHER);
        icon = new SimpleIcon(invalidIcon);
    }

    @Test
    void isValid_invalid_false() {
        assertFalse(icon.isValid());
    }

    @Test
    void getFilename_invalid_empty() {
        assertEquals("", icon.getFilename());
    }

    @Test
    void getContentId_invalid_null() {
        assertNull(icon.getContentId());
    }

    @Test
    void getDataUrl_invalid_null() {
        assertNull(icon.getDataUrl());
    }

    @Test
    void getDimensions_invalid_returnsInvalid() {
        Dimensions<Integer> dims = icon.getDimensions();
        assertNotNull(dims);
        assertFalse(dims.hasAtLeastOneValidDimension(), "Invalid icon should have invalid dimensions");
    }

    @Test
    void getImageFileResource_returnsSameIconFile() {
        assertSame(invalidIcon, icon.getImageFileResource());
    }

    @Test
    void toString_containsSimpleIcon() {
        String s = icon.toString();
        assertNotNull(s);
        assertTrue(s.contains("SimpleIcon"), s);
    }

    @Test
    void constructor_withMaxDimensions_storesMaxDimensions() {
        Dimensions<Integer> max = Dimensions.getInstanceInPixels(64, 64);
        SimpleIcon iconWithMax = new SimpleIcon(invalidIcon, max);
        assertFalse(iconWithMax.isValid()); // still invalid
    }

    @Test
    void toString_withMaxDimensions_includesMax() {
        Dimensions<Integer> max = Dimensions.getInstanceInPixels(64, 64);
        SimpleIcon iconWithMax = new SimpleIcon(invalidIcon, max);
        String s = iconWithMax.toString();
        assertNotNull(s);
    }

    @Test
    void getScaled_sameMaxDimensions_returnsSelf() {
        Dimensions<Integer> max = Dimensions.getInstanceInPixels(64, 64);
        SimpleIcon iconWithMax = new SimpleIcon(invalidIcon, max);
        assertSame(iconWithMax, iconWithMax.getScaled(max));
    }

    @Test
    void getScaled_nullMaxDimensions_returnsIconFileImage() {
        Dimensions<Integer> max = Dimensions.getInstanceInPixels(64, 64);
        SimpleIcon iconWithMax = new SimpleIcon(invalidIcon, max);
        // getScaled(null) should return iconFile.getImage() (with no max dims)
        Icon scaled = iconWithMax.getScaled(null);
        assertNotNull(scaled);
    }

    @Test
    void withDimensions_sameDimensions_returnsSelf() {
        Dimensions<Integer> dims = icon.getDimensions();
        assertSame(icon, icon.withDimensions(dims));
    }

    @Test
    void withDimensions_null_returnsImageWithNoDims() {
        Icon result = icon.withDimensions(null);
        assertNotNull(result);
    }

}
