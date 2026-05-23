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

import java.text.DateFormat;
import java.util.Date;
import java.util.Objects;

public final class SimplestUTCDisplayDate implements UTCDisplayDate {

    public static final UTCDisplayDate createDefaultInstance(long rawDateTimeValue) {
        return createDefaultInstance(new Date(rawDateTimeValue));
    }

    public static final UTCDisplayDate createDefaultInstance(Date date) {
        Objects.requireNonNull(date, "Date");
        return new SimplestUTCDisplayDate(DateType.NONE, date, false);
    }

    public static final UTCDisplayDate createInstance(DateType type, Date rawDate, boolean allDay) {
        if (allDay) {
            return new SimplestUTCDisplayDate(type, DateUtil.cvtMidnightLocalToGmt(rawDate), true);
        }
        return new SimplestUTCDisplayDate(type, rawDate, false);
    }

    public static final UTCDisplayDate getWithNewType(UTCDisplayDate displayDate, DateType newType) {
        Objects.requireNonNull(displayDate, "UTCDisplayDate");
        Objects.requireNonNull(newType, "DateType");
        if (newType.equals(displayDate.getType())) {
            return displayDate;
        }
        return new SimplestUTCDisplayDate(newType,
                                          displayDate.getDate(),
                                          displayDate.isAllDay());
    }

    private final DateType type;

    private final Date date;

    private final boolean allDay;

    public SimplestUTCDisplayDate(DateType type, Date date, boolean allDay) {
        Objects.requireNonNull(type, "DateType");
        this.type = type;
        this.date = date;
        this.allDay = allDay;
    }

    @Override
    public final DateType getType() {
        return type;
    }

    @Override
    public final Date getDate() {
        return date;
    }

    @Override
    public final boolean isAllDay() {
        return allDay;
    }

    @Override
    public final boolean equals(Object other) {
        if (!(other instanceof UTCDisplayDate otherDate)) {
            return false;
        }
        if (otherDate.getDate().equals(date) && otherDate.isAllDay() == allDay) {
            return true;
        }
        return false;
    }

    @Override
    public final int hashCode() {
        return Objects.hash(date, allDay);
    }

    @Override
    public final String toString() {

        StringBuilder ret = new StringBuilder();
        ret.append(type);
        ret.append(": ");

        DateFormat format = DateFormat.getInstance();
        if (allDay) {
            format.setTimeZone(DateUtil.GMT);
        }
        ret.append(format.format(date));

        if (allDay) {
            ret.append(" (all day)");
        }

        return ret.toString();

    }

}
