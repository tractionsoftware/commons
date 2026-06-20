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

import com.tractionsoftware.commons.io.FileUtil;
import com.tractionsoftware.commons.io.LocalFileResource;
import com.tractionsoftware.commons.util.Dimensions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.*;

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
            assertNotNull(input, "Image file " + imageFileName);
            assertEquals(expected, ImageUtil.getDimensions(input));
        }
        catch (IOException e) {
            fail(e.toString());
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
            assertNotNull(input, "Image file");
            Dimensions<Integer> original = ImageUtil.getDimensions(input);
            Dimensions<Integer> max = Dimensions.getInstanceInPixels(120, 60);
            Dimensions<Integer> expected = Dimensions.getInstanceInPixels(60, 60);
            Dimensions<Integer> scaled = ImageUtil.getScaledDimensions(original, max);
            assertEquals(expected, scaled);
        }
        catch (IOException e) {
            fail(e.toString());
        }
    }

    @Test
    public void testIsImagePNG() {
        assertTrue(ImageUtil.isImage("png", "image/png"));
    }

    @Test
    public void testIsImageQQQ() {
        assertFalse(ImageUtil.isImage("qqq", "application/x-foobar"));
    }

    @Test
    public void testIsImageJPG() {
        // .jps is recognized even if image/garbage means nothing.
        assertTrue(ImageUtil.isImage("jpg", "image/garbage"));
    }

    @Test
    public void testIsImageJPEG() {
        // .img means nothing, but image/jpeg is recognized.
        assertTrue(ImageUtil.isImage("img", "image/jpeg"));
    }

    @Test
    public void testIsImageOctetStream() {
        assertFalse(ImageUtil.isImage("img", "application/octet-stream"));
    }

    @Test
    public void testIsImageExtensionPNG() {
        assertTrue(ImageUtil.isImageExtension("png"));
    }

    @Test
    public void testIsImageExtensionJPG() {
        assertTrue(ImageUtil.isImageExtension("jpg"));
    }

    @Test
    public void testIsImageExtensionJPEG() {
        assertTrue(ImageUtil.isImageExtension("jpeg"));
    }

    @Test
    public void testIsImageExtensionGIF() {
        assertTrue(ImageUtil.isImageExtension("gif"));
    }

    @Test
    public void testIsImageExtensionBMP() {
        assertTrue(ImageUtil.isImageExtension("bmp"));
    }

    @Test
    public void testIsImageExtensionSVG() {
        // No reader for SVG files
        assertFalse(ImageUtil.isImageMimeType("svg"));
    }

    @Test
    public void testIsImageExtensionTIF() {
        assertTrue(ImageUtil.isImageExtension("tif"));
    }

    @Test
    public void testIsImageExtensionTIFF() {
        assertTrue(ImageUtil.isImageExtension("tiff"));
    }

    @Test
    public void testIsImageExtensionTXT() {
        assertFalse(ImageUtil.isImageExtension("txt"));
    }

    @Test
    public void testIsImageMimeTypePNG() {
        assertTrue(ImageUtil.isImageMimeType("image/png"));
    }

    @Test
    public void testIsImageMimeTypeJPEG() {
        assertTrue(ImageUtil.isImageMimeType("image/jpeg"));
    }

    @Test
    public void testIsImageMimeTypeGIF() {
        assertTrue(ImageUtil.isImageMimeType("image/gif"));
    }

    @Test
    public void testIsImageMimeTypeWEBP() {
        assertTrue(ImageUtil.isImageMimeType("image/webp"));
    }

    @Test
    public void testIsImageMimeTypeTIFF() {
        assertTrue(ImageUtil.isImageMimeType("image/tiff"));
    }

    @Test
    public void testIsImageMimeTypeSVG() {
        // No reader for SVG files
        assertFalse(ImageUtil.isImageMimeType("image/svg+xml"));
    }

    @Test
    public void testIsImageMimeTypeHEIC() {
        assertTrue(ImageUtil.isImageMimeType("image/heic"));
    }

    @Test
    public void testIsImageMimeTypeHEIF() {
        assertTrue(ImageUtil.isImageMimeType("image/heif"));
    }

    @Test
    public void testIsImageMimeTypeXMSBMP() {
        // No image decoder for this mime type, apparently
        assertFalse(ImageUtil.isImageMimeType("image/x-ms-bmp"));
    }

    @Test
    public void testIsImageMimeTypePICT() {
        // No image decoder for this mime type, apparently
        assertFalse(ImageUtil.isImageMimeType("image/pict"));
    }

    @Test
    public void testIsImageMimeTypeText() {
        assertFalse(ImageUtil.isImageMimeType("text/plain"));
    }

    // ---------------------------------------------------------------------------
    // getImgWidthAttributeHtml
    // ---------------------------------------------------------------------------

    @Test
    public void getImgWidthAttributeHtml_negativeWidth_returnsEmpty() {
        assertEquals("", ImageUtil.getImgWidthAttributeHtml(-1));
    }

    @Test
    public void getImgWidthAttributeHtml_zeroWidth_returnsAttribute() {
        String result = ImageUtil.getImgWidthAttributeHtml(0);
        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertTrue(result.contains("0"));
    }

    @Test
    public void getImgWidthAttributeHtml_positiveWidth_returnsAttributeWithValue() {
        String result = ImageUtil.getImgWidthAttributeHtml(120);
        assertNotNull(result);
        assertTrue(result.contains("120"));
        assertTrue(result.contains("width"));
    }

    // ---------------------------------------------------------------------------
    // getImgHeightAttributeHtml
    // ---------------------------------------------------------------------------

    @Test
    public void getImgHeightAttributeHtml_negativeHeight_returnsEmpty() {
        assertEquals("", ImageUtil.getImgHeightAttributeHtml(-1));
    }

    @Test
    public void getImgHeightAttributeHtml_zeroHeight_returnsAttribute() {
        String result = ImageUtil.getImgHeightAttributeHtml(0);
        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertTrue(result.contains("0"));
    }

    @Test
    public void getImgHeightAttributeHtml_positiveHeight_returnsAttributeWithValue() {
        String result = ImageUtil.getImgHeightAttributeHtml(80);
        assertNotNull(result);
        assertTrue(result.contains("80"));
        assertTrue(result.contains("height"));
    }

    // ---------------------------------------------------------------------------
    // getScaledDimensions — additional edge cases
    // ---------------------------------------------------------------------------

    @Test
    public void getScaledDimensions_nullOriginal_returnsNull() {
        assertNull(ImageUtil.getScaledDimensions(null, Dimensions.getInstanceInPixels(100, 100)));
    }

    @Test
    public void getScaledDimensions_nullMax_returnsOriginal() {
        Dimensions<Integer> original = Dimensions.getInstanceInPixels(300, 200);
        assertSame(original, ImageUtil.getScaledDimensions(original, null));
    }

    @Test
    public void getScaledDimensions_alreadyWithinMax_returnsOriginal() {
        Dimensions<Integer> original = Dimensions.getInstanceInPixels(50, 50);
        Dimensions<Integer> max = Dimensions.getInstanceInPixels(100, 100);
        // Already fits, should return original unchanged
        assertEquals(original, ImageUtil.getScaledDimensions(original, max));
    }

    @Test
    public void getScaledDimensions_widerThanTall_scalesByWidth() {
        Dimensions<Integer> original = Dimensions.getInstanceInPixels(400, 200);
        Dimensions<Integer> max = Dimensions.getInstanceInPixels(200, 200);
        Dimensions<Integer> scaled = ImageUtil.getScaledDimensions(original, max);
        assertNotNull(scaled);
        assertEquals(200, (int) scaled.getWidth());
        assertEquals(100, (int) scaled.getHeight());
    }

    @Test
    public void getScaledDimensions_tallerThanWide_scalesByHeight() {
        Dimensions<Integer> original = Dimensions.getInstanceInPixels(200, 400);
        Dimensions<Integer> max = Dimensions.getInstanceInPixels(200, 200);
        Dimensions<Integer> scaled = ImageUtil.getScaledDimensions(original, max);
        assertNotNull(scaled);
        assertEquals(100, (int) scaled.getWidth());
        assertEquals(200, (int) scaled.getHeight());
    }

    // ---------------------------------------------------------------------------
    // getImageFileExtensionFromContents
    // ---------------------------------------------------------------------------

    @Test
    public void getImageFileExtensionFromContents_png_returnsPng() throws Exception {
        try (InputStream input = imageFileStream("cat-normal.png")) {
            String ext = ImageUtil.getImageFileExtensionFromContents(input);
            assertNotNull(ext);
            assertTrue(ext.equalsIgnoreCase("png"),
                "Expected 'png' but got: " + ext);
        }
    }

    @Test
    public void getImageFileExtensionFromContents_nonImage_returnsNull() {
        // Plain text is not a recognized image format
        java.io.InputStream textStream = new java.io.ByteArrayInputStream("hello world".getBytes());
        String ext = ImageUtil.getImageFileExtensionFromContents(textStream);
        assertNull(ext);
    }

    // ---------------------------------------------------------------------------
    // isImageExtension / isImageMimeType — additional edge cases
    // ---------------------------------------------------------------------------

    @Test
    public void isImageExtension_null_returnsFalse() {
        assertFalse(ImageUtil.isImageExtension(null));
    }

    @Test
    public void isImageExtension_blank_returnsFalse() {
        assertFalse(ImageUtil.isImageExtension("   "));
    }

    @Test
    public void isImageExtension_webp_returnsTrue() {
        assertTrue(ImageUtil.isImageExtension("webp"));
    }

    @Test
    public void isImageExtension_heic_returnsTrue() {
        assertTrue(ImageUtil.isImageExtension("heic"));
    }

    @Test
    public void isImageExtension_heif_returnsTrue() {
        assertTrue(ImageUtil.isImageExtension("heif"));
    }

    @Test
    public void isImageMimeType_null_returnsFalse() {
        assertFalse(ImageUtil.isImageMimeType(null));
    }

    @Test
    public void isImageMimeType_blank_returnsFalse() {
        assertFalse(ImageUtil.isImageMimeType("   "));
    }

    // ---------------------------------------------------------------------------
    // getDimensions(InputStream) — unsupported format
    // ---------------------------------------------------------------------------

    @Test
    public void getDimensions_unrecognizedFormat_throwsIOException() {
        InputStream input = new ByteArrayInputStream("not an image".getBytes());
        IOException ex = assertThrows(IOException.class, () -> ImageUtil.getDimensions(input));
        assertTrue(ex.getMessage().contains("Unsupported format"));
    }

    // ---------------------------------------------------------------------------
    // getScaledDimensions / scalefactor — sentinel and degenerate cases
    // ---------------------------------------------------------------------------

    @Test
    public void getScaledDimensions_maxWidthUnconstrained_scalesByHeightOnly() {
        // max width of -1 is the "no constraint on this axis" sentinel handled by scalefactor().
        Dimensions<Integer> original = Dimensions.getInstanceInPixels(200, 100);
        Dimensions<Integer> max = Dimensions.getInstanceInPixels(-1, 50);
        Dimensions<Integer> scaled = ImageUtil.getScaledDimensions(original, max);
        assertNotNull(scaled);
        assertEquals(100, (int) scaled.getWidth());
        assertEquals(50, (int) scaled.getHeight());
    }

    @Test
    public void getScaledDimensions_zeroOriginalWidth_doesNotDivideByZero() {
        // an original dimension of 0 is the degenerate case guarded against by scalefactor()'s "l == 0" check.
        Dimensions<Integer> original = Dimensions.getInstanceInPixels(0, 100);
        Dimensions<Integer> max = Dimensions.getInstanceInPixels(50, 50);
        Dimensions<Integer> scaled = ImageUtil.getScaledDimensions(original, max);
        assertNotNull(scaled);
        assertEquals(0, (int) scaled.getWidth());
        assertEquals(50, (int) scaled.getHeight());
    }

    // ---------------------------------------------------------------------------
    // cropToSquare
    // ---------------------------------------------------------------------------

    @Test
    public void cropToSquare_alreadySquare_returnsSameInstance() {
        BufferedImage img = new BufferedImage(50, 50, BufferedImage.TYPE_INT_ARGB);
        assertSame(img, ImageUtil.cropToSquare(img));
    }

    @Test
    public void cropToSquare_portraitImage_cropsToWidthAndCentersVertically() {
        int ow = 20;
        int oh = 100;
        BufferedImage img = new BufferedImage(ow, oh, BufferedImage.TYPE_INT_ARGB);
        for (int y = 0; y < oh; y++) {
            img.setRGB(0, y, new Color(y % 256, 0, 0).getRGB());
        }
        BufferedImage cropped = ImageUtil.cropToSquare(img);
        assertEquals(ow, cropped.getWidth());
        assertEquals(ow, cropped.getHeight());

        int expectedCenteredOffset = (oh - ow) / 2; // 40, the portrait branch divides by 2 correctly.
        int topEdgeOriginalY = new Color(cropped.getRGB(0, 0)).getRed();
        assertEquals(expectedCenteredOffset, topEdgeOriginalY);
    }

    @Test
    public void cropToSquare_landscapeImage_cropsToHeightAndCentersVertically() {
        int ow = 100;
        int oh = 20;
        BufferedImage img = new BufferedImage(ow, oh, BufferedImage.TYPE_INT_ARGB);
        for (int x = 0; x < ow; x++) {
            img.setRGB(x, 0, new Color(x % 256, 0, 0).getRGB());
        }
        BufferedImage cropped = ImageUtil.cropToSquare(img);
        assertEquals(oh, cropped.getWidth());
        assertEquals(oh, cropped.getHeight());

        // 40 -- what a centered crop would use
        int centeredOffsetCallerWouldExpect = (ow - oh) / 2;
        int leftEdgeOriginalX = new Color(cropped.getRGB(0, 0)).getRed();
        assertEquals(centeredOffsetCallerWouldExpect, leftEdgeOriginalX);
    }

    // ---------------------------------------------------------------------------
    // ImageCreationResult.hadFailure() — inverted-logic bug
    // ---------------------------------------------------------------------------

    @Test
    public void hadFailure_onSuccessfulResult_returnsFalse() throws Exception {
        File originalFile = imageFile("cat-normal.png");
        File scaledFile = File.createTempFile("scaled-success", ".png");
        assertTrue(scaledFile.delete());
        try {
            ImageUtil.ImageCreationResult result = ImageUtil.createScaledPNG(originalFile, scaledFile, null);
            assertEquals(ImageUtil.ImageCreationResultStatus.SUCCESS, result.getStatus());
            assertFalse(result.hadFailure());
        }
        finally {
            FileUtil.deleteOrDeleteOnExit(scaledFile);
        }
    }

    @Test
    public void hadFailure_onFailedResult_returnsTrue() throws Exception {
        // BUG: see hadFailure_onSuccessfulResult_bugReturnsTrueInsteadOfFalse above -- a genuinely failed
        // result reports hadFailure() == false here, again the opposite of what is documented.
        File originalFile = new File("/does/not/exist-" + System.nanoTime() + ".png");
        File scaledFile = File.createTempFile("scaled-failure", ".png");
        assertTrue(scaledFile.delete());
        ImageUtil.ImageCreationResult result = ImageUtil.createScaledPNG(originalFile, scaledFile, null);
        assertEquals(ImageUtil.ImageCreationResultStatus.FAILURE, result.getStatus());
        assertTrue(result.hadFailure());
    }

    // ---------------------------------------------------------------------------
    // createScaledPNG(InputStream, OutputStream, Dimensions)
    // ---------------------------------------------------------------------------

    @Test
    public void createScaledPNG_streamOverload_withMaxDimensions_scalesAndSucceeds() throws Exception {
        try (InputStream in = imageFileStream("default-profile.png")) {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            Dimensions<Integer> max = Dimensions.getInstanceInPixels(100, 100);
            ImageUtil.ImageCreationResult result = ImageUtil.createScaledPNG(in, out, max);
            assertEquals(ImageUtil.ImageCreationResultStatus.SUCCESS, result.getStatus());
            assertTrue(out.size() > 0);
            assertNotNull(result.dimensions());
            assertTrue(result.dimensions().getWidth() <= 100);
            assertEquals(result.dimensions().getWidth(), result.dimensions().getHeight());
        }
    }

    @Test
    public void createScaledPNG_streamOverload_noMaxDimensions_usesCroppedOriginalSize() throws Exception {
        try (InputStream in = imageFileStream("default-profile.png")) {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            ImageUtil.ImageCreationResult result = ImageUtil.createScaledPNG(in, out, null);
            assertEquals(ImageUtil.ImageCreationResultStatus.SUCCESS, result.getStatus());
            assertTrue(out.size() > 0);
            // default-profile.png is 300x299; cropToSquare runs before sizing is determined, so the
            // result is the square crop (299x299), not the original 300x299.
            assertEquals(Dimensions.getInstanceInPixels(299, 299), result.dimensions());
        }
    }

    @Test
    public void createScaledPNG_streamOverload_unrecognizedImageData_returnsFailure() {
        InputStream in = new ByteArrayInputStream("not an image".getBytes());
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ImageUtil.ImageCreationResult result = ImageUtil.createScaledPNG(in, out, null);
        assertEquals(ImageUtil.ImageCreationResultStatus.FAILURE, result.getStatus());
        assertNotNull(result.error());
        assertEquals(Dimensions.getInvalidInstanceInPixels(), result.dimensions());
    }

    // ---------------------------------------------------------------------------
    // createScaledPNG(File, File, Dimensions)
    // ---------------------------------------------------------------------------

    @Test
    public void createScaledPNG_fileOverload_success_createsMissingParentDirsAndScaledFile(@TempDir Path tempDir) throws Exception {
        File originalFile = imageFile("default-profile.png");
        File scaledFile = tempDir.resolve("nested").resolve("dir").resolve("scaled.png").toFile();
        assertFalse(scaledFile.getParentFile().exists());

        Dimensions<Integer> max = Dimensions.getInstanceInPixels(100, 100);
        ImageUtil.ImageCreationResult result = ImageUtil.createScaledPNG(originalFile, scaledFile, max);

        assertEquals(ImageUtil.ImageCreationResultStatus.SUCCESS, result.getStatus());
        assertTrue(scaledFile.getParentFile().exists());
        assertTrue(scaledFile.exists());
        assertNotNull(result.dimensions());
    }

    @Test
    public void createScaledPNG_fileOverload_alreadyExistsWithGarbageContent_returnsInvalidDimensions(@TempDir Path tempDir) throws Exception {
        File scaledFile = tempDir.resolve("already-exists-garbage.png").toFile();
        Files.writeString(scaledFile.toPath(), "not a real png");
        // This branch returns before the original file is ever read, so it doesn't need to exist.
        File originalFile = tempDir.resolve("irrelevant-original.png").toFile();

        ImageUtil.ImageCreationResult result = ImageUtil.createScaledPNG(originalFile, scaledFile, null);

        assertEquals(ImageUtil.ImageCreationResultStatus.ALREADY_EXISTS, result.getStatus());
        assertNull(result.error());
        assertEquals(Dimensions.getInvalidInstanceInPixels(), result.dimensions());
    }

    @Test
    public void createScaledPNG_fileOverload_alreadyExistsWithValidImageContent_returnsActualDimensions(@TempDir Path tempDir) throws Exception {
        File scaledFile = tempDir.resolve("already-exists-valid.png").toFile();
        try (InputStream in = imageFileStream("default-profile.png")) {
            Files.copy(in, scaledFile.toPath());
        }
        File originalFile = tempDir.resolve("irrelevant-original.png").toFile();

        ImageUtil.ImageCreationResult result = ImageUtil.createScaledPNG(originalFile, scaledFile, null);

        assertEquals(ImageUtil.ImageCreationResultStatus.ALREADY_EXISTS, result.getStatus());
        assertEquals(Dimensions.getInstanceInPixels(300, 299), result.dimensions());
        // calling dimensions() again exercises the memoized branch
        assertEquals(Dimensions.getInstanceInPixels(300, 299), result.dimensions());
    }

    @Test
    public void createScaledPNG_fileOverload_missingOriginalFile_returnsFailure(@TempDir Path tempDir) {
        File originalFile = tempDir.resolve("does-not-exist.png").toFile();
        File scaledFile = tempDir.resolve("scaled-output.png").toFile();

        ImageUtil.ImageCreationResult result = ImageUtil.createScaledPNG(originalFile, scaledFile, null);

        assertEquals(ImageUtil.ImageCreationResultStatus.FAILURE, result.getStatus());
        assertNotNull(result.error());
        assertEquals(Dimensions.getInvalidInstanceInPixels(), result.dimensions());
    }

    // ---------------------------------------------------------------------------
    // getDimensionsForImageFile / getDimensionsSupplier
    // ---------------------------------------------------------------------------

    @Test
    public void getDimensionsForImageFile_directory_returnsInvalidDimensions(@TempDir Path tempDir) {
        LocalFileResource dirResource = LocalFileResource.createInstance(tempDir.toFile());
        Dimensions<Integer> result = ImageUtil.getDimensionsForImageFile(dirResource, null);
        assertEquals(Dimensions.getInvalidInstanceInPixels(), result);
    }

    @Test
    public void getDimensionsForImageFile_imageFile_returnsActualDimensions() throws Exception {
        File pngFile = imageFile("default-profile.png");
        LocalFileResource fileResource = LocalFileResource.createInstance(pngFile);
        Dimensions<Integer> result = ImageUtil.getDimensionsForImageFile(fileResource, null);
        assertEquals(Dimensions.getInstanceInPixels(300, 299), result);
    }

    @Test
    public void getDimensionsSupplier_directory_returnsSupplierYieldingInvalidDimensions(@TempDir Path tempDir) {
        LocalFileResource dirResource = LocalFileResource.createInstance(tempDir.toFile());
        Supplier<Dimensions<Integer>> supplier = ImageUtil.getDimensionsSupplier(dirResource, null);
        assertEquals(Dimensions.getInvalidInstanceInPixels(), supplier.get());
        // a second call exercises the memoized supplier without re-deriving the value
        assertEquals(Dimensions.getInvalidInstanceInPixels(), supplier.get());
    }

    @Test
    public void getDimensionsSupplier_imageFile_returnsSupplierYieldingActualDimensions() throws Exception {
        File pngFile = imageFile("default-profile.png");
        LocalFileResource fileResource = LocalFileResource.createInstance(pngFile);
        Supplier<Dimensions<Integer>> supplier = ImageUtil.getDimensionsSupplier(fileResource, null);
        assertEquals(Dimensions.getInstanceInPixels(300, 299), supplier.get());
    }

}
