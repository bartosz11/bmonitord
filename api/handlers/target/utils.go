package target

import (
	"errors"
	"strings"
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
