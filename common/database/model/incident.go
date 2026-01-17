package model

import (
	"time"
)

type Incident struct {
	BaseModel
	Start    time.Time     `gorm:"not null" json:"start"`
	End      time.Time     `json:"end"`
	Duration time.Duration `json:"duration"`
	Ongoing  bool          `gorm:"not null'" json:"ongoing"`
	Cause    string        `json:"cause"`
	TargetID uint          `gorm:"not null" json:"targetId"`
	Target   Target        `gorm:"foreignKey:TargetID" json:"target"`
	AlarmID  uint          `gorm:"not null" json:"alarmId"`
	Alarm    Alarm         `gorm:"foreignKey:AlarmID" json:"alarm"`
}
