package tasks

import (
	"bmonitord/internal/database/model"
	"bmonitord/internal/orchestrator/helpers"
	"encoding/json"
	"github.com/coder/websocket"
	"github.com/rs/zerolog/log"
	"gorm.io/gorm"
	"time"
)

var WSConnections = map[uint]*websocket.Conn{}

func BroadcastTask(db *gorm.DB, leader *bool, maxNetworkOverhead int) func() {
	return func() {
		if !*leader {
			log.Debug().Msg("orchestrator: broadcast task cancelled - not leader yet")
			return
		}

		log.Debug().Msg("orchestrator: broadcast task running")
		var targets []model.Target
		db.Joins("HTTPInfo").Joins("PingInfo").Preload("Checkers").Find(&targets, "paused = false")

		var unknownHbs []model.Heartbeat
		for _, target := range targets {
			reachedCheckers := 0
			processingTask := ProcessingTask{
				Target:              target,
				Checkers:            target.Checkers,
				UnreachableCheckers: []uint{},
				Heartbeats:          make(chan model.Heartbeat),
				CompleteCheckers:    []uint{},
				Timeout:             time.Duration(target.Timeout)*time.Second + time.Duration(maxNetworkOverhead)*time.Millisecond,
			}
			for _, checker := range target.Checkers {
				conn := WSConnections[checker.ID]
				if conn == nil {
					log.Debug().Int("checker", int(checker.ID)).Msg("orchestrator: checker unreachable")
					unknownHbs = append(unknownHbs, model.Heartbeat{
						Timestamp: time.Now(),
						Status:    2,
						TargetID:  target.ID,
						CheckerID: checker.ID,
					})
					processingTask.UnreachableCheckers = append(processingTask.UnreachableCheckers, checker.ID)
					continue
				}

				targetJSON, err := json.Marshal(target)
				if err != nil {
					log.Err(err).Msg("failed to marshal target")
					continue
				}
				helpers.SendJSON(conn, helpers.WebSocketMessage{Type: "check", Payload: targetJSON})

				reachedCheckers++
				log.Debug().Int("checker", int(checker.ID)).Msg("orchestrator: checker reached")
			}
			processingTask.ExpectedHeartbeats = reachedCheckers
			ProcessingTasks[target.ID] = &processingTask
			go StartProcessingTask(db, &processingTask)
		}
	}
}
