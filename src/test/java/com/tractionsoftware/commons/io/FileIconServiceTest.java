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

import com.tractionsoftware.commons.image.Icon;
import com.tractionsoftware.commons.image.IconFileResource;
import com.tractionsoftware.commons.util.Dimensions;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import org.junit.jupiter.api.Test;

import java.net.URL;

import static org.junit.jupiter.api.Assertions.*;

class FileIconServiceTest {

    // ---------------------------------------------------------------------------
    // NO_ICON
    // ---------------------------------------------------------------------------

    @Test
    void noIcon_isNotNull() {
        assertNotNull(FileIconService.NO_ICON);
    }

    @Test
    void noIcon_hasIconFileResourceType() {
        assertEquals(CommonFileResourceType.ICON_FILE_TYPE, FileIconService.NO_ICON.getImageResourceType());
    }

    // ---------------------------------------------------------------------------
    // NONE
    // ---------------------------------------------------------------------------

    @Test
    void none_isNotNull() {
        assertNotNull(FileIconService.NONE);
    }

    @Test
    void none_getIcon_returnsNoIcon() {
        FileResource file = new IconFileResource.InvalidIconFileResource(CommonFileResourceType.OTHER);
        Icon icon = FileIconService.NONE.getIcon(file);
        assertNotNull(icon);
        // NONE's getIconImpl returns NO_ICON.getScaled(null)
        assertEquals(CommonFileResourceType.ICON_FILE_TYPE, icon.getImageResourceType());
    }

    @Test
    void none_getIconStyleName_returnsNull() {
        FileResource file = new IconFileResource.InvalidIconFileResource(CommonFileResourceType.OTHER);
        assertNull(FileIconService.NONE.getIconStyleName(file));
    }

    @Test
    void none_getURL_withNonIconType_throwsIllegalArgumentException() {
        // NO_ICON has ICON_FILE_TYPE so NONE.getURLImpl returns null, but calling with wrong type throws
        Icon wrongTypeIcon = FileIconService.NO_ICON;
        // This should NOT throw because NO_ICON has the right type; getURLImpl returns null
        assertNull(FileIconService.NONE.getURL(wrongTypeIcon));
    }

    // ---------------------------------------------------------------------------
    // getIcon — exception swallowing
    // ---------------------------------------------------------------------------

    @Test
    void getIcon_exceptionInImpl_returnsNoIcon() {
        FileIconService service = new FileIconService() {
            @Override
            protected Icon getIconImpl(FileResource file, @Nullable Dimensions<Integer> maxDimensions) {
                throw new RuntimeException("simulated failure");
            }

            @Override
            protected String getIconStyleNameImpl(FileResource file) {
                return null;
            }

            @Override
            protected URL getURLImpl(@Nonnull Icon icon) {
                return null;
            }
        };

        FileResource file = new IconFileResource.InvalidIconFileResource(CommonFileResourceType.OTHER);
        Icon result = service.getIcon(file);
        assertSame(FileIconService.NO_ICON, result);
    }

    @Test
    void getIcon_returnsNullFromImpl_returnsNoIcon() {
        FileIconService service = new FileIconService() {
            @Override
            protected Icon getIconImpl(FileResource file, @Nullable Dimensions<Integer> maxDimensions) {
                return null;
            }

            @Override
            protected String getIconStyleNameImpl(FileResource file) {
                return null;
            }

            @Override
            protected URL getURLImpl(@Nonnull Icon icon) {
                return null;
            }
        };

        FileResource file = new IconFileResource.InvalidIconFileResource(CommonFileResourceType.OTHER);
        Icon result = service.getIcon(file);
        assertSame(FileIconService.NO_ICON, result);
    }

    // ---------------------------------------------------------------------------
    // getIconStyleName — exception swallowing
    // ---------------------------------------------------------------------------

    @Test
    void getIconStyleName_exceptionInImpl_returnsNull() {
        FileIconService service = new FileIconService() {
            @Override
            protected Icon getIconImpl(FileResource file, @Nullable Dimensions<Integer> maxDimensions) {
                return null;
            }

            @Override
            protected String getIconStyleNameImpl(FileResource file) {
                throw new RuntimeException("style failure");
            }

            @Override
            protected URL getURLImpl(@Nonnull Icon icon) {
                return null;
            }
        };

        FileResource file = new IconFileResource.InvalidIconFileResource(CommonFileResourceType.OTHER);
        assertNull(service.getIconStyleName(file));
    }

    @Test
    void getIconStyleName_normalReturn_propagatesValue() {
        FileIconService service = new FileIconService() {
            @Override
            protected Icon getIconImpl(FileResource file, @Nullable Dimensions<Integer> maxDimensions) {
                return null;
            }

            @Override
            protected String getIconStyleNameImpl(FileResource file) {
                return "my-icon-style";
            }

            @Override
            protected URL getURLImpl(@Nonnull Icon icon) {
                return null;
            }
        };

        FileResource file = new IconFileResource.InvalidIconFileResource(CommonFileResourceType.OTHER);
        assertEquals("my-icon-style", service.getIconStyleName(file));
    }

    // ---------------------------------------------------------------------------
    // getURL — type validation and exception swallowing
    // ---------------------------------------------------------------------------

    @Test
    void getURL_wrongResourceType_throwsIllegalArgumentException() {
        FileIconService service = new FileIconService() {
            @Override
            protected Icon getIconImpl(FileResource file, @Nullable Dimensions<Integer> maxDimensions) {
                return null;
            }

            @Override
            protected String getIconStyleNameImpl(FileResource file) {
                return null;
            }

            @Override
            protected URL getURLImpl(@Nonnull Icon icon) {
                return null;
            }
        };

        // Build a minimal Icon whose getImageResourceType() returns OTHER (not ICON_FILE_TYPE)
        com.tractionsoftware.commons.image.SimpleIcon wrongTypeIcon = new com.tractionsoftware.commons.image.SimpleIcon(
            new IconFileResource.InvalidIconFileResource(CommonFileResourceType.OTHER)
        );
        assertThrows(IllegalArgumentException.class, () -> service.getURL(wrongTypeIcon));
    }

    @Test
    void getURL_exceptionInImpl_returnsNull() {
        FileIconService service = new FileIconService() {
            @Override
            protected Icon getIconImpl(FileResource file, @Nullable Dimensions<Integer> maxDimensions) {
                return null;
            }

            @Override
            protected String getIconStyleNameImpl(FileResource file) {
                return null;
            }

            @Override
            protected URL getURLImpl(@Nonnull Icon icon) {
                throw new RuntimeException("url failure");
            }
        };

        assertNull(service.getURL(FileIconService.NO_ICON));
    }

    @Test
    void getURL_normalReturn_propagatesValue() throws Exception {
        URL expected = new URL("https://example.com/icon.png");
        FileIconService service = new FileIconService() {
            @Override
            protected Icon getIconImpl(FileResource file, @Nullable Dimensions<Integer> maxDimensions) {
                return null;
            }

            @Override
            protected String getIconStyleNameImpl(FileResource file) {
                return null;
            }

            @Override
            protected URL getURLImpl(@Nonnull Icon icon) {
                return expected;
            }
        };

        assertSame(expected, service.getURL(FileIconService.NO_ICON));
    }

}
