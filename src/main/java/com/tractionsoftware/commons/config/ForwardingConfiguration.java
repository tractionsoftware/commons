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
import java.util.function.Supplier;

import com.tractionsoftware.commons.properties.ForwardingGetProperty;
import com.tractionsoftware.commons.properties.GetProperty;
import jakarta.annotation.Nonnull;
import org.jspecify.annotations.NonNull;

/**
 * A base {@link Configuration} implementation for implementing the decorator pattern. The {@link #delegate()} method
 * returns the Configuration being decorated.
 *
 * <p>
 * Methods that offer basic Configuration functionality, such as {@link #getProperty(String)},
 * {@link #getPropertyNames()} and {@link #getName()}, defer directly the corresponding method of the backing
 * Configuration.
 *
 * <p>
 * Methods that return Configuration instances are guaranteed to never directly expose either the delegate or any object
 * obtained directly from the delegate. They are also guaranteed not to retrieve and use the delegate instance until one
 * of the basic methods is invoked. Each of these guarantees imply that such methods cannot be implemented via direct
 * deference to the delegate. The resulting implementations fall into two categories:
 *
 * <ul>
 * <li>methods that involve some sort of decoration of a Configuration
 * instance, such as {@link #withDefaults(GetProperty)} and
 * {@link #getNamespace(String)}. ForwardingConfiguration does not
 * override these at all, instead deferring to the default
 * implementations from Configuration, which already use wrappers, and
 * by definition do not have access to the delegate.
 *
 * <li>methods that must ultimately defer to the delegate. There are
 * exactly two such methods: {@link #getDefaults()} and
 * {@link #getLocals()}. These are implemented using
 * {@link #wrap(Supplier)} to wrap a Supplier that defers to the
 * corresponding method of the delegate to dynamically supply its own
 * delegate.
 *
 * @author Dave Shepperton
 */
public abstract class ForwardingConfiguration extends ForwardingGetProperty implements Configuration {

    /**
     * Creates a {@link Configuration} that defers to the given statically specified instance. This is perfect for
     * wrapping an object to ensure that only the Configuration functionality is exposed.
     *
     * @param props
     *     the {@link Configuration} to which the Configuration will defer.
     * @return a {@link Configuration} that defers to the given statically specified instance.
     */
    public static Configuration wrap(Configuration props) {
        return new StaticForwardingConfiguration(props);
    }

    /**
     * Creates a {@link Configuration} that defers to the Configuration provided by the given {@link Supplier}. This is
     * perfect for wrapping an object to ensure that only the Configuration functionality is exposed.
     *
     * @param provider
     *     the {@link Supplier} for the {@link Configuration} to which the returned Configuration will defer.
     * @return a {@link Configuration} that defers to the Configuration provided by the given {@link Supplier}.
     */
    public static final Configuration wrap(Supplier<? extends Configuration> provider) {
        return new DynamicForwardingConfiguration(provider);
    }

    public static final Configuration withNewName(Configuration config, final String newName) {
        if (config == null) {
            return null;
        }
        if (Objects.equals(config.getName(), newName)) {
            return config;
        }
        return new StaticForwardingConfiguration(config) {
            @Override
            public final String getName() {
                return newName;
            }
        };
    }

    /**
     * Returns the {@link Configuration} that should be used for the implementation of any Configuration methods.
     *
     * @return the {@link Configuration} that should be used for the implementation of any Configuration methods.
     */
    @Nonnull
    @Override
    protected abstract Configuration delegate();

    @Nonnull
    @Override
    public String toString() {
        return "Configuration: fwd {" + delegate() + "}";
    }

    @Override
    public String getPath() {
        return delegate().getPath();
    }

    @Override
    public Map<String,String> getTemplateSettings() {
        return delegate().getTemplateSettings();
    }

}
