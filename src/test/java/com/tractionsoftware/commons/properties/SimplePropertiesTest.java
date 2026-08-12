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

package com.tractionsoftware.commons.properties;

import com.google.common.collect.ImmutableMap;
import com.tractionsoftware.commons.config.Configuration;
import com.tractionsoftware.commons.lang.NativeTypeConversion;
import com.tractionsoftware.commons.util.DateFormats;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.text.DateFormat;
import java.time.Duration;
import java.util.*;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.*;

public final class SimplePropertiesTest {

    private static GetProperty props(Map<String,String> map) {
        return SimpleProperties.asGetProperty(map);
    }

    private static GetProperty props(String key, String value) {
        return props(ImmutableMap.of(key, value));
    }

    private static final DateFormat yyyyMMddUTC() {
        return DateFormats.createSimpleDateFormat("yyyyMMdd", Locale.US, utc());
    }

    private static final TimeZone utc() {
        return TimeZone.getTimeZone("UTC");
    }

    // =====================================================================
    // emptyGetProperty / emptyConfiguration / getEmptyConfiguration
    // =====================================================================

    @Test
    void emptyGetProperty_returnsNull() {
        assertNull(SimpleProperties.emptyGetProperty().getProperty("x"));
    }

    @Test
    void emptyGetProperty_isEmpty() {
        assertTrue(SimpleProperties.emptyGetProperty().isEmpty());
    }

    @Test
    void emptyConfiguration_hasNoProperties() {
        Configuration cfg = SimpleProperties.emptyConfiguration();
        assertNotNull(cfg);
        assertNull(cfg.getProperty("anything"));
    }

    @Test
    void getEmptyConfiguration_setsName() {
        Configuration cfg = SimpleProperties.getEmptyConfiguration("myname");
        assertEquals("myname", cfg.getName());
    }

    // =====================================================================
    // asGetProperty / asConfiguration / asPropStore
    // =====================================================================

    @Test
    void asGetProperty_map_returnsValues() {
        GetProperty gp = SimpleProperties.asGetProperty(Map.of("k", "v"));
        assertEquals("v", gp.getProperty("k"));
    }

    @Test
    void asConfiguration_setsName() {
        Configuration cfg = SimpleProperties.asConfiguration(Map.of("a", "b"), "testcfg");
        assertEquals("testcfg", cfg.getName());
        assertEquals("b", cfg.getProperty("a"));
    }

    @Test
    void asPropStore_readWrite() {
        MapPropertyStore<Void> store = MapPropertyStore.createDefaultInstance();
        store.putProperty("x", "1");
        assertEquals("1", store.getProperty("x"));
    }

    // =====================================================================
    // loadString
    // =====================================================================

    @Test
    void loadString_present_returnsValue() {
        assertEquals("hello", SimpleProperties.loadString(props("k", "hello"), "k"));
    }

    @Test
    void loadString_absent_returnsNull() {
        assertNull(SimpleProperties.loadString(SimpleProperties.emptyGetProperty(), "k"));
    }

    @Test
    void loadString_withDefault_absent_returnsDefault() {
        assertEquals("dflt", SimpleProperties.loadString(SimpleProperties.emptyGetProperty(), "k", "dflt"));
    }

    @Test
    void loadString_withDefault_present_returnsValue() {
        assertEquals("val", SimpleProperties.loadString(props("k", "val"), "k", "dflt"));
    }

    @Test
    void loadTrimmedOrEmpty_trims() {
        assertEquals("hello", SimpleProperties.loadTrimmedOrEmpty(props("k", "  hello  "), "k"));
    }

    @Test
    void loadTrimmedOrEmpty_absent_returnsEmpty() {
        assertEquals("", SimpleProperties.loadTrimmedOrEmpty(SimpleProperties.emptyGetProperty(), "k"));
    }

    @Test
    void loadTrimmedOrNull_trims() {
        assertEquals("hello", SimpleProperties.loadTrimmedOrNull(props("k", "  hello  "), "k"));
    }

    @Test
    void loadTrimmedOrNull_blank_returnsNull() {
        assertNull(SimpleProperties.loadTrimmedOrNull(props("k", "   "), "k"));
    }

    // =====================================================================
    // loadInt / loadLong / loadBoolean / loadDouble
    // =====================================================================

    @Test
    void loadInt_valid_returnsInt() {
        assertEquals(42, SimpleProperties.loadInt(props("n", "42"), "n"));
    }

    @Test
    void loadInt_absent_returnsMinusOne() {
        assertEquals(-1, SimpleProperties.loadInt(SimpleProperties.emptyGetProperty(), "n"));
    }

    @Test
    void loadInt_withDefault_absent_returnsDefault() {
        assertEquals(99, SimpleProperties.loadInt(SimpleProperties.emptyGetProperty(), "n", 99));
    }

    @Test
    void loadLong_valid_returnsLong() {
        assertEquals(Long.MAX_VALUE, SimpleProperties.loadLong(props("n", String.valueOf(Long.MAX_VALUE)), "n", 0L));
    }

    @Test
    void loadBoolean_true_returnsTrue() {
        assertTrue(SimpleProperties.loadBoolean(props("b", "true"), "b"));
    }

    @Test
    void loadBoolean_false_returnsFalse() {
        assertFalse(SimpleProperties.loadBoolean(props("b", "false"), "b"));
    }

    @Test
    void loadBoolean_absent_returnsDefault() {
        assertFalse(SimpleProperties.loadBoolean(SimpleProperties.emptyGetProperty(), "b"));
        assertTrue(SimpleProperties.loadBoolean(SimpleProperties.emptyGetProperty(), "b", true));
    }

    @Test
    void loadDouble_valid_returnsDouble() {
        assertEquals(3.14, SimpleProperties.loadDouble(props("d", "3.14"), "d", 0.0), 0.001);
    }

    // =====================================================================
    // saveString / saveInt / saveBoolean
    // =====================================================================

    @Test
    void saveString_storesValue() {
        MapPropertyStore<Void> store = MapPropertyStore.createDefaultInstance();
        SimpleProperties.saveString(store, "k", "hello");
        assertEquals("hello", store.getProperty("k"));
    }

    @Test
    void saveString_unless_skipIfMatch() {
        MapPropertyStore<Void> store = MapPropertyStore.createDefaultInstance();
        SimpleProperties.saveString(store, "k", "hello", "hello"); // unless == value, so not saved
        assertNull(store.getProperty("k"));
    }

    @Test
    void saveInt_storesValue() {
        MapPropertyStore<Void> store = MapPropertyStore.createDefaultInstance();
        SimpleProperties.saveInt(store, "n", 7);
        assertEquals(7, SimpleProperties.loadInt(store, "n"));
    }

    @Test
    void saveBoolean_storesTrue() {
        MapPropertyStore<Void> store = MapPropertyStore.createDefaultInstance();
        SimpleProperties.saveBoolean(store, "flag", true);
        assertTrue(SimpleProperties.loadBoolean(store, "flag"));
    }

    // =====================================================================
    // loadListSingleProperty / loadSetSingleProperty
    // =====================================================================

    @Test
    void loadListSingleProperty_commaDelimited() {
        List<String> list = SimpleProperties.loadListSingleProperty(props("items", "a,b,c"), "items");
        assertEquals(3, list.size());
        assertTrue(list.contains("a"));
        assertTrue(list.contains("c"));
    }

    @Test
    void loadListSingleProperty_absent_returnsEmpty() {
        assertTrue(SimpleProperties.loadListSingleProperty(SimpleProperties.emptyGetProperty(), "items").isEmpty());
    }

    @Test
    void loadSetSingleProperty_deduplicates() {
        var set = SimpleProperties.loadSetSingleProperty(props("items", "a,b,a,c"), "items");
        assertEquals(3, set.size());
    }

    // =====================================================================
    // loadFirstString
    // =====================================================================

    @Test
    void loadFirstString_firstDefined_returnsFirst() {
        GetProperty gp = props("b", "from-b");
        String val = SimpleProperties.loadFirstString(gp, List.of("a", "b", "c"), "dflt");
        assertEquals("from-b", val);
    }

    @Test
    void loadFirstString_noneDefined_returnsDefault() {
        String val = SimpleProperties.loadFirstString(SimpleProperties.emptyGetProperty(), List.of("a", "b"), "dflt");
        assertEquals("dflt", val);
    }

    // =====================================================================
    // getName
    // =====================================================================

    @Test
    void getName_namedStore_returnsName() {
        MapPropertyStore<Void> store = MapPropertyStore.createNamedInstance("mystore", new HashMap<>());
        assertEquals("mystore", SimpleProperties.getName(store));
    }

    @Test
    void getName_anonymous_returnsGenericName() {
        assertEquals("Empty", SimpleProperties.getName(SimpleProperties.emptyGetProperty()));
    }

    // =====================================================================
    // getEmptyConfiguration / EmptyConfiguration
    // =====================================================================

    @Test
    void emptyConfiguration_getPath_returnsNull() {
        assertNull(SimpleProperties.getEmptyConfiguration("n").getPath());
    }

    @Test
    void emptyConfiguration_getTemplateSettings_returnsNull() {
        assertNull(SimpleProperties.getEmptyConfiguration("n").getTemplateSettings());
    }

    @Test
    void emptyConfiguration_getPropertyNames_returnsEmptySet() {
        assertTrue(SimpleProperties.getEmptyConfiguration("n").getPropertyNames().isEmpty());
    }

    @Test
    void emptyConfiguration_withNewName_sameName_returnsSameInstance() {
        Configuration cfg = SimpleProperties.getEmptyConfiguration("n");
        assertSame(cfg, cfg.withNewName("n"));
    }

    @Test
    void emptyConfiguration_withNewName_differentName_returnsNewInstance() {
        Configuration cfg = SimpleProperties.getEmptyConfiguration("n");
        Configuration renamed = cfg.withNewName("other");
        assertNotSame(cfg, renamed);
        assertEquals("other", renamed.getName());
    }

    @Test
    void emptyConfiguration_toString_includesLabel() {
        assertTrue(SimpleProperties.getEmptyConfiguration("n").toString().contains("EmptyConfiguration"));
    }

    // =====================================================================
    // asGetProperty (StaticMapGetProperty) / asPutProperty / asGetPutProperty / asImmutablePropStore
    // =====================================================================

    @Test
    void asGetProperty_named_toStringIncludesName() {
        GetProperty gp = SimpleProperties.asGetProperty(Map.of("k", "v"), "mygp");
        assertTrue(gp.toString().contains("mygp"));
    }

    @Test
    void asGetProperty_unnamed_toStringUsesGenericLabel() {
        GetProperty gp = SimpleProperties.asGetProperty(Map.of("k", "v"));
        assertTrue(gp.toString().contains("'map'"));
    }

    @Test
    void asGetProperty_backedByImmutableMap_readsCorrectly() {
        GetProperty gp = SimpleProperties.asGetProperty(ImmutableMap.of("k", "v"));
        assertEquals("v", gp.getProperty("k"));
    }

    @Test
    void asGetProperty_backedByMutableMap_readsCorrectly() {
        GetProperty gp = SimpleProperties.asGetProperty(new HashMap<>(Map.of("k", "v")));
        assertEquals("v", gp.getProperty("k"));
    }

    @Test
    void asPutProperty_writesToBackingMap() {
        Map<String,String> map = new HashMap<>();
        PutProperty put = SimpleProperties.asPutProperty(map);
        put.putProperty("k", "v");
        assertEquals("v", map.get("k"));
    }

    @Test
    void asGetPutProperty_readsAndWrites() {
        Map<String,String> map = new HashMap<>();
        GetPutProperty getPut = SimpleProperties.asGetPutProperty(map);
        getPut.putProperty("k", "v");
        assertEquals("v", getPut.getProperty("k"));
    }

    @Test
    void asImmutablePropStore_rejectsWrites() {
        Map<String,String> map = new HashMap<>();
        PropStore<Void> store = SimpleProperties.asImmutablePropStore(map);
        assertThrows(UnsupportedOperationException.class, () -> store.putProperty("k", "v"));
    }

    // =====================================================================
    // loadFilePath / loadFile / saveFilePath / saveFile
    // =====================================================================

    @Test
    void loadFilePath_convertsGenericSlashes() {
        String result = SimpleProperties.loadFilePath(props("path", "a/b/c"), "path", null);
        assertEquals("a" + File.separator + "b" + File.separator + "c", result);
    }

    @Test
    void loadFilePath_absent_returnsNotFound() {
        assertEquals("dflt", SimpleProperties.loadFilePath(SimpleProperties.emptyGetProperty(), "path", "dflt"));
    }

    @Test
    void loadFile_present_returnsFile() {
        File f = SimpleProperties.loadFile(props("path", "a/b"), "path");
        assertNotNull(f);
        assertEquals("a" + File.separator + "b", f.getPath());
    }

    @Test
    void loadFile_absent_returnsGivenDefault() {
        File defaultValue = new File("dflt");
        assertSame(defaultValue, SimpleProperties.loadFile(SimpleProperties.emptyGetProperty(), "path", defaultValue));
    }

    @Test
    void saveFilePath_convertsToGenericSlashes() {
        MapPropertyStore<Void> store = MapPropertyStore.createDefaultInstance();
        SimpleProperties.saveFilePath(store, "path", "a" + File.separator + "b");
        assertEquals("a/b", store.getProperty("path"));
    }

    @Test
    void saveFile_storesPlatformIndependentPath() {
        MapPropertyStore<Void> store = MapPropertyStore.createDefaultInstance();
        SimpleProperties.saveFile(store, "path", new File("a", "b"));
        assertEquals("a/b", store.getProperty("path"));
    }

    // =====================================================================
    // loadBase64Encoded / saveBase64Encoded
    // =====================================================================

    @Test
    void saveBase64Encoded_thenLoadBase64Encoded_roundTrips() {
        MapPropertyStore<Void> store = MapPropertyStore.createDefaultInstance();
        SimpleProperties.saveBase64Encoded(store, "secret", "hello world");
        assertNull(store.getProperty("secret"));
        assertNotNull(store.getProperty("secret_base64_encoded"));
        assertEquals("hello world", SimpleProperties.loadBase64Encoded(store, "secret", "notfound"));
    }

    @Test
    void saveBase64Encoded_nullValue_clearsBothProperties() {
        MapPropertyStore<Void> store = MapPropertyStore.createDefaultInstance();
        store.putProperty("secret", "plain");
        SimpleProperties.saveBase64Encoded(store, "secret", null);
        assertNull(store.getProperty("secret"));
        assertNull(store.getProperty("secret_base64_encoded"));
    }

    @Test
    void loadBase64Encoded_fallsBackToPlainProperty() {
        assertEquals("plain", SimpleProperties.loadBase64Encoded(props("secret", "plain"), "secret", "notfound"));
    }

    @Test
    void loadBase64Encoded_absent_returnsNotFound() {
        assertEquals(
            "notfound",
            SimpleProperties.loadBase64Encoded(SimpleProperties.emptyGetProperty(), "secret", "notfound")
        );
    }

    // =====================================================================
    // saveInt/saveLong/saveBoolean "unless" overloads, loadShort/saveShort, saveDouble
    // =====================================================================

    @Test
    void saveInt_unless_skipsWhenMatches() {
        MapPropertyStore<Void> store = MapPropertyStore.createDefaultInstance();
        SimpleProperties.saveInt(store, "n", 5, 5);
        assertNull(store.getProperty("n"));
    }

    @Test
    void saveInt_unless_storesWhenDifferent() {
        MapPropertyStore<Void> store = MapPropertyStore.createDefaultInstance();
        SimpleProperties.saveInt(store, "n", 5, 9);
        assertEquals(5, SimpleProperties.loadInt(store, "n"));
    }

    @Test
    void saveLong_storesValue() {
        MapPropertyStore<Void> store = MapPropertyStore.createDefaultInstance();
        SimpleProperties.saveLong(store, "n", 123456789012L);
        assertEquals(123456789012L, SimpleProperties.loadLong(store, "n"));
    }

    @Test
    void saveLong_unless_skipsWhenMatches() {
        MapPropertyStore<Void> store = MapPropertyStore.createDefaultInstance();
        SimpleProperties.saveLong(store, "n", 7L, 7L);
        assertNull(store.getProperty("n"));
    }

    @Test
    void loadShort_validAndAbsent() {
        assertEquals((short) 5, SimpleProperties.loadShort(props("n", "5"), "n"));
        assertEquals((short) -1, SimpleProperties.loadShort(SimpleProperties.emptyGetProperty(), "n"));
    }

    @Test
    void saveShort_storesValue() {
        MapPropertyStore<Void> store = MapPropertyStore.createDefaultInstance();
        SimpleProperties.saveShort(store, "n", (short) 5);
        assertEquals((short) 5, SimpleProperties.loadShort(store, "n"));
    }

    @Test
    void saveDouble_storesValue() {
        MapPropertyStore<Void> store = MapPropertyStore.createDefaultInstance();
        SimpleProperties.saveDouble(store, "d", 2.5);
        assertEquals(2.5, SimpleProperties.loadDouble(store, "d", 0.0), 0.0001);
    }

    @Test
    void saveBoolean_unless_skipsWhenMatches() {
        MapPropertyStore<Void> store = MapPropertyStore.createDefaultInstance();
        SimpleProperties.saveBoolean(store, "flag", true, true);
        assertNull(store.getProperty("flag"));
    }

    @Test
    void saveBoolean_unless_storesWhenDifferent() {
        MapPropertyStore<Void> store = MapPropertyStore.createDefaultInstance();
        SimpleProperties.saveBoolean(store, "flag", true, false);
        assertTrue(SimpleProperties.loadBoolean(store, "flag"));
    }

    // =====================================================================
    // saveDate / loadDate
    // =====================================================================

    @Test
    void saveDate_thenLoadDate_roundTrips() {
        MapPropertyStore<Void> store = MapPropertyStore.createDefaultInstance();
        Date date = new Date(1_700_000_000_000L);
        SimpleProperties.saveDate(store, "d", date);
        assertEquals(date, SimpleProperties.loadDate(store, "d", null));
    }

    @Test
    void saveDate_null_storesNothing() {
        MapPropertyStore<Void> store = MapPropertyStore.createDefaultInstance();
        SimpleProperties.saveDate(store, "d", null);
        assertNull(store.getProperty("d"));
    }

    @Test
    void loadDate_absent_returnsDefault() {
        Date defaultValue = new Date(0);
        assertEquals(defaultValue, SimpleProperties.loadDate(SimpleProperties.emptyGetProperty(), "d", defaultValue));
    }

    // =====================================================================
    // safeFormattedDate / loadFormattedDate
    // =====================================================================

    @Test
    void safeFormattedDate_formatsAsYyyyMMdd() {
        MapPropertyStore<Void> store = MapPropertyStore.createDefaultInstance();
        Date date = new Date(1_710_460_800_000L); // 2024-03-15T00:00:00Z
        SimpleProperties.saveFormattedDate(store, "d", date, SimplePropertiesTest::yyyyMMddUTC);
        assertEquals("20240315", store.getProperty("d"));
    }

    @Test
    void safeFormattedDate_null_storesNull() {
        MapPropertyStore<Void> store = MapPropertyStore.createDefaultInstance();
        store.putProperty("d", "placeholder");
        SimpleProperties.saveFormattedDate(store, "d", null, SimplePropertiesTest::yyyyMMddUTC);
        assertNull(store.getProperty("d"));
    }

    @Test
    void loadFormattedDate_roundTrips() {
        MapPropertyStore<Void> store = MapPropertyStore.createDefaultInstance();
        store.putProperty("d", "20240315");
        Date loaded = SimpleProperties.loadFormattedDate(store, "d", SimplePropertiesTest::yyyyMMddUTC);
        assertNotNull(loaded);
        assertEquals(1_710_460_800_000L, loaded.getTime());
    }

    @Test
    void loadFormattedDate_absent_returnsDefault() {
        Date defaultValue = new Date(0);
        assertEquals(
            defaultValue,
            SimpleProperties.loadFormattedDate(
                SimpleProperties.emptyGetProperty(), "d", SimplePropertiesTest::yyyyMMddUTC, defaultValue
            )
        );
    }

    @Test
    void loadFormattedDate_blank_returnsDefault() {
        Date defaultValue = new Date(123);
        assertEquals(
            defaultValue,
            SimpleProperties.loadFormattedDate(props("d", "   "), "d", SimplePropertiesTest::yyyyMMddUTC, defaultValue)
        );
    }

    @Test
    void loadFormattedDate_unparseable_returnsDefault() {
        Date defaultValue = new Date(123);
        Date result =
            SimpleProperties.loadFormattedDate(
                props("d", "not-a-date"),
                "d",
                SimplePropertiesTest::yyyyMMddUTC,
                defaultValue
            );
        assertEquals(defaultValue, result);
    }

    // =====================================================================
    // loadSplitString
    // =====================================================================

    @Test
    void loadSplitString_singlePropertyPresent_returnsItDirectly() {
        assertEquals("a,b,c", SimpleProperties.loadSplitString(props("items", "a,b,c"), "items", "+", "nf"));
    }

    @Test
    void loadSplitString_separatePropertiesPresent_joinsWithGivenSeparator() {
        MapPropertyStore<Void> store = MapPropertyStore.createDefaultInstance();
        store.putProperty("items0", "a");
        store.putProperty("items1", "b");
        assertEquals("a+b", SimpleProperties.loadSplitString(store, "items", "+", "nf"));
    }

    @Test
    void loadSplitString_nothingDefined_returnsNotFound() {
        assertEquals("nf", SimpleProperties.loadSplitString(SimpleProperties.emptyGetProperty(), "items", "+", "nf"));
    }

    // =====================================================================
    // separate-properties family: loadCollectionSeparateProperties / hasCollectionSeparateProperties
    // / loadListSeparateProperties / loadSetSeparateProperties
    // =====================================================================

    @Test
    void loadCollectionSeparateProperties_boundedCount_skipsGapsWithoutStopping() {
        MapPropertyStore<Void> store = MapPropertyStore.createDefaultInstance();
        store.putProperty("items_count", "3");
        store.putProperty("items0", "a");
        store.putProperty("items2", "c"); // items1 intentionally absent
        List<String> list = new ArrayList<>();
        boolean found = SimpleProperties.loadCollectionSeparateProperties(store, "items", list);
        assertTrue(found);
        assertEquals(List.of("a", "c"), list);
    }

    @Test
    void loadCollectionSeparateProperties_unbounded_stopsAtFirstGap() {
        MapPropertyStore<Void> store = MapPropertyStore.createDefaultInstance();
        store.putProperty("items0", "x");
        store.putProperty("items1", "y");
        store.putProperty("items3", "z"); // unreachable: iteration stops at items2 (absent)
        List<String> list = new ArrayList<>();
        boolean found = SimpleProperties.loadCollectionSeparateProperties(store, "items", list);
        assertTrue(found);
        assertEquals(List.of("x", "y"), list);
    }

    @Test
    void loadCollectionSeparateProperties_nothingDefined_returnsFalse() {
        List<String> list = new ArrayList<>();
        assertFalse(SimpleProperties.loadCollectionSeparateProperties(
            SimpleProperties.emptyGetProperty(),
            "items",
            list
        ));
        assertTrue(list.isEmpty());
    }

    @Test
    void loadCollectionSeparateProperties_nullPropsOrList_returnsFalse() {
        assertFalse(SimpleProperties.loadCollectionSeparateProperties(null, "items", new ArrayList<>()));
        assertFalse(SimpleProperties.loadCollectionSeparateProperties(
            SimpleProperties.emptyGetProperty(),
            "items",
            null
        ));
    }

    @Test
    void hasCollectionSeparateProperties_countZero_returnsFalse() {
        MapPropertyStore<Void> store = MapPropertyStore.createDefaultInstance();
        store.putProperty("items_count", "0");
        assertFalse(SimpleProperties.hasCollectionSeparateProperties(store, "items"));
    }

    @Test
    void hasCollectionSeparateProperties_countPositive_checksForAnyDefinedIndex() {
        MapPropertyStore<Void> store = MapPropertyStore.createDefaultInstance();
        store.putProperty("items_count", "2");
        store.putProperty("items1", "b"); // items0 absent, items1 present
        assertTrue(SimpleProperties.hasCollectionSeparateProperties(store, "items"));
    }

    @Test
    void hasCollectionSeparateProperties_noCount_checksFirstElement() {
        MapPropertyStore<Void> store = MapPropertyStore.createDefaultInstance();
        store.putProperty("items0", "a");
        assertTrue(SimpleProperties.hasCollectionSeparateProperties(store, "items"));
    }

    @Test
    void hasCollectionSeparateProperties_absent_returnsFalse() {
        assertFalse(SimpleProperties.hasCollectionSeparateProperties(SimpleProperties.emptyGetProperty(), "items"));
    }

    @Test
    void hasCollectionSeparateProperties_nullProps_returnsFalse() {
        assertFalse(SimpleProperties.hasCollectionSeparateProperties(null, "items"));
    }

    @Test
    void loadListSeparateProperties_buildsList() {
        MapPropertyStore<Void> store = MapPropertyStore.createDefaultInstance();
        store.putProperty("items0", "a");
        store.putProperty("items1", "b");
        assertEquals(List.of("a", "b"), SimpleProperties.loadListSeparateProperties(store, "items"));
    }

    @Test
    void loadSetSeparateProperties_deduplicates() {
        MapPropertyStore<Void> store = MapPropertyStore.createDefaultInstance();
        store.putProperty("items0", "a");
        store.putProperty("items1", "a");
        store.putProperty("items2", "b");
        Set<String> set = SimpleProperties.loadSetSeparateProperties(store, "items");
        assertEquals(2, set.size());
    }

    // =====================================================================
    // loadList(Pattern) / loadList(GetProperties<T>)
    // =====================================================================

    @Test
    void loadList_withPattern_splitsAndSkipsEmpties() {
        List<String> list = new ArrayList<>();
        boolean result = SimpleProperties.loadList(props("items", "a;;b;c"), "items", list, Pattern.compile(";"));
        assertTrue(result);
        assertEquals(List.of("a", "b", "c"), list);
    }

    @Test
    void loadList_withPattern_absentProperty_returnsFalse() {
        List<String> list = new ArrayList<>();
        assertFalse(SimpleProperties.loadList(
            SimpleProperties.emptyGetProperty(),
            "items",
            list,
            Pattern.compile(";")
        ));
    }

    @Test
    void loadList_withPattern_nullProps_returnsFalse() {
        assertFalse(SimpleProperties.loadList(null, "items", new ArrayList<>(), Pattern.compile(";")));
    }

    @Test
    void loadList_getProperties_delegatesAndReturnsTrue() {
        GetProperties<String> source = (name, list) -> {
            list.add("from-" + name);
            return true;
        };
        List<String> list = new ArrayList<>();
        assertTrue(SimpleProperties.loadList(source, "n", list));
        assertEquals(List.of("from-n"), list);
    }

    @Test
    void loadList_getProperties_falseResult_returnsFalse() {
        GetProperties<String> source = (_, _) -> false;
        assertFalse(SimpleProperties.loadList(source, "n", new ArrayList<>()));
    }

    @Test
    void loadList_getProperties_nullSource_returnsFalse() {
        assertFalse(SimpleProperties.loadList((GetProperties<String>) null, "n", new ArrayList<>()));
    }

    // =====================================================================
    // loadMap / loadMapSingleProperty / saveMap
    // =====================================================================

    @Test
    void loadMap_singleProperty_parsesNameValuePairs() {
        Map<String,String> map = new LinkedHashMap<>();
        boolean result = SimpleProperties.loadMap(props("cfg", "a=1,b=2"), "cfg", map, true);
        assertTrue(result);
        assertEquals("1", map.get("a"));
        assertEquals("2", map.get("b"));
    }

    @Test
    void loadMap_separateProperties_collectsPrefixedKeys() {
        MapPropertyStore<Void> store = MapPropertyStore.createDefaultInstance();
        store.putProperty("cfg_a", "1");
        store.putProperty("cfg_b", "2");
        store.putProperty("other", "x");
        Map<String,String> map = new HashMap<>();
        boolean result = SimpleProperties.loadMap(store, "cfg", map, false);
        assertTrue(result);
        assertEquals(2, map.size());
        assertEquals("1", map.get("a"));
    }

    @Test
    void loadMap_nullProps_returnsFalse() {
        assertFalse(SimpleProperties.loadMap(null, "cfg", new HashMap<>(), true));
    }

    @Test
    void loadMapSingleProperty_absent_returnsFalse() {
        assertFalse(SimpleProperties.loadMapSingleProperty(
            SimpleProperties.emptyGetProperty(),
            "cfg",
            new HashMap<>()
        ));
    }

    @Test
    void saveMap_sortsAndJoinsEntries() {
        MapPropertyStore<Void> store = MapPropertyStore.createDefaultInstance();
        Map<String,Integer> map = new LinkedHashMap<>();
        map.put("b", 2);
        map.put("a", 1);
        SimpleProperties.saveMap(store, "cfg", map);
        assertEquals("a=1,b=2", store.getProperty("cfg"));
    }

    // =====================================================================
    // saveList / saveListSingleProperty / saveListSeparateProperties
    // =====================================================================

    @Test
    void saveListSingleProperty_joinsWithCommas() {
        MapPropertyStore<Void> store = MapPropertyStore.createDefaultInstance();
        SimpleProperties.saveListSingleProperty(store, "items", List.of("a", "b", "c"));
        assertEquals("a,b,c", store.getProperty("items"));
    }

    @Test
    void saveListSeparateProperties_collection_storesCountAndIndexedValues() {
        MapPropertyStore<Void> store = MapPropertyStore.createDefaultInstance();
        SimpleProperties.saveListSeparateProperties(
            store, "items", List.of("a", "b"), NativeTypeConversion.DEFAULT_COLLECTION_TO_STRING_OPTIONS
        );
        assertEquals("2", store.getProperty("items_count"));
        assertEquals("a", store.getProperty("items0"));
        assertEquals("b", store.getProperty("items1"));
    }

    @Test
    void saveListSeparateProperties_nonCollectionIterable_countsWhileIterating() {
        MapPropertyStore<Void> store = MapPropertyStore.createDefaultInstance();
        Iterable<String> notACollection = () -> List.of("x", "y", "z").iterator();
        SimpleProperties.saveListSeparateProperties(
            store, "items", notACollection, NativeTypeConversion.DEFAULT_COLLECTION_TO_STRING_OPTIONS
        );
        assertEquals("3", store.getProperty("items_count"));
        assertEquals("z", store.getProperty("items2"));
    }

    @Test
    void saveListSeparateProperties_nullList_doesNothing() {
        MapPropertyStore<Void> store = MapPropertyStore.createDefaultInstance();
        SimpleProperties.saveListSeparateProperties(
            store,
            "items",
            null,
            NativeTypeConversion.DEFAULT_COLLECTION_TO_STRING_OPTIONS
        );
        assertTrue(store.getPropertyNames().isEmpty());
    }

    @Test
    void saveList_singlePropertyFlag_dispatchesCorrectly() {
        MapPropertyStore<Void> single = MapPropertyStore.createDefaultInstance();
        SimpleProperties.saveList(single, "items", List.of("a", "b"), true);
        assertEquals("a,b", single.getProperty("items"));

        MapPropertyStore<Void> separate = MapPropertyStore.createDefaultInstance();
        SimpleProperties.saveList(separate, "items", List.of("a", "b"), false);
        assertEquals("a", separate.getProperty("items0"));
        assertEquals("2", separate.getProperty("items_count"));
    }

    // =====================================================================
    // loadClass / loadSubClass
    // =====================================================================

    @Test
    void loadClass_valid_returnsClass() {
        assertEquals(String.class, SimpleProperties.loadClass(props("cls", "java.lang.String"), "cls"));
    }

    @Test
    void loadClass_invalid_returnsDefault() {
        assertNull(SimpleProperties.loadClass(props("cls", "not.a.real.Class"), "cls"));
        assertEquals(Integer.class, SimpleProperties.loadClass(props("cls", "not.a.real.Class"), "cls", Integer.class));
    }

    @Test
    void loadSubClass_valid_returnsSubclass() {
        Class<? extends LoadInstanceMarker> cls = SimpleProperties.loadSubClass(
            props("cls", WithOnlyConstructor.class.getName()), "cls", LoadInstanceMarker.class
        );
        assertEquals(WithOnlyConstructor.class, cls);
    }

    @Test
    void loadSubClass_notASubtype_returnsDefault() {
        Class<? extends LoadInstanceMarker> defaultValue = WithOnlyConstructor.class;
        Class<? extends LoadInstanceMarker> cls = SimpleProperties.loadSubClass(
            props("cls", "java.lang.String"), "cls", LoadInstanceMarker.class, defaultValue
        );
        assertSame(defaultValue, cls);
    }

    @Test
    void loadSubClass_unresolvable_returnsDefault() {
        assertNull(SimpleProperties.loadSubClass(props("cls", "nope.Nope"), "cls", LoadInstanceMarker.class));
    }

    @Test
    void loadSubClass_absent_returnsDefault() {
        assertNull(SimpleProperties.loadSubClass(SimpleProperties.emptyGetProperty(), "cls", LoadInstanceMarker.class));
    }

    // =====================================================================
    // loadInstance — exercises every branch of the getInstance()/nullary-constructor
    // reflection fallback logic.
    // =====================================================================

    public interface LoadInstanceMarker {
    }

    public static final class WithGetInstance implements LoadInstanceMarker {

        private static final WithGetInstance INSTANCE = new WithGetInstance();

        public static WithGetInstance getInstance() {
            return INSTANCE;
        }

        private WithGetInstance() {
        }

    }

    public static final class WithOnlyConstructor implements LoadInstanceMarker {

        public WithOnlyConstructor() {
        }

    }

    public static final class NonStaticGetInstance implements LoadInstanceMarker {

        public NonStaticGetInstance() {
        }

        // Not static -- loadInstance should ignore this and fall back to the constructor.
        public NonStaticGetInstance getInstance() {
            return this;
        }

    }

    public static final class WrongTypeGetInstance implements LoadInstanceMarker {

        public WrongTypeGetInstance() {
        }

        // Wrong return type -- loadInstance should ignore this and fall back to the constructor.
        public static String getInstance() {
            return "not-a-marker";
        }

    }

    public static final class NeitherGetInstanceNorPublicCtor implements LoadInstanceMarker {

        private NeitherGetInstanceNorPublicCtor() {
        }

    }

    public static final class ThrowingConstructorOnly implements LoadInstanceMarker {

        public ThrowingConstructorOnly() {
            throw new RuntimeException("ctor boom");
        }

    }

    public static final class GetInstanceThrowsNoPublicCtor implements LoadInstanceMarker {

        private GetInstanceThrowsNoPublicCtor() {
        }

        public static GetInstanceThrowsNoPublicCtor getInstance() {
            throw new RuntimeException("getInstance boom");
        }

    }

    public static final class BothThrow implements LoadInstanceMarker {

        public BothThrow() {
            throw new RuntimeException("ctor boom");
        }

        public static BothThrow getInstance() {
            throw new RuntimeException("getInstance boom");
        }

    }

    @Test
    void loadInstance_prefersGetInstance() {
        GetProperty p = props("cls", WithGetInstance.class.getName());
        LoadInstanceMarker result = SimpleProperties.loadInstance(p, "cls", LoadInstanceMarker.class, null);
        assertSame(WithGetInstance.getInstance(), result);
    }

    @Test
    void loadInstance_fallsBackToConstructor_whenNoGetInstance() {
        GetProperty p = props("cls", WithOnlyConstructor.class.getName());
        LoadInstanceMarker result = SimpleProperties.loadInstance(p, "cls", LoadInstanceMarker.class, null);
        assertInstanceOf(WithOnlyConstructor.class, result);
    }

    @Test
    void loadInstance_ignoresNonStaticGetInstance_usesConstructor() {
        GetProperty p = props("cls", NonStaticGetInstance.class.getName());
        LoadInstanceMarker result = SimpleProperties.loadInstance(p, "cls", LoadInstanceMarker.class, null);
        assertInstanceOf(NonStaticGetInstance.class, result);
    }

    @Test
    void loadInstance_ignoresWrongReturnTypeGetInstance_usesConstructor() {
        GetProperty p = props("cls", WrongTypeGetInstance.class.getName());
        LoadInstanceMarker result = SimpleProperties.loadInstance(p, "cls", LoadInstanceMarker.class, null);
        assertInstanceOf(WrongTypeGetInstance.class, result);
    }

    @Test
    void loadInstance_noGetInstanceNoPublicCtor_returnsDefault() {
        GetProperty p = props("cls", NeitherGetInstanceNorPublicCtor.class.getName());
        LoadInstanceMarker defaultValue = WithGetInstance.getInstance();
        assertSame(defaultValue, SimpleProperties.loadInstance(p, "cls", LoadInstanceMarker.class, defaultValue));
    }

    @Test
    void loadInstance_constructorThrows_noGetInstance_returnsDefault() {
        GetProperty p = props("cls", ThrowingConstructorOnly.class.getName());
        LoadInstanceMarker defaultValue = WithGetInstance.getInstance();
        assertSame(defaultValue, SimpleProperties.loadInstance(p, "cls", LoadInstanceMarker.class, defaultValue));
    }

    @Test
    void loadInstance_getInstanceThrows_noPublicCtor_returnsDefault() {
        GetProperty p = props("cls", GetInstanceThrowsNoPublicCtor.class.getName());
        LoadInstanceMarker defaultValue = WithGetInstance.getInstance();
        assertSame(defaultValue, SimpleProperties.loadInstance(p, "cls", LoadInstanceMarker.class, defaultValue));
    }

    @Test
    void loadInstance_getInstanceAndConstructorBothThrow_returnsDefault() {
        GetProperty p = props("cls", BothThrow.class.getName());
        LoadInstanceMarker defaultValue = WithGetInstance.getInstance();
        assertSame(defaultValue, SimpleProperties.loadInstance(p, "cls", LoadInstanceMarker.class, defaultValue));
    }

    @Test
    void loadInstance_unresolvableClassName_returnsDefault() {
        LoadInstanceMarker defaultValue = WithGetInstance.getInstance();
        assertSame(
            defaultValue,
            SimpleProperties.loadInstance(
                props("cls", "not.a.real.ClassName"),
                "cls",
                LoadInstanceMarker.class,
                defaultValue
            )
        );
    }

    @Test
    void loadInstance_propertyAbsent_returnsDefault() {
        LoadInstanceMarker defaultValue = WithGetInstance.getInstance();
        assertSame(
            defaultValue,
            SimpleProperties.loadInstance(
                SimpleProperties.emptyGetProperty(),
                "cls",
                LoadInstanceMarker.class,
                defaultValue
            )
        );
    }

    // =====================================================================
    // loadUuid / saveUuid
    // =====================================================================

    @Test
    void loadUuid_valid_returnsUuid() {
        UUID id = UUID.randomUUID();
        assertEquals(id, SimpleProperties.loadUuid(props("id", id.toString()), "id"));
    }

    @Test
    void loadUuid_absent_returnsNull() {
        assertNull(SimpleProperties.loadUuid(SimpleProperties.emptyGetProperty(), "id"));
    }

    @Test
    void loadUuid_blank_returnsDefault() {
        UUID defaultValue = UUID.randomUUID();
        assertEquals(defaultValue, SimpleProperties.loadUuid(props("id", "   "), "id", defaultValue));
    }

    @Test
    void loadUuid_malformed_returnsDefault() {
        UUID defaultValue = UUID.randomUUID();
        assertEquals(defaultValue, SimpleProperties.loadUuid(props("id", "not-a-uuid"), "id", defaultValue));
    }

    @Test
    void saveUuid_storesStringForm() {
        MapPropertyStore<Void> store = MapPropertyStore.createDefaultInstance();
        UUID id = UUID.randomUUID();
        SimpleProperties.saveUuid(store, "id", id);
        assertEquals(id.toString(), store.getProperty("id"));
    }

    @Test
    void saveUuid_null_storesNull() {
        MapPropertyStore<Void> store = MapPropertyStore.createDefaultInstance();
        store.putProperty("id", "placeholder");
        SimpleProperties.saveUuid(store, "id", null);
        assertNull(store.getProperty("id"));
    }

    // =====================================================================
    // loadProperty
    // =====================================================================

    private static final PropertyLoader<String> UPPERCASING_LOADER = new PropertyLoader<>() {

        @Override
        public String apply(String name, String rawValue) {
            return rawValue == null ? null : rawValue.toUpperCase();
        }

        @Override
        public String cast(Object o) {
            return (String) o;
        }

    };

    @Test
    void loadProperty_present_appliesLoader() {
        assertEquals("HELLO", SimpleProperties.loadProperty(props("k", "hello"), "k", UPPERCASING_LOADER));
    }

    @Test
    void loadProperty_nullProps_returnsDefault() {
        assertEquals("dflt", SimpleProperties.loadProperty(null, "k", UPPERCASING_LOADER, true, "dflt"));
    }

    @Test
    void loadProperty_absentRaw_loaderReturnsNull_returnsDefault() {
        assertEquals(
            "dflt",
            SimpleProperties.loadProperty(
                SimpleProperties.emptyGetProperty(),
                "k",
                UPPERCASING_LOADER,
                true,
                "dflt"
            )
        );
    }

    @Test
    void loadProperty_mayUseCacheFalse_stillAppliesLoader() {
        assertEquals("HELLO", SimpleProperties.loadProperty(props("k", "hello"), "k", UPPERCASING_LOADER, false));
    }

    // =====================================================================
    // loadDurationMillis / loadDuration
    // =====================================================================

    @Test
    void loadDurationMillis_millisStoredDirectly_returnsValue() {
        MapPropertyStore<Void> store = MapPropertyStore.createDefaultInstance();
        store.putProperty("timeout", "5000");
        assertEquals(5000L, SimpleProperties.loadDurationMillis(store, "timeout", -1L));
    }

    @Test
    void loadDurationMillis_unitSuffixedProperty_returnsConvertedValue() {
        MapPropertyStore<Void> store = MapPropertyStore.createDefaultInstance();
        store.putProperty("timeout_s", "3"); // "s" suffix => SECONDS
        assertEquals(3000L, SimpleProperties.loadDurationMillis(store, "timeout", -1L));
    }

    @Test
    void loadDurationMillis_absent_returnsDefault() {
        MapPropertyStore<Void> store = MapPropertyStore.createDefaultInstance();
        assertEquals(-1L, SimpleProperties.loadDurationMillis(store, "timeout", -1L));
    }

    @Test
    void loadDuration_millisStoredDirectly_returnsDuration() {
        MapPropertyStore<Void> store = MapPropertyStore.createDefaultInstance();
        store.putProperty("timeout", "1500");
        assertEquals(Duration.ofMillis(1500), SimpleProperties.loadDuration(store, "timeout", null));
    }

    @Test
    void loadDuration_absent_returnsDefault() {
        MapPropertyStore<Void> store = MapPropertyStore.createDefaultInstance();
        Duration defaultValue = Duration.ofSeconds(30);
        assertEquals(defaultValue, SimpleProperties.loadDuration(store, "timeout", defaultValue));
    }

    @Test
    void loadDuration_nullProps_returnsDefault() {
        assertEquals(-1L, SimpleProperties.loadDurationMillis(null, "timeout", -1L));
    }

    @Test
    void loadDuration_nullProps_nullDefault_ReturnsNull() {
        assertNull(SimpleProperties.loadDuration(null, "timeout", null));
    }

    // =====================================================================
    // loadEnum
    // =====================================================================

    private enum Color {
        RED,
        GREEN,
        BLUE
    }

    @Test
    void loadEnum_caseInsensitiveMatch_returnsValue() {
        Configuration cfg = SimpleProperties.asConfiguration(Map.of("color", "green"), "cfg");
        assertEquals(Color.GREEN, SimpleProperties.loadEnum(cfg, "color", Color.class, Color.RED));
    }

    @Test
    void loadEnum_noMatch_returnsDefault() {
        Configuration cfg = SimpleProperties.asConfiguration(Map.of("color", "purple"), "cfg");
        assertEquals(Color.RED, SimpleProperties.loadEnum(cfg, "color", Color.class, Color.RED));
    }

    @Test
    void loadEnum_absent_returnsDefault() {
        assertEquals(
            Color.BLUE,
            SimpleProperties.loadEnum(SimpleProperties.emptyConfiguration(), "color", Color.class, Color.BLUE)
        );
    }

}
