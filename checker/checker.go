package checker

import (
	"github.com/bartosz11/checkmate/common/config"
)

// StartChecker is the entrypoint of the checker module
func StartChecker(cfg *config.CheckerConfig) {
	for _, orchestratorURL := range cfg.Orchestrators {
		go Connect(orchestratorURL, cfg.Key)
	}
}
