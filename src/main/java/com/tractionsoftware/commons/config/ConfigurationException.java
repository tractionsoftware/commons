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

import java.io.Serial;

public class ConfigurationException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = 3006693526768433933L;

    public static final ConfigurationException forInvalidPropertyValue(String name, String value) {
        throw new ConfigurationException(
            "The " +
            name +
            " property value (\"" +
            value +
            "\") does not appear to be a valid content-type specification."
        );
    }

    /**
     * Constructs a new ConfigurationException with no detail message.
     */
    public ConfigurationException() {
        super();
    }

    /**
     * Constructs a new ConfigurationException with the specified detail message.
     *
     * @param message
     *     the detail message. The detail message is saved for later retrieval by the {@link #getMessage()} method.
     */
    public ConfigurationException(String message) {
        super(message);
    }

    /**
     * Constructs a new ConfigurationException with the specified cause and a detail message of
     * <tt>(cause==null ? null : cause.toString())</tt> (which
     * typically contains the class and detail message of
     * <tt>cause</tt>). This constructor is useful for exceptions that
     * are little more than wrappers for other throwables.
     *
     * @param cause
     *     the underlying cause.
     */
    public ConfigurationException(Throwable cause) {
        super(cause);
    }

    /**
     * Constructs a new ConfigurationException with the specified detail message and cause.
     *
     * @param message
     *     the detail message.
     * @param cause
     *     the underlying cause.
     */
    public ConfigurationException(String message, Throwable cause) {
        super(message, cause);
    }

}
