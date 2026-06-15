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

import com.tractionsoftware.commons.lang.StringUtil;
import jakarta.annotation.Nonnull;
import org.apache.commons.lang3.StringUtils;

import java.util.Objects;
import java.util.function.Function;
import java.util.function.UnaryOperator;

/**
 * @author Dave Shepperton
 */
public final class SimplePropertyNameMapper implements PropertyNameMapper {

    public static final String NORMAL_DEFAULT_SPACE_NAME = "default";

    public static final char DEFAULT_SEPARATOR = '_';

    /**
     * The default mapper for the "default" namespace, which is commonly used throughout TeamPage.
     */
    public static final PropertyNameMapper DEFAULT_NS =
        createNamespaceInstance(NORMAL_DEFAULT_SPACE_NAME, DEFAULT_SEPARATOR);

    public static final String defaultNs(String name) {
        return ns(NORMAL_DEFAULT_SPACE_NAME, name);
    }

    /**
     * Applies a transformation to the given property name to put it inside the given "namespace".
     *
     * @param space
     *     the namespace.
     * @param name
     *     the name to be namespaced.
     * @return the new namespaced version of the given property name.
     */
    public static final String ns(String space, String name) {
        return addPrefix(name, space, DEFAULT_SEPARATOR);
    }

    /**
     * Applies a transformation to the given property name to put it inside the given "namespace".
     *
     * @param space
     *     the namespace.
     * @param name
     *     the name to be namespaced.
     * @param sep
     *     the separator character.
     * @return the new namespaced version of the given property name.
     */
    public static final String ns(String space, String name, char sep) {
        return addPrefix(name, space, sep);
    }

    public static final String ns(Iterable<String> names) {
        return ns(names, DEFAULT_SEPARATOR);
    }

    public static final String ns(Iterable<String> names, char sep) {
        return StringUtil.join(names, sep);
    }

    public static final PropertyNameMapper getNamespaceInstanceWithDefaultSeparator(String space) {
        return getNamespaceInstanceWithSeparator(space, DEFAULT_SEPARATOR);
    }

    public static final PropertyNameMapper getNamespaceInstanceWithSeparator(String space, char separator) {
        return getNamespaceInstance(space, separator);
    }

    public static final PropertyNameMapper getNamespaceInstanceWithNoSeparator(String space) {
        return getNamespaceInstance(space, null);
    }

    public static final PropertyNameMapper getPrefixInstanceWithDefaultSeparator(String prefix) {
        return getPrefixInstanceWithSeparator(prefix, DEFAULT_SEPARATOR);
    }

    public static final PropertyNameMapper getPrefixInstanceWithSeparator(String prefix, char separator) {
        return getPrefixInstance(prefix, separator);
    }

    public static final PropertyNameMapper getPrefixInstanceWithNoSeparator(String prefix) {
        return getPrefixInstance(prefix, null);
    }

    private static final PropertyNameMapper getPrefixInstance(String prefix, Character separator) {
        if (StringUtils.isEmpty(prefix)) {
            return null;
        }
        return createPrefixInstance(prefix, separator);
    }

    private static final PropertyNameMapper createPrefixInstance(String prefix, Character separator) {
        return new SimplePropertyNameMapper(
            new RemovePrefixFunction(prefix, separator),
            new AddPrefixFunction(prefix, separator)
        );
    }

    private static final PropertyNameMapper getNamespaceInstance(String space, Character separator) {
        if (StringUtils.isEmpty(space)) {
            return null;
        }
        if (NORMAL_DEFAULT_SPACE_NAME.equals(space) &&
            Character.valueOf(DEFAULT_SEPARATOR).equals(separator)) {
            return DEFAULT_NS;
        }
        return createNamespaceInstance(space, separator);
    }

    private static final PropertyNameMapper createNamespaceInstance(String space, Character separator) {
        return new SimplePropertyNameMapper(
            new AddPrefixFunction(space, separator),
            new RemovePrefixFunction(space, separator)
        );
    }

    public static final String addPrefix(String str, String prefix) {
        return addPrefix(str, prefix, DEFAULT_SEPARATOR);
    }

    /**
     * Returns the prefix concatenated with the given String, separated with the given separator character (if one is
     * supplied).
     *
     * <p>
     * For prefix "foo", and default separator '_':
     *
     * <pre>
     * "bar" -> "foo_bar"
     * null  -> "foo"
     * ""    -> "foo"
     * </pre>
     *
     * <p>
     * For null or empty prefix (and any separator):
     *
     * <pre>
     * "bar" -> "bar"
     * null  -> null
     * ""    -> ""
     * </pre>
     *
     * @return the result of applying the given prefix, with the given separator character, if one is specified, to the
     *     given String.
     */
    public static final String addPrefix(String str, String prefix, Character separator) {

        if (StringUtils.isEmpty(prefix)) {
            return str;
        }

        if (StringUtils.isEmpty(str)) {
            return prefix;
        }

        if (separator == null) {
            return prefix + str;
        }

        return prefix + separator + str;

    }

    /**
     * Attempts to remove the given prefix and separator character, assuming it was applied the same way that
     * {@link #addPrefix(String, String, Character)} would do, from the given String, and returns the result. This
     * method returns null if the prefix is not present.
     *
     * <p>
     * For the prefix "foo" and default separator '_':
     *
     * <pre>
     * "foo_bar" -> "bar"
     * "foo"     -> ""
     * "foo_"    -> ""
     * "baz"     -> null
     * null      -> null
     * ""        -> null
     * </pre>
     *
     * @return the result of removing the prefix, with the given separator character (if one is specified), if they are
     *     present, from the beginning of the String; null otherwise.
     */
    public static final String removePrefix(String str, String prefix, Character separator) {

        if (StringUtils.isEmpty(prefix)) {
            return null;
        }

        // ""   -> null
        // null -> null
        if (StringUtils.isEmpty(str)) {
            return null;
        }

        if (separator == null) {
            // "foobar" -> "bar"
            if (str.startsWith(prefix)) {
                return str.substring(prefix.length());
            }
            // "baz" -> null (?)
            return null;
        }

        if (str.startsWith(prefix + separator)) {
            if (str.length() == prefix.length()) {
                return "";
            }
            return str.substring(prefix.length() + 1);
        }
        return null;

    }

    private static abstract class PrefixFunction implements UnaryOperator<String> {

        protected final String prefix;

        protected final Character separator;

        private PrefixFunction(String prefix, Character separator) {
            this.prefix = prefix;
            this.separator = separator;
        }

        @Override
        public final String toString() {
            StringBuilder ret = new StringBuilder();
            ret.append(type());
            ret.append(':');
            ret.append(prefix);
            if (separator != null) {
                ret.append(separator);
            }
            return ret.toString();
        }

        @Override
        public final boolean equals(Object other) {
            if (!(other instanceof PrefixFunction otherPrefixFunction)) {
                return false;
            }
            return
                Objects.equals(type(), otherPrefixFunction.type()) &&
                Objects.equals(prefix, otherPrefixFunction.prefix) &&
                Objects.equals(separator, otherPrefixFunction.separator);
        }

        @Override
        public final int hashCode() {
            return Objects.hash(type(), prefix, separator);
        }

        protected final String fqPrefix() {
            if (separator == null) {
                return prefix;
            }
            return prefix + separator;
        }

        public final boolean isInverseOf(PrefixFunction otherPrefixFunction) {
            if (type().equals(otherPrefixFunction.inverseType()) &&
                Objects.equals(prefix, otherPrefixFunction.prefix) &&
                Objects.equals(separator, otherPrefixFunction.separator)) {
                return true;
            }
            return false;
        }

        protected abstract String type();

        protected abstract String inverseType();

    }

    private static final class AddPrefixFunction extends PrefixFunction {

        private AddPrefixFunction(String prefix, Character separator) {
            super(prefix, separator);
        }

        @Override
        public final String apply(String name) {
            return addPrefix(name, prefix, separator);
        }

        @Override
        protected final String type() {
            return "+prefix";
        }

        @Override
        protected final String inverseType() {
            return "-prefix";
        }

        public final AddPrefixFunction combine(AddPrefixFunction otherAddPrefix) {
            return new AddPrefixFunction(fqPrefix() + otherAddPrefix.prefix, otherAddPrefix.separator);
        }

    }

    private static final class RemovePrefixFunction extends PrefixFunction {

        private RemovePrefixFunction(String prefix, Character separator) {
            super(prefix, separator);
        }

        @Override
        public final String apply(String name) {
            return removePrefix(name, prefix, separator);
        }

        @Override
        protected final String type() {
            return "-prefix";
        }

        @Override
        protected final String inverseType() {
            return "+prefix";
        }

        public final RemovePrefixFunction combine(RemovePrefixFunction otherAddPrefix) {
            return new RemovePrefixFunction(fqPrefix() + otherAddPrefix.prefix, otherAddPrefix.separator);
        }

    }

    private static final boolean areInverses(Function<String,String> f1, Function<String,String> f2) {
        if (f1 instanceof PrefixFunction) {
            if (f2 instanceof PrefixFunction) {
                return ((PrefixFunction) f1).isInverseOf((PrefixFunction) f2);
            }
        }
        return false;
    }

    private final Function<String,String> toActualPropertyName;

    private final Function<String,String> toPublishedName;

    private SimplePropertyNameMapper(Function<String,String> toActualPropertyName,
                                     Function<String,String> toPublishedName) {
        this.toActualPropertyName = toActualPropertyName;
        this.toPublishedName = toPublishedName;
    }

    @Override
    public final String toString() {
        return toActualPropertyName.toString();
    }

    @Override
    public final boolean equals(Object other) {
        if (!(other instanceof SimplePropertyNameMapper otherNameMapper)) {
            return false;
        }
        if (Objects.equals(toActualPropertyName, otherNameMapper.toActualPropertyName) &&
            Objects.equals(toPublishedName, otherNameMapper.toPublishedName)) {
            return true;
        }
        return false;
    }

    @Override
    public final int hashCode() {
        return Objects.hash(toActualPropertyName, toPublishedName);
    }

    @Override
    public final String getActualPropertyName(String requestedName) {
        return toActualPropertyName.apply(requestedName);
    }

    @Override
    public final String getPublishedName(String propertyName) {
        return toPublishedName.apply(propertyName);
    }

    @Override
    public final boolean isInverseOf(PropertyNameMapper otherNameMapper) {
        if (!(otherNameMapper instanceof SimplePropertyNameMapper otherSimpleNameMapper)) {
            return false;
        }
        if (areInverses(toActualPropertyName, otherSimpleNameMapper.toActualPropertyName) &&
            areInverses(toPublishedName, otherSimpleNameMapper.toPublishedName)) {
            return true;
        }
        return false;
    }

    @Nonnull
    @Override
    public final PropertyNameMapper compose(PropertyNameMapper otherNameMapper) {
        if (otherNameMapper == null) {
            return this;
        }
        if (otherNameMapper instanceof SimplePropertyNameMapper simple) {
            return composeImpl(simple);
        }
        return MultiPropertyNameMapper.createInstance(this, otherNameMapper);
    }

    private final PropertyNameMapper composeImpl(SimplePropertyNameMapper otherNameMapper) {
        if (toActualPropertyName instanceof AddPrefixFunction add &&
            toPublishedName instanceof RemovePrefixFunction rem &&
            otherNameMapper.toActualPropertyName instanceof AddPrefixFunction otherAdd &&
            otherNameMapper.toPublishedName instanceof RemovePrefixFunction otherRem) {
            return new SimplePropertyNameMapper(add.combine(otherAdd), rem.combine(otherRem));
        }
        if (toActualPropertyName instanceof RemovePrefixFunction rem &&
            toPublishedName instanceof AddPrefixFunction add &&
            otherNameMapper.toActualPropertyName instanceof RemovePrefixFunction otherRem &&
            otherNameMapper.toPublishedName instanceof AddPrefixFunction otherAdd) {
            return new SimplePropertyNameMapper(rem.combine(otherRem), add.combine(otherAdd));
        }
        return MultiPropertyNameMapper.createInstance(this, otherNameMapper);
    }

}
