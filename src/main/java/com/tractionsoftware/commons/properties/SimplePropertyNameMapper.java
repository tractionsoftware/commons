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
     * Returns the given prefix and its optional namespace separator character (if specified) concatenated with the
     * given String.
     *
     * <p>
     * Here are some example inputs and outputs:
     * <pre>
     * prefix = null, str = "bar" -> "bar"
     * prefix = "", str = "bar" -> "bar"
     * prefix = "foo", str = null -> "foo"
     * prefix = "foo", str = "" -> "foo"
     * prefix = "foo", separator = null, str = "bar" -> "foobar"
     * prefix = "foo", separator = "_", str = "bar" -> "foo_bar"
     * </pre>
     *
     * @return the result of applying the given prefix, with the given separator character, if one is specified, to the
     *     given String.
     * @see #removePrefix(String, String, Character)
     */
    public static final String addPrefix(String str, String prefix, Character separator) {

        // Nothing to add.
        // prefix = null, str = "bar" -> "bar"
        // prefix = "", str = "bar" -> "bar"
        if (StringUtils.isEmpty(prefix)) {
            return str;
        }

        // Nothing to add to.
        // prefix = "foo", str = null -> "foo"
        // prefix = "foo", str = "" -> "foo"
        if (StringUtils.isEmpty(str)) {
            return prefix;
        }

        // prefix = "foo", str = "bar" -> "foobar"
        if (separator == null) {
            return prefix + str;
        }

        // prefix = "foo", sep = "_", str = "bar" -> "foo_bar"
        return prefix + separator + str;

    }

    /**
     * Attempts to remove the given prefix with its optional namespace separator character from the given String, and
     * returns the result.
     *
     * <p>
     * This method is intended to be the inverse of {@link #addPrefix(String, String, Character)}. But if either the
     * prefix or the string itself is null or empty, the string is returned as-is.
     *
     * <p>
     * Here are some examples of inputs and outputs:
     *
     * <pre>
     * str = "" -> ""
     * str = null -> null
     * prefix = "", str = "anything" -> "anything"
     * prefix = null, str = "anything" -> "anything"
     * prefix = "foo", str = "foobar" -> "bar"; str = "foo" -> ""
     * prefix = "foo", str = "baz" -> null (?)
     * prefix = "foo", separator = ".", str = "foo.bar" -> "bar"
     * prefix = "foo", separator = ".", str = "baz_bar" -> null (?)
     * </pre>
     *
     * @return the result of attempting to remove the given prefix with its optional namespace separator character, from
     *     the beginning of the given string, if the prefix is present; a suitable fallback (as described above)
     *     otherwise.
     */
    public static final String removePrefix(String str, String prefix, Character separator) {

        // str = "" -> ""
        // str = null -> null
        // prefix = "", str = "anything" -> "anything"
        // prefix = null, str = "anything" -> "anything"
        if (StringUtils.isEmpty(prefix) || StringUtils.isEmpty(str)) {
            return str;
        }

        if (separator == null) {
            // prefix = "foo", str = "foobar" -> "bar"; str = "foo" -> ""
            if (str.startsWith(prefix)) {
                return str.substring(prefix.length());
            }
            // prefix = "foo", str = "baz" -> null (?)
            return null;
        }

        String prefixWithSeparator = prefix + separator;
        // prefix+sep = "foo_", str = "foo_bar" -> "bar"
        if (str.startsWith(prefixWithSeparator)) {
            return str.substring(prefixWithSeparator.length());
        }
        // prefix+sep = "foo_", str = "baz_bar" -> null (?)
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
