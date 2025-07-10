package helpers

import (
	"github.com/go-playground/validator/v10"
	"reflect"
	"strings"
	"unicode"
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
