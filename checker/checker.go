package checker

import (
	"github.com/bartosz11/checkmate/common/config"
)

// InitializeChecker is the entrypoint of the checker module
func InitializeChecker(cfg *config.CheckerConfig) {
	for _, orchestratorURL := range cfg.Orchestrators {
		go Connect(orchestratorURL, cfg.Key)
	}
}
