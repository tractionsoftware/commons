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

package com.tractionsoftware.commons.util;

import com.tractionsoftware.commons.io.FileNameUtil;
import com.tractionsoftware.commons.io.IOUtil;
import com.tractionsoftware.commons.io.FileUtil;
import jakarta.annotation.Nonnull;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.URI;
import java.nio.file.Files;
import java.util.Collection;
import java.util.Objects;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;
import java.util.zip.GZIPOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

/**
 * Utilities related to ZIP, JAR, GZIP, etc.
 *
 * @author Dave Shepperton
 */
public final class CompressionUtil {

    private static final Logger LOGGER = LoggerFactory.getLogger(CompressionUtil.class.getName());

    /**
     * A ZipEntryNameProvider whose getName method simply returns the result of calling getName on the supplied File.
     * This represents the default naming behavior that is used when calling a zip or jar method that does not accept a
     * ZipEntryNameProvider.
     */
    public static final ZipEntryNameProvider FILE_NAME_ZIP_ENTRY_NAMER = File::getName;

    /**
     * A ZipEntryNameProvider whose getName method simply returns the result of calling getPath on the supplied File.
     * From within the TeamPage application, the path can be expected to be relative to the TeamPage installation's
     * server directory.
     */
    public static ZipEntryNameProvider FILE_PATH_ZIP_ENTRY_NAMER = File::getPath;

    /*
     * Not instantiable.
     */
    private CompressionUtil() {
    }

    public static byte[] gzipBytes(byte[] input) {
        return gzipBytes(new ByteArrayInputStream(input));
    }

    public static byte[] gzipBytes(InputStream input) {
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            GZIPOutputStream gzip = new GZIPOutputStream(out, IOUtil.DEFAULT_IO_BUFFER_SIZE);
            IOUtil.copy(input, gzip);
            gzip.finish();
            gzip.flush();
            byte[] ret = out.toByteArray();
            IOUtil.close(gzip);
            return ret;
        }
        catch (IOException e) {
            LOGGER.warn("Unexpected error gzipping byte array", e);
            return null;
        }
    }

    /**
     * Adds the given files to the ZIP archive represented by the given stream.
     *
     * @param inputFiles
     *     The files to be compressed.
     * @param zip
     *     The Archive to add to.
     */
    public static final void addToArchive(Collection<File> inputFiles, Archive zip) throws IOException {
        for (File fileToAdd : inputFiles) {
            try (InputStream fileInput = FileUtil.getBufferedInputStream(fileToAdd)) {
                zip.addFile(fileToAdd, fileInput);
            }
        }
    }

    /**
     * Returns a ZipEntryNameProvider whose getName method will return the relative path of the given File with respect
     * to a given baseDirectory File. Use this along with one of the zip or jar methods that takes a ZipEntryNamer to
     * create an archive whose entries will retain that relative path information.
     */
    public static ZipEntryNameProvider getRelativePathZipEntryNamer(File baseDirectory) {
        final URI baseUri = baseDirectory.toURI();
        return file -> baseUri.relativize(file.toURI()).getPath();
    }

    /**
     * Creates a new ZIP archive in the given file and adds the input files to the archive. The entries will be named
     * according to each File's getName method.
     */
    public static final void zip(Collection<File> input, File zipfile) throws IOException {
        zip(input, zipfile, FILE_NAME_ZIP_ENTRY_NAMER);
    }

    /**
     * Creates a new ZIP archive in the given file and adds the input files to the archive. The entries will be named
     * using the given ZipEntryNameProvider.
     */
    public static final void zip(Collection<File> input, File zipFile, ZipEntryNameProvider namer) throws IOException {
        try (ZipOutputStream zip = getZipOutputStream(zipFile)) {
            zip(input, zip, namer);
            zip.finish();
            zip.flush();
        }
    }

    /**
     * Adds a ZipEntry for each input file to the ZipOutputStream. The entries will be named according to each File's
     * getName method.
     */
    public static final void zip(Collection<File> input, ZipOutputStream zip) throws IOException {
        zip(input, zip, FILE_NAME_ZIP_ENTRY_NAMER);
    }

    /**
     * Adds a ZipEntry for each input file to the ZipOutputStream. The entries will be named using the given
     * ZipEntryNameProvider.
     */
    public static final void zip(Collection<File> input, ZipOutputStream zip, ZipEntryNameProvider namer) throws IOException {
        addToArchive(input, new ZipArchive(zip, namer));
    }

    /**
     * Creates a new JAR archive in the given file and adds the input files to the archive.
     */
    public static final void jar(Collection<File> input, File jarfile) throws IOException {
        jar(input, jarfile, (Manifest) null);
    }

    /**
     * Creates a new JAR archive in the given file with the given Manifest (optional) and adds the input files to the
     * archive.
     */
    public static final void jar(Collection<File> input, File jarfile, Manifest man) throws IOException {
        jar(input, jarfile, man, FILE_NAME_ZIP_ENTRY_NAMER);
    }

    /**
     * Creates a new JAR archive in the given file and adds the input files to the archive. The entries will be named
     * using the given ZipEntryNameProvider.
     */
    public static final void jar(Collection<File> input, File jarfile, ZipEntryNameProvider namer) throws IOException {
        jar(input, jarfile, null, namer);
    }

    /**
     * Creates a new JAR archive in the given file with the given Manifest (optional) and adds the input files to the
     * archive. The entries will be named using the given ZipEntryNameProvider.
     */
    public static final void jar(Collection<File> input, File jarFile, Manifest man, ZipEntryNameProvider namer)
        throws IOException {
        try (JarOutputStream jarOut = getJarOutputStream(jarFile, man)) {
            jar(input, jarOut, namer);
            jarOut.finish();
            jarOut.flush();
        }
    }

    public static JarOutputStream getJarOutputStream(File jarFile, Manifest man) throws IOException {
        OutputStream fileOut = Files.newOutputStream(jarFile.toPath());
        try {
            if (man == null) {
                return new JarOutputStream(fileOut);
            }
            return new JarOutputStream(fileOut, man);
        }
        catch (IOException | RuntimeException | Error e) {
            IOUtil.close(fileOut);
            throw e;
        }
    }

    /**
     * Adds a JarEntry for each input file to the JarOutputStream. The entries will be named according to each File's
     * getName method.
     */
    public static final void jar(Collection<File> input, JarOutputStream zip) throws IOException {
        jar(input, zip, FILE_NAME_ZIP_ENTRY_NAMER);
    }

    /**
     * Adds a ZipEntry for each input file to the ZipOutputStream. The entries will be named using the given
     * ZipEntryNameProvider.
     */
    public static final void jar(Collection<File> input, JarOutputStream jarOut, ZipEntryNameProvider namer)
        throws IOException {
        addToArchive(input, new JarArchive(jarOut, namer));
    }

    /**
     * Compresses the given original file, deletes the original file, and returns a File to the compressed version.
     *
     * @return null if the given File is null or doesn't exist; the given File if it is already a .zip file; null if the
     *     compression operation cannot be successfully completed; a File referring to the new compressed file
     *     otherwise.
     */
    public static File compress(File original) {

        if (original == null) {
            LOGGER.warn("No file was given to compress.");
            return null;
        }

        String originalPath = original.getPath();

        if (!original.exists()) {
            LOGGER.warn("The file {} cannot be compressed because it does not exist.", originalPath);
            return null;
        }

        // make sure it's not already a zip file. if it is, we just return the original
        String ext = FileNameUtil.getExtension(original.getName(), null);
        if ("zip".equalsIgnoreCase(ext)) {
            LOGGER.info("The file {} already appears to be a ZIP file.", originalPath);
            return original;
        }

        String destinationPath = originalPath + ".zip";
        File destination = new File(destinationPath);
        if (destination.exists()) {
            LOGGER.info("Overwriting .zip file {}", destination);
        }

        try (InputStream in = FileUtil.getBufferedInputStream(original);
             ZipOutputStream zip = getZipOutputStream(destination)) {
            zip.putNextEntry(new ZipEntry(original.getName()));
            in.transferTo(zip);
            zip.closeEntry();
            zip.finish();
            zip.flush();
        }
        catch (IOException e) {
            LOGGER.error("Unexpected error attempting to compress {}", originalPath, e);
            FileUtil.deleteOrDeleteOnExit(destination);
            return null;
        }

        if (!destination.exists()) {
            LOGGER.warn("The compressed file {} could not be created.", destination);
            return null;
        }

        long compressedSize = destination.length();
        if (compressedSize == 0) {
            LOGGER.warn("The compressed file {} is empty.", destinationPath);
            return null;
        }

        LOGGER.info("Successfully created compressed file {}, {}B.", destinationPath, compressedSize);

        // delete the original
        boolean success = original.delete();
        if (success) {
            LOGGER.info("Successfully deleted compressed file source {}.", originalPath);
        }
        else {
            LOGGER.warn("Failed to delete compressed file source {}.", originalPath);
        }

        return destination;

    }

    /**
     * Decompresses the file, deletes the compressed version, and returns a File to the uncompressed version.
     */
    public static File decompress(File original) {

        if (original == null || !original.exists()) {
            LOGGER.warn("Attempted to decompress non-existent file {}.", original);
            return null;
        }

        try {

            // Make sure it's a .zip file. If it's not, we just return the original.
            String ext = FileNameUtil.getExtension(original.getName(), null);
            if (!"zip".equalsIgnoreCase(ext)) {
                return original;
            }

            File destination = new File(FileNameUtil.stripExtension(original.getPath()));
            unzip(original, original.getParentFile());
            // delete the original
            if (destination.exists() && destination.length() > 0) {
                // assume success
                FileUtil.deleteOrDeleteOnExit(original);
            }
            return destination;

        }
        catch (IOException e) {
            LOGGER.error("Unexpected error attempting to decompress {}", original, e);
        }

        return null;

    }

    /**
     * Unzips the file to the specified directory.
     */
    public static final void unzip(File zipFile, File toDirectory) throws IOException {

        try (ZipInputStream zip = getZipInputStream(zipFile)) {

            ZipEntry entry;
            while ((entry = zip.getNextEntry()) != null) {

                // create the output file from the ZipEntry name
                File outputFile = new File(toDirectory, entry.getName());

                // make sure the path isn't to something outside this directory
                if (!FileUtil.isOrIsDescendant(toDirectory, outputFile)) {
                    LOGGER.warn("Path in {} outside the current root directory; skipping {}", zipFile, entry.getName());
                    continue;
                }

                if (entry.isDirectory()) {
                    // create directories as necessary
                    Files.createDirectories(outputFile.toPath());
                }
                else {
                    // make sure the parent directory exists
                    File parent = outputFile.getParentFile();
                    if (!parent.exists()) {
                        Files.createDirectories(parent.toPath());
                    }
                    try (OutputStream out = FileUtil.getBufferedOutputStream(outputFile)) {
                        zip.transferTo(out);
                        out.flush();
                    }
                }

            }

        }

    }

    public static ZipInputStream getZipInputStream(File zipFile) throws IOException {
        InputStream fileIn = Files.newInputStream(zipFile.toPath());
        try {
            return new ZipInputStream(fileIn);
        }
        catch (RuntimeException | Error e) {
            IOUtil.close(fileIn);
            throw e;
        }
    }

    public static ZipOutputStream getZipOutputStream(File zipFile) throws IOException {
        OutputStream fileOut = Files.newOutputStream(zipFile.toPath());
        try {
            return new ZipOutputStream(fileOut);
        }
        catch (RuntimeException | Error e) {
            IOUtil.close(fileOut);
            throw e;
        }
    }

    /**
     * Adds a JarEntry for the given File to the given JarOutputStream. Always closes the FileInputStream created to
     * read the file's contents.
     *
     * @throws IOException
     *     that may be raised by reading the file, creating the JarEntry, or adding the JarEntry or writing the file's
     *     contents to the JarOutputStream.
     */
    public static final void addJarEntry(JarOutputStream out, File file) throws IOException {
        try (InputStream in = FileUtil.getBufferedInputStream(file)) {
            JarEntry entry = new JarEntry(file.getName());
            entry.setTime(file.lastModified());
            addJarEntry(out, entry, in);
        }
    }

    /**
     * Adds the given JarEntry to the given JarOutputStream, using the InputStream (copied until EOF) as the source of
     * the data of the JarEntry.
     *
     * @throws IOException
     *     that may be raised reading from the InputStream or adding the JarEntry or otherwise writing the contents to
     *     the JarOutputStream.
     */
    public static final void addJarEntry(@Nonnull JarOutputStream output, @Nonnull JarEntry entry, @Nonnull InputStream input) throws IOException {
        Objects.requireNonNull(output, "output");
        Objects.requireNonNull(entry, "entry");
        Objects.requireNonNull(input, "input");
        output.putNextEntry(entry);
        input.transferTo(output);
    }

    /**
     * Some of the zip and jar methods may accept one of these so that the caller may indicate the name to use for a
     * ZipEntry.
     *
     * @author Dave Shepperton
     */
    @FunctionalInterface
    public static interface ZipEntryNameProvider {

        public String getName(File file);

    }

    public static interface Archive {

        public void addFile(File file, InputStream inputStream) throws IOException;

    }

    /**
     * This skeleton Archive implementation uses a ZipOutputStream and creates ZipEntry objects.
     *
     * @param <Z>
     *     The type of ZipOutputStream being supplied.
     * @param <E>
     *     The type of ZipEntry being created, which should be appropriate for the ZipOutputStream type.
     * @author Dave Shepperton
     */
    private static abstract class ZipOutputStreamArchive<Z extends ZipOutputStream, E extends ZipEntry>
        implements Archive {

        protected final Z zip;

        protected final ZipEntryNameProvider namer;

        private ZipOutputStreamArchive(Z zip, ZipEntryNameProvider namer) {
            this.zip = zip;
            this.namer = namer;
        }

        @Override
        public final void addFile(@Nonnull File file, @Nonnull InputStream input) throws IOException {
            Objects.requireNonNull(file, "file");
            Objects.requireNonNull(input, "input");
            zip.putNextEntry(getEntry(file));
            input.transferTo(zip);
            zip.closeEntry();
        }

        protected abstract E getEntry(File file);

    }

    /**
     * This Archive implementation uses ZipOutputStream and ZipEntry and names them according to the supplied
     * ZipEntryNameProvider.
     *
     * @author Dave Shepperton
     */
    private static class ZipArchive extends ZipOutputStreamArchive<ZipOutputStream,ZipEntry> implements Archive {

        private ZipArchive(ZipOutputStream zip, ZipEntryNameProvider namer) {
            super(zip, namer);
        }

        @Override
        protected final ZipEntry getEntry(File file) {
            return new ZipEntry(namer.getName(file));
        }

    }

    /**
     * This Archive implementation uses JarOutputStream and JarEntry and names them according to the supplied
     * ZipEntryNameProvider.
     *
     * @author Dave Shepperton
     */
    private static class JarArchive extends ZipOutputStreamArchive<JarOutputStream,JarEntry> implements Archive {

        private JarArchive(JarOutputStream jar, ZipEntryNameProvider namer) {
            super(jar, namer);
        }

        @Override
        protected final JarEntry getEntry(File file) {
            return new JarEntry(namer.getName(file));
        }

    }
}
