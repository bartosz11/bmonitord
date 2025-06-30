package model

import "gorm.io/gorm"

type User struct {
	gorm.Model
	Username      string         `gorm:"not null;unique"`
	Password      string         `gorm:"not null" json:"-"`
	Enabled       bool           `gorm:"not null;default:true"`
	Admin         bool           `gorm:"not null;default:false"`
	Targets       []Target       `gorm:"constraint:OnDelete:CASCADE;"`
	Notifications []Notification `gorm:"constraint:OnDelete:CASCADE;"`
	Sessions      []Session      `gorm:"constraint:OnDelete:CASCADE;"`
}
