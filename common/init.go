package common

import (
	"os"

	"github.com/bartosz11/checkmate/common/config"
	"github.com/gin-gonic/gin"
	"github.com/rs/zerolog"
	"github.com/rs/zerolog/log"
)

// This file's purpose is to de-dupe some code fragments shared across entrypoints
// It can significantly simplify changing some behaviors of Gin/GORM/zerolog/...

func SetupLogging(cfg *config.Config) {
	if cfg.PrettyLogging {
		log.Logger = log.Output(zerolog.ConsoleWriter{Out: os.Stderr})
	}
	zerolog.SetGlobalLevel(zerolog.Level(cfg.LoggingLevel))
}

func SetupRouter(cfg *config.Config) *gin.Engine {
	if cfg.Production {
		gin.SetMode(gin.ReleaseMode)
	}

	return gin.Default()
}

func StartRouter(router *gin.Engine, bind string) {
	err := router.Run(bind)
	if err != nil {
		log.Fatal().Err(err).Msg("Failed starting router!")
	}
}
