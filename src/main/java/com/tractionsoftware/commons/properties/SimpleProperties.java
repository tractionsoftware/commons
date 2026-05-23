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

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.tractionsoftware.commons.codec.Base64Util;
import com.tractionsoftware.commons.config.Configuration;
import com.tractionsoftware.commons.io.FileNameUtil;
import com.tractionsoftware.commons.lang.EnumsUtil;
import com.tractionsoftware.commons.lang.NativeTypeConversion;
import com.tractionsoftware.commons.lang.StringUtil;
import com.tractionsoftware.commons.text.StringSplitUtil;
import com.tractionsoftware.commons.util.DateFormats;
import com.tractionsoftware.commons.util.SimpleDurationUnit;
import org.apache.commons.lang3.StringUtils;
import org.jspecify.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.text.ParsePosition;
import java.time.Duration;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.IntStream;

/**
 * This class contains various convenience methods for loading and saving values of various types.
 */
public final class SimpleProperties {

    private SimpleProperties() {
    }

    private static final Logger LOGGER = LoggerFactory.getLogger(SimpleProperties.class);

    private static final GetProperty EMPTY_GET_PROPERTY = new StaticMapGetProperty(
        ImmutableMap.of(), "Empty GetProperty"
    );

    private static final Configuration EMPTY_CONFIGURATION = new EmptyConfiguration("");

    /**
     * A {@link GetProperty} which is backed by a statically specified {@link Map}.
     */
    private static final class StaticMapGetProperty extends AbstractMapGetProperty {

        private final Map<? super String,?> map;

        private final String name;

        private StaticMapGetProperty(Map<? super String,?> map) {
            this(map, null);
        }

        private StaticMapGetProperty(Map<? super String,?> map, String name) {
            Objects.requireNonNull(map, "Map");
            this.map = map;
            this.name = name;
        }

        @Override
        public @NonNull String toString() {
            return "GetProperty: stat map '" +
                   Objects.toString(name, "map") +
                   "' {" +
                   map.getClass().getName() +
                   "}";
        }

        @Override
        public final String getName() {
            return name;
        }

        @Override
        protected final Map<? super String,?> map() {
            if (map instanceof ImmutableMap) {
                return map;
            }
            return Collections.unmodifiableMap(map);
        }

    }

    private static final class EmptyConfiguration implements Configuration {

        private final String name;

        private EmptyConfiguration(String name) {
            this.name = name;
        }

        @Override
        public final @NonNull String toString() {
            return
                "EmptyConfiguration: [" +
                Objects.toString(getName(), "?") +
                "]";
        }

        @Override
        public final String getName() {
            return name;
        }

        @Override
        public final String getPath() {
            return null;
        }

        @Override
        public final Map<String,String> getTemplateSettings() {
            return null;
        }

        @Override
        public final Configuration withNewName(String newName) {
            if (Objects.equals(name, newName)) {
                return this;
            }
            return new EmptyConfiguration(newName);
        }

        @Override
        public final String getProperty(String name) {
            return null;
        }

        @Override
        public final Set<String> getPropertyNames() {
            return ImmutableSet.of();
        }

    }

    private static final class SeparateListPropertyNameIterator implements Iterator<String> {

        public static final SeparateListPropertyNameIterator getUnboundedInstance(String name) {
            return new SeparateListPropertyNameIterator(name, Integer.MAX_VALUE);
        }

        public static final SeparateListPropertyNameIterator getInstance(String name, int count) {
            return new SeparateListPropertyNameIterator(name, count);
        }

        private final String name;

        private final int count;

        private int next;

        private SeparateListPropertyNameIterator(String name, int count) {
            this.name = name;
            this.count = count;
            this.next = 0;
        }

        @Override
        public final boolean hasNext() {
            if (next < count) {
                return true;
            }
            return false;
        }

        @Override
        public final String next() {
            if (hasNext()) {
                return name + (next++);
            }
            throw new NoSuchElementException();
        }

    }

    public static final GetProperty emptyGetProperty() {
        return EMPTY_GET_PROPERTY;
    }

    public static final Configuration emptyConfiguration() {
        return EMPTY_CONFIGURATION;
    }

    /**
     * Creates and returns a {@link Configuration} with a has a name only (no configuration properties). Its
     * {@link Configuration#getName()} method will return the given name, but some other methods, such as
     * {@link Configuration#getPath()} may return null.
     *
     * @param name
     *     the name to give to the {@link Configuration}.
     * @return a {@link Configuration} with a has a name only (no configuration properties).
     */
    public static final Configuration getEmptyConfiguration(String name) {
        return new EmptyConfiguration(name);
    }

    /**
     * Provides a view of the given {@link Map} as a {@link GetProperty}. The returned GetProperty will reflect any
     * changes in the supplied Map. Therefore, if the intention is to have the returned GetProperty reflect a snapshot
     * as of the time this method is invoked, clients should make a copy, e.g., via {@link ImmutableMap#copyOf(Map)},
     * before invoking this method.
     *
     * @param map
     *     the {@link Map} whose name-value pairs will supply the name-value pairs for the returned
     *     {@link GetProperty}.
     * @return a view of the given {@link Map} as a {@link GetProperty}.
     */
    public static final GetProperty asGetProperty(Map<? super String,?> map) {
        return new StaticMapGetProperty(map);
    }

    public static final GetProperty asGetProperty(Map<? super String,?> map, String name) {
        return new StaticMapGetProperty(map, name);
    }

    public static final PutProperty asPutProperty(Map<? super String,? super String> map) {
        return MapPropertyStore.createInstance(map).toWriteOnly();
    }

    public static final GetPutProperty asGetPutProperty(Map<? super String,? super String> map) {
        return MapPropertyStore.createInstance(map).toReadWrite();
    }

    public static final <R> PropStore<R> asPropStore(Map<? super String,? super String> map) {
        return new MapPropertyStore<>("", map);
    }

    public static final <R> PropStore<R> asImmutablePropStore(Map<? super String,? super String> map) {
        return SimpleProperties.<R>asPropStore(map).toImmutable();
    }

    public static final Configuration asConfiguration(Map<? super String,? super String> map, String name) {
        return asGetProperty(map).asConfiguration().withNewName(name);
    }

    public static final String getName(GetProperty props) {
        if (props == null) {
            return null;
        }
        String name = props.getName();
        if (name == null) {
            return props.getProperty("name");
        }
        return name;
    }

    /**
     * Loads a string from the given properties, being tolerant of a null property source.
     *
     * @param props
     *     the {@link GetProperty} representing the source properties, which may be null.
     * @param name
     *     the name of the property to load.
     * @return the value of the named property in the given property source; or null if there is no such property, or if
     *     the source {@link GetProperty} was null.
     */
    public static final String loadString(GetProperty props, String name) {
        return (props != null) ? props.getProperty(name) : null;
    }

    /**
     * Loads a string from the given properties, being tolerant of a null property source, falling back to the given
     * default value.
     *
     * @param props
     *     the {@link GetProperty} representing the source properties, which may be null.
     * @param name
     *     the name of the property to load.
     * @param defaultValue
     *     the default value to be returned if there is no such property, or if the source {@link GetProperty} was
     *     null.
     * @return the value of the named property in the given property source; or the given default value if there is no
     *     such property, or if the source {@link GetProperty} was null.
     */
    public static final String loadString(GetProperty props, String name, String defaultValue) {
        return Objects.toString(loadString(props, name), defaultValue);
    }

    /**
     * Identical to invoking {@link StringUtils#trimToEmpty(String)} on the result of
     * {@code loadString(get, name, null)}.
     *
     * @param props
     *     the {@link GetProperty} representing the source properties, which may be null.
     * @param name
     *     the name of the property to load.
     * @return a trimmed version of the loaded String, or the empty string if no string was loaded.
     */
    public static final String loadTrimmedOrEmpty(GetProperty props, String name) {
        return StringUtils.trimToEmpty(loadString(props, name, null));
    }

    /**
     * Identical to invoking {@link StringUtils#trimToNull(String)} on the result of
     * {@code loadString(get, name, null)}.
     *
     * @param props
     *     the {@link GetProperty} representing the source properties, which may be null.
     * @param name
     *     the name of the property to load.
     * @return a trimmed version of the loaded String, or null if the no string was loaded or the loaded string was
     *     empty.
     */
    public static final String loadTrimmedOrNull(GetProperty props, String name) {
        return StringUtils.trimToNull(loadString(props, name, null));
    }

    public static final void saveString(PutProperty props, String name, String value) {
        props.putProperty(name, value);
    }

    public static final void saveString(PutProperty props, String name, String value, String unless) {
        if (value != null && value.equals(unless)) {
            value = null;
        }
        props.putProperty(name, value);
    }

    /**
     * Attempts to load a property value for each name and returns the first non-null value.
     */
    public static final String loadFirstString(GetProperty props, Iterable<String> names) {
        return loadFirstString(props, names, null);
    }

    public static final String loadFirstString(GetProperty props, Iterable<String> names, String defaultValue) {
        if (names == null) {
            return defaultValue;
        }
        for (String name : names) {
            String ret = props.getProperty(name);
            if (ret != null) {
                return ret;
            }
        }
        return defaultValue;
    }

    private static final String base64EncodedName(String name) {
        return (name != null) ? name + "_base64_encoded" : "base64_encoded";
    }

    public static final String loadBase64Encoded(GetProperty props, String name, String notfound) {
        String value = loadString(props, base64EncodedName(name), null);
        if (value == null) {
            return loadString(props, name, notfound);
        }
        return Base64Util.getDecodedString(value);
    }

    public static final void saveBase64Encoded(PutProperty props, String name, String value) {
        if (value != null) {
            value = Base64Util.getEncodedString(value);
        }
        props.putProperty(base64EncodedName(name), value);
        props.putProperty(name, null); // clear unencoded (convert)
    }

    /**
     * Reads the property using loadString and then converts / to File.separator.
     */
    public static final String loadFilePath(GetProperty props, String name, String notfound) {
        return FileNameUtil.platformSpecificPath(loadString(props, name, notfound));
    }

    public static final File loadFile(GetProperty props, String name) {
        return loadFile(props, name, null);
    }

    public static final File loadFile(GetProperty props, String name, File notfound) {
        String path = loadFilePath(props, name, null);
        if (path == null) {
            return notfound;
        }
        return new File(path);
    }

    /**
     * Saves a platform-independent path.
     */
    public static final void saveFilePath(PutProperty props, String name, String filePath) {
        saveString(props, name, FileNameUtil.platformIndependentPath(filePath));
    }

    /**
     * Saves the path of the given File.
     */
    public static final void saveFile(PutProperty props, String name, File file) {
        saveFilePath(props, name, file.getPath());
    }

    /**
     * <p>
     * Loads a String from a single property with the given name if it exists, or else from a sequence of properties
     * whose names are numerically indexed versions of the given name of the form "
     * <code>name<em>n</em></code>". This allows a single value to be
     * saved to a store in a single property or as a series of values, depending upon which is more convenient in a
     * given situation, and then when the saved value is loaded, this method can be used if it is not known whether the
     * value is saved in a single value or distributed in multiple values.
     *
     * <p>
     * This method works in the following manner:
     *
     * <p>
     * The property with the given name is loaded using loadString. If that property is defined, its value is used
     * directly.
     *
     * <p>
     * Otherwise, the loadList method is used to load a series of properties named "<code>name<em>n</em></code>", with n
     * starting at 0. If the loadList method reports that any such values were loaded,
     * {@link StringUtil#join(String[], String)} is used to compose the final value by sequentially concatenating the
     * individual values (in the same order in which they were loaded), with the value of the argument for the join
     * parameter inserted in between each value.
     *
     * @param props
     *     The GetProperty from which the value or values should be loaded.
     * @param name
     *     The name of the list property to be loaded.
     * @param join
     *     The String to be inserted in bewteen list elements when joining elements loaded from separate properties.
     * @param notfound
     *     The value to be returned if neither the named property nor the numerically indexed sequence of named
     *     properties are undefined.
     * @return If the property with the given name is defined, or if any property named "<code>name<em>n</em></code>",
     *     with n ranging as described above, is defined, then the value of the String that is loaded as described
     *     above; otherwise, the argument for the notfound parameter.
     */
    public static final String loadSplitString(GetProperty props, String name, String join, String notfound) {
        String ret = loadString(props, name, null);
        if (ret == null) {
            ArrayList<String> list = new ArrayList<>();
            if (loadCollectionSeparateProperties(props, name, list)) {
                ret = StringUtil.join(list, join);
            }
            else {
                ret = notfound;
            }
        }
        return ret;
    }

    /**
     * @return -1 if there is no value for this property
     */
    public static final int loadInt(GetProperty props, String name) {
        return loadInt(props, name, -1);
    }

    public static final int loadInt(GetProperty props, String name, int notfound) {
        return NativeTypeConversion.stringToInt(loadString(props, name), notfound);
    }

    public static final void saveInt(PutProperty props, String name, int value) {
        saveString(props, name, Integer.toString(value));
    }

    public static final void saveInt(PutProperty props, String name, int value, int unless) {
        saveString(props, name, (value == unless) ? null : Integer.toString(value));
    }

    /**
     * @return -1 if there is no value for this property
     */
    public static final long loadLong(GetProperty props, String name) {
        return loadLong(props, name, -1);
    }

    public static final long loadLong(GetProperty props, String name, long notfound) {
        return NativeTypeConversion.stringToLong(loadString(props, name), notfound);
    }

    public static final void saveLong(PutProperty props, String name, long value) {
        saveString(props, name, Long.toString(value));
    }

    public static final void saveLong(PutProperty props, String name, long value, long unless) {
        saveString(props, name, (value == unless) ? null : Long.toString(value));
    }

    /**
     * @return -1 if there is no value for this property
     */
    public static final short loadShort(GetProperty props, String name) {
        return loadShort(props, name, (short) -1);
    }

    public static final short loadShort(GetProperty props, String name, short notfound) {
        return NativeTypeConversion.stringToShort(loadString(props, name), notfound);
    }

    public static final void saveShort(PutProperty props, String name, short value) {
        saveString(props, name, Short.toString(value));
    }

    /**
     * @return -1 if there is no value for this property
     */
    public static final double loadDouble(GetProperty props, String name) {
        return loadDouble(props, name, -1);
    }

    public static final double loadDouble(GetProperty props, String name, double notfound) {
        return NativeTypeConversion.stringToDouble(loadString(props, name), notfound);
    }

    public static final void saveDouble(PutProperty props, String name, double value) {
        saveString(props, name, Double.toString(value));
    }

    /**
     * @return false if there is no value for this property
     */
    public static final boolean loadBoolean(GetProperty props, String name) {
        return loadBoolean(props, name, false);
    }

    public static final boolean loadBoolean(GetProperty props, String name, boolean defaultValue) {
        return NativeTypeConversion.stringToBoolean(loadString(props, name), defaultValue);
    }

    public static final void saveBoolean(PutProperty props, String name, boolean value) {
        saveString(props, name, NativeTypeConversion.booleanToString(value));
    }

    public static final void saveBoolean(PutProperty props, String name, boolean value, boolean unless) {
        saveString(props, name, (value == unless) ? null : NativeTypeConversion.booleanToString(value));
    }

    public static final void saveDate(PutProperty props, String name, Date date) {
        saveLong(props, name, (date != null ? date.getTime() : Long.MIN_VALUE), Long.MIN_VALUE);
    }

    public static final Date loadDate(GetProperty props, String name, Date defaultValue) {
        long l = loadLong(props, name, Long.MIN_VALUE);
        return (l != Long.MIN_VALUE) ? new Date(l) : defaultValue;
    }

    public static final void saveUrlDate(PutProperty props, String name, Date date, TimeZone timeZone) {
        if (date == null) {
            saveString(props, name, null);
        }
        else {
            saveString(props, name, DateFormats.getUrlDateFormat(timeZone).format(date));
        }
    }

    public static final Date loadUrlDate(GetProperty props, String name, TimeZone timeZone) {
        return loadUrlDate(props, name, timeZone, null);
    }

    public static final Date loadUrlDate(GetProperty props, String name, TimeZone timeZone, Date defaultValue) {
        String urlDateSpec = loadTrimmedOrNull(props, name);
        if (urlDateSpec == null) {
            return defaultValue;
        }
        return DateFormats.getUrlDateFormat(timeZone).parse(urlDateSpec, new ParsePosition(0));
    }

    /**
     * The suffix for a property name that may indicate to the loadList method, when list values are being loaded from
     * individually numbered properties, how many such properties are defined.
     */
    public static final String SEPARATE_PROPERTY_COUNT_PROPERTY_NAME_SUFFIX = "count";

    /**
     * Loads a series of values into a list, either from a single property (assuming list elements are delimited by
     * commas), or from a series of individual properties, depending upon the value of the argument for the listType
     * parameter.
     *
     * <p>
     * If the list type is LIST_SINGLE_PROPERTY or LIST_SINGLE_PROPERTY_ESCAPED, this method defers to
     * {@link #loadCollectionSingleProperty(GetProperty, String, Collection,
     * NativeTypeConversion.CollectionToStringOptions)}, passing false and true, respectively, for the
     * <tt>escaped</tt> parameter.
     *
     * <p>
     * If the list type is LIST_SEPARATE_PROPERTIES, or any other value, this method defers to
     * {@link #loadCollectionSeparateProperties(GetProperty, String, Collection)}.
     *
     * @param props
     *     from which the value or values should be loaded.
     * @param name
     *     the name of the list property to be loaded.
     * @param list
     *     a Collection to which the loaded values will be added. The list is used <em>without</em> first being
     *     cleared.
     * @param singleProperty
     *     indicates whether all Collection elements should be stored in a single property, instead of having each
     *     element stored in its own property.
     * @return true if values were successfully added to the list; false otherwise.
     */
    public static final boolean loadList(GetProperty props, String name, Collection<? super String> list, boolean singleProperty, NativeTypeConversion.CollectionToStringOptions options) {
        if (singleProperty) {
            return loadCollectionSingleProperty(props, name, list, options);
        }
        return loadCollectionSeparateProperties(props, name, list, options);
    }

    /**
     * Loads a list of Strings from a single property, separated by commas. Commas will always be treated as a delimiter
     * and cannot be escaped. If the property does not exist, the list returned will be empty. This will never return
     * null.
     *
     * <p>
     * This is currently the most common usage of loadList and saves the caller from having to create the List<String>.
     *
     * @param props
     *     from which the value or values should be loaded
     * @param name
     *     the name of the list property to be loaded
     */
    public static final List<String> loadList(GetProperty props, String name) {
        return loadList(props, name, NativeTypeConversion.DEFAULT_COLLECTION_TO_STRING_OPTIONS);
    }

    public static final List<String> loadList(GetProperty props, String name, NativeTypeConversion.CollectionToStringOptions options) {
        if (hasCollectionSeparateProperties(props, name)) {
            return loadListSeparateProperties(props, name, options);
        }
        return loadListSingleProperty(props, name, options);
    }

    /**
     * Loads a Set of Strings from a single property, separated by commas. Commas will always be treated as a delimiter
     * and cannot be escaped. If the property does not exist, an empty Set will be returned (never null).
     *
     * @param props
     *     from which the value or values should be loaded.
     * @param name
     *     the name of the list property to be loaded.
     */
    public static final SequencedSet<String> loadSet(GetProperty props, String name) {
        return loadSet(props, name, NativeTypeConversion.DEFAULT_COLLECTION_TO_STRING_OPTIONS);
    }

    public static final SequencedSet<String> loadSet(GetProperty props, String name, NativeTypeConversion.CollectionToStringOptions options) {
        if (hasCollectionSeparateProperties(props, name)) {
            return loadSetSeparateProperties(props, name);
        }
        return loadSetSingleProperty(props, name);
    }

    public static final List<String> loadCommaSpaceSemicolonSeparatedList(GetProperty props, String name) {
        return StringSplitUtil.parseCommaSpaceSemicolonSeperatedList(loadString(props, name));
    }

    public static final List<String> loadCommaSpaceSemicolonSeparatedListSkippingQuoted(GetProperty props, String name) {
        return StringSplitUtil.parseCommaSpaceSemicolonSeparatedListSkippingQuoted(loadString(props, name));
    }

    public static final List<String> loadListSingleProperty(GetProperty props, String name) {
        return loadListSingleProperty(props, name, NativeTypeConversion.DEFAULT_COLLECTION_TO_STRING_OPTIONS);
    }

    public static final List<String> loadListSingleProperty(GetProperty props, String name, NativeTypeConversion.CollectionToStringOptions options) {
        List<String> list = new ArrayList<>();
        loadCollectionSingleProperty(props, name, list, options);
        return list;
    }

    public static final SequencedSet<String> loadSetSingleProperty(GetProperty props, String name) {
        return loadSetSingleProperty(props, name, NativeTypeConversion.DEFAULT_COLLECTION_TO_STRING_OPTIONS);
    }

    public static final SequencedSet<String> loadSetSingleProperty(GetProperty props, String name, NativeTypeConversion.CollectionToStringOptions options) {
        SequencedSet<String> list = new LinkedHashSet<>();
        loadCollectionSingleProperty(props, name, list, options);
        return list;
    }

    public static final List<String> loadListSeparateProperties(GetProperty props, String name) {
        return loadListSeparateProperties(props, name, NativeTypeConversion.DEFAULT_COLLECTION_TO_STRING_OPTIONS);
    }

    public static final List<String> loadListSeparateProperties(GetProperty props, String name, NativeTypeConversion.CollectionToStringOptions options) {
        List<String> list = new ArrayList<>();
        loadCollectionSeparateProperties(props, name, list);
        return list;
    }

    public static final SequencedSet<String> loadSetSeparateProperties(GetProperty props, String name) {
        return loadSetSeparateProperties(props, name, NativeTypeConversion.DEFAULT_COLLECTION_TO_STRING_OPTIONS);
    }

    public static final SequencedSet<String> loadSetSeparateProperties(GetProperty props, String name, NativeTypeConversion.CollectionToStringOptions options) {
        SequencedSet<String> list = new LinkedHashSet<>();
        loadCollectionSeparateProperties(props, name, list);
        return list;
    }

    public static final boolean loadCollectionSingleProperty(GetProperty props, String name, Collection<? super String> collection, NativeTypeConversion.CollectionToStringOptions options) {
        return NativeTypeConversion.stringToList(loadString(props, name), collection, options);
    }

    public static final boolean loadCollectionSeparateProperties(GetProperty props, String name, Collection<? super String> list) {
        return loadCollectionSeparateProperties(
            props, name, list, NativeTypeConversion.DEFAULT_COLLECTION_TO_STRING_OPTIONS
        );
    }

    public static final boolean loadCollectionSeparateProperties(GetProperty props, String name, Collection<? super String> list, NativeTypeConversion.CollectionToStringOptions options) {

        if (props == null || list == null) {
            return false;
        }

        if (name == null) {
            name = "";
        }

        boolean found = false;
        int count = loadInt(props, SimplePropertyNameMapper.ns(name, SEPARATE_PROPERTY_COUNT_PROPERTY_NAME_SUFFIX));

        if (count >= 0) {
            SeparateListPropertyNameIterator propNames = SeparateListPropertyNameIterator.getInstance(name, count);
            while (propNames.hasNext()) {
                String propValue = props.getProperty(propNames.next());
                if (propValue != null) {
                    list.add(propValue);
                    if (!found) {
                        found = true;
                    }
                }
            }
            return found;
        }

        // If the count is not specified, continue until an undefined property is found.
        SeparateListPropertyNameIterator propNames = SeparateListPropertyNameIterator.getUnboundedInstance(name);
        while (propNames.hasNext()) {
            String propValue = props.getProperty(propNames.next());
            if (propValue == null) {
                break;
            }
            list.add(propValue);
            if (!found) {
                found = true;
            }
        }
        return found;

    }

    public static final boolean hasCollectionSeparateProperties(GetProperty props, String name) {

        if (props == null) {
            return false;
        }

        String useName = StringUtils.defaultString(name);
        int count = loadInt(props, SimplePropertyNameMapper.ns(useName, SEPARATE_PROPERTY_COUNT_PROPERTY_NAME_SUFFIX));

        if (count == 0) {
            return false;
        }
        if (count > 0) {
            return IntStream.range(0, count).anyMatch(i -> props.hasProperty(useName + i));
        }
        return props.hasProperty(useName + 0);

    }

    /**
     * Loads all values for the given property, from the given store that allows multiple values to be stored under the
     * same name, into the given list.
     *
     * @param get
     *     The GetProperties from which the values are to be loaded.
     * @param name
     *     The name of the property in which the values are stored.
     * @param list
     *     The Collection to which the property values are to be added.
     * @return true if the property values are loaded successfully (if any values are defined for the given property
     *     name); false otherwise.
     */
    public static final <T> boolean loadList(GetProperties<T> get, String name, Collection<? super T> list) {
        if (get != null && get.getProperties(name, list)) {
            return true;
        }
        return false;
    }

    /**
     * <p>
     * Loads a property value with a given name, applies a regular expression Pattern split operation to the value, and
     * stores the individual resulting values in the given list.
     *
     * @param pattern
     *     The Pattern whose split method will be applied to the value of the property with the given name in order to
     *     split it into individual values. To indicate that the list elements are delimited by semi-colons, for
     *     example, the value
     *     <code>java.util.regex.Pattern.compile(";")</code>
     *     could be supplied as the argument for this parameter.
     * @return true if the values were successfully loaded into the list, and false otherwise. If the value of the
     *     property with the given name is defined but is the empty string (""), this method returns true even though
     *     the list is not actually modified.
     */
    public static final boolean loadList(GetProperty props, String name, Collection<? super String> list, Pattern pattern) {
        if (props != null && StringSplitUtil.splitString(props.getProperty(name), pattern, list)) {
            return true;
        }
        return false;
    }

    public static final void saveList(PutProperty props, String name, Iterable<?> list, boolean singleProperty) {
        saveList(props, name, list, singleProperty, NativeTypeConversion.DEFAULT_COLLECTION_TO_STRING_OPTIONS);
    }

    /**
     * Saves the values from a given list, either in a single property as a comma delimited list, or in a series of
     * individual properties, depending upon the value of the argument for the listType parameter:
     *
     * <p>
     * If the list type is LIST_SINGLE_PROPERTY or LIST_SINGLE_PROPERTY_ESCAPED, this method defers to
     * {@link #saveListSingleProperty(PutProperty, String, Iterable, NativeTypeConversion.CollectionToStringOptions)},
     * passing false and true, respectively, for the <tt>escaped</tt> parameter.
     *
     * <p>
     * If the list type is LIST_SEPARATE_PROPERTIES, or any other value, this method defers to
     * {@link #saveListSeparateProperties(PutProperty, String, Iterable,
     * NativeTypeConversion.CollectionToStringOptions)}.
     *
     * @param props
     *     in which the value or values are to be saved.
     * @param name
     *     the name of the list property to be saved.
     * @param list
     *     a Collection whose values are to be saved.
     * @param singleProperty
     *     indicates whether a property should be
     * @param options
     *     {@link NativeTypeConversion.CollectionToStringOptions} governing serialization of the list.
     */
    public static final void saveList(PutProperty props, String name, Iterable<?> list, boolean singleProperty, NativeTypeConversion.CollectionToStringOptions options) {
        if (singleProperty) {
            saveListSingleProperty(props, name, list, options);
        }
        else {
            saveListSeparateProperties(props, name, list, options);
        }
    }

    public static final void saveListSingleProperty(PutProperty props, String name, Iterable<?> list) {
        saveListSingleProperty(props, name, list, NativeTypeConversion.DEFAULT_COLLECTION_TO_STRING_OPTIONS);
    }

    public static final void saveListSingleProperty(PutProperty props, String name, Iterable<?> list, NativeTypeConversion.CollectionToStringOptions options) {
        saveString(props, name, NativeTypeConversion.iterableToString(list, options));
    }

    public static final void saveListSeparateProperties(PutProperty props, String name, Iterable<?> list, NativeTypeConversion.CollectionToStringOptions options) {

        if (list == null) {
            return;
        }

        int index = 0;
        if (list instanceof Collection) {
            saveInt(
                props,
                SimplePropertyNameMapper.ns(name, SEPARATE_PROPERTY_COUNT_PROPERTY_NAME_SUFFIX),
                ((Collection<?>) list).size()
            );
        }
        for (Object o : list) {
            saveString(props, StringUtils.defaultString(name) + (index++), o.toString());
        }
        if (!(list instanceof Collection<?>)) {
            saveInt(
                props,
                SimplePropertyNameMapper.ns(name, SEPARATE_PROPERTY_COUNT_PROPERTY_NAME_SUFFIX),
                index
            );
        }

    }

    public static final boolean loadMap(GetProperty props, String name, Map<? super String,? super String> map, boolean singleProperty) {
        if (props == null) {
            return false;
        }

        if (singleProperty) {
            return loadMapSingleProperty(props, name, map);
        }
        String prefix = name + SimplePropertyNameMapper.DEFAULT_SEPARATOR;
        for (String n : props.getPropertyNames()) {
            if (n.startsWith(prefix)) {
                map.put(n.substring(prefix.length()), props.getProperty(n));
            }
        }
        return true;
    }

    public static final boolean loadMapSingleProperty(GetProperty props, String name, Map<? super String,? super String> map) {
        return loadMapSingleProperty(
            props,
            name,
            map,
            NativeTypeConversion.DEFAULT_COLLECTION_TO_STRING_MAP_SPLIT_STRING_OPTIONS
        );
    }

    public static final boolean loadMapSingleProperty(GetProperty props, String name, Map<? super String,? super String> map, NativeTypeConversion.CollectionToStringOptions options) {
        if (props == null) {
            return false;
        }
        if (NativeTypeConversion.splitStringToMap(props.getProperty(name), map, options)) {
            return true;
        }
        return false;
    }

    public static final void saveMap(PutProperty props, String name, Map<?,?> map) {
        saveMap(props, name, map, NativeTypeConversion.DEFAULT_MAP_TO_STRING_OPTIONS);
    }

    public static final void saveMap(PutProperty props, String name, Map<?,?> map, NativeTypeConversion.MapToStringOptions options) {
        saveString(props, name, NativeTypeConversion.mapToString(map, options));
    }

    public static final Class<?> loadClass(GetProperty props, String name) {
        return loadClass(props, name, null);
    }

    public static final Class<?> loadClass(GetProperty props, String name, Class<?> defaultValue) {
        return NativeTypeConversion.stringToClass(loadString(props, name), defaultValue);
    }

    public static final <T> Class<? extends T> loadSubClass(GetProperty props, String name, Class<? extends T> superType) {
        return loadSubClass(props, name, superType, null);
    }

    public static final <T> Class<? extends T> loadSubClass(GetProperty props, String name, Class<? extends T> superType, Class<? extends T> defaultValue) {
        String classSpec = loadString(props, name, null);
        if (classSpec != null) {
            try {
                return Class.forName(classSpec).asSubclass(superType);
            }
            catch (ClassNotFoundException e) {
                LOGGER.warn("Failed to load class {}", classSpec, e);
            }
            catch (ClassCastException e) {
                LOGGER.warn("Failed to load class {} as subclass of {}", classSpec, superType.getName(), e);
            }
        }
        return defaultValue;
    }

    private static enum LoadFailureType {
        NO_SUCH_TARGET,
        ERROR
    }

    /**
     * Creates and returns an instance of a class named in a property from the given GetProperty with the given name, so
     * long as it is assignment compatible with a variable of the given required super-type, and so long as the named
     * Class has a visible nullary constructor.
     *
     * @param props
     *     the properties from which the name of the Class will be loaded.
     * @param name
     *     the name of the property that contains the specification for the Class to be instantiated.
     * @param superType
     *     the required super-type for the object to be instantiated.
     * @param defaultValue
     *     the default value to return in case no instance can be produced, for any reason.
     * @return the instance of the named Class or the default value if no valid Class can be identified and
     *     instantiated.
     * @see #loadSubClass(GetProperty, String, Class)
     */
    public static final <T> T loadInstance(GetProperty props, String name, Class<? extends T> superType, T defaultValue) {

        Class<? extends T> cls = loadSubClass(props, name, superType);
        if (cls == null) {
            return defaultValue;
        }

        // Prefer getInstance() if it's available.
        Throwable getInstanceError;
        LoadFailureType getInstanceFailureType;
        try {
            Method m = cls.getMethod("getInstance");
            if ((m.getModifiers() & Modifier.STATIC) == 0) {
                throw new NoSuchMethodException(cls + ".getInstance() (static)");
            }
            if (!superType.isAssignableFrom(m.getReturnType())) {
                throw new NoSuchMethodException(cls + ".getInstance() returning " + superType);
            }
            try {
                return superType.cast(m.invoke(null));
            }
            catch (IllegalAccessException |
                   InvocationTargetException |
                   ClassCastException |
                   ExceptionInInitializerError e) {
                getInstanceError = e;
                getInstanceFailureType = LoadFailureType.ERROR;
            }
        }
        catch (NoSuchMethodException | SecurityException e) {
            getInstanceError = e;
            getInstanceFailureType = LoadFailureType.NO_SUCH_TARGET;
        }

        Exception constructionError;
        LoadFailureType constructorFailureType;
        try {
            Constructor<? extends T> con = cls.getConstructor();
            try {
                return con.newInstance();
            }
            catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
                constructionError = e;
                constructorFailureType = LoadFailureType.ERROR;
            }
        }
        catch (NoSuchMethodException | SecurityException e) {
            constructionError = e;
            constructorFailureType = LoadFailureType.NO_SUCH_TARGET;
        }

        Throwable use;
        String message;
        if (constructorFailureType == LoadFailureType.ERROR) {
            use = constructionError;
            if (getInstanceFailureType == LoadFailureType.ERROR) {
                constructionError.addSuppressed(getInstanceError);
                message = "{} nullary constructor and getInstance both failed";
            }
            else {
                message = "{} nullary constructor failed";
            }
        }
        else if (getInstanceFailureType == LoadFailureType.NO_SUCH_TARGET) {
            use = getInstanceError;
            message = "{} getInstance failed, and no nullary constructor was available";
        }
        else {
            use = constructionError;
            constructionError.addSuppressed(getInstanceError);
            message = "No way to create an instance of {} (no getInstance method and no nullary constructor)";
        }

        LOGGER.warn(message, cls, use);
        return defaultValue;

    }

    public static final UUID loadUuid(GetProperty props, String propName) {
        return loadUuid(props, propName, null);
    }

    public static final UUID loadUuid(GetProperty props, String propName, UUID defaultValue) {
        String uuidSpec = loadString(props, propName);
        if (StringUtils.isBlank(uuidSpec)) {
            return defaultValue;
        }
        return NativeTypeConversion.stringToUuid(uuidSpec, defaultValue);
    }

    public static final void saveUuid(PutProperty props, String propName, UUID uuid) {
        if (uuid == null) {
            saveString(props, propName, null);
        }
        else {
            saveString(props, propName, uuid.toString());
        }
    }

    public static final <T> T loadProperty(GetProperty props, String propName, PropertyLoader<? extends T> loader) {
        return loadProperty(props, propName, loader, true);
    }

    public static final <T> T loadProperty(GetProperty props, String propName, PropertyLoader<? extends T> loader, boolean mayUseCache) {
        return loadProperty(props, propName, loader, mayUseCache, null);
    }

    public static final <T> T loadProperty(GetProperty props, String propName, PropertyLoader<? extends T> loader, boolean mayUseCache, T defaultValue) {
        if (props == null) {
            return defaultValue;
        }
        T fromStore = props.getProperty(propName, loader, mayUseCache);
        if (fromStore == null) {
            return defaultValue;
        }
        return fromStore;
    }

    public static final long loadDurationMillis(GetProperty props, String name, long defaultValue) {
        props = props.getNamespace(name);
        long millis = SimpleProperties.loadLong(props, null, Long.MIN_VALUE);
        if (millis != Long.MIN_VALUE) {
            return millis;
        }
        return SimpleDurationUnit.defaultLoadValueMillis(props, defaultValue);
    }

    public static final Duration loadDuration(GetProperty props, String name, Duration defaultValue) {
        props = props.getNamespace(name);
        long millis = SimpleProperties.loadLong(props, null, Long.MIN_VALUE);
        if (millis != Long.MIN_VALUE) {
            return Duration.ofMillis(millis);
        }
        return SimpleDurationUnit.defaultLoadValue(props, defaultValue);
    }

    public static final <E extends Enum<E>> E loadEnum(Configuration config, String name, Class<E> type, E defaultValue) {
        return EnumsUtil.enumFromString(type, loadString(config, name), defaultValue);
    }

}
