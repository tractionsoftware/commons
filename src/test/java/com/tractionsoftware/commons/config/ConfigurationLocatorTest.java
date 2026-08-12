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

package com.tractionsoftware.commons.config;

import com.tractionsoftware.commons.properties.SimpleProperties;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public final class ConfigurationLocatorTest {

    static Configuration cfg(String name, Map<String, String> props) {
        return SimpleProperties.asConfiguration(props, name);
    }

    static Configuration cfg(String name) {
        return SimpleProperties.getEmptyConfiguration(name);
    }

    // =====================================================================
    // getSingleConfigurationOrLocator
    // =====================================================================

    @Test
    void getSingleConfigurationOrLocator_nullDefaults_returnsLocals() {
        Configuration locals = cfg("main");
        assertSame(locals, ConfigurationLocator.getSingleConfigurationOrLocator(locals, null));
    }

    @Test
    void getSingleConfigurationOrLocator_withDefaults_returnsLocator() {
        Configuration locals = cfg("main");
        Configuration defaults = cfg("defaults", Map.of("key", "fromDefaults"));
        Configuration result = ConfigurationLocator.getSingleConfigurationOrLocator(locals, defaults);
        assertNotSame(locals, result);
        assertInstanceOf(ConfigurationLocator.class, result);
    }

    @Test
    void getSingleConfigurationOrLocator_nullLocals_throwsNPE() {
        assertThrows(NullPointerException.class, () ->
            ConfigurationLocator.getSingleConfigurationOrLocator(null, null));
    }

    // =====================================================================
    // Fallback lookups
    // =====================================================================

    @Test
    void getProperty_localFirst_localOverridesDefault() {
        Configuration locals = cfg("local", Map.of("key", "local-value"));
        Configuration defaults = cfg("defaults", Map.of("key", "default-value"));
        ConfigurationLocator locator = new ConfigurationLocator(locals, defaults);
        assertEquals("local-value", locator.getProperty("key"));
    }

    @Test
    void getProperty_missingInLocal_fallsBackToDefault() {
        Configuration locals = cfg("local");
        Configuration defaults = cfg("defaults", Map.of("key", "default-value"));
        ConfigurationLocator locator = new ConfigurationLocator(locals, defaults);
        assertEquals("default-value", locator.getProperty("key"));
    }

    @Test
    void getProperty_missingInBoth_returnsNull() {
        ConfigurationLocator locator = new ConfigurationLocator(cfg("local"), cfg("defaults"));
        assertNull(locator.getProperty("nonexistent"));
    }

    // =====================================================================
    // getName / getPath / getTemplateSettings
    // =====================================================================

    @Test
    void getName_returnsLocalsName() {
        Configuration locals = cfg("local-name");
        Configuration defaults = cfg("defaults");
        ConfigurationLocator locator = new ConfigurationLocator(locals, defaults);
        assertEquals("local-name", locator.getName());
    }

    @Test
    void getPath_returnsLocalsPath() {
        Configuration locals = cfg("local");
        Configuration defaults = cfg("defaults");
        ConfigurationLocator locator = new ConfigurationLocator(locals, defaults);
        // Both are empty configs so getPath should match locals
        assertEquals(locals.getPath(), locator.getPath());
    }

    @Test
    void getTemplateSettings_returnsLocalsTemplateSettings() {
        Configuration locals = cfg("local");
        Configuration defaults = cfg("defaults");
        ConfigurationLocator locator = new ConfigurationLocator(locals, defaults);
        assertEquals(locals.getTemplateSettings(), locator.getTemplateSettings());
    }

    // =====================================================================
    // toString
    // =====================================================================

    @Test
    void toString_containsConfiguration() {
        ConfigurationLocator locator = new ConfigurationLocator(cfg("local"), cfg("defaults"));
        String s = locator.toString();
        assertNotNull(s);
        assertTrue(s.contains("Configuration"), s);
    }

}
