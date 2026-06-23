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

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import com.tractionsoftware.commons.lang.Resource;
import com.tractionsoftware.commons.lang.StringUtil;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.io.filefilter.RegexFileFilter;
import org.apache.commons.io.filefilter.WildcardFileFilter;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * Utilities related to java {@link File}s and paths.
 *
 * @author Andy Keller, Dave Shepperton
 */
public final class FileUtil {

    private FileUtil() {
    }

    private static final Logger LOGGER = LoggerFactory.getLogger(FileUtil.class.getName());

    private static final String SYSTEM_PROPERTY_NAME_TEMP_DIRECTORY = "java.io.tmpdir";

    private static final String SYSTEM_PROPERTY_NAME_WORKING_DIRECTORY = "user.dir";

    /**
     * The different types of results of an attempt to delete a file or directory.
     */
    public static enum DeleteRequestStatus {

        NO_FILE_REQUESTED(false),

        NO_SUCH_FILE(false),

        SUCCESS_IMMEDIATE(false),

        SUCCESS_ON_EXIT(false),

        FAILURE_SECURITY(true),

        FAILURE_NON_EMPTY_DIRECTORY(true),

        FAILURE_OTHER(true);

        private final boolean representsFailure;

        DeleteRequestStatus(boolean representsFailure) {
            this.representsFailure = representsFailure;
        }

        public final boolean failed() {
            return representsFailure;
        }

    }

    /**
     * A {@link FileFilter} for including files that have a given file extension. It will never match directories.
     */
    private static final class ExtFileFilter implements FileFilter {

        private final Set<String> extensions;

        private ExtFileFilter(Set<String> extensions) {
            Objects.requireNonNull(extensions, "extensions Set");
            if (extensions.isEmpty()) {
                LOGGER.warn(
                    "This FileFilter will not match any files because there are no extensions specified",
                    new IllegalArgumentException()
                );
            }
            this.extensions = ImmutableSet.copyOf(extensions);
        }

        @Override
        public final boolean accept(File pathname) {
            if (pathname.isDirectory()) {
                return false;
            }
            return hasIncludedExtension(pathname.getName());
        }

        private final boolean hasIncludedExtension(String name) {
            String ext = FileNameUtil.getExtension(name, null);
            if (ext == null) {
                return false;
            }
            if (extensions.contains(ext)) {
                return true;
            }
            return false;
        }

    }

    public static final Comparator<File> LAST_MODIFIED_ORDER_ASCENDING =
        Comparator.comparing(File::lastModified);

    public static final Comparator<File> LAST_MODIFIED_ORDER_DESCENDING =
        LAST_MODIFIED_ORDER_ASCENDING.reversed();

    /**
     * Returns true if the target {@link File} represents an {@link File#exists() existing}
     * {@link File#canRead() readable} {@link File#isFile() file} (not a {@link File#isDirectory() directory}), or a
     * non-existent file location where a file would be readable if it did exist. Specifically, this returns true for a
     * non-null argument in either of two cases:
     *
     * <ul>
     * <li>The file exists, is already a file (as opposed to a directory), and is readable.</li>
     * <li>The file does not exist, but the parent directory does exist and is readable.</li>
     * </ul>
     *
     * @param file
     *     the {@link File} to test.
     * @return true if the target {@link File} represents an {@link File#exists() existing}
     *     {@link File#canRead() readable} {@link File#isFile() file} (not a {@link File#isDirectory() directory}), or a
     *     non-existent file location where a file would be readable if it did exist; false otherwise.
     */
    public static final boolean fileIsOrWouldBeReadable(@Nullable File file) {
        return isOrWouldBeAllowed(file, File::isFile, File::canRead);
    }

    /**
     * Returns true if the target {@link File} represents an {@link File#exists() existing}
     * {@link File#canRead() readable} {@link File#isDirectory() directory} (not a {@link File#isFile() file}), or a
     * non-existent file location where a directory would be readable if it did exist. Specifically, this returns true
     * for a non-null argument in either of two cases:
     *
     * <ul>
     * <li>The file exists, is already a directory (as opposed to a file), and is readable.</li>
     * <li>The file does not exist, but the parent directory does exist and is readable.</li>
     * </ul>
     *
     * @param file
     *     the {@link File} to test.
     * @return true if the target {@link File} represents an {@link File#exists() existing}
     *     {@link File#canRead() readable} {@link File#isDirectory() directory} (not a {@link File#isFile() file}), or a
     *     non-existent file location where a directory would be readable if it did exist; false otherwise.
     */
    public static final boolean directoryIsOrWouldBeReadable(@Nullable File file) {
        return isOrWouldBeAllowed(file, File::isDirectory, File::canRead);
    }

    /**
     * Returns true if the target {@link File} represents an {@link File#exists() existing}
     * {@link File#canWrite() writable} {@link File#isFile() file} (not a {@link File#isDirectory() directory}), or a
     * non-existent file location where a file would be writable if it did exist. This is a best-efforts basis check,
     * because there is no way to be certain that a file system write operation will succeed without actually performing
     * it. Specifically, this returns true for a non-null argument in either of two cases:
     *
     * <ul>
     * <li>The file exists, is already a file (as opposed to a directory), and is writable.</li>
     * <li>The file does not exist, but the parent directory does exist and is writable.</li>
     * </ul>
     *
     * @param file
     *     the {@link File} to test.
     * @return true if the target {@link File} represents an {@link File#exists() existing}
     *     {@link File#canWrite() writable} {@link File#isFile() file} (not a {@link File#isDirectory() directory}), or
     *     a non-existent file location where a file would be writable if it did exist; false otherwise.
     */
    public static final boolean fileIsOrWouldBeWritable(@Nullable File file) {
        return isOrWouldBeAllowed(file, File::isFile, File::canWrite);
    }

    /**
     * Returns true if the target {@link File} represents an {@link File#exists() existing}
     * {@link File#canWrite() writable} {@link File#isDirectory() directory} (not a {@link File#isFile() file}), or a
     * non-existent file location where a directory could be created and be writable if it did exist. This is a
     * best-efforts basis check, because there is no way to be certain that a file system write operation will succeed
     * without actually performing it. Specifically, this returns true for a non-null argument in either of two cases:
     *
     * <ul>
     * <li>The file exists, is already a directory (as opposed to a file), and is writable.</li>
     * <li>The file does not exist, but the parent directory does exist and is writable.</li>
     * </ul>
     *
     * @param file
     *     the {@link File} to test.
     * @return true if the target {@link File} represents an {@link File#exists() existing}
     *     {@link File#canWrite() writable} {@link File#isDirectory() directory} (not a {@link File#isFile() file}), or
     *     a non-existent file location where a directory could be created and be writable if it did exist; false
     *     otherwise.
     */
    public static final boolean directoryIsOrWouldBeWritable(@Nullable File file) {
        return isOrWouldBeAllowed(file, File::isDirectory, File::canWrite);
    }

    /**
     * Returns the relative path of the given file with respect to the given root, or the original given file path if it
     * is not contained by the given root path.
     *
     * <p>
     * This method does not include any path canonicalization. It defers to {@link File#toURI()} to turn the given file
     * paths into absolute paths; to {@link URI#relativize(URI)} to create a URI representing the relative path; and to
     * {@link URI#getPath()} to return the (non-encoded) form of that path. If the file does not have a path that is
     * relative to the given root, the original path will be returned as-is. null arguments are handled gracefully.
     *
     * <p>
     * The intention is to handle paths like this:
     *
     * <pre>
     * root: /apps/traction/server
     * file: /apps/traction/server/config/user/directories/ad.properties
     * result: config/user/directories/ad.properties
     * </pre>
     *
     * @param root
     *     representing the root path, with respect to which the given file's relative path should be determined.
     * @param file
     *     representing the file path whose relative form should be determined.
     * @return the relative path of the given file with respect to the given root, or the original given file path if it
     *     is not contained by the given root path.
     */
    public static final String getRelativePath(@Nullable File root, @Nullable File file) {
        if (root == null) {
            if (file == null) {
                return null;
            }
            return file.getPath();
        }
        if (file == null) {
            return null;
        }
        return root.toURI().relativize(file.toURI()).getPath();
    }

    /**
     * Deletes the specified file or directory. If the directory is not empty, it removes all of its contents before
     * removing the directory.
     *
     * @throws IOException
     *     if the delete operation fails. In the case of a directory, some files may have been deleted.
     * @throws NullPointerException
     *     if the argument for the {@link File} is null.
     * @see FileUtils#deleteDirectory(File)
     * @see Files#delete(Path)
     */
    public static final void delete(@Nonnull File file) throws IOException {
        Objects.requireNonNull(file, "file");
        if (file.isDirectory()) {
            FileUtils.deleteDirectory(file);
        }
        else {
            Files.delete(file.toPath());
        }
    }

    @Nonnull
    public static final List<File> delete(@Nonnull Iterable<File> files) {
        Objects.requireNonNull(files, "files");
        ImmutableList.Builder<File> notDeleted = ImmutableList.builder();
        for (File file : files) {
            try {
                Files.delete(file.toPath());
            }
            catch (IOException | SecurityException e) {
                notDeleted.add(file);
            }
        }
        return notDeleted.build();
    }

    @Nonnull
    public static final List<File> listDirs(@Nonnull File file) {
        Objects.requireNonNull(file, "file");
        File[] directories = file.listFiles(File::isDirectory);
        if (directories == null) {
            return List.of();
        }
        return Arrays.asList(directories);
    }

    /**
     * Returns all of the files in the specified folder with the specified extension.
     */
    public static final Collection<File> findFilesByExtension(@Nonnull File directory, @Nonnull String matchExtension, boolean recurse) {
        Objects.requireNonNull(directory, "directory");
        Objects.requireNonNull(matchExtension, "extension");
        return FileUtils.listFiles(directory, new String[] { matchExtension }, recurse);
    }

    /**
     * Returns {@link File#getCanonicalFile() the canonicalized form} of the given {@link File} if possible.
     *
     * @param file
     *     the File to be canonicalized.
     * @return {@link File#getCanonicalFile() the canonicalized form} of the given {@link File} if possible; the given
     *     File otherwise.
     * @throws NullPointerException
     *     if the argument for the {@link File} is null.
     */
    public static final File getCanonicalFile(@Nonnull File file) {
        Objects.requireNonNull(file, "file");
        try {
            return file.getCanonicalFile();
        }
        catch (IOException | SecurityException e) {
            LOGGER.warn("Failed to canonicalize {} ", file, e);
        }
        return file;
    }

    /**
     * Checks whether it can be determined that the given file represents a descendant file of the given directory,
     * either using {@link File#getAbsoluteFile() absolute} and {@link Path#normalize() normalized} path forms or
     * canonicalized forms.
     *
     * <p>
     * Note: the way this method works handles symlinks in a very particular way. For a symlink at /foo/bar linking to
     * /foo/bar, this method will return true in all three of these cases:
     *
     * <ul>
     * <li>directory = /foo, file = /foo/bar: just by comparing the paths, this is a match.</li>
     * <li>directory = /baz, file = /baz/bar: also just by comparing the paths, this is a match.</li>
     * <li>directory = /baz, file = /foo/bar: because /foo/bar links to /baz, which contains the path /baz/bar, the
     * comparison that uses canonicalized forms will consider this a match.</li>
     * </ul>
     *
     * <p>
     * If this behavior is not desirable, use {@link #isDescendantCanonical(File, File)}.
     *
     * <p>
     * This method will also return false in the unlikely event that the an IOException or SecurityException is raised
     * while checking the canonical path forms.
     *
     * @param directory
     *     a {@link File} representing the directory.
     * @param file
     *     a {@link File} representing the file whose path is to be checked against the directory.
     * @return true if neither argument is null and if it can be determined that the given file represents a descendant
     *     file of the given directory.
     */
    public static final boolean isDescendant(@Nullable File directory, @Nullable File file) {

        if (directory == null || file == null) {
            return false;
        }

        Path absNormDirectoryPath = toAbsoluteNormalizedPath(directory);
        Path absNormFilePath = toAbsoluteNormalizedPath(file);
        if (absNormFilePath.startsWith(absNormDirectoryPath)) {
            return true;
        }

        try {
            return isDescendantForAlreadyCanonicalFilesImpl(
                absNormDirectoryPath.toFile().getCanonicalFile(), absNormFilePath.toFile().getCanonicalFile()
            );
        }
        catch (IOException | SecurityException e) {
            LOGGER.warn("isDescendant encountered an error ({} vs {})", directory, file, e);
        }
        return false;

    }

    /**
     * Checks whether it can be determined that the given file represents the same file or a descendant file of the
     * given directory, either using {@link File#getAbsoluteFile() absolute} and {@link Path#normalize() normalized}
     * path forms or canonicalized forms.
     *
     * <p>
     * Note: the way this method works handles symlinks in a very particular way. For a symlink at /foo/bar linking to
     * /foo/bar, this method will return true in all three of these cases:
     *
     * <ul>
     * <li>directory = /foo, file = /foo/bar: just by comparing the paths, this is a match.</li>
     * <li>directory = /baz, file = /baz/bar: also just by comparing the paths, this is a match.</li>
     * <li>directory = /baz, file = /foo/bar: because /foo/bar links to /baz, which contains the path /baz/bar, the
     * comparison that uses canonicalized forms will consider this a match.</li>
     * </ul>
     *
     * <p>
     * If this behavior is not desirable, use {@link #isOrIsDescendantCanonical(File, File)}.
     *
     * <p>
     * This method will also return false in the unlikely event that the an IOException or SecurityException is raised
     * while checking the canonical path forms.
     *
     * @param directory
     *     a {@link File} representing the directory.
     * @param file
     *     a {@link File} representing the file whose path is to be checked against the directory.
     * @return true if if neither argument is null and it can be determined that the given file represents the same file
     *     or a descendant file of the given directory; false otherwise.
     */
    public static final boolean isOrIsDescendant(@Nullable File directory, @Nullable File file) {

        if (directory == null || file == null) {
            return false;
        }

        Path absNormDirectoryPath = toAbsoluteNormalizedPath(directory);
        Path absNormFilePath = toAbsoluteNormalizedPath(file);
        if (absNormFilePath.equals(absNormDirectoryPath) || absNormFilePath.startsWith(absNormDirectoryPath)) {
            return true;
        }

        directory = absNormDirectoryPath.toFile();
        file = absNormFilePath.toFile();

        try {
            return isOrIsDescendantForAlreadyCanonicalFilesImpl(
                absNormDirectoryPath.toFile().getCanonicalFile(), absNormFilePath.toFile().getCanonicalFile()
            );
        }
        catch (IOException | SecurityException e) {
            LOGGER.warn("isOrIsDescendant encountered an error ({} vs {}) ", directory, file, e);
        }
        return false;

    }

    /**
     * Checks whether it can be determined that the given file represents a descendant file of the given directory,
     * using only the {@link #getFileFilterByExtension(String) canonicalized forms} based on the
     * {@link File#getAbsoluteFile() absolute} {@link Path#normalize() normalized} forms.
     *
     * <p>
     * Note: the way this method works handles symlinks less permissively than {@link #isDescendant(File, File)}. For a
     * symlink at /foo/bar linking to /foo/bar, this method will return true in only two of these three of these cases:
     *
     * <ul>
     * <li>directory = /foo, file = /foo/bar: just by comparing the paths, this would be a match, but since the
     * canonicalized form of /foo/bar is /baz/bar, this is not a match.</li>
     * <li>directory = /baz, file = /baz/bar: also just by comparing the paths, this is a match.</li>
     * <li>directory = /baz, file = /foo/bar: because /foo/bar links to /baz, which contains the path /baz/bar, the
     * comparison that uses canonicalized forms will consider this a match.</li>
     * </ul>
     *
     * <p>
     * This method will also return false in the unlikely event that the an IOException or SecurityException is raised
     * while checking the canonical path forms.
     *
     * @param directory
     *     a {@link File} representing the directory.
     * @param file
     *     a {@link File} representing the file whose path is to be checked against the directory.
     * @return true if neither argument is null and if it can be determined that the given file represents a descendant
     *     file of the given directory.
     * @see #isDescendant(File, File)
     */
    public static final boolean isDescendantCanonical(@Nullable File directory, @Nullable File file) {

        if (directory == null || file == null) {
            return false;
        }

        try {
            return isDescendantForAlreadyCanonicalFilesImpl(
                toAbsoluteNormalizedPath(directory).toFile().getCanonicalFile(),
                toAbsoluteNormalizedPath(file).toFile().getCanonicalFile()
            );
        }
        catch (IOException | SecurityException e) {
            LOGGER.warn("isDescendantCanonical encountered an error ({} vs {})", directory, file, e);
        }
        return false;

    }

    /**
     * Checks whether it can be determined that the given file represents a descendant file of the given directory,
     * using only the {@link #getFileFilterByExtension(String) canonicalized forms} based on the
     * {@link File#getAbsoluteFile() absolute} {@link Path#normalize() normalized} forms.
     *
     * <p>
     * Note: the way this method works handles symlinks less permissively than {@link #isOrIsDescendant(File, File)}.
     * For a symlink at /foo/bar linking to /foo/bar, this method will return true in only two of these three of these
     * cases:
     *
     * <ul>
     * <li>directory = /foo, file = /foo/bar: just by comparing the paths, this would be a match, but since the
     * canonicalized form of /foo/bar is /baz/bar, this is not a match.</li>
     * <li>directory = /baz, file = /baz/bar: also just by comparing the paths, this is a match.</li>
     * <li>directory = /baz, file = /foo/bar: because /foo/bar links to /baz, which contains the path /baz/bar, the
     * comparison that uses canonicalized forms will consider this a match.</li>
     * </ul>
     *
     * <p>
     * This method will also return false in the unlikely event that the an IOException or SecurityException is raised
     * while checking the canonical path forms.
     *
     * @param directory
     *     a {@link File} representing the directory.
     * @param file
     *     a {@link File} representing the file whose path is to be checked against the directory.
     * @return true if neither argument is null and if it can be determined that the given file represents a descendant
     *     file of the given directory.
     */
    public static final boolean isOrIsDescendantCanonical(@Nullable File directory, @Nullable File file) {
        if (directory == null || file == null) {
            return false;
        }

        try {
            return isOrIsDescendantForAlreadyCanonicalFilesImpl(
                toAbsoluteNormalizedPath(directory).toFile().getCanonicalFile(),
                toAbsoluteNormalizedPath(file).toFile().getCanonicalFile()
            );
        }
        catch (IOException | SecurityException e) {
            LOGGER.warn("isDescendant encountered an error ({} vs {})", directory, file, e);
        }
        return false;
    }

    /**
     * Checks whether it can be determined that the given file represents or a descendant file of the given directory,
     * assuming that both are already in canonicalized forms.
     *
     * @param directoryCanonical
     *     a canonicalized {@link File} representing the directory.
     * @param fileCanonical
     *     a canonicalized {@link File} representing the file whose path is to be checked against the directory.
     * @return true if neither argument is null and if it can be determined that the given file represents a descendant
     *     file of the given directory, assuming that both are already in canonicalized form; false otherwise.
     * @throws IllegalArgumentException
     *     if one of the {@link File#getPath() files' paths} do not have {@link File#isAbsolute() absolute paths}, or
     *     contains any path segments matching "." or ".." indicating that it has not been canonicalized.
     */
    public static final boolean isDescendantForAlreadyCanonicalFiles(@Nullable File directoryCanonical, @Nullable File fileCanonical) {
        if (directoryCanonical == null || fileCanonical == null) {
            return false;
        }
        checkAbsoluteAndNoRelativeSegmentsX(directoryCanonical);
        checkAbsoluteAndNoRelativeSegmentsX(fileCanonical);
        if (fileCanonical.toPath().startsWith(directoryCanonical.toPath())) {
            return true;
        }
        return false;
    }

    /**
     * Checks whether it can be determined that the given file represents the same file or a descendant file of the
     * given directory, assuming that both are already in canonicalized forms.
     *
     * @param directoryCanonical
     *     a canonicalized {@link File} representing the directory.
     * @param fileCanonical
     *     a canonicalized {@link File} representing the file whose path is to be checked against the directory.
     * @return true if neither argument is null and if it can be determined that the given file represents the same file
     *     or a descendant file of the given directory, assuming that both are already in canonicalized form; false
     *     otherwise.
     * @throws IllegalArgumentException
     *     if one of the {@link File#getPath() files' paths} do not have {@link File#isAbsolute() absolute paths}, or
     *     contains any path segments matching "." or ".." indicating that it has not been canonicalized.
     */
    public static final boolean isOrIsDescendantForAlreadyCanonicalFiles(@Nullable File directoryCanonical, @Nullable File fileCanonical) {
        if (directoryCanonical == null || fileCanonical == null) {
            return false;
        }
        checkAbsoluteAndNoRelativeSegmentsX(directoryCanonical);
        checkAbsoluteAndNoRelativeSegmentsX(fileCanonical);
        return isOrIsDescendantForAlreadyCanonicalFilesImpl(directoryCanonical, fileCanonical);
    }

    /**
     * Checks whether it can be determined that the given {@link File}s represent the same actual file.
     *
     * @param file
     *     the first {@link File}.
     * @param other
     *     the other {@link File}.
     * @return neither {@link File} is null, and if it can be determined that the given {@link File}s represent the same
     *     actual file; false otherwise.
     */
    public static final boolean areSameFiles(@Nullable File file, @Nullable File other) {

        if (file == null || other == null) {
            return false;
        }
        if (file.equals(other)) {
            return true;
        }

        Path absNormFilePath = toAbsoluteNormalizedPath(file);
        Path absNormOtherPath = toAbsoluteNormalizedPath(other);
        if (absNormFilePath.equals(absNormOtherPath)) {
            return true;
        }

        try {
            if (absNormFilePath.toFile().getCanonicalPath().equals(absNormOtherPath.toFile().getCanonicalPath())) {
                return true;
            }
            return false;
        }
        catch (IOException | SecurityException e) {
            LOGGER.warn("areSameFiles encountered an error", e);
        }
        return false;

    }

    public static final boolean isDescendantPath(@Nullable String directoryPath, @Nullable String filePath) {
        if (directoryPath == null || filePath == null) {
            return false;
        }
        return isDescendant(new File(directoryPath), new File(filePath));
    }

    public static final boolean isOrIsDescendantPath(@Nullable String directoryPath, @Nullable String filePath) {
        if (directoryPath == null || filePath == null) {
            return false;
        }
        return isOrIsDescendant(new File(directoryPath), new File(filePath));
    }

    public static final boolean isDescendantPathCanonical(@Nullable String directoryPath, @Nullable String filePath) {
        if (directoryPath == null || filePath == null) {
            return false;
        }
        return isDescendantCanonical(new File(directoryPath), new File(filePath));
    }

    public static final boolean isOrIsDescendantPathCanonical(@Nullable String directoryPath, @Nullable String filePath) {
        if (directoryPath == null || filePath == null) {
            return false;
        }
        return isOrIsDescendantCanonical(new File(directoryPath), new File(filePath));
    }

    public static final boolean existsAndReadable(@Nullable File file) {
        if (file != null) {
            try {
                return file.exists();
            }
            catch (SecurityException e) {
                LOGGER.warn("File {}  may or may not exist, but is apparently not readable", file, e);
            }
        }
        return false;
    }

    /**
     * Moves a file using {@link Files#move(Path, Path, CopyOption...)}, replacing any existing file, as long as the
     * given source and destination Files are not already the same.
     *
     * @return true if the file was actually moved; false if the source and destination files were already the same.
     * @throws IOException
     *     if one is raised while attempting to perform the move operation.
     */
    public static final boolean move(@Nonnull File source, @Nonnull File dest) throws IOException {
        Objects.requireNonNull(source, "source file");
        Objects.requireNonNull(dest, "destination file");
        if (areSameFiles(source, dest)) {
            return false;
        }
        Files.move(source.toPath(), dest.toPath(), StandardCopyOption.REPLACE_EXISTING);
        return true;
    }

    public static final void checkAndCreateDirectory(String directoryPath) throws IOException {
        checkAndCreateDirectory(directoryPath, IOException.class);
    }

    public static <X extends Exception> void checkAndCreateDirectory(String directoryPath, Class<X> xcpType)
        throws X {
        if (directoryPath == null) {
            return;
        }
        checkAndCreateDirectory(new File(directoryPath), xcpType);
    }

    public static final void checkAndCreateDirectory(File directory) throws IOException {
        checkAndCreateDirectory(directory, IOException.class);
    }

    public static <X extends Exception> void checkAndCreateDirectory(File directory, Class<X> xcpType)
        throws X {

        if (directory.exists()) {
            if (directory.isDirectory()) {
                return;
            }
            if (xcpType != null) {
                throwException(
                    xcpType,
                    "The specified location (" + directory.getPath() + ") exists, but is not a directory."
                );
            }
            return;
        }

        if (directory.mkdirs()) {
            return;
        }

        if (xcpType != null) {
            throwException(
                xcpType,
                "Unable to create directory \"" + directory.getPath() + "\"."
            );
        }

    }

    public static final File createSystemTempFile(String prefix, String extension) throws IOException {
        if (StringUtils.isBlank(extension)) {
            return File.createTempFile(prefix, null);
        }
        return File.createTempFile(prefix, Strings.CS.prependIfMissing(extension, FileNameUtil.EXTENSION_SEPARATOR));
    }

    public static final File getCurrentDirectory() {
        return new File(FileNameUtil.CURRENT_PATH_INDICATOR);
    }

    /**
     * Returns a File for the directory that is determined to be the working directory for the current Java process.
     *
     * @return a File for the directory that is determined to be the working directory for the current Java process.
     */
    public static final File getWorkingDirectory() {
        String pathFromProperty = System.getProperty(SYSTEM_PROPERTY_NAME_WORKING_DIRECTORY);
        return new File(StringUtils.defaultIfBlank(pathFromProperty, "."));
    }

    public static final void setTempDirectory(@Nullable String directoryPath) {
        setTempDirectory(new File(StringUtils.defaultString(directoryPath)));
    }

    /**
     * Sets the temp directory by modifying the
     * {@link #SYSTEM_PROPERTY_NAME_TEMP_DIRECTORY java.io.tmpdir system property}.
     *
     * @param directory
     *     representing the new temp directory.
     * @throws IllegalArgumentException
     *     if the given {@link File} is not suitable for a temp directory.
     * @throws NullPointerException
     *     if the argument for the {@link File} is null.
     */
    public static final void setTempDirectory(@Nonnull File directory) {
        checkTempDirectoryX(directory);
        System.setProperty(SYSTEM_PROPERTY_NAME_TEMP_DIRECTORY, directory.getPath());
    }

    /**
     * Returns the {@link File} representing the specified location of the current temp directory, as read from the
     * {@link #SYSTEM_PROPERTY_NAME_TEMP_DIRECTORY java.io.tmpdir system property}.
     *
     * @return the {@link File} representing the specified location of the current temp directory, as read from the
     *     {@link #SYSTEM_PROPERTY_NAME_TEMP_DIRECTORY java.io.tmpdir system property}.
     * @throws Error
     *     if the temporary directory is not set.
     */
    public static final File getTempDirectory() {
        String defaultTempDirectoryPath = System.getProperty(SYSTEM_PROPERTY_NAME_TEMP_DIRECTORY);
        if (StringUtils.isNotBlank(defaultTempDirectoryPath)) {
            return new File(defaultTempDirectoryPath);
        }
        throw new Error("No temp directory has been set.");
    }

    /**
     * Attempts to delete a {@link File}, falling back to set it to be deleted on exit if that attempt fails.
     *
     * @param deleteTarget
     *     the {@link File} to delete. It must not be a directory.
     * @return a {@link DeleteRequestStatus} representing the result of the attempt.
     */
    @CanIgnoreReturnValue
    public static final DeleteRequestStatus deleteOrDeleteOnExit(File deleteTarget) {

        if (deleteTarget == null) {
            return DeleteRequestStatus.NO_FILE_REQUESTED;
        }

        try {
            // We check whether the file exists or not because NoSuchFileException is /optional/ for the Files.delete
            // method. This is not atomic now, because it's check-then-act, but in most cases, a file is only being
            // deleted by one caller at a time, and this is better than returning success when no delete operation
            // actually happened.
            if (deleteTarget.exists()) {
                Files.delete(deleteTarget.toPath());
                return DeleteRequestStatus.SUCCESS_IMMEDIATE;
            }
            return DeleteRequestStatus.NO_SUCH_FILE;
        }
        catch (SecurityException e) {
            LOGGER.warn("Failed to delete {}", deleteTarget, e);
            return DeleteRequestStatus.FAILURE_SECURITY;
        }
        catch (NoSuchFileException e) {
            return DeleteRequestStatus.NO_SUCH_FILE;
        }
        catch (DirectoryNotEmptyException e) {
            return deleteOnExit(deleteTarget, e, DeleteRequestStatus.FAILURE_NON_EMPTY_DIRECTORY);
        }
        catch (IOException e) {
            return deleteOnExit(deleteTarget, e, DeleteRequestStatus.FAILURE_OTHER);
        }

    }

    public static final FileFilter getFileFilterByExtension(@Nonnull String ext) {
        Objects.requireNonNull(ext, "extension");
        return getFileFilterByExtension(Collections.singleton(ext));
    }

    public static final FileFilter getFileFilterByExtension(@Nonnull Set<String> extensions) {
        Objects.requireNonNull(extensions, "extensions");
        return new ExtFileFilter(getExtensionsForExtensionFileFilter(extensions));
    }

    @Nonnull
    public static final IOFileFilter getFileNameBlobFilter(@Nonnull String fileNameMatcherBlob) {
        StringUtil.checkNotBlankX(fileNameMatcherBlob, "file name matcher blob");
        if (StringUtils.containsAny(
            fileNameMatcherBlob,
            FileNameUtil.GENERIC_PATH_SEPARATOR_CHAR,
            FileNameUtil.WINDOWS_PATH_SEPARATOR_CHAR,
            ':'
        )) {
            throw new IllegalArgumentException("Unsupported character in file name matcher blob.");
        }
        return WildcardFileFilter.builder().setWildcards(fileNameMatcherBlob).get();
    }

    @Nonnull
    public static final IOFileFilter getFileNamePatternFilter(@Nonnull String patternSpec) {
        StringUtil.checkNotBlankX(patternSpec, "file name pattern spec");
        return new RegexFileFilter(patternSpec);
    }

    /**
     * Returns a {@link BufferedInputStream} for the given {@link File}.
     *
     * @param file
     *     the {@link File}.
     * @return a {@link BufferedInputStream} for the given {@link File}.
     * @throws IOException
     *     if there is a problem opening the {@link InputStream} for the given {@link File}.
     * @throws NullPointerException
     *     if the argument for the {@link File} is null.
     */
    @Nonnull
    public static final BufferedInputStream getBufferedInputStream(@Nonnull File file) throws IOException {
        Objects.requireNonNull(file, "File");
        return new BufferedInputStream(Files.newInputStream(file.toPath()));
    }

    /**
     * Returns a {@link BufferedReader} for the given {@link File} using the UTF-8 charset.
     *
     * @param file
     *     the {@link File}.
     * @return a {@link BufferedReader} for the given {@link File} using the UTF-8 charset.
     * @throws IOException
     *     if there is a problem opening the underlying {@link InputStream} for the given {@link File}.
     * @throws NullPointerException
     *     if the argument for the {@link File} is null.
     */
    @Nonnull
    public static final BufferedReader getBufferedUtf8Reader(@Nonnull File file) throws IOException {
        Objects.requireNonNull(file, "File");
        return Files.newBufferedReader(file.toPath(), StandardCharsets.UTF_8);
    }

    /**
     * Returns a {@link BufferedOutputStream} for the given {@link File}.
     *
     * @param file
     *     the {@link File}.
     * @return a {@link BufferedOutputStream} for the given {@link File}.
     * @throws IOException
     *     if there is a problem opening the {@link OutputStream} for the given {@link File}.
     * @throws NullPointerException
     *     if the argument for the {@link File} is null.
     */
    @Nonnull
    public static final BufferedOutputStream getBufferedOutputStream(@Nonnull File file) throws IOException {
        Objects.requireNonNull(file, "File");
        return new BufferedOutputStream(Files.newOutputStream(file.toPath()));
    }

    /**
     * Returns a {@link BufferedWriter} that writes UTF-8 character data to the given {@link File}.
     *
     * @param file
     *     the target file.
     * @return a {@link BufferedWriter} that writes UTF-8 character data to the given {@link File}.
     * @throws IOException
     *     if there is a problem opening the file for writing.
     * @throws NullPointerException
     *     if the given file is null.
     */
    @Nonnull
    public static final BufferedWriter getBufferedUtf8Writer(@Nonnull File file) throws IOException {
        Objects.requireNonNull(file, "File");
        return Files.newBufferedWriter(file.toPath(), StandardCharsets.UTF_8);
    }

    /**
     * Returns a wrapped version of the given {@link File}'s {@link FileOutputStream} which incorporates buffering and
     * tracking.
     *
     * @param file
     *     the {@link File} to be read.
     * @return a wrapped version of the given {@link File}'s {@link FileOutputStream} which incorporates buffering and
     *     tracking.
     * @throws IOException
     *     if one is raised while attempting to create a {@link FileOutputStream}.
     * @throws NullPointerException
     *     if either argument is null.
     * @see #getBufferedOutputStream(File)
     * @see IOUtil#getTrackedOutputStream(OutputStream, Supplier)
     */
    @Nonnull
    public static final OutputStream getBufferedTrackedOutputStream(@Nonnull File file, @Nonnull Supplier<? extends Resource> getTracker)
        throws IOException {
        return IOUtil.getTrackedOutputStream(getBufferedOutputStream(file), getTracker, file.toString());
    }

    /**
     * Returns a {@link PrintWriter} that writes to the given {@link File}, wrapping a {@link BufferedWriter} that
     * writes UTF-8 character data. The returned object is NOT thread safe.
     *
     * @param file
     *     the target file.
     * @return a {@link PrintWriter} that writes to the given File, wrapping a {@link BufferedWriter} that writes UTF-8
     *     character data.
     * @throws IOException
     *     if there is a problem opening the file for writing.
     * @throws NullPointerException
     *     if the given file is null.
     */
    @Nonnull
    public static final PrintWriter getBufferedUtf8PrintWriter(@Nonnull File file) throws IOException {
        Writer w = getBufferedUtf8Writer(file);
        try {
            return SingleThreadPrintWriter.createInstance(w);
        }
        catch (RuntimeException | Error e) {
            IOUtils.closeQuietly(w);
            throw e;
        }
    }

    /**
     * Returns a {@link File} representing the effective target destination for an operation to move a file or
     * directory. The result will depend upon whether the requested destination is an existing directory, in order to
     * implement the <code>mv</code> command-line style logic: if the destination is a directory, the file or directory
     * being moved will be moved so that it is inside of the target directory; and if the destination is a file or does
     * not exist, the file or directory being moved will take that exact name and location.
     *
     * @param requestedDestination
     *     the {@link File} representing the requested destination path for the move operation.
     * @param fileName
     *     the existing name (not path) of the file to be moved.
     * @return a {@link File} representing the effective target destination for an operation to move a file or
     *     directory.
     * @throws NullPointerException
     *     if either argument is null.
     * @throws IllegalArgumentException
     *     if the given file name is not {@link FileNameUtil#checkFileName(String) minimally valid} as a normal file
     *     name.
     * @throws SecurityException
     *     if there is a security manager installed that prevents the requested destination file from being read.
     */
    @Nonnull
    public static final File getMoveDestination(@Nonnull File requestedDestination, @Nonnull String fileName)
        throws SecurityException {
        Objects.requireNonNull(requestedDestination, "destination");
        Objects.requireNonNull(fileName, "file name");
        FileNameUtil.FileNameCheckResult result = FileNameUtil.checkFileName(fileName);
        if (result.hasProblem()) {
            LOGGER.debug("move destination file name {} => {}", fileName, result);
            throw new IllegalArgumentException(String.format("The file name '%s' is not valid.", fileName));
        }
        if (requestedDestination.exists() && requestedDestination.isDirectory()) {
            return new File(requestedDestination, fileName);
        }
        return requestedDestination;
    }

    @Nonnull
    public static final File getDescendant(@Nonnull File directory, @Nonnull String... pathComponents) {
        Objects.requireNonNull(directory, "directory");
        Objects.requireNonNull(pathComponents, "path components");
        File file = directory;
        for (String pathComponent : pathComponents) {
            FileNameUtil.FileNameCheckResult result = FileNameUtil.checkFileName(pathComponent);
            if (result.hasProblem()) {
                LOGGER.debug("Path component {} => {}", pathComponent, result);
                throw new IllegalArgumentException(String.format(
                    "The path component '%s' is not allowed.",
                    pathComponent
                ));
            }
            file = new File(file, pathComponent);
        }
        return file;
    }

    @Nonnull
    public static final FileFilter getFindFilesByExtensionFilter(@Nonnull String matchExtension, boolean recurse) {
        FileFilter extFilter = getFileFilterByExtension(matchExtension);
        if (recurse) {
            return (file) -> file.isDirectory() || extFilter.accept(file);
        }
        return extFilter;
    }

    /**
     * Returns an absolute normalized {@link Path} for the given {@link File}. This ensures safe and consistent
     * comparisons.
     *
     * <p>
     * Methods like {@link Path#startsWith(Path)} compare raw, unresolved path segments - it does not resolve "." or
     * ".." components. Without normalizing first, a path like "[dir]/../escaped.txt" would incorrectly be reported as a
     * descendant of [dir], because the literal segments of [dir] are a prefix of the literal segments of the file, even
     * though the path - once resolved - actually refers to a location outside [dir]. We resolve to an absolute path
     * before normalizing because {@link Path#normalize()} on a lone "." segment yields a Path with a single
     * empty-string name element (not an empty/zero-element path), which would otherwise make relative paths like "."
     * fail to match their own children (e.g. "./foo.bar").
     *
     * <p>
     * Note that "absolute and normalized" is not the same as canonicalized.
     *
     * @param file
     *     the {@link File} to be transformed.
     * @return an absolute normalized {@link Path} for the given {@link File}. This ensures safe and consistent
     *     comparisons.
     * @throws NullPointerException
     *     if the argument for the file is null.
     */
    @Nonnull
    public static final Path toAbsoluteNormalizedPath(@Nonnull File file) {
        Objects.requireNonNull(file, "file");
        return file.toPath().toAbsolutePath().normalize();
    }

    @Nonnull
    private static final DeleteRequestStatus deleteOnExit(@Nonnull File deleteTarget, @Nonnull Exception deleteError, @Nonnull DeleteRequestStatus onFailure) {
        try {
            deleteTarget.deleteOnExit();
            LOGGER.info(
                "Unable to immediately delete file {}; marked to be deleted on exit.", deleteTarget, deleteError
            );
            return DeleteRequestStatus.SUCCESS_ON_EXIT;
        }
        catch (Exception e) {
            e.addSuppressed(deleteError);
            LOGGER.info(
                "Unable to immediately delete file {}, or mark it for deletion on exit", deleteTarget, e
            );
        }
        return onFailure;
    }

    @Nonnull
    private static final Set<String> getExtensionsForExtensionFileFilter(@Nonnull Set<String> extensions) {
        Objects.requireNonNull(extensions, "extensions");
        Set<String> modifiedExtensions = new HashSet<>();
        for (String ext : extensions) {
            if (StringUtils.isBlank(ext)) {
                continue;
            }
            if (StringUtil.startsWith(ext, FileNameUtil.EXTENSION_SEPARATOR_CHAR)) {
                ext = ext.substring(1);
            }
            modifiedExtensions.add(ext);
        }
        return modifiedExtensions;
    }

    private static final <X extends Exception> void throwException(@Nonnull Class<X> xcpType, String message) throws X {
        Objects.requireNonNull(xcpType, "Exception type");
        X xcp;
        try {
            xcp = xcpType.getConstructor(String.class).newInstance(message);
        }
        catch (Exception instantiationXcp) {
            throw new RuntimeException(message);
        }
        throw xcp;
    }

    /**
     * Applies tests for requirements for a valid temp file directory path.
     *
     * @param candidateTempDirectory
     *     the directory to test.
     * @throws IllegalArgumentException
     *     if the given path is found to be invalid for some reason.
     */
    private static final void checkTempDirectoryX(@Nonnull File candidateTempDirectory)
        throws IllegalArgumentException {

        Objects.requireNonNull(candidateTempDirectory, "candidate temp directory");

        if (StringUtils.isBlank(candidateTempDirectory.getPath())) {
            throw new IllegalArgumentException("Missing or blank temp directory path.");
        }

        try {

            if (!candidateTempDirectory.exists()) {
                throw new IllegalArgumentException("Temp directory " + candidateTempDirectory + " does not exist.");
            }

            if (!candidateTempDirectory.isDirectory()) {
                throw new IllegalArgumentException(
                    "Temp directory " + candidateTempDirectory + " exists, but is a file."
                );
            }

            if (!candidateTempDirectory.canRead()) {
                throw new IllegalArgumentException("Can't read the temp directory " + candidateTempDirectory + ".");
            }

            if (!candidateTempDirectory.canWrite()) {
                throw new IllegalArgumentException("Can't write in the temp directory " + candidateTempDirectory + ".");
            }

        }
        catch (SecurityException e) {
            throw new IllegalArgumentException("Not allowed to use this directory for temp files.", e);
        }

    }

    private static final boolean isOrWouldBeAllowed(@Nullable File file, Predicate<? super File> existingTypeTest, Predicate<? super File> permissionTest) {
        if (file == null) {
            return false;
        }
        try {
            if (file.exists()) {
                if (existingTypeTest.test(file) && permissionTest.test(file)) {
                    return true;
                }
                return false;
            }
            File parent = file.getParentFile();
            if (parent != null && parent.isDirectory() && permissionTest.test(parent)) {
                return true;
            }
            return false;
        }
        catch (RuntimeException e) {
            LOGGER.warn("File location validation encountered an error while performing a test for {}", file, e);
            return false;
        }
    }

    private static final void checkAbsoluteAndNoRelativeSegmentsX(@Nonnull File file) {
        if (!file.isAbsolute()) {
            throw new IllegalArgumentException(String.format("The file %s does not use an absolute path.", file));
        }
        file.toPath().forEach(segment -> checkNonRelativeSegmentX(file, segment));
    }

    private static final void checkNonRelativeSegmentX(@Nonnull File file, @Nonnull Path segment) {
        if (Strings.CS.equalsAny(
            segment.toString(),
            FileNameUtil.CONSECUTIVE_DOTS,
            FileNameUtil.CURRENT_PATH_INDICATOR
        )) {
            throw new IllegalArgumentException(
                String.format("The file %s contains an unexpected path segment %s.", file, segment)
            );
        }
    }

    private static final boolean isDescendantForAlreadyCanonicalFilesImpl(@Nonnull File directoryCanonical, @Nonnull File fileCanonical) {
        if (fileCanonical.toPath().startsWith(directoryCanonical.toPath())) {
            return true;
        }
        return false;
    }

    private static final boolean isOrIsDescendantForAlreadyCanonicalFilesImpl(@Nonnull File directoryCanonical, @Nonnull File fileCanonical) {
        if (fileCanonical.equals(directoryCanonical) ||
            isDescendantForAlreadyCanonicalFilesImpl(directoryCanonical, fileCanonical)) {
            return true;
        }
        return false;
    }

}
