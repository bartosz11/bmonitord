package model

import (
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
	Pushbullet
	Email
	Gotify
	GenericWebhook
)
