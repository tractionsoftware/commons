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

package com.tractionsoftware.commons.lang;

import com.google.common.annotations.Beta;
import com.google.common.base.Suppliers;
import com.tractionsoftware.commons.io.IOUtil;
import com.tractionsoftware.commons.text.CharBasedFilteringTextMapper;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;

import java.io.PrintWriter;
import java.lang.ref.Cleaner;
import java.util.*;
import java.util.function.Supplier;

@Beta
public final class JavaUtil {

    private JavaUtil() {
    }

    /**
     * A {@link Cleaner} that can be used application-wide. This hard reference ensures it will never become only
     * phantom-reachable, and will therefore never terminate.
     */
    public static final Cleaner RESOURCE_CLEANER = Cleaner.create();

    public static final String[] JAVA_LITERALS_UNENCODED = new String[] {
        "\n", "\r", "'", "\"", "\\"
    };

    public static final String[] JAVA_LITERALS_ENCODED = new String[] {
        "\\n", "\\r", "'", "\"", "\\"
    };

    public static final char QUALIFIER_CHAR = '.';

    public static Cleaner.Cleanable registerCloseCleanerAction(Object object, AutoCloseable closeMe) {
        return JavaUtil.RESOURCE_CLEANER.register(object, () -> IOUtil.close(closeMe));
    }

    public static int getApproximateInternalByteSize(String s) {
        if (s == null) {
            return 0;
        }
        // We estimate 8B of "static" overhead for the String (4B for
        // the char[], 4B the cached hash int); and n chars * 2B each.
        return 8 + (s.length() * 2);
    }

    public static int getApproximateInternalByteSize(Collection<String> list) {
        if (list == null) {
            return 0;
        }
        // We estimate 16B of "static" overhead of the collection's fields.
        // This can vary widely by implementation, so it is a rough
        // estimate at best.
        int ret = 16;
        for (String s : list) {
            ret += getApproximateInternalByteSize(s);
        }
        return ret;
    }

    public static int getApproximateInternalByteSize(Map<String,String> map) {
        if (map == null) {
            return 0;
        }
        // We estimate 16B of "static" overhead of the map's fields.
        // This can vary widely by implementation, so it is a rough
        // estimate at best.
        int ret = 16;
        for (Map.Entry<String,String> e : map.entrySet()) {
            // We estimate 16B of "static" overhead for each
            // Map.Entry. This can also vary widely with
            // implementation, so is also a rough estimate at best.
            ret += 16;
            ret += getApproximateInternalByteSize(e.getKey());
            ret += getApproximateInternalByteSize(e.getValue());
        }
        return ret;
    }

    public static final class CleanupTargetWrapper<T extends AutoCloseable> implements AutoCloseable {

        public static <T extends AutoCloseable> CleanupTargetWrapper<T> create(Object object, T instance) {
            return new CleanupTargetWrapper<>(instance, registerCloseCleanerAction(object, instance));
        }

        private T instance;

        private final Cleaner.Cleanable closer;

        private CleanupTargetWrapper(T instance, Cleaner.Cleanable closer) {
            this.instance = instance;
            this.closer = closer;
        }

        public T get() {
            return instance;
        }

        @Override
        public void close() {
            try {
                closer.clean();
            }
            finally {
                instance = null;
            }
        }

    }

    public enum CharacterRequiringEscaping {

        LINE_FEED('n'),

        CARRIAGE_RETURN('r'),

        DOUBLE_QUOTATION_MARK('"', true);

        public static CharacterRequiringEscaping get(char c) {
            return switch (c) {
                case '\n' -> LINE_FEED;
                case '\r' -> CARRIAGE_RETURN;
                case '"' -> DOUBLE_QUOTATION_MARK;
                default -> null;
            };
        }

        public static String getReplacement(char c) {
            CharacterRequiringEscaping value = get(c);
            if (value == null) {
                return null;
            }
            return value.getEscapeSequence();
        }

        private final String escapeSequence;

        private final boolean isQuotationMark;

        CharacterRequiringEscaping(char escapingChar) {
            this(escapingChar, false);
        }

        CharacterRequiringEscaping(char escapingChar, boolean isQuotationMark) {
            this.escapeSequence = "\\" + escapingChar;
            this.isQuotationMark = isQuotationMark;
        }

        @Override
        public final String toString() {
            return name() + " (" + escapeSequence + ")";
        }

        public final String getEscapeSequence() {
            return escapeSequence;
        }

        public final boolean isQuotationMark() {
            return isQuotationMark;
        }

    }

    public static String getStringLiteral(String str) {
        if (StringUtils.isEmpty(str)) {
            return str;
        }
        return CharBasedFilteringTextMapper.replace(str, CharacterRequiringEscaping::getReplacement);
    }

    public static void printStringLiteral(PrintWriter out, String str) {
        if (StringUtils.isNotEmpty(str)) {
            CharBasedFilteringTextMapper.replace(out, str, CharacterRequiringEscaping::getReplacement);
        }
    }

    public static final <T> Supplier<? extends T> lazyServiceLoader(Class<T> type, T defaultService, Logger logger) {
        return lazyServiceLoader(type, Suppliers.ofInstance(defaultService), logger);
    }

    public static final <T> Supplier<? extends T> lazyServiceLoader(Class<T> type, Supplier<? extends T> defaultService, Logger logger) {
        return Suppliers.memoize(() -> loadService(type, defaultService, logger));
    }

    public static final <T> T loadService(Class<T> type, T defaultService, Logger logger) {
        return loadService(type, Suppliers.ofInstance(defaultService), logger);
    }

    public static final <T> T loadService(Class<T> type, Supplier<? extends T> getDefault, Logger logger) {
        Objects.requireNonNull(type, "type");
        Objects.requireNonNull(logger, "logger");
        Optional<T> loaded;
        try {
            loaded = ServiceLoader.load(type).findFirst();
        }
        catch (Exception e) {
            logger.error("Failed to load type", e);
            loaded = Optional.empty();
        }
        if (getDefault == null) {
            return loaded.orElse(null);
        }
        return loaded.orElseGet(getDefault);
    }

}
