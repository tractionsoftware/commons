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

import com.tractionsoftware.commons.io.CommonFileResourceType;
import com.tractionsoftware.commons.io.FileResourceType;
import com.tractionsoftware.commons.util.Dimensions;

/**
 * An Icon represents an image with particular display properties.
 */
public interface Icon {

    /**
     * Returns true if this Icon is "valid." The definition of validity as it applies here requires the icon image
     * resource to be available, and to really represent an image.
     *
     * @return The definition of validity as it applies here requires the icon image resource to be available, and to
     *     really represent an image.
     */
    public boolean isValid();

    /**
     * Returns a file name for this icon image.
     *
     * @return a file name for this icon image.
     */
    public String getFilename();

    /**
     * Returns a {@link Dimensions.Units#PIXELS}-denominated {@link Dimensions} object representing the dimensions that
     * should be used to display this icon image, if the dimensions are known or can be determined (which should be the
     * case if {@link #isValid() this Icon is valid}). These may or may not be the same as the image's intrinsic
     * dimensions.
     *
     * @return a {@link Dimensions.Units#PIXELS}-denominated {@link Dimensions} object representing the dimensions that
     *     should be used to display this icon image, if the dimensions are known or can be determined;
     *     {@link Dimensions#getInvalidInstanceInPixels() an invalid Dimensions instance otherwise}, with width and
     *     height both set to -1.
     */
    public Dimensions<Integer> getDimensions();

    /**
     * Returns the value representing the pixel width that should be used to display this icon image, if the dimensions
     * are known or can be determined (which should be the case if {@link #isValid() this Icon is valid}). This width
     * may or may not be the same as the image's intrinsic width.
     *
     * <p>
     * This method will always be implemented to be the same as invoking {@link Dimensions#getWidth()} on the result of
     * {@link #getDimensions()}.
     *
     * @return the value representing the pixel width that should be used to display this icon image, if the dimensions
     *     are known or can be determined; -1 otherwise.
     */
    public default int getWidth() {
        return getDimensions().getWidth();
    }

    /**
     * Returns the value representing the pixel height that should be used to display this icon image, if the dimensions
     * are known or can be determined (which should be the case if {@link #isValid() this Icon is valid}). This height
     * may or may not be the same as the image's intrinsic height.
     *
     * <p>
     * This method will always be implemented to be the same as invoking {@link Dimensions#getHeight()} on the result of
     * {@link #getDimensions()}.
     *
     * @return the value representing the pixel height that should be used to display this icon image, if the dimensions
     *     are known or can be determined; -1 otherwise.
     */
    public default int getHeight() {
        return getDimensions().getHeight();
    }

    /**
     * Returns a Content-ID to use for this icon resource for at least the duration of the current request. Invocations
     * on other requests for a FileInfo object referring to the same underlying resource may return different values.
     *
     * @return a Content-ID to use for this file resource for at least the duration of the current request.
     */
    public abstract String getContentId();

    /**
     * Returns a data: URL that encodes the icon image. This can be appropriate when the image needs to be embedded
     * without referring to a URL that would require a separate request to retrieve.
     *
     * @return a data: URL that encodes the icon image.
     */
    public String getDataUrl();

    /**
     * Returns a String representing a width= HTML IMG tag attribute. For example,
     *
     * <pre>
     * width="10"
     * </pre>
     *
     * <p>
     * This can be useful for building HTML IMG elements.
     *
     * @return a String representing a width= HTML IMG tag attribute; or an empty string if the width is not known or
     *     otherwise not specified.
     */
    public default String getWidthHTML() {
        return ImageUtil.getImgWidthAttributeHtml(getWidth());
    }

    /**
     * Returns a String representing a height= HTML IMG tag attribute. For example,
     *
     * <pre>
     * height="10"
     * </pre>
     *
     * <p>
     * This can be useful for building HTML IMG elements.
     *
     * @return a String representing a width= HTML IMG tag attribute; or an empty string if the width is not known or
     *     otherwise not specified.
     */
    public default String getHeightHTML() {
        return ImageUtil.getImgHeightAttributeHtml(getHeight());
    }

    /**
     * Returns an Icon instance that represents a proportionally scaled resized version of this Icon which is as large
     * as possible while still fitting within the given {@link Dimensions.Units#PIXELS pixel}-denominated
     * {@link Dimensions}. If a more appropriately sized version of the same image is already available, that will be
     * used by the new Icon instance, but the size of the new Icon's image will not be expanded beyond the original size
     * of the largest available source image data.
     *
     * @param newMaxDimensions
     *     a {@link Dimensions.Units#PIXELS pixel}-denominated {@link Dimensions} representing the requested maximum
     *     dimensions. Passing null as the argument for this parameter is equivalent to requesting an Icon that has
     *     dimensions that are as large as possible (that is, no maximum).
     * @return an Icon instance that represents a proportionally scaled resized version of this Icon which is as large
     *     as possible while still fitting within the given {@link Dimensions.Units#PIXELS pixel}-denominated
     *     {@link Dimensions}.
     */
    public Icon getScaled(Dimensions<Integer> newMaxDimensions);

    /**
     * Returns an Icon instance that represents a version of this Icon  whose {@link #getDimensions()} will return the
     * given {@link Dimensions.Units#PIXELS pixel}-denominated {@link Dimensions}. This method is only intended to be
     * used on an Icon whose the underlying image's dimensions are not known. Such an Icon instance
     * {@link #isValid() will report that it is valid}, but its getDimensions() method will return null. This is most
     * likely to happen in the case of an Icon created from an
     * {@link CommonFileResourceType#EXTERNAL external resource}.
     *
     * <p>
     * Another case in which it might possibly be appropriate to use this method would be for some sort of place-holder
     * image that can be stretched without regard to proportion, such as an image that is merely a single pixel.
     *
     * <p>
     * For all other resources, there should be no reason to invoke this method, but all implementations must handle it
     * on a sort of best-efforts basis. At best, if a version of the underlying image resource that happens to exactly
     * match the requested dimensions is available, that will be used by the new Icon instance. Otherwise, clients
     * should expect that some modest effort <em>may</em> be made to identify and use an existing version with the
     * closest partially or otherwise approximately matching dimensions and use that for the new Icon instance, and that
     * even though getDimensions() will return the requested Dimensions, the image will not "natively" have those
     * dimensions. This means that when it is displayed in the context of an HTML document via an IMG tag with
     * corresponding width= and height= attributes, it will appear stretched or compressed in a non-proportional way. In
     * those cases, unless there's some rare case in which the client is willing to use a non-proportionally stretched
     * image, it is almost certainly preferable to choose one of these options: instead of trying to specify the
     * dimensions of a static resource, load the Icon and use the dimensions that TeamPage reports; create an
     * appropriately sized image and put it into a plug-in and reference that static Icon resource; or invoke
     * {@link #getScaled(Dimensions)} to retrieve an instance that will not necessarily have the exact requested
     * dimensions, but which will appear properly scaled.
     *
     * @param newDimensions
     *     a {@link Dimensions.Units#PIXELS pixel}-denominated {@link Dimensions} representing the requested dimensions
     *     for the new Icon instance. If null is passed as the argument for this parameter, the same Icon instance will
     *     be returned.
     * @return an Icon instance that represents a version of this Icon  whose {@link #getDimensions()} will return the
     *     given {@link Dimensions.Units#PIXELS pixel}-denominated {@link Dimensions}.
     */
    public Icon withDimensions(Dimensions<Integer> newDimensions);

    /**
     * Returns a {@link IconFileResource} that can be used to provide direct access to this Icon image data, if
     * possible. This can be useful if the image has to be attached to an email message, or in certain other cases.
     *
     * @return a {@link IconFileResource} that can be used to provide direct access to this Icon image data, if
     *     possible; null otherwise.
     */
    public IconFileResource getImageFileResource();

    /**
     * Returns the {@link FileResourceType} indicating the type of resource that this Icon instance represents. This
     * method should never return null. For miscellaneous images, or anything otherwise uncategorized,
     * {@link CommonFileResourceType#OTHER} should be returned.
     *
     * <p>
     * This implementation delegates to {@code getImageFileResource().getType()}. Subclasses should override it as
     * necessary.
     *
     * @return the {@link FileResourceType} indicating the type of image resource that this Icon instance represents.
     */
    public default FileResourceType getImageResourceType() {
        return getImageFileResource().getType();
    }

    public default String getTitle() {
        return null;
    }

    public default String getAltText() {
        return null;
    }

}
