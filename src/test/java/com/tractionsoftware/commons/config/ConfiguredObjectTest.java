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

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public final class ConfiguredObjectTest {

    /** Minimal Configuration using SimpleProperties.asConfiguration. */
    static Configuration config(String name, Map<String, String> props) {
        return SimpleProperties.asConfiguration(props, name);
    }

    static Configuration config(String name) {
        return SimpleProperties.getEmptyConfiguration(name);
    }

    /** Minimal ConfiguredObject backed by a Configuration. */
    static ConfiguredObject obj(Configuration cfg) {
        return () -> cfg;
    }

    // =====================================================================
    // getRequiredProperty
    // =====================================================================

    @Test
    void getRequiredProperty_present_returnsValue() {
        Configuration cfg = config("test", Map.of("foo", "bar"));
        assertEquals("bar", ConfiguredObject.getRequiredProperty(cfg, "foo"));
    }

    @Test
    void getRequiredProperty_missing_throwsConfigurationException() {
        Configuration cfg = config("test");
        assertThrows(ConfigurationException.class, () ->
            ConfiguredObject.getRequiredProperty(cfg, "missing"));
    }

    // =====================================================================
    // getRequiredNonBlankProperty
    // =====================================================================

    @Test
    void getRequiredNonBlankProperty_present_returnsValue() {
        Configuration cfg = config("test", Map.of("key", "value"));
        assertEquals("value", ConfiguredObject.getRequiredNonBlankProperty(cfg, "key"));
    }

    @Test
    void getRequiredNonBlankProperty_blank_throwsConfigurationException() {
        Configuration cfg = config("test", Map.of("key", "   "));
        assertThrows(ConfigurationException.class, () ->
            ConfiguredObject.getRequiredNonBlankProperty(cfg, "key"));
    }

    @Test
    void getRequiredNonBlankProperty_missing_throwsConfigurationException() {
        Configuration cfg = config("test");
        assertThrows(ConfigurationException.class, () ->
            ConfiguredObject.getRequiredNonBlankProperty(cfg, "missing"));
    }

    // =====================================================================
    // getRequiredEnumPropertyValue
    // =====================================================================

    enum Color { RED, GREEN, BLUE }

    @Test
    void getRequiredEnumPropertyValue_valid_returnsEnum() {
        Configuration cfg = config("test", Map.of("color", "RED"));
        assertEquals(Color.RED, ConfiguredObject.getRequiredEnumPropertyValue(cfg, "color", Color.class));
    }

    @Test
    void getRequiredEnumPropertyValue_invalid_throwsConfigurationException() {
        Configuration cfg = config("test", Map.of("color", "PURPLE"));
        assertThrows(ConfigurationException.class, () ->
            ConfiguredObject.getRequiredEnumPropertyValue(cfg, "color", Color.class));
    }

    @Test
    void getRequiredEnumPropertyValue_missing_throwsConfigurationException() {
        Configuration cfg = config("test");
        assertThrows(ConfigurationException.class, () ->
            ConfiguredObject.getRequiredEnumPropertyValue(cfg, "color", Color.class));
    }

    // =====================================================================
    // getRequiredNonEmptyListProperty
    // =====================================================================

    @Test
    void getRequiredNonEmptyListProperty_present_returnsList() {
        Configuration cfg = config("test", Map.of("items", "a,b,c"));
        List<String> list = ConfiguredObject.getRequiredNonEmptyListProperty(cfg, "items");
        assertNotNull(list);
        assertFalse(list.isEmpty());
    }

    @Test
    void getRequiredNonEmptyListProperty_empty_throwsConfigurationException() {
        Configuration cfg = config("test", Map.of("items", ""));
        assertThrows(ConfigurationException.class, () ->
            ConfiguredObject.getRequiredNonEmptyListProperty(cfg, "items"));
    }

    @Test
    void getRequiredNonEmptyListProperty_missing_throwsConfigurationException() {
        Configuration cfg = config("test");
        assertThrows(ConfigurationException.class, () ->
            ConfiguredObject.getRequiredNonEmptyListProperty(cfg, "items"));
    }

    // =====================================================================
    // getFilteredElementsByType
    // =====================================================================

    interface SpecialObject extends ConfiguredObject {}

    @Test
    void getFilteredElementsByType_filtersCorrectly() {
        ConfiguredObject plain = obj(config("plain"));
        SpecialObject special = () -> config("special");

        Map<String, ConfiguredObject> map = new LinkedHashMap<>();
        map.put("plain", plain);
        map.put("special", special);

        List<ConfiguredObject> filtered = ConfiguredObject.getFilteredElementsByType(
            map, ConfiguredObject.class, SpecialObject.class
        );
        assertEquals(1, filtered.size());
        assertSame(special, filtered.get(0));
    }

    @Test
    void getFilteredElementsByType_noMatches_returnsEmpty() {
        Map<String, ConfiguredObject> map = Map.of("plain", obj(config("plain")));
        List<ConfiguredObject> filtered = ConfiguredObject.getFilteredElementsByType(
            map, ConfiguredObject.class, SpecialObject.class
        );
        assertTrue(filtered.isEmpty());
    }

    // =====================================================================
    // getPriority / comparators
    // =====================================================================

    @Test
    void getPriority_noPriorityProp_returnsDefault() {
        assertEquals(ConfiguredObject.DEFAULT_PRIORITY, obj(config("test")).getPriority());
    }

    @Test
    void getPriority_withPriority_returnsValue() {
        Configuration cfg = config("test", Map.of(ConfiguredObject.PROP_NAME_PRIORITY, "5"));
        assertEquals(5, obj(cfg).getPriority());
    }

    @Test
    void getAscendingPriorityOrderComparator_ordersLowToHigh() {
        ConfiguredObject low = obj(config("low", Map.of(ConfiguredObject.PROP_NAME_PRIORITY, "1")));
        ConfiguredObject high = obj(config("high", Map.of(ConfiguredObject.PROP_NAME_PRIORITY, "10")));

        Comparator<ConfiguredObject> comp = ConfiguredObject.getAscendingPriorityOrderComparator();
        assertTrue(comp.compare(low, high) < 0);
        assertTrue(comp.compare(high, low) > 0);
        assertEquals(0, comp.compare(low, low));
    }

    @Test
    void getDescendingPriorityOrderComparator_ordersHighToLow() {
        ConfiguredObject low = obj(config("low", Map.of(ConfiguredObject.PROP_NAME_PRIORITY, "1")));
        ConfiguredObject high = obj(config("high", Map.of(ConfiguredObject.PROP_NAME_PRIORITY, "10")));

        Comparator<ConfiguredObject> comp = ConfiguredObject.getDescendingPriorityOrderComparator();
        assertTrue(comp.compare(low, high) > 0);
        assertTrue(comp.compare(high, low) < 0);
    }

    @Test
    void byIntegerProperty_comparator_ordersCorrectly() {
        ConfiguredObject a = obj(config("a", Map.of("score", "3")));
        ConfiguredObject b = obj(config("b", Map.of("score", "7")));
        Comparator<ConfiguredObject> comp = ConfiguredObject.getIntegerPropertyComparator("score", 0, false);
        assertTrue(comp.compare(a, b) < 0);
    }

    @Test
    void byIntegerProperty_descending_reversesOrder() {
        ConfiguredObject a = obj(config("a", Map.of("score", "3")));
        ConfiguredObject b = obj(config("b", Map.of("score", "7")));
        Comparator<ConfiguredObject> comp = ConfiguredObject.getIntegerPropertyComparator("score", 0, true);
        assertTrue(comp.compare(a, b) > 0);
    }

    @Test
    void byIntegerProperty_equals_samePropertyName() {
        var comp1 = new ConfiguredObject.ByIntegerProperty("x", 0, false);
        var comp2 = new ConfiguredObject.ByIntegerProperty("x", 5, true); // same propName → equal
        assertEquals(comp1, comp2);
        assertEquals(comp1.hashCode(), comp2.hashCode());
    }

    @Test
    void byIntegerProperty_notEquals_differentPropertyName() {
        var comp1 = new ConfiguredObject.ByIntegerProperty("x", 0, false);
        var comp2 = new ConfiguredObject.ByIntegerProperty("y", 0, false);
        assertNotEquals(comp1, comp2);
    }

    // =====================================================================
    // compareTo / defaultToString / getName / getDisplayName
    // =====================================================================

    @Test
    void compareTo_null_returnsNegative() {
        assertTrue(obj(config("alpha")).compareTo(null) < 0);
    }

    @Test
    void compareTo_alphabeticalByDisplayName() {
        ConfiguredObject a = obj(config("alpha"));
        ConfiguredObject b = obj(config("beta"));
        assertTrue(a.compareTo(b) < 0);
        assertTrue(b.compareTo(a) > 0);
    }

    @Test
    void defaultToString_containsName() {
        ConfiguredObject o = obj(config("myconfig"));
        assertTrue(o.defaultToString().contains("myconfig"), o.defaultToString());
    }

    @Test
    void getName_returnsConfigName() {
        assertEquals("myconfig", obj(config("myconfig")).getName());
    }

    @Test
    void getDisplayName_noDisplayNameProp_returnsName() {
        assertEquals("myconfig", obj(config("myconfig")).getDisplayName());
    }

    @Test
    void getDisplayName_withDisplayNameProp_returnsDisplayName() {
        Configuration cfg = config("myconfig", Map.of(ConfiguredObject.PROP_NAME_DISPLAY_NAME, "My Config"));
        assertEquals("My Config", obj(cfg).getDisplayName());
    }

    @Test
    void getShortDisplayName_noShortProp_fallsBackToDisplayName() {
        Configuration cfg = config("myconfig", Map.of(ConfiguredObject.PROP_NAME_DISPLAY_NAME, "Full Name"));
        assertEquals("Full Name", obj(cfg).getShortDisplayName());
    }

    @Test
    void missingRequiredPropertyException_message_containsName() {
        Configuration cfg = config("myconfig");
        ConfigurationException ex = ConfiguredObject.missingRequiredPropertyException(cfg, "thekey");
        assertTrue(ex.getMessage().contains("thekey"), ex.getMessage());
    }

}
