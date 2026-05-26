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

import com.tractionsoftware.commons.properties.GetProperty;

import java.util.Map;
import java.util.Objects;

/**
 * An interface representing an object type along with an associated set of configuration properties.
 *
 * <p>
 * The properties may determine the specific type itself, and may modify various behaviors of the instances to which
 * they are applied.
 *
 * @author Dave Shepperton
 */
public interface Configuration extends GetProperty {

    /**
     * Returns the logical name for this configuration.
     *
     * @return the logical name for this configuration.
     */
    public String getName();

    /**
     * Returns the path to the file from which the Configuration properties were loaded, if applicable.
     */
    public String getPath();

    /**
     * Returns the templated setting values stored in the configuration, if present.
     *
     * @return the templated setting values stored in the configuration, if present; null otherwise.
     */
    public Map<String,String> getTemplateSettings();

    /**
     * Returns a Configuration object which is identical to this one, but whose {@link #getName()} method will return
     * the given name instead of this Configuration's name.
     *
     * <p>
     * If the requested name matches this Configuration's existing name, this method should generally return this
     * instance. Otherwise, a new instance may be created.
     *
     * <p>
     * This default implementation uses {@link ForwardingConfiguration#withNewName(Configuration, String)}, which should
     * be suitable for almost all cases.
     *
     * @param newName
     *     the requested new name.
     * @return a new Configuration object which is a copy of this one, using the given name in place of this instance's
     *     name.
     */
    public default Configuration withNewName(String newName) {
        if (Objects.equals(getName(), newName)) {
            return this;
        }
        return ForwardingConfiguration.withNewName(this, newName);
    }

    /**
     * This default implementation creates a {@link ConfigurationLocator}.
     */
    @Override
    public default Configuration withDefaults(GetProperty defaults) {
        return ConfigurationLocator.getSingleConfigurationOrLocator(this, defaults);
    }

    /**
     * Returns a view of this Configuration that is guaranteed to supply access to the Configuration API methods only,
     * or at least to methods that are deemed safe for general read-only access. For example, to pass a subclass of
     * Configuration to an alien method that accepts a Configuration, and to ensure that that method cannot cast the
     * argument back to the subtype in order to a to gain access to other sensitive methods, a client may invoke
     * toReadOnly on that object before passing it along to that method.
     *
     * <p>
     * This default implementation returns this Configuration itself, which is a suitable implementation for subclasses
     * that do not have any public methods beyond those declared in Configuration. Implementations that do have public
     * methods that should not be accessible to clients that arbitrary clients when a Configuration would do should
     * override it, perhaps using {@link ForwardingConfiguration#wrap(Configuration)}. Likewise, this method may be
     * re-declared in subclasses using a different return type if there is a more specific safe read-only subtype that
     * could be offered in place of Configuration, as this method itself overrides the declaration from
     * {@link GetProperty#toReadOnly()}.
     *
     * @return a view of this Configuration that is guaranteed to supply access to the Configuration API methods only,
     *     or at least to methods that are deemed safe for general read-only access.
     */
    @Override
    public default Configuration toReadOnly() {
        return this;
    }

    /**
     * This default implementation returns this Configuration instance itself, which should be suitable for all cases.
     */
    @Override
    public default Configuration asConfiguration() {
        return this;
    }

    /**
     * This implementation uses {@link PropertyNameMappingConfiguration#wrapInNamespace(Configuration, String)} , which
     * should be adequate for all Configuration implementations that do not need to return another specific sub-type of
     * Configuration.
     */
    @Override
    public default Configuration getNamespace(String space) {
        return PropertyNameMappingConfiguration.wrapInNamespace(this, space);
    }

    /**
     * This implementation uses {@link PropertyNameMappingConfiguration#wrapInNamespace(Configuration, String, char)} ,
     * which should be adequate for all Configuration implementations that do not need to return another specific
     * sub-type of Configuration.
     */
    @Override
    public default Configuration getNamespace(String space, char separator) {
        return PropertyNameMappingConfiguration.wrapInNamespace(this, space, separator);
    }

    /**
     * This implementation uses {@link PropertyNameMappingConfiguration#wrapInPrefix(Configuration, String)} , which
     * should be adequate for all Configuration implementations that do not need to return another specific sub-type of
     * Configuration.
     */
    @Override
    public default Configuration getPrefix(String prefix) {
        return PropertyNameMappingConfiguration.wrapInPrefix(this, prefix);
    }

    /**
     * This implementation uses {@link PropertyNameMappingConfiguration#wrapInPrefix(Configuration, String, char)} ,
     * which should be adequate for all GetProperty implementations that do not need to return another specific sub-type
     * of GetProperty.
     */
    @Override
    public default Configuration getPrefix(String prefix, char separator) {
        return PropertyNameMappingConfiguration.wrapInPrefix(this, prefix, separator);
    }
}
