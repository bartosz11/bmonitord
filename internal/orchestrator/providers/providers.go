package providers

import "bmonitord/internal/database/model"

var NotificationProviders = map[model.NotificationType]func(payload NotificationPayload, credentials string){
	model.Discord:        SendDiscordNotification,
	model.Slack:          SendSlackNotification,
	model.Pushbullet:     SendPushBulletNotification,
	model.Email:          SendEmailNotification,
	model.Gotify:         SendGotifyNotification,
	model.GenericWebhook: SendGenericWebhookNotification,
}

type NotificationPayload struct {
	Header            string
	Body              string
	Target            model.Target
	Incident          model.Incident
	DecisiveHeartbeat model.Heartbeat
	Heartbeats        []model.Heartbeat
}
