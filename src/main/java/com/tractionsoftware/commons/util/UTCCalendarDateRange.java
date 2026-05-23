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

import com.google.common.annotations.Beta;
import org.threeten.extra.Interval;

import java.util.Date;
import java.util.Objects;
import java.util.TimeZone;

/**
 * Encapsulates access to two raw Dates representing the start and end points for a date/time range, plus another
 * property indicating whether this range should should be displayed as spanning "all day" on the days it covers.
 *
 * <p>
 * "All day" ranges should always be displayed as covering the entire days from the start and end points (inclusive)
 * regardless of time zone of the viewing user. This requires translating each Date from its given value, which should
 * be considered to represent midnight GMT on the day in question, to the equivalent Date in the viewing user's
 * preferred time zone. Contrast that with the normal display behavior for dates, in which the date is displayed mapped
 * to the corresponding time in the viewing user's time zone.
 *
 * @author Dave Shepperton
 */
public interface UTCCalendarDateRange {

    /**
     * Returns a UTCDisplayDate object representing the start start point of this range.
     *
     * @return a UTCDisplayDate object representing the start start point of this range.
     */
    public UTCDisplayDate getStart();

    /**
     * Returns a UTCDisplayDate object representing the end start point of this range.
     *
     * @return a UTCDisplayDate object representing the end start point of this range.
     */
    public UTCDisplayDate getEnd();

    /**
     * Returns true if this date range covers the entire days for the range of calendar dates it covers.
     *
     * <p>
     * If this method returns true, the correct display in the context of a calendar would require translating each of
     * the start and end point Dates for this range to their equivalents for midnight on the same day as seen same time
     * in the viewing user's preferred time zone. Otherwise, the Dates representing the start and end points may be
     * treated as a literal representation of those points.
     *
     * @return true if this date range covers the entire days for the range of calendar dates it covers; false
     *     otherwise.
     */
    public boolean isAllDay();

    /**
     * Returns true if this date range covers one single calendar day (must be "all day").
     *
     * @return true if this date range covers one single calendar day; false otherwise.
     */
    public boolean isFullSingleDay();

    /**
     * Returns true if the range covers a time span within a single calendar day for the requesting user's time zone.
     * This method will always return true if {@link #isFullSingleDay()} returns true, since the span would be exactly
     * that one single day. Otherwise, it will return true if the start and end date both refer to specific times on the
     * same calendar day.
     *
     * @return true if the range covers a time span within a single calendar day for the requesting user's time zone.
     */
    public boolean isWithinSingleDay();

    /**
     * Returns true if this event is "in progress" because the current {@link Date} is within the interval it covers --
     * i.e., after the start date/time and before the end date/time.
     *
     * @param now
     *     the current date/time value.
     * @return true if this event is "in progress" because the current {@link Date} is within the interval it covers;
     *     false otherwise. Note: if the start Date is null, this method returns false; but if the end Date is null,
     *     this method returns true if the current date/time has already been established to be after the start
     *     date/time.
     */
    public default boolean isInProgress(Date now) {
        if (contains(now)) {
            return true;
        }
        return false;
    }

    public default boolean contains(UTCDisplayDate date) {
        if (date == null) {
            return false;
        }
        return contains(date.getAdjustedDate());
    }

    public default boolean contains(Date date) {
        if (asInterval().contains(date.toInstant())) {
            return true;
        }
        return false;
    }

    /**
     * Returns true if this event is "complete" because the entire interval it covers has passed.
     *
     * @param now
     *     the current date/time value.
     * @return true if this event is "complete" because the entire interval it covers has passed; false otherwise,
     *     including if there is no {@link #getEnd() end date}.
     */
    public default boolean isComplete(Date now) {
        UTCDisplayDate end = getEnd();
        if (end == null) {
            return false;
        }
        Date endDate = end.getAdjustedDate();
        if (endDate.equals(now) || endDate.before(now)) {
            return true;
        }
        return false;
    }

    /**
     * Returns true if this event is either in the future or is {@link #isInProgress(Date) in progress}.
     *
     * <p>
     * This implementation simply tests whether the {@link #getEnd() end date/time} is after the given current
     * date/time. It returns false if the end date/time is missing. Unless an implementation requires special handling
     * for a missing {@link #getStart() start date/time}, it should not need to be overridden.
     *
     * @param now
     *     the current date/time value.
     * @return true if this event is either in the future or is {@link #isInProgress(Date) in progress}.
     */
    public default boolean isIncomplete(Date now) {
        UTCDisplayDate end = getEnd();
        if (end == null) {
            return false;
        }
        Date endDate = end.getAdjustedDate();
        if (endDate.equals(now) || now.before(endDate)) {
            return true;
        }
        return false;
    }

    /**
     * Returns an {@link Interval} equivalent to this UTCCalendarDateRange, with respect to preferred {@link TimeZone}
     * for the current request.
     *
     * @return an {@link Interval} equivalent to this UTCCalendarDateRange, with respect to preferred {@link TimeZone}
     *     for the current request.
     */
    @Beta
    public default Interval asInterval() {
        return asInterval(null);
    }

    /**
     * Returns an {@link Interval} equivalent to this UTCCalendarDateRange, with respect to the given {@link TimeZone}.
     *
     * @param zone
     *     the {@link TimeZone} that should be used to create the {@link Interval}.
     * @return an {@link Interval} equivalent to this UTCCalendarDateRange, with respect to the given {@link TimeZone}.
     * @implNote We pass the {@link UTCDisplayDate#getDate() raw Date value} for both start and end date-time
     *     values, because that is how {@link DateUtil#createInterval(Date, Date, boolean, TimeZone)} expects things to
     *     be.
     */
    @Beta
    public default Interval asInterval(TimeZone zone) {
        return DateUtil.createInterval(
            getStart().getDate(),
            getEnd().getDate(),
            isAllDay(),
            Objects.requireNonNullElseGet(zone, TimeZoneUtil::getCurrentTimeZone)
        );
    }

}
