package tasks

import (
	"strconv"
	"time"

	"github.com/bartosz11/checkmate/common/config"
	"github.com/bartosz11/checkmate/common/database/model"
	"github.com/robfig/cron/v3"
	"github.com/rs/zerolog/log"
	"gorm.io/gorm"
)

// Just a random number
const leaderLockId = 245

func LeaderTakeoverTask(db *gorm.DB, orchestratorCfg *config.OrchestratorConfig, leader *bool, c *cron.Cron) func() {
	return func() {
		log.Trace().Msg("orchestrator: leader takeover attempt")

		if *leader {
			log.Trace().Msg("orchestrator: stopping leader takeover attempt early: already leader")
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

			// This setting is saved every time by the broadcast task
			var lastCheckTasksRun model.Setting
			db.First(&lastCheckTasksRun, "key = ?", "last-check-tasks-run")
			if lastCheckTasksRun.Value == nil || *lastCheckTasksRun.Value == "" {
				// Means this setting hasn't been created yet - the check tasks never ran
				runCheckTasksNowAndEvery60s(db, orchestratorCfg, c)
				return
			}
			lastRunTimestamp, err := strconv.ParseInt(*lastCheckTasksRun.Value, 10, 64)
			if err != nil {
				//Shouldn't happen
				log.Error().Err(err).Msg("orchestrator: failed to parse last check tasks run timestamp")
				//I guess we should fall back to running NOW and every 60s
				runCheckTasksNowAndEvery60s(db, orchestratorCfg, c)
				return
			}
			now := time.Now().UnixMilli()
			delta := now - lastRunTimestamp
			//Technically this shouldn't go below 0 without any tampering with the DB, but just to be safe
			// Also if it went negative, without this "handler" we'd be stuck without a check for 60,000+ ms
			// because even 60,000 - (-1) = 60000
			if delta >= 60000 || delta < 0 {
				runCheckTasksNowAndEvery60s(db, orchestratorCfg, c)
			} else {
				//delta is in range 0-59999 ms
				timeTillNext := 60000 - delta
				go time.AfterFunc(time.Duration(timeTillNext)*time.Millisecond, func() {
					//Delay execution and the scheduling by timeTillNext
					runCheckTasksNowAndEvery60s(db, orchestratorCfg, c)
				})
			}
		}
	}
}

func runCheckTasksNowAndEvery60s(db *gorm.DB, orchestratorCfg *config.OrchestratorConfig, c *cron.Cron) {
	go BroadcastTask(db, orchestratorCfg.MaxNetworkOverhead)()
	go CheckPushTargetsTask(db, orchestratorCfg.GracePeriod)()

	_, err := c.AddFunc("@every 60s", BroadcastTask(db, orchestratorCfg.MaxNetworkOverhead))
	if err != nil {
		log.Err(err).Msg("failed to schedule broadcast task")
	}
	_, err = c.AddFunc("@every 60s", CheckPushTargetsTask(db, orchestratorCfg.GracePeriod))
	if err != nil {
		log.Err(err).Msg("failed to schedule check push targets task")
	}
}
