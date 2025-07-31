package main

import (
	"github.com/bartosz11/checkmate/config"
	"github.com/bartosz11/checkmate/internal/api"
	"github.com/bartosz11/checkmate/internal/database"
	"github.com/bartosz11/checkmate/internal/orchestrator/helpers"
	"github.com/gin-gonic/gin"
	"github.com/rs/zerolog"
	"github.com/rs/zerolog/log"
	"os"
)

func main() {
	cfg := config.LoadConfig()
	if cfg.PrettyLogging {
		log.Logger = log.Output(zerolog.ConsoleWriter{Out: os.Stderr})
	}

	zerolog.SetGlobalLevel(zerolog.Level(cfg.LoggingLevel))

	db := database.InitDatabase(&cfg.DatabaseConfig)

	if cfg.Production {
		gin.SetMode(gin.ReleaseMode)
	}

	router := gin.Default()

	helpers.InitEmail(&cfg.EmailConfig)

	//orchestrator.StartOrchestrator(&cfg.OrchestratorConfig, db, router)

	api.StartAPI(db, router, cfg.Production, &cfg.APIConfig)

	err := router.Run()
	if err != nil {
		log.Fatal().Err(err).Msg("Failed starting router!")
	}

}
