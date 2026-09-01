package com.github.jinzhu.now;

import java.time.DateTimeException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.OptionalInt;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Port of the reference-layout scanner, formatter and parser from Go's {@code time}
 * package, so that {@code now}'s layouts ({@code "2006-01-02 15:04:05"} and friends)
 * keep working unchanged.
 */
final class GoLayout {

    private GoLayout() {
    }

    // Chunk kinds, mirroring Go's std* constants.
    static final int NONE = 0;
    static final int LONG_MONTH = 1;                // "January"
    static final int MONTH = 2;                     // "Jan"
    static final int NUM_MONTH = 3;                 // "1"
    static final int ZERO_MONTH = 4;                // "01"
    static final int LONG_WEEKDAY = 5;              // "Monday"
    static final int WEEKDAY = 6;                   // "Mon"
    static final int DAY = 7;                       // "2"
    static final int UNDER_DAY = 8;                 // "_2"
    static final int ZERO_DAY = 9;                  // "02"
    static final int UNDER_YEAR_DAY = 10;           // "__2"
    static final int ZERO_YEAR_DAY = 11;            // "002"
    static final int HOUR = 12;                     // "15"
    static final int HOUR12 = 13;                   // "3"
    static final int ZERO_HOUR12 = 14;              // "03"
    static final int MINUTE = 15;                   // "4"
    static final int ZERO_MINUTE = 16;              // "04"
    static final int SECOND = 17;                   // "5"
    static final int ZERO_SECOND = 18;              // "05"
    static final int LONG_YEAR = 19;                // "2006"
    static final int YEAR = 20;                     // "06"
    static final int PM = 21;                       // "PM"
    static final int LOWER_PM = 22;                 // "pm"
    static final int TZ = 23;                       // "MST"
    static final int ISO8601_TZ = 24;               // "Z0700"
    static final int ISO8601_SECONDS_TZ = 25;       // "Z070000"
    static final int ISO8601_SHORT_TZ = 26;         // "Z07"
    static final int ISO8601_COLON_TZ = 27;         // "Z07:00"
    static final int ISO8601_COLON_SECONDS_TZ = 28; // "Z07:00:00"
    static final int NUM_TZ = 29;                   // "-0700"
    static final int NUM_SECONDS_TZ = 30;           // "-070000"
    static final int NUM_SHORT_TZ = 31;             // "-07"
    static final int NUM_COLON_TZ = 32;             // "-07:00"
    static final int NUM_COLON_SECONDS_TZ = 33;     // "-07:00:00"
    static final int FRAC_SECOND0 = 34;             // ".000"
    static final int FRAC_SECOND9 = 35;             // ".999"

    /** Mirrors Go's {@code std0x}: the chunks introduced by a leading '0'. */
    private static final int[] STD_0X = {ZERO_MONTH, ZERO_DAY, ZERO_HOUR12, ZERO_MINUTE, ZERO_SECOND, YEAR};

    static final String[] LONG_MONTH_NAMES = {
            "January", "February", "March", "April", "May", "June",
            "July", "August", "September", "October", "November", "December"
    };
    static final String[] SHORT_MONTH_NAMES = {
            "Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"
    };
    static final String[] LONG_DAY_NAMES = {
            "Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday"
    };
    static final String[] SHORT_DAY_NAMES = {"Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"};

    /**
     * One piece of a compiled layout: either a literal run of text or a single
     * reference-time chunk.
     */
    private record Token(String literal, int std, int digits, char separator, String text) {

        static Token ofLiteral(String literal) {
            return new Token(literal, NONE, 0, '\0', literal);
        }

        static Token ofStd(int std, int digits, char separator, String text) {
            return new Token(null, std, digits, separator, text);
        }

        boolean isLiteral() {
            return literal != null;
        }
    }

    private record Chunk(String prefix, int std, int digits, char separator, String suffix) {
    }

    private static final Map<String, List<Token>> COMPILED = new ConcurrentHashMap<>();

    private static List<Token> compile(String layout) {
        return COMPILED.computeIfAbsent(layout, GoLayout::doCompile);
    }

    private static List<Token> doCompile(String layout) {
        List<Token> tokens = new ArrayList<>();
        String rest = layout;
        while (true) {
            Chunk chunk = nextStdChunk(rest);
            if (!chunk.prefix().isEmpty()) {
                tokens.add(Token.ofLiteral(chunk.prefix()));
            }
            if (chunk.std() == NONE) {
                break;
            }
            String text = rest.substring(chunk.prefix().length(), rest.length() - chunk.suffix().length());
            tokens.add(Token.ofStd(chunk.std(), chunk.digits(), chunk.separator(), text));
            rest = chunk.suffix();
        }
        return List.copyOf(tokens);
    }

    private static Chunk chunk(String layout, int start, int std, int end) {
        return new Chunk(layout.substring(0, start), std, 0, '\0', layout.substring(end));
    }

    /** Port of Go's {@code nextStdChunk}. */
    private static Chunk nextStdChunk(String layout) {
        for (int i = 0; i < layout.length(); i++) {
            char c = layout.charAt(i);
            switch (c) {
                case 'J': // January, Jan
                    if (layout.startsWith("Jan", i)) {
                        if (layout.startsWith("January", i)) {
                            return chunk(layout, i, LONG_MONTH, i + 7);
                        }
                        if (!startsWithLowerCase(layout, i + 3)) {
                            return chunk(layout, i, MONTH, i + 3);
                        }
                    }
                    break;

                case 'M': // Monday, Mon, MST
                    if (layout.startsWith("Mon", i)) {
                        if (layout.startsWith("Monday", i)) {
                            return chunk(layout, i, LONG_WEEKDAY, i + 6);
                        }
                        if (!startsWithLowerCase(layout, i + 3)) {
                            return chunk(layout, i, WEEKDAY, i + 3);
                        }
                    }
                    if (layout.startsWith("MST", i)) {
                        return chunk(layout, i, TZ, i + 3);
                    }
                    break;

                case '0': // 01, 02, 03, 04, 05, 06, 002
                    if (i + 1 < layout.length() && layout.charAt(i + 1) >= '1' && layout.charAt(i + 1) <= '6') {
                        return chunk(layout, i, STD_0X[layout.charAt(i + 1) - '1'], i + 2);
                    }
                    if (layout.startsWith("002", i)) {
                        return chunk(layout, i, ZERO_YEAR_DAY, i + 3);
                    }
                    break;

                case '1': // 15, 1
                    if (i + 1 < layout.length() && layout.charAt(i + 1) == '5') {
                        return chunk(layout, i, HOUR, i + 2);
                    }
                    return chunk(layout, i, NUM_MONTH, i + 1);

                case '2': // 2006, 2
                    if (layout.startsWith("2006", i)) {
                        return chunk(layout, i, LONG_YEAR, i + 4);
                    }
                    return chunk(layout, i, DAY, i + 1);

                case '_': // _2, _2006, __2
                    if (i + 1 < layout.length() && layout.charAt(i + 1) == '2') {
                        // _2006 is a literal '_' followed by the long year.
                        if (layout.startsWith("2006", i + 1)) {
                            return new Chunk(layout.substring(0, i + 1), LONG_YEAR, 0, '\0', layout.substring(i + 5));
                        }
                        return chunk(layout, i, UNDER_DAY, i + 2);
                    }
                    if (layout.startsWith("__2", i)) {
                        return chunk(layout, i, UNDER_YEAR_DAY, i + 3);
                    }
                    break;

                case '3':
                    return chunk(layout, i, HOUR12, i + 1);

                case '4':
                    return chunk(layout, i, MINUTE, i + 1);

                case '5':
                    return chunk(layout, i, SECOND, i + 1);

                case 'P': // PM
                    if (layout.startsWith("PM", i)) {
                        return chunk(layout, i, PM, i + 2);
                    }
                    break;

                case 'p': // pm
                    if (layout.startsWith("pm", i)) {
                        return chunk(layout, i, LOWER_PM, i + 2);
                    }
                    break;

                case '-': // -070000, -07:00:00, -0700, -07:00, -07
                    if (layout.startsWith("-070000", i)) {
                        return chunk(layout, i, NUM_SECONDS_TZ, i + 7);
                    }
                    if (layout.startsWith("-07:00:00", i)) {
                        return chunk(layout, i, NUM_COLON_SECONDS_TZ, i + 9);
                    }
                    if (layout.startsWith("-0700", i)) {
                        return chunk(layout, i, NUM_TZ, i + 5);
                    }
                    if (layout.startsWith("-07:00", i)) {
                        return chunk(layout, i, NUM_COLON_TZ, i + 6);
                    }
                    if (layout.startsWith("-07", i)) {
                        return chunk(layout, i, NUM_SHORT_TZ, i + 3);
                    }
                    break;

                case 'Z': // Z070000, Z07:00:00, Z0700, Z07:00, Z07
                    if (layout.startsWith("Z070000", i)) {
                        return chunk(layout, i, ISO8601_SECONDS_TZ, i + 7);
                    }
                    if (layout.startsWith("Z07:00:00", i)) {
                        return chunk(layout, i, ISO8601_COLON_SECONDS_TZ, i + 9);
                    }
                    if (layout.startsWith("Z0700", i)) {
                        return chunk(layout, i, ISO8601_TZ, i + 5);
                    }
                    if (layout.startsWith("Z07:00", i)) {
                        return chunk(layout, i, ISO8601_COLON_TZ, i + 6);
                    }
                    if (layout.startsWith("Z07", i)) {
                        return chunk(layout, i, ISO8601_SHORT_TZ, i + 3);
                    }
                    break;

                case '.':
                case ',': // ,000 or .000 or ,999 or .999 - repeated digits for fractional seconds
                    if (i + 1 < layout.length() && (layout.charAt(i + 1) == '0' || layout.charAt(i + 1) == '9')) {
                        char digit = layout.charAt(i + 1);
                        int j = i + 1;
                        while (j < layout.length() && layout.charAt(j) == digit) {
                            j++;
                        }
                        // The run of digits must end here: a fractional second is all digits.
                        if (!isDigit(layout, j)) {
                            int code = digit == '9' ? FRAC_SECOND9 : FRAC_SECOND0;
                            return new Chunk(layout.substring(0, i), code, j - (i + 1), c, layout.substring(j));
                        }
                    }
                    break;

                default:
                    break;
            }
        }
        return new Chunk(layout, NONE, 0, '\0', "");
    }

    // ---------------------------------------------------------------- formatting

    /** Port of Go's {@code Time.Format}. */
    static String format(GoTime t, String layout) {
        StringBuilder b = new StringBuilder();
        int year = t.year();
        int month = t.month();
        int day = t.day();
        int hour = t.hour();
        int minute = t.minute();
        int second = t.second();
        int nanosecond = t.nanosecond();
        GoTime.Zone zone = t.zone();

        for (Token token : compile(layout)) {
            if (token.isLiteral()) {
                b.append(token.literal());
                continue;
            }
            switch (token.std()) {
                case YEAR -> appendInt(b, Math.abs(year) % 100, 2);
                case LONG_YEAR -> appendInt(b, year, 4);
                case MONTH -> b.append(SHORT_MONTH_NAMES[month - 1]);
                case LONG_MONTH -> b.append(LONG_MONTH_NAMES[month - 1]);
                case NUM_MONTH -> appendInt(b, month, 0);
                case ZERO_MONTH -> appendInt(b, month, 2);
                case WEEKDAY -> b.append(SHORT_DAY_NAMES[t.weekday().value()]);
                case LONG_WEEKDAY -> b.append(LONG_DAY_NAMES[t.weekday().value()]);
                case DAY -> appendInt(b, day, 0);
                case UNDER_DAY -> {
                    if (day < 10) {
                        b.append(' ');
                    }
                    appendInt(b, day, 0);
                }
                case ZERO_DAY -> appendInt(b, day, 2);
                case UNDER_YEAR_DAY, ZERO_YEAR_DAY -> {
                    int yday = t.yearDay();
                    if (token.std() == UNDER_YEAR_DAY) {
                        if (yday < 100) {
                            b.append(' ');
                            if (yday < 10) {
                                b.append(' ');
                            }
                        }
                        appendInt(b, yday, 0);
                    } else {
                        appendInt(b, yday, 3);
                    }
                }
                case HOUR -> appendInt(b, hour, 2);
                case HOUR12 -> appendInt(b, hour12(hour), 0);
                case ZERO_HOUR12 -> appendInt(b, hour12(hour), 2);
                case MINUTE -> appendInt(b, minute, 0);
                case ZERO_MINUTE -> appendInt(b, minute, 2);
                case SECOND -> appendInt(b, second, 0);
                case ZERO_SECOND -> appendInt(b, second, 2);
                case PM -> b.append(hour >= 12 ? "PM" : "AM");
                case LOWER_PM -> b.append(hour >= 12 ? "pm" : "am");
                case ISO8601_TZ, ISO8601_COLON_TZ, ISO8601_SECONDS_TZ, ISO8601_COLON_SECONDS_TZ,
                        ISO8601_SHORT_TZ, NUM_TZ, NUM_SHORT_TZ, NUM_COLON_TZ, NUM_SECONDS_TZ,
                        NUM_COLON_SECONDS_TZ -> appendOffset(b, token.std(), zone.offset());
                case TZ -> {
                    if (!zone.name().isEmpty()) {
                        b.append(zone.name());
                    } else {
                        // No zone name known, but one must be printed: fall back to -0700.
                        int minutes = zone.offset() / 60;
                        if (minutes < 0) {
                            b.append('-');
                            minutes = -minutes;
                        } else {
                            b.append('+');
                        }
                        appendInt(b, minutes / 60, 2);
                        appendInt(b, minutes % 60, 2);
                    }
                }
                case FRAC_SECOND0, FRAC_SECOND9 ->
                        appendNano(b, nanosecond, token.std(), token.digits(), token.separator());
                default -> throw new IllegalStateException("unhandled layout chunk: " + token.text());
            }
        }
        return b.toString();
    }

    private static int hour12(int hour) {
        int h = hour % 12;
        return h == 0 ? 12 : h;
    }

    private static void appendOffset(StringBuilder b, int std, int offset) {
        // Go cheats and lets the "Z" variants mean "as formatted for ISO 8601".
        if (offset == 0 && (std == ISO8601_TZ || std == ISO8601_COLON_TZ || std == ISO8601_SECONDS_TZ
                || std == ISO8601_SHORT_TZ || std == ISO8601_COLON_SECONDS_TZ)) {
            b.append('Z');
            return;
        }
        int minutes = offset / 60;
        int absOffset = offset;
        if (minutes < 0) {
            b.append('-');
            minutes = -minutes;
            absOffset = -absOffset;
        } else {
            b.append('+');
        }
        appendInt(b, minutes / 60, 2);
        boolean colon = std == ISO8601_COLON_TZ || std == NUM_COLON_TZ
                || std == ISO8601_COLON_SECONDS_TZ || std == NUM_COLON_SECONDS_TZ;
        if (colon) {
            b.append(':');
        }
        if (std != NUM_SHORT_TZ && std != ISO8601_SHORT_TZ) {
            appendInt(b, minutes % 60, 2);
        }
        if (std == ISO8601_SECONDS_TZ || std == NUM_SECONDS_TZ
                || std == ISO8601_COLON_SECONDS_TZ || std == NUM_COLON_SECONDS_TZ) {
            if (colon) {
                b.append(':');
            }
            appendInt(b, absOffset % 60, 2);
        }
    }

    private static void appendInt(StringBuilder b, int x, int width) {
        if (x < 0) {
            b.append('-');
            x = -x;
        }
        String digits = Integer.toString(x);
        for (int i = digits.length(); i < width; i++) {
            b.append('0');
        }
        b.append(digits);
    }

    /** Port of Go's {@code appendNano}. */
    private static void appendNano(StringBuilder b, int nanosecond, int std, int digits, char separator) {
        boolean trim = std == FRAC_SECOND9;
        if (trim && (digits == 0 || nanosecond == 0)) {
            return;
        }
        char dot = separator == ',' ? ',' : '.';
        int start = b.length();
        b.append(dot);
        StringBuilder nanos = new StringBuilder();
        appendInt(nanos, nanosecond, 9);
        b.append(digits < 9 ? nanos.substring(0, digits) : nanos);
        if (trim) {
            int end = b.length();
            while (end > start && b.charAt(end - 1) == '0') {
                end--;
            }
            if (end > start && b.charAt(end - 1) == dot) {
                end--;
            }
            b.setLength(end);
        }
    }

    // ------------------------------------------------------------------ parsing

    /**
     * Port of Go's {@code time.parse}. {@code defaultLocation} is used when the value
     * carries no zone at all; {@code local} is the location consulted when the value
     * carries an offset or an abbreviation, mirroring {@code ParseInLocation}.
     */
    static GoTime parse(String layout, String value, Location defaultLocation, Location local)
            throws TimeParseException {
        final String wholeLayout = layout;
        final String wholeValue = value;

        boolean amSet = false;
        boolean pmSet = false;

        int year = 0;
        int month = -1;
        int day = -1;
        int yday = -1;
        int hour = 0;
        int minute = 0;
        int second = 0;
        int nanosecond = 0;
        Location z = null;
        int zoneOffset = -1;
        String zoneName = "";

        List<Token> tokens = compile(layout);
        for (int ti = 0; ti < tokens.size(); ti++) {
            Token token = tokens.get(ti);
            if (token.isLiteral()) {
                String skipped = skip(value, token.literal());
                if (skipped == null) {
                    throw new TimeParseException(wholeLayout, wholeValue, token.literal(), value, "");
                }
                value = skipped;
                continue;
            }

            int std = token.std();
            String hold = value;
            String rangeErrString = null;
            boolean bad = false;

            switch (std) {
                case YEAR: {
                    if (value.length() < 2) {
                        bad = true;
                        break;
                    }
                    Integer n = atoi(value.substring(0, 2));
                    value = value.substring(2);
                    if (n == null) {
                        bad = true;
                        break;
                    }
                    year = n >= 69 ? n + 1900 : n + 2000;
                    break;
                }
                case LONG_YEAR: {
                    if (value.length() < 4 || !isDigit(value, 0)) {
                        bad = true;
                        break;
                    }
                    Integer n = atoi(value.substring(0, 4));
                    value = value.substring(4);
                    if (n == null) {
                        bad = true;
                        break;
                    }
                    year = n;
                    break;
                }
                case MONTH: {
                    int[] found = lookup(SHORT_MONTH_NAMES, value);
                    if (found == null) {
                        bad = true;
                        break;
                    }
                    month = found[0] + 1;
                    value = value.substring(found[1]);
                    break;
                }
                case LONG_MONTH: {
                    int[] found = lookup(LONG_MONTH_NAMES, value);
                    if (found == null) {
                        bad = true;
                        break;
                    }
                    month = found[0] + 1;
                    value = value.substring(found[1]);
                    break;
                }
                case NUM_MONTH:
                case ZERO_MONTH: {
                    int[] num = getnum(value, std == ZERO_MONTH);
                    if (num == null) {
                        bad = true;
                        break;
                    }
                    month = num[0];
                    value = value.substring(num[1]);
                    if (month <= 0 || month > 12) {
                        rangeErrString = "month";
                    }
                    break;
                }
                case WEEKDAY: {
                    // The weekday is ignored beyond checking that it is well formed.
                    int[] found = lookup(SHORT_DAY_NAMES, value);
                    if (found == null) {
                        bad = true;
                        break;
                    }
                    value = value.substring(found[1]);
                    break;
                }
                case LONG_WEEKDAY: {
                    int[] found = lookup(LONG_DAY_NAMES, value);
                    if (found == null) {
                        bad = true;
                        break;
                    }
                    value = value.substring(found[1]);
                    break;
                }
                case DAY:
                case UNDER_DAY:
                case ZERO_DAY: {
                    if (std == UNDER_DAY && !value.isEmpty() && value.charAt(0) == ' ') {
                        value = value.substring(1);
                    }
                    int[] num = getnum(value, std == ZERO_DAY);
                    if (num == null) {
                        bad = true;
                        break;
                    }
                    day = num[0];
                    value = value.substring(num[1]);
                    break;
                }
                case UNDER_YEAR_DAY:
                case ZERO_YEAR_DAY: {
                    for (int i = 0; i < 2; i++) {
                        if (std == UNDER_YEAR_DAY && !value.isEmpty() && value.charAt(0) == ' ') {
                            value = value.substring(1);
                        }
                    }
                    int[] num = getnum3(value, std == ZERO_YEAR_DAY);
                    if (num == null) {
                        bad = true;
                        break;
                    }
                    yday = num[0];
                    value = value.substring(num[1]);
                    break;
                }
                case HOUR: {
                    int[] num = getnum(value, false);
                    if (num == null) {
                        bad = true;
                        break;
                    }
                    hour = num[0];
                    value = value.substring(num[1]);
                    if (hour < 0 || hour >= 24) {
                        rangeErrString = "hour";
                    }
                    break;
                }
                case HOUR12:
                case ZERO_HOUR12: {
                    int[] num = getnum(value, std == ZERO_HOUR12);
                    if (num == null) {
                        bad = true;
                        break;
                    }
                    hour = num[0];
                    value = value.substring(num[1]);
                    if (hour < 0 || hour > 12) {
                        rangeErrString = "hour";
                    }
                    break;
                }
                case MINUTE:
                case ZERO_MINUTE: {
                    int[] num = getnum(value, std == ZERO_MINUTE);
                    if (num == null) {
                        bad = true;
                        break;
                    }
                    minute = num[0];
                    value = value.substring(num[1]);
                    if (minute < 0 || minute >= 60) {
                        rangeErrString = "minute";
                    }
                    break;
                }
                case SECOND:
                case ZERO_SECOND: {
                    int[] num = getnum(value, std == ZERO_SECOND);
                    if (num == null) {
                        bad = true;
                        break;
                    }
                    second = num[0];
                    value = value.substring(num[1]);
                    if (second < 0 || second >= 60) {
                        rangeErrString = "second";
                        break;
                    }
                    // Special case: a fractional second in the value with none in the layout.
                    if (value.length() >= 2 && commaOrPeriod(value.charAt(0)) && isDigit(value, 1)) {
                        int next = nextStdAfter(tokens, ti);
                        if (next == FRAC_SECOND0 || next == FRAC_SECOND9) {
                            break; // The layout has one after all; handle it normally.
                        }
                        int n = 2;
                        while (n < value.length() && isDigit(value, n)) {
                            n++;
                        }
                        long[] nanos = parseNanoseconds(value, n);
                        if (nanos == null) {
                            bad = true;
                            break;
                        }
                        if (nanos[1] == 1) {
                            rangeErrString = "fractional second";
                            break;
                        }
                        nanosecond = (int) nanos[0];
                        value = value.substring(n);
                    }
                    break;
                }
                case PM: {
                    if (value.length() < 2) {
                        bad = true;
                        break;
                    }
                    String p = value.substring(0, 2);
                    value = value.substring(2);
                    if (p.equals("PM")) {
                        pmSet = true;
                    } else if (p.equals("AM")) {
                        amSet = true;
                    } else {
                        bad = true;
                    }
                    break;
                }
                case LOWER_PM: {
                    if (value.length() < 2) {
                        bad = true;
                        break;
                    }
                    String p = value.substring(0, 2);
                    value = value.substring(2);
                    if (p.equals("pm")) {
                        pmSet = true;
                    } else if (p.equals("am")) {
                        amSet = true;
                    } else {
                        bad = true;
                    }
                    break;
                }
                case ISO8601_TZ:
                case ISO8601_COLON_TZ:
                case ISO8601_SECONDS_TZ:
                case ISO8601_COLON_SECONDS_TZ:
                case ISO8601_SHORT_TZ:
                case NUM_TZ:
                case NUM_SHORT_TZ:
                case NUM_COLON_TZ:
                case NUM_SECONDS_TZ:
                case NUM_COLON_SECONDS_TZ: {
                    boolean iso = std == ISO8601_TZ || std == ISO8601_SHORT_TZ || std == ISO8601_COLON_TZ
                            || std == ISO8601_SECONDS_TZ || std == ISO8601_COLON_SECONDS_TZ;
                    if (iso && !value.isEmpty() && value.charAt(0) == 'Z') {
                        value = value.substring(1);
                        z = Location.UTC;
                        break;
                    }
                    String sign;
                    String hh;
                    String mm;
                    String ss;
                    if (std == ISO8601_COLON_TZ || std == NUM_COLON_TZ) {
                        if (value.length() < 6 || value.charAt(3) != ':') {
                            bad = true;
                            break;
                        }
                        sign = value.substring(0, 1);
                        hh = value.substring(1, 3);
                        mm = value.substring(4, 6);
                        ss = "00";
                        value = value.substring(6);
                    } else if (std == NUM_SHORT_TZ || std == ISO8601_SHORT_TZ) {
                        if (value.length() < 3) {
                            bad = true;
                            break;
                        }
                        sign = value.substring(0, 1);
                        hh = value.substring(1, 3);
                        mm = "00";
                        ss = "00";
                        value = value.substring(3);
                    } else if (std == ISO8601_COLON_SECONDS_TZ || std == NUM_COLON_SECONDS_TZ) {
                        if (value.length() < 9 || value.charAt(3) != ':' || value.charAt(6) != ':') {
                            bad = true;
                            break;
                        }
                        sign = value.substring(0, 1);
                        hh = value.substring(1, 3);
                        mm = value.substring(4, 6);
                        ss = value.substring(7, 9);
                        value = value.substring(9);
                    } else if (std == ISO8601_SECONDS_TZ || std == NUM_SECONDS_TZ) {
                        if (value.length() < 7) {
                            bad = true;
                            break;
                        }
                        sign = value.substring(0, 1);
                        hh = value.substring(1, 3);
                        mm = value.substring(3, 5);
                        ss = value.substring(5, 7);
                        value = value.substring(7);
                    } else {
                        if (value.length() < 5) {
                            bad = true;
                            break;
                        }
                        sign = value.substring(0, 1);
                        hh = value.substring(1, 3);
                        mm = value.substring(3, 5);
                        ss = "00";
                        value = value.substring(5);
                    }
                    Integer hr = twoDigits(hh);
                    Integer mn = twoDigits(mm);
                    Integer sc = twoDigits(ss);
                    if (hr == null || mn == null || sc == null) {
                        bad = true;
                        break;
                    }
                    zoneOffset = (hr * 60 + mn) * 60 + sc;
                    if (sign.charAt(0) == '-') {
                        zoneOffset = -zoneOffset;
                    } else if (sign.charAt(0) != '+') {
                        bad = true;
                    }
                    break;
                }
                case TZ: {
                    if (value.startsWith("UTC")) {
                        z = Location.UTC;
                        value = value.substring(3);
                        break;
                    }
                    int n = parseTimeZone(value);
                    if (n == 0) {
                        bad = true;
                        break;
                    }
                    zoneName = value.substring(0, n);
                    value = value.substring(n);
                    break;
                }
                case FRAC_SECOND0: {
                    // A fixed fractional second requires exactly the digits the layout asks for.
                    int ndigit = 1 + token.digits();
                    if (value.length() < ndigit) {
                        bad = true;
                        break;
                    }
                    long[] nanos = parseNanoseconds(value, ndigit);
                    if (nanos == null) {
                        bad = true;
                        break;
                    }
                    if (nanos[1] == 1) {
                        rangeErrString = "fractional second";
                        break;
                    }
                    nanosecond = (int) nanos[0];
                    value = value.substring(ndigit);
                    break;
                }
                case FRAC_SECOND9: {
                    if (value.length() < 2 || !commaOrPeriod(value.charAt(0)) || !isDigit(value, 1)) {
                        break; // Fractional second omitted.
                    }
                    // Take any number of digits, even more than asked for.
                    int i = 0;
                    while (i + 1 < value.length() && isDigit(value, i + 1)) {
                        i++;
                    }
                    long[] nanos = parseNanoseconds(value, 1 + i);
                    if (nanos == null) {
                        bad = true;
                        break;
                    }
                    if (nanos[1] == 1) {
                        rangeErrString = "fractional second";
                        break;
                    }
                    nanosecond = (int) nanos[0];
                    value = value.substring(1 + i);
                    break;
                }
                default:
                    throw new IllegalStateException("unhandled layout chunk: " + token.text());
            }

            if (rangeErrString != null) {
                throw new TimeParseException(wholeLayout, wholeValue, token.text(), hold,
                        ": " + rangeErrString + " out of range");
            }
            if (bad) {
                throw new TimeParseException(wholeLayout, wholeValue, token.text(), hold, "");
            }
        }

        if (!value.isEmpty()) {
            throw new TimeParseException(wholeLayout, wholeValue, "", value, ": extra text: \"" + value + "\"");
        }

        if (pmSet && hour < 12) {
            hour += 12;
        } else if (amSet && hour == 12) {
            hour = 0;
        }

        if (yday >= 0) {
            if (yday < 1 || yday > (LocalDate.of(year, 1, 1).isLeapYear() ? 366 : 365)) {
                throw new TimeParseException(wholeLayout, wholeValue, "", value, ": day-of-year out of range");
            }
            LocalDate fromYday = LocalDate.ofYearDay(year, yday);
            if (month >= 0 && month != fromYday.getMonthValue()) {
                throw new TimeParseException("time: day-of-year does not match month");
            }
            month = fromYday.getMonthValue();
            if (day >= 0 && day != fromYday.getDayOfMonth()) {
                throw new TimeParseException("time: day-of-year does not match day");
            }
            day = fromYday.getDayOfMonth();
        } else {
            if (month < 0) {
                month = 1;
            }
            if (day < 0) {
                day = 1;
            }
        }

        if (day < 1 || day > YearMonth.of(year, month).lengthOfMonth()) {
            throw new TimeParseException(wholeLayout, wholeValue, "", value, ": day out of range");
        }

        if (z != null) {
            return GoTime.date(year, month, day, hour, minute, second, nanosecond, z);
        }

        if (zoneOffset != -1) {
            GoTime asUtc = GoTime.date(year, month, day, hour, minute, second, nanosecond, Location.UTC);
            long unixSeconds = asUtc.toInstant().getEpochSecond() - zoneOffset;
            Instant at = Instant.ofEpochSecond(unixSeconds);
            // Use the target location if that zone really was in effect at that moment.
            if (local.offset(at) == zoneOffset
                    && (zoneName.isEmpty() || local.abbreviation(at).equals(zoneName))) {
                return GoTime.ofInstant(Instant.ofEpochSecond(unixSeconds, nanosecond), local);
            }
            // Otherwise fabricate a zone that just records the offset.
            return GoTime.ofInstant(Instant.ofEpochSecond(unixSeconds, nanosecond),
                    fixedZone(zoneName, zoneOffset));
        }

        if (!zoneName.isEmpty()) {
            GoTime asUtc = GoTime.date(year, month, day, hour, minute, second, nanosecond, Location.UTC);
            long unixSeconds = asUtc.toInstant().getEpochSecond();
            OptionalInt known = local.lookupName(zoneName, unixSeconds);
            if (known.isPresent()) {
                return GoTime.ofInstant(Instant.ofEpochSecond(unixSeconds - known.getAsInt(), nanosecond), local);
            }
            // Otherwise fabricate a zone with an unknown offset, except for GMT+N.
            int offset = 0;
            if (zoneName.length() > 3 && zoneName.startsWith("GMT")) {
                Integer hours = atoi(zoneName.substring(3));
                if (hours != null) {
                    offset = hours * 3600;
                }
            }
            return GoTime.ofInstant(Instant.ofEpochSecond(unixSeconds, nanosecond), fixedZone(zoneName, offset));
        }

        return GoTime.date(year, month, day, hour, minute, second, nanosecond, defaultLocation);
    }

    private static Location fixedZone(String name, int offsetSeconds) throws TimeParseException {
        try {
            return Location.fixed(name, offsetSeconds);
        } catch (DateTimeException e) {
            throw new TimeParseException("time: invalid zone offset " + offsetSeconds);
        }
    }

    private static int nextStdAfter(List<Token> tokens, int index) {
        for (int j = index + 1; j < tokens.size(); j++) {
            if (!tokens.get(j).isLiteral()) {
                return tokens.get(j).std();
            }
        }
        return NONE;
    }

    /** Port of Go's {@code skip}: consume the layout literal, treating runs of spaces as one. */
    private static String skip(String value, String prefix) {
        int vi = 0;
        int pi = 0;
        while (pi < prefix.length()) {
            if (prefix.charAt(pi) == ' ') {
                if (vi < value.length() && value.charAt(vi) != ' ') {
                    return null;
                }
                while (pi < prefix.length() && prefix.charAt(pi) == ' ') {
                    pi++;
                }
                while (vi < value.length() && value.charAt(vi) == ' ') {
                    vi++;
                }
                continue;
            }
            if (vi >= value.length() || value.charAt(vi) != prefix.charAt(pi)) {
                return null;
            }
            pi++;
            vi++;
        }
        return value.substring(vi);
    }

    /** Port of Go's {@code getnum}. Returns {@code {value, charsConsumed}} or null. */
    private static int[] getnum(String s, boolean fixed) {
        if (!isDigit(s, 0)) {
            return null;
        }
        if (!isDigit(s, 1)) {
            if (fixed) {
                return null;
            }
            return new int[]{s.charAt(0) - '0', 1};
        }
        return new int[]{(s.charAt(0) - '0') * 10 + (s.charAt(1) - '0'), 2};
    }

    /** Port of Go's {@code getnum3}. */
    private static int[] getnum3(String s, boolean fixed) {
        int n = 0;
        int i = 0;
        for (; i < 3 && isDigit(s, i); i++) {
            n = n * 10 + (s.charAt(i) - '0');
        }
        if (i == 0 || (fixed && i != 3)) {
            return null;
        }
        return new int[]{n, i};
    }

    private static Integer twoDigits(String s) {
        if (s.length() != 2 || !isDigit(s, 0) || !isDigit(s, 1)) {
            return null;
        }
        return (s.charAt(0) - '0') * 10 + (s.charAt(1) - '0');
    }

    /** Port of Go's {@code lookup}. Returns {@code {index, charsConsumed}} or null. */
    private static int[] lookup(String[] table, String value) {
        for (int i = 0; i < table.length; i++) {
            String candidate = table[i];
            if (value.length() >= candidate.length()
                    && value.regionMatches(true, 0, candidate, 0, candidate.length())) {
                return new int[]{i, candidate.length()};
            }
        }
        return null;
    }

    /** Port of Go's {@code parseNanoseconds}. Returns {@code {nanos, rangeError}} or null. */
    private static long[] parseNanoseconds(String value, int nbytes) {
        if (value.isEmpty() || !commaOrPeriod(value.charAt(0))) {
            return null;
        }
        if (nbytes > 10) {
            value = value.substring(0, 10);
            nbytes = 10;
        }
        Integer ns = atoi(value.substring(1, nbytes));
        if (ns == null) {
            return null;
        }
        if (ns < 0) {
            return new long[]{0, 1};
        }
        long scaled = ns;
        for (int i = 0, scaleDigits = 10 - nbytes; i < scaleDigits; i++) {
            scaled *= 10;
        }
        return new long[]{scaled, 0};
    }

    /** Port of Go's {@code parseTimeZone}: how many bytes of {@code value} name a zone. */
    private static int parseTimeZone(String value) {
        if (value.length() < 3) {
            return 0;
        }
        // ChST and MeST are the only zone abbreviations with a lower-case letter.
        if (value.length() >= 4 && (value.startsWith("ChST") || value.startsWith("MeST"))) {
            return 4;
        }
        // GMT may carry an hour offset.
        if (value.startsWith("GMT")) {
            return parseGMT(value);
        }
        // Some zones are unnamed and show up as +/-00.
        if (value.charAt(0) == '+' || value.charAt(0) == '-') {
            return parseSignedOffset(value);
        }
        int upper = 0;
        while (upper < 6 && upper < value.length()) {
            char c = value.charAt(upper);
            if (c < 'A' || c > 'Z') {
                break;
            }
            upper++;
        }
        switch (upper) {
            case 5: // Must end in T to match.
                return value.charAt(4) == 'T' ? 5 : 0;
            case 4: // Must end in T, with one special case.
                return value.charAt(3) == 'T' || value.startsWith("WITA") ? 4 : 0;
            case 3:
                return 3;
            default:
                return 0;
        }
    }

    private static int parseGMT(String value) {
        String rest = value.substring(3);
        if (rest.isEmpty()) {
            return 3;
        }
        return 3 + parseSignedOffset(rest);
    }

    private static int parseSignedOffset(String value) {
        char sign = value.charAt(0);
        if (sign != '-' && sign != '+') {
            return 0;
        }
        int i = 1;
        long x = 0;
        while (i < value.length() && value.charAt(i) >= '0' && value.charAt(i) <= '9') {
            x = x * 10 + (value.charAt(i) - '0');
            if (x > 23) {
                return 0;
            }
            i++;
        }
        if (i == 1) {
            return 0;
        }
        return i;
    }

    /** Port of Go's {@code atoi}: a strict decimal integer with an optional sign. */
    private static Integer atoi(String s) {
        if (s.isEmpty()) {
            return null;
        }
        boolean negative = false;
        int i = 0;
        char first = s.charAt(0);
        if (first == '-' || first == '+') {
            negative = first == '-';
            i = 1;
        }
        if (i >= s.length()) {
            return null;
        }
        long x = 0;
        for (; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c < '0' || c > '9') {
                return null;
            }
            x = x * 10 + (c - '0');
            if (x > Integer.MAX_VALUE) {
                return null;
            }
        }
        return (int) (negative ? -x : x);
    }

    private static boolean isDigit(String s, int i) {
        if (i >= s.length()) {
            return false;
        }
        char c = s.charAt(i);
        return c >= '0' && c <= '9';
    }

    private static boolean commaOrPeriod(char c) {
        return c == '.' || c == ',';
    }

    private static boolean startsWithLowerCase(String s, int i) {
        if (i >= s.length()) {
            return false;
        }
        char c = s.charAt(i);
        return c >= 'a' && c <= 'z';
    }
}
