package tasks

import (
	"bmonitord/internal/database/model"
	"github.com/rs/zerolog/log"
	"gorm.io/gorm"
)

// Just a random number
const leaderLockId = 245

func LeaderTakeoverTask(db *gorm.DB, name string, leader *bool) func() {
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
			db.Model(&model.Orchestrator{}).Where("name = ?", name).Update("leader", true)
			log.Info().Msg("orchestrator: leader takeover succeeded")
		}

	}
}
