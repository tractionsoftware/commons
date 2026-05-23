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

import java.time.ZoneId;
import java.util.TimeZone;
import java.util.function.Supplier;

public final class TimeZoneUtil {

    private TimeZoneUtil() {
    }

    private static final Logger LOGGER = LoggerFactory.getLogger(TimeZoneUtil.class);

    private static final Supplier<? extends CurrentZoneProvider> currentZoneProvider = JavaUtil.lazyServiceLoader(
        CurrentZoneProvider.class, TimeZone::getDefault, LOGGER
    );

    public static interface CurrentZoneProvider {

        @Nonnull
        public TimeZone getZone();

        @Nonnull
        public default ZoneId getZoneId() {
            return getZone().toZoneId();
        }

        @Nonnull
        public default TimeZone getDefaultEnvironmentZone() {
            return getZone();
        }

    }

    public static final TimeZone getCurrentTimeZone() {
        return currentZoneProvider.get().getZone();
    }

    public static final ZoneId getCurrentZoneId() {
        return currentZoneProvider.get().getZoneId();
    }

    public static final TimeZone getEnvironmentDefaultTimeZone() {
        return currentZoneProvider.get().getDefaultEnvironmentZone();
    }

}
