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

import org.apache.commons.lang3.StringUtils;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Generates various {@link DateFormat}s used to format dates and times in various contexts.
 */
public final class DateFormats {

    private DateFormats() {
    }

    public static final String DATEMODE_NORMAL = "normal";

    public static final String DATEMODE_TIME_TODAY = "timetoday";

    public static final String DATEMODE_RELATIVE = "relative";

    public static final String DATEMODE_ICAL = "ical";

    public static final String DATEFORMAT_TINY = "tiny";

    public static final String DATEFORMAT_SHORT = "short";

    public static final String DATEFORMAT_MEDIUM = "medium";

    public static final String DATEFORMAT_LONG = "long";

    public static final String DATEFORMAT_FULL = "full";

    public static final String DATEFORMAT_TIME = "time";

    public static final String DATEFORMAT_DATE = "date";

    public static final String DATEFORMAT_RAW = "raw";

    public static final int DATEFORMAT_STYLE_NONE = -1;

    public static final int DATEFORMAT_STYLE_RAW = -1000;

    public static final String DATEFORMAT_ICAL_DATESTAMP_ALLDAY = "yyyyMMdd";

    public static final String DATEFORMAT_ICAL_DATESTAMP = "yyyyMMdd'T'HHmmss'Z'";

    public static final SimpleDateFormat getDateFormat(String format) {
        return newSimpleDateFormat(format, LocaleUtil.getCurrentLocale(), TimeZoneUtil.getCurrentTimeZone());
    }

    private static final SimpleDateFormat newSimpleDateFormat(String format, Locale locale, TimeZone timeZone) {
        SimpleDateFormat ret = new SimpleDateFormat(format, locale);
        setTimeZone(ret, timeZone);
        return ret;
    }

    private static final void setTimeZone(DateFormat dateFormat, TimeZone timeZone) {
        if (timeZone != null) {
            dateFormat.setTimeZone(timeZone);
        }
    }

    private static final DateFormat newStyleDateFormat(int style, Locale locale, TimeZone timeZone) {
        DateFormat ret = DateFormat.getDateInstance(style, locale);
        setTimeZone(ret, timeZone);
        return ret;
    }

    private static final DateFormat newStyleTimeFormat(int style, Locale locale, TimeZone timeZone) {
        DateFormat ret = DateFormat.getTimeInstance(style, locale);
        setTimeZone(ret, timeZone);
        return ret;
    }

    private static final DateFormat newStyleDateTimeFormat(int dateStyle, int timeStyle, Locale locale) {
        DateFormat ret = DateFormat.getDateTimeInstance(dateStyle, timeStyle, locale);
        setTimeZone(ret, TimeZoneUtil.getCurrentTimeZone());
        return ret;
    }

    public static final SimpleDateFormat getDefaultDateFormat(String format) {
        return newSimpleDateFormat(format, LocaleUtil.getCurrentLocale(), TimeZoneUtil.getEnvironmentDefaultTimeZone());
    }

    public static final DateFormat getDateInstance(int style) {
        return newStyleDateFormat(style, LocaleUtil.getCurrentLocale(), TimeZoneUtil.getCurrentTimeZone());
    }

    public static final DateFormat getTimeInstance(int style) {
        return newStyleTimeFormat(style, LocaleUtil.getCurrentLocale(), TimeZoneUtil.getCurrentTimeZone());
    }

    public static final DateFormat getDateTimeInstance(int dateStyle, int timeStyle) {
        return newStyleDateTimeFormat(dateStyle, timeStyle, LocaleUtil.getCurrentLocale());
    }

    public static final DateFormat getCommentDateFormat() {
        return newSimpleDateFormat("M/d/y", LocaleUtil.getCurrentLocale(), TimeZoneUtil.getCurrentTimeZone());
    }

    public static final DateFormat getCalendarDialogFormat() {
        return newSimpleDateFormat("M/d/y", Locale.US, TimeZoneUtil.getCurrentTimeZone());
    }

    public static final SimpleDateFormat getUrlDateFormat() {
        return getUrlDateFormat(TimeZoneUtil.getCurrentTimeZone());
    }

    public static final SimpleDateFormat getUrlDateFormat(TimeZone timeZone) {
        return newSimpleDateFormat("yyyyMMdd", Locale.US, timeZone);
    }

    public static final SimpleDateFormat getUrlDateTimeFormat() {
        return newSimpleDateFormat("yyyyMMddHHmmssZ", Locale.US, TimeZoneUtil.getCurrentTimeZone());
    }

    public static final SimpleDateFormat getRsDisplayFormat() {
        return newSimpleDateFormat("yyyy/MM/dd", Locale.US, TimeZoneUtil.getCurrentTimeZone());
    }

    public static final SimpleDateFormat getRsDateFormat() {
        return newSimpleDateFormat("dMMMyyyy", Locale.US, TimeZoneUtil.getCurrentTimeZone());
    }

    /**
     * Returns a new {@link SimpleDateFormat} corresponding to RFC 1123.
     *
     * @param timeZone
     *     to be applied to the {@link SimpleDateFormat}.
     * @return a new {@link SimpleDateFormat} corresponding to RFC 1123.
     */
    public static final SimpleDateFormat getHttpDateFormat(TimeZone timeZone) {
        return newSimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz", Locale.US, timeZone);
    }

    /**
     * Returns a new {@link SimpleDateFormat} corresponding to RFC 1123, including using the GMT time zone. This is
     * mandatory for certain HTTP request and response headers, including "If-Modified-Since".
     *
     * @return a new {@link SimpleDateFormat} corresponding to RFC 1123, including using the GMT time zone. This is
     *     mandatory for certain HTTP request and response headers, including "If-Modified-Since".
     */
    public static final SimpleDateFormat getHttpDateFormat() {
        return getHttpDateFormat(DateUtil.GMT);
    }

    /**
     * Returns a new {@link SimpleDateFormat} corresponding to RFC 850. (HTTP: The Definitive Guide says use this for
     * Cookies).
     *
     * @param timeZone
     *     to be set on the {@link SimpleDateFormat}.
     * @return a new {@link SimpleDateFormat} corresponding to RFC 850. (HTTP: The Definitive Guide says use this for
     *     Cookies).
     */
    public static final SimpleDateFormat getCookieDateFormat(TimeZone timeZone) {
        return newSimpleDateFormat("EEEEE, dd-MMM-yyyy HH:mm:ss zzz", Locale.US, timeZone);
    }

    /**
     * Returns a {@link SimpleDateFormat} that corresponds to the format of time stamp strings that are returned by Java
     * DB.
     *
     * @return a {@link SimpleDateFormat} that corresponds to the format of time stamp strings that are returned by Java
     *     DB.
     * @implNote The time stamp format does not include a time zone. Previously, the TimeZone set on the DateFormat
     *     was GMT, but that did not achieve parity with behaviors related to database time stamps. This method now
     *     always uses the current time zone for the request, which does seem to achieve such parity, and works as
     *     expected in TeamPage when it comes to capabilities such as due date groupings. This may change in the future
     *     if any issues come up, but for now this appears to be correct. [shep 29.Sep.2010]
     */
    public static final SimpleDateFormat getTimeStampFormat() {
        SimpleDateFormat ret = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);
        ret.setTimeZone(TimeZoneUtil.getCurrentTimeZone());
        return ret;
    }

    /**
     * Returns a {@link SimpleDateFormat} identical to {@link #getTimeStampFormat()}, but has the "general time zone"
     * parameter appended to the end of the rest of the date/time stamp.
     *
     * @return a {@link SimpleDateFormat} identical to {@link #getTimeStampFormat()}, but has the "general time zone"
     *     parameter appended to the end of the rest of the date/time stamp.
     */
    public static final SimpleDateFormat getTimeStampFormatWithTimeZone() {
        SimpleDateFormat ret = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss z", Locale.US);
        ret.setTimeZone(TimeZoneUtil.getCurrentTimeZone());
        return ret;
    }

    /**
     * Returns a new {@link SimpleDateFormat} corresponding to the required format for dates in Extended Log Files
     * (yyyy-MM-dd).
     *
     * @return a new {@link SimpleDateFormat} corresponding to the required format for dates in Extended Log Files
     *     (yyyy-MM-dd).
     */
    public static final SimpleDateFormat getExlfDateFormat() {
        return newSimpleDateFormat("yyyy-MM-dd", Locale.US, DateUtil.GMT);
    }

    /**
     * Returns a new {@link SimpleDateFormat} corresponding to the required format for times in Extended Log Files
     * (hh:mm:ss).
     *
     * @return a new {@link SimpleDateFormat} corresponding to the required format for times in Extended Log Files
     *     (hh:mm:ss).
     */
    public static final SimpleDateFormat getExlfTimeFormat() {
        return newSimpleDateFormat("HH:mm:ss", Locale.US, DateUtil.GMT);
    }

    /**
     * Returns a new {@link SimpleDateFormat} that is used for by TeamPage to create a date stamp that is used as part
     * of the name for archived files (e.g., log files).
     *
     * @return a new {@link SimpleDateFormat} that is used for by TeamPage to create a date stamp that is used as part
     *     of the name for archived files (e.g., log files).
     */
    public static final SimpleDateFormat getDefaultArchiveLogFileNameDateStampFormat() {
        return newSimpleDateFormat("yyyyMMdd", Locale.US, TimeZone.getDefault());
    }

    /**
     * Returns a {@link SimpleDateFormat} that is used to render {@link Date} values in the server time zone in certain
     * contexts.
     *
     * @return a {@link SimpleDateFormat} that is used to render {@link Date} values in the server time zone in certain
     *     contexts.
     */
    public static final SimpleDateFormat getServerTimeZoneFormat() {
        return newSimpleDateFormat("yyyyMMddHHmmssZ", Locale.US, null);
    }

    /**
     * Returns the {@link DateFormat} used to format {@link Date}s in TeamPage's log files.
     *
     * @return the {@link DateFormat} used to format {@link Date}s in TeamPage's log files.
     */
    public static final DateFormat getLogFileDateFormat() {
        return new SimpleDateFormat("EEE MMM dd HH:mm:ss.SSS zzz yyyy");
    }

    /**
     * Returns a new {@link GregorianCalendar} representing the current date and time, and using the given
     * {@link TimeZone}.
     *
     * @param timeZone
     *     the {@link TimeZone} to apply to the calendar.
     * @return a new {@link GregorianCalendar} representing the current date and time, and using the given
     *     {@link TimeZone}.
     */
    public static final GregorianCalendar getGregorianCalendar(TimeZone timeZone) {
        return (timeZone != null) ? new GregorianCalendar(timeZone) : new GregorianCalendar();
    }

    /**
     * Returns a new {@link GregorianCalendar} instance set to use the TimeZone for the current request.
     *
     * @return a new {@link GregorianCalendar} instance set to use the TimeZone for the current request.
     */
    public static final GregorianCalendar getGregorianCalendar() {
        return getGregorianCalendar(TimeZoneUtil.getCurrentTimeZone());
    }

    /**
     * Returns a new {@link GregorianCalendar} representing the given {@link Date}, and using the given
     * {@link TimeZone}.
     *
     * @param timeZone
     *     the {@link TimeZone} to apply to the calendar.
     * @param date
     *     the {@link Date} that should be used to initialize the calendar.
     * @return a new {@link GregorianCalendar} representing the given {@link Date}, and using the given
     *     {@link TimeZone}.
     */
    public static final GregorianCalendar getGregorianCalendar(TimeZone timeZone, Date date) {
        GregorianCalendar ret = getGregorianCalendar(timeZone);
        ret.setTime(date);
        return ret;
    }

    /**
     * Returns a new {@link GregorianCalendar} instance for the given {@link Date}, set to the given {@link Date}.
     *
     * @param date
     *     the {@link Date} to use to initialize the new calendar.
     * @return a new {@link GregorianCalendar} instance for the given {@link Date}, set to the given {@link Date}.
     */
    public static final GregorianCalendar getGregorianCalendar(Date date) {
        return getGregorianCalendar(TimeZoneUtil.getCurrentTimeZone(), date);
    }

    /**
     * Returns a new {@link GregorianCalendar} representing midnight on the the given {@link Date}'s day, in the given
     * {@link TimeZone}.
     *
     * @param timezone
     *     the {@link TimeZone} that will be used with for the new calendar.
     * @param date
     *     the {@link Date} which will be used to initialize the calendar.
     * @return a new {@link GregorianCalendar} representing midnight on the the given {@link Date}'s day, in the given
     *     {@link TimeZone}.
     */
    public static final GregorianCalendar getGregorianCalendarForDay(TimeZone timezone, Date date) {
        GregorianCalendar ret = getGregorianCalendar(timezone);
        ret.setTime(date);
        setToMidnight(ret);
        return ret;
    }

    /**
     * Returns a new {@link GregorianCalendar} representing midnight on the the given {@link Date}'s day, in the
     * {@link TimeZone} for the current request.
     *
     * @param date
     *     the {@link Date} which will be used to initialize the calendar.
     * @return a new {@link GregorianCalendar} representing midnight on the the given {@link Date}'s day, in the
     *     {@link TimeZone} for the current request.
     */
    public static final GregorianCalendar getGregorianCalendarForDay(Date date) {
        return getGregorianCalendarForDay(TimeZoneUtil.getCurrentTimeZone(), date);
    }

    /**
     * Returns a new {@link GregorianCalendar} representing the given {@link Date} in the {@link TimeZone} for the
     * current request.
     *
     * @param dateAtMidnightGMT
     *     a {@link Date} that already represents midnight at GMT, congruent with the way that date values are
     *     representing days are stored in database tables.
     */
    public static final GregorianCalendar getGregorianCalendarForDayInGMT(Date dateAtMidnightGMT) {
        return getGregorianCalendarForDayInGMT(TimeZoneUtil.getCurrentTimeZone(), dateAtMidnightGMT);
    }

    /**
     * Returns a new {@link GregorianCalendar} representing midnight on the given {@link Date}'s day in the given
     * {@link TimeZone}.
     *
     * @param dateAtMidnightGMT
     *     A date set to midnight at GMT. This is the way we store dates that represent days in the database.
     * @param timezone
     *     The {@link TimeZone} in which the Calendar should be returned. This is important because, for example,
     *     midnight in Japan is a different time (calendar.getTime()) than midnight in the US. The Calendar will be set
     *     to midnight of the same day as dateAtMidnightGMT in the specified TimeZone.
     * @return a new {@link GregorianCalendar} representing midnight on the given {@link Date}'s day in the given
     *     {@link TimeZone}.
     */
    public static final GregorianCalendar getGregorianCalendarForDayInGMT(TimeZone timezone, Date dateAtMidnightGMT) {

        GregorianCalendar gmtCalendar = getGregorianCalendar(DateUtil.GMT);
        gmtCalendar.setTime(dateAtMidnightGMT);

        // this should be unnecessary, but just in case
        setToMidnight(gmtCalendar);

        // now return a calendar with the same date fields at midnight
        GregorianCalendar ret = getGregorianCalendar(timezone);
        copyDayFields(gmtCalendar, ret);

        return ret;

    }

    private static final void copyDayFields(Calendar fromCalendar, Calendar toCalendar) {
        // reset to avoid any strange issues
        toCalendar.setTime(new Date(0));
        // copy the date fields for the day
        toCalendar.set(Calendar.YEAR, fromCalendar.get(Calendar.YEAR));
        toCalendar.set(Calendar.MONTH, fromCalendar.get(Calendar.MONTH));
        toCalendar.set(Calendar.DAY_OF_MONTH, fromCalendar.get(Calendar.DAY_OF_MONTH));
        // make sure there aren't any time fields
        setToMidnight(toCalendar);
    }

    /**
     * Sets all fields of lower significance than day fields to 0 on the given {@link Calendar}. This has the effect of
     * flattening the date and time represented by the Calendar to midnight on the same day it currently represents.
     *
     * @param calendar
     *     the {@link Calendar} to be modified.
     */
    public static final void setToMidnight(Calendar calendar) {
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.HOUR, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        calendar.set(Calendar.AM_PM, 0);
    }

    public static final String getTimeToday(Date date, Date today, int dateStyle, int timeStyle) {

        Calendar todayCal = getGregorianCalendar(today);
        Calendar dateCal = getGregorianCalendar(date);

        // check if it's today
        if ((todayCal.get(Calendar.DAY_OF_YEAR) == dateCal.get(Calendar.DAY_OF_YEAR)) &&
            (todayCal.get(Calendar.YEAR) == dateCal.get(Calendar.YEAR))) {
            return getTimeInstance(timeStyle).format(date);
        }
        return getDateInstance(dateStyle).format(date);

    }

    public static final int getTimeFormatStyle(String styleSpec, int defaultStyle) {
        if (StringUtils.isBlank(styleSpec)) {
            return defaultStyle;
        }
        return switch (styleSpec) {
            case DateFormats.DATEFORMAT_TINY, DateFormats.DATEFORMAT_SHORT -> DateFormat.SHORT;
            case DateFormats.DATEFORMAT_MEDIUM -> DateFormat.MEDIUM;
            case DateFormats.DATEFORMAT_LONG -> DateFormat.LONG;
            case DateFormats.DATEFORMAT_FULL -> DateFormat.FULL;
            case DateFormats.DATEFORMAT_RAW -> DateFormats.DATEFORMAT_STYLE_RAW;
            default -> defaultStyle;
        };
    }

    /**
     * The dateformat="" attribute for date tags specifies the format to be used for the date. A special format, "time",
     * is also recognized which uses the server's default time format. Also, "short", "medium", "long", and "full"
     * correspond to the DateFormat constants and the dateformat will be constructed using DateFormat.getDateInstance,
     * passing the constant.
     *
     * <p>
     * The timeformat="" attribute for date tags specifies the format to be used for the time. Also, "short", "medium",
     * "long", and "full" correspond to the DateFormat constants and the dateformat will be constructed using
     * DateFormat.getTimeInstance, passing the constant. Finally, "tiny" can be specified which is a custom version of
     * "small" that tries to minimize the representation in a way that works in multiple locales.
     *
     * <p>
     * If both timeformat= and dateformat= specify "short", "medium", "long", or "full", DateFormat.getDateTimeInstance
     * will be used.
     */
    public static final DateFormat getDateFormat(int dateStyle, int timeStyle) {
        if (dateStyle != DATEFORMAT_STYLE_NONE) {
            if (timeStyle != DATEFORMAT_STYLE_NONE) {
                return getDateTimeInstance(dateStyle, timeStyle);
            }
            return getDateInstance(dateStyle);
        }
        return DateFormats.getTimeInstance(timeStyle);
    }

}
