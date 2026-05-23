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
import java.util.Objects;

public final class SimplestUTCCalendarDateRange implements UTCCalendarDateRange {

    private final UTCDisplayDate start;

    private final UTCDisplayDate end;

    private final boolean fullSingleDay;

    public SimplestUTCCalendarDateRange(UTCDisplayDate start, UTCDisplayDate end, boolean fullSingleDay) {
        this.start = start;
        this.end = end;
        this.fullSingleDay = fullSingleDay;
    }

    @Override
    public final UTCDisplayDate getStart() {
        return start;
    }

    @Override
    public final UTCDisplayDate getEnd() {
        return end;
    }

    @Override
    public final boolean isAllDay() {
        return start.isAllDay();
    }

    @Override
    public final boolean isFullSingleDay() {
        return fullSingleDay;
    }

    @Override
    public final boolean isWithinSingleDay() {
        if (fullSingleDay) {
            return true;
        }
        if (getStart().isAllDay()) {
            return false;
        }
        if (getStart().isOnSameDay(getEnd())) {
            return true;
        }
        return false;
    }

    @Override
    public final boolean equals(Object other) {
        if (!(other instanceof UTCCalendarDateRange otherRange)) {
            return false;
        }
        if (getStart().getDate().equals(otherRange.getStart().getDate()) &&
            getEnd().getDate().equals(otherRange.getEnd().getDate()) &&
            isAllDay() == otherRange.isAllDay() &&
            isFullSingleDay() == otherRange.isFullSingleDay()) {
            return true;
        }
        return false;
    }

    @Override
    public final int hashCode() {
        return Objects.hash(getStart().getDate(), getEnd().getDate(), isAllDay(), isFullSingleDay());
    }

    @Override
    public final String toString() {

        StringBuilder ret = new StringBuilder();

        ret.append("Calendar Date Range: ");

        DateFormat format = DateFormat.getInstance();
        if (isAllDay()) {
            format.setTimeZone(DateUtil.GMT);
        }

        ret.append(format.format(getStart().getDate()));
        ret.append(" -> ");
        ret.append(format.format(getEnd().getDate()));

        if (isAllDay()) {
            ret.append(" (all day)");
        }

        return ret + " GMT";

    }

}
