package checker_go

import "github.com/bartosz11/checkmate/config"

// InitializeChecker is the entrypoint of the checker module
func InitializeChecker(cfg *config.CheckerConfig) {
	for _, orchestratorURL := range cfg.Orchestrators {
		go Connect(orchestratorURL, cfg.Key)
	}
}
