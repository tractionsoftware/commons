/*
 *
 *    Copyright 1996-2025 Traction Software, Inc.
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

import com.tractionsoftware.commons.properties.ReadOnlyPropertyMap;

/**
 * An interface representing an object type along with an associated set of configuration properties.
 *
 * <p>
 * The properties may determine the specific type itself, and may modify various behaviors of the instances to which
 * they are applied.
 *
 * @author Dave Shepperton
 */
public interface NamedConfiguration {

    /**
     * Returns the logical name for this configuration.
     *
     * @return the logical name for this configuration.
     */
    public String getName();

    /**
     * Returns a display name for this configuration.
     *
     * <p>
     * This may be the same as {@link #getName()}; or it may come from some configuration property; or it may be
     * specified some other way. But this method should never return null.
     *
     * @return a display name for this configuration.
     */
    public String getDisplayName();

    /**
     * Returns an {@link ReadOnlyPropertyMap} providing raw access to this configuration's properties.
     *
     * @return an {@link ReadOnlyPropertyMap} providing raw access to this configuration's properties.
     */
    public ReadOnlyPropertyMap getProperties();

}
