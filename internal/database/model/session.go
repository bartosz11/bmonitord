package model

import (
	"time"
)

type Session struct {
	BaseModel
	ExpiresAt  time.Time `gorm:"not null" json:"expiresAt"`
	LastActive time.Time `json:"lastActive"`
	UserAgent  string    `json:"userAgent"`
	IpAddress  string    `json:"ipAddress"`
	UserID     uint      `gorm:"not null" json:"userId"`
	User       User      `gorm:"foreignKey:UserID" json:"user"`
}
