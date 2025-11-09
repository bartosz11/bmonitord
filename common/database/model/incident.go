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
	TargetID uint          `gorm:"not null" json:"targetId"`
	Target   Target        `gorm:"foreignKey:TargetID" json:"target"`
}
