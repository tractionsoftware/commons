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

import com.tractionsoftware.commons.io.FileResource;
import com.tractionsoftware.commons.io.IOUtil;
import com.tractionsoftware.commons.io.LocalFileResource;
import com.tractionsoftware.commons.lang.ObjectUtil;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.math.BigInteger;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

public final class MD5Util {

    private static final Logger LOGGER = LoggerFactory.getLogger(MD5Util.class);

    /*
     * Not instantiable.
     */
    private MD5Util() {
    }

    public static final String ALGORITHM_NAME = "MD5";

    public interface DigestResult {

        public boolean wasSuccessful();

        public boolean isEmpty();

        @Nullable
        public byte[] hashBytes();

        @Nullable
        public String hashString();

        @Nullable
        public String paddedHashString();

    }

    private static final DigestResult DIGEST_RESULT_EMPTY_SUCCESS = new DigestResult() {

        @Override
        public boolean wasSuccessful() {
            return true;
        }

        @Override
        public boolean isEmpty() {
            return true;
        }

        @Nonnull
        @Override
        public byte[] hashBytes() {
            return ArrayUtils.EMPTY_BYTE_ARRAY;
        }

        @Nonnull
        @Override
        public String hashString() {
            return "";
        }

        @Nonnull
        @Override
        public String paddedHashString() {
            return "";
        }

    };

    private static final DigestResult DIGEST_RESULT_EMPTY_FAILURE = new DigestResult() {

        @Override
        public boolean wasSuccessful() {
            return false;
        }

        @Override
        public boolean isEmpty() {
            return true;
        }

        @Override
        public byte[] hashBytes() {
            return null;
        }

        @Override
        public String hashString() {
            return null;
        }

        @Override
        public String paddedHashString() {
            return null;
        }

    };

    private static DigestResult getResult(byte[] bytes) {
        if (bytes == null) {
            return DIGEST_RESULT_EMPTY_FAILURE;
        }
        if (bytes.length == 0) {
            return DIGEST_RESULT_EMPTY_SUCCESS;
        }
        return new NormalDigestResult(bytes);
    }

    private static final class NormalDigestResult implements DigestResult {

        private final byte[] bytes;

        private String hash;

        private String paddedHash;

        private NormalDigestResult(byte[] bytes) {
            this(bytes, null, null);
        }

        private NormalDigestResult(byte[] bytes, String hash, String paddedHash) {
            this.bytes = bytes;
            this.hash = hash;
            this.paddedHash = paddedHash;
        }

        @Override
        public boolean wasSuccessful() {
            return true;
        }

        @Override
        public boolean isEmpty() {
            return false;
        }

        @Override
        public byte[] hashBytes() {
            return bytes;
        }

        @Override
        public String hashString() {
            if (hash == null) {
                hash = MD5Util.getHashString(bytes);
            }
            return hash;
        }

        @Override
        public String paddedHashString() {
            if (paddedHash == null) {
                paddedHash = getPaddedHashString(bytes);
            }
            return paddedHash;
        }

    }

    public static MessageDigest getMessageDigest() {
        try {
            return MessageDigest.getInstance(ALGORITHM_NAME);
        }
        catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("This server is missing a required component.");
        }
    }

    public static final DigestInputStream createDigestInputStream(FileResource file) throws IOException {
        return createDigestInputStream(file.getInputStream());
    }

    public static final DigestInputStream createDigestInputStream(InputStream input) throws IOException {
        return new DigestInputStream(input, getMessageDigest());
    }

    public static final DigestResult digest(FileResource file) {
        if (file.isDirectory()) {
            return DIGEST_RESULT_EMPTY_SUCCESS;
        }
        return digestNonDirectoryFile(file);
    }

    public static final DigestResult digest(File file) {
        if (file.isDirectory()) {
            return DIGEST_RESULT_EMPTY_SUCCESS;
        }
        return digestNonDirectoryFile(LocalFileResource.createInstance(file));
    }

    public static final DigestResult digest(byte[] data) {
        try (DigestInputStream input = createDigestInputStream(new ByteArrayInputStream(data))) {
            return digestImpl(input);
        }
        catch (IOException e) {
            LOGGER.warn("Unexpected error attempting to compute an MD5 hash for bytes.", e);
            return DIGEST_RESULT_EMPTY_FAILURE;
        }
    }

    public static final DigestResult digest(InputStream input) {
        try {
            if (input instanceof DigestInputStream digestStream &&
                digestStream.getMessageDigest().getAlgorithm().equalsIgnoreCase(ALGORITHM_NAME)) {
                return digestImpl(digestStream);
            }
            return digestImpl(createDigestInputStream(input));
        }
        catch (IOException e) {
            LOGGER.warn(
                "Unexpected error attempting to compute an MD5 hash for stream {}",
                ObjectUtil.safeToStringObject(input),
                e
            );
            return DIGEST_RESULT_EMPTY_FAILURE;
        }
    }

    public static final String getHashString(byte[] digestedBytes) {
        return HexFormat.of().formatHex(digestedBytes);
    }

    public static final String getPaddedHashString(byte[] digestedBytes) {
        if (digestedBytes == null) {
            return null;
        }
        return String.format("%1$032x", new BigInteger(1, digestedBytes));
    }

    private static final DigestResult digestNonDirectoryFile(FileResource file) {

        if (file.isEmpty()) {
            try {
                return getInstanceForEmptyFile(file.getContentType());
            }
            catch (IOException e) {
                LOGGER.warn("Failed to determine MD5 for empty file {}", file, e);
                return DIGEST_RESULT_EMPTY_FAILURE;
            }
        }

        try (DigestInputStream input = file.getDigestInputStream()) {
            return digestImpl(input);
        }
        catch (IOException e) {
            LOGGER.warn("Failed to compute MD5 for file {}", file, e);
            return DIGEST_RESULT_EMPTY_FAILURE;
        }

    }

    public static final DigestResult getInstanceForEmptyFile(String contentType) throws IOException {
        return digest(IOUtil.getStringAsUtf8InputStream(StringUtils.trimToEmpty(contentType).toLowerCase()));
    }

    private static DigestResult digestImpl(DigestInputStream digestInput) throws IOException {
        try (OutputStream out = OutputStream.nullOutputStream()) {
            digestInput.transferTo(out);
        }
        return getResult(digestInput.getMessageDigest().digest());
    }

}
