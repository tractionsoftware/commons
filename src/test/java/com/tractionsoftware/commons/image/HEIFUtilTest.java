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

import com.tractionsoftware.commons.util.Dimensions;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;

import static org.junit.jupiter.api.Assertions.*;

public final class HEIFUtilTest {

    @Test
    void getDimensions_sampleImage_returns320x480() throws IOException, URISyntaxException {
        File file = ImageUtilTest.imageFile("fiat-500-320x480.heic");
//        try (InputStream is = ImageUtilTest.imageFileStream("fiat-500-320x480.heic")) {
//            Dimensions<Integer> dims = HEIFUtil.getDimensions(is);
            Dimensions<Integer> dims = HEIFUtil.getDimensions(file);
            assertNotNull(dims);
            assertEquals(320, dims.getWidth().intValue(), "Width should be 320");
            assertEquals(480, dims.getHeight().intValue(), "Height should be 480");
//        }
    }

    @Test
    void getDimensions_emptyStream_throwsException() {
        assertThrows(Exception.class, () ->
            HEIFUtil.getDimensions(new ByteArrayInputStream(new byte[0]))
        );
    }

    @Test
    void getDimensions_notHeif_throwsException() {
        byte[] garbage = new byte[64]; // not a valid HEIF file
        assertThrows(Exception.class, () ->
            HEIFUtil.getDimensions(new ByteArrayInputStream(garbage))
        );
    }

}
