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
	Os         string    `json:"os"`
	Device     string    `json:"device"`
	Browser    string    `json:"browser"`
	UserID     uint      `gorm:"not null" json:"userId"`
	User       User      `gorm:"foreignKey:UserID" json:"user"`
}
