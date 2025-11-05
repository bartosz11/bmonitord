package processing

import (
	"sync"
	"time"

	"github.com/bartosz11/checkmate/common/database/model"
	"gorm.io/gorm"
)

var Tasks sync.Map

type Task struct {
	Target              model.Target
	DecisiveHeartbeat   model.Heartbeat
	Heartbeats          chan model.Heartbeat
	Checkers            []model.Checker // All checkers
	UnreachableCheckers []uint          // Checkers that already were unreachable when the task was broadcasted
	CompleteCheckers    []uint          // Checkers that sent the result in time
	ExpectedHeartbeats  int
	Timeout             time.Duration
}

func StartProcessingTask(db *gorm.DB, task *Task) {
	defer Tasks.Delete(task.Target.ID)

	timer := time.NewTimer(task.Timeout)
	buffer := make([]model.Heartbeat, 0, task.ExpectedHeartbeats)

	for {
		select {
		case hb, ok := <-task.Heartbeats:
			if !ok {
				ProcessPullHeartbeats(buffer, task, db)
				return
			}
			buffer = append(buffer, hb)
			if len(buffer) >= task.ExpectedHeartbeats {
				ProcessPullHeartbeats(buffer, task, db)
				return
			}
		case <-timer.C:
			ProcessPullHeartbeats(buffer, task, db)
			return
		}
	}
}
