package main

import (
	"github.com/bartosz11/checkmate/api"
	"github.com/bartosz11/checkmate/common"
	"github.com/bartosz11/checkmate/common/config"
	"github.com/bartosz11/checkmate/common/database"
	"github.com/bartosz11/checkmate/common/helpers"
)

// main serves as the entrypoint of the standalone API/Web image
func main() {
	cfg := config.LoadConfig()

	common.SetupLogging(&cfg)

	db := database.InitDatabase(&cfg.DatabaseConfig)

	router := common.SetupRouter(&cfg)

	helpers.InitEmail(&cfg.EmailConfig)

	api.StartAPI(db, router, cfg.Production, &cfg.APIConfig)

	common.StartRouter(router)
}
