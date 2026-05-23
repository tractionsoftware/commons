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

package com.tractionsoftware.commons.util;

/**
 * Represents a named type of date value. It provides a definition of the corresponding named value with respect to any
 * given Entry for both retrieving and storing {@link UTCDisplayDate} values.
 *
 * @author Dave Shepperton
 */
public interface DateType {

    /**
     * A DateType implementation suitable as a place-holder that does nothing and can neither produce nor store any
     * {@link UTCDisplayDate} values.
     */
    public static final DateType NONE = new DateType() {

        @Override
        public final String getName() {
            return "";
        }

        @Override
        public final boolean canBeAuthored() {
            return false;
        }

    };

    /**
     * Returns a name for this DateType, which should be unique across types.
     *
     * @return a name for this DateType, which should be unique across types.
     */
    public String getName();

    /**
     * Returns true if this DateType represents a sort of date value that can be authored, as opposed to DateTypes
     * representing intrinsic attributes of an object.
     *
     * @return true if this DateType represents a sort of date value that can be authored, as opposed to DateTypes
     *     representing intrinsic attributes of an object; false otherwise.
     */
    public boolean canBeAuthored();

}
