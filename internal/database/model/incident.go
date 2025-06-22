package model

import (
	"gorm.io/gorm"
	"time"
)

type Incident struct {
	gorm.Model
	Start    time.Time `gorm:"not null"`
	End      time.Time
	Duration time.Duration
	Ongoing  bool `gorm:"not null'"`
	TargetID uint `gorm:"not null"`
}
