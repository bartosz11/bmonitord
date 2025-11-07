package tasks

import (
	"time"

	"github.com/bartosz11/checkmate/common/database/model"
	"github.com/bartosz11/checkmate/orchestrator/tasks/processing"
	"github.com/rs/zerolog/log"
	"gorm.io/gorm"
)

func CheckPushTargetsTask(db *gorm.DB, gracePeriod int) func() {
	return func() {
		log.Trace().Msg("orchestrator: checking push targets")
		var thisOrchestrator model.Orchestrator
		// orchestrator has to be leader in order for this task to run and has to exist
		db.Find(&thisOrchestrator, "leader = true")

		// orchestrator is being updated every time it takes over leadership, we can use this time to determine if the "grace period" has passed
		if time.Since(thisOrchestrator.UpdatedAt).Minutes() < float64(gracePeriod) {
			log.Trace().Msg("orchestrator: stopping push targets check early - grace period isn't over yet")
			return
		}

		var pushTargets []model.Target
		db.Joins("Agent").Find(&pushTargets, "paused = false and type = 2")

		for _, target := range pushTargets {
			agent := &(target.Agent)
			if !agent.Installed {
				continue
			}

			target.LastCheck = time.Now()
			if time.Since(agent.LastDataReceived).Seconds() > float64(target.Timeout) {
				heartbeat := model.Heartbeat{
					// we set it like 2ms ago, good enough ig
					Timestamp: target.LastCheck,
					Status:    model.Down,
					TargetID:  target.ID,
					Target:    target,
					Payload:   &(model.HeartbeatPayload{}),
				}

				processing.ProcessPushHeartbeat(&target, &heartbeat, db, false)
				// The function above doesn't handle this because this behavior isn't supposed to happen in the collection endpoint and the function is meant to be reusable between the two
				target.ChecksDown++
				target.LastStatus = model.Down
			} else {
				target.ChecksUp++
				target.LastStatus = model.Up
			}
			db.Save(&target)
		}
	}
}
