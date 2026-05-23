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
import jakarta.annotation.Nonnull;

/**
 * A base {@link PropertyCollection} implementation for implementing
 * the decorator pattern. The {@link #delegate()} method returns the
 * PropertyCollection being decorated.
 *
 * <p>
 * It provides implementations of the {@link #getName()} and
 * {@link #fullyQualify(String)} methods that defer directly to the
 * underlying PropertyCollection. Other methods will use the default
 * implementations defined in the PropertyCollection interface.
 *
 * <p>
 * ForwardingPropertyCollection subclasses should maintain the
 * following guarantees:
 *
 * <ul>
 * <li><strong>Protected</strong>: methods should never return objects
 * that provide direct access to the delegate, or to other
 * PropertyCollection instances produced directly by the delegate.
 * This prevents clients from accessing possibly sensitive data
 * obtained from the delegate by performing a narrowing reference
 * conversion (i.e., a cast to a more specific subtype). For example,
 * for an instance representing a {@link GetProperty} whose delegate
 * is a {@link PropStore}, it should not be possible for clients to
 * access the write or commit methods on the delegate.</li>
 *
 * <li>
 * <strong>Current</strong>: methods that produce other
 * PropertyCollection instances based upon the delegate instance or
 * other PropertyCollections produced directly from the delegate
 * instance should use a dynamic forwarding pattern so that the
 * PropertyCollection instance actually used reflects the delegate
 * PropertyCollection at the time any of its methods are invoked. This
 * ensures that if the data backing the delegate changes, that the
 * resulting PropertyCollection reflects those changes.</li>
 * </ul>
 *
 * @author Dave Shepperton
 */
public abstract class ForwardingPropertyCollection extends ForwardingObject implements PropertyCollection {

    /**
     * Returns the {@link PropertyCollection} that should be used for
     * the implementation of any PropertyCollection methods.
     *
     * @return the {@link PropertyCollection} that should be used for
     *         the implementation of any PropertyCollection methods.
     */
    @Nonnull
    @Override
    protected abstract PropertyCollection delegate();

    @Nonnull
    @Override
    public String toString() {
        return "PropertyCollection: fwd {" + delegate() + "}";
    }

    @Override
    public String getName() {
        return delegate().getName();
    }

    @Override
    public String fullyQualify(String name) {
        return delegate().fullyQualify(name);
    }

}
