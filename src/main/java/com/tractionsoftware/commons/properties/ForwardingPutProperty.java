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

import com.google.common.collect.ForwardingObject;
import com.tractionsoftware.commons.util.CollectionUtil;
import jakarta.annotation.Nonnull;

import java.util.Collection;
import java.util.Objects;
import java.util.function.Supplier;
import java.util.stream.Stream;

/**
 * A base {@link PutProperty} implementation for implementing the decorator pattern. The {@link #delegate()} method
 * returns the PutProperty being decorated.
 *
 * <p>
 * Methods that offer basic PutProperty functionality, such as {@link #putProperty(String, String)} and
 * {@link #getName()}, defer directly the corresponding method of the backing PutProperty.
 *
 * <p>
 * Methods that return other PutProperty instances are guaranteed to never directly expose either the delegate or any
 * object obtained directly from the delegate. They are also guaranteed not to retrieve and use the delegate instance
 * until one of the basic methods is invoked. Each of these guarantees imply that such methods cannot be implemented via
 * direct deference to the delegate. The resulting implementations fall into two categories:
 *
 * <ul>
 * <li>
 * methods that involve some sort of decoration of a PutProperty instance {@link #getNamespace(String)}.
 * ForwardingPutProperty does not override these at all, instead deferring to the default implementations from
 * GetProperty, which already use wrappers, and by definition do not have access to the delegate.
 * </li>
 *
 * <li>
 * methods that must ultimately defer to the delegate. These are implemented using {@link #wrap(Supplier)} to wrap a
 * Supplier that defers to the corresponding method of the delegate to dynamically supply its own delegate.
 * </li>
 *
 * </ul>
 *
 * @author Dave Shepperton
 */
public abstract class ForwardingPutProperty extends ForwardingObject implements PutProperty {

    public static class StaticForwardingPutProperty extends ForwardingPutProperty {

        protected final PutProperty props;

        public StaticForwardingPutProperty(PutProperty props) {
            this.props = props;
        }

        @Nonnull
        @Override
        public String toString() {
            return "PutProperty: stat fwd {" + props + "}";
        }

        @Nonnull
        @Override
        protected final PutProperty delegate() {
            return props;
        }

    }

    public static class DynamicForwardingPutProperty extends ForwardingPutProperty {

        protected final Supplier<? extends PutProperty> provider;

        public DynamicForwardingPutProperty(Supplier<? extends PutProperty> provider) {
            this.provider = provider;
        }

        @Nonnull
        @Override
        public String toString() {
            return "PutProperty dyn fwd {" + provider + " -> " + provider.get() + "}";
        }

        @Nonnull
        @Override
        protected final PutProperty delegate() {
            PutProperty delegate = provider.get();
            Objects.requireNonNull(delegate, "dynamic delegate PutProperty");
            return delegate;
        }

    }

    private static final class PropertyNameMappingPutProperty extends StaticForwardingPutProperty {

        private final PropertyNameMapper nameMapper;

        private PropertyNameMappingPutProperty(PutProperty props, PropertyNameMapper nameMapper) {
            super(props);
            this.nameMapper = nameMapper;
        }

        @Nonnull
        @Override
        public final String toString() {
            return "PutProperty name-mapper [" + nameMapper + "] {" + delegate() + "}";
        }

        @Override
        public final String fullyQualify(String name) {
            return super.fullyQualify(nameMapper.getActualPropertyName(name));
        }

        @Override
        public final void putProperty(String name, String value) {
            delegate().putProperty(nameMapper.getActualPropertyName(name), value);
        }

        @Override
        public final void putAllProperties(GetProperty source) {
            putProperties(source, null);
        }

        @Override
        public final void putProperties(GetProperty source, Collection<?> exceptions) {

            if (source == null) {
                return;
            }

            Stream<String> propNames = source.getPropertyNames().stream();
            if (CollectionUtil.isNotEmpty(exceptions)) {
                propNames = propNames.filter((propName) -> !exceptions.contains(propName));
            }
            propNames.forEach((propName) -> putProperty(propName, source.getProperty(propName)));

        }

    }

    /**
     * Creates a {@link PutProperty} that defers to the given statically specified instance. This is perfect for
     * wrapping an object to ensure that only the PutProperty functionality is exposed.
     *
     * @param props
     *     the {@link PutProperty} to which the GetProperty will defer.
     * @return a {@link PutProperty} that defers to the given statically specified instance.
     */
    public static PutProperty wrap(final PutProperty props) {
        return new StaticForwardingPutProperty(props);
    }

    /**
     * Creates a {@link PutProperty} that defers to the GetProperty provided by the given {@link Supplier}. This is
     * perfect for wrapping an object to ensure that only the GetProperty functionality is exposed.
     *
     * @param provider
     *     the {@link Supplier} for the {@link PutProperty} to which the returned PutProperty will defer.
     * @return a {@link PutProperty} that defers to the GetProperty provided by the given {@link Supplier}.
     */
    public static final PutProperty wrap(Supplier<? extends PutProperty> provider) {
        return new DynamicForwardingPutProperty(provider);
    }

    public static final PutProperty wrapInNamespace(PutProperty props, String space) {
        return applyPropertyNameMapper(props, SimplePropertyNameMapper.getNamespaceInstanceWithDefaultSeparator(space));
    }

    public static final PutProperty wrapInNamespace(PutProperty props, String space, char separator) {
        return applyPropertyNameMapper(
            props,
            SimplePropertyNameMapper.getNamespaceInstanceWithSeparator(space, separator)
        );
    }

    public static final PutProperty wrapInPrefix(PutProperty props, String space) {
        return applyPropertyNameMapper(props, SimplePropertyNameMapper.getPrefixInstanceWithDefaultSeparator(space));
    }

    public static final PutProperty wrapInPrefix(PutProperty props, String space, char separator) {
        return applyPropertyNameMapper(
            props, SimplePropertyNameMapper.getPrefixInstanceWithSeparator(space, separator)
        );
    }

    public static final PutProperty applyPropertyNameMapper(PutProperty props, PropertyNameMapper nameMapper) {
        if (nameMapper == null) {
            return props;
        }
        if (props == null) {
            return null;
        }
        if (props instanceof PropertyNameMappingPutProperty put) {
            if (put.nameMapper.isInverseOf(nameMapper)) {
                return put.delegate();
            }
            return new PropertyNameMappingPutProperty(props, put.nameMapper.compose(nameMapper));
        }
        return new PropertyNameMappingPutProperty(props, nameMapper);
    }

    @Nonnull
    @Override
    protected abstract PutProperty delegate();

    @Nonnull
    @Override
    public String toString() {
        return "PutProperty: fwd {" + delegate() + "}";
    }

    @Override
    public String getName() {
        return delegate().getName();
    }

    @Override
    public String fullyQualify(String name) {
        return delegate().fullyQualify(name);
    }

    @Override
    public void putProperty(String name, String value) {
        delegate().putProperty(name, value);
    }

    @Override
    public void putBooleanProperty(String name, boolean value) {
        delegate().putBooleanProperty(name, value);
    }

    @Override
    public void putIntProperty(String name, int value) {
        delegate().putIntProperty(name, value);
    }

    @Override
    public void putLongProperty(String name, long value) {
        delegate().putLongProperty(name, value);
    }

    @Override
    public void putDoubleProperty(String name, double value) {
        delegate().putDoubleProperty(name, value);
    }

    @Override
    public void removeProperty(String name) {
        delegate().removeProperty(name);
    }

    @Override
    public boolean clearLocalProperties() {
        return delegate().clearLocalProperties();
    }

    @Override
    public void putAllProperties(GetProperty source) {
        delegate().putAllProperties(source);
    }

    @Override
    public void putProperties(GetProperty source, Collection<?> exceptions) {
        delegate().putProperties(source, exceptions);
    }

}
