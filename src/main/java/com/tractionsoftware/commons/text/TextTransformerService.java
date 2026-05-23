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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.Supplier;

public abstract class TextTransformerService {

    private static final Logger LOGGER = LoggerFactory.getLogger(TextTransformerService.class);

    private static final Supplier<? extends TextTransformerService> instance =
        JavaUtil.<TextTransformerService>lazyServiceLoader(
            TextTransformerService.class, TextTransformerService::defaultTextTransformerService, LOGGER
        );

    public static final TextTransformerService get() {
        return instance.get();
    }

    private static final TextTransformerService defaultTextTransformerService() {
        return new TextTransformerService() {

            @Override
            public final TextTransformer textExtractionSnippets() {
                return null;
            }

        };

    }

    public abstract TextTransformer textExtractionSnippets();

}
