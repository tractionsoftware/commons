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

import java.io.IOException;
import java.io.InputStream;

/**
 * @author Andy Keller, Dave Shepperton
 */
public class ImageUtilTest {

    static final InputStream imageFile(String imageFileName) {
        return ImageUtilTest.class.getResourceAsStream(imageFileName);
    }

    static final void assertSize(Dimensions<Integer> expected, String imageFileName) {
        try (InputStream input = imageFile(imageFileName)) {
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
        try (InputStream input = imageFile("default-profile.png")) {
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
    public void testIsImage1() {
        Assertions.assertTrue(ImageUtil.isImage("png", "image/png"));
    }

    @Test
    public void testIsImage2() {
        Assertions.assertFalse(ImageUtil.isImage("qqq", "application/x-foobar"));
    }

    @Test
    public void testIsImage3() {
        Assertions.assertTrue(ImageUtil.isImage("jpg", "image/jpeg"));
    }

    @Test
    public void testIsImage4() {
        Assertions.assertTrue(ImageUtil.isImage("img", "image/jpeg"));
    }

    @Test
    public void testIsImage5() {
        Assertions.assertFalse(ImageUtil.isImage("img", "application/octet-stream"));
    }

    @Test
    public void testIsImageExtension1() {
        Assertions.assertTrue(ImageUtil.isImageExtension("png"));
    }

    @Test
    public void testIsImageExtension2() {
        Assertions.assertTrue(ImageUtil.isImageExtension("jpg"));
    }

    @Test
    public void testIsImageExtension3() {
        Assertions.assertTrue(ImageUtil.isImageExtension("jpeg"));
    }

    @Test
    public void testIsImageExtension4() {
        Assertions.assertTrue(ImageUtil.isImageExtension("gif"));
    }

    @Test
    public void testIsImageExtension5() {
        Assertions.assertTrue(ImageUtil.isImageExtension("bmp"));
    }

    @Test
    public void testIsImageExtension6() {
        Assertions.assertTrue(ImageUtil.isImageExtension("tiff"));
    }

    @Test
    public void testIsImageExtension7() {
        Assertions.assertFalse(ImageUtil.isImageExtension("txt"));
    }

    @Test
    public void testIsImageMimeType1() {
        Assertions.assertTrue(ImageUtil.isImageMimeType("image/png"));
    }

    @Test
    public void testIsImageMimeType2() {
        Assertions.assertTrue(ImageUtil.isImageMimeType("image/jpeg"));
    }

    @Test
    public void testIsImageMimeType3() {
        Assertions.assertTrue(ImageUtil.isImageMimeType("image/gif"));
    }

    @Test
    public void testIsImageMimeType4() {
        Assertions.assertTrue(ImageUtil.isImageMimeType("image/bmp"));
    }

    @Test
    public void testIsImageMimeType5() {
        // No image decoder for this mime type, apparently
        Assertions.assertFalse(ImageUtil.isImageMimeType("image/x-ms-bmp"));
    }

    @Test
    public void testIsImageMimeType6() {
        // No image decoder for this mime type, apparently
        Assertions.assertFalse(ImageUtil.isImageMimeType("image/pict"));
    }

    @Test
    public void testIsImageMimeType7() {
        Assertions.assertFalse(ImageUtil.isImageMimeType("text/plain"));
    }

}
