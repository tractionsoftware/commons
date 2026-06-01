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

import com.google.common.annotations.Beta;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.util.Objects;
import java.util.function.Function;

/**
 * A representation of the transformation of some text.
 *
 * @author Andy Keller, Dave Shepperton
 */
@Beta
public interface TextTransformer {

    /**
     * Returns a String representing the result of applying the transformation to the input text.
     *
     * <p>
     * This default implementation creates a {@link StringBuilder} and then delegates to
     * {@link #transform(CharSequence, Appendable)}.
     *
     * <p>
     * For large quantities of text, clients should prefer {@link #transform(Reader, Writer)}.
     *
     * @param text
     *     the input text.
     * @return a String representing the transformed input text.
     */
    public default String transform(@Nullable CharSequence text) {
        if (StringUtils.isEmpty(text)) {
            return Objects.toString(text, null);
        }
        StringBuilder out = new StringBuilder();
        try {
            transform(text, out);
        }
        catch (IOException impossible) {
            // Impossible
        }
        catch (TextTransformationException e) {
            throw new RuntimeException(e);
        }
        return out.toString();
    }

    /**
     * Transforms the input text and writes the result to the given {@link Appendable}.
     *
     * @param text
     *     the input text.
     * @param out
     *     the destination for writing the result.
     * @throws IOException
     *     if there is a problem writing the result.
     */
    public void transform(@Nullable CharSequence text, @Nonnull Appendable out)
        throws IOException, TextTransformationException;

    /**
     * Transforms the input text from the given {@link Reader} and writes the result to the given {@link Writer}.
     *
     * @param in
     *     the source for the input text.
     * @param out
     *     the destination for the writing the result.
     * @throws IOException
     *     if there is a problem reading the input text or writing the result.
     */
    public void transform(@Nonnull Reader in, @Nonnull Writer out) throws IOException, TextTransformationException;

    /**
     * Returns a {@link Function} representing this instance's {@link TextTransformer#transform(CharSequence)} method as
     * a way to map a {@link CharSequence} to a (transformed) {@link String}.
     *
     * @return a {@link Function} representing this instance's {@link TextTransformer#transform(CharSequence)} method as
     *     a way to map a {@link CharSequence} to a (transformed) {@link String}.
     */
    public default Function<CharSequence,String> asFunction() {
        return this::transform;
    }

}
