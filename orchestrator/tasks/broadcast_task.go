package tasks

import (
	"encoding/json"
	"strconv"
	"sync"
	"time"

	"github.com/bartosz11/checkmate/common/database/model"
	"github.com/bartosz11/checkmate/orchestrator/helpers"
	"github.com/coder/websocket"
	"github.com/rs/zerolog/log"
	"gorm.io/gorm"
)

var WSConnections sync.Map

func BroadcastTask(db *gorm.DB, maxNetworkOverhead int) func() {
	return func() {
		log.Debug().Msg("orchestrator: broadcast task running")

		unixTimestamp := strconv.FormatInt(time.Now().UnixMilli(), 10)
		db.Save(&model.Setting{
			Key:   "last-broadcast-task-started",
			Value: &unixTimestamp,
		})

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
				load, ok := WSConnections.Load(checker.ID)
				if !ok {
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
				conn := load.(*websocket.Conn)

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
			ProcessingTasks.Store(target.ID, &processingTask)
			go StartProcessingTask(db, &processingTask)
		}
	}
}
