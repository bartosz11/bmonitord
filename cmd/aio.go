package main

import (
	"errors"
	"fmt"
	"strings"

	"github.com/bartosz11/checkmate/api"
	checkerApi "github.com/bartosz11/checkmate/api/handlers/checker"
	"github.com/bartosz11/checkmate/checker"
	"github.com/bartosz11/checkmate/common"
	"github.com/bartosz11/checkmate/common/config"
	"github.com/bartosz11/checkmate/common/database"
	"github.com/bartosz11/checkmate/common/database/model"
	"github.com/bartosz11/checkmate/common/helpers"
	"github.com/bartosz11/checkmate/orchestrator"
	"github.com/rs/zerolog/log"
	"gorm.io/gorm"
)

// main serves as the entrypoint for the all-in-one (AIO) binary/image
func main() {
	cfg := config.LoadConfig()

	common.SetupLogging(&cfg)

	db := database.InitDatabase(&cfg.DatabaseConfig)

	sysOrchestrator, sysChecker := BootstrapAIO(db)
	cfg.OrchestratorConfig.Name = sysOrchestrator.Name
	cfg.CheckerConfig.Key = sysChecker.Key
	bind := cfg.Bind
	var orchestratorAddr string
	if strings.HasPrefix(bind, ":") {
		// This means the user has specified only the port to bind to, so we need to prepend the loopback address (127.0.0.1)
		orchestratorAddr = "127.0.0.1" + bind
	} else {
		// Otherwise use the entire address because the Gin might not bind to loopback
		orchestratorAddr = bind
	}
	cfg.CheckerConfig.Orchestrators = append(cfg.CheckerConfig.Orchestrators, fmt.Sprintf("ws://%s/orchestrator/ws", orchestratorAddr))

	router := common.SetupRouter(&cfg)

	helpers.InitEmail(&cfg.EmailConfig)

	orchestrator.StartOrchestrator(&cfg.OrchestratorConfig, db, router)

	api.StartAPI(db, router, cfg.Production, &cfg.APIConfig)

	checker.StartChecker(&cfg.CheckerConfig)

	common.StartRouter(router, bind)

}

func BootstrapAIO(db *gorm.DB) (model.Orchestrator, model.Checker) {
	var systemOrchestrator model.Orchestrator
	err := db.First(&systemOrchestrator, "system = true").Error
	if errors.Is(err, gorm.ErrRecordNotFound) {
		systemOrchestrator = model.Orchestrator{
			Name:   "AIO built-in orchestrator",
			Host:   "ws://127.0.0.1:8080/orchestrator/ws",
			System: true,
		}
		if db.Create(&systemOrchestrator).Error != nil {
			log.Fatal().Err(err).Msg("an error occurred while adding default orchestrator")
		}
	} else if err != nil {
		// any init errors in here are fatal
		log.Fatal().Err(err).Msg("an error occurred while adding default orchestrator")
	}

	var systemChecker model.Checker
	err = db.First(&systemChecker, "system = true").Error
	if errors.Is(err, gorm.ErrRecordNotFound) {
		systemChecker = model.Checker{
			Name:   "AIO built-in checker",
			Key:    checkerApi.GenerateUniqueKey(db),
			System: true,
		}
		if db.Create(&systemChecker).Error != nil {
			log.Fatal().Err(err).Msg("an error occurred while adding default checker")
		}
	} else if err != nil {
		// any init errors in here are fatal
		log.Fatal().Err(err).Msg("an error occurred while adding default checker")
	}

	return systemOrchestrator, systemChecker
}
