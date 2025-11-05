package helpers

import (
	"fmt"
	"time"
)

func HumanizeDuration(d time.Duration) string {
	if d < 0 {
		d = -d
	}

	days := int(d.Hours()) / 24
	hours := int(d.Hours()) % 24
	minutes := int(d.Minutes()) % 60
	seconds := int(d.Seconds()) % 60

	var parts []string

	if days > 0 {
		parts = append(parts, plural(days, "day"))
	}
	if hours > 0 {
		parts = append(parts, plural(hours, "hour"))
	}
	if minutes > 0 {
		parts = append(parts, plural(minutes, "minute"))
	}
	if seconds > 0 || len(parts) == 0 {
		parts = append(parts, plural(seconds, "second"))
	}

	// Join with commas and spaces
	return joinParts(parts)
}

// plural adds "s" when needed
func plural(value int, unit string) string {
	if value == 1 {
		return fmt.Sprintf("%d %s", value, unit)
	}
	return fmt.Sprintf("%d %ss", value, unit)
}

func joinParts(parts []string) string {
	result := parts[0]
	for i := 1; i < len(parts); i++ {
		result += ", " + parts[i]
	}
	return result
}
