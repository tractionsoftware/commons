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

import com.google.common.collect.ImmutableMap;
import com.tractionsoftware.commons.util.Dimensions;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

import java.io.IOException;
import java.io.InputStream;

/**
 * Helpers for the WebP format.
 *
 * <p>
 * References:
 *
 * <ul>
 * <li>
 * <a href="https://developers.google.com/speed/webp/docs/riff_container">RIFF Container Format</a>
 * </li>
 * <li>
 * <a href="https://developers.google.com/speed/webp/docs/webp_lossless_bitstream_specification">Lossless Bitstream
 * Specification</a>
 * </li>
 * <li>
 * <a href="https://datatracker.ietf.org/doc/html/rfc6386">RFC 6386</a>
 * </li>
 * <li>
 * <a href="https://github.com/drewnoakes/metadata-extractor">drewnoakes / metadata-extractor on GitHub</a>
 * </li>
 * </ul>
 *
 * @author Dave Shepperton
 */
public final class WebPUtil {

    private WebPUtil() {
    }

    private static final Logger LOGGER = LoggerFactory.getLogger(WebPUtil.class);

    private static final int LENGTH_UINT32 = 4;

    private static final int HEADER_LENGTH_WEBP = 8;

    private static final byte[] HEADER_RIFF = new byte[] { 'R', 'I', 'F', 'F' };

    private static final byte[] HEADER_WEBP = new byte[] { 'W', 'E', 'B', 'P' };

    private static final byte[] HEADER_VP8 = new byte[] { 'V', 'P', '8' };

    private static final int HEADER_VP8_OFFSET = 4;

    public static final Dimensions<Integer> getDimensions(InputStream input) throws IOException {

        // "RIFF" block plus file size block
        byte[] data = input.readNBytes(8);

        checkSufficientBytesX("WebP header", HEADER_LENGTH_WEBP, data.length);
        checkRequiredBytesX("RIFF header", HEADER_RIFF, data);

        // "WEBP" block plus "VP8*" block
        data = input.readNBytes(8);
        checkSufficientBytesX("WEBP/VP8 header", HEADER_LENGTH_WEBP, data.length);
        checkRequiredBytesX("WEBP header", HEADER_WEBP, data);
        checkRequiredBytesX("WEBP header", HEADER_VP8, data, HEADER_VP8_OFFSET);

        Format format = getFormat((char) data[7]);

        int chunkSize = getUInt32("Chunk size", input.readNBytes(LENGTH_UINT32), 0);
        Metadata metadata = format.readMetadata(input, chunkSize);
        Dimensions<Integer> result = metadata.getDimensions();
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Decoded WebP image dimensions ({}): {}", metadata.getFormat().getName(), result);
        }
        return result;

    }

    public static final class Metadata {

        private final Format format;

        private final Dimensions<Integer> dimensions;

        private final ImmutableMap<String,Object> otherAttributes;

        private Metadata(Format format, Dimensions<Integer> dimensions, ImmutableMap<String,Object> otherAttributes) {
            this.format = format;
            this.dimensions = dimensions;
            this.otherAttributes = otherAttributes;
        }

        public Format getFormat() {
            return format;
        }

        public final Dimensions<Integer> getDimensions() {
            return dimensions;
        }

        public final ImmutableMap<String,Object> getOtherAttributes() {
            return otherAttributes;
        }

    }

    /**
     * Represents a WebP format, of which there are three.
     *
     * <p>
     * The common header elements for all WebP formats are 20 bytes long, and are as follows:
     *
     * <pre>
     * 1. String "RIFF"
     *  -> 4 bytes, 0 - 3
     *
     * 2. A little-endian 32 bit value of the block length, the whole size of the block controlled by the
     * RIFF header. Normally this equals the payload size (file size minus 8 bytes: 4 bytes for the 'RIFF'
     * identifier and 4 bytes for storing the value itself).
     *  -> 4 byte, 4 - 7
     *
     * 3. String "WEBP" (RIFF container name).
     *  -> 4 bytes, 8 - 11
     *
     * 4. String "VP8*" (chunk tag with one of ' ' [space], 'L' or 'X' in place of the '*').
     *  -> 4 bytes, 12 - 15
     *
     * 5. A little-endian 32 bit value representing the chunk size.
     *  -> 4 bytes, 16 - 19
     * </pre>
     *
     * <p>
     * All of those elements are read in {@link #getDimensions(InputStream)} before invoking
     * {@link Format#readMetadata(InputStream, int)}, so implementations should assume that those 20 bytes have already
     * been read from the {@link InputStream}.
     */
    public static abstract class Format {

        public String toString() {
            return getName();
        }

        /**
         * Returns the name of this format.
         *
         * @return the name of this format.
         */
        public abstract String getName();

        /**
         * Returns the character from the "VP8*" header that corresponds to this format.
         *
         * @return the character from the "VP8*" header that corresponds to this format.
         */
        public abstract char getCode();

        /**
         * Reads the metadata for a stream of data that is encoded in this format.
         *
         * @param input
         *     an {@link InputStream} that contains the data, which will have been advanced past the first 20 bytes, so
         *     that the next byte that will be read is byte 20 (0-indexed, or byte 21 1-indexed).
         * @return the {@link Metadata} for the input interpreted according to this format's specification.
         * @throws IOException
         *     if there is a problem reading from the {@link InputStream} or if the data are not compatible with this
         *     format.
         */
        public final Metadata readMetadata(InputStream input) throws IOException {
            return readMetadata(input, -1);
        }

        /**
         * Reads the metadata for a stream of data that is encoded in this format.
         *
         * @param input
         *     an {@link InputStream} that contains the data, which will have been advanced past the first 20 bytes, so
         *     that the next byte that will be read is byte 20 (0-indexed, or byte 21 1-indexed).
         * @param chunkSize
         *     the chunk size read from the header, if any; -1 otherwise.
         * @return the {@link Metadata} for the input interpreted according to this format's specification.
         * @throws IOException
         *     if there is a problem reading from the {@link InputStream} or if the data are not compatible with this
         *     format.
         */
        public abstract Metadata readMetadata(InputStream input, int chunkSize) throws IOException;

    }

    /**
     * <pre>
     * Simple Format: Lossy
     *
     * After the common header, the lossy format uses the same encoding as a key frame in the VP8 data format as
     * described in RFC 6386, with the assumption that the frame width and height refers to the image's canvas width and
     * height. The VP8 data stream is formatted follows:
     *
     * 1. Common frame tag, with four fields:
     *    1.  A 1-bit frame type (0 for key frames, 1 for interframes).
     *    2.  A 3-bit version number (0 - 3 are defined as four different profiles with different decoding
     *        complexity; other values may be defined for future variants of the VP8 data format).
     *    3.  A 1-bit show_frame flag (0 when current frame is not for display, 1 when current frame is for
     *        display).
     *    4.  A 19-bit field containing the size of the first data partition in bytes.
     *  -> 3 bytes, 20 - 22
     *
     * 2. Start Code bytes: 0x9D, 0x01, 0x2A.
     *  -> 3 bytes, 23 - 25
     *
     * 8. Horizontal scale and width: (2 bits Horizontal Scale << 14) | Width (14 bits).
     *  -> 2 bytes, 26 - 27
     *
     * 9. Vertical scale and height:  (2 bits Vertical Scale << 14) | Height (14 bits)
     *  -> 2 bytes, 28 - 29
     * </pre>
     */
    static final Format LOSSY = new Format() {

        private static final int HEADER_LENGTH = 10;

        private static final byte[] START_CODE_BYTES = new byte[] { (byte) 0x9D, 0x01, 0x2A };

        private static final int getWebPWidthFieldVP8Lossy(byte[] data) {
            return getDimensionFieldVPC8KeyFrame(data, 6);
        }

        private static final int getWebPHeightFieldVP8Lossy(byte[] data) {
            return getDimensionFieldVPC8KeyFrame(data, 8);
        }

        private static final int getDimensionFieldVPC8KeyFrame(byte[] data, int index) {
            return (data[index + 1] & 0xFF) | ((data[index] & 0x3F) << 8);
        }

        @Override
        public final String getName() {
            return "Lossy";
        }

        @Override
        public final char getCode() {
            return ' ';
        }

        @Override
        public final Metadata readMetadata(InputStream input, int chunkSize) throws IOException {

            if (chunkSize >= 0) {
                checkSufficientBytesX("WebP lossy format header", HEADER_LENGTH, chunkSize);
            }

            byte[] data = input.readNBytes(HEADER_LENGTH);

            checkSufficientBytesX("WebP lossy format header", HEADER_LENGTH, data.length);
            checkRequiredBytesX("WebP lossy format start code bytes", START_CODE_BYTES, data, 3);

            int width = getWebPWidthFieldVP8Lossy(data);
            int height = getWebPHeightFieldVP8Lossy(data);
            Dimensions<Integer> dimensions = Dimensions.getInstanceInPixels(width, height);

            return new Metadata(this, dimensions, ImmutableMap.of());

        }

    };

    /**
     * <pre>
     * Simple Format: Lossless
     *
     * After the common header, the data are formatted follows:
     *
     * 1. One byte signature 0x2f.
     *  -> 1 byte, 20
     *
     * 2. The VP8 bitstream data. The first 28 bits of the bitstream specify the width and height of the image. Width
     * and height are decoded as 14-bit integers as follows:
     *     int image_width = ReadBits(14) + 1;
     *     int image_height = ReadBits(14) + 1;
     * The 14-bit precision for image width and height limits the maximum size of a WebP lossless image to
     * 16384✕16384 pixels.
     * </pre>
     */
    static final Format LOSSLESS = new Format() {

        private static final int getWebPWidthFieldVP8Lossless(byte[] data) {
            return getIntFromUInt8(data, 1, 0xFF, 0) | getIntFromUInt8(data, 2, 0x3F, 8);
        }

        private static final int getWebPHeightFieldVP8Lossless(byte[] data) {
            return getIntFromUInt8(data, 2, 0xC0, -6) |
                   getIntFromUInt8(data, 3, 0xFF, 2) |
                   getIntFromUInt8(data, 4, 0x0F, 10);
        }

        @Override
        public final String getName() {
            return "Lossless";
        }

        @Override
        public final char getCode() {
            return 'L';
        }

        @Override
        public final Metadata readMetadata(InputStream input, int chunkSize) throws IOException {

            // 1 B for signature, 4 bytes for width and height info.
            byte[] data = input.readNBytes(5);
            if (data[0] != 0x2F) {
                throw new IOException("Missing signature byte for lossless WebP format.");
            }

            Dimensions<Integer> dimensions = Dimensions.getInstanceInPixels(
                1 + getWebPWidthFieldVP8Lossless(data),
                1 + getWebPHeightFieldVP8Lossless(data)
            );
            return new Metadata(this, dimensions, ImmutableMap.of());

        }

    };

    /**
     * <pre>
     * Extended Format
     *
     * After the common header, the data are formatted follows:
     *
     * 1. One byte with bit flags.
     *  -> 1 byte, 20
     *
     * 2. Three bytes, reserved.
     *  -> 3 bytes, 21 - 23
     *
     * 3. Canvas Width Minus One: 24 bits. 1-based width of the canvas in pixels. The actual canvas width is
     * 1 + Canvas Width Minus One
     *  -> 3 bytes, 24 - 26
     *
     * 4. Canvas Height Minus One: 24 bits. 1-based height of the canvas in pixels. The actual canvas height
     * is 1 + Canvas Height Minus One.
     *  -> 3 bytes, 27 - 29
     * </pre>
     */
    static final Format EXTENDED = new Format() {

        private static final int CHUNK_SIZE_METADATA = 6;

        private static final int getWebPWidthFieldVP8Extended(byte[] data) throws IOException {
            // byte 4 in the chunk is where the width field starts
            return getUInt24("WebP width field (VP8 extended)", data, 4);
        }

        private static final int getWebPHeightFieldVP8Extended(byte[] data) throws IOException {
            // byte 7 in the chunk is where the height field starts
            return getUInt24("WebP height field (VP8 extended)", data, 7);
        }

        @Override
        public final String getName() {
            return "Extended";
        }

        @Override
        public final char getCode() {
            return 'X';
        }

        @Override
        public final Metadata readMetadata(InputStream input, int chunkSize) throws IOException {
            if (chunkSize >= 0) {
                checkSufficientBytesX("WebP extended format metadata", CHUNK_SIZE_METADATA, chunkSize);
            }
            byte[] data = input.readNBytes(chunkSize);
            checkSufficientBytesX("WebP extended format metadata", CHUNK_SIZE_METADATA, data.length);
            int width = 1 + getWebPWidthFieldVP8Extended(data);
            int height = 1 + getWebPHeightFieldVP8Extended(data);
            if (((long) width) * ((long) height) >= 0x100000000L) {
                throw new IOException(
                    "WebP extended format dimensions (" + width + "x" + height + ") invalid (too large)."
                );
            }
            return new Metadata(this, Dimensions.getInstanceInPixels(width, height), ImmutableMap.of());
        }

    };

    static Format getFormat(char code) throws IOException {
        return switch (code) {
            case ' ' -> LOSSY;
            case 'L' -> LOSSLESS;
            case 'X' -> EXTENDED;
            default -> throw new IOException(
                "Unrecognized WebP VP8 format code '" + code + "' (" + Integer.toHexString(code) + ")."
            );
        };
    }

    private static final short getUInt8(String desc, byte[] data, int index) throws IOException {
        if (data.length < index) {
            throw new IOException(String.format("Insufficient data for %s.", desc));
        }
        return (short) (data[index] & 0xFF);
    }

    private static final int getUInt24(String desc, byte[] data, int offset) throws IOException {
        if (data.length < offset + 2) {
            throw new IOException(String.format("Insufficient data for %s.", desc));
        }
        return (((int) data[offset + 2]) << 16 & 0xFF0000) |
               (((int) data[offset + 1]) << 8 & 0xFF00) |
               (((int) data[offset]) & 0xFF);
    }

    private static final int getUInt32(String desc, byte[] data, int index) throws IOException {
        if (data.length < index + 3) {
            throw new IOException(String.format("Insufficient data for %s.", desc));
        }
        return (((int) data[index + 3]) << 24 & 0xFF000000) |
               (((int) data[index + 2]) << 16 & 0xFF0000) |
               (((int) data[index + 1]) << 8 & 0xFF00) |
               (((int) data[index]) & 0xFF);
    }

    private static final int getIntFromUInt8(byte[] data, int index, int mask, int lShift) {
        int v = (data[index] & mask);
        if (lShift == 0) {
            return v;
        }
        if (lShift > 1) {
            return v << lShift;
        }
        return v >> -lShift;
    }

    private static final void checkSufficientBytesX(String desc, int requiredLength, int actualLength)
        throws IOException {
        if (actualLength < requiredLength) {
            throw new IOException(
                String.format("Insufficient bytes for %s (%d < %d).", desc, actualLength, requiredLength)
            );
        }
    }

    private static final void checkRequiredBytesX(String desc, byte[] requiredData, byte[] actualData)
        throws IOException {
        checkRequiredBytesX(desc, requiredData, actualData, 0);
    }

    private static final void checkRequiredBytesX(String desc, byte[] requiredData, byte[] actualData, int offset)
        throws IOException {
        int len = requiredData.length;
        for (int i = 0; i < len; i++) {
            if (requiredData[i] != actualData[i + offset]) {
                throw new IOException(String.format("Missing/invalid %s.", desc));
            }
        }
    }

}
