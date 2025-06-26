package model

import (
	"gorm.io/gorm"
	"time"
)

type Session struct {
	gorm.Model
	ExpiresAt  time.Time `gorm:"not null"`
	LastActive time.Time
	UserAgent  string
	IpAddress  string
	UserID     uint `gorm:"not null"`
	User       User `gorm:"foreignKey:UserID"`
}
