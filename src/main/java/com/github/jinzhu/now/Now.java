package com.github.jinzhu.now;

import java.time.DateTimeException;
import java.time.Duration;
import java.util.regex.Pattern;

/**
 * Port of the {@code now.Now} struct. Go embeds {@code time.Time} in it, which Java models
 * as extending {@link GoTime}, so every time method is available on a {@code Now} directly.
 *
 * <p>The package-level helpers from Go's {@code main.go} live on {@link Nows}, because Java
 * cannot host a static and an instance method of the same signature on one class.
 */
public class Now extends GoTime {

    // Matches 15:04:05, 15:04:05.000, 15:04:05.000000, 15, 2017-01-01 15:04,
    // 2021-07-20T00:59:10Z, 2021-07-20T00:59:10+08:00, 2021-07-20T00:00:10-07:00 etc.
    private static final Pattern HAS_TIME_REGEXP = Pattern.compile(
            "(\\s+|^\\s*|T)\\d{1,2}((:\\d{1,2})*|((:\\d{1,2}){2}\\.(\\d{3}|\\d{6}|\\d{9})))(\\s*$|[Z+-])");

    // Matches 15:04:05, 15, 15:04:05.000, 15:04:05.000000, etc.
    private static final Pattern ONLY_TIME_REGEXP = Pattern.compile(
            "^\\s*\\d{1,2}((:\\d{1,2})*|((:\\d{1,2}){2}\\.(\\d{3}|\\d{6}|\\d{9})))\\s*$");

    private static final Duration NANOSECOND = Duration.ofNanos(1);

    public final Config config;

    public Now(GoTime time, Config config) {
        super(time);
        this.config = config;
    }

    public Config config() {
        return config;
    }

    /** Port of {@code time.go}: the time's fields, least significant first. */
    static int[] formatTimeToList(GoTime t) {
        return new int[]{t.nanosecond(), t.second(), t.minute(), t.hour(), t.day(), t.month(), t.year()};
    }

    // -------------------------------------------------------------- beginning of

    /** Beginning of minute. */
    public GoTime beginningOfMinute() {
        return truncate(Duration.ofMinutes(1));
    }

    /** Beginning of hour. */
    public GoTime beginningOfHour() {
        return GoTime.date(year(), month(), day(), hour(), 0, 0, 0, location());
    }

    /** Beginning of day. */
    public GoTime beginningOfDay() {
        return GoTime.date(year(), month(), day(), 0, 0, 0, 0, location());
    }

    /** Beginning of week. */
    public GoTime beginningOfWeek() {
        GoTime t = beginningOfDay();
        int weekday = t.weekday().value();

        if (config.weekStartDay != Weekday.SUNDAY) {
            int weekStartDayInt = config.weekStartDay.value();

            if (weekday < weekStartDayInt) {
                weekday = weekday + 7 - weekStartDayInt;
            } else {
                weekday = weekday - weekStartDayInt;
            }
        }
        return t.addDate(0, 0, -weekday);
    }

    /** Beginning of month. */
    public GoTime beginningOfMonth() {
        return GoTime.date(year(), month(), 1, 0, 0, 0, 0, location());
    }

    /** Beginning of quarter. */
    public GoTime beginningOfQuarter() {
        GoTime month = beginningOfMonth();
        int offset = (month.month() - 1) % 3;
        return month.addDate(0, -offset, 0);
    }

    /** Beginning of half year. */
    public GoTime beginningOfHalf() {
        GoTime month = beginningOfMonth();
        int offset = (month.month() - 1) % 6;
        return month.addDate(0, -offset, 0);
    }

    /** Beginning of year. */
    public GoTime beginningOfYear() {
        return GoTime.date(year(), 1, 1, 0, 0, 0, 0, location());
    }

    // -------------------------------------------------------------------- end of

    /** End of minute. */
    public GoTime endOfMinute() {
        return beginningOfMinute().add(Duration.ofMinutes(1).minus(NANOSECOND));
    }

    /** End of hour. */
    public GoTime endOfHour() {
        return beginningOfHour().add(Duration.ofHours(1).minus(NANOSECOND));
    }

    /** End of day. */
    public GoTime endOfDay() {
        return GoTime.date(year(), month(), day(), 23, 59, 59, 999_999_999, location());
    }

    /** End of week. */
    public GoTime endOfWeek() {
        return beginningOfWeek().addDate(0, 0, 7).add(NANOSECOND.negated());
    }

    /** End of month. */
    public GoTime endOfMonth() {
        return beginningOfMonth().addDate(0, 1, 0).add(NANOSECOND.negated());
    }

    /** End of quarter. */
    public GoTime endOfQuarter() {
        return beginningOfQuarter().addDate(0, 3, 0).add(NANOSECOND.negated());
    }

    /** End of half year. */
    public GoTime endOfHalf() {
        return beginningOfHalf().addDate(0, 6, 0).add(NANOSECOND.negated());
    }

    /** End of year. */
    public GoTime endOfYear() {
        return beginningOfYear().addDate(1, 0, 0).add(NANOSECOND.negated());
    }

    // ------------------------------------------------------------ monday, sunday

    /** Returns the Monday of the week specified by this time. */
    public GoTime monday(String... strs) {
        GoTime parseTime;
        if (strs.length > 0) {
            parseTime = mustParse(strs);
        } else {
            parseTime = beginningOfDay();
        }
        int weekday = parseTime.weekday().value();
        if (weekday == 0) {
            weekday = 7;
        }
        return parseTime.addDate(0, 0, -weekday + 1);
    }

    /** Returns the Sunday of the week specified by this time. */
    public GoTime sunday(String... strs) {
        GoTime parseTime;
        if (strs.length > 0) {
            parseTime = mustParse(strs);
        } else {
            parseTime = beginningOfDay();
        }
        int weekday = parseTime.weekday().value();
        if (weekday == 0) {
            weekday = 7;
        }
        return parseTime.addDate(0, 0, 7 - weekday);
    }

    /** End of Sunday. */
    public GoTime endOfSunday() {
        return Nows.newInstance(sunday()).endOfDay();
    }

    /** Returns the yearly quarter. */
    public int quarter() {
        return (month() - 1) / 3 + 1;
    }

    // -------------------------------------------------------------------- parsing

    private GoTime parseWithFormat(String str, Location location) throws TimeParseException {
        for (String format : config.timeFormats) {
            try {
                return GoTime.parseInLocation(format, str, location);
            } catch (TimeParseException ignored) {
                // Try the next format.
            }
        }
        throw new TimeParseException("Can't parse string as time: " + str);
    }

    /** Parses strings to a time. */
    public GoTime parse(String... strs) throws TimeParseException {
        boolean setCurrentTime = false;
        Location currentLocation = location();
        boolean onlyTimeInStr = true;
        int[] currentTime = formatTimeToList(this);

        GoTime t = GoTime.ZERO;
        TimeParseException err = null;

        for (String str : strs) {
            boolean hasTimeInStr = HAS_TIME_REGEXP.matcher(str).find(); // match 15:04:05, 15
            onlyTimeInStr = hasTimeInStr && onlyTimeInStr && ONLY_TIME_REGEXP.matcher(str).find();

            try {
                t = parseWithFormat(str, currentLocation);
                err = null;
            } catch (TimeParseException e) {
                t = GoTime.ZERO;
                err = e;
                continue;
            }

            Location location = t.location();
            int[] parseTime = formatTimeToList(t);

            for (int i = 0; i < parseTime.length; i++) {
                // Don't reset hour, minute, second if the current time string includes a time.
                if (hasTimeInStr && i <= 3) {
                    continue;
                }

                // If the value is zero, replace it with the current time.
                if (parseTime[i] == 0) {
                    if (setCurrentTime) {
                        parseTime[i] = currentTime[i];
                    }
                } else {
                    setCurrentTime = true;
                }

                // If the current time string only includes a time, the day and month
                // should come from the current time.
                if (onlyTimeInStr && (i == 4 || i == 5)) {
                    parseTime[i] = currentTime[i];
                }
            }

            t = GoTime.date(parseTime[6], parseTime[5], parseTime[4], parseTime[3],
                    parseTime[2], parseTime[1], parseTime[0], location);
            currentTime = formatTimeToList(t);
        }

        if (err != null) {
            throw err;
        }
        return t;
    }

    /** Parses strings to a time, or throws. Mirrors Go's {@code MustParse}, which panics. */
    public GoTime mustParse(String... strs) {
        try {
            return parse(strs);
        } catch (TimeParseException e) {
            throw new DateTimeException(e.getMessage(), e);
        }
    }

    /** Checks whether this time falls between the begin and end times. */
    public boolean between(String begin, String end) {
        GoTime beginTime = mustParse(begin);
        GoTime endTime = mustParse(end);
        return after(beginTime) && before(endTime);
    }
}
