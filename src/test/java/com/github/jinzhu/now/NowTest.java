package com.github.jinzhu.now;

import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Port of {@code now_test.go}. Go runs a file's tests in source order and the suite relies
 * on that, since {@code WeekStartDay} and {@code TimeFormats} are package-level state that
 * the tests mutate, so the same order is pinned here.
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class NowTest {

    private static final String FORMAT = "2006-01-02 15:04:05.999999999";

    private static final Location LOCATION_CARACAS = Location.load("America/Caracas");
    private static final Location LOCATION_BERLIN = Location.load("Europe/Berlin");
    private static final GoTime TIME_CARACAS = GoTime.date(2016, 1, 1, 12, 10, 0, 0, LOCATION_CARACAS);

    private static void assertT(GoTime actual, String expected, String message) {
        assertEquals(expected, actual.format(FORMAT), message);
    }

    @Test
    @Order(1)
    void beginningOf() {
        GoTime n = GoTime.date(2013, 11, 18, 17, 51, 49, 123456789, Location.UTC);

        assertT(Nows.with(n).beginningOfMinute(), "2013-11-18 17:51:00", "BeginningOfMinute");

        Nows.weekStartDay = Weekday.MONDAY;
        assertT(Nows.with(n).beginningOfWeek(), "2013-11-18 00:00:00", "BeginningOfWeek, FirstDayMonday");

        Nows.weekStartDay = Weekday.TUESDAY;
        assertT(Nows.with(n).beginningOfWeek(), "2013-11-12 00:00:00", "BeginningOfWeek, FirstDayTuesday");

        Nows.weekStartDay = Weekday.WEDNESDAY;
        assertT(Nows.with(n).beginningOfWeek(), "2013-11-13 00:00:00", "BeginningOfWeek, FirstDayWednesday");

        Nows.weekStartDay = Weekday.THURSDAY;
        assertT(Nows.with(n).beginningOfWeek(), "2013-11-14 00:00:00", "BeginningOfWeek, FirstDayThursday");

        Nows.weekStartDay = Weekday.FRIDAY;
        assertT(Nows.with(n).beginningOfWeek(), "2013-11-15 00:00:00", "BeginningOfWeek, FirstDayFriday");

        Nows.weekStartDay = Weekday.SATURDAY;
        assertT(Nows.with(n).beginningOfWeek(), "2013-11-16 00:00:00", "BeginningOfWeek, FirstDaySaturday");

        Nows.weekStartDay = Weekday.SUNDAY;
        assertT(Nows.with(n).beginningOfWeek(), "2013-11-17 00:00:00", "BeginningOfWeek, FirstDaySunday");

        assertT(Nows.with(n).beginningOfHour(), "2013-11-18 17:00:00", "BeginningOfHour");

        // Truncate with hour bug
        assertT(Nows.with(TIME_CARACAS).beginningOfHour(), "2016-01-01 12:00:00", "BeginningOfHour Caracas");

        assertT(Nows.with(n).beginningOfDay(), "2013-11-18 00:00:00", "BeginningOfDay");

        Location location = Location.load("Japan");
        GoTime beginningOfDay = GoTime.date(2015, 5, 1, 0, 0, 0, 0, location);
        assertT(Nows.with(beginningOfDay).beginningOfDay(), "2015-05-01 00:00:00", "BeginningOfDay");

        // DST
        GoTime dstBeginningOfDay = GoTime.date(2017, 10, 29, 10, 0, 0, 0, LOCATION_BERLIN);
        assertT(Nows.with(dstBeginningOfDay).beginningOfDay(), "2017-10-29 00:00:00", "BeginningOfDay DST");

        assertT(Nows.with(n).beginningOfWeek(), "2013-11-17 00:00:00", "BeginningOfWeek");

        GoTime dstBeginningOfWeek = GoTime.date(2017, 10, 30, 12, 0, 0, 0, LOCATION_BERLIN);
        assertT(Nows.with(dstBeginningOfWeek).beginningOfWeek(), "2017-10-29 00:00:00", "BeginningOfWeek");

        dstBeginningOfWeek = GoTime.date(2017, 10, 29, 12, 0, 0, 0, LOCATION_BERLIN);
        assertT(Nows.with(dstBeginningOfWeek).beginningOfWeek(), "2017-10-29 00:00:00", "BeginningOfWeek");

        Nows.weekStartDay = Weekday.MONDAY;
        assertT(Nows.with(n).beginningOfWeek(), "2013-11-18 00:00:00", "BeginningOfWeek, FirstDayMonday");
        dstBeginningOfWeek = GoTime.date(2017, 10, 24, 12, 0, 0, 0, LOCATION_BERLIN);
        assertT(Nows.with(dstBeginningOfWeek).beginningOfWeek(), "2017-10-23 00:00:00",
                "BeginningOfWeek, FirstDayMonday");

        dstBeginningOfWeek = GoTime.date(2017, 10, 29, 12, 0, 0, 0, LOCATION_BERLIN);
        assertT(Nows.with(dstBeginningOfWeek).beginningOfWeek(), "2017-10-23 00:00:00",
                "BeginningOfWeek, FirstDayMonday");

        Nows.weekStartDay = Weekday.SUNDAY;

        assertT(Nows.with(n).beginningOfMonth(), "2013-11-01 00:00:00", "BeginningOfMonth");

        // DST
        GoTime dstBeginningOfMonth = GoTime.date(2017, 10, 31, 0, 0, 0, 0, LOCATION_BERLIN);
        assertT(Nows.with(dstBeginningOfMonth).beginningOfMonth(), "2017-10-01 00:00:00", "BeginningOfMonth DST");

        assertT(Nows.with(n).beginningOfQuarter(), "2013-10-01 00:00:00", "BeginningOfQuarter");

        // DST
        assertT(Nows.with(dstBeginningOfMonth).beginningOfQuarter(), "2017-10-01 00:00:00",
                "BeginningOfQuarter DST");
        GoTime dstBeginningOfQuarter = GoTime.date(2017, 11, 24, 0, 0, 0, 0, LOCATION_BERLIN);
        assertT(Nows.with(dstBeginningOfQuarter).beginningOfQuarter(), "2017-10-01 00:00:00",
                "BeginningOfQuarter DST");

        assertT(Nows.with(dstBeginningOfQuarter).beginningOfHalf(), "2017-07-01 00:00:00", "BeginningOfHalf DST");

        assertT(Nows.with(n.addDate(0, -1, 0)).beginningOfQuarter(), "2013-10-01 00:00:00", "BeginningOfQuarter");

        assertT(Nows.with(n.addDate(0, 1, 0)).beginningOfQuarter(), "2013-10-01 00:00:00", "BeginningOfQuarter");

        assertT(Nows.with(n.addDate(0, 1, 0)).beginningOfHalf(), "2013-07-01 00:00:00", "BeginningOfHalf");

        // DST
        assertT(Nows.with(dstBeginningOfQuarter).beginningOfYear(), "2017-01-01 00:00:00", "BeginningOfYear DST");

        assertT(Nows.with(TIME_CARACAS).beginningOfYear(), "2016-01-01 00:00:00", "BeginningOfYear Caracas");
    }

    @Test
    @Order(2)
    void endOf() {
        GoTime n = GoTime.date(2013, 11, 18, 17, 51, 49, 123456789, Location.UTC);

        assertT(Nows.with(n).endOfMinute(), "2013-11-18 17:51:59.999999999", "EndOfMinute");

        assertT(Nows.with(n).endOfHour(), "2013-11-18 17:59:59.999999999", "EndOfHour");

        assertT(Nows.with(TIME_CARACAS).endOfHour(), "2016-01-01 12:59:59.999999999", "EndOfHour Caracas");

        assertT(Nows.with(n).endOfDay(), "2013-11-18 23:59:59.999999999", "EndOfDay");

        GoTime dstEndOfDay = GoTime.date(2017, 10, 29, 1, 0, 0, 0, LOCATION_BERLIN);
        assertT(Nows.with(dstEndOfDay).endOfDay(), "2017-10-29 23:59:59.999999999", "EndOfDay DST");

        Nows.weekStartDay = Weekday.TUESDAY;
        assertT(Nows.with(n).endOfWeek(), "2013-11-18 23:59:59.999999999", "EndOfWeek, FirstDayTuesday");

        Nows.weekStartDay = Weekday.WEDNESDAY;
        assertT(Nows.with(n).endOfWeek(), "2013-11-19 23:59:59.999999999", "EndOfWeek, FirstDayWednesday");

        Nows.weekStartDay = Weekday.THURSDAY;
        assertT(Nows.with(n).endOfWeek(), "2013-11-20 23:59:59.999999999", "EndOfWeek, FirstDayThursday");

        Nows.weekStartDay = Weekday.FRIDAY;
        assertT(Nows.with(n).endOfWeek(), "2013-11-21 23:59:59.999999999", "EndOfWeek, FirstDayFriday");

        Nows.weekStartDay = Weekday.SATURDAY;
        assertT(Nows.with(n).endOfWeek(), "2013-11-22 23:59:59.999999999", "EndOfWeek, FirstDaySaturday");

        Nows.weekStartDay = Weekday.SUNDAY;
        assertT(Nows.with(n).endOfWeek(), "2013-11-23 23:59:59.999999999", "EndOfWeek, FirstDaySunday");

        Nows.weekStartDay = Weekday.MONDAY;
        assertT(Nows.with(n).endOfWeek(), "2013-11-24 23:59:59.999999999", "EndOfWeek, FirstDayMonday");

        GoTime dstEndOfWeek = GoTime.date(2017, 10, 24, 12, 0, 0, 0, LOCATION_BERLIN);
        assertT(Nows.with(dstEndOfWeek).endOfWeek(), "2017-10-29 23:59:59.999999999", "EndOfWeek, FirstDayMonday");

        dstEndOfWeek = GoTime.date(2017, 10, 29, 12, 0, 0, 0, LOCATION_BERLIN);
        assertT(Nows.with(dstEndOfWeek).endOfWeek(), "2017-10-29 23:59:59.999999999", "EndOfWeek, FirstDayMonday");

        Nows.weekStartDay = Weekday.SUNDAY;
        assertT(Nows.with(n).endOfWeek(), "2013-11-23 23:59:59.999999999", "EndOfWeek");

        dstEndOfWeek = GoTime.date(2017, 10, 29, 0, 0, 0, 0, LOCATION_BERLIN);
        assertT(Nows.with(dstEndOfWeek).endOfWeek(), "2017-11-04 23:59:59.999999999", "EndOfWeek");

        dstEndOfWeek = GoTime.date(2017, 10, 29, 12, 0, 0, 0, LOCATION_BERLIN);
        assertT(Nows.with(dstEndOfWeek).endOfWeek(), "2017-11-04 23:59:59.999999999", "EndOfWeek");

        assertT(Nows.with(n).endOfMonth(), "2013-11-30 23:59:59.999999999", "EndOfMonth");

        assertT(Nows.with(n).endOfQuarter(), "2013-12-31 23:59:59.999999999", "EndOfQuarter");

        assertT(Nows.with(n).endOfHalf(), "2013-12-31 23:59:59.999999999", "EndOfHalf");

        assertT(Nows.with(n.addDate(0, -1, 0)).endOfQuarter(), "2013-12-31 23:59:59.999999999", "EndOfQuarter");

        assertT(Nows.with(n.addDate(0, 1, 0)).endOfQuarter(), "2013-12-31 23:59:59.999999999", "EndOfQuarter");

        assertT(Nows.with(n.addDate(0, 1, 0)).endOfHalf(), "2013-12-31 23:59:59.999999999", "EndOfHalf");

        assertT(Nows.with(n).endOfYear(), "2013-12-31 23:59:59.999999999", "EndOfYear");

        GoTime n1 = GoTime.date(2013, 2, 18, 17, 51, 49, 123456789, Location.UTC);
        assertT(Nows.with(n1).endOfMonth(), "2013-02-28 23:59:59.999999999", "EndOfMonth for 2013/02");

        GoTime n2 = GoTime.date(1900, 2, 18, 17, 51, 49, 123456789, Location.UTC);
        assertT(Nows.with(n2).endOfMonth(), "1900-02-28 23:59:59.999999999", "EndOfMonth");
    }

    @Test
    @Order(3)
    void mondayAndSunday() {
        GoTime n = GoTime.date(2013, 11, 19, 17, 51, 49, 123456789, Location.UTC);
        GoTime n2 = GoTime.date(2013, 11, 24, 17, 51, 49, 123456789, Location.UTC);
        GoTime nDst = GoTime.date(2017, 10, 29, 10, 0, 0, 0, LOCATION_BERLIN);

        assertT(Nows.with(n).monday(), "2013-11-18 00:00:00", "Monday");

        assertT(Nows.with(n2).monday(), "2013-11-18 00:00:00", "Monday");

        assertT(Nows.with(TIME_CARACAS).monday(), "2015-12-28 00:00:00", "Monday Caracas");

        assertT(Nows.with(nDst).monday(), "2017-10-23 00:00:00", "Monday DST");

        assertT(Nows.with(n).monday("17:51:49"), "2013-11-18 17:51:49", "Monday");

        assertT(Nows.with(n).monday("17:51"), "2013-11-18 17:51:00", "Monday");

        assertT(Nows.with(n).sunday(), "2013-11-24 00:00:00", "Sunday");

        assertT(Nows.with(n).sunday("18:19:20"), "2013-11-24 18:19:20", "Sunday");

        assertT(Nows.with(n).sunday("18:19"), "2013-11-24 18:19:00", "Sunday");

        assertT(Nows.with(n2).sunday(), "2013-11-24 00:00:00", "Sunday");

        assertT(Nows.with(TIME_CARACAS).sunday(), "2016-01-03 00:00:00", "Sunday Caracas");

        assertT(Nows.with(nDst).sunday(), "2017-10-29 00:00:00", "Sunday DST");

        assertT(Nows.with(n).endOfSunday(), "2013-11-24 23:59:59.999999999", "EndOfSunday");

        assertT(Nows.with(TIME_CARACAS).endOfSunday(), "2016-01-03 23:59:59.999999999", "EndOfSunday Caracas");

        assertT(Nows.with(nDst).endOfSunday(), "2017-10-29 23:59:59.999999999", "EndOfSunday DST");

        assertT(Nows.with(n).beginningOfWeek(), "2013-11-17 00:00:00", "BeginningOfWeek, FirstDayMonday");

        Nows.weekStartDay = Weekday.MONDAY;
        assertT(Nows.with(n).beginningOfWeek(), "2013-11-18 00:00:00", "BeginningOfWeek, FirstDayMonday");
    }

    @Test
    @Order(4)
    void parse() {
        GoTime n = GoTime.date(2013, 11, 18, 17, 51, 49, 123456789, Location.UTC);

        assertT(Nows.with(n).mustParse("2002"), "2002-01-01 00:00:00", "Parse 2002");

        assertT(Nows.with(n).mustParse("2002-10"), "2002-10-01 00:00:00", "Parse 2002-10");

        assertT(Nows.with(n).mustParse("2002-10-12"), "2002-10-12 00:00:00", "Parse 2002-10-12");

        assertT(Nows.with(n).mustParse("2002-10-12 22"), "2002-10-12 22:00:00", "Parse 2002-10-12 22");

        assertT(Nows.with(n).mustParse("2002-10-12 22:14"), "2002-10-12 22:14:00", "Parse 2002-10-12 22:14");

        assertT(Nows.with(n).mustParse("2002-10-12 2:4"), "2002-10-12 02:04:00", "Parse 2002-10-12 2:4");

        assertT(Nows.with(n).mustParse("2002-10-12 02:04"), "2002-10-12 02:04:00", "Parse 2002-10-12 02:04");

        assertT(Nows.with(n).mustParse("2002-10-12 22:14:56"), "2002-10-12 22:14:56", "Parse 2002-10-12 22:14:56");

        assertT(Nows.with(n).mustParse("2002-10-12 00:14:56"), "2002-10-12 00:14:56", "Parse 2002-10-12 00:14:56");

        assertT(Nows.with(n).mustParse("2013-12-19 23:28:09.999999999 +0800 CST"),
                "2013-12-19 23:28:09.999999999",
                "Parse two strings 2013-12-19 23:28:09.999999999 +0800 CST");

        assertT(Nows.with(n).mustParse("10-12"), "2013-10-12 00:00:00", "Parse 10-12");

        assertT(Nows.with(n).mustParse("18"), "2013-11-18 18:00:00", "Parse 18 as hour");

        assertT(Nows.with(n).mustParse("18:20"), "2013-11-18 18:20:00", "Parse 18:20");

        assertT(Nows.with(n).mustParse("00:01"), "2013-11-18 00:01:00", "Parse 00:01");

        assertT(Nows.with(n).mustParse("00:00:00"), "2013-11-18 00:00:00", "Parse 00:00:00");

        assertT(Nows.with(n).mustParse("18:20:39"), "2013-11-18 18:20:39", "Parse 18:20:39");

        assertT(Nows.with(n).mustParse("18:20:39", "2011-01-01"), "2011-01-01 18:20:39",
                "Parse two strings 18:20:39, 2011-01-01");

        assertT(Nows.with(n).mustParse("2011-1-1", "18:20:39"), "2011-01-01 18:20:39",
                "Parse two strings 2011-01-01, 18:20:39");

        assertT(Nows.with(n).mustParse("2011-01-01", "18"), "2011-01-01 18:00:00",
                "Parse two strings 2011-01-01, 18");

        assertT(Nows.with(n).mustParse("2002-10-12T00:14:56Z"), "2002-10-12 00:14:56",
                "Parse 2002-10-12T00:14:56Z");
        assertT(Nows.with(n).mustParse("2002-10-12T00:00:56Z"), "2002-10-12 00:00:56",
                "Parse 2002-10-12T00:00:56Z");
        assertT(Nows.with(n).mustParse("2002-10-12T00:00:00.999Z"), "2002-10-12 00:00:00.999",
                "Parse 2002-10-12T00:00:00.999Z");
        assertT(Nows.with(n).mustParse("2002-10-12T00:14:56.999999Z"), "2002-10-12 00:14:56.999999",
                "Parse 2002-10-12T00:14:56.999999Z");
        assertT(Nows.with(n).mustParse("2002-10-12T00:00:56.999999999Z"), "2002-10-12 00:00:56.999999999",
                "Parse 2002-10-12T00:00:56.999999999Z");

        assertT(Nows.with(n).mustParse("2002-10-12T00:14:56+08:00"), "2002-10-12 00:14:56",
                "Parse 2002-10-12T00:14:56+08:00");
        assertEquals(28800, Nows.with(n).mustParse("2002-10-12T00:14:56+08:00").zone().offset(),
                "Parse 2002-10-12T00:14:56+08:00 shouldn't lose time zone offset");

        assertT(Nows.with(n).mustParse("2002-10-12T00:00:56-07:00"), "2002-10-12 00:00:56",
                "Parse 2002-10-12T00:00:56-07:00");
        assertEquals(-25200, Nows.with(n).mustParse("2002-10-12T00:00:56-07:00").zone().offset(),
                "Parse 2002-10-12T00:00:56-07:00 shouldn't lose time zone offset");

        assertT(Nows.with(n).mustParse("2002-10-12T00:01:12.333+0200"), "2002-10-12 00:01:12.333",
                "Parse 2002-10-12T00:01:12.333+0200");
        assertEquals(7200, Nows.with(n).mustParse("2002-10-12T00:01:12.333+0200").zone().offset(),
                "Parse 2002-10-12T00:01:12.333+0200 shouldn't lose time zone offset");

        assertT(Nows.with(n).mustParse("2002-10-12T00:00:56.999999999+08:00"), "2002-10-12 00:00:56.999999999",
                "Parse 2002-10-12T00:00:56.999999999+08:00");
        assertEquals(28800, Nows.with(n).mustParse("2002-10-12T00:14:56.999999999+08:00").zone().offset(),
                "Parse 2002-10-12T00:14:56.999999999+08:00 shouldn't lose time zone offset");

        assertT(Nows.with(n).mustParse("2002-10-12T00:00:56.666666-07:00"), "2002-10-12 00:00:56.666666",
                "Parse 2002-10-12T00:00:56.666666-07:00");
        assertEquals(-25200, Nows.with(n).mustParse("2002-10-12T00:00:56.666666-07:00").zone().offset(),
                "Parse 2002-10-12T00:00:56.666666-07:00 shouldn't lose time zone offset");

        assertT(Nows.with(n).mustParse("2002-10-12T00:01:12.999999999-06"), "2002-10-12 00:01:12.999999999",
                "Parse 2002-10-12T00:01:12.999999999-06");
        assertEquals(-21600, Nows.with(n).mustParse("2002-10-12T00:01:12.999999999-06").zone().offset(),
                "Parse 2002-10-12T00:01:12.999999999-06 shouldn't lose time zone offset");

        Nows.timeFormats.add("02 Jan 15:04");
        assertT(Nows.with(n).mustParse("04 Feb 12:09"), "2013-02-04 12:09:00",
                "Parse 04 Feb 12:09 with specified format");

        assertT(Nows.with(n).mustParse("23:28:9 Dec 19, 2013 PST"), "2013-12-19 23:28:09",
                "Parse 23:28:9 Dec 19, 2013 PST");

        assertEquals("PST", Nows.with(n).mustParse("23:28:9 Dec 19, 2013 PST").location().toString(),
                "Parse 23:28:9 Dec 19, 2013 PST shouldn't lose time zone");

        GoTime n2 = Nows.with(n).mustParse("23:28:9 Dec 19, 2013 PST");
        assertEquals("PST", Nows.with(n2).mustParse("10:20").location().toString(),
                "Parse 10:20 shouldn't change time zone");

        Nows.timeFormats.add("2006-01-02T15:04:05.0");
        assertEquals("2018-02-13 15:17:06 +0000 UTC",
                Nows.mustParseInLocation(Location.UTC, "2018-02-13T15:17:06.0").toString(),
                "ParseInLocation 2018-02-13T15:17:06.0");

        Nows.timeFormats.add("2006-01-02 15:04:05.000");
        assertT(Nows.with(n).mustParse("2018-04-20 21:22:23.473"), "2018-04-20 21:22:23.473",
                "Parse 2018/04/20 21:22:23.473");

        Nows.timeFormats.add("15:04:05.000");
        assertT(Nows.with(n).mustParse("13:00:01.365"), "2013-11-18 13:00:01.365", "Parse 13:00:01.365");

        Nows.timeFormats.add("2006-01-02 15:04:05.000000");
        assertT(Nows.with(n).mustParse("2010-01-01 07:24:23.131384"), "2010-01-01 07:24:23.131384",
                "Parse 2010-01-01 07:24:23.131384");
        assertT(Nows.with(n).mustParse("00:00:00.182736"), "2013-11-18 00:00:00.182736",
                "Parse 00:00:00.182736");

        GoTime n3 = Nows.mustParse("2017-12-11T10:25:49Z");
        assertEquals(Location.UTC, n3.location(), "time location should be UTC");
    }

    @Test
    @Order(5)
    void between() {
        GoTime tm = GoTime.date(2015, 6, 30, 17, 51, 49, 123456789, Location.local());
        assertTrue(Nows.with(tm).between("23:28:9 Dec 19, 2013 PST", "23:28:9 Dec 19, 2015 PST"), "Between");

        assertTrue(Nows.with(tm).between("2015-05-12 12:20", "2015-06-30 17:51:50"), "Between");
    }

    @Test
    @Order(6)
    void config() throws TimeParseException {
        Location location = Location.load("Asia/Shanghai");

        Config myConfig = new Config(Weekday.MONDAY, location, List.of("2006-01-02 15:04:05"));

        // 2013-11-18 17:51:49.123456789 Mon
        GoTime n = GoTime.date(2013, 11, 18, 17, 51, 49, 123456789, Location.local());
        assertT(myConfig.with(n).beginningOfWeek(), "2013-11-18 00:00:00", "BeginningOfWeek, FirstDayMonday");

        assertEquals("2018-02-13 15:17:06 +0800 CST", myConfig.parse("2018-02-13 15:17:06").toString(),
                "ParseInLocation 2018-02-13T15:17:06.0");

        assertEquals("2018-02-13 15:17:06 +0800 CST", myConfig.mustParse("2018-02-13 15:17:06").toString(),
                "ParseInLocation 2018-02-13T15:17:06.0");
    }

    @Test
    @Order(7)
    void quarter() {
        record TestCase(GoTime givenDate, int expectedQuarter) {
        }

        List<TestCase> tests = List.of(
                new TestCase(GoTime.date(2021, 6, 18, 0, 0, 0, 0, Location.UTC), 2),
                new TestCase(GoTime.date(2021, 7, 18, 0, 0, 0, 0, Location.UTC), 3));

        for (TestCase tc : tests) {
            int got = Nows.with(tc.givenDate()).quarter();
            assertEquals(tc.expectedQuarter(), got, "Quarter " + tc.expectedQuarter() + " expected, got " + got);
        }
    }

    /**
     * Port of the {@code Example} function in {@code now_test.go}. Go compiles but does not run
     * examples without an output comment; here it doubles as a smoke test of the package API.
     */
    @Test
    @Order(8)
    void example() {
        Weekday saved = Nows.weekStartDay;
        try {
            Nows.beginningOfMinute(); // 2013-11-18 17:51:00 Mon
            Nows.beginningOfHour();   // 2013-11-18 17:00:00 Mon
            Nows.beginningOfDay();    // 2013-11-18 00:00:00 Mon
            Nows.beginningOfWeek();   // 2013-11-17 00:00:00 Sun

            Nows.weekStartDay = Weekday.MONDAY; // Set Monday as first day
            Nows.beginningOfWeek();             // 2013-11-18 00:00:00 Mon
            Nows.beginningOfMonth();            // 2013-11-01 00:00:00 Fri
            Nows.beginningOfQuarter();          // 2013-10-01 00:00:00 Tue
            Nows.beginningOfYear();             // 2013-01-01 00:00:00 Tue

            Nows.endOfMinute(); // 2013-11-18 17:51:59.999999999 Mon
            Nows.endOfHour();   // 2013-11-18 17:59:59.999999999 Mon
            Nows.endOfDay();    // 2013-11-18 23:59:59.999999999 Mon
            Nows.endOfWeek();   // 2013-11-23 23:59:59.999999999 Sat

            Nows.weekStartDay = Weekday.MONDAY; // Set Monday as first day
            Nows.endOfWeek();                   // 2013-11-24 23:59:59.999999999 Sun
            Nows.endOfMonth();                  // 2013-11-30 23:59:59.999999999 Sat
            Nows.endOfQuarter();                // 2013-12-31 23:59:59.999999999 Tue
            Nows.endOfYear();                   // 2013-12-31 23:59:59.999999999 Tue

            // Use another time
            GoTime t = GoTime.date(2013, 2, 18, 17, 51, 49, 123456789, Location.UTC);
            assertT(Nows.with(t).endOfMonth(), "2013-02-28 23:59:59.999999999", "EndOfMonth");

            Nows.monday();        // 2013-11-18 00:00:00 Mon
            Nows.monday("17:44"); // 2013-11-18 17:44:00 Mon
            Nows.sunday();        // 2013-11-24 00:00:00 Sun
            Nows.sunday("17:44"); // 2013-11-24 17:44:00 Sun
            Nows.endOfSunday();   // 2013-11-24 23:59:59.999999999 Sun
        } finally {
            Nows.weekStartDay = saved;
        }
    }
}
