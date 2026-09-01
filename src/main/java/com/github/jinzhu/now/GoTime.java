package com.github.jinzhu.now;

import java.math.BigInteger;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.zone.ZoneOffsetTransition;
import java.time.zone.ZoneRules;
import java.util.Objects;

/**
 * Port of Go's {@code time.Time}: an instant paired with the {@link Location} it is
 * interpreted in.
 *
 * <p>{@link ZonedDateTime} is almost the same thing, and {@link #toZonedDateTime()} hands
 * one over, but it cannot carry the fabricated zones that {@code time.Parse} produces (see
 * {@link Location}), and it resolves DST gaps and overlaps by different rules than Go does.
 * Both matter to this library, so the Go semantics are reproduced here.
 */
public class GoTime implements Comparable<GoTime> {

    /** Seconds between January 1, year 1 (Go's absolute epoch) and the Unix epoch. */
    private static final long UNIX_TO_INTERNAL = 62135596800L;

    private static final BigInteger NANOS_PER_SECOND = BigInteger.valueOf(1_000_000_000L);

    // Reference layouts, mirroring the constants in Go's time package.
    public static final String ANSIC = "Mon Jan _2 15:04:05 2006";
    public static final String UNIX_DATE = "Mon Jan _2 15:04:05 MST 2006";
    public static final String RUBY_DATE = "Mon Jan 02 15:04:05 -0700 2006";
    public static final String RFC822 = "02 Jan 06 15:04 MST";
    public static final String RFC822Z = "02 Jan 06 15:04 -0700";
    public static final String RFC850 = "Monday, 02-Jan-06 15:04:05 MST";
    public static final String RFC1123 = "Mon, 02 Jan 2006 15:04:05 MST";
    public static final String RFC1123Z = "Mon, 02 Jan 2006 15:04:05 -0700";
    public static final String RFC3339 = "2006-01-02T15:04:05Z07:00";
    public static final String RFC3339_NANO = "2006-01-02T15:04:05.999999999Z07:00";
    public static final String KITCHEN = "3:04PM";
    public static final String STAMP = "Jan _2 15:04:05";
    public static final String STAMP_MILLI = "Jan _2 15:04:05.000";
    public static final String STAMP_MICRO = "Jan _2 15:04:05.000000";
    public static final String STAMP_NANO = "Jan _2 15:04:05.000000000";

    /** Mirrors Go's zero {@code Time}: January 1, year 1, 00:00:00 UTC. */
    public static final GoTime ZERO = date(1, 1, 1, 0, 0, 0, 0, Location.UTC);

    private final Instant instant;
    private final Location location;
    private final ZonedDateTime zoned;

    protected GoTime(Instant instant, Location location) {
        this.instant = instant;
        this.location = location;
        this.zoned = instant.atZone(location.zoneId());
    }

    protected GoTime(GoTime other) {
        this(other.instant, other.location);
    }

    public static GoTime ofInstant(Instant instant, Location location) {
        return new GoTime(Objects.requireNonNull(instant), Objects.requireNonNull(location));
    }

    public static GoTime of(ZonedDateTime zonedDateTime) {
        return new GoTime(zonedDateTime.toInstant(), Location.of(zonedDateTime.getZone()));
    }

    /** Mirrors Go's {@code time.Now}. */
    public static GoTime now() {
        return new GoTime(Instant.now(), Location.local());
    }

    /**
     * Port of Go's {@code time.Date}, including its normalisation of out-of-range fields
     * and its resolution of wall-clock times that fall in a DST gap or overlap.
     */
    public static GoTime date(int year, int month, int day, int hour, int minute, int second,
                              int nanosecond, Location location) {
        // Normalise nanosecond, second, minute and hour, overflowing into day.
        long[] n = norm(second, nanosecond, 1_000_000_000L);
        long sec = n[0];
        long nsec = n[1];
        n = norm(minute, sec, 60);
        long min = n[0];
        sec = n[1];
        n = norm(hour, min, 60);
        long hr = n[0];
        min = n[1];
        n = norm(day, hr, 24);
        long dy = n[0];
        hr = n[1];
        // Normalise the month, overflowing into the year.
        n = norm(year, month - 1L, 12);
        long yr = n[0];
        long mon = n[1] + 1;

        LocalDate localDate = LocalDate.of(Math.toIntExact(yr), 1, 1)
                .plusMonths(mon - 1)
                .plusDays(dy - 1);
        LocalDateTime localDateTime = LocalDateTime.of(localDate,
                LocalTime.of((int) hr, (int) min, (int) sec, (int) nsec));

        long unix = localDateTime.toEpochSecond(ZoneOffset.UTC);
        ZoneRules rules = location.zoneId().getRules();
        // Look up the offset for the expected time so it can be adjusted to UTC. The lookup
        // wants UTC, so try `unix` first and correct it if that lands in another zone period.
        int offset = rules.getOffset(Instant.ofEpochSecond(unix)).getTotalSeconds();
        if (offset != 0) {
            long utc = unix - offset;
            Instant at = Instant.ofEpochSecond(unix);
            ZoneOffsetTransition previous = rules.previousTransition(at);
            ZoneOffsetTransition next = rules.nextTransition(at);
            long start = previous == null ? Long.MIN_VALUE : previous.toEpochSecond();
            long end = next == null ? Long.MAX_VALUE : next.toEpochSecond();
            if (utc < start || utc >= end) {
                offset = rules.getOffset(Instant.ofEpochSecond(utc)).getTotalSeconds();
            }
            unix -= offset;
        }
        return new GoTime(Instant.ofEpochSecond(unix, nsec), location);
    }

    /** Port of Go's {@code norm}: move {@code lo} into {@code [0, base)}, carrying into {@code hi}. */
    private static long[] norm(long hi, long lo, long base) {
        if (lo < 0) {
            long n = (-lo - 1) / base + 1;
            hi -= n;
            lo += n * base;
        }
        if (lo >= base) {
            long n = lo / base;
            hi += n;
            lo -= n * base;
        }
        return new long[]{hi, lo};
    }

    /** Port of Go's {@code time.Parse}: parse in UTC unless the value says otherwise. */
    public static GoTime parse(String layout, String value) throws TimeParseException {
        Location local = Location.local();
        return GoLayout.parse(layout, value, local, local);
    }

    /** Port of Go's {@code time.ParseInLocation}. */
    public static GoTime parseInLocation(String layout, String value, Location location)
            throws TimeParseException {
        return GoLayout.parse(layout, value, location, location);
    }

    // ------------------------------------------------------------------ accessors

    public int year() {
        return zoned.getYear();
    }

    /** The month of the year, 1 through 12. */
    public int month() {
        return zoned.getMonthValue();
    }

    public int day() {
        return zoned.getDayOfMonth();
    }

    public int hour() {
        return zoned.getHour();
    }

    public int minute() {
        return zoned.getMinute();
    }

    public int second() {
        return zoned.getSecond();
    }

    public int nanosecond() {
        return zoned.getNano();
    }

    public int yearDay() {
        return zoned.getDayOfYear();
    }

    public Weekday weekday() {
        return Weekday.from(zoned.getDayOfWeek());
    }

    public Location location() {
        return location;
    }

    /** Mirrors Go's {@code Time.Zone}. */
    public Zone zone() {
        return new Zone(location.abbreviation(instant), location.offset(instant));
    }

    /** The abbreviation and offset of the zone in effect at this time. */
    public record Zone(String name, int offset) {
    }

    public Instant toInstant() {
        return instant;
    }

    public ZonedDateTime toZonedDateTime() {
        return zoned;
    }

    /** Mirrors Go's {@code Time.Unix}. */
    public long unix() {
        return instant.getEpochSecond();
    }

    // ----------------------------------------------------------------- arithmetic

    /** Mirrors Go's {@code Time.Add}: shifts the instant, not the wall clock. */
    public GoTime add(Duration duration) {
        return new GoTime(instant.plus(duration), location);
    }

    /**
     * Mirrors Go's {@code Time.AddDate}: shifts the calendar fields and renormalises, so
     * adding a day across a DST boundary keeps the wall-clock time of day.
     */
    public GoTime addDate(int years, int months, int days) {
        return date(year() + years, month() + months, day() + days,
                hour(), minute(), second(), nanosecond(), location);
    }

    /**
     * Mirrors Go's {@code Time.Truncate}: rounds down to a multiple of {@code duration}
     * since the zero time. Like Go, this works on the absolute time, not the wall clock.
     */
    public GoTime truncate(Duration duration) {
        if (duration.isZero() || duration.isNegative()) {
            return this;
        }
        BigInteger total = BigInteger.valueOf(instant.getEpochSecond() + UNIX_TO_INTERNAL)
                .multiply(NANOS_PER_SECOND)
                .add(BigInteger.valueOf(instant.getNano()));
        BigInteger remainder = total.mod(BigInteger.valueOf(duration.toNanos()));
        return new GoTime(instant.minusNanos(remainder.longValueExact()), location);
    }

    /** Mirrors Go's {@code Time.In}. */
    public GoTime in(Location location) {
        return new GoTime(instant, location);
    }

    public boolean before(GoTime other) {
        return instant.isBefore(other.instant);
    }

    public boolean after(GoTime other) {
        return instant.isAfter(other.instant);
    }

    /** Mirrors Go's {@code Time.Equal}: compares the instant, ignoring the location. */
    public boolean equalTime(GoTime other) {
        return instant.equals(other.instant);
    }

    @Override
    public int compareTo(GoTime other) {
        return instant.compareTo(other.instant);
    }

    // ----------------------------------------------------------------- formatting

    /** Mirrors Go's {@code Time.Format}, taking a Go reference layout. */
    public String format(String layout) {
        return GoLayout.format(this, layout);
    }

    /** Mirrors Go's {@code Time.String}. */
    @Override
    public String toString() {
        return format("2006-01-02 15:04:05.999999999 -0700 MST");
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof GoTime other)) {
            return false;
        }
        return instant.equals(other.instant) && location.equals(other.location);
    }

    @Override
    public int hashCode() {
        return Objects.hash(instant, location);
    }
}
