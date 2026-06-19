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

package com.tractionsoftware.commons.net;

import com.google.errorprone.annotations.CanIgnoreReturnValue;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

import java.net.MalformedURLException;
import java.util.Collection;
import java.util.Collections;

/**
 * A simple interface representation a mutable builder for a URL. This is intended for use with URL generators, and so
 * supports persisting certain parameters based on, e.g., a "current" URL.
 *
 * @author Dave Shepperton
 */
public interface URLBuilder<U> {

    /**
     * Returns the current value of the existing parameter with the given name.
     *
     * @param urlParameterName
     *     the name of the parameter whose value is to be retrieved.
     * @return the current value of the existing parameter with the given name, if one is present; null otherwise.
     * @throws NullPointerException
     *     if the argument for the parameter name is null.
     */
    @Nullable
    public String getParameterValue(@Nonnull String urlParameterName);

    /**
     * Returns a {@link Collection} containing the current values of the existing parameter with the given name. If the
     * parameter is undefined, implementations MUST return an empty Collection.
     *
     * <p>
     * It is up to the implementation what sort of Collection to use for this purpose, or whether to directly support
     * multiple values for a given parameter name at all; however, implementations not supporting multiple values for a
     * single parameter name MUST NOT return null for this method: if a single value is present, the result should be a
     * Collection containing only that value.
     *
     * @param urlParameterName
     *     the name of the parameter whose values are to be retrieved.
     * @return a {@link Collection} containing the current values of the parameter with the given name, if any values
     *     are present; an empty Collection otherwise.
     * @throws NullPointerException
     *     if the argument for the parameter name is null.
     */
    @Nonnull
    public Collection<String> getParameterValues(@Nonnull String urlParameterName);

    /**
     * Sets the value of the parameter with the given name to the new value. It is up to the implementation whether
     * setting a null value is equivalent to {@link #removeParameter(String)}.
     *
     * @param urlParameterName
     *     the name of the parameter to set.
     * @param value
     *     the new value for the named parameter.
     * @throws NullPointerException
     *     if the argument for the parameter name is null.
     * @throws UnsupportedOperationException
     *     if the implementation does not support modifying the named parameter.
     */
    public void setParameterValue(@Nonnull String urlParameterName, @Nullable String value);

    public void setParameterValues(@Nonnull String urlParameterName, @Nullable Collection<String> values);

    public default void addParameterValue(@Nonnull String urlParameterName, @Nullable String value) {
        if (value != null) {
            addParameterValues(urlParameterName, Collections.singleton(value));
        }
    }

    public void addParameterValues(@Nonnull String urlParameterName, @Nullable Collection<String> values);

    /**
     * Removes a parameter with the given name if it's present.
     *
     * @param urlParameterName
     *     the name of the parameter to be removed.
     * @return true if the parameter was present and removed; false otherwise.
     * @throws NullPointerException
     *     if the argument for the parameter name is null.
     * @throws UnsupportedOperationException
     *     if the implementation does not support modifying the named parameter.
     */
    @CanIgnoreReturnValue
    public boolean removeParameter(@Nonnull String urlParameterName);

    /**
     * Removes a single value from a multi-valued parameter value with the given name, if a parameter with that name is
     * present and its values contain the requested value.
     *
     * @param urlParameterName
     *     the name of the parameter to be removed.
     * @param removeValue
     *     the value to remove.
     * @return true if the parameter was present and removed; false otherwise.
     * @throws NullPointerException
     *     if the argument for the parameter name is null.
     * @throws UnsupportedOperationException
     *     if the implementation does not support modifying the named parameter.
     */
    @CanIgnoreReturnValue
    public boolean removeParameterValue(@Nonnull String urlParameterName, @Nullable String removeValue);

    /**
     * Allows an existing parameter with the given name should be kept when generating the URL.
     *
     * @param urlParameterName
     *     the name of the parameter to be kept.
     * @return true if the parameter was present; false otherwise.
     * @throws NullPointerException
     *     if the argument for the parameter name is null.
     * @throws UnsupportedOperationException
     *     if the implementation does not support keeping the named parameter.
     */
    @CanIgnoreReturnValue
    public boolean keepParameter(@Nonnull String urlParameterName);

    /**
     * Allows an existing parameter with the given name to "stick": i.e., to be kept indefinitely for future URLs
     * generated using the same process.
     *
     * @param urlParameterName
     *     the name of the parameter to "stick."
     * @return true if the parameter was present; false otherwise.
     * @throws NullPointerException
     *     if the argument for the parameter name is null.
     * @throws UnsupportedOperationException
     *     if the implementation does not support "sticking" the named parameter.
     */
    @CanIgnoreReturnValue
    public boolean stickParameter(@Nonnull String urlParameterName);

    public U build() throws MalformedURLException;

}
