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

import com.google.common.annotations.Beta;
import com.tractionsoftware.commons.util.CollectionsUtil;
import com.tractionsoftware.commons.text.TextTransformer;

import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.stream.Stream;

/**
 * A simple write-only interface for objects that support write access to name-value pairs of properties that can
 * managed as Strings.
 *
 * <p>
 * General purpose implementations are encouraged to support
 * <code>null</code> for property names and values, but some specific
 * implementations may not.
 *
 * @author Andy Keller, Dave Shepperton
 */
public interface PutProperty extends PropertyCollection, BiConsumer<String,String> {

    /**
     * Set the value of a given named property.
     *
     * @param name
     *     the name of the property whose value is to be set.
     * @param value
     *     the new value of the property.
     */
    public void putProperty(String name, String value);

    /**
     * Sets the value of the given named property to the given boolean value converted to a String.
     *
     * <p>
     * This default implementation defers to {@link SimpleProperties#saveBoolean(PutProperty, String, boolean)} , which
     * should be adequate for almost all implementations.
     *
     * @param name
     *     the name of the property whose value is to be set.
     * @param value
     *     the new value of the property.
     */
    public default void putBooleanProperty(String name, boolean value) {
        SimpleProperties.saveBoolean(this, name, value);
    }

    /**
     * Sets the value of the given named property to the given int value converted to a String.
     *
     * <p>
     * This default implementation defers to {@link SimpleProperties#saveInt(PutProperty, String, int)} , which should
     * be adequate for almost all implementations.
     *
     * @param name
     *     the name of the property whose value is to be set.
     * @param value
     *     the new value of the property.
     */
    public default void putIntProperty(String name, int value) {
        SimpleProperties.saveInt(this, name, value);
    }

    /**
     * Sets the value of the given named property to the given long value converted to a String.
     *
     * <p>
     * This default implementation defers to {@link SimpleProperties#saveLong(PutProperty, String, long)} , which should
     * be adequate for almost all implementations.
     *
     * @param name
     *     the name of the property whose value is to be set.
     * @param value
     *     the new value of the property.
     */
    public default void putLongProperty(String name, long value) {
        SimpleProperties.saveLong(this, name, value);
    }

    /**
     * Sets the value of the given named property to the given double value converted to a String.
     *
     * <p>
     * This default implementation defers to {@link SimpleProperties#saveDouble(PutProperty, String, double)} , which
     * should be adequate for almost all implementations.
     *
     * @param name
     *     the name of the property whose value is to be set.
     * @param value
     *     the new value of the property.
     */
    public default void putDoubleProperty(String name, double value) {
        SimpleProperties.saveDouble(this, name, value);
    }

    public default void putObjectPropertyAsString(String name, Object value) {
        putProperty(name, Objects.toString(value, null));
    }

    /**
     * Removes the property with the given name.
     *
     * <p>
     * This default implementation defers to {@link #putProperty(String, String)}, passing null as the value. This
     * should be adequate for all implementations for which putting null value is equivalent to removing a property.
     *
     * @param name
     *     the name of the property to remove.
     */
    public default void removeProperty(String name) {
        putProperty(name, null);
    }

    /**
     * Removes <strong>all local properties that can be removed</strong>, if supported. Not all implementations will
     * support this method, and even those that do may not support removing all properties.
     *
     * <p>
     * This implementation does nothing and returns false. Implementations that support this operation must return true,
     * even if it turns out that no property modifications were required.
     *
     * @return true if the implementation generally supports this operation (regardless of whether any property
     *     modifications were required); false if the implementation does not support this operation (and did nothing).
     */
    public default boolean clearLocalProperties() {
        return false;
    }

    /**
     * Adds all property name-value pairs from the given {@link GetProperty} into this PutProperty.
     *
     * <p>
     * This default implementation defers to {@link #putProperties(GetProperty, Collection)}, passing null for the
     * exceptions. It gracefully handles a null argument.
     *
     * @param source
     *     the {@link GetProperty} whose name-value pairs should be added to this PutProperty.
     */
    @Beta
    public default void putAllProperties(GetProperty source) {
        putProperties(source, null);
    }

    /**
     * Adds all property name-value pairs, except any whose names appear in the given set of exceptions, from the given
     * {@link GetProperty} into this PutProperty.
     *
     * <p>
     * This implementation filters {@link GetProperty#getPropertyNames() the property names supplied} by the given
     * GetProperty as necessary, deferring to {@link #putProperty(String, String)} to add each name-value pair. It
     * gracefully handles null arguments.
     *
     * @param source
     *     the {@link GetProperty} whose name-value pairs should be added to this PutProperty.
     * @param exceptions
     *     any property names for which the name-value pairs from the given {@link GetProperty} should be omitted from
     *     this operation.
     */
    @Beta
    public default void putProperties(GetProperty source, Collection<?> exceptions) {

        if (source == null) {
            return;
        }

        Stream<String> propNames = source.getPropertyNames().stream();
        if (CollectionsUtil.isNotEmpty(exceptions)) {
            propNames = propNames.filter((propName) -> !exceptions.contains(propName));
        }
        propNames.forEach((propName) -> putProperty(propName, source.getProperty(propName)));

    }

    /**
     * Copies all the name-value pairs from the given {@link Map} to this PutProperty.
     *
     * <p>
     * This default implementation defers to {@link #copyFrom(Map, Collection)}, passing null for the exceptions.
     *
     * @param source
     *     the Map containing the name-value pairs to be added.
     */
    @Beta
    public default void copyFrom(Map<? super String,? super String> source) {
        copyFrom(source, null);
    }

    /**
     * Copies all the name-value pairs from the given {@link Map} to this PutProperty, except any whose names appear in
     * the given set of exceptions.
     *
     * <p>
     * This default implementation defers to {@link #putProperties(GetProperty, Collection)}, wrapping the given Map to
     * create a {@link GetProperty} from which name-value pairs can be read, and passing on the exceptions. It
     * gracefully handles null arguments.
     *
     * @param source
     *     the Map containing the name-value pairs to be added.
     * @param exceptions
     *     any property names for which the name-value pairs from the given {@link Map} should be omitted from this
     *     operation.
     */
    @Beta
    public default void copyFrom(Map<? super String,? super String> source, Collection<?> exceptions) {
        if (CollectionsUtil.isEmpty(source)) {
            return;
        }
        putProperties(SimpleProperties.asGetProperty(source), exceptions);
    }

    /**
     * Implements {@link BiConsumer} for two Strings using the {@link #putProperty(String, String)} method of this
     * PutProperty.
     *
     * <p>
     * This default implementation should be adequate for all implementations.
     */
    @Override
    public default void accept(String name, String value) {
        putProperty(name, value);
    }

    /**
     * Returns a view of this PutProperty that is guaranteed to supply access to the PutProperty API methods only. This
     * method should be used to prevent other code from being able to cast PutProperty instances to other specific
     * subclasses in order to gain access to their specific methods.
     *
     * <p>
     * This default implementation returns this PutProperty itself, which is a suitable implementation for subclasses
     * that do not have any public methods beyond those declared in PutProperty. Implementations that do have public
     * methods that should not be accessible to clients that arbitrary clients when a PutProperty would do should
     * override it, perhaps using {@link ForwardingPutProperty#wrap(PutProperty)}.
     *
     * @return a view of this PutProperty that is guaranteed to supply access to the PutProperty API methods only.
     */
    public default PutProperty toWriteOnly() {
        return this;
    }

    /**
     * This implementation uses {@link ForwardingPutProperty#wrapInNamespace(PutProperty, String)} , which should be
     * adequate for all PutProperty implementations that do not need to return another specific sub-type of
     * PutProperty.
     */
    @Override
    public default PutProperty getNamespace(String space) {
        return ForwardingPutProperty.wrapInNamespace(this, space);
    }

    /**
     * This implementation uses {@link ForwardingPutProperty#wrapInNamespace(PutProperty, String, char)} , which should
     * be adequate for all GetProperty implementations that do not need to return another specific sub-type of
     * PutProperty.
     */
    @Override
    public default PutProperty getNamespace(String space, char separator) {
        return ForwardingPutProperty.wrapInNamespace(this, space, separator);
    }

    /**
     * This implementation uses {@link ForwardingPutProperty#wrapInPrefix(PutProperty, String)} , which should be
     * adequate for all PutProperty implementations that do not need to return another specific sub-type of
     * PutProperty.
     */
    @Override
    public default PutProperty getPrefix(String prefix) {
        return ForwardingPutProperty.wrapInPrefix(this, prefix);
    }

    /**
     * This implementation uses {@link ForwardingPutProperty#wrapInPrefix(PutProperty, String, char)} , which should be
     * adequate for all GetProperty implementations that do not need to return another specific sub-type of
     * PutProperty.
     */
    @Override
    public default PutProperty getPrefix(String prefix, char separator) {
        return ForwardingPutProperty.wrapInPrefix(this, prefix, separator);
    }

    /**
     * Returns a PutProperty that will write values to this PutProperty as modified by the given {@link Function}.
     *
     * <p>
     * This default implementation uses
     * {@link PropertyValueMappingPutProperty#applyPropertyValueTransformer(PutProperty, Function)} , which should be
     * sufficient for most implementations.
     *
     * @param valueMapper
     *     the {@link Function} providing the desired mapping.
     * @return a PutProperty that will write values to this PutProperty as modified by the given {@link Function}.
     */
    public default PutProperty transformingValuesOnWrite(Function<String,String> valueMapper) {
        return PropertyValueMappingPutProperty.applyPropertyValueTransformer(this, valueMapper);
    }

    /**
     * Returns a PutProperty that will write values to this PutProperty as modified by the given {@link TextTransformer}.
     *
     * <p>
     * This default implementation uses
     * {@link PropertyValueMappingPutProperty#wrapWithValueTransformer(PutProperty, TextTransformer)} , which should be
     * sufficient for most implementations.
     *
     * @param transformer
     *     the {@link TextTransformer} providing the desired mapping.
     * @return a PutProperty that will write values to this PutProperty as modified by the given {@link TextTransformer}.
     */
    public default PutProperty transformingValuesOnWrite(TextTransformer transformer) {
        return PropertyValueMappingPutProperty.wrapWithValueTransformer(this, transformer);
    }

}
