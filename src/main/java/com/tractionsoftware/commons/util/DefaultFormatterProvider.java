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

import java.util.Formatter;
import java.util.Locale;
import java.util.Objects;

public final class DefaultFormatterProvider implements FormatterProvider {

    public static final DefaultFormatterProvider getInstance(Appendable out) {
        Objects.requireNonNull(out, "Appendable");
        return new DefaultFormatterProvider(out);
    }

    private final Appendable out;

    private Formatter formatter;

    private DefaultFormatterProvider(Appendable out) {
        this.out = out;
    }

    @Override
    public final Formatter getDefault() {
        if (formatter == null || !Objects.equals(formatter.locale(), Locale.getDefault())) {
            formatter = new Formatter(out);
        }
        return formatter;
    }

    @Override
    public final Formatter get(Locale locale) {
        if (formatter == null || !Objects.equals(formatter.locale(), locale)) {
            formatter = new Formatter(out, locale);
        }
        return formatter;
    }

}
