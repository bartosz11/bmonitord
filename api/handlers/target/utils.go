package target

import (
	"errors"
	"math"
	"sort"
	"strings"
	"time"
	"unicode"

	"github.com/bartosz11/checkmate/api/helpers"
	"github.com/bartosz11/checkmate/common/database/model"
	"gorm.io/gorm"
)

func ValidateAllowedCodes(codes string) error {
	codes = strings.TrimSpace(codes)
	split := strings.Split(codes, " ")

	if len(split) == 0 {
		return errors.New("there must be at least one allowed HTTP response code")
	}

	for _, code := range split {
		if len(code) != 3 {
			return errors.New("HTTP response codes must be exactly 3 digits long: " + code)
		}
		for _, char := range code {
			if !unicode.IsDigit(char) {
				return errors.New("HTTP response code contains non-digit characters: " + code)
			}
		}
	}

	return nil
}

func SanitizeTarget(t *model.Target) {
	for i := range t.Checkers {
		t.Checkers[i].Key = ""
	}
}

func SanitizeTargets(targets *[]model.Target) {
	for i := range *targets {
		SanitizeTarget(&(*targets)[i])
	}
}

func GenerateUniqueAgentKey(db *gorm.DB) string {
	var key string
	for {
		key = helpers.GenerateRandomAlphanumericString(20)
		var count int64

		err := db.Model(&model.Agent{}).Where("key = ?", key).Count(&count).Error
		if err != nil {
			return ""
		}

		if count == 0 {
			break
		}
	}
	return key
}

type Interval struct {
	Start time.Time
	End   time.Time
}

// CalculateUptime returns uptime percentage with 3 decimal point precision for a target with given incident in given time range, incidents are expected to be pre-sorted with start date ascending
func CalculateUptime(rangeStart time.Time, rangeEnd time.Time, incidents []model.Incident) float64 {
	var intervals []Interval

	for _, incident := range incidents {
		start := incident.Start
		end := incident.End

		if incident.Ongoing {
			end = rangeEnd
		}

		// skip if no overlap
		if start.After(rangeEnd) || end.Before(rangeStart) {
			continue
		}

		// clamp to range boundaries if necessary
		if start.Before(rangeStart) {
			start = rangeStart
		}
		if end.After(rangeEnd) {
			end = rangeEnd
		}

		// shouldn't happen
		if !start.Before(end) {
			continue
		}

		intervals = append(intervals, Interval{Start: start, End: end})
	}

	if len(intervals) == 0 {
		return 100
	}

	sort.Slice(intervals, func(i, j int) bool {
		return intervals[i].Start.Before(intervals[j].Start)
	})

	merged := MergeIntervals(intervals)
	downtime := TotalDuration(merged)
	rangeDuration := rangeEnd.Sub(rangeStart)

	if rangeDuration <= 0 {
		return 100
	}

	uptime := float64(rangeDuration-downtime) / float64(rangeDuration) * 100

	if uptime < 0 {
		return 0
	}
	if uptime > 100 {
		return 100
	}

	return RoundToDecimalPlaces(uptime, 3)
}

func MergeIntervals(intervals []Interval) []Interval {
	if len(intervals) == 0 {
		return nil
	}

	merged := make([]Interval, 0, len(intervals))
	current := intervals[0]

	for i := 1; i < len(intervals); i++ {
		next := intervals[i]

		// Overlap or touching (<= is intentional)
		if !next.Start.After(current.End) {
			// Extend current interval if needed
			if next.End.After(current.End) {
				current.End = next.End
			}
		} else {
			// No overlap, push current and start new
			merged = append(merged, current)
			current = next
		}
	}

	// Push the last interval
	merged = append(merged, current)
	return merged
}

func TotalDuration(intervals []Interval) time.Duration {
	var total time.Duration
	for _, interval := range intervals {
		total += interval.End.Sub(interval.Start)
	}
	return total
}

func RoundToDecimalPlaces(x float64, decimalPlaces int) float64 {
	factor := math.Pow(10, float64(decimalPlaces))
	return math.Round(x*factor) / factor
}
