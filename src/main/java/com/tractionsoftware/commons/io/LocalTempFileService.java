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

import com.tractionsoftware.commons.lang.JavaUtil;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import org.apache.commons.io.function.IOSupplier;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.Objects;
import java.util.function.Supplier;

public abstract class LocalTempFileService {

    public static final TempFileResource.Factory DEFAULT_FACTORY = new TempFileResource.AbstractFactory() {

        @Nonnull
        @Override
        protected final TempFileResource createImpl(@Nonnull MutableFileMetadata metadata, @Nullable IOSupplier<? extends InputStream> content, @Nonnull Logger logger)
            throws IOException {

            TempFileResource temp = LocalTempFileService.get().createNew(metadata);
            if (content == null) {
                return temp;
            }

            try (InputStream source = content.get()) {
                temp.setContent(source);
            }
            catch (IOException | RuntimeException e) {
                temp.delete();
                throw e;
            }
            return temp;

        }

        @Nonnull
        @Override
        protected final TempFileResource loadExistingImpl(@Nonnull MutableFileMetadata metadata, @Nonnull Logger logger)
            throws IOException {
            File file = LocalTempFileService.get().getFile(metadata.getURI());
            MutableFileMetadata updatedMetadata = metadata.mutableCopy();
            return new LocalTempFileResource(file, updatedMetadata, -1);
        }

    };

    public static final LocalTempFileService DEFAULT = new LocalTempFileService() {

        @Nonnull
        @Override
        protected final LocalTempFileResource createNewImpl(@Nonnull FileMetadata metadata) throws IOException {
            MutableFileMetadata updatedMetadata = metadata.mutableCopy();
            File temp = FileUtil.createSystemTempFile("temp-", metadata.getExtension());
            try {
                updatedMetadata.setURI(getURIImpl(temp));
                updatedMetadata.ensureGoodFilename();
            }
            catch (RuntimeException e) {
                FileUtil.deleteOrDeleteOnExit(temp);
                throw e;
            }
            return new LocalTempFileResource(temp, updatedMetadata, -1);
        }

        @Nonnull
        @Override
        protected final URI getURIImpl(@Nonnull File file) {
            return URI.create(TempFileResource.URI_SCHEME_PREFIX + file.getName());
        }

        @Nonnull
        @Override
        protected final File getFileImpl(@Nonnull URI uri) throws FileNotFoundException {
            String part = uri.getSchemeSpecificPart();
            String name = FileNameUtil.stripPath(part);
            if (StringUtils.isBlank(name)) {
                throw new FileNotFoundException();
            }
            File file = new File(FileUtil.getTempDirectory(), name);
            if (!file.exists() || !file.canRead()) {
                throw new FileNotFoundException(part);
            }
            return file;
        }

    };

    private static final Supplier<? extends LocalTempFileService> instance = JavaUtil.lazyServiceLoader(
        LocalTempFileService.class, DEFAULT, TempFileResource.LOGGER
    );

    @Nonnull
    public static final LocalTempFileService get() {
        return instance.get();
    }

    /**
     * Returns a {@link LocalTempFileResource} wrapping a newly created local temp file.
     *
     * @param metadata
     *     containing the metadata for the temporary file.
     * @return a {@link LocalTempFileResource} wrapping a newly created local temp file
     * @throws IOException
     *     if the temp file can't be created.
     */
    @Nonnull
    public final TempFileResource createNew(@Nonnull FileMetadata metadata) throws IOException {
        Objects.requireNonNull(metadata, "metadata");
        return createNewImpl(metadata);
    }

    /**
     * Returns a {@link TempFileResource#URI_SCHEME_PREFIX "temp:"} {@link URI} referring to the given temporary file.
     *
     * @param file
     *     the {@link File} representing the temp file on disk, generally created via
     *     {@link File#createTempFile(String, String)}.
     * @return a {@link TempFileResource#URI_SCHEME_PREFIX "temp:"} {@link URI} referring to the given temporary file.
     * @throws NullPointerException
     *     if the {@link File} is null.
     * @throws IllegalArgumentException
     *     if the {@link File} is considered invalid for a temp file.
     */
    @Nonnull
    public final URI getURI(@Nonnull File file) {
        Objects.requireNonNull(file, "file");
        if (!file.exists() || !file.canRead()) {
            throw new IllegalArgumentException("Invalid file.");
        }
        return getURIImpl(file);
    }

    /**
     * Returns the {@link File} corresponding to the given published temp file {@link URI}, if one exists.
     *
     * @param uri
     *     the logical "published" URI for the temp file.
     * @return the {@link File} corresponding to the given temp file {@link URI}, if one exists.
     * @throws NullPointerException
     *     if the {@link URI} is null.
     * @throws IllegalArgumentException
     *     if the {@link URI} is invalid, including if it does not use the
     *     {@link TempFileResource#URI_SCHEME_PREFIX "temp:" URI scheme prefix}.
     * @throws FileNotFoundException
     *     if the requested {@link URI} does not correspond to a known temp file.
     */
    @Nonnull
    public final File getFile(@Nonnull URI uri) throws FileNotFoundException {
        Objects.requireNonNull(uri, "URI");
        if (!TempFileResource.URI_SCHEME_NAME.equals(uri.getScheme())) {
            throw new IllegalArgumentException("Illegal temp file URI.");
        }
        return getFileImpl(uri);
    }

    @Nonnull
    protected abstract TempFileResource createNewImpl(@Nonnull FileMetadata metadata) throws IOException;

    @Nonnull
    protected abstract URI getURIImpl(@Nonnull File file);

    @Nonnull
    protected abstract File getFileImpl(@Nonnull URI uri) throws FileNotFoundException;

}
