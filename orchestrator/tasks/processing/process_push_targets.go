package processing

import (
	"fmt"
	"strconv"
	"strings"

	"github.com/bartosz11/checkmate/common/database/model"
	"github.com/bartosz11/checkmate/common/notificationproviders"
	"github.com/bartosz11/checkmate/orchestrator/helpers"
	"gorm.io/gorm"
)

func ProcessPushHeartbeat(target *model.Target, heartbeat *model.Heartbeat, db *gorm.DB, persist bool) {
	db.Preload("Incidents", func(db *gorm.DB) *gorm.DB {
		return db.Order("start desc") // Sort incidents by timestamps descending
	}).Preload("Alarms").Preload("Alarms.Notifications").First(target, "id = ?", target.ID)

	lastIncident := CheckLastIncident(target, heartbeat, db)

	hbs := []model.Heartbeat{*heartbeat}
	ProcessAlarms(db, target, heartbeat, lastIncident, &hbs, &hbs, pushNotificationContentBuilder)

	if persist {
		db.Save(heartbeat)
	}
}

func pushNotificationContentBuilder(payload *notificationproviders.NotificationPayload, target *model.Target, alarm *model.Alarm, decisiveHeartbeat *model.Heartbeat, _ *[]model.Heartbeat, _ *[]model.Heartbeat) {
	var bodyBuilder strings.Builder
	switch alarm.Type {
	case model.Unavailable:
		payload.Header = target.Name + " is now " + model.StatusToString(decisiveHeartbeat.Status) + "."
		if decisiveHeartbeat.Status == model.Up {
			humanDuration := helpers.HumanizeDuration(payload.Incident.Duration)
			bodyBuilder.WriteString(fmt.Sprintf("Duration: %s\n", humanDuration))
		}
	case model.Threshold:
		meta := model.AlarmThresholdFieldMetas[alarm.ThresholdField]
		if alarm.Active {
			payload.Header = target.Name + ": " + meta.FormattedName + " threshold exceeded"
		} else {
			payload.Header = target.Name + ": " + meta.FormattedName + " threshold no longer exceeded"
			// No duration info for Threshold alarms sadly (yet?)
		}
		valueStr := strconv.FormatFloat(meta.GetValueFunc(decisiveHeartbeat, alarm), 'f', -1, 64)
		bodyBuilder.WriteString(fmt.Sprintf("Current %s value: %s\n", meta.FormattedName, valueStr))
	}
	bodyBuilder.WriteString(fmt.Sprintf("Host: %s\n", target.GetHost()))
	bodyBuilder.WriteString(decisiveHeartbeat.Timestamp.Format(NotificationTimeFormat))
	payload.Body = bodyBuilder.String()
}
