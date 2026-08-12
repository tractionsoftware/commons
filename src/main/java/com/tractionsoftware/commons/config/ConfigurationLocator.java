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

import java.util.Map;
import java.util.Objects;

import com.tractionsoftware.commons.properties.AbstractGetPropertyLocator;
import com.tractionsoftware.commons.properties.GetProperty;
import jakarta.annotation.Nonnull;

/**
 * Like a PropLocator, a ConfigurationLocator allows property lookups via the getProperty method to fall back from one
 * store to another. This class provides an implementation of both the Configuration interface as well as an
 * implementation of PropLocator that in which a Configuration is either the primary source or the fall back source,
 * depending upon which constructor is used.
 */
public final class ConfigurationLocator extends AbstractGetPropertyLocator<Configuration> implements Configuration {

    public static final Configuration getSingleConfigurationOrLocator(Configuration locals, GetProperty defaults) {
        Objects.requireNonNull(locals, "main Configuration");
        if (defaults == null) {
            return locals;
        }
        return new ConfigurationLocator(locals, defaults);
    }

    public ConfigurationLocator(Configuration locals, GetProperty defaults) {
        super(locals, defaults);
    }

    @Nonnull
    @Override
    public final String toString() {
        return "Configuration: " + super.toString();
    }

    @Override
    public final String getPath() {
        return locals.getPath();
    }

    @Override
    public final Map<String,String> getTemplateSettings() {
        return locals.getTemplateSettings();
    }

}
