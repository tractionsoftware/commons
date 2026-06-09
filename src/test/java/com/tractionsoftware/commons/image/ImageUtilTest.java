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

import com.tractionsoftware.commons.util.Dimensions;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.util.Objects;

/**
 * @author Andy Keller, Dave Shepperton
 */
public class ImageUtilTest {

    static final InputStream imageFileStream(String imageFileName) {
        return ImageUtilTest.class.getResourceAsStream(imageFileName);
    }

    static final File imageFile(String imageFileName) throws URISyntaxException {
        URL url = ImageUtilTest.class.getResource(imageFileName);
        Objects.requireNonNull(url, "image file URL for " + imageFileName);
        return Path.of(url.toURI()).toFile();
    }

    static final void assertSize(Dimensions<Integer> expected, String imageFileName) {
        try (InputStream input = imageFileStream(imageFileName)) {
            Assertions.assertNotNull(input, "Image file " + imageFileName);
            Assertions.assertEquals(expected, ImageUtil.getDimensions(input));
        }
        catch (IOException e) {
            Assertions.fail(e.toString());
        }
    }

    @Test
    public void normalPngImage() {
        assertSize(Dimensions.getInstanceInPixels(170, 170), "cat-normal.png");
    }

    @Test
    public void retinaPngImage() {
        assertSize(Dimensions.getInstanceInPixels(170 / 2, 170 / 2), "cat-retina.png");
    }

    @Test
    public void testDimensionsPng1() {
        assertSize(Dimensions.getInstanceInPixels(300, 299), "default-profile.png");
    }

    @Test
    public void testScaledDimensionsPng1() {
        try (InputStream input = imageFileStream("default-profile.png")) {
            Assertions.assertNotNull(input, "Image file");
            Dimensions<Integer> original = ImageUtil.getDimensions(input);
            Dimensions<Integer> max = Dimensions.getInstanceInPixels(120, 60);
            Dimensions<Integer> expected = Dimensions.getInstanceInPixels(60, 60);
            Dimensions<Integer> scaled = ImageUtil.getScaledDimensions(original, max);
            Assertions.assertEquals(expected, scaled);
        }
        catch (IOException e) {
            Assertions.fail(e.toString());
        }
    }

    @Test
    public void testIsImagePNG() {
        Assertions.assertTrue(ImageUtil.isImage("png", "image/png"));
    }

    @Test
    public void testIsImageQQQ() {
        Assertions.assertFalse(ImageUtil.isImage("qqq", "application/x-foobar"));
    }

    @Test
    public void testIsImageJPG() {
        // .jps is recognized even if image/garbage means nothing.
        Assertions.assertTrue(ImageUtil.isImage("jpg", "image/garbage"));
    }

    @Test
    public void testIsImageJPEG() {
        // .img means nothing, but image/jpeg is recognized.
        Assertions.assertTrue(ImageUtil.isImage("img", "image/jpeg"));
    }

    @Test
    public void testIsImageOctetStream() {
        Assertions.assertFalse(ImageUtil.isImage("img", "application/octet-stream"));
    }

    @Test
    public void testIsImageExtensionPNG() {
        Assertions.assertTrue(ImageUtil.isImageExtension("png"));
    }

    @Test
    public void testIsImageExtensionJPG() {
        Assertions.assertTrue(ImageUtil.isImageExtension("jpg"));
    }

    @Test
    public void testIsImageExtensionJPEG() {
        Assertions.assertTrue(ImageUtil.isImageExtension("jpeg"));
    }

    @Test
    public void testIsImageExtensionGIF() {
        Assertions.assertTrue(ImageUtil.isImageExtension("gif"));
    }

    @Test
    public void testIsImageExtensionBMP() {
        Assertions.assertTrue(ImageUtil.isImageExtension("bmp"));
    }

    @Test
    public void testIsImageExtensionSVG() {
        // No reader for SVG files
        Assertions.assertFalse(ImageUtil.isImageMimeType("svg"));
    }

    @Test
    public void testIsImageExtensionTIF() {
        Assertions.assertTrue(ImageUtil.isImageExtension("tif"));
    }

    @Test
    public void testIsImageExtensionTIFF() {
        Assertions.assertTrue(ImageUtil.isImageExtension("tiff"));
    }

    @Test
    public void testIsImageExtensionTXT() {
        Assertions.assertFalse(ImageUtil.isImageExtension("txt"));
    }

    @Test
    public void testIsImageMimeTypePNG() {
        Assertions.assertTrue(ImageUtil.isImageMimeType("image/png"));
    }

    @Test
    public void testIsImageMimeTypeJPEG() {
        Assertions.assertTrue(ImageUtil.isImageMimeType("image/jpeg"));
    }

    @Test
    public void testIsImageMimeTypeGIF() {
        Assertions.assertTrue(ImageUtil.isImageMimeType("image/gif"));
    }

    @Test
    public void testIsImageMimeTypeWEBP() {
        Assertions.assertTrue(ImageUtil.isImageMimeType("image/webp"));
    }

    @Test
    public void testIsImageMimeTypeTIFF() {
        Assertions.assertTrue(ImageUtil.isImageMimeType("image/tiff"));
    }

    @Test
    public void testIsImageMimeTypeSVG() {
        // No reader for SVG files
        Assertions.assertFalse(ImageUtil.isImageMimeType("image/svg+xml"));
    }

    @Test
    public void testIsImageMimeTypeHEIC() {
        Assertions.assertTrue(ImageUtil.isImageMimeType("image/heic"));
    }

    @Test
    public void testIsImageMimeTypeHEIF() {
        Assertions.assertTrue(ImageUtil.isImageMimeType("image/heif"));
    }

    @Test
    public void testIsImageMimeTypeXMSBMP() {
        // No image decoder for this mime type, apparently
        Assertions.assertFalse(ImageUtil.isImageMimeType("image/x-ms-bmp"));
    }

    @Test
    public void testIsImageMimeTypePICT() {
        // No image decoder for this mime type, apparently
        Assertions.assertFalse(ImageUtil.isImageMimeType("image/pict"));
    }

    @Test
    public void testIsImageMimeTypeText() {
        Assertions.assertFalse(ImageUtil.isImageMimeType("text/plain"));
    }

}
