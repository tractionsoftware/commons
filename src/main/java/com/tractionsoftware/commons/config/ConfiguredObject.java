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

package com.tractionsoftware.commons.config;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.tractionsoftware.commons.lang.EnumUtil;
import com.tractionsoftware.commons.lang.StringUtil;
import com.tractionsoftware.commons.properties.AbstractGetPropertyLocator;
import com.tractionsoftware.commons.properties.GetProperty;
import com.tractionsoftware.commons.properties.SimpleProperties;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import org.apache.commons.lang3.StringUtils;

import java.text.MessageFormat;
import java.util.*;

/**
 * Something that is associated with a configuration, usually in the form of a configuration file. Access is provided to
 * its {@link Configuration}, and to other properties.
 *
 * @property display_name=
 *
 *     This optional property is used by the default implementation of the {@link #getDisplayName()} method to provide
 *     the display name for this Configurable as it appears in various contexts.
 * @property display_name_short=
 *
 *     This optional property is used by the default implementation of the {@link #getShortDisplayName()} method to
 *     provide a short version of the display name for this Configurable to be used in certain contexts.
 * @property description= and description_arg=
 *
 *     These optional properties are used by the default implementation of the {@link #getDescription()} method to
 *     provide the description text and arguments for any formatting parameters that may appear in the text.
 */
public interface ConfiguredObject extends Comparable<ConfiguredObject> {

    /**
     * The default name of the property that appears in a configuration (usually an individual .properties file, or an
     * object from a .json file) to indicate the specification for the class for which the file represents a
     * configuration.
     */
    public static final String PROP_NAME_CLASS = "class";

    public static final String DISPLAY_PROP_NAME_NAME = "name";

    public static final String PROP_NAME_DISPLAY_NAME = "display_name";

    public static final String PROP_NAME_DISPLAY_NAME_SHORT = "display_name_short";

    public static final String PROP_NAME_DESCRIPTION = "description";

    public static final String DESCRIPTION_ARG = "description_arg";

    /**
     * When supported, this property may be set to a comma separated list of names representing aliases for this
     * Configurables.
     */
    public static final String PROP_NAME_ALIASES = "aliases";

    public static final String PROP_NAME_PRIORITY = "priority";

    public static final int DEFAULT_PRIORITY = 0;

    public static final String PROP_NAMESPACE_PERFORMANCE_CONCERN_THRESHOLD = "performance_concern_threshold";

    public static interface DisplayProperties extends GetProperty {

        public String getDisplayName();

        public String getShortDisplayName();

        public String getDescription();

        /**
         * Returns a SequencedSet of aliases -- alternative logical names -- from the given {@link Configuration}.
         *
         * @return a SequencedSet of aliases -- alternative logical names -- for the given {@link Configuration}.
         */
        @Nonnull
        public SequencedSet<String> getAliases();

    }

    public static class LocalDisplayProperties implements DisplayProperties {

        protected final ConfiguredObject element;

        public LocalDisplayProperties(@Nonnull ConfiguredObject element) {
            Objects.requireNonNull(element, "Configurable");
            this.element = element;
        }

        @Nonnull
        @Override
        public String toString() {
            return "DisplayProperties for " + element;
        }

        @Override
        public String getProperty(String propName) {
            return switch (propName) {
                case DISPLAY_PROP_NAME_NAME -> element.getName();
                case PROP_NAME_DISPLAY_NAME -> getDisplayName();
                case PROP_NAME_DISPLAY_NAME_SHORT -> getShortDisplayName();
                case PROP_NAME_DESCRIPTION -> getDescription();
                case PROP_NAME_ALIASES -> StringUtil.join(getAliases(), ',');
                case null, default -> null;
            };
        }

        @Override
        public final Set<String> getPropertyNames() {
            return ImmutableSet.of(
                DISPLAY_PROP_NAME_NAME, PROP_NAME_DISPLAY_NAME, PROP_NAME_DISPLAY_NAME_SHORT, PROP_NAME_DESCRIPTION
            );
        }

        @Override
        public String getDisplayName() {
            return Objects.requireNonNullElseGet(
                SimpleProperties.loadString(element.getConfiguration(), PROP_NAME_DISPLAY_NAME), element::getName
            );
        }

        @Override
        public String getShortDisplayName() {
            return Objects.requireNonNullElseGet(
                SimpleProperties.loadString(element.getConfiguration(), PROP_NAME_DISPLAY_NAME_SHORT),
                this::getDisplayName
            );
        }

        @Override
        public String getDescription() {
            String description = SimpleProperties.loadString(element.getConfiguration(), PROP_NAME_DESCRIPTION);
            if (description != null && hasProperty(DESCRIPTION_ARG + "0")) {
                List<String> args = SimpleProperties.loadListSeparateProperties(
                    element.getConfiguration(), DESCRIPTION_ARG
                );
                return (new MessageFormat(description)).format(args.toArray());
            }
            return description;
        }

        @Nonnull
        @Override
        public SequencedSet<String> getAliases() {
            return SimpleProperties.loadSetSingleProperty(element.getConfiguration(), PROP_NAME_ALIASES);
        }

    }

    public static class DisplayPropertiesAll extends AbstractGetPropertyLocator<DisplayProperties>
        implements DisplayProperties {

        public static DisplayPropertiesAll createInstance(ConfiguredObject configured) {
            return new DisplayPropertiesAll(
                new LocalDisplayProperties(configured),
                configured.getConfiguration()
            );
        }

        public DisplayPropertiesAll(LocalDisplayProperties locals, Configuration all) {
            super(locals, all);
        }

        @Override
        public String getDisplayName() {
            return locals.getDisplayName();
        }

        @Override
        public String getShortDisplayName() {
            return locals.getShortDisplayName();
        }

        @Override
        public String getDescription() {
            return locals.getDescription();
        }

        @Nonnull
        @Override
        public SequencedSet<String> getAliases() {
            return locals.getAliases();
        }

    }

    public static final class ByIntegerProperty implements Comparator<ConfiguredObject> {

        private final String propName;

        private final int defaultValue;

        private final boolean descending;

        public ByIntegerProperty(String propName, int defaultValue, boolean descending) {
            this.propName = propName;
            this.defaultValue = defaultValue;
            this.descending = descending;
        }

        @Override
        public final int compare(ConfiguredObject c1, ConfiguredObject c2) {
            int ret = compareByIntPropValue(c1, c2);
            if (descending) {
                return -ret;
            }
            return ret;
        }

        private final int compareByIntPropValue(ConfiguredObject c1, ConfiguredObject c2) {
            return Integer.compare(
                SimpleProperties.loadInt(c1.getConfiguration(), propName, defaultValue),
                SimpleProperties.loadInt(c2.getConfiguration(), propName, defaultValue)
            );
        }

        @Override
        public final boolean equals(Object other) {
            if (this == other) {
                return true;
            }
            if (other instanceof ByIntegerProperty otherComparator) {
                if (propName == null) {
                    if (otherComparator.propName == null) {
                        return true;
                    }
                    return false;
                }
                if (otherComparator.propName == null) {
                    return false;
                }
                return propName.equals(otherComparator.propName);
            }
            return false;
        }

        @Override
        public final int hashCode() {
            return Objects.hash(propName);
        }

    }

    /**
     * Returns the elements in the map that are instances of the given test type. Since the test type can be an
     * interface, therefore not extending the main type, the test type is not bound.
     *
     * @param <C>
     *     narrows the allowable types for the {@link ConfiguredObject}s.
     * @param name2element
     *     the name-to-Configurable mappings to be filtered.
     * @param mainType
     *     the super-type for all the Configurables.
     * @param testType
     *     the sub-type for the desired Configurables.
     * @return the elements in the map that are instances of the given test type.
     */
    public static <C extends ConfiguredObject> List<C> getFilteredElementsByType(Map<String,C> name2element, Class<C> mainType, Class<?> testType) {
        List<C> ret = new ArrayList<>();
        for (C e : name2element.values()) {
            if (testType.isInstance(e)) {
                ret.add(e);
            }
        }
        return ret;
    }

    /**
     * Returns the value of the named property from the given {@link Configuration} if one is defined, and otherwise
     * throws a {@link ConfigurationException}.
     *
     * @param config
     *     the {@link Configuration} from which to load the property value.
     * @param name
     *     the name of the property to retrieve.
     * @return the value of the named property from the given {@link Configuration} if one is defined.
     * @throws ConfigurationException
     *     if the named property is not defined in the given {@link Configuration}.
     */
    public static String getRequiredProperty(Configuration config, String name) {
        String value = config.getProperty(name);
        if (value == null) {
            throw missingRequiredPropertyException(config, name);
        }
        return value;
    }

    /**
     * Returns the value of the named property from the given {@link Configuration} if one is defined and has a
     * non-blank value, and otherwise throws a {@link ConfigurationException}.
     *
     * @param config
     *     the {@link Configuration} from which to load the property value.
     * @param name
     *     the name of the property to retrieve.
     * @return the value of the named property from the given {@link Configuration} if one is defined and not blank.
     * @throws ConfigurationException
     *     if the named property is not defined in the given {@link Configuration}, or if its value is blank.
     */
    public static String getRequiredNonBlankProperty(Configuration config, String name) {
        String value = config.getProperty(name);
        if (StringUtils.isBlank(value)) {
            throw missingOrBlankRequiredPropertyException(config, name);
        }
        return value;
    }

    /**
     * Returns an Enum object of the given type, whose name corresponds to the named property from the given
     * {@link Configuration}, as long as the property is defined with a non-blank value, and a matching enum value can
     * be identified. Otherwise, it throws a {@link ConfigurationException}.
     *
     * @param config
     *     the {@link Configuration} from which to load the property value.
     * @param name
     *     the name of the property to retrieve.
     * @param type
     *     the type of Enum to be loaded.
     * @return an Enum object of the given type, whose name corresponds to the named property from the given
     *     {@link Configuration}, as long as the property is defined with a non-blank value, and a matching enum value
     *     can be identified
     * @throws ConfigurationException
     *     if the named property is not defined in the given {@link Configuration}, or if its value is blank or does not
     *     correspond to any of the values of the given enum.
     */
    public static <E extends Enum<E>> E getRequiredEnumPropertyValue(Configuration config, String name, Class<E> type) {
        String rawValue = getRequiredNonBlankProperty(config, name);
        E value = EnumUtil.enumFromString(type, rawValue, null);
        if (value == null) {
            throw noRecognizedEnumValueException(config, name, type, rawValue);
        }
        return value;
    }

    public static List<String> getRequiredNonEmptyListProperty(Configuration config, String name) {
        List<String> list = SimpleProperties.loadListSingleProperty(config, name);
        if (list.isEmpty()) {
            throw missingOrBlankRequiredPropertyException(config, name);
        }
        return ImmutableList.copyOf(list);
    }

    public static ConfigurationException missingRequiredPropertyException(Configuration config, String name) {
        return new ConfigurationException("The required property '" + config.fullyQualify(name) + "' is missing.");
    }

    public static ConfigurationException missingOrBlankRequiredPropertyException(Configuration config, String name) {
        return new ConfigurationException("The required property '" +
                                          config.fullyQualify(name) +
                                          "' is missing or empty/whitespace.");
    }

    public static ConfigurationException noRecognizedEnumValueException(Configuration config, String name, Class<? extends Enum<?>> type, String rawValue) {
        throw new ConfigurationException("No " +
                                         type +
                                         " value could be determined for the raw property " +
                                         config.fullyQualify(name) +
                                         "=" +
                                         rawValue +
                                         ".");
    }

    public static Comparator<ConfiguredObject> getIntegerPropertyComparator(String propName, int defaultValue, boolean descending) {
        return new ByIntegerProperty(propName, defaultValue, descending);
    }

    public static Comparator<ConfiguredObject> getAscendingPriorityOrderComparator() {
        return Comparator.comparingInt(ConfiguredObject::getPriority);
    }

    public static Comparator<ConfiguredObject> getDescendingPriorityOrderComparator() {
        return getAscendingPriorityOrderComparator().reversed();
    }

    /**
     * Returns a priority, with a higher value representing a higher priority, for this Configurable. The scale with
     * respect to which a priority value is produced will generally be dependent upon what type of Configurable is
     * involved, and is outside the specification of this method. Not all types of Configurables will include a built-in
     * concept of priority. But for those that do, the value should generally indicate a relative importance level so
     * that a family of Configurables of a particular type may be sorted (in ascending or descending order, as may be
     * appropriate) by priority.
     *
     * @return a priority, with a higher value representing a higher priority, for this Configurable.
     */
    public default int getPriority() {
        return SimpleProperties.loadInt(getConfiguration(), PROP_NAME_PRIORITY, DEFAULT_PRIORITY);
    }

    /**
     * Retrieves the {@link Configuration} associated with this Configurable, providing access to its configuration
     * properties.
     *
     * <p>
     * This implementation simply returns the Configuration object supplied to the constructor. Subclasses may override
     * this method if necessary, but it must never return null.
     *
     * @return the {@link Configuration} associated with this Configurable.
     */
    @Nonnull
    public Configuration getConfiguration();

    /**
     * Retrieves the name associated with this Configurable, which is usually the name of a configuration file (usually
     * a .properties file) in which the Configuration corresponding to this Configurable is stored (without the file
     * extension). Although Configurations can exist in different namespaces with the same name -- e.g., there can be a
     * skin whose configuration is named "simple" and a digest skin whose configuration is also named "simple" -- the
     * name retrieved here is not namespace qualified, as the namespace is usually known to the caller, and the name is
     * merely retrieved to identify the Configurable's Configuration within whatever namespace it is known to inhabit.
     *
     * @return the name to be used in configuration files to identify this Configurable.
     */
    public default String getName() {
        return getConfiguration().getName();
    }

    /**
     * Retrieves the user-friendly name that can be displayed to identify this Configurable. It should be sufficiently
     * specific that a user viewing the name somewhere in a user interface can unambiguously differentiate it from all
     * the others in the same collection.
     *
     * <p>
     * This default implementation returns uses the value of the display_name= configuration property, defaulting to
     * {@link #getName() the logical name of this Configurable}. Subclasses should override it as necessary.
     *
     * @return a user-friendly name that can be displayed to identify this Configurable.
     */
    public default String getDisplayName() {
        return getDisplayProperties().getDisplayName();
    }

    /**
     * Retrieves the short version of the user-friendly name that can be displayed to identify this Configurable. In
     * contrast to {@link #getDisplayName()}, this name does not necessarily need to be completely unique across all
     * Configurables in the collection to which this instance belongs. Some types of Configurables are only likely to
     * appear as part of a set of other elements in which a shorter display name is enough to uniquely identify it to
     * the viewing user. For types that only appear in, e.g., lists of all the elements in the collection, the short
     * display name would generally be identical to the full display name.
     *
     * <p>
     * This default implementation uses the value of the display_name_short= configuration property, defaulting to
     * {@link #getDisplayName()}. Subclasses should override it as necessary.
     *
     * @return the short version of a user-friendly name that can be displayed to identify this Configurable.
     */
    public default String getShortDisplayName() {
        return getDisplayProperties().getShortDisplayName();
    }

    /**
     * Returns a description of the Configurable, if one is available.
     *
     * @return a description of the Configurable, if one is available; null otherwise.
     */
    public default String getDescription() {
        return getDisplayProperties().getDescription();
    }

    /**
     * Represents the default ordering for Configurables in the same namespace, which is based upon a case-insensitive
     * alphabetical ordering of their display names. In case of display names with the same order, the plain names are
     * compared instead, which will not be equal unless either the Configurables really are the same, or the
     * Configurables come from two different namespaces and happen to have the same Configuration name. (Note that
     * Configurables from different namespaces are not generally -- nor should they be -- part of a single collection or
     * grouping for the reason that there may be two completely different Configurables from different namespaces that
     * have the same name, which would then be indistinguishable.)
     *
     * @param other
     *     the other {@link ConfiguredObject} to compare this to.
     */
    @Override
    public default int compareTo(@Nullable ConfiguredObject other) {
        if (other == null) {
            return -1;
        }
        int testCompare = String.CASE_INSENSITIVE_ORDER.compare(getDisplayName(), other.getDisplayName());
        if (testCompare != 0) {
            return testCompare;
        }
        testCompare = String.CASE_INSENSITIVE_ORDER.compare(getName(), other.getName());
        if (testCompare != 0) {
            return testCompare;
        }
        return String.CASE_INSENSITIVE_ORDER.compare(getClass().getName(), other.getClass().getName());
    }

    public default String defaultToString() {
        return getClass().getName() + ":" + getName();
    }

    /**
     * Returns a {@link DisplayProperties} that reflects display properties rather than raw properties.
     *
     * <p>
     * This default implementation returns a GetProperty which handles "name", "display_name", "description" and
     * "configuration_file_path" by deferring to {@link #getName()}, {@link #getDisplayName()},
     * {@link #getDescription()}, and {@link Configuration#getPath()}, respectively; and otherwise, which defers to the
     * {@link Configuration} provided by {@link #getConfiguration()}. Subclasses that offer more display properties
     * should override this method, most likely with an implementation that uses this implementation for its default
     * values (see {@link GetProperty#withDefaults(GetProperty)}).
     *
     * @return a {@link DisplayProperties} that reflects display properties rather than raw properties.
     */
    public default DisplayProperties getDisplayProperties() {
        return new DisplayPropertiesAll(new LocalDisplayProperties(this), getConfiguration());
    }

}
