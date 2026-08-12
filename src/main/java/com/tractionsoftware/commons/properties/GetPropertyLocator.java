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

/**
 * @author Dave Shepperton
 */
public class GetPropertyLocator extends AbstractGetPropertyLocator<GetProperty> implements GetProperty {

    public static GetProperty getSingleGetPropertyOrLocator(GetProperty locals, GetProperty defaults) {
        if (locals == null) {
            return defaults;
        }
        if (defaults == null) {
            return locals;
        }
        return new GetPropertyLocator(locals, defaults);
    }

    GetPropertyLocator(GetProperty locals, GetProperty defaults) {
        super(locals, defaults);
    }

    @Nonnull
    @Override
    public String toString() {
        return "GetProperty: " + super.toString();
    }

}
