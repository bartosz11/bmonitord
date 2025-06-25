package tasks

import (
	"bmonitord/config"
	"bmonitord/internal/database/model"
	"github.com/robfig/cron/v3"
	"github.com/rs/zerolog/log"
	"gorm.io/gorm"
	"strconv"
	"time"
)

// Just a random number
const leaderLockId = 245

func LeaderTakeoverTask(db *gorm.DB, orchestratorCfg *config.OrchestratorConfig, leader *bool, c *cron.Cron) func() {
	return func() {
		log.Trace().Msg("orchestrator: leader takeover attempt")

		if *leader {
			log.Debug().Msg("orchestrator: stopping leader takeover attempt early: already leader")
			return
		}

		sqlDB, err := db.DB()
		if err != nil {
			log.Error().Err(err).Msg("orchestrator: failed to get database connection")
		}

		res, err := sqlDB.Query("SELECT pg_try_advisory_lock($1)", leaderLockId)
		if err != nil {
			//If this fails, we can pretty much safely ignore it - we failed to acquire the lock and pretty much nothing else, I guess
			log.Debug().Err(err).Msg("orchestrator: failed to acquire leader lock")
		}
		defer res.Close()

		var success bool
		next := res.Next()
		if !next {
			log.Error().Msg("orchestrator: no result row in leader lock result set")
		}
		err = res.Scan(&success)
		if err != nil {
			log.Error().Err(err).Msg("orchestrator: failed to parse leader lock result")
			return
		}

		*leader = success
		if success {
			db.Model(&model.Orchestrator{}).Where("leader = ?", true).Update("leader", false)
			db.Model(&model.Orchestrator{}).Where("name = ?", orchestratorCfg.Name).Update("leader", true)
			log.Info().Msg("orchestrator: leader takeover succeeded")

			var lastBroadcastTaskRun model.Setting
			db.First(&lastBroadcastTaskRun, "key = ?", "last-broadcast-task-started")
			if lastBroadcastTaskRun.Value == nil || *lastBroadcastTaskRun.Value == "" {
				// Means this setting hasn't been created yet - the broadcast task never ran
				runBroadcastTaskNowAndEvery60s(db, orchestratorCfg, c)
				return
			}
			lastRunTimestamp, err := strconv.ParseInt(*lastBroadcastTaskRun.Value, 10, 64)
			if err != nil {
				//Shouldn't happen
				log.Error().Err(err).Msg("orchestrator: failed to parse last broadcast task run timestamp")
				//I guess we should fall back to running NOW and every 60s
				runBroadcastTaskNowAndEvery60s(db, orchestratorCfg, c)
				return
			}
			now := time.Now().UnixMilli()
			delta := now - lastRunTimestamp
			//Technically this shouldn't go below 0 without any tampering with the DB, but just to be safe
			// Also if it went negative, without this "handler" we'd be stuck without a check for 60,000+ ms
			// because even 60,000 - (-1) = 60000
			if delta >= 60000 || delta < 0 {
				runBroadcastTaskNowAndEvery60s(db, orchestratorCfg, c)
			} else {
				//delta is in range 0-59999 ms
				timeTillNext := 60000 - delta
				go time.AfterFunc(time.Duration(timeTillNext)*time.Millisecond, func() {
					//Delay execution and the scheduling by timeTillNext
					runBroadcastTaskNowAndEvery60s(db, orchestratorCfg, c)
				})
			}
		}
	}
}

func runBroadcastTaskNowAndEvery60s(db *gorm.DB, orchestratorCfg *config.OrchestratorConfig, c *cron.Cron) {
	go BroadcastTask(db, orchestratorCfg.MaxNetworkOverhead)()

	_, err := c.AddFunc("@every 60s", BroadcastTask(db, orchestratorCfg.MaxNetworkOverhead))
	if err != nil {
		log.Err(err).Msg("failed to schedule broadcast task")
	}
}
