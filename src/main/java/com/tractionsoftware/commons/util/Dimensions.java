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

package com.tractionsoftware.commons.util;

import com.tractionsoftware.commons.image.HEIFUtil;
import com.tractionsoftware.commons.image.IconFileResource;
import com.tractionsoftware.commons.image.ImageUtil;
import com.tractionsoftware.commons.image.WebPUtil;
import com.tractionsoftware.commons.io.FileResource;
import com.tractionsoftware.commons.io.FileNameUtil;
import com.tractionsoftware.commons.lang.NativeTypeConversion;
import com.tractionsoftware.commons.lang.ObjectUtil;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A simple and immutable encapsulation of the dimensions of an object that has layout properties (generally an image or
 * a paper page).
 *
 * @param <N>
 *     the type of Number class used for the width and height values.
 * @author Dave Shepperton
 */
public final class Dimensions<N extends Number> {

    private static final Logger LOGGER = LoggerFactory.getLogger(Dimensions.class);

    private static final Dimensions<Integer> INVALID_INSTANCE_PIXELS =
        getInstance(-1, -1, Units.PIXELS);

    /**
     * Represents the pattern TeamPage uses in view configuration files to represent maximum image dimensions allowed to
     * be used in the view.
     */
    private static final Pattern MAX_IMAGE_DIMENSIONS_PATTERN =
        Pattern.compile("(-?[1-9][0-9]*)x(-?[1-9][0-9]*)");

    /**
     * Enumerates the most useful possible units for measuring the dimensions of an image or other entity with a
     * rectangular shape.
     *
     * @author Dave Shepperton
     */
    public static enum Units {

        PIXELS("px"),

        INCHES("in"),

        CENTIMETERS("cm");

        public static final Units fromNameOrAbbreviation(String abbreviationOrName, Units defaultValue) {
            if (StringUtils.isBlank(abbreviationOrName)) {
                return defaultValue;
            }
            for (Units u : values()) {
                if (u.name().equalsIgnoreCase(abbreviationOrName) ||
                    u.abbreviation.equalsIgnoreCase(abbreviationOrName)) {
                    return u;
                }
            }
            return defaultValue;
        }

        private final String abbreviation;

        private Units(String abbreviation) {
            this.abbreviation = abbreviation;
        }

        public final String getAbbreviation() {
            return abbreviation;
        }

    }

    /**
     * Returns a Dimensions instance with the given width, height and {@link Units}.
     *
     * @param <N>
     *     the type of numeric value.
     * @param width
     *     the requested width, which must not be null.
     * @param height
     *     the requested height, which must not be null.
     * @param units
     *     the requested {@link Units}, which must not be null.
     * @return a Dimensions instance with the given width, height and {@link Units}.
     */
    public static final <N extends Number> Dimensions<N> getInstance(N width, N height, Units units) {
        Objects.requireNonNull(width, "width");
        Objects.requireNonNull(height, "height");
        Objects.requireNonNull(units, "units");
        return new Dimensions<>(width, height, units);
    }

    /**
     * Parses a String containing a specification for the maximum desired image dimensions. It handles values in the
     * standard maximum image dimension specification format: "[width]x[height]", with a value of "-1" representing "no
     * maximum", in keeping with the standard interpretation of such values.
     *
     * @param maxImageDimensionsSpec
     *     a String containing a standard maximum image dimensions specification.
     * @return the {@link Dimensions.Units#PIXELS}-denominated {@link Dimensions} derived from parsing the given String;
     *     or null if it is not in the supported format.
     */
    public static final Dimensions<Integer> parseMaxImageDimensions(String maxImageDimensionsSpec) {

        if (StringUtils.isBlank(maxImageDimensionsSpec)) {
            return null;
        }

        Matcher m = MAX_IMAGE_DIMENSIONS_PATTERN.matcher(maxImageDimensionsSpec);
        if (m.matches()) {
            return getInstanceInPixels(
                NativeTypeConversion.stringToInt(m.group(1).trim(), -1),
                NativeTypeConversion.stringToInt(m.group(2).trim(), -1)
            );
        }

        return null;

    }

    /**
     * Returns a Dimensions instance denominated in {@link Units#PIXELS} representing the requested width and height. No
     * validation is performed on the width and height.
     *
     * @param width
     *     the requested width.
     * @param height
     *     the requested height.
     * @return a Dimensions instance denominated in {@link Units#PIXELS} representing the requested width and height.
     */
    public static final Dimensions<Integer> getInstanceInPixels(int width, int height) {
        return getInstance(width, height, Units.PIXELS);
    }

    /**
     * Attempts to determine the dimensions of the image read from the given {@link InputStream}, and returns a
     * Dimensions denominated in {@link Units#PIXELS} representing those dimensions.
     *
     * @param fileName
     *     an informational file name or path for the source data, if that's available for the resource, which will only
     *     be used for diagnostic logging if the image dimensions cannot be determined.
     * @param input
     *     the {@link InputStream} from which the image whose dimensions are to be determined can be read.
     * @return a Dimensions denominated in {@link Units#PIXELS} representing the dimensions of the image read from the
     *     given {@link InputStream}, if they can be determined;
     *     {@link #getInvalidInstanceInPixels() an invalid instance} otherwise.
     * @see com.tractionsoftware.commons.image.ImageUtil#getDimensions(InputStream)
     */
    public static final Dimensions<Integer> getInstanceInPixels(String fileName, InputStream input) {
        return getImageDimensionsInPixels(fileName, input, null);
    }

    /**
     * Attempts to determine the dimensions of the image read from the given {@link InputStream}, and returns a
     * Dimensions denominated in {@link Units#PIXELS} representing those dimensions.
     *
     * @param fileName
     *     an informational file name or path for the source data, if that's available for the resource, which will only
     *     be used for diagnostic logging if the image dimensions cannot be determined.
     * @param input
     *     the {@link InputStream} from which the image whose dimensions are to be determined can be read.
     * @param maxDimensions
     *     optional maximum dimensions in pixels. If a non-null value is specified, the result will be proportionally
     *     scaled to fit into these dimensions.
     * @return a Dimensions denominated in {@link Units#PIXELS} representing the dimensions of the image read from the
     *     given {@link InputStream}, if they can be determined;
     *     {@link #getInvalidInstanceInPixels() an invalid instance} otherwise.
     * @see ImageUtil#getDimensions(InputStream)
     * @see ImageUtil#getScaledDimensions(Dimensions, Dimensions)
     */
    public static final Dimensions<Integer> getImageDimensionsInPixels(String fileName, InputStream input, Dimensions<Integer> maxDimensions) {
        String ext = FileNameUtil.getExtension(fileName, null);
        try {
            if ("webp".equalsIgnoreCase(ext)) {
                return ImageUtil.getScaledDimensions(WebPUtil.getDimensions(input), maxDimensions);
            }
            if ("heic".equalsIgnoreCase(ext) ||
                "heif".equalsIgnoreCase(ext)) {
                return ImageUtil.getScaledDimensions(HEIFUtil.getDimensions(input), maxDimensions);
            }
            return ImageUtil.getScaledDimensions(ImageUtil.getDimensions(input), maxDimensions);
        }
        catch (IOException e) {
            if (ImageUtil.isImageExtension(ext)) {
                LOGGER.warn(
                    "Failed to read dimensions for {} ({})", fileName, ObjectUtil.safeToStringObject(input), e
                );
            }
            return getInvalidInstanceInPixels();
        }
    }

    /**
     * Attempts to determine the dimensions of the image contained in the given {@link FileResource}, and returns a
     * Dimensions denominated in {@link Units#PIXELS} representing those dimensions.
     *
     * @param fileResource
     *     the {@link FileResource} representing the image dimensions are to be determined.
     * @return a Dimensions denominated in {@link Units#PIXELS} representing the dimensions of the given image, if they
     *     can be determined; {@link #getInvalidInstanceInPixels() an invalid instance} otherwise.
     * @see ImageUtil#getDimensions(InputStream)
     */
    public static final Dimensions<Integer> getImageDimensionsInPixels(FileResource fileResource) {
        return getImageDimensionsInPixels(fileResource, null);
    }

    /**
     * Attempts to determine the dimensions of the image contained in the given {@link FileResource}, and returns a
     * Dimensions denominated in {@link Units#PIXELS} representing those dimensions, proportionally scaled if necessary
     * to fit within the given maximum dimensions.
     *
     * @param fileResource
     *     the {@link FileResource} representing the image dimensions are to be determined.
     * @param maxDimensions
     *     an optional Dimensions object representing the maximum width and height for the returned Dimensions. If the
     *     argument for this parameter is null, the image dimensions will be returned directly. Otherwise, they will be
     *     scaled proportionally to fit the given maximum width and height.
     * @return a Dimensions denominated in {@link Units#PIXELS} representing the dimensions of the given image, if they
     *     can be determined, proportionally scaled if necessary to fit within the given maximum dimensions;
     *     {@link #getInvalidInstanceInPixels() an invalid instance} otherwise.
     * @see ImageUtil#getDimensions(InputStream)
     * @see ImageUtil#getScaledDimensions(Dimensions, Dimensions)
     */
    public static final Dimensions<Integer> getImageDimensionsInPixels(FileResource fileResource, Dimensions<Integer> maxDimensions) {
        try (InputStream input = fileResource.getInputStream()) {
            return getImageDimensionsInPixels(fileResource.getFilename(), input, maxDimensions);
        }
        catch (IOException e) {
            LOGGER.warn("Failed to open InputStream to find dimensions of {}", fileResource, e);
            return getInvalidInstanceInPixels();
        }
    }

    /**
     * Attempts to determine the dimensions of the image contained in the given {@link IconFileResource}, and returns a
     * Dimensions denominated in {@link Units#PIXELS} representing those dimensions, proportionally scaled if necessary
     * to fit within the given maximum dimensions.
     *
     * @param iconFile
     *     the {@link IconFileResource} whose image dimensions are to be determined.
     * @param maxDimensions
     *     an optional Dimensions object representing the maximum width and height for the returned Dimensions. If the
     *     argument for this parameter is null, the image dimensions will be returned directly. Otherwise, they will be
     *     proportionally scaled to fit the given maximum width and height.
     * @return a Dimensions denominated in {@link Units#PIXELS} representing the dimensions of the given icon image, if
     *     they can be determined, proportionally scaled if necessary to fit within the given maximum dimensions;
     *     {@link #getInvalidInstanceInPixels() an invalid instance} otherwise.
     * @see ImageUtil#getDimensions(InputStream)
     * @see ImageUtil#getScaledDimensions(Dimensions, Dimensions)
     */
    public static final Dimensions<Integer> getInstanceInPixels(IconFileResource iconFile, Dimensions<Integer> maxDimensions) {
        try (InputStream imageFileStream = iconFile.getInputStream()) {
            return Dimensions.getImageDimensionsInPixels(iconFile.getFilename(), imageFileStream, maxDimensions);
        }
        catch (Exception e) {
            LOGGER.warn("Failed to open InputStream to find dimensions of the icon file {}", iconFile, e);
            return Dimensions.getInvalidInstanceInPixels();
        }
    }

    /**
     * Returns an instance denominated in {@link Units#PIXELS} that is "invalid" by convention, meaning that the
     * instance has -1 for both width and height.
     *
     * @return an instance that is "invalid" by convention, meaning that the instance has -1 for both width and height.
     */
    public static final Dimensions<Integer> getInvalidInstanceInPixels() {
        return INVALID_INSTANCE_PIXELS;
    }

    private final N width;

    private final N height;

    private final Units units;

    private Dimensions(N width, N height, Units units) {
        this.width = width;
        this.height = height;
        this.units = units;
    }

    public final N getWidth() {
        return width;
    }

    public final N getHeight() {
        return height;
    }

    public final Units getUnits() {
        return units;
    }

    public final double[] asDoubleArray() {
        return new double[] {
            width.doubleValue(),
            height.doubleValue()
        };
    }

    public final int[] asIntArray() {
        return new int[] {
            width.intValue(),
            height.intValue()
        };
    }

    @Override
    public final String toString() {
        return width +
               "x" +
               height +
               units.getAbbreviation();
    }

    @Override
    public final boolean equals(Object other) {
        if (!(other instanceof Dimensions<?> otherDim)) {
            return false;
        }
        if (units.equals(otherDim.units) &&
            width.equals(otherDim.width) &&
            height.equals(otherDim.height)) {
            return true;
        }
        return false;
    }

    @Override
    public final int hashCode() {
        return Objects.hash(units, width, height);
    }

    public final boolean hasAtLeastOneValidDimension() {
        if (width.doubleValue() >= 0d || height.doubleValue() >= 0d) {
            return true;
        }
        return false;
    }

    /**
     * Returns true if this {@link Dimensions} fits within the other given {@link Dimensions}.
     *
     * @param otherDimensions
     *     the other {@link Dimensions} to test.
     * @return true if this {@link Dimensions} fits within the other given {@link Dimensions}.
     * @throws UnsupportedOperationException
     *     if the other {@link Dimensions} object is not denominated in the same {@link Units} as this instance.
     */
    public final boolean fitsWithin(Dimensions<?> otherDimensions) {

        if (otherDimensions == null) {
            // I guess?
            return true;
        }

        if (!units.equals(otherDimensions.getUnits())) {
            throw new UnsupportedOperationException(units + " vs. " + otherDimensions.getUnits());
        }

        double otherWidth = otherDimensions.getWidth().doubleValue();
        if (otherWidth > 0d && otherWidth < getWidth().doubleValue()) {
            // This Dimensions object is too wide to fit within the
            // other.
            return false;
        }

        double otherHeight = otherDimensions.getHeight().doubleValue();
        if (otherHeight > 0d && otherHeight < getHeight().doubleValue()) {
            // This Dimensions object is too tall to fit within the
            // other.
            return false;
        }

        return true;

    }

}
