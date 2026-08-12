package com.tractionsoftware.commons.codegen.java;

import com.google.common.annotations.Beta;
import com.tractionsoftware.commons.text.CharBasedFilteringTextMapper;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import org.apache.commons.lang3.StringUtils;

import java.io.PrintWriter;

/**
 * Helper methods for code that generates .java source files.
 *
 * @author Dave Shepperton
 */
@Beta
public final class JavaGeneratorStringUtil {

    private JavaGeneratorStringUtil() {
    }

    public static enum CharacterRequiringEscaping {

        LINE_FEED('n'),

        CARRIAGE_RETURN('r'),

        BACKSLASH('\\'),

        DOUBLE_QUOTATION_MARK('"', true);

        @Nullable
        public static final CharacterRequiringEscaping get(char c) {
            return switch (c) {
                case '\n' -> LINE_FEED;
                case '\r' -> CARRIAGE_RETURN;
                case '\\' -> BACKSLASH;
                case '"' -> DOUBLE_QUOTATION_MARK;
                default -> null;
            };
        }

        @Nullable
        public static final String getReplacement(char c) {
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

        @Nonnull
        @Override
        public final String toString() {
            return name() + " (" + escapeSequence + ")";
        }

        @Nonnull
        public final String getEscapeSequence() {
            return escapeSequence;
        }

        public final boolean isQuotationMark() {
            return isQuotationMark;
        }

    }

    public static final String getStringLiteral(@Nullable String str) {
        if (StringUtils.isEmpty(str)) {
            return str;
        }
        return CharBasedFilteringTextMapper.replace(str, CharacterRequiringEscaping::getReplacement);
    }

    public static final void printStringLiteral(PrintWriter out, String str) {
        if (StringUtils.isNotEmpty(str)) {
            CharBasedFilteringTextMapper.replace(str, out, CharacterRequiringEscaping::getReplacement);
        }
    }

}
