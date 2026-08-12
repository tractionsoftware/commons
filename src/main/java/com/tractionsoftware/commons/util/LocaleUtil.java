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

import com.tractionsoftware.commons.lang.JavaUtil;
import jakarta.annotation.Nonnull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Locale;
import java.util.function.Supplier;

public final class LocaleUtil {

    private LocaleUtil() {
    }

    private static final Logger LOGGER = LoggerFactory.getLogger(LocaleUtil.class);

    private static final Supplier<? extends CurrentLocaleProvider> currentLocaleProvider =
        JavaUtil.<CurrentLocaleProvider>lazyServiceLoader(
            CurrentLocaleProvider.class, LocaleUtil::defaultCurrentLocaleProvider, LOGGER
        );

    public static interface CurrentLocaleProvider {

        @Nonnull
        public Locale getLocale();

        @Nonnull
        public default Locale getDefaultEnvironmentLocale() {
            return getLocale();
        }

    }

    public static final CurrentLocaleProvider defaultCurrentLocaleProvider() {
        return Locale::getDefault;
    }

    public static final Locale getCurrentLocale() {
        return currentLocaleProvider.get().getLocale();
    }

    public static final Locale getDefaultLocale() {
        return currentLocaleProvider.get().getDefaultEnvironmentLocale();
    }

}
