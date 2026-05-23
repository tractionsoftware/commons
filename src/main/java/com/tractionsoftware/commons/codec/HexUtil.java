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

public final class HexUtil {

    /*
     * Not instantiable.
     */
    private HexUtil() {
    }

    private static final char[] HEX_DIGITS = new char[] {
        '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'
    };

    /**
     * Returns a String representing a hex encoding of the bytes. Each byte is encoded with 2 hex chars.
     *
     * @param bytes
     *     the bytes from which the hex encoding is to be produced.
     */
    public static String getEncodedString(byte[] bytes) {

        if (bytes == null) {
            return null;
        }

        if (bytes.length == 0) {
            return "";
        }

        StringBuilder ret = new StringBuilder(bytes.length * 2);
        for (byte b : bytes) {
            int x = (b >> 4) & 0x0f;
            int y = b & 0x0f;
            ret.append(HEX_DIGITS[x]);
            ret.append(HEX_DIGITS[y]);
        }
        return ret.toString();

    }

    /**
     * each byte is decoded from 2 hex chars
     */
    public static byte[] getHexDecodedBytes(String str) {

        if (str == null) {
            return null;
        }
        if (str.isEmpty()) {
            return new byte[0];
        }

        byte[] ret = new byte[str.length() / 2];
        for (int i = 0; i < str.length(); i += 2) {
            byte x = hex2byte(str.charAt(i));
            byte y = hex2byte(str.charAt(i + 1));
            ret[i / 2] = (byte) ((x << 4) + y);
        }
        return ret;

    }

    private static byte hex2byte(char c) {
        if (c <= '9') {  // 0-9
            return (byte) (c - '0');
        }
        if (c <= 'F') {  // A-F
            return (byte) (c - 'A' + 10);
        }
        // a-f
        return (byte) (c - 'a' + 10);
    }

}
