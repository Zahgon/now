// Package now is a time toolkit for golang.
//
// More details README here: https://github.com/jinzhu/now
//
//	import "github.com/jinzhu/now"
//
//	now.BeginningOfMinute() // 2013-11-18 17:51:00 Mon
//	now.BeginningOfDay()    // 2013-11-18 00:00:00 Mon
//	now.EndOfDay()          // 2013-11-18 23:59:59.999999999 Mon
package now

import "time"

// WeekStartDay set week start day, default is sunday
var WeekStartDay = time.Sunday

// TimeFormats default time formats will be parsed as
var TimeFormats = []string{
	"2006", "2006-1", "2006-1-2", "2006-1-2 15", "2006-1-2 15:4", "2006-1-2 15:4:5", "1-2",
	"15:4:5", "15:4", "15",
	"15:4:5 Jan 2, 2006 MST", "2006-01-02 15:04:05.999999999 -0700 MST", "2006-01-02T15:04:05Z0700", "2006-01-02T15:04:05Z07",
	"2006.1.2", "2006.1.2 15:04:05", "2006.01.02", "2006.01.02 15:04:05", "2006.01.02 15:04:05.999999999",
	"1/2/2006", "1/2/2006 15:4:5", "2006/01/02", "20060102", "2006/01/02 15:04:05",
	time.ANSIC, time.UnixDate, time.RubyDate, time.RFC822, time.RFC822Z, time.RFC850,
	time.RFC1123, time.RFC1123Z, time.RFC3339, time.RFC3339Nano,
	time.Kitchen, time.Stamp, time.StampMilli, time.StampMicro, time.StampNano,
}

// Config configuration for now package
type Config struct {
	WeekStartDay time.Weekday
	TimeLocation *time.Location
	TimeFormats  []string
}

// DefaultConfig default config
var DefaultConfig *Config

// With initializes Now based on configuration
func (config *Config) With(t time.Time) *Now { _ = "STUB: not implemented"; return nil }

// Parse parses string to time based on configuration
func (config *Config) Parse(strs ...string) (time.Time, error) {
	_ = "STUB: not implemented"
	return *new(time.Time), nil
}

// MustParse must parse string to time or will panic
func (config *Config) MustParse(strs ...string) time.Time {
	_ = "STUB: not implemented"
	return *new(time.Time)
}

// Now now struct
type Now struct {
	time.Time
	*Config
}

// With initialize Now with time
func With(t time.Time) *Now { _ = "STUB: not implemented"; return nil }

// New initialize Now with time
func New(t time.Time) *Now {
	_ = "STUB: not implemented"

	// BeginningOfMinute beginning of minute
	return nil
}

func BeginningOfMinute() time.Time { _ = "STUB: not implemented"; return *new(time.Time) }

// BeginningOfHour beginning of hour
func BeginningOfHour() time.Time { _ = "STUB: not implemented"; return *new(time.Time) }

// BeginningOfDay beginning of day
func BeginningOfDay() time.Time { _ = "STUB: not implemented"; return *new(time.Time) }

// BeginningOfWeek beginning of week
func BeginningOfWeek() time.Time { _ = "STUB: not implemented"; return *new(time.Time) }

// BeginningOfMonth beginning of month
func BeginningOfMonth() time.Time { _ = "STUB: not implemented"; return *new(time.Time) }

// BeginningOfQuarter beginning of quarter
func BeginningOfQuarter() time.Time { _ = "STUB: not implemented"; return *new(time.Time) }

// BeginningOfYear beginning of year
func BeginningOfYear() time.Time { _ = "STUB: not implemented"; return *new(time.Time) }

// EndOfMinute end of minute
func EndOfMinute() time.Time { _ = "STUB: not implemented"; return *new(time.Time) }

// EndOfHour end of hour
func EndOfHour() time.Time { _ = "STUB: not implemented"; return *new(time.Time) }

// EndOfDay end of day
func EndOfDay() time.Time { _ = "STUB: not implemented"; return *new(time.Time) }

// EndOfWeek end of week
func EndOfWeek() time.Time { _ = "STUB: not implemented"; return *new(time.Time) }

// EndOfMonth end of month
func EndOfMonth() time.Time { _ = "STUB: not implemented"; return *new(time.Time) }

// EndOfQuarter end of quarter
func EndOfQuarter() time.Time { _ = "STUB: not implemented"; return *new(time.Time) }

// EndOfYear end of year
func EndOfYear() time.Time { _ = "STUB: not implemented"; return *new(time.Time) }

// Monday returns the time.Time value of Monday
func Monday(strs ...string) time.Time { _ = "STUB: not implemented"; return *new(time.Time) }

// Sunday returns the time.Time value of Sunday
func Sunday(strs ...string) time.Time { _ = "STUB: not implemented"; return *new(time.Time) }

// EndOfSunday end of sunday
func EndOfSunday() time.Time { _ = "STUB: not implemented"; return *new(time.Time) }

// Quarter returns the yearly quarter
func Quarter() uint { _ = "STUB: not implemented"; return 0 }

// Parse parse string to time
func Parse(strs ...string) (time.Time, error) {
	_ = "STUB: not implemented"
	return *new(time.Time), nil
}

// ParseInLocation parse string to time in location
func ParseInLocation(loc *time.Location, strs ...string) (time.Time, error) {
	_ = "STUB: not implemented"
	return *new(time.Time), nil
}

// MustParse must parse string to time or will panic
func MustParse(strs ...string) time.Time { _ = "STUB: not implemented"; return *new(time.Time) }

// MustParseInLocation must parse string to time in location or will panic
func MustParseInLocation(loc *time.Location, strs ...string) time.Time {
	_ = "STUB: not implemented"
	return *new(time.Time)
}

// Between check now between the begin, end time or not
func Between(time1, time2 string) bool { _ = "STUB: not implemented"; return false }
