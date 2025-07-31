package orchestrator

import (
	"github.com/bartosz11/checkmate/config"
	"github.com/bartosz11/checkmate/internal/orchestrator/handlers"
	"github.com/bartosz11/checkmate/internal/orchestrator/tasks"
	"github.com/gin-gonic/gin"
	"github.com/robfig/cron/v3"
	"github.com/rs/zerolog/log"
	"gorm.io/gorm"
)

var leader = false

func StartOrchestrator(orchestratorCfg *config.OrchestratorConfig, db *gorm.DB, router *gin.Engine) {
	router.GET("/orchestrator/ws", handlers.HandleNewWSConnection(db, &leader))

	c := cron.New()

	//run now and every 5s
	tasks.LeaderTakeoverTask(db, orchestratorCfg, &leader, c)()
	_, err := c.AddFunc("@every 5s", tasks.LeaderTakeoverTask(db, orchestratorCfg, &leader, c))
	if err != nil {
		log.Fatal().Err(err).Msg("orchestrator: failed to schedule leader takeover task")
	}

	c.Start()
	log.Info().Msg("orchestrator: started")
}
