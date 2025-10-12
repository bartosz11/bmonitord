package model

import (
	"fmt"
	"time"
)

type Target struct {
	BaseModel
	Name        string         `gorm:"not null" json:"name"`
	ChecksUp    uint64         `json:"checksUp"`
	ChecksDown  uint64         `json:"checksDown"`
	MaxRetries  uint           `gorm:"not null" json:"maxRetries"`
	UsedRetries uint           `gorm:"not null;default:0" json:"usedRetries"`
	LastCheck   time.Time      `json:"lastCheck"`
	LastStatus  TargetStatus   `json:"lastStatus"`
	Type        TargetType     `gorm:"not null" json:"type"`
	Paused      bool           `gorm:"default:false" json:"paused"`
	Timeout     uint           `json:"timeout"`
	UserID      uint           `gorm:"not null" json:"userId"`
	Heartbeats  []Heartbeat    `gorm:"constraint:OnDelete:CASCADE;" json:"heartbeats"`
	Alarms      []Alarm        `gorm:"constraint:OnDelete:CASCADE;" json:"alarms"`
	Incidents   []Incident     `gorm:"constraint:OnDelete:CASCADE;" json:"incidents"`
	Checkers    []Checker      `gorm:"many2many:targets_checkers;constraint:OnDelete:CASCADE;" json:"checkers"`
	HTTPInfo    TargetHTTPInfo `gorm:"constraint:OnDelete:CASCADE;" json:"httpInfo"`
	PingInfo    TargetPingInfo `gorm:"constraint:OnDelete:CASCADE;" json:"pingInfo"`
}

type TargetType uint

const (
	PING TargetType = iota
	HTTP
	targetTypeMax // "sentinel" value
)

type TargetStatus uint

const (
	Up TargetStatus = iota
	Down
	Unknown
)

func StatusToString(status TargetStatus) string {
	switch status {
	case Up:
		return "UP"
	case Down:
		return "DOWN"
	case Unknown:
		return "UNKNOWN"
	}
	return "" // Literally impossible
}

func ValidateTargetType(tt TargetType) error {
	if tt >= targetTypeMax {
		return fmt.Errorf("invalid target type: %d", tt)
	}
	return nil
}
