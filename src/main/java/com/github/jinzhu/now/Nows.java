package com.github.jinzhu.now;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Port of the package-level API in Go's {@code main.go}.
 *
 * <p>In Go these are plain functions in package {@code now}, called as
 * {@code now.BeginningOfDay()} alongside the methods on {@code *now.Now}. Java cannot put a
 * static and an instance method with the same signature on one class, so the functions live
 * here and the struct methods live on {@link Now}.
 *
 * <pre>{@code
 * Nows.beginningOfDay();          // today at 00:00:00
 * Nows.with(t).endOfMonth();      // end of t's month
 * }</pre>
 */
public final class Nows {

    private Nows() {
    }

    /** Sets the day a week starts on; the default is Sunday. */
    public static Weekday weekStartDay = Weekday.SUNDAY;

    /** The time formats strings are parsed as. */
    public static List<String> timeFormats = new ArrayList<>(Arrays.asList(
            "2006", "2006-1", "2006-1-2", "2006-1-2 15", "2006-1-2 15:4", "2006-1-2 15:4:5", "1-2",
            "15:4:5", "15:4", "15",
            "15:4:5 Jan 2, 2006 MST", "2006-01-02 15:04:05.999999999 -0700 MST",
            "2006-01-02T15:04:05Z0700", "2006-01-02T15:04:05Z07",
            "2006.1.2", "2006.1.2 15:04:05", "2006.01.02", "2006.01.02 15:04:05",
            "2006.01.02 15:04:05.999999999",
            "1/2/2006", "1/2/2006 15:4:5", "2006/01/02", "20060102", "2006/01/02 15:04:05",
            GoTime.ANSIC, GoTime.UNIX_DATE, GoTime.RUBY_DATE, GoTime.RFC822, GoTime.RFC822Z, GoTime.RFC850,
            GoTime.RFC1123, GoTime.RFC1123Z, GoTime.RFC3339, GoTime.RFC3339_NANO,
            GoTime.KITCHEN, GoTime.STAMP, GoTime.STAMP_MILLI, GoTime.STAMP_MICRO, GoTime.STAMP_NANO));

    /** The default configuration; when null one is built from the fields above. */
    public static Config defaultConfig;

    /** Initializes {@link Now} with a time. */
    public static Now with(GoTime t) {
        Config config = defaultConfig;
        if (config == null) {
            config = new Config(weekStartDay, null, timeFormats);
        }
        return new Now(t, config);
    }

    /** Initializes {@link Now} with a time. Mirrors Go's {@code New}. */
    public static Now newInstance(GoTime t) {
        return with(t);
    }

    public static GoTime beginningOfMinute() {
        return with(GoTime.now()).beginningOfMinute();
    }

    public static GoTime beginningOfHour() {
        return with(GoTime.now()).beginningOfHour();
    }

    public static GoTime beginningOfDay() {
        return with(GoTime.now()).beginningOfDay();
    }

    public static GoTime beginningOfWeek() {
        return with(GoTime.now()).beginningOfWeek();
    }

    public static GoTime beginningOfMonth() {
        return with(GoTime.now()).beginningOfMonth();
    }

    public static GoTime beginningOfQuarter() {
        return with(GoTime.now()).beginningOfQuarter();
    }

    public static GoTime beginningOfYear() {
        return with(GoTime.now()).beginningOfYear();
    }

    public static GoTime endOfMinute() {
        return with(GoTime.now()).endOfMinute();
    }

    public static GoTime endOfHour() {
        return with(GoTime.now()).endOfHour();
    }

    public static GoTime endOfDay() {
        return with(GoTime.now()).endOfDay();
    }

    public static GoTime endOfWeek() {
        return with(GoTime.now()).endOfWeek();
    }

    public static GoTime endOfMonth() {
        return with(GoTime.now()).endOfMonth();
    }

    public static GoTime endOfQuarter() {
        return with(GoTime.now()).endOfQuarter();
    }

    public static GoTime endOfYear() {
        return with(GoTime.now()).endOfYear();
    }

    /** Returns the Monday of the current week. */
    public static GoTime monday(String... strs) {
        return with(GoTime.now()).monday(strs);
    }

    /** Returns the Sunday of the current week. */
    public static GoTime sunday(String... strs) {
        return with(GoTime.now()).sunday(strs);
    }

    public static GoTime endOfSunday() {
        return with(GoTime.now()).endOfSunday();
    }

    /** Returns the yearly quarter. */
    public static int quarter() {
        return with(GoTime.now()).quarter();
    }

    /** Parses strings to a time. */
    public static GoTime parse(String... strs) throws TimeParseException {
        return with(GoTime.now()).parse(strs);
    }

    /** Parses strings to a time in the given location. */
    public static GoTime parseInLocation(Location location, String... strs) throws TimeParseException {
        return with(GoTime.now().in(location)).parse(strs);
    }

    /** Parses strings to a time, or throws. */
    public static GoTime mustParse(String... strs) {
        return with(GoTime.now()).mustParse(strs);
    }

    /** Parses strings to a time in the given location, or throws. */
    public static GoTime mustParseInLocation(Location location, String... strs) {
        return with(GoTime.now().in(location)).mustParse(strs);
    }

    /** Checks whether the current time falls between the two given times. */
    public static boolean between(String time1, String time2) {
        return with(GoTime.now()).between(time1, time2);
    }
}
