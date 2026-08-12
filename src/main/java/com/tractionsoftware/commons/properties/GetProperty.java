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
import com.tractionsoftware.commons.config.Configuration;
import com.tractionsoftware.commons.lang.StringUtil;
import com.tractionsoftware.commons.text.TextTransformer;
import com.tractionsoftware.commons.util.CollectionUtil;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.*;
import java.util.function.*;
import java.util.stream.Stream;

/**
 * A simple read-only interface for objects that support read access to name-value pairs of properties that can managed
 * as Strings.
 *
 * @author Andy Keller, Dave Shepperton
 * @see PutProperty
 * @see GetPutProperty
 */
public interface GetProperty extends PropertyCollection {

    /**
     * Returns the value of the property with the given name, if such a property is known to this GetProperty.
     *
     * <p>
     * This is a <strong>key method</strong> for this interface: it is expected that other methods will be implemented
     * in terms of this one, as is the case for, e.g., the default implementation of
     * {@link #getProperty(String, PropertyLoader, boolean)}, {@link #hasProperty(String)}, etc.
     *
     * @param name
     *     the name of the property whose value is to be retrieved.
     * @return the value of the property with the given name, if such a property is known to this GetProperty; null
     *     otherwise.
     */
    public String getProperty(String name);

    /**
     * Returns the given property name converted to a boolean, or a suitable default if no such property is defined.
     *
     * <p>
     * This default implementation defers to {@link SimpleProperties#loadBoolean(GetProperty, String)}, which should be
     * adequate for almost all implementations.
     *
     * @param name
     *     the name of the property to retrieve as a boolean.
     * @return the given property name converted to a boolean, or a suitable default if no such property is defined.
     */
    public default boolean getBooleanProperty(String name) {
        return SimpleProperties.loadBoolean(this, name);
    }

    /**
     * Returns the given property name converted to a boolean, or the given default if no such property is defined.
     *
     * <p>
     * This default implementation defers to {@link SimpleProperties#loadBoolean(GetProperty, String, boolean)} , which
     * should be adequate for almost all implementations.
     *
     * @param name
     *     the name of the property to retrieve as a boolean.
     * @param defaultValue
     *     the value to be returned if no property with the given name is defined.
     * @return the given property name converted to a boolean, or the given default if no such property is defined.
     */
    public default boolean getBooleanProperty(String name, boolean defaultValue) {
        return SimpleProperties.loadBoolean(this, name, defaultValue);
    }

    /**
     * Returns the given property name converted to a int, or a suitable default if no such property is defined.
     *
     * <p>
     * This default implementation defers to {@link SimpleProperties#loadInt(GetProperty, String)}, which should be
     * adequate for almost all implementations.
     *
     * @param name
     *     the name of the property to retrieve as a int.
     * @return the given property name converted to a int, or a suitable default if such property is defined.
     */
    public default int getIntProperty(String name) {
        return SimpleProperties.loadInt(this, name);
    }

    /**
     * Returns the given property name converted to a int, or the given default if no such property is defined.
     *
     * <p>
     * This default implementation defers to {@link SimpleProperties#loadInt(GetProperty, String, int)} , which should
     * be adequate for almost all implementations.
     *
     * @param name
     *     the name of the property to retrieve as a int.
     * @param defaultValue
     *     the value to be returned if no property with the given name is defined.
     * @return the given property name converted to a int, or the given default if no such property is defined.
     */
    public default int getIntProperty(String name, int defaultValue) {
        return SimpleProperties.loadInt(this, name, defaultValue);
    }

    /**
     * Returns the given property name converted to a long, or a suitable default if no such property is defined.
     *
     * <p>
     * This default implementation defers to {@link SimpleProperties#loadLong(GetProperty, String)}, which should be
     * adequate for almost all implementations.
     *
     * @param name
     *     the name of the property to retrieve as a long.
     * @return the given property name converted to a long, or a suitable default if no such property is defined.
     */
    public default long getLongProperty(String name) {
        return SimpleProperties.loadLong(this, name);
    }

    /**
     * Returns the given property name converted to a long, or the given default if no such property is defined.
     *
     * <p>
     * This default implementation defers to {@link SimpleProperties#loadLong(GetProperty, String, long)} , which should
     * be adequate for almost all implementations.
     *
     * @param name
     *     the name of the property to retrieve as a long.
     * @param defaultValue
     *     the value to be returned if no property with the given name is defined.
     * @return the given property name converted to a long, or the given default if no such property is defined.
     */
    public default long getLongProperty(String name, long defaultValue) {
        return SimpleProperties.loadLong(this, name, defaultValue);
    }

    /**
     * Returns the given property name converted to a double, or a suitable default if no such property is defined.
     *
     * <p>
     * This default implementation defers to {@link SimpleProperties#loadDouble(GetProperty, String)}, which should be
     * adequate for almost all implementations.
     *
     * @param name
     *     the name of the property to retrieve as a double.
     * @return the given property name converted to a double, or a suitable default if no such property is defined.
     */
    public default double getDoubleProperty(String name) {
        return SimpleProperties.loadDouble(this, name);
    }

    /**
     * Returns the given property name converted to a double, or the given default if no such property is defined.
     *
     * <p>
     * This default implementation defers to {@link SimpleProperties#loadDouble(GetProperty, String, double)} , which
     * should be adequate for almost all implementations.
     *
     * @param name
     *     the name of the property to retrieve as a double.
     * @param defaultValue
     *     the value to be returned if no property with the given name is defined.
     * @return the given property name converted to a double, or the given default if no such property is defined.
     */
    public default double getDoubleProperty(String name, double defaultValue) {
        return SimpleProperties.loadDouble(this, name, defaultValue);
    }

    /**
     * Returns an Object corresponding to a single raw String property with the given name, as interpreted by the given
     * {@link PropertyLoader}.
     *
     * <p>
     * This default implementation retrieves the raw String property value, and if the value is not null, directly
     * invokes {@link PropertyLoader#apply(String, String)}, passing that value. It performs no caching. It should be
     * adequate for most implementations.
     *
     * @param <T>
     *     the type of the object to be loaded.
     * @param propName
     *     the name of the property to be loaded.
     * @param loader
     *     the Function that can be used to create the appropriate sort of Object from a raw String property value if
     *     necessary.
     * @param mayUseCache
     *     indicating whether it is safe to cache the Object created by the loader, or to use a cached version.
     * @return the Object representing the value that was loaded by the given {@link PropertyLoader}, or, if allowed,
     *     which was cached from a previous load operation.
     */
    public default <T> T getProperty(String propName, PropertyLoader<? extends T> loader, boolean mayUseCache) {
        return loader.apply(propName, getProperty(propName));
    }

    /**
     * This is a convenience method for determining whether this GetProperty has a property with the given name.
     *
     * <p>
     * This default implementation simply returns false if {@link #getProperty(String)} returns null, and true
     * otherwise. This is intended to be a more or less universal practical definition of "has a property." Therefore,
     * this implementation should be adequate for all cases except those in which some special handling is required for
     * null property values. Override this method with care.
     *
     * @param name
     *     the name of the property of interest.
     * @return true if this GetProperty has a property with the given name.
     */
    public default boolean hasProperty(String name) {
        if (getProperty(name) == null) {
            return false;
        }
        return true;
    }

    public default boolean hasNonBlankProperty(String name) {
        if (StringUtils.isNotBlank(getProperty(name))) {
            return true;
        }
        return false;
    }

    /**
     * Returns true if any of the properties from the given property names is defined.
     *
     * <p>This default implementation iterates over the given property names until it finds one for which {@link
     * #hasProperty(String)} returns true, otherwise returning false. Subclasses should override it if they can provide
     * a more efficient implementation.</p>
     *
     * @param names
     *     the property names to check.
     * @return true if any of the properties from the given property names is defined; false otherwise.
     */
    public default boolean hasAnyProperty(String... names) {
        for (String name : names) {
            if (hasProperty(name)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Finds the first property from the given property names that has a value, and returns it.
     *
     * <p>This default implementation iterates over the given property names until it finds one for which {@link
     * #getProperty(String)} returns a non-null value, otherwise returning null. Subclasses should not need to override
     * it.</p>
     *
     * @param names
     *     the property names to check.
     * @return the first property from the given property names that has a value; null otherwise.
     */
    public default String getFirstProperty(String... names) {
        for (String name : names) {
            String value = getProperty(name);
            if (value != null) {
                return value;
            }
        }
        return null;
    }

    public default int size() {
        return ArrayUtils.getLength(getPropertyNames());
    }

    /**
     * Returns true if this GetProperty does not define any properties. The definition of "does not define any
     * properties" may vary by implementation and/or context. For example, different implementations may have different
     * handling with respect to null property values.
     *
     * <p>
     * This default implementation returns true if the property names from {@link #getPropertyNames()} is null or of
     * length 0.
     *
     * <p>
     * This is intended to be a more or less universal practical definition of "does not define any properties," and
     * should be at least adequate for any GetProperty implementation whose getPropertyNames() method publishes an
     * accurate set of property names. But because it requires assembling all property names, it can be quite
     * inefficient. Subclasses that require a more subtle definition of "does not define any properties," or which could
     * offer a more efficient implementation, should override it with care.
     *
     * @return true if this GetProperty does not have any properties; false otherwise.
     * @see #hasProperty(String)
     */
    public default boolean isEmpty() {
        if (getPropertyNames().isEmpty()) {
            return true;
        }
        return false;
    }

    /**
     * Returns a set containing the names of the properties known to this object. This effectively provides a
     * "published" set of properties.
     *
     * <p>
     * The meaning of "the names of the properties known to this object" is context dependent. In most cases, this will
     * include all property names for which a value is currently present -- i.e., for which {@link #hasProperty(String)}
     * would return true or for which {@link #getProperty(String)} or
     * {@link #getProperty(String, PropertyLoader, boolean)} would return a non-null value. But it is permissible for
     * implementations to publish property names that are
     * <em>supported</em>, but for which no property is necessarily
     * currently defined, or to <em>omit</em> some known or supported property names -- or to return an empty array if
     * appropriate -- even if invocations of getProperty might produce a value, particularly if such an enumeration
     * cannot be produced in a reasonable amount of time. Implementors should use their judgment, taking into account
     * what the GetProperty object represents, and should advertise the expected behavior of this method in
     * documentation.
     *
     * <p>
     * This is a <strong>key method</strong> for this interface: it is expected that other methods will be implemented
     * in terms of this one, as is the case for, e.g., the default implementation of {@link #getProperties(Iterable)},
     * {@link #copyTo(Map)}, etc.
     *
     * @return a set containing the names of all the properties in this GetProperty2.
     */
    public Set<String> getPropertyNames();

    /**
     * Retrieves all properties in the form of a {@link Map} representing the name-value pairs.
     *
     * <p>
     * This default implementation defers to {@link #getProperties(Iterable)}, requesting
     * {@link #getPropertyNames() all available property names}. It should be adequate for most implementations.
     *
     * @return all properties in the form of a {@link Map} representing the name-value pairs. This may or may not
     *     contain null values; that will depend entirely upon the underlying source of name-value pairs and the
     *     implement of this method.
     */
    @Beta
    public default Map<String,String> getAllProperties() {
        return getProperties(getPropertyNames());
    }

    /**
     * Retrieves the properties corresponding to the requested property names, in the form of a {@link Map} representing
     * the name-value pairs.
     *
     * <p>
     * This default implementation produces a Map that contains name-value pairs only for the requested property names
     * for which name-value pairs are defined, and for which the value is not null. It gracefully handles a null
     * argument. It should be adequate for most implementations.
     *
     * @param names
     *     the names of the properties to be retrieved.
     * @return a {@link Map} containing the available name-value pairs corresponding to the requested names. This may or
     *     may not contain null values, and name-value pairs for requested property names that are not defined in this
     *     GetProperty2; that will depend entirely upon the underlying source of name-value pairs and the implement of
     *     this method.
     */
    @Beta
    public default Map<String,String> getProperties(Iterable<String> names) {
        Map<String,String> ret = new LinkedHashMap<>();
        if (names != null) {
            for (String name : names) {
                CollectionUtil.putIfNotNull(name, getProperty(name), ret);
            }
        }
        return ret;
    }

    /**
     * Copies all the name-value pairs from the this GetProperty2 to the given {@link Map}.
     *
     * <p>
     * This default implementation defers to {@link #copyTo(Map, Collection)}, passing null for the exceptions. This
     * should be adequate for most implementations.
     *
     * @param map
     *     the {@link Map} to which the name-value pairs should be copied.
     */
    @Beta
    public default void copyTo(Map<? super String,? super String> map) {
        copyTo(map, null);
    }

    /**
     * Copies the name-value pairs from the this GetProperty2 to the given {@link Map}, except any whose names appear in
     * the given set of exceptions.
     *
     * @param map
     *     the {@link Map} to which the name-value pairs should be copied.
     * @param exceptions
     *     any property names for which the name-value pairs from this GetProperty2 should be omitted from this
     *     operation.
     */
    @Beta
    public default void copyTo(Map<? super String,? super String> map, Collection<?> exceptions) {
        if (map == null) {
            return;
        }
        Stream<String> propNames = getPropertyNames().stream();
        if (CollectionUtil.isNotEmpty(exceptions)) {
            propNames = propNames.filter((propName) -> !exceptions.contains(propName));
        }
        propNames.forEach((propName) -> map.put(propName, getProperty(propName)));
    }

    /**
     * Returns a {@link UnaryOperator} that maps Strings to Strings using the {@link #getProperty(String)} method of
     * this GetProperty.
     *
     * <p>
     * This default implementation should be adequate for all implementations.
     *
     * @return a {@link UnaryOperator} that maps Strings to Strings using the {@link #getProperty(String)} method of
     *     this GetProperty.
     */
    public default UnaryOperator<String> asFunction() {
        return this::getProperty;
    }

    /**
     * Convenience method for consuming each of the name-value property pairs known to this object. In general, no
     * guarantee is made about the order in which the properties will be supplied to the given {@link BiConsumer}.
     *
     * <p>
     * This implementation uses {@link #getPropertyNames()} to enumerate the names of the properties, and supplies that
     * name along with the value returned by {@link #getProperty(String)}.
     *
     * @param consumer
     *     the {@link BiConsumer} to which name-value pairs will be supplied.
     */
    public default void forEach(BiConsumer<String,String> consumer) {
        if (consumer == null) {
            return;
        }
        for (String name : getPropertyNames()) {
            consumer.accept(name, getProperty(name));
        }
    }

    /**
     * Returns the "local" value of the property in the underlying store -- i.e., the value obtained without falling
     * back to any defaults.
     *
     * <p>
     * This default implementation defers to {@link #getProperty(String)} on the GetProperty returned by
     * {@link #getLocals()}. While this should technically always give the correct answer, since this likely incurs the
     * overhead of creating a new GetProperty object isolating the locals, GetProperty implementations should override
     * it when they can provide a more efficient implementation by consulting those local values directly.
     *
     * @param name
     *     the name of the local property to be retrieved.
     * @return the "local" value of the property in the underlying store -- i.e., the value obtained without falling
     *     back to any defaults.
     */
    public default String getLocalProperty(String name) {
        return getLocals().getProperty(name);
    }

    /**
     * Returns a GetProperty representing the default values, to which this GetProperty will fall back as necessary when
     * a particular property is not defined "locally". It may return null if there are no such default properties.
     *
     * <p>
     * This default implementation returns null. GetProperty implementations that incorporate defaults should override
     * it.
     *
     * @return a GetProperty representing the default values, to which this GetProperty will fall back as necessary when
     *     a particular property is not defined "locally".
     */
    public default GetProperty getDefaults() {
        return null;
    }

    /**
     * Returns a GetProperty representing the "local" values, with no defaults.
     *
     * <p>
     * This is a <strong>key method</strong> for this interface: certain default method implementations are implemented
     * in terms of this method, although in this case, that currently only includes {@link #getLocalProperty(String)}).
     *
     * <p>
     * This default implementation returns this GetProperty itself. GetProperty implementations which do not represent
     * "leaves" - i.e., those whose key methods reflect a combination of other GetProperty objects which are logically
     * separated as "locals" and "defaults" -- must override it. Implementations that do not have such a published
     * logical separation should not need to override it.
     *
     * @return a GetProperty representing the "local" values, with no defaults.
     */
    public default GetProperty getLocals() {
        return this;
    }

    /**
     * Returns a GetProperty that will fall back to the given defaults as necessary when "local" values or existing
     * defaults do not include a requested property.
     *
     * <p>
     * This implementation uses {@link GetPropertyLocator}, which should be adequate for most implementations. It also
     * gracefully handles a null argument by returning this GetProperty instance itself.
     *
     * @param defaults
     *     to which the resulting {@link GetProperty} should fall back in case a given named property is not defined in
     *     this GetProperty.
     * @return a GetProperty that will fall back to the given defaults as necessary when "local" values or existing
     *     defaults do not include a requested property.
     */
    public default GetProperty withDefaults(GetProperty defaults) {
        return GetPropertyLocator.getSingleGetPropertyOrLocator(this, defaults);
    }

    /**
     * Returns a GetProperty that encapsulates the same properties as this one, but which caches values for
     * {@link #getProperty(String, PropertyLoader, boolean)}.
     *
     * <p>
     * This default implementation uses {@link CachingGetProperty#wrapWithDefaultCache(GetProperty)} . This should be
     * adequate for most implementations.
     *
     * @return a GetProperty that encapsulates the same properties as this one, but which caches values for
     *     {@link #getProperty(String, PropertyLoader, boolean)}.
     */
    public default GetProperty withCache() {
        return CachingGetProperty.wrapWithDefaultCache(this);
    }

    /**
     * Returns a GetProperty that encapsulates the same properties as this one, but which caches values for
     * {@link #getProperty(String, PropertyLoader, boolean)}.
     *
     * <p>
     * This default implementation uses {@link CachingGetProperty#wrapWithCache(GetProperty, PropertyCache)}. This
     * should be adequate for most implementations.
     *
     * @param cache
     *     the {@link PropertyCache} to use.
     * @return a GetProperty that encapsulates the same properties as this one, but which caches values for
     *     {@link #getProperty(String, PropertyLoader, boolean)}.
     */
    public default GetProperty withCache(PropertyCache cache) {
        if (cache == null) {
            return this;
        }
        return CachingGetProperty.wrapWithCache(this, cache);
    }

    /**
     * Returns a view of this GetProperty that is guaranteed to supply access to the GetProperty API methods only, or at
     * least to methods that are deemed safe for general read-only access. For example, to pass a {@link PropStore} to
     * an alien method that accepts a GetProperty, and to ensure that that method cannot cast the argument back to a
     * PropStore to gain read-write-commit access to the underlying store, a client may invoke toReadOnly on that
     * PropStore before passing it along to that method.
     *
     * <p>
     * This default implementation returns this GetProperty itself, which is a suitable implementation for subclasses
     * that do not have any public methods beyond those declared in GetProperty. Implementations that do have public
     * methods that should not be accessible to clients that arbitrary clients when a GetProperty would do should
     * override it, perhaps using {@link StaticForwardingGetProperty#wrap(GetProperty)}. Likewise, this method may be
     * re-declared in subclasses using a different return type if there is a more specific safe read-only subtype that
     * could be offered in place of GetProperty.
     *
     * @return a view of this GetProperty that is guaranteed to supply access to the GetProperty API methods only, or at
     *     least to methods that are deemed safe for general read-only access.
     */
    public default GetProperty toReadOnly() {
        return this;
    }

    /**
     * Returns a {@link Configuration} that has this GetProperty's properties as its configuration properties.
     *
     * <p>
     * This default implementation uses {@link PropertyAdapters#getPropertyAsConfiguration(GetProperty)}. It should be
     * adequate for all implementations that are not already instances of Configuration (which should override it to
     * return themselves).
     *
     * @return a {@link Configuration} that has this GetProperty's properties as its configuration properties.
     */
    public default Configuration asConfiguration() {
        return PropertyAdapters.getPropertyAsConfiguration(this);
    }

    /**
     * Returns a {@link Configuration} that has this GetProperty's properties as its configuration properties.
     *
     * <p>
     * This default implementation uses {@link PropertyAdapters#getPropertyAsConfiguration(GetProperty)}. It should be
     * adequate for all implementations that are not already instances of Configuration (which should override it to
     * return themselves).
     *
     * @param usePath
     *     the path to use for the resulting {@link Configuration}'s {@link Configuration#getPath()}.
     * @return a {@link Configuration} that has this GetProperty's properties as its configuration properties.
     */
    public default Configuration asConfiguration(String usePath) {
        return PropertyAdapters.getPropertyAsConfiguration(this, usePath);
    }

    /**
     * This implementation uses {@link PropertyNameMappingGetProperty#wrapInNamespace(GetProperty, String)} , which
     * should be adequate for all GetProperty implementations that do not need to return another specific sub-type of
     * GetProperty.
     */
    @Override
    public default GetProperty getNamespace(String space) {
        return PropertyNameMappingGetProperty.wrapInNamespace(this, space);
    }

    /**
     * This implementation uses {@link PropertyNameMappingGetProperty#wrapInNamespace(GetProperty, String, char)} ,
     * which should be adequate for all GetProperty implementations that do not need to return another specific sub-type
     * of GetProperty.
     */
    @Override
    public default GetProperty getNamespace(String space, char separator) {
        return PropertyNameMappingGetProperty.wrapInNamespace(this, space, separator);
    }

    /**
     * This implementation uses {@link PropertyNameMappingGetProperty#wrapInPrefix(GetProperty, String)} , which should
     * be adequate for all GetProperty implementations that do not need to return another specific sub-type of
     * GetProperty.
     */
    @Override
    public default GetProperty getPrefix(String prefix) {
        return PropertyNameMappingGetProperty.wrapInPrefix(this, prefix);
    }

    /**
     * This implementation uses {@link PropertyNameMappingGetProperty#wrapInPrefix(GetProperty, String, char)} , which
     * should be adequate for all GetProperty implementations that do not need to return another specific sub-type of
     * GetProperty.
     */
    @Override
    public default GetProperty getPrefix(String prefix, char separator) {
        return PropertyNameMappingGetProperty.wrapInPrefix(this, prefix, separator);
    }

    /**
     * Returns a GetProperty that will produce values from this GetProperty as modified by the given {@link Function}.
     *
     * <p>
     * This default implementation uses
     * {@link PropertyValueMappingGetProperty#applyPropertyValueTransformer(GetProperty, Function)}, which should be
     * sufficient for most implementations.
     *
     * @param valueMapper
     *     the {@link Function} providing the desired mapping.
     * @return a GetProperty that will produce values from this GetProperty as modified by the given {@link Function}.
     */
    public default GetProperty transformingValuesOnRead(Function<String,String> valueMapper) {
        return PropertyValueMappingGetProperty.applyPropertyValueTransformer(this, valueMapper);
    }

    /**
     * Returns a GetProperty that will produce values from this GetProperty as modified by the given {@link BiFunction},
     * which should accept the name and value of the property.
     *
     * <p>
     * This default implementation uses
     * {@link PropertyValueMappingGetProperty#applyPropertyValueTransformer(GetProperty, BiFunction)}, which should be
     * sufficient for most implementations.
     *
     * @param nameValueMapper
     *     the {@link BiFunction} providing the desired mapping from name and value to a new value.
     * @return a GetProperty that will produce values from this GetProperty as modified by the given {@link Function}.
     */
    public default GetProperty transformingValuesOnRead(BiFunction<String,String,String> nameValueMapper) {
        return PropertyValueMappingGetProperty.applyPropertyValueTransformer(this, nameValueMapper);
    }

    /**
     * Returns a GetProperty that will produce values from this GetProperty as modified by the given
     * {@link TextTransformer}.
     *
     * <p>
     * This default implementation uses
     * {@link PropertyValueMappingGetProperty#wrapWithValueTransformer(GetProperty, TextTransformer)}, which should be
     * sufficient for most implementations.
     *
     * @param transformer
     *     the {@link TextTransformer} providing the desired mapping.
     * @return a GetProperty that will produce values from this GetProperty as modified by the given
     *     {@link TextTransformer}.
     */
    public default GetProperty transformingValuesOnRead(TextTransformer transformer) {
        return PropertyValueMappingGetProperty.wrapWithValueTransformer(this, transformer);
    }

    /**
     * Returns a {@link Supplier} that gives the value of the property with the given name.
     *
     * <p>
     * This default implementation simply dynamically delegates to {@link #getProperty(String)} for the given property
     * name, which should be sufficient for all implementations.
     *
     * @param propName
     *     the name of the property whose value should be returned by the {@link Supplier}.
     * @return a {@link Supplier} that gives the value of the property with the given name.
     */
    public default Supplier<String> getPropertySupplier(final String propName) {
        return () -> GetProperty.this.getProperty(propName);
    }

    /**
     * Returns a String rendering of this GetProperty instance that can be used for diagnostic purposes.
     *
     * <p>
     * This default implementation returns a String that includes the name of this instance's class followed by a
     * comma-separated series of "name=value" pairs for each property whose name is returned by
     * {@link #getPropertyNames()}. Subclasses should override it as necessary.
     *
     * @return a String rendering of this GetProperty instance that can be used for diagnostic purposes.
     */
    public default String toDebugString() {
        return getClass().getName() + ":[" +
               StringUtil.join(
                   getPropertyNames().stream()
                       .map((String propName) -> propName + "=" + getProperty(propName)),
                   ','
               ) +
               "]";
    }

}
