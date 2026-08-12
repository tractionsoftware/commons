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

import com.google.common.annotations.Beta;
import com.google.common.base.Ascii;
import com.google.common.base.CharMatcher;
import com.tractionsoftware.commons.net.MediaTypeUtil;
import com.tractionsoftware.commons.text.CharBasedFilteringTextMapper;
import com.tractionsoftware.commons.net.URLUtil;
import com.tractionsoftware.commons.lang.EnhancedCharSequence;
import com.tractionsoftware.commons.lang.StringUtil;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.util.Objects;
import java.util.function.IntPredicate;
import java.util.function.Supplier;

public final class FileNameUtil {

    private FileNameUtil() {
    }

    /**
     * The usual separator in a file name that appears before the file extension.
     */
    public static final char EXTENSION_SEPARATOR_CHAR = '.';

    /**
     * String version of {@link #EXTENSION_SEPARATOR_CHAR}.
     */
    public static final String EXTENSION_SEPARATOR = ".";

    public static final char CURRENT_PATH_INDICATOR_CHAR = '.';

    public static final String CURRENT_PATH_INDICATOR = ".";

    /**
     * The default path separator used in Windows (backslash).
     *
     * <pre>
     * C:\Program Files\Foo\bar
     * </pre>
     */
    public static final char WINDOWS_PATH_SEPARATOR_CHAR = '\\';

    /**
     * The default generic path separator used in UNIX, Linux, macOS, etc. (forward slash).
     *
     * <pre>
     * /Users/bob/foo/bar
     * </pre>
     */
    public static final char GENERIC_PATH_SEPARATOR_CHAR = '/';

    /**
     * String version of {@link #GENERIC_PATH_SEPARATOR_CHAR}.
     */
    public static final String GENERIC_PATH_SEPARATOR = "/";

    /**
     * Consecutive dot characters (.), usually denoting the parent container in a path specification.
     *
     * <pre>
     * ../foo/bar
     * </pre>
     */
    public static final String CONSECUTIVE_DOTS = "..";

    private static final char INVALID_FILE_NAME_CHARACTER_REPLACEMENT_CHAR = '_';

    public static final int FILE_NAME_LENGTH_LIMIT = 255;

    /**
     * On Windows, these are considered reserved, and are explicitly disallowed in file names:
     *
     * <ul>
     * <li>&lt; (less than)</li>
     * <li>&gt; (greater than)</li>
     * <li>: (colon)</li>
     * <li>; (semi-colon - not usually illegal, but can cause problems in various contexts)</li>
     * <li>&quot; (double quotation mark)</li>
     * <li>/ (slash)</li>
     * <li>/ (backslash)</li>
     * <li>| (vertical bar or pipe)</li>
     * <li>? (question mark)</li>
     * <li>* (asterisk)</li>
     * </ul>
     *
     * <p>
     * See <a href="https://msdn.microsoft.com/en-us/library/aa365247.aspx">Naming Files, Paths, and Namespaces</a>
     */
    public static final CharMatcher RESERVED_FILENAME_CHARACTERS = CharMatcher.anyOf(
        EnhancedCharSequence.getInstance(
            new char[] {
                '<', '>', ':', '"', GENERIC_PATH_SEPARATOR_CHAR, WINDOWS_PATH_SEPARATOR_CHAR, '|', '*', '?'
            }
        )
    );

    /**
     * These characters are dangerous because they are often part of commands on UNIX-like systems.
     *
     * <ul>
     * <li>Java ISO control characters, including 0x0 through 0x1f (such as NUL, BEL and ESC), as well as 0x7f (DEL)
     * through 0x9f ()</li>
     * <li>; (semi-colon)</li>
     * <li>` (backtick)</li>
     * </ul>
     */
    public static final CharMatcher DISCOURAGED_FILENAME_CHARACTERS = CharMatcher.anyOf(
        EnhancedCharSequence.getInstance(new char[] { ';', '&', '`', Ascii.HT, Ascii.NL, Ascii.CR, Ascii.FF })
    );

    /**
     * The following reserved characters are not allowed:
     *
     * <ul>
     * <li>Java ISO control characters, including 0x0 through 0x1f (such as NUL, BEL and ESC), as well as 0x7f through
     * 0x9f (such as DEL and C1 controls)</li>
     * <li>&lt; (less than)</li>
     * <li>&gt; (greater than)</li>
     * <li>: (colon)</li>
     * <li>&quot; (double quotation mark)</li>
     * <li>/ (slash)</li>
     * <li>/ (backslash)</li>
     * <li>| (vertical bar or pipe)</li>
     * <li>? (question mark)</li>
     * <li>* (asterisk)</li>
     * </ul>
     *
     * <p>
     * See <a href="https://msdn.microsoft.com/en-us/library/aa365247.aspx">Naming Files, Paths, and Namespaces</a>
     */
    public static final CharMatcher ILLEGAL_FILENAME_CHARACTERS = CharMatcher.javaIsoControl()
        .or(RESERVED_FILENAME_CHARACTERS);

    private static final IntPredicate INVALID_CODE_POINT = cp -> !Character.isValidCodePoint(cp);

    public static final IntPredicate ILLEGAL_FILENAME_CODEPOINTS = INVALID_CODE_POINT;

    public static final IntPredicate DISCOURAGED_FILENAME_CODEPOINTS = StringUtil::isAlternativeWhitespaceCodePoint;

    public static final StringUtil.CharMapper ILLEGAL_OR_DISCOURAGED_FILENAME_CHAR_REPLACER =
        new StringUtil.CharMapper() {

            @Override
            public final String toString() {
                return "Illegal/Discouraged file name character replacer";
            }

            @Override
            public final char getReplacement(char c) {
                if (ILLEGAL_FILENAME_CHARACTERS.matches(c) ||
                    DISCOURAGED_FILENAME_CHARACTERS.matches(c)) {
                    if (Character.isWhitespace(c)) {
                        return ' ';
                    }
                    return INVALID_FILE_NAME_CHARACTER_REPLACEMENT_CHAR;
                }
                return c;
            }

        };

    private static enum ProblemClassificationLevel {
        NONE,
        DISCOURAGED,
        ILLEGAL
    }

    @Beta
    public static enum FileNameCheckResult {

        /**
         * No illegal or discouraged characters, sequences, or names were found.
         */
        NO_PROBLEMS(ProblemClassificationLevel.NONE),

        /**
         * The file name was empty or blank (or null).
         */
        EMPTY_OR_BLANK(ProblemClassificationLevel.ILLEGAL),

        /**
         * The file name is too long to be allowed in modern operating environments.
         */
        TOO_LONG(ProblemClassificationLevel.ILLEGAL),

        /**
         * The file name contains at least one character that is illegal in a modern operating environment.
         */
        ILLEGAL_CHARACTERS(ProblemClassificationLevel.ILLEGAL),

        /**
         * The file name contains a sequence of at least {@link #CONSECUTIVE_DOTS two consecutive dots}, which is
         * illegal in most modern operating environments.
         */
        ILLEGAL_DOT_SEQUENCE(ProblemClassificationLevel.ILLEGAL),

        /**
         * The file name is an illegal name in one or more modern operating environments, such as a name consisting of a
         * single dot.
         */
        ILLEGAL_FILENAME(ProblemClassificationLevel.ILLEGAL),

        /**
         * The file name is a
         * {@link #isSpecialIllegalWindowsFileName(String) special reserved and illegal file name on Windows}.
         */
        ILLEGAL_SPECIAL_WINDOWS_FILENAME(ProblemClassificationLevel.ILLEGAL),

        /**
         * The file name contains at least one discouraged character, such as as semi-colon or backtick.
         */
        DISCOURAGED_CHARACTERS(ProblemClassificationLevel.DISCOURAGED),

        /**
         * The file name contains a discouraged sequence of whitespace characters, such as consecutive space
         * characters.
         */
        DISCOURAGED_WHITESPACE_SEQUENCE(ProblemClassificationLevel.DISCOURAGED),

        /**
         * The file name ends with the "." extension separator character.
         */
        DISCOURAGED_ENDS_WITH_EXTENSION_SEPARATOR(ProblemClassificationLevel.DISCOURAGED),

        /**
         * The file name is discouraged in UNIX environments because of its tendency to accidentally become part of a
         * dangerous command, or some other similar reason. See {@link #isDangerousUnixFileName(String)}.
         */
        DISCOURAGED_UNIX_FILENAME(ProblemClassificationLevel.DISCOURAGED);

        private final ProblemClassificationLevel level;

        private FileNameCheckResult(ProblemClassificationLevel level) {
            this.level = level;
        }

        public final boolean noProblem() {
            if (level == ProblemClassificationLevel.NONE) {
                return true;
            }
            return false;
        }

        public final boolean hasProblem() {
            if (level == ProblemClassificationLevel.NONE) {
                return false;
            }
            return true;
        }

        public final boolean legal() {
            if (level == ProblemClassificationLevel.ILLEGAL) {
                return false;
            }
            return true;
        }

        public final boolean discouraged() {
            if (level == ProblemClassificationLevel.DISCOURAGED) {
                return true;
            }
            return false;
        }

        public final boolean illegal() {
            if (level == ProblemClassificationLevel.ILLEGAL) {
                return true;
            }
            return false;
        }

    }

    /**
     * A {@link StringUtil.CharMapper} that can be used to replace the generic or UNIX platform file separator character
     * with the platform-specific file separator char.
     */
    public static final StringUtil.CharMapper GENERIC_TO_WINDOWS_SEPARATOR_REPLACEMENT = c -> {
        if (GENERIC_PATH_SEPARATOR_CHAR == c) {
            return File.separatorChar;
        }
        return c;
    };

    /**
     * Returns true if the given file name is outright illegal, even when followed by extensions, in Windows.
     *
     * <p>
     * See <a href="https://msdn.microsoft.com/en-us/library/aa365247.aspx">Naming Files, Paths, and Namespaces</a>
     *
     * @param fileName
     *     the file name to check.
     * @return true if the given file name is outright illegal, even when followed by extensions, in Windows.
     * @throws NullPointerException
     *     if the given file name is null.
     */
    public static final boolean isSpecialIllegalWindowsFileName(@Nonnull String fileName) {
        String name = stripExtension(fileName);
        return switch (name.toUpperCase()) {
            case "CON", "PRN", "AUX", "NUL", "COM0", "COM1", "COM2", "COM3", "COM4", "COM5", "COM6", "COM7", "COM8",
                 "COM9", "COM¹", "COM²", "COM³", "LPT0", "LPT1", "LPT2", "LPT3", "LPT4", "LPT5", "LPT6", "LPT7", "LPT8",
                 "LPT9", "LPT¹", "LPT²", "LPT" -> true;
            default -> false;
        };
    }

    /**
     * Checks whether the given file name is a dangerous file name in a UNIX-like environment.
     *
     * @param fileName
     *     the file name to check.
     * @return true the given file name is a dangerous file name in a UNIX-like environment according to the heuristic
     *     used here; false otherwise.
     */
    public static final boolean isDangerousUnixFileName(String fileName) {
        if (StringUtil.startsWith(fileName, '-')) {
            return true;
        }
        return false;
    }

    /**
     * Strips the path from the beginning of a full path specification, leaving just the file name.
     *
     * @param fullPath
     *     the full path of the file.
     * @return the file name from the full path.
     */
    public static final String stripPath(String fullPath) {
        if (fullPath == null) {
            return null;
        }
        String pi = platformIndependentPath(fullPath);
        while (StringUtil.endsWith(pi, GENERIC_PATH_SEPARATOR_CHAR)) {
            pi = pi.substring(0, pi.length() - 1);
        }
        int s = pi.lastIndexOf(GENERIC_PATH_SEPARATOR_CHAR);
        if (s == -1) {
            return fullPath;
        }
        return pi.substring(s + 1);
    }

    /**
     * Strips the file name from the end of a full path specification, leaving just the directory path to the file, if
     * any.
     *
     * @param fullPath
     *     the full path of the file.
     * @return the path component, if any.
     */
    public static final String stripFile(String fullPath) {
        if (fullPath == null) {
            return null;
        }
        String pi = platformIndependentPath(fullPath);
        while (StringUtil.endsWith(pi, GENERIC_PATH_SEPARATOR_CHAR)) {
            pi = pi.substring(0, pi.length() - 1);
        }
        int s = pi.lastIndexOf(GENERIC_PATH_SEPARATOR_CHAR);
        if (s == -1) {
            return "";
        }
        return fullPath.substring(0, s + 1);
    }

    /**
     * Returns true if the character represents a file separator for any known platforms or situations. This means
     * either a slash or backslash.
     *
     * @param c
     *     the character to check.
     * @return true if the character represents a file separator for any known platforms or situations; false otherwise.
     */
    public static final boolean isSeparator(char c) {
        return switch (c) {
            case GENERIC_PATH_SEPARATOR_CHAR, WINDOWS_PATH_SEPARATOR_CHAR -> true;
            default -> false;
        };
    }

    /**
     * Joins a name with a folder to create a complete path using the separator. If the folder name already ends in
     * separator, another separator will not be added.
     */
    public static final String joinPath(String folder, String name, String sep) {
        return folder.endsWith(sep) ? folder + name : folder + sep + name;
    }

    public static final String getExtension(String fileNameOrPath) {
        return getExtension(fileNameOrPath, null);
    }

    /**
     * Returns the file extension for the given file name or path, not including the period, or the given default if no
     * extension is identified.
     *
     * <p>
     * This method will return anything after the final "." in the filename portion of the given file name or path
     * (including returning the empty String if the file name or path ends in a dot). If no extension is found, the
     * given default value is returned instead.
     *
     * <p>
     * If the given name or path String is a path ending with a trailing slash, that will be treated as a directory, and
     * thus any "extension" in that directory name -- e.g., "baz" in the path "/foo/bar.baz/" -- will not be returned.
     * The extension must be identified before the last file path separator.
     *
     * <p>
     * In general, no validation is performed on the given file name or path. Identification of a file path separator is
     * platform-independent (i.e., both "/" and "\" are handled).
     *
     * @param fileNameOrPath
     *     the file name, possibly in the form of a path, whose extension is to be identified.
     * @param defaultValue
     *     the value to be returned if there is no extension found.
     * @return the file extension for the given file name or path if there is one, including empty String for a name or
     *     path ending in "."; the given default value otherwise.
     * @see #stripExtension(String)
     */
    public static final String getExtension(String fileNameOrPath, String defaultValue) {

        if (fileNameOrPath == null) {
            return defaultValue;
        }

        int lastDot = fileNameOrPath.lastIndexOf(EXTENSION_SEPARATOR_CHAR);
        if (lastDot == -1) {
            // No extension.
            return defaultValue;
        }

        int lastSep = StringUtils.lastIndexOfAny(fileNameOrPath, URLUtil.PATH_SEPARATOR, File.separator);
        if (lastSep != -1 && lastDot < lastSep) {
            // no extension after last separator.
            return defaultValue;
        }
        if (lastDot == fileNameOrPath.length() - 1) {
            return "";
        }
        return fileNameOrPath.substring(lastDot + 1);

    }

    /**
     * Returns the given file path, with the file extension, if any (including the period), removed.
     *
     * <p>
     * This method has the same logic as {@link #getExtension(String, String)} for what constitutes an extension.
     *
     * @param fileNameOrPath
     *     the file name, possibly in the form of a path, whose extension is to be identified.
     * @return the given file path, with the file extension, if any (including the period), removed; the given file path
     *     as-is otherwise.
     * @see #getExtension(String, String)
     */
    public static final String stripExtension(String fileNameOrPath) {

        if (fileNameOrPath == null) {
            return null;
        }

        int lastDot = fileNameOrPath.lastIndexOf(EXTENSION_SEPARATOR_CHAR);
        if (lastDot == -1) {
            // No extension.
            return fileNameOrPath;
        }

        int lastSep = StringUtils.lastIndexOfAny(fileNameOrPath, URLUtil.PATH_SEPARATOR, File.separator);
        if (lastSep != -1 && lastDot < lastSep) {
            // no extension after last separator.
            return fileNameOrPath;
        }
        return fileNameOrPath.substring(0, lastDot);

    }

    /**
     * Returns the given path with any leading separator removed if it's present. "Separator" here covers either the
     * slash or backslash.
     *
     * @param path
     *     the path to transform.
     * @return the given path with a leading separator removed if it's present; otherwise, the given path as-is.
     */
    public static final String removeLeadingSeparator(String path) {
        int len = StringUtils.length(path);
        if (len == 0) {
            return path;
        }
        if (isSeparator(path.charAt(0))) {
            return path.substring(1, len);
        }
        return path;
    }

    /**
     * Returns the given path with any trailing separator removed if it's present. "Separator" here covers either the
     * slash or backslash.
     *
     * @param path
     *     the path to transform.
     * @return the given path with a trailing separator removed if it's present; otherwise, the given path as-is.
     */
    public static final String removeTrailingSeparator(String path) {
        int len = StringUtils.length(path);
        if (len == 0) {
            return path;
        }
        boolean removeTrailing = isSeparator(path.charAt(len - 1));
        if (removeTrailing) {
            return path.substring(0, len - 1);
        }
        return path;
    }

    /**
     * Returns the given path with any leading and trailing separators removed if they're present. "Separator" here
     * covers either the slash or backslash.
     *
     * @param path
     *     the path to transform.
     * @return the given path with any leading and trailing separators removed if they're present; otherwise, the given
     *     path as-is.
     */
    public static final String removeLeadingAndTrailingSeparators(String path) {

        int len = StringUtils.length(path);
        if (len == 0) {
            return path;
        }

        boolean removeLeading = isSeparator(path.charAt(0));
        if (len == 1) {
            if (removeLeading) {
                return "";
            }
            return path;
        }

        boolean removeTrailing = isSeparator(path.charAt(len - 1));
        if (removeLeading || removeTrailing) {
            return path.substring(removeLeading ? 1 : 0, removeTrailing ? len - 1 : len);
        }
        return path;

    }

    public static final String platformSpecificPath(@Nullable String path) {
        if (GENERIC_PATH_SEPARATOR_CHAR == File.separatorChar) {
            return path;
        }
        return CharBasedFilteringTextMapper.replace(path, GENERIC_TO_WINDOWS_SEPARATOR_REPLACEMENT);
    }

    public static final void appendPlatformSpecificPath(@Nonnull Appendable out, @Nullable CharSequence path) {
        Objects.requireNonNull(out, "output");
        if (GENERIC_PATH_SEPARATOR_CHAR == File.separatorChar) {
            StringWriteUtil.safeAppend(out, path);
        }
        else {
            CharBasedFilteringTextMapper.replace(path, out, GENERIC_TO_WINDOWS_SEPARATOR_REPLACEMENT);
        }
    }

    public static final String platformIndependentPath(@Nullable String fileNameOrPath) {

        if (fileNameOrPath == null) {
            return null;
        }

        int l = fileNameOrPath.length();

        StringBuilder ret = null;
        for (int i = 0; i < l; i++) {
            char c = fileNameOrPath.charAt(i);
            switch (c) {
            case ':':
                // either a mac directory separator or a DOS "c:\" drive specifier
                if (i + 1 < l &&
                    (fileNameOrPath.charAt(i + 1) == WINDOWS_PATH_SEPARATOR_CHAR ||
                     fileNameOrPath.charAt(i + 1) == GENERIC_PATH_SEPARATOR_CHAR)) {
                    if (ret != null) {
                        ret.append(':'); // keep the colon
                    }
                }
                else {
                    if (ret == null) {
                        ret = new StringBuilder(l);
                        ret.append(fileNameOrPath, 0, i);
                    }
                    ret.append(GENERIC_PATH_SEPARATOR_CHAR); // mac separator
                }
                break;
            case WINDOWS_PATH_SEPARATOR_CHAR:  // windows
                if (ret == null) {
                    ret = new StringBuilder(l);
                    ret.append(fileNameOrPath, 0, i);
                }
                ret.append(GENERIC_PATH_SEPARATOR_CHAR);
                break;
            case GENERIC_PATH_SEPARATOR_CHAR:   // Unix (no change)
                if (ret != null) {
                    ret.append(GENERIC_PATH_SEPARATOR_CHAR);
                }
                break;
            default:
                if (ret != null) {
                    ret.append(c);
                }
                break;
            }
        }
        if (ret == null) {
            return fileNameOrPath;
        }
        return ret.toString();
    }

    /**
     * Checks whether the given file name is illegal or discouraged. Although the
     *
     * @param fileName
     *     the file name to check.
     * @return a FileNameValidityCheckResult representing the result of examining the given file name.
     */
    @Beta
    @Nonnull
    public static final FileNameCheckResult checkFileName(@Nullable String fileName) {

        if (StringUtils.isBlank(fileName)) {
            return FileNameCheckResult.EMPTY_OR_BLANK;
        }

        if (fileName.length() > FILE_NAME_LENGTH_LIMIT) {
            return FileNameCheckResult.TOO_LONG;
        }

        if (ILLEGAL_FILENAME_CHARACTERS.matchesAnyOf(fileName) ||
            fileName.chars().anyMatch(ILLEGAL_FILENAME_CODEPOINTS)) {
            return FileNameCheckResult.ILLEGAL_CHARACTERS;
        }

        if (fileName.equals(CURRENT_PATH_INDICATOR) || fileName.equals(CONSECUTIVE_DOTS)) {
            return FileNameCheckResult.ILLEGAL_FILENAME;
        }

        if (isSpecialIllegalWindowsFileName(fileName)) {
            return FileNameCheckResult.ILLEGAL_SPECIAL_WINDOWS_FILENAME;
        }

        if (fileName.contains(CONSECUTIVE_DOTS)) {
            return FileNameCheckResult.ILLEGAL_DOT_SEQUENCE;
        }

        if (DISCOURAGED_FILENAME_CHARACTERS.matchesAnyOf(fileName) ||
            fileName.chars().anyMatch(DISCOURAGED_FILENAME_CODEPOINTS)) {
            return FileNameCheckResult.DISCOURAGED_CHARACTERS;
        }

        if (StringUtil.hasCollapsableOrNormalizableWhitespace(fileName, false)) {
            return FileNameCheckResult.DISCOURAGED_WHITESPACE_SEQUENCE;
        }

        int lastCodePoint = fileName.codePointBefore(fileName.length());
        if (lastCodePoint == EXTENSION_SEPARATOR_CHAR || Character.isWhitespace(lastCodePoint)) {
            return FileNameCheckResult.DISCOURAGED_ENDS_WITH_EXTENSION_SEPARATOR;
        }

        if (isDangerousUnixFileName(fileName)) {
            return FileNameCheckResult.DISCOURAGED_UNIX_FILENAME;
        }

        return FileNameCheckResult.NO_PROBLEMS;

    }

    /**
     * Returns a "good" file name for the given name. Attempts are made to remove or replace illegal portions of the
     * name so that the result can be as close to the originally supplied version as possible, but as a last result, a
     * default name will be used based on the given Content-Type specification, if any (generally "Untitled" followed by
     * a suitable extension if one can be determined).
     *
     * <p>
     * Specifically, the file name is guaranteed to be a valid name for a file stored in a file system, or in some other
     * sort of server side repository.
     *
     * <p>
     * At the time of writing, the following characters are illegal:
     *
     * <ul>
     * <li>&lt; (less than)</li>
     * <li>&gt; (greater than)</li>
     * <li>: (colon)</li>
     * <li>; (semicolon)</li>
     * <li>double quotation mark</li>
     * <li>forward slash</li>
     * <li>backward slash</li>
     * <li>vertical pipe</li>
     * <li>question mark</li>
     * <li>asterisk</li>
     * <li>characters 0 through 31 (0x0 through 0x1f), including various control characters and other whitespace
     * characters, such as new line and carriage return</li>
     * </ul>
     *
     * <p>
     * Besides individual characters, the following are also illegal:
     *
     * <ul>
     * <li>consecutive file extension separators (.)</li>
     * <li>alternative whitespace (see {@link StringUtil#isAlternativeWhitespaceCodePoint(int)})</li>
     * <li>ending with a file extension separator (.) or whitespace</li>
     * <li>case-sensitive matches for any of the proscribed file names for Windows operating systems, such as "CON",
     * "PRN" or "NUL", with or without a file extension (e.g., "nul", "NUL", or "NUL.txt")</li>
     * </ul>
     *
     * <p>
     * See <a href="https://learn.microsoft.com/en-us/windows/win32/fileio/naming-a-file">Windows &gt; Apps &gt; Win32
     * &gt; Desktop &gt; Technologies &gt; Data Access and Storage &gt; Local File Systems &gt; Naming Files, Paths, and
     * Namespaces</a>.
     *
     * @param fileName
     *     the file name from which a "good" file name will be determined.
     * @param getDefaultBaseName
     *     an optional Supplier for a preferred base file name in case the supplied name cannot be repaired. The
     *     argument for this parameter may be null, or it may return a null or empty value; in either case, a localized
     *     default base file name will be used (generally, "Untitled").
     * @param contentType
     *     the supplied Content-Type or MIME type of the file.
     * @return a file name guaranteed to be a valid name for a file stored in a file system, or in some other sort of
     *     sort of server side repository, and which has been otherwise normalized as applicable.
     * @see #getGoodNormalizedFileName(String, Supplier, String, boolean)
     */
    public static final String getGoodFileName(@Nullable String fileName, @Nullable Supplier<String> getDefaultBaseName, @Nullable String contentType) {
        String validFileName = getGoodFileNameImpl(fileName);
        if (validFileName == null) {
            return getDefaultGoodFileName(getDefaultBaseName, contentType);
        }
        return validFileName;
    }

    /**
     * A more aggressive alternative to {@link #getGoodFileName(String, Supplier, String)} that will apply additional
     * normalizations, such as collapsing consecutive space characters, removing whitespace before the file extension
     * and optionally ensuring the presence of some sort of file extension.
     *
     * @param fileNameOrPath
     *     the file name or path from which a validated file name will be determined.
     * @param getDefaultBaseName
     *     an optional Supplier for a preferred base file name in case the supplied name cannot be repaired. The
     *     argument for this parameter may be null, or it may return a null or empty value; in either case, a localized
     *     default base file name will be used (generally, "Untitled").
     * @param contentType
     *     the supplied Content-Type or MIME type of the file.
     * @param ensureExtension
     *     indicates whether the name should definitely have a suitable file extension, possibly replacing one that may
     *     already be present. This will be ignored for "dot files" (i.e., those whose names start with '.').
     * @return a valid and normalized file name extracted from the given file name or path.
     */
    public static final String getGoodNormalizedFileName(@Nullable String fileNameOrPath, @Nullable Supplier<String> getDefaultBaseName, @Nullable String contentType, boolean ensureExtension) {

        String fileName = getGoodFileName(fileNameOrPath, getDefaultBaseName, contentType);
        fileName = StringUtils.trimToNull(StringUtil.collapseAndNormalizeWhitespace(fileName, true));

        // Check again for a blank (null/empty/whitespace) file name.
        if (StringUtils.isBlank(fileName)) {
            return getDefaultGoodFileName(contentType);
        }

        String ext = getExtension(fileName, null);
        if (StringUtils.isNotBlank(ext)) {
            return stripExtension(fileName).trim() + EXTENSION_SEPARATOR_CHAR + ext;
        }

        // For file names that are not "dot files", try to ensure they have a reasonable file extension, if requested.
        if (ensureExtension && fileName.charAt(0) != EXTENSION_SEPARATOR_CHAR) {
            if (StringUtils.isBlank(ext)) {
                // Guess the file extension based upon the supplied Content-Type.
                ext = MediaTypeUtil.getExtensionFromContentType(contentType);
                if (StringUtils.isNotBlank(ext)) {
                    return fileName + EXTENSION_SEPARATOR_CHAR + ext;
                }
            }
        }

        return fileName;

    }

    public static final String getDefaultGoodFileName(@Nullable String contentType) {
        return getDefaultGoodFileName(null, contentType);
    }

    public static final String getDefaultGoodFileName(@Nullable Supplier<String> getDefaultBaseName, @Nullable String contentType) {
        String ext = MediaTypeUtil.getExtensionFromContentType(contentType);
        String baseName = getDefaultBaseFileName(getDefaultBaseName);
        if (StringUtils.isBlank(ext)) {
            return baseName;
        }
        return baseName + EXTENSION_SEPARATOR_CHAR + ext;
    }

    public static final String getDescendantPathSpec(@Nullable String directoryPath, String... pathComponents) {

        if (directoryPath == null) {
            return StringUtil.join(pathComponents, File.separator);
        }

        if (ArrayUtils.isEmpty(pathComponents)) {
            if (directoryPath.isEmpty()) {
                return CURRENT_PATH_INDICATOR;
            }
            return platformSpecificPath(directoryPath);
        }

        StringBuilder spec = new StringBuilder(50);
        appendPlatformSpecificPath(spec, directoryPath);
        if (!StringUtil.endsWith(spec, File.separatorChar)) {
            spec.append(File.separatorChar);
        }
        StringUtil.getNullSkippingJoiner(File.separatorChar).appendTo(spec, pathComponents);
        return spec.toString();

    }

    private static final String getDefaultBaseFileName() {
        return System.getProperty("com.tractionsoftware.commons.io.default_base_file_name", "Untitled");
    }

    private static final String getDefaultBaseFileName(@Nullable Supplier<String> getDefaultBaseName) {
        if (getDefaultBaseName == null) {
            return getDefaultBaseFileName();
        }
        String baseName = StringUtils.trimToNull(getDefaultBaseName.get());
        if (baseName == null) {
            return getDefaultBaseFileName();
        }
        return baseName;
    }

    private static final String getGoodFileNameImpl(String fileName) {

        if (StringUtils.isBlank(fileName)) {
            return null;
        }

        fileName = StringUtil.normalizeAlternativeWhitespace(fileName);
        fileName = CharBasedFilteringTextMapper.replace(fileName, ILLEGAL_OR_DISCOURAGED_FILENAME_CHAR_REPLACER);
        fileName = StringUtil.collapseConsecutiveCharacters(fileName, EXTENSION_SEPARATOR_CHAR);
        fileName = fileName.trim();
        if (fileName.isEmpty()) {
            return null;
        }

        int[] codePoints = fileName.codePoints().toArray();
        int removeCharsFromEnd = 0;
        for (int index = codePoints.length - 1; index >= 0; index--) {
            int codePoint = codePoints[index];
            if (codePoint == EXTENSION_SEPARATOR_CHAR || Character.isWhitespace(codePoint)) {
                removeCharsFromEnd += Character.charCount(codePoint);
            }
            else {
                break;
            }
        }

        int len = fileName.length();
        if (removeCharsFromEnd > 0) {
            if (removeCharsFromEnd == len) {
                return null;
            }
            fileName = fileName.substring(0, len - removeCharsFromEnd);
        }

        if (isSpecialIllegalWindowsFileName(fileName)) {
            return null;
        }

        return fileName;

    }

}
