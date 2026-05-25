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

import com.tractionsoftware.commons.lang.NativeTypeConversion;
import com.tractionsoftware.commons.text.StringSplitUtil;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

/**
 * @author Dave Shepperton
 */
public class NativeTypeConversionTest {

    private static final <T> void doStringToListTest(String input, boolean escaped, Function<String,T> converter, String separator, List<T> expected) {
        List<T> actual = new ArrayList<>();
        NativeTypeConversion.stringToList(
            input, actual, converter, new NativeTypeConversion.CollectionToStringOptions() {
                @Override
                public boolean escapeValues() {
                    return escaped;
                }

                @Override
                public StringSplitUtil.Options getJoinOptions() {
                    return new StringSplitUtil.Options() {
                        @Override
                        public boolean escapeValues() {
                            return escaped;
                        }

                        @Override
                        public String getSeparator() {
                            return separator;
                        }
                    };
                }

            }
        );
        Assertions.assertEquals(expected, actual);
    }

    @Test
    public void testString2Integer1() {
        doStringToListTest(
            "255534,4421345,1934,23425235,211288,13412",
            true,
            NativeTypeConversion::stringToInt,
            StringSplitUtil.DEFAULT_STRING_LIST_SEPARATOR,
            List.of(255534, 4421345, 1934, 23425235, 211288, 13412)
        );
    }

    @Test
    public void testString2Short1() {
        doStringToListTest(
            "255534,4421345,1934,23425235,211288,13412",
            true,
            NativeTypeConversion::stringToShort,
            StringSplitUtil.DEFAULT_STRING_LIST_SEPARATOR,
            List.of(Short.MIN_VALUE, Short.MIN_VALUE, (short) 1934, Short.MIN_VALUE, Short.MIN_VALUE, (short) 13412)
        );
    }

    @Test
    public void testString2Short2() {
        doStringToListTest(
            "25553,4421,1934,23425,21128,13412",
            true,
            NativeTypeConversion::stringToShort,
            StringSplitUtil.DEFAULT_STRING_LIST_SEPARATOR,
            List.of((short) 25553, (short) 4421, (short) 1934, (short) 23425, (short) 21128, (short) 13412)
        );
    }

    @Test
    public void testString2Long1() {
        doStringToListTest(
            "",
            true,
            NativeTypeConversion::stringToLong,
            StringSplitUtil.DEFAULT_STRING_LIST_SEPARATOR,
            List.of()
        );
    }

    @Test
    public void testString2Long2() {
        doStringToListTest(
            null,
            true,
            NativeTypeConversion::stringToLong,
            StringSplitUtil.DEFAULT_STRING_LIST_SEPARATOR,
            List.of()
        );
    }

    @Test
    public void testString2Long3() {
        doStringToListTest(
            "-",
            true,
            NativeTypeConversion::stringToLong,
            StringSplitUtil.DEFAULT_STRING_LIST_SEPARATOR,
            List.of(Long.MIN_VALUE)
        );
    }

}
