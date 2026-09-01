package com.github.jinzhu.now;

import java.time.DayOfWeek;

/**
 * Port of Go's {@code time.Weekday}.
 *
 * <p>Note the numbering: Go counts {@code Sunday == 0} through {@code Saturday == 6},
 * whereas {@link DayOfWeek} counts {@code MONDAY == 1} through {@code SUNDAY == 7}.
 * {@code now}'s week arithmetic relies on Go's numbering, so it is preserved here.
 */
public enum Weekday {
    SUNDAY,
    MONDAY,
    TUESDAY,
    WEDNESDAY,
    THURSDAY,
    FRIDAY,
    SATURDAY;

    private static final String[] NAMES = {
            "Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday"
    };

    /** The Go weekday number: Sunday is 0, Saturday is 6. */
    public int value() {
        return ordinal();
    }

    /** Mirrors Go's {@code Weekday.String()}. */
    public String goName() {
        return NAMES[ordinal()];
    }

    public static Weekday of(int value) {
        return values()[Math.floorMod(value, 7)];
    }

    public static Weekday from(DayOfWeek dayOfWeek) {
        return values()[dayOfWeek.getValue() % 7];
    }

    public DayOfWeek toDayOfWeek() {
        return this == SUNDAY ? DayOfWeek.SUNDAY : DayOfWeek.of(ordinal());
    }
}
