package checker

import "github.com/bartosz11/checkmate/internal/database/model"

func SanitizeChecker(checker *model.Checker) {
	checker.Key = ""
}

func SanitizeCheckers(checkers *[]model.Checker) {
	for i := range *checkers {
		SanitizeChecker(&(*checkers)[i])
	}
}
