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

package com.tractionsoftware.commons.codec;

import com.tractionsoftware.commons.io.IOUtil;
import com.tractionsoftware.commons.lang.StringUtil;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

public final class Base64Util {

    private static final Logger LOGGER = LoggerFactory.getLogger(Base64Util.class);

    private Base64Util() {
    }

    public static Base64.Decoder getDecoder(boolean mime) {
        if (mime) {
            return Base64.getMimeDecoder();
        }
        return Base64.getDecoder();
    }

    public static Base64.Decoder getMimeDecoder() {
        return getDecoder(true);
    }

    /**
     * Decodes the base-64 data encoded in the given input String.
     *
     * @param encodedStr
     *     the String containing the base-64 encoded representation of the bytes.
     * @return the bytes encoded in the base-64 encoding input String.
     */
    public static byte[] getDecodedBytes(String encodedStr) {
        return getDecodedBytes(encodedStr, false);
    }

    public static byte[] getDecodedBytes(String encodedStr, boolean mime) {
        if (encodedStr == null) {
            return null;
        }
        try {
            return getDecoder(mime).decode(encodedStr.getBytes());
        }
        catch (RuntimeException e) {
            LOGGER.warn(
                "Failed to decode base 64 string value {}", StringUtil.truncatedToStringForLog(encodedStr, 100), e
            );
            return ArrayUtils.EMPTY_BYTE_ARRAY;
        }
    }

    public static byte[] getDecodedBytes(byte[] encoding) {
        return getDecodedBytes(encoding, false);
    }

    public static byte[] getDecodedBytes(byte[] encoding, boolean mime) {
        if (encoding == null) {
            return null;
        }
        try {
            return getDecoder(mime).decode(encoding);
        }
        catch (RuntimeException e) {
            LOGGER.warn("Failed to decode base 64 bytes", e);
            return ArrayUtils.EMPTY_BYTE_ARRAY;
        }
    }

    public static String getDecodedString(byte[] encoding) {
        return getDecodedString(encoding, StandardCharsets.UTF_8, false);
    }

    public static String getDecodedString(byte[] encoding, Charset charset, boolean mime) {
        byte[] bytes = getDecodedBytes(encoding, mime);
        if (bytes == null) {
            return null;
        }
        return new String(bytes, charset);
    }

    /**
     * Creates a String from the UTF-8 encoded bytes represented by the given base-64 encoded String.
     *
     * @param encodedStr
     *     the String containing the base-64 encoded representation of the bytes.
     * @return a String from the UTF-8 encoded bytes represented by the given base-64 encoded String.
     */
    public static String getUtf8DecodedString(String encodedStr) {
        return getUtf8DecodedString(encodedStr, false);
    }

    public static String getUtf8DecodedString(String encodedStr, boolean mime) {
        return getDecodedString(encodedStr, StandardCharsets.UTF_8, mime);
    }
    public static String getDecodedString(String encodedStr, Charset charset, boolean mime) {
        if (encodedStr == null) {
            return null;
        }
        return new String(getDecodedBytes(encodedStr, mime), charset);
    }

    public static void printUtf8DecodedString(String encodedStr, boolean mime, PrintWriter out) {
        printDecodedString(encodedStr, StandardCharsets.UTF_8, mime, out);
    }

    /**
     * Decodes the given String from Base64 and
     *
     * @param encodedStr
     *     representing the Base64 encoding.
     * @param charset
     *     the Charset that should be used to interpret the Base64 bytes.
     * @param mime
     *     whether mime decoding should be used.
     * @param out
     *     to which the decoded String should be written.
     */
    public static void printDecodedString(String encodedStr, Charset charset, boolean mime, Writer out) {
        if (StringUtils.isEmpty(encodedStr)) {
            return;
        }
        try (Reader reader = getDecodingReader(encodedStr, charset, mime)) {
            reader.transferTo(out);
        }
        catch (IOException e) {
            LOGGER.warn("There was a problem attempting to Base64 decode a string ({})", charset, e);
        }
    }

    public static Base64.Encoder getEncoder(int bytesPerLine) {
        if (bytesPerLine <= 0) {
            return Base64.getEncoder();
        }
        return Base64.getMimeEncoder(bytesPerLine, new byte[] { '\n' });
    }

    public static Base64.Encoder getDefaultMimeEncoder() {
        return getEncoder(80);
    }

    /**
     * Creates a base-64 encoded String representation of the input byte array using the default number of bytes per
     * line.
     *
     * @param bytes
     *     the bytes to be encoded.
     * @return the base-64 encoded String representation of the input byte array using the default number of bytes per
     *     line.
     */
    public static byte[] getEncodedBytes(byte[] bytes) {
        if (bytes == null) {
            return null;
        }
        return Base64.getEncoder().encode(bytes);
    }

    public static byte[] getEncodedBytes(byte[] bytes, int bytesPerLine) {
        if (bytes == null) {
            return null;
        }
        return getEncoder(bytesPerLine).encode(bytes);
    }

    public static String getEncodedString(byte[] bytes) {
        if (bytes == null) {
            return null;
        }
        return Base64.getEncoder().encodeToString(bytes);
    }

    /**
     * Creates a base-64 encoded String representation of the input byte array using the requested number of bytes per
     * line.
     *
     * @param bytes
     *     the bytes to be encoded.
     * @param bytesPerLine
     *     the requested number of bytes per line in the output String. After the given number of bytes, a CR/LF
     *     sequence will be inserted. Pass Integer.MAX_VALUE to omit CR/LF sequences entirely. The requested number of
     *     bytes per line may be rounded (e.g., to the nearest multiple of 4).
     * @return the base-64 encoded String representation of the input byte array using requested number of bytes per
     *     line.
     */
    public static String getEncodedString(byte[] bytes, int bytesPerLine) {
        if (bytes == null) {
            return null;
        }
        return getEncoder(bytesPerLine).encodeToString(bytes);
    }

    /**
     * Creates a base-64 encoded String representation of the bytes in the given input String as encoded in the UTF-8
     * character set.
     *
     * @param input
     *     the input String whose UTF-8 bytes are to be encoded in the output String.
     * @return a base-64 encoded String representation of the bytes in the given input String as encoded in the UTF-8
     *     character set.
     */
    public static String getUtf8EncodedString(String input) {
        return getUtf8EncodedString(input, -1);
    }

    public static String getUtf8EncodedString(String input, int bytesPerLine) {
        if (input == null) {
            return null;
        }
        return getEncodedString(input, StandardCharsets.UTF_8, bytesPerLine);
    }

    public static String getEncodedString(String input, Charset charset, int bytesPerLine) {
        return getEncodedString(input.getBytes(charset), bytesPerLine);
    }

    public static void printUtf8EncodedString(String str, int bytesPerLine, PrintWriter out) {
        printEncodedString(str, StandardCharsets.UTF_8, bytesPerLine, out);
    }

    public static void printEncodedString(String str, Charset charset, int bytesPerLine, PrintWriter out) {
        if (str != null) {
            printEncodedString(str.getBytes(charset), bytesPerLine, out);
        }
    }

    public static void printEncodedString(byte[] strBytes, int bytesPerLine, PrintWriter out) {
        if (ArrayUtils.isEmpty(strBytes)) {
            return;
        }
        try (OutputStream outStream = getEncodingOutputStream(bytesPerLine, out)) {
            outStream.write(strBytes);
        }
        catch (IOException e) {
            LOGGER.warn("Failed to encode base 64 bytes", e);
        }
    }

    private static OutputStream getEncodingOutputStream(int bytesPerLine, PrintWriter out) throws IOException {
        return getEncoder(bytesPerLine).wrap(
            IOUtil.getPrintWriterOutputStream(out, false)
        );
    }

    private static InputStream getDecodingInputStream(String encodedStr, Charset charset, boolean mime) {
        return getDecoder(mime).wrap(IOUtil.getStringAsInputStream(encodedStr, charset));
    }

    private static Reader getDecodingReader(String encodedStr, Charset charset, boolean mime) throws IOException {
        return new InputStreamReader(
            getDecodingInputStream(encodedStr, charset, mime), charset
        );
    }

}
