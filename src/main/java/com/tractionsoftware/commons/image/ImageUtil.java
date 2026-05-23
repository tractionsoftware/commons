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

import com.google.common.base.Suppliers;
import com.google.common.collect.ImmutableSet;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import com.tractionsoftware.commons.html.HtmlUtil;
import com.tractionsoftware.commons.io.FileResource;
import com.tractionsoftware.commons.lang.NativeTypeConversion;
import com.tractionsoftware.commons.io.FileUtil;
import com.tractionsoftware.commons.util.Dimensions;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.metadata.IIOMetadataNode;
import javax.imageio.stream.ImageInputStream;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.Iterator;
import java.util.Set;
import java.util.function.Supplier;

/**
 * Helper methods for images, including determining dimensions and generating scaled versions.
 *
 * @author Andy Keller, Dave Shepperton
 */
public final class ImageUtil {

    private ImageUtil() {
    }

    private static final Logger LOGGER = LoggerFactory.getLogger(ImageUtil.class);

    /**
     * The set of image file name extensions that are generally supported on the web and by TeamPage: jpg (and jpeg),
     * png, and gif. This doesn't mean that other images won't be supported, but only that TeamPage will generally only
     * use or offer JPEG, PNG and GIF formatted images where web-facing resources are involved.
     */
    public static final Set<String> SUPPORTED_IMAGE_FILE_EXTENSIONS = ImmutableSet.of("jpg", "jpeg", "png", "gif");

    /**
     * A {@link FileFilter} that only matches files that have one of the {@link #SUPPORTED_IMAGE_FILE_EXTENSIONS}.
     */
    public static final FileFilter SUPPORTED_IMAGE_FILES_FILE_FILTER =
        FileUtil.getFileFilterByExtension(SUPPORTED_IMAGE_FILE_EXTENSIONS);

    /**
     * Represents the status of an attempt to create an image.
     */
    public static enum ImageCreationResultStatus {

        /**
         * The operation succeeded.
         */
        SUCCESS,

        /**
         * The operation was aborted because the target resource already exists.
         */
        ALREADY_EXISTS,

        /**
         * The operation failed.
         */
        FAILURE

    }

    /**
     * Represents the result of an attempt to create an image.
     */
    public static interface ImageCreationResult {

        /**
         * Returns the {@link ImageCreationResultStatus} representing the status of the requested image creation
         * operation.
         *
         * @return the {@link ImageCreationResultStatus} representing the status of the requested image creation
         *     operation.
         */
        public ImageCreationResultStatus getStatus();

        /**
         * Returns a {@link Dimensions.Units#PIXELS}-denominated {@link Dimensions} if the image was successfully
         * created.
         *
         * @return a {@link Dimensions.Units#PIXELS}-denominated {@link Dimensions} if the image was successfully
         *     created; {@link Dimensions#getInvalidInstanceInPixels() an invalid Dimensions object} otherwise.
         */
        public Dimensions<Integer> dimensions();

        /**
         * Returns true if the operation failed. This should be the same as checking either whether {@link #getStatus()}
         * return {@link ImageCreationResultStatus#FAILURE}.
         *
         * @return true if the operation failed; false otherwise.
         */
        public default boolean hadFailure() {
            if (getStatus() == ImageCreationResultStatus.FAILURE) {
                return false;
            }
            return true;
        }

        /**
         * Returns the Exception representing the error that caused the requested operation to fail, if any. This will
         * always return null unless {@link #getStatus()} returns {@link ImageCreationResultStatus#FAILURE}.
         *
         * @return the Exception representing the error that caused the requested operation to fail, if any; null
         *     otherwise.
         */
        public Exception error();

    }

    /**
     * Represents a result for a successful image creation attempt.
     */
    private record SuccessfulImageCreationResult(Dimensions<Integer> dimensions) implements ImageCreationResult {

        @Override
        public final ImageCreationResultStatus getStatus() {
            return ImageCreationResultStatus.SUCCESS;
        }

        @Override
        public final Exception error() {
            return null;
        }

    }

    /**
     * Represents the result for an image creation attempt that was aborted because the target resource seemed to
     * already exist. Its {@link #dimensions()} method still provides access to the existing resource's dimensions in
     * case they're still required.
     */
    private static final class AlreadyExistedImageCreationResult implements ImageCreationResult {

        private final Supplier<InputStream> getInputStream;

        private Dimensions<Integer> dimensions = null;

        private AlreadyExistedImageCreationResult(Supplier<InputStream> getInputStream) {
            this.getInputStream = getInputStream;
        }

        @Override
        public final ImageCreationResultStatus getStatus() {
            return ImageCreationResultStatus.ALREADY_EXISTS;
        }

        @Override
        public final Dimensions<Integer> dimensions() {
            if (dimensions == null) {
                try (InputStream input = getInputStream.get()) {
                    dimensions = ImageUtil.getDimensions(input);
                }
                catch (Exception e) {
                    dimensions = Dimensions.getInvalidInstanceInPixels();
                }
            }
            return dimensions;
        }

        @Override
        public final Exception error() {
            return null;
        }

    }

    /**
     * Represents a result for a failed image creation attempt.
     */
    private record FailedImageCreationResult(Exception error) implements ImageCreationResult {

        @Override
        public final ImageCreationResultStatus getStatus() {
            return ImageCreationResultStatus.FAILURE;
        }

        @Override
        public final Dimensions<Integer> dimensions() {
            return Dimensions.getInvalidInstanceInPixels();
        }

    }

    /**
     * Returns true if a file with the given extension or mime type identifier will be considered an image by the image
     * format support in this JVM {@link ImageIO}.
     */
    public static final boolean isImage(String fileExtension, String mimeType) {
        if (isImageExtension(fileExtension) || isImageMimeType(mimeType)) {
            return true;
        }
        return false;
    }

    /**
     * Returns true if a file with the given extension will be considered an image by the image format support in this
     * JVM (via {@link ImageIO}).
     */
    public static final boolean isImageExtension(String fileExtension) {
        if (StringUtils.isBlank(fileExtension)) {
            return false;
        }
        try {
            if (ImageIO.getImageReadersBySuffix(fileExtension).hasNext()) {
                return true;
            }
            if ("webp".equalsIgnoreCase(fileExtension) ||
                "heic".equalsIgnoreCase(fileExtension) ||
                "heif".equalsIgnoreCase(fileExtension)) {
                return true;
            }
            return false;
        }
        catch (Exception e) {
            LOGGER.warn("Unable to find a decoder for file extension '" + fileExtension + "'", e);
        }
        return false;
    }

    /**
     * Returns true if a file with the given mime type or content type will be considered an image by the image format
     * support in this JVM (via {@link ImageIO}).
     */
    public static final boolean isImageMimeType(String mimeType) {
        if (StringUtils.isBlank(mimeType)) {
            return false;
        }
        try {
            if (ImageIO.getImageReadersByMIMEType(mimeType).hasNext()) {
                return true;
            }
            if ("image/webp".equalsIgnoreCase(mimeType) ||
                "image/heic".equalsIgnoreCase(mimeType) ||
                "image/heif".equalsIgnoreCase(mimeType)) {
                return true;
            }
            return false;
        }
        catch (Exception e) {
            LOGGER.warn("Unable to find a decoder for mime/content type '" + mimeType + "'", e);
        }
        return false;
    }

    /**
     * Returns the {@link Dimensions.Units#PIXELS}-denominated {@link Dimensions} representing the size of the image
     * from the given {@link InputStream}.
     *
     * @param input
     *     from which the image data be read.
     * @return the {@link Dimensions.Units#PIXELS}-denominated {@link Dimensions} representing the size of the image
     *     from the given {@link InputStream}, if a decoder can be found for the image format, and the dimensions can be
     *     determined successfully.
     * @throws IOException
     *     if one is raised while attempting to use the given {@link InputStream}, or if no decoder
     */
    public static final Dimensions<Integer> getDimensions(InputStream input) throws IOException {

        try (ImageInputStream iis = ImageIO.createImageInputStream(input)) {

            Iterator<ImageReader> decoders = ImageIO.getImageReaders(iis);

            if (decoders.hasNext()) {

                ImageReader r = decoders.next();
                r.setInput(iis);
                int index = r.getMinIndex();
                int width = r.getWidth(index);
                int height = r.getHeight(index);
                int[] dimensions = new int[] { width, height };
                // special handling for retina PNG images
                IIOMetadata imageMetadata = r.getImageMetadata(0);
                if ("png".equals(r.getFormatName())) {
                    handleHiDpiPngDimensions(dimensions, imageMetadata);
                }
                return Dimensions.getInstanceInPixels(dimensions[0], dimensions[1]);

            }

            throw new IOException("Unsupported format: Failed to find an image decoder for " + input);

        }

    }

    private static final void handleHiDpiPngDimensions(int[] dimensions, IIOMetadata imageMetadata) {

        Node node = imageMetadata.getAsTree(imageMetadata.getNativeMetadataFormatName());
        NodeList children = node.getChildNodes();
        int len = children.getLength();

        for (int i = 0; i < len; i++) {
            Node child = children.item(i);
            if (!(child instanceof IIOMetadataNode) || !"pHYs".equals(child.getNodeName())) {
                continue;
            }
            handleHiDpiPngDimensions(dimensions, (IIOMetadataNode) child);
            return;
        }

    }

    private static final void handleHiDpiPngDimensions(int[] dimensions, IIOMetadataNode pHYsNode) {

        // To understand how HiDPI PNGs are encoded, I used to following reference.
        //
        // http://www.libpng.org/pub/png/spec/1.2/PNG-Chunks.html
        //
        // convert numbers to inches and check > 140
        //
        // [andy]

        if (!"meter".equals(pHYsNode.getAttribute("unitSpecifier"))) {
            return;
        }

        int pHYs_pixelsPerUnitXAxis = NativeTypeConversion.stringToInt(pHYsNode.getAttribute("pixelsPerUnitXAxis"), -1);
        int pHYs_pixelsPerUnitYAxis = NativeTypeConversion.stringToInt(pHYsNode.getAttribute("pixelsPerUnitYAxis"), -1);

        float dpiX = pHYs_pixelsPerUnitXAxis * 0.0254f;
        float dpiY = pHYs_pixelsPerUnitYAxis * 0.0254f;

        if (dpiX > 140 && dpiY > 140) {
            dimensions[0] /= 2;
            dimensions[1] /= 2;
        }

    }

    /**
     * Attempts to determine the appropriate file extension that would be used for the image that can be read from the
     * given {@link InputStream}. Specifically, if the data from the InputStream seem to represent a supported image
     * format, the value returned will be the result of invoking {@link ImageReader#getFormatName()} on an
     * {@link ImageReader} from {@link ImageIO#getImageReaders(Object)} (as long as it returns a non-blank name).
     *
     * @param input
     *     an {@link InputStream} from which an image can be read.
     * @return the appropriate file extension that would be used for the image that can be read from the given
     *     {@link InputStream}, if the data represent a valid image that uses a supported format; null otherwise.
     */
    public static final String getImageFileExtensionFromContents(InputStream input) {
        try (ImageInputStream iis = ImageIO.createImageInputStream(input)) {
            Iterator<ImageReader> decoders = ImageIO.getImageReaders(iis);
            if (decoders.hasNext()) {
                ImageReader r = decoders.next();
                r.setInput(iis);
                String formatName = r.getFormatName();
                if (StringUtils.isNotBlank(formatName)) {
                    return formatName;
                }
            }
        }
        catch (IOException e) {
            LOGGER.warn("Unexpected failure reading image data from input " + input, e);
        }
        return null;
    }

    /**
     * Determines the {@link Dimensions} representing the largest width and height no larger than the given original
     * Dimensions, and proportional to the original Dimensions, but still fitting within the given maximum dimensions.
     *
     * @param original
     *     the {@link Dimensions} representing the original dimensions of an image.
     * @param max
     *     the maximum dimensions for the proportional scaling, if any are required. If the argument for this parameter
     *     is null, the original {@link Dimensions} will be returned as-is.
     * @return the {@link Dimensions} representing the largest width and height no larger than the given original
     *     Dimensions, and proportional to the original Dimensions, but still fitting within the given maximum
     *     dimensions, if maximum dimensions were requested; otherwise, the original {@link Dimensions} as-is.
     */
    public static final Dimensions<Integer> getScaledDimensions(Dimensions<Integer> original, Dimensions<Integer> max) {

        if (original == null || !original.hasAtLeastOneValidDimension()) {
            return null;
        }
        if (max == null) {
            return original;
        }

        // Start at 100%
        int sw = original.getWidth();
        int sh = original.getHeight();

        double fw = scalefactor(sw, max.getWidth());
        double fh = scalefactor(sh, max.getHeight());

        // take the smaller
        double f = Math.min(fw, fh);

        if (f < 1) {  // don't make images bigger
            sw = (int) (sw * f);
            sh = (int) (sh * f);
            return Dimensions.getInstanceInPixels(sw, sh);
        }

        return original;

    }

    private static final double scalefactor(double l, double max) {
        return (max == -1 || l == 0) ? 1.0 : (max / l);
    }

    /**
     * Attempts to create a new PNG formatted image representing a scaled version of the image in the given original
     * image {@link File}, constrained by the given maximum {@link Dimensions}, and storing the result in the given
     * destination scaled image File.
     *
     * @param originalImageFile
     *     the {@link File} containing the original image file.
     * @param scaledImageFile
     *     the destination {@link File} for the newly created scaled PNG formatted version of the original image.
     * @param maxDimensions
     *     the {@link Dimensions}, if any, representing the dimensions for the proportional scaling of the original
     *     image, if any are required. If the argument for this parameter is null, the new image's dimensions will be
     *     the same as those of the original. See {@link #getScaledDimensions(Dimensions, Dimensions)}.
     * @return an {@link ImageCreationResult} representing the status of the attempt.
     */
    public static final ImageCreationResult createScaledPNG(File originalImageFile, File scaledImageFile, Dimensions<Integer> maxDimensions) {

        // It's up to us to make sure the directory structure and
        // destination file exist before we try to start using it.
        File scaledImageFileDir = scaledImageFile.getParentFile();

        if (!scaledImageFileDir.exists()) {
            if (scaledImageFileDir.mkdirs()) {
                LOGGER.debug("Created directory for scaled PNG {}", scaledImageFileDir);
            }
        }

        boolean createdFile;
        try {
            createdFile = scaledImageFile.createNewFile();
        }
        catch (Exception e) {
            LOGGER.error("Unable to create the file '" + scaledImageFile, e);
            return new FailedImageCreationResult(e);
        }

        if (!createdFile) {
            LOGGER.warn("The file {} already exists.", scaledImageFile);
            return new AlreadyExistedImageCreationResult(() -> {
                try {
                    return FileUtil.getBufferedInputStream(scaledImageFile);
                }
                catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });
        }

        try (InputStream originalImageStream = FileUtil.getBufferedInputStream(originalImageFile);
             OutputStream scaledImageStream = FileUtil.getBufferedOutputStream(scaledImageFile)) {
            return createScaledPNG(originalImageStream, scaledImageStream, maxDimensions);
        }
        catch (Exception e) {
            LOGGER.error("Unable to create a scaled PNG from the image file " + originalImageFile + ".", e);
            return new FailedImageCreationResult(e);
        }

    }

    /**
     * Attempts to create a new PNG formatted image representing a scaled version of the image read from the given image
     * {@link InputStream}, constrained by the given maximum {@link Dimensions}, and writing the result to the given
     * {@link OutputStream}.
     *
     * @param originalImageInput
     *     the {@link InputStream} from which the original image can be read.
     * @param scaledImageOutput
     *     the {@link OutputStream} to which the for the newly created scaled PNG formatted version of the original
     *     image will be written.
     * @param maxDimensions
     *     the {@link Dimensions}, if any, representing the dimensions for the proportional scaling of the original
     *     image, if any are required. If the argument for this parameter is null, the new image's dimensions will be
     *     the same as those of the original. See {@link #getScaledDimensions(Dimensions, Dimensions)}.
     * @return an {@link ImageCreationResult} representing the status of the attempt.
     */
    public static final ImageCreationResult createScaledPNG(InputStream originalImageInput, OutputStream scaledImageOutput, Dimensions<Integer> maxDimensions) {

        try {

            BufferedImage originalImage = ImageIO.read(originalImageInput);

            originalImage = cropToSquare(originalImage);

            Dimensions<Integer> originalDimensions =
                Dimensions.getInstanceInPixels(originalImage.getWidth(), originalImage.getHeight());

            // We use ImageIcon as a hack to make sure the image is completely loaded; if it isn't, the drawImage
            // operation will fail.
            loadImageAsIcon(originalImage);
            ImageIcon scaledImageIcon;

            // If the image is already small enough, we don't need
            // to scale it.
            Dimensions<Integer> scaledDimensions = getScaledDimensions(originalDimensions, maxDimensions);
            if (scaledDimensions != null) {
                scaledImageIcon = new ImageIcon(
                    originalImage.getScaledInstance(
                        scaledDimensions.getWidth(), scaledDimensions.getHeight(), Image.SCALE_SMOOTH
                    )
                );
            }
            // -1 -1 uses the original dimensions.
            else {
                scaledImageIcon = new ImageIcon(originalImage.getScaledInstance(-1, -1, Image.SCALE_SMOOTH));
            }

            int finalWidth = scaledImageIcon.getIconWidth();
            int finalHeight = scaledImageIcon.getIconHeight();
            BufferedImage renderedImage = new BufferedImage(finalWidth, finalHeight, BufferedImage.TYPE_INT_ARGB);
            Graphics g = renderedImage.getGraphics();
            g.drawImage(scaledImageIcon.getImage(), 0, 0, null);
            // JavaDoc says this is a good idea, even though gc
            // will do it automatically.
            g.dispose();
            ImageIO.write(renderedImage, "png", scaledImageOutput);
            scaledImageOutput.flush();
            return new SuccessfulImageCreationResult(Dimensions.getInstanceInPixels(finalWidth, finalHeight));

        }
        catch (IOException | RuntimeException e) {
            LOGGER.error("A problem was encountered while attempting to create a scaled PNG image", e);
            return new FailedImageCreationResult(e);
        }

    }

    /**
     * Gets a {@link BufferedImage} corresponding to a cropped square version of the given BufferedImage.
     *
     * @param img
     *     the {@link BufferedImage} representing the existing image data.
     * @return a {@link BufferedImage} corresponding to a cropped square version of the given BufferedImage.
     */
    public static final BufferedImage cropToSquare(BufferedImage img) {

        // original width and height
        int ow = img.getWidth();
        int oh = img.getHeight();

        // new width and height
        int w = ow;
        int h = oh;

        // delta/offset where we should start crop
        int dw = 0;
        int dh = 0;

        if (ow < oh) {
            h = w;
            dh = (oh - h) / 2;
        }
        else {
            w = h;
            dw = (ow - w) / w;
        }

        BufferedImage cropped = img.getSubimage(dw, dh, w, h);

        return cropped != null ? cropped : img;
    }

    public static final String getImgWidthAttributeHtml(int width) {
        if (width < 0) {
            return "";
        }
        return HtmlUtil.getTagAttribute(HtmlUtil.ATTRIBUTE_NAME_WIDTH, String.valueOf(width));
    }

    public static final String getImgHeightAttributeHtml(int height) {
        if (height < 0) {
            return "";
        }
        return HtmlUtil.getTagAttribute(HtmlUtil.ATTRIBUTE_NAME_HEIGHT, String.valueOf(height));
    }

    public static final Dimensions<Integer> getDimensionsForImageFile(FileResource file, Dimensions<Integer> maximumDimensions) {
        if (file.isDirectory()) {
            return Dimensions.getInvalidInstanceInPixels();
        }
        return Dimensions.getImageDimensionsInPixels(file, maximumDimensions);
    }

    public static final Supplier<Dimensions<Integer>> getDimensionsSupplier(final FileResource file, final Dimensions<Integer> maxDimensions) {
        return Suppliers.memoize(() -> ImageUtil.getDimensionsForImageFile(file, maxDimensions));
    }

    @CanIgnoreReturnValue
    private static final ImageIcon loadImageAsIcon(Image image) {
        return new ImageIcon(image);
    }

}
