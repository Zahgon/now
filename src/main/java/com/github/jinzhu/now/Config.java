package com.github.jinzhu.now;

import java.util.ArrayList;
import java.util.List;

/**
 * Port of {@code now.Config}: configuration for the {@code now} package.
 *
 * <p>The fields are public and mutable, mirroring the exported fields of the Go struct.
 */
public class Config {

    public Weekday weekStartDay = Weekday.SUNDAY;

    /** The location parsing happens in; when null the current location is used. */
    public Location timeLocation;

    public List<String> timeFormats = new ArrayList<>();

    public Config() {
    }

    public Config(Weekday weekStartDay, Location timeLocation, List<String> timeFormats) {
        this.weekStartDay = weekStartDay;
        this.timeLocation = timeLocation;
        this.timeFormats = new ArrayList<>(timeFormats);
    }

    /** Initializes a {@link Now} based on this configuration. */
    public Now with(GoTime t) {
        return new Now(t, this);
    }

    /** Parses strings to a time based on this configuration. */
    public GoTime parse(String... strs) throws TimeParseException {
        if (timeLocation == null) {
            return with(GoTime.now()).parse(strs);
        }
        return with(GoTime.now().in(timeLocation)).parse(strs);
    }

    /** Parses strings to a time based on this configuration, or throws. */
    public GoTime mustParse(String... strs) {
        if (timeLocation == null) {
            return with(GoTime.now()).mustParse(strs);
        }
        return with(GoTime.now().in(timeLocation)).mustParse(strs);
    }
}
