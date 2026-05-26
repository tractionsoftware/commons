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

/**
 * Some common file resource types.
 *
 * @author Dave Shepperton
 */
public enum CommonFileResourceType implements FileResourceType {

    /**
     * Content, such as an image embedded in a document.
     */
    CONTENT(true),

    /**
     * An icon for a type of file.
     */
    ICON_FILE_TYPE(true),

    ICON_OTHER(true),

    /**
     * A logo or other banner image.
     */
    LOGO(true),

    /**
     * A user profile picture image.
     */
    PROFILE_PICTURE(true),

    /**
     * Inline data, usually from a data: URI.
     */
    DATA(false),

    /**
     * Referencing an external source, but which has been retrieved and persisted locally.
     */
    EXTERNAL_RETRIEVED(true),

    /**
     * Referencing an external source.
     */
    EXTERNAL(false),

    /**
     * A thumbnail of a content image.
     */
    CONTENT_THUMBNAIL(false),

    /**
     * Something else.
     */
    OTHER(false);

    private final boolean supportsContentId;

    private CommonFileResourceType(boolean supportsContentId) {
        this.supportsContentId = supportsContentId;
    }

    public final boolean supportsContentId() {
        return supportsContentId;
    }

}
