package model

import "gorm.io/gorm"

type User struct {
	gorm.Model
	Username      string `gorm:"not null;unique"`
	Password      string `gorm:"not null"`
	Enabled       bool   `gorm:"not null;default:true"`
	Admin         bool   `gorm:"not null;default:false"`
	Targets       []Target
	Notifications []Notification
}
