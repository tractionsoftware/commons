package com.tractionsoftware.commons.io;

import com.tractionsoftware.commons.image.Icon;
import com.tractionsoftware.commons.util.Dimensions;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.Objects;

public final class JavaFileResource implements FileResource {

    public static final JavaFileResource createInstance(File file) {
        return createInstance(file, null);
    }

    public static final JavaFileResource createInstance(File file, FileMetadata metadata) {
        Objects.requireNonNull(file, "File");
        return new JavaFileResource(file, metadata);
    }

    private final File file;

    private final FileMetadata metadata;

    private JavaFileResource(File file, FileMetadata metadata) {
        this.file = file;
        this.metadata = metadata;
    }

    @Override
    public boolean isValid() {
        return file.exists() && file.canRead();
    }

    @Override
    public String getPath() {
        return file.getPath();
    }

    @Override
    public String getFilename() {
        return file.getName();
    }

    @Override
    public SizedInputStream getInputStream() throws IOException {
        return SizedInputStream.forInputStream(FileUtil.getBufferedInputStream(file), file.length());
    }

    @Override
    public String getDescription() {
        if (metadata == null) {
            return null;
        }
        return metadata.getDescription();
    }

    @Override
    public String getContentType() {
        if (metadata == null) {
            return null;
        }
        return metadata.getContentType();
    }

    @Override
    public String getContentId() {
        if (metadata == null) {
            return null;
        }
        return metadata.getContentId();
    }

    @Override
    public long getByteSize() {
        return file.length();
    }

    @Override
    public String getFormattedSize() {
        throw new RuntimeException("Not quite implemented");
    }

    @Override
    public Date getLastModified() {
        return new Date(file.lastModified());
    }

    @Override
    public FileMetadata getMetadata() {
        return metadata.toReadOnly();
    }

    @Override
    public Icon getImage(Dimensions<Integer> maxDimensions) {
        return null;
    }

}
