package now

import (
	"regexp"
	"time"
)

// BeginningOfMinute beginning of minute
func (now *Now) BeginningOfMinute() time.Time { _ = "STUB: not implemented"; return *new(time.Time) }

// BeginningOfHour beginning of hour
func (now *Now) BeginningOfHour() time.Time { _ = "STUB: not implemented"; return *new(time.Time) }

// BeginningOfDay beginning of day
func (now *Now) BeginningOfDay() time.Time { _ = "STUB: not implemented"; return *new(time.Time) }

// BeginningOfWeek beginning of week
func (now *Now) BeginningOfWeek() time.Time { _ = "STUB: not implemented"; return *new(time.Time) }

// BeginningOfMonth beginning of month
func (now *Now) BeginningOfMonth() time.Time { _ = "STUB: not implemented"; return *new(time.Time) }

// BeginningOfQuarter beginning of quarter
func (now *Now) BeginningOfQuarter() time.Time { _ = "STUB: not implemented"; return *new(time.Time) }

// BeginningOfHalf beginning of half year
func (now *Now) BeginningOfHalf() time.Time { _ = "STUB: not implemented"; return *new(time.Time) }

// BeginningOfYear BeginningOfYear beginning of year
func (now *Now) BeginningOfYear() time.Time { _ = "STUB: not implemented"; return *new(time.Time) }

// EndOfMinute end of minute
func (now *Now) EndOfMinute() time.Time { _ = "STUB: not implemented"; return *new(time.Time) }

// EndOfHour end of hour
func (now *Now) EndOfHour() time.Time { _ = "STUB: not implemented"; return *new(time.Time) }

// EndOfDay end of day
func (now *Now) EndOfDay() time.Time { _ = "STUB: not implemented"; return *new(time.Time) }

// EndOfWeek end of week
func (now *Now) EndOfWeek() time.Time { _ = "STUB: not implemented"; return *new(time.Time) }

// EndOfMonth end of month
func (now *Now) EndOfMonth() time.Time { _ = "STUB: not implemented"; return *new(time.Time) }

// EndOfQuarter end of quarter
func (now *Now) EndOfQuarter() time.Time { _ = "STUB: not implemented"; return *new(time.Time) }

// EndOfHalf end of half year
func (now *Now) EndOfHalf() time.Time { _ = "STUB: not implemented"; return *new(time.Time) }

// EndOfYear end of year
func (now *Now) EndOfYear() time.Time { _ = "STUB: not implemented"; return *new(time.Time) }

// Monday monday
/*
func (now *Now) Monday() time.Time {
	t := now.BeginningOfDay()
	weekday := int(t.Weekday())
	if weekday == 0 {
		weekday = 7
	}
	return t.AddDate(0, 0, -weekday+1)
}
*/

// Monday returns the Monday Date of the week specified by now
func (now *Now) Monday(strs ...string) time.Time { _ = "STUB: not implemented"; return *new(time.Time) }

// Sunday returns the Sunday Date of the week specified by now
func (now *Now) Sunday(strs ...string) time.Time { _ = "STUB: not implemented"; return *new(time.Time) }

// EndOfSunday end of sunday
func (now *Now) EndOfSunday() time.Time { _ = "STUB: not implemented"; return *new(time.Time) }

// Quarter returns the yearly quarter
func (now *Now) Quarter() uint { _ = "STUB: not implemented"; return 0 }

func (now *Now) parseWithFormat(str string, location *time.Location) (t time.Time, err error) {
	_ = "STUB: not implemented"
	return *new(time.Time), nil
}

var hasTimeRegexp = regexp.MustCompile(`(\s+|^\s*|T)\d{1,2}((:\d{1,2})*|((:\d{1,2}){2}\.(\d{3}|\d{6}|\d{9})))(\s*$|[Z+-])`) // match 15:04:05, 15:04:05.000, 15:04:05.000000 15, 2017-01-01 15:04, 2021-07-20T00:59:10Z, 2021-07-20T00:59:10+08:00, 2021-07-20T00:00:10-07:00 etc
var onlyTimeRegexp = regexp.MustCompile(`^\s*\d{1,2}((:\d{1,2})*|((:\d{1,2}){2}\.(\d{3}|\d{6}|\d{9})))\s*$`)                // match 15:04:05, 15, 15:04:05.000, 15:04:05.000000, etc

// Parse parses string to time
func (now *Now) Parse(strs ...string) (t time.Time, err error) {
	_ = "STUB: not implemented"
	return *new(time.Time), nil
}

// match 15:04:05, 15

// Don't reset hour, minute, second if current time str including time

// If value is zero, replace it with current time

// if current time only includes time, should change day, month to current time

// MustParse must parse string to time or it will panic
func (now *Now) MustParse(strs ...string) (t time.Time) {
	_ = "STUB: not implemented"
	return *new(time.Time)
}

// Between check time between the begin, end time or not
func (now *Now) Between(begin, end string) bool { _ = "STUB: not implemented"; return false }
