package model

import (
	"time"
)

type Heartbeat struct {
	BaseModel
	Latency   uint64       `json:"latency"`
	Timestamp time.Time    `gorm:"not null" json:"timestamp"`
	Status    TargetStatus `gorm:"not null" json:"status"`
	TargetID  uint         `gorm:"not null" json:"targetId"`
	Target    Target       `gorm:"foreignKey:TargetID" json:"target"`
	CheckerID uint         `gorm:"not null" json:"checkerId"`
	Checker   Checker      `gorm:"foreignKey:CheckerID" json:"checker"`
}
