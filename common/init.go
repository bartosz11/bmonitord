package common

import (
	"os"
	"strings"

	"github.com/bartosz11/checkmate/common/config"
	"github.com/gin-contrib/gzip"
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
	r := gin.Default()
	r.Use(func(c *gin.Context) {
		// we want Upgrade: websocket in any case specifically, hence EqualFold, but Connection can be something like Connection: Upgrade, keep-alive, so we have to watch out for that
		// WS connections need to be excluded from the gzip
		if strings.Contains(strings.ToLower(c.GetHeader("Connection")), "upgrade") && strings.EqualFold(c.GetHeader("Upgrade"), "websocket") {
			c.Next()
			return
		}

		gzip.Gzip(gzip.DefaultCompression, gzip.WithDecompressFn(gzip.DefaultDecompressHandle))(c)
	})
	return r
}

func StartRouter(router *gin.Engine, bind string) {
	err := router.Run(bind)
	if err != nil {
		log.Fatal().Err(err).Msg("Failed starting router!")
	}
}
