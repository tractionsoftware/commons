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
import java.io.IOException;
import java.io.InputStream;

import static org.junit.jupiter.api.Assertions.*;

public final class WebPUtilTest {

    // =====================================================================
    // Sample image: monalisa-100x100.webp (100x100 pixels)
    // =====================================================================

    @Test
    void getDimensions_sampleImage_returns100x100() throws IOException {
        try (InputStream is = ImageUtilTest.imageFileStream("monalisa-100x100.webp")) {
            Dimensions<Integer> dims = WebPUtil.getDimensions(is);
            assertNotNull(dims);
            assertEquals(100, dims.getWidth().intValue(), "Width should be 100");
            assertEquals(100, dims.getHeight().intValue(), "Height should be 100");
        }
    }

    // =====================================================================
    // Error cases
    // =====================================================================

    @Test
    void getDimensions_missingRiffHeader_throwsIOException() {
        byte[] notWebP = new byte[30];
        assertThrows(IOException.class, () ->
            WebPUtil.getDimensions(new ByteArrayInputStream(notWebP))
        );
    }

    @Test
    void getDimensions_hasRiffButMissingWebp_throwsIOException() {
        // "RIFF" + 4 bytes size, then garbage (not "WEBP")
        byte[] data = new byte[30];
        data[0] = 'R'; data[1] = 'I'; data[2] = 'F'; data[3] = 'F';
        // size bytes, then NOT "WEBP"
        data[8] = 'X'; data[9] = 'X'; data[10] = 'X'; data[11] = 'X';
        assertThrows(IOException.class, () ->
            WebPUtil.getDimensions(new ByteArrayInputStream(data))
        );
    }

    @Test
    void getDimensions_empty_throwsIOException() {
        assertThrows(IOException.class, () ->
            WebPUtil.getDimensions(new ByteArrayInputStream(new byte[0]))
        );
    }

    // =====================================================================
    // getFormat
    // =====================================================================

    @Test
    void getFormat_space_returnsLossy() throws IOException {
        WebPUtil.Format f = WebPUtil.getFormat(' ');
        assertNotNull(f);
        assertEquals("Lossy", f.getName());
        assertEquals(' ', f.getCode());
        assertEquals("Lossy", f.toString());
    }

    @Test
    void getFormat_L_returnsLossless() throws IOException {
        WebPUtil.Format f = WebPUtil.getFormat('L');
        assertNotNull(f);
        assertEquals("Lossless", f.getName());
        assertEquals('L', f.getCode());
    }

    @Test
    void getFormat_X_returnsExtended() throws IOException {
        WebPUtil.Format f = WebPUtil.getFormat('X');
        assertNotNull(f);
        assertEquals("Extended", f.getName());
        assertEquals('X', f.getCode());
    }

    @Test
    void getFormat_unknownCode_throwsIOException() {
        assertThrows(IOException.class, () -> WebPUtil.getFormat('Z'));
    }

    // =====================================================================
    // LOSSY format direct test
    // =====================================================================

    @Test
    void lossy_format_constants() {
        assertEquals(' ', WebPUtil.LOSSY.getCode());
        assertEquals("Lossy", WebPUtil.LOSSY.getName());
    }

    @Test
    void lossy_readMetadata_missingStartCode_throwsIOException() {
        // 10 bytes, but bytes 3,4,5 are not 0x9D,0x01,0x2A
        byte[] bad = new byte[10];
        assertThrows(IOException.class, () ->
            WebPUtil.LOSSY.readMetadata(new ByteArrayInputStream(bad), bad.length)
        );
    }

    @Test
    void lossy_readMetadata_validData_returnsDimensions() throws IOException {
        // Construct a minimal valid VP8 lossy frame header (10 bytes after chunk size):
        // bytes[0..2] = frame tag (3 bytes)
        // bytes[3..5] = start code 0x9D, 0x01, 0x2A
        // bytes[6..7] = width (14-bit): 99 pixels → width field = 99
        // bytes[8..9] = height (14-bit): 99 pixels → height field = 99
        // width = data[6] & 0x3F | (data[7] & 0xFF) << 8
        // For width=99: 99 = 0x63 → data[6]=0x63, data[7]=0x00
        byte[] frame = new byte[] {
            0, 0, 0, (byte) 0x9d, 0x01, 0x2a, 0, 99, 0, 99
        };

        WebPUtil.Metadata meta = WebPUtil.LOSSY.readMetadata(new ByteArrayInputStream(frame), frame.length);
        assertNotNull(meta);
        assertEquals(99, meta.getDimensions().getWidth().intValue());
        assertEquals(99, meta.getDimensions().getHeight().intValue());
    }

    // =====================================================================
    // LOSSLESS format
    // =====================================================================

    @Test
    void lossless_format_constants() {
        assertEquals('L', WebPUtil.LOSSLESS.getCode());
        assertEquals("Lossless", WebPUtil.LOSSLESS.getName());
    }

    @Test
    void lossless_readMetadata_missingSignature_throwsIOException() {
        // First byte must be 0x2F
        byte[] bad = new byte[5];
        bad[0] = 0x00; // wrong signature
        assertThrows(IOException.class, () ->
            WebPUtil.LOSSLESS.readMetadata(new ByteArrayInputStream(bad), bad.length)
        );
    }

    // =====================================================================
    // EXTENDED format
    // =====================================================================

    @Test
    void extended_format_constants() {
        assertEquals('X', WebPUtil.EXTENDED.getCode());
        assertEquals("Extended", WebPUtil.EXTENDED.getName());
    }

    @Test
    void extended_readMetadata_tooSmallExplicitChunkSize_throwsIOException() {
        byte[] data = new byte[10];
        assertThrows(IOException.class, () ->
            WebPUtil.EXTENDED.readMetadata(new ByteArrayInputStream(data), 5)
        );
    }

    @Test
    void extended_readMetadata_tooSmallImplicitChunkSize_throwsIOException() {
        byte[] data = new byte[5];
        assertThrows(IOException.class, () ->
            WebPUtil.EXTENDED.readMetadata(new ByteArrayInputStream(data), data.length)
        );
    }

    @Test
    void extended_readMetadata_validData_returnsDimensions() throws IOException {
        // Extended header after chunk size (chunkSize >= 6):
        // byte[0] = flags
        // byte[1..3] = reserved
        // byte[4..6] = Canvas Width Minus One (24-bit LE): e.g., 49 (width = 50)
        // byte[7..9] = Canvas Height Minus One (24-bit LE): e.g., 49 (height = 50)
        // getUInt24(data, 4): = data[4] & 0xFF | data[5]<<8&0xFF00 | data[6]<<16&0xFF0000
        int chunkSize = 10;
        byte[] data = new byte[chunkSize];
        // Canvas Width Minus One = 49 → stored at indices 4,5,6 as little-endian
        data[4] = 49; data[5] = 0; data[6] = 0;
        // Canvas Height Minus One = 49
        data[7] = 49; data[8] = 0; data[9] = 0;

        WebPUtil.Metadata meta = WebPUtil.EXTENDED.readMetadata(new ByteArrayInputStream(data), data.length);
        assertEquals(50, meta.getDimensions().getWidth().intValue());
        assertEquals(50, meta.getDimensions().getHeight().intValue());
    }

}
