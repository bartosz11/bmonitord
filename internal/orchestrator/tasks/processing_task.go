package tasks

import (
	"bmonitord/internal/database/model"
	"bmonitord/internal/orchestrator/helpers"
	"bmonitord/internal/orchestrator/providers"
	"gorm.io/gorm"
	"sort"
	"strconv"
	"strings"
	"time"
)

var ProcessingTasks = make(map[uint]*ProcessingTask)

type ProcessingTask struct {
	Target              model.Target
	DecisiveHeartbeat   model.Heartbeat
	Heartbeats          chan model.Heartbeat
	Checkers            []model.Checker // All checkers
	UnreachableCheckers []uint          // Checkers that already were unreachable when the task was broadcasted
	CompleteCheckers    []uint          // Checkers that sent the result in time
	ExpectedHeartbeats  int
	Timeout             time.Duration
}

func StartProcessingTask(db *gorm.DB, task *ProcessingTask) {
	defer delete(ProcessingTasks, task.Target.ID)

	timer := time.NewTimer(task.Timeout)
	buffer := make([]model.Heartbeat, 0, task.ExpectedHeartbeats)

	for {
		select {
		case hb, ok := <-task.Heartbeats:
			if !ok {
				processBuffer(buffer, task, db)
				return
			}
			buffer = append(buffer, hb)
			if len(buffer) >= task.ExpectedHeartbeats {
				processBuffer(buffer, task, db)
				return
			}
		case <-timer.C:
			processBuffer(buffer, task, db)
			return
		}
	}
}

func processBuffer(hbs []model.Heartbeat, task *ProcessingTask, db *gorm.DB) {
	var missingHbs []model.Heartbeat
	target := &task.Target
	for _, checker := range task.Checkers {
		if !helpers.Contains(task.CompleteCheckers, checker.ID) || helpers.Contains(task.UnreachableCheckers, checker.ID) {
			missingHbs = append(missingHbs, model.Heartbeat{
				CheckerID: checker.ID,
				TargetID:  target.ID,
				Status:    model.Unknown,
				Timestamp: time.Now(),
			})
		}
	}
	if len(hbs) == 0 {
		//No heartbeats that are meaningful, save the ones from "missing" checkers and stop processing early
		db.Save(&missingHbs)
		return
	}

	sort.Slice(hbs, func(i, j int) bool { // Sort by timestamp ascending
		return hbs[i].Timestamp.Before(hbs[j].Timestamp)
	})

	for _, hb := range hbs {
		if hb.Status == model.Down {
			//Take the first heartbeat with the "DOWN" status as decisive
			task.DecisiveHeartbeat = hb
			break
		}
	}
	//If there is no hb with "DOWN" status, take the first from the slice as decisive (has to be "UP" then)
	//Timestamp is a meaningful enough field to check if the struct hasn't been set yet
	if task.DecisiveHeartbeat.Timestamp.IsZero() {
		task.DecisiveHeartbeat = hbs[0]
	}
	decisiveHeartbeat := &task.DecisiveHeartbeat

	target.LastCheck = decisiveHeartbeat.Timestamp
	if decisiveHeartbeat.Status == model.Up {
		target.ChecksUp++
		target.UsedRetries = 0
		target.LastStatus = model.Up
	} else {
		target.UsedRetries++
		if target.UsedRetries <= target.MaxRetries {
			db.Save(target) // Save just the retries count if it hasn't been exceeded yet, stop further processing
			return
		} else {
			target.ChecksDown++
			target.LastStatus = model.Down
		}
	}
	db.Save(target)

	db.Preload("Incidents", func(db *gorm.DB) *gorm.DB {
		return db.Order("start desc") // Sort incidents by timestamps descending
	}).Preload("Alarms").Preload("Alarms.Notifications").First(target, "id = ?", target.ID)

	var lastIncident *model.Incident
	if decisiveHeartbeat.Status != target.LastStatus { // Process incidents on status changes, no need to check for UNKNOWN since we already saved the new status and re-fetched it
		if decisiveHeartbeat.Status == model.Up { // Change from DOWN to UP
			lastIncident = &target.Incidents[0] // Has to exist, incidents are always created on DOWN statuses
			lastIncident.Ongoing = false
			lastIncident.End = decisiveHeartbeat.Timestamp
			lastIncident.Duration = decisiveHeartbeat.Timestamp.Sub(lastIncident.Start) // end - start, I love Go types
			db.Save(lastIncident)
		} else { // change from UP to DOWN, check providers shouldn't return UNKNOWN
			incident := model.Incident{
				Start:    decisiveHeartbeat.Timestamp,
				Ongoing:  true,
				TargetID: target.ID,
			}
			lastIncident = &incident
			db.Save(&incident)
		}
	}

	hbsAll := append(hbs, missingHbs...) // For notification body content purposes and saving

	checkerIDs := make(map[uint]struct{}) // Get checkers assigned to heartbeats, also for notification purposes
	for _, hb := range hbsAll {
		checkerIDs[hb.CheckerID] = struct{}{} // Map allows us to de-duplicate easily, keys can't repeat
	}
	ids := make([]uint, 0, len(checkerIDs))
	for id := range checkerIDs {
		ids = append(ids, id) // Make a slice out of all keys
	}

	var checkers []model.Checker
	db.Where("id IN ?", ids).Find(&checkers) // batch fetch

	checkerMap := make(map[uint]model.Checker, len(checkers)) // id:checker map for fast lookup
	for _, checker := range checkers {
		checkerMap[checker.ID] = checker
	}

	for i := range hbsAll { // finally assign the checkers to heartbeats
		hbsAll[i].Checker = checkerMap[hbsAll[i].CheckerID]
	}

	for _, alarm := range target.Alarms {
		switch alarm.Type {
		case model.Unavailable:
			shouldNotify := false
			if alarm.Active && decisiveHeartbeat.Status == model.Up {
				alarm.Active = false
				shouldNotify = !alarm.Muted
			} else if !alarm.Active && decisiveHeartbeat.Status == model.Down {
				alarm.Active = true
				shouldNotify = !alarm.Muted
			}

			if shouldNotify {
				header := target.Name + " is now " + model.StatusToString(decisiveHeartbeat.Status) + "."
				var bodyBuilder strings.Builder
				for i, heartbeat := range hbsAll {
					bodyBuilder.WriteString(heartbeat.Checker.Name)
					bodyBuilder.WriteString(": ")
					bodyBuilder.WriteString(model.StatusToString(heartbeat.Status))
					if i != len(hbsAll)-1 {
						bodyBuilder.WriteString("\n")
					}
				}

				payload := providers.NotificationPayload{
					Header:            header,
					Body:              bodyBuilder.String(),
					Incident:          *lastIncident, // no "potential nil dereference" here, because we send notifications only if status changes, but then we also create/modify incidents
					Target:            *target,
					DecisiveHeartbeat: *decisiveHeartbeat,
					Heartbeats:        hbsAll,
				}

				for _, notification := range alarm.Notifications {
					go providers.NotificationProviders[notification.Type](payload, notification.Credentials)
				}
			}
			break
		case model.Threshold:
			meta := model.AlarmThresholdFieldMetas[alarm.ThresholdField]
			thresholdExceeded := false
			for _, hb := range hbs { // Check on all non-0 hbs for threshold exceeding, if it's exceeded anywhere, trigger the alarm
				value := meta.GetValueFunc(&hb)
				if value > alarm.Threshold {
					thresholdExceeded = true
					break // No need to check further since at least one still exceeds the threshold
				}
			}

			shouldNotify := false
			if !alarm.Active && thresholdExceeded {
				alarm.Active = true
				shouldNotify = !alarm.Muted
			}
			if alarm.Active && !thresholdExceeded {
				alarm.Active = false
				shouldNotify = !alarm.Muted
			}

			if shouldNotify {
				var header string
				if thresholdExceeded {
					header = target.Name + ": " + meta.FormattedName + " threshold exceeded"
				} else {
					header = target.Name + ": " + meta.FormattedName + " threshold no longer exceeded"
				}
				var bodyBuilder strings.Builder
				for i, heartbeat := range hbsAll {
					bodyBuilder.WriteString(heartbeat.Checker.Name)
					bodyBuilder.WriteString(": ")
					bodyBuilder.WriteString(strconv.FormatFloat(meta.GetValueFunc(&hbs[i]), 'f', -1, 64))
					if i != len(hbsAll)-1 {
						bodyBuilder.WriteString("\n")
					}
				}

				payload := providers.NotificationPayload{
					Header:            header,
					Body:              bodyBuilder.String(),
					Incident:          *lastIncident, // no "potential nil dereference" here, because we send notifications only if status changes, but then we also create/modify incidents
					Target:            *target,
					DecisiveHeartbeat: *decisiveHeartbeat,
					Heartbeats:        hbsAll,
				}
				for _, notification := range alarm.Notifications {
					go providers.NotificationProviders[notification.Type](payload, notification.Credentials)
				}
			}
			break
		}
		db.Save(&alarm) // We might want to do this in bulk/reintroduce the alarmChanged variable from the unfinished Java orchestrator
	}

	db.Save(&hbsAll)
}
