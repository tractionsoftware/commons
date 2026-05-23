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

import jakarta.annotation.Nonnull;
import org.jspecify.annotations.NonNull;

import java.util.Map;
import java.util.Set;
import java.util.function.Function;

/**
 * A base {@link GetProperty} implementation for implementing the decorator pattern. The {@link #delegate()} method
 * returns the GetProperty being decorated.
 *
 * <p>
 * Methods that offer basic GetProperty functionality, such as {@link #getProperty(String)}, {@link #getPropertyNames()}
 * and {@link #getName()}, defer directly the corresponding method of the backing GetProperty.
 *
 * <p>
 * Methods that return GetProperty instances are guaranteed to never directly expose either the delegate or any objects,
 * such as other GetProperty objects, obtained directly from the delegate. They are also guaranteed not to retrieve and
 * use the delegate instance until one of the basic methods is invoked. Each of these guarantees imply that such methods
 * cannot be implemented via direct deference to the delegate. The resulting implementations fall into two categories:
 *
 * <ul>
 * <li>
 * methods that involve some sort of decoration of a GetProperty instance, such as {@link #withDefaults(GetProperty)}
 * and {@link #getNamespace(String)}. ForwardingGetProperty does not override these at all, instead deferring to the
 * default implementations from GetProperty, which already use wrappers, and by definition do not have access to the
 * delegate.
 * </li>
 *
 * <li>
 * methods that must ultimately defer to the delegate. There are exactly two such methods: {@link #getDefaults()} and
 * {@link #getLocals()}. These should be implemented by wrapping either the result of invoking the corresponding method
 * on the delegate, or by wrapping a {@link java.util.function.Supplier} that defers to the corresponding method of the
 * delegate. The selection of the applicable approach should depend upon whether the wrapped instance is static or
 * dynamic, but should in any case maintain the "current" guarantee of ForwardingPropertyCollection.
 * </li>
 * </ul>
 *
 * @author Dave Shepperton
 */
public abstract class ForwardingGetProperty extends ForwardingPropertyCollection implements GetProperty {

    @Nonnull
    @Override
    public String toString() {
        return "GetProperty: fwd {" + delegate() + "}";
    }
    /**
     * Returns the {@link GetProperty} that should be used for the implementation of any GetProperty methods.
     *
     * @return the {@link GetProperty} that should be used for the implementation of any GetProperty methods.
     */
    @Nonnull
    @Override
    protected abstract GetProperty delegate();


    @Override
    public boolean getBooleanProperty(String name) {
        return delegate().getBooleanProperty(name);
    }

    @Override
    public boolean getBooleanProperty(String name, boolean defaultValue) {
        return delegate().getBooleanProperty(name, defaultValue);
    }

    @Override
    public int getIntProperty(String name) {
        return delegate().getIntProperty(name);
    }

    @Override
    public int getIntProperty(String name, int defaultValue) {
        return delegate().getIntProperty(name, defaultValue);
    }

    @Override
    public long getLongProperty(String name) {
        return delegate().getLongProperty(name);
    }

    @Override
    public long getLongProperty(String name, long defaultValue) {
        return delegate().getLongProperty(name, defaultValue);
    }

    @Override
    public double getDoubleProperty(String name) {
        return delegate().getDoubleProperty(name);
    }

    @Override
    public double getDoubleProperty(String name, double defaultValue) {
        return delegate().getDoubleProperty(name, defaultValue);
    }

    @Override
    public String getProperty(String name) {
        return delegate().getProperty(name);
    }

    @Override
    public <T> T getProperty(String name, PropertyLoader<? extends T> loader, boolean mayUseCache) {
        return delegate().getProperty(name, loader, mayUseCache);
    }

    @Override
    public boolean hasProperty(String name) {
        return delegate().hasProperty(name);
    }

    @Override
    public boolean hasNonBlankProperty(String name) {
        return delegate().hasNonBlankProperty(name);
    }

    @Override
    public boolean isEmpty() {
        return delegate().isEmpty();
    }

    @Override
    public Set<String> getPropertyNames() {
        return delegate().getPropertyNames();
    }

    @Override
    public Map<String,String> getAllProperties() {
        return delegate().getAllProperties();
    }

    @Override
    public Map<String,String> getProperties(Iterable<String> names) {
        return delegate().getProperties(names);
    }

    @Override
    public String getLocalProperty(String name) {
        return delegate().getLocalProperty(name);
    }

    @Override
    public final GetProperty getDefaults() {
        return createWrappedInstance(GetProperty::getDefaults);
    }

    @Override
    public final GetProperty getLocals() {
        return createWrappedInstance(GetProperty::getLocals);
    }

    public final GetProperty createWrappedInstance(Function<GetProperty,GetProperty> delegateMapper) {
        if (isStaticallySpecifiedDelegate()) {
            return delegateMapper.apply(delegate());
        }
        return DynamicForwardingGetProperty.wrap(() -> {
            return delegateMapper.apply(delegate());
        });
    }

    /**
     * This method should return true if the {@link #delegate()} method returns the value of a statically specified
     * {@link GetProperty} -- that is, if the same GetProperty instance will always be returned. This method exists to
     * ensure that certain methods can be optimized while still maintaining the "current" guarantee of the
     * {@link PropertyCollection} interface.
     *
     * <p>
     * This default implementation returns false, since by default
     *
     * @return true if the {@link #delegate()} method returns the value of a statically specified {@link GetProperty};
     *     false otherwise.
     */
    protected boolean isStaticallySpecifiedDelegate() {
        return false;
    }

}
