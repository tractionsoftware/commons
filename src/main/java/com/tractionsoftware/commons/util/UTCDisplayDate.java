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

import java.util.Calendar;
import java.util.Date;

/**
 * UTCDisplayDate encapsulates a particular a date-time value in the UTC/GMT time zone, and which may need to be
 * adjusted if necessary with respect to the current thread's default time zone before being displayed (i.e.,
 * formatted). It has an associated {@link DateType} to indicate how it should be handled.
 *
 * <p>
 * An "all day" UTCDisplayDate refers abstractly to <em>the day corresponding to a particular date</em>, independent of
 * time zone. By convention, the raw Date for an "all day" date corresponds to midnight on a particular date in the
 * UTC/GMT time zone. Before being used, such a value needs to be adjusted so that it corresponds to midnight in the
 * thread's current time zone.
 *
 * <p>
 * A UTCDisplayDate that is not "all day" is just like an ordinary date: it refers to a particular instant in time, and
 * no adjustment is required.
 *
 * <p>
 * To obtain a Date suitable for formatting in the current time zone, clients should use {@link #getAdjustedDate()}
 * since the returned value will always have any necessary adjustment applied.
 *
 * <p>
 * To obtain the raw and unadjusted Date suitable for storing the real value (in an entry property, a database, or
 * anywhere else that a time zone agnostic value is required), clients should use {@link #getDate()} since no adjustment
 * will be applied to that value.
 *
 * @author Dave Shepperton
 */
public interface UTCDisplayDate extends Comparable<UTCDisplayDate> {

    /**
     * Returns the {@link DateType} associated with this date value.
     *
     * <p>
     * This method must never return null, and should at least return a place-holder such as {@link DateType#NONE}.
     *
     * @return the {@link DateType} associated with this date value, never null.
     */
    public DateType getType();

    /**
     * Returns the raw {@link Date} value for this UTCDisplayDate. If {@link #isAllDay() this is an all-day date}, this
     * value will correspond to midnight UTC/GMT on a particular day. Otherwise, it will simply correspond to some
     * arbitrary date-time.
     *
     * <p>
     * Clients should use this method to obtain the raw and unadjusted Date suitable for persisting, e.g., as an entry
     * property value.
     *
     * @return the raw {@link Date} value for this UTCDisplayDate.
     */
    public Date getDate();

    /**
     * Returns a {@link Date} that has had any necessary adjustment applied for the time zone for the current thread. If
     * {@link #isAllDay() this is an all-day date}, this value will correspond to midnight on a particular day in the
     * time zone for the current thread. Otherwise, it will simply correspond to some arbitrary date-time.
     *
     * <p>
     * Clients should generally use this method to obtain a Date suitable for formatting in the thread's current time
     * zone. If the intention is to adjust it at a later time, or for a time zone other than the current one for the
     * thread at the time this method is invoked, or to persist the raw value somewhere, use {@link #getDate()}
     * instead.
     *
     * <p>
     * This default method defers to {@link DateUtil#getDateAdjustedForAllDay(UTCDisplayDate)}, which should be suitable
     * for all implementations.
     *
     * @return {@link #getDate() this UTCDisplayDate's Date} with an adjustment applied if this represents
     *     {@link #isAllDay() an all-day date}; otherwise, the Date as-is.
     */
    public default Date getAdjustedDate() {
        return DateUtil.getDateAdjustedForAllDay(this);
    }

    public default Calendar getAdjustedCalendar() {
        return DateUtil.getCalendarAdjustedForAllDay(this);
    }

    /**
     * Returns true if this UTCDisplayDate represents an "all day" date, referring abstractly to <em>the day
     * corresponding to a particular date</em>, independent of time zone.
     *
     * @return true if this UTCDisplayDate represents an "all day" date, referring abstractly to <em>the day
     *     corresponding to a particular date</em>, independent of time zone; false otherwise.
     */
    public boolean isAllDay();

    /**
     * Returns true if this UTCDisplayDate and the other given UTCDisplayDate both represent dates that are on the same
     * calendar day for the requesting user's time zone.
     *
     * @param other
     *     the other UTCDisplayDate.
     * @return true if this UTCDisplayDate and the other given UTCDisplayDate both represent dates that are on the same
     *     calendar day for the requesting user's time zone; false otherwise.
     */
    public default boolean isOnSameDay(UTCDisplayDate other) {
        if (other == null) {
            return false;
        }
        return DateUtil.areOnSameDay(getAdjustedDate(), other.getAdjustedDate());
    }

    @Override
    public default int compareTo(UTCDisplayDate otherDisplayDate) {
        return getDate().compareTo(otherDisplayDate.getDate());
    }

}
