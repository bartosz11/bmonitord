package helpers

import (
	"reflect"
	"strings"
	"unicode"

	"github.com/go-playground/validator/v10"
)

func IsStrongPassword(password string) bool {
	if len(password) < 8 {
		return false
	}

	var hasUpper, hasLower, hasDigit bool
	for _, c := range password {
		switch {
		case 'A' <= c && c <= 'Z':
			hasUpper = true
		case 'a' <= c && c <= 'z':
			hasLower = true
		case '0' <= c && c <= '9':
			hasDigit = true
		}
	}

	return hasUpper && hasLower && hasDigit
}

func ContainsAnySpace(s string) bool {
	for _, c := range s {
		if unicode.IsSpace(c) {
			return true
		}
	}
	return false
}

func IsBlank(s string) bool {
	return strings.TrimSpace(s) == ""
}

func ValidateRequiredUint(fl validator.FieldLevel) bool {
	//This exists just for the sake of validating uints properly, 0 is a value we can't ignore in most cases
	field := fl.Field()

	switch field.Kind() {
	case reflect.Uint, reflect.Uint8, reflect.Uint16, reflect.Uint32, reflect.Uint64:
		return true // All unsigned ints are always considered valid (even 0)
	default:
		return false
	}
}

// IsValidStatuspageSlug tests a string against the criteria that must be met in order for such string to be a statuspage slug. Returns true if all criteria are met, false otherwise. Criteria:
// 1. String must not be empty
// 2. String must not start or end with a hyphen (-)
// 3. Only a-z and 0-9 characters are allowed in the string. Hyphens are allowed in the middle.
// 4. Hyphens cannot be consecutive
func IsValidStatuspageSlug(slug string) bool {
	if len(slug) == 0 {
		return false
	}

	// must not start or end with '-'
	if slug[0] == '-' || slug[len(slug)-1] == '-' {
		return false
	}

	prevHyphen := false

	for i := 0; i < len(slug); i++ {
		c := slug[i]

		if c >= 'a' && c <= 'z' || c >= '0' && c <= '9' {
			prevHyphen = false
			continue
		}

		if c == '-' {
			if prevHyphen {
				return false // disallow consecutive hyphens
			}
			prevHyphen = true
			continue
		}

		// invalid character - only a-z, 0-9 and non-consecutive hyphens are allowed in the middle of the string
		return false
	}

	return true
}
