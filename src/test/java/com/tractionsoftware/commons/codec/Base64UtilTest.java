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
import com.tractionsoftware.commons.io.StringWriteUtil;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

public final class Base64UtilTest {

    private static final String japanese_utf8() {
        try (InputStream input = Base64UtilTest.class.getResourceAsStream("japanese.utf8")) {
            Assertions.assertNotNull(input, "file input");
            return IOUtil.readContent(input, StandardCharsets.UTF_8);
        }
        catch (IOException e) {
            Assertions.fail(e);
            return "";
        }
    }

    private static final byte[] japanese_bytes() {
        try (InputStream input = Base64UtilTest.class.getResourceAsStream("japanese.utf8")) {
            Assertions.assertNotNull(input, "file input");
            return IOUtil.readContentBytes(input);
        }
        catch (IOException e) {
            Assertions.fail(e);
            return new byte[0];
        }
    }

    private static final String MESSAGE_1 = "This is a short and simple message.";

    private static final String MESSAGE_1_ENCODED = "VGhpcyBpcyBhIHNob3J0IGFuZCBzaW1wbGUgbWVzc2FnZS4=";

    private static final String MESSAGE_2 =
        "This is a much longer input string than in the first example.  It's not specifically important that it contain particular runs of text, but just that it exercises the code a bit.\nIt also has a line break.";

    private static final String MESSAGE_2_ENCODED =
        "VGhpcyBpcyBhIG11Y2ggbG9uZ2VyIGlucHV0IHN0cmluZyB0aGFuIGluIHRoZSBmaXJzdCBleGFtcGxlLiAgSXQncyBub3Qgc3BlY2lmaWNhbGx5IGltcG9ydGFudCB0aGF0IGl0IGNvbnRhaW4gcGFydGljdWxhciBydW5zIG9mIHRleHQsIGJ1dCBqdXN0IHRoYXQgaXQgZXhlcmNpc2VzIHRoZSBjb2RlIGEgYml0LgpJdCBhbHNvIGhhcyBhIGxpbmUgYnJlYWsu";

    private static final String MESSAGE_2_ENCODED_MIME = """
        VGhpcyBpcyBhIG11Y2ggbG9uZ2VyIGlucHV0IHN0cmluZyB0aGFuIGluIHRoZSBmaXJzdCBleGFtcGxl
        LiAgSXQncyBub3Qgc3BlY2lmaWNhbGx5IGltcG9ydGFudCB0aGF0IGl0IGNvbnRhaW4gcGFydGljdWxh
        ciBydW5zIG9mIHRleHQsIGJ1dCBqdXN0IHRoYXQgaXQgZXhlcmNpc2VzIHRoZSBjb2RlIGEgYml0LgpJ
        dCBhbHNvIGhhcyBhIGxpbmUgYnJlYWsu""";

    private static final String MESSAGE_3 =
        "こんにちは! 私の名前は Dave です。これは、一部のテキスト ユーティリティでより幅広い Unicode 文字が完全にサポートされているかどうかをテストするために使用できる日本語のメッセージです。";

    private static final String MESSAGE_3_ENCODED =
        "44GT44KT44Gr44Gh44GvISDnp4Hjga7lkI3liY3jga8gRGF2ZSDjgafjgZnjgILjgZPjgozjga/jgIHkuIDpg6jjga7jg4bjgq3jgrnjg4gg44Om44O844OG44Kj44Oq44OG44Kj44Gn44KI44KK5bmF5bqD44GEIFVuaWNvZGUg5paH5a2X44GM5a6M5YWo44Gr44K144Od44O844OI44GV44KM44Gm44GE44KL44GL44Gp44GG44GL44KS44OG44K544OI44GZ44KL44Gf44KB44Gr5L2/55So44Gn44GN44KL5pel5pys6Kqe44Gu44Oh44OD44K744O844K444Gn44GZ44CC";

    private static final String MESSAGE_3_ENCODED_MIME = """
        44GT44KT44Gr44Gh44GvISDnp4Hjga7lkI3liY3jga8gRGF2ZSDjgafjgZnjgILjgZPjgozjga/jgIHk
        uIDpg6jjga7jg4bjgq3jgrnjg4gg44Om44O844OG44Kj44Oq44OG44Kj44Gn44KI44KK5bmF5bqD44GE
        IFVuaWNvZGUg5paH5a2X44GM5a6M5YWo44Gr44K144Od44O844OI44GV44KM44Gm44GE44KL44GL44Gp
        44GG44GL44KS44OG44K544OI44GZ44KL44Gf44KB44Gr5L2/55So44Gn44GN44KL5pel5pys6Kqe44Gu
        44Oh44OD44K744O844K444Gn44GZ44CC""";


    private static final String getPrintedEncodedString(String str) {
        return StringWriteUtil.getPrintedString(out -> Base64Util.printUtf8EncodedString(str, -1, out));
    }

    private static final String getPrintedMimeEncodedString(String str) {
        return StringWriteUtil.getPrintedString(out -> Base64Util.printUtf8EncodedString(str, 80, out));
    }

    private static final String getPrintedDecodedString(String str) {
        return StringWriteUtil.getPrintedString(out -> Base64Util.printUtf8DecodedString(str, false, out));
    }

    private static final String getPrintedMimeDecodedString(String str) {
        return StringWriteUtil.getPrintedString(out -> Base64Util.printUtf8DecodedString(str, true, out));
    }

    @Test
    public void test_getEncodedStringNull() {
        Assertions.assertNull(Base64Util.getUtf8EncodedString(null));
    }

    @Test
    public void test_getEncodedString1() {
        Assertions.assertEquals(MESSAGE_1_ENCODED, Base64Util.getUtf8EncodedString(MESSAGE_1));
    }

    @Test
    public void test_getEncodedString2() {
        Assertions.assertEquals(MESSAGE_2_ENCODED, Base64Util.getUtf8EncodedString(MESSAGE_2));
    }

    @Test
    public void test_getEncodedString3() {
        Assertions.assertEquals(MESSAGE_3_ENCODED, Base64Util.getUtf8EncodedString(MESSAGE_3));
    }

    @Test
    public void test_getMimeEncodedString1() {
        Assertions.assertEquals(
            MESSAGE_1_ENCODED /* same for mime and non-mime versions */, Base64Util.getUtf8EncodedString(MESSAGE_1, 80)
        );
    }

    @Test
    public void test_getMimeEncodedString2() {
        Assertions.assertEquals(MESSAGE_2_ENCODED_MIME, Base64Util.getUtf8EncodedString(MESSAGE_2, 80));
    }

    @Test
    public void test_getMimeEncodedString3() {
        Assertions.assertEquals(MESSAGE_3_ENCODED_MIME, Base64Util.getUtf8EncodedString(MESSAGE_3, 80));
    }

    @Test
    public void test_printEncodedStringNull() {
        Assertions.assertEquals("", getPrintedEncodedString(null));
    }

    @Test
    public void test_printEncodedString1() {
        Assertions.assertEquals(MESSAGE_1_ENCODED, getPrintedEncodedString(MESSAGE_1));
    }

    @Test
    public void test_printEncodedString2() {
        Assertions.assertEquals(MESSAGE_2_ENCODED, getPrintedEncodedString(MESSAGE_2));
    }

    @Test
    public void test_printEncodedString3() {
        Assertions.assertEquals(MESSAGE_3_ENCODED, getPrintedEncodedString(MESSAGE_3));
    }

    @Test
    public void test_printMimeEncodedString1() {
        Assertions.assertEquals(
            MESSAGE_1_ENCODED, // same for mime and non-mime versions
            getPrintedMimeEncodedString(MESSAGE_1)
        );
    }

    @Test
    public void test_printMimeEncodedString2() {
        Assertions.assertEquals(MESSAGE_2_ENCODED_MIME, getPrintedMimeEncodedString(MESSAGE_2));
    }

    @Test
    public void test_printMimeEncodedString3() {
        Assertions.assertEquals(MESSAGE_3_ENCODED_MIME, getPrintedMimeEncodedString(MESSAGE_3));
    }

    @Test
    public void test_getDecodedStringNull() {
        Assertions.assertNull(Base64Util.getUtf8DecodedString(null));
    }

    @Test
    public void test_getDecodedString1() {
        Assertions.assertEquals(MESSAGE_1, Base64Util.getUtf8DecodedString(MESSAGE_1_ENCODED));
    }

    @Test
    public void test_getDecodedString2() {
        Assertions.assertEquals(MESSAGE_2, Base64Util.getUtf8DecodedString(MESSAGE_2_ENCODED));
    }

    @Test
    public void test_getDecodedString3() {
        Assertions.assertEquals(MESSAGE_3, Base64Util.getUtf8DecodedString(MESSAGE_3_ENCODED));
    }

    @Test
    public void test_getMimeDecodedString1() {
        Assertions.assertEquals(
            MESSAGE_1,
            Base64Util.getUtf8DecodedString(MESSAGE_1_ENCODED /* same for mime and non-mime versions */, true)
        );
    }

    @Test
    public void test_getMimeDecodedString2() {
        Assertions.assertEquals(MESSAGE_2, Base64Util.getUtf8DecodedString(MESSAGE_2_ENCODED_MIME, true));
    }

    @Test
    public void test_getMimeDecodedString3() {
        Assertions.assertEquals(MESSAGE_3, Base64Util.getUtf8DecodedString(MESSAGE_3_ENCODED_MIME, true));
    }

    @Test
    public void test_printDecodedStringNull() {
        Assertions.assertEquals("", getPrintedDecodedString(null));
    }

    @Test
    public void test_printDecodedString1() {
        Assertions.assertEquals(MESSAGE_1, getPrintedDecodedString(MESSAGE_1_ENCODED));
    }

    @Test
    public void test_printDecodedString2() {
        Assertions.assertEquals(MESSAGE_2, getPrintedDecodedString(MESSAGE_2_ENCODED));
    }

    @Test
    public void test_printDecodedString3() {
        Assertions.assertEquals(MESSAGE_3, getPrintedDecodedString(MESSAGE_3_ENCODED));
    }

    @Test
    public void test_printMimeDecodedString1() {
        Assertions.assertEquals(
            MESSAGE_1,
            getPrintedMimeDecodedString(MESSAGE_1_ENCODED /* same for mime and non-mime versions */)
        );
    }

    @Test
    public void test_printMimeDecodedString2() {
        Assertions.assertEquals(MESSAGE_2, getPrintedMimeDecodedString(MESSAGE_2_ENCODED_MIME));
    }

    @Test
    public void test_printMimeDecodedString3() {
        Assertions.assertEquals(MESSAGE_3, getPrintedMimeDecodedString(MESSAGE_3_ENCODED_MIME));
    }

    @Test
    public void test_ja_symmetricEncodeDecodeString() {
        String start = japanese_utf8();
        String base64 = Base64Util.getUtf8EncodedString(start);
        String end = Base64Util.getUtf8DecodedString(base64);
        Assertions.assertEquals(start, end);
    }

    @Test
    public void test_ja_getEncodedString() {
        byte[] asBytes = japanese_bytes();
        String actual = Base64Util.getEncodedString(asBytes, Integer.MAX_VALUE);
        String expected = "5LmF5L+d";
        Assertions.assertEquals(expected, actual);
    }

    @Test
    public void test_ja_getEncodedBytes() {
        byte[] actual = Base64Util.getDecodedBytes("5LmF5L+d");
        byte[] expected = japanese_bytes();
        Assertions.assertTrue(Objects.deepEquals(actual, expected));
    }

}
