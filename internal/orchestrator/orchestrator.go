package orchestrator

import (
	"bmonitord/config"
	"bmonitord/internal/orchestrator/handlers"
	"bmonitord/internal/orchestrator/tasks"
	"github.com/gin-gonic/gin"
	"github.com/robfig/cron/v3"
	"github.com/rs/zerolog/log"
	"gorm.io/gorm"
)

var leader = false

func StartOrchestrator(cfg config.Config, db *gorm.DB, router *gin.Engine) {
	orchestratorCfg := cfg.OrchestratorConfig

	router.GET("/orchestrator/ws", handlers.HandleNewWSConnection(db, &leader))

	c := cron.New()
	//TODO: move the scheduling of broadcast task to leader takeover task, when we become leader we run the task once and then every 60s
	_, err := c.AddFunc("@every 5s", tasks.LeaderTakeoverTask(db, orchestratorCfg.Name, &leader))
	tasks.LeaderTakeoverTask(db, orchestratorCfg.Name, &leader)
	if err != nil {
		log.Fatal().Err(err).Msg("orchestrator: failed to schedule leader takeover task")
	}
	_, err = c.AddFunc("@every 60s", tasks.BroadcastTask(db, &leader, cfg.OrchestratorConfig.MaxNetworkOverhead))
	if err != nil {
		log.Fatal().Err(err).Msg("orchestrator: failed to schedule broadcast task")
	}
	c.Start()
	log.Info().Msg("orchestrator: started")
}
