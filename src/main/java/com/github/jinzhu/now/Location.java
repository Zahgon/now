package com.github.jinzhu.now;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.zone.ZoneRules;
import java.util.Locale;
import java.util.Objects;
import java.util.OptionalInt;
import java.util.TimeZone;

/**
 * Port of Go's {@code time.Location}.
 *
 * <p>{@link ZoneId} alone is not enough: Go carries a zone <em>name</em> next to the
 * offset rules, and {@code time.Parse} fabricates anonymous zones such as
 * {@code FixedZone("PST", 0)} when it meets an abbreviation the target location does
 * not know. This class keeps that name alongside the {@code ZoneId}.
 */
public final class Location {

    /** Mirrors Go's {@code time.UTC}. */
    public static final Location UTC = new Location("UTC", ZoneOffset.UTC, true, 0);

    private final String name;
    private final ZoneId zone;
    private final boolean fixed;
    private final int fixedOffset;
    private final TimeZone timeZone;

    private Location(String name, ZoneId zone, boolean fixed, int fixedOffset) {
        this.name = name;
        this.zone = zone;
        this.fixed = fixed;
        this.fixedOffset = fixedOffset;
        this.timeZone = fixed ? null : TimeZone.getTimeZone(zone);
    }

    public static Location of(ZoneId zone) {
        Objects.requireNonNull(zone, "zone");
        String id = zone.getId();
        if (zone.equals(ZoneOffset.UTC) || id.equals("UTC") || id.equals("Z")) {
            return UTC;
        }
        if (zone instanceof ZoneOffset offset) {
            return new Location(offset.getId(), offset, true, offset.getTotalSeconds());
        }
        return new Location(id, zone, false, 0);
    }

    /** Mirrors Go's {@code time.LoadLocation}. */
    public static Location load(String name) {
        return of(ZoneId.of(name));
    }

    /** Mirrors Go's {@code time.Local}. */
    public static Location local() {
        return of(ZoneId.systemDefault());
    }

    /** Mirrors Go's {@code time.FixedZone}. */
    public static Location fixed(String name, int offsetSeconds) {
        return new Location(name, ZoneOffset.ofTotalSeconds(offsetSeconds), true, offsetSeconds);
    }

    public ZoneId zoneId() {
        return zone;
    }

    public boolean isFixed() {
        return fixed;
    }

    /** Mirrors Go's {@code Location.String()}. */
    @Override
    public String toString() {
        return name;
    }

    public String name() {
        return name;
    }

    /** The zone offset, in seconds east of UTC, in effect at {@code at}. */
    public int offset(Instant at) {
        return fixed ? fixedOffset : zone.getRules().getOffset(at).getTotalSeconds();
    }

    /**
     * The zone abbreviation in effect at {@code at}, e.g. {@code "CST"} or {@code "CEST"}.
     *
     * <p>Go reads abbreviations straight out of the tzdata file; the JDK does not expose
     * them through {@link ZoneRules}, so the legacy {@link TimeZone} short display names
     * are used instead. They agree with tzdata for the zones this library is used with.
     */
    public String abbreviation(Instant at) {
        if (fixed) {
            return name;
        }
        return timeZone.getDisplayName(zone.getRules().isDaylightSavings(at), TimeZone.SHORT, Locale.US);
    }

    /**
     * Mirrors Go's {@code Location.lookupName}: find the offset this location uses for the
     * given zone abbreviation around the given wall-clock instant, if it uses it at all.
     */
    public OptionalInt lookupName(String abbreviation, long unixWallSeconds) {
        if (fixed) {
            return name.equals(abbreviation) ? OptionalInt.of(fixedOffset) : OptionalInt.empty();
        }
        ZoneRules rules = zone.getRules();
        Instant guess = Instant.ofEpochSecond(unixWallSeconds);
        int standard = rules.getStandardOffset(guess).getTotalSeconds();
        int current = rules.getOffset(guess).getTotalSeconds();
        for (int candidate : new int[]{current, standard, standard + 3600}) {
            Instant at = Instant.ofEpochSecond(unixWallSeconds - candidate);
            if (abbreviation(at).equals(abbreviation)) {
                return OptionalInt.of(rules.getOffset(at).getTotalSeconds());
            }
        }
        return OptionalInt.empty();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof Location other)) {
            return false;
        }
        return name.equals(other.name) && zone.equals(other.zone);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, zone);
    }
}
