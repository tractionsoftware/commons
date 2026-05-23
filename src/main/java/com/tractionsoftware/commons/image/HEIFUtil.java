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

package com.tractionsoftware.commons.image;

import com.tractionsoftware.heif.HeicMetaReader;
import com.tractionsoftware.heif.entity.ImageSimpleMeta;
import com.tractionsoftware.commons.util.Dimensions;

import java.io.IOException;
import java.io.InputStream;

/**
 * Helpers for the HEIF/HEIC format. Currently this only covers metadata.
 *
 * @author Dave Shepperton
 */
public final class HEIFUtil {

    private HEIFUtil() {
    }

    public static final Dimensions<Integer> getDimensions(InputStream input) throws IOException {
        ImageSimpleMeta meta = HeicMetaReader.readMetadata(input);
        return Dimensions.getInstanceInPixels(meta.getWidth(), meta.getHeight());
    }

}
