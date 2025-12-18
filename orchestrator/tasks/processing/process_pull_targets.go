package processing

import (
	"fmt"
	"sort"
	"strconv"
	"strings"
	"time"

	"github.com/bartosz11/checkmate/common/database/model"
	"github.com/bartosz11/checkmate/common/notificationproviders"
	"github.com/bartosz11/checkmate/orchestrator/helpers"
	"gorm.io/gorm"
)

func ProcessPullHeartbeats(hbs []model.Heartbeat, task *Task, db *gorm.DB) {
	target := &task.Target

	missingHbs := createMissingHbs(task, target)
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
	db.Save(target)

	db.Preload("Alarms", "suspended = false").Preload("Alarms.Incidents", func(db *gorm.DB) *gorm.DB {
		return db.Order("start desc") // Sort incidents by timestamps descending
	}).Preload("Alarms.Notifications").First(target, "id = ?", target.ID)

	hbsAll := append(hbs, missingHbs...) // For notification body content purposes and saving

	assignCheckersToHeartbeats(hbsAll, db)

	ProcessAlarms(db, target, decisiveHeartbeat, &hbs, &hbsAll, pullNotificationContentBuilder)

	db.Save(&hbsAll)
}

func createMissingHbs(task *Task, target *model.Target) []model.Heartbeat {
	var missingHbs []model.Heartbeat
	for _, checker := range task.Checkers {
		if !helpers.Contains(task.CompleteCheckers, checker.ID) || helpers.Contains(task.UnreachableCheckers, checker.ID) {
			missingHbs = append(missingHbs, model.Heartbeat{
				CheckerID: &(checker.ID),
				TargetID:  target.ID,
				Status:    model.Unknown,
				Timestamp: time.Now(),
				Payload:   &(model.HeartbeatPayload{}),
			})
		}
	}
	return missingHbs
}

func assignCheckersToHeartbeats(hbsAll []model.Heartbeat, db *gorm.DB) {
	checkerIDs := make(map[uint]struct{}) // Get checkers assigned to heartbeats, also for notification purposes
	for _, hb := range hbsAll {
		checkerIDs[*hb.CheckerID] = struct{}{} // Map allows us to de-duplicate easily, keys can't repeat
	}
	ids := make([]uint, 0, len(checkerIDs))
	for id := range checkerIDs {
		ids = append(ids, id) // Make a slice out of all keys
	}

	var checkers []model.Checker
	db.Where("id IN ?", ids).Find(&checkers) // batch fetch

	checkerMap := make(map[uint]*model.Checker, len(checkers)) // id:checker map for fast lookup
	for _, checker := range checkers {
		c := checker
		checkerMap[checker.ID] = &c
	}

	for i := range hbsAll { // finally assign the checkers to heartbeats
		hbsAll[i].Checker = checkerMap[*hbsAll[i].CheckerID]
	}
}

func pullNotificationContentBuilder(payload *notificationproviders.NotificationPayload, target *model.Target, alarm *model.Alarm, decisiveHeartbeat *model.Heartbeat, _ *[]model.Heartbeat, allHbs *[]model.Heartbeat) {
	var bodyBuilder strings.Builder
	if !payload.Incident.Ongoing {
		humanDuration := helpers.HumanizeDuration(payload.Incident.Duration)
		bodyBuilder.WriteString(fmt.Sprintf("Duration: %s\n", humanDuration))
	}
	bodyBuilder.WriteString(fmt.Sprintf("Host: %s\n", target.GetHost()))
	bodyBuilder.WriteString(decisiveHeartbeat.Timestamp.Format(NotificationTimeFormat))
	bodyBuilder.WriteString("\n")

	switch alarm.Type {
	case model.Unavailable:
		payload.Header = target.Name + " is now " + model.StatusToString(decisiveHeartbeat.Status) + "."
		for i, heartbeat := range *allHbs {
			// It's nearly impossible for Checker to be nil here since this is pull not push
			bodyBuilder.WriteString(heartbeat.Checker.Name)
			bodyBuilder.WriteString(": ")
			bodyBuilder.WriteString(model.StatusToString(heartbeat.Status))
			if i != len(*allHbs)-1 {
				bodyBuilder.WriteString("\n")
			}
		}
	case model.Threshold:
		meta := model.AlarmThresholdFieldMetas[alarm.ThresholdField]
		if alarm.Triggered {
			payload.Header = target.Name + ": " + meta.FormattedName + " threshold exceeded"
		} else {
			payload.Header = target.Name + ": " + meta.FormattedName + " threshold no longer exceeded"
		}
		for i, heartbeat := range *allHbs {
			bodyBuilder.WriteString(heartbeat.Checker.Name)
			bodyBuilder.WriteString(": ")
			if heartbeat.Status == model.Unknown {
				bodyBuilder.WriteString("unknown")
			} else {
				bodyBuilder.WriteString(strconv.FormatFloat(meta.GetValueFunc(&(*allHbs)[i], alarm), 'f', -1, 64))
			}
			if i != len(*allHbs)-1 {
				bodyBuilder.WriteString("\n")
			}
		}
	}

	payload.Body = bodyBuilder.String()
}
