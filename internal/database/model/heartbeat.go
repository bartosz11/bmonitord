package model

import (
	"gorm.io/gorm"
	"time"
)

type Heartbeat struct {
	gorm.Model
	Latency   uint64
	Timestamp time.Time    `gorm:"not null"`
	Status    TargetStatus `gorm:"not null"`
	TargetID  uint         `gorm:"not null"`
	Target    Target       `gorm:"foreignKey:TargetID"`
	CheckerID uint         `gorm:"not null"`
	Checker   Checker      `gorm:"foreignKey:CheckerID"`
}
