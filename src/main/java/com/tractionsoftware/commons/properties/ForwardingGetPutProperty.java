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

import java.util.Collection;

/**
 * A base {@link GetPutProperty} implementation for implementing the decorator pattern. The {@link #delegate()} method
 * returns the GetPutProperty being decorated.
 *
 * <p>
 * Methods that offer basic GetPutProperty functionality, such as {@link #getProperty(String)}, {@link
 * #putProperty(String, String)} and {@link #getName()}, defer directly the corresponding method of the backing
 * GetPutProperty.
 *
 * <p>
 * Methods that return GetPutProperty or GetProperty instances are guaranteed to never directly expose either the
 * delegate or any object obtained directly from the delegate. They are also guaranteed not to retrieve and use the
 * delegate instance until one of the basic methods is invoked. Each of these guarantees imply that such methods cannot
 * be implemented via direct deference to the delegate. The resulting implementations fall into two categories:
 *
 * <ul>
 * <li>methods that involve some sort of decoration of a GetPutProperty instance, such as {@link
 * #withDefaults(GetProperty)} and {@link #getNamespace(String)}. ForwardingGetPutProperty does not override these at
 * all, instead deferring to the default implementations from GetPutProperty, which already use wrappers, and by
 * definition do not have access to the delegate.
 *
 * <li>methods that must ultimately defer to the delegate. There are exactly two such methods: {@link #getDefaults()}
 * and {@link #getLocals()}. These implementations are inherited from {@link ForwardingGetProperty}.
 *
 * @author Dave Shepperton
 */
public abstract class ForwardingGetPutProperty extends ForwardingGetProperty implements GetPutProperty {

    /**
     * Returns the {@link GetPutProperty} that should be used for the implementation of any GetPutProperty methods.
     *
     * @return the {@link GetPutProperty} that should be used for the implementation of any GetPutProperty methods.
     */
    @Nonnull
    @Override
    protected abstract GetPutProperty delegate();

    @Nonnull
    @Override
    public String toString() {
        return "GetPutProperty: fwd {" + delegate() + "}";
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

    @Override
    public void appendToListProperty(String name, String value) {
        delegate().appendToListProperty(name, value);
    }

    @Override
    public void appendToProperty(String name, String value, String separator) {
        delegate().appendToProperty(name, value, separator);
    }

}
