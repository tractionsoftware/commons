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

public class StaticForwardingGetProperty extends ForwardingGetProperty {

    /**
     * Creates a {@link GetProperty} that defers to the given statically specified instance. This is perfect for
     * wrapping an object to ensure that only the GetProperty functionality is exposed.
     *
     * @param props
     *     the {@link GetProperty} to which the GetProperty will defer.
     * @return a {@link GetProperty} that defers to the given statically specified instance.
     */
    public static GetProperty wrap(GetProperty props) {
        return new StaticForwardingGetProperty(props);
    }

    protected final GetProperty props;

    public StaticForwardingGetProperty(GetProperty props) {
        this.props = props;
    }

    @Nonnull
    @Override
    public String toString() {
        return "GetProperty: stat fwd {" + props + "}";
    }

    @Nonnull
    @Override
    protected final GetProperty delegate() {
        return props;
    }

    @Override
    protected final boolean isStaticallySpecifiedDelegate() {
        return true;
    }

}
