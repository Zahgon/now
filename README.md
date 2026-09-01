## Now

Now is a time toolkit for Java — a port of [jinzhu/now](https://github.com/jinzhu/now).

## Install

```xml
<dependency>
  <groupId>com.github.jinzhu</groupId>
  <artifactId>now</artifactId>
  <version>1.0-SNAPSHOT</version>
</dependency>
```

Requires Java 17 or later.

## Usage

Calculating time based on current time

```java
import com.github.jinzhu.now.*;

GoTime.now(); // 2013-11-18 17:51:49.123456789 Mon

Nows.beginningOfMinute();          // 2013-11-18 17:51:00 Mon
Nows.beginningOfHour();            // 2013-11-18 17:00:00 Mon
Nows.beginningOfDay();             // 2013-11-18 00:00:00 Mon
Nows.beginningOfWeek();            // 2013-11-17 00:00:00 Sun
Nows.beginningOfMonth();           // 2013-11-01 00:00:00 Fri
Nows.beginningOfQuarter();         // 2013-10-01 00:00:00 Tue
Nows.beginningOfYear();            // 2013-01-01 00:00:00 Tue

Nows.endOfMinute();                // 2013-11-18 17:51:59.999999999 Mon
Nows.endOfHour();                  // 2013-11-18 17:59:59.999999999 Mon
Nows.endOfDay();                   // 2013-11-18 23:59:59.999999999 Mon
Nows.endOfWeek();                  // 2013-11-23 23:59:59.999999999 Sat
Nows.endOfMonth();                 // 2013-11-30 23:59:59.999999999 Sat
Nows.endOfQuarter();               // 2013-12-31 23:59:59.999999999 Tue
Nows.endOfYear();                  // 2013-12-31 23:59:59.999999999 Tue

Nows.weekStartDay = Weekday.MONDAY; // Set Monday as first day, default is Sunday
Nows.endOfWeek();                   // 2013-11-24 23:59:59.999999999 Sun
```

Calculating time based on another time

```java
GoTime t = GoTime.date(2013, 2, 18, 17, 51, 49, 123456789, Location.local());
Nows.with(t).endOfMonth();  // 2013-02-28 23:59:59.999999999 Thu
```

Calculating time based on configuration

```java
Location location = Location.load("Asia/Shanghai");

Config myConfig = new Config(
        Weekday.MONDAY,
        location,
        List.of("2006-01-02 15:04:05"));

GoTime t = GoTime.date(2013, 11, 18, 17, 51, 49, 123456789, Location.local()); // 2013-11-18 17:51:49.123456789 Mon
myConfig.with(t).beginningOfWeek();        // 2013-11-18 00:00:00 Mon

myConfig.parse("2002-10-12 22:14:01");     // 2002-10-12 22:14:01
myConfig.parse("2002-10-12 22:14");        // throws TimeParseException "Can't parse string as time: 2002-10-12 22:14"
```

### Monday/Sunday

Don't be bothered with the `weekStartDay` setting, you can use `monday`, `sunday`

```java
Nows.monday();              // 2013-11-18 00:00:00 Mon
Nows.monday("17:44");       // 2013-11-18 17:44:00 Mon
Nows.sunday();              // 2013-11-24 00:00:00 Sun (Next Sunday)
Nows.sunday("18:19:24");    // 2013-11-24 18:19:24 Sun (Next Sunday)
Nows.endOfSunday();         // 2013-11-24 23:59:59.999999999 Sun (End of next Sunday)

GoTime t = GoTime.date(2013, 11, 24, 17, 51, 49, 123456789, Location.local()); // 2013-11-24 17:51:49.123456789 Sun
Nows.with(t).monday();              // 2013-11-18 00:00:00 Mon (Last Monday if today is Sunday)
Nows.with(t).monday("17:44");       // 2013-11-18 17:44:00 Mon (Last Monday if today is Sunday)
Nows.with(t).sunday();              // 2013-11-24 00:00:00 Sun (Beginning Of Today if today is Sunday)
Nows.with(t).sunday("18:19:24");    // 2013-11-24 18:19:24 Sun (Beginning Of Today if today is Sunday)
Nows.with(t).endOfSunday();         // 2013-11-24 23:59:59.999999999 Sun (End of Today if today is Sunday)
```

### Parse String to Time

```java
GoTime.now(); // 2013-11-18 17:51:49.123456789 Mon

// GoTime parse(String...) throws TimeParseException
GoTime t = Nows.parse("2017");                // 2017-01-01 00:00:00
GoTime t = Nows.parse("2017-10");             // 2017-10-01 00:00:00
GoTime t = Nows.parse("2017-10-13");          // 2017-10-13 00:00:00
GoTime t = Nows.parse("1999-12-12 12");       // 1999-12-12 12:00:00
GoTime t = Nows.parse("1999-12-12 12:20");    // 1999-12-12 12:20:00
GoTime t = Nows.parse("1999-12-12 12:20:21"); // 1999-12-12 12:20:21
GoTime t = Nows.parse("10-13");               // 2013-10-13 00:00:00
GoTime t = Nows.parse("12:20");               // 2013-11-18 12:20:00
GoTime t = Nows.parse("12:20:13");            // 2013-11-18 12:20:13
GoTime t = Nows.parse("14");                  // 2013-11-18 14:00:00
GoTime t = Nows.parse("99:99");               // throws TimeParseException "Can't parse string as time: 99:99"

// mustParse must parse the string to a time or it throws DateTimeException
Nows.mustParse("2013-01-13");             // 2013-01-13 00:00:00
Nows.mustParse("02-17");                  // 2013-02-17 00:00:00
Nows.mustParse("2-17");                   // 2013-02-17 00:00:00
Nows.mustParse("8");                      // 2013-11-18 08:00:00
Nows.mustParse("2002-10-12 22:14");       // 2002-10-12 22:14:00
Nows.mustParse("99:99");                  // throws DateTimeException: Can't parse string as time: 99:99
```

Extending `now` to support more formats is quite easy, just add other time layouts to `Nows.timeFormats`, e.g:

```java
Nows.timeFormats.add("02 Jan 2006 15:04");
```

## Notes on the port

The Java API mirrors the Go one, with a few adjustments the language forces:

- **`Nows` holds the package-level functions.** Go has both a `now.Now` struct and package-level
  functions with the same names, e.g. `now.BeginningOfDay()` next to `(*Now).BeginningOfDay()`.
  Java cannot put a static and an instance method of the same signature on one class, so the
  struct methods live on `Now` and the package-level functions live on `Nows`.

- **Layouts stay Go reference layouts.** `timeFormats` still takes strings like
  `"2006-01-02 15:04:05"`, not `DateTimeFormatter` patterns, so existing format lists carry over
  unchanged. Go's layout scanner, formatter and parser are ported in `GoLayout`.

- **`GoTime` rather than `ZonedDateTime`.** `ZonedDateTime` cannot carry the anonymous zones
  Go's parser fabricates (`Parse("23:28:9 Dec 19, 2013 PST")` keeps a zone literally named
  `PST`), and it resolves daylight-saving gaps and overlaps by different rules than Go. Both
  behaviours are observable through this library, so `time.Time` is ported as `GoTime`.
  `GoTime.toZonedDateTime()` and `GoTime.toInstant()` hand back standard types.

- **`Weekday` counts Sunday as 0**, like Go, not Monday as 1 like `java.time.DayOfWeek`. The
  week arithmetic depends on it. `Weekday.from(DayOfWeek)` and `Weekday.toDayOfWeek()` convert.

- **`parse` throws a checked `TimeParseException`**, matching Go's returned `error`, while
  `mustParse` throws an unchecked `DateTimeException`, matching Go's panic.

## Build

```
mvn test
```

## License

Released under the [MIT License](http://www.opensource.org/licenses/MIT).
