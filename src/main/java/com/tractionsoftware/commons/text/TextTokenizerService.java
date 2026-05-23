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

package com.tractionsoftware.commons.text;

import com.tractionsoftware.commons.lang.JavaUtil;
import com.tractionsoftware.commons.util.LocaleUtil;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.BreakIterator;
import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public abstract class TextTokenizerService {

    private static final Logger LOGGER = LoggerFactory.getLogger(TextTokenizerService.class);

    private static final Supplier<? extends TextTokenizerService> instance =
        JavaUtil.<TextTokenizerService>lazyServiceLoader(
            TextTokenizerService.class,
            TextTokenizerService::defaultTextTransformerService,
            LOGGER
        );

    private static final class BreakIteratorTokenIterator implements Iterator<String> {

        private static final BreakIteratorTokenIterator createInstance(String textOnlyContent, Locale locale) {
            Objects.requireNonNull(textOnlyContent, "text");
            Objects.requireNonNull(locale, "Locale");
            BreakIterator boundary = BreakIterator.getWordInstance(locale);
            boundary.setText(textOnlyContent);
            return new BreakIteratorTokenIterator(textOnlyContent, boundary);
        }

        private final String textOnlyContent;

        private final BreakIterator readyBreakIterator;

        private int start;

        private int end;

        private BreakIteratorTokenIterator(String textOnlyContent, BreakIterator readyBreakIterator) {
            this.textOnlyContent = textOnlyContent;
            this.readyBreakIterator = readyBreakIterator;
            start = 0;
            end = readyBreakIterator.first();
            if (end == 0) {
                end = readyBreakIterator.next();
            }
        }

        @Override
        public final boolean hasNext() {
            if (end == BreakIterator.DONE) {
                return false;
            }
            return true;
        }

        @Override
        public final String next() {
            if (end == BreakIterator.DONE) {
                throw new NoSuchElementException();
            }
            String token = textOnlyContent.substring(start, end);
            start = end;
            end = readyBreakIterator.next();
            return token;
        }

    }

    public static final TextTokenizerService get() {
        return instance.get();
    }

    private static final TextTokenizerService defaultTextTransformerService() {
        return new TextTokenizerService() {
            @Override
            protected final Iterator<String> getTokensImpl(String textOnlyContent) {
                return BreakIteratorTokenIterator.createInstance(textOnlyContent, LocaleUtil.getCurrentLocale());
            }
        };
    }

    public Stream<String> tokens(String textOnlyContent) {
        if (StringUtils.isBlank(textOnlyContent)) {
            return Stream.empty();
        }
        return StreamSupport.stream(
            Spliterators.spliteratorUnknownSize(getTokensImpl(textOnlyContent), Spliterator.ORDERED), false
        );
    }

    public final Iterable<String> getTokens(String textOnlyContent) {
        if (StringUtils.isBlank(textOnlyContent)) {
            return Collections.emptyList();
        }
        return () -> getTokensImpl(textOnlyContent);
    }

    protected abstract Iterator<String> getTokensImpl(String textOnlyContent);

}
