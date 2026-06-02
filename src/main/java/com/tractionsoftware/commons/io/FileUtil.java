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

import com.google.common.collect.ImmutableSet;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import com.tractionsoftware.commons.lang.Resource;
import com.tractionsoftware.commons.lang.StringUtil;
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
    public enum DeleteRequestStatus {

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

//    private static final class FileNamePredicateFileFilter extends AbstractFileFilter {
//
//        static FileNamePredicateFileFilter createInstanceForPattern(String patternSpec) {
//            StringUtil.checkNotBlankX(patternSpec, "file name pattern spec");
//            Pattern pattern = Pattern.compile(patternSpec);
//            return new FileNamePredicateFileFilter(name -> pattern.matcher(name).matches());
//        }
//
//        private final Predicate<String> test;
//
//        private FileNamePredicateFileFilter(Predicate<String> test) {
//            this.test = test;
//        }
//
//        @Override
//        public boolean accept(File pathname) {
//            return test.test(pathname.getName());
//        }
//
//    }

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
        public boolean accept(File pathname) {
            if (pathname.isDirectory()) {
                return false;
            }
            return hasIncludedExtension(pathname.getName());
        }

        private boolean hasIncludedExtension(String name) {
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
     * Returns true if the target {@link File} exists, represents a file (as opposed to a directory), and is readable by
     * the process.
     *
     * @param file
     *     the {@link File} to test.
     * @return true if the target {@link File} exists, represents a file (as opposed to a directory), and is readable by
     *     the process; false otherwise.
     */
    public static boolean isValidFileForRead(File file) {
        try {
            if (file != null && file.exists() && file.isFile() && file.canRead()) {
                return true;
            }
            return false;
        }
        catch (Exception e) {
            return false;
        }
    }

    /**
     * Returns true if the target {@link File} exists, represents a directory (as opposed to a file), and is readable by
     * the process.
     *
     * @param file
     *     the {@link File} to test.
     * @return true if the target {@link File} exists, represents a directory (as opposed to a file), and is readable by
     *     the process; false otherwise.
     */
    public static boolean isValidDirectoryForRead(File file) {
        try {
            if (file != null && file.exists() && file.isDirectory() && file.canRead()) {
                return true;
            }
            return false;
        }
        catch (Exception e) {
            return false;
        }
    }

    /**
     * Returns true if the target {@link File} either does not exist, or exists and represents a file (as opposed to a
     * directory), and is writable by the process.
     *
     * @param file
     *     the {@link File} to test.
     * @return true if the target {@link File} either does not exist, or exists and represents a file (as opposed to a
     *     directory), and is writable by the process; false otherwise.
     */
    public static boolean isValidFileForWrite(File file) {
        try {
            if (file != null && (!file.exists() || file.isFile()) && file.canWrite()) {
                return true;
            }
            return false;
        }
        catch (Exception e) {
            return false;
        }
    }

    /**
     * Returns true if the target {@link File} either does not exist, or exists and represents a directory (as opposed
     * to a file), and is writable by the process.
     *
     * @param file
     *     the {@link File} to test.
     * @return true if the target {@link File} either does not exist, or exists and represents a directory (as opposed
     *     to a file), and is writable by the process; false otherwise.
     */
    public static boolean isValidDirectoryForWrite(File file) {
        try {
            if (file != null && (!file.exists() || file.isDirectory()) && file.canWrite()) {
                return true;
            }
            return false;
        }
        catch (Exception e) {
            return false;
        }
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
    public static String getRelativePath(File root, File file) {
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
     * @see FileUtils#deleteDirectory(File)
     * @see Files#delete(Path)
     */
    public static void delete(File file) throws IOException {
        if (file.isDirectory()) {
            FileUtils.deleteDirectory(file);
        }
        else {
            Files.delete(file.toPath());
        }
    }

    public static List<File> delete(Collection<File> files) {
        List<File> notDeleted = new ArrayList<>();
        for (File file : files) {
            try {
                Files.delete(file.toPath());
            }
            catch (IOException | SecurityException e) {
                notDeleted.add(file);
            }
        }
        return notDeleted;
    }

    public static Collection<File> listDirs(File file) {
        File[] directories = file.listFiles(File::isDirectory);
        if (directories == null) {
            return List.of();
        }
        return Arrays.asList(directories);
    }

    /**
     * Returns all of the files in the specified folder with the specified extension.
     */
    public static Collection<File> findFilesByExtension(File directory, String matchExtension, boolean recurse) {
        Objects.requireNonNull(matchExtension, "extension");
        return FileUtils.listFiles(directory, new String[] { matchExtension }, recurse);
    }

    /**
     * Returns the canonical version of the given File if possible.
     *
     * @param file
     *     the File to be canonicalized.
     * @return the canonical version of the given File if possible; the given File otherwise.
     */
    public static File getCanonicalFile(File file) {
        try {
            return file.getCanonicalFile();
        }
        catch (IOException | SecurityException e) {
            LOGGER.warn("Failed to canonicalize {} ", file, e);
        }
        return file;
    }

    /**
     * Returns if the other given File can be shown to be the same as the given directory, or can be shown to be a
     * descendant of the other given File. This method can return false either if the other given File can definitely be
     * shown not to be the same as the directory or a descendant of the directory, or if there is an IOException or
     * SecurityException raised while, e.g., attempting to canonicalize either File's path.
     *
     * @param directory
     *     a directory which may be the other file, or may have the other file as a descendant.
     * @param file
     *     a File which may represent the same file or a descendant of the given directory.
     * @return true if the other given File can be shown to be the same as the given directory, or can be shown to be a
     *     descendant of the other given File; false otherwise.
     */
    public static boolean isOrIsDescendant(File directory, File file) {

        directory = new File(FileNameUtil.platformIndependentPath(directory.getPath()));
        file = new File(FileNameUtil.platformIndependentPath(file.getPath()));

        if (isOrIsDescendantPath(directory.getPath(), file.getPath())) {
            return true;
        }

        try {
            return isOrIsDescendantForCanonicalFiles(directory.getCanonicalFile(), file.getCanonicalFile());
        }
        catch (IOException | SecurityException e) {
            LOGGER.warn("isOrIsDescendant encountered an error ({} vs {}) ", directory, file, e);
        }
        return false;

    }

    public static boolean isDescendant(File directory, File file) {

        if (file.toPath().startsWith(directory.toPath())) {
            return true;
        }

        directory = new File(FileNameUtil.platformIndependentPath(directory.getPath()));
        file = new File(FileNameUtil.platformIndependentPath(file.getPath()));

        try {
            return isDescendantForCanonicalFiles(directory.getCanonicalFile(), file.getCanonicalFile());
        }
        catch (IOException | SecurityException e) {
            LOGGER.warn("isDescendant encountered an error ({} vs {})", directory, file, e);
        }
        return false;

    }

    public static boolean isDescendantForCanonicalFiles(File directoryCanonical, File fileCanonical) {
        if (fileCanonical.toPath().startsWith(directoryCanonical.toPath())) {
            return true;
        }
        return false;
    }

    public static boolean isOrIsDescendantForCanonicalFiles(File directoryCanonical, File fileCanonical) {
        if (fileCanonical.equals(directoryCanonical)) {
            return true;
        }
        return isDescendantForCanonicalFiles(directoryCanonical, fileCanonical);
    }

    public static boolean areSameFiles(File file, File other) {
        if (file.equals(other)) {
            return true;
        }
        try {
            if (file.getCanonicalPath().equals(other.getCanonicalPath())) {
                return true;
            }
            return false;
        }
        catch (IOException | SecurityException e) {
            LOGGER.warn("areSameFiles encountered an error", e);
        }
        return false;
    }

    public static boolean isOrIsDescendantPath(String directoryPath, String filePath) {
        if (directoryPath.equals(filePath)) {
            return true;
        }
        if (isDescendantPath(directoryPath, filePath)) {
            return true;
        }
        return false;
    }

    public static boolean isDescendantPath(String directoryPath, String filePath) {
        return isDescendant(new File(directoryPath), new File(filePath));
    }

    public static boolean existsAndReadable(File file) {
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
     * Moves a file using {@link Files#move(Path, Path, CopyOption...)} , if the given source and destination Files are
     * not already the same.
     *
     * @return true if the file was actually moved; false if the source and destination files were already the same.
     * @throws IOException
     *     if one is raised while attempting to perform the move operation.
     */
    public static boolean move(File source, File dest) throws IOException {
        if (areSameFiles(source, dest)) {
            return false;
        }
        Files.move(source.toPath(), dest.toPath(), StandardCopyOption.REPLACE_EXISTING);
        return true;
    }

    public static void checkAndCreateDir(String directoryPath) throws IOException {
        checkAndCreateDir(directoryPath, IOException.class);
    }

    public static <X extends Exception> void checkAndCreateDir(String directoryPath, Class<X> xcpType)
        throws X {
        if (directoryPath == null) {
            return;
        }
        checkAndCreateDir(new File(directoryPath), xcpType);
    }

    public static void checkAndCreateDir(File directory) throws IOException {
        checkAndCreateDir(directory, IOException.class);
    }

    public static <X extends Exception> void checkAndCreateDir(File directory, Class<X> xcpType)
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

    public static File createSystemTempFile(String prefix, String extension) throws IOException {
        if (StringUtils.isBlank(extension)) {
            return File.createTempFile(prefix, null);
        }
        return File.createTempFile(prefix, Strings.CS.prependIfMissing(extension, FileNameUtil.EXTENSION_SEPARATOR));
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

    public static final void setTempDirectory(String directoryPath) {
        setTempDirectory(new File(StringUtils.defaultString(directoryPath)));
    }

    public static final void setTempDirectory(File directory) {
        checkTempDirectoryX(directory);
    }

    public static final File getTempDirectory() {
        String defaultTempDirectoryPath = System.getProperty(SYSTEM_PROPERTY_NAME_TEMP_DIRECTORY);
        if (StringUtils.isNotBlank(defaultTempDirectoryPath)) {
            return new File(defaultTempDirectoryPath);
        }
        throw new Error("No temp directory has been set.");
    }

    @CanIgnoreReturnValue
    public static DeleteRequestStatus deleteOrDeleteOnExit(File deleteTarget) {

        if (deleteTarget == null) {
            return DeleteRequestStatus.NO_FILE_REQUESTED;
        }

        try {
            // We check whether the file exists or not because NoSuchFileException is /optional/ for the Files.delete
            // method. This is not atomic now, because it's check-then-act, but in most cases, a file is only being
            // deleted by one caller at a time, and this is better than returning success when no delete operation
            // actually happened. [shep 30.Apr.2022]
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

    public static FileFilter getFileFilterByExtension(String ext) {
        return getFileFilterByExtension(Collections.singleton(ext));
    }

    public static FileFilter getFileFilterByExtension(Set<String> extensions) {
        return new ExtFileFilter(getExtensionsForExtensionFileFilter(extensions));
    }

    public static IOFileFilter getFileNameBlobFilter(String fileNameMatcherBlob) {
        StringUtil.checkNotBlankX(fileNameMatcherBlob, "file name matcher blob");
        if (StringUtils.containsAny(
            fileNameMatcherBlob,
            FileNameUtil.GENERIC_PATH_SEPARATOR_CHAR,
            FileNameUtil.WINDOWS_PATH_SEPARATOR_CHAR,
            ':'
        )) {
            throw new IllegalArgumentException();
        }
        return WildcardFileFilter.builder().setWildcards(fileNameMatcherBlob).get();
    }

    public static IOFileFilter getFileNamePatternFilter(String patternSpec) {
        StringUtil.checkNotBlankX(patternSpec, "file name pattern spec");
        return new RegexFileFilter(patternSpec);
    }

    public static InputStream getBufferedInputStream(File file) throws IOException {
        Objects.requireNonNull(file, "File");
        return IOUtil.getBufferedInputStream(Files.newInputStream(file.toPath()));
    }

    public static BufferedReader getBufferedUtf8Reader(File file) throws IOException {
        Objects.requireNonNull(file, "File");
        return Files.newBufferedReader(file.toPath(), StandardCharsets.UTF_8);
    }

    public static OutputStream getBufferedOutputStream(File file) throws IOException {
        Objects.requireNonNull(file, "File");
        return IOUtil.getBufferedOutputStream(new FileOutputStream(file));
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
     * @see #getBufferedOutputStream(File)
     * @see IOUtil#getTrackedOutputStream(OutputStream, Supplier)
     */
    public static OutputStream getBufferedTrackedOutputStream(File file, Supplier<? extends Resource> getTracker) throws IOException {
        return IOUtil.getTrackedOutputStream(getBufferedOutputStream(file), getTracker, file.toString());
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
    public static BufferedWriter getBufferedUtf8Writer(File file) throws IOException {
        Objects.requireNonNull(file, "File");
        return Files.newBufferedWriter(file.toPath(), StandardCharsets.UTF_8);
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
    public static PrintWriter getBufferedUtf8PrintWriter(File file) throws IOException {
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
     * @throws SecurityException
     *     if there is a security manager installed that prevents the requested destination file from being read.
     */
    public static File getMoveDestination(File requestedDestination, String fileName) throws SecurityException {
        if (requestedDestination.exists() && requestedDestination.isDirectory()) {
            return new File(requestedDestination, fileName);
        }
        return requestedDestination;
    }

    public static File getDescendant(File directory, String... pathComponents) {
        File file = directory;
        for (String pathComponent : pathComponents) {
            file = new File(file, pathComponent);
        }
        return file;
    }

    public static FileFilter getFindFilesByExtensionFilter(String matchExtension, boolean recurse) {
        FileFilter extFilter = getFileFilterByExtension(matchExtension);
        if (recurse) {
            return (file) -> file.isDirectory() || extFilter.accept(file);
        }
        return extFilter;
    }

    private static DeleteRequestStatus deleteOnExit(File deleteTarget, Exception deleteError, DeleteRequestStatus onFailure) {
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

    private static Set<String> getExtensionsForExtensionFileFilter(Set<String> extensions) {
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

    private static <X extends Exception> void throwException(Class<X> xcpType, String message) throws X {
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
    private static final void checkTempDirectoryX(File candidateTempDirectory) throws IllegalArgumentException {

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

}
