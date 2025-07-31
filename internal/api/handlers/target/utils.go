package target

import (
	"errors"
	"github.com/bartosz11/checkmate/internal/database/model"
	"strings"
	"unicode"
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
