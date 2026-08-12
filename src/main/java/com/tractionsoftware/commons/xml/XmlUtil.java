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

package com.tractionsoftware.commons.xml;

import com.tractionsoftware.commons.codec.HtmlEncodingUtil;
import com.tractionsoftware.commons.lang.EnhancedCharSequence;
import com.tractionsoftware.commons.lang.ObjectUtil;
import com.tractionsoftware.commons.text.CodePointBasedFilteringTextMapper;
import jakarta.annotation.Nonnull;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.function.IntPredicate;

public final class XmlUtil {

    /*
     * Not instantiable.
     */
    private XmlUtil() {
    }

    private static final Logger LOGGER = LoggerFactory.getLogger(XmlUtil.class);

    /**
     * The default character to be used to replace unsafe characters.
     */
    public static final int DEFAULT_SAFE_REPLACEMENT_CHARACTER = ' ';

    public enum CharacterSafetyLevel {

        /**
         *
         */
        AVOID_INVALID(XmlUtil::isValidXMLCharacter),

        /**
         * Avoid characters that match the <tt>RestrictedChar</tt> rule in the <a
         * href="https://www.w3.org/TR/xml11/#charsets">XML 1.1 Specification</a>.
         */
        AVOID_RESTRICTED(
            codePoint -> XmlUtil.isValidXMLCharacter(codePoint) && !XmlUtil.isRestrictedXMLCharacter(codePoint)
        ),

        /**
         * Avoid characters that match the definition of "discouraged" according to the <a
         * href="https://www.w3.org/TR/xml11/#charsets">XML 1.1 Specification</a>.
         */
        AVOID_DISCOURAGED(
            codePoint ->
                XmlUtil.isValidXMLCharacter(codePoint) &&
                !XmlUtil.isDiscouragedXMLCharacter(codePoint)
        );

        public static CharacterSafetyLevel getMinimal(boolean avoidRestricted, boolean avoidDiscouraged) {
            if (avoidDiscouraged) {
                return AVOID_DISCOURAGED;
            }
            if (avoidRestricted) {
                return AVOID_RESTRICTED;
            }
            return AVOID_INVALID;
        }

        private final IntPredicate isSafeTest;

        CharacterSafetyLevel(IntPredicate isSafeTest) {
            this.isSafeTest = isSafeTest;
        }

        public final boolean isSafe(int codePoint) {
            return isSafeTest.test(codePoint);
        }

        public final boolean isUnsafe(int codePoint) {
            return !isSafeTest.test(codePoint);
        }

        public final Writer createSafeWriter(Writer out) {
            return XmlUtil.createSafeWriter(out, this);
        }

        public final Writer createSafeWriter(Writer out, int replacementCodePoint) {
            return XmlUtil.createSafeWriter(out, this, replacementCodePoint);
        }

    }

    private static final class ReplacementParameters {

        public static ReplacementParameters createInstance(CharacterSafetyLevel level, int replacementCodePoint) {
            if (level.isUnsafe(replacementCodePoint)) {
                replacementCodePoint = DEFAULT_SAFE_REPLACEMENT_CHARACTER;
            }
            return new ReplacementParameters(level, Character.toChars(replacementCodePoint));
        }

        /**
         * Indicating how the reader will treat the XML 1.1 "discouraged" characters as unsafe.
         */
        private final CharacterSafetyLevel level;

        /**
         * The char values representing the character code point that will be used to replace unsafe characters.
         */
        private final char[] replacementCodePointChars;

        private ReplacementParameters(CharacterSafetyLevel level, char[] replacementCodePointChars) {
            this.level = level;
            this.replacementCodePointChars = replacementCodePointChars;
        }

        public int addReplacement(char[] buff, int offset) {
            for (char replacementCodePointChar : replacementCodePointChars) {
                buff[offset++] = replacementCodePointChar;
            }
            return offset;
        }

        public int addOrReplace(char[] buff, int offset, char c) {
            if (level.isSafe(c)) {
                buff[offset] = c;
                return offset + 1;
            }
            return addReplacement(buff, offset);
        }

        public int addOrReplace(char[] buff, int offset, char highSurrogate, char lowSurrogate) {
            int codePoint = Character.codePointAt(new char[] { highSurrogate, lowSurrogate }, 0);
            if (level.isSafe(codePoint)) {
                buff[offset] = highSurrogate;
                buff[offset + 1] = lowSurrogate;
                return offset + 2;
            }
            return addReplacement(buff, offset);
        }

    }

    private static final class SafeBlock {

        static final class Builder {

            private enum HandleStatus {

                SURROGATE_HIGH,

                SURROGATE_HIGH_DUPLICATE,

                SURROGATE_LOW,

                SURROGATE_LOW_NO_HIGH,

                BMP,

                BMP_WITH_HIGH

            }

            public static Builder createInstance(ReplacementParameters replacementParams, char[] cbuf, int charCount) {
                return new Builder(replacementParams, cbuf, charCount);
            }

            private final ReplacementParameters replacementParams;

            private final char[] cbuf;

            private final int charCount;

            private char[] blockBuffer;

            private int blockIndex;

            private Character highSurrogate;

            private Builder(ReplacementParameters replacementParams, char[] cbuf, int charCount) {
                this.replacementParams = replacementParams;
                this.cbuf = cbuf;
                this.charCount = charCount;
            }

            public SafeBlock build() {
                if (blockBuffer == null) {
                    initBlockBuffer();
                }
                return SafeBlock.getInstance(blockBuffer, blockIndex, highSurrogate);
            }

            private void initBlockBuffer() {

                if (charCount <= 0) {
                    this.blockBuffer = ArrayUtils.EMPTY_CHAR_ARRAY;
                    this.blockIndex = 0;
                    return;
                }

                this.blockBuffer = new char[charCount * 2];
                this.blockIndex = 0;
                int buffIndex = 0;

                while (buffIndex < charCount) {
                    char c = cbuf[buffIndex++];
                    switch (getHandleStatus(c)) {
                    case SURROGATE_HIGH -> onHighSurrogate(c);
                    case SURROGATE_HIGH_DUPLICATE -> onHighSurrogateDuplicate(c);
                    case SURROGATE_LOW -> onLowSurrogate(c);
                    case SURROGATE_LOW_NO_HIGH -> onLowSurrogateNoHigh(c);
                    case BMP -> onBmp(c);
                    case BMP_WITH_HIGH -> onBmpAfterHighSurrogate(c);
                    }
                }

            }

            private HandleStatus getHandleStatus(char c) {
                if (Character.isHighSurrogate(c)) {
                    if (highSurrogate == null) {
                        return HandleStatus.SURROGATE_HIGH;
                    }
                    return HandleStatus.SURROGATE_HIGH_DUPLICATE;
                }
                if (Character.isLowSurrogate(c)) {
                    if (highSurrogate == null) {
                        return HandleStatus.SURROGATE_LOW_NO_HIGH;
                    }
                    return HandleStatus.SURROGATE_LOW;
                }
                if (highSurrogate == null) {
                    return HandleStatus.BMP;
                }
                return HandleStatus.BMP_WITH_HIGH;
            }

            private void onHighSurrogate(char c) {
                this.highSurrogate = c;
            }

            private void onHighSurrogateDuplicate(char c) {
                LOGGER.warn(
                    "Expected low surrogate to finish code point for {} but encountered another high surrogate character {}",
                    ObjectUtil.safeToStringObject(() -> Integer.toHexString(highSurrogate)),
                    ObjectUtil.safeToStringObject(() -> Integer.toHexString(c)),
                    new IllegalStateException()
                );
                blockIndex = replacementParams.addReplacement(blockBuffer, blockIndex);
                highSurrogate = c;
            }

            private void onLowSurrogate(char c) {
                blockIndex = replacementParams.addOrReplace(
                    blockBuffer,
                    blockIndex,
                    highSurrogate,
                    c
                );
                highSurrogate = null;
            }

            private void onLowSurrogateNoHigh(char c) {
                LOGGER.warn(
                    "Unexpected low surrogate character {} with no high surrogate.",
                    ObjectUtil.safeToStringObject(() -> Integer.toHexString(c)),
                    new IllegalStateException()
                );
            }

            private void onBmp(char c) {
                blockIndex = replacementParams.addOrReplace(
                    blockBuffer,
                    blockIndex,
                    c
                );
            }

            private void onBmpAfterHighSurrogate(char c) {
                LOGGER.warn(
                    "Expected low surrogate to finish code point for {}, but encountered normal BMP character {}",
                    ObjectUtil.safeToStringObject(() -> Integer.toHexString(highSurrogate)),
                    ObjectUtil.safeToStringObject(() -> Integer.toHexString(c)),
                    new IllegalStateException()
                );
            }

        }

        private static final SafeBlock EMPTY = new SafeBlock(ArrayUtils.EMPTY_CHAR_ARRAY, 0, null);

        public static SafeBlock getInstance(char[] blockBuffer, int blockIndex, Character highSurrogateLeftOver) {
            if (blockIndex == 0) {
                return EMPTY;
            }
            return new SafeBlock(blockBuffer, blockIndex, highSurrogateLeftOver);
        }

        private final char[] buff;

        private final int maxIndex;

        private final Character highSurrogateLeftOver;

        private int nextIndex = 0;

        private SafeBlock(char[] buff, int maxIndex, Character highSurrogateLeftOver) {
            this.buff = buff;
            this.maxIndex = maxIndex;
            this.highSurrogateLeftOver = highSurrogateLeftOver;
        }

        public boolean isDone() {
            if (maxIndex == -1) {
                return true;
            }
            return false;
        }

        public boolean hasNext() {
            if (nextIndex < maxIndex) {
                return true;
            }
            return false;
        }

        public char next() {
            return buff[nextIndex++];
        }

        public void writeTo(Writer writer) throws IOException {
            if (hasNext()) {
                writer.write(buff, nextIndex, maxIndex - nextIndex);
                nextIndex = maxIndex;
            }
        }

        public Character leftOver() {
            return highSurrogateLeftOver;
        }

    }

    private static abstract class SafeChars<T> {

        static final int BUFFER_SIZE = 1024;

        final T wrapped;

        final ReplacementParameters replacementParams;

        private SafeChars(T wrapped, ReplacementParameters replacementParams) {
            this.wrapped = wrapped;
            this.replacementParams = replacementParams;
        }

    }

    private static final class ReadSafeChars extends SafeChars<Reader> {

        public static ReadSafeChars createInstance(Reader reader, CharacterSafetyLevel level, int replacementCodePoint) {
            return new ReadSafeChars(reader, ReplacementParameters.createInstance(level, replacementCodePoint));
        }

        private SafeBlock block = null;

        private Character leftOver = null;

        private ReadSafeChars(Reader reader, ReplacementParameters replacementParams) {
            super(reader, replacementParams);
        }

        public boolean hasNext() throws IOException {
            ensureReady();
            return block.hasNext();
        }

        public char next() throws IOException {
            ensureReady();
            if (!block.hasNext()) {
                throw new NoSuchElementException();
            }
            return consumeNext();
        }

        public boolean ready() throws IOException {
            if (block != null || wrapped.ready()) {
                return true;
            }
            return false;
        }

        private void ensureReady() throws IOException {
            if (block == null) {
                initNextBlock();
            }
        }

        private char consumeNext() {
            char c = block.next();
            if (!block.hasNext()) {
                block = null;
            }
            return c;
        }

        private void initNextBlock() throws IOException {

            char[] cbuf = new char[BUFFER_SIZE];
            int charCount;
            if (leftOver == null) {
                charCount = wrapped.read(cbuf, 0, BUFFER_SIZE);
            }
            else {
                cbuf[0] = leftOver;
                charCount = wrapped.read(cbuf, 1, BUFFER_SIZE - 1) + 1;
            }

            block = SafeBlock.Builder.createInstance(replacementParams, cbuf, charCount).build();
            leftOver = block.leftOver();

        }

    }

    private static final class WriteSafeChars extends SafeChars<Writer> {

        public static WriteSafeChars createInstance(Writer writer, CharacterSafetyLevel level, int replacementCodePoint) {
            return new WriteSafeChars(writer, ReplacementParameters.createInstance(level, replacementCodePoint));
        }

        private char[] buff;

        private int nextIndex = 0;

        private WriteSafeChars(Writer writer, ReplacementParameters replacementParams) {
            super(writer, replacementParams);
        }

        public void write(int c) throws IOException {
            ensureReady();
            buff[nextIndex++] = (char) c;
        }

        public void write(char[] cbuf, int off, int len) throws IOException {
            ensureReady();
            int remainingLen = len;
            int nextSourceOffset = off;
            while (remainingLen > 0) {
                int writeLen = Math.min(remainingLen, BUFFER_SIZE - nextIndex);
                System.arraycopy(cbuf, nextSourceOffset, buff, nextIndex, writeLen);
                remainingLen -= writeLen;
                nextSourceOffset += writeLen;
                nextIndex += writeLen;
                commitIfNecessary();
            }
        }

        public void write(String s, int off, int len) throws IOException {
            ensureReady();
            int remainingLen = len;
            int nextSourceOffset = off;
            while (remainingLen > 0) {
                int writeLen = Math.min(remainingLen, BUFFER_SIZE - nextIndex);
                s.getChars(nextSourceOffset, nextSourceOffset + writeLen, buff, nextIndex);
                remainingLen -= writeLen;
                nextSourceOffset += writeLen;
                nextIndex += writeLen;
                commitIfNecessary();
            }
        }

        private void ensureReady() throws IOException {
            if (buff == null) {
                buff = new char[BUFFER_SIZE];
                nextIndex = 0;
            }
            else {
                commitIfNecessary();
            }
        }

        public void flush() throws IOException {
            commit();
            wrapped.flush();
        }

        private void commitIfNecessary() throws IOException {
            if (nextIndex == BUFFER_SIZE) {
                commit();
            }
        }

        private void commit() throws IOException {

            if (nextIndex == 0) {
                return;
            }

            SafeBlock block = SafeBlock.Builder.createInstance(replacementParams, buff, nextIndex).build();
            block.writeTo(wrapped);

            Character leftOver = block.leftOver();
            if (leftOver == null) {
                nextIndex = 0;
            }
            else {
                buff[0] = leftOver;
                nextIndex = 1;
            }

        }

    }

    /**
     * A character string reader that ensures that only characters that are safe characters for an XML document are
     * supplied as the result of its read methods. Any character that is found not to be a safe XML character is
     * replaced by a replacement character, which is guaranteed to be a safe and valid XML character.
     *
     * <p>
     * Besides outright invalid characters, characters that are, according to <a
     * href="http://www.w3.org/TR/xml11/#charsets">Section 2.2 (Characters)</a> of <a
     * href="http://www.w3.org/TR/xml11/">the XML 1.1 Specification</a>, either discouraged or restricted may also be
     * optionally excluded.
     */
    private static final class SafeXmlReader extends FilterReader {

        private ReadSafeChars chars;

        private SafeXmlReader(Reader reader, ReadSafeChars chars) {
            super(reader);
            this.chars = chars;
        }

        /**
         * Read a single character, ensuring it is a safe XML character.
         *
         * @return the int representing the Unicode code point for the character being read if the character is a safe
         *     XML character; the replacement character otherwise.
         * @throws IOException
         *     If an I/O error occurs
         */
        @Override
        public int read() throws IOException {
            checkOpen();
            if (chars.hasNext()) {
                return chars.next();
            }
            return -1;
        }

        /**
         * Read characters into a portion of an array, ensuring that unsafe characters are replaced.
         *
         * @throws IOException
         *     If an I/O error occurs
         */
        @Override
        public int read(@Nonnull char[] cbuf, int off, int len) throws IOException {
            checkOpen();
            if (!chars.hasNext()) {
                return -1;
            }
            int buffIndex = off;
            while (buffIndex < len && chars.hasNext()) {
                cbuf[buffIndex++] = chars.next();
            }
            return buffIndex - off;
        }

        @Override
        public long skip(long n) throws IOException {
            checkOpen();
            if (n < 0L) {
                throw new IllegalArgumentException("skip value is negative");
            }
            long skipped = 0;
            while (skipped < n && chars.hasNext()) {
                chars.next();
                skipped++;
            }
            return skipped;
        }

        @Override
        public boolean ready() throws IOException {
            checkOpen();
            return chars.ready();
        }

        @Override
        public boolean markSupported() {
            return false;
        }

        @Override
        public void mark(int readAheadLimit) throws IOException {
            checkOpen();
            throw new IOException("mark() not supported");
        }

        @Override
        public void reset() throws IOException {
            checkOpen();
            throw new IOException("reset() not supported");
        }

        @Override
        public void close() throws IOException {
            if (chars != null) {
                chars = null;
                super.close();
            }
        }

        private void checkOpen() throws IOException {
            if (chars == null) {
                throw new IOException("Closed.");
            }
        }

    }

    /**
     * A character string writer that ensures that only characters that are safe characters for an XML document are
     * written by its write methods. Any character that is found not to be a safe XML character is not written, and a
     * replacement character, which is guaranteed to be a safe and valid XML character, is written in its place.
     *
     * <p>
     * Besides outright invalid characters, characters that are, according to <a
     * href="http://www.w3.org/TR/xml11/#charsets">Section 2.2 (Characters)</a> of <a
     * href="http://www.w3.org/TR/xml11/">the XML 1.1 Specification</a>, either discouraged or restricted may also be
     * optionally excluded.
     */
    private static final class SafeXmlWriter extends FilterWriter {

        private WriteSafeChars chars;

        private SafeXmlWriter(Writer writer, WriteSafeChars chars) {
            super(writer);
            this.chars = chars;
        }

        @Override
        public void write(int c) throws IOException {
            checkOpen();
            chars.write(c);
        }

        @Override
        public void write(char[] cbuf, int off, int len) throws IOException {
            checkOpen();
            chars.write(cbuf, off, len);
        }

        @Override
        public void write(String s, int off, int len) throws IOException {
            checkOpen();
            chars.write(s, off, len);
        }

        @Override
        public void flush() throws IOException {
            checkOpen();
            chars.flush();
        }

        @Override
        public void close() throws IOException {
            chars = null;
            super.close();
        }

        private void checkOpen() throws IOException {
            if (chars == null) {
                throw new IOException("Closed.");
            }
        }

    }

    public static String getLiteralText(String str) {
        if (StringUtils.isEmpty(str)) {
            return str;
        }
        return CodePointBasedFilteringTextMapper.replace(str, XmlUtil::getRequiredLiteralEncodingReplacement);
    }

    public static void printLiteralText(PrintWriter out, String str) {
        if (StringUtils.isNotEmpty(str)) {
            CodePointBasedFilteringTextMapper.replace(str, out, XmlUtil::getRequiredLiteralEncodingReplacement);
        }
    }

    public static CharSequence getHexEncoding(int codePoint) {
        char[] encoding = createBaseHexEncoding();
        String hex = Integer.toHexString(codePoint);
        int hexLen = hex.length();
        int nextIndex = 3 + (4 - hexLen);
        for (int i = 0; i < hexLen; i++) {
            encoding[nextIndex++] = hex.charAt(i);
        }
        return EnhancedCharSequence.getInstance(encoding);
    }

    public static boolean isCompletelySafeCharacter(int codePoint) {
        return CharacterSafetyLevel.AVOID_DISCOURAGED.isSafe(codePoint);
    }

    /**
     * Tests whether the value for the given Unicode code point is considered valid.
     *
     * @param codePoint
     *     the value representing the Unicode code point to check.
     * @return true if the value for the given Unicode code point meets the XML 1.1 specification's guidelines for
     *     character ranges that "are also discouraged"; false otherwise.
     */
    public static boolean isValidXMLCharacter(int codePoint) {
        // Char ::= [#x1-#xD7FF] | [#xE000-#xFFFD] | [#x10000-#x10FFFF]
        if ((codePoint >= 0x1 && codePoint <= 0xD7FF) ||
            (codePoint >= 0XE000 && codePoint <= 0xFFFD) ||
            (codePoint >= 0x10000 && codePoint <= 0x10FFFF)) {
            return true;
        }
        return false;
    }

    public static boolean isRestrictedXMLCharacter(int codePoint) {
        // RestrictedChar ::= [#x1-#x8] | [#xB-#xC] | [#xE-#x1F] | [#x7F-#x84] | [#x86-#x9F]
        if ((codePoint >= 0x1 && codePoint <= 0x8) ||
            (codePoint >= 0xB && codePoint <= 0xC) ||
            (codePoint >= 0xE && codePoint <= 0x1F) ||
            (codePoint >= 0x7F && codePoint <= 0x84) ||
            (codePoint >= 0x86 && codePoint <= 0x9F)) {
            return true;
        }
        return false;
    }

    public static boolean isDiscouragedXMLCharacter(int codePoint) {
        if (isInDiscouragedUnicodeBlock(codePoint) ||
            isControlOrPermanentlyUndefined(codePoint)) {
            return true;
        }
        return false;
    }

    /**
     * Tests whether the value for the given Unicode code point meets the XML 1.1 specification's guidelines for
     * character ranges that: "...are also discouraged. They are either control characters or permanently undefined
     * Unicode characters."
     *
     * @param codePoint
     *     the value representing the Unicode code point to check.
     * @return true if the value for the given Unicode code point meets the XML 1.1 specification's guidelines for
     *     character ranges that "are also discouraged"; false otherwise.
     */
    public static boolean isControlOrPermanentlyUndefined(int codePoint) {
        // [#x1-#x8], [#xB-#xC], [#xE-#x1F], [#x7F-#x84], [#x86-#x9F], [#xFDD0-#xFDDF],
        // [#x1FFFE-#x1FFFF], [#x2FFFE-#x2FFFF], [#x3FFFE-#x3FFFF],
        // [#x4FFFE-#x4FFFF], [#x5FFFE-#x5FFFF], [#x6FFFE-#x6FFFF],
        // [#x7FFFE-#x7FFFF], [#x8FFFE-#x8FFFF], [#x9FFFE-#x9FFFF],
        // [#xAFFFE-#xAFFFF], [#xBFFFE-#xBFFFF], [#xCFFFE-#xCFFFF],
        // [#xDFFFE-#xDFFFF], [#xEFFFE-#xEFFFF], [#xFFFFE-#xFFFFF],
        // [#x10FFFE-#x10FFFF].
        if (isRestrictedXMLCharacter(codePoint) ||
            !Character.isDefined(codePoint)) {
            return true;
        }
        return false;
    }

    /**
     * Tests whether a value for a given Unicode code point meets the XML 1.1 specification's definition for a
     * "permanently undefined" Unicode code point. The {@link #isControlOrPermanentlyUndefined(int)} method currently
     * uses {@link Character#isDefined(int)} instead of this method, because although the definition appears to be
     * <em>slightly</em> different than the one in the XML 1.1 specification, Character.isDefined should be based on
     * the Unicode definition of "non-characters" which should be scoped correctly for determining whether or not a
     * character belongs in an XML document. This method, however, is preserved here in case the slightly different
     * definition becomes useful.
     *
     * @param codePoint
     *     the value representing the Unicode code point to check.
     * @return true if the given value for a given Unicode code point meets the XML 1.1 specification's definition for a
     *     "permanently undefined" Unicode character; false otherwise.
     */
    public static boolean isPermanentlyUndefined(int codePoint) {
        if ((codePoint >= 0xFDD0 && codePoint <= 0xFDDF) ||
            (codePoint >= 0x1FFFE && codePoint <= 0x1FFFF) ||
            (codePoint >= 0x2FFFE && codePoint <= 0x2FFFF) ||
            (codePoint >= 0x3FFFE && codePoint <= 0x3FFFF) ||
            (codePoint >= 0x4FFFE && codePoint <= 0x4FFFF) ||
            (codePoint >= 0x5FFFE && codePoint <= 0x5FFFF) ||
            (codePoint >= 0x6FFFE && codePoint <= 0x6FFFF) ||
            (codePoint >= 0x7FFFE && codePoint <= 0x7FFFF) ||
            (codePoint >= 0x8FFFE && codePoint <= 0x8FFFF) ||
            (codePoint >= 0x9FFFE && codePoint <= 0x9FFFF) ||
            (codePoint >= 0xAFFFE && codePoint <= 0xAFFFF) ||
            (codePoint >= 0xBFFFE && codePoint <= 0xBFFFF) ||
            (codePoint >= 0xCFFFE && codePoint <= 0xCFFFF) ||
            (codePoint >= 0xDFFFE && codePoint <= 0xDFFFF) ||
            (codePoint >= 0xEFFFE && codePoint <= 0xEFFFF) ||
            (codePoint >= 0xFFFFE && codePoint <= 0xFFFFF) ||
            (codePoint >= 0x10FFFE && codePoint <= 0x10FFFF)) {
            return true;
        }
        return false;
    }

    /**
     * Returns true if the given code point belongs to one of the "compatibility" Unicode blocks.
     *
     * <blockquote>
     * Document authors are encouraged to avoid "compatibility characters", as defined in <a
     * href="https://www.w3.org/TR/xml11/#Unicode">Unicode</a>.
     * </blockquote>
     *
     * @param codePoint
     *     the code point.
     * @return true if the given code point belongs to one of the "compatibility" Unicode blocks.
     */
    public static boolean isInDiscouragedUnicodeBlock(int codePoint) {
        Character.UnicodeBlock uBlock = Character.UnicodeBlock.of(codePoint);
        if (uBlock == Character.UnicodeBlock.HANGUL_COMPATIBILITY_JAMO ||
            uBlock == Character.UnicodeBlock.CJK_COMPATIBILITY ||
            uBlock == Character.UnicodeBlock.CJK_COMPATIBILITY_IDEOGRAPHS ||
            uBlock == Character.UnicodeBlock.CJK_COMPATIBILITY_FORMS ||
            uBlock == Character.UnicodeBlock.CJK_COMPATIBILITY_IDEOGRAPHS_SUPPLEMENT) {
            return true;
        }
        return false;
    }

    /**
     * Create a new XML character string reader, using the default replacement character.
     *
     * @param string
     *     The String to be read.
     * @param level
     *     Indicating the level of "safety" to be enforced.
     */
    public static Reader createSafeReader(String string, CharacterSafetyLevel level) {
        return createSafeReader(string, level, DEFAULT_SAFE_REPLACEMENT_CHARACTER);
    }

    /**
     * Create a new XML character string reader.
     *
     * @param string
     *     The String to be read.
     * @param level
     *     Indicating the level of "safety" to be enforced.
     * @param replacementCodePoint
     *     indicating the character that will be used to replace characters deemed unsafe XML characters. If this
     *     character itself is an unsafe XML character, {@link #DEFAULT_SAFE_REPLACEMENT_CHARACTER} will be used
     *     instead.
     */
    public static Reader createSafeReader(String string, CharacterSafetyLevel level, int replacementCodePoint) {
        return createSafeReader(new StringReader(string), level, replacementCodePoint);
    }

    public static Reader createSafeReader(Reader reader, CharacterSafetyLevel level, int replacementCodePoint) {
        Objects.requireNonNull(reader, "Reader");
        return new SafeXmlReader(reader, ReadSafeChars.createInstance(reader, level, replacementCodePoint));
    }

    public static Writer createSafeWriter(Writer writer) {
        return createSafeWriter(writer, CharacterSafetyLevel.AVOID_DISCOURAGED);
    }

    public static Writer createSafeWriter(Writer writer, CharacterSafetyLevel level) {
        return createSafeWriter(writer, level, DEFAULT_SAFE_REPLACEMENT_CHARACTER);
    }

    public static Writer createSafeWriter(Writer writer, CharacterSafetyLevel level, int replacementCodePoint) {
        Objects.requireNonNull(writer, "Writer");
        return new SafeXmlWriter(writer, WriteSafeChars.createInstance(writer, level, replacementCodePoint));
    }

    private static CharSequence getRequiredLiteralEncodingReplacement(int codePoint) {
        return switch (codePoint) {
            case '"' -> HtmlEncodingUtil.SimpleHtmlEntity.QUOTATION_MARK.getEncoding();
            case '>' -> HtmlEncodingUtil.SimpleHtmlEntity.GREATER_THAN.getEncoding();
            case '<' -> HtmlEncodingUtil.SimpleHtmlEntity.LESS_THAN.getEncoding();
            case '&' -> HtmlEncodingUtil.SimpleHtmlEntity.AMPERSAND.getEncoding();
            default -> CharacterSafetyLevel.AVOID_RESTRICTED.isSafe(codePoint) ? null : getHexEncoding(codePoint);
        };
    }

    private static char[] createBaseHexEncoding() {
        return new char[] {
            '&', '#', 'x', '0', '0', '0', '0', ';'
        };
    }

}
