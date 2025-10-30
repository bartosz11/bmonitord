package model

import "time"

type Agent struct {
	BaseModel
	TargetID         uint      `gorm:"not null" json:"targetId"`
	Key              string    `gorm:"not null" json:"key"`
	Installed        bool      `gorm:"not null;default:false" json:"installed"`
	LastDataReceived time.Time `json:"lastDataReceived"`
	HideIp           bool      `gorm:"not null;default:true" json:"HideIp"`
}
