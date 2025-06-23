package main

import (
	"bmonitord/config"
	"bmonitord/internal/database"
	"bmonitord/internal/orchestrator"
	"bmonitord/internal/orchestrator/helpers"
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

	db := database.InitDatabase(cfg)

	router := gin.Default()

	helpers.InitEmail(&cfg)

	orchestrator.StartOrchestrator(cfg, db, router)

	err := router.Run()
	if err != nil {
		log.Fatal().Err(err).Msg("Failed starting router!")
	}

	select {}
}
