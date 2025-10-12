package model

type Notification struct {
	BaseModel
	Name        string           `gorm:"not null" json:"name"`
	Type        NotificationType `gorm:"not null" json:"type"`
	Credentials string           `gorm:"not null" json:"credentials"`
	UserID      uint             `gorm:"not null" json:"userId"`
	Alarms      []Alarm          `gorm:"many2many:alarms_notifications;constraint:OnDelete:CASCADE;" json:"alarms"`
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
