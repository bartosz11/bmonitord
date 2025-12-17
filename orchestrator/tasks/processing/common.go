package processing

import (
	"github.com/bartosz11/checkmate/common/database/model"
	"github.com/bartosz11/checkmate/common/notificationproviders"
	"gorm.io/gorm"
)

const NotificationTimeFormat = "2006-01-02 15:04:05"

func ProcessAlarms(db *gorm.DB, target *model.Target, decisiveHeartbeat *model.Heartbeat, hbs *[]model.Heartbeat, hbsAll *[]model.Heartbeat, contentBuilder func(payload *notificationproviders.NotificationPayload, target *model.Target, alarm *model.Alarm, decisiveHeartbeat *model.Heartbeat, heartbeats *[]model.Heartbeat, allHbs *[]model.Heartbeat)) {
	targetStatus := model.Up

	// target.Alarms shouldn't contain alarms that are suspended
	for _, alarm := range target.Alarms {
		triggeredBefore := alarm.Triggered

		conditionMet := false
		switch alarm.Type {
		case model.Unavailable:
			conditionMet = decisiveHeartbeat.Status == model.Down
		case model.Threshold:
			meta := model.AlarmThresholdFieldMetas[alarm.ThresholdField]
			for _, hb := range *hbs { // Check on all non-0 hbs for threshold exceeding, if it's exceeded anywhere, trigger the alarm
				value := meta.GetValueFunc(&hb, &alarm)
				if value > alarm.Threshold {
					conditionMet = true
					break // No need to check further since at least one still exceeds the threshold
				}
			}
		}

		if conditionMet {
			alarm.UsedRetries++
			if alarm.UsedRetries <= alarm.MaxRetries {
				// the retry stage is supposed to be a place where the alarm is not triggered yet
				alarm.Triggered = false
				// "down" is the highest status in the "hierarchy" - down -> unknown -> alarm
				// because "something has failed definitely" -> "something might be failing" -> "everything's ok"
				// this check is performed so alarms don't overwrite each other's result
				if targetStatus != model.Down {
					targetStatus = model.Unknown
				}
			} else {
				alarm.Triggered = true
				// "down' is the highest status in the "hierarchy", we can safely overwrite it over and over
				targetStatus = model.Down
			}
		} else {
			alarm.UsedRetries = 0
			alarm.Triggered = false
			// "up" is the default value of targetStatus - if no other alarm will overwrite it because of it's "conclusions", it's gonna remain as it is now
		}

		shouldNotify := false
		var lastIncident model.Incident

		if triggeredBefore != alarm.Triggered {
			alarm.TriggeredStateChangedAt = decisiveHeartbeat.Timestamp
			shouldNotify = !alarm.Muted

			if alarm.Triggered { // changed from "up" to "down"
				lastIncident = model.Incident{
					Start:    decisiveHeartbeat.Timestamp,
					Ongoing:  true,
					TargetID: target.ID,
					AlarmID:  alarm.ID,
				}
				db.Save(&lastIncident)
			} else { // changed from "down" to "up"
				lastIncident = alarm.Incidents[0] // Has to exist, incidents are always created on DOWN statuses
				lastIncident.Ongoing = false
				lastIncident.End = decisiveHeartbeat.Timestamp
				lastIncident.Duration = decisiveHeartbeat.Timestamp.Sub(lastIncident.Start) // end - start, I love Go types
				db.Save(&lastIncident)
			}
		}

		if shouldNotify {
			payload := notificationproviders.NotificationPayload{
				Target:            *target,
				DecisiveHeartbeat: *decisiveHeartbeat,
				Heartbeats:        *hbsAll,
				Incident:          lastIncident,
			}

			//build and send the notifications
			contentBuilder(&payload, target, &alarm, decisiveHeartbeat, hbs, hbsAll)
			for _, notification := range alarm.Notifications {
				go notificationproviders.NotificationProviders[notification.Type](payload, notification.Credentials)
			}
		}
		db.Save(&alarm) // We might want to do this in bulk/reintroduce the alarmChanged variable from the unfinished Java orchestrator
	}

	target.LastStatus = targetStatus
	if targetStatus == model.Up {
		target.ChecksUp++
	} else if targetStatus == model.Down {
		target.ChecksDown++
	}
	db.Save(&target)
}
