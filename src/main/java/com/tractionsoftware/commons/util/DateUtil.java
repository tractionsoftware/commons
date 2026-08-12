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

import com.tractionsoftware.commons.properties.GetProperty;
import com.tractionsoftware.commons.properties.SimpleProperties;
import org.threeten.extra.Interval;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Calendar;
import java.util.Date;
import java.util.Objects;
import java.util.TimeZone;

/**
 * @author Andy Keller, Dave Shepperton
 */
public final class DateUtil {

    private DateUtil() {
    }

    public static final TimeZone GMT = TimeZone.getTimeZone("GMT");

    private static final long HOURS_25_IN_MS = 90000000L;

    public static final long MS_PER_DAY = 86400000L;

    /**
     * For use with Dates that represent midnight.
     */
    public static final Date cvtDateAtMidnight(Date date, TimeZone sourceTimeZone, TimeZone targetTimeZone) {
        if (date == null) {
            return null;
        }
        Instant instant =
            date.toInstant().atZone(sourceTimeZone.toZoneId()).withZoneSameLocal(targetTimeZone.toZoneId()).toInstant();
        return Date.from(instant);
//        return Date.from(cvtDateAtMidnight(date.toInstant(), sourceTimeZone.toZoneId(), targetTimeZone.toZoneId()));
    }

    public static final Date cvtMidnightGmtToLocal(Date date, TimeZone localTimeZone) {
        return cvtDateAtMidnight(date, GMT, localTimeZone);
    }

    public static final Date cvtMidnightLocalToGmt(Date date) {
        return cvtMidnightLocalToGmt(date, TimeZone.getDefault());
    }

    public static final Date cvtMidnightLocalToGmt(Date date, TimeZone localTimeZone) {
        return cvtDateAtMidnight(date, localTimeZone, GMT);
    }

    // java.time versions of the above

    /**
     * For use with Dates that represent midnight.
     */
    public static final Instant cvtDateAtMidnight(Instant instant, ZoneId sourceZoneId, ZoneId targetZoneId) {
        if (instant == null) {
            return null;
        }
        return instant.atZone(sourceZoneId).withZoneSameLocal(targetZoneId).toInstant();
    }

    public static final Instant cvtMidnightGmtToLocal(Instant instant, ZoneId localZone) {
        return cvtDateAtMidnight(instant, GMT.toZoneId(), localZone);
    }

    public static final Instant cvtMidnightLocalToGmt(Instant instant, ZoneId localZone) {
        return cvtDateAtMidnight(instant, localZone, GMT.toZoneId());
    }

    /**
     * Returns the distance in number of days between 2 days that represent midnight in the same timezone.
     *
     * @return a negative number if date1 is after date2
     */
    public static final long getDistanceInDays(Date date1, Date date2) {
        return getDistanceInDays(date1, date2, GMT.toZoneId());
    }

    public static final long getDistanceInDays(Date date1, Date date2, TimeZone timezone) {
        return getDistanceInDays(date1, date2, timezone.toZoneId());
    }

    public static final long getDistanceInDays(Date date1, Date date2, ZoneId zone) {
        ZonedDateTime zdt1 = date1.toInstant().atZone(zone);
        ZonedDateTime zdt2 = date2.toInstant().atZone(zone);
        return zdt1.until(zdt2, ChronoUnit.DAYS);
    }

    /**
     * This makes the following assumptions (which hold true for the data that we store):
     *
     * <p>
     * 1) if isAllDay is true, the startTime and endTime represent dates at midnight in UTC and are timezone
     * independent. For example, New Years Day is 1/1 in any timezone.
     *
     * <p>
     * 2) If endTime is -1, this is an all day interval for the entire date of the startTime.
     *
     * <p>
     * 3) If it is not an all day interval, the startTime and endTime represent specific times and should be formatted
     * for the specified timezone.
     *
     * <p>
     * 4) If startTime > endTime, the interval is assumed to cover the entire day on the date of the startTime.
     */
    public static final Interval createInterval(Date start, Date end, boolean isAllDay, TimeZone zone) {

        Objects.requireNonNull(start, "start date");
        Objects.requireNonNull(zone, "time zone");

        ZoneId zoneId = zone.toZoneId();

        if (end != null && end.before(start)) {
            end = null;
        }

        ZonedDateTime zonedStart, zonedEnd;

        if (isAllDay) {

            // these are dates at midnight GMT
            zonedStart = cvtMidnightGmtToLocal(start, zone).toInstant().atZone(zoneId);

            if (end == null) {
                // assume it begins and ends on the same day
                zonedEnd = zonedStart;
            }
            else {
                zonedEnd = cvtMidnightGmtToLocal(end, zone).toInstant().atZone(zoneId);
            }

            // we add one because we want the interval to be the end
            // of the last day for an all day event.
            zonedEnd = zonedEnd.plusDays(1);

        }
        else {

            zonedStart = start.toInstant().atZone(zoneId);
            if (end == null) {
                zonedEnd = zonedStart.plusHours(1);
            }
            else {
                zonedEnd = end.toInstant().atZone(zoneId);
            }

        }

        return Interval.of(zonedStart.toInstant(), zonedEnd.toInstant());


    }

    /**
     * I wasn't sure exactly how this was supposed to be done, so I put it in one place and we do it consistently.
     */
    public static final long getDaysInInterval(Interval interval) {
        return interval.toDuration().toDays();
    }

    public static final boolean spansMultipleDays(Interval interval) {
        //return getDaysSpan() > 1;
        //
        // changed to check to see if the start and end are on
        // different days. an event as short as 2 minutes can span
        // days.
        //
        // we do minusMillis(1) because the end of the duration is not
        // included in the duration. e.g. an event from
        // 11pm-12am is not considered to be spanning, but
        // 11pm-12:01am is a spanning event.
        //
        ZoneId zone = TimeZoneUtil.getCurrentZoneId();
        ZonedDateTime testStart = interval.getStart().atZone(zone);
        ZonedDateTime testEnd = interval.getEnd().minusMillis(1L).atZone(zone);
        if (testStart.getYear() != testEnd.getYear() || testStart.getDayOfYear() != testEnd.getDayOfYear()) {
            return true;
        }
        return false;
    }

    private static final long getNewEndDateMidnightUTC(long newStartDateMidnightUTC, long oldStartDateMidnightUTC, long oldEndDateMidnightUTC) {
        ZonedDateTime newStartDate =
            ZonedDateTime.ofInstant(Instant.ofEpochMilli(newStartDateMidnightUTC), GMT.toZoneId());
        Interval interval =
            Interval.of(Instant.ofEpochMilli(oldStartDateMidnightUTC), Instant.ofEpochMilli(oldEndDateMidnightUTC));
        ZonedDateTime newEndDate = newStartDate.plusDays(getDaysInInterval(interval));
        return newEndDate.toInstant().toEpochMilli();
    }

    /**
     * Returns the local start time for an entry with the specified class configuration and entry properties. This takes
     * into account whether or not the event is an allday event and adjusts from midnight GMT to midnight localtime.
     */
    public static final Date getLocalStartTime(GetProperty entryClassConfiguration, GetProperty entryProperties, TimeZone tz) {

        String startDateProperty = entryClassConfiguration.getProperty("start_date_property");
        if (startDateProperty != null) {
            Date startDate = SimpleProperties.loadDate(entryProperties, startDateProperty, null);
            if (startDate != null) {
                return cvtMidnightGmtToLocal(startDate, tz);
            }
            return null;
        }

        String startTimeProperty = entryClassConfiguration.getProperty("start_time_property");
        String allDayProperty = entryClassConfiguration.getProperty("all_day_property");

        Date startTime = SimpleProperties.loadDate(entryProperties, startTimeProperty, null);
        if (startTime != null) {
            boolean isAllDay = SimpleProperties.loadBoolean(entryProperties, allDayProperty);
            if (isAllDay) {
                return cvtMidnightGmtToLocal(startTime, tz);
            }
            return startTime;
        }

        return null;

    }

    /**
     * Returns the Date value computed from the UTCDisplayDate as adjusted, if necessary, to take into account whether
     * the raw value represents an "all day" date. If it is an "all day" date, the Date returned will represent the raw
     * Date value for midnight GMT on the same calendar date as seen from the requesting user's preferred time zone.
     *
     * @param entryDate
     *     the UTCDisplayDate from which to compute the adjusted Date.
     * @return the Date value computed from the UTCDisplayDate as adjusted, if necessary, to take into account whether
     *     the raw value represents an "all day" date; or null if the given UTCDisplayDate is null.
     */
    public static final Date getDateAdjustedForAllDay(UTCDisplayDate entryDate) {
        if (entryDate == null) {
            return null;
        }
        if (entryDate.isAllDay()) {
            return cvtMidnightGmtToLocal(entryDate.getDate(), TimeZoneUtil.getCurrentTimeZone());
        }
        return entryDate.getDate();
    }

    public static final Calendar getCalendarAdjustedForAllDay(UTCDisplayDate entryDate) {
        if (entryDate == null) {
            return null;
        }
        Date adjustedDate = getDateAdjustedForAllDay(entryDate);
        if (entryDate.isAllDay()) {
            return DateFormats.getGregorianCalendar(DateUtil.GMT, adjustedDate);
        }
        return DateFormats.getGregorianCalendar(adjustedDate);
    }

    public static final Date getEndDateForDurationAdjustedForAllDay(UTCDisplayDate entryDate) {
        if (entryDate == null) {
            return null;
        }
        if (entryDate.isAllDay()) {
            Date d = cvtMidnightGmtToLocal(entryDate.getDate(), TimeZoneUtil.getCurrentTimeZone());
            return new Date(d.getTime() + MS_PER_DAY);
        }
        return entryDate.getDate();
    }

    public static final Date getTranslateByDays(Date date, TimeZone zone, long days) {
        return Date.from(ZonedDateTime.ofInstant(date.toInstant(), zone.toZoneId()).plusDays(days).toInstant());
    }

    public static final UTCCalendarDateRange createDefaultCalendarDateRange(DateType startType, DateType endType, Date startDate, Date endDate, boolean allDay, boolean fullSingleDay) {
        return new SimplestUTCCalendarDateRange(
            new SimplestUTCDisplayDate(startType, startDate, allDay),
            new SimplestUTCDisplayDate(endType, endDate, allDay),
            fullSingleDay
        );
    }

    public static final UTCCalendarDateRange createDefaultCalendarDateRangeForStart(UTCDisplayDate start, DateType endType) {
        Date startDate = start.getDate();
        boolean allDay = start.isAllDay();
        Date endDate = (allDay) ? startDate : new Date(startDate.getTime() + 1000L);
        return new SimplestUTCCalendarDateRange(start, new SimplestUTCDisplayDate(endType, endDate, allDay), allDay);
    }

    public static final UTCCalendarDateRange createDefaultCalendarDateRange(UTCDisplayDate start, UTCDisplayDate end, boolean forIcal)
        throws IllegalArgumentException {

        if (start == null || end == null) {
            return null;
        }

        boolean allDay = start.isAllDay();
        if (allDay != end.isAllDay()) {
            throw new IllegalArgumentException(
                "The date range start and end points must both be 'all day' or not 'all day' dates for " +
                start +
                " -> " +
                end);
        }

        boolean fullSingleDay = start.getDate().equals(end.getDate());

        if (forIcal && allDay) {
            // Increment the end date by one day because Outlook seems to need that.
            // [cjn 15.Aug.2013]
            end = new SimplestUTCDisplayDate(end.getType(), new Date(end.getDate().getTime() + HOURS_25_IN_MS), true);
        }

        return new SimplestUTCCalendarDateRange(start, end, fullSingleDay);

    }

    public static boolean areOnSameDay(Date d1, Date d2) {
        return areOnSameDay(d1, d2, TimeZoneUtil.getCurrentZoneId());
    }

    public static boolean areOnSameDay(Date d1, Date d2, ZoneId zoneId) {
        ZonedDateTime zdt1 = d1.toInstant().atZone(zoneId);
        ZonedDateTime zdt2 = d2.toInstant().atZone(zoneId);
        if (zdt1.getYear() == zdt2.getYear() && zdt1.getDayOfYear() == zdt2.getDayOfYear()) {
            return true;
        }
        return false;
    }

    public static final UTCCalendarDateRange getEventNewCalendarDateRange(DateType startType, DateType endType, long localStartTimeInMs, long localEndTimeInMs, boolean allDay) {

        Date startDate = new Date(localStartTimeInMs);
        if (allDay) {
            startDate = cvtMidnightLocalToGmt(startDate);
        }

        if (localEndTimeInMs == -1L) {
            return createDefaultCalendarDateRange(startType, endType, startDate, new Date(startDate.getTime() + 1000L), allDay, allDay);
        }

        Date endDate = new Date(localEndTimeInMs);
        if (allDay) {
            endDate = cvtMidnightLocalToGmt(endDate, TimeZoneUtil.getCurrentTimeZone());
        }
        return createDefaultCalendarDateRange(startType, endType, startDate, endDate, allDay, false);

    }

    public static final boolean setDefaultTimeZone(TimeZone newDefault) {
        TimeZone currentDefault = TimeZone.getDefault();
        if (!currentDefault.equals(newDefault)) {
            TimeZone.setDefault(newDefault);
            return true;
        }
        return false;
    }

}
