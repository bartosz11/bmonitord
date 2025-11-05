package processing

import (
	"github.com/bartosz11/checkmate/common/database/model"
	"github.com/bartosz11/checkmate/common/notificationproviders"
	"gorm.io/gorm"
)

const NotificationTimeFormat = "2006-01-02 15:04:05"

func CheckLastIncident(target *model.Target, hb *model.Heartbeat, db *gorm.DB) *model.Incident {
	var lastIncident *model.Incident
	if hb.Status != target.LastStatus { // Process incidents on status changes, no need to check for UNKNOWN since we already saved the new status and re-fetched it
		if hb.Status == model.Up { // Change from DOWN to UP
			lastIncident = &target.Incidents[0] // Has to exist, incidents are always created on DOWN statuses
			lastIncident.Ongoing = false
			lastIncident.End = hb.Timestamp
			lastIncident.Duration = hb.Timestamp.Sub(lastIncident.Start) // end - start, I love Go types
			db.Save(lastIncident)
		} else { // change from UP to DOWN, check providers shouldn't return UNKNOWN
			incident := model.Incident{
				Start:    hb.Timestamp,
				Ongoing:  true,
				TargetID: target.ID,
			}
			lastIncident = &incident
			db.Save(&incident)
		}
	}
	return lastIncident
}

func ProcessAlarms(db *gorm.DB, target *model.Target, decisiveHeartbeat *model.Heartbeat, lastIncident *model.Incident, hbs *[]model.Heartbeat, hbsAll *[]model.Heartbeat, contentBuilder func(payload *notificationproviders.NotificationPayload, target *model.Target, alarm *model.Alarm, decisiveHeartbeat *model.Heartbeat, heartbeats *[]model.Heartbeat, allHbs *[]model.Heartbeat)) {
	for _, alarm := range target.Alarms {
		shouldNotify := false
		payload := notificationproviders.NotificationPayload{
			Target:            *target,
			DecisiveHeartbeat: *decisiveHeartbeat,
			Heartbeats:        *hbsAll,
		}
		if lastIncident != nil { // I actually experienced a panic because of a nil dereference, funny how I was sure it won't happen :)
			payload.Incident = *lastIncident
		}
		// determine if we should notify the user
		switch alarm.Type {
		case model.Unavailable:
			if alarm.Active && decisiveHeartbeat.Status == model.Up {
				alarm.Active = false
				shouldNotify = !alarm.Muted
			} else if !alarm.Active && decisiveHeartbeat.Status == model.Down {
				alarm.Active = true
				shouldNotify = !alarm.Muted
			}
		case model.Threshold:
			meta := model.AlarmThresholdFieldMetas[alarm.ThresholdField]
			thresholdExceeded := false
			for _, hb := range *hbs { // Check on all non-0 hbs for threshold exceeding, if it's exceeded anywhere, trigger the alarm
				value := meta.GetValueFunc(&hb, &alarm)
				if value > alarm.Threshold {
					thresholdExceeded = true
					break // No need to check further since at least one still exceeds the threshold
				}
			}

			if !alarm.Active && thresholdExceeded {
				alarm.Active = true
				shouldNotify = !alarm.Muted
			}
			if alarm.Active && !thresholdExceeded {
				alarm.Active = false
				shouldNotify = !alarm.Muted
			}
		}

		if shouldNotify {
			//build and send the notifications
			contentBuilder(&payload, target, &alarm, decisiveHeartbeat, hbs, hbsAll)
			for _, notification := range alarm.Notifications {
				go notificationproviders.NotificationProviders[notification.Type](payload, notification.Credentials)
			}
		}
		db.Save(&alarm) // We might want to do this in bulk/reintroduce the alarmChanged variable from the unfinished Java orchestrator
	}
}
