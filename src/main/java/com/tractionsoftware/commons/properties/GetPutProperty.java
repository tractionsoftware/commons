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

package com.tractionsoftware.commons.properties;

import com.tractionsoftware.commons.lang.NativeTypeConversion;
import com.tractionsoftware.commons.text.StringSplitUtil;
import com.tractionsoftware.commons.text.TextTransformer;
import org.apache.commons.lang3.StringUtils;

import java.util.function.Function;

/**
 * A simple read-write interface for objects that support read-write access to name-value pairs of properties that can
 * managed as Strings. It is a hybrid of {@link GetProperty} and {@link PutProperty}.
 *
 * @author Andy Keller, Dave Shepperton
 */
public interface GetPutProperty extends GetProperty, PutProperty, PropertyCollection {

    /**
     * Appends the given value to the existing list value for the given named property. If there is no current value for
     * the given named property (or if its value is null), this should be equivalent to
     * {@link #putProperty(String, String)}. If the given value to append is null, no action should be taken.
     *
     * <p>
     * This default implementation uses putProperty to set a new value created by concatenating the existing value,
     * {@link StringSplitUtil#DEFAULT_STRING_LIST_SEPARATOR the default list separator String}, and the new value. It
     * handles null values gracefully. It should be adequate for any implementation that doesn't require any special
     * handling for certain list element values or special data structures to manage list values.
     *
     * @param name
     *     the named property to be treated as a list, and to which the given value should be appended.
     * @param value
     *     the value to be appended to the existing list property value.
     */
    public default void appendToListProperty(String name, String value) {
        if (value != null) {
            appendToProperty(name, value, StringSplitUtil.DEFAULT_STRING_LIST_SEPARATOR);
        }
    }

    /**
     * Appends the given value to the existing value for the given named property. If there is no current value for the
     * given named property (or if its value is null), this should be equivalent to
     * {@link #putProperty(String, String)}. If the given value to append is null, no action should be taken.
     *
     * <p>
     * This default implementation uses putProperty to set a new value created by concatenating the existing and new
     * values, handling null values gracefully. It should be adequate for most implementations.
     *
     * @param name
     *     the named property to which the given value should be appended.
     * @param value
     *     the value to be appended.
     * @param separator
     *     the separator to use, if any, when appending.
     */
    public default void appendToProperty(String name, String value, String separator) {

        if (value == null) {
            return;
        }

        String newValue;
        String existingValue = getProperty(name);
        if (existingValue == null) {
            newValue = value;
        }
        else {
            if (value.isEmpty()) {
                newValue = existingValue;
            }
            else if (StringUtils.isEmpty(separator)) {
                newValue = existingValue + value;
            }
            else {
                newValue = existingValue + separator + value;
            }
        }

        putProperty(name, newValue);

    }

    /**
     * Returns a view of this GetPutProperty that is guaranteed to supply access to the GetPutProperty API methods only,
     * or at least to methods that are deemed safe for general read-write access. For example, to pass a
     * {@link PropStore} to an alien method that accepts a GetPutProperty, and to ensure that that method cannot cast
     * the argument back to a GetPutProperty to gain commit access to the underlying store, a client may invoke
     * toReadWrite on that PropStore before passing it along to that method.
     *
     * <p>
     * This default implementation returns this GetPutProperty itself, which is a suitable implementation for subclasses
     * that do not have any public methods beyond those declared in GetPutProperty. Implementations that do have public
     * methods that should not be accessible to clients that arbitrary clients when a GetPutProperty would do should
     * override it, perhaps using {@link StaticForwardingGetPutProperty#wrap(GetPutProperty)}. Likewise, this method may
     * be re-declared in subclasses using a different return type if there is a more specific safe read-only subtype
     * that could be offered in place of GetPutProperty.
     *
     * @return a view of this GetPutProperty that is guaranteed to supply access to the GetPutProperty API methods only,
     *     or at least to methods that are deemed safe for general read-write access.
     */
    public default GetPutProperty toReadWrite() {
        return this;
    }

    /**
     * Returns a GetProperty that will fall back to the given defaults as necessary when "local" values or existing
     * defaults do not include a requested property.
     *
     * <p>
     * This implementation uses {@link GetPropertyLocator}, which should be adequate for most implementations. It also
     * gracefully handles a null argument by returning this GetProperty instance itself.
     */
    @Override
    public default GetPutProperty withDefaults(GetProperty defaults) {
        return GetPutPropertyLocator.getSingleGetPutPropertyOrLocator(this, defaults);
    }

    /**
     * This default implementation uses {@link CachingGetPutProperty#wrapWithDefaultCache(GetPutProperty)}. This should
     * be adequate for most implementations.
     */
    @Override
    public default GetPutProperty withCache() {
        return CachingGetPutProperty.wrapWithDefaultCache(this);
    }

    /**
     * This default implementation uses {@link CachingGetPutProperty#wrapWithCache(GetPutProperty, PropertyCache)}. This
     * should be adequate for most implementations.
     */
    @Override
    public default GetPutProperty withCache(PropertyCache cache) {
        return CachingGetPutProperty.wrapWithCache(this, cache);
    }

    /**
     * This default implementation uses {@link StaticForwardingGetProperty#wrap(GetProperty)} to wrap this
     * GetPutProperty instance, cutting off write access.
     */
    @Override
    public default GetProperty toReadOnly() {
        return StaticForwardingGetProperty.wrap(this);
    }

    /**
     * This default implementation uses {@link ForwardingPutProperty#wrap(PutProperty)} to wrap this GetPutProperty
     * instance, cutting off read access.
     */
    @Override
    public default PutProperty toWriteOnly() {
        return ForwardingPutProperty.wrap(this);
    }

    /**
     * This implementation uses {@link PropertyNameMappingGetPutProperty#wrapInNamespace(GetPutProperty, String)}, which
     * should be adequate for all GetPutProperty implementations that do not need to return another specific sub-type of
     * GetPutProperty.
     */
    @Override
    public default GetPutProperty getNamespace(String space) {
        return PropertyNameMappingGetPutProperty.wrapInNamespace(this, space);
    }

    /**
     * This implementation uses {@link PropertyNameMappingGetPutProperty#wrapInNamespace(GetPutProperty, String, char)}
     * , which should be adequate for all GetPutProperty implementations that do not need to return another specific
     * sub-type of GetPutProperty.
     */
    @Override
    public default GetPutProperty getNamespace(String space, char separator) {
        return PropertyNameMappingGetPutProperty.wrapInNamespace(this, space, separator);
    }

    /**
     * This implementation uses {@link PropertyNameMappingGetPutProperty#wrapInPrefix(GetPutProperty, String)}, which
     * should be adequate for all GetPutProperty implementations that do not need to return another specific sub-type of
     * GetPutProperty.
     */
    @Override
    public default GetPutProperty getPrefix(String prefix) {
        return PropertyNameMappingGetPutProperty.wrapInPrefix(this, prefix);
    }

    /**
     * This implementation uses {@link PropertyNameMappingGetPutProperty#wrapInPrefix(GetPutProperty, String, char)},
     * which should be adequate for all GetPutProperty implementations that do not need to return another specific
     * sub-type of GetPutProperty.
     */
    @Override
    public default GetPutProperty getPrefix(String prefix, char separator) {
        return PropertyNameMappingGetPutProperty.wrapInPrefix(this, prefix, separator);
    }

    @Override
    public default boolean clearLocalProperties() {
        for (String propName : getLocals().getPropertyNames()) {
            removeProperty(propName);
        }
        return true;
    }

    /**
     * Returns a GetPutProperty that will apply the given {@link Function}s to a value being read from this GetProperty
     * or to a value being written to this GetProperty, respectively.
     *
     * <p>
     * This default implementation uses
     * {@link PropertyValueMappingGetPutProperty#applyPropertyValueTransformers(GetPutProperty, Function, Function)},
     * which should be sufficient for most implementations.
     *
     * @param readValueMapper
     *     the {@link Function} providing the desired mapping for {@link #getProperty(String)} and other read methods.
     * @param writeValueMapper
     *     the {@link Function} providing the desired mapping for {@link #putProperty(String, String)} and other write
     *     methods.
     * @return a GetPutProperty that will apply the given {@link Function}s to a value being read from this GetProperty
     *     or to a value being written to this GetProperty, respectively.
     */
    public default GetPutProperty transformingValues(Function<String,String> readValueMapper, Function<String,String> writeValueMapper) {
        return PropertyValueMappingGetPutProperty.applyPropertyValueTransformers(
            this,
            readValueMapper,
            writeValueMapper
        );
    }

    /**
     * Returns a GetPutProperty that will apply the given {@link Function}s to a value being read from this GetProperty
     * or to a value being written to this GetProperty, respectively.
     *
     * <p>
     * This default implementation uses
     * {@link PropertyValueMappingGetPutProperty#wrapWithValueTransformers(GetPutProperty, TextTransformer,
     * TextTransformer)}, which should be sufficient for most implementations.
     *
     * @param readValueTransformer
     *     the {@link TextTransformer} providing the desired mapping for {@link #getProperty(String)} and other read
     *     methods.
     * @param writeValueTransformer
     *     the {@link TextTransformer} providing the desired mapping for {@link #putProperty(String, String)} and other
     *     write methods.
     * @return a GetPutProperty that will apply the given {@link TextTransformer}s to a value being read from this
     *     GetProperty or to a value being written to this GetProperty, respectively.
     */
    public default GetPutProperty transformingValues(TextTransformer readValueTransformer, TextTransformer writeValueTransformer) {
        return PropertyValueMappingGetPutProperty.wrapWithValueTransformers(
            this,
            readValueTransformer,
            writeValueTransformer
        );
    }

}
