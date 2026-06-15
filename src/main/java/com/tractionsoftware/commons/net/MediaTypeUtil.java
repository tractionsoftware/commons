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

package com.tractionsoftware.commons.net;

import com.google.common.collect.*;
import com.google.common.net.MediaType;
import com.tractionsoftware.commons.lang.JavaUtil;
import com.tractionsoftware.commons.lang.StringUtil;
import com.tractionsoftware.commons.util.CollectionUtil;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.function.Supplier;

public final class MediaTypeUtil {

    private MediaTypeUtil() {
    }

    private static final Logger LOGGER = LoggerFactory.getLogger(MediaTypeUtil.class);

    public static final MediaType RFC_822_MESSAGE = MediaType.create("message", "rfc822");

    private static final Supplier<? extends ContentTypeFileExtensionMapper> contentTypeFileExtensionMapper =
        JavaUtil.<ContentTypeFileExtensionMapper>lazyServiceLoader(
            ContentTypeFileExtensionMapper.class,
            MediaTypeUtil::createDefaultContentTypeFileExtensionMapper,
            LOGGER
        );

    public static interface ContentTypeFileExtensionMapper {

        @Nullable
        public String getPreferredFileExtension(@Nullable MediaType contentType);

        @Nonnull
        public SequencedSet<String> getFileExtensions(@Nullable MediaType contentType);

        @Nullable
        public MediaType getPreferredContentType(@Nullable String extension);

        @Nonnull
        public SequencedSet<MediaType> getContentTypes(@Nullable String extension);

    }

    public static final class SimpleMultimapContentTypeFileExtensionMapper implements ContentTypeFileExtensionMapper {

        public static final SimpleMultimapContentTypeFileExtensionMapper createInstance(Map<MediaType,SequencedSet<String>> type2extensions, Map<String,SequencedSet<MediaType>> extension2types) {
            Objects.requireNonNull(type2extensions, "type-to-extensions");
            Objects.requireNonNull(extension2types, "extension-to-types");
            return new SimpleMultimapContentTypeFileExtensionMapper(type2extensions, extension2types);
        }

        private final Map<MediaType,SequencedSet<String>> type2extensions;

        private final Map<String,SequencedSet<MediaType>> extension2types;

        private SimpleMultimapContentTypeFileExtensionMapper(Map<MediaType,SequencedSet<String>> type2extensions, Map<String,SequencedSet<MediaType>> extension2types) {
            this.type2extensions = type2extensions;
            this.extension2types = extension2types;
        }

        @Nullable
        @Override
        public final String getPreferredFileExtension(@Nullable MediaType contentType) {
            if (contentType == null) {
                return null;
            }
            SequencedSet<String> extensions = type2extensions.get(contentType);
            if (extensions == null) {
                return null;
            }
            return extensions.getFirst();
        }

        @Nonnull
        @Override
        public final SequencedSet<String> getFileExtensions(@Nullable MediaType contentType) {
            if (contentType == null) {
                return CollectionUtil.emptySequencedSet();
            }
            SequencedSet<String> extensions = type2extensions.get(contentType);
            if (extensions == null) {
                if (!contentType.parameters().isEmpty()) {
                    extensions = type2extensions.get(contentType.withoutParameters());
                }
            }
            if (extensions == null) {
                return CollectionUtil.emptySequencedSet();
            }
            return CollectionUtil.unmodifiableSequencedSet(extensions);
        }

        @Nullable
        @Override
        public final MediaType getPreferredContentType(@Nullable String extension) {
            extension = StringUtils.removeStart(extension, '.');
            if (StringUtils.isBlank(extension)) {
                return null;
            }
            return CollectionUtil.firstOrDefault(extension2types.get(extension.toLowerCase()), null);
        }

        @Nonnull
        @Override
        public final SequencedSet<MediaType> getContentTypes(@Nullable String extension) {
            if (extension == null) {
                return CollectionUtil.emptySequencedSet();
            }
            return Objects.requireNonNullElseGet(
                extension2types.get(extension.toLowerCase()), CollectionUtil::emptySequencedSet
            );
        }

    }

    public static final MediaType parseMediaType(@Nullable String contentTypeSpec) {
        return parseMediaType(contentTypeSpec, null);
    }

    public static final MediaType parseMediaType(@Nullable String contentTypeSpec, @Nullable MediaType defaultType) {
        if (StringUtils.isBlank(contentTypeSpec)) {
            return defaultType;
        }
        try {
            return MediaType.parse(contentTypeSpec);
        }
        catch (IllegalArgumentException e) {
            LOGGER.warn(
                "Failed to parse mime/content-type spec {}", StringUtil.truncatedToStringForLog(contentTypeSpec), e
            );
            return defaultType;
        }
    }

    public static final boolean isTextHtmlContentType(@Nullable String contentTypeSpec) {
        return matchesMainAndSubTypes(contentTypeSpec, MediaType.HTML_UTF_8);
    }

    public static final boolean isTextPlainContentType(@Nullable String contentTypeSpec) {
        return matchesMainAndSubTypes(contentTypeSpec, MediaType.PLAIN_TEXT_UTF_8);
    }

    public static final boolean matchesMainAndSubTypes(@Nullable String contentTypeSpec, @Nullable MediaType typeToMatch) {
        if (contentTypeSpec == null || typeToMatch == null) {
            return false;
        }
        MediaType contentType = parseMediaType(contentTypeSpec);
        if (contentType == null) {
            return false;
        }
        return typeToMatch.withoutParameters().is(contentType.withoutParameters());
    }

    /**
     * Returns true if the given "Content-Type" value is not null and is a text-oriented type.
     *
     * @param contentTypeSpec
     *     the value for a "Content-Type" HTTP response header field.
     * @return true if the given "Content-Type" value is not null and is a text-oriented type; false otherwise.
     */
    public static final boolean isTextContentType(@Nullable String contentTypeSpec) {
        if (contentTypeSpec == null) {
            return false;
        }
        return isTextContentType(parseMediaType(contentTypeSpec));
    }

    public static final boolean isTextContentType(@Nullable MediaType type) {

        if (type == null) {
            return false;
        }

        MediaType noParamsType = type.withoutParameters();
        if (noParamsType.is(MediaType.ANY_TEXT_TYPE) || RFC_822_MESSAGE.is(type)) {
            // text/*
            return true;
        }

        if (MediaType.ANY_APPLICATION_TYPE.type().equals(noParamsType.type())) {
            // Some application/* types that we know to be text.
            String subType = noParamsType.subtype();
            if (MediaType.JAVASCRIPT_UTF_8.subtype().equals(subType) ||
                MediaType.XML_UTF_8.subtype().equals(subType) ||
                MediaType.JSON_UTF_8.subtype().equals(subType) ||
                MediaType.JOSE.subtype().equals(subType) ||
                "x-sh".equals(subType) ||
                "x-powershell".equals(subType) ||
                subType.endsWith("+xml") ||
                subType.endsWith("+json")) {
                return true;
            }
        }

        return false;

    }

    public static final boolean hasCharsetParameter(@Nullable String contentTypeSpec) {

        if (StringUtils.isBlank(contentTypeSpec)) {
            return false;
        }

        try {
            MediaType type = parseMediaType(contentTypeSpec);
            if (type != null) {
                return type.charset().isPresent();
            }
        }
        catch (Exception e) {
            LOGGER.warn("Failed to parse mime type spec {}", StringUtil.truncatedToStringForLog(contentTypeSpec), e);
        }
        return false;

    }

    public static final String getExtensionFromContentType(@Nullable String contentType) {
        return getExtensionFromContentType(parseMediaType(contentType));
    }

    public static final String getExtensionFromContentType(@Nullable MediaType contentType) {
        if (contentType == null) {
            return null;
        }
        return contentTypeFileExtensionMapper.get().getPreferredFileExtension(contentType);
    }

    @Nullable
    public static final MediaType getContentTypeFromExtension(@Nullable String extension) {
        return getContentTypeFromExtension(extension, null);
    }

    @Nullable
    public static final MediaType getContentTypeFromExtension(@Nullable String extension, @Nullable MediaType defaultType) {
        return ObjectUtils.getIfNull(
            contentTypeFileExtensionMapper.get().getPreferredContentType(extension), defaultType
        );
    }

    private static final SimpleMultimapContentTypeFileExtensionMapper createDefaultContentTypeFileExtensionMapper() {

        ImmutableMap.Builder<MediaType,SequencedSet<String>> type2extensions = ImmutableMap.builder();
        ImmutableMap.Builder<String,SequencedSet<MediaType>> extension2types = ImmutableMap.builder();

        addFallbackContentTypeSingletonMapping(
            MediaType.PLAIN_TEXT_UTF_8, "txt", type2extensions, extension2types
        );
        addFallbackContentTypeSingletonMapping(
            MediaType.CSS_UTF_8, "css", type2extensions, extension2types
        );
        addFallbackContentTypeSingletonMapping(
            MediaType.MD_UTF_8, "md", type2extensions, extension2types
        );
        type2extensions.put(
            MediaType.CSV_UTF_8.withoutParameters(), CollectionUtil.singletonOrEmptySequencedSet("csv")
        );
        addFallbackContentTypeSingletonMapping(
            MediaType.GIF.withoutParameters(), "gif", type2extensions, extension2types
        );
        addFallbackContentTypeSingletonMapping(
            MediaType.PNG.withoutParameters(), "png", type2extensions, extension2types
        );
        addFallbackContentTypeSingletonMapping(
            MediaType.PDF.withoutParameters(), "pdf", type2extensions, extension2types
        );
        type2extensions.put(
            MediaType.HEIF, CollectionUtil.unmodifiableSequencedSet("heif", "heifs", "heic", "heics", "hif")
        );

        MediaType htmlType = MediaType.HTML_UTF_8.withoutParameters();
        SequencedSet<MediaType> htmlTypeSet = CollectionUtil.singletonSequencedSet(htmlType);

        type2extensions.put(htmlType, CollectionUtil.unmodifiableSequencedSet("html", "htm"));
        extension2types.put("html", htmlTypeSet);
        extension2types.put("htm", htmlTypeSet);

        // JavaScript, XML, JSON, JPEG
        MediaType jsonType = MediaType.JSON_UTF_8.withoutParameters();
        MediaType joseJsonType = MediaType.JOSE_JSON.withoutParameters();
        MediaType geoJsonType = MediaType.GEO_JSON.withoutParameters();
        MediaType halJsonType = MediaType.HAL_JSON.withoutParameters();
        MediaType manJsonType = MediaType.MANIFEST_JSON_UTF_8.withoutParameters();

        SequencedSet<String> jsonExt = CollectionUtil.singletonSequencedSet("json");
        type2extensions.put(jsonType, jsonExt);
        type2extensions.put(joseJsonType, jsonExt);
        type2extensions.put(geoJsonType, jsonExt);
        type2extensions.put(halJsonType, jsonExt);
        type2extensions.put(manJsonType, jsonExt);
        extension2types.put(
            "json", CollectionUtil.unmodifiableSequencedSet(
                jsonType, joseJsonType, geoJsonType, halJsonType, manJsonType
            )
        );

        type2extensions.put(
            MediaType.JPEG, CollectionUtil.unmodifiableSequencedSet("jpg", "jpeg", "jpe", "jif", "jfif", "jfi")
        );
        SequencedSet<MediaType> jpegType = CollectionUtil.singletonSequencedSet(MediaType.JPEG);
        extension2types.put("jpg", jpegType);
        extension2types.put("jpeg", jpegType);
        extension2types.put("jpe", jpegType);
        extension2types.put("jif", jpegType);
        extension2types.put("jfif", jpegType);
        extension2types.put("jfi", jpegType);

        MediaType jsApp = MediaType.JAVASCRIPT_UTF_8.withoutParameters();
        MediaType jsTxt = MediaType.TEXT_JAVASCRIPT_UTF_8.withoutParameters();
        SequencedSet<String> jsExtSet = CollectionUtil.singletonSequencedSet("js");
        type2extensions.put(jsApp, jsExtSet);
        type2extensions.put(jsTxt, jsExtSet);
        extension2types.put("js", CollectionUtil.unmodifiableSequencedSet(jsApp, jsTxt));

        return SimpleMultimapContentTypeFileExtensionMapper.createInstance(
            type2extensions.build(), extension2types.build()
        );

    }

    private static void addFallbackContentTypeSingletonMapping(@Nonnull MediaType type, @Nonnull String ext, @Nonnull ImmutableMap.Builder<MediaType,SequencedSet<String>> type2extensions, ImmutableMap.Builder<String,SequencedSet<MediaType>> extension2types) {
        type = type.withoutParameters();
        type2extensions.put(type, CollectionUtil.singletonSequencedSet(ext));
        extension2types.put(ext, CollectionUtil.singletonSequencedSet(type));
    }

}
