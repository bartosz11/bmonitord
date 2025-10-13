package main

import (
	"github.com/bartosz11/checkmate/common"
	"github.com/bartosz11/checkmate/common/config"
	"github.com/bartosz11/checkmate/common/database"
	"github.com/bartosz11/checkmate/common/helpers"
	"github.com/bartosz11/checkmate/orchestrator"
)

// main serves as the entrypoint for the standalone orchestrator binary
func main() {
	cfg := config.LoadConfig()

	common.SetupLogging(&cfg)

	db := database.InitDatabase(&cfg.DatabaseConfig)

	router := common.SetupRouter(&cfg)

	helpers.InitEmail(&cfg.EmailConfig)

	orchestrator.StartOrchestrator(&cfg.OrchestratorConfig, db, router)

	common.StartRouter(router, cfg.Bind)
}
