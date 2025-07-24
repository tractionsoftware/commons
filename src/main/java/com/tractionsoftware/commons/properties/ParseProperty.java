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

package com.tractionsoftware.commons.properties;

import com.tractionsoftware.commons.util.NativeTypeConversion;

/**
 * @author Dave Shepperton
 */
public final class ParseProperty {

    private ParseProperty() {
    }

    public static final String loadString(ReadOnlyPropertyMap get, String name) {
        if (get == null) {
            return null;
        }
        return get.getValue(name);
    }

    public static final String loadString(ReadOnlyPropertyMap get, String name, String defaultValue) {
        String ret = loadString(get, name);
        if (ret == null) {
            return defaultValue;
        }
        return ret;
    }

    public static final int loadInt(ReadOnlyPropertyMap get, String name) {
        return loadInt(get, name, -1);
    }

    public static final int loadInt(ReadOnlyPropertyMap get, String name, int defaultValue) {
        return NativeTypeConversion.stringToInt(loadString(get, name), defaultValue);
    }

    public static final long loadLong(ReadOnlyPropertyMap get, String name) {
        return loadLong(get, name, -1L);
    }

    public static final long loadLong(ReadOnlyPropertyMap get, String name, long defaultValue) {
        return NativeTypeConversion.stringToLong(loadString(get, name), defaultValue);
    }

    public static final double loadDouble(ReadOnlyPropertyMap get, String name) {
        return loadDouble(get, name, -1L);
    }

    public static final double loadDouble(ReadOnlyPropertyMap get, String name, double defaultValue) {
        return NativeTypeConversion.stringToDouble(loadString(get, name), defaultValue);
    }

}
