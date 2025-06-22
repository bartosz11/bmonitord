package model

import (
	"bmonitord/internal/orchestrator/providers"
	"gorm.io/gorm"
)

type Notification struct {
	gorm.Model
	Name        string           `gorm:"not null"`
	Type        NotificationType `gorm:"not null"`
	Credentials string           `gorm:"not null"`
	UserID      uint             `gorm:"not null"`
	Alarms      []Alarm          `gorm:"many2many:alarms_notifications;"`
}

type NotificationType uint

const (
	Discord NotificationType = iota
	Slack
	PushBullet
	Email
	Gotify
	GenericWebhook
)

var NotificationProviders = map[NotificationType]func(header string, body string, credentials string){
	Discord:    providers.SendDiscordNotification,
	Slack:      providers.SendSlackNotification,
	PushBullet: providers.SendPushBulletNotification,
	//TODO all other types
}
